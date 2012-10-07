package at.the.gogo.parkoid.receiver;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.db.DBConstants;

public class SmsReceiver extends BroadcastReceiver {
    // All available column names in SMS table
    // [_id, thread_id, address,
    // person, date, protocol, read,
    // status, type, reply_path_present,
    // subject, body, service_center,
    // locked, error_code, seen]

    public static final String SMS_EXTRA_NAME      = "pdus";
    public static final String SMS_URI             = "content://sms";

    public static final String ADDRESS             = "address";
    public static final String PERSON              = "person";
    public static final String DATE                = "date";
    public static final String READ                = "read";
    public static final String STATUS              = "status";
    public static final String TYPE                = "type";
    public static final String BODY                = "body";
    public static final String SEEN                = "seen";

    public static final int    MESSAGE_TYPE_INBOX  = 1;
    public static final int    MESSAGE_TYPE_SENT   = 2;

    public static final int    MESSAGE_IS_NOT_READ = 0;
    public static final int    MESSAGE_IS_READ     = 1;

    public static final int    MESSAGE_IS_NOT_SEEN = 0;
    public static final int    MESSAGE_IS_SEEN     = 1;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // Get SMS map from Intent
        final Bundle extras = intent.getExtras();

        String messages = "";

        if (extras != null) {
            // Get received SMS array
            final Object[] smsExtra = (Object[]) extras
                    .get(SmsReceiver.SMS_EXTRA_NAME);

            // context.getContentResolver();

            for (int i = 0; i < smsExtra.length; ++i) {
                final SmsMessage sms = SmsMessage
                        .createFromPdu((byte[]) smsExtra[i]);

                final String body = sms.getMessageBody().toString();
                final String address = sms.getOriginatingAddress();

                messages += "SMS from " + address + " :\n";
                messages += body + "\n";

                putSmsToDatabase(sms);
            }

            // Display SMS message
            Toast.makeText(context, messages, Toast.LENGTH_SHORT).show();
        }

        // WARNING!!!
        // If you uncomment next line then received SMS will not be put to
        // incoming.
        // Be careful!
        // this.abortBroadcast();
    }

    private void putSmsToDatabase( // final ContentResolver contentResolver,
            final SmsMessage smsMsg) {

        CoreInfoHolder
                .getInstance()
                .getDbManager()
                .addSMS(smsMsg.getOriginatingAddress(),
                        smsMsg.getMessageBody(),
                        new Date(smsMsg.getTimestampMillis()),
                        DBConstants.TABLE_SMSR);

    }
}
