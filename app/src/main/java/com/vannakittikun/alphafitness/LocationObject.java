package com.vannakittikun.alphafitness;

/**
 * Created by Rule on 11/4/2017.
 */

public class LocationObject {
    private int id;
    private double latitude;
    private double longitude;
    private long time;

    public LocationObject (double lat, double lng, long time){
        this.latitude = lat;
        this.longitude = lng;
        this.time = time;
    }

    public void setLatitude(double lat){
        this.latitude = lat;
    }

    public void setLongitude(double lng){
        this.longitude = lng;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }
}
