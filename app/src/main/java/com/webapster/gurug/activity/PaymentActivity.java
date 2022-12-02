package com.webapster.gurug.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.gson.Gson;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.sslcommerz.library.payment.model.datafield.MandatoryFieldModel;
import com.sslcommerz.library.payment.model.dataset.TransactionInfo;
import com.sslcommerz.library.payment.model.util.CurrencyType;
import com.sslcommerz.library.payment.model.util.ErrorKeys;
import com.sslcommerz.library.payment.model.util.SdkCategory;
import com.sslcommerz.library.payment.model.util.SdkType;
import com.sslcommerz.library.payment.viewmodel.listener.OnPaymentResultListener;
import com.sslcommerz.library.payment.viewmodel.management.PayUsingSSLCommerz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.webapster.gurug.R;
import com.webapster.gurug.adapter.AddressAdapter;
import com.webapster.gurug.adapter.CheckoutItemListAdapter;
import com.webapster.gurug.adapter.DateAdapter;
import com.webapster.gurug.adapter.SlotAdapter;
import com.webapster.gurug.fragment.WalletTransactionFragment;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.PaymentModelClass;
import com.webapster.gurug.helper.Session;
import com.webapster.gurug.model.Address;
import com.webapster.gurug.model.BookingDate;
import com.webapster.gurug.model.Cart;
import com.webapster.gurug.model.Slot;

public class PaymentActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback, PaymentResultListener {
    public static final String TAG = PaymentActivity.class.getSimpleName();
    public static String customerId;
    public static String razorPayId;
    public static String paymentMethod = "";
    public static String defaultPaymentMethod = "";
    public static String deliveryTime = "";
    public static String deliveryDay = "";
    RadioGroup lytPayment;
    public static Map<String, String> sendParams;
    public static RecyclerView recyclerViewTimeSlot, recyclerViewSingleAddress, recyclerViewCartItems;
    @SuppressLint("StaticFieldLeak")
    public static SlotAdapter adapter;
    public LinearLayout paymentLyt, deliveryTimeLyt, lytCLocation, processLyt;
    RelativeLayout lytPaymentMethods;
    public ArrayList<String> variantIdList, qtyList, dateList;
    TextView tvSubTotal, tvSelectDeliveryDate, tvWltBalance, tvProceedOrder, tvConfirmOrder, tvPayment;
    double grandTotal = 0.0, savedAmount = 0.0, subTotal = 0.0, usedBalance = 0.0, pCodeDiscount = 0.0;
    RadioButton rbCOD, rbPayU, rbPayPal, rbRazorPay, rbPayStack, rbFlutterWave, rbMidTrans, rbStripe, rbPayTm, rbSslCommerz, rbBankTransfer;
    ArrayList<BookingDate> bookingDates;
    RelativeLayout confirmLyt, lytWallet;
    RecyclerView recyclerViewDates;
    Calendar StartDate, EndDate;
    ScrollView scrollView;
    ArrayList<Slot> slotList;
    DateAdapter dateAdapter;
    int mYear, mMonth, mDay;
    Activity activity;
    CheckBox chWallet;
    Session session;
    double total;
    CardView cardViewHamburger;
    TextView toolbarTitle;
    ImageView imageMenu;
    ImageView imageHome;
    Toolbar toolbar;


    CheckoutItemListAdapter checkoutItemListAdapter;
    ArrayList<Cart> carts;

    public static ArrayList<Address> singleAddress;

    TextView tvTotalBeforeTax;
    TextView tvSaveAmount;
    TextView tvDeliveryCharge;
    TextView tvGrandTotal;
    TextView tvOrderSummary;
    ImageView imgPaymentListClose;
    TextView tvUsedWalletAmount;
    LinearLayout lytSaveAmount;
    LinearLayout lytWalletAmount;

    double deliveryCharge = 0;

    boolean isPaymentAvailable = false;
    int paymentMethods = 0;

    LinearLayout lytCalculations;

    TextView tvPaymentMethod;
    TextView tvChangeAddress;

    Animation animShow, animHide;

    Drawable drawable = null;
    Drawable drawableStart = null;

    RelativeLayout lytMain;
    ShimmerFrameLayout shimmerFrameLayout;
    String pCode = "";

    LinearLayout lytPromoDiscount;
    TextView tvPromoDiscount;
    TextView tvPromoCode;

    RelativeLayout lytMainPaymentMethods;

    static AlertDialog dialog_;
    AddressAdapter addressAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        activity = PaymentActivity.this;
        Constant.selectedDatePosition = 0;
        session = new Session(activity);
        paymentMethod = "";

        drawableStart = ContextCompat.getDrawable(activity, R.drawable.ic_down_arrow);

        animShow = AnimationUtils.loadAnimation(this, R.anim.view_show);
        animHide = AnimationUtils.loadAnimation(this, R.anim.view_hide);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardViewHamburger = findViewById(R.id.cardViewHamburger);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageMenu = findViewById(R.id.imageMenu);
        imageHome = findViewById(R.id.imageHome);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        toolbarTitle.setText(getString(R.string.payment));

        imageHome.setVisibility(View.GONE);

