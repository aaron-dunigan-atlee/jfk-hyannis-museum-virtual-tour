package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.util.SparseArray;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    MediaPlayerFragment.OnFragmentInteractionListener {

    // Request code for launching camera app
    private int REQUEST_CAMERA_IMAGE = 1;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.duniganatlee.fileprovider";
    // Path for a temporary photo taken when user scans a barcode.
    private String mTempPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromCamera();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Add media player fragment to its container.
        MediaPlayerFragment fragment = new MediaPlayerFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.media_player_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO: Remove this interface if no interaction is needed.
    }

    // Basic process taken from https://developer.android.com/training/camera/photobasics
    // Also modeled after the Emojify app
    private void getImageFromCamera() {
        // Intent to launch the camera.
        Intent launchCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Check if there's an app to handle this intent.

        if (launchCameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = ImageUtils.createTemporaryImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imageFile != null) {
                // Get the file's absolute path.
                mTempPhotoPath = imageFile.getAbsolutePath();

                // Store the file's URI as an intent extra, and launch the intent.
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        imageFile);
                launchCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(launchCameraIntent, REQUEST_CAMERA_IMAGE);
            }

        } else {
            Toast.makeText(this,getString(R.string.no_camera_app),Toast.LENGTH_LONG).show();
        }

    }

    // Process data received back from Intents launched by this activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_CAMERA_IMAGE && resultCode == RESULT_OK) {
            // Extract the barcode and use its information.
            processImage();
        } else {
            // Otherwise, delete the temporary image file
            ImageUtils.deleteTemporaryImageFile(this, mTempPhotoPath);
        }
    }

    private void processImage() {
        SparseArray<Barcode> barcodes = ImageUtils.detectBarcodes(this, mTempPhotoPath);
        ImageUtils.deleteTemporaryImageFile(this, mTempPhotoPath);
        if (barcodes != null) {
            // Check that there is exactly one barcode detected.
            if (barcodes.size() == 1) {
                Barcode barcode = barcodes.valueAt(0);
                String barcodeContent = barcode.rawValue;
                Toast.makeText(this, barcodeContent, Toast.LENGTH_LONG).show();
            } else if (barcodes.size() == 0) {
                Toast.makeText(this, getString(R.string.no_barcode_in_image), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.multiple_barcodes_in_image), Toast.LENGTH_LONG).show();
            }
        }
    }
}
