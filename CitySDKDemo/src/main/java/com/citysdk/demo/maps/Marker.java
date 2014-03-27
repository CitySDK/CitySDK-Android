package com.citysdk.demo.maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Marker implements ClusterItem {
    private final LatLng mPosition;
    private String name;
    private String category;
    private String id;
    private String base;

    public Marker(double lat, double lng, String name, String category, String id, String base) {
        mPosition = new LatLng(lat, lng);
        this.name = name;
        this.category = category;
        this.id = id;
        this.base = base;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public String getBase() {
        return base;
    }
}
