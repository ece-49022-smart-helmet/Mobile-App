package com.maggie.smarthelmet;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;


public class ConnectSocket extends Thread {
    private BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  //known SPP UUID
    private InputStream inputStream;
    private OutputStream outputStream;


    public ConnectSocket(BluetoothDevice device) {
        BluetoothSocket tempSocket = null;  //will later be assigned to mSocket
        mDevice = device;

        try {
            if (mDevice != null) {
                tempSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
        } catch (IOException e) {
            Log.e("Connect Thread", "Socket create failed: "+e);
        }

        mSocket = tempSocket;
    }


    public void run() {
        try {
            mSocket.connect();
        } catch (IOException connectException) {
            Log.e("Run", "Socket connection failed: "+connectException);

            try {
                Log.e("Run", "trying fallback solution");
                mSocket = (BluetoothSocket) mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mDevice, 1);
                mSocket.connect();

            } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException fallbackException) {
                Log.e("fallback", "fallback connection failed");

                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e("Run", "Socket close failed: "+closeException);
                }
                return;
            }
        }

        Log.i("Run", "Socket connection successful");
        try {
            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            Log.e("Socket IO", "Unable to get I/O stream from socket");
        }
    }


    public void sendData(byte[] data) {
        if (mSocket == null) {
            return;
        }
        byte[] bytes = data;
        try {
            outputStream.write(data);
        } catch (IOException e) {
            Log.e("sendDataRFCOMM", "Write to output stream failed");
        }

    }


    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException closeException) {
            Log.e("Run", "Socket close failed: "+closeException);
        }
    }

}