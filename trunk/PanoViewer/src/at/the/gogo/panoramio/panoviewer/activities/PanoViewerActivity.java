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

package at.the.gogo.panoramio.panoviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import at.the.gogo.panoramio.panoviewer.ImageManager;
import at.the.gogo.panoramio.panoviewer.R;
import at.the.gogo.panoramio.panoviewer.R.id;
import at.the.gogo.panoramio.panoviewer.R.layout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

/**
 * Activity which lets the user select a search area
 * 
 */
public class PanoViewerActivity extends MapActivity implements OnClickListener {
	private MapView mMapView;
	private MyLocationOverlay mMyLocationOverlay;
	public static final int MILLION = 1000000;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		final FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		final Button goButton = (Button) findViewById(R.id.go);
		goButton.setOnClickListener(this);

		// Add the map view to the frame
		mMapView = new MapView(this, "0hXhWRAFjnpU-H9AvNSrgo5VXwFbwg-eyuPu2Yw");
		frame.addView(mMapView, new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		// Create an overlay to show current location
		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMyLocationOverlay.runOnFirstFix(new Runnable() {
			@Override
			public void run() {
				mMapView.getController().animateTo(
						mMyLocationOverlay.getMyLocation());
			}
		});

		mMapView.getOverlays().add(mMyLocationOverlay);
		mMapView.getController().setZoom(15);
		mMapView.setClickable(true);
		mMapView.setEnabled(true);
		mMapView.setSatellite(true);

		mMapView.setOnClickListener(this);
		
		
		addZoomControls(frame);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMyLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onStop() {
		mMyLocationOverlay.disableMyLocation();
		super.onStop();
	}

	/**
	 * Add zoom controls to our frame layout
	 */
	private void addZoomControls(final FrameLayout frame) {
		// Get the zoom controls and add them to the bottom of the map
		// View zoomControls = mMapView.getZoomControls();
		//
		// FrameLayout.LayoutParams p =
		// new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT, Gravity.BOTTOM +
		// Gravity.CENTER_HORIZONTAL);
		// frame.addView(zoomControls, p);
		mMapView.setBuiltInZoomControls(true);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Starts a new search when the user clicks the search button.
	 */
	@Override
	public void onClick(final View view) {
		// Get the search area
		final int latHalfSpan = mMapView.getLatitudeSpan() >> 1;
		final int longHalfSpan = mMapView.getLongitudeSpan() >> 1;

		// Remember how the map was displayed so we can show it the same way
		// later
		final GeoPoint center = mMapView.getMapCenter();
		final int zoom = mMapView.getZoomLevel();
		final int latitudeE6 = center.getLatitudeE6();
		final int longitudeE6 = center.getLongitudeE6();

		final Intent i = new Intent(this, ImageListActivity.class);
		i.putExtra(ImageManager.ZOOM_EXTRA, zoom);
		i.putExtra(ImageManager.LATITUDE_E6_EXTRA, latitudeE6);
		i.putExtra(ImageManager.LONGITUDE_E6_EXTRA, longitudeE6);

		final float minLong = ((float) (longitudeE6 - longHalfSpan)) / MILLION;
		final float maxLong = ((float) (longitudeE6 + longHalfSpan)) / MILLION;

		final float minLat = ((float) (latitudeE6 - latHalfSpan)) / MILLION;
		final float maxLat = ((float) (latitudeE6 + latHalfSpan)) / MILLION;

		i.putExtra(ImageManager.LATITUDE_E6_MIN, minLat);
		i.putExtra(ImageManager.LATITUDE_E6_MAX, maxLat);
		i.putExtra(ImageManager.LONGITUDE_E6_MIN, minLong);
		i.putExtra(ImageManager.LONGITUDE_E6_MAX, maxLong);

		// Show results
		startActivity(i);
	}
}
