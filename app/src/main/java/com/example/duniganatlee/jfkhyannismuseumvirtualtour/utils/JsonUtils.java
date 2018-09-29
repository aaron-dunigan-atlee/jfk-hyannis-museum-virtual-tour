package com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils;

import android.content.Context;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.R;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtils {
    // Method to load Json from local file asset.
    // Taken from https://stackoverflow.com/questions/19945411/android-java-how-can-i-parse-a-local-json-file-from-assets-folder-into-a-listvi/19945484#19945484
    public String loadJSONFromAsset(Context context) {
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
}
