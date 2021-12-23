package com.example.android.smarthomesecurity.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.android.smarthomesecurity.NodeNames;
import com.example.android.smarthomesecurity.R;
import com.example.android.smarthomesecurity.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etName, etPass, etConfirmPass, etPhoneNumber;
    private String email, name, password, confirmPassword, phoneNumber;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private View progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        progressBar = findViewById(R.id.progBar);
        etEmail = findViewById(R.id.snupEmail);
        etName = findViewById(R.id.snupname);
        etPass = findViewById(R.id.snupPassword);
        etConfirmPass = findViewById(R.id.snupconfirmPass);
        etPhoneNumber = findViewById(R.id.snupphoneNumber);
        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
    }
    private void updateName(){
        progressBar.setVisibility(View.VISIBLE);
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setDisplayName(etName.getText().toString().trim()).build();
        firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    String userID = firebaseUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
                    HashMap<String,String> data = new HashMap<>();
                    data.put(NodeNames.NAME,etName.getText().toString().trim());
                    data.put(NodeNames.EMAIL,etEmail.getText().toString().trim());
                    data.put(NodeNames.PHONE,etPhoneNumber.getText().toString().trim());
                    progressBar.setVisibility(View.VISIBLE);
                    databaseReference.child(userID).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                Toast.makeText(SignUpActivity.this,"User created successfully",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            }else {
                                Toast.makeText(SignUpActivity.this,"SignUp failed: "+task.getException(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(SignUpActivity.this,"Failed to update profile: "+task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void goToLogin(View view){
        Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
        startActivity(intent);
    }
    public void SignUp(View view){
        email = etEmail.getText().toString().trim();
        name = etName.getText().toString().trim();
        password = etPass.getText().toString().trim();
        confirmPassword = etConfirmPass.getText().toString().trim();
        phoneNumber = etPhoneNumber.getText().toString().trim();
        if(name.equals("")){
            etName.setError(getString(R.string.enter_name));
        }
        else if(email.equals("")){
            etEmail.setError(getString(R.string.enter_email));
            etEmail.requestFocus();
        }
        else if(password.equals("")){
            etPass.setError(getString(R.string.enter_pass));
            etPass.requestFocus();
        }
        else if (confirmPassword.equals("")){
            etConfirmPass.setError(getString(R.string.enter_confirm));
            etConfirmPass.requestFocus();
        }
        else if (phoneNumber.equals("")){
            etPhoneNumber.setError(getString(R.string.enter_phone));
            etPhoneNumber.requestFocus();
        }
        else if (!password.equals(confirmPassword)){
            etConfirmPass.setError(getString(R.string.pass_mismatch));
            etConfirmPass.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError(getString(R.string.enter_correct_email));
            etEmail.requestFocus();
        }else {
            if (Util.connectAvailable(this)) {
                progressBar.setVisibility(View.VISIBLE);
                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            firebaseUser = firebaseAuth.getCurrentUser();
                            updateName();
                        } else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                etPass.setError(getString(R.string.error_weak_password));
                                etPass.requestFocus();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                etEmail.setError(getString(R.string.error_invalid_email));
                                etEmail.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e) {
                                etEmail.setError(getString(R.string.error_user_exists));
                                etEmail.requestFocus();
                            } catch (Exception e) {
                                Toast.makeText(SignUpActivity.this, "Login Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }else{
                startActivity(new Intent(SignUpActivity.this, ConnectionMessageActivity.class));
            }
        }
    }
}