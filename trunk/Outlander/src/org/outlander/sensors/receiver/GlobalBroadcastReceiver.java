package org.outlander.sensors.receiver;

import org.outlander.utils.Ut;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class GlobalBroadcastReceiver extends BroadcastReceiver {

    public final static int NO_VALID_VALUE = -1;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        // check for battery event

        final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, NO_VALID_VALUE);
        final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, NO_VALID_VALUE);
        if ((level != NO_VALID_VALUE) && (scale != NO_VALID_VALUE)) {
            final float batteryPct = level / (float) scale;

            Ut.d("Battery State changed: " + (int) batteryPct + "%");
        }

        // check for docking state
        final int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, NO_VALID_VALUE);

        if (dockState != NO_VALID_VALUE) {
            final boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;

            final boolean isCar = dockState == Intent.EXTRA_DOCK_STATE_CAR;
            final boolean isDesk = (dockState == Intent.EXTRA_DOCK_STATE_DESK) || (dockState == Intent.EXTRA_DOCK_STATE_LE_DESK)
                    || (dockState == Intent.EXTRA_DOCK_STATE_HE_DESK);

            Ut.d("Dock State changed: " + (isDocked ? (isCar ? "Car dock" : (isDesk ? "Desk" : "unknown")) : "undocked"));
        }

        // powerstate

        final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (status != NO_VALID_VALUE) {
            final boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL);

            final int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            final boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            // final boolean acCharge = chargePlug ==
            // BatteryManager.BATTERY_PLUGGED_AC;

            Ut.d("Powerconnection State changed: " + (isCharging ? (usbCharge ? "USB charge" : "AC charge") : "not isCharging"));
        }

        if (intent.getAction().equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED")) {

            // Check if the SMS matches our SOS message
            final SmsMessage[] messages = getMessagesFromIntent(intent);
            if (messages != null) {
                for (final SmsMessage message : messages) {
                    if (matchesSosMessage(context, message.getDisplayMessageBody())) {
                        Ut.i("Received SOS! Launching Service");

                        final Intent serviceIntent = new Intent(context, SmsLocatorService.class);
                        serviceIntent.putExtra(SmsLocatorService.INTENT_DESTINATION_NUMBER, message.getOriginatingAddress());
                        context.startService(serviceIntent);
                        break;
                    }
                }
            }
        }

    }

    private boolean matchesSosMessage(final Context context, final String message) {

        // SharedPreferences preferences = context.getSharedPreferences(
        // MainSetting.PREFERENCES, Context.MODE_PRIVATE);
        //
        // String sosMessage =
        // preferences.getString(MainSetting.PREFERENCES_SOS,
        // MainSetting.PREFERENCES_SOS_DEFAULT);

        // return sosMessage.equalsIgnoreCase(message);

        return true;

    }

    /*
     * Stolen from http://www.devx.com/wireless/Article/39495/1954 Thanks Chris
     * Haseman. All credit to him since I think he is a martial arts instructor
     * and likely to do bad things to me.
     */
    private SmsMessage[] getMessagesFromIntent(final Intent intent) {
        SmsMessage retMsgs[] = null;
        final Bundle bdl = intent.getExtras();
        try {
            final Object pdus[] = (Object[]) bdl.get("pdus");
            retMsgs = new SmsMessage[pdus.length];
            for (int n = 0; n < pdus.length; n++) {
                final byte[] byteData = (byte[]) pdus[n];
                retMsgs[n] = SmsMessage.createFromPdu(byteData);
            }
        }
        catch (final Exception e) {
            Ut.e("SMS parsing failed");
        }
        return retMsgs;
    }
}
