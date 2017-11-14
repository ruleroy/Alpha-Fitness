package com.vannakittikun.alphafitness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rule on 11/4/2017.
 */

public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 17;
    private static final String DATABASE_NAME = "locationDB";

    public static final String TABLE_LOCATION = "location";
    public static final String TABLE_USER = "user";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_TIME = "time";

    public static final String USER_NAME = "name";
    public static final String USER_GENDER = "gender";
    public static final String USER_WEIGHT = "weight";

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_LOCATION + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_LAT + " TEXT," +
                COLUMN_LNG + " TEXT," +
                COLUMN_TIME + " TEXT" +
                ");";
        sqLiteDatabase.execSQL(query);

        String query2 = "CREATE TABLE " + TABLE_USER + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_NAME + " TEXT," +
                USER_GENDER + " TEXT," +
                USER_WEIGHT + " INTEGER" +
                ");";
        sqLiteDatabase.execSQL(query2);

        sqLiteDatabase.execSQL("INSERT INTO " + TABLE_USER + " (name, gender, weight) VALUES ('Name', 'Male', 160)");
        //addUser("Name", "Male", 160);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(sqLiteDatabase);
    }

    public void addLocation(double lat, double lng, long time) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LAT, lat);
        values.put(COLUMN_LNG, lng);
        values.put(COLUMN_TIME, time);

        db.insert(TABLE_LOCATION, null, values);
        db.close();

    }

    public void addUser(String name, String gender, int weight) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_NAME, name);
        values.put(USER_GENDER, gender);
        values.put(USER_WEIGHT, weight);

        db.insert(TABLE_USER, null, values);
        db.close();

    }

    public void updateUser(int id, String name, String gender, int weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_NAME, name);
        values.put(USER_GENDER, gender);
        values.put(USER_WEIGHT, weight);

        db.update(TABLE_USER, values, "_id=" + Integer.toString(id), null);
        db.close();
    }

    public User getUser(int id) {
        User user = new User();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_ID + "=" + Integer.toString(id), null);
        c.moveToFirst();
        if (c.getString(c.getColumnIndex("_id")) != null) {
            user.setName(c.getString(c.getColumnIndex(USER_NAME)));
            user.setGender(c.getString(c.getColumnIndex(USER_GENDER)));
            user.setWeight(c.getInt(c.getColumnIndex(USER_WEIGHT)));
        }
        return user;
    }

    public void deleteAllLocation() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='" + TABLE_LOCATION + "'");
        db.execSQL("DELETE FROM " + TABLE_LOCATION);
        db.close();
    }

    public ArrayList<LatLng> getLastWorkoutPath() {
        ArrayList<LatLng> result = new ArrayList<LatLng>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_LOCATION + ";", null);
        c.moveToLast();
        if (c.getCount() > 1) {
            for (int i = 0; i < 2; i++) {
                if (c.getString(c.getColumnIndex("_id")) != null) {
                    LatLng path = new LatLng(c.getDouble(c.getColumnIndex(COLUMN_LAT)), c.getDouble(c.getColumnIndex(COLUMN_LNG)));
                    result.add(path);
                }
                c.moveToPrevious();
            }
        }
        db.close();
        c.close();
        return result;
    }

    public ArrayList<LatLng> getTotalPath() {
        ArrayList<LatLng> result = new ArrayList<LatLng>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_LOCATION + ";", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex("_id")) != null) {
                LatLng path = new LatLng(c.getDouble(c.getColumnIndex(COLUMN_LAT)), c.getDouble(c.getColumnIndex(COLUMN_LNG)));
                result.add(path);
            }
            c.moveToNext();
        }
        db.close();
        c.close();
        return result;
    }

    public ArrayList<LocationObject> getSavedLocations() {
        ArrayList<LocationObject> locations = new ArrayList<LocationObject>();
        SQLiteDatabase db = getWritableDatabase();
        String Query = "SELECT * FROM " + TABLE_LOCATION + ";";
        Cursor c = db.rawQuery(Query, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            if (c.getString(c.getColumnIndex("_id")) != null) {
                LocationObject newLocationObject = new LocationObject(c.getDouble(c.getColumnIndex(COLUMN_LAT)), c.getDouble(c.getColumnIndex(COLUMN_LNG)), c.getLong(c.getColumnIndex(COLUMN_TIME)));
                locations.add(newLocationObject);
            }
            c.moveToNext();
        }
        db.close();
        c.close();
        return locations;
    }

    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"message"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }
    }
}
