package wrteam.multivendor.shop.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.activity.LoginActivity;
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.adapter.CartAdapter;
import wrteam.multivendor.shop.adapter.OfflineCartAdapter;
import wrteam.multivendor.shop.adapter.OfflineSaveForLaterAdapter;
import wrteam.multivendor.shop.adapter.SaveForLaterAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.DatabaseHelper;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Cart;
import wrteam.multivendor.shop.model.OfflineCart;
import wrteam.multivendor.shop.model.PromoCode;

@SuppressLint("SetTextI18n")
public class CartFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout lytEmpty;
    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout lytTotal;
    public static ArrayList<Cart> carts, saveForLater;
    public static ArrayList<OfflineCart> offlineCarts, offlineSaveForLaterItems;
    @SuppressLint("StaticFieldLeak")
    public static CartAdapter cartAdapter;
    @SuppressLint("StaticFieldLeak")
    public static SaveForLaterAdapter saveForLaterAdapter;
    @SuppressLint("StaticFieldLeak")
    public static OfflineCartAdapter offlineCartAdapter;
    @SuppressLint("StaticFieldLeak")
    public static OfflineSaveForLaterAdapter offlineSaveForLaterAdapter;
    public static HashMap<String, String> values, saveForLaterValues;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTotalAmount, tvTotalItems, tvConfirmOrder, tvSaveForLaterTitle;
    Activity activity;
    @SuppressLint("StaticFieldLeak")
    static Session session;
    static JSONObject jsonObject;
    View root;
    RecyclerView cartRecycleView, saveForLaterRecyclerView;
    NestedScrollView scrollView;
    RelativeLayout lytPinCode;
    double total;
    Button btnShowNow;
    DatabaseHelper databaseHelper;
    TextView tvTitleLocation;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvLocation;
    private ShimmerFrameLayout shimmerFrameLayout;
    @SuppressLint("StaticFieldLeak")
    public static LinearLayout lytSaveForLater;
    ArrayList<String> variantIdList, qtyList;
    public static SwipeRefreshLayout.OnRefreshListener refreshListener;
    public static boolean isDeliverable = false;
    public static boolean isSoldOut = false;

    Button btnRemoveOffer;
    TextView tvAppliedPromoCodeAmount;
    TextView tvAppliedPromoCode;
    LinearLayout lytPromoDiscount;
    RelativeLayout lytPromoCode, lytAppliedPromoCode;
    TextView tvPromoCode;
    TextView tvPromoDiscount;
    PromoCodeAdapter promoCodeAdapter;
    public static String pCode = "";
    double pCodeDiscount = 0.0;
    LottieAnimationView lottieAnimationViewParty, lottieAnimationViewSmile;

    ArrayList<PromoCode> promoCodes;
    boolean isApplied;
    double subTotal = 0;
    int offset = 0;
    boolean isLoadMore;

    @SuppressLint("SetTextI18n")
    public static void setData(Activity activity) {
        tvTotalAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(String.valueOf(Constant.FLOAT_TOTAL_AMOUNT)));
        int count;
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            count = carts.size();
        } else {
            count = offlineCarts.size();
        }
        tvTotalItems.setText(count + (count > 1 ? activity.getString(R.string.items) : activity.getString(R.string.item)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_cart, container, false);

        values = new HashMap<>();
        saveForLaterValues = new HashMap<>();
        activity = getActivity();
        session = new Session(getActivity());
        lytTotal = root.findViewById(R.id.lytTotal);
        lytEmpty = root.findViewById(R.id.lytEmpty);
        btnShowNow = root.findViewById(R.id.btnShowNow);
        tvTotalAmount = root.findViewById(R.id.tvTotalAmount);
        tvTotalItems = root.findViewById(R.id.tvTotalItems);
        lytSaveForLater = root.findViewById(R.id.lytSaveForLater);
        tvSaveForLaterTitle = root.findViewById(R.id.tvSaveForLaterTitle);
        scrollView = root.findViewById(R.id.scrollView);
        cartRecycleView = root.findViewById(R.id.cartRecycleView);
        saveForLaterRecyclerView = root.findViewById(R.id.saveForLaterRecyclerView);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        shimmerFrameLayout = root.findViewById(R.id.shimmerFrameLayout);
        tvLocation = root.findViewById(R.id.tvLocation);
        tvTitleLocation = root.findViewById(R.id.tvTitleLocation);
        lytPinCode = root.findViewById(R.id.lytPinCode);

        carts = new ArrayList<>();
        saveForLater = new ArrayList<>();
        offlineCarts = new ArrayList<>();
        offlineSaveForLaterItems = new ArrayList<>();

        pCode = "";

        btnRemoveOffer = root.findViewById(R.id.btnRemoveOffer);
        tvAppliedPromoCodeAmount = root.findViewById(R.id.tvAppliedPromoCodeAmount);
        tvAppliedPromoCode = root.findViewById(R.id.tvAppliedPromoCode);
        lytPromoDiscount = root.findViewById(R.id.lytPromoDiscount);
        lytPromoCode = root.findViewById(R.id.lytPromoCode);
        lytAppliedPromoCode = root.findViewById(R.id.lytAppliedPromoCode);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvPromoDiscount = root.findViewById(R.id.tvPromoDiscount);
        lottieAnimationViewParty = root.findViewById(R.id.lottieAnimationViewParty);
        lottieAnimationViewSmile = root.findViewById(R.id.lottieAnimationViewSmile);

        lytPromoCode.setOnClickListener(v -> {
            if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                openDialog(activity);
            } else {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            startActivity(new Intent(activity, LoginActivity.class).putExtra(Constant.FROM, "checkout"));
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.dismiss();
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(activity.getString(R.string.promo_code_use_message))
                        .setPositiveButton(activity.getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(activity.getString(R.string.no), dialogClickListener).show();
            }
        });

        btnRemoveOffer.setOnClickListener(v -> {
            btnRemoveOffer.setVisibility(View.GONE);
            tvTotalAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + Constant.FLOAT_TOTAL_AMOUNT));
            pCode = "";
            tvPromoCode.setText(activity.getString(R.string.use_promo_code));
            isApplied = false;
            lytPromoDiscount.setVisibility(View.GONE);
            pCodeDiscount = 0.0;
        });

        databaseHelper = new DatabaseHelper(activity);

        setHasOptionsMenu(true);

        tvLocation.setText(session.getData(Constant.GET_SELECTED_PINCODE_NAME));

        variantIdList = new ArrayList<>();
        qtyList = new ArrayList<>();

        cartRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        saveForLaterRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        GetSettings(activity);

        refreshListener = () -> GetSettings(activity);

        if (session.getData(Constant.SHIPPING_TYPE).equals("local")) {
            lytPinCode.setVisibility(View.VISIBLE);

            if (session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0") || session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("")) {
                MainActivity.pinCodeFragment = new PinCodeFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "cart");
                MainActivity.pinCodeFragment.setArguments(bundle);
                MainActivity.pinCodeFragment.show(MainActivity.fm, null);
            }

            tvTitleLocation.setOnClickListener(v -> {
                MainActivity.pinCodeFragment = new PinCodeFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "cart");
                MainActivity.pinCodeFragment.setArguments(bundle);
                MainActivity.pinCodeFragment.show(MainActivity.fm, null);
            });

            tvLocation.setOnClickListener(v -> {
                MainActivity.pinCodeFragment = new PinCodeFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "cart");
                MainActivity.pinCodeFragment.setArguments(bundle);
                MainActivity.pinCodeFragment.show(MainActivity.fm, null);
            });
        }

        tvConfirmOrder.setOnClickListener(v -> {
            if (!isSoldOut && !isDeliverable) {
                if (Float.parseFloat(session.getData(Constant.min_order_amount)) <= Constant.FLOAT_TOTAL_AMOUNT) {
                    if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                        if (values.size() > 0) {
                            ApiConfig.AddMultipleProductInCart(session, activity, values);
                        }
                        Fragment fragment = new AddressListFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.FROM, "process");
                        bundle.putString(Constant.PROMO_CODE, pCode);
                        bundle.putDouble(Constant.PROMO_DISCOUNT, pCodeDiscount);
                        bundle.putDouble(Constant.TOTAL, Constant.FLOAT_TOTAL_AMOUNT);
                        fragment.setArguments(bundle);
                        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
                    } else {
                        startActivity(new Intent(activity, LoginActivity.class).putExtra(Constant.FROM, "checkout"));
                    }
                } else {
                    Toast.makeText(activity, getString(R.string.msg_minimum_order_amount) + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat(session.getData(Constant.min_order_amount)), Toast.LENGTH_SHORT).show();
                }

            } else if (isDeliverable) {
                Toast.makeText(activity, getString(R.string.msg_non_deliverable), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, getString(R.string.msg_sold_out), Toast.LENGTH_SHORT).show();
            }

        });

        btnShowNow.setOnClickListener(v -> MainActivity.fm.popBackStack());

        return root;
    }

    private void GetOfflineCart() {
        CartFragment.isDeliverable = false;
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_VARIANTS_OFFLINE, Constant.GetVal);
        params.put(Constant.VARIANT_IDs, databaseHelper.getCartList().toString().replace("[", "").replace("]", "").replace("\"", ""));
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE, session.getData(Constant.GET_SELECTED_PINCODE_NAME));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        session.setData(Constant.TOTAL, jsonObject.getString(Constant.TOTAL));

                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);

                        Gson g = new Gson();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            OfflineCart cart = g.fromJson(jsonObject1.toString(), OfflineCart.class);

                            variantIdList.add(cart.getProduct_variant_id());
                            qtyList.add(databaseHelper.CheckCartItemExist(cart.getProduct_variant_id(), cart.getProduct_id()));

                            double price;
                            String taxPercentage = "0";

                            try {
                                taxPercentage = (Double.parseDouble(cart.getItem().get(0).getTax_percentage()) > 0 ? cart.getItem().get(0).getTax_percentage() : "0");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (cart.getItem().get(0).getDiscounted_price().equals("0") || cart.getItem().get(0).getDiscounted_price().equals("")) {
                                price = ((Float.parseFloat(cart.getItem().get(0).getPrice()) + ((Float.parseFloat(cart.getItem().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                            } else {
                                price = ((Float.parseFloat(cart.getItem().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItem().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                            }

                            Constant.FLOAT_TOTAL_AMOUNT += (price * Integer.parseInt(databaseHelper.CheckCartItemExist(cart.getProduct_variant_id(), cart.getProduct_id())));

                            offlineCarts.add(cart);
                        }

                        offlineCartAdapter = new OfflineCartAdapter(activity);
                        cartRecycleView.setAdapter(offlineCartAdapter);

                        setData(activity);

                        lytTotal.setVisibility(View.VISIBLE);

                    }
                    GetOfflineSaveForLater();
                } catch (JSONException e) {
                    GetOfflineSaveForLater();

                }
            } else {
                GetOfflineSaveForLater();
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }


    private void GetOfflineSaveForLater() {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_VARIANTS_OFFLINE, Constant.GetVal);
        params.put(Constant.VARIANT_IDs, databaseHelper.getSaveForLaterList().toString().replace("[", "").replace("]", "").replace("\"", ""));
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE, session.getData(Constant.GET_SELECTED_PINCODE_NAME));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            OfflineCart cart = new Gson().fromJson(jsonObject1.toString(), OfflineCart.class);
                            offlineSaveForLaterItems.add(cart);
                        }

                        offlineSaveForLaterAdapter = new OfflineSaveForLaterAdapter(activity);
                        saveForLaterRecyclerView.setAdapter(offlineSaveForLaterAdapter);

                        tvSaveForLaterTitle.setText(activity.getResources().getString(R.string.save_for_later) + " (" + offlineSaveForLaterItems.size() + ")");

                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        lytSaveForLater.setVisibility(View.VISIBLE);
                    } else {
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        lytSaveForLater.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    lytSaveForLater.setVisibility(View.GONE);

                }
            } else {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                lytSaveForLater.setVisibility(View.GONE);
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }

    public void GetSettings(final Activity activity) {
        Constant.FLOAT_TOTAL_AMOUNT = 0.00;
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        Session session = new Session(activity);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.SETTINGS, Constant.GetVal);
        params.put(Constant.GET_TIMEZONE, Constant.GetVal);
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONObject object = jsonObject.getJSONObject(Constant.SETTINGS);

                        session.setData(Constant.minimum_version_required, object.getString(Constant.minimum_version_required));
                        session.setData(Constant.is_version_system_on, object.getString(Constant.is_version_system_on));

                        session.setData(Constant.CURRENCY, object.getString(Constant.CURRENCY));

                        session.setData(Constant.min_order_amount, object.getString(Constant.min_order_amount));
                        session.setData(Constant.max_cart_items_count, object.getString(Constant.max_cart_items_count));
                        session.setData(Constant.area_wise_delivery_charge, object.getString(Constant.area_wise_delivery_charge));

                        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
                            getCartData();
                        } else {
                            offlineCarts = new ArrayList<>();
                            offlineCartAdapter = new OfflineCartAdapter(activity);
                            cartRecycleView.setAdapter(offlineCartAdapter);

                            offlineSaveForLaterItems = new ArrayList<>();
                            offlineSaveForLaterAdapter = new OfflineSaveForLaterAdapter(activity);
                            saveForLaterRecyclerView.setAdapter(offlineSaveForLaterAdapter);

                            GetOfflineCart();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.SETTING_URL, params, false);
    }

    private void getCartData() {
        CartFragment.isDeliverable = false;

        carts = new ArrayList<>();
        cartAdapter = new CartAdapter(activity);
        cartRecycleView.setAdapter(cartAdapter);

        saveForLater = new ArrayList<>();
        saveForLaterAdapter = new SaveForLaterAdapter(activity);
        saveForLaterRecyclerView.setAdapter(saveForLaterAdapter);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE, session.getData(Constant.GET_SELECTED_PINCODE_NAME));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            if (jsonObject1 != null) {
                                if (!jsonObject1.has("is_item_deliverable")) {
                                    jsonObject1.getJSONArray("item").getJSONObject(0).put("is_item_deliverable", true);
                                }
                                Cart cart = new Gson().fromJson(jsonObject1.toString(), Cart.class);
                                variantIdList.add(cart.getProduct_variant_id());
                                qtyList.add(cart.getQty());

                                double price;
                                String taxPercentage = "0";

                                try {
                                    taxPercentage = (Double.parseDouble(cart.getItems().get(0).getTax_percentage()) > 0 ? cart.getItems().get(0).getTax_percentage() : "0");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
                                    price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                                } else {
                                    price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                                }

                                Constant.FLOAT_TOTAL_AMOUNT += (price * Double.parseDouble(cart.getQty()));
                                carts.add(cart);
                            } else {
                                break;
                            }
                        }

                        setData(activity);


                        JSONArray jsonArraySaveForLater = object.getJSONArray(Constant.SAVE_FOR_LATER);

                        for (int i = 0; i < jsonArraySaveForLater.length(); i++) {
                            JSONObject jsonObject1 = jsonArraySaveForLater.getJSONObject(i);
                            if (jsonObject1 != null) {
                                Cart cart = new Gson().fromJson(jsonObject1.toString(), Cart.class);
                                saveForLater.add(cart);
                            } else {
                                break;
                            }
                        }

                        cartAdapter = new CartAdapter(activity);
                        cartRecycleView.setAdapter(cartAdapter);

                        tvSaveForLaterTitle.setText(activity.getResources().getString(R.string.save_for_later) + " (" + saveForLater.size() + ")");

                        if (jsonArraySaveForLater.length() == 0) {
                            lytSaveForLater.setVisibility(View.GONE);
                        } else {
                            lytSaveForLater.setVisibility(View.VISIBLE);
                            saveForLaterAdapter = new SaveForLaterAdapter(activity);
                            saveForLaterRecyclerView.setAdapter(saveForLaterAdapter);
                        }

                        lytTotal.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        Constant.TOTAL_CART_ITEM = Integer.parseInt(jsonObject.getString(Constant.TOTAL));

                    } else {
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        lytEmpty.setVisibility(View.VISIBLE);
                        lytTotal.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);

                }
            }
        }, activity, Constant.CART_URL, params, false);
    }

    /*   Promo Code Part Start   */

    @SuppressLint("ClickableViewAccessibility")
    public void openDialog(Activity activity) {
        offset = 0;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater1 = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View dialogView = inflater1.inflate(R.layout.dialog_promo_code_selection, null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(true);
        final AlertDialog dialog = alertDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RecyclerView recyclerViewTimeSlot;
        NestedScrollView scrollView;
        TextView tvAlert;
        Button btnCancel;
        ShimmerFrameLayout shimmerFrameLayout;

        scrollView = dialogView.findViewById(R.id.scrollView);
        tvAlert = dialogView.findViewById(R.id.tvAlert);
        btnCancel = dialogView.findViewById(R.id.btnCancel);
        recyclerViewTimeSlot = dialogView.findViewById(R.id.recyclerView);
        shimmerFrameLayout = dialogView.findViewById(R.id.shimmerFrameLayout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerViewTimeSlot.setLayoutManager(linearLayoutManager);

        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

        tvAlert.setText(getString(R.string.no_promo_code_found));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        getPromoCodes(recyclerViewTimeSlot, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout);

        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    void getPromoCodes(RecyclerView recyclerViewTimeSlot, TextView tvAlert, LinearLayoutManager linearLayoutManager, NestedScrollView scrollView, AlertDialog dialog, ShimmerFrameLayout shimmerFrameLayout) {
        promoCodes = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_PROMO_CODES, Constant.GetVal);
        params.put(Constant.USER_ID, "" + session.getData(Constant.ID));
        params.put(Constant.AMOUNT, String.valueOf(Constant.FLOAT_TOTAL_AMOUNT));
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
        params.put(Constant.OFFSET, "" + offset);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        try {

                            total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));

                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                PromoCode promoCode = new Gson().fromJson(jsonObject1.toString(), PromoCode.class);
                                promoCodes.add(promoCode);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (offset == 0) {
                            recyclerViewTimeSlot.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                            promoCodeAdapter = new PromoCodeAdapter(activity, promoCodes, dialog);
                            promoCodeAdapter.setHasStableIds(true);
                            recyclerViewTimeSlot.setAdapter(promoCodeAdapter);
                            shimmerFrameLayout.setVisibility(View.GONE);
                            shimmerFrameLayout.stopShimmer();
                            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                                // if (diff == 0) {
                                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    if (promoCodes.size() < total) {
                                        if (!isLoadMore) {
                                            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == promoCodes.size() - 1) {
                                                //bottom of list!
                                                promoCodes.add(null);
                                                promoCodeAdapter.notifyItemInserted(promoCodes.size() - 1);
                                                offset += Constant.LOAD_ITEM_LIMIT;

                                                Map<String, String> params1 = new HashMap<>();
                                                params1.put(Constant.GET_PROMO_CODES, Constant.GetVal);
                                                params1.put(Constant.USER_ID, "" + session.getData(Constant.ID));
                                                params1.put(Constant.AMOUNT, String.valueOf(Constant.FLOAT_TOTAL_AMOUNT));
                                                params1.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);
                                                params1.put(Constant.OFFSET, "" + offset);

                                                ApiConfig.RequestToVolley((result1, response1) -> {
                                                    if (result1) {
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject(response1);
                                                            if (!jsonObject1.getBoolean(Constant.ERROR)) {
                                                                promoCodes.remove(promoCodes.size() - 1);
                                                                promoCodeAdapter.notifyItemRemoved(promoCodes.size());

                                                                JSONObject object = new JSONObject(response1);
                                                                JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                                                                Gson g = new Gson();
                                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                                                                    PromoCode promoCode = g.fromJson(jsonObject2.toString(), PromoCode.class);
                                                                    promoCodes.add(promoCode);
                                                                }
                                                                promoCodeAdapter.notifyDataSetChanged();
                                                                promoCodeAdapter.setLoaded();
                                                                isLoadMore = false;
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }, activity, Constant.PROMO_CODE_CHECK_URL, params1, false);

                                            }
                                            isLoadMore = true;
                                        }

                                    }
                                }
                            });
                        }
                    } else {
                        shimmerFrameLayout.setVisibility(View.GONE);
                        shimmerFrameLayout.stopShimmer();
                        recyclerViewTimeSlot.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    e.printStackTrace();
                }
            }
        }, activity, Constant.PROMO_CODE_CHECK_URL, params, false);
    }

    class PromoCodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // for load more
        public final int VIEW_TYPE_ITEM = 0;
        public final int VIEW_TYPE_LOADING = 1;
        final Activity activity;
        final ArrayList<PromoCode> promoCodes;
        public boolean isLoading;
        final Session session;
        final AlertDialog dialog;


        public PromoCodeAdapter(Activity activity, ArrayList<PromoCode> promoCodes, AlertDialog dialog) {
            this.activity = activity;
            this.session = new Session(activity);
            this.promoCodes = promoCodes;
            this.dialog = dialog;
        }

        public void add(int position, PromoCode promoCode) {
            promoCodes.add(position, promoCode);
            notifyItemInserted(position);
        }

        public void setLoaded() {
            isLoading = false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
            View view;
            switch (viewType) {
                case (VIEW_TYPE_ITEM):
                    view = LayoutInflater.from(activity).inflate(R.layout.lyt_promo_code_list, parent, false);
                    return new PromoCodeAdapter.HolderItems(view);
                case (VIEW_TYPE_LOADING):
                    view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                    return new PromoCodeAdapter.ViewHolderLoading(view);
                default:
                    throw new IllegalArgumentException("unexpected viewType: " + viewType);
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderParent, final int position) {

            if (holderParent instanceof PromoCodeAdapter.HolderItems) {
                final PromoCodeAdapter.HolderItems holder = (PromoCodeAdapter.HolderItems) holderParent;
                try {
                    final PromoCode promoCode = promoCodes.get(position);

                    holder.tvMessage.setText(promoCode.getMessage());

                    holder.tvPromoCode.setText(promoCode.getPromo_code());

                    if (promoCode.getIs_validate().get(0).isError()) {
                        holder.tvMessageAlert.setTextColor(ContextCompat.getColor(activity, R.color.tx_promo_code_fail));
                        holder.tvMessageAlert.setText(promoCode.getIs_validate().get(0).getMessage());
                        holder.tvApply.setTextColor(ContextCompat.getColor(activity, R.color.gray));
                    } else {
                        holder.tvMessageAlert.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                        holder.tvMessageAlert.setText(activity.getString(R.string.you_will_save) + session.getData(Constant.CURRENCY) + promoCode.getIs_validate().get(0).getDiscount() + activity.getString(R.string.with_this_code));
                        holder.tvApply.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                    }

                    if (pCode.equals(promoCode.getPromo_code())) {
                        holder.tvApply.setEnabled(false);
                        holder.tvApply.setText(activity.getString(R.string.applied));
                        holder.tvApply.setTextColor(ContextCompat.getColor(activity, R.color.green));
                    } else {
                        holder.tvApply.setEnabled(true);
                    }

                    holder.tvApply.setOnClickListener(v -> {
                        try {
                            if (!promoCode.getIs_validate().get(0).isError()) {
                                pCode = promoCode.getPromo_code();
                                btnRemoveOffer.setVisibility(View.VISIBLE);
                                btnRemoveOffer.setText(activity.getString(R.string.remove_offer));
                                btnRemoveOffer.setTag("applied");
                                isApplied = true;
                                pCode = promoCode.getPromo_code();
                                tvPromoCode.setText(activity.getString(R.string.applied) + " " + pCode);
                                pCodeDiscount = Double.parseDouble(promoCode.getIs_validate().get(0).getDiscount());
                                subTotal = Double.parseDouble(promoCode.getIs_validate().get(0).getDiscounted_amount());
                                tvTotalAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subTotal));
                                lytPromoDiscount.setVisibility(View.VISIBLE);
                                tvPromoDiscount.setText("-" + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + promoCode.getIs_validate().get(0).getDiscount()));
                                dialog.dismiss();
                                openPartyDialog(session, activity);
                            } else {
                                ObjectAnimator.ofFloat(holder.tvMessageAlert, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0).setDuration(300).start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (holderParent instanceof PromoCodeAdapter.ViewHolderLoading) {
                PromoCodeAdapter.ViewHolderLoading loadingViewHolder = (PromoCodeAdapter.ViewHolderLoading) holderParent;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        private void openPartyDialog(Session session, Activity activity) {
            try {
                lytAppliedPromoCode.setVisibility(View.VISIBLE);

                lottieAnimationViewParty.setAnimation("celebration.json");
                lottieAnimationViewParty.playAnimation();

                lottieAnimationViewSmile.setAnimation("promo_applied.json");
                lottieAnimationViewSmile.playAnimation();

                tvAppliedPromoCodeAmount.setText(activity.getString(R.string.you_saved) + session.getData(Constant.CURRENCY) + pCodeDiscount);
                tvAppliedPromoCode.setText(activity.getString(R.string.with) + "\"" + pCode + "\"" + activity.getString(R.string.code));

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    lytAppliedPromoCode.setVisibility(View.GONE);
                    lottieAnimationViewParty.clearAnimation();
                    lottieAnimationViewSmile.clearAnimation();
                }, 4000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return promoCodes.size();
        }

        @Override
        public int getItemViewType(int position) {
            return promoCodes.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        class ViewHolderLoading extends RecyclerView.ViewHolder {
            public final ProgressBar progressBar;

            public ViewHolderLoading(View view) {
                super(view);
                progressBar = view.findViewById(R.id.itemProgressbar);
            }
        }

        class HolderItems extends RecyclerView.ViewHolder {

            final TextView tvMessage, tvPromoCode, tvMessageAlert, tvApply;

            public HolderItems(@NonNull View itemView) {
                super(itemView);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvPromoCode = itemView.findViewById(R.id.tvPromoCode);
                tvMessageAlert = itemView.findViewById(R.id.tvMessageAlert);
                tvApply = itemView.findViewById(R.id.tvApply);
            }
        }
    }

    /*   Promo Code Part End   */

    @Override
    public void onPause() {
        super.onPause();
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            if (values.size() > 0) {
                ApiConfig.AddMultipleProductInCart(session, activity, values);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.cart);
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
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}