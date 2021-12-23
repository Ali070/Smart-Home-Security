package com.example.android.smarthomesecurity.GasSubsystem;

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

public class Gas extends AsyncTask<String,Void,String> {
    private Context context;
    public static int counter = 0;
    public Gas( Context context) {
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


            // Getting data of endpoint of Gas leakage system in form of JSON using authentication token
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
        String gasResult = "";
        try {
            JSONObject root = new JSONObject(s);
            JSONObject endPoint = root.getJSONObject(Constants.endpointId_Gas);
            JSONArray gasValues = endPoint.getJSONArray(Constants.paramGas);

            for (int i = 0; i < gasValues.length(); i++) {
                JSONObject temp = gasValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                gasResult = values.getString("value");
            }

            /* Checking data of gas sensor and apply suitable UI for user if gas concentration in air is bigger than 900, UI for danger is
             * appeared for user and if gas concentration in air is between 900 and 400, warning UI appeared for user
             * other than safe UI appears */
            int gasNum = Math.round(Float.parseFloat(gasResult));
            TextView gasView = (TextView) ((Activity)context).findViewById(R.id.gasView);
            CircularProgressBar progressBarGas = (CircularProgressBar) ((Activity)context).findViewById(R.id.GasProBar);
            TextView messageView = (TextView) ((Activity)context).findViewById(R.id.textView3);
            ImageView messageImView = (ImageView) ((Activity)context).findViewById(R.id.safeImage);
            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.GONE);
            if(gasNum<400){
                counter=0;
                gasView.setText(Integer.toString(gasNum));
                progressBarGas.setProgressWithAnimation(gasNum, (long) 1000);
                progressBarGas.setProgressBarColor(context.getResources().getColor(R.color.safe,null));
                messageView.setText("You are safe");
                messageImView.setImageResource(R.drawable.safe);
                messageView.setTextColor(context.getResources().getColor(R.color.white,null));
            }else if(gasNum>=400 && gasNum<900){
                counter++;
                Log.e("gas","Warning");
                Log.e("countWarning",Integer.toString(counter));
                gasView.setText(Integer.toString(gasNum));
                progressBarGas.setProgressWithAnimation(gasNum, (long) 1000);
                progressBarGas.setProgressBarColor(context.getResources().getColor(R.color.warning,null));
                if(counter>=5){
                    messageView.setText("Warning");
                    messageView.setTextColor(context.getResources().getColor(R.color.warning,null));
                    messageImView.setImageResource(R.drawable.ic_action_warning);

                }
            }else if(gasNum>=900){
                counter++;
                gasView.setText(Integer.toString(gasNum));
                progressBarGas.setProgressWithAnimation(gasNum, (long) 1000);
                progressBarGas.setProgressBarColor(context.getResources().getColor(R.color.dangerous,null));
                if(counter>=5){
                    messageView.setText("Danger");
                    messageView.setTextColor(context.getResources().getColor(R.color.dangerous,null));
                    messageImView.setImageResource(R.drawable.ic_action_warning);

                }
            }
        }catch (JSONException e){

            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.VISIBLE);
        }
    }

}
