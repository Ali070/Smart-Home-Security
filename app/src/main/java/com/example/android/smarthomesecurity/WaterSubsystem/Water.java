package com.example.android.smarthomesecurity.WaterSubsystem;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.smarthomesecurity.Constants;
import com.example.android.smarthomesecurity.MainActivity;
import com.example.android.smarthomesecurity.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Water extends AsyncTask<String,Void,String> {
    private Context context;

    public Water( Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String json = "",json2 = "";
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

            // Getting data of endpoint of Water system in form of JSON using authentication token
            url = new URL(strings[0]);

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

        } catch (Exception e) {
            Log.i("Error","Failed:"+e);
        }
        return json;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // Dividing JSON data into objects and get object with suitable parameter
        String waterResult = "";
        try {
            JSONObject root = new JSONObject(s);
            JSONObject endPoint = root.getJSONObject(Constants.endpointId_Water);
            JSONArray waterValues = endPoint.getJSONArray(Constants.paramWater);

            for (int i = 0; i < waterValues.length(); i++) {
                JSONObject temp = waterValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                waterResult = values.getString("value");
            }
            int waterNum = Math.round(Float.parseFloat(waterResult));

            /* Checking data of water sensor and apply suitable UI for user if water reaches dangerous level , UI for Warning is
             * appeared for user other than safe UI appears */
            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.GONE);
            TextView waterView = (TextView) ((Activity)context).findViewById(R.id.textView3);
            ImageView waterStateView = (ImageView) ((Activity)context).findViewById(R.id.safeImage);
            if(waterNum>0){
                waterView.setText("Warning");
                waterView.setTextColor(context.getResources().getColor(R.color.locking,null));
                waterStateView.setImageResource(R.drawable.ic_action_warning);
            }
            else {
                waterView.setText("You are safe");
                waterView.setTextColor(context.getResources().getColor(R.color.white,null));
                waterStateView.setImageResource(R.drawable.safe);
            }


        }catch (JSONException e){
            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.VISIBLE);

        }
    }
}
