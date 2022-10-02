package com.example.budgetmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText mEmail;
    private EditText mPass;
    private Button btnLogin;
    private TextView mForgetPassword;
    private TextView mSignupHere;
    private CheckBox remember;
    private ProgressDialog mDialog;

    //Firebase stuff
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        mDialog=new ProgressDialog(this);
        LoginDetails();

    }

    /*my Login method (main method) */
    private void LoginDetails() {
        mEmail = findViewById(R.id.email_login);
        mPass = findViewById(R.id.password_login);
        btnLogin = findViewById(R.id.btn_login);
        mForgetPassword = findViewById(R.id.forgot_password);
        mSignupHere = findViewById(R.id.signup_reg);
        remember = findViewById(R.id.checkBox);

        //remember checkbox stuff
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);//this line avoids having the user re-login in everytime if already registered on the app
        String checkbox = preferences.getString("remember", "");
        if (checkbox.equals("true")) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        } else if (!checkbox.equals("false")) {

        }

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { //this line avoids having the user re-login in everytime if already registered on the app
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Remember me Checked..", Toast.LENGTH_SHORT).show();

                } else if (!buttonView.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Remember me Unchecked..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //RegistrationActivity
        mSignupHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));/*This line redirects user to the sign up page*/

            }
        });


        //ResetPasswordActivity
        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ResetActivity.class));
            }
        });

        //Login button...
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Declaring my variables*/
                String email = mEmail.getText().toString().trim();
                String pass = mPass.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email Required...");  /*This if statement displays an error to the user if no email is present */
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    mPass.setError("Password Required...");/*This if statement displays an error to the user if no password is present */
                    return;
                }
                mDialog.setMessage("Processing...");//This displays the processing animation action
                mDialog.show();//This allows the processing animation to be seen

                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        mDialog.dismiss();
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));//this takes you to the home activity

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Login Successful...", Toast.LENGTH_SHORT).show();
                        } else {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Login Failed...", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

            }
        });
    }


        private void checkEmailVerification()
        {
            FirebaseUser firebaseUser=mAuth.getInstance().getCurrentUser();
            Boolean emailflag=firebaseUser.isEmailVerified();
            if(emailflag)
            {
                finish();
                Toast.makeText(getApplicationContext(),"SignUp Successful..",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this,HomeActivity.class));
            }
            else
            {
                Toast.makeText(this,"Please verify your email..",Toast.LENGTH_LONG).show();
                mAuth.signOut();
            }
        }

    }
