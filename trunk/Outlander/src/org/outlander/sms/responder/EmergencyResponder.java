package org.outlander.sms.responder;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.outlander.R;
import org.outlander.utils.Ut;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

public class EmergencyResponder extends Activity {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String SENT_SMS     = "org.outlander.sms.responder.SMS_SENT";

    ReentrantLock              lock;
    CheckBox                   locationCheckBox;
    ArrayList<String>          requesters;
    ArrayAdapter<String>       aa;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergencyresponder);

        lock = new ReentrantLock();
        requesters = new ArrayList<String>();
        wireUpControls();

        final IntentFilter filter = new IntentFilter(
                EmergencyResponder.SMS_RECEIVED);
        registerReceiver(emergencyResponseRequestReceiver, filter);

        final IntentFilter attemptedDeliveryfilter = new IntentFilter(
                EmergencyResponder.SENT_SMS);
        registerReceiver(attemptedDeliveryReceiver, attemptedDeliveryfilter);
    }

    private void wireUpControls() {
        locationCheckBox = (CheckBox) findViewById(R.id.checkboxSendLocation);
        final ListView myListView = (ListView) findViewById(R.id.myListView);

        final int layoutID = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<String>(this, layoutID, requesters);
        myListView.setAdapter(aa);

        final Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                respond(true, locationCheckBox.isChecked());
            }
        });

        final Button notOkButton = (Button) findViewById(R.id.notOkButton);
        notOkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                respond(false, locationCheckBox.isChecked());
            }
        });

        final Button autoResponderButton = (Button) findViewById(R.id.autoResponder);
        autoResponderButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                startAutoResponder();
            }
        });
    }

    public void respond(final boolean _ok, final boolean _includeLocation) {
        final String okString = getString(R.string.respondAllClearText);
        final String notOkString = getString(R.string.respondMaydayText);

        final String outString = _ok ? okString : notOkString;

        @SuppressWarnings("unchecked")
        final ArrayList<String> requestersCopy = (ArrayList<String>) requesters
                .clone();

        for (final String to : requestersCopy) {
            respond(to, outString, _includeLocation);
        }
    }

    public void respond(final String _to, final String _response,
            final boolean _includeLocation) {
        // Remove the target from the list of people we
        // need to respond to.
        lock.lock();
        requesters.remove(_to);
        aa.notifyDataSetChanged();
        lock.unlock();

        final SmsManager sms = SmsManager.getDefault();

        final Intent intent = new Intent(EmergencyResponder.SENT_SMS);
        intent.putExtra("recipient", _to);

        final PendingIntent sentIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intent, 0);

        // Send the message
        sms.sendTextMessage(_to, null, _response, sentIntent, null);

        final StringBuilder sb = new StringBuilder();

        // Find the current location and send it
        // as SMS messages if required.
        if (_includeLocation) {
            final String ls = Context.LOCATION_SERVICE;
            final LocationManager lm = (LocationManager) getSystemService(ls);
            final Location l = lm
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

            sb.append("I'm @:\n");
            sb.append(l.toString() + "\n");

            sb.append("Address:\n").append(
                    Ut.getAddress(this, l.getLatitude(), l.getLongitude()));
            sb.append("Map:\n").append(
                    Ut.buildMapsUrl(this, (float) l.getLatitude(),
                            (float) l.getLongitude(), 18));

            final ArrayList<String> locationMsgs = sms.divideMessage(sb
                    .toString());
            for (final String locationMsg : locationMsgs) {
                sms.sendTextMessage(_to, null, locationMsg, sentIntent, null);
            }
        }
    }

    private void startAutoResponder() {
        startActivityForResult(new Intent(EmergencyResponder.this,
                AutoResponder.class), 0);
    }

    BroadcastReceiver emergencyResponseRequestReceiver = new BroadcastReceiver() {
                                                           @Override
                                                           public void onReceive(
                                                                   final Context _context,
                                                                   final Intent _intent) {
                                                               if (_intent
                                                                       .getAction()
                                                                       .equals(EmergencyResponder.SMS_RECEIVED)) {
                                                                   final String queryString = getString(R.string.querystring);

                                                                   final Bundle bundle = _intent
                                                                           .getExtras();
                                                                   if (bundle != null) {
                                                                       final Object[] pdus = (Object[]) bundle
                                                                               .get("pdus");
                                                                       final SmsMessage[] messages = new SmsMessage[pdus.length];
                                                                       for (int i = 0; i < pdus.length; i++) {
                                                                           messages[i] = SmsMessage
                                                                                   .createFromPdu((byte[]) pdus[i]);
                                                                       }

                                                                       for (final SmsMessage message : messages) {
                                                                           if (message
                                                                                   .getMessageBody()
                                                                                   .toLowerCase()
                                                                                   .contains(
                                                                                           queryString)) {
                                                                               requestReceived(message
                                                                                       .getOriginatingAddress());
                                                                           }
                                                                       }
                                                                   }
                                                               }
                                                           }
                                                       };

    public void requestReceived(final String _from) {
        if (!requesters.contains(_from)) {
            lock.lock();
            requesters.add(_from);
            aa.notifyDataSetChanged();
            lock.unlock();

            // Check for auto-responder
            final String preferenceName = getString(R.string.user_preferences);
            final SharedPreferences prefs = getSharedPreferences(
                    preferenceName, 0);
            final String autoRespondPref = getString(R.string.autoRespondPref);
            final boolean autoRespond = prefs
                    .getBoolean(autoRespondPref, false);

            if (autoRespond) {
                final String responseTextPref = getString(R.string.responseTextPref);
                final String includeLocationPref = getString(R.string.includeLocationPref);

                final String respondText = prefs
                        .getString(responseTextPref, "");
                final boolean includeLoc = prefs.getBoolean(
                        includeLocationPref, false);

                respond(_from, respondText, includeLoc);
            }
        }
    }

    private final BroadcastReceiver attemptedDeliveryReceiver = new BroadcastReceiver() {
                                                                  @Override
                                                                  public void onReceive(
                                                                          final Context _context,
                                                                          final Intent _intent) {
                                                                      if (_intent
                                                                              .getAction()
                                                                              .equals(EmergencyResponder.SENT_SMS)) {
                                                                          if (getResultCode() != Activity.RESULT_OK) {
                                                                              final String recipient = _intent
                                                                                      .getStringExtra("recipient");
                                                                              requestReceived(recipient);
                                                                          }
                                                                      }
                                                                  }
                                                              };
}