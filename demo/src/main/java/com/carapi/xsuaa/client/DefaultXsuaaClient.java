package com.carapi.xsuaa.client;

import com.carapi.xsuaa.exception.XsuaaClientException;
import com.sap.cloud.security.xsuaa.client.ClientCredentials;
import com.sap.cloud.security.xsuaa.client.DefaultOAuth2TokenService;
import com.sap.cloud.security.xsuaa.client.XsuaaDefaultEndpoints;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;

import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultXsuaaClient implements XsuaaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultXsuaaClient.class);
    private static final String CLIENT_CREDENTIALS_TOKEN_EXCEPTION_MESSAGE = "Couldn't fetch access token from destination";
    private static final String USER_TOKEN_EXCEPTION_MESSAGE = "Couldn't fetch user exchange access token";

    private final String xsuaaUri;

    public DefaultXsuaaClient(String xsuaaUri) {
        this.xsuaaUri = xsuaaUri;
    }

    @Override
    public String fetchClientCredentialsToken(String clientId, String clientCredentials) throws XsuaaClientException {
        try {
            return createXsuaaTokenFlows(clientId, clientCredentials).clientCredentialsTokenFlow().execute().getAccessToken();
        } catch (TokenFlowException e) {
            LOGGER.error(CLIENT_CREDENTIALS_TOKEN_EXCEPTION_MESSAGE, e);
            throw new XsuaaClientException(CLIENT_CREDENTIALS_TOKEN_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public String fetchUserToken(String clientId, String clientSecret, String tokenValue) throws XsuaaClientException {
        try {
            return createXsuaaTokenFlows(clientId, clientSecret).userTokenFlow().token(tokenValue).execute().getAccessToken();
        } catch (TokenFlowException e) {
            LOGGER.error(USER_TOKEN_EXCEPTION_MESSAGE, e);
            throw new XsuaaClientException(USER_TOKEN_EXCEPTION_MESSAGE, e);
        }
    }

    private XsuaaTokenFlows createXsuaaTokenFlows(String clientId, String clientSecret) {
        return new XsuaaTokenFlows(new DefaultOAuth2TokenService(), new XsuaaDefaultEndpoints(xsuaaUri),
                new ClientCredentials(clientId, clientSecret));
    }

}
