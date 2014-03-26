package com.murach.runtracker;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class RunTrackerService extends Service 
implements GooglePlayServicesClient.ConnectionCallbacks,
           GooglePlayServicesClient.OnConnectionFailedListener,
           LocationListener {

    public static final int UPDATE_INTERVAL = 5000;         // 5 seconds
    public static final int FASTEST_UPDATE_INTERVAL = 2000; // 2 seconds
    
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    
    private RunTrackerDB db;

    @Override
    public void onCreate() {
        super.onCreate();

        // get database
        db = new RunTrackerDB(getApplicationContext());
        
        // get location client
        locationClient = new LocationClient(this, this, this);
        
        // get location request and set it up
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (locationClient.isConnected()) {
            locationClient.disconnect();
        }
        super.onDestroy();
    }
    
    //**************************************************************
    // Implement ConnectionCallbacks interface
    //****************************************************************
    @Override
    public void onConnected(Bundle dataBundle) {
        Location location = locationClient.getLastLocation();
        if (location != null){
            db.insertLocation(location);
        }
        locationClient.requestLocationUpdates(locationRequest, this);
    }
    
    @Override
    public void onDisconnected() {
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
    }
    
    //**************************************************************
    // Implement OnConnectionFailedListener
    //****************************************************************
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed! " + 
            "Please check your settings and try again.", 
            Toast.LENGTH_SHORT).show();
    }
    
    //**************************************************************
    // Implement LocationListener
    //****************************************************************
    @Override
    public void onLocationChanged(Location location) {
        if (location != null){
            db.insertLocation(location);    
        }
    }
}