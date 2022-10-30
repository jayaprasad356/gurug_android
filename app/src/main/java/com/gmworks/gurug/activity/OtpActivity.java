package com.gmworks.gurug.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmworks.gurug.R;
import com.gmworks.gurug.helper.ApiConfig;
import com.gmworks.gurug.helper.Constant;
import com.gmworks.gurug.helper.Session;
import com.gmworks.gurug.ui.Pinview;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    Button verifyButton;
    String firebase_otp = "";
    Pinview pinViewOTP;
    Activity activity;
    Session session;
    ProgressDialog dialog;
    String phoneNumber, otpFor = "", from, mobile;
    FirebaseAuth auth;
    String countryCode = "IN";

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        verifyButton = findViewById(R.id.btn_verify);
        pinViewOTP = findViewById(R.id.pinViewOTP);
        mobile = getIntent().getStringExtra(Constant.MOBILE);
        from = getIntent().getStringExtra(Constant.FROM);
        activity = OtpActivity.this;
        session = new Session(activity);
        otpFor = "new_user";
        generateOTP(mobile);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otptext = Objects.requireNonNull(pinViewOTP.getValue()).trim();

                OTP_Varification(otptext);

            }
        });
        StartFirebaseLogin();

    }

    public void generateOTP(String mobile) {
        dialog = ProgressDialog.show(activity, "", getString(R.string.please_wait), true);
       // session.setData(Constant.COUNTRY_CODE, countryCode);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.VERIFY_USER);
        params.put(Constant.MOBILE, mobile);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    phoneNumber = ("+91" + mobile);
                    if (otpFor.equals("new_user")) {
                        if (!object.getBoolean(Constant.ERROR)) {
                            dialog.dismiss();
                            Toast.makeText(activity, getString(R.string.alert_not_register_num1) + getString(R.string.app_name) + getString(R.string.alert_not_register_num2), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(activity,SignInActivity.class);
                            startActivity(intent);
                            finish();
                            //setSnackBar(getString(R.string.alert_register_num1) + getString(R.string.app_name) + getString(R.string.alert_register_num2), getString(R.string.btn_ok), from);
                        } else {
                            sentRequest(phoneNumber);
                        }
                    } else if (otpFor.equals("exist_user")) {
                        if (!object.getBoolean(Constant.ERROR)) {
                            Constant.U_ID = object.getString(Constant.ID);
                            sentRequest(phoneNumber);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(activity, getString(R.string.alert_not_register_num1) + getString(R.string.app_name) + getString(R.string.alert_not_register_num2), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(activity,SignInActivity.class);
                            startActivity(intent);
                            finish();
                            //setSnackBar(getString(R.string.alert_not_register_num1) + getString(R.string.app_name) + getString(R.string.alert_not_register_num2), getString(R.string.btn_ok), from);
                        }
                    }
                } catch (JSONException ignored) {
                    System.out.println("error");
                }
            }
        }, activity, Constant.REGISTER_URL, params, false);
    }

    public void setSnackBar(String message, String action, final String type) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(action, view -> {
            if (type.equals("forgot_password")) {
                try {

                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            snackbar.dismiss();
        });

        snackbar.setActionTextColor(Color.RED);
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    public void OTP_Varification(String otptext) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(firebase_otp, otptext);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(activity, "success", Toast.LENGTH_SHORT).show();
                        navigate();
                    } else {
                        //verification unsuccessful.. display an error message
                        String message = "Something is wrong, we will fix it soon...";
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            message = "Invalid code entered...";
                        }
                        //pinViewOTP.requestFocus();
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigate() {
        Intent intent = new Intent(activity, SignUpActivity.class);
        intent.putExtra(Constant.MOBILE,mobile);
        startActivity(intent);
    }

    void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NotNull PhoneAuthCredential phoneAuthCredential) {
                System.out.println("====verification complete call  " + phoneAuthCredential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                setSnackBar(e.getLocalizedMessage(), getString(R.string.btn_ok), Constant.FAILED);
            }

            @Override
            public void onCodeSent(@NotNull String s, @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                dialog.dismiss();
                firebase_otp = s;

            }
        };
    }

    public void sentRequest(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallback)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }
}