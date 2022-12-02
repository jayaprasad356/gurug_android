package com.webapster.gurug.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.webapster.gurug.R;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.DatabaseHelper;
import com.webapster.gurug.helper.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {
    TextView tvRegister;
    Activity activity;
    private Session session;
    private DatabaseHelper databaseHelper;
    String from;
    Button signinBtn;
    EditText etMobileNumber, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        tvRegister = findViewById(R.id.register);
        activity=SignInActivity.this;
        databaseHelper = new DatabaseHelper(activity);
        session = new Session(activity);
        from = getIntent().getStringExtra(Constant.FROM);
        etMobileNumber = findViewById(R.id.edtLoginMobile);
        signinBtn=findViewById(R.id.btnLogin);
        etPassword = findViewById(R.id.imgLoginPassword);
        signinBtn.setOnClickListener(view -> UserLogin(etMobileNumber.getText().toString(), etPassword.getText().toString()));
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MobileNumberActivity.class);
                startActivity(intent);
            }
        });

    }

    public void UserLogin(String mobile, String password) {

        Map<String, String> params = new HashMap<>();
        params.put(Constant.LOGIN, Constant.GetVal);
        params.put(Constant.MOBILE, mobile);
        params.put(Constant.PASSWORD, password);
        params.put(Constant.FCM_ID, "" + session.getData(Constant.FCM_ID));
        ApiConfig.RequestToVolley((result, response) -> {

            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        StartMainActivity(jsonObject.getJSONArray(Constant.DATA).getJSONObject(0), password);
                    }
                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.LOGIN_URL, params, true);
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
            if (from != null && from.equals("checkout")) {
                intent.putExtra("total", ApiConfig.StringFormat("" + Constant.FLOAT_TOTAL_AMOUNT));
                intent.putExtra(Constant.FROM, from);
            } else if (from != null && from.equals("tracker")) {
                intent.putExtra(Constant.FROM, "tracker");
            }
            startActivity(intent);

            finish();
        } catch (JSONException e) {
            e.printStackTrace();
            e.printStackTrace();
        }

    }
}