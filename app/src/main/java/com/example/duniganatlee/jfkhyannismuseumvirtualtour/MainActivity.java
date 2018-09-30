package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.SparseArray;
import android.view.SubMenu;
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

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.AppDatabase;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.ImageUtils;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.JsonUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    MediaPlayerFragment.OnFragmentInteractionListener {

    // Request code for launching camera app
    private static final int REQUEST_CAMERA_IMAGE = 1;
    // Max distance (meters) from JFK museum to be considered "at museum."
    private static final float MAX_DISTANCE_TO_MUSEUM = (float) 100.0;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.duniganatlee.fileprovider";
    private static final String PIECE_ID = "piece_id";
    // Path for a temporary photo taken when user scans a barcode.
    private String mTempPhotoPath;
    private FusedLocationProviderClient mFusedLocationClient;
    // Constants for checking permissions.
    private final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    // Whether the user is currently at the JFK Hyannis museum
    private boolean mAtMuseum = false;

    private String mExhibitsJson = null;
    private Exhibit[] mExhibitsList;
    private AppDatabase mHistoryDb;
    private int mPieceId;

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

        // TODO: Handle error loading JSON.
        mExhibitsJson = JsonUtils.loadJSONFromAsset(this);
        mExhibitsList = JsonUtils.parseExhibitList(mExhibitsJson);


        // Get instance of viewing history database.
        mHistoryDb = AppDatabase.getInstance(getApplicationContext());

        // Find out which exhibit piece we're looking at.  If not specified, view the intro video.
        if (savedInstanceState != null) {
            mPieceId = savedInstanceState.getInt(PIECE_ID);
        } else {
            mPieceId = 0;
        }
        setUpViewModel();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Add exhibit titles to navigation drawer menu.
        // https://freakycoder.com/android-notes-53-how-to-create-menu-item-for-navigationdrawer-programmatically-67ddfa8027bc
        // Note that Menu.findItem(id) finds by *resource* id, whereas Menu.getItem(index) gets by position.
        // https://developer.android.com/reference/android/view/Menu#findItem(int)
        SubMenu exhibitsMenu = navigationView.getMenu().findItem(R.id.nav_exhibits_section).getSubMenu();
        for (int i=0; i<mExhibitsList.length; i++) {
            exhibitsMenu.add(mExhibitsList[i].getExhibitTitle())
                .setIcon(R.drawable.ic_menu_gallery);
        }

        // Add media player fragment to its container.
        MediaPlayerFragment fragment = new MediaPlayerFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.media_player_container, fragment)
                .commit();

        // Get location services client.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


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
            getImageFromCamera();
        } else if (id == R.id.nav_location) {
            checkLocation(this);
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

    // Check whether we are at the museum.
    // Location checking based on https://developer.android.com/training/location/retrieve-current
    private void checkLocation(final Context context) {
        final String LOCATION_TAG = "Location";
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location currentLocation) {
                            // Got last known location. In some rare situations this can be null.
                            if (currentLocation == null) {
                                // Logic to handle location object
                                Log.d(LOCATION_TAG,"Null location.");
                            } else {
                                Log.d(LOCATION_TAG, currentLocation.toString());
                                Log.d("Location provider",currentLocation.getProvider());
                                float museumLatitude = Float.parseFloat(getString(R.string.jfk_museum_latitude));
                                float museumLongitude = Float.parseFloat(getString(R.string.jfk_museum_longitude));
                                Location museumLocation = new Location(currentLocation);
                                museumLocation.setLatitude(museumLatitude);
                                museumLocation.setLongitude(museumLongitude);
                                float distanceToMuseum = currentLocation.distanceTo(museumLocation);
                                Log.d("Distance to museum",Float.toString(distanceToMuseum));
                                mAtMuseum = (distanceToMuseum < MAX_DISTANCE_TO_MUSEUM);
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Location",e.toString());
                        }
                    });
        }
    }

    // Set up ViewModel
    private void setUpViewModel() {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PIECE_ID, mPieceId);
    }
}
