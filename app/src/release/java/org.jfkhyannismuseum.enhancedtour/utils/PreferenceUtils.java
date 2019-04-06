package org.jfkhyannismuseum.enhancedtour.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/* We are using SharedPreferences for local storage of the exhibits JSON in the case that it
 * cannot be accessed from the museum's website.
 */
public class PreferenceUtils {
    private static final String EXHIBITS_JSON_KEY = "exhibits_json_key";
    private static final String LAST_VISIT_DATE_KEY = "last_visit_date_key";

    // Prevent instantiation.
    private PreferenceUtils() {}

    public static void setPreferenceExhibitsJson(Context context, String json) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();
        preferenceEditor.putString(EXHIBITS_JSON_KEY, json);
        preferenceEditor.apply();
    }

    public static String getPreferenceExhibitsJson(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(EXHIBITS_JSON_KEY, null);
    }

    public static void setPreferenceLastMuseumVisitDate(Context context) {
        // Set the last date that an exhibit was viewed at the museum.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        preferenceEditor.putLong(LAST_VISIT_DATE_KEY, currentTime);
        preferenceEditor.apply();
    }

    public static long getPreferenceLastViewDate(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(LAST_VISIT_DATE_KEY, Calendar.getInstance().getTimeInMillis());
    }

    public static long getDaysSinceLastVisit(Context context) {
        long lastVisit = getPreferenceLastViewDate(context);
        long elapsedTime = Calendar.getInstance().getTimeInMillis() - lastVisit;
        return TimeUnit.MILLISECONDS.toDays(elapsedTime);
    }
}
