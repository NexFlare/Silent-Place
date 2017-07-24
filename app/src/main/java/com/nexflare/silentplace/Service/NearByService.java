package com.nexflare.silentplace.Service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.location.LocationListener;

import com.nexflare.silentplace.DataBase.SilentPlaceDB;

public class NearByService extends Service {
    public NearByService() {
    }
    SilentPlaceDB database;
    Double latitude, longitude;
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
        NearByLocationListner listner = new NearByLocationListner();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 80, listner);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10000,80,listner);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public class NearByLocationListner implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
           /* Log.d("LocationUpdate", "onLocationChanged: "+location.getLatitude());
            Log.d("LocationUpdate", "onLocationChanged: "+location.getLongitude());*/
           latitude=location.getLatitude();
            longitude=location.getLongitude();
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
