package com.maggie.smarthelmet;

import com.mapbox.directions.service.models.Waypoint;

public class Coordinates {
    public double latitude;
    public double longitude;
    Waypoint waypoint = null;
    private MappingActivity mappingActivity;

    Coordinates(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }


    public void setWaypoint() {
        mappingActivity = new MappingActivity();
        waypoint = mappingActivity.dropWayPoint(latitude, longitude);
    }


    public Waypoint getWaypoint() {
        return this.waypoint;
    }

}
