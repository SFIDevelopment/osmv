package org.outlander.sms.responder;

import org.outlander.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class AutoResponder extends Activity {

    public static final String alarmAction = "org.outlander.sms.responder.AUTO_RESPONSE_EXPIRED";

    PendingIntent              intentToFire;

    Spinner                    respondForSpinner;
    CheckBox                   locationCheckbox;
    EditText                   responseTextBox;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autoresponder);
        respondForSpinner = (Spinner) findViewById(R.id.spinnerRespondFor);
        locationCheckbox = (CheckBox) findViewById(R.id.checkboxLocation);
        responseTextBox = (EditText) findViewById(R.id.responseText);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.respondForDisplayItems,
                        android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        respondForSpinner.setAdapter(adapter);
        final Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                savePreferences();
                setResult(Activity.RESULT_OK, null);
                finish();
            }
        });

        final Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                respondForSpinner.setSelection(-1);
                savePreferences();
                setResult(Activity.RESULT_CANCELED, null);
                finish();
            }
        });
        // Load the saved preferences and update the UI
        updateUIFromPreferences();
    }

    private void updateUIFromPreferences() {
        // Get the saves settings
        final String preferenceName = getString(R.string.user_preferences);
        final SharedPreferences sp = getSharedPreferences(preferenceName, 0);

        final String autoResponsePref = getString(R.string.autoRespondPref);
        final String responseTextPref = getString(R.string.responseTextPref);
        final String includeLocPref = getString(R.string.includeLocationPref);
        final String respondForPref = getString(R.string.respondForPref);

        final boolean autoRespond = sp.getBoolean(autoResponsePref, false);
        final String respondText = sp.getString(responseTextPref, "");
        final boolean includeLoc = sp.getBoolean(includeLocPref, false);
        final int respondForIndex = sp.getInt(respondForPref, 0);

        // Apply the saved settings to the UI
        if (autoRespond) {
            respondForSpinner.setSelection(respondForIndex);
        } else {
            respondForSpinner.setSelection(0);
        }

        locationCheckbox.setChecked(includeLoc);
        responseTextBox.setText(respondText);
    }

    private void savePreferences() {
        // Get the current settings from the UI
        final boolean autoRespond = respondForSpinner.getSelectedItemPosition() > 0;
        final int respondForIndex = respondForSpinner.getSelectedItemPosition();
        final boolean includeLoc = locationCheckbox.isChecked();
        final String respondText = responseTextBox.getText().toString();

        // Save them to the Shared Preference file
        final String preferenceName = getString(R.string.user_preferences);
        final SharedPreferences sp = getSharedPreferences(preferenceName, 0);

        final Editor editor = sp.edit();
        editor.putBoolean(getString(R.string.autoRespondPref), autoRespond);
        editor.putString(getString(R.string.responseTextPref), respondText);
        editor.putBoolean(getString(R.string.includeLocationPref), includeLoc);
        editor.putInt(getString(R.string.respondForPref), respondForIndex);
        editor.commit();

        // Set the alarm to turn off the autoresponder
        setAlarm(respondForIndex);
    }

    private void setAlarm(final int respondForIndex) {
        // Create the alarm and register the alarm intent receiver.
        final AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (intentToFire == null) {
            final Intent intent = new Intent(AutoResponder.alarmAction);
            intentToFire = PendingIntent.getBroadcast(getApplicationContext(),
                    0, intent, 0);

            final IntentFilter filter = new IntentFilter(
                    AutoResponder.alarmAction);
            registerReceiver(stopAutoResponderReceiver, filter);
        }

        if (respondForIndex < 1) {
            // If "disabled" is selected, cancel the alarm.
            alarms.cancel(intentToFire);
        } else {
            // Otherwise find the length of time represented
            // by the selection and and set the alarm to
            // trigger after that time has passed.
            final Resources r = getResources();
            final int[] respondForValues = r
                    .getIntArray(R.array.respondForValues);
            final int respondFor = respondForValues[respondForIndex];

            long t = System.currentTimeMillis();
            t = t + (respondFor * 1000 * 60);

            // Set the alarm.
            alarms.set(AlarmManager.RTC_WAKEUP, t, intentToFire);
        }
    }

    private final BroadcastReceiver stopAutoResponderReceiver = new BroadcastReceiver() {
                                                                  @Override
                                                                  public void onReceive(
                                                                          final Context context,
                                                                          final Intent intent) {
                                                                      if (intent
                                                                              .getAction()
                                                                              .equals(AutoResponder.alarmAction)) {
                                                                          final String preferenceName = getString(R.string.user_preferences);
                                                                          final SharedPreferences sp = getSharedPreferences(
                                                                                  preferenceName,
                                                                                  0);

                                                                          final Editor editor = sp
                                                                                  .edit();
                                                                          editor.putBoolean(
                                                                                  getString(R.string.autoRespondPref),
                                                                                  false);
                                                                          editor.commit();
                                                                      }
                                                                  }
                                                              };
}