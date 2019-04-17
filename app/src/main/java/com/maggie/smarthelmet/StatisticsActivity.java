package com.maggie.smarthelmet;

import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.Waypoint;

import java.io.IOException;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private MappingActivity mappingActivity;
    private Geocoder geocoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        db = new DatabaseHelper(this);
        mappingActivity = new MappingActivity();
        geocoder = new Geocoder(this);
    }

    //Check Frequently traveled routes
    public void checkedFrequentRoutes() {
        Cursor cursor = db.sortDatabase();
        float duration = 30;  //TODO: put this column in the table

        while (cursor.moveToNext()) {
            Log.i("sorted", "day: "+cursor.getString(0)+", time: "+cursor.getString(1)+", count: "+cursor.getInt(5));
            Waypoint start = stringToWaypoint(cursor.getString(2));
            Waypoint end = stringToWaypoint(cursor.getString(4));

            DirectionsRoute route = mappingActivity.findOptimalRoute(start, end);
            if (route.getDuration() > duration) {
                //tell user
            }

        }
    }


    private void showMostTraveledRoute() {
        Cursor cursor = db.sortDatabase();

        //display top route
        cursor.moveToNext();
        Coordinates start = stringToCoord(cursor.getString(2));
        Coordinates end = stringToCoord(cursor.getString(4));
        String startAddr = coordToAddr(start.latitude, start.longitude);
        String endAddr = coordToAddr(end.latitude, end.longitude);
    }


    private Waypoint stringToWaypoint(String loc) {
        String[] arr = loc.split(", ");  //assume lat, lon format
        double lat = Double.valueOf(arr[0]);
        double lon = Double.valueOf(arr[1]);
        return new Waypoint(lat, lon);
    }


    private Coordinates stringToCoord(String loc) {
        String[] arr = loc.split(", ");  //assume lat, lon format
        double lat = Double.valueOf(arr[0]);
        double lon = Double.valueOf(arr[1]);

        return new Coordinates(lat, lon);
    }



    private String coordToAddr(double latitude, double longitude) {
        String address;
        List<Address> addrList;

        try {
            addrList = geocoder.getFromLocation(latitude, longitude, 1);  //only want to get first result
            address = addrList.get(0).getAddressLine(0);
        } catch (IOException e) {
            Log.e("coordToAddr", "Could not convert coordinates to an address");
            address = null;
        }
        return address;
    }



}
