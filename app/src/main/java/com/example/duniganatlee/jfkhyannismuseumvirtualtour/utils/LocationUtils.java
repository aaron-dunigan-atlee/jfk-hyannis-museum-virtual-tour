package com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

// TODO: Add a justification for using Location Permissions.
public class LocationUtils {
    final static private String LOCATION_TAG = "Location";
    // Constants for checking permissions.
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    // Max distance (meters) from JFK museum to be considered "at museum."
    private static final float MAX_DISTANCE_TO_MUSEUM = (float) 100.0;

    // Whether the user is currently at the JFK Hyannis Museum.
    public static boolean userIsAtMuseum = false;

    // Check whether we are at the museum.
    // Location checking based on https://developer.android.com/training/location/retrieve-current
    public static void checkLocation(final Context context) {

        if (!(context instanceof Activity)) {
            Log.d("checkLocation","Called without a valid activity.");
            return;  }
        Activity activity = (Activity) context;
        // Get location services client.
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location currentLocation) {
                            // Get last known location. In some rare situations this can be null.
                            if (currentLocation == null) {
                                // TODO: Logic to handle null location object
                                Log.d(LOCATION_TAG,"Null location.");
                            } else {
                                Log.d(LOCATION_TAG, currentLocation.toString());
                                Log.d("Location provider",currentLocation.getProvider());
                                float museumLatitude = Float.parseFloat(context.getString(R.string.jfk_museum_latitude));
                                float museumLongitude = Float.parseFloat(context.getString(R.string.jfk_museum_longitude));
                                Location museumLocation = new Location(currentLocation);
                                museumLocation.setLatitude(museumLatitude);
                                museumLocation.setLongitude(museumLongitude);
                                float distanceToMuseum = currentLocation.distanceTo(museumLocation);
                                Log.d("Distance to museum",Float.toString(distanceToMuseum));
                                userIsAtMuseum = (distanceToMuseum < MAX_DISTANCE_TO_MUSEUM);
                            }
                        }
                    })
                    .addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Location",e.toString());
                        }
                    });
        }
    }
}
