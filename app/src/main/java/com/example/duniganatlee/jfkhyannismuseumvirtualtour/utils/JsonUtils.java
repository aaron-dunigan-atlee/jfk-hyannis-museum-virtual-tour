package com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils;

import android.content.Context;
import android.util.Log;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.R;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtils {
    public static final String JSON_LOG_TAG = "JSON";
    // Method to load Json from local file asset.
    // Taken from https://stackoverflow.com/questions/19945411/android-java-how-can-i-parse-a-local-json-file-from-assets-folder-into-a-listvi/19945484#19945484
    public static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(context.getString(R.string.json_filename));
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

    /*
    Given the JSON exhibit list, extract each individual exhibit, assign it to an exhibit object,
    and return an array of all exhibits.
     */
    public static Exhibit[] parseExhibitList(String jsonRecipeList) {
        Gson gson = new Gson();
        Exhibit[] exhibits = gson.fromJson(jsonRecipeList, Exhibit[].class);
        Log.d("Exhibits found",Integer.toString(exhibits.length));
        return exhibits;
    }
}
