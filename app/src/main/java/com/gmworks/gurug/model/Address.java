package com.gmworks.gurug.model;

import java.io.Serializable;

public class Address implements Serializable {

    String id;
    String user_id;
    String type;
    String name;
    String mobile;
    String alternate_mobile;
    String address;
    String landmark;
    String state;
    String country;
    String latitude;
    String longitude;
    String is_default;
    String area_id;
    String area;
    String city_id;
    String city;
    String pincode_id;
    String pincode;
    boolean selected;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAlternate_mobile() {
        return alternate_mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLandmark() {
        return landmark;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getIs_default() {
        return is_default;
    }

    public void setIs_default(String is_default) {
        this.is_default = is_default;
    }


    public String getArea_id() {
        return area_id;
    }

    public String getArea() {
        return area;
    }

    public String getCity_id() {
        return city_id;
    }

    public String getCity() {
        return city;
    }

    public String getPincode_id() {
        return pincode_id;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
