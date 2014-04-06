package com.citysdk.demo.activities;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationDetails {

    private int type = 0;

    private List<LatLng> list;

    public LocationDetails(int type) {
        this.type = type;
        list = new ArrayList<LatLng>();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return list.size();
    }

    public List<LatLng> getLatLngList() {
        return list;
    }

    public void addLatLngList(LatLng latlng) {
        list.add(latlng);
    }
}
