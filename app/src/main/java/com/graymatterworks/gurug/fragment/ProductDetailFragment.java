package com.graymatterworks.gurug.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.graymatterworks.gurug.helper.ApiConfig.AddOrRemoveFavorite;
import static com.graymatterworks.gurug.helper.ApiConfig.GetSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.graymatterworks.gurug.R;
import com.graymatterworks.gurug.activity.MainActivity;
import com.graymatterworks.gurug.adapter.AdapterStyle1;
import com.graymatterworks.gurug.adapter.SliderAdapter;
import com.graymatterworks.gurug.helper.ApiConfig;
import com.graymatterworks.gurug.helper.Constant;
import com.graymatterworks.gurug.helper.DatabaseHelper;
import com.graymatterworks.gurug.helper.Session;
import com.graymatterworks.gurug.model.PriceVariation;
import com.graymatterworks.gurug.model.Product;
import com.graymatterworks.gurug.model.Slider;

public class ProductDetailFragment extends Fragment {
    static ArrayList<Slider> sliderArrayList;
    TextView tvCodDeliveryCharges, tvOnlineDeliveryCharges, tvOnlineDeliveryChargesDate, btnChangePinCode, tvPinCode, tvSeller, showDiscount, tvMfg, tvMadeIn, txtProductName, tvQuantity, txtPrice, tvOriginalPrice, txtMeasurement, tvStatus, tvTitleMadeIn, tvTitleMfg;
    EditText edtPinCode;
    WebView webDescription;
    ViewPager viewPager;
    Spinner spinner;
    LinearLayout lytSpinner, lytDeliveryCharge;
    ImageView imgIndicator;
    LinearLayout mMarkersLayout, lytMfg, lytMadeIn;
    RelativeLayout lytMainPrice, lytQuantity;
    ScrollView scrollView;
    Session session;
    boolean favorite;
    ImageView imgFav;
    ImageButton imgAdd, imgMinus;
    LinearLayout lytShare, lytSave, lytSimilar;
    View root;
    String from, id;
    boolean isLogin;
    Product product;
    DatabaseHelper databaseHelper;
    int variantPosition = 0;
    Button btnCart;
    Activity activity;
    RecyclerView recyclerView;
    RelativeLayout relativeLayout;
    TextView tvMore;
    ImageView imgReturnable, imgCancellable;
    TextView tvReturnable, tvCancellable;
    String taxPercentage;
    LottieAnimationView lottieAnimationView;
    ShimmerFrameLayout shimmerFrameLayout;
    Button btnAddToCart;

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_product_detail, container, false);

        setHasOptionsMenu(true);
        activity = getActivity();

        Constant.CartValues = new HashMap<>();
        sliderArrayList = new ArrayList<>();

        session = new Session(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        databaseHelper = new DatabaseHelper(activity);

        from = requireArguments().getString(Constant.FROM);

        taxPercentage = "0";

        assert getArguments() != null;
        variantPosition = getArguments().getInt(Constant.VARIANT_POSITION, 0);
        id = getArguments().getString(Constant.ID);

        tvQuantity = root.findViewById(R.id.tvQuantity);
        scrollView = root.findViewById(R.id.scrollView);
        mMarkersLayout = root.findViewById(R.id.layout_markers);
        viewPager = root.findViewById(R.id.viewPager);
        txtProductName = root.findViewById(R.id.tvProductName);
        tvOriginalPrice = root.findViewById(R.id.tvOriginalPrice);
        webDescription = root.findViewById(R.id.txtDescription);
        txtPrice = root.findViewById(R.id.tvPrice);
        txtMeasurement = root.findViewById(R.id.tvMeasurement);
        imgFav = root.findViewById(R.id.imgFav);
        lytMainPrice = root.findViewById(R.id.lytMainPrice);
        lytQuantity = root.findViewById(R.id.lytQuantity);
        tvQuantity = root.findViewById(R.id.tvQuantity);
        tvStatus = root.findViewById(R.id.tvStatus);
        btnAddToCart = root.findViewById(R.id.btnAddToCart);
        imgAdd = root.findViewById(R.id.btnAddQuantity);
        imgMinus = root.findViewById(R.id.btnMinusQuantity);
        spinner = root.findViewById(R.id.spinner);
        lytSpinner = root.findViewById(R.id.lytSpinner);
        lytDeliveryCharge = root.findViewById(R.id.lytDeliveryCharge);
        imgIndicator = root.findViewById(R.id.imgIndicator);
        showDiscount = root.findViewById(R.id.showDiscount);
        tvSeller = root.findViewById(R.id.tvSeller);
        tvSeller.setPaintFlags(tvSeller.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        lytShare = root.findViewById(R.id.lytShare);
        lytSave = root.findViewById(R.id.lytSave);
        lytSimilar = root.findViewById(R.id.lytSimilar);
        tvMadeIn = root.findViewById(R.id.tvMadeIn);
        tvTitleMadeIn = root.findViewById(R.id.tvTitleMadeIn);
        tvMfg = root.findViewById(R.id.tvMfg);
        tvTitleMfg = root.findViewById(R.id.tvTitleMfg);
        lytMfg = root.findViewById(R.id.lytMfg);
        lytMadeIn = root.findViewById(R.id.lytMadeIn);
        btnCart = root.findViewById(R.id.btnCart);
        recyclerView = root.findViewById(R.id.recyclerView);
        relativeLayout = root.findViewById(R.id.relativeLayout);
        tvMore = root.findViewById(R.id.tvMore);
        tvPinCode = root.findViewById(R.id.tvPinCode);
        btnChangePinCode = root.findViewById(R.id.btnChangePinCode);
        edtPinCode = root.findViewById(R.id.edtPinCode);

        tvReturnable = root.findViewById(R.id.tvReturnable);
        tvCancellable = root.findViewById(R.id.tvCancellable);
        imgReturnable = root.findViewById(R.id.imgReturnable);
        imgCancellable = root.findViewById(R.id.imgCancellable);

        tvCodDeliveryCharges = root.findViewById(R.id.tvCodDeliveryCharges);
        tvOnlineDeliveryCharges = root.findViewById(R.id.tvOnlineDeliveryCharges);
        tvOnlineDeliveryChargesDate = root.findViewById(R.id.tvOnlineDeliveryChargesDate);

        lottieAnimationView = root.findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation("add_to_wish_list.json");

        shimmerFrameLayout = root.findViewById(R.id.shimmerFrameLayout);

        GetProductDetail(id);
        GetSettings(activity);

        tvMore.setOnClickListener(v -> ShowSimilar());

        lytSimilar.setOnClickListener(view -> ShowSimilar());

        btnCart.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new CartFragment()).addToBackStack(null).commit());

        lytShare.setOnClickListener(view -> {
            String message = Constant.WebSiteUrl + "product/" + product.getSlug();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_via));
            startActivity(shareIntent);
        });


        btnChangePinCode.setOnClickListener(v ->
        {
            if (btnChangePinCode.getTag().equals("checkLoc")) {
                String pinCode = edtPinCode.getText().toString().trim();
                session.setData(Constant.GET_SELECTED_PINCODE_NAME, pinCode);
                if (!pinCode.isEmpty()) {
                    showKeyboardWithFocus(edtPinCode, activity, false);
                    btnChangePinCode.setTag("changeLoc");
                    btnChangePinCode.setText(activity.getString(R.string.change));

                    edtPinCode.setVisibility(View.GONE);
                    tvPinCode.setVisibility(View.VISIBLE);

                    checkDelivery(product.getPriceVariations().get(variantPosition), pinCode, true);
                } else {
                    edtPinCode.setError(activity.getString(R.string.enter_valid_pin_code));
                    showKeyboardWithFocus(edtPinCode, activity, true);
                }
            } else {
                edtPinCode.setVisibility(View.VISIBLE);
                tvPinCode.setVisibility(View.GONE);

                showKeyboardWithFocus(edtPinCode, activity, true);

                btnChangePinCode.setTag("checkLoc");
                btnChangePinCode.setText(activity.getString(R.string.check));
            }
        });

        return root;
    }

    @SuppressLint("SetTextI18n")
    void checkDelivery(PriceVariation priceVariation, String pinCode, boolean isLoading) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.CHECK_DELIEVABILITY, Constant.GetVal);
        params.put(Constant.PRODUCT_VARIANT_ID, priceVariation.getId());
        params.put(Constant.PINCODE, pinCode);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        tvPinCode.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));
                        tvPinCode.setText(activity.getString(R.string.available_at) + pinCode);

                        if (jsonObject.has(Constant.DELIVERY_CHARGE_WITHOUT_COD) && jsonObject.has(Constant.DELIVERY_CHARGE_WITH_COD)) {
                            lytDeliveryCharge.setVisibility(View.VISIBLE);
                            tvCodDeliveryCharges.setText(activity.getString(R.string.cod_payment) + session.getData(Constant.CURRENCY) + jsonObject.getString(Constant.DELIVERY_CHARGE_WITH_COD));
                            tvOnlineDeliveryCharges.setText(activity.getString(R.string.online_payment) + session.getData(Constant.CURRENCY) + jsonObject.getString(Constant.DELIVERY_CHARGE_WITHOUT_COD));
                            tvOnlineDeliveryChargesDate.setText(activity.getString(R.string.delivery_by) + jsonObject.getString(Constant.ESTIMATED_DATE) + " - " + getDay(jsonObject.getString(Constant.ESTIMATED_DATE)));
                        }
                    } else {
                        tvPinCode.setTextColor(ContextCompat.getColor(activity, R.color.red));
                        tvPinCode.setText(activity.getString(R.string.not_available_at) + pinCode);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, isLoading);
    }

    public void showKeyboardWithFocus(View v, Activity a, boolean isVisible) {
        try {
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (isVisible) {
                v.requestFocus();
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            } else {
                v.clearFocus();
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                a.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ShowSimilar() {
        Fragment fragment = new ProductListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.ID, product.getPriceVariations().get(0).getProduct_id());
        bundle.putString("cat_id", product.getCategory_id());
        bundle.putString(Constant.FROM, "similar");
        bundle.putString("name", "Similar Products");
        fragment.setArguments(bundle);
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
    }


    void GetSimilarData(Product product) {
        ArrayList<Product> productArrayList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
        params.put(Constant.PRODUCT_ID, product.getPriceVariations().get(0).getProduct_id());
        params.put(Constant.CATEGORY_ID, product.getCategory_id());
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE, session.getData(Constant.GET_SELECTED_PINCODE_NAME));
        }
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                        try {
                            productArrayList.addAll(ApiConfig.GetProductList(jsonArray));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        AdapterStyle1 adapter = new AdapterStyle1(activity, productArrayList, R.layout.offer_layout);
                        recyclerView.setAdapter(adapter);
                        relativeLayout.setVisibility(View.VISIBLE);
                    } else {
                        relativeLayout.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }

    void GetProductDetail(final String productId) {
        scrollView.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_PRODUCTS, Constant.GetVal);
        if (session.getBoolean(Constant.GET_SELECTED_PINCODE) && !session.getData(Constant.GET_SELECTED_PINCODE_ID).equals("0")) {
            params.put(Constant.PINCODE, session.getData(Constant.GET_SELECTED_PINCODE_NAME));
        }
        if (from.equals("share")) {
            params.put(Constant.SLUG, productId);
        } else {
            params.put(Constant.PRODUCT_ID, productId);
        }
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                product = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Product.class);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        SetProductDetails(product);
                        GetSimilarData(product);

                    }
                    scrollView.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                } catch (JSONException e) {
                    e.printStackTrace();
                    scrollView.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                }
            }
        }, activity, Constant.GET_PRODUCTS_URL, params, false);
    }


    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    void SetProductDetails(final Product product) {
        try {

            txtProductName.setText(product.getName());
            try {
                taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (product.getMade_in().length() > 0) {
                lytMadeIn.setVisibility(View.VISIBLE);
                tvMadeIn.setText(product.getMade_in());
            }

            if (product.getManufacturer().length() > 0) {
                lytMfg.setVisibility(View.VISIBLE);
                tvMfg.setText(product.getManufacturer());
            }

            tvSeller.setText(product.getSeller_name());

            if (session.getBoolean(Constant.GET_SELECTED_PINCODE)) {
                if (!session.getData(Constant.GET_SELECTED_PINCODE_NAME).equals(activity.getString(R.string.all))) {
                    tvPinCode.setText(activity.getString(R.string.available_at) + session.getData(Constant.GET_SELECTED_PINCODE_NAME));
                }
            }

            tvSeller.setOnClickListener(v -> {
                Fragment fragment = new SellerProductsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constant.ID, product.getSeller_id());
                bundle.putString(Constant.TITLE, product.getSeller_name());
                bundle.putString(Constant.FROM, from);
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            if (isLogin) {
                if (product.isIs_favorite()) {
                    favorite = true;
                    imgFav.setImageResource(R.drawable.ic_is_favorite);
                } else {
                    favorite = false;
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                }
            } else {
                if (databaseHelper.getFavoriteById(product.getPriceVariations().get(0).getProduct_id())) {
                    imgFav.setImageResource(R.drawable.ic_is_favorite);
                } else {
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                }
            }

            if (isLogin) {
                if (Constant.CartValues.containsKey(product.getPriceVariations().get(0).getId())) {
                    tvQuantity.setText("" + Constant.CartValues.get(product.getPriceVariations().get(0).getId()));
                } else {
                    tvQuantity.setText(product.getPriceVariations().get(0).getCart_count());
                }
            } else {
                tvQuantity.setText(databaseHelper.CheckCartItemExist(product.getPriceVariations().get(0).getId(), product.getPriceVariations().get(0).getProduct_id()));
            }

            if (product.getReturn_status().equalsIgnoreCase("1")) {
                imgReturnable.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_returnable));
                tvReturnable.setText(product.getReturn_days() + getString(R.string.days) + getString(R.string.returnable));
            } else {
                imgReturnable.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_not_returnable));
                tvReturnable.setText(getString(R.string.not_returnable));
            }

            if (product.getCancelable_status().equalsIgnoreCase("1")) {
                imgCancellable.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_cancellable));
                tvCancellable.setText(getString(R.string.cancellable_till) + ApiConfig.toTitleCase(product.getTill_status()));
            } else {
                imgCancellable.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_not_cancellable));
                tvCancellable.setText(getString(R.string.not_cancellable));
            }

            if (product.getPriceVariations().size() == 1) {
                spinner.setVisibility(View.INVISIBLE);
                lytSpinner.setVisibility(View.INVISIBLE);
                lytMainPrice.setEnabled(false);
                session.setData(Constant.PRODUCT_VARIANT_ID, "" + 0);
                SetSelectedData(product.getPriceVariations().get(0));
            }

            if (!product.getIndicator().equals("0")) {
                imgIndicator.setVisibility(View.VISIBLE);
                if (product.getIndicator().equals("1"))
                    imgIndicator.setImageResource(R.drawable.ic_veg_icon);
                else if (product.getIndicator().equals("2"))
                    imgIndicator.setImageResource(R.drawable.ic_non_veg_icon);
            }
            CustomAdapter customAdapter = new CustomAdapter();
            spinner.setAdapter(customAdapter);

            webDescription.setVerticalScrollBarEnabled(true);

            webDescription.loadDataWithBaseURL("", product.getDescription(), "text/html", "UTF-8", "");
            webDescription.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
            txtProductName.setText(product.getName());

            spinner.setSelection(variantPosition);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                }

                @Override
                public void onPageSelected(int variantPosition) {
                    ApiConfig.addMarkers(variantPosition, sliderArrayList, mMarkersLayout, activity);
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    variantPosition = i;
                    session.setData(Constant.PRODUCT_VARIANT_ID, "" + i);
                    SetSelectedData(product.getPriceVariations().get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            scrollView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.app_name);
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

    public String getDay(String date) {
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            cal.setTime(Objects.requireNonNull(sdf.parse(date)));// all done

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ApiConfig.getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK), activity);
    }

    @SuppressLint("SetTextI18n")
    public void SetSelectedData(PriceVariation priceVariation) {
        txtMeasurement.setText(" ( " + priceVariation.getMeasurement() + priceVariation.getMeasurement_unit_name() + " ) ");

        sliderArrayList = new ArrayList<>();
        sliderArrayList.add(new Slider(product.getImage()));

        if (priceVariation.getImages().size() != 0) {
            ArrayList<String> arrayList = priceVariation.getImages();
            for (int i = 0; i < arrayList.size(); i++) {
                sliderArrayList.add(new Slider(arrayList.get(i)));
            }
        } else {
            ArrayList<String> arrayList = product.getOther_images();
            for (int i = 0; i < arrayList.size(); i++) {
                sliderArrayList.add(new Slider(arrayList.get(i)));
            }
        }

        if (!session.getData(Constant.GET_SELECTED_PINCODE_NAME).equals(activity.getString(R.string.all)) && !session.getData(Constant.GET_SELECTED_PINCODE_NAME).equals("") && !session.getData(Constant.SHIPPING_TYPE).equals("local")) {
            checkDelivery(product.getPriceVariations().get(variantPosition), session.getData(Constant.GET_SELECTED_PINCODE_NAME), false);
        }

        viewPager.setAdapter(new SliderAdapter(sliderArrayList, activity, R.layout.lyt_detail_slider, "detail"));
        ApiConfig.addMarkers(0, sliderArrayList, mMarkersLayout, activity);
        double DiscountedPrice, OriginalPrice;
        String taxPercentage = "0";
        try {
            taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (priceVariation.getDiscounted_price().equals("0") || priceVariation.getDiscounted_price().equals("")) {
            showDiscount.setVisibility(View.GONE);
            tvOriginalPrice.setVisibility(View.GONE);
            DiscountedPrice = ((Float.parseFloat(priceVariation.getPrice()) + ((Float.parseFloat(priceVariation.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
        } else {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            DiscountedPrice = ((Float.parseFloat(priceVariation.getDiscounted_price()) + ((Float.parseFloat(priceVariation.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            OriginalPrice = (Float.parseFloat(priceVariation.getPrice()) + ((Float.parseFloat(priceVariation.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + OriginalPrice));

            showDiscount.setVisibility(View.VISIBLE);
            showDiscount.setText("-" + ApiConfig.GetDiscount(OriginalPrice, DiscountedPrice));
        }

        txtPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + DiscountedPrice));


        if (isLogin) {
            if (Constant.CartValues.containsKey(priceVariation.getId())) {
                tvQuantity.setText(Constant.CartValues.get(priceVariation.getId()));
            } else {
                tvQuantity.setText(priceVariation.getCart_count());
            }
        } else {
            tvQuantity.setText(databaseHelper.CheckCartItemExist(priceVariation.getId(), priceVariation.getProduct_id()));
        }

        String maxCartCont;

        if (product.getTotal_allowed_quantity() == null || product.getTotal_allowed_quantity().equals("") || product.getTotal_allowed_quantity().equals("0")) {
            maxCartCont = session.getData(Constant.max_cart_items_count);
        } else {
            maxCartCont = product.getTotal_allowed_quantity();
        }

        imgMinus.setOnClickListener(v -> addQuantity(priceVariation, false, maxCartCont));

        imgAdd.setOnClickListener(v -> addQuantity(priceVariation, true, maxCartCont));

        btnAddToCart.setOnClickListener(v -> addQuantity(priceVariation, true, maxCartCont));

        if (isLogin) {
            if (priceVariation.getCart_count().equals("0")) {
                btnAddToCart.setVisibility(View.VISIBLE);
            } else {
                btnAddToCart.setVisibility(View.GONE);
            }
        } else {
            if (!databaseHelper.CheckCartItemExist(priceVariation.getId(), priceVariation.getProduct_id()).equals("0") || databaseHelper.CheckCartItemExist(priceVariation.getId(), priceVariation.getProduct_id()) == null) {
                btnAddToCart.setVisibility(View.GONE);
            } else {
                btnAddToCart.setVisibility(View.VISIBLE);
            }
        }


        lytSave.setOnClickListener(view -> {
            if (isLogin) {
                favorite = product.isIs_favorite();
                if (favorite) {
                    favorite = false;
                    lottieAnimationView.setVisibility(View.GONE);
                    product.setIs_favorite(false);
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                } else {
                    favorite = true;
                    product.setIs_favorite(true);
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                AddOrRemoveFavorite(activity, session, priceVariation.getProduct_id(), favorite);

            } else {
                favorite = databaseHelper.getFavoriteById(product.getPriceVariations().get(0).getProduct_id());
                if (favorite) {
                    favorite = false;
                    lottieAnimationView.setVisibility(View.GONE);
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                } else {
                    favorite = true;
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                databaseHelper.AddOrRemoveFavorite(product.getPriceVariations().get(0).getProduct_id(), favorite);
            }

            switch (from) {
                case "fragment":
                case "sub_cate":
                case "search":
                    ProductListFragment.productArrayList.get(variantPosition).setIs_favorite(favorite);
                    ProductListFragment.mAdapter.notifyDataSetChanged();
                    break;
                case "favorite":
                    if (favorite) {
                        FavoriteFragment.favoriteArrayList.add(product);
                    } else {
                        FavoriteFragment.favoriteArrayList.remove(product);
                    }
                    FavoriteFragment.favoriteLoadMoreAdapter.notifyDataSetChanged();
                    break;
                case "seller":
                    SellerProductsFragment.productArrayList.get(variantPosition).setIs_favorite(favorite);
                    SellerProductsFragment.mAdapter.notifyDataSetChanged();
                    break;
            }
        });

        if (priceVariation.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
            tvStatus.setVisibility(View.VISIBLE);
            lytQuantity.setVisibility(View.GONE);
        } else {
            tvStatus.setVisibility(View.GONE);
            lytQuantity.setVisibility(View.VISIBLE);
        }
    }


    @SuppressLint("SetTextI18n")
    public void addQuantity(PriceVariation extra, boolean isAdd, String maxCartCont) {
        try {
            if (session.getData(Constant.STATUS).equals("1")) {
                int count = Integer.parseInt(tvQuantity.getText().toString());

                if (isAdd) {
                    count++;
                    if (Float.parseFloat(extra.getStock()) >= count) {
                        if (Float.parseFloat(maxCartCont) >= count) {
                            tvQuantity.setText("" + count);
                            if (isLogin) {
                                if (Constant.CartValues.containsKey(extra.getId())) {
                                    Constant.CartValues.replace(extra.getId(), "" + count);
                                } else {
                                    Constant.CartValues.put(extra.getId(), "" + count);
                                }
                                ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                            } else {
                                databaseHelper.AddToCart(extra.getId(), extra.getProduct_id(), "" + count);
                                databaseHelper.getTotalItemOfCart(activity);
                                activity.invalidateOptionsMenu();
                            }
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    count--;
                    tvQuantity.setText("" + count);
                    if (isLogin) {
                        if (Constant.CartValues.containsKey(extra.getId())) {
                            Constant.CartValues.replace(extra.getId(), "" + count);
                        } else {
                            Constant.CartValues.put(extra.getId(), "" + count);
                        }
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    } else {
                        databaseHelper.AddToCart(extra.getId(), extra.getProduct_id(), "" + count);
                        databaseHelper.getTotalItemOfCart(activity);
                        activity.invalidateOptionsMenu();
                    }
                }
                if (count == 0) {
                    btnAddToCart.setVisibility(View.VISIBLE);
                } else {
                    btnAddToCart.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(activity, activity.getString(R.string.user_block_msg), Toast.LENGTH_SHORT).show();
            }
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
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return product.getPriceVariations().size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"ViewHolder", "SetTextI18n", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.tvMeasurement);

            PriceVariation priceVariation = product.getPriceVariations().get(i);
            measurement.setText(priceVariation.getMeasurement() + " " + priceVariation.getMeasurement_unit_name());

            if (priceVariation.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
                measurement.setTextColor(ContextCompat.getColor(activity, R.color.red));
            } else {
                measurement.setTextColor(ContextCompat.getColor(activity, R.color.black));
            }

            return view;
        }
    }
}
