package com.maggie.smarthelmet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;

    private static final String DATABASE_NAME = "trip_information.db";
    private static final String TABLE_NAME = "Recent_Trips";

    private static final String col0 = "DAY_OF_WEEK";
    private static final String col1 = "START_TIME";
    private static final String col2 = "START_LOCATION";
    private static final String col3 = "MID_LOCATION";
    private static final String col4 = "END_LOCATOIN";
    private static final String col5 = "NUM_TIMES_TAKEN";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }


    public boolean addRouteEntry(String day, String startTime, String startLoc, String midLoc, String endLoc, int numTimes) {
        SQLiteDatabase database = this.getWritableDatabase();
        onCreate(database);  //create the table if it was recently deleted

        ContentValues contentValues = new ContentValues();
        contentValues.put(col0, day);
        contentValues.put(col1, startTime);
        contentValues.put(col2, startLoc);
        contentValues.put(col3, midLoc);
        contentValues.put(col4, endLoc);
        contentValues.put(col5, numTimes);

        long entryResult = database.insert(TABLE_NAME, null, contentValues);  //returns -1 if there was an error

        return entryResult != -1;
    }

    public Cursor getData() {
        SQLiteDatabase database = this.getWritableDatabase();
        onCreate(database);  //create the table if it was recently deleted

        return database.rawQuery("SELECT * FROM "+TABLE_NAME, null);
    }

    public Cursor getEntriesByDayAndTime(String day, String timeLower, String timeUpper) {
        SQLiteDatabase database = this.getWritableDatabase();
        onCreate(database);  //create the table if it was recently deleted

        return database.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE ("+col0+" = '"+day+"') AND ("+col1+" > '"+timeLower+"') AND ("+col1+" < '"+timeUpper+"')", null);
    }


    public boolean updateEntryCount(String day, String time, String startLoc, String midLoc, String endLoc, int newCount) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col5, newCount);

        String[] whereArgs = {day, time, startLoc, midLoc, endLoc};
        long updateResult = database.update(TABLE_NAME, contentValues, col0+" = ? AND "+col1+" = ? AND "+col2+" = ? AND "+col3+" = ? AND "+col4+" = ?", whereArgs);

        return updateResult != -1;
    }


    public Cursor sortDatabaseByCount() {
        SQLiteDatabase database = this.getWritableDatabase();
        onCreate(database);  //create the table if it was recently deleted

        return database.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+col5+ " DESC ", null);
        /*
        String[] str = {col5};
        Cursor cursor = database.query(TABLE_NAME, str, null, null, null, null, col5+" DESC");
        return cursor;
        */
    }


    public Cursor sortDatabaseByDayAndCount(String day) {
        SQLiteDatabase database = this.getWritableDatabase();
        onCreate(database);

        return database.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+col0+" = "+"'"+day+"'"+" ORDER BY "+col5+" DESC", null);
    }


    public void clearDatabase() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
    }


    public void showDatabaseAsAlertDialog() {
        Cursor cursor = this.getData();
        if (cursor.getCount() == 0) {  //no data available
            showMessage("Error", "No data found");
            return;
        }
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            buffer.append("Day: "+cursor.getString(0)+"\n");
            buffer.append("Start time: "+cursor.getString(1)+"\n");
            buffer.append("Start location: "+cursor.getString(2)+"\n");
            buffer.append("Middle location: "+cursor.getString(3)+"\n");
            buffer.append("End location: "+cursor.getString(4)+"\n");
            buffer.append("Times taken: "+cursor.getString(5)+"\n\n");
        }
        showMessage("Data", buffer.toString());

    }

    public void showDatabaseAsAlertDialog(Cursor cursor) {
        if (cursor.getCount() == 0) {  //no data available
            showMessage("Error", "No data found");
            return;
        }
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            buffer.append("Day: "+cursor.getString(0)+"\n");
            buffer.append("Start time: "+cursor.getString(1)+"\n");
            buffer.append("Start location: "+cursor.getString(2)+"\n");
            buffer.append("Middle location: "+cursor.getString(3)+"\n");
            buffer.append("End location: "+cursor.getString(4)+"\n");
            buffer.append("Times taken: "+cursor.getString(5)+"\n\n");
        }
        showMessage("Data", buffer.toString());
    }

    private void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+col0+" TEXT, "+col1+" TEXT, "+col2+" TEXT, " +
                ""+col3+" TEXT, "+col4+" TEXT, "+col5+" INT) ");
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(database);  //create the table if it was recently deleted
    }

}
