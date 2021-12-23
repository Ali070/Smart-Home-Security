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

public class closeDoor extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... strings) {
        String json2 = "";
        URL url,url2;
        HttpsURLConnection urlConnection = null,urlConnection2 = null;
        String auth = "";
        String body = "grant_type=password&client_id="+ Constants.clientID+"&username="+Constants.username+"&password="+Constants.password+"&client_secret="+Constants.clientSecret;
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

            // Make a JSON payload with a state parameter for door motors to open door and a name for the command which is command type called door
            JSONObject root2 = new JSONObject(json2);
            auth = root2.getString("access_token");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("door","off");
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

            // Check if command is not executed by door motors in wait timeout, it will send another command
            if(response.length()==0){
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("door","off");
                url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Authorization","Bearer "+auth);

                DataOutputStream localDataOutputStream2 = new DataOutputStream(urlConnection.getOutputStream());
                localDataOutputStream2.writeBytes(jsonObject2.toString());
                localDataOutputStream2.flush();
                localDataOutputStream2.close();
                BufferedReader br2 = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response2 = new StringBuilder();
                String responseLine2 = null;
                while ((responseLine2 = br2.readLine()) != null) {
                    response2.append(responseLine2.trim());
                }
            }

        }catch (Exception e) {
            Log.i("Error","Failed:"+e);
        }
        return null;
    }
}
