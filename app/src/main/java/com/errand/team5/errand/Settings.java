package com.errand.team5.errand;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * TODO All of the settings
 */
public class Settings extends Fragment {
    private static String userIDs;
    private static String electronicMails;
    private static String photos;
    private static String nameOfUsers;



    public Settings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        userIDs = MainActivity.getUID();

        DatabaseReference s1 = FirebaseDatabase.getInstance().getReference();
        DatabaseReference s2 = s1.child("errand-9c52d/users/");
        s2.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                Long score = (Long) data.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

    }

}
