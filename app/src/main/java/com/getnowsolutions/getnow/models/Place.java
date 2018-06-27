package com.getnowsolutions.getnow.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dilip on 2/10/17.
 */

public class Place {
    public String placeAddress;
    public double lat;
    public double lng;
    public String placeId;

    public Place(String placeAddress){
        this.placeAddress = placeAddress;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
