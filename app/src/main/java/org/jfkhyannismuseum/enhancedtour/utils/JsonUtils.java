package org.jfkhyannismuseum.enhancedtour.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.jfkhyannismuseum.enhancedtour.model.Exhibit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtils {

    // Prevent instantiation.
    private JsonUtils() {}

    // Method to load Json from local file asset.
    // Taken from https://stackoverflow.com/questions/19945411/android-java-how-can-i-parse-a-local-json-file-from-assets-folder-into-a-listvi/19945484#19945484
    public static String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open(context.getString(org.jfkhyannismuseum.enhancedtour.R.string.json_filename));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private static String getExhibitListJson(String fullJsonString) throws JSONException {
        JSONObject fullJson = new JSONObject(fullJsonString);
        JSONArray exhibitsArray = fullJson.getJSONArray("exhibits");
        return exhibitsArray.toString();
    }

    public static int getJsonVersion(String fullJsonString) throws JSONException {
        if (fullJsonString != null) {
            JSONObject fullJSon = new JSONObject(fullJsonString);
            return fullJSon.getInt("json_version");
        } else {
            JSONException exception = new JSONException("Null JSON string passed.");
            throw exception;
        }
    }

    /*
    Given the JSON exhibit list, extract each individual exhibit, assign it to an exhibit object,
    and return an array of all exhibits.
     */
    public static Exhibit[] parseExhibitList(String jsonString) {
        String jsonExhibitList;
        try {
            jsonExhibitList = getExhibitListJson(jsonString);
        } catch (JSONException e) {
            return null;
        }
        Gson gson = new Gson();
        Exhibit[] exhibits = gson.fromJson(jsonExhibitList, Exhibit[].class);
        Log.d("Exhibits found",Integer.toString(exhibits.length));
        return exhibits;
    }
}
