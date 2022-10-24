package wrteam.multivendor.shop.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wrteam.multivendor.shop.R;
import wrteam.multivendor.shop.activity.MainActivity;
import wrteam.multivendor.shop.activity.PaymentActivity;
import wrteam.multivendor.shop.adapter.AddressAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Address;

public class AddressListFragment extends Fragment {
    public static RecyclerView recyclerView;
    public static ArrayList<Address> addresses;
    @SuppressLint("StaticFieldLeak")
    public static AddressAdapter addressAdapter;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvAlert;
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;
    public int total = 0;
    FloatingActionButton fabAddAddress;
    View root;
    SwipeRefreshLayout swipeLayout;
    TextView tvTotalItems;
    TextView tvSubTotal;
    TextView tvConfirmOrder;
    RelativeLayout confirmLyt;
    private Session session;
    private ShimmerFrameLayout shimmerFrameLayout;

    LinearLayout lytPromoDiscount;
    TextView tvPromoDiscount;
    TextView tvPromoCode;

    double subTotal, pCodeDiscount;
    String pCode;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_address_list, container, false);
        activity = getActivity();
        session = new Session(activity);

        recyclerView = root.findViewById(R.id.recyclerView);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        tvAlert = root.findViewById(R.id.tvAlert);
        fabAddAddress = root.findViewById(R.id.fabAddAddress);
        tvSubTotal = root.findViewById(R.id.tvSubTotal);
        tvTotalItems = root.findViewById(R.id.tvTotalItems);
        confirmLyt = root.findViewById(R.id.confirmLyt);
        shimmerFrameLayout = root.findViewById(R.id.shimmerFrameLayout);
        lytPromoDiscount = root.findViewById(R.id.lytPromoDiscount);
        tvPromoDiscount = root.findViewById(R.id.tvPromoDiscount);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        Constant.selectedAddressId="";
        getAddresses();

        if (requireArguments().getString(Constant.FROM).equalsIgnoreCase("process")) {
            confirmLyt.setVisibility(View.VISIBLE);
            subTotal = requireArguments().getDouble(Constant.TOTAL);

            if (requireArguments().getString(Constant.PROMO_CODE) != null && !requireArguments().getString(Constant.PROMO_CODE).isEmpty()) {
                pCode = requireArguments().getString(Constant.PROMO_CODE);
                pCodeDiscount = requireArguments().getDouble(Constant.PROMO_DISCOUNT, 0);

                lytPromoDiscount.setVisibility(View.VISIBLE);
                tvPromoDiscount.setText("-" + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + pCodeDiscount));
                tvPromoCode.setText(activity.getString(R.string.promo_discount) + " (" + pCode + ")");
            } else {
                lytPromoDiscount.setVisibility(View.GONE);
            }

            tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (subTotal - pCodeDiscount)));
            tvTotalItems.setText(Constant.TOTAL_CART_ITEM + (Constant.TOTAL_CART_ITEM > 1 ? activity.getString(R.string.items) : activity.getString(R.string.item)));

            tvConfirmOrder.setOnClickListener(view -> {
                if (Constant.selectedAddressId.equals("")) {
                    Toast.makeText(activity, activity.getString(R.string.please_select_address), Toast.LENGTH_SHORT).show();
                } else if (session.getData(Constant.STATUS).equals("1")) {
                    Intent intent = new Intent(activity, PaymentActivity.class);
                    intent.putExtra(Constant.FROM, "process");
                    intent.putExtra(Constant.PROMO_CODE, getArguments().getString(Constant.PROMO_CODE));
                    intent.putExtra(Constant.PROMO_DISCOUNT, getArguments().getDouble(Constant.PROMO_DISCOUNT));
                    startActivity(intent);
                } else {
                    Toast.makeText(activity, activity.getString(R.string.user_block_msg), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            confirmLyt.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);

        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity, R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(() ->

        {
            addresses.clear();
            addressAdapter = null;
            getAddresses();
            swipeLayout.setRefreshing(false);
        });

        fabAddAddress.setOnClickListener(view ->

                addNewAddress());

        return root;
    }

    public void addNewAddress() {
        Fragment fragment = new AddressAddUpdateFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("model", "");
        bundle.putString("for", "add");
        bundle.putInt("position", 0);

        fragment.setArguments(bundle);
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
    }

    public void getAddresses() {
        recyclerView.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        addresses = new ArrayList<>();
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ADDRESSES, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        total = Integer.parseInt(jsonObject.getString(Constant.TOTAL));
                        session.setData(Constant.TOTAL, String.valueOf(total));
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        Gson g = new Gson();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                            if (jsonObject1 != null) {
                                Address address = g.fromJson(jsonObject1.toString(), Address.class);
                                if (address.getIs_default().equals("1")) {
                                    Constant.selectedAddressId = address.getId();
                                }
                                addresses.add(address);
                            } else {
                                break;
                            }

                        }
                        addressAdapter = new AddressAdapter(activity, addresses, R.layout.lyt_address_list);
                        recyclerView.setAdapter(addressAdapter);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tvAlert.setVisibility(View.VISIBLE);
                    }
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, activity, Constant.GET_ADDRESS_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.addresses);
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
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }
}