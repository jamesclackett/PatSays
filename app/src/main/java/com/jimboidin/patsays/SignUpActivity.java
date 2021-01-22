package com.jimboidin.patsays;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {
    private Button mRegister;
    private EditText mEmail;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmail = findViewById(R.id.new_email_edit_text);
        mPassword = findViewById(R.id.new_password_edit_text);
        mRegister = findViewById(R.id.register_button);
        mRegister.setOnClickListener(v -> register());
    }

    private void register() {
        Bundle bundle = new Bundle();
        bundle.putString("email", mEmail.getText().toString());
        bundle.putString("password", mPassword.getText().toString());

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