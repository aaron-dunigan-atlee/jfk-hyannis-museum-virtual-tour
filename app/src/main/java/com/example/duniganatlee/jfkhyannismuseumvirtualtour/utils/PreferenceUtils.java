package com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/* We are using SharedPreferences for local storage of the exhibits JSON in the case that it
 * cannot be accessed from the museum's website.
 */
public class PreferenceUtils {
    private static final String EXHIBITS_JSON_KEY = "exhibits_json_key";

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

}