        imageMenu.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_arrow_back));
        imageMenu.setVisibility(View.VISIBLE);

        cardViewHamburger.setOnClickListener(view -> onBackPressed());

        ApiConfig.getWalletBalance(activity, session);

        variantIdList = getIntent().getStringArrayListExtra("variantIdList");
        qtyList = getIntent().getStringArrayListExtra("qtyList");

        getAllWidgets();


    }

    @SuppressLint("SetTextI18n")
    public void getAllWidgets() {

        lytMain = findViewById(R.id.lytMain);
        lytPromoDiscount = findViewById(R.id.lytPromoDiscount);
        tvPromoDiscount = findViewById(R.id.tvPromoDiscount);
        tvPromoCode = findViewById(R.id.tvPromoCode);
        lytMainPaymentMethods = findViewById(R.id.lytMainPaymentMethods);

        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);

        recyclerViewTimeSlot = findViewById(R.id.recyclerViewTimeSlot);
        recyclerViewSingleAddress = findViewById(R.id.recyclerViewSingleAddress);
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);

        recyclerViewTimeSlot.setLayoutManager(new LinearLayoutManager(activity));
        recyclerViewSingleAddress.setLayoutManager(new LinearLayoutManager(activity));
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(activity));

        rbPayTm = findViewById(R.id.rbPayTm);
        rbSslCommerz = findViewById(R.id.rbSslCommerz);
        rbPayStack = findViewById(R.id.rbPayStack);
        rbFlutterWave = findViewById(R.id.rbFlutterWave);
        rbCOD = findViewById(R.id.rbCOD);
        lytPayment = findViewById(R.id.lytPayment);
        rbPayU = findViewById(R.id.rbPayU);
        rbPayPal = findViewById(R.id.rbPayPal);
        rbRazorPay = findViewById(R.id.rbRazorPay);
        rbMidTrans = findViewById(R.id.rbMidTrans);
        rbStripe = findViewById(R.id.rbStripe);
        rbBankTransfer = findViewById(R.id.rbBankTransfer);
        tvPayment = findViewById(R.id.tvPayment);
        chWallet = findViewById(R.id.chWallet);
        lytPaymentMethods = findViewById(R.id.lytPaymentMethods);
        lytCLocation = findViewById(R.id.lytCLocation);
        lytWallet = findViewById(R.id.lytWallet);
        paymentLyt = findViewById(R.id.paymentLyt);
        tvProceedOrder = findViewById(R.id.tvProceedOrder);
        tvConfirmOrder = findViewById(R.id.tvConfirmOrder);
        processLyt = findViewById(R.id.processLyt);
        tvSelectDeliveryDate = findViewById(R.id.tvSelectDeliveryDate);
        deliveryTimeLyt = findViewById(R.id.deliveryTimeLyt);
        recyclerViewDates = findViewById(R.id.recyclerViewDates);
        tvSubTotal = findViewById(R.id.tvSubTotal);
        confirmLyt = findViewById(R.id.confirmLyt);
        scrollView = findViewById(R.id.scrollView);
        tvWltBalance = findViewById(R.id.tvWltBalance);
        tvTotalBeforeTax = findViewById(R.id.tvTotalBeforeTax);
        tvSaveAmount = findViewById(R.id.tvSaveAmount);
        tvDeliveryCharge = findViewById(R.id.tvDeliveryCharge);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        lytSaveAmount = findViewById(R.id.lytSaveAmount);
        lytCalculations = findViewById(R.id.lytCalculations);
        tvOrderSummary = findViewById(R.id.tvOrderSummary);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        imgPaymentListClose = findViewById(R.id.imgPaymentListClose);
        tvUsedWalletAmount = findViewById(R.id.tvUsedWalletAmount);
        lytWalletAmount = findViewById(R.id.lytWalletAmount);
        tvChangeAddress = findViewById(R.id.tvChangeAddress);

        showShimmer();

        tvPaymentMethod.setOnClickListener(v -> showPaymentOptions());

        imgPaymentListClose.setOnClickListener(v -> hidePaymentOptions());

        getAddresses();

        tvChangeAddress.setOnClickListener(v -> onBackPressed());

        tvProceedOrder.setOnClickListener(v -> PlaceOrderProcess(activity));

        chWallet.setOnClickListener(view -> {
            lytWalletAmount.setVisibility(View.VISIBLE);
            if (chWallet.getTag().equals("false")) {
                chWallet.setChecked(true);
                lytWallet.setVisibility(View.VISIBLE);

                if (Double.parseDouble(session.getData(Constant.WALLET_BALANCE)) >= subTotal) {
                    usedBalance = subTotal;
                    tvWltBalance.setText(getString(R.string.remaining_wallet_balance) + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (Double.parseDouble(session.getData(Constant.WALLET_BALANCE)) - usedBalance)));
                } else {
                    usedBalance = Double.parseDouble(session.getData(Constant.WALLET_BALANCE));
                    tvWltBalance.setText(getString(R.string.remaining_wallet_balance) + session.getData(Constant.CURRENCY) + "0.00");
                }

                tvUsedWalletAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + usedBalance));
                grandTotal = ((subTotal + deliveryCharge) - usedBalance);

                if (grandTotal == 0) {
                    paymentMethod = Constant.WALLET;
                }

                tvGrandTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + grandTotal));
                chWallet.setTag("true");
            } else {
                lytWalletAmount.setVisibility(View.GONE);
                walletUncheck();
            }

        });

        confirmLyt.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);

        lytPayment.setOnCheckedChangeListener((group, checkedId) -> {
            try {
                RadioButton rb = findViewById(checkedId);
                carts = new ArrayList<>();
                getCartData(rb.getTag().toString().equals("cod"));
                paymentMethod = rb.getTag().toString();
                defaultPaymentMethod = rb.getTag().toString();
                hidePaymentOptions();
                setPaymentDrawable(rb.getTag().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            RadioButton rb = findViewById(R.id.rbCOD);
            carts = new ArrayList<>();
            getCartData(rb.getTag().toString().equals("cod"));
            paymentMethod = rb.getTag().toString();
            defaultPaymentMethod = rb.getTag().toString();
            hidePaymentOptions();
            setPaymentDrawable(rb.getTag().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        chWallet.setTag("false");
        tvWltBalance.setText("Total Balance: " + session.getData(Constant.CURRENCY) + session.getData(Constant.WALLET_BALANCE));
        if (Double.parseDouble(session.getData(Constant.WALLET_BALANCE)) == 0) {
            lytWallet.setVisibility(View.GONE);
        } else {
            lytWallet.setVisibility(View.VISIBLE);
        }
        tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subTotal));
    }

    public void setPaymentDrawable(String tag) {
        switch (tag) {
            case "cod":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_cod);
                break;
            case "RazorPay":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_razorpay);
                break;
            case "PayUMoney":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_payu);
                break;
            case "Paystack":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_paystack);
                break;
            case "Flutterwave":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_flutterwave);
                break;
            case "PayPal":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_paypal);
                break;
            case "Midtrans":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_midtrans);
                break;
            case "Stripe":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_stripe);
                break;
            case "PayTm":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_paytm);
                break;
            case "SSLCOMMERZ":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_sslecommerz);
                break;
            case "bank_transfer":
                drawable = ContextCompat.getDrawable(activity, R.drawable.ic_bank);
                break;
        }

        tvPaymentMethod.setCompoundDrawablesWithIntrinsicBounds(drawable, null, drawableStart, null);

    }

    public void getPaymentConfig() {
        recyclerViewTimeSlot.setVisibility(View.GONE);
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
                            if (object.has(Constant.cod_payment_method)) {
                                Constant.COD = object.getString(Constant.cod_payment_method);
                                Constant.COD_MODE = object.getString(Constant.cod_mode);
                            }
                            if (object.has(Constant.payu_method)) {
                                Constant.PAYUMONEY = object.getString(Constant.payu_method);
                                Constant.PAYUMONEY_MODE = object.getString(Constant.payumoney_mode);
                                Constant.MERCHANT_KEY = object.getString(Constant.PAY_M_KEY);
                                Constant.MERCHANT_ID = object.getString(Constant.PAYU_M_ID);
                                Constant.MERCHANT_SALT = object.getString(Constant.PAYU_SALT);
                                ApiConfig.SetAppEnvironment(activity);
                            }
                            if (object.has(Constant.razor_pay_method)) {
                                Constant.RAZORPAY = object.getString(Constant.razor_pay_method);
                                Constant.RAZOR_PAY_KEY_VALUE = object.getString(Constant.RAZOR_PAY_KEY);
                            }
                            if (object.has(Constant.paypal_method)) {
                                Constant.PAYPAL = object.getString(Constant.paypal_method);
                            }
                            if (object.has(Constant.paystack_method)) {
                                Constant.PAYSTACK = object.getString(Constant.paystack_method);
                                Constant.PAYSTACK_KEY = object.getString(Constant.paystack_public_key);
                            }
                            if (object.has(Constant.flutterwave_payment_method)) {
                                Constant.FLUTTERWAVE = object.getString(Constant.flutterwave_payment_method);
                                Constant.FLUTTERWAVE_ENCRYPTION_KEY_VAL = object.getString(Constant.flutterwave_encryption_key);
                                Constant.FLUTTERWAVE_PUBLIC_KEY_VAL = object.getString(Constant.flutterwave_public_key);
                                Constant.FLUTTERWAVE_SECRET_KEY_VAL = object.getString(Constant.flutterwave_secret_key);
                                Constant.FLUTTERWAVE_SECRET_KEY_VAL = object.getString(Constant.flutterwave_secret_key);
                                Constant.FLUTTERWAVE_CURRENCY_CODE_VAL = object.getString(Constant.flutterwave_currency_code);
                            }
                            if (object.has(Constant.midtrans_payment_method)) {
                                Constant.MIDTRANS = object.getString(Constant.midtrans_payment_method);
                            }
                            if (object.has(Constant.stripe_payment_method)) {
                                Constant.STRIPE = object.getString(Constant.stripe_payment_method);
                            }
                            if (object.has(Constant.paytm_payment_method)) {
                                Constant.PAYTM = object.getString(Constant.paytm_payment_method);
                                Constant.PAYTM_MERCHANT_ID = object.getString(Constant.paytm_merchant_id);
                                Constant.PAYTM_MERCHANT_KEY = object.getString(Constant.paytm_merchant_key);
                                Constant.PAYTM_MODE = object.getString(Constant.paytm_mode);
                            }
                            if (object.has(Constant.ssl_commerce_payment_method)) {
                                Constant.SSLECOMMERZ = object.getString(Constant.ssl_commerce_payment_method);
                                Constant.SSLECOMMERZ_MODE = object.getString(Constant.ssl_commerece_mode);
                                Constant.SSLECOMMERZ_STORE_ID = object.getString(Constant.ssl_commerece_store_id);
                                Constant.SSLECOMMERZ_SECRET_KEY = object.getString(Constant.ssl_commerece_secret_key);
                            }
                            if (object.has(Constant.direct_bank_transfer_method)) {
                                Constant.DIRECT_BANK_TRANSFER = object.getString(Constant.direct_bank_transfer_method);
                                Constant.ACCOUNT_NAME = object.getString(Constant.account_name);
                                Constant.ACCOUNT_NUMBER = object.getString(Constant.account_number);
                                Constant.BANK_NAME = object.getString(Constant.bank_name);
                                Constant.BANK_CODE = object.getString(Constant.bank_code);
                                Constant.NOTES = object.getString(Constant.notes);
                            }
                            setPaymentMethod();
                        } else {
                            recyclerViewTimeSlot.setVisibility(View.VISIBLE);
                            Toast.makeText(activity, getString(R.string.alert_payment_methods_blank), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    recyclerViewTimeSlot.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    public void setCheckItem(RadioButton radioButton) {
        radioButton.setChecked(true);
        setPaymentDrawable(radioButton.getTag().toString());
    }

    public void setPaymentMethod() {
        if (Constant.DIRECT_BANK_TRANSFER.equals("0") && Constant.FLUTTERWAVE.equals("0") && Constant.PAYPAL.equals("0") && Constant.PAYUMONEY.equals("0") && Constant.COD.equals("0") && Constant.RAZORPAY.equals("0") && Constant.PAYSTACK.equals("0") && Constant.MIDTRANS.equals("0") && Constant.STRIPE.equals("0") && Constant.PAYTM.equals("0") && Constant.SSLECOMMERZ.equals("0")) {
            isPaymentAvailable = false;
        } else {
            isPaymentAvailable = true;

            if (Constant.COD.equals("1")) {
                if (Constant.COD_MODE.equals(Constant.product) && !Constant.isCODAllow) {
                    rbCOD.setVisibility(View.GONE);
                } else {
                    paymentMethods++;
                    if (paymentMethod.equals("")) {
                        setCheckItem(rbCOD);
                    }
                    rbCOD.setVisibility(View.VISIBLE);
                }
            }

            if (Constant.PAYUMONEY.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    rbPayU.setChecked(true);
                rbPayU.setVisibility(View.VISIBLE);
            }

            if (Constant.RAZORPAY.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbRazorPay);
                rbRazorPay.setVisibility(View.VISIBLE);
            }

            if (Constant.PAYSTACK.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbPayStack);
                rbPayStack.setVisibility(View.VISIBLE);
            }

            if (Constant.FLUTTERWAVE.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbFlutterWave);
                rbFlutterWave.setVisibility(View.VISIBLE);
            }

            if (Constant.PAYPAL.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbPayPal);
                rbPayPal.setVisibility(View.VISIBLE);
            }

            if (Constant.MIDTRANS.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbMidTrans);
                rbMidTrans.setVisibility(View.VISIBLE);
            }

            if (Constant.STRIPE.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbStripe);
                rbStripe.setVisibility(View.VISIBLE);
            }

            if (Constant.PAYTM.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbPayTm);
                rbPayTm.setVisibility(View.VISIBLE);
            }

            if (Constant.SSLECOMMERZ.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbSslCommerz);
                rbSslCommerz.setVisibility(View.VISIBLE);
            }

            if (Constant.DIRECT_BANK_TRANSFER.equals("1")) {
                paymentMethods++;
                if (paymentMethod.equals(""))
                    setCheckItem(rbBankTransfer);
                rbBankTransfer.setVisibility(View.VISIBLE);
            }

            tvPaymentMethod.setClickable(paymentMethods > 1);
        }
    }

    @SuppressLint("SetTextI18n")
    public void walletUncheck() {
        paymentMethod = defaultPaymentMethod;
        setPaymentDrawable(paymentMethod);
        usedBalance = 0;
        tvWltBalance.setText(getString(R.string.total) + session.getData(Constant.CURRENCY) + session.getData(Constant.WALLET_BALANCE));
        grandTotal = (subTotal + deliveryCharge);
        tvGrandTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + grandTotal));
        chWallet.setChecked(false);
        chWallet.setTag("false");
    }

    public void getTimeSlots(final Session session, Activity activity) {
        if (session.getData(Constant.SHIPPING_TYPE).equals("local")) {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.SETTINGS, Constant.GetVal);
            params.put(Constant.GET_TIME_SLOT_CONFIG, Constant.GetVal);

            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(response);
                        if (!jsonObject1.getBoolean(Constant.ERROR)) {
                            JSONObject jsonObject = new JSONObject(jsonObject1.getJSONObject(Constant.TIME_SLOT_CONFIG).toString());

                            session.setData(Constant.IS_TIME_SLOTS_ENABLE, jsonObject.getString(Constant.IS_TIME_SLOTS_ENABLE));
                            session.setData(Constant.DELIVERY_STARTS_FROM, jsonObject.getString(Constant.DELIVERY_STARTS_FROM));
                            session.setData(Constant.ALLOWED_DAYS, jsonObject.getString(Constant.ALLOWED_DAYS));

                            if (session.getData(Constant.IS_TIME_SLOTS_ENABLE).equals(Constant.GetVal)) {
                                deliveryTimeLyt.setVisibility(View.VISIBLE);

                                StartDate = Calendar.getInstance();
                                EndDate = Calendar.getInstance();
                                mYear = StartDate.get(Calendar.YEAR);
                                mMonth = StartDate.get(Calendar.MONTH);
                                mDay = StartDate.get(Calendar.DAY_OF_MONTH);

                                int DeliveryStartFrom = Integer.parseInt(session.getData(Constant.DELIVERY_STARTS_FROM)) - 1;
                                int DeliveryAllowFrom = Integer.parseInt(session.getData(Constant.ALLOWED_DAYS));

                                StartDate.add(Calendar.DATE, DeliveryStartFrom);
                                EndDate.add(Calendar.DATE, (DeliveryStartFrom + DeliveryAllowFrom));

                                dateList = ApiConfig.getDates(StartDate.get(Calendar.DATE) + "-" + (StartDate.get(Calendar.MONTH) + 1) + "-" + StartDate.get(Calendar.YEAR), EndDate.get(Calendar.DATE) + "-" + (EndDate.get(Calendar.MONTH) + 1) + "-" + EndDate.get(Calendar.YEAR));
                                setDateList(dateList);

                                GetTimeSlots();

                            } else {
                                deliveryTimeLyt.setVisibility(View.GONE);
                                deliveryDay = "Date : N/A";
                                deliveryTime = "Time : N/A";

                                recyclerViewTimeSlot.setVisibility(View.VISIBLE);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        recyclerViewTimeSlot.setVisibility(View.VISIBLE);
                    }
                }
            }, activity, Constant.SETTING_URL, params, false);
        } else {
            deliveryDay = "Date : N/A";
            deliveryTime = "Time : N/A";
        }
        getPaymentConfig();
    }

    public void GetTimeSlots() {
        slotList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put("get_time_slots", Constant.GetVal);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);

                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = object.getJSONArray("time_slots");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object1 = jsonArray.getJSONObject(i);
                            slotList.add(new Slot(object1.getString(Constant.ID), object1.getString("title"), object1.getString("last_order_time")));
                        }

                        recyclerViewTimeSlot.setLayoutManager(new LinearLayoutManager(activity));

                        adapter = new SlotAdapter(deliveryTime, activity, slotList);
                        recyclerViewTimeSlot.setAdapter(adapter);

                        recyclerViewTimeSlot.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    recyclerViewTimeSlot.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.SETTING_URL, params, true);
    }

    public void setDateList(ArrayList<String> datesList) {
        bookingDates = new ArrayList<>();
        for (int i = 0; i < datesList.size(); i++) {
            String[] date = datesList.get(i).split("-");

            BookingDate bookingDate1 = new BookingDate();
            bookingDate1.setDate(date[0]);
            bookingDate1.setMonth(date[1]);
            bookingDate1.setYear(date[2]);
            bookingDate1.setDay(date[3]);

            bookingDates.add(bookingDate1);
        }
        dateAdapter = new DateAdapter(activity, bookingDates);

        recyclerViewDates.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewDates.setAdapter(dateAdapter);
    }


    @SuppressLint("SetTextI18n")
    public void PlaceOrderProcess(Activity activity) {
        if (deliveryDay.length() == 0) {
            Toast.makeText(activity, getString(R.string.select_delivery_day), Toast.LENGTH_SHORT).show();
            return;
        } else if (deliveryTime.length() == 0) {
            Toast.makeText(activity, getString(R.string.select_delivery_time), Toast.LENGTH_SHORT).show();
            return;
        } else if (paymentMethod.isEmpty()) {
            Toast.makeText(activity, getString(R.string.select_payment_method), Toast.LENGTH_SHORT).show();
            return;
        }
        sendParams = new HashMap<>();
        sendParams.put(Constant.PLACE_ORDER, Constant.GetVal);
        sendParams.put(Constant.USER_ID, session.getData(Constant.ID));
        sendParams.put(Constant.PRODUCT_VARIANT_ID, String.valueOf(variantIdList));
        sendParams.put(Constant.QUANTITY, String.valueOf(qtyList));
        sendParams.put(Constant.TOTAL, "" + subTotal);
        sendParams.put(Constant.DELIVERY_CHARGE, "" + deliveryCharge);
        sendParams.put(Constant.WALLET_BALANCE, String.valueOf(usedBalance));
        sendParams.put(Constant.KEY_WALLET_USED, chWallet.getTag().toString());
        sendParams.put(Constant.FINAL_TOTAL, "" + grandTotal);
        sendParams.put(Constant.PAYMENT_METHOD, paymentMethod);
        if (paymentMethod.equals("bank_transfer")) {
            sendParams.put(Constant.STATUS, Constant.AWAITING_PAYMENT);
        }
        if (!pCode.isEmpty()) {
            sendParams.put(Constant.PROMO_CODE, pCode);
            sendParams.put(Constant.PROMO_DISCOUNT, ApiConfig.StringFormat("" + pCodeDiscount));
        }
        sendParams.put(Constant.DELIVERY_TIME, (deliveryDay + " - " + deliveryTime));
        sendParams.put(Constant.ADDRESS_ID, Constant.selectedAddressId);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogView = inflater.inflate(R.layout.dialog_order_confirm, null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(true);
        final AlertDialog dialog = alertDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView tvDialogCancel, tvDialogConfirm, tvDialogItemTotal, tvDialogDeliveryCharge, tvDialogTotal, tvDialogPCAmount, tvDialogWallet, tvDialogFinalTotal;
        LinearLayout lytDialogPromo, lytDialogWallet;
        EditText tvSpecialNote;

        lytDialogPromo = dialogView.findViewById(R.id.lytDialogPromo);
        lytDialogWallet = dialogView.findViewById(R.id.lytDialogWallet);
        tvDialogItemTotal = dialogView.findViewById(R.id.tvDialogItemTotal);
        tvDialogDeliveryCharge = dialogView.findViewById(R.id.tvDialogDeliveryCharge);
        tvDialogTotal = dialogView.findViewById(R.id.tvDialogTotal);
        tvDialogPCAmount = dialogView.findViewById(R.id.tvDialogPCAmount);
        tvDialogWallet = dialogView.findViewById(R.id.tvDialogWallet);
        tvDialogFinalTotal = dialogView.findViewById(R.id.tvDialogFinalTotal);
        tvDialogCancel = dialogView.findViewById(R.id.tvDialogCancel);
        tvDialogConfirm = dialogView.findViewById(R.id.tvDialogConfirm);
        tvSpecialNote = dialogView.findViewById(R.id.tvSpecialNote);

        if (pCodeDiscount > 0) {
            lytDialogPromo.setVisibility(View.VISIBLE);
            tvDialogPCAmount.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + pCodeDiscount));
        } else {
            lytDialogPromo.setVisibility(View.GONE);
        }

        if (chWallet.getTag().toString().equals("true")) {
            lytDialogWallet.setVisibility(View.VISIBLE);
            tvDialogWallet.setText("- " + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + usedBalance));
        } else {
            lytDialogWallet.setVisibility(View.GONE);
        }

        tvDialogItemTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subTotal));
        tvDialogDeliveryCharge.setText(deliveryCharge > 0 ? session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + deliveryCharge) : getString(R.string.free));
        tvDialogTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (subTotal + deliveryCharge)));
        tvDialogFinalTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + grandTotal));
        tvDialogConfirm.setOnClickListener(v -> {
            setProgressDialog();
            sendParams.put(Constant.ORDER_NOTE, tvSpecialNote.getText().toString().trim());
            if (paymentMethod.equals("cod") || paymentMethod.equals("wallet") || paymentMethod.equals("bank_transfer")) {
                ApiConfig.RequestToVolley((result, response) -> {
                    if (result) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (!object.getBoolean(Constant.ERROR)) {
                                if (chWallet.getTag().toString().equals("true")) {
                                    ApiConfig.getWalletBalance(activity, session);
                                }
                                dialog.dismiss();

                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(Constant.FROM, "payment_success");
                                activity.startActivity(intent);
                            } else {
                                PaymentActivity.dialog_.dismiss();
                                Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            PaymentActivity.dialog_.dismiss();
                        }
                    }
                }, activity, Constant.ORDER_PROCESS_URL, sendParams, true);
                dialog.dismiss();
            } else {
                sendParams.put(Constant.USER_NAME, session.getData(Constant.NAME));
                if (paymentMethod.equals(getString(R.string.pay_u))) {
                    dialog.dismiss();
                    sendParams.put(Constant.MOBILE, session.getData(Constant.MOBILE));
                    sendParams.put(Constant.USER_NAME, session.getData(Constant.NAME));
                    sendParams.put(Constant.EMAIL, session.getData(Constant.EMAIL));
                    new PaymentModelClass(activity).OnPayClick(activity, sendParams, Constant.PAYMENT, sendParams.get(Constant.FINAL_TOTAL));
                } else if (paymentMethod.equals(getString(R.string.paypal))) {
                    dialog.dismiss();
                    sendParams.put(Constant.FROM, Constant.PAYMENT);
                    sendParams.put(Constant.STATUS, Constant.AWAITING_PAYMENT);
                    PlaceOrder(activity, getString(R.string.midtrans), System.currentTimeMillis() + Constant.randomNumeric(3), true, sendParams, "paypal");
                } else if (paymentMethod.equals("RazorPay")) {
                    dialog.dismiss();
                    CreateOrderId(grandTotal);
                } else if (paymentMethod.equals("Paystack")) {
                    dialog.dismiss();
                    sendParams.put(Constant.FROM, Constant.PAYMENT);
                    Intent intent = new Intent(activity, PayStackActivity.class);
                    intent.putExtra(Constant.PARAMS, (Serializable) sendParams);
                    startActivity(intent);
                } else if (paymentMethod.equals("Midtrans")) {
                    dialog.dismiss();
                    sendParams.put(Constant.FROM, Constant.PAYMENT);
                    sendParams.put(Constant.STATUS, Constant.AWAITING_PAYMENT);
                    PlaceOrder(activity, getString(R.string.midtrans), System.currentTimeMillis() + Constant.randomNumeric(3), true, sendParams, "midtrans");
                } else if (paymentMethod.equals("stripe")) {
                    dialog.dismiss();
                    sendParams.put(Constant.FROM, Constant.PAYMENT);
                    sendParams.put(Constant.STATUS, Constant.AWAITING_PAYMENT);
                    PlaceOrder(activity, getString(R.string.stripe), System.currentTimeMillis() + Constant.randomNumeric(3), true, sendParams, "stripe");
                } else if (paymentMethod.equals("Flutterwave")) {
                    dialog.dismiss();
                    StartFlutterWavePayment();
                } else if (paymentMethod.equals("PayTm")) {
                    dialog.dismiss();
                    startPayTmPayment();
                } else if (paymentMethod.equals("SSLCOMMERZ")) {
                    dialog.dismiss();
                    startSslCommerzPayment(activity, sendParams.get(Constant.FINAL_TOTAL), System.currentTimeMillis() + Constant.randomNumeric(3), sendParams);
                }
            }
        });

        tvDialogCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void startSslCommerzPayment(Activity activity, String amount, String transId, Map<String, String> sendParams) {
        String mode;
        if (Constant.SSLECOMMERZ_MODE.equals("sandbox")) {
            mode = SdkType.TESTBOX;
        } else {
            mode = SdkType.LIVE;
        }

        MandatoryFieldModel mandatoryFieldModel = new MandatoryFieldModel(Constant.SSLECOMMERZ_STORE_ID, Constant.SSLECOMMERZ_SECRET_KEY, amount, transId, CurrencyType.BDT, mode, SdkCategory.BANK_LIST);

        /* Call for the payment */
        PayUsingSSLCommerz.getInstance().setData(activity, mandatoryFieldModel, new OnPaymentResultListener() {
            @Override
            public void transactionSuccess(TransactionInfo transactionInfo) {
                // If payment is success and risk label is 0.

                PlaceOrder(activity, getString(R.string.sslecommerz), transactionInfo.getTranId(), true, sendParams, "SSLECOMMERZ");
                try {
                    if (PaymentActivity.dialog_ != null) {
                        PaymentActivity.dialog_.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void transactionFail(String sessionKey) {
                try {
                    if (PaymentActivity.dialog_ != null) {
                        PaymentActivity.dialog_.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(activity, sessionKey, Toast.LENGTH_LONG).show();
            }

            @Override
            public void error(int errorCode) {
                try {
                    if (PaymentActivity.dialog_ != null) {
                        PaymentActivity.dialog_.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                switch (errorCode) {
                    case ErrorKeys.USER_INPUT_ERROR:
                        Toast.makeText(activity, activity.getString(R.string.user_input_error), Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.INTERNET_CONNECTION_ERROR:
                        Toast.makeText(activity, activity.getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.DATA_PARSING_ERROR:
                        Toast.makeText(activity, activity.getString(R.string.data_parsing_error), Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.CANCEL_TRANSACTION_ERROR:
                        Toast.makeText(activity, activity.getString(R.string.user_cancel_transaction_error), Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.SERVER_ERROR:
                        Toast.makeText(activity, activity.getString(R.string.server_error), Toast.LENGTH_LONG).show();
                        break;
                    case ErrorKeys.NETWORK_ERROR:
                        Toast.makeText(activity, activity.getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    public void CreateOrderId(double payable) {
        Map<String, String> params = new HashMap<>();
        params.put("amount", "" + Math.round(payable) + "00");
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        startPayment(object.getString(Constant.ID), object.getString("amount"));
                    } else {
                        Toast.makeText(activity, object.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            dialog_.dismiss();
        }, activity, Constant.GET_RAZORPAY_ORDER_URL, params, true);

    }

    public void startPayment(String orderId, String payAmount) {
        Checkout checkout = new Checkout();
        checkout.setKeyID(Constant.RAZOR_PAY_KEY_VALUE);
        checkout.setImage(R.mipmap.ic_launcher);

        try {
            JSONObject options = new JSONObject();
            options.put(Constant.NAME, session.getData(Constant.NAME));
            options.put(Constant.ORDER_ID, orderId);
            options.put(Constant.CURRENCY, "INR");
            options.put(Constant.AMOUNT, payAmount);

            JSONObject preFill = new JSONObject();
            preFill.put(Constant.EMAIL, session.getData(Constant.EMAIL));
            preFill.put(Constant.CONTACT, session.getData(Constant.MOBILE));
            options.put("prefill", preFill);

            checkout.open(activity, options);

        } catch (Exception e) {
            Log.d(TAG, "Error in starting Razorpay Checkout", e);
        }
    }


    public void PlaceOrder(final Activity activity, final String paymentType, final String transactionId, boolean isSuccess, final Map<String, String> sendParams, final String payType) {
        if (isSuccess) {
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            sendParams.put(Constant.ORDER_ID, object.getString(Constant.ORDER_ID));
                            switch (payType) {
                                case "stripe":
                                    CreateStripePayment(object.getString(Constant.ORDER_ID));
                                    break;
                                case "midtrans":
                                    CreateMidtransPayment(object.getString(Constant.ORDER_ID), ApiConfig.StringFormat("" + subTotal));
                                    break;
                                case "paypal":
                                    StartPayPalPayment(sendParams);
                                    break;
                                default:
                                    AddTransaction(activity, paymentType, transactionId, payType, activity.getString(R.string.order_success), sendParams);
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, sendParams, false);
        } else {
            AddTransaction(activity, "RazorPay", transactionId, payType, getString(R.string.order_failed), sendParams);
        }
    }

    public void CreateMidtransPayment(String orderId, String grossAmount) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.ORDER_ID, orderId);
        if (grossAmount.contains(",")) {
            params.put(Constant.GROSS_AMOUNT, grossAmount.split(",")[0]);
        } else if (grossAmount.contains(".")) {
            params.put(Constant.GROSS_AMOUNT, grossAmount.split("\\.")[0]);
        }
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        Intent intent = new Intent(activity, MidtransActivity.class);
                        intent.putExtra(Constant.URL, jsonObject.getJSONObject(Constant.DATA).getString(Constant.REDIRECT_URL));
                        intent.putExtra(Constant.ORDER_ID, orderId);
                        intent.putExtra(Constant.FROM, Constant.PAYMENT);
                        intent.putExtra(Constant.PARAMS, (Serializable) sendParams);
                        startActivity(intent);
                    } else {
                        Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (PaymentActivity.dialog_ != null) {
                PaymentActivity.dialog_.dismiss();
            }
        }, activity, Constant.MIDTRANS_PAYMENT_URL, params, true);
    }

    public void CreateStripePayment(String orderId) {
        Intent intent = new Intent(activity, StripeActivity.class);
        intent.putExtra(Constant.ORDER_ID, orderId);
        intent.putExtra(Constant.FROM, Constant.PAYMENT);
        intent.putExtra(Constant.PARAMS, (Serializable) sendParams);
        startActivity(intent);
    }

    public void AddTransaction(Activity activity, String paymentType, String transactionId, final String status, String message, Map<String, String> sendParams) {
        Map<String, String> transactionParams = new HashMap<>();
        transactionParams.put(Constant.ADD_TRANSACTION, Constant.GetVal);
        transactionParams.put(Constant.USER_ID, sendParams.get(Constant.USER_ID));
        transactionParams.put(Constant.ORDER_ID, sendParams.get(Constant.ORDER_ID));
        transactionParams.put(Constant.TYPE, paymentType);
        transactionParams.put(Constant.TAX_PERCENT, "0");
        transactionParams.put(Constant.TRANS_ID, transactionId);
        transactionParams.put(Constant.AMOUNT, sendParams.get(Constant.FINAL_TOTAL));
        transactionParams.put(Constant.STATUS, status);
        transactionParams.put(Constant.MESSAGE, message);
        Date c = Calendar.getInstance().getTime();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        transactionParams.put("transaction_date", df.format(c));

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        if (!status.equals(Constant.FAILED)) {
                            try {
                                if (PaymentActivity.dialog_ != null) {
                                    PaymentActivity.dialog_.dismiss();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Intent intent = new Intent(activity, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Constant.FROM, "payment_success");
                            activity.startActivity(intent);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, transactionParams, false);
    }

    public void StartPayPalPayment(final Map<String, String> sendParams) {
        final Map<String, String> params = new HashMap<>();
        params.put(Constant.FIRST_NAME, sendParams.get(Constant.USER_NAME));
        params.put(Constant.LAST_NAME, sendParams.get(Constant.USER_NAME));
        params.put(Constant.PAYER_EMAIL, "" + sendParams.get(Constant.EMAIL));
        params.put(Constant.ITEM_NAME, "Card Order");
        params.put(Constant.ITEM_NUMBER, System.currentTimeMillis() + Constant.randomNumeric(3));
        params.put(Constant.AMOUNT, sendParams.get(Constant.FINAL_TOTAL));
        ApiConfig.RequestToVolley((result, response) -> {
            Intent intent = new Intent(activity, PayPalWebActivity.class);
            intent.putExtra(Constant.URL, response);
            intent.putExtra(Constant.ORDER_ID, params.get(Constant.ITEM_NUMBER));
            intent.putExtra(Constant.FROM, Constant.PAYMENT);
            intent.putExtra(Constant.PARAMS, (Serializable) sendParams);
            startActivity(intent);
        }, activity, Constant.PAPAL_URL, params, true);
    }


    public void startPayTmPayment() {
        Map<String, String> params = new HashMap<>();

        params.put(Constant.ORDER_ID_, Constant.randomAlphaNumeric(20));
        params.put(Constant.CUST_ID, Constant.randomAlphaNumeric(10));
        params.put(Constant.TXN_AMOUNT, ApiConfig.StringFormat("" + subTotal));
        if (Constant.PAYTM_MODE.equals("sandbox")) {
            params.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_DEMO_VAL);
            params.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_DEMO_VAL);
            params.put(Constant.WEBSITE, Constant.WEBSITE_DEMO_VAL);
        } else if (Constant.PAYTM_MODE.equals("production")) {
            params.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_LIVE_VAL);
            params.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_LIVE_VAL);
            params.put(Constant.WEBSITE, Constant.WEBSITE_LIVE_VAL);
        }

//        System.out.println("====" + params.toString());
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject object = jsonObject.getJSONObject(Constant.DATA);
//                    System.out.println("=======res  " + response);

                    PaytmPGService Service = null;
                    if (Constant.PAYTM_MODE.equals("sandbox")) {
                        Service = PaytmPGService.getStagingService(Constant.PAYTM_ORDER_PROCESS_DEMO_VAL);
                    } else if (Constant.PAYTM_MODE.equals("production")) {
                        Service = PaytmPGService.getProductionService();
                    }

                    customerId = object.getString(Constant.CUST_ID);
                    //creating a hashmap and adding all the values required

                    HashMap<String, String> paramMap = new HashMap<>();
                    paramMap.put(Constant.MID, Constant.PAYTM_MERCHANT_ID);
                    paramMap.put(Constant.ORDER_ID_, jsonObject.getString("order id"));
                    paramMap.put(Constant.CUST_ID, object.getString(Constant.CUST_ID));
                    paramMap.put(Constant.TXN_AMOUNT, ApiConfig.StringFormat("" + subTotal));

                    if (Constant.PAYTM_MODE.equals("sandbox")) {
                        paramMap.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_DEMO_VAL);
                        paramMap.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_DEMO_VAL);
                        paramMap.put(Constant.WEBSITE, Constant.WEBSITE_DEMO_VAL);
                    } else if (Constant.PAYTM_MODE.equals("production")) {
                        paramMap.put(Constant.INDUSTRY_TYPE_ID, Constant.INDUSTRY_TYPE_ID_LIVE_VAL);
                        paramMap.put(Constant.CHANNEL_ID, Constant.MOBILE_APP_CHANNEL_ID_LIVE_VAL);
                        paramMap.put(Constant.WEBSITE, Constant.WEBSITE_LIVE_VAL);
                    }

                    paramMap.put(Constant.CALLBACK_URL, object.getString(Constant.CALLBACK_URL));
                    paramMap.put(Constant.CHECKSUMHASH, jsonObject.getString("signature"));

                    //creating a paytm order object using the hashmap
                    PaytmOrder order = new PaytmOrder(paramMap);

                    //initializing the paytm service
                    Objects.requireNonNull(Service).initialize(order, null);

                    //finally starting the payment transaction
                    Service.startPaymentTransaction(activity, true, true, this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GENERATE_PAYTM_CHECKSUM, params, false);


    }

    @Override
    public void onTransactionResponse(Bundle bundle) {
        String orderId = bundle.getString(Constant.ORDERID);
        String status = bundle.getString(Constant.STATUS_);
        if (status.equalsIgnoreCase(Constant.TXN_SUCCESS)) {
            verifyTransaction(orderId);
        } else {
            dialog_.dismiss();
        }
    }

    /**
     * Verifying the transaction status once PayTM transaction is over
     * This makes server(own) -> server(PayTM) call to verify the transaction status
     */
    public void verifyTransaction(String orderId) {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", orderId);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getJSONObject("body").getJSONObject("resultInfo").getString("resultStatus");
                    if (status.equalsIgnoreCase("TXN_SUCCESS")) {
                        String txnId = jsonObject.getJSONObject("body").getString("txnId");
                        PlaceOrder(activity, getString(R.string.paytm), txnId, true, sendParams, Constant.SUCCESS);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, activity, Constant.VALID_TRANSACTION, params, false);
    }

    @Override
    public void networkNotAvailable() {
        dialog_.dismiss();
        Toast.makeText(activity, "Network error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        dialog_.dismiss();
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void someUIErrorOccurred(String s) {
        dialog_.dismiss();
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        dialog_.dismiss();
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        dialog_.dismiss();
        Toast.makeText(activity, "Back Pressed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        dialog_.dismiss();
        Toast.makeText(activity, s + bundle.toString(), Toast.LENGTH_LONG).show();
    }

    void StartFlutterWavePayment() {
        new RavePayManager(this)
                .setAmount(subTotal)
                .setEmail(session.getData(Constant.EMAIL))
                .setCurrency(Constant.FLUTTERWAVE_CURRENCY_CODE_VAL)
                .setfName(session.getData(Constant.FIRST_NAME))
                .setlName(session.getData(Constant.LAST_NAME))
                .setNarration(getString(R.string.app_name) + getString(R.string.shopping))
                .setPublicKey(Constant.FLUTTERWAVE_PUBLIC_KEY_VAL)
                .setEncryptionKey(Constant.FLUTTERWAVE_ENCRYPTION_KEY_VAL)
                .setTxRef(System.currentTimeMillis() + "Ref")
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .acceptAccountPayments(true)
                .acceptAchPayments(true)
                .acceptBankTransferPayments(true)
                .acceptBarterPayments(true)
                .acceptGHMobileMoneyPayments(true)
                .acceptRwfMobileMoneyPayments(true)
                .acceptSaBankPayments(true)
                .acceptFrancMobileMoneyPayments(true)
                .acceptZmMobileMoneyPayments(true)
                .acceptUssdPayments(true)
                .acceptUkPayments(true)
                .acceptMpesaPayments(true)
                .shouldDisplayFee(true)
                .onStagingEnv(false)
                .showStagingLabel(false)
                .initialize();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RaveConstants.RAVE_REQUEST_CODE && data != null) {
            new PaymentModelClass(activity).TransactionMethod(data, activity, Constant.PAYMENT);
        } else if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null && data.getStringExtra("response") != null) {
            try {
                JSONObject details = new JSONObject(data.getStringExtra("response"));
                JSONObject jsonObject = details.getJSONObject(Constant.DATA);

                if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                    Toast.makeText(activity, getString(R.string.order_placed1), Toast.LENGTH_LONG).show();
                    PlaceOrder(activity, getString(R.string.flutterwave), jsonObject.getString("txRef"), true, sendParams, Constant.SUCCESS);
                } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                    PlaceOrder(activity, "", "", false, sendParams, Constant.PENDING);
                    Toast.makeText(activity, getString(R.string.order_error), Toast.LENGTH_LONG).show();
                } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                    PlaceOrder(activity, "", "", false, sendParams, Constant.FAILED);
                    Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            if (PaymentActivity.dialog_ != null) {
                PaymentActivity.dialog_.dismiss();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.payment);
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        try {
            if (WalletTransactionFragment.payFromWallet) {
                WalletTransactionFragment.payFromWallet = false;
                new WalletTransactionFragment().AddWalletBalance(activity, new Session(activity), WalletTransactionFragment.amount, WalletTransactionFragment.msg);
            } else {
                PaymentActivity.razorPayId = razorpayPaymentID;
                new PaymentActivity().PlaceOrder(PaymentActivity.this, PaymentActivity.paymentMethod, PaymentActivity.razorPayId, true, PaymentActivity.sendParams, Constant.SUCCESS);
            }
        } catch (Exception e) {
            Log.d(TAG, "onPaymentSuccess  ", e);
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        try {
            try {
                if (PaymentActivity.dialog_ != null) {
                    PaymentActivity.dialog_.dismiss();
                }
            } catch (Exception ignore) {

            }
            Toast.makeText(activity, getString(R.string.order_cancel), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d(TAG, "onPaymentError  ", e);
        }
    }

    /*   Address Part Start   */

    public void getAddresses() {
        singleAddress = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerViewSingleAddress.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ADDRESSES, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.ADDRESS_ID, Constant.selectedAddressId);


        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        session.setData(Constant.TOTAL, String.valueOf(total));

                        JSONObject jsonObject1 = jsonObject.getJSONArray(Constant.DATA).getJSONObject(0);
                        Address address = new Gson().fromJson(jsonObject1.toString(), Address.class);
                        address.setSelected(true);

                        singleAddress.add(address);
                        getTimeSlots(session, activity);

                        addressAdapter = new AddressAdapter(activity, singleAddress, R.layout.lyt_address_checkout);
                        recyclerViewSingleAddress.setAdapter(addressAdapter);

                    } else {
                        tvChangeAddress.setText(activity.getString(R.string.add_address));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_ADDRESS_URL, params, false);
    }

    /*  Cart Items Part Start   */

    @SuppressLint("SetTextI18n")
    void getCartData(boolean isCOD) {
        carts = new ArrayList<>();
        ApiConfig.getCartItemCount(activity, session);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.IS_COD, isCOD ? "1" : "0");
        params.put(Constant.ADDRESS_ID, Constant.selectedAddressId);
        params.put(Constant.LIMIT, "" + Constant.TOTAL_CART_ITEM);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {

                        if (getIntent().getStringExtra(Constant.PROMO_CODE) != null && !getIntent().getStringExtra(Constant.PROMO_CODE).isEmpty()) {
                            pCode = getIntent().getStringExtra(Constant.PROMO_CODE);
                            pCodeDiscount = getIntent().getDoubleExtra(Constant.PROMO_DISCOUNT, 0);

                            lytPromoDiscount.setVisibility(View.VISIBLE);
                            tvPromoDiscount.setText("-" + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + pCodeDiscount));
                            tvPromoCode.setText(activity.getString(R.string.promo_discount) + " (" + pCode + ")");
                        } else {
                            lytPromoDiscount.setVisibility(View.GONE);
                        }

                        subTotal = ((Double.parseDouble(jsonObject.getString(Constant.SUB_TOTAL)) - pCodeDiscount) - pCodeDiscount);
                        deliveryCharge = Double.parseDouble(jsonObject.getString(Constant.DELIVERY_CHARGE));
                        grandTotal = subTotal + deliveryCharge;
                        savedAmount = Double.parseDouble(jsonObject.getString(Constant.SAVED_AMOUNT));

                        tvSubTotal.setText(session.getData(Constant.CURRENCY) + subTotal);
                        tvDeliveryCharge.setText(session.getData(Constant.CURRENCY) + deliveryCharge);
                        tvSaveAmount.setText(session.getData(Constant.CURRENCY) + savedAmount);
                        tvGrandTotal.setText(session.getData(Constant.CURRENCY) + grandTotal);

                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                        Gson gson = new Gson();
                        variantIdList = new ArrayList<>();
                        qtyList = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                Cart cart = gson.fromJson(String.valueOf(jsonArray.getJSONObject(i)), Cart.class);
                                if (cart.getSave_for_later().equals("0")) {
                                    variantIdList.add(cart.getProduct_variant_id());
                                    qtyList.add(cart.getQty());
                                    carts.add(cart);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        checkoutItemListAdapter = new CheckoutItemListAdapter(activity, carts);
                        recyclerViewCartItems.setAdapter(checkoutItemListAdapter);

                        confirmLyt.setVisibility(View.VISIBLE);
                        recyclerViewCartItems.setVisibility(View.VISIBLE);
                    }
                    hideShimmer();
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideShimmer();
                }
            }
        }, activity, Constant.CART_URL, params, false);
    }

    /*   Cart Items Part End
    =======================================================================
         Change Payment Method Actions Start */

    public void showPaymentOptions() {
        lytMainPaymentMethods.setVisibility(View.VISIBLE);
        lytPaymentMethods.setVisibility(View.VISIBLE);
        lytPaymentMethods.startAnimation(animShow);
    }

    public void hidePaymentOptions() {
        lytPaymentMethods.setVisibility(View.GONE);
        lytPaymentMethods.startAnimation(animHide);

        animHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lytMainPaymentMethods.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /*  Change Payment Method Actions End
    =======================================================================
        Shimmer Layout Actions Start   */
    public void showShimmer() {
        lytMain.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
    }

    public void hideShimmer() {
        lytMain.setVisibility(View.VISIBLE);
        shimmerFrameLayout.setVisibility(View.GONE);
        shimmerFrameLayout.stopShimmer();
    }

    /*  Shimmer Layout Actions End

        Progress Dialog Start */
    public void setProgressDialog() {
        int llPadding = 30;
        LinearLayout ll = new LinearLayout(activity);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(activity);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(activity);
        tvText.setText(activity.getString(R.string.please_wait));
        tvText.setTextColor((ContextCompat.getColor(activity, R.color.colorPrimary)));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);
        builder.setView(ll);

        dialog_ = builder.create();
        dialog_.show();
        Window window = dialog_.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog_.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog_.getWindow().setAttributes(layoutParams);
        }
    }
}