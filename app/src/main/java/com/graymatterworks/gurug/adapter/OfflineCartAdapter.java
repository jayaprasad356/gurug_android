package com.graymatterworks.gurug.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.graymatterworks.gurug.R;
import com.graymatterworks.gurug.fragment.CartFragment;
import com.graymatterworks.gurug.helper.ApiConfig;
import com.graymatterworks.gurug.helper.Constant;
import com.graymatterworks.gurug.helper.DatabaseHelper;
import com.graymatterworks.gurug.helper.Session;
import com.graymatterworks.gurug.model.OfflineCart;
import com.graymatterworks.gurug.model.OfflineItems;
import com.google.android.material.snackbar.Snackbar;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

@SuppressLint("NotifyDataSetChanged")
public class OfflineCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final DatabaseHelper databaseHelper;
    final Session session;


    public OfflineCartAdapter(Activity activity) {
        this.activity = activity;
        databaseHelper = new DatabaseHelper(activity);
        session = new Session(activity);
    }

    public void add(int position, OfflineCart cart) {
        CartFragment.isSoldOut = false;
        CartFragment.isDeliverable = false;
        if (position != 0) {
            CartFragment.offlineCarts.add(position, cart);
        } else {
            CartFragment.offlineCarts.add(cart);
        }
        CartFragment.offlineCartAdapter.notifyDataSetChanged();
    }

    public void removeItem(int position) {
        OfflineCart cart = CartFragment.offlineCarts.get(position);

        totalCalculate(cart, false, false);

        if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
            CartFragment.values.replace(cart.getProduct_variant_id(), "0");
        } else {
            CartFragment.values.put(cart.getProduct_variant_id(), "0");
        }

        CartFragment.offlineCarts.remove(cart);
        CartFragment.isSoldOut = false;
        CartFragment.isDeliverable = false;
        notifyDataSetChanged();
        Constant.TOTAL_CART_ITEM = getItemCount();
        CartFragment.setData(activity);
        activity.invalidateOptionsMenu();

        if (getItemCount() == 0 && CartFragment.offlineSaveForLaterItems.size() == 0) {
            CartFragment.lytEmpty.setVisibility(View.VISIBLE);
            CartFragment.lytTotal.setVisibility(View.GONE);
        } else {
            CartFragment.lytEmpty.setVisibility(View.GONE);
            CartFragment.lytTotal.setVisibility(View.VISIBLE);
        }

        databaseHelper.RemoveFromCart(cart.getProduct_variant_id(), cart.getProduct_id());

        showUndoSnackBar(cart, position);
    }

    @SuppressLint("SetTextI18n")
    public void moveItem(int position) {
        try {
            OfflineCart cart = CartFragment.offlineCarts.get(position);

            CartFragment.offlineCarts.remove(cart);

            CartFragment.isSoldOut = false;
            CartFragment.isDeliverable = false;

            totalCalculate(cart, false, false);

            CartFragment.offlineSaveForLaterAdapter.add(0, cart);

            if (CartFragment.lytSaveForLater.getVisibility() == View.GONE)
                CartFragment.lytSaveForLater.setVisibility(View.VISIBLE);

            CartFragment.tvSaveForLaterTitle.setText(activity.getResources().getString(R.string.save_for_later) + " (" + CartFragment.offlineSaveForLaterItems.size() + ")");

            CartFragment.saveForLaterValues.put(cart.getProduct_variant_id(), databaseHelper.CheckCartItemExist(cart.getProduct_variant_id(), cart.getProduct_id()));

            Constant.TOTAL_CART_ITEM = getItemCount();

            if (getItemCount() == 0)
                CartFragment.lytTotal.setVisibility(View.GONE);

            databaseHelper.MoveToCartOrSaveForLater(cart.getProduct_variant_id(), cart.getProduct_id(), "cart", activity);

            CartFragment.offlineCartAdapter.notifyDataSetChanged();
            CartFragment.offlineSaveForLaterAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void totalCalculate(OfflineCart cart, boolean isAdd, boolean isSingleQty) {
        String taxPercentage1 = "0";
        try {
            taxPercentage1 = (Double.parseDouble(cart.getItem().get(0).getTax_percentage()) > 0 ? cart.getItem().get(0).getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        double price;
        if (cart.getItem().get(0).getDiscounted_price().equals("0") || cart.getItem().get(0).getDiscounted_price().equals("")) {
            price = ((Float.parseFloat(cart.getItem().get(0).getPrice()) + ((Float.parseFloat(cart.getItem().get(0).getPrice()) * Float.parseFloat(taxPercentage1)) / 100)));
        } else {
            price = ((Float.parseFloat(cart.getItem().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItem().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage1)) / 100)));
        }

        if (isAdd) {
            Constant.FLOAT_TOTAL_AMOUNT += isSingleQty ? price : (price * Integer.parseInt(cart.getItem().get(0).getCart_count()));
        } else {
            Constant.FLOAT_TOTAL_AMOUNT -= isSingleQty ? price : (price * Integer.parseInt(cart.getItem().get(0).getCart_count()));
        }

        CartFragment.setData(activity);

    }


    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, final int viewType) {
        View view;
        switch (viewType) {
            case (VIEW_TYPE_ITEM):
                view = LayoutInflater.from(activity).inflate(R.layout.lyt_cartlist, parent, false);
                return new HolderItems(view);
            case (VIEW_TYPE_LOADING):
                view = LayoutInflater.from(activity).inflate(R.layout.item_progressbar, parent, false);
                return new ViewHolderLoading(view);
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holderParent, final int position) {

        if (holderParent instanceof HolderItems) {
            final HolderItems holder = (HolderItems) holderParent;

            final OfflineCart cart = CartFragment.offlineCarts.get(position);

            Glide.with(activity).load(cart.getItem().get(0).getImage())
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProduct);

            holder.tvProductName.setText(cart.getItem().get(0).getName());
            holder.tvMeasurement.setText(cart.getItem().get(0).getMeasurement() + "\u0020" + cart.getItem().get(0).getUnit());
            double price, oPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(cart.getItem().get(0).getTax_percentage()) > 0 ? cart.getItem().get(0).getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cart.getItem().get(0).getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvQuantity.setVisibility(View.GONE);
                CartFragment.isSoldOut = true;
            } else {
                holder.tvStatus.setVisibility(View.GONE);
            }

            if (!Boolean.parseBoolean(cart.getItem().get(0).getIs_item_deliverable()) && session.getData(Constant.SHIPPING_TYPE).equals("local")) {
                holder.txtDeliveryStatus.setVisibility(View.VISIBLE);
                holder.txtDeliveryStatus.setText(activity.getString(R.string.msg_non_deliverable_to) + session.getData(Constant.GET_SELECTED_PINCODE_NAME));
                CartFragment.isDeliverable = true;
            } else {
                holder.txtDeliveryStatus.setVisibility(View.GONE);
            }

            holder.tvPrice.setText(new Session(activity).getData(Constant.CURRENCY) + (cart.getDiscounted_price().equals("0") ? cart.getPrice() : cart.getDiscounted_price()));

            holder.tvDelete.setOnClickListener(v -> removeItem(position));

            holder.tvAction.setOnClickListener(v -> moveItem(position));

            if (cart.getDiscounted_price().equals("0") || cart.getDiscounted_price().equals("")) {
                price = ((Float.parseFloat(cart.getPrice()) + ((Float.parseFloat(cart.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(cart.getDiscounted_price()) + ((Float.parseFloat(cart.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                oPrice = ((Float.parseFloat(cart.getPrice()) + ((Float.parseFloat(cart.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvOriginalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));
            }
            holder.tvPrice.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

            holder.tvProductName.setText(cart.getItem().get(0).getName());
            holder.tvMeasurement.setText(cart.getItem().get(0).getMeasurement() + "\u0020" + cart.getItem().get(0).getUnit());

            holder.tvQuantity.setText(databaseHelper.CheckCartItemExist(CartFragment.offlineCarts.get(position).getId(), cart.getProduct_id()));
            cart.getItem().get(0).setCart_count(databaseHelper.CheckCartItemExist(CartFragment.offlineCarts.get(position).getId(), cart.getProduct_id()));

            holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price * Integer.parseInt(databaseHelper.CheckCartItemExist(CartFragment.offlineCarts.get(position).getId(), cart.getProduct_id()))));

            String maxCartCont;

            if (cart.getItem().get(0).getTotal_allowed_quantity() == null || cart.getItem().get(0).getTotal_allowed_quantity().equals("") || cart.getItem().get(0).getTotal_allowed_quantity().equals("0")) {
                maxCartCont = session.getData(Constant.max_cart_items_count);
            } else {
                maxCartCont = cart.getItem().get(0).getTotal_allowed_quantity();
            }

            holder.btnAddQuantity.setOnClickListener(view -> addQuantity(cart, cart.getItem().get(0), holder, true, maxCartCont, price));

            holder.btnMinusQuantity.setOnClickListener(view -> addQuantity(cart, cart.getItem().get(0), holder, false, maxCartCont, price));


            if (getItemCount() == 0) {
                CartFragment.lytEmpty.setVisibility(View.VISIBLE);
                CartFragment.lytTotal.setVisibility(View.GONE);
            } else {
                CartFragment.lytEmpty.setVisibility(View.GONE);
                CartFragment.lytTotal.setVisibility(View.VISIBLE);
            }

        } else if (holderParent instanceof ViewHolderLoading) {
            ViewHolderLoading loadingViewHolder = (ViewHolderLoading) holderParent;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }

    }


    @SuppressLint("SetTextI18n")
    public void addQuantity(OfflineCart cart, OfflineItems cartItem, HolderItems holder, boolean isAdd, String maxCartCont, double price) {
        try {
            if (session.getData(Constant.STATUS).equals("1")) {
                int count = Integer.parseInt(holder.tvQuantity.getText().toString());

                if (isAdd) {
                    count++;
                    if (Float.parseFloat(cartItem.getStock()) >= count) {
                        if (Float.parseFloat(maxCartCont) >= count) {
                            cartItem.setCart_count("" + count);
                            holder.tvQuantity.setText("" + count);
                            totalCalculate(cart, true, true);
                            holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (price * Integer.parseInt(cartItem.getCart_count()))));
                            if (Constant.CartValues.containsKey(cart.getProduct_variant_id())) {
                                Constant.CartValues.replace(cart.getProduct_variant_id(), "" + count);
                            } else {
                                Constant.CartValues.put(cart.getProduct_variant_id(), "" + count);
                            }
                            ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);

                        } else {
                            Toast.makeText(activity, activity.getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    count--;
                    if (count > 0) {
                        cartItem.setCart_count("" + count);
                        holder.tvQuantity.setText("" + count);
                        totalCalculate(cart, false, true);
                        holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + (price * Integer.parseInt(cartItem.getCart_count()))));
                        if (Constant.CartValues.containsKey(cart.getProduct_variant_id())) {
                            Constant.CartValues.replace(cart.getProduct_variant_id(), "" + count);
                        } else {
                            Constant.CartValues.put(cart.getProduct_variant_id(), "" + count);
                        }
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    }
                }
                CartFragment.setData(activity);
            } else {
                Toast.makeText(activity, activity.getString(R.string.user_block_msg), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return CartFragment.offlineCarts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return CartFragment.offlineCarts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        OfflineCart cart = CartFragment.offlineCarts.get(position);
        if (cart != null)
            return Integer.parseInt(CartFragment.offlineCarts.get(position).getId());
        else
            return position;
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;

        public ViewHolderLoading(View view) {
            super(view);
            progressBar = view.findViewById(R.id.itemProgressbar);
        }
    }

    public static class HolderItems extends RecyclerView.ViewHolder {
        final ImageView imgProduct;
        final ImageView btnMinusQuantity;
        final ImageView btnAddQuantity;
        final TextView tvDelete;
        final TextView tvAction;
        final TextView tvProductName;
        final TextView tvMeasurement;
        final TextView tvPrice;
        final TextView tvOriginalPrice;
        final TextView tvQuantity;
        final TextView tvTotalPrice;
        final TextView tvStatus;
        final TextView txtDeliveryStatus;

        public HolderItems(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvDelete = itemView.findViewById(R.id.tvDelete);
            tvAction = itemView.findViewById(R.id.tvAction);

            btnMinusQuantity = itemView.findViewById(R.id.btnMinusQuantity);
            btnAddQuantity = itemView.findViewById(R.id.btnAddQuantity);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            txtDeliveryStatus = itemView.findViewById(R.id.txtDeliveryStatus);
        }
    }

    void showUndoSnackBar(OfflineCart cart, int position) {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.undo_message), Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, R.color.gray));
        snackbar.setAction(activity.getString(R.string.undo), view -> {
            snackbar.dismiss();
            databaseHelper.AddToCart(cart.getItem().get(0).getId(), cart.getItem().get(0).getProduct_id(), cart.getItem().get(0).getCart_count());

            totalCalculate(cart, true, false);

            add(position, cart);

            CartFragment.isSoldOut = false;
            Constant.TOTAL_CART_ITEM = CartFragment.offlineCarts.size();
            CartFragment.setData(activity);
            notifyDataSetChanged();
            activity.invalidateOptionsMenu();

        }).setActionTextColor(Color.WHITE);

        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}