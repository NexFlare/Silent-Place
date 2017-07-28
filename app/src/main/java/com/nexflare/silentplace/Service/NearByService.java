package com.nexflare.silentplace.Service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
    public NearByService() {
    }

    public static GoogleApiClient mGoogleApiClient;
    ArrayList<PlaceDetail> placeDetailArray;
    Retrofit retrofit;
    SilentPlaceDB database;
    Double latitude = null, longitude = null;
    AudioManager audioManager;
    SharedPreferences sharedPref;
    public static LocationRequest request;
    public static final String TAGGER = "TAGGER";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Location", "onCreate: ");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Location", "onStartCommand: ");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mGoogleApiClient.connect();
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
        Log.d("RELEASE", "checkIfNearByPlace: " + database.getAllPlaces().size());
        DistanceMatrixApi api = retrofit.create(DistanceMatrixApi.class);
        if (latitude != null && placeDetailArray.size() > 0 && audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
            for (int i = 0; i < placeDetailArray.size(); i++) {
                String destination = placeDetailArray.get(i).getLatLng().latitude + "," + placeDetailArray.get(i).getLatLng().longitude;
                Log.d(TAGGER, "checkIfNearByPlace: " + destination);
                try {
                    api.getWalkingDistance(latitude + "," + longitude, destination, "false", "metric", "walking").enqueue(new Callback<WalkDistanceResult>() {
                        @Override
                        public void onResponse(Call<WalkDistanceResult> call, Response<WalkDistanceResult> response) {
                            Log.d("TAGGER", "onResponse: " + response.body().getStatus());
                            //Log.d(TAGGER, "onResponse: "+response.body().getRoutes().get(0).getLegs().get(0).getDistance().getValue());
                            if (response.body().getStatus().equals("OK")) {
                                long distance = Long.parseLong(response.body().getRoutes().get(0).getLegs().get(0).getDistance().getValue());
                                Log.d("RELEASE", "onResponse: " + distance);
                                if (distance <= 100 && (sharedPref.getBoolean("enable", true))) {
                                    silentPhone();
                                    Log.d(TAGGER, "onResponse:" + sharedPref.getBoolean("enable", true) + "called called called called");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<WalkDistanceResult> call, Throwable t) {
                            Log.d(TAGGER, "onFailure: ");
                        }
                    });
                } catch (Exception e) {
                    Log.d("RELEASE", "checkIfNearByPlace: " + e.getMessage());
                }
            }
    }

    private void silentPhone() {
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        request = LocationRequest.create()
                .setInterval(30000)
                .setFastestInterval(20000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        } else {
            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, new com.google.android.gms.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.d("RELEASE", "onLocationChanged: " + latitude);
                        Log.d("RELEASE", "onLocationChanged: " + longitude);
                        checkIfNearByPlace();
                    }
                });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
