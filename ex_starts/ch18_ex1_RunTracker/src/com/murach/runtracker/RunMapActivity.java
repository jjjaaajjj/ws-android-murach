package com.murach.runtracker;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RunMapActivity extends FragmentActivity 
implements OnClickListener,  
           GooglePlayServicesClient.ConnectionCallbacks,
           GooglePlayServicesClient.OnConnectionFailedListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int INTERVAL_REFRESH = 10 * 1000;   // 10 seconds
    
    private GoogleMap map;
    private LocationClient locationClient;
    private List<Location> locationList;
    
    private RunTrackerDB db;
    
    private Button stopwatchButton;
    private Intent stopwatchIntent;

    private Timer timer;
    
    //**************************************************************
    // Activity lifecycle methods
    //****************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_map);

        stopwatchButton = (Button) findViewById(R.id.viewStopwatchButton);
        stopwatchButton.setOnClickListener(this);        
        stopwatchIntent = new Intent(getApplicationContext(), 
            StopwatchActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        db = new RunTrackerDB(this);
        
        // if GPS is not enabled, start GPS settings activity
        LocationManager locationManager = 
                (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "Please enable GPS!",
                    Toast.LENGTH_LONG).show();
            Intent intent = 
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        
        locationClient = new LocationClient(this, this, this);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // if GoogleMap object is not already available, get it
        if (map == null) {
            FragmentManager manager = getSupportFragmentManager();
            SupportMapFragment fragment = 
                    (SupportMapFragment) manager.findFragmentById(R.id.map);
            map = fragment.getMap();
        }
        
        // if GoogleMap object is available, configure it
        if (map != null) { 
            map.getUiSettings().setZoomControlsEnabled(true);
        }

        locationClient.connect();
    }
    
    @Override
    protected void onStop() {
        locationClient.disconnect();
        
        super.onStop();
    }
    
    //**************************************************************
    // Private methods
    //****************************************************************
    private void updateMap(){
        if (locationClient.isConnected()){
            setCurrentLocationMarker();
        }
        displayRun();
    }
    
    private void setCurrentLocationMarker(){
        if (map != null) { 
            // get current location
            Location location = locationClient.getLastLocation();
            
            if (location != null) {
                // zoom in on current location
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                       new CameraPosition.Builder()
                           .target(new LatLng(location.getLatitude(), 
                                              location.getLongitude()))
                           .zoom(16.5f)
                           .bearing(0)
                           .tilt(25)
                           .build()));
                
                // add a marker for the current location
                map.clear();      // clear old marker(s)
                map.addMarker(    // add new marker
                    new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), 
                                             location.getLongitude()))
                        .title("You are here"));
            }
        }
    }
    
    private void displayRun(){
        if (map != null) {
            locationList = db.getLocations();
            PolylineOptions polyline = new PolylineOptions();
            if (locationList.size() > 0) {
                for (Location l : locationList) {
                    LatLng point = new LatLng(
                        l.getLatitude(), l.getLongitude()); 
                    polyline.add(point);
                }
            }
            map.addPolyline(polyline);
        }
    }
    
    private void setMapToRefresh(){
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                RunMapActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMap();
                    }
                });
            }
        };
        timer.schedule(task, INTERVAL_REFRESH, INTERVAL_REFRESH);
    }
    
    //**************************************************************
    // Implement ConnectionCallbacks interface
    //****************************************************************
    @Override
    public void onConnected(Bundle dataBundle) {
        updateMap();
        setMapToRefresh();
    }
        
    @Override
    public void onDisconnected() {
        timer.cancel();
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }
    
    //**************************************************************
    // Implement OnConnectionFailedListener
    //****************************************************************
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // if Google Play services can resolve the error, display activity
        if (connectionResult.hasResolution()) {
            try {
                // start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }
            catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } 
        else {
            new AlertDialog.Builder(this)
                .setMessage("Connection failed. Error code: " 
                            + connectionResult.getErrorCode())
                .show();
        }
    }
    
    //**************************************************************
    // Implement OnClickListener
    //****************************************************************
    @Override
    public void onClick(View v) {
        startActivity(stopwatchIntent);
    }
}