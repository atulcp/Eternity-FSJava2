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
    private static final String KEY_HABIT_NUMBER = "HabitNumber ";
    private static final String KEY_HABIT_DETAIL = "HabitDetail ";

    private TextView mFullName;
    private EditText mHabitName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private int habitNum;

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
        final String userEmail = mAuth.getCurrentUser().getEmail();



        //26May2020 - Logic to find id the document in the Firestore exists and is empty. If empty then set the habit Number to 1, else increment the habit number by 1 before putting the new record

        DocumentReference documentReference = mStore.collection("Habits").document(UserID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    assert document != null;
                    if(document.exists()){
                        Map<String, Object> map = document.getData();
                        assert map != null;
                        if(map.size() == 0){

                            habitNum = 1;
                            Log.d("ACP", "Inside zero map size. The map size is: "+map.size());
                        }else{
                            habitNum = habitNum+1;
                            Log.d("ACP", "Inside non-zero map size. The map size is: "+map.size());
                        }

                    }
                }


            }
        });

        Log.d("ACP", "The Habit Number is: "+habitNum);

        final Map<String, Object> userHabit = new HashMap<>();
        final Map<String, Object> userHabitDetail = new HashMap<>();

        userHabitDetail.put(KEY_HABIT_NUMBER, habitNum);
        userHabitDetail.put(KEY_HABIT_NAME, userHabitName );
        userHabitDetail.put("CreatedOn: ", new Timestamp(new Date()) );


        userHabit.put(KEY_HABIT_DETAIL, userHabitDetail);


        mStore.collection("Habits").document(UserID).set(userHabit, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SetAndTrackHabits.this, "User HABIT Saved...", Toast.LENGTH_SHORT).show();
                        Log.d("ACP", "User HABIT Saved for user..."+ userEmail + " Habit Name..." + userHabitDetail.get(KEY_HABIT_NAME));
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