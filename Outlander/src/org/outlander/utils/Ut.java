package org.outlander.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.geonames.GeoCodeResult;
import org.geonames.ReverseGeocode;
import org.geonames.WebService;
import org.outlander.R;
import org.outlander.utils.geo.GeoMathUtil;
import org.xmlrpc.android.XMLRPCClient;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

public class Ut implements OpenStreetMapConstants, OpenStreetMapViewConstants {

    public static ProgressDialog ShowWaitDialog(final Context mCtx, final int ResourceId) {
        final ProgressDialog dialog = new ProgressDialog(mCtx);
        dialog.setMessage(mCtx.getString(ResourceId == 0 ? R.string.message_wait : ResourceId));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        dialog.show();

        return dialog;
    }

    public static String getAppVersion(final Context ctx) {
        PackageInfo pi;
        String res = "";
        try {
            pi = ctx.getPackageManager().getPackageInfo("org.outlander", 0);
            res = pi.versionName;
        }
        catch (final NameNotFoundException e) {
        }

        return res;
    }

    public static void dd(final String str) {
        Log.d(OpenStreetMapConstants.DEBUGTAG, str);
    }

    public static void e(final String str) {
        if (OpenStreetMapViewConstants.DEBUGMODE) {
            Log.e(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }

    public static void i(final String str) {
        if (OpenStreetMapViewConstants.DEBUGMODE) {
            Log.i(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }

    public static void w(final String str) {
        if (OpenStreetMapViewConstants.DEBUGMODE) {
            Log.w(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }

    public static void d(final String str) {
        if (OpenStreetMapViewConstants.DEBUGMODE) {
            Log.d(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }

    public static String FileName2ID(final String name) {
        return name.replace(".", "_").replace(" ", "_").replace("-", "_").trim();
    }

    private static File getDir(final Context mCtx, final String aPref, final String aDefaultDirName, final String aFolderName) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        final String dirName = pref.getString(aPref, aDefaultDirName) + "/" + aFolderName + "/";

        final File dir = new File(dirName.replace("//", "/").replace("//", "/"));
        if (!dir.exists()) {
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                dir.mkdirs();
            }
        }

        return dir;
    }

    public static File getTschekkoMapsMainDir(final Context mCtx, final String aFolderName) {
        return getDir(mCtx, "pref_dir_main", "/sdcard/TschekkoMaps/", aFolderName);
    }

    public static File getTschekkoMapsMapsDir(final Context mCtx) {
        return getDir(mCtx, "pref_dir_maps", "/sdcard/TschekkoMaps/maps/", "");
    }

    public static File getTschekkoMapsImportDir(final Context mCtx) {
        return getDir(mCtx, "pref_dir_import", "/sdcard/TschekkoMaps/import/", "");
    }

    public static File getTschekkoMapsExportDir(final Context mCtx) {
        return getDir(mCtx, "pref_dir_export", "/sdcard/TschekkoMaps/export/", "");
    }

    public static String readString(final InputStream in, final int size) throws IOException {
        final byte b[] = new byte[size];

        final int lenght = in.read(b);
        if (b[0] == 0) {
            return "";
        }
        else if (lenght > 0) {
            return new String(b, 0, lenght);
        }
        else {
            return "";
        }
    }

    /*
     * 
     */

    public static int readInt(final InputStream in) throws IOException {
        int res = 0;
        final byte b[] = new byte[4];

        if (in.read(b) > 0) {
            res = (((b[0] & 0xFF)) << 24) + +((b[1] & 0xFF) << 16) + +((b[2] & 0xFF) << 8) + +(b[3] & 0xFF);
        }

        return res;
    }

    public static Intent sendMail(final String subject, final String text, final String[] receivers) {

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
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(content));
        return Intent.createChooser(sharingIntent, "Share using");
    }

    public static Intent shareLocation(final double longitude, final double latitude, final String description, final Context context) {
        final String dateTime = DateFormat.getDateInstance(DateFormat.FULL).format(new Date());
        String content = description + ";\n" + dateTime + "\n";

        final String googleMapUrl = "http://maps.google.com/maps?q=loc:" + longitude + "," + latitude;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final int coordFormt = Integer.parseInt(sharedPreferences.getString("pref_coords", "1"));
        final String location = GeoMathUtil.formatCoordinate(latitude, longitude, coordFormt);

        content += "Location:" + location + " " + googleMapUrl;

        return sendText(content);

    }

    public static Intent sendErrorReportMail(final String subject, final String text) {
        final String[] email = { "furykid@gmail.com" };
        return sendMail(subject, text, email);
    }

    // http://maps.google.com/maps?f=d&source=s_d&saddr=Traviatagasse+21,+1230+Wien,+Austria&daddr=Am+Hof,+Wien,+%C3%96sterreich&output=js&hl=en&abauth=4eff5991_0TaxG6CSO76Vv0ivJXYmedS-CE&authuser=0&aq=1&oq=Am+hof&vps=6&vrp=5&ei=uFn_TqPHI8_y_AbqkYW5Bg&jsv=386c&sll=48.17616,16.358814&sspn=0.10268,0.192089&vpsrc=0&dirflg=w&mra=ltm
    // http://maps.google.com/maps?f=d&source=s_d&saddr=Traviatagasse+21,+1230+Wien,+Austria&daddr=Am+Hof,+Wien,+%C3%96sterreich&output=js&hl=en&abauth=4eff5991_0TaxG6CSO76Vv0ivJXYmedS-CE&authuser=0&aq=1&oq=Am+hof&vps=7&vrp=6&ei=3Fn_TqStD4XH_QbT5OD3Cw&jsv=386c&sll=48.176195,16.35885&sspn=0.10268,0.192089&vpsrc=0&mra=ltm

    public static Intent showRouteOnGoogleMaps(final double latitudeS, final double longitudeS, final double latitudeD, final double longitudeD,
            final boolean pedestrian) {
        final Uri uri = Uri.parse("http://maps.google.com/maps?&saddr=" + latitudeS + "," + longitudeS + "&daddr=" + latitudeD + "," + longitudeD
                + (pedestrian ? "&dirflg=w" : ""));
        final Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        return intent;
    }

    public static Intent showLocationExternal(final double latitude, final double longitude) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude + "," + longitude + "?z=18"));

    }

    // JF

    public static boolean isExternDeviceAvailable() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        final String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        }
        else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        return (mExternalStorageAvailable && mExternalStorageWriteable);
    }

