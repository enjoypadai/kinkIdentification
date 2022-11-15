package com.research.kink.utils;

import org.locationtech.jts.geom.Coordinate;

public class CurvatureAndAngleAtPoint {
    private final double curvature;
    private final double angle;
    private final Coordinate point;
    private final double angleRelative;

    public CurvatureAndAngleAtPoint(double curvature, double angle, double angleRelative, Coordinate point) {
        this.curvature = curvature;
        this.angle = angle;
        this.point = point;
        this.angleRelative = angleRelative;
    }

    public double getCurvature() {
        return curvature;
    }

    public double getAngle() {
        return angle;
    }

    public Coordinate getPoint() {
        return point;
    }

    public CurvatureAtPoint getCurvatureAtPoint(){
        return new CurvatureAtPoint(point, curvature);
    }

    public AngleAtPoint getAngleAtPoint(){
        return new AngleAtPoint(point, angle, angleRelative);
    }

    public double getAngleRelative() {
        return angleRelative;
    }
}
