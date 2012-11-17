package org.outlander.activities;

import org.andnav.osm.OpenStreetMapActivity;
import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.outlander.R;
import org.outlander.fragments.AboutDialogFragment;
import org.outlander.fragments.ErrorDialogFragment;
import org.outlander.fragments.FragmentFactory;
import org.outlander.fragments.MapFragment;
import org.outlander.fragments.WhatsNewDialogFragment;
import org.outlander.io.db.DBManager;
import org.outlander.search.SearchSuggestionsProvider;
import org.outlander.trackwriter.ITrackWriterService;
import org.outlander.trackwriter.TrackWriterService;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.CrashReportHandler;
import org.outlander.utils.ProximityHandler;
import org.outlander.utils.StrictModeWrapper;
import org.outlander.utils.Ut;
import org.outlander.views.ViewPagerHelper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends OpenStreetMapActivity implements OpenStreetMapConstants {

    // ===========================================================
    // Constants
    // ===========================================================

    public final static int         ACTIVITY_ID_DATA       = 54321;
    public final static int         ACTIVITY_ID_PREF       = 12345;

    // ===========================================================
    // Fields
    // ===========================================================
    private int                     activeFragment;
    private final boolean           fragmentVisible        = true;
    private PowerManager.WakeLock   myWakeLock;
    private boolean                 mFullScreen;
    private SensorManager           mSensorManager;
    private int                     mScreenOrientation;
    private DBManager               mPoiManager;

    private SharedPreferences       sharedPreferences;
    //
    private FrameLayout             leftPane;

    private MapFragment             mapFragment;
    private ProximityHandler        proximityHandler;

    private ITrackWriterService     trackRecordingService;

    /**
     * True if a new track should be created after the track recording service
     * binds.
     */
    private boolean                 startNewTrackRequested = false;
    /**
     * Whether {@link #serviceConnection} is bound or not.
     */
    private boolean                 isBound                = false;
    /**
     * The connection to the track recording service.
     */
    private final ServiceConnection serviceConnection      = new ServiceConnection() {

                                                               @Override
                                                               public void onServiceConnected(final ComponentName className, final IBinder service) {
                                                                   Ut.d("TrackWriterService now connected.");
                                                                   // Delay
                                                                   // setting
                                                                   // the
                                                                   // service
                                                                   // until we
                                                                   // are
                                                                   // done with
                                                                   // initialization.
                                                                   final ITrackWriterService trackRecordingService = ITrackWriterService.Stub
                                                                           .asInterface(service);
                                                                   try {
                                                                       // TODO:
                                                                       // Send
                                                                       // a
                                                                       // start
                                                                       // service
                                                                       // intent
                                                                       // and
                                                                       // broadcast
                                                                       // service
                                                                       // started
                                                                       // message
                                                                       // to
                                                                       // avoid
                                                                       // the
                                                                       // hack
                                                                       // below
                                                                       // and a
                                                                       // race
                                                                       // condition.
                                                                       if (startNewTrackRequested) {
                                                                           startNewTrackRequested = false;
                                                                           startRecordingNewTrack(trackRecordingService);
                                                                       }
                                                                   }
                                                                   finally {
                                                                       MainActivity.this.trackRecordingService = trackRecordingService;
                                                                   }
                                                               }

                                                               @Override
                                                               public void onServiceDisconnected(final ComponentName className) {
                                                                   Ut.d("Service now disconnected.");
                                                                   trackRecordingService = null;
                                                               }
                                                           };

    // private MyPagerAdapter mPagerAdapter;
    // private ViewPager mViewPager;
    // private TitlePageIndicator mIndicator;

    // ===========================================================
    // Constructors
    // ===========================================================

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        if (OpenStreetMapViewConstants.DEBUGMODE) {
            android.os.Debug.startMethodTracing("lsd");
        }

        super.onCreate(savedInstanceState, false); // Pass true here to actually
                                                   // contribute to OSM!

        final int applicationFlags = getApplicationInfo().flags;
        if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            CrashReportHandler.attach(this);
        }

        if (OpenStreetMapViewConstants.DEBUGMODE) {
            try {
                StrictModeWrapper.init(this);
            }
            catch (final Throwable throwable) {
                Ut.i("StrictMode is not available!");
            }
        }
        CoreInfoHandler.getInstance().setMainActivity(this);

        setupActionBar();

        initializeAllSensors();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.main); // might be orientationspecific !!

        setupPager();

        restoreUIState();

        checkQuery();

        setupProximityHandler();

        startLocationService();
    }

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    private void checkQuery() {
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();

        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(queryIntent);
        }
        else if (Intent.ACTION_VIEW.equalsIgnoreCase(queryAction)) {
            doShowExternalPoint(queryIntent);
        }

    }

    private void setupPager() {

        // CoreInfoHandler
        // .getInstance()
        // .setPageSet(
        // Ut.isMultiPane(this) ? FragmentFactory.FRAGMENT_PAGES_TAB_TABLETT
        // : FragmentFactory.FRAGMENT_PAGES_TAB_PORTRAIT);
        //
        // mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),
        // (Ut.isMultiPane(this)));

        if (Ut.isMultiPane(this)) {
            // if we are multipane then we have to load

            // View view = findViewById(R.id.main);

            // try these
            leftPane = (FrameLayout) findViewById(R.id.frame_fragment);
            // rightPane = (FrameLayout) findViewById(R.id.frame_map);
            // parentLayout = (LinearLayout) findViewById(R.id.parentframe);

            final View view = new ViewPagerHelper().getViewPagerView(this);

            // add pager view to left frame
            leftPane.addView(view);

        }

        mapFragment = (MapFragment) FragmentFactory.getFragmentPage(FragmentFactory.FRAG_ID_MAP);

        replaceFragment(R.id.frame_map, mapFragment);

    }

    private void setupActionBar() {

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        // if (Ut.isMultiPane(this)) {
        // getSupportActionBar().setNavigationMode(
        // ActionBar.NAVIGATION_MODE_STANDARD);
        //
        // getSupportActionBar().setDisplayUseLogoEnabled(false);
        //
        // } else {
        // // requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        // //
        // getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));
        // // getSupportActionBar().hide();
        // //
        // getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));
        // getSupportActionBar().hide();
        // }
    }

    private void setupProximityHandler() {
        proximityHandler = new ProximityHandler(this, getLocationManager());
        proximityHandler.registerProximityTargetsFromDB();

    }

    @Override
    protected void onDestroy() {

        tryUnbindTrackRecordingService();

        proximityHandler.unregisterAllProximityTargets();
        proximityHandler.unregisterReceiver();

        try {
            mPoiManager.freeDatabases();
            CoreInfoHandler.getInstance().setDBManager(null);
        }
        catch (final Exception e) {
            Ut.d(e.toString());
        }
        super.onDestroy();
    }

    private void initializeAllSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        toggleOrientationSensor(true);
        toggleAccelerationSensor(true);
    }

    private void toggleOrientationSensor(final boolean switchOn) {
        if (switchOn) {
            mSensorManager.registerListener(CoreInfoHandler.getInstance().getOrientationListener(), mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_UI);
        }
        else {
            mSensorManager.unregisterListener(CoreInfoHandler.getInstance().getOrientationListener());
        }
    }

    private void toggleAccelerationSensor(final boolean switchOn) {
        if (switchOn) {
            final Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            final Sensor magField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(CoreInfoHandler.getInstance().getAccellerationListener(), accelerometer, SensorManager.SENSOR_DELAY_UI);// SENSOR_DELAY_FASTEST);

            mSensorManager.registerListener(CoreInfoHandler.getInstance().getAccellerationListener(), magField, SensorManager.SENSOR_DELAY_UI);// SENSOR_DELAY_FASTEST);
        }
        else {
            mSensorManager.unregisterListener(CoreInfoHandler.getInstance().getAccellerationListener());
        }
    }

    boolean isIn;

    @TargetApi(11)
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // Get the SearchView and set the searchable configuration
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true); // Do not iconify the
                                                    // widget;
                                                    // expand it by default
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryRefinementEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // super.onOptionsItemSelected(item);
        boolean result = false;
        switch (item.getItemId()) {

        // case (R.id.switchview):
        // if (!Ut.isMultiPane(this)) {
        // // flipViewMode(++activePage);
        // } else {
        // // flip instruments
        // }
        // result = true;
        // break;

            case (R.id.settings): {
                startActivityForResult(new Intent(this, org.outlander.activities.PreferencesActivity.class), ACTIVITY_ID_PREF);

                result = true;
                break;
            }
            case android.R.id.home: {

                CoreInfoHandler.getInstance().gotoPage(0);

                result = true;
                break;
            }
            case (R.id.about): {
                // showDialogPrivate(R.id.about);

                final AboutDialogFragment newFragment = AboutDialogFragment.newInstance();
                newFragment.show(getSupportFragmentManager(), "dialog");

                result = true;
                break;
            }
            case (R.id.showhideFragment): {
                showHideLeftPane(leftPane.getVisibility() == View.INVISIBLE);
                result = true;
                break;
            }
            case (R.id.menu_search): {
                onSearchRequested();
                result = true;
                break;
            }
        }
        return result;
    }

  

    @Override
    public void onLocationLost() {

        // NOOP ?!?
    }

    @Override
    public void onLocationChanged(final Location loc) {

        CoreInfoHandler.getInstance().getLocationListener().onLocationChanged(loc);
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {

        CoreInfoHandler.getInstance().getLocationListener().onStatusChanged(provider, status, extras);
    }

    @Override
    protected void onPause() {
        final SharedPreferences uiState = getPreferences(0);
        final SharedPreferences.Editor editor = uiState.edit();

        editor.putInt("activeFragment", activeFragment);
        editor.putBoolean("fragmentVisible", fragmentVisible);
        if (CoreInfoHandler.getInstance().getViewPager() != null) {
            editor.putInt("PageInFlipper", CoreInfoHandler.getInstance().getViewPager().getCurrentItem());
        }

        editor.commit();

        if (myWakeLock != null) {
            myWakeLock.release();
        }

        toggleAccelerationSensor(false);
        toggleOrientationSensor(false);

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences uiState = getPreferences(0);

        final int page = uiState.getInt("PageInFlipper", 0);
        if (CoreInfoHandler.getInstance().getViewPager() != null) {
            CoreInfoHandler.getInstance().getViewPager().setCurrentItem(page); // TODO:
                                                                               // ???
                                                                               // move
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (pref.getBoolean("pref_keepscreenon", true)) {
            myWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "OutLander");
            myWakeLock.acquire();
        }
        else {
            myWakeLock = null;
        }

        toggleAccelerationSensor(true);
        toggleOrientationSensor(true);

        tryBindTrackRecordingService();

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void restoreUIState() {

        mScreenOrientation = Integer.parseInt(sharedPreferences.getString("pref_screen_orientation", "-1"));
        setRequestedOrientation(mScreenOrientation);

        mFullScreen = sharedPreferences.getBoolean("pref_showstatusbar", true);
        if (mFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        final SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);

        if (settings.getString("error", "").length() > 0) {
            final ErrorDialogFragment newFragment = ErrorDialogFragment.newInstance();
            newFragment.show(getSupportFragmentManager(), "dialog");

        }

        if (!settings.getString("app_version", "").equalsIgnoreCase(Ut.getAppVersion(this))) {
            final WhatsNewDialogFragment newFragment = WhatsNewDialogFragment.newInstance();
            newFragment.show(getSupportFragmentManager(), "dialog");

        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case R.id.menu_importpoi:
                if (resultCode == Activity.RESULT_OK) {
                    final int pois = data.getIntExtra(ImportPoiActivity.RESPONSE_NRPOIS, 0);
                    final int routes = data.getIntExtra(ImportPoiActivity.RESPONSE_NRROUTES, 0);
                    final int tracks = data.getIntExtra(ImportPoiActivity.RESPONSE_NRTRACKS, 0);
                    Toast.makeText(this, "Pois imported:   " + pois + "\n" + "Routes imported: " + routes + "\n" + "Tracks imported: " + tracks + "\n",
                            Toast.LENGTH_LONG).show();
                    // FillData();
                }
                break;
            case ACTIVITY_ID_PREF: // R.id.settings_activity_closed:
                finish();
                startActivity(new Intent(this, this.getClass()));
                break;
            case ACTIVITY_ID_DATA: // check detailed returncode
            {
                mapFragment.pageGetsActivated();
                Ut.d("Data Activity returned");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSearchRequested() {
        startSearch("", false, null, false);
        return true;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        final String queryAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(intent);
        }
    }

    private void doSearchQuery(final Intent queryIntent) {

        final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);

        // switch to ToponymSearchActivity in case...

        // Record the query string in the recent queries suggestions provider.
        final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionsProvider.AUTHORITY, SearchSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(queryString, null);
        showSearchQuery(queryString);
    }

    private void showSearchQuery(final String queryString) {
        CoreInfoHandler.getInstance().setTopoSearchString(queryString);

        final SwitchPageTask spt = new SwitchPageTask();

        spt.execute(FragmentFactory.FRAG_ID_TOPOSEARCH);

    }

    private void doShowExternalPoint(final Intent queryIntent) {

        final ShowExternalPointTask showExternalPoint = new ShowExternalPointTask();

        showExternalPoint.execute(new Intent[] { queryIntent });
    }

    private void replaceFragment(final int id, final Fragment fragment) {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        transaction.replace(id, fragment);
        // transaction.remove(fragment);
        // transaction.add(id, fragment);
        transaction.addToBackStack("");
        transaction.commit();
    }

    public void showHideLeftPane(final boolean show) {
        if ((leftPane != null) && (((!show) && (leftPane.getVisibility() == View.VISIBLE)) || ((show) && (leftPane.getVisibility() == View.INVISIBLE)))) {
            leftPane.startAnimation(AnimationUtils.loadAnimation(this, (show ? android.R.anim.slide_in_left : android.R.anim.slide_out_right)));
            leftPane.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void showDialog(final DialogFragment dialog) {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        final Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");

        if (prev != null) {
            transaction.remove(prev);
        }

        transaction.addToBackStack(null);
        dialog.show(transaction, "dialog");
    }

    // public class MyPagerAdapter extends FragmentPagerAdapter implements
    // TitleProvider {
    //
    // private String[] titles;
    //
    // public MyPagerAdapter(final FragmentManager fman,
    // final boolean multiPane) {
    // super(fman);
    // titles = multiPane ? getResources().getStringArray(
    // R.array.pager_titles_tab) : getResources().getStringArray(
    // R.array.pager_titles_port);
    // }
    //
    // public MyPagerAdapter(final FragmentManager fm) {
    // super(fm);
    // }
    //
    // @Override
    // public int getCount() {
    // return CoreInfoHandler.getInstance().getPageSet().length;
    // }
    //
    // @Override
    // public Fragment getItem(final int position) {
    // return FragmentFactory.getFragmentTabPage(position);
    // }
    //
    // @Override
    // public String getTitle(final int pos) {
    // return titles[pos];
    // }
    // }

    public class SwitchPageTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(final Integer... paramArrayOfParams) {

            return paramArrayOfParams[0];
        }

        @Override
        protected void onPostExecute(final Integer pageId) {

            final int page = FragmentFactory.getFragmentTabPageIndexById(pageId);

            if (Ut.isMultiPane(MainActivity.this)) {
                if (CoreInfoHandler.getInstance().getViewPager() != null) {
                    CoreInfoHandler.getInstance().getViewPager().setCurrentItem(page, true);
                }
            }
            else {
                final Intent intent = new Intent(MainActivity.this, PagerActivity.class);

                final Bundle bundle = new Bundle();
                bundle.putInt("PageInFlipper", page);
                intent.putExtras(bundle);

                startActivityForResult(intent, MainActivity.ACTIVITY_ID_DATA);
            }

        }
    }

    public class ShowExternalPointTask extends AsyncTask<Intent, Void, Integer> {

        @Override
        protected Integer doInBackground(final Intent... paramArrayOfParams) {

            final Uri uri = paramArrayOfParams[0].getData();

            // supported variants:

            // "geo:0,0?q=37.423156,-122.084917 (" + name + ")"
            // "geo:37.423156,-122.084917 (" + name + ")"
            // "geo:0,0?q=address+to+lookup (" + name + ")"
            if (uri != null) {
                try {
                    final String coords = uri.getEncodedSchemeSpecificPart();
                    int pos = coords.indexOf(',');

                    String title = null;

                    String latT = coords.substring(0, pos);
                    String lonT = coords.substring(pos + 1);

                    double lat = Double.parseDouble(latT);
                    double lon = Double.parseDouble(lonT);

                    if ((lat == 0) && (lon == 0)) // might be query
                    {
                        final String token = "?q=";
                        pos = coords.indexOf(token);

                        // either lat & lon or string to search
                        int pos2 = -1;
                        if (pos > -1) {
                            pos2 = coords.indexOf("(", pos + token.length() + 1);
                            int pos3 = -1;
                            if (pos2 > -1) // we have a title
                            {
                                pos3 = coords.indexOf(")", pos2);
                                title = coords.substring(pos2 + 1, pos3);
                            }

                            pos2 = coords.indexOf(",", pos + 1);

                            if (pos2 > -1) { // seems that we have coords
                                latT = coords.substring(pos + token.length() + 1, pos2);
                                if (pos3 > -1) {
                                    lonT = coords.substring(pos2 + 1, pos3);
                                }
                                else {
                                    lonT = coords.substring(pos2 + 1);
                                }
                                lat = Double.parseDouble(latT);
                                lon = Double.parseDouble(lonT);
                            }
                            else {
                                // lets try a topic search....
                                String searchString = coords.substring(pos + token.length() + 1);
                                pos2 = coords.indexOf("(", pos + token.length() + 1);
                                if (pos2 > -1) {
                                    searchString = searchString.substring(0, pos2);
                                }
                                showSearchQuery(searchString);
                                return FragmentFactory.FRAG_ID_MAP;
                            }
                        }
                    }

                    final String address = Ut.getAddress(MainActivity.this, lat, lon);

                    CoreInfoHandler.getInstance().getExternalPointOverlay().setLocation(lat, lon, address, title);

                    CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_SHOW_EXTERNAL);

                }
                catch (final Exception x) {
                    Ut.d("GEO URI parsing problem:" + x.toString());
                    Toast.makeText(MainActivity.this, R.string.message_URI_not_parsable, Toast.LENGTH_LONG).show();
                }
            }

            return FragmentFactory.FRAG_ID_MAP;
        }

        @Override
        protected void onPostExecute(final Integer pageId) {

            mapFragment.pageGetsActivated();

            // CoreInfoHandler.getInstance().gotoPage(
            // FragmentFactory.getFragmentTabPageIndexById(pageId));

        }
    }

    // --------------------------------------

    /**
     * Starts the track recording service (if not already running) and binds to
     * it. Starts recording a new track.
     */

    public void startLocationService() {
        if (trackRecordingService == null) {
            startNewTrackRequested = true;
            final Intent startIntent = new Intent(this, TrackWriterService.class);

            startService(startIntent);
            tryBindTrackRecordingService();
        }
        Ut.i("Starting location service ");
    }

    public void startRecording() {
        if (trackRecordingService == null) {
            startLocationService();
        }
        else {
            startRecordingNewTrack(trackRecordingService);
        }
    }

    private void startRecordingNewTrack(final ITrackWriterService trackRecordingService) {
        try {
            trackRecordingService.startNewTrack();
            // Select the recording track.

            Toast.makeText(this, getString(R.string.status_now_recording), Toast.LENGTH_SHORT).show();

            // TODO: We catch Exception, because after eliminating the service
            // process
            // all exceptions it may throw are no longer wrapped in a
            // RemoteException.
        }
        catch (final Exception e) {
            Toast.makeText(this, getString(R.string.error_unable_to_start_recording), Toast.LENGTH_SHORT).show();
            Ut.w("Unable to start recording." + e.getMessage());
        }
    }

    /**
     * Stops the track recording service and unbinds from it. Will display a
     * toast "Stopped recording" and pop up the Track Details activity.
     */
    public void stopRecording() {
        if (trackRecordingService != null) {
            try {

                if (trackRecordingService.isRecording()) {
                    trackRecordingService.finishTrack();
                }
            }
            catch (final Exception e) {
                Ut.e("Unable to stop recording. " + e.getMessage());
            }
        }
        // tryUnbindTrackRecordingService();
        // try {
        // stopService(new Intent(this, TrackWriterService.class));
        // } catch (final SecurityException e) {
        // Ut.e("Encountered a security exception when trying to stop service."
        // + e.getMessage());
        // }
        // trackRecordingService = null;
    }

    /**
     * Binds to track recording service if it is running.
     */
    private void tryBindTrackRecordingService() {
        Ut.d("Trying to bind to track recording service...");
        bindService(new Intent(this, TrackWriterService.class), serviceConnection, 0);
        Ut.d("...bind finished!");
        isBound = true;
    }

    /**
     * Tries to unbind the track recording service. Catches exception silently
     * in case service is not registered anymore.
     */
    private void tryUnbindTrackRecordingService() {
        if (isBound) {
            Ut.d("Trying to unbind from track recording service...");
            try {
                unbindService(serviceConnection);
                Ut.d("...unbind finished!");
            }
            catch (final IllegalArgumentException e) {
                Ut.d("ried unbinding, but service was not registered." + e.getMessage());
            }
            isBound = false;
        }
    }
}
