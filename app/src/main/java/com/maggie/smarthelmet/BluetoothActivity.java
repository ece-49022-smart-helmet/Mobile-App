package com.maggie.smarthelmet;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import javax.xml.transform.Templates;


public class BluetoothActivity extends AppCompatActivity {

    private static final String DEVICE_NAME = "SmartHelmet";  //TEMP
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_TIME = 15000;  //60 seconds

    protected static  BluetoothDevice myDevice;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean btEnabled;
    private boolean alreadyPaired = false;
    private boolean scanning = false;

    private ArrayList<ListItem> bluetoothDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> nearbyDevices = new ArrayList<>();
    private int prevNearbyDevicesSize = 0;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    private ProgressBar progressBar;
    private TextView rescan_txt;
    private Button rescan_btn;


    @TargetApi(19)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //set UI variables
        progressBar = (ProgressBar) findViewById(R.id.scanningProgressBar);
        rescan_txt = (TextView) findViewById(R.id.deviceNotFound);
        rescan_btn = (Button) findViewById(R.id.btn_rescan);
        rescan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanForDevices();
            }
        });

        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();


        //Check that the device is Bluetooth capable
        if (bluetoothAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.AlertDialogTheme)
                    .setTitle("Bluetooth Not Supported")
                    .setMessage("Your phone does not support Bluetooth. Smart Helmet will not function as intended.")
                    .setCancelable(true);
            AlertDialog btNotSupported = builder.create();
            btNotSupported.show();
        }

        //Check that Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            btEnabled = false;
        } else {
            btEnabled = true;
        }

        //Check for devices that are already paired
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            String name = device.getName();
            if (name != null) {
                bluetoothDevices.add(new ListItem(name));
                Log.i("BluetoothActivity", "Bonded device: " + name + ", DEVICE name: " + DEVICE_NAME);
                if (name.equals(DEVICE_NAME)) {
                    myDevice = device;
                    alreadyPaired = true;
                }
            }

        }

        if (!alreadyPaired) {
            scanForDevices();  //scan for 1 minute and list devices as they show up
        }

        //Modify layout for Recycler View
        RecyclerView bondedDev = (RecyclerView) findViewById(R.id.bondedDevicesList);
        bondedDev.setHasFixedSize(true);
        adapter = new BluetoothListAdapter(bluetoothDevices, bluetoothAdapter);
        bondedDev.setLayoutManager(mLayoutManager);
        bondedDev.setAdapter(adapter);

    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {  //Device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                if (deviceName != null) {
                    if (!nearbyDevices.contains(device)) {
                        nearbyDevices.add(device);
                    }
                    //nearbyDevices.append(device.hashCode(), device);
                    if (deviceName.equals(DEVICE_NAME) && !alreadyPaired) {
                        myDevice = device;
                        Toast.makeText(getApplicationContext(), "Device found!", Toast.LENGTH_LONG).show();
                        bluetoothDevices.add(new ListItem(deviceName));
                        adapter.notifyDataSetChanged();
                    } else {
                        if (nearbyDevices.size() != prevNearbyDevicesSize) {  //if a new device is found
                            bluetoothDevices.add(new ListItem(deviceName));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                Log.i("Broadcast Receiver", "New device: "+deviceName+", MAC address: "+deviceAddress);
            }
        }
    };


    @TargetApi(19)
    private final BroadcastReceiver conReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                try {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234);
                    //the pin in case you need to accept for an specific pin
                    Log.d("Conncet Receiver", "Start Auto Pairing. PIN = " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",1234));
                    byte[] pinBytes;
                    pinBytes = (""+pin).getBytes("UTF-8");
                    device.setPin(pinBytes);
                } catch (Exception e) {
                    Log.e("Connect Receiver", "Error occurs when trying to auto pair");
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable stopScan = new Runnable() {
        @Override
        public void run() {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.e("Stop Scan", "Could not unregister reciever; "+e);
            }

            bluetoothAdapter.cancelDiscovery();
            progressBar.setVisibility(View.INVISIBLE);
            scanning = false;
            if (myDevice == null || nearbyDevices.size() == 0) {
                rescan_txt.setVisibility(View.VISIBLE);
                rescan_btn.setVisibility(View.VISIBLE);
            } else {
                rescan_btn.setVisibility(View.INVISIBLE);
                rescan_txt.setVisibility(View.INVISIBLE);
            }
            Log.i("scanForDevices", "Done scanning.");
        }
    };


    private void scanForDevices() {
        if (btEnabled) {
            //Register for broadcast when a device is discovered
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);

            //create handler to run scan for SCAN_TIME
            Handler handler = new Handler();
            handler.postDelayed(stopScan, SCAN_TIME);
            Log.i("scanForDevices", "Begin scanning...");

            bluetoothAdapter.startDiscovery();
            progressBar.setVisibility(View.VISIBLE);
            scanning = true;
        } else {
            bluetoothAdapter.cancelDiscovery();
            scanning = false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.AlertDialogTheme)
                    .setTitle("Bluetooth Not Enabled")
                    .setMessage("Smart Helmet requires the use of Bluetooth")
                    .setCancelable(true);
            AlertDialog btNotEnabled = builder.create();
            btNotEnabled.show();

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.e("Unregister Receiver", "Could not unregister reciever; "+e);
        }
        try {
            unregisterReceiver(conReceiver);
        } catch (Exception e) {
            Log.e("Unregister Receiver", "Could not unregister conReciever; "+e);
        }
        bluetoothAdapter.cancelDiscovery();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.e("Unregister Receiver", "Could not unregister reciever; "+e);
        }
        try {
            unregisterReceiver(conReceiver);
        } catch (Exception e) {
            Log.e("Unregister Receiver", "Could not unregister conReciever; "+e);
        }
        bluetoothAdapter.cancelDiscovery();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            Log.e("Unregister Receiver", "Could not unregister reciever; "+e);
        }
        try {
            unregisterReceiver(conReceiver);
        } catch (Exception e) {
            Log.e("Unregister Receiver", "Could not unregister conReciever; "+e);
        }
        bluetoothAdapter.cancelDiscovery();
    }
}

