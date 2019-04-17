package com.maggie.smarthelmet;

public class MyBluetoothDevice extends BluetoothActivity {

    public android.bluetooth.BluetoothDevice getDevice() {
        if (myDevice == null) {
            return null;
        } else {
            return myDevice;
        }
    }
}
