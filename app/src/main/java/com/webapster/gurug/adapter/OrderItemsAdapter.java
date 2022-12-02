package com.webapster.gurug.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.webapster.gurug.R;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.Session;
import com.webapster.gurug.model.OrderItems;
import com.webapster.gurug.model.TrackTimeLine;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.CartItemHolder> {

    final Activity activity;
    final ArrayList<OrderItems> orderTrackerArrayList;
    final Session session;
    public RecyclerView recyclerViewTrackerTimeLine;
    RelativeLayout lytMainTrackerTimeLine;
    LinearLayout lytTrackerTimeLine;

    public OrderItemsAdapter(Activity activity, ArrayList<OrderItems> orderTrackerArrayList, RecyclerView recyclerViewTrackerTimeLine, RelativeLayout lytMainTrackerTimeLine, LinearLayout lytTrackerTimeLine) {
        this.activity = activity;
        this.orderTrackerArrayList = orderTrackerArrayList;
        this.recyclerViewTrackerTimeLine = recyclerViewTrackerTimeLine;
        this.lytMainTrackerTimeLine = lytMainTrackerTimeLine;
        this.lytTrackerTimeLine = lytTrackerTimeLine;
        session = new Session(activity);
    }

    @NonNull
    @Override
    public CartItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_order_items, null);
        return new CartItemHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final CartItemHolder holder, final int position) {

        final OrderItems orderItems = orderTrackerArrayList.get(position);

        holder.tvQuantity.setText(orderItems.getQuantity());

        String taxPercentage = orderItems.getTax_percentage();
        double DiscountedPrice;

        holder.tvActiveStatus.setText(ApiConfig.toTitleCase(orderItems.getActive_status()));

        if (orderItems.getCancelable_status().equals("1")) {
            if (orderItems.getTill_status().equals(Constant.RECEIVED) && orderItems.getActive_status().equals(Constant.RECEIVED)) {
                holder.btnCancel.setVisibility(View.VISIBLE);
            } else if (orderItems.getTill_status().equals(Constant.PROCESSED) && (orderItems.getActive_status().equals(Constant.RECEIVED) || orderItems.getActive_status().equals(Constant.PROCESSED))) {
                holder.btnCancel.setVisibility(View.VISIBLE);
            } else if (orderItems.getTill_status().equals(Constant.SHIPPED) && (orderItems.getActive_status().equals(Constant.RECEIVED) || orderItems.getActive_status().equals(Constant.PROCESSED) || orderItems.getActive_status().equals(Constant.SHIPPED))) {
                holder.btnCancel.setVisibility(View.VISIBLE);
            } else {
                holder.btnCancel.setVisibility(View.GONE);
            }
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }

        if (orderItems.getReturn_status().equals("1")) {
            if (orderItems.getActive_status().equals(Constant.DELIVERED)) {
                holder.btnReturn.setVisibility(View.VISIBLE);
            }
        } else {
            holder.btnReturn.setVisibility(View.GONE);
        }

        if (orderItems.getDiscounted_price().equals("0") || orderItems.getDiscounted_price().equals("")) {
            DiscountedPrice = (((Float.parseFloat(orderItems.getPrice()) + ((Float.parseFloat(orderItems.getPrice()) * Float.parseFloat(taxPercentage)) / 100))) * Integer.parseInt(orderItems.getQuantity()));
        } else {
            DiscountedPrice = (((Float.parseFloat(orderItems.getDiscounted_price()) + ((Float.parseFloat(orderItems.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100))) * Integer.parseInt(orderItems.getQuantity()));
        }
        holder.tvPrice.setText(session.getData(Constant.CURRENCY) + ApiConfig.StringFormat("" + DiscountedPrice));

        holder.tvName.setText(orderItems.getName() + "(" + orderItems.getMeasurement() + orderItems.getUnit() + ")");

        Glide.with(activity).load(orderItems.getImage())
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgOrder);


        holder.btnCancel.setOnClickListener(view -> updateOrderStatus(activity, orderItems, Constant.CANCELLED, holder));

        holder.btnReturn.setOnClickListener(view -> {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            Date date = new Date();
            String inputString1 = orderItems.getDate_added();
            String inputString2 = myFormat.format(date);
            try {
                Date date1 = myFormat.parse(inputString1);
                Date date2 = myFormat.parse(inputString2);
                long diff = Objects.requireNonNull(date2).getTime() - Objects.requireNonNull(date1).getTime();
                if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) <= Integer.parseInt(orderItems.getReturn_days())) {
                    updateOrderStatus(activity, orderItems, Constant.RETURNED, holder);
                } else {
                    final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity.getResources().getString(R.string.product_return) + Integer.parseInt(new Session(activity).getData(Constant.max_product_return_days)) + activity.getString(R.string.day_max_limit), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(activity.getResources().getString(R.string.ok), view1 -> snackbar.dismiss());
                    snackbar.setActionTextColor(Color.RED);
                    View snackBarView = snackbar.getView();
                    TextView textView = snackBarView.findViewById(R.id.snackbar_text);
                    textView.setMaxLines(5);
                    snackbar.show();

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        holder.tvTrackItem.setOnClickListener(view -> {
            if (!orderItems.getShipping_method().equals("local") && orderItems.getShipment_id().equals("0")) {
                Toast.makeText(activity, orderItems.getActive_status(), Toast.LENGTH_SHORT).show();
            } else {
                getTrackerData(orderItems);
            }
        });

    }

    private void getTrackerData(OrderItems orderItems) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.TRACK_ORDER, Constant.GetVal);
        if (orderItems.getShipping_method().equals("local")) {
            params.put(Constant.ORDER_ITEM_ID, orderItems.getId());
        } else {
            params.put(Constant.SHIPMENT_ID, orderItems.getShipment_id());
        }
        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    showDialog(activity, jsonObject.getJSONArray("activities"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.ORDER_PROCESS_URL, params, true);
    }

    public void showDialog(Activity activity, JSONArray jsonArray) {
        try {
            lytMainTrackerTimeLine.setVisibility(View.VISIBLE);
            lytTrackerTimeLine.setVisibility(View.VISIBLE);
            lytTrackerTimeLine.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.view_show));

            ArrayList<TrackTimeLine> trackTimeLines = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                TrackTimeLine trackTimeLine = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), TrackTimeLine.class);
                trackTimeLines.add(trackTimeLine);
            }

            OrderTimeLineAdapter orderTimeLineAdapter = new OrderTimeLineAdapter(activity, trackTimeLines);
            recyclerViewTrackerTimeLine.setAdapter(orderTimeLineAdapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateOrderStatus(final Activity activity, final OrderItems orderItems, final String status, final CartItemHolder holder) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        if (status.equals(Constant.CANCELLED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.cancel_item));
            alertDialog.setMessage(activity.getResources().getString(R.string.cancel_msg));
        } else if (status.equals(Constant.RETURNED)) {
            alertDialog.setTitle(activity.getResources().getString(R.string.return_order));
            alertDialog.setMessage(activity.getResources().getString(R.string.return_msg));
        }
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(activity.getResources().getString(R.string.yes), (dialog, which) -> {

            Map<String, String> params = new HashMap<>();
            params.put(Constant.UPDATE_ORDER_STATUS, Constant.GetVal);
            params.put(Constant.ORDER_ID, orderItems.getId());
            params.put(Constant.ORDER_ITEM_ID, orderItems.getId());
            params.put(Constant.STATUS, status);
            ApiConfig.RequestToVolley((result, response) -> {
                // System.out.println("================= " + response);
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            if (status.equals(Constant.CANCELLED)) {
                                holder.btnCancel.setVisibility(View.GONE);
                                orderItems.setActive_status(status);
                                orderTrackerArrayList.size();
                                ApiConfig.getWalletBalance(activity, new Session(activity));
                            } else {
                                holder.btnReturn.setVisibility(View.GONE);
                            }
                            Constant.isOrderCancelled = true;
                        }
                        Toast.makeText(activity, object.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                }
            }, activity, Constant.ORDER_PROCESS_URL, params, true);

        });
        alertDialog.setNegativeButton(activity.getResources().getString(R.string.no), (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return orderTrackerArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class CartItemHolder extends RecyclerView.ViewHolder {
        final TextView tvQuantity;
        final TextView tvPrice;
        final TextView tvActiveStatus;
        final TextView tvName;
        final TextView tvTrackItem;
        final ImageView imgOrder;
        final CardView cardViewTrack;
        final Button btnCancel;
        final Button btnReturn;

        public CartItemHolder(View itemView) {
            super(itemView);

            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvActiveStatus = itemView.findViewById(R.id.tvActiveStatus);
            tvTrackItem = itemView.findViewById(R.id.tvTrackItem);
            tvName = itemView.findViewById(R.id.tvName);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            imgOrder = itemView.findViewById(R.id.imgOrder);
            cardViewTrack = itemView.findViewById(R.id.cardViewTrack);
            btnReturn = itemView.findViewById(R.id.btnReturn);
        }
    }

}
