package at.the.gogo.windig.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import at.the.gogo.windig.R;

public class PrefsFragment extends PreferenceFragment {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.mainpreferences);
	}
}