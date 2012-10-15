/**
 *
 */
package at.the.gogo.windig.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import at.the.gogo.windig.R;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class MainPreferenceActivity extends SherlockPreferenceActivity
		implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.mainpreferences);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences aPref,
			final String aKey) {

		onContentChanged();
	}

}
