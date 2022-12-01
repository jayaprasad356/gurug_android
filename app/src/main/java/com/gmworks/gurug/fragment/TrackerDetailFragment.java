package com.gmworks.gurug.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.gmworks.gurug.R;
import com.gmworks.gurug.activity.MainActivity;
import com.gmworks.gurug.adapter.OrderItemsAdapter;
import com.gmworks.gurug.adapter.ProductImagesAdapter;
import com.gmworks.gurug.com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.gmworks.gurug.com.darsh.multipleimageselect.helpers.Constants;
import com.gmworks.gurug.com.darsh.multipleimageselect.models.Image;
import com.gmworks.gurug.helper.ApiConfig;
import com.gmworks.gurug.helper.Constant;
import com.gmworks.gurug.helper.Session;
import com.gmworks.gurug.model.OrderTracker;

@SuppressLint("NotifyDataSetChanged")
public class TrackerDetailFragment extends Fragment {
    public Button btnReorder, btnInvoice;
    View root;
    OrderTracker order;
    public ProgressBar progressBar;
    TextView tvOrderOTP, tvItemTotal, tvDeliveryCharge, tvTotal, tvPromoCode, tvPCAmount, tvWallet, tvFinalTotal, tvDPercent, tvDAmount;
    TextView tvOtherDetail, tvOrderId, tvOrderDate, btnOtherImages, btnSubmit, tvReceiptStatus, tvReceiptStatusReason, tvBankDetail, tvOrderNote;
    RecyclerView recyclerView, recyclerViewImageGallery, recyclerViewReceiptImages;
    RelativeLayout relativeLyt;
    LinearLayout lytPromo, lytWallet, lytPriceDetail, lytOTP, lytOrderNote;
    double totalAfterTax = 0.0;
    Activity activity;
    String id;
    Session session;
    HashMap<String, String> hashMap;
    private ShimmerFrameLayout shimmerFrameLayout;
    ScrollView scrollView;

    LinearLayout lytReceipt_;
    private ArrayList<Image> receiptImages;
    ProductImagesAdapter productImagesAdapter;
    RelativeLayout lytReceipt;

