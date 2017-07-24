package com.nexflare.silentplace.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.nexflare.silentplace.Model.PlaceDetail;

import java.util.ArrayList;

import static com.nexflare.silentplace.Utils.SilentPlaceDBHelper.*;

/**
 * Created by 15103068 on 22-07-2017.
 */

public class SilentPlaceDB extends SQLiteOpenHelper {
    public static final String DATABASE="silent_place";
    public static final int VERSION=1;
    public SilentPlaceDB(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE=CREATE_TABLE+TABLE_NAME+LBR+
                COLUMN_ID+INT_PK_AUTOIC+COMMA+
                COLUMN_NAME+TYPE_TEXT+COMMA+
                COLUMN_LATITUDE+TYPE_REAL+COMMA+
                COLUMN_LONGITUDE+TYPE_REAL+RBR+SEMICOLON;
        db.execSQL(CREATE);

    }
    public ArrayList<PlaceDetail> getAllPlaces(){
        ArrayList<PlaceDetail> placeDetails=new ArrayList<>();
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        int nameColumn=cursor.getColumnIndex(COLUMN_NAME);
        int latitudeColumn=cursor.getColumnIndex(COLUMN_LATITUDE);
        int longitudeColumn=cursor.getColumnIndex(COLUMN_LONGITUDE);
        String placeName;
        LatLng latLng;
        while (cursor.moveToNext()){
            placeName=cursor.getString(nameColumn);
            latLng=new LatLng(cursor.getFloat(latitudeColumn),cursor.getFloat(longitudeColumn));
            placeDetails.add(new PlaceDetail(placeName,latLng));
        }
        cursor.close();
        return placeDetails;
    }

    public void insertIntoTable(PlaceDetail placeDetail){
        SQLiteDatabase database=getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(COLUMN_NAME,placeDetail.getName());
        contentValues.put(COLUMN_LATITUDE,placeDetail.getLatLng().latitude);
        contentValues.put(COLUMN_LONGITUDE,placeDetail.getLatLng().longitude);
        database.insert(TABLE_NAME,null,contentValues);

    }
    public void deleteItem(String name){
        SQLiteDatabase database=getWritableDatabase();
        database.delete(TABLE_NAME,COLUMN_NAME+" = ?",new String[]{name});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
