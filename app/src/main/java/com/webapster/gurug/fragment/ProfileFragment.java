package com.webapster.gurug.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.webapster.gurug.R;
import com.webapster.gurug.com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.webapster.gurug.com.darsh.multipleimageselect.helpers.Constants;
import com.webapster.gurug.com.darsh.multipleimageselect.models.Image;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.Session;
import com.webapster.gurug.helper.Utils;

public class ProfileFragment extends Fragment {

    public ImageView imgProfile;
    public FloatingActionButton fabProfile;
    public ProgressBar progressBar;
    View root;
    TextView tvChangePassword;
    Session session;
    Button btnSubmit;
    Activity activity;
    EditText edtName, edtEmail, edtMobile, edtOldPassword, edtNewPassword, edtConfirmNewPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_profile, container, false);
        activity = getActivity();

        edtName = root.findViewById(R.id.edtName);
        edtEmail = root.findViewById(R.id.edtEmail);
        edtMobile = root.findViewById(R.id.edtMobile);
        btnSubmit = root.findViewById(R.id.btnSubmit);
        tvChangePassword = root.findViewById(R.id.tvChangePassword);
        fabProfile = root.findViewById(R.id.fabProfile);
        progressBar = root.findViewById(R.id.progressBar);

        edtOldPassword = root.findViewById(R.id.edtOldPassword);
        edtNewPassword = root.findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = root.findViewById(R.id.edtConfirmNewPassword);

        setHasOptionsMenu(true);

        session = new Session(activity);

        imgProfile = root.findViewById(R.id.imgProfile);

        Glide.with(activity).load(session.getData(Constant.PROFILE))
                .centerInside()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(imgProfile);

        fabProfile.setOnClickListener(view -> {
            Intent intent = new Intent(activity, AlbumSelectActivity.class);
            //set limit on number of images that can be selected, default is 10
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
            startActivityForResult(intent, Constants.REQUEST_CODE);
        });

        tvChangePassword.setOnClickListener(v -> OpenBottomDialog(activity));

        btnSubmit.setOnClickListener(view -> {
            final String name = edtName.getText().toString();
            final String email = edtEmail.getText().toString();
            final String mobile = edtMobile.getText().toString();

            if (ApiConfig.CheckValidation(name, false, false)) {
                edtName.requestFocus();
                edtName.setError(getString(R.string.enter_name));
            } else if (ApiConfig.CheckValidation(email, false, false)) {
                edtEmail.requestFocus();
                edtEmail.setError(getString(R.string.enter_email));
            } else if (ApiConfig.CheckValidation(email, true, false)) {
                edtEmail.requestFocus();
                edtEmail.setError(getString(R.string.enter_valid_email));
            } else {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.TYPE, Constant.EDIT_PROFILE);
                params.put(Constant.USER_ID, session.getData(Constant.ID));
                params.put(Constant.NAME, name);
                params.put(Constant.EMAIL, email);
                params.put(Constant.MOBILE, mobile);
                params.put(Constant.FCM_ID, session.getData(Constant.FCM_ID));
                //System.out.println("====update res " + params.toString());
                ApiConfig.RequestToVolley((result, response) -> {
                    //System.out.println ("=================* " + response);
                    if (result) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean(Constant.ERROR)) {
                                session.setData(Constant.NAME, name);
                                session.setData(Constant.EMAIL, email);
                                session.setData(Constant.MOBILE, mobile);

                            }
                            Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                }, activity, Constant.REGISTER_URL, params, true);
            }


        });

        edtName.setText(session.getData(Constant.NAME));
        edtEmail.setText(session.getData(Constant.EMAIL));
        edtMobile.setText(session.getData(Constant.MOBILE));

        return root;
    }

    public void OpenBottomDialog(final Activity activity) {
        try {
            View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(requireActivity(), R.style.BottomSheetTheme);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            EditText edtOldPassword = sheetView.findViewById(R.id.edtOldPassword);
            EditText edtNewPassword = sheetView.findViewById(R.id.edtNewPassword);
            EditText edtConfirmNewPassword = sheetView.findViewById(R.id.edtConfirmNewPassword);
            ImageView imgChangePasswordClose = sheetView.findViewById(R.id.imgChangePasswordClose);
            Button btnChangePassword = sheetView.findViewById(R.id.btnChangePassword);

            edtOldPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
            edtNewPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);
            edtConfirmNewPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pass, 0, R.drawable.ic_show, 0);

            Utils.setHideShowPassword(edtOldPassword);
            Utils.setHideShowPassword(edtNewPassword);
            Utils.setHideShowPassword(edtConfirmNewPassword);
            mBottomSheetDialog.setCancelable(true);


            imgChangePasswordClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

            btnChangePassword.setOnClickListener(view -> {
                String oldpsw = edtOldPassword.getText().toString();
                String password = edtNewPassword.getText().toString();
                String cpassword = edtConfirmNewPassword.getText().toString();

                if (!password.equals(cpassword)) {
                    edtConfirmNewPassword.requestFocus();
                    edtConfirmNewPassword.setError(activity.getString(R.string.pass_not_match));
                } else if (ApiConfig.CheckValidation(oldpsw, false, false)) {
                    edtOldPassword.requestFocus();
                    edtOldPassword.setError(activity.getString(R.string.enter_old_pass));
                } else if (ApiConfig.CheckValidation(password, false, false)) {
                    edtNewPassword.requestFocus();
                    edtNewPassword.setError(activity.getString(R.string.enter_new_pass));
                } else if (!oldpsw.equals(new Session(activity).getData(Constant.PASSWORD))) {
                    edtOldPassword.requestFocus();
                    edtOldPassword.setError(activity.getString(R.string.no_match_old_pass));
                } else {
                    ChangePassword(password);
                }
            });

            mBottomSheetDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void ChangePassword(String password) {

        final Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.CHANGE_PASSWORD);
        params.put(Constant.PASSWORD, password);
        params.put(Constant.USER_ID, session.getData(Constant.ID));

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setTitle(getString(R.string.change_pass));
        alertDialog.setMessage(getString(R.string.reset_alert_msg));
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(getString(R.string.yes), (dialog, which) -> ApiConfig.RequestToVolley((result, response) -> {
            //  System.out.println("=================*change_password " + response);
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        session.logoutUser(activity);
                    }
                    Toast.makeText(activity, object.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, activity, Constant.REGISTER_URL, params, true));
        alertDialog.setNegativeButton(getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.profile);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_logout).setVisible(true);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_layout).setVisible(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == Constants.REQUEST_CODE) {
                ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                UpdateProfile(activity, images.get(0).path);
            }
        }
    }

    public void UpdateProfile(Activity activity, String filePath) {
        Map<String, String> params = new HashMap<>();
        Map<String, String> fileParams = new HashMap<>();
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        fileParams.put(Constant.PROFILE, filePath);
        params.put(Constant.TYPE, Constant.UPLOAD_PROFILE);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.PROFILE, jsonObject.getString(Constant.PROFILE));
                        Glide.with(activity).load(session.getData(Constant.PROFILE))
                                .centerInside()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(imgProfile);

                        Glide.with(activity).load(session.getData(Constant.PROFILE))
                                .centerInside()
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(DrawerFragment.imgProfile);
                    }
                    Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.REGISTER_URL, params, fileParams);
    }
}