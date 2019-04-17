package com.maggie.smarthelmet;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

//extending Activity instead of AppCompatActivity gets rid of the action bar
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_settings);

        ArrayList<ListItem> settingsItems = new ArrayList<>();
        settingsItems.add(new ListItem(R.drawable.notifications_icon_light_mode,
                getString(R.string.str_setItem_notif_label),
                getString(R.string.str_setItem_notif_des)));
        settingsItems.add(new ListItem(R.drawable.settings_bluetooth_light_mode,
                getString(R.string.str_setItem_bt_label),
                getString(R.string.str_setItem_bt_des)));
        settingsItems.add(new ListItem(R.drawable.night_mode_icon_light_mode,
                getString(R.string.str_setItem_night_label),
                getString(R.string.str_setItem_night_des)));
        settingsItems.add(new ListItem(R.drawable.privacy_policy_icon_light_mode,
                getString(R.string.str_abtItem_abtTermsPriv_label),
                getString(R.string.str_abtItem_abtTermsPriv_des)));

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_settings);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView.Adapter mAdapter = new SettingsAdapter(settingsItems);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


    }
}