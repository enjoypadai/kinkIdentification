package com.research.kink.utils;


import org.locationtech.jts.geom.Coordinate;


public class RepairResults {
    public GeoPoint getPoints() {
        return Points;
    }

    public double size;

    public Coordinate[] getInterpolatedValues() {
        return interpolatedValues;
    }

    public RepairResults(GeoPoint points, Coordinate[] interpolatedValues, double size) {
        Points = points;
        this.interpolatedValues = interpolatedValues;
        this.size=size;
    }

    GeoPoint Points=null;
    Coordinate[] interpolatedValues=null;

}
