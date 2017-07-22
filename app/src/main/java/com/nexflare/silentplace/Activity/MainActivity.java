package com.nexflare.silentplace.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.nexflare.silentplace.Adapter.PlaceDetailAdapter;
import com.nexflare.silentplace.DataBase.SilentPlaceDB;
import com.nexflare.silentplace.Pojo.PlaceDetail;
import com.nexflare.silentplace.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG="place_search";
    private static final int REQUEST_CODE_PLACE = 2511;
    FloatingActionButton fabGetPlace;
    ArrayList<PlaceDetail> placeDetailArray;
    RecyclerView rvPlace;
    PlaceDetailAdapter adapter;
    SilentPlaceDB database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fabGetPlace= (FloatingActionButton) findViewById(R.id.fabGetPlace);
        rvPlace= (RecyclerView) findViewById(R.id.rvPlace);
        database=new SilentPlaceDB(this);
        placeDetailArray=database.getAllPlaces();
        adapter=new PlaceDetailAdapter(placeDetailArray,this);
        fabGetPlace.setOnClickListener(this);
        rvPlace.setLayoutManager(new LinearLayoutManager(this));
        rvPlace.setAdapter(adapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_PLACE){
            if(resultCode==RESULT_OK){
                Place place=PlaceAutocomplete.getPlace(this,data);
                database.insertIntoTable(new PlaceDetail(place.getName().toString(),place.getLatLng()));
                placeDetailArray=database.getAllPlaces();
                adapter.updateArray(placeDetailArray);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fabGetPlace){
            try {
                Intent intent=new PlaceAutocomplete.
                        IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).
                        build(MainActivity.this);
                startActivityForResult(intent,REQUEST_CODE_PLACE);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }
}
