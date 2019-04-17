package com.maggie.smarthelmet;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.BluetoothViewHolder> {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice myDevice;
    private ArrayList<ListItem> mList;
    private MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice();

    public BluetoothListAdapter(ArrayList<ListItem> items, BluetoothAdapter adapter) {
        mList = items;
        bluetoothAdapter = adapter;
        myDevice = null;
    }


    public static class BluetoothViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewName;
        public CardView mCardView;

        public BluetoothViewHolder(View view) {
            super(view);
            mTextViewName = view.findViewById(R.id.nameTextView);
            mCardView = view.findViewById(R.id.bluetoothCardView);
        }
    }

    @NonNull
    @Override
    public BluetoothViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_bluetooth,
                parent, false);
        return new BluetoothViewHolder(view);
    }

    @TargetApi(19)
    @Override
    public void onBindViewHolder(@NonNull BluetoothViewHolder holder, int position) {
        ListItem listItem = mList.get(position);
        holder.mTextViewName.setText(listItem.getName());

        final String name = listItem.getName();
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDevice = myBluetoothDevice.getDevice();
                bluetoothAdapter.cancelDiscovery();  //will slow down connection if still on

                switch(name) {
                    case "SmartHelmet":
                        if (myDevice != null) {
                            try {
                                myDevice.createBond();
                            } catch (Exception e) {
                                Log.e("BT List Adapter", "Could not create device bond: "+e);
                            }

                        } else {
                            Log.e("BT List Adapter", "the device is null");
                        }

                        break;
                    default:
                        Toast.makeText(view.getContext(), "Don't click this one :)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

}
