package com.carapi.car.destination.client;

import com.carapi.car.ServiceCredentials;
import com.carapi.car.destination.exception.DestinationClientException;
import com.carapi.car.destination.exception.DestinationResponseException;
import com.carapi.xsuaa.client.DefaultXsuaaClient;
import com.carapi.xsuaa.client.XsuaaClient;
import com.carapi.xsuaa.exception.XsuaaClientException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.carapi.costants.Environment.getDestinationCredentials;
import static com.carapi.costants.Environment.getXsuaaUrl;

public class DestinationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DestinationClient.class);
    private static final String NO_RESPONSE_FROM_DESTINATION = "Couldn't get response from destination";
    private static final String CLIENT_CREDENTIALS_TOKEN_EXCEPTION_MESSAGE = "Couldn't fetch clientCredentialsToken";
    private static final String DESTINATION_URI_EXCEPTION_MESSAGE = "Couldn't get the destination uri from the credentials";

    private final ServiceCredentials destinationServiceCredentials;
    private static final String DESTINATION_NAME = "carsDestinationService";
    private final HttpClient destinationHttpClient;
    private final XsuaaClient xsuaaTokenClient;

    public DestinationClient(XsuaaClient xsuaaClient, ServiceCredentials serviceCredentials, HttpClient httpClient) {
        this.xsuaaTokenClient = xsuaaClient;
        this.destinationServiceCredentials = serviceCredentials;
        this.destinationHttpClient = httpClient;
    }

    public DestinationClient() {
        this.xsuaaTokenClient = new DefaultXsuaaClient(getXsuaaUrl());
        this.destinationServiceCredentials = getDestinationCredentials();
        this.destinationHttpClient = HttpClient.newHttpClient();
    }

    public String getOnPremiseDestinationUrl() throws DestinationClientException, DestinationResponseException {
        HttpRequest requestToDest = HttpRequest.newBuilder().uri(createDestinationURI())
                .headers("Authorization", "Bearer " + getClientCredentialsToken())
                .GET()
                .build();

        try {
            HttpResponse<String> responseDest = destinationHttpClient.send(requestToDest, HttpResponse.BodyHandlers.ofString());
            if(responseDest.statusCode() != 200) {
                throw new DestinationResponseException("Bad response from destination");
            }
            return new JSONObject(responseDest.body()).getJSONObject("destinationConfiguration").getString("URL");
        } catch (InterruptedException | IOException e) {
            LOGGER.error(NO_RESPONSE_FROM_DESTINATION, e);
            throw new DestinationClientException(NO_RESPONSE_FROM_DESTINATION, e);
        }
    }

    private String getClientCredentialsToken() throws DestinationClientException {
        try {
            return  xsuaaTokenClient.fetchClientCredentialsToken(destinationServiceCredentials.getClientId(),
                    destinationServiceCredentials.getClientSecret());
        } catch (XsuaaClientException e) {
            LOGGER.error(CLIENT_CREDENTIALS_TOKEN_EXCEPTION_MESSAGE, e);
            throw new DestinationClientException(CLIENT_CREDENTIALS_TOKEN_EXCEPTION_MESSAGE, e);
        }
    }

    private URI createDestinationURI() throws DestinationClientException {
        URI destinationUri;
        try {
            destinationUri = new URI(destinationServiceCredentials.getUrl());
        } catch (URISyntaxException e) {
            LOGGER.error(DESTINATION_URI_EXCEPTION_MESSAGE, e);
            throw new DestinationClientException(DESTINATION_URI_EXCEPTION_MESSAGE, e);
        }
        return URI.create(destinationUri + "/destination-configuration/v1/destinations/" + DESTINATION_NAME);
    }
}
