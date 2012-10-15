package at.the.gogo.windig.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import at.the.gogo.windig.R;

public class Util {

	public static final String DEBUGTAG = "Windig";
	public static boolean DEBUGMODE = false;

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
			pi = ctx.getPackageManager()
					.getPackageInfo("at.the.gogo.windig", 0);
			res = pi.versionName;
		} catch (final NameNotFoundException e) {
		}

		return res;
	}

	public static void dd(final String str) {
		Log.d(Util.DEBUGTAG, str);
	}

	public static void e(final String str) {
		if (Util.DEBUGMODE) {
			Log.e(Util.DEBUGTAG, str);
		}
	}

	public static void i(final String str) {
		if (Util.DEBUGMODE) {
			Log.i(Util.DEBUGTAG, str);
		}
	}

	public static void w(final String str) {
		if (Util.DEBUGMODE) {
			Log.w(Util.DEBUGTAG, str);
		}
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

	public static Intent sendSms(final String number, final String body) {
		final Uri smsUri = Uri.parse("tel:" + number);
		final Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
		intent.putExtra("sms_body", body);
		intent.setType("vnd.android-dir/mms-sms");
		return Intent.createChooser(intent, "Send SMS");
	}

	public static Intent sendText(final String content) {
		final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/html");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				Html.fromHtml(content));
		return Intent.createChooser(sharingIntent, "Share using");
	}

	public static Intent sendErrorReportMail(final String subject,
			final String text) {
		final String[] email = { "furykid@gmail.com" };// {"robertk506@gmail.com"};
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

}
