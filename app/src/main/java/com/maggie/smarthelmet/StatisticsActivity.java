package com.maggie.smarthelmet;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.Waypoint;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private MappingActivity mappingActivity;
    private Geocoder geocoder;
    private int currentDay;
    //private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        db = new DatabaseHelper(this);
        mappingActivity = new MappingActivity(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        showMostTraveledRoute();
        checkedFrequentRoutes();


        Button displayByCount_btn = (Button) findViewById(R.id.display_database_count);
        displayByCount_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = db.sortDatabaseByCount();
                db.showDatabaseAsAlertDialog(cursor);
            }
        });

        Button displayDatabase_btn = (Button) findViewById(R.id.display_database_weekday);
        displayDatabase_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = db.sortDatabaseByDayAndCount(dayOfWeek(currentDay));
                db.showDatabaseAsAlertDialog(cursor);
            }
        });
    }


    private void setDisplayStat(Cursor cursor, TextView tvFrom, TextView tvTo) {
        String dispFrom;
        String dispTo;

        if (cursor.getCount() == 0) {
            dispFrom = "";
            dispTo = "";
        } else {
            cursor.moveToNext();
            Coordinates start = stringToCoord(cursor.getString(2));
            Coordinates end = stringToCoord(cursor.getString(4));
            String startAddr = coordToAddr(start.latitude, start.longitude);
            String endAddr = coordToAddr(end.latitude, end.longitude);

            if (startAddr == null || endAddr == null) {
                dispFrom = "From: "+cursor.getString(2);
                dispTo = "To: "+cursor.getString(4);
                //disp = "From: "+cursor.getString(2)+" to: "+cursor.getString(4);
            } else {
                dispFrom = "From: "+startAddr;
                dispTo = "To: "+endAddr;
                //disp = "from: " +startAddr+" to: "+endAddr;
            }
        }
        tvFrom.setText(dispFrom);
        tvTo.setText(dispTo);
    }

    //Check Frequently traveled routes
    public void checkedFrequentRoutes() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        String dayStr = dayOfWeek(day);
        Cursor cursor = db.sortDatabaseByDayAndCount(dayStr);
        TextView mostTraveledFrom_weekday = (TextView) findViewById(R.id.most_traveledFrom_route_weekday);
        TextView mostTraveledTo_weekday = (TextView) findViewById(R.id.most_traveledTo_route_weekday);

        setDisplayStat(cursor, mostTraveledFrom_weekday, mostTraveledTo_weekday);
        int duration = 30;  //TODO: put this column in the table

        //cursor.moveToPosition(-1);
        cursor.moveToPosition(0);
        Waypoint start = stringToWaypoint(cursor.getString(2));
        Waypoint end = stringToWaypoint(cursor.getString(4));

        mappingActivity.findOptimalRoute(start, end, duration);

        /*
        while (cursor.moveToNext()) {
            Log.i("sorted", "day: "+cursor.getString(0)+", time: "+cursor.getString(1)+", count: "+cursor.getInt(5));
            Waypoint start = stringToWaypoint(cursor.getString(2));
            Waypoint end = stringToWaypoint(cursor.getString(4));

            mappingActivity.findOptimalRoute(start, end, true);
        }
        */
    }


    private void showMostTraveledRoute() {
        TextView mostTraveledFromRoute = (TextView) findViewById(R.id.most_traveledFrom_route_all);
        TextView mostTraveledToRoute = (TextView) findViewById(R.id.most_traveledTo_route_all);
        Cursor cursor = db.sortDatabaseByCount();

        setDisplayStat(cursor, mostTraveledFromRoute, mostTraveledToRoute);
    }


    private Waypoint stringToWaypoint(String loc) {
        String[] arr = loc.split(",");  //assume lat,lon format
        double lat = Double.valueOf(arr[0]);
        double lon = Double.valueOf(arr[1]);
        return new Waypoint(lon, lat);
    }


    private Coordinates stringToCoord(String loc) {
        String[] arr = loc.split(",");  //assume lat,lon format
        double lat = Double.valueOf(arr[0]);
        double lon = Double.valueOf(arr[1]);

        return new Coordinates(lat, lon, getApplicationContext());
    }



    private String coordToAddr(double latitude, double longitude) {
        String address;
        List<Address> addrList;
        Log.i("help me", "lat: "+latitude+", long: "+longitude);

        try {
            addrList = geocoder.getFromLocation(latitude, longitude, 1);  //only want to get first result
            address = addrList.get(0).getAddressLine(0);
        } catch (IOException e) {
            Log.e("coordToAddr", "Could not convert coordinates to an address: "+e);
            address = null;
        }
        return address;
    }


    private String dayOfWeek(int day) {
        String dayStr = "";
        switch(day) {
            case Calendar.SUNDAY: {
                dayStr = "Sunday";
                break;
            }
            case Calendar.MONDAY: {
                dayStr = "Monday";
                break;
            }
            case Calendar.TUESDAY: {
                dayStr = "Tuesday";
                break;
            }
            case Calendar.WEDNESDAY: {
                dayStr = "Wednesday";
                break;
            }
            case Calendar.THURSDAY: {
                dayStr = "Thursday";
                break;
            }
            case Calendar.FRIDAY: {
                dayStr = "Friday";
                break;
            }
            case Calendar.SATURDAY: {
                dayStr = "Saturday";
                break;
            }
        }
        return dayStr;
    }



}
