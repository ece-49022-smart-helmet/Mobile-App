package com.maggie.smarthelmet;


import android.content.Context;
import android.location.Geocoder;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.MapboxDirections;
import com.mapbox.directions.service.models.DirectionsResponse;
import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.RouteStep;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.light.Position;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MappingActivity {
    private static final String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoibWJhcmJhZ2FsbG8iLCJhIjoiY2pyd3FvaTYzMGZpdTQ0bWw2dWZvdnN2ayJ9.wja8Auxhuv3swR7dOtIhpw";
    private static Context mContext;
    public List<RouteStep> routeSteps = null;

    MappingActivity(Context context) {
        mContext = context;
    }

    public Waypoint coordToWaypoint(double latitude, double longitude) {  //drop way point start, end and then every 10 min
        return new Waypoint(longitude, latitude);
    }


    /* Gets a list of Mapbox directions for a route with origin, mid and end
     * @params - start and end way points
     * @return - Mapbox Directions with routes
     */
    public MapboxDirections getRoute(Waypoint origin, Waypoint dest) {
        MapboxDirections md = new MapboxDirections.Builder()
                .setAccessToken(MAPBOX_ACCESS_TOKEN)
                .setOrigin(origin)
                .setDestination(dest)
                .setSteps(true)
                .setProfile(DirectionsCriteria.PROFILE_CYCLING)
                .build();
        Log.i("in getRoute", "md = "+md.toString());
        return md;
    }

   // public MapboxDirections getRoute(Waypoint origin, List<Waypoint> wayPointList, Waypoint dest)

    /* Checks to see if the midpoint of the most recently travel route is within 0.1 miles of a previous route
     * @params - start, mid and end way point of the original route, and mid of the new route
     * @return - bool stating whether or not the way point is on the route
     */
    public boolean compareRoutes(Waypoint start, Waypoint end, Waypoint mid, Waypoint check) {
        List<Waypoint> list = Arrays.asList(mid);

        /*
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
        */
        return false;
    }


    /* Finds the optimal route between two points
     * @params - start and end way points
     * @return - DirectionsRoute object that is the optimal route
     */
    public void findOptimalRoute(Waypoint start, Waypoint end, final int duration) {
        MapboxDirections directions = getRoute(start, end);

        directions.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Response<DirectionsResponse> response, Retrofit retrofit) {
                String title = "Map Notification";

                if (response.body() == null) {
                    Log.e("onResponse","No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e("onResponse", "No routes found");
                    showMessage(title, "No driving route found");
                } else {
                    DirectionsRoute currentRoute = response.body().getRoutes().get(0);
                    int dur = currentRoute.getDuration();
                    if (dur > duration) {
                        showMessage(title, "Unusual traffic on your most common route for today");
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("onFailure", t.getMessage());
            }
        });
    }


    /* Finds the optimal route between two points
     * @params - start and end way points
     * @return - DirectionsRoute object that is the optimal route
     */
    public void findOptimalRoute(Waypoint start, Waypoint end) {
        MapboxDirections directions = getRoute(start, end);

        directions.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Response<DirectionsResponse> response, Retrofit retrofit) {
                String title = "Map Notification";
                int duration = 1;  //TODO temp

                if (response.body() == null) {
                    Log.e("onResponse","No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e("onResponse", "No routes found");
                    showMessage(title, "No driving route found");
                } else {
                    DirectionsRoute currentRoute = response.body().getRoutes().get(0);
                    List<RouteStep> steps = currentRoute.getSteps();
                    Log.i("onResponse", "Found route!");
                    routeSteps = steps;
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("onFailure", t.getMessage());
            }
        });
        if (routeSteps == null) {
            Log.i("after onResponse", "wtf its still null");
        } else {
            Log.i("after onResposne", "it is not null");
        }
    }


    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


    public void getDirections(List<RouteStep> steps) {
        for (RouteStep step : steps) {
            String s = step.getManeuver().getInstruction();
            Log.i("RouteStep", s);
        }
    }
}

