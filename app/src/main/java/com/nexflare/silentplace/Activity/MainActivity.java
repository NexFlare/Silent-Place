package com.nexflare.silentplace.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.nexflare.silentplace.Adapter.PlaceDetailAdapter;
import com.nexflare.silentplace.DataBase.SilentPlaceDB;
import com.nexflare.silentplace.Interface.DeleteItem;
import com.nexflare.silentplace.Model.PlaceDetail;
import com.nexflare.silentplace.R;
import com.nexflare.silentplace.Service.NearByService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "place_search";
    private static final int REQUEST_CODE_PLACE = 2511;
    private static final int REQUEST_CODE_PERMISSION = 3215;
    FloatingActionButton fabGetPlace;
    ArrayList<PlaceDetail> placeDetailArray;
    LocationManager locationManager;
    boolean permissionGranted;
    RecyclerView rvPlace;
    PlaceDetailAdapter adapter;
    SilentPlaceDB database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fabGetPlace = (FloatingActionButton) findViewById(R.id.fabGetPlace);
        rvPlace = (RecyclerView) findViewById(R.id.rvPlace);
        database = new SilentPlaceDB(this);
        locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
        placeDetailArray = database.getAllPlaces();
        adapter = new PlaceDetailAdapter(placeDetailArray, this, new DeleteItem() {
            @Override
            public void onItemSelected(String item) {
                database.deleteItem(item);
                placeDetailArray = database.getAllPlaces();
                adapter.updateArray(placeDetailArray);
            }
        });
        checkforPermission();
        checkLocationEnabled();
        startService(new Intent(MainActivity.this, NearByService.class));
        fabGetPlace.setOnClickListener(this);
        rvPlace.setLayoutManager(new LinearLayoutManager(this));
        rvPlace.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rvPlace.setAdapter(adapter);
    }

    private void checkLocationEnabled(){
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder locationAlert=new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setMessage("Enable location settings")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog=locationAlert.create();
            dialog.show();
        }
    }

    private void checkforPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_PERMISSION);

        }
        else{
            permissionGranted=true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PLACE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                database.insertIntoTable(new PlaceDetail(place.getName().toString(), place.getLatLng()));
                placeDetailArray = database.getAllPlaces();
                adapter.updateArray(placeDetailArray);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabGetPlace) {
            try {
                Intent intent = new PlaceAutocomplete.
                        IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).
                        build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_PLACE);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_PERMISSION){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                permissionGranted=true;
                Log.d("TAGGER", "onRequestPermissionsResult: ");
                startService(new Intent(MainActivity.this, NearByService.class));
            }
        }
    }
}
