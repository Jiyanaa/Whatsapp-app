package com.example.yadhav.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private EditText mSearchField;
    private ImageButton mSearchButton;
    private RecyclerView mResultList;

    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    ArrayList<String> fullNameList;
    ArrayList<String> userStatusList;
    ArrayList<String> profilePicList;

    SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchButton = (ImageButton) findViewById(R.id.search_btn);
        mResultList = (RecyclerView) findViewById(R.id.result_list);
        mResultList.setHasFixedSize(true);
        mResultList.setLayoutManager(new LinearLayoutManager(this));
        mResultList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        fullNameList = new ArrayList<>();
        userStatusList = new ArrayList<>();
        profilePicList = new ArrayList<>();



        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    setAdapter(s.toString());
                }
                else {
                    fullNameList.clear();
                    userStatusList.clear();
                    profilePicList.clear();
                    mResultList.removeAllViews();
                }
            }
        });
    }

    private void setAdapter(final String SearchString) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                fullNameList.clear();
                userStatusList.clear();
                profilePicList.clear();
                mResultList.removeAllViews();
                int counter = 0;

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String uid = snapshot.getKey();
                    String full_name = snapshot.child("name").getValue(String.class);
                    String user_status = snapshot.child("status").getValue(String.class);
                    String profile_pic = snapshot.child("image").getValue(String.class);
                    String user_id = snapshot.child("uid").getValue(String.class);//code

                    if(full_name.toLowerCase().contains(SearchString.toLowerCase())){
                        fullNameList.add(full_name);
                        userStatusList.add(user_status);
                        profilePicList.add(profile_pic);
                        counter++;
                    }
                    else if(user_status.toLowerCase().contains(SearchString.toLowerCase()))
                    {
                        fullNameList.add(full_name);
                        userStatusList.add(user_status);
                        profilePicList.add(profile_pic);
                        counter++;
                    }

                    if(counter == 15)
                    {
                        break;
                    }


                }
                searchAdapter = new SearchAdapter(SearchActivity.this, fullNameList, userStatusList, profilePicList);
                mResultList.setAdapter(searchAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
