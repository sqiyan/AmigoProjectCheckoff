package com.example.infosys1d_amigoproject.profilemanagement_tab;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys1d_amigoproject.adapter.MyAdapter;
import com.example.infosys1d_amigoproject.R;
import com.example.infosys1d_amigoproject.Utils.FirebaseMethods;
import com.example.infosys1d_amigoproject.models.Userdataretrieval;
import com.example.infosys1d_amigoproject.models.users_display;
import com.example.infosys1d_amigoproject.models.users_private;
import com.example.infosys1d_amigoproject.projectmanagement_tab.Project;
import com.example.infosys1d_amigoproject.signinsignup.SignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

//Fragment to display the user's profile.
public class profilefragment extends Fragment {
    MyAdapter myAdapter;
    private static final String TAG = "profilefragment";
    private TextView mName, mBio, mAboutme, mlookingfor,  muserid, memail;
    private ImageView mProfilepic;
    private Button changeProfilePic, signoutbutton;
    private Context mcontext;
    private ImageButton editProfile;
    private ChipGroup mskills;

    //Firebase Database
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference databaseReference;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();


    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthstatelistner;
    private FirebaseMethods firebaseMethods;


    private Uri imageUri, downloadUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container,false);


        Log.d(TAG, "onCreateView: init widgets");

        mName = view.findViewById(R.id.profilenametextview);
        mAboutme = view.findViewById(R.id.profileaboutmetextview);
        mlookingfor = view.findViewById(R.id.profilelookingfortextview);
        mProfilepic = view.findViewById(R.id.profilepic);
        mcontext = getActivity();
        memail = view.findViewById(R.id.profileemailtextview);
        changeProfilePic = view.findViewById(R.id.Changepicturebutton);
        mskills = view.findViewById(R.id.profileskillchipsgroup);
        editProfile = view.findViewById(R.id.editprofilebutton);
        signoutbutton = view.findViewById(R.id.signoutbutton);

        Log.d(TAG, "onCreateView: widgets inited");


        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseMethods = new FirebaseMethods(mcontext);
        DatabaseReference userref = FirebaseDatabase.getInstance().getReference().child("users_display").child(FirebaseAuth.getInstance().getUid());
        userref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users_display user = snapshot.getValue(users_display.class);
                Picasso.get().load(user.getProfile_picture()).into(mProfilepic);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setupfirebaseauth();


        //OnClickListeners
        signoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(mcontext, SignIn.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick, navigating to : edit profile fragment");
                editprofilefragment fragment = new editprofilefragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack("editprofilefragment");
                transaction.commit();
            }
        });

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(gallery,1);

            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data!=null && data.getData() != null){
            imageUri = data.getData();
            mProfilepic.setImageURI(imageUri);
            uploadpicture();
        }
    }
    private void uploadpicture() {
        StorageReference newRef = storageReference.child("images/" + firebaseMethods.getUserID());
        newRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        newRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri;
                                firebaseMethods.updateProfilePicture(downloadUrl.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
    private void setProfileWidgets(Userdataretrieval userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieved from firebase database");

        users_display displaydata = userSettings.getUsersdisplay();
        users_private privatedata = userSettings.getUsersprivate();

        mName.setText(displaydata.getName());
        mAboutme.setText(displaydata.getAbout_me());
        mlookingfor.setText(displaydata.getLooking_for());
        memail.setText(privatedata.getEmail());

        ArrayList<String> chipslist = displaydata.getSkills();
        LayoutInflater inflater = LayoutInflater.from(mcontext);

        mskills.removeAllViews();
        for(String text: chipslist){
            Chip newchip = (Chip) inflater.inflate(R.layout.chip_item,null,false);
            newchip.setText(text);
            mskills.addView(newchip);}


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
        if(mAuthstatelistner!=null){
            mAuth.removeAuthStateListener(mAuthstatelistner);
        }
    }

    //FirebaseAuth
    private void setupfirebaseauth(){
        Log.d(TAG, "Setup FirebaseAuth");

        mAuth = FirebaseAuth.getInstance();
        mFirebasedatabase = FirebaseDatabase.getInstance();
        databaseReference = mFirebasedatabase.getReference();

        //check if user is sign in
        mAuthstatelistner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user  = firebaseAuth.getCurrentUser();


                if(user !=null){
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in" +user.getUid());
                }
                else{
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }

            }
        };

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //retrieve user information from database
                setProfileWidgets(firebaseMethods.getUserData(snapshot));
                //retrieve profile pic from database

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}