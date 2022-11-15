package com.research.kink.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.util.Assert;


import java.util.LinkedList;
import java.util.List;


/**
 * Credit goes to Here Maps
 */
public class Spline
{
    private static final double EARTH_RADIUS = 6371000.0;


    private List<Coordinate> d_shape;

    private double[] d_x, d_y;


    private double[] d_x2, d_y2;


    private double[] d_dist;


    public Spline(List<? extends Coordinate> link)
    {
        d_shape = new LinkedList<Coordinate>(link);

        calcSpline(link.toArray(new Coordinate[link.size()]));
    }


    public Coordinate getSplinePoint(double m)
    {
        m = clampM(m);

        int    k0 = findSegment(d_dist, m);
        double t  = calculateT(k0, d_dist, m);

        return getSplinePoint(k0, t);
    }


    private Coordinate getSplinePoint(int k0, double t)
    {
        t = clampT(t);

        double y      = splineInterpolation(k0, d_dist, d_y, d_y2, t);
        double x      = splineInterpolation(k0, d_dist, d_x, d_x2, t);
        double ntuLat = locLatToLat(y, d_y[0]);
        double ntuLon = locLonToLon(x, d_y[0], d_x[0]);

        return new Coordinate(ntuLon,ntuLat,0);
    }


    public double getShapeLength()
    {
        return d_dist[d_dist.length - 1];
    }



    private double clampM(double m)
    {
        if (m < 0.0)
        {
            m = 0.0;
        }
        else if (m > getShapeLength())
        {
            m = getShapeLength();
        }

        return m;
    }


    static private double clampT(double t)
    {
        if (t < 0.0)
        {
            t = 0.0;
        }
        else if (t > 1.0)
        {
            t = 1.0;
        }

        return t;
    }


    private double calculateT(int k0, double[] xa, double m)
    {

        Assert.isTrue(xa[k0] <= m && xa[k0 + 1] >= m);

        return (m - xa[k0]) / (xa[k0 + 1] - xa[k0]);
    }


    private void calcSpline(Coordinate[] shape)
    {
        //DoublePoint[] shape = convertToDegrees(link);

        d_x = convertLonToLocCordSys(shape);
        d_y = convertLatToLocCordSys(shape);

        Assert.isTrue(d_x.length == d_y.length);

        d_dist = calculateShapeLength(d_x, d_y);

        Assert.isTrue(d_x.length == d_dist.length);

        //second derivatives
        d_x2 = new double[d_x.length];
        d_y2 = new double[d_y.length];

        //compute the second derivative tables for the x and y values.
        spline(d_dist, d_x, Float.MAX_VALUE, Float.MAX_VALUE, d_x2,
                shape.length);
        spline(d_dist, d_y, Float.MAX_VALUE, Float.MAX_VALUE, d_y2,
                shape.length);
    }


    private static double[] convertLonToLocCordSys(Coordinate[] shape)
    {
        double[] x      = new double[shape.length + 1];
        double   orgLon = shape[0].x;
        double   orgLat = shape[0].y;

        //x[0] stores the original lon of the first point
        //so that we can translate back to the global coordinates
        x[0] = shape[0].x;
        x[1] = 0.0;

        for (int i = 1; i < shape.length; ++i)
        {
            x[i + 1] = lonToLocLon(shape[i].x, orgLat, orgLon);
        }

        return x;
    }

    private static double[] convertLatToLocCordSys(Coordinate[] shape)
    {
        double[] y      = new double[shape.length + 1];
        double   orgLat = shape[0].y;

        //y[0] stores the original lon of the first point
        //so that we can translate back to the global coordinates
        y[0] = shape[0].y;
        y[1] = 0.0;

        for (int i = 1; i < shape.length; ++i)
        {
            y[i + 1] = latToLocLat(shape[i].y, orgLat);
        }

        return y;
    }


    private static double lonToLocLon(double lon, double orgLat,
                                      double orgLon)
    {
        return Math.toRadians(lon - orgLon) * EARTH_RADIUS
                * Math.cos(Math.toRadians(orgLat));
    }


    private static double latToLocLat(double lat, double orgLat)
    {
        return Math.toRadians(lat - orgLat) * EARTH_RADIUS;
    }


    private static double locLonToLon(double x, double orgLat, double orgLon)
    {
        return Math.toDegrees(x) / EARTH_RADIUS
                / Math.cos(Math.toRadians(orgLat)) + orgLon;
    }


    private static double locLatToLat(double y, double orgLat)
    {
        return Math.toDegrees(y) / EARTH_RADIUS + orgLat;
    }


    private static double[] calculateShapeLength(double[] x, double[] y)
    {
        double[] result = new double[x.length];

        //we set the first distance to 0 as it is the first point.
        //remember that shape[0] represents the first point in global space
        //in this instance we don't worry about result[0].
        result[1] = 0.0;

        for (int i = 1; i < result.length - 1; ++i)
        {
            final double x0 = x[i],
                    x1 = x[i + 1];
            final double y0 = y[i],
                    y1 = y[i + 1];

            result[i + 1] = result[i]
                    + Math.sqrt(Math.pow(x1 - x0, 2.0)
                    + Math.pow(y1 - y0, 2.0));
        }

        return result;
    }

