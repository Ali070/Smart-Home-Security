package com.example.android.smarthomesecurity.HouseBreakingSubsystem;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.smarthomesecurity.Constants;
import com.example.android.smarthomesecurity.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Sniffing extends AsyncTask<String,Void,String> {
    private Context context;
    public Sniffing( Context context) {
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
        String sniffResult = "";
        try {
            JSONObject sniffRoot = new JSONObject(s);
            JSONObject sniffEndPoint = sniffRoot.getJSONObject(Constants.endpointId_Fire);
            JSONArray sniffValues = sniffEndPoint.getJSONArray(Constants.paramSniffing);
            for (int i = 0; i < sniffValues.length(); i++) {
                JSONObject temp = sniffValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                sniffResult = values.getString("value");
            }


            int sniffNum = Math.round(Float.parseFloat(sniffResult));

        }catch (JSONException e){

        }
    }
}
