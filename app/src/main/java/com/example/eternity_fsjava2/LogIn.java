package com.example.eternity_fsjava2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogIn extends AppCompatActivity {

    EditText mEmail, mPassword;
    ProgressBar mProgressbar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mEmail = findViewById(R.id.loginEmail);
        mPassword = findViewById(R.id.loginPassword);
        mProgressbar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();


    }

    //When the Sign Up button is clicked

    public void goToSignUp(View view) {

        startActivity(new Intent(getApplicationContext(), SignUp.class));
    }

    public void completeSignIn (View view){

    // Capture email and password provided in the view

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

    // Perform Data Validations:

        // Validation 1: Check if the email is blank

        if(TextUtils.isEmpty(email)){
            mEmail.setError("Email is required");
            return;
        }

        //Validation 2: If the password is blank

        if(TextUtils.isEmpty(password)){

            mPassword.setError("Password is required");
            return;
        }

        // Validation 3: If the password is less than 6 characters

        if(password.length()<6){
            mPassword.setError("Password must be at least 6 characters");
            return;
        }

    // Sign in the user

        mProgressbar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {
            mProgressbar.setVisibility(View.GONE);
            Toast.makeText(LogIn.this, "User Signed in....", Toast.LENGTH_SHORT).show();
            Log.d("ACP", "User signed in....");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(LogIn.this, "Error signing in...." +e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("ACP", "Error signing in...."+e.toString());
        }
        });

    }

}
