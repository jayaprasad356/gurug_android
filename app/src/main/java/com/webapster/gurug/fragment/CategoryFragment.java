package com.webapster.gurug.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.webapster.gurug.R;
import com.webapster.gurug.adapter.CategoryAdapter;
import com.webapster.gurug.helper.ApiConfig;
import com.webapster.gurug.helper.Constant;
import com.webapster.gurug.helper.Session;
import com.webapster.gurug.model.Category;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class CategoryFragment extends Fragment {

    public static ArrayList<Category> categoryArrayList;
    TextView tvAlert;
    RecyclerView categoryRecycleView;
    SwipeRefreshLayout swipeLayout;
    View root;
    Activity activity;
    private ShimmerFrameLayout shimmerFrameLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_category, container, false);
        shimmerFrameLayout = root.findViewById(R.id.shimmerFrameLayout);

        activity = getActivity();

        setHasOptionsMenu(true);


        tvAlert = root.findViewById(R.id.tvAlert);
        swipeLayout = root.findViewById(R.id.swipeLayout);
        categoryRecycleView = root.findViewById(R.id.categoryRecycleView);

        categoryRecycleView.setLayoutManager(new GridLayoutManager(activity, Constant.GRID_COLUMN));
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(activity,R.color.colorPrimary));

        swipeLayout.setOnRefreshListener(() -> {
            swipeLayout.setRefreshing(false);
            if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
                ApiConfig.getWalletBalance(activity, new Session(activity));
            }
            GetCategory();
        });

            if (new Session(activity).getBoolean(Constant.IS_USER_LOGIN)) {
                ApiConfig.getWalletBalance(activity, new Session(activity));
            }
            GetCategory();


        return root;
    }

    void GetCategory() {
        categoryArrayList = new ArrayList<>();
        categoryRecycleView.setVisibility(View.GONE);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        Map<String, String> params = new HashMap<>();
        ApiConfig.RequestToVolley((result, response) -> {
            //System.out.println("======cate " + response);
            Log.d("CATEGORY_LIST",response);
            if (result) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        Gson gson = new Gson();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Category category = gson.fromJson(jsonObject.toString(), Category.class);
                            categoryArrayList.add(category);
                        }
                        categoryRecycleView.setAdapter(new CategoryAdapter(activity, categoryArrayList, R.layout.lyt_subcategory, "category", 0));
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        categoryRecycleView.setVisibility(View.VISIBLE);
                    } else {
                        tvAlert.setVisibility(View.VISIBLE);
                        shimmerFrameLayout.stopShimmer();
                        shimmerFrameLayout.setVisibility(View.GONE);
                        categoryRecycleView.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                        e.printStackTrace();
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    categoryRecycleView.setVisibility(View.GONE);
                }
            }
        }, activity, Constant.CATEGORY_URL, params, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.title_category);
        requireActivity().invalidateOptionsMenu();
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
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
    }
}