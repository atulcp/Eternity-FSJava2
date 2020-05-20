package com.example.eternity_fsjava2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogIn extends AppCompatActivity {

    EditText mEmail, mPassword;
    ProgressBar mProgressbar;
    FirebaseAuth mAuth;
    TextView forgotPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mEmail = findViewById(R.id.loginEmail);
        mPassword = findViewById(R.id.loginPassword);
        mProgressbar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        forgotPasswordLink = findViewById(R.id.ForgotPasswordText);


    }


    //When the Sign Up button is clicked

    public void goToSignUp(View view) {

        startActivity(new Intent(getApplicationContext(), SignUp.class));
    }

    // When the Sign In button is clicked

    public void completeSignIn (View view){

    // Capture email and password provided in the view

        final String email = mEmail.getText().toString();
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
        //19 May 2020 - added email in the Log and Toast messages

        mProgressbar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
        @Override
        public void onSuccess(AuthResult authResult) {
            mProgressbar.setVisibility(View.GONE);
            Toast.makeText(LogIn.this, "User Signed in...."+ email, Toast.LENGTH_SHORT).show();
            Log.d("ACP", "User signed in...."+email);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(LogIn.this, "Error signing in...." +email+"..."+e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("ACP", "Error signing in...."+e.toString());
        }
        });

    }

    // When "Forgot Password ? " text is clikced

    public void forgotPassword(View view){

        final EditText resetEmail = new EditText(view.getContext());

        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
        passwordResetDialog.setTitle("Reset Password ?");
        passwordResetDialog.setMessage("Enter your email to receive link");
        passwordResetDialog.setView(resetEmail);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //send the resetlink to the email entered

                String email = resetEmail.getText().toString();

                //Validate if the email is blank

                if(TextUtils.isEmpty(email)) {
                    resetEmail.setError("Email can't be blank");
                    return;
                }
                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(LogIn.this, "Password Reset Email sent....", Toast.LENGTH_SHORT).show();
                        Log.d("ACP", "Password Reset Email sent....");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(LogIn.this, "Error...." +e.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("ACP", "Error...."+e.toString());

                    }
                });
            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        passwordResetDialog.create().show();
    }
}
