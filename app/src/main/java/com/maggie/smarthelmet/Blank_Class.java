package com.maggie.smarthelmet;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.directions.service.models.Waypoint;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Blank_Class extends AppCompatActivity {

    private LocationActivity locationActivity;
    //private int SAMPLE_RATE = 60000;  //initialize to 1 minute
    //private static final int BASE_THRESHOLD = 24;  //24 km/h = 15 mph
    //private static final int MAX_IDLE_TIME = 120000;  //2 minutes
    private int SAMPLE_RATE = 30000;  //testing
    private static final int BASE_THRESHOLD = 8;  //testing
    private static final int MAX_IDLE_TIME = 60000;  //testing
    private static final int COORDINATES_INTERVAL = 60000;
    private Handler handler = new Handler();
    private Handler handler2 = new Handler();
    private boolean tripStarted = false;
    private boolean tripComplete = false;
    private boolean dataStored = false;
    private int belowThresholdCount = 0;

    //database variables
    private String startLocation;
    private String endLocation;
    private String startTime;
    private String endTime;

    private DatabaseHelper db;
    private Context mContext;
    private Geocoder geocoder;
    private MappingActivity mappingActivity;
    private List<Waypoint> waypointList;

    private List<Coordinates> coordinatesList;
    private Coordinates startCoordinates;
    private Coordinates endCoordinates;
    private Coordinates midCoordinates;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.black_template);

        locationActivity = new LocationActivity(Blank_Class.this);
        db = new DatabaseHelper(this);
        geocoder = new Geocoder(this);
        mappingActivity = new MappingActivity();
        mContext = this;

        Button temp_loc = (Button) findViewById(R.id.track_location);
        temp_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationActivity == null) {
                    locationActivity = new LocationActivity(mContext);
                }
                locationActivity.startTracking();
                handler.post(trackLocation);
                handler2.post(getCoordinates);
            }
        });


        Button stop = (Button) findViewById(R.id.stop_tracking);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationActivity != null) {
                    locationActivity.stopTracking();
                    locationActivity = null;
                }
            }
        });


        //database buttons
        Button addTempData = (Button) findViewById(R.id.add_dummy_entry);
        addTempData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDummyData();
            }
        });


        //location buttons
        Button duplicate = (Button) findViewById(R.id.add_duplicate);
        duplicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForDuplicate_tempData("Monday", "06:45:18");
            }
        });


        Button viewDatabase = (Button) findViewById(R.id.view_database);
        viewDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = db.getData();
                if (cursor.getCount() == 0) {  //no data available
                    showMessage("Error", "No data found");
                    return;
                }
                StringBuffer buffer = new StringBuffer();
                while (cursor.moveToNext()) {
                    buffer.append("Day: "+cursor.getString(0)+"\n");
                    buffer.append("Start time: "+cursor.getString(1)+"\n");
                    buffer.append("Start location: "+cursor.getString(2)+"\n");
                    buffer.append("Middle location: "+cursor.getString(3)+"\n");
                    buffer.append("End location: "+cursor.getString(4)+"\n");
                    buffer.append("Times taken: "+cursor.getString(5)+"\n\n");  //puts the count first?
                }
                showMessage("Data", buffer.toString());

            }
        });

        Button clearDatabase = (Button) findViewById(R.id.clear_database);
        clearDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.clearDatabase();
            }
        });

    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }



    private Runnable getCoordinates = new Runnable() {
        @Override
        public void run() {
            if (tripStarted) {
                coordinatesList.add(new Coordinates(locationActivity.getLatitude(), locationActivity.getLatitude()));
                handler2.postDelayed(this, COORDINATES_INTERVAL);
            } else if (tripComplete) {
                handler2.removeCallbacks(this);
            }
        }
    };


    private Runnable trackLocation = new Runnable() {
        double speed;
        String startAddr;
        String endAddr;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        @Override
        public void run() {
            speed = locationActivity.myGetSpeed();
            if (speed >= BASE_THRESHOLD && !tripStarted) {
                tripStarted = true;
                SAMPLE_RATE = 5000;  //increase sampling rate

                startTime = format.format(Calendar.getInstance().getTime());
                startCoordinates = new Coordinates(locationActivity.getLatitude(), locationActivity.getLongitude());
                startLocation = String.valueOf(locationActivity.getLongitude())+", "+String.valueOf(locationActivity.getLongitude());
                //startAddr = coordToAddr(locationActivity.getLatitude(), locationActivity.getLongitude());
                /*
                startLocation = (startAddr != null) ? startAddr :
                        String.valueOf(locationActivity.getLongitude())+", "+String.valueOf(locationActivity.getLongitude());
                */
                Toast.makeText(Blank_Class.this, "Begin Trip!", Toast.LENGTH_LONG).show();
                Log.i("Handler", "BEGIN TRIP");
            }
            if (tripStarted) {
                db.addRouteEntry("Temp", format.format(locationActivity.time), String.valueOf(locationActivity.getLatitude()), String.valueOf(locationActivity.getLongitude()), String.valueOf(speed), 18);
                if (System.currentTimeMillis() - locationActivity.time > MAX_IDLE_TIME) {
                    tripComplete = true;
                    tripStarted = false;
                } else if (speed < BASE_THRESHOLD) {
                    belowThresholdCount++;
                    if (belowThresholdCount >= 10) {
                        tripComplete = true;
                        tripStarted = false;
                    }
                } else {
                    belowThresholdCount = 0;
                }
            }
            if (tripComplete) {
                endTime = format.format(Calendar.getInstance().getTime());
                //endAddr = coordToAddr(locationActivity.getLatitude(), locationActivity.getLongitude());
                endCoordinates = new Coordinates(locationActivity.getLatitude(), locationActivity.getLongitude());
                endLocation = String.valueOf(locationActivity.getLongitude())+", "+String.valueOf(locationActivity.getLongitude());
                /*
                endLocation = (endAddr != null) ? endAddr :
                        String.valueOf(locationActivity.getLongitude())+", "+String.valueOf(locationActivity.getLongitude());
                */
                db.addRouteEntry("END TRIP", format.format(locationActivity.time), String.valueOf(locationActivity.getLatitude()), String.valueOf(locationActivity.getLongitude()), String.valueOf(speed), 18);
                Toast.makeText(Blank_Class.this, "End Trip!", Toast.LENGTH_LONG).show();
                Log.i("Handler", "END TRIP");
                //db.addRouteEntry(startTime, endTime, startLocation, endLocation, 99);
                handler.removeCallbacks(this);
                handleData(startTime, startLocation, endLocation);
                dataStored = true;
            } else {
                handler.postDelayed(this, SAMPLE_RATE);
                locationActivity.stopTracking();
            }
        }
    };


    private void handleData(String startTime, String startLoc, String endLoc) {
        tripStarted = false;
        tripComplete = false;
        Toast.makeText(mContext, "in HandleData", Toast.LENGTH_LONG).show();

        String day = dayOfWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        int mid = coordinatesList.size() / 2;
        startCoordinates.setWaypoint();
        endCoordinates.setWaypoint();
        midCoordinates = coordinatesList.get(mid);
        midCoordinates.setWaypoint();
        String midLoc = String.valueOf(midCoordinates.latitude)+", "+String.valueOf(midCoordinates.longitude);

        if (!isDuplicate(day, startTime, startLoc, endLoc)) {
            db.addRouteEntry(day, startTime, startLoc, midLoc, endLoc, 1);
        }
        dataStored = true;
    }


    private List<String> timeInterval(String time) {
        String[] timeArray = time.split(":");
        int HH = Integer.parseInt(timeArray[0]);
        int mm = Integer.parseInt(timeArray[1]);
        int ss = Integer.parseInt(timeArray[2]);
        int HH_lower = (HH - 1 > 0) ? HH - 1 : HH;
        int HH_upper = (HH + 1 < 24) ? HH + 1 : HH;

        String formatter = String.format("%%0%dd", 2);
        String lower = String.format(formatter, HH_lower)+":"+String.format(formatter, mm)+":"+String.format(formatter, ss);
        String upper = String.format(formatter, HH_upper)+":"+String.format(formatter, mm)+":"+String.format(formatter, ss);

        return  Arrays.asList(lower, upper);
    }


    private Waypoint stringToWaypoint(String loc) {
        String[] arr = loc.split(", ");  //assume lat, lon format
        double lat = Double.valueOf(arr[0]);
        double lon = Double.valueOf(arr[1]);
        return new Waypoint(lat, lon);
    }


    private boolean isDuplicate(String day, String time, String startLoc, String endLoc) {
        boolean alreadyInTable = false;
        List<String> timeBounds = timeInterval(time);
        Cursor cursor = db.getEntriesByDayAndTime(day, timeBounds.get(0), timeBounds.get(1));

        while (cursor.moveToNext()) {
            if (cursor.getString(2) == startLoc && cursor.getString(4) == endLoc) {
                alreadyInTable = mappingActivity.compareRoutes(stringToWaypoint(cursor.getString(2)), stringToWaypoint(cursor.getString(4)), stringToWaypoint(cursor.getString(3)), midCoordinates.getWaypoint());
                db.updateEntryCount(day, time, startLoc, cursor.getString(3), endLoc, (cursor.getInt(5)+1));
            }
        }
        return alreadyInTable;
    }


    public String coordToAddr(double latitude, double longitude) {
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
                dayStr = "Sunday";
                break;
            }
        }
        return dayStr;
    }





    /* * * * * * * * * * * * * * * * * * * * * * DUMMY DATA * * * * * * * * * * * * * * * * * * * * * * * * * */

    public void addDummyData() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        db.addRouteEntry("Sunday", "09:12:18", "TempStart_sunday", "TempMid_sunday", "TempEnd_sunday", 1);
        db.addRouteEntry("Saturday", "23:14:31", "TempStart_saturday", "TempMid_saturday", "TempEnd_saturday", 8);
        db.addRouteEntry("Monday", "07:18:27", "TempStart_monday", "TempMid_monday", "TempEnd_monday", 1);
        db.addRouteEntry("Sunday", "12:39:52", "TempStart_sunday2", "TempMid_sunday2", "TempEnd_sunday2", 2);
        db.addRouteEntry("Thursday", "17:45:41", "TempStart_thursday", "TempMid_thursday", "TempEnd_thursday", 1);
        db.addRouteEntry("Tuesday","21:57:12", "TempStart_tuesday", "TempMid_tuesday", "TempEnd_tuesday", 3);
        db.addRouteEntry("Friday", "13:23:42", "TempStart_friday", "TempMid_friday", "TempEnd_friday", 5);
        db.addRouteEntry("Monday", "08:45:19", "TempStart_monday", "TempMid_monday", "TempEnd_monday", 1);

        return;
    }


    public boolean checkForDuplicate_tempData(String day, String time) {
        boolean successfulUpdate = false;
        List<String> timeBounds = timeInterval(time);
        Cursor cursor = db.getEntriesByDayAndTime(day, timeBounds.get(0), timeBounds.get(1));

        if (cursor.getCount() != 1) {
            Log.e("isDuplicate", "Two entries with similar data");
            return false;
        }
        while (cursor.moveToNext()) {
            successfulUpdate = db.updateEntryCount(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), (cursor.getInt(5)+1));
        }

        return successfulUpdate;
    }


}
