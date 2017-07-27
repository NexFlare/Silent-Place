package com.nexflare.silentplace.Service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
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
import com.nexflare.silentplace.Activity.MainActivity;
import com.nexflare.silentplace.DataBase.SilentPlaceDB;
import com.nexflare.silentplace.Interface.DistanceMatrixApi;
import com.nexflare.silentplace.Model.PlaceDetail;
import com.nexflare.silentplace.Model.WalkDistanceResult;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearByService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public GoogleApiClient mGoogleApiClient;

    public LocationRequest request;

    private final IBinder mBinder = new LocalBinder();
    public NearByService() {

    }

    ArrayList<PlaceDetail> placeDetailArray;
    Retrofit retrofit;
    SilentPlaceDB database;
    Double latitude = null, longitude = null;
    AudioManager audioManager;
    SharedPreferences sharedPref;

    public static final String TAGGER="TAGGER";

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        Log.d("Location", "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Location", "onStartCommand: ");

         sharedPref = PreferenceManager.getDefaultSharedPreferences(this);




        retrofit = new Retrofit.Builder()
                .baseUrl("http://maps.google.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        database = new SilentPlaceDB(this);
        placeDetailArray = new ArrayList<>();
        return START_STICKY;
    }

    private void checkIfNearByPlace() {
        placeDetailArray = database.getAllPlaces();
        DistanceMatrixApi api = retrofit.create(DistanceMatrixApi.class);
        if (latitude != null&&placeDetailArray.size()>0&&audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
            for (int i = 0; i < placeDetailArray.size(); i++) {
                String destination = placeDetailArray.get(i).getLatLng().latitude + "," + placeDetailArray.get(i).getLatLng().longitude;
                Log.d(TAGGER, "checkIfNearByPlace: " + destination);
                api.getWalkingDistance(latitude+","+longitude,destination,"false","metric","walking").enqueue(new Callback<WalkDistanceResult>() {
                    @Override
                    public void onResponse(Call<WalkDistanceResult> call, Response<WalkDistanceResult> response) {
                        Log.d(TAGGER, "onResponse: "+response.body().getRoutes().get(0).getLegs().get(0).getDistance().getValue());
                        if(response.body().getStatus().equals("OK")) {
                            long distance = Long.parseLong(response.body().getRoutes().get(0).getLegs().get(0).getDistance().getValue());
                            if (distance <= 100 && (sharedPref.getBoolean("enable", true))) {
                                silentPhone();
                                Log.d(TAGGER, "onResponse:" + sharedPref.getBoolean("enable", true) + "calledcalledcalledcalled");

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<WalkDistanceResult> call, Throwable t) {
                        Log.d(TAGGER, "onFailure: ");
                    }
                });
            }
    }

    private void silentPhone() {
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
         request = LocationRequest.create()
                .setInterval(30000)
                .setFastestInterval(20000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                    Log.d("TAGGER", "onLocationChanged: "+latitude);
                    Log.d("TAGGER", "onLocationChanged: "+longitude);
                    checkIfNearByPlace();
                }
            });
            if(MainActivity.c!=null)
            {
                AutoLoactiongiven(request,mGoogleApiClient);
            }



        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {
        public NearByService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NearByService.this;
        }
    }

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
                            status.startResolutionForResult((Activity) MainActivity.c, 1000);
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
