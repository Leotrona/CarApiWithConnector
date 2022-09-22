package com.carapi.car.destination.client;

import com.carapi.car.ServiceCredentials;
import com.carapi.car.destination.exception.DestinationClientException;
import com.carapi.car.destination.exception.DestinationResponseException;
import com.carapi.xsuaa.client.DefaultXsuaaClient;
import com.carapi.xsuaa.exception.XsuaaClientException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DestinationClientTest {
    private static final String DESTINATION_CLIENT_ID = "clientid";
    private static final String DESTINATION_CLIENT_SECRET = "clientsecret";
    private static final String CORRECT_TOKEN = "correctToken";
    private static final String DESTINATION_URL = "http://correctUrl.com";
    private static final String DESTINATION_URI = DESTINATION_URL + "/destination-configuration/v1/destinations/carsDestinationService";
    private HttpRequest TEST_GET = HttpRequest.newBuilder().uri(URI.create(DESTINATION_URI))
            .headers("Authorization", "Bearer " + CORRECT_TOKEN)
            .GET()
            .build();

    @Mock
    private DefaultXsuaaClient xsuaaClientMock;

    @Mock
    private ServiceCredentials serviceCredentialsMock;

    @Mock
    private HttpClient httpClientMock;

    @InjectMocks
    private DestinationClient destinationClient;

    @Mock
    private HttpResponse httpResponseMock;

    @Before
    public void setup() throws XsuaaClientException {
        when(serviceCredentialsMock.getClientId()).thenReturn(DESTINATION_CLIENT_ID);
        when(serviceCredentialsMock.getClientSecret()).thenReturn(DESTINATION_CLIENT_SECRET);
        when(serviceCredentialsMock.getUrl()).thenReturn(DESTINATION_URL);
        when(xsuaaClientMock.fetchClientCredentialsToken(DESTINATION_CLIENT_ID, DESTINATION_CLIENT_SECRET)).thenReturn(CORRECT_TOKEN);
    }

    @Test
    public void testGetOnPremiseDestinationUrlSuccess() throws Exception {
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("{  \n" +
                "    \"destinationConfiguration\": {  \n" +
                "        \"URL\":       \"http://correct.com\",   \n" +
                "    }  \n" +
                "}  ");

        when(httpClientMock.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponseMock);
        assertEquals("http://correct.com", destinationClient.getOnPremiseDestinationUrl());
    }

    @Test(expected = DestinationClientException.class)
    public void testGetOnPremiseDestinationUrlHttpClientFail() throws Exception {
        when(httpClientMock.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(new IOException());
        destinationClient.getOnPremiseDestinationUrl();
    }

    @Test(expected = DestinationResponseException.class)
    public void testGetOnPremiseDestinationUrlBadResponseFail()throws Exception {
        when(httpClientMock.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponseMock);
        when(httpResponseMock.statusCode()).thenReturn(500);
        destinationClient.getOnPremiseDestinationUrl();
    }
}
