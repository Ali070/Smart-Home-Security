package com.example.android.smarthomesecurity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.smarthomesecurity.Authentication.LoginActivity;

public class SplashScreen extends AppCompatActivity {
    private TextView tvSplash ;
    private ImageView ivSplash;
    private Animation anim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        tvSplash = findViewById(R.id.tvSplash);
        ivSplash = findViewById(R.id.ivSplash);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        anim = AnimationUtils.loadAnimation(this,R.anim.splash_anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        tvSplash.startAnimation(anim);
        ivSplash.startAnimation(anim);
    }
}