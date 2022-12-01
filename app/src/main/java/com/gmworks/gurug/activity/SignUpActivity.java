package com.gmworks.gurug.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmworks.gurug.R;
import com.gmworks.gurug.helper.ApiConfig;
import com.gmworks.gurug.helper.Constant;
import com.gmworks.gurug.helper.DatabaseHelper;
import com.gmworks.gurug.helper.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    EditText edtName, edtEmail, edtPassword, edtConfirmPassword, edtMobileVerify,edtRefer;
    Button btnRegister;
    CheckBox chPrivacy;
    Activity activity;
    String mobile;
    Session session;
    DatabaseHelper databaseHelper;
    TextView tvLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        activity = SignUpActivity.this;
        databaseHelper = new DatabaseHelper(activity);
        session = new Session(activity);
        mobile = getIntent().getStringExtra(Constant.MOBILE);
        btnRegister = findViewById(R.id.btnRegister);
        edtMobileVerify = findViewById(R.id.edtMobileVerify);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtRefer = findViewById(R.id.edtRefer);
        chPrivacy = findViewById(R.id.chPrivacy);
        tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtName.getText().toString().trim();
                String email = "" + edtEmail.getText().toString().trim();
                final String password = edtPassword.getText().toString().trim();
                String cpassword = edtConfirmPassword.getText().toString().trim();
                if (ApiConfig.CheckValidation(name, false, false)) {
                    edtName.requestFocus();
                    edtName.setError(getString(R.string.enter_name));
                }
//                else if (ApiConfig.CheckValidation(email, false, false)) {
//                    edtEmail.requestFocus();
//                    edtEmail.setError(getString(R.string.enter_email));
//                } else if (ApiConfig.CheckValidation(email, true, false)) {
//                    edtEmail.requestFocus();
//                    edtEmail.setError(getString(R.string.enter_valid_email));
//                }
                else if (ApiConfig.CheckValidation(password, false, false)) {
                    edtConfirmPassword.requestFocus();
                    edtPassword.setError(getString(R.string.enter_pass));
                } else if (ApiConfig.CheckValidation(cpassword, false, false)) {
                    edtConfirmPassword.requestFocus();
                    edtConfirmPassword.setError(getString(R.string.enter_confirm_pass));
                } else if (!password.equals(cpassword)) {
                    edtConfirmPassword.requestFocus();
                    edtConfirmPassword.setError(getString(R.string.pass_not_match));
                }
//                else if (!chPrivacy.isChecked()) {
//                    Toast.makeText(activity, getString(R.string.alert_privacy_msg), Toast.LENGTH_LONG).show();
//                }
                else {
                    UserSignUpSubmit(name, email, password);
                }
            }
        });
    }
    public void UserSignUpSubmit(String name, String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.REGISTER);
        params.put(Constant.NAME, name);
        params.put(Constant.EMAIL, email);
        params.put(Constant.MOBILE, mobile);
        params.put(Constant.PASSWORD, password);
        params.put(Constant.COUNTRY_CODE, session.getData(Constant.COUNTRY_CODE));
        params.put(Constant.FCM_ID, "" + session.getData(Constant.FCM_ID));
        params.put(Constant.FRIEND_CODE, edtRefer.getText().toString().trim());
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        StartMainActivity(jsonObject, password);
                    }
                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.REGISTER_URL, params, true);
    }
    public void StartMainActivity(JSONObject jsonObject, String password) {
        try {
            new Session(activity).createUserLoginSession(jsonObject.getString(Constant.PROFILE)
                    , session.getData(Constant.FCM_ID),
                    jsonObject.getString(Constant.USER_ID),
                    jsonObject.getString(Constant.NAME),
                    jsonObject.getString(Constant.EMAIL),
                    jsonObject.getString(Constant.MOBILE),
                    password,
                    jsonObject.getString(Constant.REFERRAL_CODE));

            ApiConfig.AddMultipleProductInCart(session, activity, databaseHelper.getCartData());
            ApiConfig.AddMultipleProductInSaveForLater(session, activity, databaseHelper.getSaveForLaterData());
            ApiConfig.getCartItemCount(activity, session);

            ArrayList<String> favorites = databaseHelper.getFavorite();
            for (int i = 0; i < favorites.size(); i++) {
                ApiConfig.AddOrRemoveFavorite(activity, session, favorites.get(i), true);
            }

            databaseHelper.DeleteAllFavoriteData();
            databaseHelper.ClearCart();
            databaseHelper.ClearSaveForLater();

            ApiConfig.getWalletBalance(activity, session);
            session.setData(Constant.COUNTRY_CODE, jsonObject.getString(Constant.COUNTRY_CODE));

            MainActivity.homeClicked = false;
            MainActivity.categoryClicked = false;
            MainActivity.favoriteClicked = false;
            MainActivity.cartClicked = false;
            MainActivity.drawerClicked = false;

            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.FROM, "");
//            if (from != null && from.equals("checkout")) {
//                intent.putExtra("total", ApiConfig.StringFormat("" + Constant.FLOAT_TOTAL_AMOUNT));
//                intent.putExtra(Constant.FROM, from);
//            } else if (from != null && from.equals("tracker")) {
//                intent.putExtra(Constant.FROM, "tracker");
//            }
            startActivity(intent);

            finish();
        } catch (JSONException e) {
            e.printStackTrace();
            e.printStackTrace();
        }

    }


}