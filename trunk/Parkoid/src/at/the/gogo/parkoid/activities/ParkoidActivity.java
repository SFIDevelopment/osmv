package at.the.gogo.parkoid.activities;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.WindowManager;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.fragments.FragmentFactory;
import at.the.gogo.parkoid.fragments.PageChangeNotifyer;
import at.the.gogo.parkoid.receiver.SmsHelper;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.CrashReportHandler;
import at.the.gogo.parkoid.util.StrictModeWrapper;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.speech.SpeakItOut;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
import com.viewpagerindicator.TitleProvider;

public class ParkoidActivity extends SherlockFragmentActivity implements
        TextToSpeech.OnInitListener {

    public final static int    PREF_ID                             = 123;

    private static final int   MY_TTS_CHECK_CODE                   = 1234;

    private static final long  MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 5;    // in
                                                                            // Meters
    private static final long  MINIMUM_TIME_BETWEEN_UPDATES        = 10000; // in
                                                                            // Milliseconds

    protected LocationManager  mLocationManager;
    private MyLocationListener myListener;

    boolean                    wantToUseTTS                        = false;

    private MyPagerAdapter     mPagerAdapter;
    private ViewPager          mViewPager;
    // private ViewPagerIndicator mIndicator;
    private TitlePageIndicator mIndicator;

    // @Override
    // public void onAttachedToWindow() {
    //
    // super.onAttachedToWindow();
    // Window window = getWindow();
    // window.setFormat(PixelFormat.RGBA_8888);
    //
    // }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CoreInfoHolder.getInstance().setContext(this);

        final int applicationFlags = getApplicationInfo().flags;
        if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            CrashReportHandler.attach(this);
        }

        if (Util.DEBUGMODE) {
            try {
                StrictModeWrapper.init(this);
            } catch (final Throwable throwable) {
                Util.i("StrictMode is not available!");
            }
        }

        // now switch debug mode off if
        Util.DEBUGMODE = ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);

        setContentView(R.layout.mainsimple);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.viewflipper);
        mViewPager.setAdapter(mPagerAdapter);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        final int lastpageViewed = sharedPreferences.getInt("PageInFlipper", 0);

        mViewPager.setCurrentItem(lastpageViewed);

        CoreInfoHolder.getInstance().setPager(mViewPager);

        //
        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setFooterIndicatorStyle(IndicatorStyle.Underline);

        // other indicator impl !!
        // Find the indicator from the layout
        // mIndicator = (ViewPagerIndicator) view.findViewById(R.id.indicator);

        // Set the indicator as the pageChangeListener
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageScrolled(final int position,
                    final float positionOffset, final int positionOffsetPixels) {
                mIndicator.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }

            private int lastPositionWorkaround = -1; // TODO: we get called
                                                     // twice ... why plz fix

            @Override
            public void onPageSelected(final int position) {

                if (position != lastPositionWorkaround) {
                    final PageChangeNotifyer oldPage = FragmentFactory.pages[mIndicator
                            .getCurrentItem()];

                    if (oldPage != null) {
                        oldPage.pageGetsDeactivated();
                    }

                    final PageChangeNotifyer newPage = FragmentFactory.pages[position];
                    if (newPage != null) {
                        newPage.pageGetsActivated();
                    }
                    lastPositionWorkaround = position;
                }
                mIndicator.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                mIndicator.onPageScrollStateChanged(state);

            }
        });

        // Initialize the OTHER indicator. We need some information here:
        // * What page do we start on.
        // * How many pages are there in total
        // * A callback to get page titles
        // mIndicator.init(0, mPagerAdapter.getCount(), mPagerAdapter);
        // final Resources res = getResources();
        // final Drawable prev =
        // res.getDrawable(R.drawable.indicator_prev_arrow);
        // final Drawable next =
        // res.getDrawable(R.drawable.indicator_next_arrow);
        // mIndicator.setFocusedTextColor(new int[] { 0xFF, 0xAF, 0x3F });
        // mIndicator.setUnfocusedTextColor(new int[] { 0x00, 0x00, 0x00 });
        //
        // // Set images for previous and next arrows.
        // mIndicator.setArrows(prev, next);
        //
        // mIndicator.setOnClickListener(new OnIndicatorClickListener());
        //

        setTitle(R.string.app_name);

        getSupportActionBar().setNavigationMode(
                ActionBar.NAVIGATION_MODE_STANDARD);

        // pref_gui_showactionbar

        getSupportActionBar().setDisplayUseLogoEnabled(false);

        myListener = new MyLocationListener();

        restoreUIState();

        // CoreInfoHolder.getInstance().setPager(
        // (PagerFragment) FragmentFactory
        // .getFragmentPage(FragmentFactory.FRAG_ID_PAGER));
        //
        // replaceFragment(R.id.frame_fragment, CoreInfoHolder.getInstance()
        // .getPager());

        wantToUseTTS = sharedPreferences.getBoolean("pref_tts_speech", true);

        // if (speakit) {

        // Fire off an intent to check if a TTS engine is installed
        checkTTS();
        // }

        // Check to see if a recognition activity is present
        checkSpeakReco();

        // TODO: add to prefs
        setKeepScreenOn(this, true);

    }

    private void checkTTS() {
        final Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, ParkoidActivity.MY_TTS_CHECK_CODE);
    }

    private void checkSpeakReco() {
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        CoreInfoHolder.getInstance().setSpeechRecoAvailable(
                activities.size() > 0);
    }

    @Override
    protected void onDestroy() {

        if (CoreInfoHolder.getInstance().isSpeakit()) {
            SpeakItOut.speak(getText(R.string.tts_bye).toString());
        }
        if (CoreInfoHolder.getInstance().getTts() != null) {
            CoreInfoHolder.getInstance().getTts().stop();
            CoreInfoHolder.getInstance().getTts().shutdown();
        }
        super.onDestroy();
    }

    private void restoreUIState() {
        final SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);

        if (settings.getString("error", "").length() > 0) {
            showDialog(R.id.error);
        }

        if (!settings.getString("app_version", "").equalsIgnoreCase(
                Util.getAppVersion(this))) {
            showDialog(R.id.whatsnew);
            final SharedPreferences.Editor editor = settings.edit();
            editor.putString("app_version", Util.getAppVersion(this));
            editor.commit();
        }

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        final boolean showActionBar = sharedPreferences.getBoolean(
                "pref_gui_showactionbar", true);
        if (!showActionBar) {
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void onPause() {

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(myListener);
        }

        SmsHelper.deregisterSMSDeliveredReceiver(this);
        SmsHelper.deregisterSMSSentReceiver(this);

        CoreInfoHolder.getInstance().getDbManager().freeDatabases();
        CoreInfoHolder.getInstance().setDbManager(null);

        final SharedPreferences uiState = PreferenceManager
                .getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = uiState.edit();
        editor.putInt("PageInFlipper", mViewPager.getCurrentItem());
        editor.commit();
        
        super.onPause();
    }

    @Override
    protected void onResume() {

        SmsHelper.registerSMSDeliveredReceiver(this);
        SmsHelper.registerSMSSentReceiver(this);

        getBestProvider();

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        // super.onBackPressed();
        finish();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {

        // outState.putInt(POSKEY, mPageIndicator.getCurrentPosition());

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        final boolean showActionBar = sharedPreferences.getBoolean(
                "pref_gui_showactionbar", true);

        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(showActionBar ? R.menu.main_option_menu
                : R.menu.main_option_menu_plain, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home: {
                CoreInfoHolder.getInstance().gotoPage(0);
                result = true;
                break;
            }
            case R.id.settings: {
                startActivityForResult(new Intent(this,
                        MainPreferenceActivity.class), ParkoidActivity.PREF_ID);
                result = true;
                break;
            }
            case R.id.about: {
                if (CoreInfoHolder.getInstance().isSpeakit()) {
                    SpeakItOut.speak(getText(R.string.tts_about).toString());
                }
                showDialog(R.id.about);
                result = true;
                break;
            }
            case R.id.deleteParinkingInfo: {
                deleteAllParkingSlots(); // temp as only one is available - move
                                         // to parkingFragment l8er
                result = true;
                break;

            }

        }
        return result;
    }

    private void deleteAllParkingSlots() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.warning_delete_parkingSlot)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogYES,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {

                                // CoreInfoHolder.getInstance()
                                // .getDbManager(getActivity()).beginTransaction();

                                CoreInfoHolder.getInstance().getDbManager()
                                        .deleteAllLocations();
                                // CoreInfoHolder.getInstance()
                                // .getDbManager(getActivity()).commitTransaction();

                                dialog.dismiss();
                                CoreInfoHolder.getInstance()
                                        .getParkingCarOverlay().refresh();
                            }
                        })
                .setNegativeButton(R.string.dialogNO,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id) {
                                dialog.cancel();
                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        // System.out.println("Code:" + requestCode);
        // if (resultCode == RESULT_OK) {

        if (requestCode == ParkoidActivity.MY_TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                CoreInfoHolder.getInstance().setTts(
                        new TextToSpeech(this, this));
            } else {
                // missing data, install it
                final Intent installIntent = new Intent();
                installIntent
                        .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        } else {

            // NB: I only expect preferences to return here - so we kill and
            // restart

            finish();
            startActivity(new Intent(this, this.getClass()));
        }
        // }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {

            case R.id.whatsnew:
                return new AlertDialog.Builder(this)
                        // .setIcon( R.drawable.alert_dialog_icon)
                        .setTitle(R.string.about_dialog_whats_new)
                        .setMessage(R.string.whats_new_dialog_text)
                        .setNegativeButton(R.string.about_dialog_close,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int whichButton) {

                                        /* User clicked Cancel so do some stuff */
                                    }
                                }).create();
            case R.id.about:
                return new AlertDialog.Builder(this)
                        // .setIcon(R.drawable.alert_dialog_icon)
                        .setTitle(R.string.menu_about)
                        .setMessage(
                                getText(R.string.app_name) + " v."
                                        + Util.getAppVersion(this) + "\n\n"
                                        + getText(R.string.about_dialog_text))
                        .setPositiveButton(R.string.about_dialog_whats_new,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int whichButton) {

                                        showDialog(R.id.whatsnew);
                                    }
                                })
                        .setNegativeButton(R.string.about_dialog_close,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int whichButton) {

                                        /* User clicked Cancel so do some stuff */
                                    }
                                }).create();
            case R.id.error:
                return new AlertDialog.Builder(this)
                        // .setIcon(R.drawable.alert_dialog_icon)
                        .setTitle(R.string.error_title)
                        .setMessage(getText(R.string.error_text))
                        .setPositiveButton(R.string.error_send,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    @SuppressWarnings("static-access")
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int whichButton) {

                                        final SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
                                        String text = settings.getString(
                                                "error", "");
                                        String subj = getText(R.string.app_name)
                                                + " runtime error: ";
                                        try {
                                            final String[] lines = text.split(
                                                    "\n", 2);
                                            final Pattern p = Pattern
                                                    .compile("[.][\\w]+[:| |\\t|\\n]");
                                            final Matcher m = p
                                                    .matcher(lines[0] + "\n");
                                            if (m.find()) {
                                                subj += m.group()
                                                        .replace(".", "")
                                                        .replace(":", "")
                                                        .replace("\n", "")
                                                        + " at ";
                                            }
                                            final Pattern p2 = Pattern
                                                    .compile("[.][\\w]+[(][\\w| |\\t]*[)]");
                                            final Matcher m2 = p2
                                                    .matcher(lines[1]);
                                            if (m2.find()) {
                                                subj += m2.group().substring(2);
                                            }
                                        } catch (final Exception e) {
                                        }

                                        final Build b = new Build();
                                        final Build.VERSION v = new Build.VERSION();
                                        text = "Your message:"
                                                + "\n\n"
                                                + getText(R.string.app_name)
                                                + ": "
                                                + Util.getAppVersion(ParkoidActivity.this)
                                                + "\nAndroid: " + v.RELEASE
                                                + "\nDevice: " + b.BOARD + " "
                                                + b.BRAND + " " + b.DEVICE
                                                + " " + b.MANUFACTURER + " "
                                                + b.MODEL + " " + b.PRODUCT
                                                + "\n\n" + text;

                                        startActivity(Util.sendErrorReportMail(
                                                subj, text));
                                        Util.e(text);
                                        final SharedPreferences uiState = getPreferences(0);
                                        final SharedPreferences.Editor editor = uiState
                                                .edit();
                                        editor.putString("error", "");
                                        editor.commit();

                                    }
                                })
                        .setNegativeButton(R.string.about_dialog_close,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int whichButton) {

                                        final SharedPreferences uiState = getPreferences(0);
                                        final SharedPreferences.Editor editor = uiState
                                                .edit();
                                        editor.putString("error", "");
                                        editor.commit();
                                    }
                                }).create();

        }
        return null;
    }

    // private void replaceFragment(final int id, final Fragment fragment) {
    // final FragmentTransaction transaction = getSupportFragmentManager()
    // .beginTransaction();
    // transaction.setCustomAnimations(android.R.anim.fade_in,
    // android.R.anim.fade_out);
    //
    // transaction.replace(id, fragment);
    // // transaction.remove(fragment);
    // // transaction.add(id, fragment);
    // transaction.addToBackStack("");
    // transaction.commit();
    //
    // }

    protected LocationManager getLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return mLocationManager;
    }

    private boolean findGPSProvider(final String providerName) {
        final long minTime = ParkoidActivity.MINIMUM_TIME_BETWEEN_UPDATES;
        final float minDistance = ParkoidActivity.MINIMUM_DISTANCE_CHANGE_FOR_UPDATES;

        boolean use = false;

        if (getLocationManager().isProviderEnabled(providerName)) {
            getLocationManager().requestLocationUpdates(providerName, minTime,
                    minDistance, myListener);
            use = true;
        }

        return use;
    }

    private void getBestProvider() {

        getLocationManager().removeUpdates(myListener);

        String txt;

        if (findGPSProvider(LocationManager.GPS_PROVIDER)) {
            txt = getText(R.string.location_provider_gps).toString();
        } else if (findGPSProvider(LocationManager.NETWORK_PROVIDER)) {
            txt = getText(R.string.location_provider_net).toString();
        } else {
            txt = getText(R.string.location_provider_no).toString();
        }

        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();

    }

    private void setKeepScreenOn(final Activity activity,
            final boolean keepScreenOn) {
        if (keepScreenOn) {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    // delegate all for dispatching
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            CoreInfoHolder.getInstance().getLocationListener()
                    .onLocationChanged(location);
        }

        @Override
        public void onStatusChanged(final String s, final int i, final Bundle b) {
            CoreInfoHolder.getInstance().getLocationListener()
                    .onStatusChanged(s, i, b);
        }

        @Override
        public void onProviderDisabled(final String s) {
            CoreInfoHolder.getInstance().getLocationListener()
                    .onProviderDisabled(s);
        }

        @Override
        public void onProviderEnabled(final String s) {
            CoreInfoHolder.getInstance().getLocationListener()
                    .onProviderEnabled(s);
        }

    }

    // for TTS
    @Override
    public void onInit(final int status) {

        CoreInfoHolder.getInstance().setSpeakit(wantToUseTTS); // wanted &
                                                               // installed

        if (CoreInfoHolder.getInstance().isSpeakit()) {
            SpeakItOut.speak(getText(R.string.tts_welcome).toString());
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter implements
            TitleProvider {

        public MyPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return FragmentFactory.FRAGMENT_PAGES_TAB.length;
        }

        @Override
        public Fragment getItem(final int position) {
            return FragmentFactory.getFragmentTabPage(position);
        }

        @Override
        public String getTitle(final int pos) {
            return getResources().getStringArray(R.array.page_titles)[pos];

        }
    }

}
