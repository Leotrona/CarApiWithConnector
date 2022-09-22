package com.carapi.xsuaa.client;

import com.carapi.xsuaa.exception.XsuaaClientException;
import org.junit.Before;
import org.junit.Test;

public class DefaultXsuaaClientTest {

    private static final String RANDOM_ID = "RANDOMID";
    private static final String RANDOM_SECRET = "RANDOMSECRET";
    private static final String RANDOM_TOKEN_VALUE = "RANDOMTOKENVALUE";
    private static final String XSUAA_URL = "https://b208bdfctrial.authentication.us10.hana.ondemand.com";
    private static final String DESTINATION_CLIENT_ID = "sb-clone9eb08b5045e44bae8d989ed043e00d35!b55669|destination-xsappname!b62";
    private static final String DESTINATION_CLIENT_SECRET = "88a3543f-884f-4d8f-af0d-605c714d88ac$eQJatifqc9lq997zWKLReGhhx0NTVHWG0ei5g2NPo6w=";

    private DefaultXsuaaClient xsuaaClient;

    @Before
    public void setUp() {
        xsuaaClient = new DefaultXsuaaClient(XSUAA_URL);
    }

    @Test(expected = XsuaaClientException.class)
    public void testFetchUserTokenFail() throws XsuaaClientException {
        xsuaaClient.fetchUserToken(RANDOM_ID, RANDOM_SECRET, RANDOM_TOKEN_VALUE);
    }

    @Test(expected = XsuaaClientException.class)
    public void testFetchClientCredentialsTokenFail() throws XsuaaClientException {
        xsuaaClient.fetchClientCredentialsToken(RANDOM_ID, RANDOM_SECRET);
    }

    @Test
    public void testFetchClientCredentialsSuccess() throws XsuaaClientException {
        String clientToken = xsuaaClient.fetchClientCredentialsToken(DESTINATION_CLIENT_ID, DESTINATION_CLIENT_SECRET);
        System.out.println("TOKEN " + clientToken);
    }
}
