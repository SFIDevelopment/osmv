package org.anize.ur.life.wimp.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.anize.ur.life.wimp.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

public class Util {

	public static final String DEBUGTAG = "WIMP";
	public static boolean DEBUGMODE = true;

	public static boolean USEBUGSENSE = true;

	public static ProgressDialog ShowWaitDialog(final Context mCtx,
			final int ResourceId) {
		final ProgressDialog dialog = new ProgressDialog(mCtx);
		dialog.setMessage(mCtx
				.getString(ResourceId == 0 ? R.string.message_wait : ResourceId));
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);

		dialog.show();

		return dialog;
	}

	public static String getAppVersion(final Context ctx) {
		PackageInfo pi;
		String res = "";
		try {
//			pi = ctx.getPackageManager().getPackageInfo("org.anize.ur.life", 0);
			pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			res = pi.versionName;
		} catch (final NameNotFoundException e) {
		}

		return res;
	}

	public static void dd(final String str) {
		Log.d(Util.DEBUGTAG, str);
	}

	public static void e(final String str) {

		Log.e(Util.DEBUGTAG, str);

	}

	public static void i(final String str) {

		Log.i(Util.DEBUGTAG, str);

	}

	public static void w(final String str) {

		Log.w(Util.DEBUGTAG, str);

	}

	public static void d(final String str) {
		if (Util.DEBUGMODE) {
			Log.d(Util.DEBUGTAG, str);
		}
	}

	public static String FileName2ID(final String name) {
		return name.replace(".", "_").replace(" ", "_").replace("-", "_")
				.trim();
	}

	public static Intent sendMail(final String subject, final String text,
			final String[] receivers) {

		final Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		sendIntent.putExtra(Intent.EXTRA_EMAIL, receivers);
		sendIntent.setType("message/rfc822");
		return Intent.createChooser(sendIntent, "Error report to the author");
	}

	// public static Intent sendSms(final String number, final String body) {
	// final Uri smsUri = Uri.parse("tel:" + number);
	// final Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
	// intent.putExtra("sms_body", body);
	// intent.setType("vnd.android-dir/mms-sms");
	// return Intent.createChooser(intent, "Send SMS");
	// }

	// public static void sendViennaParkingSMS(final Context context,
	// String phoneNumber, String carLicence, String duration) {
	//
	// String message = formatParkingSMS(phoneNumber,carLicence,duration);
	//
	// sendSMS(context, context.getString(R.string.SMS_TEL_WIEN), message);
	// }
	//
	// public static void sendSMS(final Context context, String phoneNumber,
	// String message) {
	// String SENT = "SMS_SENT";
	// String DELIVERED = "SMS_DELIVERED";
	//
	// PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
	// new Intent(SENT), 0);
	//
	// PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
	// new Intent(DELIVERED), 0);
	//
	// // ---when the SMS has been sent---
	// context.registerReceiver(new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context arg0, Intent arg1) {
	// switch (getResultCode()) {
	// case Activity.RESULT_OK:
	// Toast.makeText(context, R.string.SMS_sent,
	// Toast.LENGTH_SHORT).show();
	// break;
	// case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	// Toast.makeText(context, R.string.Generic_failure,
	// Toast.LENGTH_SHORT).show();
	// break;
	// case SmsManager.RESULT_ERROR_NO_SERVICE:
	// Toast.makeText(context, R.string.No_service,
	// Toast.LENGTH_SHORT).show();
	// break;
	// case SmsManager.RESULT_ERROR_NULL_PDU:
	// Toast.makeText(context, R.string.Null_PDU,
	// Toast.LENGTH_SHORT).show();
	// break;
	// case SmsManager.RESULT_ERROR_RADIO_OFF:
	// Toast.makeText(context, R.string.Radio_off,
	// Toast.LENGTH_SHORT).show();
	// break;
	// }
	// }
	// }, new IntentFilter(SENT));
	//
	// // ---when the SMS has been delivered---
	// context.registerReceiver(new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context arg0, Intent arg1) {
	// switch (getResultCode()) {
	// case Activity.RESULT_OK:
	// Toast.makeText(context, R.string.SMS_delivered,
	// Toast.LENGTH_SHORT).show();
	// break;
	// case Activity.RESULT_CANCELED:
	// Toast.makeText(context, R.string.SMS_not_delivered,
	// Toast.LENGTH_SHORT).show();
	// break;
	// }
	// }
	// }, new IntentFilter(DELIVERED));
	//
	// SmsManager sms = SmsManager.getDefault();
	// sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	// }

	public static Intent sendText(final String content) {
		final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/html");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				Html.fromHtml(content));
		return Intent.createChooser(sharingIntent, "Share using");
	}

	public static Intent sendErrorReportMail(final String subject,
			final String text) {
		final String[] email = { "furykid@gmail.com" };
		return sendMail(subject, text, email);
	}

	// JF

	public static boolean isExternDeviceAvailable() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		final String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return (mExternalStorageAvailable && mExternalStorageWriteable);
	}

	public static boolean isInternetConnectionAvailable(final Context context) {
		final ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// test for connection
		boolean connected = false;
		if ((cm.getActiveNetworkInfo() != null)
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			connected = true;
		}
		return connected;
	}

	//

	public static boolean isMultiPane(final Context context) {
		// return getResources().getConfiguration().orientation ==
		// Configuration.ORIENTATION_LANDSCAPE;
		boolean isHires = false;
		final Configuration configuration = context.getResources()
				.getConfiguration();

		// isHires = getResources().getConfiguration().screenWidthDp >= 600; //
		// for future !

		isHires = ((configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) && ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE));

		return isHires;
	}

	public static Address getRawAddress(final Context context,
			final double latitude, final double longitude) {

		Address address = null;

		if (isInternetConnectionAvailable(context)) {
			final Geocoder gc = new Geocoder(context, Locale.getDefault());
			List<Address> addresses = null;
			try {
				addresses = gc.getFromLocation(latitude, longitude, 1);
			} catch (final IOException x) {
				Util.dd("emulator? " + x.toString());
				addresses = ReverseGeocode.getFromLocation(latitude, longitude,
						1);
			}

			if ((addresses != null) && (addresses.size() > 0)) {
				address = addresses.get(0);
			}
		}

		return address;

	}

	public static File getDBDir(final Context mCtx, final String aFolderName) {
		return getDir(mCtx, "pref_dir_main", Environment
				.getExternalStorageDirectory().getPath()
				+ "/"
				+ mCtx.getResources().getString(R.string.app_name) + "/",
				aFolderName);
	}

	private static File getDir(final Context mCtx, final String aPref,
			final String aDefaultDirName, final String aFolderName) {

		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(mCtx);

		final String dirName = pref.getString(aPref, aDefaultDirName) + "/"
				+ aFolderName + "/";

		final File dir = new File(dirName.replace("//", "/").replace("//", "/"));
		if (!dir.exists()) {
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				dir.mkdirs();
			}
		}

		return dir;
	}

}
