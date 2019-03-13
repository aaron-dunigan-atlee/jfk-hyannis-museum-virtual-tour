package org.jfkhyannismuseum.enhancedtour.utils;

import android.content.Context;

public class LocationUtils {
    // Debug version of LocationUtils always assumes we are at the museum.
    public static boolean userIsAtMuseum = true;

    // Debug version of LocationUtils always assumes we are at the museum
    public static void checkLocation(Context context) {
        userIsAtMuseum = true;
    }
}
