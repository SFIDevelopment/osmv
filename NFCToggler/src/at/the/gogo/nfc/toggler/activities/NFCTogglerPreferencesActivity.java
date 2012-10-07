/*
 * Copyright 2012 Jim Fandango
 * 
 * Licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0
 * Unported (CC BY-NC-SA 3.0) you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */
package at.the.gogo.nfc.toggler.activities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import at.the.gogo.nfc.toggler.R;
import at.the.gogo.nfc.util.CoreInfoHolder;
import at.the.gogo.nfc.util.CrashReportHandler;
import at.the.gogo.nfc.util.StrictModeWrapper;
import at.the.gogo.nfc.util.Ut;

/**
 * {@link NFCTogglerPreferencesActivity} displays the preferences which can be
 * configured to be switched on NFC tag detection.
 * 
 * @author Jim Fandango
 */
public class NFCTogglerPreferencesActivity extends PreferenceActivity implements
        TextToSpeech.OnInitListener {
    private static final int MY_TTS_CHECK_CODE = 1234;
    boolean                  wantToUseTTS      = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.nfc_preferences);
        setContentView(R.layout.preference_layout);

        final int applicationFlags = getApplicationInfo().flags;
        if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            CrashReportHandler.attach(this);
        }

        if (Ut.DEBUGMODE) {
            try {
                StrictModeWrapper.init(this);
            } catch (final Throwable throwable) {
                Ut.i("StrictMode is not available!");
            }
        }

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        wantToUseTTS = sharedPreferences.getBoolean("pref_tts_speech", true);

        checkTTS();

        if (sharedPreferences.getString("error", "").length() > 0) {
            showDialog(R.id.error);
        }

        if (!sharedPreferences.getString("app_version", "").equalsIgnoreCase(
                Ut.getAppVersion(this))) {
            showDialog(R.id.whatsnew);

            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("app_version", Ut.getAppVersion(this));
            editor.commit();

        }
    }

    @Override
    protected void onDestroy() {

        if (CoreInfoHolder.getInstance().isSpeakit()) {
            // SpeakItOut.speak(getText(R.string.tts_bye).toString());
        }
        if (CoreInfoHolder.getInstance().getTts() != null) {
            CoreInfoHolder.getInstance().getTts().stop();
            CoreInfoHolder.getInstance().getTts().shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(final Void... params) {
                final NfcAdapter nfcAdapter = NfcAdapter
                        .getDefaultAdapter(NFCTogglerPreferencesActivity.this);
                if (nfcAdapter != null) {
                    return nfcAdapter.isEnabled();
                }
                return false;
            }

            @Override
            protected void onPostExecute(final Boolean enabled) {
                if (!enabled) {
                    final AlertDialog.Builder nfcDisabledDialogBuilder = new AlertDialog.Builder(
                            NFCTogglerPreferencesActivity.this);
                    nfcDisabledDialogBuilder
                            .setMessage(R.string.nfc_disabled_message);
                    nfcDisabledDialogBuilder.setCancelable(true);
                    final OnClickListener onClicklistener = new OnClickListener() {

                        @Override
                        public void onClick(final DialogInterface dialog,
                                final int which) {
                            dialog.dismiss();
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                final Intent systemSettingsIntent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(systemSettingsIntent);
                            }
                        }
                    };
                    nfcDisabledDialogBuilder.setPositiveButton(R.string.yes,
                            onClicklistener);
                    nfcDisabledDialogBuilder.setNegativeButton(R.string.no,
                            onClicklistener);
                    nfcDisabledDialogBuilder.show();
                }
            };

        }.execute((Void[]) null);

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean result = false;
        switch (item.getItemId()) {
            case (R.id.about): {
                showDialog(R.id.about);
                result = true;
                break;
            }
            case (R.id.feedback): {
                sendFeedback();
                result = true;
                break;
            }
        }
        return result;
    }

    // private void showInfo() {
    // final Intent infoTextIntent = new Intent(getApplicationContext(),
    // InfoActivity.class);
    // startActivity(infoTextIntent);
    // }

    private void sendFeedback() {
        final Intent feedbackMailIntent = new Intent(Intent.ACTION_SEND);
        feedbackMailIntent.setType("plain/text");
        feedbackMailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[] { getString(R.string.feedback_mail_address) });
        feedbackMailIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.feedback));
        feedbackMailIntent
                .putExtra(Intent.EXTRA_TEXT, "\n\n" + getSystemInfo());
        startActivity(feedbackMailIntent);
    }

    private String getSystemInfo() {
        final StringBuilder builder = new StringBuilder();
        builder.append("---------- System Info ----------");
        builder.append("\n");
        builder.append("OS Version: ");
        builder.append(Build.VERSION.RELEASE);
        builder.append("\n");
        builder.append("OS Api Level: ");
        builder.append(Build.VERSION.SDK_INT);
        builder.append("\n");
        builder.append("Manufacturer: ");
        builder.append(Build.MANUFACTURER);
        builder.append("\n");
        builder.append("Model: ");
        builder.append(Build.MODEL);
        builder.append("\n");
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_ACTIVITIES);
            builder.append("App VersionCode: ");
            builder.append(packageInfo.versionCode);
            builder.append("\n");
            builder.append("App VersionName: ");
            builder.append(packageInfo.versionName);
            builder.append("\n");
        } catch (final Exception e) {
        }
        builder.append("---------- System Info ----------");
        return builder.toString();
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        if (requestCode == NFCTogglerPreferencesActivity.MY_TTS_CHECK_CODE) {
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
            // restart
            // finish();
            // startActivity(new Intent(this, this.getClass()));
        }

        // }
    }

    private void checkTTS() {
        // Fire off an intent to check if a TTS engine is installed
        final Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent,
                NFCTogglerPreferencesActivity.MY_TTS_CHECK_CODE);
        // }
    }

    // for TTS
    @Override
    public void onInit(final int status) {

        CoreInfoHolder.getInstance().setSpeakit(wantToUseTTS); // wanted &
                                                               // installed
        // if (CoreInfoHolder.getInstance().isSpeakit()) {
        // SpeakItOut.speak(getText(R.string.tts_welcome).toString());
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
                                        + Ut.getAppVersion(this) + "\n\n"
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
                        .setMessage(
                                Html.fromHtml(getString(R.string.error_text)))
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
                                                + " error: ";
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
                                                + Ut.getAppVersion(NFCTogglerPreferencesActivity.this)
                                                + "\nAndroid: " + v.RELEASE
                                                + "\nDevice: " + b.BOARD + " "
                                                + b.BRAND + " " + b.DEVICE
                                                + " " + b.MANUFACTURER + " "
                                                + b.MODEL + " " + b.PRODUCT
                                                + "\n\n" + text;

                                        startActivity(Ut.sendErrorReportMail(
                                                subj, text));
                                        Ut.e(text);
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

}
