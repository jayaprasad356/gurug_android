package com.graymatterworks.gurug.model;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderTracker implements Serializable {
    String id, user_id, otp, mobile, order_note, total, delivery_charge, tax_amount, tax_percentage, wallet_balance, discount, promo_code, promo_discount, final_total, payment_method, address, latitude, longitude, delivery_time, date_added, order_from, pincode_id, area_id, address_id, bank_transfer_message, bank_transfer_status, user_name, discount_rupees;
    ArrayList<Attachment> attachment;
    ArrayList<OrderItems> items;

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getOtp() {
        return otp;
    }

    public String getMobile() {
        return mobile;
    }

    public String getOrder_note() {
        return order_note;
    }

    public String getTotal() {
        return total;
    }

    public String getDelivery_charge() {
        return delivery_charge;
    }

    public String getTax_amount() {
        return tax_amount;
    }

    public String getTax_percentage() {
        return tax_percentage;
    }

    public String getWallet_balance() {
        return wallet_balance;
    }

    public String getDiscount() {
        return discount;
    }

    public String getPromo_code() {
        return promo_code;
    }

    public String getPromo_discount() {
        return promo_discount;
    }

    public String getFinal_total() {
        return final_total;
    }


    public String getPayment_method() {
        return payment_method;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDelivery_time() {
        return delivery_time;
    }

    public String getDate_added() {
        return date_added;
    }

    public String getOrder_from() {
        return order_from;
    }

    public String getPincode_id() {
        return pincode_id;
    }

    public String getArea_id() {
        return area_id;
    }

    public String getAddress_id() {
        return address_id;
    }

    public String getBank_transfer_message() {
        return bank_transfer_message;
    }

    public String getBank_transfer_status() {
        return bank_transfer_status;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getDiscount_rupees() {
        return discount_rupees;
    }

    public ArrayList<Attachment> getAttachment() {
        return attachment;
    }

    public ArrayList<OrderItems> getItems() {
        return items;
    }
}