package com.graymatterworks.gurug.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.graymatterworks.gurug.R;
import com.graymatterworks.gurug.helper.Session;
import com.graymatterworks.gurug.model.TrackTimeLine;

public class OrderTimeLineAdapter extends RecyclerView.Adapter<OrderTimeLineAdapter.CartItemHolder> {

    final Activity activity;
    final ArrayList<TrackTimeLine> trackTimeLines;
    final Session session;

    public OrderTimeLineAdapter(Activity activity, ArrayList<TrackTimeLine> trackTimeLines) {
        this.activity = activity;
        this.trackTimeLines = trackTimeLines;
        session = new Session(activity);
    }

    @NonNull
    @Override
    public CartItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_order_time_line, null);
        return new CartItemHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final CartItemHolder holder, final int position) {

        final TrackTimeLine trackTimeLine = trackTimeLines.get(position);
        String from = "";
        if (!trackTimeLine.getLocation().equals("") && trackTimeLine.getLocation() != null) {
            from = (activity.getString(R.string.from) + " <b> " + trackTimeLine.getLocation() + " </b>");
        }
        holder.tvTrackerDetail.setText(Html.fromHtml(activity.getString(R.string.order) + " <b> " + trackTimeLine.getActivity() + " </b> " + activity.getString(R.string.on) + " <b> " + trackTimeLine.getDate() + "</br>" + from, 0));

        if ((position + 1) != getItemCount()) {
            holder.lytTimeLine.setVisibility(View.VISIBLE);
        } else {
            holder.lytTimeLine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return trackTimeLines.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(trackTimeLines.get(position).getDate());
    }

    public static class CartItemHolder extends RecyclerView.ViewHolder {
        final TextView tvTrackerDetail;
        final View viewTimeLine;
        final RelativeLayout lytTimeLine;

        public CartItemHolder(View itemView) {
            super(itemView);

            tvTrackerDetail = itemView.findViewById(R.id.tvTrackerDetail);
            viewTimeLine = itemView.findViewById(R.id.viewTimeLine);
            lytTimeLine = itemView.findViewById(R.id.lytTimeLine);

        }
    }

}
