package at.the.gogo.gpxviewer.activities;

import info.androidhive.slidingmenu.adapter.NavDrawerListAdapter;
import info.androidhive.slidingmenu.model.NavDrawerItem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.the.gogo.gpxviewer.R;
import at.the.gogo.gpxviewer.mapsources.MapSourcesManager;
import at.the.gogo.gpxviewer.mapsources.adapter.BaseTileProvider;
import at.the.gogo.gpxviewer.model.GeoPoint;
import at.the.gogo.gpxviewer.model.PoiPoint;
import at.the.gogo.gpxviewer.model.Route;
import at.the.gogo.gpxviewer.model.Track;
import at.the.gogo.gpxviewer.model.TrackPoint;
import at.the.gogo.gpxviewer.util.GoogleAdressLookupCompleted;
import at.the.gogo.gpxviewer.util.GoogleAdressLookupTask;
import at.the.gogo.gpxviewer.util.OpenStreetMapConstants;
import at.the.gogo.gpxviewer.util.Util;
import at.the.gogo.gpxviewer.util.geo.GPXContentHolder;
import at.the.gogo.gpxviewer.util.geo.GPXLoader;
import at.the.gogo.gpxviewer.util.mytrack.MyTrackAdapter;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class MainActivity extends Activity implements LocationListener {

	private GoogleMap map;

	// private ClusterManager<PoiPoint> mClusterManager;

	private static final int FILE_REQUEST_CODE = 6384;
	private static final String TAG = "MAPActivity";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter drawerAdapter;

	private GPXContentHolder gpxContent = null;
	// MyTRack tracks
	private List<com.google.android.apps.mytracks.content.Track> myTracks = null;

	// Define an object that holds accuracy and frequency parameters
	LocationRequest mLocationRequest;
	boolean mUpdatesRequested;
	LocationClient mLocationClient;

	// customTileSupport
	TileProvider customTileProvider = null;
	TileOverlay customTileOverlay = null;

	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;

	// @Override
	// protected void onStart() {
	//
	// mLocationClient.connect();
	// }
	//
	// @Override
	// protected void onPause() {
	// // Save the current setting for updates
	// mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
	// mEditor.commit();
	// super.onPause();
	// }
	//
	// @Override
	// protected void onResume() {
	// /*
	// * Get any previous setting for location updates
	// * Gets "false" if an error occurs
	// */
	// if (mPrefs.contains("KEY_UPDATES_ON")) {
	// mUpdatesRequested =
	// mPrefs.getBoolean("KEY_UPDATES_ON", false);
	//
	// // Otherwise, turn off location updates
	// } else {
	// mEditor.putBoolean("KEY_UPDATES_ON", false);
	// mEditor.commit();
	// }
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// check if we are debugging
		final int applicationFlags = getApplicationInfo().flags;
		if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {

			// we are not in debug mode!
			Util.setDebugMode(false);
		} else {
			// set to on if really wanted!!!
			Util.setDebugMode(OpenStreetMapConstants.DEBUGMODE);
		}

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		// if (map != null) {
		// Marker hamburg = getMap().addMarker(
		// new MarkerOptions().position(HAMBURG).title("Hamburg"));
		// Marker kiel = getMap().addMarker(
		// new MarkerOptions()
		// .position(KIEL)
		// .title("Kiel")
		// .snippet("Kiel is cool")
		// .icon(BitmapDescriptorFactory
		// .fromResource(R.drawable.ic_launcher)));
		//
		// Marker wien = getMap()
		// .addMarker(
		// new MarkerOptions()
		// .position(WIEN)
		// .title("Wien")
		// .snippet("Wien eoe!")
		// .icon(BitmapDescriptorFactory
		// .fromResource(R.drawable.map_pin_holed_red_normal_small)));
		//
		// // initialize ClusterManager
		// // mClusterManager = new ClusterManager<PoiPoint>(this, map);
		// // getMap().setOnCameraChangeListener(mClusterManager);
		// // getMap().setOnMarkerClickListener(mClusterManager);
		//
		// // Move the camera instantly to hamburg with a zoom of 15.
		// getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(WIEN, 10));

		// Zoom in, animating the camera.
		// getMap().animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		//

		getMap().setMyLocationEnabled(true);

		getMap().setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(final LatLng point) {

				GoogleAdressLookupTask task = new GoogleAdressLookupTask(
						MainActivity.this,

						new GoogleAdressLookupCompleted() {
							public void doneRetrievingAddress(String address) {

								// we got an address ?

								Toast.makeText(MainActivity.this,
										"points Adress: " + address,
										Toast.LENGTH_LONG).show();

							}
						});

				// Convert LatLng to Location
				Location location = new Location("Clicked");
				location.setLatitude(point.latitude);
				location.setLongitude(point.longitude);
				location.setTime(new Date().getTime()); // Set time as current
														// Date

				task.execute(location);

			}

		});

		//
		//
		//
		// }
		//
		// -----------------------

		mTitle = mDrawerTitle = getTitle();

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		setupNavDrawer();

		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();

		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		// // Open the shared preferences
		// mPrefs = getSharedPreferences("SharedPreferences",
		// Context.MODE_PRIVATE);
		// // Get a SharedPreferences editor
		// mEditor = mPrefs.edit();

		// if (savedInstanceState == null) {
		// // on first time display view for first nav item
		// displayView(0);
		// }

		// process launch intent
		Intent intent = getIntent();

		if (intent != null) {
			processLaunchIntent(intent);
		}

	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_REQUEST_CODE:
			// If the file selection was successful
			if (resultCode == RESULT_OK) {
				if (data != null) {
					// Get the URI of the selected file
					final Uri uri = data.getData();

					try {
						Log.i(TAG, "Uri = " + uri.toString());

						// // Get the file path from the URI
						// final String path = FileUtils.getPath(this, uri);
						//
						// if (path != null) {
						// Toast.makeText(this, "File Selected: " + path,
						// Toast.LENGTH_LONG).show();

						// start import

						importDataFile(uri);

						// }
					} catch (Exception e) {
						Log.e(TAG, "File select error", e);
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated

		if (Util.isDebugMode()) {
			String msg = "Updated Location: "
					+ Double.toString(location.getLatitude()) + ","
					+ Double.toString(location.getLongitude());

			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_file:
			showChooserForFileImport();
			return true;
		case R.id.menu_about:

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	// @Override
	// protected void onStart() {
	//
	// mLocationClient.connect();
	// }
	//
	// @Override
	// protected void onPause() {
	// // Save the current setting for updates
	// mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
	// mEditor.commit();
	// super.onPause();
	// }
	//
	// @Override
	// protected void onResume() {
	// /*
	// * Get any previous setting for location updates
	// * Gets "false" if an error occurs
	// */
	// if (mPrefs.contains("KEY_UPDATES_ON")) {
	// mUpdatesRequested =
	// mPrefs.getBoolean("KEY_UPDATES_ON", false);
	//
	// // Otherwise, turn off location updates
	// } else {
	// mEditor.putBoolean("KEY_UPDATES_ON", false);
	// mEditor.commit();
	// }
	// }

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// if nav drawer is opened, hide the action items !!!
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.menu_about).setVisible(!drawerOpen);
		menu.findItem(R.id.menu_file).setVisible(!drawerOpen);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments

		Fragment fragment = null;

		switch (position) {
		case 0:
			// switch from custom overlay ?
			switchFromCustomTileProvider();
			getMap().setMapType(
					com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL);
			break;
		case 1:
			// switch from custom overlay ?
			switchFromCustomTileProvider();
			getMap().setMapType(
					com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID);
			break;
		case 2:
			// switch from custom overlay ?
			switchFromCustomTileProvider();
			getMap().setMapType(
					com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case 3:
			// alternate map
			getMap().setMapType(
					com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE);
			// switchToCustomMapProvider();

			chooseCustomMapDialog();

			break;
		case 4:
			importMyTracks();
			break;
		case 5:
		case 6:
		case 7: // focus on gpx poi / route / track
			if (gpxContent != null) {
				chooseGPXCategoryEntryDialog(gpxContent, position - 5);
			}
			break;

		default:
			break;
		}

		// if (fragment != null) {
		// FragmentManager fragmentManager = getFragmentManager();
		// fragmentManager.beginTransaction()
		// .replace(R.id.frame_container, fragment).commit();
		//
		// // update selected item and title, then close the drawer
		// mDrawerList.setItemChecked(position, true);
		// mDrawerList.setSelection(position);
		//
		// } else {
		// // error in creating fragment
		// Log.e("MainActivity", "Error in creating fragment");
		// }

		setTitle(navMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	private GoogleMap getMap() {
		return map;
	}

	private void setupNavDrawer() {
		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		for (int i = 0; i < navMenuTitles.length; i++) {
			if (i < navMenuTitles.length - 3) {
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[i],
						navMenuIcons.getResourceId(i, -1)));
			} else {
				navDrawerItems.add(new NavDrawerItem(navMenuTitles[i],
						navMenuIcons.getResourceId(i, -1), true, "0"));
			}
		}

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		drawerAdapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);

		mDrawerList.setAdapter(drawerAdapter);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

	}

	private void importMyTracks() {
		ImportMyTracksTask myTracksTask = new ImportMyTracksTask();
		myTracksTask.execute((Void) null);
	}

	// @Override
	// protected void onStart() {
	//
	// mLocationClient.connect();
	// }
	//
	// @Override
	// protected void onPause() {
	// // Save the current setting for updates
	// mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
	// mEditor.commit();
	// super.onPause();
	// }
	//
	// @Override
	// protected void onResume() {
	// /*
	// * Get any previous setting for location updates
	// * Gets "false" if an error occurs
	// */
	// if (mPrefs.contains("KEY_UPDATES_ON")) {
	// mUpdatesRequested =
	// mPrefs.getBoolean("KEY_UPDATES_ON", false);
	//
	// // Otherwise, turn off location updates
	// } else {
	// mEditor.putBoolean("KEY_UPDATES_ON", false);
	// mEditor.commit();
	// }
	// }

	// ---------------------
	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);

		}
	}

	private void processLaunchIntent(Intent intent) {

		Uri data = intent.getData();
		// Clear intent action to prevent triggering download many times
		intent.setAction(null);

		if (data != null) {
			importDataFile(data);
		}
	}

	private void importDataFile(Uri geller) {
		ImportFileTask importTask = new ImportFileTask();

		importTask.execute(new Uri[] { geller });
	}

	// /*
	// * Called by Location Services when the request to connect the
	// * client finishes successfully. At this point, you can
	// * request the current location or start periodic updates
	// */
	// @Override
	// public void onConnected(Bundle dataBundle) {
	// // Display the connection status
	// Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
	// // If already requested, start periodic updates
	// if (mUpdatesRequested) {
	// mLocationClient.requestLocationUpdates(mLocationRequest, this);
	// }
	// }

	private void showChooserForFileImport() {
		// Use the GET_CONTENT intent from the utility class
		Intent target = FileUtils.createGetContentIntent();
		// Create the chooser Intent
		Intent intent = Intent.createChooser(target,
				getString(R.string.chooser_title));
		try {
			startActivityForResult(intent, FILE_REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}
	}

	private void addGPXToMap(GPXContentHolder gpxContent) {

		boolean movedCam = false;

		if (gpxContent != null) {
			map.clear();

			for (PoiPoint point : gpxContent.getPoints()) {
				// mClusterManager.addItem(point);

				getMap().addMarker(
						new MarkerOptions()
								.position(point.getPosition())
								.title(point.getTitle())
								.snippet(point.getDescr())
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.map_pin_holed_red_normal_small)));

				if (!movedCam) {
					movedCam = true;
					getMap().moveCamera(
							CameraUpdateFactory.newLatLngZoom(
									point.getPosition(), 10));

				}

			}
			for (Route route : gpxContent.getRoutes()) {

				PolylineOptions routeLineOption = new PolylineOptions();
				routeLineOption.color(Color.GREEN);
				routeLineOption.width(6);
				routeLineOption.zIndex(3);

				if (route.getRoutePoints() != null) {
					for (PoiPoint point : route.getRoutePoints()) {

						routeLineOption.add(point.getPosition());

						// mClusterManager.addItem(point);
						getMap().addMarker(
								new MarkerOptions()
										.position(point.getPosition())
										.title(route.getName() + " - "
												+ point.getTitle())
										.snippet(point.getDescr())
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.map_pin_holed_green_normal_small)));

						if (!movedCam) {
							movedCam = true;
							getMap().moveCamera(
									CameraUpdateFactory.newLatLngZoom(
											point.getPosition(), 10));
						}

					}
				}
				Polyline routeline = map.addPolyline(routeLineOption);
			}

			for (Track track : gpxContent.getTracks()) {

				PolylineOptions trackLineOption = new PolylineOptions();

				trackLineOption.color(Color.BLUE);
				trackLineOption.width(6);
				trackLineOption.zIndex(3);

				if (track.getPoints() != null) {

					for (TrackPoint point : track.getPoints()) {
						trackLineOption.add(new LatLng(point.getLatitude(),
								point.getLongitude()));
					}
					Polyline trackline = map.addPolyline(trackLineOption);

					PoiPoint pp = new PoiPoint();
					pp.setGeoPoint(track.getFirstTrackPoint());
					pp.setTitle(track.Name + getString(R.string.gpx_track_start));
					pp.setDescr(track.Descr);

					// mClusterManager.addItem(pp);

					getMap().addMarker(
							new MarkerOptions()
									.position(pp.getPosition())
									.title(pp.getTitle())
									.snippet(pp.getDescr())
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.map_pin_holed_violet_normal_small)));

					pp = new PoiPoint();
					pp.setGeoPoint(track.getLastTrackPoint());
					pp.setTitle(track.Name + getString(R.string.gpx_track_finish));
					pp.setDescr(track.Descr);

					// mClusterManager.addItem(pp);
					getMap().addMarker(
							new MarkerOptions()
									.position(pp.getPosition())
									.title(pp.getTitle())
									.snippet(pp.getDescr())
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.map_pin_holed_violet_normal_small)));

					if (!movedCam) {
						movedCam = true;
						getMap().moveCamera(
								CameraUpdateFactory.newLatLngZoom(
										pp.getPosition(), 10));
					}
				}
			}
		}
	}

	void addMyTrackTackToMap(
			com.google.android.apps.mytracks.content.Track track) {

		if ((track.getLocations() != null) && (track.getLocations().size() > 0)) {
			PolylineOptions trackLineOption = new PolylineOptions();
			trackLineOption.color(Color.BLUE);
			trackLineOption.zIndex(3);

			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			for (Location location : track.getLocations()) {
				trackLineOption.add(new LatLng(location.getLatitude(), location
						.getLongitude()));

				builder.include(new LatLng(location.getLatitude(), location
						.getLongitude()));

			}
			LatLngBounds bounds = builder.build();
			bounds = builder.build();

			Polyline trackline = map.addPolyline(trackLineOption);

			PoiPoint pp = new PoiPoint();
			pp.setGeoPoint(new GeoPoint(track.getLocations().get(0)
					.getLatitude(), track.getLocations().get(0).getLongitude()));
			pp.setTitle(track.getName());
			pp.setDescr(track.getDescription());

			getMap().addMarker(
					new MarkerOptions()
							.position(pp.getPosition())
							.title(pp.getTitle() + getString(R.string.gpx_track_start))
							.snippet(pp.getDescr())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.map_pin_holed_violet_normal_small)));

			getMap().moveCamera(
					CameraUpdateFactory.newLatLngZoom(pp.getPosition(), 10));

			pp = new PoiPoint();
			pp.setGeoPoint(new GeoPoint(track.getLocations()
					.get(track.getLocations().size() - 1).getLatitude(), track
					.getLocations().get(track.getLocations().size() - 1)
					.getLongitude()));
			pp.setTitle(track.getName());
			pp.setDescr(track.getDescription());

			getMap().addMarker(
					new MarkerOptions()
							.position(pp.getPosition())
							.title(pp.getTitle() + getString(R.string.gpx_track_finish))
							.snippet(pp.getDescr())
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.map_pin_holed_violet_normal_small)));

			getMap().animateCamera(
					CameraUpdateFactory.newLatLngBounds(bounds, 30));

		}
	}

	public class ImportFileTask extends
			AsyncTask<Uri, Integer, GPXContentHolder> {
		@Override
		protected GPXContentHolder doInBackground(Uri... params) {

			try {
				InputStream stream = getContentResolver().openInputStream(
						params[0]);
				gpxContent = GPXLoader.importFile(stream);
			} catch (Exception x) {

			}
			return gpxContent;
		}

		@Override
		protected void onPostExecute(GPXContentHolder result) {
			addGPXToMap(result);

			// update navDrawer Info
			if (result != null) {
				navDrawerItems.get(navDrawerItems.size() - 1).setCount(
						result.getTracks().size() + "");
				navDrawerItems.get(navDrawerItems.size() - 2).setCount(
						result.getRoutes().size() + "");
				navDrawerItems.get(navDrawerItems.size() - 3).setCount(
						result.getPoints().size() + "");

				mDrawerList.setAdapter(drawerAdapter);
			}

			// if there is just a single entry - focus on it!
			// can be optimized
			if (result.getPoints().size() == 1) {
				moveToGPXCOntent(0, 0);
			} else if (result.getRoutes().size() == 1) {
				moveToGPXCOntent(1, 0);
			} else if (result.getTracks().size() == 1) {
				moveToGPXCOntent(2, 0);
			} else {
				// build overall bounds and show
				moveToGPXCOntent(3, 0);
			}

		}
	}

	public class ImportMyTracksTask
			extends
			AsyncTask<Void, Integer, List<com.google.android.apps.mytracks.content.Track>> {
		@Override
		protected List<com.google.android.apps.mytracks.content.Track> doInBackground(
				Void... params) {

			if (myTracks == null) {
				MyTrackAdapter mtAdapter = new MyTrackAdapter(MainActivity.this);
				if (mtAdapter.isAvailable()) {
					myTracks = mtAdapter.getTrackList();
				} else {
					Toast.makeText(
							MainActivity.this,
							"MyTracks App not installed or tracks not shareable",
							Toast.LENGTH_LONG).show();
				}
			}

			return myTracks;
		}

		@Override
		protected void onPostExecute(
				List<com.google.android.apps.mytracks.content.Track> tracks) {

			if (Util.isDebugMode()) {

				Toast.makeText(MainActivity.this,
						"MyTracks: " + (tracks != null ? tracks.size() : 0),
						Toast.LENGTH_LONG).show();
			}
			if (tracks != null) {
				chooseMyTrackDialog(tracks);
			}
		}
	}

	public class LoadMyTracksTrackDetailsTask
			extends
			AsyncTask<com.google.android.apps.mytracks.content.Track, Integer, com.google.android.apps.mytracks.content.Track> {
		@Override
		protected com.google.android.apps.mytracks.content.Track doInBackground(
				com.google.android.apps.mytracks.content.Track... params) {

			// track exists but has no details already loaded
			if ((params[0] != null) && (params[0].getLocations().size() == 0)) {
				MyTrackAdapter mtAdapter = new MyTrackAdapter(MainActivity.this);

				mtAdapter.getTrackdetails(params[0]);
			}

			return params[0];
		}

		@Override
		protected void onPostExecute(
				com.google.android.apps.mytracks.content.Track track) {
			addMyTrackTackToMap(track);
		}
	}

	private void moveToGPXCOntent(int catId, int index) {
		LatLng camPos = null;
		LatLngBounds bounds = null;
		switch (catId) {
		case 0: // Pois

			camPos = gpxContent.getPoints().get(index).getPosition();
			break;
		case 1: // Routes
		{
			// camPos =
			// gpxContent.getRoutes().get(index).getRoutePoints().get(0)
			// .getPosition();

			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			for (PoiPoint point : gpxContent.getRoutes().get(index)
					.getRoutePoints()) {
				builder.include(point.getPosition());
			}

			break;
		}
		case 2: // Tracks
		{
			// PoiPoint pp = new PoiPoint();
			// pp.setGeoPoint(gpxContent.getTracks().get(index).getFirstGeoPoint());
			// camPos = pp.getPosition();

			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			for (TrackPoint point : gpxContent.getTracks().get(index)
					.getPoints()) {
				builder.include(new LatLng(point.getLatitude(), point
						.getLongitude()));
			}

			bounds = builder.build();

			break;
		}
		case 3: // overall
		{

			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			// all pois
			for (PoiPoint point : gpxContent.getPoints()) {
				builder.include(point.getPosition());
			}

			// all routes

			for (Route route : gpxContent.getRoutes()) {
				for (PoiPoint point : route.getRoutePoints()) {
					builder.include(point.getPosition());
				}
			}
			for (Track track : gpxContent.getTracks()) {
				for (TrackPoint point : track.getPoints()) {
					builder.include(new LatLng(point.getLatitude(), point
							.getLongitude()));
				}
			}

			bounds = builder.build();

		}
			break;
		}
		if (camPos != null) {
			getMap().animateCamera(
					CameraUpdateFactory.newLatLngZoom(camPos, 17));
		} else {
			if (bounds != null) {
				getMap().animateCamera(
						CameraUpdateFactory.newLatLngBounds(bounds, 30));
			}
		}

	}

	private void switchFromCustomTileProvider() {
		if (customTileProvider != null) {
			// close tile provider
			customTileProvider = null;

			if (customTileOverlay != null) {
				customTileOverlay.remove();
				customTileOverlay = null;
			}
		}

	}

	private void switchToCustomMapProvider(TileProvider tileProvider) {

		if (customTileOverlay != null) {
			customTileOverlay.remove();
			customTileOverlay = null;
		}

		// Create new TileOverlayOptions instance.
		TileOverlayOptions opts = new TileOverlayOptions();
		opts.zIndex(1);

		customTileProvider = tileProvider;

		// if (customTileProvider != null) {
		// // customTileProvider.close();
		// }
		//
		// // Create an instance of MapBoxOfflineTileProvider.
		// customTileProvider = new OpenStreetMapTileProvider(new
		// GoogleTileCache());

		// Set the tile provider on the TileOverlayOptions.
		opts.tileProvider(customTileProvider);

		// Add the tile overlay to the map.
		customTileOverlay = getMap().addTileOverlay(opts);

	}

	void chooseMyTrackDialog(
			final List<com.google.android.apps.mytracks.content.Track> tracks) {

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setTitle("Select a Track: ");

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, R.layout.single_sel_list_item_with_icon) {
			final class ViewHolder {
				TextView text;
				TextView descr;
				TextView icon;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View rowView = convertView;
				if (rowView == null) {
					LayoutInflater inflater = MainActivity.this
							.getLayoutInflater();
					rowView = inflater.inflate(
							R.layout.single_sel_list_item_with_icon, null);
					ViewHolder viewHolder = new ViewHolder();

					viewHolder.text = (TextView) rowView
							.findViewById(R.id.title);
					viewHolder.descr = (TextView) rowView
							.findViewById(R.id.description);
					viewHolder.icon = (TextView) rowView
							.findViewById(R.id.icon);
					rowView.setTag(viewHolder);
				}

				ViewHolder holder = (ViewHolder) rowView.getTag();

				holder.text.setText(tracks.get(position).getName());
				holder.descr.setText(tracks.get(position).getDescription());

				holder.icon.setText(R.string.gpx_track_short);
				holder.icon.setBackgroundResource(R.drawable.list_icon_bg_3);

				// holder.icon.setImageResource(R.drawable.map_pin_holed_violet_normal_small);

				return rowView;
			}
		};

		for (com.google.android.apps.mytracks.content.Track track : tracks) {
			arrayAdapter.add(track.getName());
		}

		builderSingle.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builderSingle.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = arrayAdapter.getItem(which);
						if (Util.isDebugMode()) {

							Toast.makeText(MainActivity.this,
									"you chose: " + strName, Toast.LENGTH_LONG)
									.show();
						}
						dialog.dismiss();

						LoadMyTracksTrackDetailsTask task = new LoadMyTracksTrackDetailsTask();
						task.execute(tracks.get(which));

					}
				});

		builderSingle.show();

	}

	void chooseGPXCategoryEntryDialog(final GPXContentHolder gpxContent,
			final int sectionid) {

		// precheck if we have just one record
		// in that case we skipp the user interaction !
		switch (sectionid) {
		case 0: // Pois
			if (gpxContent.getPoints() != null) {
				if (gpxContent.getPoints().size() == 1) {
					moveToGPXCOntent(sectionid, 0);
					return;
				}
			} else {
				return;
			}
			break;
		case 1: // Routes
			if (gpxContent.getRoutes() != null) {
				if (gpxContent.getRoutes().size() == 1) {
					moveToGPXCOntent(sectionid, 0);
					return;
				}
			} else {
				return;
			}
			break;
		case 2: // Tracks
			if (gpxContent.getTracks() != null) {
				if (gpxContent.getTracks().size() == 1) {
					moveToGPXCOntent(sectionid, 0);
					return;
				}
			} else {
				return;
			}
			break;
		}

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		builderSingle.setIcon(R.drawable.ic_launcher);

		String[] gpxSectionTitles = getResources().getStringArray(
				R.array.gpx_sections);

		builderSingle.setTitle(gpxSectionTitles[sectionid]);

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, R.layout.single_sel_list_item_with_icon) {
			final class ViewHolder {
				TextView text;
				TextView descr;
				TextView icon;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View rowView = convertView;
				if (rowView == null) {
					LayoutInflater inflater = MainActivity.this
							.getLayoutInflater();
					rowView = inflater.inflate(
							R.layout.single_sel_list_item_with_icon, null);
					ViewHolder viewHolder = new ViewHolder();

					viewHolder.text = (TextView) rowView
							.findViewById(R.id.title);
					viewHolder.descr = (TextView) rowView
							.findViewById(R.id.description);
					viewHolder.icon = (TextView) rowView
							.findViewById(R.id.icon);
					rowView.setTag(viewHolder);
				}

				ViewHolder holder = (ViewHolder) rowView.getTag();

				switch (sectionid) {
				case 0: // Pois

					PoiPoint point = gpxContent.getPoints().get(position);

					holder.icon.setText("P");
					holder.icon.setText(R.string.gpx_poi_short);
					holder.icon
							.setBackgroundResource(R.drawable.list_icon_bg_1);
					holder.text.setText(point.getTitle());
					holder.descr.setText(point.getDescr());

					break;
				case 1: // Routes

					Route route = gpxContent.getRoutes().get(position);

					holder.icon.setText(R.string.gpx_route_short);
					holder.icon
							.setBackgroundResource(R.drawable.list_icon_bg_2);
					holder.text.setText(route.getName());
					holder.descr.setText(route.getDescr());

					break;
				case 2: // Tracks

					Track track = gpxContent.getTracks().get(position);

					holder.icon.setText(R.string.gpx_track_short);
					holder.icon
							.setBackgroundResource(R.drawable.list_icon_bg_3);

					holder.text.setText(track.Name);
					holder.descr.setText(track.Descr);

					break;
				}

				return rowView;
			}
		};

		// final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
		// this, android.R.layout.select_dialog_singlechoice);

		// for (com.google.android.apps.mytracks.content.Track track : tracks) {
		// arrayAdapter.add(track.getName());
		// }

		switch (sectionid) {
		case 0: // Pois
			for (PoiPoint point : gpxContent.getPoints()) {
				arrayAdapter.add(point.getTitle());
			}
			break;
		case 1: // Routes
			for (Route route : gpxContent.getRoutes()) {
				arrayAdapter.add(route.getName());
			}
			break;
		case 2: // Tracks
			for (Track track : gpxContent.getTracks()) {
				arrayAdapter.add(track.Name);
			}
			break;
		}

		builderSingle.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builderSingle.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (Util.isDebugMode()) {
							String strName = arrayAdapter.getItem(which);
							Toast.makeText(MainActivity.this,
									"you chose: " + strName, Toast.LENGTH_LONG)
									.show();
						}
						dialog.dismiss();

						moveToGPXCOntent(sectionid, which);

					}
				});

		builderSingle.show();

	}

	void chooseCustomMapDialog() {

		AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setTitle("Select custom Map: ");

		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, R.layout.single_sel_list_item_with_icon) {
			final class ViewHolder {
				TextView text;
				TextView descr;
				TextView icon;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View rowView = convertView;
				if (rowView == null) {
					LayoutInflater inflater = MainActivity.this
							.getLayoutInflater();
					rowView = inflater.inflate(
							R.layout.single_sel_list_item_with_icon, null);
					ViewHolder viewHolder = new ViewHolder();

					viewHolder.text = (TextView) rowView
							.findViewById(R.id.title);
					viewHolder.descr = (TextView) rowView
							.findViewById(R.id.description);
					viewHolder.icon = (TextView) rowView
							.findViewById(R.id.icon);
					rowView.setTag(viewHolder);
				}

				ViewHolder holder = (ViewHolder) rowView.getTag();

				holder.text.setText(MapSourcesManager.getAllMapSources()
						.get(position).getMapName());
				holder.descr.setText(" ");
				holder.icon.setText(R.string.gpx_map_short);
				// holder.icon
				// .setImageResource(R.drawable.map_pin_holed_violet_normal_small);

				return rowView;
			}
		};

		for (BaseTileProvider btp : MapSourcesManager.getAllMapSources()) {
			arrayAdapter.add(btp.getMapName());
		}

		builderSingle.setNegativeButton("cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builderSingle.setAdapter(arrayAdapter,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strName = arrayAdapter.getItem(which);

						if (Util.isDebugMode()) {
							Toast.makeText(MainActivity.this,
									"you chose Map: " + strName,
									Toast.LENGTH_LONG).show();
						}
						dialog.dismiss();

						switchToCustomMapProvider(MapSourcesManager
								.getAllMapSources().get(which));

					}
				});

		builderSingle.show();

	}
}