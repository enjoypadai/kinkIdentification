package com.research.kink.utils;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplineUtils {

    private static double METERS_BETWEEN_POINTS = 2.0;
    private static Integer ntu =100000;


    public static void dynamicdistanceinMeter(Road originalroad){
        double originalsize= originalroad.getGeometry().getCoordinates().length;

        Coordinate[] coordinates=originalroad.getGeometry().getCoordinates();

        Integer ntu =100000;
        double totaldistanceofthespline=0.0;

        for(int i=0;i<coordinates.length-1;i++){
            GeoPoint point1= new GeoSimplePoint((int)(ntu * coordinates[i].y),(int)(ntu*coordinates[i].x));
            GeoPoint point2= new GeoSimplePoint((int)(ntu * coordinates[i+1].y),(int)(ntu*coordinates[i+1].x));

            GeoSegment geoSegment= new GeoSegment(point1,point2); // in meters
            totaldistanceofthespline+=geoSegment.length();

        }



        METERS_BETWEEN_POINTS=(totaldistanceofthespline/(originalsize+1)); // dynamic change in the distance

    }


    public static List<RepairResults> calculatethedifferenceForHighPrecision(Road original, Road interpolated){



        Coordinate[] coordinatesInterpolated=interpolated.getGeometry().getCoordinates();

        List<RepairResults> changedPoint= new ArrayList();


        for(int i=0;i<coordinatesInterpolated.length-1;i++){

            DoublePoint point1 = new DoublePoint(coordinatesInterpolated[i].y, coordinatesInterpolated[i].x);
            DoublePoint point2 = new DoublePoint(coordinatesInterpolated[i+1].y, coordinatesInterpolated[i+1].x);

            Coordinate[] coordinatesoriginal=original.getGeometry().getCoordinates();



            for(int j=0;j<coordinatesoriginal.length-1;j++){

                DoublePoint point= new DoublePoint(coordinatesoriginal[j].y,coordinatesoriginal[j].x);

                DoublePoint bisectedPerpendicularPoint=bisectionPoint(point,point1,point2);

                if(bisectedPerpendicularPoint!=null){

                    double validdistance = GeoUtils.ntuDistance(point, bisectedPerpendicularPoint)*ntu;
                    Coordinate[] result=null;

                    if(validdistance>0.10 && validdistance<5.0)//(not more than 3 can get tyhat from metadata as per countrymetes)
                    {
                        System.out.println(".....point is "+point
                                +"bisected pointis"+bisectedPerpendicularPoint
                                +"DistancebetweenthePointis"+ validdistance );
                        GeoPoint temppoint= new GeoSimplePoint((int)(ntu * point.lat()),(int)(ntu*point.lon()));

                        if(validdistance>1.0& validdistance<5.0){
                            Coordinate[] InterpolatedGeometry= new Coordinate[]{coordinatesoriginal[j-1],coordinatesoriginal[j],coordinatesoriginal[j+1]};

                            result=AutoCorrecttheSpline(changedPoint,InterpolatedGeometry);

                        }

                        changedPoint.add(new RepairResults(temppoint,result,validdistance));

                    }


                }

            }





        }


        return  changedPoint;

    }

public static Coordinate[] AutoCorrecttheSpline(List<RepairResults> checkinthechangedPoint,Coordinate[] interpolatedGeometry){


        Coordinate[] result= null;
        Boolean tosolve=true;
        for(int i=0;i<checkinthechangedPoint.size();i++){
            if(tosolve==false)
                break;
           GeoPoint point= checkinthechangedPoint.get(i).Points;
           double templat=point.lat()/ntu;
           double templon=point.lon()/ntu;
           for(int j=0;j<interpolatedGeometry.length;j++){

               Coordinate coordinate =interpolatedGeometry[j];
               if(coordinate.x==templon && coordinate.y==templat) {
                   tosolve = false;
                   break;
               }
           }


        }

        if(tosolve){
            Spline spline = new Spline(Arrays.asList(interpolatedGeometry));
            Coordinate[] splinePoints = SplineUtils.calcSplinePoints(spline);
            result=splinePoints;
        }

        return result;
    }



    public static Double calculatethedistancefortheroads(Road left, Road right){


        Integer ntu =100000;
        double count=0;
        double sumofeachpointsDistance=0;

        Coordinate[] coordinatesInterpolated=right.getGeometry().getCoordinates();

        List<GeoPoint> changedPoint= new ArrayList<>();


        for(int i=0;i<coordinatesInterpolated.length-1;i++){

            DoublePoint point1 = new DoublePoint(coordinatesInterpolated[i].y, coordinatesInterpolated[i].x);
            DoublePoint point2 = new DoublePoint(coordinatesInterpolated[i+1].y, coordinatesInterpolated[i+1].x);

            Coordinate[] coordinatesoriginal=left.getGeometry().getCoordinates();



            for(int j=0;j<coordinatesoriginal.length-1;j++){

                DoublePoint point= new DoublePoint(coordinatesoriginal[j].y,coordinatesoriginal[j].x);

                DoublePoint bisectedPerpendicularPoint=bisectionPoint(point,point1,point2);

                if(bisectedPerpendicularPoint!=null){

                    double validdistance = GeoUtils.ntuDistance(point, bisectedPerpendicularPoint)*ntu;


                        System.out.println(".....point is "+point
                                +"bisected pointis"+bisectedPerpendicularPoint
                                +"DistancebetweenthePointis"+ validdistance );
                        GeoPoint temppoint= new GeoSimplePoint((int)(ntu * point.lat()),(int)(ntu*point.lon()));
                        changedPoint.add(temppoint);


                    sumofeachpointsDistance+=validdistance;
                       count++;

                }

            }





        }


        System.out.print("distance between the boundary is ..... "+sumofeachpointsDistance/count +"all the point above this are case for relative accuracy");

        return  sumofeachpointsDistance/count;

    }


    public static DoublePoint bisectionPoint(DoublePoint orthoPoint,DoublePoint point1,DoublePoint point2)
    {
        DoublePoint result = new DoublePoint(0, 0);
        double      dotPAB;
        double      dotPBA;
        double      ab;    // distance AB
        double      aq;    // distance A-result

        DoublePoint P = orthoPoint;


        DoublePoint A = point1;
        DoublePoint B =point2;


        // if AP dot-product AB < 0 then angle PAB is > 90 degrees
        dotPBA = GeoUtils.realDotProduct(P, B, A);
        dotPAB = GeoUtils.realDotProduct(P, A, B);

        if (dotPAB <= 0.0)
        {
            // P is at or beyond A
            result = null;
        }
        // if AB dot-product BP < 0, then angle ABP is > 90 degrees
        else if (dotPBA <= 0.0)
        {
            // P is at or beyond B
            result = null;
        }
        else
        {
            // P is within AB
            if (A.sameXY(B))
            {
                // defensive programming: |AB| is zero
                result = new DoublePoint(A.lat(), A.lon());
            }
            else
            {
                // Combine dot product equality and "cos(ang) = adj/hypot", then
                // subst AQ (adj) and AP (hypot) to get a formula involving AQ:
                // AP dot AB = |AP| |AB| cos(PAQ)
                // AP dot AB = |AP| |AB|  |AQ| / |AP|
                // simplify and extract AQ:
                // |AQ| = (AP dot AB) / |AB|
                //
                ab = GeoUtils.ntuDistance(B, A);
                aq = Math.abs(dotPAB) / ab;
                result = new DoublePoint((A.lat()
                        + (B.lat() - A.lat())
                        * (aq / ab)), (A.lon()
                        + (B.lon() - A.lon())
                        * (aq / ab)));
            }
        }

        return result;
    }


    public static Coordinate[] calcSplinePoints(Spline spline)
    {
        // Set-up output parameters
        int         size            = (int) Math.ceil(spline.getShapeLength() / METERS_BETWEEN_POINTS);
        Coordinate result[]        = new Coordinate[size + 1];
        double      distanceCounter = 0.0;

        for (int i = 0; i < result.length; ++i, distanceCounter += METERS_BETWEEN_POINTS)
        {
            result[i] = spline.getSplinePoint(distanceCounter);
        }

        return result;
    }


}
