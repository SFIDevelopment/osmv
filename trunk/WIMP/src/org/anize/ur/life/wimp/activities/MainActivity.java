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
		mMapView.setSelectedAnnotation(0);

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