    /**
     * Calculates the second derivative for a cubic spline for each y[i] segment continuous across y[i] and y[i-1]
     * It uses the tridiagonal algorithm to solve the equations for the second derivative for each segment.
     * The equation to solve is
     * x[i] - x[i-1]             x[i+1] - x[i-1]
     * ------------- y"[i-1] +   --------------- y"[i]
     *      6.0                         3.0
     * x[i+1] - x[i]
     * ------------- y"[i+1] =
     * 6.0
     *
     * y[i+1] - y[i]     y[i] - y[i-1]
     * -------------  -  -------------
     * x[i+1] - x[i]     x[i] - x[i-1]
     *
     *

     */
    private static void spline(double[] x, double[] y, double yp1,
                               double ypn, double[] y2, int n)
    {
        double p, qn, sig, un;
        double u[] = new double[n];

        //determine second derivative for first point
        if (yp1 > 0.99e30)
        {
            y2[1] = 0.0;
            u[1]  = 0.0;
        }
        else
        {
            y2[1] = -0.5;
            u[1] = (3.0 / (x[2] - x[1]))
                    * ((y[2] - y[1]) / (x[2] - x[1]) - yp1);
        }

        //determine second derivative for inside points
        for (int i = 2; i < n; ++i)
        {
            sig   = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
            p     = sig * y2[i - 1] + 2.0;
            y2[i] = (sig - 1.0) / p;
            u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i])
                    - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
            u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
        }

        //determine second derivative for last point
        if (ypn > 0.99e30)
        {
            qn = 0.0;
            un = 0.0;
        }
        else
        {
            qn = 0.5;
            un = (3.0 / (x[n] - x[n - 1]))
                    * (ypn - (y[n] - y[n - 1]) / (x[n] - x[n - 1]));
        }

        y2[n] = (un - qn * u[n - 1]) / (qn * y2[n - 1] + 1.0);

        for (int i = n - 1; i > 0; --i)
        {
            y2[i] = y2[i] * y2[i + 1] + u[i];
        }
    }

    private static double splineInterpolation(int k0, double[] xa,
                                              double[] ya, double[] y2a, double t)
    {
        int    k1 = k0 + 1;
        double h  = xa[k1] - xa[k0];
        //calculate the Lagrange coefficients so that we can interpolate
        //from the beginning of the segment
        double[] coefficiants = calculateLagrangeCoeff(h, t);
        double   a            = coefficiants[0];
        double   b            = coefficiants[1];
        double   c            = coefficiants[2];
        double   d            = coefficiants[3];
        double   f            = coefficiants[4];
        double   ya0          = ya[k0];
        double   ya1          = ya[k1];
        double   y2a0         = y2a[k0];
        double   y2a1         = y2a[k1];

        return splineSegmentInt(a, b, c, d, f, ya0, ya1, y2a0, y2a1);
    }


    private static int findSegment(double[] xa, double m)
    {
        Assert.isTrue(m >= 0.0);

        int k0 = 1,
                k1 = xa.length - 1, k;

        //The while loop below is guaranteed to converge if
        //the following condition holds.
        Assert.isTrue(k1 > k0 && xa[k0] <= m && xa[k1] >= m);

        //find the segment that m lies between using binary search
        while (k1 - k0 > 1)
        {
            k = (k1 + k0) / 2;

            if (xa[k] > m)
            {
                k1 = k;
            }
            else
            {
                k0 = k;
            }
        }

        //Clearly from the while loop the following should hold true.
        Assert.isTrue(k1 - k0 == 1);

        return k0;
    }


    private static double[] calculateLagrangeCoeff(double h, double t)
    {
        //A and B are just the linear interpolation from xa[i+1] - xa[i]
        //if we were working with line segments instead of splines, we could use the special case of
        //the Lagrange interpolation formula
        //y = Ay[i] + By[i+1]
        double a  = 1.0 - t;
        double b  = t;
        double a3 = a * a * a;
        double b3 = b * b * b;
        double h2 = h * h;
        // Here
        // C = (1/6)(a^3 - a)(h^2)
        // D = (1/6)(b^3 - b)(h^2)
        double c = (a3 - a);
        double d = (b3 - b);
        // which we factor and get f = (h^2 / 6.0);
        double f = (h2 / 6.0);

        return new double[]
                {
                        a, b, c, d, f
                };
    }


    private static double splineSegmentInt(double a, double b, double c,
                                           double d, double f, double ya0,
                                           double ya1, double y2a0,
                                           double y2a1)
    {
        //y = Ay[i] + By[i+1] + Cy2[i] + Dy2[i+1]
        //f is the factor of C and D
        return a * ya0 + b * ya1 + (c * y2a0 + d * y2a1) * f;
    }
}


