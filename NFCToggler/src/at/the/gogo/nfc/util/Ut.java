package at.the.gogo.nfc.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.Log;

public class Ut {

    final static public boolean DEBUGMODE = false;
    final static public String  DEBUGTAG  = "NFC Toggler";

    public static String getAppVersion(final Context ctx) {
        PackageInfo pi;
        String res = "";
        try {
            pi = ctx.getPackageManager().getPackageInfo(
                    "at.the.gogo.nfc.toggler", 0);
            res = pi.versionName;
        } catch (final NameNotFoundException e) {
        }

        return res;
    }

    public static void dd(final String str) {
        Log.d(DEBUGTAG, str);
    }

    public static void e(final String str) {
        if (DEBUGMODE) {
            Log.e(DEBUGTAG, str);
        }
    }

    public static void i(final String str) {
        if (DEBUGMODE) {
            Log.i(DEBUGTAG, str);
        }
    }

    public static void w(final String str) {
        if (DEBUGMODE) {
            Log.w(DEBUGTAG, str);
        }
    }

    public static void d(final String str) {
        if (DEBUGMODE) {
            Log.d(DEBUGTAG, str);
        }
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

    public static void copyTextToClipboard(final Context context,
            final String text) {
        final ClipboardManager clipboard = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);

        clipboard.setText(text);

    }

}
