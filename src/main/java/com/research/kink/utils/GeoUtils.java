package com.research.kink.utils;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GeoUtils {
    private static double MIN_ANGLE_ALLOWED = 175.0;

    public static double curvature(Coordinate p1, Coordinate p2, Coordinate p3) {
        double y1 = p1.y;
        double y2 = p2.y;
        double y3 = p3.y;
        double x1 = p1.x;
        double x2 = p2.x;
        double x3 = p3.x;

        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        double dx2 = x3 - x1;
        double dy2 = y3 - y1;

        double area = dx1 * dy2 - dy1 * dx2;
        double len0 = p3.distance(p1);
        double len1 = p2.distance(p1);
        double len2 = p2.distance(p3);
        double curvature = (4 * area / (len0 * len1 * len2));

        return Math.abs(curvature);
    }

    public static Map<Coordinate, Double> calculateCurvature(List<Coordinate> coordinates) {
        Map<Coordinate, Double> pointToCurvature = new LinkedHashMap<>();

        for (int i = 1; i < coordinates.size() - 1; i++) {
            int start = i;
            int end = start + 1;
            int before = start - 1;
            double curvature = curvature(coordinates.get(before), coordinates.get(start), coordinates.get(end));
            pointToCurvature.put(coordinates.get(start), curvature);
        }

        return pointToCurvature;
    }

    public static CurvatureAtPoint getMaxCurvaturePoint(LineString lineString) {
        Map<Coordinate, Double> curvatures = calculateCurvature(Arrays.asList(lineString.getCoordinates()));
        double maxCurvature = 0.0;
        Coordinate coordinate = null;

        for (Map.Entry<Coordinate, Double> e : curvatures.entrySet()) {
            if (e.getValue() > maxCurvature) {
                maxCurvature = e.getValue();
                coordinate = e.getKey();
            }
        }

        return new CurvatureAtPoint(coordinate, maxCurvature);
    }

    public static AngleAtPoint getMinAnglePointBetweenSegments(LineString lineString) {
        Coordinate[] coordinates = lineString.getCoordinates();
        double minAngle = 1000.0;
        double relativeAngle = 0.0;
        Coordinate coordinate = null;

        for (int i = 1; i < coordinates.length - 1; i++) {
            int start = i;
            int end = start + 1;
            int before = start - 1;
            double angle = Math.abs(Angle.toDegrees(Angle.angleBetweenOriented(coordinates[before], coordinates[start], coordinates[end])));

            if (angle < minAngle) {
                minAngle = angle;
                relativeAngle = 180 - minAngle;
                coordinate = coordinates[start];
            }
        }

        return new AngleAtPoint(coordinate, minAngle, relativeAngle);
    }

    public static List<CurvatureAndAngleAtPoint> getCurvatureAndAngle(LineString lineString) {
        Coordinate[] coordinates = lineString.getCoordinates();
        Map<Coordinate, Double> curvatures = calculateCurvature(Arrays.asList(coordinates));
        List<CurvatureAndAngleAtPoint> curvatureAndAngleAtPoints = new ArrayList<>();

        for (int i = 1; i < coordinates.length - 1; i++) {
            int start = i;
            int end = start + 1;
            int before = start - 1;
            double distance = -1.0;

            if (start == 1) {
                distance = distance(coordinates[before], coordinates[start]);
            } else if (start == coordinates.length - 2) {
                distance = distance(coordinates[end], coordinates[start]);
            }

            if (distance == -1.0 || distance > 0) {
                double angle = Math.abs(Angle.toDegrees(Angle.angleBetweenOriented(coordinates[before], coordinates[start], coordinates[end])));
                double angleRelative = 180.0 - angle;

                //if (angle < MIN_ANGLE_ALLOWED) {
                    curvatureAndAngleAtPoints.add(new CurvatureAndAngleAtPoint(curvatures.get(coordinates[start]), angle, angleRelative, coordinates[start]));
                //}
            }
        }

        return curvatureAndAngleAtPoints;
    }

    public static double distance(Coordinate p1, Coordinate p2) {
        double lat1 = p1.y;
        double lng1 = p1.x;
        double lat2 = p2.y;
        double lng2 = p2.x;
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist;
    }

    public static double ntuDistance(GeoPoint p1, GeoPoint p2) {
        double dx = (double)(p1.lon() - p2.lon());
        double dy = (double)(p1.lat() - p2.lat());
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double ntuDistance(DoublePoint p1, DoublePoint p2) {
        double dx = p1.lon() - p2.lon();
        double dy = p1.lat() - p2.lat();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double realDotProduct(GeoPoint p1, GeoPoint vertex, GeoPoint p2) {
        int vLon = vertex.lon();
        int vLat = vertex.lat();
        double dx1 = (double)(p1.lon() - vLon);
        double dy1 = (double)(p1.lat() - vLat);
        double dx2 = (double)(p2.lon() - vLon);
        double dy2 = (double)(p2.lat() - vLat);
        double result = dx1 * dx2 + dy1 * dy2;
        return result;
    }

    public static double realDotProduct(DoublePoint p1, DoublePoint vertex, DoublePoint p2) {
        double vLon = vertex.lon();
        double vLat = vertex.lat();
        double dx1 = p1.lon() - vLon;
        double dy1 = p1.lat() - vLat;
        double dx2 = p2.lon() - vLon;
        double dy2 = p2.lat() - vLat;
        double result = dx1 * dx2 + dy1 * dy2;
        return result;
    }


    public static String readFileFromResource(String filename) throws NullPointerException {
        InputStream resource = GeoUtils.class.getClassLoader().getResourceAsStream(filename);
        String result = "";
        try (InputStreamReader streamReader =
                     new InputStreamReader(resource, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }

        } catch (IOException e) {
            // e.printStackTrace();
        }catch (NullPointerException n){
            System.out.println("no file present");
        }

        return result;
    }

    public static LineString covertRoad2jtsGeometry(Geometry r){
        Coordinate[] originalGeometry=new Coordinate[r.getCoordinates().size()];


        int i=0;
        for (List<Double> coordinate : r.getCoordinates()) {
             double x =coordinate.get(0);
             double y = coordinate.get(1);
            originalGeometry[i]=new Coordinate(x,y);
            i++;
        }

      return   new GeometryFactory().createLineString(originalGeometry);

    }

    public static Geometry covertjtsGeometry2Road(LineString r){
        Geometry g = new Geometry();
        g.setType("LineString");

        List<List<Double>> coordinates = new ArrayList<>();

        for (Coordinate coordinate : r.getCoordinates()) {
            List<Double> c = new ArrayList<>();
            c.add(coordinate.x);
            c.add(coordinate.y);
            coordinates.add(c);
        }
        g.setCoordinates(coordinates);

        return   g;

    }
}
