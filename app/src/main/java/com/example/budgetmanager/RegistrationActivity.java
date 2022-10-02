package com.example.budgetmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.budgetmanager.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegistrationActivity extends AppCompatActivity {
    /*These are my variables being declared*/
    private EditText mEmail;
    private EditText mPass;
    private Button  btnReg;
    private TextView mSignin;

// Firebase authentication variable
    private FirebaseAuth mAuth;

    private ProgressDialog mDialog;







    @Override
    /*This is where my methods are called*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //This Initializes Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mDialog=new ProgressDialog(this);

        registration();
    }

    /*my Registration method (main method) */
    private void registration () {
        mEmail=findViewById(R.id.email_reg);
        mPass=findViewById(R.id.password_reg);
        btnReg=findViewById(R.id.btn_reg);
        mSignin=findViewById(R.id.signin_here);
        mSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));//This redirects you to the main activity for signing in.
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmail.getText().toString().trim();
                String pass=mPass.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email Required...");  /*This if statement displays an error to the user if no email is present */
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    mPass.setError("Password Required...");/*This if statement displays an error to the user if no password is present */
                    return;
                }
                if(pass.length()<6)
                {
                    mPass.setError("Must contain at least 6 characters..",null);
                    return;
                }

                mDialog.setMessage("Processing..."); // shows processing animation dialog
                mDialog.show();

                mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            sendEmailVerification();
                            mEmail=findViewById(R.id.email_reg);
                            String email=mEmail.getText().toString().trim();
                            FirebaseUser mUser=mAuth.getCurrentUser();
                            if(mAuth!=null) {
                                String uid = mUser.getUid();
                                DatabaseReference myRootRef = FirebaseDatabase.getInstance().getReference().child("UserInfo").child(uid);
                                DatabaseReference userNameRef =  myRootRef.child("Email");
                                userNameRef.setValue(email);
                            }
                        }

                        else {
                            mDialog.dismiss();

                            Toast.makeText(getApplicationContext(),"Registration failed..",Toast.LENGTH_SHORT).show();//toast displays this error message to the user

                        }
                    }

                });
            }
        });
    }
    //new line added
    private void sendEmailVerification()
    {
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Registration Successful.Verification email sent..",Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        finish();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));// on completing the email verification this statement redirects user to the login activity(Main activity)
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Error occurred sending verification email..",Toast.LENGTH_LONG).show(); //toast displays this error message to the user
                    }
                }
            });
        }
    }
}