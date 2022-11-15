package com.research.kink.utils;

import org.locationtech.jts.geom.Coordinate;

public class CurvatureAtPoint {
    private final double curvature;
    private final Coordinate point;

    public CurvatureAtPoint(Coordinate point, double curvature){
        this.point = point;
        this.curvature = curvature;
    }

    public double getCurvature() {
        return curvature;
    }

    public Coordinate getPoint() {
        return point;
    }
}