    RelativeLayout lytMainTrackerTimeLine;
    LinearLayout lytTrackerTimeLine;
    ImageView imgTrackerClose;
    RecyclerView recyclerViewTrackerTimeLine;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tracker_detail, container, false);
        activity = getActivity();
        session = new Session(activity);

        lytPriceDetail = root.findViewById(R.id.lytPriceDetail);
        lytPromo = root.findViewById(R.id.lytPromo);
        lytWallet = root.findViewById(R.id.lytWallet);
        tvItemTotal = root.findViewById(R.id.tvItemTotal);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        tvDAmount = root.findViewById(R.id.tvDAmount);
        tvDPercent = root.findViewById(R.id.tvDPercent);
        tvTotal = root.findViewById(R.id.tvTotal);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvPCAmount = root.findViewById(R.id.tvPCAmount);
        tvWallet = root.findViewById(R.id.tvWallet);
        tvFinalTotal = root.findViewById(R.id.tvFinalTotal);
        tvOrderId = root.findViewById(R.id.tvOrderId);
        btnOtherImages = root.findViewById(R.id.btnOtherImages);
        btnSubmit = root.findViewById(R.id.btnSubmit);
        recyclerViewImageGallery = root.findViewById(R.id.recyclerViewImageGallery);
        recyclerViewReceiptImages = root.findViewById(R.id.recyclerViewReceiptImages);
        tvBankDetail = root.findViewById(R.id.tvBankDetail);
        tvOrderNote = root.findViewById(R.id.tvOrderNote);
        tvReceiptStatusReason = root.findViewById(R.id.tvReceiptStatusReason);
        tvReceiptStatus = root.findViewById(R.id.tvReceiptStatus);
        tvOrderDate = root.findViewById(R.id.tvOrderDate);
        relativeLyt = root.findViewById(R.id.relativeLyt);
        tvOtherDetail = root.findViewById(R.id.tvOtherDetail);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setNestedScrollingEnabled(false);
        btnReorder = root.findViewById(R.id.btnReorder);
        btnInvoice = root.findViewById(R.id.btnInvoice);
        tvOrderOTP = root.findViewById(R.id.tvOrderOTP);
        lytOTP = root.findViewById(R.id.lytOTP);
        lytOrderNote = root.findViewById(R.id.lytOrderNote);
        lytReceipt = root.findViewById(R.id.lytReceipt);
        lytReceipt_ = root.findViewById(R.id.lytReceipt_);
        scrollView = root.findViewById(R.id.scrollView);
        shimmerFrameLayout = root.findViewById(R.id.shimmerFrameLayout);
        progressBar = root.findViewById(R.id.progressBar);

        lytMainTrackerTimeLine = root.findViewById(R.id.lytMainTrackerTimeLine);
        lytTrackerTimeLine = root.findViewById(R.id.lytTrackerTimeLine);
        imgTrackerClose = root.findViewById(R.id.imgTrackerClose);
        recyclerViewTrackerTimeLine = root.findViewById(R.id.recyclerViewTrackerTimeLine);

        recyclerViewTrackerTimeLine.setLayoutManager(new LinearLayoutManager(activity));
        Animation animHide = AnimationUtils.loadAnimation(activity, R.anim.view_hide);

        animHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lytMainTrackerTimeLine.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imgTrackerClose.setOnClickListener(v -> {
            lytTrackerTimeLine.setVisibility(View.GONE);
            lytTrackerTimeLine.startAnimation(animHide);
        });

        hashMap = new HashMap<>();

        id = requireArguments().getString(Constant.ID);
        if (id.equals("")) {
            order = (OrderTracker) requireArguments().getSerializable("model");
            id = order.getId();
            SetData(order);
        } else {
            getOrderDetails(id);
        }

        setHasOptionsMenu(true);

        btnReorder.setOnClickListener(view -> new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.re_order))
                .setMessage(getString(R.string.reorder_msg))
                .setPositiveButton(getString(R.string.proceed), (dialog, which) -> {
                    if (activity != null) {
                        GetReOrderData();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss()).show());

        receiptImages = new ArrayList<>();
        recyclerViewImageGallery.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));

        recyclerViewReceiptImages.setLayoutManager(new GridLayoutManager(activity, 3));
        recyclerViewReceiptImages.setNestedScrollingEnabled(false);

        tvBankDetail.setOnClickListener(v -> {
            if (Constant.ACCOUNT_NAME.isEmpty() || Constant.ACCOUNT_NUMBER.isEmpty() || Constant.BANK_NAME.isEmpty() || Constant.BANK_CODE.isEmpty()) {
                getPaymentConfig();
            } else {
                openBankDetails();
            }
        });

        btnOtherImages.setOnClickListener(v -> {
            lytReceipt_.setVisibility(View.VISIBLE);
            Intent intent = new Intent(activity, AlbumSelectActivity.class);
            //set limit on number of images that can be selected, default is 10
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
            startActivityForResult(intent, Constants.REQUEST_CODE);
        });

        btnSubmit.setOnClickListener(v -> {
            if (receiptImages != null && receiptImages.size() > 0) {
                progressBar.setVisibility(View.VISIBLE);
                submitReceipt();
            } else {
                Toast.makeText(activity, activity.getString(R.string.no_receipt_select_message), Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            receiptImages = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            recyclerViewReceiptImages.setAdapter(new ProductImagesAdapter(activity, order.getAttachment(), "api", order.getId()));
        }
    }


    public void getPaymentConfig() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_PAYMENT_METHOD, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        if (jsonObject.has(Constant.PAYMENT_METHODS)) {
                            JSONObject object = jsonObject.getJSONObject(Constant.PAYMENT_METHODS);

                            if (object.has(Constant.direct_bank_transfer_method)) {
                                Constant.DIRECT_BANK_TRANSFER = object.getString(Constant.direct_bank_transfer_method);
                                Constant.ACCOUNT_NAME = object.getString(Constant.account_name);
                                Constant.ACCOUNT_NUMBER = object.getString(Constant.account_number);
                                Constant.BANK_NAME = object.getString(Constant.bank_name);
                                Constant.BANK_CODE = object.getString(Constant.bank_code);
                                Constant.NOTES = object.getString(Constant.notes);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public void openBankDetails() {
        {
            @SuppressLint("InflateParams") View sheetView = activity.getLayoutInflater().inflate(R.layout.dialog_bank_detail, null);
            ViewGroup parentViewGroup = (ViewGroup) sheetView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }

            final Dialog mBottomSheetDialog = new Dialog(activity);
            mBottomSheetDialog.setContentView(sheetView);
            mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mBottomSheetDialog.show();

            TextView tvAccountName = sheetView.findViewById(R.id.tvAccountName);
            TextView tvAccountNumber = sheetView.findViewById(R.id.tvAccountNumber);
            TextView tvBankName = sheetView.findViewById(R.id.tvBankName);
            TextView tvIFSCCode = sheetView.findViewById(R.id.tvIFSCCode);
            TextView tvExtraNote = sheetView.findViewById(R.id.tvExtraNote);

            tvAccountName.setText(Constant.ACCOUNT_NAME);
            tvAccountNumber.setText(Constant.ACCOUNT_NUMBER);
            tvBankName.setText(Constant.BANK_NAME);
            tvIFSCCode.setText(Constant.BANK_CODE);
            tvExtraNote.setText(Constant.NOTES);

            tvAccountName.setOnClickListener(view -> ApiConfig.copyToClipboard(activity, activity.getString(R.string.bank_account_name_).replace(":", ""), tvAccountName.getText().toString()));
            tvAccountNumber.setOnClickListener(view -> ApiConfig.copyToClipboard(activity, activity.getString(R.string.bank_account_number_), tvAccountNumber.getText().toString()));
            tvBankName.setOnClickListener(view -> ApiConfig.copyToClipboard(activity, activity.getString(R.string.bank_name_), tvBankName.getText().toString()));
            tvIFSCCode.setOnClickListener(view -> ApiConfig.copyToClipboard(activity, activity.getString(R.string.bank_ifsc_code_), tvIFSCCode.getText().toString()));
            tvExtraNote.setOnClickListener(view -> ApiConfig.copyToClipboard(activity, activity.getString(R.string.extra_note_), tvExtraNote.getText().toString()));

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void submitReceipt() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart(Constant.AccessKey, Constant.AccessKeyVal);
            builder.addFormDataPart(Constant.UPLOAD_BANK_TRANSFER_ATTACHMENT, Constant.GetVal);
            builder.addFormDataPart(Constant.ORDER_ID, order.getId());

            for (int i = 0; i < receiptImages.size(); i++) {
                File file = new File(receiptImages.get(i).path);
                builder.addFormDataPart(Constant.IMAGES, file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
            }

            RequestBody body = builder.build();

            Request request = new Request.Builder()
                    .url(Constant.ORDER_PROCESS_URL)
                    .method("POST", body)
                    .addHeader(Constant.AUTHORIZATION, "Bearer " + ApiConfig.createJWT("eKart", "eKart Authentication"))
                    .build();

            Response response = client.newCall(request).execute();
            Toast.makeText(activity, new JSONObject(Objects.requireNonNull(response.body()).string()).getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void GetReOrderData() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_REORDER_DATA, Constant.GetVal);
        params.put(Constant.ID, id);

        ApiConfig.RequestToVolley((result, response) -> {

            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONObject(Constant.DATA).getJSONArray(Constant.ITEMS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        hashMap.put(jsonArray.getJSONObject(i).getString(Constant.PRODUCT_VARIANT_ID), jsonArray.getJSONObject(i).getString(Constant.QUANTITY));
                    }
                    ApiConfig.AddMultipleProductInCart(session, activity, hashMap);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    public void getOrderDetails(String id) {
        scrollView.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ORDERS, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.ORDER_ID, id);

        //  System.out.println("=====params " + params.toString());
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        SetData(new Gson().fromJson(jsonObject1.getJSONArray(Constant.DATA).getJSONObject(0).toString(), OrderTracker.class));

                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    scrollView.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, false);
    }

    @SuppressLint("SetTextI18n")
    public void SetData(OrderTracker order) {
        try {
            String[] date = order.getDate_added().split("\\s+");
            tvOrderId.setText(order.getId());
            if (order.getOtp().equals("0")) {
                lytOTP.setVisibility(View.GONE);
            } else {
                tvOrderOTP.setText(order.getOtp());
            }

            if (!order.getOrder_note().isEmpty()) {
                lytOrderNote.setVisibility(View.VISIBLE);
                tvOrderNote.setText(order.getOrder_note());
            } else {
                lytOrderNote.setVisibility(View.GONE);
            }

            btnInvoice.setOnClickListener(v -> {
                Fragment fragment = new WebViewFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", activity.getString(R.string.order) + "#" + order.getId());
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            tvReceiptStatus.setText(order.getBank_transfer_status().equalsIgnoreCase("0") ? getString(R.string.pending) : order.getBank_transfer_status().equalsIgnoreCase("1") ? getString(R.string.accepted) : getString(R.string.rejected));

            if (order.getBank_transfer_status().equalsIgnoreCase("2")) {
                tvReceiptStatusReason.setVisibility(View.VISIBLE);
                tvReceiptStatusReason.setText(order.getBank_transfer_message());
            }

            productImagesAdapter = new ProductImagesAdapter(activity, order.getAttachment(), "api", order.getId());
            recyclerViewImageGallery.setAdapter(productImagesAdapter);

            if (order.getPayment_method().equalsIgnoreCase("bank_transfer")) {
                lytReceipt.setVisibility(View.VISIBLE);
                getPaymentConfig();
            }

            tvOrderDate.setText(Html.fromHtml(activity.getString(R.string.ordered_on) + "<b>" + date[0] + "</b> " + activity.getString(R.string.via) + "<b>" + getPayMethod(order.getPayment_method()) + "</b>", 0));

            tvOtherDetail.setText(getString(R.string.name_1) + order.getUser_name() + getString(R.string.mobile_no_1) + order.getMobile() + getString(R.string.address_1) + order.getAddress());
            totalAfterTax = (Double.parseDouble(order.getTotal()) + Double.parseDouble(order.getDelivery_charge()));
            tvItemTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getTotal()));
            tvDeliveryCharge.setText("+ " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getDelivery_charge()));
            tvDPercent.setText(getString(R.string.discount) + "(" + order.getDiscount() + "%) :");
            tvDAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getDiscount_rupees()));
            tvTotal.setText(session.getData(Constant.CURRENCY) + totalAfterTax);
            tvPCAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getPromo_discount()));
            tvWallet.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getWallet_balance()));
            tvFinalTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(order.getFinal_total()));

            scrollView.setVisibility(View.VISIBLE);
            shimmerFrameLayout.setVisibility(View.GONE);
            shimmerFrameLayout.stopShimmer();

            recyclerView.setAdapter(new OrderItemsAdapter(activity, order.getItems(), recyclerViewTrackerTimeLine, lytMainTrackerTimeLine, lytTrackerTimeLine));

            relativeLyt.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPayMethod(String payMethod) {
        switch (payMethod.toLowerCase()) {
            case "wallet":
                return activity.getString(R.string.wallet);
            case "cod":
                return activity.getString(R.string.cod);
            case "stripe":
                return activity.getString(R.string.stripe);
            case "bank_transfer":
                return activity.getString(R.string.bank_transfer);
            case "paypal":
                return activity.getString(R.string.paypal);
            case "razorpay":
                return activity.getString(R.string.razor_payment);
            case "sslecommerz":
                return activity.getString(R.string.sslecommerz);
            case "payumoney":
                return activity.getString(R.string.pay_u_pay);
            case "paystack":
                return activity.getString(R.string.paystack);
            case "midtrans":
                return activity.getString(R.string.midtrans);
            case "flutterwave":
                return activity.getString(R.string.flutterwave);
            default:
                return "";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.order_track_detail);
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
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
    }
}