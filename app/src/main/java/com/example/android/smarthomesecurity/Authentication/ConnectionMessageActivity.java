package com.example.android.smarthomesecurity.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.smarthomesecurity.R;
import com.example.android.smarthomesecurity.Util;

public class ConnectionMessageActivity extends AppCompatActivity {
    private TextView message;
    private ProgressBar pbmessage;
    private ConnectivityManager.NetworkCallback networkCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_message);
        message = findViewById(R.id.textView6);
        pbmessage = findViewById(R.id.progressBar2);
        pbmessage.setProgressTintList(ColorStateList.valueOf(Color.WHITE));
        networkCallback = new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                finish();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                message.setText(R.string.No_Connction);
                Log.i("touch","lost");
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                message.setText(R.string.No_Connction);

            }
        };
        ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),networkCallback);

    }
    public void check(View view){
        Log.i("touch","on");
        pbmessage.setVisibility(View.VISIBLE);
        message.setText("Checking Internet Connection...");
        if(Util.connectAvailable(this)){
            finish();
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pbmessage.setVisibility(View.GONE);
                    message.setText(R.string.No_Connction);
                }
            },1000);
        }
    }
}