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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SetAndTrackHabits extends AppCompatActivity {

    private static final String KEY_NAME = "UserFullName";

    private static final String KEY_HABIT_NAME = "HabitName ";
    private static final String KEY_HABIT_DETAIL = "HabitDetail ";
    private static final String KEY_HABIT_COUNT = "HabitNumberCount ";
    private static final String KEY_HABIT_CREATEDON = "HabitCreatedOn ";

    private TextView mFullName;
    private EditText mHabitName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    // Check if the current user is logged in

    final String UserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
    final String userEmail = mAuth.getCurrentUser().getEmail();

    int habitNum =1;
    boolean habitExists = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_and_track_habits);

        mFullName = findViewById(R.id.ProfileNameValue);
        mHabitName = findViewById(R.id.habitName);

        //26 Jun 2020 - suppressing the below logcat message originally created for debugging to check if the user logged in is the current user
        //Log.d("ACP", "Entering Set Habit Screen For...."+ Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());

        DocumentReference documentReference = mStore.collection("Users").document(UserID);

        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                mFullName.setText(documentSnapshot.getString(KEY_NAME));
            }
        });

        checkAndUpdateHabitCounter();

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


        // 03Jun2020: Log a message in the Logcat to check if the latest habitNum is 1 greater than the last HabitCounter count
        // 26 Jun 2020 - Suppressed logcat message originally created for debugging
        // Log.d("ACP", "The Habit Number is now: "+ habitNum);

        // 03Jun2020 - Added userHabitNum Map to capture the Habit Details for each habitNum in a separate Map:
        final Map<String, Object> userHabit = new HashMap<>();
        final Map<String, Object> userHabitDetail = new HashMap<>();

        //03Jun2020: Step 1- Update the userHabitDetail Map first
        userHabitDetail.put(KEY_HABIT_NAME , userHabitName );
        userHabitDetail.put(KEY_HABIT_CREATEDON, new Timestamp(new Date()) );

        userHabit.put(KEY_HABIT_DETAIL + habitNum , userHabitDetail);
        userHabit.put(KEY_HABIT_COUNT, habitNum);


        if(habitExists){
            checkIfHabitExists(userHabitName);
        }
        else {
            mStore.collection("Habits").document(UserID).set(userHabit, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(SetAndTrackHabits.this, "User HABIT Saved...", Toast.LENGTH_SHORT).show();
                            Log.d("ACP", "User HABIT Saved for user..."+ userEmail + " Habit Name..." + userHabitDetail.get(KEY_HABIT_NAME));
                            startActivity(new Intent(getApplicationContext(), SetAndTrackHabits.class));
                            checkIfHabitExists(userHabitName); // 24 Jun 2020
                            checkAndUpdateHabitCounter();
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

    private void checkAndUpdateHabitCounter() {

        //03Jun2020 - Logic to find if the Habit document in the FireStore exists for the user.
        // If it doesn't exist then set the habitNum to 1.
        // If it exists, then get the latest HabitCounter from the document and set the habitNum to 1 more than the HabitCounter.
        // 26 Jun 2020 - suppressing logcat messages created initially for debugging

        DocumentReference HabitDocRef = mStore.collection("Habits").document(UserID);

        HabitDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //Log.d("ACP", "Inside HabitDocRef....On Success...");
                if(!documentSnapshot.exists()){
                    habitNum = 1;
                    //Log.d("ACP", "Inside HabitDocRef....If document doesn't exist..");
                    habitExists = false;
                }else{
                    int latestCounter = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(documentSnapshot.getData()).get(KEY_HABIT_COUNT)).toString());
                       habitNum = latestCounter + 1;
                    //Log.d("ACP", "Inside HabitDocRef....If document exists and is not null.." +habitNum);
                }
            }
        });

    }

    public void viewHabits(View view){

        startActivity(new Intent(getApplicationContext(), ViewHabits.class));

    }

    private void checkIfHabitExists(final String userHabitName){

        //25 Jun 2020 - Trying to find if the document already contains the Habit Name entered in the Set Habit screen
        DocumentReference documentReference = mStore.collection("Habits").document(UserID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                //25 Jun2020 - adding toUpperCase() method to avoid duplication due to case
                // 26 Jun 2020 - suppressing logcat messages created initially for debugging

                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    //Log.d("ACP", "Checking Exsting document data..." + document.getData().toString());
                    String userHabitNameCaps = userHabitName.toUpperCase();
                    if(document.exists() && document.getData().toString().toUpperCase().contains(userHabitNameCaps)){
                        habitExists = true;
                        mHabitName.setError("Habit already exists in the database...");

                    }else {
                        //Log.d("ACP", "Habit doesn't exist in the database....setting the habitExists flag to false..." );
                        habitExists = false;
                    }
                }
            }
        });
    }
}