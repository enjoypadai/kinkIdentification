package com.research.kink.SplineTest;


import com.research.kink.utils.Road;

import java.io.Serializable;
import java.util.List;

public class FeatureCollection implements Serializable {

    String type="FeatureCollection";
     List<Road> features=null;

    public FeatureCollection() {
    }

    public FeatureCollection(List<Road> features) {
        this.features = features;
    }
}
