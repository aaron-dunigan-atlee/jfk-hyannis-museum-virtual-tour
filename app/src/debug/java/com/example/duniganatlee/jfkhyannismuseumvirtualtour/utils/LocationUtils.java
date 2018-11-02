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

    // Debug version of LocationUtils always assumes we are at the museum.
    public static boolean userIsAtMuseum = true;

    // Debug version of LocationUtils always assumes we are at the museum
    public static void checkLocation(final Context context) {
        userIsAtMuseum = true;
    }
}
