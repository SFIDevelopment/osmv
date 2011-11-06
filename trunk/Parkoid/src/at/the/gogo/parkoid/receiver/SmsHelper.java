package at.the.gogo.parkoid.receiver;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;
import at.the.gogo.parkoid.R;

public class SmsHelper {
    private static final String      SENT                 = "SMS_SENT";
    private static final String      DELIVERED            = "SMS_DELIVERED";

    private static BroadcastReceiver smsSentReceiver      = null;
    private static BroadcastReceiver smsDeliveredReceiver = null;

    public static String formatParkingSMS(final String carLicence,
            final String duration, final boolean business) {
        String message = duration + " Wien"; // for now as a hack

        if (carLicence != null) {
            message += "*" + carLicence;
        }

        if (business) {
            message = "B" + message;
        }

        return message;
    }

    public static PendingIntent getSentPendingIntent(final Context context) {
        return PendingIntent.getBroadcast(context, 0,
                new Intent(SmsHelper.SENT), 0);
    }

    public static PendingIntent getDeliveredPendingIntent(final Context context) {
        return PendingIntent.getBroadcast(context, 0, new Intent(
                SmsHelper.DELIVERED), 0);
    }

    public static void registerSMSSentReceiver(final Context context) {
        if (SmsHelper.smsSentReceiver == null) {
            SmsHelper.smsSentReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context arg0, final Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, R.string.SMS_sent,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(context, R.string.Generic_failure,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(context, R.string.No_service,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(context, R.string.Null_PDU,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(context, R.string.Radio_off,
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
        }

        context.registerReceiver(SmsHelper.smsSentReceiver, new IntentFilter(
                SmsHelper.SENT));

    }

    public static void registerSMSDeliveredReceiver(final Context context) {
        if (SmsHelper.smsDeliveredReceiver == null) {
            SmsHelper.smsDeliveredReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context arg0, final Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            Toast.makeText(context, R.string.SMS_delivered,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(context, R.string.SMS_not_delivered,
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
        }

        context.registerReceiver(SmsHelper.smsDeliveredReceiver,
                new IntentFilter(SmsHelper.SENT));

    }

    public static void deregisterSMSSentReceiver(final Context context) {
        context.unregisterReceiver(SmsHelper.smsSentReceiver);
    }

    public static void deregisterSMSDeliveredReceiver(final Context context) {
        context.unregisterReceiver(SmsHelper.smsDeliveredReceiver);
    }

    /**
     * receivers should be already registered!
     * 
     * @param context
     * @param phoneNumber
     * @param message
     */
    public static void sendSMS(final Context context, final String phoneNumber,
            final String message) {

        final SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message,
                getSentPendingIntent(context),
                getDeliveredPendingIntent(context));
    }

}
