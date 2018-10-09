package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
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
import android.support.v4.view.ViewPager;
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
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.ExhibitHistoryViewModel;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.database.HistoryEntry;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitPiece;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.AppExecutors;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.HistoryUtils;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.ImageUtils;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.utils.JsonUtils;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.widget.MuseumHistoryWidget;
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


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Request code for launching camera app
    private static final int REQUEST_CAMERA_IMAGE = 1;
    // Max distance (meters) from JFK museum to be considered "at museum."
    private static final float MAX_DISTANCE_TO_MUSEUM = (float) 100.0;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.duniganatlee.fileprovider";
    public static final String PIECE_ID = "piece_id";
    public static final String EXHIBIT_ID = "exhibit_id";
    public static final String HISTORY_POSITION = "history_position";
    public static final int WELCOME_ID = 0;
    private static final int HISTORY_END = -1;

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
    // TODO: we probably no longer need mExhibitId.  Remove instances.
    private int mExhibitId;
    private int mNextId = HistoryEntry.NONE;
    private int mPreviousId = HistoryEntry.NONE;
    private HistoryEntry historyEntryForCurrentPiece;
    private List<HistoryEntry> mHistory = new ArrayList<>();
    private ExhibitPiece mPiece;
    private String mResourceURL;
    HistoryPagerAdapter mPagerAdapter;
    private int mHistoryPosition;

    // Views for ButterKnife binding.
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.view_pager) ViewPager viewPager;

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
        mHistoryDb = AppDatabase.getInstance(getApplication());

        // Find out which position in the history we'd like to view.
        // This could come from the savedInstanceState
        // or from Intent extras (when launched by the MuseumHistoryWidget).
        // If none of the above, view the end of the history stack (last piece viewed).
        Intent sendingIntent = getIntent();
        if (sendingIntent != null) {
            mHistoryPosition = sendingIntent.getIntExtra(HISTORY_POSITION, HISTORY_END);
        } else if (savedInstanceState != null) {
            mHistoryPosition = savedInstanceState.getInt(HISTORY_POSITION);
        } else {
            mHistoryPosition = HISTORY_END;
        }

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

        // Set up ViewModel.  This will trigger the observer's onChange, which will
        // take care of loading the UI by loading the ViewPagerFragment.
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
    // Specifically, the camera intent.
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
                    updateDatabase(barcodePieceId);
                } catch (IndexOutOfBoundsException ex) {
                    Log.d("Barcode", "Bad barcode read.  Exhibit or piece ID not found.");
                }
            }
        }
    }

    // Set member variables related to the current piece.
    // This is called from the ViewModel observer's onChange() method.
    private void setCurrentPiece(int pieceId) {
        // Set mPiece, mPieceId, and mExhibitId.
        // Exhibit ID is encoded in piece ID:
        int exhibitId = Exhibit.getExhibitId(pieceId);
        Exhibit exhibit = Exhibit.getExhibitById(mExhibitsList, exhibitId);
        if (exhibit == null) {
            // TODO: Handle this error case.
            Log.e(PIECE_ID, "exhibitId did not correspond to any known Exhibit");
            return;
        } else {
            ExhibitPiece piece = exhibit.getPieceById(pieceId);
            if (piece == null) {
                // TODO: Handle this error case.
                Log.e(PIECE_ID, "pieceId did not correspond to any known ExhibitPiece");
                return;
            } else {
                mPieceId = pieceId;
                mPiece = piece;
                mExhibitId = exhibitId;
            }
        }
    }

    // This method updates the database when a new piece is viewed.
    // Updating the database will trigger a change observed by the ViewModel,
    // which will in turn update the adapter on the ViewPagerFragment to show the
    // new piece in the UI.
    private void updateDatabase(int newPieceId) {
        // This will cause the ViewPager to scroll to the new piece:
        mHistoryPosition = HISTORY_END;
        // We want to collect all entries to be updated in the db,
        // so we can make just one insertion/update operation; otherwise
        // onChange() may be called multiple times for this one change.
        final List<HistoryEntry> entriesToUpdate = new ArrayList<>();
        // Update history next and previous.
        Log.d("updateDatabase", "Current history size " + mHistory.size());
        int finalEntryId;
        // Get the entry that is currently last on the stack
        final HistoryEntry finalEntry = HistoryUtils.getFinalEntry(mHistory);
        // if finalEntry is null, this is the first piece visited, so no need to update previous entry.
        if (finalEntry != null) {
            finalEntry.setNextPiece(newPieceId);
            finalEntryId = finalEntry.getPieceId();
            // Check if the new piece is the same as the final one on the stack;
            // if so, no need to update db.  But we do want to page to that piece if we're
            // currently viewing another.
            if (finalEntryId == newPieceId) {
                viewPager.setCurrentItem(mHistory.size()-1);
                return;
            } else {
                entriesToUpdate.add(finalEntry);
            }
        } else {
            finalEntryId = HistoryEntry.NONE;
        }

        // Check if we've viewed this piece before:
        HistoryEntry previousInstance = HistoryUtils.getEntryById(mHistory, newPieceId);
        // If previousInstance is not null, user has viewed this piece before.
        // Therefore we need to link this entry's previous to its next
        // (i.e. remove this entry from its previous place in the linked list).
        if (previousInstance != null) {
            final HistoryEntry previousEntry = HistoryUtils.getEntryById(mHistory, previousInstance.getPreviousPiece());
            final HistoryEntry nextEntry = HistoryUtils.getEntryById(mHistory, previousInstance.getNextPiece());
            if (previousEntry != null) {
                if (nextEntry != null) {
                    previousEntry.setNextPiece(nextEntry.getPieceId());
                    nextEntry.setPreviousPiece(previousEntry.getPieceId());
                } else {
                    // nextEntry is null, so set previousEntry's nextPiece to NONE
                    // This *should* only happen when new piece is same as final piece,
                    // in which case we already returned above; but we'll check just in case.
                    previousEntry.setNextPiece(HistoryEntry.NONE);
                }
            } else {
                // previousEntry is null, so if necessary set nextEntry's previousPiece to NONE.
                // This happens if the piece was previously first on the stack.
                if (nextEntry != null) {
                    nextEntry.setPreviousPiece(HistoryEntry.NONE);
                }
            }
            // Add previousEntry and nextEntry, if applicable, to the update list.
            if (previousEntry != null) {
                entriesToUpdate.add(previousEntry);
            }
            if (nextEntry != null) {
                entriesToUpdate.add(nextEntry);
            }
        }
        // Now create the new entry and update the old final entry if necessary.
        final HistoryEntry newEntry = new HistoryEntry(newPieceId,
                Exhibit.getExhibitId(newPieceId), finalEntryId, HistoryEntry.NONE);
        entriesToUpdate.add(newEntry);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // As we're using a ViewModel observer, these database updates will
                // be automatically added to the mHistory field.
                mHistoryDb.historyDao().updateOrAddToHistory(entriesToUpdate);
            }
        });
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
    // If the database is changed, mHistory will be updated to reflect the change.
    // The viewPager depends on the history, so it will also be assigned a new adapter.
    private void setUpViewModel() {
        Log.d("setUpViewModel","Setting up view model...");
        final ExhibitHistoryViewModel viewModel = ViewModelProviders.of(this).get(ExhibitHistoryViewModel.class);
        viewModel.getHistory().observe(this, new Observer<List<HistoryEntry>>() {
            @Override
            public void onChanged(@Nullable List<HistoryEntry> historyEntries) {
                mHistory = historyEntries;
                Log.d("Database change", "New history size " + mHistory.size());
                if (mHistory.size() == 0) {
                    // History is empty, so add Welcome as the first entry.  This will
                    // re-trigger onChanged and then we'll display the viewPager.
                    final HistoryEntry firstEntry = new HistoryEntry(WELCOME_ID,
                            Exhibit.getExhibitId(WELCOME_ID), HistoryEntry.NONE, HistoryEntry.NONE);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mHistoryDb.historyDao().addToHistory(firstEntry);
                        }
                    });
                } else {
                    // Set current piece info (mPieceId, mExhibitId, mPiece).
                    if (mHistoryPosition == HISTORY_END) {
                        setCurrentPiece(HistoryUtils.getFinalEntry(mHistory)
                                .getPieceId());
                    } else {
                        setCurrentPiece(HistoryUtils.getEntryByPosition(mHistory, mHistoryPosition)
                                .getPieceId());
                    }

                    // Inform widget that data set has changed so it can update listview.
                    Context context = getApplicationContext();
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MuseumHistoryWidget.class));
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_history);

                    // Re-set viewPager with updated history.
                    mPagerAdapter = new HistoryPagerAdapter(getSupportFragmentManager(), mHistory, mExhibitsList);
                    viewPager.setAdapter(mPagerAdapter);
                    if (mHistoryPosition == HISTORY_END) {
                        viewPager.setCurrentItem(mHistory.size() - 1);
                    } else {
                        viewPager.setCurrentItem(mHistoryPosition);
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(HISTORY_POSITION, mHistoryPosition);
    }

}
