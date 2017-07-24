package com.nexflare.silentplace.Service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.nexflare.silentplace.DataBase.SilentPlaceDB;
import com.nexflare.silentplace.Interface.DistanceMatrixApi;
import com.nexflare.silentplace.Model.DistanceMatrixResult;
import com.nexflare.silentplace.Model.PlaceDetail;
import com.nexflare.silentplace.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearByService extends Service {
    public NearByService() {
    }
    ArrayList<PlaceDetail> placeDetailArray;
    Retrofit retrofit;
    SilentPlaceDB database;
    Double latitude=null, longitude=null;
    LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Location", "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Location", "onStartCommand: ");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        retrofit=new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        NearByLocationListener listner = new NearByLocationListener();
        database=new SilentPlaceDB(this);
        placeDetailArray=new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 80, listner);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10000,80,listner);

        }
        return START_STICKY;
    }

    private void checkIfNearByPlace() {
        placeDetailArray=database.getAllPlaces();
        DistanceMatrixApi api=retrofit.create(DistanceMatrixApi.class);
        if(latitude!=null)
        for (int i=0;i<placeDetailArray.size();i++){
            String destination=placeDetailArray.get(i).getLatLng().latitude+","+placeDetailArray.get(i).getLatLng().longitude;
            Log.d("TAGGER", "checkIfNearByPlace: "+destination);
            Log.d("TAGGER", "checkIfNearByPlace: "+getString(R.string.API_KEY));
            api.getDistance(latitude+","+longitude,destination, getString(R.string.API_KEY)).enqueue(new Callback<DistanceMatrixResult>() {
                @Override
                public void onResponse(Call<DistanceMatrixResult> call, Response<DistanceMatrixResult> response) {
                    Log.d("TAGGER", "onResponse: "+response.body().getRows().get(0).getElements().get(0).getDistance().getValue());
                }

                @Override
                public void onFailure(Call<DistanceMatrixResult> call, Throwable t) {
                    Log.d("TAGGER", "onFailure: ");
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public class NearByLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            Log.d("LocationUpdate", "onLocationChanged: "+location.getLatitude());
            Log.d("LocationUpdate", "onLocationChanged: "+location.getLongitude());
           latitude=location.getLatitude();
            longitude=location.getLongitude();
            checkIfNearByPlace();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}
