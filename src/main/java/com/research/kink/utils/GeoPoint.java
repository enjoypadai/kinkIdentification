package com.research.kink.utils;

public interface GeoPoint extends Comparable{
    int  lat();

    int lon();

    boolean sameXY(GeoPoint var1);
    void setLat(int var1);

    void setLon(int var1);

    double pointDistance(GeoPoint var1);


}
