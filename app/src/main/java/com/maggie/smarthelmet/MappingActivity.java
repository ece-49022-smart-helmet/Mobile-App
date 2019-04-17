package com.maggie.smarthelmet;


import android.location.Geocoder;
import android.util.Log;

import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.MapboxDirections;
import com.mapbox.directions.service.models.DirectionsResponse;
import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.Waypoint;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit.Retrofit;
import retrofit2.Response;

public class MappingActivity {
    private static final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoibWJhcmJhZ2FsbG8iLCJhIjoiY2pyd3FvaTYzMGZpdTQ0bWw2dWZvdnN2ayJ9.wja8Auxhuv3swR7dOtIhpw";

    public Waypoint dropWayPoint(double latitude, double longitude) {  //drop way point start, end and then every 10 min
        return new Waypoint(latitude, latitude);
    }


    /* Gets a list of Mapbox directions for a route with origin, mid and end
     * @params - start, mid and end way points (the list will just be one way point)
     * @return - Mapbox Directions with routes
     */
    public MapboxDirections getRoute(Waypoint origin, List<Waypoint> wayPointList, Waypoint dest) {
        MapboxDirections md = new MapboxDirections.Builder()
                .setAccessToken(MAPBOX_ACCESS_TOKEN)
                .setOrigin(origin)
                .setWaypoints(wayPointList)
                .setDestination(dest)
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .build();
        return md;
    }


    /* Checks to see if the midpoint of the most recently travel route is within 0.1 miles of a previous route
     * @params - start, mid and end way point of the original route, and mid of the new route
     * @return - bool stating whether or not the way point is on the route
     */
    public boolean compareRoutes(Waypoint start, Waypoint end, Waypoint mid, Waypoint check) {
        List<Waypoint> list = Arrays.asList(mid);

        MapboxDirections directions = getRoute(start, list, end);
        try {
            retrofit.Response<DirectionsResponse> response = directions.execute();  //has to cast to retrofit.Response
            DirectionsRoute route = response.body().getRoutes().get(0);
            if (route.isOffRoute(check)) {
                Log.i("compareRoutes", "route is too different");
                return false;
            } else {
                return true;
            }

        } catch (IOException e) {
            Log.e("compareRoutes", "directions.execute() failed");
        }
        return false;
    }


    /* Finds the optimal route between two points
     * @params - start and end way points
     * @return - DirectionsRoute object that is the optimal route
     */
    public DirectionsRoute findOptimalRoute(Waypoint start, Waypoint end) {
        MapboxDirections directions = getRoute(start, Collections.EMPTY_LIST, end);
        try {
            retrofit.Response<DirectionsResponse> response = directions.execute();  //has to cast to retrofit.Response
            DirectionsRoute route = response.body().getRoutes().get(0);
            return route;
        } catch (IOException e) {
            Log.e("findOptimalRoute", "directions.execute() failed -- could not get optimal route");
            return null;
        }
    }
}
