package com.maggie.smarthelmet;

import android.content.Context;

import com.mapbox.directions.service.models.Waypoint;

public class Coordinates {
    public double latitude;
    public double longitude;
    private Context mContext;
    Waypoint waypoint = null;
    private MappingActivity mappingActivity;

    Coordinates(double lat, double lon, Context context) {
        latitude = lat;
        longitude = lon;
        mContext = context;
    }


    public void setWaypoint() {
        mappingActivity = new MappingActivity(mContext);
        waypoint = mappingActivity.coordToWaypoint(latitude, longitude);
    }


    public Waypoint getWaypoint() {
        return this.waypoint;
    }

}
