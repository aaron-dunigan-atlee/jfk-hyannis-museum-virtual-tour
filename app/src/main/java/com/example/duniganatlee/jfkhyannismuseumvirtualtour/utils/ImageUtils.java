package com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtils {
    private static final String LOG_TAG = "ImageUtils";

    // Create a temporary image file in the app's cache directory, in order to store camera images
    // for barcode processing.
    // See https://developer.android.com/training/camera/photobasics
    static File createTemporaryImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File imageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                imageDir      /* directory */
        );
    }

    static boolean deleteTemporaryImageFile(Context context, String filePath) {
        // Get the file.
        File fileToDelete = new File(filePath);

        // Attempt to delete
        boolean deleted = fileToDelete.delete();

        if (!deleted) {
            Log.d(LOG_TAG, "Temporary file could not be deleted.");
        }

        return deleted;
    }

    static SparseArray<Barcode> detectBarcodes(Context context, String filePath) {
        // Setup barcode detector
        BarcodeDetector detector =
                new BarcodeDetector.Builder(context.getApplicationContext())
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();
        if (!detector.isOperational()) {
            Toast.makeText(context, context.getString(R.string.no_barcode_detector), Toast.LENGTH_LONG).show();
            return null;
        }

        //  Detect the Barcode
        Bitmap image = BitmapFactory.decodeFile(filePath);
        Frame frame = new Frame.Builder().setBitmap(image).build();
        return detector.detect(frame);
    }
}
