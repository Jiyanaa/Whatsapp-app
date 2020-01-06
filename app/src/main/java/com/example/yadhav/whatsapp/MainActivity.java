package com.example.yadhav.whatsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static com.example.yadhav.whatsapp.R.id.main_logout_option;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdaptor myTabsAccessorAdaptor;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference RootRef;
    private DatabaseReference mUserRef;//code
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        //code
        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ChatApp");
        mToolbar.setTitleTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorAccent));

        myViewPager = (ViewPager)findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdaptor = new TabsAccessorAdaptor(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdaptor);

        myTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // user auth state is changed - user is null
            // launch login activity
            SendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");

            VerifyUserExistance();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT);
                }
                else
                {
                    SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.main_logout_option:
                updateUserStatus("offline");

                mAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;

            case R.id.main_find_friends_option:
                SendUserToFindFriendsActivity();
                break;

            case R.id.main_settings_option:
                SendUserToSettingActivity();
                break;

            case R.id.main_create_group_option:
               RequestNewGroup();
                break;

            default:
                break;

        }
        return true;
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Best Friends");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this,"Please write group name...",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName)
    {
        RootRef.child("Groups").child(groupName).setValue("")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this,groupName+" is created successfully...",Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void SendUserToLoginActivity() {
        Intent loginintent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginintent);
    }

    private void SendUserToSettingActivity() {
        Intent settingintent = new Intent(MainActivity.this,SettingActivity.class);
        startActivity(settingintent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findfriendsintent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findfriendsintent);
    }

    private void SendUserToSearchActivity() {
        Intent searchintent = new Intent(MainActivity.this,SearchActivity.class);
        startActivity(searchintent);
    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }
}
