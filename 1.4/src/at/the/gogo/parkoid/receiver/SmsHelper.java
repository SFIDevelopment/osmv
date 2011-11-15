package at.the.gogo.parkoid.receiver;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.Sms;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.db.DBConstants;

public class SmsHelper {
    private static final String      SMS_INTENT_SENT      = "SMS_SENT";
    private static final String      SMS_INTENT_DELIVERED = "SMS_DELIVERED";

    private static final String      SMS_RECEIVER         = "receiver";
    private static final String      SMS_MESSAGE          = "message";

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

    public static PendingIntent getSentPendingIntent(final Context context,
            final String receiver, final String message) {

        final Intent intent = new Intent(SmsHelper.SMS_INTENT_SENT);

        intent.putExtra(SmsHelper.SMS_RECEIVER, receiver);
        intent.putExtra(SmsHelper.SMS_MESSAGE, message);

        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public static PendingIntent getDeliveredPendingIntent(final Context context) {

        final Intent intent = new Intent(SmsHelper.SMS_INTENT_DELIVERED);

        final PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                0);

        return pi;
    }

    public static void registerSMSSentReceiver(final Context context) {
        if (SmsHelper.smsSentReceiver == null) {
            SmsHelper.smsSentReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context arg0, final Intent arg1) {
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:

                            final Bundle bundle = arg1.getExtras();

                            if (bundle != null) {
                                final String receiver = bundle
                                        .getString(SmsHelper.SMS_RECEIVER);
                                final String message = bundle
                                        .getString(SmsHelper.SMS_MESSAGE);

                                // persist it here .... leider
                                CoreInfoHolder
                                        .getInstance()
                                        .getDbManager()
                                        .updateSMS(new Sms(receiver, message),
                                                DBConstants.TABLE_SMS);
                            }
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
                SmsHelper.SMS_INTENT_SENT));

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
                new IntentFilter(SmsHelper.SMS_INTENT_SENT));

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
                getSentPendingIntent(context, phoneNumber, message),
                getDeliveredPendingIntent(context));
    }

}
