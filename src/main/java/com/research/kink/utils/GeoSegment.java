package com.research.kink.utils;

public class GeoSegment {

    private GeoPoint d_p1;
    private GeoPoint d_p2;

    public GeoSegment(GeoPoint p1, GeoPoint p2) {
        if (p1.compareTo(p2) <= 0) {
            this.d_p1 = p1;
            this.d_p2 = p2;
        } else {
            this.d_p1 = p2;
            this.d_p2 = p1;
        }

    }

    public double length() {
        return this.d_p1.pointDistance(this.d_p2);
    }

    public GeoPoint bisectionPoint(GeoPoint orthoPoint) {
        GeoPoint result = new GeoSimplePoint(0, 0);
        GeoPoint A = this.d_p1;
        GeoPoint B = this.d_p2;

        double dotPBA = GeoUtils.realDotProduct(orthoPoint, B, A);
        double dotPAB = GeoUtils.realDotProduct(orthoPoint, A, B);
        if (dotPAB <= 0.0) {
            result = null;
        } else if (dotPBA <= 0.0) {
            result = null;
        } else if (A.sameXY(B)) {
            result.setLon(A.lon());
            result.setLat(A.lat());
        } else {
            double ab = GeoUtils.ntuDistance(B, A);
            double aq = Math.abs(dotPAB) / ab;
            result.setLon((int)Math.round((double)A.lon() + (double)(B.lon() - A.lon()) * (aq / ab)));
            result.setLat((int)Math.round((double)A.lat() + (double)(B.lat() - A.lat()) * (aq / ab)));
        }

        return result;
    }
}