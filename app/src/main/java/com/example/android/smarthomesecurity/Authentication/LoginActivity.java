package com.example.android.smarthomesecurity.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.smarthomesecurity.MainActivity;
import com.example.android.smarthomesecurity.R;
import com.example.android.smarthomesecurity.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPass;
    private String email,password;
    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.restEmail);
        etPass = findViewById(R.id.lgnPassword);
        progressBar = findViewById(R.id.progBar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
    }
    public void gotoSignUp(View view){
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
    public void resetPassword(View view){
        Intent intent = new Intent(LoginActivity.this, resetPasswordActivity.class);
        startActivity(intent);
    }
    public void Login(View view){
        email = etEmail.getText().toString().trim();
        password = etPass.getText().toString().trim();
        if (email.equals("")){
            etEmail.setError(getString(R.string.enter_email));
            etEmail.requestFocus();
        }
        else if (password.equals("")){
            etPass.setError(getString(R.string.enter_pass));
            etPass.requestFocus();
        }
        else {
            if (Util.connectAvailable(this)) {
                progressBar.setVisibility(View.VISIBLE);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            try {
                                throw task.getException();
                            }  catch(FirebaseAuthInvalidCredentialsException e) {
                                etPass.setError(getString(R.string.error_invalid_password));
                                etPass.requestFocus();
                            } catch (FirebaseAuthInvalidUserException e){
                                etEmail.setError(getString(R.string.error_User_notfound));
                                etEmail.requestFocus();
                            }
                            catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }else{
                startActivity(new Intent(LoginActivity.this, ConnectionMessageActivity.class));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }
    }
}