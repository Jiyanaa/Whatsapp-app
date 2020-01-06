package com.example.yadhav.whatsapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton, mChatAddBtn;
    ;
    private EditText userMessageInput;

    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef, UsersRef, GroupMessageKeyRef;
    String currentGroupName, currentUserID, currentUserName, currentDate, currentTime, currentUserImage;

    // Storage Firebase
    private StorageReference mImageStorage;
    private static final int GALLERY_PICK = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gchat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();


        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();
        GetUserInfo();
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallaryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();

                userMessageInput.setText("");


            }
        });
    }

    private void InitializeFields()
    {
        mToolbar = (Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        sendMessageButton = (ImageButton)findViewById(R.id.send_message_button);
        userMessageInput = (EditText)findViewById(R.id.input_group_message);
        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

    }

    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    currentUserName=dataSnapshot.child("name").getValue().toString();
                }
                if(dataSnapshot.hasChild("image")){
                    currentUserImage=dataSnapshot.child("image").getValue().toString();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageInfoToDatabase()
    {
        String messages = userMessageInput.getText().toString();
        String messageKey = mRef.push().getKey();

        if(TextUtils.isEmpty(messages))
        {
            Toast.makeText(GChatActivity.this,"Please write message first...",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate= Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime= Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMessageKey = new HashMap<>();
            mRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = mRef.child(messageKey);

            HashMap<String, Object>messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",messages);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            messageInfoMap.put("image",currentUserImage);
            messageInfoMap.put("type", "text");
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    //Add Image Source Code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            DatabaseReference user_message_push = mRef.child("Groups").child(currentGroupName)
                    .push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("group_message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String messageKey = mRef.push().getKey();
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Calendar calForDate= Calendar.getInstance();
                        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,yyyy");
                        currentDate = currentDateFormat.format(calForDate.getTime());

                        Calendar calForTime= Calendar.getInstance();
                        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                        currentTime = currentTimeFormat.format(calForTime.getTime());

                        HashMap<String,Object> groupMessageKey = new HashMap<>();
                        mRef.updateChildren(groupMessageKey);


                        GroupMessageKeyRef = mRef.child(messageKey);

                        Map messageMap = new HashMap();
                        messageMap.put("name",currentUserName);
                        messageMap.put("message",download_url);
                        messageMap.put("date",currentDate);
                        messageMap.put("time",currentTime);
                        messageMap.put("image",currentUserImage);
                        messageMap.put("type", "image");
                        GroupMessageKeyRef.updateChildren(messageMap);

                        userMessageInput.setText("");
                    }

                }
            });

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<GChat> options =
                new FirebaseRecyclerOptions.Builder<GChat>()
                        .setQuery(mRef,GChat.class)
                        .build();

        FirebaseRecyclerAdapter<GChat, GChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<GChat, GChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull GChatViewHolder holder, final int position, @NonNull GChat model) {
                       holder.setDetails(getApplicationContext(), model.getName(), model.getMessage(), model.getDate(), model.getTime(),model.getImage(), model.getType());
                    }

                    @NonNull
                    @Override
                    public GChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);
                        GChatViewHolder viewHolder = new GChatViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }

}
