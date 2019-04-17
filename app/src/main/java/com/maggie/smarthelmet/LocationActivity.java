package com.maggie.smarthelmet;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.Math;


public class LocationActivity extends AppCompatActivity implements LocationListener {
    //private final static long MIN_DISTANCE = 10;  //min distance for updates -- 10 meters
    //private final static long MIN_TIME = 60000;  //min time for updates  -- 60 seconds
    private final static long MIN_DISTANCE = 1;  //using for testing purposes
    private final static long MIN_TIME = 10000;  //using for testing purposed
    private final Context mContext;

    private boolean GPSEnabled = false;
    private boolean networkEnabled = false;
    private boolean canGetLocation = false;
    private boolean ignoreFirst = true;

    private LocationManager locationManager;
    private Location location = null;  //initialize to null
    private double lat;
    private double lon;

    private double distance;
    public double time;
    private double currTime;
    private Location currLocation;
    private Location prevLocation;
    private DatabaseHelper db;


    LocationActivity(Context context) {
        mContext = context;
    }


    public Location getLocation() {
        db = new DatabaseHelper(mContext);

        //recheck if location permissions have changed since last got location
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != 0) {  //0 = permission granted
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 1000); }
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != 0) {  //0 = permission granted
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); }

        locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e("getLocation", "Location Manager is null :(");
            return null;
        }

        GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (GPSEnabled && networkEnabled) {
            canGetLocation = true;
        }

        if (networkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                //Log.i("LocationInfo", "Latitude: "+lat+", Longitude: "+lon);
            }
        }
        if (GPSEnabled && !networkEnabled) {  //could not get location from network provider
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                //Log.i("LocationInfo", "Latitude: "+lat+", Longitude: "+lon);
            }
        }

        if (location != null) {
            time = System.currentTimeMillis();  //initialize time;
            currTime = time;
            prevLocation = location;  //initialize previous location
            calculateSpeed();
        }

        return location;
    }


    private double calculateDistance(Location loc1, Location loc2) {
        int earthRadius_km = 6371;
        double degToRadConvert = Math.PI / 180;

        double lat1_rad = loc1.getLatitude() * degToRadConvert;
        double lat2_rad = loc2.getLatitude() * degToRadConvert;
        double latDiff_rad = lat2_rad - lat1_rad;
        double lonDiff_rad = (loc2.getLongitude() - loc1.getLongitude()) * degToRadConvert;

        double A = Math.sin(latDiff_rad/2) * Math.sin(latDiff_rad/2) + Math.cos(lat1_rad) *
                Math.cos(lat2_rad) * Math.sin(lonDiff_rad/2) * Math.sin(lonDiff_rad/2);
        double C = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1-A));

        double dist = C * earthRadius_km;
        distance = dist;
        return dist;
    }


    public void calculateSpeed() {
        double speed;
        double distance;
        if (prevLocation == location) {
            location.setSpeed(0);
            return;
        }
        distance = calculateDistance(prevLocation, location);
        speed =  (distance / (currTime - time)) * 1000;  //km/s
        speed *= 1000;  //TEMP -- put in m/s for debug/demo purposes
        speed *= 10;  //to surpass threshold
        location.setSpeed((float)speed);
    }


    public double myGetSpeed() {
        if (location == null) {
            return 0;
        }
        //Toast.makeText(mContext,"Speed: "+location.getSpeed(), Toast.LENGTH_LONG).show();
        return location.getSpeed();
    }


    public double getLatitude() {
        return location.getLatitude();
    }


    public double getLongitude() {
        return location.getLongitude();
    }


    public void startTracking() {
        getLocation();
    }


    public void stopTracking() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            try {
                locationManager.removeUpdates(this);
            } catch (Exception e) {
                Log.e("stopTracking", "Only requested updates from one provider");
            }
        }
    }


    @Override
    public void onLocationChanged(Location newLocation) {
        location = newLocation;
        currTime = System.currentTimeMillis();
        if (ignoreFirst) {
            ignoreFirst = false;
            return;
        }
        calculateSpeed();

        //Toast.makeText(mContext, "lat: "+location.getLatitude()+", long: "+location.getLongitude()+", speed: "+location.getSpeed(), Toast.LENGTH_LONG).show();
        //Log.i("LocationInfo", "Latitude: "+location.getLatitude()+", Longitude: "+location.getLongitude()+", distance: "+distance+", speed: "+location.getSpeed()+", currTime: "+currTime+", time: "+time);

        prevLocation = location;
        time = currTime;
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }


    @Override
    public void onProviderEnabled(String s) {

    }


    @Override
    public void onProviderDisabled(String s) {

    }
}
