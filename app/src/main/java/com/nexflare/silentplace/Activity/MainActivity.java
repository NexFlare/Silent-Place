package com.nexflare.silentplace.Activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    ArrayList<PlaceDetail> placeDetailArray;
    LocationManager locationManager;
    boolean permissionGranted;
    RecyclerView rvPlace;
    AdRequest adRequest;
    NearByService mService;

    boolean mBound = false;
    PlaceDetailAdapter adapter;
    SilentPlaceDB database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdView = (AdView) findViewById(R.id.bannerAd);
        adRequest = new AdRequest.Builder().build();
        mInterstitialAd=new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mAdView.loadAd(adRequest);
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
/*
        startService(new Intent(MainActivity.this, NearByService.class));
*/
        fabGetPlace.setOnClickListener(this);
        rvPlace.setLayoutManager(new LinearLayoutManager(this));
        rvPlace.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rvPlace.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(MainActivity.this, NearByService.class));

        Intent intent = new Intent(this, NearByService.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
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
            AutoLoactiongiven(mService.request,mService.mGoogleApiClient);

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
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                mInterstitialAd.setAdListener(new AdListener() {

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        Toast.makeText(getBaseContext(), "Thank you for watching the ad", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        mInterstitialAd.show();
                    }
                });
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
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
                AutoLoactiongiven(mService.request,mService.mGoogleApiClient);

                startService(new Intent(MainActivity.this, NearByService.class));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_setting){
            Intent i=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NearByService.LocalBinder binder = (NearByService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            checkforPermission();


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    private void AutoLoactiongiven(LocationRequest mLocationRequest, GoogleApiClient mGoogleApiClient)
    {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling
                            // startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }
}
