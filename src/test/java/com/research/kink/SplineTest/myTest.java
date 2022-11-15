package com.research.kink.SplineTest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.research.kink.geo.TrySpline;

import com.research.kink.utils.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class myTest {

    private static double METERS_BETWEEN_POINTS = 2.0;
    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        String feature = TrySpline.class.getClassLoader().getResourceAsStream("ROAD_CURVATURE_RULE_SCENARIOS/t.json").toString();

        Road road = null;
        try{
           road=  mapper.readValue(feature, Road.class);
        }catch (NullPointerException ex){
            // convert JSON string to `JsonNode`
            JsonNode node = mapper.readTree(feature);
            feature =node.get("features").get(0).toString();
            road=mapper.readValue(feature, Road.class);

        }

        dynamicdistanceinMeter(road); // new values for distance between the points

        Coordinate[] originalGeometry=road.getGeometry().getCoordinates();

        Spline spline = new Spline(Arrays.asList(originalGeometry));
        Coordinate[] splinePoints = calcSplinePoints(spline);

        Boolean toMultistring = false;
        if(toMultistring){

            LineString[] multiLineStringArrayforComparision= new LineString[2];
            multiLineStringArrayforComparision[0]=new GeometryFactory().createLineString(originalGeometry);
            multiLineStringArrayforComparision[1]=new GeometryFactory().createLineString(splinePoints);



            //update current feature's geometry to spline geometry
            //road.setGeometry( new GeometryFactory().createMultiLineString(multiLineStringArrayforComparision));
//            road.getProperties().setUnknown("stroke","#020000"); //black
//            road.getProperties().setUnknown("stroke-width",1.0);

            String json = mapper.writeValueAsString(road);
            System.out.println(json);

            System.exit(0);

        }
        else{
 /// For feature Collection
            road.setGeometry( new GeometryFactory().createLineString(splinePoints));
//            road.getProperties().setUnknown("stroke","#020000"); // black
//            road.getProperties().setUnknown("stroke-width",1.0);


        }



        // For multiple Features creating the files to visualize
        String json = mapper.writeValueAsString(road);  // after converting the spline
        System.out.println(json);

        Road Originalroad = mapper.convertValue(feature, Road.class);
        Originalroad.setId("1232432");
//        Originalroad.getProperties().setUnknown("stroke","#E95C07"); // orange
//        Originalroad.getProperties().setUnknown("stroke-width",1.0);


        String originalJson=mapper.writeValueAsString(Originalroad);


        System.out.println("{\"type\": \"FeatureCollection\",\"features\":["+originalJson+","+json+"]}");


        // Next step identify the valid points
        List<GeoPoint> needtochange=   calculatethedifferenceForHighPrecision(Originalroad,road);


      // List<GeoPoint> needtochange= calculatethedifference(Originalroad,road);

        System.out.println(needtochange.size());

        // show points on the Geojson

        Road gettheChangedPointRoad = mapper.convertValue(feature, Road.class);
        gettheChangedPointRoad.setId("456789");
        Coordinate[] points = new Coordinate[needtochange.size()];

      for(int i=0;i<needtochange.size();i++){
          points[i]= new Coordinate(needtochange.get(i).lon()/(double)100000,needtochange.get(i).lat()/(double)100000);
      }

        //gettheChangedPointRoad.setGeometry(new GeometryFactory().createMultiPoint(points));

      System.out.println("{\"type\": \"FeatureCollection\",\"features\":["+originalJson+","+json+","+gettheChangedPointRoad+"]}");


    }

    private static List<GeoPoint> calculatethedifference(Road original, Road interpolated){



        Coordinate[] coordinatesInterpolated=interpolated.getGeometry().getCoordinates();
        Integer ntu =100000;
        List<GeoPoint> changedPoint= new ArrayList();


        for(int i=0;i<coordinatesInterpolated.length-1;i++){

            GeoPoint point1= new GeoSimplePoint((int)(ntu * coordinatesInterpolated[i].y),(int)(ntu*coordinatesInterpolated[i].x));
            GeoPoint point2= new GeoSimplePoint((int)(ntu * coordinatesInterpolated[i+1].y),(int)(ntu*coordinatesInterpolated[i+1].x));

            GeoSegment geoSegment= new GeoSegment(point1,point2); // in meters

            Coordinate[] coordinatesoriginal=original.getGeometry().getCoordinates();



         for(int j=0;j<coordinatesoriginal.length-1;j++){

             GeoPoint point= new GeoSimplePoint((int)(ntu * coordinatesoriginal[j].y),(int)(ntu*coordinatesoriginal[j].x));

             GeoPoint bisectedPerpendicularPoint=geoSegment.bisectionPoint(point);

            if(bisectedPerpendicularPoint!=null){

                // double validdistance =point.pointDistance(bisectedPerpendicularPoint);

                double validdistance = GeoUtils.ntuDistance(point, bisectedPerpendicularPoint);
                double validdistance_precise =point.pointDistance(bisectedPerpendicularPoint);




//       EarthConstants.METERS_PER_NTU_AT_EQUATOR;

//                if(validdistance>=1.0 & validdistance<5.0)//(not more than 3 can get tyhat from metadata as per countrymetes)
//                 {
//                     System.out.println("Segment is" + geoSegment+ ".....point is "+point
//                             +"bisected pointis"+bisectedPerpendicularPoint
//                             +"DistancebetweenthePointis"+ validdistance +"precise_distance"+validdistance_precise);
//                     changedPoint.add(point);
//                 }

                if(validdistance_precise>0.0 & validdistance_precise<5.0)//(not more than 3 can get tyhat from metadata as per countrymetes)
                {
                    System.out.println("Segment is" + geoSegment+ ".....point is "+point
                            +"bisected pointis"+bisectedPerpendicularPoint
                            +"DistancebetweenthePointis"+ validdistance +"precise_distance"+validdistance_precise);
                    changedPoint.add(point);
                }


            }

         }





        }


        return  changedPoint;

    }



    private static void dynamicdistanceinMeter(Road originalroad){
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

       // System.out.println(METERS_BETWEEN_POINTS);

        //METERS_BETWEEN_POINTS=2;
    }


    private static Coordinate[] calcSplinePoints(Spline spline)
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

 // For High precision
    private static List<GeoPoint> calculatethedifferenceForHighPrecision(Road original,Road interpolated){


        Integer ntu =100000;

        Coordinate[] coordinatesInterpolated=interpolated.getGeometry().getCoordinates();

        List<GeoPoint> changedPoint= new ArrayList<>();


        for(int i=0;i<coordinatesInterpolated.length-1;i++){

            DoublePoint point1 = new DoublePoint(coordinatesInterpolated[i].y, coordinatesInterpolated[i].x);
            DoublePoint point2 = new DoublePoint(coordinatesInterpolated[i+1].y, coordinatesInterpolated[i+1].x);
//            GeoPoint point1= new GeoSimplePoint((int)(ntu * coordinatesInterpolated[i].y),(int)(ntu*coordinatesInterpolated[i].x));
//            GeoPoint point2= new GeoSimplePoint((int)(ntu * coordinatesInterpolated[i+1].y),(int)(ntu*coordinatesInterpolated[i+1].x));



            Coordinate[] coordinatesoriginal=original.getGeometry().getCoordinates();



            for(int j=0;j<coordinatesoriginal.length-1;j++){

                DoublePoint point= new DoublePoint(coordinatesoriginal[j].y,coordinatesoriginal[j].x);

                DoublePoint bisectedPerpendicularPoint=bisectionPoint(point,point1,point2);

                if(bisectedPerpendicularPoint!=null){

                    // double validdistance =point.pointDistance(bisectedPerpendicularPoint);

                    double validdistance = GeoUtils.ntuDistance(point, bisectedPerpendicularPoint)*ntu;
                //    double validdistance_precise =point.pointDistance(bisectedPerpendicularPoint);




                    if(validdistance>0.5 & validdistance<5.0)//(not more than 3 can get tyhat from metadata as per countrymetes)
                    {
                        System.out.println(".....point is "+point
                                +"bisected pointis"+bisectedPerpendicularPoint
                                +"DistancebetweenthePointis"+ validdistance );
                        GeoPoint temppoint= new GeoSimplePoint((int)(ntu * point.lat()),(int)(ntu*point.lon()));
                        changedPoint.add(temppoint);
                    }


                }

            }





        }


        return  changedPoint;

    }

    private static DoublePoint bisectionPoint(DoublePoint orthoPoint,DoublePoint point1,DoublePoint point2)
    {
        DoublePoint result = new DoublePoint(0, 0);
        double      dotPAB;
        double      dotPBA;
        double      ab;    // distance AB
        double      aq;    // distance A-result
        // Aliases P, A, B for the argument and the end points
        // of the current segment.
        DoublePoint P = orthoPoint;
//        DoublePoint A = new DoublePoint(d_p1.lat(), d_p1.lon());
//        DoublePoint B = new DoublePoint(d_p2.lat(), d_p2.lon());

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


}
