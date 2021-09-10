package com.jimboidin.patsays.Auth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.jimboidin.patsays.R;

/*
    SignUpActivity is a simple UI Activity that allows user to create login details
    These details are then passed back to LoginActivity.
 */
public class SignUpActivity extends AppCompatActivity {
    private EditText mUsernameEditText, mEmailEditText, mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUsernameEditText = findViewById(R.id.new_username_edit_text);
        mEmailEditText = findViewById(R.id.new_email_edit_text);
        mPasswordEditText = findViewById(R.id.new_password_edit_text);

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> register());
    }


    // Places the user input details in a Bundle and returns this to LoginActivity
    private void register() {
        Bundle bundle = new Bundle();
        bundle.putString("username", mUsernameEditText.getText().toString());
        bundle.putString("email", mEmailEditText.getText().toString());
        bundle.putString("password", mPasswordEditText.getText().toString());

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", bundle);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    // return the RESULT_CANCELED to LoginActivity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}