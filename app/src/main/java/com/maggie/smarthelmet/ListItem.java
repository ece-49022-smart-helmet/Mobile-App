package com.maggie.smarthelmet;

public class ListItem {
    private int mImage;
    private String mName;
    private String mDescription;


    //constructor for settings items
    public ListItem(int image, String name, String description) {
        mImage = image;
        mName = name;
        mDescription = description;
    }

    public ListItem(String name, String description) {
        mName = name;
        mDescription = description;
        mImage = 0;  //not using with this constructor
    }

    public ListItem(String name) {
        mName = name;
    }

    //getters
    public int getImage() {
        return mImage;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }
}