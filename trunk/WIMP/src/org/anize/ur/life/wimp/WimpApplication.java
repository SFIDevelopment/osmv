package org.anize.ur.life.wimp;

import org.acra.annotation.ReportsCrashes;
import org.anize.ur.life.wimp.util.Util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

// https://docs.google.com/spreadsheet/viewform?formkey=dG52TjNTbktoUzQ3eVlVQ2pBUlAxQmc6MQ
// https://docs.google.com/spreadsheet/viewform?formkey=dHI5QVBEb25rUjhZSm10czNUS05Za2c6MQ
// @ReportsCrashes(formKey = "dHI5QVBEb25rUjhZSm10czNUS05Za2c6MQ")
@ReportsCrashes(formUri = "http://www.bugsense.com/api/acra?api_key=ca5925d8", formKey = "dHI5QVBEb25rUjhZSm10czNUS05Za2c6MQ")
public class WimpApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// ACRA.init(this);

		BugSenseHandler
				.initAndStartSession(getApplicationContext(), "ca5925d8");

		initSingletons();
	}

	protected void initSingletons() {

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		prefs.edit().clear().commit();

		LocationLibrary.initializeLibrary(getBaseContext());
		Util.i("Loclibrary initialized");
	}

}
