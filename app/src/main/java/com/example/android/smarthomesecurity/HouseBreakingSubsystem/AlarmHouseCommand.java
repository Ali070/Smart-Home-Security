package com.example.android.smarthomesecurity.HouseBreakingSubsystem;

import android.os.AsyncTask;
import android.util.Log;

import com.example.android.smarthomesecurity.Constants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class AlarmHouseCommand extends AsyncTask<String,Void,String> {
    private String alarm;

    public AlarmHouseCommand(String alarm) {
        this.alarm = alarm;
    }
    @Override
    protected String doInBackground(String... strings) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        String json2 = "";
        URL url,url2;
        HttpsURLConnection urlConnection = null,urlConnection2 = null;
        String auth = "";
        String body = "grant_type=password&client_id="+ Constants.clientID+"&username="+Constants.username+"&password="+Constants.password+"&client_secret="+Constants.clientSecret;
        try {
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
            wr.close();
            InputStream in2 = urlConnection2.getInputStream();
            InputStreamReader reader2 = new InputStreamReader(in2);
            int data2 = reader2.read();
            while (data2 != -1) {
                char character = (char) data2;
                json2 += character;
                data2 = reader2.read();
            }
            in2.close();
            reader2.close();
            JSONObject root2 = new JSONObject(json2);
            auth = root2.getString("access_token");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("power",alarm);
            url = new URL(strings[0]);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "*/*");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Authorization","Bearer "+auth);

            DataOutputStream localDataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
            localDataOutputStream.writeBytes(jsonObject.toString());
            localDataOutputStream.flush();
            localDataOutputStream.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }


        }catch (Exception e) {
            Log.i("Error","Failed:"+e);
        }
        return null;
    }
}
