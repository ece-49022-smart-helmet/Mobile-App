package com.maggie.smarthelmet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {
    private ArrayList<ListItem> mList;

    public SettingsAdapter(ArrayList<ListItem> items) {
        mList = items;
    }

    public static class SettingsViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextViewName;
        public TextView mTextViewDescription;
        public CardView mCardView;

        public SettingsViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.settingsImageView);
            mTextViewName = view.findViewById(R.id.nameTextView);
            mTextViewDescription = view.findViewById(R.id.descriptionTextView);
            mCardView = view.findViewById(R.id.settingsCardView);
        }
    }



    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_settings,
                parent, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        ListItem listItem = mList.get(position);
        holder.mImageView.setImageResource(listItem.getImage());
        holder.mTextViewName.setText(listItem.getName());
        holder.mTextViewDescription.setText(listItem.getDescription());

        final String name = listItem.getName();
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(name) {
                    case "Notifications":
                        Intent intenNotif = new Intent(view.getContext(), NotificationsActivity.class);
                        view.getContext().startActivity(intenNotif);
                        break;
                    case "Bluetooth":
                        Intent intentBluetooth = new Intent(view.getContext(), BluetoothActivity.class);
                        view.getContext().startActivity(intentBluetooth);
                        break;
                    case "Night Mode":
                        Toast.makeText(view.getContext(), "night mode", Toast.LENGTH_SHORT).show();
                        break;
                    case "About, terms & privacy":
                        Intent intentAbt = new Intent(view.getContext(), AboutActivity.class);
                        view.getContext().startActivity(intentAbt);
                        break;
                    default:
                        Toast.makeText(view.getContext(), "hello there", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
