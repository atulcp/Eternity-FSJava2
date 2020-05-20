package com.example.eternity_fsjava2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetAndTrackHabits extends AppCompatActivity {

    private static final String KEY_NAME = "UserFullName";
    private static final String KEY_USER_ID = "UserID";

    private static final String KEY_HABIT_NAME = "HabitName";


    private TextView mFullName;
    private EditText mHabitName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_and_track_habits);


        mFullName = findViewById(R.id.ProfileNameValue);
        mHabitName = findViewById(R.id.habitName);

        String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Log.d("ACP", "Entering Set Habit Screen For...."+mAuth.getCurrentUser().getEmail());

        DocumentReference documentReference = mStore.collection("Users").document(userID);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                mFullName.setText(documentSnapshot.getString(KEY_NAME));

            }
        });
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

    // Save the Habit Name in the database

    public void setHabit(View view) {

        //Extract information entered in the text fields

        final String userHabitName  = mHabitName.getText().toString();

        // Perform Validations on the fields

        // Validation 1: Check if the Habit Name is blank

        if(TextUtils.isEmpty(userHabitName)){
            mHabitName.setError("Habit Name is required");
            return;
        }

        // Validation 2: Check if the Habit Name is minimum 3 characters
        if(userHabitName.length()< 3){
            mHabitName.setError("Habit Name must be at least 3 characters");
        }

        // Check if the current user is logged in and then save the Habit Name in the database

        final String UserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        final Map<String, Object> userHabit = new HashMap<>();

        userHabit.put(KEY_USER_ID, UserID);
        userHabit.put(KEY_HABIT_NAME, userHabitName);



        mStore.collection("Habits").document().set(userHabit)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SetAndTrackHabits.this, "User HABIT Saved...", Toast.LENGTH_SHORT).show();
                        Log.d("ACP", "User HABIT Saved for user..."+ userHabit.get(KEY_USER_ID) + " Habit Name..." + userHabit.get(KEY_HABIT_NAME));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetAndTrackHabits.this, "Failure...."+e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("ACP", "Failure...."+e.toString());
            }
        });

    }
}
