package com.research.kink.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.LineString;

import java.util.Objects;

public class Road  {
    public Road() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String type;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String id;
    @JsonProperty("geometry")
    public LineString geometry;
    @JsonProperty("geometry")
    public LineString getGeometry() {
        return this.geometry;
    }
    @JsonProperty("geometry")
    public void setGeometry(LineString value) {
        this.geometry = value;
    }





    public int hashCode() {
        return Objects.hash(new Object[]{this.id});
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Road)) {
            return false;
        } else {
            Road other = (Road)obj;
            return Objects.equals(this.id, other.id);
        }
    }


}