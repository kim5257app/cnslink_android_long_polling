package com.longpolling;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class PollingRequestQueue {
    @SuppressLint("StaticFieldLeak")
    private static PollingRequestQueue instance;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private RequestQueue requestQueue;

    private PollingRequestQueue(Context context) {
        PollingRequestQueue.context = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized PollingRequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new PollingRequestQueue(context);
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
