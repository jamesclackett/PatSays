package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginButton;
    private Button mSignUpButton;
    private DatabaseReference usersDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userIsLoggedIn();

        mEmail = findViewById(R.id.email_edit_text);
        mPassword = findViewById(R.id.password_edit_text);

        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(v -> signInExistingUser());

        mSignUpButton = findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(v -> launchSignUpActivity());

        usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void userIsLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            displayToast("user is logged in");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else
            displayToast("user is not logged in"); //toasts are for development purposes only
    }

    int LAUNCH_SIGNUP_ACTIVITY = 1;

    private void launchSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, LAUNCH_SIGNUP_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getBundleExtra("result");

        if (requestCode == LAUNCH_SIGNUP_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                String email = bundle.getString("email");
                String password = bundle.getString("password");
                signUpNewUser(email, password);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.w(TAG, "signUpNewUser: cancelled");
            }
        }
    }

    private void signUpNewUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            usersDB.child(mAuth.getUid()).child("name").setValue(email);
                            Log.d(TAG, "createUserWithEmail:success");
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        }
                        userIsLoggedIn();
                    }
                });
    }

    private void signInExistingUser() {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            createUserDB(email);
                            Log.d(TAG, "signInWithEmail:success");
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                        userIsLoggedIn();
                    }
                });
    }

    private void createUserDB(String email) {
        usersDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(mAuth.getUid())){
                    usersDB.child(mAuth.getUid()).child("name").setValue(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void displayToast(String message) {
        Toast.makeText(LoginActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}