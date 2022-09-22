package com.carapi.car;

import com.carapi.car.destination.client.DestinationClient;
import com.carapi.car.destination.exception.DestinationClientException;
import com.carapi.car.destination.exception.DestinationResponseException;
import com.carapi.xsuaa.client.DefaultXsuaaClient;
import com.carapi.xsuaa.exception.XsuaaClientException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

@RunWith(MockitoJUnitRunner.class)
public class CarControllerTest {

    private static final String CARS_RESOURCE_PATH = "/api/v1/cars";
    private static final String SINGLE_CAR_PATH = CARS_RESOURCE_PATH + "/1";
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CLIENT_SECRET = "CLIENT_SECRET";
    private static final String TOKEN_VALUE = "someAuthorization";
    private static final String USER_EXCHANGE_TOKEN = "CLIENT_TOKEN";
    private static final String TEST_URL = "http://someUrl.com";
    private static MockHttpServletRequest TEST_GET_SINGLE_CAR = new MockHttpServletRequest(GET.toString(), SINGLE_CAR_PATH);

    @Mock
    private ServiceCredentials connectivityServiceCredentialsMock;

    @Mock
    private HttpClient httpClientMock;

    @Mock
    private DestinationClient destinationServiceCredentialsMock;

    @Mock
    private DefaultXsuaaClient xsuaaClientMock;

    @InjectMocks
    private CarController carController;

    @Mock
    private HttpResponse httpResponseMock;

    @Before
    public void setUp() {
        TEST_GET_SINGLE_CAR.addHeader("authorization", "Bearer " + TOKEN_VALUE);
        when(connectivityServiceCredentialsMock.getClientId()).thenReturn(CLIENT_ID);
        when(connectivityServiceCredentialsMock.getClientSecret()).thenReturn(CLIENT_SECRET);
    }

    @Test
    public void testGetSingleCarSuccess() throws Exception {
        when(destinationServiceCredentialsMock.getOnPremiseDestinationUrl()).thenReturn(TEST_URL);
        when(xsuaaClientMock.fetchUserToken(CLIENT_ID, CLIENT_SECRET, TOKEN_VALUE)).thenReturn(USER_EXCHANGE_TOKEN);
        when(httpClientMock.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponseMock);
        when(httpResponseMock.body()).thenReturn("Successfully received response");
        Assert.assertEquals("Successfully received response", carController.processRequest(TEST_GET_SINGLE_CAR));
    }

    @Test(expected = ResponseStatusException.class)
    public void testGetSingleCarUserTokenFail() throws Exception {
        when(destinationServiceCredentialsMock.getOnPremiseDestinationUrl()).thenReturn(TEST_URL);
        when(xsuaaClientMock.fetchUserToken(CLIENT_ID, CLIENT_SECRET, TOKEN_VALUE)).thenThrow(XsuaaClientException.class);
        carController.processRequest(TEST_GET_SINGLE_CAR);
    }

    @Test(expected = ResponseStatusException.class)
    public void testGetSingleCarSendRequestFail() throws Exception {
        when(destinationServiceCredentialsMock.getOnPremiseDestinationUrl()).thenReturn(TEST_URL);
        when(xsuaaClientMock.fetchUserToken(CLIENT_ID, CLIENT_SECRET, TOKEN_VALUE)).thenReturn(USER_EXCHANGE_TOKEN);
        when(httpClientMock.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(IOException.class);
        carController.processRequest(TEST_GET_SINGLE_CAR);
    }

    @Test(expected = ResponseStatusException.class)
    public void testGetSingleCarDestinationUrlFail() throws DestinationClientException, DestinationResponseException {
        when(destinationServiceCredentialsMock.getOnPremiseDestinationUrl()).thenThrow(DestinationClientException.class);
        carController.processRequest(TEST_GET_SINGLE_CAR);
    }
}
