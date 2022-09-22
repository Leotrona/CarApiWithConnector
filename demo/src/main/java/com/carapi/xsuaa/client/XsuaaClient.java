package com.carapi.xsuaa.client;

import com.carapi.xsuaa.exception.XsuaaClientException;

public interface XsuaaClient {

    /**
     * Fetches client credentials access token which we use to send request to the destination and get the
     * destination configuration json.
     *
     * @param clientId     The client id which we use to fetch the access token.
     * @param clientSecret The client secret which we use to fetch the access token.
     * @throws XsuaaClientException if we didn't manage to fetch client credentials token due to TokenFlowException.
     */
    String fetchClientCredentialsToken(String clientId, String clientSecret) throws XsuaaClientException;

    /**
     * Fetches client user token which we use for the proxy-authorization header in the request to the on premise.
     *
     * @param clientId            The client id which we use to fetch the user token.
     * @param clientSecret        The client secret which we use to fetch the user token.
     * @param technicalTokenValue The authorization header value of the user request.
     * @throws XsuaaClientException if we didn't manage to fetch user token due to TokenFlowException.
     */
    String fetchUserToken(String clientId, String clientSecret, String technicalTokenValue) throws XsuaaClientException;

}
