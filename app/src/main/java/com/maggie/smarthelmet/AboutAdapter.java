package com.maggie.smarthelmet;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.AboutViewHolder> {
    private ArrayList<ListItem> mList;

    public AboutAdapter(ArrayList<ListItem> items) {
        mList = items;
    }

    public static class AboutViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTextViewName;
        public TextView mTextViewDescription;
        public CardView mCardView;

        public AboutViewHolder(View view) {
            super(view);
            mImageView = view.findViewById(R.id.settingsImageView);
            mTextViewName = view.findViewById(R.id.nameTextView);
            mTextViewDescription = view.findViewById(R.id.descriptionTextView);
            mCardView = view.findViewById(R.id.aboutCardView);
        }
    }

    @NonNull
    @Override
    public AboutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_about,
                parent, false);
        return new AboutViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AboutViewHolder holder, int position) {
        ListItem listItem = mList.get(position);
        holder.mTextViewName.setText(listItem.getName());
        holder.mTextViewDescription.setText(listItem.getDescription());

        final String name = listItem.getName();

        //TODO: make Smart Helmet and Version items non-clickable (don't change colors when pressed)
        View.OnClickListener listener = (name.equals("Smart Helmet") ||
                name.equals("Version") || name.equals("Location data collection")) ? null : new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(name) {
                    case "Privacy Policy":
                        Intent intentPriv = new Intent(view.getContext(), PrivacyPolicyActivity.class);
                        view.getContext().startActivity(intentPriv);
                        break;
                    case "Clear application data":
                        final File file = view.getContext().getCacheDir();

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialogTheme)
                                .setTitle("Clear Cache")
                                .setMessage("Are you sure you want to clear the application's cache?")
                                .setCancelable(true)
                                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try {
                                            boolean cleared = clearCache(file);
                                        } catch (Exception e) {
                                            Log.i("clear cache", "******ERROR******: clear cache error: "+e);
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                        AlertDialog clearCacheAlert = builder.create();
                        clearCacheAlert.show();
                        break;

                    default:
                        Toast.makeText(view.getContext(), "Add functionality", Toast.LENGTH_SHORT).show();
                }
            }
        };
        holder.mCardView.setOnClickListener(listener);

        /*
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("in activity adapter: ", "item name is "+name);
                switch(name) {
                    case "Privacy Policy":
                        Intent intentPriv = new Intent(view.getContext(), PrivacyPolicyActivity.class);
                        view.getContext().startActivity(intentPriv);
                        break;
                    default:
                        Toast.makeText(view.getContext(), "hello there", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    private static boolean clearCache(File file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                boolean deleted = clearCache(new File(file, children[i]));
                if (!deleted) {
                    return false;
                }
            }
        } else if (file.isFile()) {
            return file.delete();
        }
        return true;
    }

}
