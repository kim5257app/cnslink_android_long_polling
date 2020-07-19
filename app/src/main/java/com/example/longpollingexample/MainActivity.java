package com.example.longpollingexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.longpolling.Polling;
import com.longpolling.PollingInterface;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity
    extends AppCompatActivity
    implements PollingInterface {
    final String TAG = "MainActivity";

    private Polling polling = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        polling = new Polling(this.getApplicationContext(), this);

        initCtrl();
    }

    private void initCtrl() {
        Button btnConnect = findViewById(R.id.connect);
        btnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                polling.connect();
            }
        });

        Button btnEmit = findViewById(R.id.emit);
        btnEmit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("key", "TEST Key");
                    data.put("value", "TEST Value");

                    polling.emit("ping", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onConnect(String clientId) {
        Log.d(TAG, "connected: " + clientId);
    }

    @Override
    public void onReceive(String name, JSONObject data) {
        Log.d(TAG, "onReceive[" + name + "]: " + ((data != null) ? data.toString() : ""));
    }

    @Override
    public void onError() {
        Log.e(TAG, "Error");
    }
}