/*
 * Copyright 2011 Mario Böhmer
 * 
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0
 * Unported (CC BY-NC-SA 3.0) you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */
package at.the.gogo.nfc.toggler.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;
import at.the.gogo.nfc.toggler.R;
import at.the.gogo.nfc.util.CoreInfoHolder;
import at.the.gogo.nfc.util.speech.SpeakItOut;

/**
 * {@link NFCTogglerActivity} manages the switching of configured preferences on
 * NFC tag detection.
 * 
 * @author Mario Boehmer / JIm Fandango
 */
public class NFCTogglerActivity extends Activity {

    private boolean manageWifi;
    private boolean manageBluetooth;
    private boolean manageRinger;
    private boolean manageAirplaneMode;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().setFeatureInt(Window.FEATURE_NO_TITLE, 0);
        // getWindow().setBackgroundDrawable(
        // new ColorDrawable(android.R.color.transparent));
        readPreferences();
        if (manageWifi) {
            final WifiManager wifimgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifimgr != null) {
                wifimgr.setWifiEnabled(!wifimgr.isWifiEnabled());

                if (CoreInfoHolder.getInstance().isSpeakit()) {
                    SpeakItOut.speak(getText(
                            wifimgr.isWifiEnabled() ? R.string.nfc_wifi_on
                                    : R.string.nfc_wifi_off).toString());
                }
            }
        }

        if (manageBluetooth) {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                } else if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                }

                if (CoreInfoHolder.getInstance().isSpeakit()) {
                    SpeakItOut
                            .speak(getText(
                                    bluetoothAdapter.isEnabled() ? R.string.nfc_bluetooth_on
                                            : R.string.nfc_bluetooth_off)
                                    .toString());
                }

            }
        }
        if (manageRinger) {
            final AudioManager audiomgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audiomgr != null) {
                audiomgr.setRingerMode((audiomgr.getRingerMode() == AudioManager.RINGER_MODE_SILENT) ? AudioManager.RINGER_MODE_NORMAL
                        : AudioManager.RINGER_MODE_SILENT);

                if (CoreInfoHolder.getInstance().isSpeakit()) {
                    SpeakItOut
                            .speak(getText(
                                    (audiomgr.getRingerMode() == AudioManager.RINGER_MODE_SILENT) ? R.string.nfc_ringer_off
                                            : R.string.nfc_ringer_on)
                                    .toString());
                }

            }
        }

        if (manageAirplaneMode) {
            // Toggle airplane mode.
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON,
                    isAirplaneModeOn(this) ? 0 : 1);

            // Change so that only radio cell is turned off
            // NOTE: This affects the behavior of the system button for
            // toggling air-plane mode. You might want to reset it, in order to
            // maintain the system behavior.
            Settings.System.putString(getContentResolver(),
                    Settings.System.AIRPLANE_MODE_RADIOS, "cell");

            // Post an intent to reload.
            final Intent intent = new Intent(
                    Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", isAirplaneModeOn(this));
            sendBroadcast(intent);

            if (CoreInfoHolder.getInstance().isSpeakit()) {
                SpeakItOut.speak(getText(
                        (isAirplaneModeOn(this)) ? R.string.nfc_airplane_on
                                : R.string.nfc_airplane_off).toString());
            }

        }

        final int toastId = R.string.toggle_notification;
        Toast.makeText(this, toastId, Toast.LENGTH_SHORT).show();
        finish();
    }

    private static boolean isAirplaneModeOn(final Context context) {

        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

    }

    private void readPreferences() {
        // final SharedPreferences preferences = getSharedPreferences(
        // "at.the.gogo.nfc.toggler.preferences",
        // Context.MODE_PRIVATE);
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        manageWifi = preferences.getBoolean("wifi_preference", false);
        manageBluetooth = preferences.getBoolean("bluetooth_preference", false);
        manageRinger = preferences.getBoolean("ringer_preference", false);
        manageAirplaneMode = preferences.getBoolean("airplane_preference",
                false);
    }
}
