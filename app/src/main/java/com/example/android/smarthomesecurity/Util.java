package com.example.android.smarthomesecurity;

import android.content.Context;
import android.net.ConnectivityManager;

public class Util {
    public static boolean connectAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null && connectivityManager.getActiveNetworkInfo()!= null){
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        }else {
            return false;
        }
    }
}
