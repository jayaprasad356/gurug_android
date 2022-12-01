package com.gmworks.gurug.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gmworks.gurug.R;
import com.gmworks.gurug.helper.ApiConfig;
import com.gmworks.gurug.helper.Constant;
import com.gmworks.gurug.helper.Session;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MobileNumberActivity extends AppCompatActivity {
    Button otpButton;
    Activity activity;
    Session session;
    ProgressDialog dialog;
    String countryCode = "IN", phoneNumber, otpFor = "", firebase_otp = "", from;
    EditText mobilNumber;
    FirebaseAuth auth;
    String otp;
    TextView tvLogin;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_number);
        otpButton = findViewById(R.id.otp_button);
        activity = MobileNumberActivity.this;
        mobilNumber = findViewById(R.id.edtMobileVerify);
        tvLogin = findViewById(R.id.tvLogin);
        session = new Session(activity);
        from = getIntent().getStringExtra(Constant.FROM);

        otpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), OtpActivity.class);
                intent.putExtra(Constant.MOBILE,mobilNumber.getText().toString());
                startActivity(intent);
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SignInActivity.class);
                startActivity(intent);

            }
        });

    }


}