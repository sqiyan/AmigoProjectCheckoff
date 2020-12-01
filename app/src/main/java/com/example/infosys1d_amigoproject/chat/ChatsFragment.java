package com.example.infosys1d_amigoproject.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.infosys1d_amigoproject.R;
import com.example.infosys1d_amigoproject.chat.ActiveChatsFragment;
import com.example.infosys1d_amigoproject.chat.UsersFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.infosys1d_amigoproject.models.Userdataretrieval;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;


public class ChatsFragment extends Fragment {


    private static final String TAG = "ChatActivity";


    private Context mContext = getContext();

    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference myRef;
    ArrayList<Userdataretrieval> mUsers = new ArrayList<>();

    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthstatelistner;

    FirebaseUser fuser;

    FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();

    ImageButton btn_send;
    EditText text_send;

    TabLayout tabLayout;
    ViewPager viewPager;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.activity_chat, container, false);



        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebasedatabase = FirebaseDatabase.getInstance();
        myRef = mFirebasedatabase.getReference("users_display").child(fuser.getUid());

        tabLayout = view.findViewById(R.id.tablayout);
        viewPager = view.findViewById(R.id.viewpagerchat);


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getFragmentManager());


        tabLayout.setupWithViewPager(viewPager);

        viewPagerAdapter.addFragment(new ActiveChatsFragment(), "Chats");
        viewPagerAdapter.addFragment(new UsersFragment(), "Users");
        viewPager.setAdapter(viewPagerAdapter);

        return view;
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }



        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment frag, String title){
            fragments.add(frag);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}