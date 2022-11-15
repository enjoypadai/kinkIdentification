package com.research.kink.utils;

import org.locationtech.jts.geom.Coordinate;

public class AngleAtPoint {
    private final double angle;
    private final double relativeAngle;
    private final Coordinate point;

    public AngleAtPoint(Coordinate point, double angle, double relativeAngle){
        this.point = point;
        this.angle = angle;
        this.relativeAngle = relativeAngle;
    }

    public double getAngle() {
        return angle;
    }

    public Coordinate getPoint() {
        return point;
    }

    public double getRelativeAngle() {
        return relativeAngle;
    }
}
