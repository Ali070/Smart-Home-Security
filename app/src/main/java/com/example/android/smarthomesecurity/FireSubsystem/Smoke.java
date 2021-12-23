package com.example.android.smarthomesecurity.FireSubsystem;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

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

public class Smoke extends AsyncTask<String,Void,String> {
    private Context context;
    public static int count = 0;
    public Smoke(Context con) {
        context = con;
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

            // Getting data of endpoint of fire system in form of JSON using authentication token
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
        String smokeResult = "";
        try {
            JSONObject root = new JSONObject(s);
            JSONObject endPoint = root.getJSONObject(Constants.endpointId_Fire);
            JSONArray smokeValues = endPoint.getJSONArray(Constants.paramSmoke);

            for (int i=0;i<smokeValues.length();i++){
                JSONObject temp = smokeValues.getJSONObject(i);
                JSONObject values = temp.getJSONObject("values");
                smokeResult = values.getString("value");
            }

            /* Checking data of smoke sensor and apply suitable UI for user if smoke is bigger than 5o, UI for warning is
            * appeared for user other than safe UI appears */
            int smokeNum = Math.round(Float.parseFloat(smokeResult)) ;
            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.GONE);
            SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.Smarthomesecurity", Context.MODE_PRIVATE);
            TextView smokeView = (TextView) ((Activity)context).findViewById(R.id.smokeView);
            TextView messageView = (TextView) ((Activity)context).findViewById(R.id.textView3);
            ImageView messageImView = (ImageView) ((Activity)context).findViewById(R.id.safeImage);
            CircularProgressBar progressBarSmoke = (CircularProgressBar) ((Activity)context).findViewById(R.id.circularProgressBar2);
            SwitchCompat alarmSwitch = ((Activity)context).findViewById(R.id.switch1);
            if(smokeNum>=32){
                smokeView.setText(Integer.toString(smokeNum));
                progressBarSmoke.setProgressWithAnimation(smokeNum, (long) 1000);
                progressBarSmoke.setProgressBarColor(context.getResources().getColor(R.color.dangerous,null));
                messageView.setText("Warning");
                messageImView.setImageResource(R.drawable.ic_action_warning);
                messageView.setTextColor(context.getResources().getColor(R.color.dangerous,null));
                if(count==0){
                    alarmSwitch.setChecked(true);
                    alarmSwitch.setClickable(false);
                    sharedPreferences.edit().putBoolean("ischecked", true).apply();
                }
                count++;
                Log.i("count",Integer.toString(count));

                if(count==10){
                    alarmSwitch.setClickable(true);
                }
            }else{
                smokeView.setText(Integer.toString(smokeNum));
                progressBarSmoke.setProgressWithAnimation(smokeNum, (long) 1000);
                progressBarSmoke.setProgressBarColor(context.getResources().getColor(R.color.warning,null));
                messageView.setText("You are safe");
                messageImView.setImageResource(R.drawable.safe);
                messageView.setTextColor(context.getResources().getColor(R.color.white,null));
                if(count>0){
                    alarmSwitch.setChecked(false);
                    sharedPreferences.edit().putBoolean("ischecked", false).apply();
                }
                count = 0;
                alarmSwitch.setClickable(true);
            }
        }catch (JSONException e){
            View serverError = (View) ((Activity)context).findViewById(R.id.serverErr);
            serverError.setVisibility(View.VISIBLE);
        }
    }
}
