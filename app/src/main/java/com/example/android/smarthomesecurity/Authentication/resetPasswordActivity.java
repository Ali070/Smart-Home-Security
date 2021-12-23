package com.example.android.smarthomesecurity.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.smarthomesecurity.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class resetPasswordActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private LinearLayout llResetPass, llMessage;
    private TextView tvMessage;
    private String email;
    private Button btonRetry;
    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        progressBar = findViewById(R.id.progBar);
        etEmail = findViewById(R.id.restEmail);
        llResetPass = findViewById(R.id.resetEmailLayout);
        llMessage = findViewById(R.id.resetCounterLayout);
        tvMessage = findViewById(R.id.messageView);
        btonRetry = findViewById(R.id.retryBtn);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
    }

    public void btnClose(View view){
        finish();
    }
    public void Reset(View view){
        email = etEmail.getText().toString().trim();
        if(email.equals("")){
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Invalid email");
            etEmail.requestFocus();
        }else {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseAuth firebaseAuth  = FirebaseAuth.getInstance();
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                    llResetPass.setVisibility(View.GONE);
                    llMessage.setVisibility(View.VISIBLE);
                    if(task.isSuccessful()){
                        tvMessage.setText(getString(R.string.Reset_Password_instructions,email));
                        new CountDownTimer(60000,1000){

                            @Override
                            public void onTick(long millisUntilFinished) {
                                btonRetry.setText(getString(R.string.resend_timer,String.valueOf(millisUntilFinished/1000)));
                                btonRetry.setOnClickListener(null);
                            }

                            @Override
                            public void onFinish() {
                                btonRetry.setText(getString(R.string.retry));
                                btonRetry.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        llResetPass.setVisibility(View.VISIBLE);
                                        llMessage.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }.start();
                    }else {
                        tvMessage.setText(getString(R.string.failed_send_email));
                    }
                }
            });
        }
    }
}