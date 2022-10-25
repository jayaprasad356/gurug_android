package com.graymatterworks.gurug.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.graymatterworks.gurug.R;
import com.graymatterworks.gurug.activity.MainActivity;
import com.graymatterworks.gurug.fragment.AddressAddUpdateFragment;
import com.graymatterworks.gurug.fragment.AddressListFragment;
import com.graymatterworks.gurug.helper.ApiConfig;
import com.graymatterworks.gurug.helper.Constant;
import com.graymatterworks.gurug.helper.Session;
import com.graymatterworks.gurug.model.Address;

public class AddressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final Activity activity;
    final ArrayList<Address> addresses;
    String id = "0";
    Session session;
    int layout;

    public AddressAdapter(Activity activity, ArrayList<Address> addresses, int layout) {
        this.activity = activity;
        this.session = new Session(activity);
        this.addresses = addresses;
        this.layout = layout;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(activity).inflate(layout, parent, false);
        return new AddressItemHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holderParent, final int position) {
        final AddressItemHolder holder = (AddressItemHolder) holderParent;
        final Address address = addresses.get(position);
        id = address.getId();

        holder.setIsRecyclable(false);

        if (Constant.selectedAddressId.equals(id) || layout == R.layout.lyt_address_checkout) {

            Constant.selectedAddressId = address.getId();

            holder.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));

            holder.tvAddressType.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.right_btn_bg, null));
            holder.tvDefaultAddress.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.right_btn_bg, null));

            holder.imgSelect.setImageResource(R.drawable.ic_check_circle);
            holder.lytMain.setBackgroundResource(R.drawable.selected_shadow);

            Constant.DefaultPinCode = address.getPincode();
            Constant.DefaultCity = address.getCity();

            setData(address);

        } else {
            holder.tvName.setTextColor(ContextCompat.getColor(activity, R.color.gray));
            holder.tvAddressType.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.left_btn_bg, null));
            holder.tvDefaultAddress.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.left_btn_bg, null));
            holder.imgSelect.setImageResource(R.drawable.ic_uncheck_circle);
            holder.lytMain.setBackgroundResource(R.drawable.address_card_shadow);
        }

        holder.tvAddress.setText(address.getAddress() + ", " + address.getLandmark() + ", " + address.getCity() + ", " + address.getArea() + ", " + address.getState() + ", " + address.getCountry() + ", " + activity.getString(R.string.pincode_) + address.getPincode());

        if (address.getIs_default().equals("1") && layout != R.layout.lyt_address_checkout) {
            holder.tvDefaultAddress.setVisibility(View.VISIBLE);
        }

        holder.lytMain.setPadding((int) activity.getResources().getDimension(R.dimen.dimen_15dp), (int) activity.getResources().getDimension(R.dimen.dimen_15dp), (int) activity.getResources().getDimension(R.dimen.dimen_15dp), (int) activity.getResources().getDimension(R.dimen.dimen_15dp));
        holder.tvName.setText(address.getName());
        if (!address.getType().equalsIgnoreCase("")) {
            holder.tvAddressType.setText(address.getType());
        }

        holder.tvMobile.setText(address.getMobile());

        holder.imgDelete.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getResources().getString(R.string.delete_address));
            builder.setIcon(R.drawable.ic_delete);
            builder.setMessage(activity.getResources().getString(R.string.delete_address_msg));

            builder.setCancelable(false);
            builder.setPositiveButton(activity.getResources().getString(R.string.remove), (dialog, which) -> {
                if (ApiConfig.isConnected(activity)) {
                    addresses.remove(address);
                    notifyItemRemoved(position);
                    ApiConfig.removeAddress(activity, address.getId());
                }
                if (addresses.size() == 0) {
                    Constant.selectedAddress = "";
                    AddressListFragment.tvAlert.setVisibility(View.VISIBLE);
                } else {
                    AddressListFragment.tvAlert.setVisibility(View.GONE);
                }
            });

            builder.setNegativeButton(activity.getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();

        });

        holder.lytMain.setOnClickListener(v -> {
            if ((session.getData(Constant.SHIPPING_TYPE).equals("local") && address.getCity_id().equals("0") && address.getArea_id().equals("0") && address.getPincode_id().equals("0")) || (session.getData(Constant.SHIPPING_TYPE).equals("standard") && !address.getCity_id().equals("0") && !address.getArea_id().equals("0") && !address.getPincode_id().equals("0"))) {
                Toast.makeText(activity, activity.getString(R.string.address_is_not_selectable) + activity.getString(R.string.because) + (session.getData(Constant.SHIPPING_TYPE).equals("local") ? activity.getString(R.string.standard) : activity.getString(R.string.local)), Toast.LENGTH_SHORT).show();
            } else {
                setData(address);
                notifyDataSetChanged();
            }

        });

        if (layout == R.layout.lyt_address_list) {
            if ((session.getData(Constant.SHIPPING_TYPE).equals("local") && address.getCity_id().equals("0") && address.getArea_id().equals("0") && address.getPincode_id().equals("0")) || (session.getData(Constant.SHIPPING_TYPE).equals("standard") && !address.getCity_id().equals("0") && !address.getArea_id().equals("0") && !address.getPincode_id().equals("0"))) {
                holder.tvAlert.setVisibility(View.VISIBLE);
                holder.imgEdit.setVisibility(View.GONE);
            } else {
                holder.tvAlert.setVisibility(View.GONE);
                holder.imgEdit.setVisibility(View.VISIBLE);
            }
        }

        holder.imgEdit.setOnClickListener(view -> {
            if (ApiConfig.isConnected(activity)) {
                Fragment fragment = new AddressAddUpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("model", address);
                bundle.putString("for", "update");
                bundle.putInt("position", position);
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            }
        });


    }

    public void setData(Address address) {
        Constant.selectedAddress = address.getAddress() + ", " + address.getLandmark() + ", " + address.getCity() + ", " + address.getArea() + ", " + address.getState() + ", " + address.getCountry() + ", " + activity.getString(R.string.pincode_) + address.getPincode();
        Constant.selectedAddressId = address.getId();

        session.setData(Constant.LONGITUDE, address.getLongitude());
        session.setData(Constant.LATITUDE, address.getLatitude());

    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    static class AddressItemHolder extends RecyclerView.ViewHolder {

        final TextView tvName;
        final TextView tvAddress;
        final TextView tvAddressType;
        final TextView tvMobile;
        final TextView tvDefaultAddress;
        final TextView tvAlert;
        final ImageView imgEdit;
        final ImageView imgDelete;
        final ImageView imgSelect;
        final LinearLayout lytMain;

        public AddressItemHolder(@NonNull View itemView) {
            super(itemView);
            lytMain = itemView.findViewById(R.id.lytMain);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvAddressType = itemView.findViewById(R.id.tvAddressType);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvDefaultAddress = itemView.findViewById(R.id.tvDefaultAddress);
            tvAlert = itemView.findViewById(R.id.tvAlert);

            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgSelect = itemView.findViewById(R.id.imgSelect);
            imgDelete = itemView.findViewById(R.id.imgDelete);


        }
    }
}