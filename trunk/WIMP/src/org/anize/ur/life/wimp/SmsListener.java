package org.anize.ur.life.wimp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsListener extends BroadcastReceiver {

	private static final String LOGNAME = SmsListener.class.getCanonicalName();

	@Override
	public void onReceive(final Context context, final Intent intent) {

		if (intent.getAction().equalsIgnoreCase(
				"android.provider.Telephony.SMS_RECEIVED")) {

			// Check if the SMS matches our SOS message
			final SmsMessage[] messages = getMessagesFromIntent(intent);
			if (messages != null) {
				for (final SmsMessage message : messages) {
					if (matchesSosMessage(context,
							message.getDisplayMessageBody())) {
						Log.i(LOGNAME, "Received Request Launching Service");

						final Intent serviceIntent = new Intent(context,
								SmsLocatorService.class);
						serviceIntent.putExtra(
								SmsLocatorService.INTENT_DESTINATION_NUMBER,
								message.getOriginatingAddress());
						context.startService(serviceIntent);
						break;
					}
				}
			}
		}

	}

	private boolean matchesSosMessage(final Context context,
			final String message) {

		final SharedPreferences preferences = context.getSharedPreferences(
				MainSetting.PREFERENCES, Context.MODE_PRIVATE);

		final String sosMessage = preferences.getString(
				MainSetting.PREFERENCES_SOS,
				MainSetting.PREFERENCES_SOS_DEFAULT);

		return sosMessage.equalsIgnoreCase(message);

	}

	/*
	 * Stolen from http://www.devx.com/wireless/Article/39495/1954
	 * 
	 * Thanks Chris Haseman. All credit to him since I think he is a martial
	 * arts instructor and likely to do bad things to me.
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
		} catch (final Exception e) {
			Log.e(LOGNAME, "fail", e);
		}
		return retMsgs;
	}

}
