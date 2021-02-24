package com.jimboidin.patsays.Auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.jimboidin.patsays.MainActivity;
import com.jimboidin.patsays.R;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private DatabaseReference mUserDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userIsLoggedIn();

        mEmailEditText = findViewById(R.id.email_edit_text);
        mPasswordEditText = findViewById(R.id.password_edit_text);

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> signInExistingUser());

        Button signUpButton = findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(v -> launchSignUpActivity());

        mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void userIsLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            displayToast("user is logged in");
            Log.d(TAG, "user logged in");
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            displayToast("user is not logged in"); //toasts are for development purposes only
            Log.i(TAG, "user not logged in");
        }

    }

    int LAUNCH_SIGNUP_ACTIVITY = 1;
    private void launchSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivityForResult(intent, LAUNCH_SIGNUP_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!= null){
            Bundle bundle = data.getBundleExtra("result");

            if (requestCode == LAUNCH_SIGNUP_ACTIVITY && bundle != null) {
                if (resultCode == Activity.RESULT_OK) {
                    String email = bundle.getString("email");
                    String password = bundle.getString("password");
                    String username = bundle.getString("username");
                    signUpNewUser(username, email, password);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    Log.w(TAG, "sign up: cancelled");
                }
            }
        }

    }

    private void signUpNewUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && mAuth.getUid() != null) {
                            createUserDB(username, email);
                            Log.d(TAG, "sign up user: success");
                        } else {
                            Log.w(TAG, "sign up user: failure", task.getException());
                        }
                        userIsLoggedIn();
                    }
                });
    }

    private void signInExistingUser() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            createUserDB(email, email);
                            Log.d(TAG, "sign in user: success");
                        } else {
                            Log.w(TAG, "sign in user: failure", task.getException());
                        }
                        userIsLoggedIn();
                    }
                });
    }

    private void createUserDB(String username, String email) {
        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(mAuth.getUid())){
                    mUserDB.child(mAuth.getUid()).child("email").setValue(email);
                    mUserDB.child(mAuth.getUid()).child("username").setValue(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayToast(String message) {
        Toast.makeText(LoginActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}