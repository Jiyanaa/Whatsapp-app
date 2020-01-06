package com.example.yadhav.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private Button show;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();
        show.setOnClickListener(new showOrHidePassword());//invoking the showOrHidePassword class to show the password


        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }



    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password...",Toast.LENGTH_SHORT).show();
        }
        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

            loadingBar.setTitle("Create New Account");
            loadingBar.setMessage("Please wait, while creating new Account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");

                                RootRef.child("Users").child(currentUserID).child("device_token")
                                .setValue(deviceToken);

                                Toast.makeText(RegisterActivity.this,"Account created successfully...",Toast.LENGTH_LONG).show();
                                SendUserToMainActivity();
                                loadingBar.dismiss();

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });

    }

    private void InitializeFields() {
        show = (Button) findViewById(R.id.showpass);  //Show button in password
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);

        loadingBar = new ProgressDialog(this);

    }

    private void SendUserToLoginActivity() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(RegisterActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    //class to show or hide password on button click in main activity
    class showOrHidePassword implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (show.getText().toString() == "SHOW") {
                UserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                show.setText("HIDE");

            } else {

                UserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                show.setText("SHOW");
            }

        }
    }
}