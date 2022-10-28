package com.gmworks.gurug.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.util.ArrayList;

import com.gmworks.gurug.R;
import com.gmworks.gurug.activity.MainActivity;
import com.gmworks.gurug.fragment.ProductListFragment;
import com.gmworks.gurug.helper.ApiConfig;
import com.gmworks.gurug.helper.Constant;
import com.gmworks.gurug.model.Category;


public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionHolder> {

    public final ArrayList<Category> sectionList;
    public final Activity activity;
    JSONArray jsonArrayImages;
    public final JSONArray jsonArray;

    public SectionAdapter(Activity activity, ArrayList<Category> sectionList, JSONArray jsonArray) {
        this.activity = activity;
        this.sectionList = sectionList;
        this.jsonArray = jsonArray;
        jsonArrayImages = new JSONArray();
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    @Override
    public void onBindViewHolder(SectionHolder holder, final int position) {
        final Category section;
        section = sectionList.get(position);
        holder.tvTitle.setText(section.getName());
        holder.tvSubTitle.setText(section.getSubtitle());

        holder.lytBelowSectionOfferImages.setLayoutManager(new LinearLayoutManager(activity));
        holder.lytBelowSectionOfferImages.setNestedScrollingEnabled(false);

        try {
            jsonArrayImages = jsonArray.getJSONObject(position).getJSONArray(Constant.OFFER_IMAGES);
            ApiConfig.GetOfferImage(activity,jsonArrayImages, holder.lytBelowSectionOfferImages);
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (section.getStyle()) {
            case "style_1":
                holder.recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                AdapterStyle1 adapter = new AdapterStyle1(activity, section.getProductList(), R.layout.offer_layout);
                holder.recyclerView.setAdapter(adapter);
                break;
            case "style_2":
                holder.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                AdapterStyle2 adapterStyle2 = new AdapterStyle2(activity, section.getProductList());
                holder.recyclerView.setAdapter(adapterStyle2);
                break;
            case "style_3":
                holder.recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
                AdapterStyle1 adapter3 = new AdapterStyle1(activity, section.getProductList(), R.layout.lyt_style_3);
                holder.recyclerView.setAdapter(adapter3);
                break;
        }

        holder.tvMore.setOnClickListener(view -> {
            Fragment fragment = new ProductListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.FROM, "section");
            bundle.putString(Constant.NAME, section.getName());
            bundle.putString(Constant.ID, section.getId());
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });
    }

    @NonNull
    @Override
    public SectionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_layout, parent, false);
        return new SectionHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class SectionHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSubTitle;
        final TextView tvMore;
        final RecyclerView recyclerView,lytBelowSectionOfferImages;
        final RelativeLayout relativeLayout;

        public SectionHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubTitle = itemView.findViewById(R.id.tvSubTitle);
            tvMore = itemView.findViewById(R.id.tvMore);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            lytBelowSectionOfferImages = itemView.findViewById(R.id.lytBelowSectionOfferImages);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);

        }
    }


}