    public static boolean isInternetConnectionAvailable(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        boolean connected = false;
        if ((cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            connected = true;
        }
        return connected;
    }

    //
    public static String buildMapsUrl(final Context context, final float lat, final float lon, final int zoom) {
        final String url = String.format(context.getString(R.string.mapsurl), lat, lon, zoom);

        return url;

    }

    public static Address getRawAddressFromYahoo(final Context context, final double latitude, final double longitude) {

        Address address = null;

        if (isInternetConnectionAvailable(context)) {

            final GeoCodeResult gcr = WebService.reverseGeoCode(latitude, longitude);

            if (gcr != null) {
                address = new Address(Locale.getDefault());

                address.setAddressLine(0, gcr.getLine1());
                address.setAddressLine(1, gcr.getLine2());
                address.setAddressLine(2, gcr.getLine3());
                address.setAddressLine(3, gcr.getLine4());
                address.setLocality(gcr.getCity());
                address.setCountryName(gcr.getCountry());
            }
        }

        return address;

    }

    public static Address getRawAddressFromGoogle(final Context context, final double latitude, final double longitude) {

        Address address = null;

        if (isInternetConnectionAvailable(context)) {
            final Geocoder gc = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gc.getFromLocation(latitude, longitude, 1);
            }
            catch (final IOException x) {
                Ut.dd("emulator? " + x.toString());
                addresses = ReverseGeocode.getFromLocation(latitude, longitude, 1);
            }

            if ((addresses != null) && (addresses.size() > 0)) {
                address = addresses.get(0);
            }
        }

        return address;

    }

    public static String getAddress(final Context context, final double latitude, final double longitude) {
        String addressTxt = "";

        final Address address = getRawAddressFromYahoo(context, latitude, longitude);

        if (address != null) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                sb.append(address.getAddressLine(i)).append("\n");
            }

            if (address.getLocality() != null) {
                sb.append(address.getLocality()).append("\n");
            }
            if (address.getPostalCode() != null) {
                sb.append(address.getPostalCode()).append("\n");
            }
            if (address.getCountryName() != null) {
                sb.append(address.getCountryName());
            }

