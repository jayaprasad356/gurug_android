package wrteam.multivendor.shop.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import wrteam.multivendor.shop.activity.PaymentActivity;
import wrteam.multivendor.shop.adapter.CheckoutItemListAdapter;
import wrteam.multivendor.shop.helper.ApiConfig;
import wrteam.multivendor.shop.helper.Constant;
import wrteam.multivendor.shop.helper.Session;
import wrteam.multivendor.shop.model.Cart;
import wrteam.multivendor.shop.model.PromoCode;

public class CheckoutFragment extends Fragment {
    public static String pCode = "", appliedCode = "", deliveryCharge = "0";
    public static double pCodeDiscount = 0.0, subtotal = 0.0, dCharge = 0.0; //, total = 0.0; //taxAmt = 0.0,
    public TextView tvConfirmOrder, tvPayment, tvDelivery;
    public ArrayList<String> variantIdList, qtyList;
    public TextView tvSaveAmount, tvAlert, tvTotalBeforeTax, tvDeliveryCharge, tvSubTotal, tvTotalItems;//,tvPromoCode;
    public LinearLayout lytPromoDiscount;
    CardView lytSaveAmount;
    RecyclerView recyclerView;
    View root;
    RelativeLayout confirmLyt;
    boolean isApplied;
    Button btnRemoveOffer;
    TextView tvPromoCode;
    Session session;
    Activity activity;
    CheckoutItemListAdapter checkoutItemListAdapter;
    ArrayList<Cart> carts;
    float OriginalAmount = 0, DiscountedAmount = 0;
    private ShimmerFrameLayout shimmerFrameLayout;
    public static boolean OrderPlacable = false;
    String from;
    int offset = 0;
    int total = 0;
    boolean isLoadMore = false;
    PromoCodeAdapter promoCodeAdapter;
    ArrayList<PromoCode> promoCodes;
    RelativeLayout lytPromoCode;
    TextView tvPromoDiscount;
    double delivery_charge = 0;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_checkout, container, false);

        activity = getActivity();
        session = new Session(activity);
        tvDelivery = root.findViewById(R.id.tvSummary);
        tvPayment = root.findViewById(R.id.tvPayment);
        tvAlert = root.findViewById(R.id.tvAlert);
        btnRemoveOffer = root.findViewById(R.id.btnRemoveOffer);
        tvPromoCode = root.findViewById(R.id.tvPromoCode);
        tvSubTotal = root.findViewById(R.id.tvSubTotal);
        tvTotalItems = root.findViewById(R.id.tvTotalItems);
        tvDeliveryCharge = root.findViewById(R.id.tvDeliveryCharge);
        confirmLyt = root.findViewById(R.id.confirmLyt);
        tvConfirmOrder = root.findViewById(R.id.tvConfirmOrder);
        lytPromoDiscount = root.findViewById(R.id.lytPromoDiscount);
        tvTotalBeforeTax = root.findViewById(R.id.tvTotalBeforeTax);
        tvSaveAmount = root.findViewById(R.id.tvSaveAmount);
        lytSaveAmount = root.findViewById(R.id.lytSaveAmount);
        btnRemoveOffer = root.findViewById(R.id.btnRemoveOffer);
        recyclerView = root.findViewById(R.id.recyclerView);
        shimmerFrameLayout = root.findViewById(R.id.shimmerFrameLayout);
        lytPromoCode = root.findViewById(R.id.lytPromoCode);
        tvPromoDiscount = root.findViewById(R.id.tvPromoDiscount);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        assert getArguments() != null;
        from = getArguments().getString("from");

        OrderPlacable = false;

        setHasOptionsMenu(true);

        tvTotalItems.setText(Constant.TOTAL_CART_ITEM + (Constant.TOTAL_CART_ITEM > 1 ? activity.getString(R.string.items) : activity.getString(R.string.item)));

        Constant.FLOAT_TOTAL_AMOUNT = 0;

        tvConfirmOrder.setOnClickListener(view -> {
            if (subtotal != 0 && Constant.FLOAT_TOTAL_AMOUNT != 0) {
                if (!OrderPlacable) {



                    Intent intent = new Intent(activity, PaymentActivity.class);
                    intent.putExtra("subtotal", Double.parseDouble("" + (subtotal + dCharge)));
                    intent.putExtra("total", Double.parseDouble("" + Constant.FLOAT_TOTAL_AMOUNT));
                    intent.putExtra("pCodeDiscount", Double.parseDouble("" + pCodeDiscount));
                    intent.putExtra("pCode", pCode);
                    intent.putExtra("variantIdList", variantIdList);
                    intent.putExtra("qtyList", qtyList);
                    intent.putExtra(Constant.FROM, "process");

                    PaymentActivity.paymentMethod = "";
                    PaymentActivity.deliveryTime = "";
                    PaymentActivity.deliveryDay = "";

                    startActivity(intent);

                } else {
                    Toast.makeText(activity, activity.getString(R.string.msg_order_can_not_placed), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRemoveOffer.setOnClickListener(v -> getPromoCode());

        tvPromoCode.setOnClickListener(v -> getPromoCode());

        getCartData();


        return root;
    }


    public void getPromoCode() {
        if (btnRemoveOffer.getTag().equals("applied")) {
            pCode = "";
            btnRemoveOffer.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
            btnRemoveOffer.setText(activity.getString(R.string.view_offers));
            tvPromoCode.setText(activity.getString(R.string.use_promo_code));
            btnRemoveOffer.setTag("not_applied");
            isApplied = false;
            appliedCode = "";
            pCodeDiscount = 0;
            SetDataTotal(false);
        } else {
            OpenDialog(activity);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void OpenDialog(Activity activity) {
        offset = 0;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater1 = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View dialogView = inflater1.inflate(R.layout.dialog_promo_code_selection, null);
        alertDialog.setView(dialogView);
        alertDialog.setCancelable(true);
        final AlertDialog dialog = alertDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RecyclerView recyclerView;
        NestedScrollView scrollView;
        TextView tvAlert;
        Button btnCancel;
        ShimmerFrameLayout shimmerFrameLayout;

        scrollView = dialogView.findViewById(R.id.scrollView);
        tvAlert = dialogView.findViewById(R.id.tvAlert);
        btnCancel = dialogView.findViewById(R.id.btnCancel);
        recyclerView = dialogView.findViewById(R.id.recyclerView);
        shimmerFrameLayout = dialogView.findViewById(R.id.shimmerFrameLayout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);

        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

        tvAlert.setText(getString(R.string.no_promo_code_found));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        getPromoCodes(recyclerView, tvAlert, linearLayoutManager, scrollView, dialog, shimmerFrameLayout);

        dialog.show();
    }


    @SuppressLint("NotifyDataSetChanged")
    void getPromoCodes(RecyclerView recyclerView, TextView tvAlert, LinearLayoutManager linearLayoutManager, NestedScrollView scrollView, AlertDialog dialog, ShimmerFrameLayout shimmerFrameLayout) {
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
                            recyclerView.setVisibility(View.VISIBLE);
                            tvAlert.setVisibility(View.GONE);
                            promoCodeAdapter = new PromoCodeAdapter(activity, promoCodes, dialog);
                            promoCodeAdapter.setHasStableIds(true);
                            recyclerView.setAdapter(promoCodeAdapter);
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
                        recyclerView.setVisibility(View.GONE);
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


    void getCartData() {
        carts = new ArrayList<>();
        recyclerView.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();

        ApiConfig.getCartItemCount(activity, session);
        subtotal = 0;
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_USER_CART, Constant.GetVal);
        params.put(Constant.USER_ID, session.getData(Constant.ID));
//        params.put(Constant.ADDRESS_ID, Constant.selectedAddressId);
        params.put(Constant.LIMIT, "" + Constant.TOTAL_CART_ITEM);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
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

                    subtotal = Constant.FLOAT_TOTAL_AMOUNT;
                    checkoutItemListAdapter = new CheckoutItemListAdapter(activity, carts);
                    recyclerView.setAdapter(checkoutItemListAdapter);
                    SetDataTotal(false);

                    confirmLyt.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();

                    confirmLyt.setVisibility(View.VISIBLE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                }
            }
        }, activity, Constant.CART_URL, params, false);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void SetDataTotal(boolean isApplied) {
        try {
            if (isApplied) {
                lytPromoDiscount.setVisibility(View.VISIBLE);
                if ((OriginalAmount - DiscountedAmount) != 0) {
                    lytSaveAmount.setVisibility(View.VISIBLE);
                    if (pCodeDiscount != 0) {
                        tvSaveAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + ((OriginalAmount - DiscountedAmount) + pCodeDiscount)));
                    } else {
                        tvSaveAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + ((OriginalAmount - DiscountedAmount) - pCodeDiscount)));
                    }
                } else {
                    if (pCodeDiscount == 0) {
                        lytSaveAmount.setVisibility(View.GONE);
                    }
                }
            } else {
                subtotal = 0;
                OriginalAmount = 0;
                DiscountedAmount = 0;
                Constant.FLOAT_TOTAL_AMOUNT = 0;
                lytPromoDiscount.setVisibility(View.GONE);
                for (int i = 0; i < carts.size(); i++) {
                    Cart cart = carts.get(i);
                    double price;
                    String taxPercentage = "0";

                    try {
                        taxPercentage = (Double.parseDouble(cart.getItems().get(0).getTax_percentage()) > 0 ? cart.getItems().get(0).getTax_percentage() : "0");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    OriginalAmount += ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                    DiscountedAmount += ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));

                    if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
                        price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));

                    } else {
                        price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                    }

                    Constant.FLOAT_TOTAL_AMOUNT += (price * Double.parseDouble(cart.getQty()));
                    subtotal = Constant.FLOAT_TOTAL_AMOUNT;
                }
                tvSaveAmount.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (OriginalAmount - DiscountedAmount)));
            }
            if (subtotal <= Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY) {
//                tvDeliveryCharge.setText(session.getData(Constant.CURRENCY) + Constant.SETTING_DELIVERY_CHARGE);
//                deliveryCharge = "" + Constant.SETTING_DELIVERY_CHARGE;
            } else {
                tvDeliveryCharge.setText(getResources().getString(R.string.free));
                deliveryCharge = "0";
            }
