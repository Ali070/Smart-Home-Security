package com.example.android.smarthomesecurity.WaterSubsystem;

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
import com.example.android.smarthomesecurity.commands;

import java.util.ArrayList;
import java.util.Locale;

public class WaterActivity extends AppCompatActivity {
    SwitchCompat drainSwitch, valveSwitch;
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private boolean isChecked1,isChecked2;
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
        }else {
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         // Get data and apply it on UI each 2 seconds
         runnable = new Runnable() {
            @Override
            public void run() {
                if (Util.connectAvailable(WaterActivity.this)) {
                    new Water(WaterActivity.this).execute("https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_Water + "&timeSeriesName=" + Constants.paramWater);
                    handler.postDelayed(this, 2000);
                }else {
                    startActivity(new Intent(WaterActivity.this,ConnectionMessageActivity.class));
                }
            }
        };
        runnable.run();
        valveSwitch = findViewById(R.id.switch1);
        drainSwitch = findViewById(R.id.switch2);
        sharedPreferences = this.getSharedPreferences("com.example.Smarthomesecurity", Context.MODE_PRIVATE);
        isChecked1 = sharedPreferences.getBoolean("ischecked1",true);
        isChecked2 = sharedPreferences.getBoolean("ischecked2",false);
        if(isChecked1){
            valveSwitch.setChecked(true);
        }else {
            valveSwitch.setChecked(false);
        }
        if(isChecked2){
                drainSwitch.setChecked(true);
        }else {
            drainSwitch.setChecked(false);
        }

        // Get the value of water valve switch checking to send the suitable command and save its state in preferences
        valveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Util.connectAvailable(WaterActivity.this)) {
                    if (isChecked) {
                        new commands().waterValveOn();
                        sharedPreferences.edit().putBoolean("ischecked1", true).apply();
                    } else {
                        new commands().waterValveOff();
                        sharedPreferences.edit().putBoolean("ischecked1", false).apply();
                    }
                }else {
                    startActivity(new Intent(WaterActivity.this, ConnectionMessageActivity.class));
                }
            }
        });

        // Get the value of drain switch checking to send the suitable command and save its state in preferences
        drainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Util.connectAvailable(WaterActivity.this)) {
                    if (isChecked) {
                        new commands().drainOn();
                        sharedPreferences.edit().putBoolean("ischecked2", true).apply();
                    } else {
                        new commands().drainOff();
                        sharedPreferences.edit().putBoolean("ischecked2", false).apply();
                    }
                }else {
                    startActivity(new Intent(WaterActivity.this,ConnectionMessageActivity.class));
                }
            }
        });

    }
    public void Mic(){
        if (Util.connectAvailable(WaterActivity.this)) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 10);
            } else {
                Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
            }
        }else {
            startActivity(new Intent(WaterActivity.this,ConnectionMessageActivity.class));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if (resultCode == RESULT_OK && data != null) {
                String result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toString();
                result = result.toLowerCase();
                if (result.contains("water") || result.contains("valve")) {
                    if (result.contains("on") || result.contains("open")) {
                        if(!valveSwitch.isChecked()) {
                            valveSwitch.setChecked(true);
                            sharedPreferences.edit().putBoolean("ischecked1", true).apply();
                        }
                    } else if(result.contains("off") || result.contains("close")) {
                        if(valveSwitch.isChecked()) {
                            valveSwitch.setChecked(false);
                            sharedPreferences.edit().putBoolean("ischecked1", false).apply();
                        }
                    }
                } else if (result.contains("drain")) {
                    if (result.contains("on") || result.contains("open")) {
                        if(!drainSwitch.isChecked()) {
                            sharedPreferences.edit().putBoolean("ischecked2", true).apply();
                            drainSwitch.setChecked(true);
                        }
                    } else if(result.contains("off") || result.contains("close")) {
                        if(drainSwitch.isChecked()) {
                            sharedPreferences.edit().putBoolean("ischecked2", false).apply();
                            drainSwitch.setChecked(false);
                        }
                    }
                }
            }
        }
    }
}