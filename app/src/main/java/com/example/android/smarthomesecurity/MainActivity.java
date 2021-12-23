package com.example.android.smarthomesecurity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.android.smarthomesecurity.Authentication.ConnectionMessageActivity;
import com.example.android.smarthomesecurity.Authentication.LoginActivity;
import com.example.android.smarthomesecurity.FireSubsystem.FireActivity;
import com.example.android.smarthomesecurity.GasSubsystem.GasActivity;
import com.example.android.smarthomesecurity.HouseBreakingSubsystem.HouseBreakingActivity;
import com.example.android.smarthomesecurity.WaterSubsystem.WaterActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private boolean power ;
    private  Button powerbtn;
    private LinearLayout subsystems;
    private ImageView shield;
    private Button micbtn;
    private commands command;
    private Intent intent;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.itemSignout){
            if (Util.connectAvailable(this)) {
                signout();
                closeSystem();
                finish();
                return true;
            }else{
                startActivity(new Intent(MainActivity.this, ConnectionMessageActivity.class));
                return true;
            }
        }else {

            return super.onOptionsItemSelected(item);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        command = new commands();
        powerbtn = findViewById(R.id.homePowerbtn);
        micbtn = findViewById(R.id.homeMicbtn);
        subsystems = findViewById(R.id.systemsLayout);
        shield = findViewById(R.id.mainShieldImage);
        intent = new Intent(this,BackgroundServices.class);

        // get the last state of UI if system was opened or closed
        sharedPreferences = this.getSharedPreferences("com.example.Smarthomesecurity", Context.MODE_PRIVATE);
        power = sharedPreferences.getBoolean("power",false);
        if(power){
            ((MaterialButton)powerbtn).setIconTintResource(R.color.white);
            shield.setVisibility(View.VISIBLE);
            subsystems.setVisibility(View.VISIBLE);
            subsystems.setAlpha(1);
            shield.setAlpha((float) 0.7);
        }else {
            ((MaterialButton)powerbtn).setIconTintResource(R.color.black);
            shield.setVisibility(View.GONE);
            subsystems.setVisibility(View.GONE);
            subsystems.setAlpha(0);
            shield.setAlpha((float) 0);
        }


    }

    public void gotofire(View view){
        startActivity(new Intent(MainActivity.this, FireActivity.class));
    }

    public void gotowater(View view){
        startActivity(new Intent(MainActivity.this, WaterActivity.class));
    }

    public void gotogas(View view){
        startActivity(new Intent(MainActivity.this, GasActivity.class));
    }

    public void gotoاhouseBreaking(View view){
        startActivity(new Intent(MainActivity.this, HouseBreakingActivity.class));
    }


    public void signout(){
        if (Util.connectAvailable(this)) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }else{
            startActivity(new Intent(MainActivity.this, ConnectionMessageActivity.class));
        }
    }

    // Close the security system by stopping the foreground service and prevent getting data from sensors
    public void closeSystem(){
        sharedPreferences.edit().putBoolean("power",false).apply();
        ((MaterialButton)powerbtn).setIconTintResource(R.color.black);
        shield.setVisibility(View.GONE);
        subsystems.setVisibility(View.GONE);
        subsystems.setAlpha(0);
        shield.setAlpha((float) 0);
        power = false;
        stopService(intent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    // Open the security system by starting the foreground service and starts getting data from sensors
    public void openSystem(){
        ((MaterialButton)powerbtn).setIconTintResource(R.color.white);
        sharedPreferences.edit().putBoolean("power",true).apply();
        shield.setVisibility(View.VISIBLE);
        subsystems.setVisibility(View.VISIBLE);
        subsystems.animate().alpha(1).setDuration(2000);
        shield.animate().alpha((float) 0.7).setDuration(2000);
        power = true;
        startForegroundService(intent);
    }

    public void on_of(View view){
        if (Util.connectAvailable(this)) {
            if (power) {
                closeSystem();
            } else {
                openSystem();
            }
        }else{
            startActivity(new Intent(MainActivity.this, ConnectionMessageActivity.class));
        }
    }


    public void Mic(View view){
        if (Util.connectAvailable(this)) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 10);
            } else {
                Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
            }
        }else{
            startActivity(new Intent(MainActivity.this, ConnectionMessageActivity.class));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int count = 0;
        if (requestCode == 10) {
            if (resultCode == RESULT_OK && data != null) {
                String result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).toString();
                result.toLowerCase();
                for(int i=0;i<result.length();i++){
                    if(result.charAt(i)==' '){
                        count++;
                    }
                }
                Log.i("string",result);
                if(result.contains("on") || result.contains("open")){
                    Log.i("Length",Integer.toString(count));
                    if(count>=1) {
                        if(result.contains("system") || result.contains("security")) {
                            if (!power) {
                                openSystem();
                            }
                        }
                    }else {
                        if (!power) {
                            openSystem();
                        }
                    }
                }else if(result.contains("off") || result.contains("close")){
                    Log.i("Length",Integer.toString(count));
                    if(count>=1) {
                        if(result.contains("system") || result.contains("security")) {
                            if(power){
                                closeSystem();
                            }
                        }
                    }else{
                        if(power){
                            closeSystem();
                        }
                    }
                }
            }
        }
    }
}