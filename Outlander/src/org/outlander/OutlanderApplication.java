package org.outlander;

import android.app.Application;

import com.bugsense.trace.BugSenseHandler;

// https://docs.google.com/spreadsheet/viewform?formkey=dHdWRkFQUlVmX216OHo5M1lyOG03S1E6MQ
//
//
// @ReportsCrashes(formKey = "dHI5QVBEb25rUjhZSm10czNUS05Za2c6MQ")
// @ReportsCrashes(formUri =
// "http://www.bugsense.com/api/acra?api_key=ca5925d8", formKey =
// "dHdWRkFQUlVmX216OHo5M1lyOG03S1E6MQ")
public class OutlanderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ACRA.init(this);

        BugSenseHandler
                .initAndStartSession(getApplicationContext(), "1fe4b404");

        // initSingletons();
    }

    // protected void initSingletons() {
    //
    // final SharedPreferences prefs = PreferenceManager
    // .getDefaultSharedPreferences(getApplicationContext());
    // prefs.edit().clear().commit();
    //
    // LocationLibrary.initializeLibrary(getBaseContext());
    // Util.i("Loclibrary initialized");
    // }

}
