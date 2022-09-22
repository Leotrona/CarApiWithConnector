package com.carapi.car;

public class ServiceCredentials {

    private final String clientId;
    private final String clientSecret;
    private final String url;

    public ServiceCredentials(String clientId, String clientSecret, String url) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.url = url;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getUrl() {
        return url;
    }
}
