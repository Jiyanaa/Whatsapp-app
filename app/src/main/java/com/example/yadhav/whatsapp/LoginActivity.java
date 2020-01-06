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

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private Button show;
    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmail,UserPassword;
    private TextView  NeedNewAccountLink,ForgetPasswordLink;
    private ProgressDialog loadingBar;

    private DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        InitializeFields();

        show.setOnClickListener(new showOrHidePassword());//invoking the showOrHidePassword class to show the password

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
    }

    //class to show or hide password on button click in main activity
    class showOrHidePassword implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (show.getText().toString() == "SHOW") {
                UserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                show.setText("HIDE");

            }
            else {

                UserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                show.setText("SHOW");
            }

        }

    }

    private void AllowUserToLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        //authenticate user
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            loadingBar.setTitle("Sign In");
                            loadingBar.setMessage("Please wait...");
                            loadingBar.setCanceledOnTouchOutside(true);
                            loadingBar.show();

                            if(task.isSuccessful()){

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                UserRef.child(currentUserID).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    SendUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this,"Log in Successful...",Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
    }

    private void InitializeFields() {
        show = (Button) findViewById(R.id.show);  //Show button in password
        LoginButton = (Button) findViewById(R.id.login_button);
        PhoneLoginButton = (Button) findViewById(R.id.phone_login_button);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        NeedNewAccountLink = (TextView) findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);

    }

    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(LoginActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerintent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerintent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
