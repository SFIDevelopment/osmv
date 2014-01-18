package at.the.gogo.gpxviewer.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class Util {

	static boolean debugModeEnabled = false;
	
	public static void setDebugMode(boolean newMode)
	{
		debugModeEnabled = newMode;
	}
	
	public static boolean isDebugMode()
	{
		return debugModeEnabled;
	}
	
	
	 public static  boolean servicesConnected(Context context) {
	        // Check that Google Play services is available
	        int resultCode =
	                GooglePlayServicesUtil.
	                        isGooglePlayServicesAvailable(context);
	        // If Google Play services is available
	        
	        return (ConnectionResult.SUCCESS == resultCode);
	        
	        
	        
//	        if (ConnectionResult.SUCCESS == resultCode) {
//	            // In debug mode, log the status
//	            Log.d("Activity Recognition",
//	                    "Google Play services is available.");
//	            // Continue
//	            return true;
//	        // Google Play services was not available for some reason
//	        } else {
//	            // Get the error dialog from Google Play services
//	            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
//	                    resultCode,
//	                    this,
//	                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
//
//	            // If Google Play services can provide an error dialog
//	            if (errorDialog != null) {
//	                // Create a new DialogFragment for the error dialog
//	                ErrorDialogFragment errorFragment =
//	                        new ErrorDialogFragment();
//	                // Set the dialog in the DialogFragment
//	                errorFragment.setDialog(errorDialog);
//	                // Show the error dialog in the DialogFragment
//	                errorFragment.show(
//	                        getSupportFragmentManager(),
//	                        "Activity Recognition");
//	            }
//	            return false;
//	        }
	 }
	 
	 
	 
    public static void dd(final String str) {
        Log.d(OpenStreetMapConstants.DEBUGTAG, str);
    }

    public static void e(final String str) {
        if (debugModeEnabled) {
            Log.e(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }

    public static void i(final String str) {
        if (debugModeEnabled) {
            Log.i(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }

    public static void w(final String str) {
        if (debugModeEnabled) {
            Log.w(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }

    public static void d(final String str) {
        if (debugModeEnabled) {
            Log.d(OpenStreetMapConstants.DEBUGTAG, str);
        }
    }
	 
}
