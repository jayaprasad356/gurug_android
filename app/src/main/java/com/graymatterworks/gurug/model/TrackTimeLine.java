package com.graymatterworks.gurug.model;

import java.io.Serializable;

public class TrackTimeLine implements Serializable {
    String date,location,activity;

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getActivity() {
        return activity;
    }
}
