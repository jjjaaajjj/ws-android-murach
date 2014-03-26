package com.murach.runtracker;

import com.google.android.gms.location.LocationRequest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class RunTrackerApp extends Application {

    public static final int UPDATE_INTERVAL = 5000;         // 5 seconds
    public static final int FASTEST_UPDATE_INTERVAL = 2000; // 2 seconds
    
    private LocationRequest locationRequest;
    private RunTrackerDB db;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Temp", "App started");
        
        // get database
        db = new RunTrackerDB(this);
        
        // get location request and set it up
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(RunTrackerApp.UPDATE_INTERVAL)
            .setFastestInterval(RunTrackerApp.FASTEST_UPDATE_INTERVAL);
    }
    
    public LocationRequest getLocationRequest() {
        return locationRequest;
    }
    
    public RunTrackerDB getDB() {
        return db;
    }
    
    public void enableGPS(Context context) {
        LocationManager locationManager = 
                (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(context, "Please enable GPS!",
                    Toast.LENGTH_LONG).show();
            Intent intent = 
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }
}