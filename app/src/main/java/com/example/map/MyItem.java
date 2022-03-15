package com.example.map;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private  LatLng position;
    private  String title;

    public MyItem(double lat, double lng, String title) {
        position = new LatLng(lat, lng);
        this.title = title;
    }

    public void setPosition(LatLng position){
        this.position = position;
    }
    public LatLng getPosition() {
        return position;
    }

    public  void setTitle(String title){
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return null;
    }

}

