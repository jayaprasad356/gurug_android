package com.webapster.gurug.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.webapster.gurug.R;
import com.webapster.gurug.fragment.CartFragment;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.Session;
import com.webapster.gurug.model.Cart;
import com.webapster.gurug.model.CartItems;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

@SuppressLint("NotifyDataSetChanged")
public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // for load more
    public final int VIEW_TYPE_ITEM = 0;
    public final int VIEW_TYPE_LOADING = 1;
    final Activity activity;
    final Session session;


    public CartAdapter(Activity activity) {
        this.activity = activity;
        session = new Session(activity);
    }


    public void add(int position, Cart item) {
        CartFragment.isSoldOut = false;
        CartFragment.isDeliverable = false;
        CartFragment.carts.add(position, item);
        CartFragment.cartAdapter.notifyDataSetChanged();
    }

    public void removeItem(int position) {
        Cart cart = CartFragment.carts.get(position);

        totalCalculate(cart, false, false);

        if (CartFragment.values.containsKey(cart.getProduct_variant_id())) {
            CartFragment.values.replace(cart.getProduct_variant_id(), "0");
        } else {
            CartFragment.values.put(cart.getProduct_variant_id(), "0");
        }


        CartFragment.carts.remove(cart);
        CartFragment.isSoldOut = false;
        CartFragment.isDeliverable = false;
        notifyDataSetChanged();
        Constant.TOTAL_CART_ITEM = getItemCount();
        CartFragment.setData(activity);
        activity.invalidateOptionsMenu();

        if (getItemCount() == 0 && CartFragment.saveForLater.size() == 0) {
            CartFragment.lytEmpty.setVisibility(View.VISIBLE);
            CartFragment.lytTotal.setVisibility(View.GONE);
        } else {
            CartFragment.lytEmpty.setVisibility(View.GONE);
            CartFragment.lytTotal.setVisibility(View.VISIBLE);
        }

        showUndoSnackBar(cart, position);
    }

    public void totalCalculate(Cart cart, boolean isAdd, boolean isSingleQty) {
        String taxPercentage1 = "0";
        try {
            taxPercentage1 = (Double.parseDouble(cart.getItems().get(0).getTax_percentage()) > 0 ? cart.getItems().get(0).getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        double price;
        if (cart.getItems().get(0).getDiscounted_price().equals("0") || cart.getItems().get(0).getDiscounted_price().equals("")) {
            price = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage1)) / 100)));
        } else {
            price = ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) + ((Float.parseFloat(cart.getItems().get(0).getDiscounted_price()) * Float.parseFloat(taxPercentage1)) / 100)));
        }

        if (isAdd) {
            Constant.FLOAT_TOTAL_AMOUNT += isSingleQty ? price : (price * Integer.parseInt(cart.getQty()));
        } else {
            Constant.FLOAT_TOTAL_AMOUNT -= isSingleQty ? price : (price * Integer.parseInt(cart.getQty()));
        }

        CartFragment.setData(activity);

    }

    @SuppressLint("SetTextI18n")
    public void moveItem(int position) {
        try {
            Cart cart = CartFragment.carts.get(position);
            totalCalculate(cart, false, false);

            CartFragment.isSoldOut = false;
            CartFragment.isDeliverable = false;


            CartFragment.carts.remove(cart);
            CartFragment.cartAdapter.notifyDataSetChanged();

            CartFragment.saveForLater.add(cart);
            CartFragment.saveForLaterAdapter.notifyDataSetChanged();

            if (CartFragment.lytSaveForLater.getVisibility() == View.GONE)
                CartFragment.lytSaveForLater.setVisibility(View.VISIBLE);

            CartFragment.tvSaveForLaterTitle.setText(activity.getResources().getString(R.string.save_for_later) + " (" + CartFragment.saveForLater.size() + ")");

            CartFragment.saveForLaterValues.put(cart.getProduct_variant_id(), cart.getQty());

            Constant.TOTAL_CART_ITEM = getItemCount();
            CartFragment.setData(activity);

            if (getItemCount() == 0)
                CartFragment.lytTotal.setVisibility(View.GONE);

            ApiConfig.AddMultipleProductInSaveForLater(session, activity, CartFragment.saveForLaterValues);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
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
        try {
            if (holderParent instanceof HolderItems) {
                final HolderItems holder = (HolderItems) holderParent;
                final Cart cart = CartFragment.carts.get(position);

                double price;
                double oPrice;
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
                    oPrice = ((Float.parseFloat(cart.getItems().get(0).getPrice()) + ((Float.parseFloat(cart.getItems().get(0).getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
                    holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.tvOriginalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));
                }

                Glide.with(activity).load(cart.getItems().get(0).getImage())
                        .centerInside()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgProduct);

                holder.tvProductName.setText(cart.getItems().get(0).getName());

                holder.imgDelete.setOnClickListener(v -> removeItem(position));

                holder.tvAction.setOnClickListener(v -> moveItem(position));

                holder.tvMeasurement.setText(cart.getItems().get(0).getMeasurement() + "\u0020" + cart.getItems().get(0).getUnit());

                if (cart.getItems().get(0).getServe_for().equals(Constant.SOLD_OUT_TEXT)) {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.lytQuantity.setVisibility(View.GONE);
                    CartFragment.isSoldOut = true;
                } else if (Float.parseFloat(cart.getQty()) > Float.parseFloat(cart.getItems().get(0).getStock())) {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText(activity.getString(R.string.low_stock_warning1) + cart.getItems().get(0).getStock() + activity.getString(R.string.low_stock_warning2));
                    CartFragment.isSoldOut = true;
                }

                if (Boolean.parseBoolean(cart.getItems().get(0).getIs_item_deliverable())) {
                    holder.txtDeliveryStatus.setVisibility(View.GONE);
                } else {
                    holder.txtDeliveryStatus.setVisibility(View.VISIBLE);
                    holder.txtDeliveryStatus.setText(activity.getString(R.string.msg_non_deliverable_to) + session.getData(Constant.GET_SELECTED_PINCODE_NAME));
                    CartFragment.isDeliverable = true;
                }

                holder.tvPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

                holder.tvQuantity.setText(cart.getQty());

                holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price * Integer.parseInt(cart.getQty())));

                String maxCartCont;

                if (cart.getItems().get(0).getTotal_allowed_quantity() == null || cart.getItems().get(0).getTotal_allowed_quantity().equals("") || cart.getItems().get(0).getTotal_allowed_quantity().equals("0")) {
                    maxCartCont = session.getData(Constant.max_cart_items_count);
                } else {
                    maxCartCont = cart.getItems().get(0).getTotal_allowed_quantity();
                }


                holder.btnAddQuantity.setOnClickListener(view -> addQuantity(cart, cart.getItems().get(0), holder, true, maxCartCont, price));

                holder.btnMinusQuantity.setOnClickListener(view -> addQuantity(cart, cart.getItems().get(0), holder, false, maxCartCont, price));

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    public void addQuantity(Cart cart, CartItems cartItem, HolderItems holder, boolean isAdd, String maxCartCont, double price) {
        try {
            if (session.getData(Constant.STATUS).equals("1")) {
                int count = Integer.parseInt(holder.tvQuantity.getText().toString());

                if (isAdd) {
                    count++;
                    if (Float.parseFloat(cartItem.getStock()) >= count) {
                        if (Float.parseFloat(maxCartCont) >= count) {
                            cart.setQty("" + count);
                            holder.tvQuantity.setText("" + count);
                            totalCalculate(cart, true, true);
                            holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY)+ApiConfig.StringFormat(""+(price * Integer.parseInt(cart.getQty()))));
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
                        cart.setQty("" + count);
                        holder.tvQuantity.setText("" + count);
                        totalCalculate(cart, false, true);
                        holder.tvTotalPrice.setText(session.getData(Constant.CURRENCY)+ApiConfig.StringFormat("" + (price * Integer.parseInt(cart.getQty()))));
                        if (Constant.CartValues.containsKey(cart.getProduct_variant_id())) {
                            Constant.CartValues.replace(cart.getProduct_variant_id(), "" + count);
                        } else {
                            Constant.CartValues.put(cart.getProduct_variant_id(), "" + count);
                        }
                        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                    }
                }
            } else {
                Toast.makeText(activity, activity.getString(R.string.user_block_msg), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return CartFragment.carts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return CartFragment.carts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
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
        final ImageView imgDelete;
        final TextView tvAction;
        final TextView tvProductName;
        final TextView tvMeasurement;
        final TextView tvPrice;
        final TextView tvOriginalPrice;
        final TextView tvQuantity;
        final TextView tvTotalPrice;
        final TextView tvStatus;
        final TextView txtDeliveryStatus;
        LinearLayout lytQuantity;

        public HolderItems(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            tvAction = itemView.findViewById(R.id.tvAction);
            btnMinusQuantity = itemView.findViewById(R.id.btnMinusQuantity);
            btnAddQuantity = itemView.findViewById(R.id.btnAddQuantity);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            txtDeliveryStatus = itemView.findViewById(R.id.txtDeliveryStatus);
            lytQuantity = itemView.findViewById(R.id.lytQuantity);
        }
    }

    void showUndoSnackBar(Cart cart, int position) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.undo_message), Snackbar.LENGTH_LONG);
        snackbar.setAction(activity.getString(R.string.undo), view -> {
            snackbar.dismiss();

            CartFragment.values.put(cart.getProduct_variant_id(), cart.getQty());

            add(position, cart);
            notifyDataSetChanged();
            CartFragment.isSoldOut = false;
            Constant.TOTAL_CART_ITEM = getItemCount();
            ApiConfig.AddMultipleProductInCart(session, activity, CartFragment.values);
            activity.invalidateOptionsMenu();
            totalCalculate(cart, true, false);
            CartFragment.setData(activity);
        });
        snackbar.setActionTextColor(Color.WHITE);
        View snackBarView = snackbar.getView();
        TextView textView = snackBarView.findViewById(R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}