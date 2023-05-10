package com.webapster.gurug.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.webapster.gurug.R;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PasswordActivity extends AppCompatActivity {

    Button btnChangePassword;
    Activity activity;

    String mobileNumber;
    EditText etPassword;
    EditText etConfirmPassword;
    String uid ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        activity = PasswordActivity.this;

        Intent intent = getIntent();
         mobileNumber = intent.getStringExtra(Constant.MOBILE);
         uid = intent.getStringExtra(Constant.ID);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        etPassword = findViewById(R.id.edtPassword);
        etConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etPassword.getText().toString().trim().equals("")) {
                    etPassword.setError(getString(R.string.enter_password));
                } else if (etConfirmPassword.getText().toString().trim().equals("")) {
                    etConfirmPassword.setError(getString(R.string.enter_confirm_password));
                } else if (!etPassword.getText().toString().trim().equals(etConfirmPassword.getText().toString().trim())) {
                    etConfirmPassword.setError(getString(R.string.password_not_match));
                } else {
                    updatePassword();
                }

            }
        });
    }
    public void updatePassword() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.CHANGE_PASSWORD);
        params.put(Constant.USER_ID, uid);
        params.put(Constant.PASSWORD, etPassword.getText().toString().trim());
        ApiConfig.RequestToVolley((result, response) -> {
            Log.d("RESPONSE_USER",response);
            if (result) {
                Intent intent = new Intent(activity,SignInActivity.class);
                startActivity(intent);
                finish();
            }
        }, activity, Constant.REGISTER_URL, params, false);
    }
}