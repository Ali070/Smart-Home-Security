package com.example.android.smarthomesecurity.HouseBreakingSubsystem;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.android.smarthomesecurity.Constants;
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

public class Door extends AsyncTask<String,Void,String> {
    private Context context;

    public Door( Context context) {
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

            // Getting data of endpoint of House breaking system in form of JSON using authentication token
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
        String doorResult = "";
        try {
            JSONObject doorRoot = new JSONObject(s);
            JSONObject doorEndPoint = doorRoot.getJSONObject(Constants.endpointId_Fire);
            JSONArray doorValues = doorEndPoint.getJSONArray(Constants.paramDoor);
            for (int i = 0; i < doorValues.length(); i++) {
                JSONObject temp = doorValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                doorResult = values.getString("value");
            }

            /* Checking data of door sensor and apply suitable UI for user if door is opened , UI for door unlocking is
             * appeared for user other than door locking UI appears */
            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.GONE);
            int doorNum = Math.round(Float.parseFloat(doorResult));
            TextView doorView = (TextView) ((Activity)context).findViewById(R.id.DoorLocked);
            ImageView doorIV = (ImageView)((Activity)context).findViewById(R.id.iVDoor);
            if(doorNum==0){
                doorView.setText("Locked");
                doorView.setTextColor(context.getResources().getColor(R.color.locking,null));
                doorIV.setImageResource(R.drawable.ic_action_lock);
            }
            else {
                doorView.setText("Unlocked");
                doorView.setTextColor(context.getResources().getColor(R.color.unlocking,null));
                doorIV.setImageResource(R.drawable.ic_action_unlock);
            }
        }catch (JSONException e){
            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.VISIBLE);
        }
    }
}

