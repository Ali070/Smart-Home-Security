package com.example.android.smarthomesecurity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import android.os.Handler;

import com.example.android.smarthomesecurity.FireSubsystem.FireActivity;
import com.example.android.smarthomesecurity.GasSubsystem.GasActivity;
import com.example.android.smarthomesecurity.WaterSubsystem.WaterActivity;

import javax.net.ssl.HttpsURLConnection;

public class BackgroundServices extends Service {
    boolean isServiceRunning = false;
    boolean isWaterNotified = false;
    boolean isFireNotified = false;
    boolean isSmokeCommandSentOn = false;
    boolean isSmokeCommandSentOff = true;
    boolean isGasNotified1 = false;
    boolean isGasNotified2 = false;
    boolean isHouseNotified = false;
    int counter = 0;
    @Override
    public void onCreate() {
        // Foreground service Notification
        isServiceRunning = true;
        final Handler handler = new Handler();
        createForegroundNotification();
        Intent intent1 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0
                , intent1, 0);
        Notification Notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.iot)
                .setContentTitle("Security is on")
                .setContentText("Running...")
                .setChannelId("111")
                .setContentIntent(pendingIntent1).build();
        startForeground(1, Notification);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        // Checking data from security systems in service
        new Thread(new Runnable(){
            public void run() {
                // TODO Auto-generated method stub
                while(isServiceRunning)
                {
                    Log.i("state","running");
                    getWater();
                    getGas();
                    getFire();
                    getHouse();
                    try {
                        Log.i("state","sleep");
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        Log.i("Error","Service Interrupted");
                    }
                }

            }
        }).start();
        return START_STICKY;
    }

    private void createForegroundNotification() {
        NotificationChannel mChannel = new NotificationChannel("111", "Foreground Notification", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(mChannel);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        isServiceRunning = false;
        super.onDestroy();
    }

    public String getHouse(){
        String urlTemp2 = "https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_HouseBreaking + "&timeSeriesName=" + Constants.paramSniffing;
        String urlTemp = "https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_HouseBreaking + "&timeSeriesName=" + Constants.paramDoor;
        String json = "", json2 = "",json3 = "";
        URL url, url2,url3;
        HttpsURLConnection urlConnection = null, urlConnection2 = null,urlConnection3 = null;
        String auth = "";
        String body = "grant_type=password&client_id=" + Constants.clientID + "&username=" + Constants.username + "&password=" + Constants.password + "&client_secret=" + Constants.clientSecret;
        try {
            // Getting the authentication token to use it in getting data
            url2 = new URL("https://authq.cloud.kaaiot.com/auth/realms/" + Constants.realmid + "/protocol/openid-connect/token");
            urlConnection2 = (HttpsURLConnection) url2.openConnection();
            urlConnection2.setRequestMethod("POST");
            urlConnection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection2.setConnectTimeout(15000);
            urlConnection2.setDoOutput(true);
            urlConnection2.setRequestProperty("charset", "utf-8");
            urlConnection2.connect();
            byte[] postData = body.getBytes();
            DataOutputStream wr = new DataOutputStream(urlConnection2.getOutputStream());
            wr.write(postData);

            InputStream in2 = urlConnection2.getInputStream();
            InputStreamReader reader2 = new InputStreamReader(in2);
            int data2 = reader2.read();
            while (data2 != -1) {
                char character = (char) data2;
                json2 += character;
                data2 = reader2.read();
            }
            JSONObject root2 = new JSONObject(json2);
            auth = root2.getString("access_token");

            // Getting data of endpoint of House breaking system in form of JSON using authentication token
            url = new URL(urlTemp);

            urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setInstanceFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);


            urlConnection.setRequestProperty("Authorization", "Bearer " + auth);
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();
            while (data != -1) {
                char character = (char) data;
                json += character;
                data = reader.read();
            }

            // Dividing JSON data into objects and get object with suitable parameter
            String doorResult = "";
            JSONObject root = new JSONObject(json);
            JSONObject endPoint = root.getJSONObject(Constants.endpointId_HouseBreaking);
            JSONArray doorValues = endPoint.getJSONArray(Constants.paramDoor);

            for (int i = 0; i < doorValues.length(); i++) {
                JSONObject temp = doorValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                doorResult = values.getString("value");
            }
            int doorNum = Math.round(Float.parseFloat(doorResult));

            // Getting data of endpoint of House breaking system in form of JSON using authentication token
            url3 = new URL(urlTemp2);

            urlConnection3 = (HttpsURLConnection) url3.openConnection();

            urlConnection3.setInstanceFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);


            urlConnection3.setRequestProperty("Authorization", "Bearer " + auth);
            InputStream in3 = urlConnection3.getInputStream();
            InputStreamReader reader3 = new InputStreamReader(in);
            int data3 = reader3.read();
            while (data3 != -1) {
                char character = (char) data3;
                json3 += character;
                data3 = reader3.read();
            }

            // Dividing JSON data into objects and get object with suitable parameter
            String sniffResult = "";
            JSONObject root3 = new JSONObject(json);
            JSONObject endPoint3 = root3.getJSONObject(Constants.endpointId_HouseBreaking);
            JSONArray sniffValues = endPoint3.getJSONArray(Constants.paramSniffing);

            for (int i = 0; i < sniffValues.length(); i++) {
                JSONObject temp = sniffValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                sniffResult = values.getString("value");
            }
            int sniffNum = Math.round(Float.parseFloat(doorResult));
            if(sniffNum==1){
                new commands().openDoor();
            }

        } catch (Exception e) {
            Log.i("House Error", "Failed:" + e);
        }
        return json;
    }

    public String getGas(){
        String json = "",json2 = "";
        String urlTemp = "https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_Gas + "&timeSeriesName=" + Constants.paramGas;
        URL url,url2;
        HttpsURLConnection urlConnection = null,urlConnection2 = null;
        String auth = "";
        String body = "grant_type=password&client_id="+ Constants.clientID+"&username="+Constants.username+"&password="+Constants.password+"&client_secret="+Constants.clientSecret;
        try {
            // Getting the authentication token to use it in getting data
            url2 = new URL("https://authq.cloud.kaaiot.com/auth/realms/"+Constants.realmid+"/protocol/openid-connect/token");
            urlConnection2 = (HttpsURLConnection) url2.openConnection();
            urlConnection2.setRequestMethod("POST");
            urlConnection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection2.setConnectTimeout(15000);
            urlConnection2.setDoOutput(true);
            urlConnection2.setRequestProperty("charset", "utf-8");
            urlConnection2.connect();
            byte[] postData = body.getBytes();
            DataOutputStream wr = new DataOutputStream(urlConnection2.getOutputStream());
            wr.write(postData);

            InputStream in2 = urlConnection2.getInputStream();
            InputStreamReader reader2 = new InputStreamReader(in2);
            int data2 = reader2.read();
            while (data2 != -1) {
                char character = (char) data2;
                json2 += character;
                data2 = reader2.read();
            }
            JSONObject root2 = new JSONObject(json2);
            auth = root2.getString("access_token");

            // Getting data of endpoint of Gas leakage system in form of JSON using authentication token
            url = new URL(urlTemp);

            urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setInstanceFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);


            urlConnection.setRequestProperty("Authorization","Bearer "+auth);


            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();
            while (data != -1){
                char character = (char)data;
                json+=character;
                data = reader.read();
            }

            // Dividing JSON data into objects and get object with suitable parameter
            String gasResult = "";
            JSONObject root = new JSONObject(json);
            JSONObject endPoint = root.getJSONObject(Constants.endpointId_Gas);
            JSONArray gasValues = endPoint.getJSONArray(Constants.paramGas);

            for (int i = 0; i < gasValues.length(); i++) {
                JSONObject temp = gasValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                gasResult = values.getString("value");
            }

            /* Checking data of gas sensor in service if gas concentration is greater than 900 for 5 seconds, a notification will be
            * sent to user with dangerous message, if gas concentration is between 900 and 400 for 5 seconds a notification will be,
            * sent to user with warning message and if gas concentration is less than 400 so it is safe*/
            int gasNum = Math.round(Float.parseFloat(gasResult));
            if(gasNum<400){
                counter=0;
                isGasNotified1 = false;
                isGasNotified2 = false;
            }else if(gasNum>=400 && gasNum<900){
                counter++;

                if(counter>=5){
                    if(!isGasNotified1) {
                        isGasNotified1 = true;
                        getNotification("gasNotify", "Check gas", 3);
                    }
                }
            }else if(gasNum>=900) {
                counter++;
                if (counter >= 5) {
                    if(!isGasNotified2) {
                        isGasNotified2 = true;
                        getNotification("gasNotify", "Gas is in danger level", 3);
                    }
                }
            }
        } catch (Exception e) {
            Log.i("Error","Failed:"+e);
        }

        return json;

    }
    public String getFire(){
        String urlTemp2 = "https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_Fire + "&timeSeriesName=" + Constants.paramSmoke;
        String json = "", json2 = "", json3="";
        URL url2,url3;
        HttpsURLConnection  urlConnection2 = null,urlConnection3;
        String auth = "";
        String body = "grant_type=password&client_id=" + Constants.clientID + "&username=" + Constants.username + "&password=" + Constants.password + "&client_secret=" + Constants.clientSecret;
        try {
            // Getting the authentication token to use it in getting data
            url2 = new URL("https://authq.cloud.kaaiot.com/auth/realms/" + Constants.realmid + "/protocol/openid-connect/token");
            urlConnection2 = (HttpsURLConnection) url2.openConnection();
            urlConnection2.setRequestMethod("POST");
            urlConnection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection2.setConnectTimeout(15000);
            urlConnection2.setDoOutput(true);
            urlConnection2.setRequestProperty("charset", "utf-8");
            urlConnection2.connect();
            byte[] postData = body.getBytes();
            DataOutputStream wr = new DataOutputStream(urlConnection2.getOutputStream());
            wr.write(postData);

            InputStream in2 = urlConnection2.getInputStream();
            InputStreamReader reader2 = new InputStreamReader(in2);
            int data2 = reader2.read();
            while (data2 != -1) {
                char character = (char) data2;
                json2 += character;
                data2 = reader2.read();
            }
            JSONObject root2 = new JSONObject(json2);
            auth = root2.getString("access_token");

            // Getting data of endpoint of fire system in form of JSON using authentication token
            url3 = new URL(urlTemp2);
            urlConnection3 = (HttpsURLConnection) url3.openConnection();

            urlConnection3.setInstanceFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);


            urlConnection3.setRequestProperty("Authorization","Bearer "+auth);
            InputStream in3 = urlConnection3.getInputStream();
            InputStreamReader reader3 = new InputStreamReader(in3);
            int data3 = reader3.read();
            while (data3 != -1){
                char character = (char)data3;
                json3+=character;
                data3 = reader3.read();
            }

            // Dividing JSON data into objects and get object with suitable parameter
            String smokeResult = "";
            JSONObject root3 = new JSONObject(json3);
            JSONObject endPoint3 = root3.getJSONObject(Constants.endpointId_Fire);
            JSONArray smokeValues = endPoint3.getJSONArray(Constants.paramSmoke);

            for (int i=0;i<smokeValues.length();i++){
                JSONObject temp = smokeValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                smokeResult = values.getString("value");
            }

            /*Checking data of smoke sensor in service if data greater than 50, a notification will be appeared for user
            * that there is fire and a command sent to open alarm other than it is safe*/
            int smokeNum = Math.round(Float.parseFloat(smokeResult)) ;
            if(smokeNum>=32){
                if(!isFireNotified) {
                    isFireNotified = true;
                    getNotification("fireNotify", "There is fire", 4);

                }
                if(!isSmokeCommandSentOn) {
                    new commands().fireAlarmOn();
                    isSmokeCommandSentOn = true;
                    isSmokeCommandSentOff = false;
                }
            }else{
                isFireNotified = false;
                if(!isSmokeCommandSentOff) {
                    new commands().fireAlarmOff();
                    isSmokeCommandSentOff = true;
                    isSmokeCommandSentOn = false;
                }
            }

        } catch (Exception e) {
            Log.i("Error", "Failed:" + e);
        }
        return json;
    }

    public String getWater(){
        String json = "",json2 = "";
        String urlTemp = "https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_Water + "&timeSeriesName=" + Constants.paramWater;
        URL url,url2;
        HttpsURLConnection urlConnection = null,urlConnection2 = null;
        String auth = "";
        String body = "grant_type=password&client_id="+ Constants.clientID+"&username="+Constants.username+"&password="+Constants.password+"&client_secret="+Constants.clientSecret;
        try {
            // Getting the authentication token to use it in getting data
            url2 = new URL("https://authq.cloud.kaaiot.com/auth/realms/"+Constants.realmid+"/protocol/openid-connect/token");
            urlConnection2 = (HttpsURLConnection) url2.openConnection();
            urlConnection2.setRequestMethod("POST");
            urlConnection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection2.setConnectTimeout(15000);
            urlConnection2.setDoOutput(true);
            urlConnection2.setRequestProperty("charset", "utf-8");
            urlConnection2.connect();
            byte[] postData = body.getBytes();
            DataOutputStream wr = new DataOutputStream(urlConnection2.getOutputStream());
            wr.write(postData);

            InputStream in2 = urlConnection2.getInputStream();
            InputStreamReader reader2 = new InputStreamReader(in2);
            int data2 = reader2.read();
            while (data2 != -1) {
                char character = (char) data2;
                json2 += character;
                data2 = reader2.read();
            }
            JSONObject root2 = new JSONObject(json2);
            auth = root2.getString("access_token");
            Log.i("auth",auth);
            // Getting data of endpoint of Water system in form of JSON using authentication token
            url = new URL(urlTemp);

            urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setInstanceFollowRedirects(true);
            HttpsURLConnection.setFollowRedirects(true);


            urlConnection.setRequestProperty("Authorization","Bearer "+auth);

            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();
            while (data != -1){
                char character = (char)data;
                json+=character;
                data = reader.read();
            }

            // Dividing JSON data into objects and get object with suitable parameter
            String waterResult = "";
            JSONObject root = new JSONObject(json);
            JSONObject endPoint = root.getJSONObject(Constants.endpointId_Water);
            JSONArray waterValues = endPoint.getJSONArray(Constants.paramWater);

            for (int i = 0; i < waterValues.length(); i++) {
                JSONObject temp = waterValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                waterResult = values.getString("value");
            }

            /* Checking tha data of water sensor in service if data greater than 0 this means that sensor sense water,
            *  a notification will be sent to the user with warning message other than it is safe */
            int waterNum = Math.round(Float.parseFloat(waterResult));
            if(waterNum>0){
                if(!isWaterNotified){
                    getNotification("waterNotify","High water level",2);
                    isWaterNotified = true;
                }

            }else {
                isWaterNotified = false;
            }
        } catch (Exception e) {
            Log.i("Error","Failed:"+e);
        }

        return json;
    }

    // This method is for making and sending notification if all systems if there is warning or danger
    public void getNotification(String name, String content,int id) {
        Intent intent = null;
        if (id==2){
            intent = new Intent(this, WaterActivity.class);
        }else if(id==3){
            intent = new Intent(this, GasActivity.class);
        }else if(id==4){
            intent = new Intent(this, FireActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0
                , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel("123", name, importance);
        Notification Notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.iot)
                .setContentTitle("Warning")
                .setContentText(content)
                .setAutoCancel(true)
                .setChannelId("123")
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager.notify(id, Notification);
    }
}
