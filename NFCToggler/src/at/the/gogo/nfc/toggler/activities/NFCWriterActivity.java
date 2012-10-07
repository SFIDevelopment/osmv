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

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.media.MediaPlayer;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.the.gogo.nfc.toggler.R;
import at.the.gogo.nfc.util.CoreInfoHolder;
import at.the.gogo.nfc.util.speech.SpeakItOut;

/**
 * {@link NFCWriterActivity} writes an URI Intent trigger for the NFC profile to
 * a NFC tag.
 * 
 * @author Mario Boehmer
 */
public class NFCWriterActivity extends Activity {

    private IntentFilter[]      intentFiltersArray;
    private String[][]          techListsArray;
    private NfcAdapter          mNfcAdapter;
    private PendingIntent       pendingIntent;
    private static final String URI       = "nfctoggler://at.the.gogo.nfc/toggler";
    private TextView            nfcWriterMessage;
    private ProgressBar         progressbar;
    private static final byte   NO_PREFIX = 0x00;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_writer_layout);
        nfcWriterMessage = (TextView) findViewById(R.id.nfc_writer_message);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        final IntentFilter ndef = new IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (final MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[] { ndef };
        techListsArray = new String[][] { new String[] { Ndef.class.getName() } };
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            finish();
        }

        if (CoreInfoHolder.getInstance().isSpeakit()) {
            SpeakItOut.speak(getText(R.string.nfc_writer_waiting).toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
        progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent,
                intentFiltersArray, techListsArray);
        progressbar.setVisibility(View.VISIBLE);
        nfcWriterMessage.setText(R.string.nfc_writer_waiting);
    }

    @Override
    public void onNewIntent(final Intent intent) {
        new AsyncTask<Intent, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(final Intent... params) {
                boolean success = false;
                try {
                    success = writeUriToTag(params[0], NFCWriterActivity.URI);
                } catch (final IOException e) {
                } catch (final FormatException e) {
                }
                return success;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                super.onPostExecute(success);
                progressbar.setVisibility(View.GONE);
                MediaPlayer mediaPlayer = null;

                int soundId = R.raw.error;
                int textId = R.string.nfc_writer_error;

                if (success) {
                    soundId = R.raw.success;
                    textId = R.string.nfc_writer_success;
                }

                mediaPlayer = MediaPlayer.create(NFCWriterActivity.this,
                        soundId);
                nfcWriterMessage.setText(textId);
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.start();
                new Handler().postDelayed(finishRunnable, 3000);

                if (CoreInfoHolder.getInstance().isSpeakit()) {
                    SpeakItOut.speak(getText(textId).toString());
                } else {
                    // sound ?
                }
            }
        }.execute(intent);

    }

    Runnable finishRunnable = new Runnable() {

                                @Override
                                public void run() {
                                    NFCWriterActivity.this.finish();
                                }
                            };

    private boolean writeUriToTag(final Intent intent, final String uri)
            throws IOException, FormatException {
        final String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            final Ndef ndef = Ndef.get(tag);
            final byte[] data = concatByteArrays(
                    new byte[] { NFCWriterActivity.NO_PREFIX },
                    uri.getBytes(Charset.forName("UTF_8")));
            final NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_URI, new byte[0], data);
            try {
                final NdefRecord[] records = { record };
                final NdefMessage message = new NdefMessage(records);
                ndef.connect();
                ndef.writeNdefMessage(message);
                return true;
            } catch (final Exception e) {
            }
        }
        return false;
    }

    private byte[] concatByteArrays(final byte[] first, final byte[] second) {
        final byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
