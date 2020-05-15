package com.example.eternity_fsjava2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NAME = "UserFullName";
    private static final String KEY_EMAIL = "UserEmail";
    private static final String KEY_PHONE = "UserPhoneNumber";

    private TextView mFullName, mEmail, mPhone;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFullName = findViewById(R.id.ProfileNameValue);
        mEmail = findViewById(R.id.ProfileEmailValue);
        mPhone = findViewById(R.id.ProfilePhoneValue);

        String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
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

    public void signOut(View view){

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LogIn.class));

    }
}