//            dCharge = tvDeliveryCharge.getText().toString().equals(getString(R.string.free)) ? 0.0 : Constant.SETTING_DELIVERY_CHARGE;
            tvTotalBeforeTax.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + subtotal));
            tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (subtotal + dCharge)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.checkout);
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

        @RequiresApi(api = Build.VERSION_CODES.M)
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

                    holder.tvApply.setOnClickListener(v -> {
                        try {
                            if (!promoCode.getIs_validate().get(0).isError()) {
                                pCode = promoCode.getPromo_code();
                                btnRemoveOffer.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_green));
                                btnRemoveOffer.setText(activity.getString(R.string.remove_offer));
                                btnRemoveOffer.setTag("applied");
                                isApplied = true;
                                appliedCode = tvPromoCode.getText().toString();
                                dCharge = tvDeliveryCharge.getText().toString().equals(getString(R.string.free)) ? 0.0 : delivery_charge;
                                subtotal = Double.parseDouble(promoCode.getIs_validate().get(0).getDiscounted_amount());
                                System.out.println(">>>>>>>>>>>>>>>>> " + promoCode.getIs_validate().get(0).getDiscounted_amount());
                                pCodeDiscount = Double.parseDouble(promoCode.getIs_validate().get(0).getDiscount());
                                tvSubTotal.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + promoCode.getIs_validate().get(0).getDiscounted_amount()));
                                lytPromoCode.setVisibility(View.VISIBLE);
                                tvPromoCode.setText(promoCode.getPromo_code());
                                tvPromoDiscount.setText("-" + session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + promoCode.getIs_validate().get(0).getDiscount()));
                                dialog.dismiss();
                                SetDataTotal(true);
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

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(false);
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(false);
        activity.invalidateOptionsMenu();
    }

}
