package com.carapi.car;

public class ConnectivityProxyAddress {

    private final String connProxyHost;
    private final int connProxyPort;

    public ConnectivityProxyAddress(String connProxyHost, int connProxyPort) {
        this.connProxyHost = connProxyHost;
        this.connProxyPort = connProxyPort;
    }

    public String getProxyHost() {
        return connProxyHost;
    }

    public int getProxyPort() {
        return connProxyPort;
    }
}
