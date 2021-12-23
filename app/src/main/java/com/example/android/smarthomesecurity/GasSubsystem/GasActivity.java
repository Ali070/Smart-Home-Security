package com.example.android.smarthomesecurity.GasSubsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.android.smarthomesecurity.Authentication.ConnectionMessageActivity;
import com.example.android.smarthomesecurity.Constants;
import com.example.android.smarthomesecurity.R;
import com.example.android.smarthomesecurity.Util;
import com.example.android.smarthomesecurity.WaterSubsystem.Water;
import com.example.android.smarthomesecurity.WaterSubsystem.WaterActivity;
import com.example.android.smarthomesecurity.commands;

import java.util.ArrayList;
import java.util.Locale;

public class GasActivity extends AppCompatActivity {
    SwitchCompat gasValveSwitch;
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private boolean isChecked1;
    Runnable runnable;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.mic_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }else if(item.getItemId() == R.id.micItem){
            Mic();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gas);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        gasValveSwitch = findViewById(R.id.gasValveswitch);
        sharedPreferences = this.getSharedPreferences("com.example.Smarthomesecurity", Context.MODE_PRIVATE);
        isChecked1 = sharedPreferences.getBoolean("ischecked1",false);
        if(isChecked1){
            gasValveSwitch.setChecked(true);
        }else {
            gasValveSwitch.setChecked(false);
        }


        // Get the value of gas valve switch checking to send the suitable command and save its state in preferences
        gasValveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(Util.connectAvailable(GasActivity.this)) {
                    if (isChecked) {
                        new commands().gasValveOn();
                        sharedPreferences.edit().putBoolean("ischecked1", true).apply();
                    } else {
                        new commands().gasValveOff();
                        sharedPreferences.edit().putBoolean("ischecked1", false).apply();
                    }
                }else{
                    startActivity(new Intent(GasActivity.this, ConnectionMessageActivity.class));
                }
            }
        });



        // Get data and apply it on UI each 2 seconds
         runnable = new Runnable() {
            @Override
            public void run() {
                if(Util.connectAvailable(GasActivity.this)) {
                    new Gas(GasActivity.this).execute("https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_Gas + "&timeSeriesName=" + Constants.paramGas);
                    handler.postDelayed(this, 2000);
                }else{
                    startActivity(new Intent(GasActivity.this, ConnectionMessageActivity.class));
                }
            }
        };
        runnable.run();
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
        finish();
    }

    public void Mic(){
        if(Util.connectAvailable(GasActivity.this)) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 10);
            } else {
                Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
            }
        }else{
            startActivity(new Intent(GasActivity.this, ConnectionMessageActivity.class));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if (resultCode == RESULT_OK && data != null) {
                String resultTemp = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toString();
                String result = resultTemp.toLowerCase();
                Log.i("Messaage",result);
                if (result.contains("gas") || result.contains("valve")) {
                    if (result.contains("on") || result.contains("open")) {
                        if(!gasValveSwitch.isChecked()) {
                            new commands().gasValveOn();
                            sharedPreferences.edit().putBoolean("ischecked1", true).apply();
                            gasValveSwitch.setChecked(true);
                        }
                    } else if(result.contains("off") || result.contains("close")) {
                        if(gasValveSwitch.isChecked()) {
                            new commands().gasValveOff();
                            sharedPreferences.edit().putBoolean("ischecked1", false).apply();
                            gasValveSwitch.setChecked(false);
                        }
                    }
                } /*else if (result.contains("suction")) {
                    result = result.replaceAll("suction","");
                    Log.i("ss","ads");
                    if (result.contains("on") || result.contains("open")) {
                        if(!suctionSwitch.isChecked()) {
                            Log.e("ss", "on");

                            sharedPreferences.edit().putBoolean("ischecked2", true).apply();
                            suctionSwitch.setChecked(true);
                        }
                    } else if(result.contains("off") || result.contains("close")) {
                        if(suctionSwitch.isChecked()) {
                            Log.e("ss", "close");

                            sharedPreferences.edit().putBoolean("ischecked2", false).apply();
                            suctionSwitch.setChecked(false);
                        }
                    }
                }*/
            }
        }
    }
}