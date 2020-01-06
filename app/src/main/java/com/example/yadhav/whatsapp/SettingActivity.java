package com.example.yadhav.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private Toolbar SettingsToolbar;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private final static int GallaryPick = 1;
    private StorageReference UserProfileImageRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        //keep offline capabilities
        RootRef.keepSynced(true);

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetriveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallaryIntent,"Select Image") ,GallaryPick);
            }
        });
    }


    private void InitializeFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_setting_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GallaryPick && resultCode == RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingActivity.this, "Profile Image uploaded successfully...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            RootRef.child("Users").child(currentUserID).child("image")
                                    .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SettingActivity.this, "Image save in database, successfully...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }

                                }
                            });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });

            }

        }
    }

    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(SettingActivity.this,"Please write your user name...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(SettingActivity.this,"Please write your status...",Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this,"Profile Updated Successfully...",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this,"Error:"+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void RetriveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image")))
                        {
                            final String retriveProfileImage = dataSnapshot.child("image").getValue().toString();
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveStatus = dataSnapshot.child("status").getValue().toString();

                            //Picasso.get().load(retriveProfileImage).into(userProfileImage);
                            Picasso.get().load(retriveProfileImage).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.profile_image).into(userProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(retriveProfileImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                                }
                            });

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);


                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);


                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this,"Please, set or update your profile...",Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(SettingActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }



}
