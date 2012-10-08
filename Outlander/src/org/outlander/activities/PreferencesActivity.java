/**
 *
 */
package org.outlander.activities;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.outlander.R;
import org.outlander.constants.PrefConstants;
import org.outlander.io.XML.PredefMapsParser;
import org.outlander.utils.Ut;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener, PrefConstants {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        final PreferenceGroup prefMapsgroup = (PreferenceGroup) findPreference("pref_predefmaps_mapsgroup");

        final SAXParserFactory fac = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = fac.newSAXParser();
            if (parser != null) {
                final InputStream in = getResources().openRawResource(R.raw.predefmaps);
                parser.parse(in, new PredefMapsParser(prefMapsgroup, this));
            }
        }
        catch (final Exception e) {
            e.printStackTrace();
        }

        final File folder = Ut.getTschekkoMapsMapsDir(this);
        LoadUseTschekkoMaps(folder);
    }

    private void LoadUseTschekkoMaps(final File folder) {
        // Cash file preferences
        final PreferenceGroup prefUseTschekkoMapsgroup = (PreferenceGroup) findPreference("pref_usermaps_mapsgroup");

        if (prefUseTschekkoMapsgroup != null) {
            prefUseTschekkoMapsgroup.removeAll();

            final File[] files = folder.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (file.getName().toLowerCase().endsWith(getString(R.string.mnm)) || file.getName().toLowerCase().endsWith(getString(R.string.tar))
                            || file.getName().toLowerCase().endsWith(getString(R.string.sqlitedb))) {
                        final String name = Ut.FileName2ID(file.getName());

                        final PreferenceScreen prefscr = getPreferenceManager().createPreferenceScreen(this);
                        prefscr.setKey(PrefConstants.PREF_USERMAPS_ + name);
                        {
                            final CheckBoxPreference pref = new CheckBoxPreference(this);
                            pref.setKey(PrefConstants.PREF_USERMAPS_ + name + "_enabled");
                            pref.setTitle(getString(R.string.pref_usermap_enabled));
                            pref.setSummary(getString(R.string.pref_usermap_enabled_summary));
                            pref.setDefaultValue(false);
                            prefscr.addPreference(pref);
                        }
                        {
                            final EditTextPreference pref = new EditTextPreference(this);
                            pref.setKey(PrefConstants.PREF_USERMAPS_ + name + "_name");
                            pref.setTitle(getString(R.string.pref_usermap_name));
                            pref.setSummary(file.getName());
                            pref.setDefaultValue(file.getName());
                            prefscr.addPreference(pref);
                        }
                        {
                            final EditTextPreference pref = new EditTextPreference(this);
                            pref.setKey(PrefConstants.PREF_USERMAPS_ + name + "_baseurl");
                            pref.setTitle(getString(R.string.pref_usermap_baseurl));
                            pref.setSummary(file.getAbsolutePath());
                            pref.setDefaultValue(file.getAbsolutePath());
                            pref.setEnabled(false);
                            prefscr.addPreference(pref);
                        }
                        {
                            final ListPreference pref = new ListPreference(this);
                            pref.setKey(PrefConstants.PREF_USERMAPS_ + name + "_projection");
                            pref.setTitle(getString(R.string.pref_usermap_projection));
                            pref.setEntries(R.array.projection_title);
                            pref.setEntryValues(R.array.projection_value);
                            pref.setDefaultValue("1");
                            prefscr.addPreference(pref);
                            pref.setSummary(pref.getEntry());
                        }

                        prefscr.setTitle(prefscr.getSharedPreferences().getString(PrefConstants.PREF_USERMAPS_ + name + "_name", file.getName()));
                        if (prefscr.getSharedPreferences().getBoolean(PrefConstants.PREF_USERMAPS_ + name + "_enabled", false)) {
                            prefscr.setSummary("Enabled  " + file.getAbsolutePath());
                        }
                        else {
                            prefscr.setSummary("Disabled  " + file.getAbsolutePath());
                        }
                        prefUseTschekkoMapsgroup.addPreference(prefscr);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences aPref, final String aKey) {

        if (aKey.equalsIgnoreCase("pref_dir_maps")) {
            findPreference("pref_main_useTschekkoMaps").setSummary(
                    "Maps from " + aPref.getString("pref_dir_maps", Environment.getExternalStorageDirectory().getPath() + "/TschekkoMaps/maps/"));
            findPreference(aKey).setSummary(aPref.getString("pref_dir_maps", Environment.getExternalStorageDirectory().getPath() + "/TschekkoMaps/maps/"));

            final File dir = new File(aPref.getString("pref_dir_maps", Environment.getExternalStorageDirectory().getPath() + "/TschekkoMaps/maps/").concat("/")
                    .replace("//", "/"));
            if (!dir.exists()) {
                if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                    dir.mkdirs();
                }
            }
            if (dir.exists()) {
                LoadUseTschekkoMaps(dir);
            }
        }
        else if (aKey.substring(0, 9).equalsIgnoreCase("pref_dir_")) {
            findPreference("pref_dir_main")
                    .setSummary(aPref.getString("pref_dir_main", Environment.getExternalStorageDirectory().getPath() + "/TschekkoMaps/"));
            findPreference("pref_dir_import").setSummary(
                    aPref.getString("pref_dir_import", Environment.getExternalStorageDirectory().getPath() + "/TschekkoMaps/import/"));
            findPreference("pref_dir_export").setSummary(
                    aPref.getString("pref_dir_export", Environment.getExternalStorageDirectory().getPath() + "/TschekkoMaps/export/"));
        }
        else if (aKey.length() > 14) {
            if (aKey.substring(0, 14).equalsIgnoreCase(PrefConstants.PREF_USERMAPS_)) {
                if (aKey.endsWith("name") && (findPreference(aKey) != null)) {
                    findPreference(aKey).setSummary(aPref.getString(aKey, ""));
                    findPreference(aKey.replace("_name", "")).setTitle(aPref.getString(aKey, ""));
                }
                else if (aKey.endsWith("enabled") && (findPreference(aKey.replace("_enabled", "")) != null)) {
                    if (aPref.getBoolean(aKey, false)) {
                        findPreference(aKey.replace("_enabled", "")).setSummary("Enabled  " + aPref.getString(aKey.replace("_enabled", "_baseurl"), ""));
                    }
                    else {
                        findPreference(aKey.replace("_enabled", "")).setSummary("Disabled  " + aPref.getString(aKey.replace("_enabled", "_baseurl"), ""));
                    }
                }
                else if (aKey.endsWith("projection") && (findPreference(aKey) != null)) {
                    final ListPreference pref = (ListPreference) findPreference(aKey);
                    findPreference(aKey).setSummary(pref.getEntry());
                }
            }
        }
        onContentChanged();
    }

}