            addressTxt = sb.toString();
        }
        if (addressTxt.length() < 1) {
            addressTxt = context.getString(R.string.no_address_found);
        }

        return addressTxt;
    }

    public static boolean isMultiPane(final Context context) {
        // return getResources().getConfiguration().orientation ==
        // Configuration.ORIENTATION_LANDSCAPE;
        boolean isHires = false;
        final Configuration configuration = context.getResources().getConfiguration();

        // isHires = getResources().getConfiguration().screenWidthDp >= 600; //
        // for future !

        isHires = ((configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) && ((configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE));

        return isHires;
    }

    private static final SimpleDateFormat BASE_XML_DATE_FORMAT    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static {
        Ut.BASE_XML_DATE_FORMAT.setTimeZone(new SimpleTimeZone(0, "UTC"));
    }
    private static final Pattern          XML_DATE_EXTRAS_PATTERN = Pattern.compile("^(\\.\\d+)?(?:Z|([+-])(\\d{2}):(\\d{2}))?$");

    /**
     * Parses an XML dateTime element as defined by the XML standard.
     * 
     * @see <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">dateTime</a>
     */
    public static long parseXmlDateTime(final String xmlTime) {
        // Parse the base date (fixed format)
        final ParsePosition position = new ParsePosition(0);
        final Date date = Ut.BASE_XML_DATE_FORMAT.parse(xmlTime, position);
        if (date == null) {
            throw new IllegalArgumentException("Invalid XML dateTime value: '" + xmlTime + "' (at position " + position.getErrorIndex() + ")");
        }

        // Parse the extras
        final Matcher matcher = Ut.XML_DATE_EXTRAS_PATTERN.matcher(xmlTime.substring(position.getIndex()));
        if (!matcher.matches()) {
            // This will match even an empty string as all groups are optional,
            // so a non-match means some other garbage was there
            throw new IllegalArgumentException("Invalid XML dateTime value: " + xmlTime);
        }

        long time = date.getTime();

        // Account for fractional seconds
        final String fractional = matcher.group(1);
        if (fractional != null) {
            // Regex ensures fractional part is in (0,1(
            final float fractionalSeconds = Float.parseFloat(fractional);
            final long fractionalMillis = (long) (fractionalSeconds * 1000.0f);
            time += fractionalMillis;
        }

        // Account for timezones
        final String sign = matcher.group(2);
        final String offsetHoursStr = matcher.group(3);
        final String offsetMinsStr = matcher.group(4);
        if ((sign != null) && (offsetHoursStr != null) && (offsetMinsStr != null)) {
            // Regex ensures sign is + or -
            final boolean plusSign = sign.equals("+");
            final int offsetHours = Integer.parseInt(offsetHoursStr);
            final int offsetMins = Integer.parseInt(offsetMinsStr);

            // Regex ensures values are >= 0
            if ((offsetHours > 14) || (offsetMins > 59)) {
                throw new IllegalArgumentException("Bad timezone in " + xmlTime);
            }

            final long totalOffsetMillis = (offsetMins + (offsetHours * 60L)) * 60000L;

            // Make time go back to UTC
            if (plusSign) {
                time -= totalOffsetMillis;
            }
            else {
                time += totalOffsetMillis;
            }
        }

        return time;
    }

    /**
     * Formats a number of milliseconds as a string.
     * 
     * @param time
     *            - A period of time in milliseconds.
     * @return A string of the format M:SS, MM:SS or HH:MM:SS
     */
    public static String formatTime(final long time) {
        return formatTimeInternal(time, false);
    }

    /**
     * Formats a number of milliseconds as a string.
     * 
     * @param time
     *            - A period of time in milliseconds
     * @param alwaysShowHours
     *            - Whether to display 00 hours if time is less than 1 hour
     * @return A string of the format HH:MM:SS
     */
    private static String formatTimeInternal(final long time, final boolean alwaysShowHours) {
        final int[] parts = getTimeParts(time);
        final StringBuilder builder = new StringBuilder();
        if ((parts[2] > 0) || alwaysShowHours) {
            builder.append(parts[2]);
            builder.append(':');
            if (parts[1] <= 9) {
                builder.append("0");
            }
        }

        builder.append(parts[1]);
        builder.append(':');
        if (parts[0] <= 9) {
            builder.append("0");
        }
        builder.append(parts[0]);

        return builder.toString();
    }

    /**
     * Gets the time as an array of parts.
     */
    public static int[] getTimeParts(final long time) {
        if (time < 0) {
            final int[] parts = getTimeParts(time * -1);
            parts[0] *= -1;
            parts[1] *= -1;
            parts[2] *= -1;
            return parts;
        }
        final int[] parts = new int[3];

        final long seconds = time / 1000;
        parts[0] = (int) (seconds % 60);
        final int tmp = (int) (seconds / 60);
        parts[1] = tmp % 60;
        parts[2] = tmp / 60;

        return parts;
    }

    public static void copyTextToClipboard(final Context context, final String text) {
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        clipboard.setText(text);

    }

    public static boolean checkPointInVKPZ(final double latitude, final double longitude) {
        boolean isInArea = false;
        try {

            final XMLRPCClient client = new XMLRPCClient("http://spaceinfo.v10.at:8080/spaceinfo/xmlrpc");

            isInArea = (Boolean) client.callEx("Spaceinfo.getInZone", new Object[] { new Double(latitude), new Double(longitude) });

        }
        catch (final Exception x) {
            Ut.dd("KLUMP" + x.toString());
        }
        return isInArea;
    }

}
