package com.carapi.costants;

import com.carapi.car.ConnectivityProxyAddress;
import com.carapi.car.ServiceCredentials;
import org.json.JSONArray;
import org.json.JSONObject;

public class Environment {
    private static JSONObject JSON_OBJECT = new JSONObject(System.getenv("VCAP_SERVICES"));

    public static final String CLIENT_ID = "clientid";
    public static final String CLIENT_SECRET = "clientsecret";
    public static final String DESTINATION_SERVICE_NAME = "destination";
    public static final String CONNECTIVITY_SERVICE_NAME = "connectivity";

    public static ServiceCredentials getConnectivityCredentials() {
        JSONObject credentialsJson =  getCredentialsJson(CONNECTIVITY_SERVICE_NAME);
        return new ServiceCredentials(credentialsJson.getString(CLIENT_ID), credentialsJson.getString(CLIENT_SECRET),
                credentialsJson.getString("url"));
    }

    public static ServiceCredentials getDestinationCredentials() {
        JSONObject credentialsJson =  getCredentialsJson(DESTINATION_SERVICE_NAME);
        return new ServiceCredentials(credentialsJson.getString(CLIENT_ID), credentialsJson.getString(CLIENT_SECRET),
                credentialsJson.getString("uri"));
    }

    public static ConnectivityProxyAddress getConnectivityProxyAddress(){
        JSONObject connectivityCredentialsJson =  getCredentialsJson(CONNECTIVITY_SERVICE_NAME);
        return new ConnectivityProxyAddress(connectivityCredentialsJson.getString("onpremise_proxy_host"),
                Integer.parseInt(connectivityCredentialsJson.getString("onpremise_proxy_http_port")));
    }

    public static String getXsuaaUrl() {
        return getCredentialsJson("xsuaa").getString("url");
    }

    private static JSONObject getCredentialsJson(String service) {
        JSONArray jsonArray = JSON_OBJECT.getJSONArray(service);
        return jsonArray.getJSONObject(0).getJSONObject("credentials");
    }

}
