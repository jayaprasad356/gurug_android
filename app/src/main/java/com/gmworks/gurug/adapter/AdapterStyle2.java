package com.gmworks.gurug.adapter;

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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import com.gmworks.gurug.R;
import com.gmworks.gurug.fragment.ProductDetailFragment;
import com.gmworks.gurug.helper.ApiConfig;
import com.gmworks.gurug.helper.Constant;
import com.gmworks.gurug.helper.DatabaseHelper;
import com.gmworks.gurug.helper.Session;
import com.gmworks.gurug.model.PriceVariation;
import com.gmworks.gurug.model.Product;


/**
 * Created by shree1 on 3/16/2017.
 */

public class AdapterStyle2 extends RecyclerView.Adapter<AdapterStyle2.HolderItems> {

    public final ArrayList<Product> productList;
    public final Activity activity;
    Session session;
    boolean isLogin;
    DatabaseHelper databaseHelper;

    public AdapterStyle2(Activity activity, ArrayList<Product> productList) {
        this.activity = activity;
        this.session = new Session(activity);
        this.databaseHelper = new DatabaseHelper(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        this.productList = productList;
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderItems holder, final int position) {

        if (productList.size() > 0) {
            Product product = productList.get(0);
            PriceVariation variant = product.getPriceVariations().get(0);
            String maxCartCont;

            if (product.getTotal_allowed_quantity() == null || product.getTotal_allowed_quantity().equals("") || product.getTotal_allowed_quantity().equals("0")) {
                maxCartCont = session.getData(Constant.max_cart_items_count);
            } else {
                maxCartCont = product.getTotal_allowed_quantity();
            }

            double price, oPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(productList.get(0).getTax_percentage()) > 0 ? productList.get(0).getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (variant.getDiscounted_price().equals("0") || variant.getDiscounted_price().equals("")) {
                holder.tvSubStyle2_1_.setVisibility(View.GONE);
                price = ((Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(variant.getDiscounted_price()) + ((Float.parseFloat(variant.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                oPrice = (Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                holder.tvSubStyle2_1_.setPaintFlags(holder.tvSubStyle2_1_.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvSubStyle2_1_.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));

                holder.tvSubStyle2_1_.setVisibility(View.VISIBLE);
            }

            holder.tvSubStyle2_1.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

            holder.tvStyle2_1.setText(product.getName());

            Glide.with(activity).load(product.getImage())
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgStyle2_1);

            holder.layoutStyle2_1.setOnClickListener(view -> {
                AppCompatActivity activity1 = (AppCompatActivity) activity;
                Fragment fragment = new ProductDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "section");
                bundle.putInt(Constant.VARIANT_POSITION, 0);
                bundle.putString(Constant.ID, product.getId());
                fragment.setArguments(bundle);
                activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            if (isLogin) {
                holder.tvQuantity2_1.setText(variant.getCart_count());
            } else {
                holder.tvQuantity2_1.setText(databaseHelper.CheckCartItemExist(variant.getId(), variant.getProduct_id()));
            }

            holder.btnAddToCart2_1.setVisibility(holder.tvQuantity2_1.getText().equals("0") ? View.VISIBLE : View.GONE);


            holder.imgAdd2_1.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_1, holder.btnAddToCart2_1, true, maxCartCont));
            holder.imgMinus2_1.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_1, holder.btnAddToCart2_1, false, maxCartCont));
            holder.btnAddToCart2_1.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_1, holder.btnAddToCart2_1, true, maxCartCont));

        }

        if (productList.size() > 1) {
            Product product = productList.get(1);
            PriceVariation variant = product.getPriceVariations().get(0);
            String maxCartCont;

            if (product.getTotal_allowed_quantity() == null || product.getTotal_allowed_quantity().equals("") || product.getTotal_allowed_quantity().equals("0")) {
                maxCartCont = session.getData(Constant.max_cart_items_count);
            } else {
                maxCartCont = product.getTotal_allowed_quantity();
            }

            double price, oPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(productList.get(1).getTax_percentage()) > 0 ? productList.get(1).getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (variant.getDiscounted_price().equals("0") || variant.getDiscounted_price().equals("")) {
                holder.tvSubStyle2_2_.setVisibility(View.GONE);
                price = ((Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(variant.getDiscounted_price()) + ((Float.parseFloat(variant.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                oPrice = (Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                holder.tvSubStyle2_2_.setPaintFlags(holder.tvSubStyle2_2_.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvSubStyle2_2_.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));

                holder.tvSubStyle2_2_.setVisibility(View.VISIBLE);
            }

            holder.tvStyle2_2.setText(product.getName());

            holder.tvSubStyle2_2.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

            Glide.with(activity).load(product.getImage())
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgStyle2_2);

            holder.layoutStyle2_2.setOnClickListener(view -> {
                AppCompatActivity activity1 = (AppCompatActivity) activity;
                Fragment fragment = new ProductDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "section");
                bundle.putInt(Constant.VARIANT_POSITION, 0);
                bundle.putString(Constant.ID, product.getId());
                fragment.setArguments(bundle);
                activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });


            if (isLogin) {
                holder.tvQuantity2_2.setText(variant.getCart_count());
            } else {
                holder.tvQuantity2_2.setText(databaseHelper.CheckCartItemExist(variant.getId(), variant.getProduct_id()));
            }

            holder.btnAddToCart2_2.setVisibility(holder.tvQuantity2_2.getText().equals("0") ? View.VISIBLE : View.GONE);

            holder.imgAdd2_2.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_2, holder.btnAddToCart2_2, true, maxCartCont));
            holder.imgMinus2_2.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_2, holder.btnAddToCart2_2, false, maxCartCont));
            holder.btnAddToCart2_2.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_2, holder.btnAddToCart2_2, true, maxCartCont));
        }

        if (productList.size() > 2) {

            Product product = productList.get(2);
            PriceVariation variant = product.getPriceVariations().get(0);
            String maxCartCont;

            if (product.getTotal_allowed_quantity() == null || product.getTotal_allowed_quantity().equals("") || product.getTotal_allowed_quantity().equals("0")) {
                maxCartCont = session.getData(Constant.max_cart_items_count);
            } else {
                maxCartCont = product.getTotal_allowed_quantity();
            }
            
            holder.tvStyle2_3.setText(product.getName());

            double price, oPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(productList.get(2).getTax_percentage()) > 0 ? productList.get(2).getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (variant.getDiscounted_price().equals("0") || variant.getDiscounted_price().equals("")) {
                holder.tvSubStyle2_3_.setVisibility(View.GONE);
                price = ((Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100)));
            } else {
                price = ((Float.parseFloat(variant.getDiscounted_price()) + ((Float.parseFloat(variant.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                oPrice = (Float.parseFloat(variant.getPrice()) + ((Float.parseFloat(variant.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                holder.tvSubStyle2_3_.setPaintFlags(holder.tvSubStyle2_3_.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.tvSubStyle2_3_.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + oPrice));

                holder.tvSubStyle2_3_.setVisibility(View.VISIBLE);
            }

            holder.tvSubStyle2_3.setText(new Session(activity).getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + price));

            Glide.with(activity).load(product.getImage())
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgStyle2_3);

            holder.layoutStyle2_3.setOnClickListener(view -> {
                AppCompatActivity activity1 = (AppCompatActivity) activity;
                Fragment fragment = new ProductDetailFragment();
                final Bundle bundle = new Bundle();
                bundle.putString(Constant.FROM, "section");
                bundle.putInt(Constant.VARIANT_POSITION, 0);
                bundle.putString(Constant.ID, product.getId());
                fragment.setArguments(bundle);
                activity1.getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            });

            if (isLogin) {
                holder.tvQuantity2_3.setText(variant.getCart_count());
            } else {
                holder.tvQuantity2_3.setText(databaseHelper.CheckCartItemExist(variant.getId(), variant.getProduct_id()));
            }

            holder.btnAddToCart2_3.setVisibility(holder.tvQuantity2_3.getText().equals("0") ? View.VISIBLE : View.GONE);

            holder.imgAdd2_3.setOnClickListener(v -> addQuantity(variant, holder.tvQuantity2_3, holder.btnAddToCart2_3, true, maxCartCont));
            holder.imgMinus2_3.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_3, holder.btnAddToCart2_3, false, maxCartCont));
            holder.btnAddToCart2_3.setOnClickListener(v -> addQuantity(variant,  holder.tvQuantity2_3, holder.btnAddToCart2_3, true, maxCartCont));
        }
    }


    @SuppressLint("SetTextI18n")
    public void addQuantity(PriceVariation extra, TextView tvQuantity, TextView btnAddToCart, boolean isAdd, String maxCartCont) {
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

    @NonNull
    @Override
    public HolderItems onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lyt_style_2, parent, false);
        return new HolderItems(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public static class HolderItems extends RecyclerView.ViewHolder {

        public final ImageView imgStyle2_1;
        public final ImageView imgStyle2_2;
        public final ImageView imgStyle2_3;

        public final TextView tvStyle2_1;
        public final TextView tvStyle2_2;
        public final TextView tvStyle2_3;

        public final TextView tvSubStyle2_1;
        public final TextView tvSubStyle2_1_;
        public final TextView tvSubStyle2_2;
        public final TextView tvSubStyle2_2_;
        public final TextView tvSubStyle2_3;
        public final TextView tvSubStyle2_3_;

        public final CardView layoutStyle2_1;
        public final CardView layoutStyle2_2;
        public final CardView layoutStyle2_3;

        public final TextView tvStatus2_1;
        public final TextView tvStatus2_2;
        public final TextView tvStatus2_3;

        public final RelativeLayout lytQuantity2_1;
        public final RelativeLayout lytQuantity2_2;
        public final RelativeLayout lytQuantity2_3;

        public final ImageView imgMinus2_1;
        public final ImageView imgMinus2_2;
        public final ImageView imgMinus2_3;

        public final TextView tvQuantity2_1;
        public final TextView tvQuantity2_2;
        public final TextView tvQuantity2_3;

        public final ImageView imgAdd2_1;
        public final ImageView imgAdd2_2;
        public final ImageView imgAdd2_3;

        public final TextView btnAddToCart2_1;
        public final TextView btnAddToCart2_2;
        public final TextView btnAddToCart2_3;


        public HolderItems(View itemView) {
            super(itemView);
            imgStyle2_1 = itemView.findViewById(R.id.imgStyle2_1);
            imgStyle2_2 = itemView.findViewById(R.id.imgStyle2_2);
            imgStyle2_3 = itemView.findViewById(R.id.imgStyle2_3);

            tvStyle2_1 = itemView.findViewById(R.id.tvStyle2_1);
            tvStyle2_2 = itemView.findViewById(R.id.tvStyle2_2);
            tvStyle2_3 = itemView.findViewById(R.id.tvStyle2_3);

            tvSubStyle2_1 = itemView.findViewById(R.id.tvSubStyle2_1);
            tvSubStyle2_1_ = itemView.findViewById(R.id.tvSubStyle2_1_);
            tvSubStyle2_2 = itemView.findViewById(R.id.tvSubStyle2_2);
            tvSubStyle2_2_ = itemView.findViewById(R.id.tvSubStyle2_2_);
            tvSubStyle2_3 = itemView.findViewById(R.id.tvSubStyle2_3);
            tvSubStyle2_3_ = itemView.findViewById(R.id.tvSubStyle2_3_);

            layoutStyle2_1 = itemView.findViewById(R.id.layoutStyle2_1);
            layoutStyle2_2 = itemView.findViewById(R.id.layoutStyle2_2);
            layoutStyle2_3 = itemView.findViewById(R.id.layoutStyle2_3);

            tvStatus2_1 = itemView.findViewById(R.id.tvStatus2_1);
            tvStatus2_2 = itemView.findViewById(R.id.tvStatus2_2);
            tvStatus2_3 = itemView.findViewById(R.id.tvStatus2_3);

            lytQuantity2_1 = itemView.findViewById(R.id.lytQuantity2_1);
            lytQuantity2_2 = itemView.findViewById(R.id.lytQuantity2_2);
            lytQuantity2_3 = itemView.findViewById(R.id.lytQuantity2_3);

            imgMinus2_1 = itemView.findViewById(R.id.imgMinus2_1);
            imgMinus2_2 = itemView.findViewById(R.id.imgMinus2_2);
            imgMinus2_3 = itemView.findViewById(R.id.imgMinus2_3);

            tvQuantity2_1 = itemView.findViewById(R.id.tvQuantity2_1);
            tvQuantity2_2 = itemView.findViewById(R.id.tvQuantity2_2);
            tvQuantity2_3 = itemView.findViewById(R.id.tvQuantity2_3);

            imgAdd2_1 = itemView.findViewById(R.id.imgAdd2_1);
            imgAdd2_2 = itemView.findViewById(R.id.imgAdd2_2);
            imgAdd2_3 = itemView.findViewById(R.id.imgAdd2_3);

            btnAddToCart2_1 = itemView.findViewById(R.id.btnAddToCart2_1);
            btnAddToCart2_2 = itemView.findViewById(R.id.btnAddToCart2_2);
            btnAddToCart2_3 = itemView.findViewById(R.id.btnAddToCart2_3);

        }


    }
}