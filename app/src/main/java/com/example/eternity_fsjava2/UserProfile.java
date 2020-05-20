package com.example.eternity_fsjava2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class UserProfile extends AppCompatActivity {

    private static final String KEY_NAME = "UserFullName";
    private static final String KEY_EMAIL = "UserEmail";
    private static final String KEY_PHONE = "UserPhoneNumber";

    private TextView mFullName;
    private TextView mEmail;
    private TextView mPhone;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mFullName = findViewById(R.id.ProfileNameValue);
        mEmail = findViewById(R.id.ProfileEmailValue);
        mPhone = findViewById(R.id.ProfilePhoneValue);

        TextView mVerifyAccountMessage = findViewById(R.id.verificationMessage);
        Button mVerifyAccountButton = findViewById(R.id.verifyAccountButton);

        String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        FirebaseUser mUser = mAuth.getCurrentUser();

        //Check if the current user Email / Account is verified

        if(!mUser.isEmailVerified()){

            mVerifyAccountMessage.setVisibility(View.VISIBLE);
            mVerifyAccountButton.setVisibility(View.VISIBLE);
            mFullName.setVisibility(View.GONE);
            mEmail.setVisibility(View.GONE);
            mPhone.setVisibility(View.GONE);
        }
        else{
            DocumentReference documentReference = mStore.collection("Users").document(userID);

            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    assert documentSnapshot != null;
                    mPhone.setText(documentSnapshot.getString(KEY_PHONE));
                    mFullName.setText(documentSnapshot.getString(KEY_NAME));
                    mEmail.setText(documentSnapshot.getString(KEY_EMAIL));
                }
            });
        }

    }

    // 19 May 2020 - Added the below code for showing a menu on the Main Activity Screen

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.eternity_menu, menu);

        return true;
    }

    // 19 May 2020 - Processing clicks on Menu Items


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.logOutButton:
                String userEmailID = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
                FirebaseAuth.getInstance().signOut();
                Log.d("ACP", "User signed out...."+userEmailID);
                startActivity(new Intent(getApplicationContext(), LogIn.class));
                return true;

            case R.id.myProfile:
                startActivity(new Intent(getApplicationContext(), UserProfile.class));
                return true;

            case R.id.myHome:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void verifyAccount(View view) {

        FirebaseUser fuser = mAuth.getCurrentUser();

        assert fuser != null;
        fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(UserProfile.this, "Email Verification notification sent...", Toast.LENGTH_SHORT).show();
                Log.d("ACP", "Email Verification notification sent...");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(UserProfile.this, "Failure sending email verification link...."+e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("ACP", "Failure sending email verification link...."+e.toString());
            }
        });
    }
}
