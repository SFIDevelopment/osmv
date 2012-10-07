/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.the.gogo.parkoid.sensors.proximity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * Displays the UI tha allows the user to set a proximity alert for a specified
 * location.
 * 
 * @author Adam Stroud &#60;<a
 *         href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class ProximityAlertActivity extends Activity {
    private static final String USE_ANDROID_PROXIMITY_TYPE_KEY = "useAndroidProximityTypeKey";

    private LocationManager     locationManager;
    private PendingIntent       pendingIntent;
    private SharedPreferences   preferences;
    private RadioButton         androidProximityTypeRadioButton;
    private Button              setProximityAlert;
    private Button              clearProximityAlert;
    private double              latitude                       = Double.MAX_VALUE;
    private double              longitude                      = Double.MAX_VALUE;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.proximity_alert);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        pendingIntent = ProximityPendingIntentFactory.createPendingIntent(this);

        preferences = getPreferences(MODE_PRIVATE);
        // androidProximityTypeRadioButton =
        // (RadioButton)findViewById(R.id.androidProximityAlert);
        //
        // setProximityAlert = (Button) findViewById(R.id.setProximityAlert);
        // clearProximityAlert = (Button)
        // findViewById(R.id.clearProximityAlert);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (preferences.getBoolean(USE_ANDROID_PROXIMITY_TYPE_KEY, true)) {
            androidProximityTypeRadioButton.setChecked(true);
        } else {
            // ((RadioButton)findViewById(R.id.customProximityAlert)).setChecked(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeProximityAlert(pendingIntent);
        preferences
                .edit()
                .putBoolean(USE_ANDROID_PROXIMITY_TYPE_KEY,
                        androidProximityTypeRadioButton.isChecked()).commit();
    }

    public void onSetProximityAlertClick(final View view) {
        // EditText radiusView = (EditText)findViewById(R.id.radiusValue);
        final int radius = 125;
        // Integer.parseInt(radiusView.getText().toString());

        if (androidProximityTypeRadioButton.isChecked()) {
            locationManager.addProximityAlert(latitude, longitude, radius, -1,
                    pendingIntent);
        } else {
            final Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            final Intent intent = new Intent(this, ProximityAlertService.class);
            intent.putExtra(ProximityAlertService.LATITUDE_INTENT_KEY, latitude);
            intent.putExtra(ProximityAlertService.LONGITUDE_INTENT_KEY,
                    longitude);
            intent.putExtra(ProximityAlertService.RADIUS_INTENT_KEY,
                    (float) radius);
            startService(intent);
        }

        setProximityAlert.setEnabled(false);
        clearProximityAlert.setEnabled(true);
    }

    public void onClearProximityAlertClick(final View view) {
        if (androidProximityTypeRadioButton.isChecked()) {
            locationManager.removeProximityAlert(pendingIntent);
        }

        setProximityAlert.setEnabled(true);
        clearProximityAlert.setEnabled(false);
    }

    public void onSetLocationClick(final View view) {
        // startActivityForResult(new Intent(this, GeocodeActivity.class), 1);
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((resultCode == RESULT_OK) && (data != null)
                && data.hasExtra("name") && data.hasExtra("latitude")
                && data.hasExtra("longitude")) {
            latitude = data.getDoubleExtra("latitude", Double.MAX_VALUE);
            longitude = data.getDoubleExtra("longitude", Double.MAX_VALUE);

            // ((TextView)findViewById(R.id.locationValue)).setText(data.getStringExtra("name"));
            // ((TextView)findViewById(R.id.latitudeValue)).setText(String.valueOf(latitude));
            // ((TextView)findViewById(R.id.longitudeValue)).setText(String.valueOf(longitude));

            setProximityAlert.setEnabled(true);
            clearProximityAlert.setEnabled(false);
        }
    }
}
