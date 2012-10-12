/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anize.ur.life.wimp.activities;

import java.util.ArrayList;

import org.anize.ur.life.wimp.R;
import org.anize.ur.life.wimp.util.Config;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.cyrilmottier.polaris.Annotation;
import com.cyrilmottier.polaris.MapCalloutView;
import com.cyrilmottier.polaris.MapViewUtils;
import com.cyrilmottier.polaris.PolarisMapView;
import com.cyrilmottier.polaris.PolarisMapView.OnAnnotationSelectionChangedListener;
import com.cyrilmottier.polaris.PolarisMapView.OnRegionChangedListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

/**
 * Activity which lets the user select a search area
 * 
 */

public class MainActivity extends MapActivity implements
		OnAnnotationSelectionChangedListener {

	private static final String LOG_TAG = "MainActivity";

	private PolarisMapView mMapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		mMapView = new PolarisMapView(this, Config.GOOGLE_MAPS_API_KEY);
		mMapView.setUserTrackingButtonEnabled(true);
		// mMapView.setOnRegionChangedListenerListener(this);
		mMapView.setOnAnnotationSelectionChangedListener(this);

		// Prepare an alternate pin Drawable
		final Drawable altMarker = MapViewUtils
				.boundMarkerCenterBottom(getResources().getDrawable(
						R.drawable.map_pin_holed_violet));

		// Prepare the list of Annotation using the alternate Drawable for all
		// Annotation located in France
		final ArrayList<Annotation> annotations = new ArrayList<Annotation>();

		Intent intent = getIntent(); // do we have a requested position ?

		if (intent.getExtras() != null) {
			double lat = intent.getExtras().getDouble("lat", 0);
			double lon = intent.getExtras().getDouble("lon", 0);
			String title = intent.getExtras(). getString("title",
					"current Position");
			String descr = intent.getExtras().getString("content", "here I am");

			Annotation annotation = new Annotation(new GeoPoint(
					(int) (lat * 1E6), (int) (lon * 1E6)), title, descr);
			annotation.setMarker(altMarker);
			annotations.add(annotation);
		}

		mMapView.setAnnotations(annotations, R.drawable.map_pin_holed_blue);

		final FrameLayout mapViewContainer = (FrameLayout) findViewById(R.id.map_view_container);
		mapViewContainer.addView(mMapView, new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMapView.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mMapView.onStop();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onAnnotationSelected(PolarisMapView mapView,
			MapCalloutView calloutView, int position, Annotation annotation) {
		if (Config.INFO_LOGS_ENABLED) {
			Log.i(LOG_TAG, "onAnnotationSelected");
		}
		calloutView.setDisclosureEnabled(true);
		calloutView.setClickable(true);
		if (!TextUtils.isEmpty(annotation.getSnippet())) {
			calloutView.setLeftAccessoryView(getLayoutInflater().inflate(
					R.layout.accessory, calloutView, false));
		} else {
			calloutView.setLeftAccessoryView(null);
		}
	}

	@Override
	public void onAnnotationDeselected(PolarisMapView mapView,
			MapCalloutView calloutView, int position, Annotation annotation) {
		if (Config.INFO_LOGS_ENABLED) {
			Log.i(LOG_TAG, "onAnnotationDeselected");
		}
	}

	@Override
	public void onAnnotationClicked(PolarisMapView mapView,
			MapCalloutView calloutView, int position, Annotation annotation) {
		if (Config.INFO_LOGS_ENABLED) {
			Log.i(LOG_TAG, "onAnnotationClicked");
		}
		Toast.makeText(this,
				getString(R.string.hello_world, annotation.getTitle()),
				Toast.LENGTH_SHORT).show();
	}
}

// public class MainActivity extends MapActivity implements OnClickListener {
//
//
//
// private MapView mMapView;
// private MyLocationOverlay mMyLocationOverlay;
// public static final int MILLION = 1000000;
//
// @Override
// public void onCreate(final Bundle savedInstanceState) {
// super.onCreate(savedInstanceState);
//
// setContentView(R.layout.map);
//
// final FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
// // final Button goButton = (Button) findViewById(R.id.go);
// // goButton.setOnClickListener(this);
//
// // Add the map view to the frame
// mMapView = new MapView(this, "0hXhWRAFjnpU-H9AvNSrgo5VXwFbwg-eyuPu2Yw");
// frame.addView(mMapView, new FrameLayout.LayoutParams(
// android.view.ViewGroup.LayoutParams.MATCH_PARENT,
// android.view.ViewGroup.LayoutParams.MATCH_PARENT));
//
// // Create an overlay to show current location
// mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
// mMyLocationOverlay.runOnFirstFix(new Runnable() {
// @Override
// public void run() {
// mMapView.getController().animateTo(
// mMyLocationOverlay.getMyLocation());
// }
// });
//
// mMapView.getOverlays().add(mMyLocationOverlay);
// mMapView.getController().setZoom(15);
// mMapView.setClickable(true);
// mMapView.setEnabled(true);
// mMapView.setSatellite(true);
//
// mMapView.setOnClickListener(this);
//
// addZoomControls(frame);
// }
//
// @Override
// protected void onResume() {
// super.onResume();
// mMyLocationOverlay.enableMyLocation();
// }
//
// @Override
// protected void onStop() {
// mMyLocationOverlay.disableMyLocation();
// super.onStop();
// }
//
// /**
// * Add zoom controls to our frame layout
// */
// private void addZoomControls(final FrameLayout frame) {
// // Get the zoom controls and add them to the bottom of the map
// // View zoomControls = mMapView.getZoomControls();
// //
// // FrameLayout.LayoutParams p =
// // new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
// // LayoutParams.WRAP_CONTENT, Gravity.BOTTOM +
// // Gravity.CENTER_HORIZONTAL);
// // frame.addView(zoomControls, p);
// mMapView.setBuiltInZoomControls(true);
// }
//
// @Override
// protected boolean isRouteDisplayed() {
// return false;
// }
//
// /**
// * Starts a new search when the user clicks the search button.
// */
// @Override
// public void onClick(final View view) {
// // Get the search area
// final int latHalfSpan = mMapView.getLatitudeSpan() >> 1;
// final int longHalfSpan = mMapView.getLongitudeSpan() >> 1;
//
// // Remember how the map was displayed so we can show it the same way
// // later
// final GeoPoint center = mMapView.getMapCenter();
// final int zoom = mMapView.getZoomLevel();
// final int latitudeE6 = center.getLatitudeE6();
// final int longitudeE6 = center.getLongitudeE6();
//
// // final Intent i = new Intent(this, ImageListActivity.class);
// // i.putExtra(ImageManager.ZOOM_EXTRA, zoom);
// // i.putExtra(ImageManager.LATITUDE_E6_EXTRA, latitudeE6);
// // i.putExtra(ImageManager.LONGITUDE_E6_EXTRA, longitudeE6);
// //
// // final float minLong = ((float) (longitudeE6 - longHalfSpan)) /
// // MILLION;
// // final float maxLong = ((float) (longitudeE6 + longHalfSpan)) /
// // MILLION;
// //
// // final float minLat = ((float) (latitudeE6 - latHalfSpan)) / MILLION;
// // final float maxLat = ((float) (latitudeE6 + latHalfSpan)) / MILLION;
// //
// // i.putExtra(ImageManager.LATITUDE_E6_MIN, minLat);
// // i.putExtra(ImageManager.LATITUDE_E6_MAX, maxLat);
// // i.putExtra(ImageManager.LONGITUDE_E6_MIN, minLong);
// // i.putExtra(ImageManager.LONGITUDE_E6_MAX, maxLong);
// //
// // // Show results
// // startActivity(i);
// }
// }
