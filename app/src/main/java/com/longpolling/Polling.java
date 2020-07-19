package com.longpolling;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.longpollingexample.MainActivity;

import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class Polling {
    final String TAG = "Polling";

    final String host = "http://49.247.5.168:4000";
    final String connect = "/comm/connect";
    final String emit = "/comm/emit";
    final String wait = "/comm/wait";

    private PollingInterface handler;
    private Context context;
    private String connId = null;

    private Map<String, String> waitHeader = new HashMap<String, String>();
    private Map<String, String> emitHeader = new HashMap<String, String>();

    public Polling(Context context, PollingInterface handler) {
        this.context = context;
        this.handler = handler;
    }

    private void initWait() {
        Log.d(TAG, "initWait");

        if (this.connId != null) {
            PollingRequestQueue queue = PollingRequestQueue.getInstance(context);
            JsonObjectRequest request = makeWaitRequest();
            queue.addToRequestQueue(request);
        }
    }

    private void handleClientError(ClientError error) {
        this.connId = null;

        switch (error.networkResponse.statusCode) {
        case 410:
            this.connect();
            break;
        default:
            break;
        }
    }

    private JsonObjectRequest makeWaitRequest() {
        String url = host + wait;

        final JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String name = response.getString("name");
                        JSONObject data = (response.isNull("data"))
                            ? null : response.getJSONObject("data");

                        handler.onReceive(name, data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    initWait();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (error instanceof TimeoutError) {
                    } else if (error instanceof ClientError) {
                        handleClientError((ClientError) error);
                    } else {
                        handler.onError();
                    }

                    initWait();
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return Polling.this.waitHeader;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
            5 * 1000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        return request;
    }

    public void connect() {
        PollingRequestQueue queue = PollingRequestQueue.getInstance(context);
        String url = host + connect;

        StringRequest request = new StringRequest(
            Request.Method.GET,
            url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("result").equals("success")) {
                        String connId = jsonObject.getString("clientId");
                        handler.onConnect(connId);
                        Polling.this.connId = connId;

                        // 헤더 설정
                        Polling.this.waitHeader.put("id", connId);
                        Polling.this.emitHeader.put("id", connId);
                        Polling.this.emitHeader.put("Content-Type", "application/json");

                        Polling.this.initWait();
                    } else {
                        handler.onError();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.onError();
                }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    handler.onError();
                }
            }
        );

        queue.addToRequestQueue(request);
    }

    public void emit(String event, JSONObject data) throws JSONException {
        PollingRequestQueue queue = PollingRequestQueue.getInstance(context);
        String url = host + emit;

        JSONObject param = new JSONObject();
        param.put("name", event);
        param.put("data", data);

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST,
            url,
            param,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    handler.onError();
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return Polling.this.emitHeader;
            }
        };

        queue.addToRequestQueue(request);
    }

    public String getConnId() {
        return connId;
    }
}
