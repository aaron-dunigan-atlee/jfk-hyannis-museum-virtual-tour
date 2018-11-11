package org.jfkhyannismuseum.enhancedtour.utils;

import android.content.Context;


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
