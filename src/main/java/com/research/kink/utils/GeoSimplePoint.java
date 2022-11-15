package com.research.kink.utils;


public class GeoSimplePoint implements GeoPoint {
    private int d_lat;
    private int d_lon;
    private byte d_zLevel;

    public GeoSimplePoint(int lat, int lon, byte zLevel) {
        this.d_lat = lat;
        this.d_lon = lon;
        this.d_zLevel = zLevel;
    }

    public GeoSimplePoint(int lat, int lon) {
        this(lat, lon, (byte)0);
    }
    public double pointDistance(GeoPoint other) {
        int variation = Math.abs(this.lat() - other.lat()) + Math.abs(this.lon() - other.lon());
        double result;
        result = this.linearInterpolationDistance(other);
        return result;
    }

    @Override
    public int lat() {
        return this.d_lat;
    }

    @Override
    public int lon() {
        return this.d_lon;
    }

    public double linearInterpolationDistance(GeoPoint other) {
        double f = 0.00335281069344662;
        double a = 6378137.0;
        double RPNTU = 1.7453292519943297E-7;
        double e2 = f * (2.0 - f);
        double phi1 = (double)this.lat() * RPNTU;
        double lam1 = (double)this.lon() * RPNTU;
        double phi2 = (double)other.lat() * RPNTU;
        double lam2 = (double)other.lon() * RPNTU;
        double dphi = phi2 - phi1;
        double dlam = lam2 - lam1;
        double phim = (phi1 + phi2) / 2.0;
        double cphi = Math.cos(phim);
        double s2phi = 1.0 - cphi * cphi;
        double t = 1.0 - e2 * s2phi;
        double N = a / Math.sqrt(t);
        double M = a * (1.0 - e2) / Math.sqrt(t * t * t);
        double De = N * cphi * dlam;
        double Dn = M * dphi;
        double result = Math.sqrt(De * De + Dn * Dn);
        return result;
    }

    public boolean sameXY(GeoPoint other) {
        return this.compareXY(other) == 0;
    }

    public void setLat(int newLat) {
        this.d_lat = newLat;
    }

    public void setLon(int newLon) {
        this.d_lon = newLon;
    }

    public int compareXY(GeoPoint otherPoint) {
        int result = 0;
        if (result == 0) {
            result = this.lat() - otherPoint.lat();
        }

        if (result == 0) {
            result = this.lon() - otherPoint.lon();
        }

        if (result < 0) {
            result = -1;
        } else if (result > 0) {
            result = 1;
        }

        return result;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
