package org.jfkhyannismuseum.enhancedtour;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import org.jfkhyannismuseum.enhancedtour.alert_dialogs.LocationAlertDialog;
import org.jfkhyannismuseum.enhancedtour.alert_dialogs.NoNetworkAlertDialog;
import org.jfkhyannismuseum.enhancedtour.database.AppDatabase;
import org.jfkhyannismuseum.enhancedtour.database.ExhibitHistoryViewModel;
import org.jfkhyannismuseum.enhancedtour.database.HistoryEntry;
import org.jfkhyannismuseum.enhancedtour.model.Exhibit;
import org.jfkhyannismuseum.enhancedtour.model.ExhibitPiece;
import org.jfkhyannismuseum.enhancedtour.utils.AppExecutors;
import org.jfkhyannismuseum.enhancedtour.utils.HistoryUtils;
import org.jfkhyannismuseum.enhancedtour.utils.ImageUtils;
import org.jfkhyannismuseum.enhancedtour.utils.JsonUtils;
import org.jfkhyannismuseum.enhancedtour.utils.LocationUtils;
import org.jfkhyannismuseum.enhancedtour.utils.NetworkUtils;
import org.jfkhyannismuseum.enhancedtour.utils.PreferenceUtils;
import org.jfkhyannismuseum.enhancedtour.widget.MuseumHistoryWidgetProvider;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LocationAlertDialog.LocationAlertListener,
        NoNetworkAlertDialog.NoNetworkAlertListener {

    // Global constants.
    private static final int REQUEST_CAMERA_IMAGE = 1; // Request code for launching camera app
    private static final String FILE_PROVIDER_AUTHORITY = "com.duniganatlee.fileprovider";
    public static final String PIECE_ID = "piece_id";
    public static final String HISTORY_POSITION = "history_position";
    public static final int WELCOME_ID = 0;
    private static final int HISTORY_END = -1;
    private static final String LOCATION_FRAGMENT_TAG = "location";
    private static final String NO_NETWORK_FRAGMENT_TAG = "no_network";
    private static final String NO_NETWORK_WARNING = "Network is not available.";
    private static final String LOG_TAG = "Main Activity";
    private static final String TEMP_PHOTO_PATH = "temp_photo_path";
    private static final String EXHIBITS_JSON = "exhibits_json";

    // Views for ButterKnife binding.
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab_camera) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.view_pager) ViewPager viewPager;

    // Path for a temporary photo taken when user scans a barcode.
    private String mTempPhotoPath;

    private Exhibit[] mExhibitsList;
    private AppDatabase mHistoryDb;
    private List<HistoryEntry> mHistory;
    HistoryPagerAdapter mPagerAdapter;
    private int mHistoryPosition;
    private String mExhibitsJson;

    private boolean setUpViewModelFinished;
    private boolean databaseUpdatePending;
    private int pendingPieceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Creating Main Activity...");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        viewPager.addOnPageChangeListener(pageChangeListener);
        setTitle(R.string.app_short_name);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromCamera();
            }
        });

        // Check whether user is at the JFK Hyannis Museum.
        // If so, LocationUtils.userIsAtMuseum will be set to true.
        LocationUtils.checkLocation(this);

        // Get instance of viewing history database.
        mHistoryDb = AppDatabase.getInstance(getApplication());

        // Set up ActionBar toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Find out which position in the history we'd like to view.
        // This could come from the savedInstanceState
        // or from Intent extras (when launched by the MuseumHistoryWidgetProvider).
        // If none of the above, view the end of the history stack (last piece viewed).
        Intent sendingIntent = getIntent();
        databaseUpdatePending = false;
        setUpViewModelFinished = false;
        if (savedInstanceState != null) {
            Log.d(LOG_TAG, "Getting values from savedInstanceState.");
            mHistoryPosition = savedInstanceState.getInt(HISTORY_POSITION, HISTORY_END);
            mTempPhotoPath = savedInstanceState.getString(TEMP_PHOTO_PATH);
            // Retrieve the json, which *should* also re-instate the previous mHistory.
            String exhibitsJson = savedInstanceState.getString(EXHIBITS_JSON);
            processExhibitsJson(exhibitsJson);
        } else {
            if (sendingIntent != null && sendingIntent.hasExtra(HISTORY_POSITION)) {
                Log.d(LOG_TAG, "Getting values from sending Intent.");
                mHistoryPosition = sendingIntent.getIntExtra(HISTORY_POSITION, HISTORY_END);
            } else {
                Log.d(LOG_TAG, "Using default values.  No instance state found.");
                mHistoryPosition = HISTORY_END;
            }
            /* Use an AsyncTask to load the JSON file from the web. */
            // TODO: Maybe move this to after the checkForNetwork() in onResume()
            // TODO: Check for local copy first?  (But how to know if it's been updated?)
            if (NetworkUtils.deviceIsConnected(this)) {
                String jsonUrlString = getString(R.string.json_url);
                URL jsonUrl = NetworkUtils.buildUrl(jsonUrlString);
                JsonQueryTask queryTask = new JsonQueryTask(this);
                queryTask.execute(jsonUrl);
            } else {
                Toast.makeText(this, NO_NETWORK_WARNING, Toast.LENGTH_LONG).show();
                // TODO: Check if json is stored locally.
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForNetwork();
    }

    private void checkForNetwork() {
        if (!NetworkUtils.deviceIsConnected(this)) {
            DialogFragment noNetworkAlertDialog = new NoNetworkAlertDialog();
            noNetworkAlertDialog.show(getSupportFragmentManager(), NO_NETWORK_FRAGMENT_TAG);
        }
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
        } else if (id == R.id.get_tickets) {
            launchTicketPurchase();
        } else if (id == R.id.visit_website) {
            visitWebsite();
        } 
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Basic process taken from https://developer.android.com/training/camera/photobasics
    // Also modeled after the Emojify app
    private void getImageFromCamera() {
        if (!LocationUtils.userIsAtMuseum) {
        // if (false) {
            DialogFragment locationAlertDialog = new LocationAlertDialog();
            locationAlertDialog.show(getSupportFragmentManager(), LOCATION_FRAGMENT_TAG);
            return;
        }
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
                Log.d("Setting mTempPhotoPath",mTempPhotoPath);

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
    // Note that this is called *before* onResume(), see
    // https://developer.android.com/reference/android/app/Activity#onActivityResult(int,%20int,%20android.content.Intent)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_CAMERA_IMAGE && resultCode == RESULT_OK) {
            // Extract the barcode and use its information.
            Integer pieceId = ImageUtils.getBarcodeFromImage(this, mTempPhotoPath);
            if (pieceId == null) {
                Toast.makeText(this, getString(R.string.invalid_barcode), Toast.LENGTH_LONG).show();
            } else {
                updateDatabase(pieceId);
            }
        } else {
            // Otherwise, delete the temporary image file
            ImageUtils.deleteTemporaryImageFile(this, mTempPhotoPath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Destroying Main Activity...");
    }

    // Make sure current piece is valid.
    // This is called from the ViewModel observer's onChange() method.
    private boolean pieceIsValid(int pieceId) {
        // Exhibit ID is encoded in piece ID:
        int exhibitId = Exhibit.getExhibitId(pieceId);
        Exhibit exhibit = Exhibit.getExhibitById(mExhibitsList, exhibitId);
        if (exhibit == null) {
            Log.e(PIECE_ID, "exhibitId " + exhibitId + " does not correspond to any known Exhibit");
            return false;
        }
        ExhibitPiece piece = exhibit.getPieceById(pieceId);
        if (piece == null) {
            Log.e(PIECE_ID, "pieceId " + pieceId + " did not correspond to any known ExhibitPiece");
            return false;
        }
        // Piece is valid.
        return true;
    }

    // This method updates the database when a new piece is viewed.
    // Updating the database will trigger a change observed by the ViewModel,
    // which will in turn update the adapter on the ViewPagerFragment to show the
    // new piece in the UI.
    private void updateDatabase(int newPieceId) {
        // If the device is rotated while the camera activity is open, upon return this
        // updateDatabase method may be called *before* the viewModel has loaded the current
        // state of the database, causing asynchronous havoc.  Therefore, check if setupViewModel has finished
        // before running this method.
        if (!setUpViewModelFinished) {
            Log.d("updateDatabase","Pending database update.  Waiting for ViewModel to get current database state.");
            databaseUpdatePending = true;
            pendingPieceId = newPieceId;
            return;
        }
        // This will cause the ViewPager to scroll to the new piece after we are done:
        mHistoryPosition = HISTORY_END;
        // We want to collect all entries to be updated in the db,
        // so we can make just one insertion/update operation; otherwise
        // onChange() may be called multiple times for this one change.
        final List<HistoryEntry> entriesToUpdate = new ArrayList<>();
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
                for (HistoryEntry entry: entriesToUpdate) {
                    String logMessage = "Updating dabase. ID: " + entry.getPieceId()
                            + " Previous: " + entry.getPreviousPiece()
                            + " Next: " + entry.getNextPiece();
                    Log.d("updateDatabase", logMessage);
                }
                mHistoryDb.historyDao().updateOrAddToHistory(entriesToUpdate);
            }
        });
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
                if (mHistory == null) {
                    Log.e("onChanged","Null history returned.");
                    return;
                }
                Log.d("Database change", "New history size " + mHistory.size());
                if (mHistory.size() == 0) {
                    // History is empty, so add Welcome as the first entry.  This will
                    // re-trigger onChanged and then we'll display the viewPager.
                    final HistoryEntry firstEntry = new HistoryEntry(WELCOME_ID,
                            Exhibit.getExhibitId(WELCOME_ID), HistoryEntry.NONE, HistoryEntry.NONE);
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("DBViewModel", "History empty, so adding Welcome to history.");
                            mHistoryDb.historyDao().addToHistory(firstEntry);
                        }
                    });
                } else {
                    // Set current piece info (mPieceId, mExhibitId, mPiece).
                    HistoryEntry entry;
                    if (mHistoryPosition == HISTORY_END) {
                        entry = HistoryUtils.getFinalEntry(mHistory);
                    } else {
                        entry = HistoryUtils.getEntryByPosition(mHistory, mHistoryPosition);
                    }
                    if (entry == null || !pieceIsValid(entry.getPieceId())) {
                        // Something is wrong.  Fail gracefully.
                        Log.e(LOG_TAG, "Null entry or invalid piece id.");
                        Toast.makeText(getApplicationContext(),
                                R.string.invalid_history_entry_message,
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Inform widget that data set has changed so it can update listview.
                    Context context = getApplicationContext();
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MuseumHistoryWidgetProvider.class));
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_history);

                    // Re-set viewPager with updated history.
                    mPagerAdapter = new HistoryPagerAdapter(getSupportFragmentManager(), mHistory, mExhibitsList);
                    viewPager.setAdapter(mPagerAdapter);
                    if (mHistoryPosition == HISTORY_END) {
                        int historySize = mHistory.size();
                        // First set the current item as the one previous to what we want,
                        // so the user sees a smooth scroll in, of the new item.
                        // This helps teach the user that swiping left/right moves through the history.
                        // TODO: This doesn't seem to be working.
                        if (historySize > 1) {
                            viewPager.setCurrentItem(mHistory.size() - 2);
                        }
                        viewPager.setCurrentItem(mHistory.size() - 1, true);
                    } else {
                        viewPager.setCurrentItem(mHistoryPosition, true);
                    }

                }
                setUpViewModelFinished = true;
                if (databaseUpdatePending) {
                    databaseUpdatePending = false;
                    Log.d("onChanged","Resolving pending database update.");
                    updateDatabase(pendingPieceId);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "Saving Instance State.");
        outState.putInt(HISTORY_POSITION, mHistoryPosition);
        outState.putString(TEMP_PHOTO_PATH, mTempPhotoPath);
        outState.putString(EXHIBITS_JSON, mExhibitsJson);
    }

    @Override
    public void onLocationDialogPositiveClick(DialogFragment dialog) {
        launchTicketPurchase();
    }

    public void onLocationDialogNeutralClick(DialogFragment dialog) {
        LocationUtils.checkLocation(this);
    }

    @Override
    public void onNoNetworkDialogSettingsClick(DialogFragment dialog) {
        launchWirelessSettings();
    }

    @Override
    public void onNoNetworkDialogQuitClick(DialogFragment dialog) {
        finish();
    }

    @Override
    public void onNoNetworkDialogTryAgainClick(DialogFragment dialog) {
        checkForNetwork();
    }

    private void launchWirelessSettings() {
        // TODO: Should this be Settings.ACTION_WIFI_SETTINGS?  What's the difference?
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityWithTransition(intent);
        } else {
            Toast.makeText(this,getString(R.string.no_wireless_settings),Toast.LENGTH_LONG).show();
        }
    }

    private void launchTicketPurchase() {
        Uri webpage = Uri.parse(getString(R.string.museum_tickets_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityWithTransition(intent);
        } else {
            notifyNoWebBrowser();
        }
    }

    // Use transitions between activities.
    private void startActivityWithTransition(Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
            startActivity(intent, bundle);
        } else {
            startActivity(intent);
        }
    }

    private void notifyNoWebBrowser() {
        Toast.makeText(this,getString(R.string.no_web_browser),Toast.LENGTH_LONG).show();
    }

    private void visitWebsite() {
        Uri webpage = Uri.parse(getString(R.string.museum_website_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityWithTransition(intent);
        }
        else {
            notifyNoWebBrowser();
        }
    }

    private static final class JsonQueryTask extends AsyncTask<URL, Void, String> {
        // Avoid memory leaks with a WeakReference to the MainActivity.
        // See https://medium.com/google-developer-experts/finally-understanding-how-references-work-in-android-and-java-26a0d9c92f83
        private WeakReference<MainActivity> mainActivityWeakReference;
        private JsonQueryTask(MainActivity mainActivity) {
            mainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        protected String doInBackground(URL... urls) {
            String result = null;
            try {
                result = NetworkUtils.getResponseFromHttpUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String exhibitsJson) {
            super.onPostExecute(exhibitsJson);
            // Continue if MainActivity has not been killed.
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity != null) {
                mainActivity.processExhibitsJson(exhibitsJson);
            }
        }
    }

    private void processExhibitsJson(String exhibitsJson) {
        if (exhibitsJson == null) {
            // If we couldn't get a remote copy, check for a locally saved copy.
            Log.d(LOG_TAG, "Fetching JSON from shared preferences.");
            exhibitsJson = PreferenceUtils.getPreferenceExhibitsJson(getApplicationContext());
            if (exhibitsJson == null) {
                Log.e(LOG_TAG, "JSON fetch failed.  Using asset copy.");
                Toast.makeText(getApplicationContext(), R.string.no_json_message, Toast.LENGTH_LONG).show();
                // App will be packaged with a JSON asset, but changes to the JSON
                // after app release will be posted on the museum web site,
                // so the local asset is used only as a last resort:
                exhibitsJson = JsonUtils.loadJSONFromAsset(getApplicationContext());
            }
        }

        // Parse the json.
        mExhibitsJson = exhibitsJson;
        mExhibitsList = JsonUtils.parseExhibitList(exhibitsJson);

        // Add exhibit titles to navigation drawer menu.
        // https://freakycoder.com/android-notes-53-how-to-create-menu-item-for-navigationdrawer-programmatically-67ddfa8027bc
        // Note that Menu.findItem(id) finds by *resource* id, whereas Menu.getItem(index) gets by position.
        // https://developer.android.com/reference/android/view/Menu#findItem(int)
        SubMenu exhibitsMenu = navigationView.getMenu().findItem(R.id.nav_exhibits_section).getSubMenu();
        for (Exhibit exhibit: mExhibitsList) {
            exhibitsMenu.add(exhibit.getExhibitTitle())
                    .setIcon(R.drawable.ic_menu_gallery);
            // TODO: Add actions for clicking exhibits.
        }

        // Set up ViewModel.  This will trigger the observer's onChange, which will
        // take care of loading the UI by loading the ViewPagerFragment.
        setUpViewModel();
    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int newPosition) {
            int currentPosition = mHistoryPosition;
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            //Fragment fragmentToShow = mPagerAdapter.getItem(newPosition);
            //Fragment fragmentToHide = mPagerAdapter.getItem(currentPosition);
            for (Fragment fragment: fragments) {
                if (fragment instanceof ViewPagerFragment) {
                    Log.d("Fragment ID",Integer.toString(fragment.getId()));
                    ((ViewPagerFragment) fragment).pauseMedia();
                    if (fragment.getTag() != null) {
                        Log.d("Fragment Tag",fragment.getTag());
                    }
                }

            }
            mHistoryPosition = newPosition;
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) { }

        public void onPageScrollStateChanged(int arg0) { }
    };

}
