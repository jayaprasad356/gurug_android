package com.webapster.gurug.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.webapster.gurug.R;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.Session;

public class SplashActivity extends Activity {
    Session session;
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = SplashActivity.this;
        session = new Session(activity);
        session.setBoolean("update_skip", false);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));

        setContentView(R.layout.activity_splash);

        Uri data = this.getIntent().getData();

        getShippingType(activity, session, data);
    }

    public void getShippingType(final Activity activity, Session session, Uri data) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SHIPPING_TYPE, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.SHIPPING_TYPE, jsonObject.getString(Constant.SHIPPING_TYPE));

                        if (data == null) {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        } else if (data.isHierarchical()) {
                            switch (data.getPath().split("/")[data.getPath().split("/").length - 2]) {
                                case "seller":
                                case "product":
                                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra(Constant.ID, data.getPath().split("/")[2]);
                                    intent.putExtra(Constant.FROM, data.getPath().split("/")[data.getPath().split("/").length - 2]);
                                    intent.putExtra(Constant.VARIANT_POSITION, 0);
                                    startActivity(intent);
                                    finish();
                                    break; // Handle the item detail deep link
                                case "refer": // Handle the refer deep link
                                    if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                                        ApiConfig.copyToClipboard(activity, activity.getString(R.string.your_friends_code), data.getPath().split("/")[2]);
                                        Intent referIntent = new Intent(this, LoginActivity.class);
                                        referIntent.putExtra(Constant.FROM, "refer");
                                        startActivity(referIntent);
                                        finish();
                                    } else {
                                        startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                        Toast.makeText(activity, activity.getString(R.string.msg_refer), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                default:
                                    startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra(Constant.FROM, "").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        }
                    } else {
                        Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }
}
