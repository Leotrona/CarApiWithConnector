package com.carapi.car;

import com.carapi.car.destination.client.DestinationClient;
import com.carapi.car.destination.exception.DestinationClientException;
import com.carapi.car.destination.exception.DestinationResponseException;
import com.carapi.xsuaa.client.DefaultXsuaaClient;
import com.carapi.xsuaa.client.XsuaaClient;
import com.carapi.xsuaa.exception.XsuaaClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static com.carapi.costants.Environment.getConnectivityCredentials;
import static com.carapi.costants.Environment.getConnectivityProxyAddress;
import static com.carapi.costants.Environment.getXsuaaUrl;

@RequestScope
@RestController
@RequestMapping(path = "api/v1/cars")
public class CarController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarController.class);

    private final ServiceCredentials connectivityCredentials;
    private final HttpClient onPremiseClient;
    private final DestinationClient destinationServiceClient;
    private final XsuaaClient xsuaaTokenClient;

    public CarController(ServiceCredentials connectivityCredentials,
                         HttpClient onPremiseClient, DestinationClient destinationClient, XsuaaClient xsuaaTokenClient) {
        this.connectivityCredentials = connectivityCredentials;
        this.onPremiseClient = onPremiseClient;
        this.destinationServiceClient = destinationClient;
        this.xsuaaTokenClient = xsuaaTokenClient;
    }

    public CarController() {
        this(getConnectivityCredentials(),
                HttpClient.newBuilder()
                        .proxy(ProxySelector.of(new InetSocketAddress(getConnectivityProxyAddress().getProxyHost(),
                                getConnectivityProxyAddress().getProxyPort())))
                        .build(),
                new DestinationClient(),
                new DefaultXsuaaClient(getXsuaaUrl()));
    }

    @RequestMapping
    public String processRequest(HttpServletRequest httpServletRequest) {
        return executeRequestToOnPremiseDestination(httpServletRequest).body();
    }

    @RequestMapping(path = "{carId}")
    public String proccesSingleCarRequest(HttpServletRequest httpServletRequest) {
        return executeRequestToOnPremiseDestination(httpServletRequest).body();
    }

    private HttpResponse<String> executeRequestToOnPremiseDestination(HttpServletRequest httpServletRequest) {
        LOGGER.info("AUTHORIZATION HEADER " + httpServletRequest.getHeader("authorization"));
        URL onPremiseUrl = createOnPremiseURL(httpServletRequest.getRequestURI());
        HttpRequest request = createRequestToOnPremiseDestination(httpServletRequest, onPremiseUrl);
        try {
            return onPremiseClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            LOGGER.error("Couldn't execute request to the destination ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private URL createOnPremiseURL(String requestUri) {
        try {
            return new URL(getOnPremiseDestinationUrl() + requestUri);
        } catch (MalformedURLException e) {
            LOGGER.error("On premise destination url creation failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getOnPremiseDestinationUrl() {
        try {
            return destinationServiceClient.getOnPremiseDestinationUrl();
        } catch (DestinationResponseException | DestinationClientException e) {
            LOGGER.error("Couldnt' get on premise destination url", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpRequest createRequestToOnPremiseDestination(HttpServletRequest httpServletRequest, URL url) {
        String clientRequestBody;
        try {
            clientRequestBody = new String(httpServletRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Couldn't get the client request body properly", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String userExchangeAccessToken;
        try {
            userExchangeAccessToken = getUserExchangeAccessToken(httpServletRequest.getHeader("authorization"));
        } catch (XsuaaClientException e) {
            LOGGER.error("Couldn't fetch user exchange access token", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return HttpRequest.newBuilder().uri(URI.create(String.valueOf(url)))
                .headers("Proxy-Authorization", "Bearer " + userExchangeAccessToken)
                .headers("Content-Type", "application/json")
                .method(httpServletRequest.getMethod(), HttpRequest.BodyPublishers.ofString(clientRequestBody))
                .build();
    }

    private String getUserExchangeAccessToken(String userToken) throws XsuaaClientException {
        String tokenValue = userToken.split(" ")[1];

        return xsuaaTokenClient.fetchUserToken(connectivityCredentials.getClientId(),
                connectivityCredentials.getClientSecret(), tokenValue);
    }
}
