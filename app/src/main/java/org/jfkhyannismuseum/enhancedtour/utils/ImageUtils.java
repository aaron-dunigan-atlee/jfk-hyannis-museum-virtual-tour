package org.jfkhyannismuseum.enhancedtour.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jfkhyannismuseum.enhancedtour.MediaPlayerFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtils {
    private static final String LOG_TAG = "ImageUtils";

    // Create a temporary image file in the app's cache directory, in order to store camera images
    // for barcode processing.
    // See https://developer.android.com/training/camera/photobasics
    public static File createTemporaryImageFile(Context context) throws IOException {
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

    public static boolean deleteTemporaryImageFile(Context context, String filePath) {
        // Get the file.
        File fileToDelete = new File(filePath);

        // Attempt to delete
        boolean deleted = fileToDelete.delete();

        if (!deleted) {
            Log.d(LOG_TAG, "Temporary file could not be deleted.");
        }

        return deleted;
    }

    public static SparseArray<Barcode> detectBarcodes(Context context, String filePath) {
        // Setup barcode detector
        BarcodeDetector detector =
                new BarcodeDetector.Builder(context.getApplicationContext())
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();
        if (!detector.isOperational()) {
            Toast.makeText(context, context.getString(org.jfkhyannismuseum.enhancedtour.R.string.no_barcode_detector), Toast.LENGTH_LONG).show();
            return null;
        }

        //  Detect the Barcode
        Bitmap image = BitmapFactory.decodeFile(filePath);
        Frame frame = new Frame.Builder().setBitmap(image).build();
        return detector.detect(frame);
    }


    public static Integer getBarcodeFromImage(Context context, String photoPath) {
        SparseArray<Barcode> barcodes = ImageUtils.detectBarcodes(context, photoPath);
        ImageUtils.deleteTemporaryImageFile(context, photoPath);
        if (barcodes != null) {
            // Check that there is exactly one barcode detected.
            if (barcodes.size() == 1) {
                Barcode barcode = barcodes.valueAt(0);
                String barcodeContent = barcode.rawValue;
                Log.d("Barcode found", barcodeContent);
                return processBarcode(barcode);
            } else if (barcodes.size() == 0) {
                Log.d("Barcode","No barcodes in image.");
                return null;
            } else {
                Log.d("Barcode","Multiple barcodes in image.");
                return null;
            }
        }
        Log.d("Barcode","barcodes was null");
        return null;
    }

    private static Integer processBarcode(Barcode barcode) {
        // Check that the user has scanned a valid barcode for this app.
        if (barcode.valueFormat != Barcode.TEXT) {
            return null;
        } else /* We have a text barcode */ {
            String barcodeText = barcode.displayValue;
            if (!barcodeText.startsWith("JFK Hyannis Museum")) {
                return null;
            } else /* We have a barcode meant for this app. */ {
                String splitBarcodeText[] = barcodeText.split("\n");
                try {
                    // TODO: Remove exhibit ID from barcode format?
                    int barcodeExhibitId = Integer.parseInt(splitBarcodeText[1]);
                    int barcodePieceId = Integer.parseInt(splitBarcodeText[2]);
                    Log.d("Barcode Exhibit",Integer.toString(barcodeExhibitId));
                    Log.d("Barcode Piece",Integer.toString(barcodePieceId));
                    return barcodePieceId;
                } catch (IndexOutOfBoundsException ex) {
                    Log.d("Barcode", "Bad barcode read.  Exhibit or piece ID not found.");
                    return null;
                }
            }
        }
    }
    /* A custom Picasso Target so that we can download an image to be set as
     * the ExoPlayer's default artwork.  Based on
     * https://www.codexpedia.com/android/android-download-and-save-image-through-picasso/
     * Also see https://square.github.io/picasso/2.x/picasso/com/squareup/picasso/Target.html
     */
    public static Target picassoImageTarget(final MediaPlayerFragment playerFragment) {
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                playerFragment.setPlayerViewDefaultArtwork(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception exception, Drawable errorDrawable) {
                exception.printStackTrace();
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }

}
