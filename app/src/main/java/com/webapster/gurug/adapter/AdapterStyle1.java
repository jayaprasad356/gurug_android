package com.webapster.gurug.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import com.webapster.gurug.R;
import com.webapster.gurug.fragment.ProductDetailFragment;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.DatabaseHelper;
import com.webapster.gurug.helper.Session;
import com.webapster.gurug.model.PriceVariation;
import com.webapster.gurug.model.Product;

/**
 * Created by shree1 on 3/16/2017.
 */

public class AdapterStyle1 extends RecyclerView.Adapter<AdapterStyle1.HolderItems> {

    public final ArrayList<Product> productList;
    public final Activity activity;
    public final int itemResource;
    Session session;
    boolean isLogin;
    DatabaseHelper databaseHelper;

    public AdapterStyle1(Activity activity, ArrayList<Product> productList, int itemResource) {
        this.activity = activity;
        this.session = new Session(activity);
        this.databaseHelper = new DatabaseHelper(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        this.productList = productList;
        this.itemResource = itemResource;

    }

    @Override
    public int getItemCount() {
        return Math.min(productList.size(), 4);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderItems holder, final int position) {
        final Product product = productList.get(position);
        PriceVariation variant = product.getPriceVariations().get(0);

        String maxCartCont;

        if (product.getTotal_allowed_quantity() == null || product.getTotal_allowed_quantity().equals("") || product.getTotal_allowed_quantity().equals("0")) {
            maxCartCont = session.getData(Constant.max_cart_items_count);
        } else {
            maxCartCont = product.getTotal_allowed_quantity();
        }

        if (variant.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.lytQuantity.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
            holder.lytQuantity.setVisibility(View.VISIBLE);
        }

        Glide.with(activity).load(product.getImage())
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.thumbnail);


        holder.tvTitle.setText(product.getName());

        double price, oPrice;
        String taxPercentage = "0";
        try {
            taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (variant.getDiscounted_price().equals("0") || variant.getDiscounted_price().equals("")) {
            holder.tvDPrice.setVisibility(View.GONE);
            price = ((Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
        } else {
            price = ((Float.parseFloat(variant.getDiscounted_price()) + ((Float.parseFloat(variant.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            oPrice = (Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

            holder.tvDPrice.setPaintFlags(holder.tvDPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvDPrice.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));

            holder.tvDPrice.setVisibility(View.VISIBLE);
        }
        holder.tvPrice.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

        holder.tvTitle.setText(product.getName());
        
        if (isLogin) {
            holder.tvQuantity.setText(variant.getCart_count());
        } else {
            holder.tvQuantity.setText(databaseHelper.CheckCartItemExist(product.getPriceVariations().get(0).getId(), product.getPriceVariations().get(0).getProduct_id()));
        }

        holder.btnAddToCart.setVisibility(holder.tvQuantity.getText().equals("0") ? View.VISIBLE : View.GONE);

        
        holder.relativeLayout.setOnClickListener(view -> {
            AppCompatActivity activity1 = (AppCompatActivity) activity;
            Fragment fragment = new ProductDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.ID, product.getPriceVariations().get(0).getProduct_id());
            bundle.putString(Constant.FROM, "section");
            bundle.putInt(Constant.VARIANT_POSITION, 0);
            fragment.setArguments(bundle);
            activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        holder.imgAdd.setOnClickListener(v -> addQuantity(variant, holder, true, maxCartCont));
        holder.imgMinus.setOnClickListener(v -> addQuantity(variant, holder, false, maxCartCont));
        holder.btnAddToCart.setOnClickListener(v -> addQuantity(variant, holder, true, maxCartCont));
    }

    @NonNull
    @Override
    public HolderItems onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemResource, parent, false);
        return new HolderItems(view);
    }


    @SuppressLint("SetTextI18n")
    public void addQuantity(PriceVariation extra, HolderItems holder, boolean isAdd, String maxCartCont) {
        try {
            if (session.getData(Constant.STATUS).equals("1")) {
                int count = Integer.parseInt(holder.tvQuantity.getText().toString());

                if (isAdd) {
                    count++;
                    if (Float.parseFloat(extra.getStock()) >= count) {
                        if (Float.parseFloat(maxCartCont) >= count) {
                            holder.tvQuantity.setText("" + count);
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
                    holder.tvQuantity.setText("" + count);
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
                    holder.btnAddToCart.setVisibility(View.VISIBLE);
                } else {
                    holder.btnAddToCart.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(activity, activity.getString(R.string.user_block_msg), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class HolderItems extends RecyclerView.ViewHolder {

        public final ImageView thumbnail, imgAdd, imgMinus;
        public final TextView tvTitle, tvPrice, tvQuantity,tvDPrice;
        public final RelativeLayout relativeLayout,lytQuantity;
        public final TextView btnAddToCart;
        public final TextView tvStatus;

        public HolderItems(View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDPrice = itemView.findViewById(R.id.tvDPrice);
            relativeLayout = itemView.findViewById(R.id.play_layout);
            lytQuantity = itemView.findViewById(R.id.lytQuantity);
            imgAdd = itemView.findViewById(R.id.imgAdd);
            imgMinus = itemView.findViewById(R.id.imgMinus);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            tvStatus = itemView.findViewById(R.id.tvStatus);

        }


    }
}