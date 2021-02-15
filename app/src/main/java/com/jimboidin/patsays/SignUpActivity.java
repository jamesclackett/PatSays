package com.jimboidin.patsays;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {
    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmailEditText = findViewById(R.id.new_email_edit_text);
        mPasswordEditText = findViewById(R.id.new_password_edit_text);

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> register());
    }

    private void register() {
        Bundle bundle = new Bundle();
        bundle.putString("email", mEmailEditText.getText().toString());
        bundle.putString("password", mPasswordEditText.getText().toString());

        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", bundle);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}