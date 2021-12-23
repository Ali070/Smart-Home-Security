package com.example.android.smarthomesecurity.HouseBreakingSubsystem;

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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.smarthomesecurity.Authentication.ConnectionMessageActivity;
import com.example.android.smarthomesecurity.Constants;
import com.example.android.smarthomesecurity.R;
import com.example.android.smarthomesecurity.Util;
import com.example.android.smarthomesecurity.commands;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;

public class HouseBreakingActivity extends AppCompatActivity {
    SwitchCompat alarmSwitch;
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private boolean isChecked;
    Runnable runnable;
    TextView doorState;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.mic_menu,menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if(item.getItemId() == R.id.micItem){
            Mic();
            return true;
        }else if(item.getItemId() == android.R.id.home){
            this.finish();
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_breaking);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        alarmSwitch = findViewById(R.id.AlarmHouseswitch);
        doorState = findViewById(R.id.DoorLocked);
        sharedPreferences = this.getSharedPreferences("com.example.Smarthomesecurity", Context.MODE_PRIVATE);
        isChecked = sharedPreferences.getBoolean("ischecked",false);
        if(isChecked){
            alarmSwitch.setChecked(true);
        }else{
            alarmSwitch.setChecked(false);
        }

        // Get the value of alarm switch checking to send the suitable command and save its state in preferences
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(Util.connectAvailable(HouseBreakingActivity.this)) {
                    if (isChecked) {
                        Log.i("checked", "true");
                        new commands().fireAlarmOn();
                        sharedPreferences.edit().putBoolean("ischecked", true).apply();

                    } else {
                        Log.i("checked", "false");
                        new commands().fireAlarmOff();
                        sharedPreferences.edit().putBoolean("ischecked", false).apply();
                    }
                }else{
                    startActivity(new Intent(HouseBreakingActivity.this, ConnectionMessageActivity.class));
                }
            }
        });

        // Get data and apply it on UI each 2 seconds
         runnable = new Runnable() {
            @Override
            public void run() {
                if(Util.connectAvailable(HouseBreakingActivity.this)) {
                    new Door(HouseBreakingActivity.this).execute("https://cloud.kaaiot.com/epts/api/v1/applications/" + Constants.appName + "/time-series/last?endpointId=" + Constants.endpointId_Fire + "&timeSeriesName=" + Constants.paramDoor);
                    handler.postDelayed(this, 2000);
                }else{
                    startActivity(new Intent(HouseBreakingActivity.this, ConnectionMessageActivity.class));
                }
            }
        };
        runnable.run();
    }

    // sending a command to open the door
    public void openDoor(View view){
        if(doorState.getText().equals("Locked")) {
            new commands().openDoor();
        }
    }
    public void closeDoor(View view){
       new commands().closeDoor();
    }
    public void Mic(){
        if(Util.connectAvailable(HouseBreakingActivity.this)) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 10);
            } else {
                Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
            }
        }else{
            startActivity(new Intent(HouseBreakingActivity.this, ConnectionMessageActivity.class));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            if (resultCode == RESULT_OK && data != null) {
                String result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toString();
                result = result.toLowerCase();
                if ( result.contains("alarm")) {
                    if (result.contains("on") || result.contains("open")) {
                        if(!alarmSwitch.isChecked()) {
                            alarmSwitch.setChecked(true);
                            sharedPreferences.edit().putBoolean("ischecked", true).apply();
                        }
                    } else if(result.contains("off") || result.contains("close")) {
                        if(alarmSwitch.isChecked()) {
                            alarmSwitch.setChecked(false);
                            sharedPreferences.edit().putBoolean("ischecked", false).apply();
                        }
                    }
                }
                if(result.contains("house") || result.contains("door")){
                    if (result.contains("on") || result.contains("open")) {
                        if(doorState.getText().equals("Locked")) {
                            new commands().openDoor();
                        }

                    }
                }
            }
        }
    }
}