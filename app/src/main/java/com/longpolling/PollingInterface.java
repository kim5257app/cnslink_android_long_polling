package com.longpolling;

import org.json.JSONObject;

public interface PollingInterface {
    public void onConnect(String clientId);
    public void onReceive(String name, JSONObject data);
    public void onError();
}
