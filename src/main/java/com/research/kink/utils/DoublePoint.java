package com.research.kink.utils;

public class DoublePoint {

    private double d_lat;
    private double d_lon;

    public DoublePoint(double lat, double lon) {
        this.d_lat = lat;
        this.d_lon = lon;
    }

    public double lat() {
        return this.d_lat;
    }

    public double lon() {
        return this.d_lon;
    }

    public String toString() {
        return " Lat : " + this.d_lat + "Lon : " + this.d_lon;
    }
    public boolean sameXY(DoublePoint other) {
        return this.lat() == other.lat() && this.lon() == other.lon();
    }
}
