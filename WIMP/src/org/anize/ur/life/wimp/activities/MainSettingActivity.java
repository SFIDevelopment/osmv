package org.anize.ur.life.wimp.activities;

import org.anize.ur.life.wimp.R;
import org.anize.ur.life.wimp.R.xml;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainSettingActivity extends PreferenceActivity {

	public static final String PREFERENCES = "SmsLocatorPreferences";

	public static final String PREFERENCES_SOS = "SmsLocatorPreferenceSos";
	public static final String PREFERENCES_SOS_DEFAULT = "where";

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(PREFERENCES);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

	}

}