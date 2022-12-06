package com.webapster.gurug.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.webapster.gurug.R;

public class PasswordActivity extends AppCompatActivity {

    Button btnChangePassword;
    Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        activity = PasswordActivity.this;


        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity,MobileNumberActivity.class);
                startActivity(intent);
            }
        });
    }
}