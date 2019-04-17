package com.maggie.smarthelmet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;


import java.util.List;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, PermissionsListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int USER_BUILD_LEVEL = Build.VERSION.SDK_INT;
    private static final int TARGET_BUILD_LEVEL = Build.VERSION_CODES.O;

    private BluetoothAdapter bluetoothAdapter;
    private boolean isBleEnabled;

    private MapView mMapView;
    private MapboxMap mMapboxMap;
    private PermissionsManager permissionsManager;

    private LocationActivity locationActivity;
    private int SAMPLE_RATE = 60000;  //initialize to 1 minute
    private static final int BASE_THRESHOLD = 15;
    private static final int MAX_IDLE_TIME = 120000;  //2 minutes
    private Handler handler = new Handler();
    private boolean tripStarted = false;
    private boolean tripComplete = false;
    private boolean dataStored = false;
    private int belowThresholdCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.Mapbox_access_token));
        setContentView(R.layout.activity_main);


        //navigation drawer code
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Bluetooth Variables
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        //request location permissions
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1002);


        //check notification access permissions
        int notificationPolicyAccess = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        int notificationListenerAccess = ContextCompat.checkSelfPermission(this, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE);
        if (USER_BUILD_LEVEL >= TARGET_BUILD_LEVEL) {
            if (notificationPolicyAccess != 0) {
                Intent intentNotif = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intentNotif);
            }
            if (notificationListenerAccess != -1) {  //TODO: figure out how to actually check this
                Intent notificationIntent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(notificationIntent);
            }
        }


        //Mapbox code
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        permissionsManager = new PermissionsManager(this);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                MainActivity.this.mMapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        //TODO: do things in here
                    }
                });
            }
        });


        //Button code for getting current location
        Button btn_user_location = (Button) findViewById(R.id.user_location_btn);
        btn_user_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/mapbox/cjerxnqt3cgvp2rmyuxbeqme7"),
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                enableLocationComponent(style);
                            }
                        });

            }
        });


        handler =  new Handler();
        locationActivity = new LocationActivity(MainActivity.this);
        //locationActivity.startTracking();
        //handler.post(trackLocation);


        //temp button code for tracking user's location
        Button temp_loc = (Button) findViewById(R.id.temp_location);
        temp_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Blank_Class.class);
                view.getContext().startActivity(intent);
            }
        });

    }


    private Runnable trackLocation = new Runnable() {
        @Override
        public void run() {
            double speed = locationActivity.myGetSpeed();
            if (speed >= BASE_THRESHOLD) {
                tripStarted = true;
                SAMPLE_RATE = 30000;
            }
            if (tripStarted) {
                if (System.currentTimeMillis() - locationActivity.time > MAX_IDLE_TIME) {
                    tripComplete = true;
                    tripStarted = false;
                } else if (speed < BASE_THRESHOLD) {
                    belowThresholdCount++;
                    if (belowThresholdCount >= 4) {
                        tripComplete = true;
                        tripStarted = false;
                    }
                } else {
                    belowThresholdCount = 0;
                    //call Mapbox API
                }
            }
            if (tripComplete) {
                handler.removeCallbacks(this);
                //call handleData();
                dataStored = true;
            } else {
                handler.postDelayed(this, SAMPLE_RATE);
            }
        }
    };


    private boolean appInstalled(String app) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(app, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("*******CHECK FOR APP: ", "App: "+app+" NOT installed on this device");
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.drawer_navigation) {
            //Check if they have Google Maps or some other maps app installed
            if (appInstalled("com.google.android.apps.maps")) {
                Toast.makeText(this, "Google Maps is installed...YAY!", Toast.LENGTH_LONG).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                        .setTitle("Google Maps not Installed")
                        .setMessage("Smart Helmet requires the use of Google Maps in order for the navigation features to work properly");
                AlertDialog mapsNotInstalled = builder.create();
                mapsNotInstalled.show();
            }

        } else if (id == R.id.drawer_statistics) {
            intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);

        } else if (id == R.id.drawer_settings) {
            intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mMapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);  //Activate with options
            locationComponent.setLocationComponentEnabled(true);  //Enable to make component visible
            locationComponent.setCameraMode(CameraMode.TRACKING);  //Set camera mode
            locationComponent.setRenderMode(RenderMode.COMPASS);  //Set render mode
        } else {
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.location_permission_explain, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mMapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    @SuppressWarnings( {"MissingPermission"} )
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //make sure that Bluetooth is enabled
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {  //if bluetooth is not enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        mMapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        //locationActivity.stopTracking();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
