package com.research.kink.geo;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.research.kink.utils.GeoUtils;
import com.research.kink.utils.RoadN;
import com.research.kink.utils.Spline;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.IOException;
import java.util.Arrays;

public class TrySpline {
    private static final double METERS_BETWEEN_POINTS = 2.0;
    static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) throws IOException {
        //example feature
        String feature = GeoUtils.readFileFromResource("ROAD_CURVATURE_RULE_SCENARIOS/t.json");
       RoadN road = mapper.readValue(feature,RoadN.class);
      //  Road road = mapper.convertValue(feature,Road.class);
        Spline spline = new Spline(Arrays.asList(road.getJtsLineString().getCoordinates()));
        Coordinate[] splinePoints = calcSplinePoints(spline);

        //update current feature's geometry to spline geometry
        road.setGeometry(road.getLineString2Geo(new GeometryFactory().createLineString(splinePoints)));
        String json = mapper.writeValueAsString(road);
        System.out.println(json);
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
}
