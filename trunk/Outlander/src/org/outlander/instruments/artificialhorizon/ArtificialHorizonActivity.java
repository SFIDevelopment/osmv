package org.outlander.instruments.artificialhorizon;

import org.outlander.R;
import org.outlander.views.ArtificialHorizontView;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class ArtificialHorizonActivity extends Activity {
    float[]                aValues = new float[3];
    float[]                mValues = new float[3];
    ArtificialHorizontView artificialhorizonView;
    // CompassView compassView;
    SensorManager          sensorManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.artificialhorizon);

        artificialhorizonView = (ArtificialHorizontView) findViewById(R.id.artificialhorizonView);
        // compassView = (CompassView) findViewById(R.id.compassView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        updateOrientation(new float[] { 0, 0, 0 });
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.artificialhorizon);

        updateOrientation(new float[] { 0, 0, 0 });
    }

    private void updateOrientation(final float[] values) {
        if (artificialhorizonView != null) {
            artificialhorizonView.setBearing(values[0]);
            artificialhorizonView.setPitch(values[1]);
            artificialhorizonView.setRoll(-values[2]);
            artificialhorizonView.invalidate();
        }
    }

    private float[] calculateOrientation() {
        final float[] values = new float[3];
        final float[] R = new float[9];
        final float[] outR = new float[9];

        SensorManager.getRotationMatrix(R, null, aValues, mValues);
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X,
                SensorManager.AXIS_Z, outR);

        SensorManager.getOrientation(outR, values);

        // Convert from Radians to Degrees.
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        return values;
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
                                                              @Override
                                                              public void onSensorChanged(
                                                                      final SensorEvent event) {
                                                                  if (event.sensor
                                                                          .getType() == Sensor.TYPE_ACCELEROMETER) {
                                                                      aValues = event.values;
                                                                  }
                                                                  if (event.sensor
                                                                          .getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                                                                      mValues = event.values;
                                                                  }

                                                                  updateOrientation(calculateOrientation());
                                                              }

                                                              @Override
                                                              public void onAccuracyChanged(
                                                                      final Sensor sensor,
                                                                      final int accuracy) {
                                                              }
                                                          };

    @Override
    protected void onResume() {
        super.onResume();

        final Sensor accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        final Sensor magField = sensorManager
                .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener, accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener, magField,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onStop();
    }
}