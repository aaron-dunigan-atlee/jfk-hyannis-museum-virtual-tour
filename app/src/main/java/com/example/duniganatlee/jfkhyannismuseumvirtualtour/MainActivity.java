package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.AppDatabase;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.ExhibitHistoryViewModel;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.ExhibitHistoryViewModelFactory;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.HistoryEntry;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitPiece;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitResource;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.AppExecutors;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.HistoryUtils;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.ImageUtils;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.JsonUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/* Database branch.  Initial work on this branch ended 10/7/18.
   This branch attempts to track each exhibit's history separately.
   Abandoned in favor of tracking overall viewing history as one single stack.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    MediaPlayerFragment.OnFragmentInteractionListener,
                    ResourceListFragment.OnListFragmentInteractionListener {

    // Request code for launching camera app
    private static final int REQUEST_CAMERA_IMAGE = 1;
    // Max distance (meters) from JFK museum to be considered "at museum."
    private static final float MAX_DISTANCE_TO_MUSEUM = (float) 100.0;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.duniganatlee.fileprovider";
    private static final String PIECE_ID = "piece_id";
    private static final int WELCOME_ID = 0;
    // Path for a temporary photo taken when user scans a barcode.
    private String mTempPhotoPath;
    private FusedLocationProviderClient mFusedLocationClient;
    // Constants for checking permissions.
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    // Whether the user is currently at the JFK Hyannis museum
    private boolean mAtMuseum = false;

    private String mExhibitsJson = null;
    private Exhibit[] mExhibitsList;
    private AppDatabase mHistoryDb;
    private int mPieceId;
    private int mExhibitId;
    private int mNextId = HistoryEntry.NONE;
    private int mPreviousId = HistoryEntry.NONE;
    private HistoryEntry historyEntryForCurrentPiece;
    private List<HistoryEntry> mHistoryForCurrentExhibit = new ArrayList<>();
    private ExhibitPiece mPiece;
    private String mResourceURL;

    // Views for ButterKnife binding.
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.piece_description_text_view) TextView pieceDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

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
            Log.d(PIECE_ID, "Getting ID from savedInstanceState");
            mPieceId = savedInstanceState.getInt(PIECE_ID);
        } else {
            mPieceId = WELCOME_ID;
        }

        // Populate views
        loadNewPiece(mPieceId);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        // Add exhibit titles to navigation drawer menu.
        // https://freakycoder.com/android-notes-53-how-to-create-menu-item-for-navigationdrawer-programmatically-67ddfa8027bc
        // Note that Menu.findItem(id) finds by *resource* id, whereas Menu.getItem(index) gets by position.
        // https://developer.android.com/reference/android/view/Menu#findItem(int)
        SubMenu exhibitsMenu = navigationView.getMenu().findItem(R.id.nav_exhibits_section).getSubMenu();
        for (Exhibit exhibit: mExhibitsList) {
            exhibitsMenu.add(exhibit.getExhibitTitle())
                .setIcon(R.drawable.ic_menu_gallery);
        }

        // Get location services client.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up ViewModel
        setUpViewModel();


    }

    @Override
    public void onBackPressed() {
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
                Log.d("Barcode found", barcodeContent);
                processBarcode(barcode);
            } else if (barcodes.size() == 0) {
                Toast.makeText(this, getString(R.string.no_barcode_in_image), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.multiple_barcodes_in_image), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void processBarcode(Barcode barcode) {
        // Check that the user has scanned a valid barcode for this app.
        if (barcode.valueFormat != Barcode.TEXT) {
            Toast.makeText(this, getString(R.string.invalid_barcode), Toast.LENGTH_LONG).show();
        } else /* We have a text barcode */ {
            String barcodeText = barcode.displayValue;
            if (!barcodeText.startsWith("JFK Hyannis Museum")) {
                Toast.makeText(this, getString(R.string.invalid_barcode), Toast.LENGTH_LONG).show();
            } else /* We have a barcode for this app. */ {
                String splitBarcodeText[] = barcodeText.split("\n");
                try {
                    // TODO: Remove exhibit ID from barcode format?
                    int barcodeExhibitId = Integer.parseInt(splitBarcodeText[1]);
                    int barcodePieceId = Integer.parseInt(splitBarcodeText[2]);
                    Log.d("Barcode Exhibit",Integer.toString(barcodeExhibitId));
                    Log.d("Barcode Piece",Integer.toString(barcodePieceId));
                    loadNewPiece(barcodePieceId);
                } catch (IndexOutOfBoundsException ex) {
                    Log.d("Barcode", "Bad barcode read.  Exhibit or piece ID not found.");
                }
            }
        }
    }

    private void loadNewPiece(int newPieceId) {
        // Exhibit ID is encoded in piece ID:
        int exhibitId = getExhibitId(newPieceId);
        Exhibit exhibit = Exhibit.getExhibitById(mExhibitsList, exhibitId);
        if (exhibit == null) {
            // TODO: Handle this error case.
            Log.e(PIECE_ID, "exhibitId did not correspond to any known Exhibit");
            return;
        } else {
            ExhibitPiece piece = exhibit.getPieceById(newPieceId);
            if (piece == null) {
                // TODO: Handle this error case.
                Log.e(PIECE_ID, "pieceId did not correspond to any known ExhibitPiece");
                return;
            } else {
                mPieceId = newPieceId;
                mPiece = piece;
                mExhibitId = exhibitId;
                Log.d(PIECE_ID,Integer.toString(mPieceId));
            }
        }

        // TODO: Check database to see if we've viewed this one.
        // Update history next and previous.
        int finalEntryId;
        final HistoryEntry finalEntry = HistoryUtils.getFinalEntry(mHistoryForCurrentExhibit);
        if (finalEntry != null) {
            finalEntry.setNextPiece(newPieceId);
            finalEntryId = finalEntry.getPieceId();
        } else {
            finalEntryId = HistoryEntry.NONE;
        }
        final HistoryEntry newEntry = new HistoryEntry(mPieceId, mExhibitId, finalEntryId, HistoryEntry.NONE);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mHistoryDb.historyDao().updateOrAddToHistory(newEntry);
                if (finalEntry != null) {
                    mHistoryDb.historyDao().updateOrAddToHistory(finalEntry);
                }
            }
        });


        // Replace the media player fragment and the resource list fragment.
        // By default, load the piece narration and description, which is the first resource.
        // TODO: Create helper functions in ExhibitPiece to get narration and background.
        ExhibitResource resource = mPiece.getResources().get(0);
        MediaPlayerFragment mediaPlayerFragment = MediaPlayerFragment
                .newInstance(resource.getResourceURL(), resource.getBackgroundImageURL());
        ResourceListFragment resourceListFragment = ResourceListFragment.newInstance(mPiece);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.media_player_container, mediaPlayerFragment)
                .replace(R.id.resource_list_container,resourceListFragment)
                .commit();

        // Swap out the description
        pieceDescriptionTextView.setText(mPiece.getDescription());
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

    // Set up ViewModel.  This should observe the database for items in the current exhibit.
    // If the database is changed, mHistoryForCurrentExhibit will be updated to reflect the change.
    // TODO: set up the view model again if the user changes to a different exhibit?
    private void setUpViewModel() {
        ExhibitHistoryViewModelFactory factory = new ExhibitHistoryViewModelFactory(mHistoryDb, mExhibitId);
        final ExhibitHistoryViewModel viewModel = ViewModelProviders.of(this, factory).get(ExhibitHistoryViewModel.class);
        viewModel.getExhibitHistory().observe(this, new Observer<List<HistoryEntry>>() {
            @Override
            public void onChanged(@Nullable List<HistoryEntry> historyEntries) {
                mHistoryForCurrentExhibit = historyEntries;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PIECE_ID, mPieceId);
    }

    @Override
    public void onListFragmentInteraction(ExhibitResource resource) {
        // Change the media player to the resource that was clicked.
        MediaPlayerFragment mediaPlayerFragment = MediaPlayerFragment
                .newInstance(resource.getResourceURL(), resource.getBackgroundImageURL());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.media_player_container, mediaPlayerFragment)
                .commit();
    }

    private int getExhibitId(int pieceId) {
        return pieceId / 1000;
    }
}
