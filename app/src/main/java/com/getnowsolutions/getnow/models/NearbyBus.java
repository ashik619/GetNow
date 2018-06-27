package com.getnowsolutions.getnow.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dilip on 1/2/18.
 */

public class NearbyBus {

    private String name;
    private LatLng latLng;

    public LatLng getLatLng() {
        return latLng;
    }

    public String getName() {
        return name;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setName(String name) {
        this.name = name;
    }
}
