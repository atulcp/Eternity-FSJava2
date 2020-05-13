package com.example.eternity_fsjava2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUp";

    private static final String KEY_NAME = "UserFullName";
    private static final String KEY_EMAIL = "UserEmail";
    private static final String KEY_PHONE = "UserPhoneNumber";

    private EditText mFullName, mEmail, mPassword, mPhone;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private TextView mSignInText;
    private ProgressBar mProgressBar;
    private Button mSignUpButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.Email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phoneNumber);



        mSignUpButton = findViewById(R.id.signUpButton);
        mSignInText = findViewById(R.id.textSignIn);
        mProgressBar = findViewById(R.id.progressBar);

        // When Sign In Text View is clicked

        /*mSignInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LogIn.class));
                finish();
            }
        });*/

    }

    public void goToLogIn(View view){

        startActivity(new Intent(getApplicationContext(), LogIn.class));
        finish();
    }

    public void completeSignUp(View v){

        //Extract information entered in the text fields

        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        final String userFullName = mFullName.getText().toString();
        final String userPhoneNumber = mPhone.getText().toString();

        // Perform Validations on the fields

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

        //Create the user

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        if (authResult.getUser().getUid() !=null){

                            String UserID = mAuth.getCurrentUser().getUid();
                            Toast.makeText(SignUp.this, "User Created...", Toast.LENGTH_SHORT).show();
                            Log.d("ACP", "User Created....");

                            Map<String, Object> profile = new HashMap<>();

                            profile.put(KEY_NAME, userFullName);
                            profile.put(KEY_EMAIL, email);
                            profile.put(KEY_PHONE, userPhoneNumber);

                            mStore.collection("Users").document(UserID).set(profile)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUp.this, "User Profile Saved...", Toast.LENGTH_SHORT).show();
                                    Log.d("ACP", "User Profile Saved..."+ mStore.collection("Users").toString());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignUp.this, "Failure...."+e.toString(), Toast.LENGTH_SHORT).show();
                                    Log.d("ACP", "Failure...."+e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}
