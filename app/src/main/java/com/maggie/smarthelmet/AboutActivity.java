package com.maggie.smarthelmet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String str_aboutItem_locDat;

        PackageManager packageManager = getPackageManager();
        int hasCoarseLocPermission = packageManager.checkPermission(Manifest.permission.
                ACCESS_COARSE_LOCATION, getPackageName());
        int hasFineLocPermission = packageManager.checkPermission(Manifest.permission.
                ACCESS_FINE_LOCATION, getPackageName());

        if (hasCoarseLocPermission == PackageManager.PERMISSION_GRANTED && hasFineLocPermission == PackageManager.PERMISSION_GRANTED) {
            str_aboutItem_locDat = "Enabled";
        } else {
            str_aboutItem_locDat = "Not Enabled";
        }

        ArrayList<ListItem> aboutItems = new ArrayList<>();
        aboutItems.add(new ListItem(getString(R.string.app_name), getString(R.string.str_abtItem_appName_des)));
        aboutItems.add(new ListItem(getString(R.string.str_abtItem_v_label), getString(R.string.str_abtItem_v_des)));
        aboutItems.add(new ListItem(getString(R.string.str_abtItem_locDat_label), str_aboutItem_locDat));  //change this later to be dynamic
        aboutItems.add(new ListItem(getString(R.string.str_abtItem_terms_label), getString(R.string.str_abtItem_terms_des)));
        aboutItems.add(new ListItem(getString(R.string.str_abtItem_priv_label), getString(R.string.str_abtItem_priv_des)));
        aboutItems.add(new ListItem(getString(R.string.str_abtItem_clear_label), getString(R.string.str_abtItem_clear_des)));

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_about);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        RecyclerView.Adapter mAdapter = new AboutAdapter(aboutItems);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }
}
