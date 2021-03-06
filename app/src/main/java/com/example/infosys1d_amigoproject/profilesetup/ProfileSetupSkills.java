package com.example.infosys1d_amigoproject.profilesetup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.infosys1d_amigoproject.MainActivity;
import com.example.infosys1d_amigoproject.R;
import com.example.infosys1d_amigoproject.Utils.FirebaseMethods;
import com.example.infosys1d_amigoproject.models.Userdataretrieval;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//profile setup skill chips activity
public class ProfileSetupSkills extends AppCompatActivity {

    private static final String TAG = "ProfileSetupAboutMe";
    TextInputLayout aboutme;
    Button nextbutton, prevbutton;

    private Userdataretrieval mUserSettings;

    //Firebase Database
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference myRef;
    private Context mcontext;
    private ChipGroup mskills;

    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthstatelistner;
    private FirebaseMethods firebaseMethods;
    private String selectedChipData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup_skills);

        aboutme = findViewById(R.id.aboutme);
        nextbutton = findViewById(R.id.nextbuttonaboutme);
        prevbutton = findViewById(R.id.prevbuttonaboutme);
        selectedChipData = "";

        firebaseMethods = new FirebaseMethods(ProfileSetupSkills.this);
        setupfirebaseauth();

        nextbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileSettings();
            }
        });

        String[] skillsList = getApplicationContext().getResources().getStringArray(R.array.skills_list);

        mskills = findViewById(R.id.setSkillChipsgroup);

        LayoutInflater inflater_0 = LayoutInflater.from(getApplicationContext());
        for(String text: skillsList){
            Chip newChip = (Chip) inflater_0.inflate(R.layout.chip_filter,null,false);
            newChip.setText(text);
            mskills.addView(newChip);}


    }

    private void saveProfileSettings() {

        selectedChipData = "";
        for(int i = 0; i<mskills.getChildCount(); i++){
            Chip chip = (Chip)mskills.getChildAt(i);
            if(chip.isChecked()){
                selectedChipData += (chip.getText().toString() + " ");
            }
        }

        final String skillstext = selectedChipData;

        final String aboutmetext = getIntent().getStringExtra("About Me");
        final String lookingfortext = getIntent().getStringExtra("Looking For");
        Intent intent = new Intent(ProfileSetupSkills.this, ProfileSetupProfilePic.class);
        intent.putExtra("About Me", aboutmetext);
        intent.putExtra("Looking For", lookingfortext);
        intent.putExtra("Skills", skillstext);
        startActivity(intent);

    }


    //------------------------------------------ Firebase ----------------------------------------------------------------------------------------------------

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthstatelistner);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthstatelistner != null) {
            mAuth.removeAuthStateListener(mAuthstatelistner);
        }
    }

    //FirebaseAuth
    private void setupfirebaseauth() {
        Log.d(TAG, "Setup FirebaseAuth");

        mAuth = FirebaseAuth.getInstance();
        mFirebasedatabase = FirebaseDatabase.getInstance();
        myRef = mFirebasedatabase.getReference();
        ;
        String userID = mAuth.getCurrentUser().getUid();

        //check if user is sign in
        mAuthstatelistner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in" + user.getUid());
                } else {
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }

            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //retrieve user information from database
                mUserSettings = firebaseMethods.getUserData(snapshot);
                //retrieve profile pic from database

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}