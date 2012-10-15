package at.the.gogo.windig.widget;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public abstract class APrefWidgetModel implements IWidgetModelSaveContract {
	private static String tag = "AWidgetModel";
	public static int STATUS_ACTIVE = 1;
	public static int STATUS_DELETED = 2;

	public int iid;
	public int status = APrefWidgetModel.STATUS_ACTIVE;

	public APrefWidgetModel(final int instanceId) {
		iid = instanceId;
	}

	public void setStatus(final int inStatus) {
		status = inStatus;
	}

	public int getStatus() {
		return status;
	}

	public void setDeleted() {
		status = APrefWidgetModel.STATUS_DELETED;
	}

	public boolean isDeleted() {
		return (status == APrefWidgetModel.STATUS_DELETED) ? true : false;
	}

	@Override
	public abstract String getPrefname();

	@Override
	public abstract void init();

	@Override
	public Map<String, String> getPrefsToSave() {
		return null;
	}

	public void savePreferences(final Context context) {
		final Map<String, String> keyValuePairs = getPrefsToSave();
		if (keyValuePairs == null) {
			return;
		}
		// going to save some values

		final SharedPreferences.Editor prefs = context.getSharedPreferences(
				getPrefname(), 0).edit();

		for (final String key : keyValuePairs.keySet()) {
			final String value = keyValuePairs.get(key);
			savePref(prefs, key, value);
		}
		// finally commit the values
		prefs.commit();
	}

	private void savePref(final SharedPreferences.Editor prefs,
			final String key, final String value) {
		final String newkey = getStoredKeyForFieldName(key);
		Log.d(APrefWidgetModel.tag, "saving:" + newkey + ":" + value);
		prefs.putString(newkey, value);
	}

	private void removePref(final SharedPreferences.Editor prefs,
			final String key) {
		final String newkey = getStoredKeyForFieldName(key);
		Log.d(APrefWidgetModel.tag, "Removing:" + newkey);
		prefs.remove(newkey);
	}

	protected String getStoredKeyForFieldName(final String fieldName) {
		return fieldName + "_" + iid;
	}

	public static void clearAllPreferences(final Context context,
			final String prefname) {
		Log.d(APrefWidgetModel.tag, "Clearing all preferences for:" + prefname);
		final SharedPreferences prefs = context.getSharedPreferences(prefname,
				0);
		Log.d(APrefWidgetModel.tag, "Number of preferences:"
				+ prefs.getAll().size());
		final SharedPreferences.Editor prefsEdit = prefs.edit();
		prefsEdit.clear();
		// finally commit the values
		prefsEdit.commit();
	}

	public boolean retrievePrefs(final Context ctx) {
		Log.d(APrefWidgetModel.tag, "Rerieving preferences for widget id:"
				+ iid);
		final SharedPreferences prefs = ctx.getSharedPreferences(getPrefname(),
				0);
		final Map<String, ?> keyValuePairs = prefs.getAll();
		Log.d(APrefWidgetModel.tag,
				"Number of keys for all widget ids of this type:"
						+ keyValuePairs.size());
		boolean prefFound = false;
		for (final String key : keyValuePairs.keySet()) {
			if (isItMyPref(key) == true) {
				final String value = (String) keyValuePairs.get(key);
				Log.d(APrefWidgetModel.tag, "setting value for:" + key + ":"
						+ value);
				setValueForPref(key, value);
				prefFound = true;
			}
		}
		return prefFound;
	}

	public void removePrefs(final Context context) {
		Log.d(APrefWidgetModel.tag, "Removing preferences for widget id:" + iid);
		final Map<String, String> keyValuePairs = getPrefsToSave();
		if (keyValuePairs == null) {
			return;
		}
		// going to save some values

		final SharedPreferences.Editor prefs = context.getSharedPreferences(
				getPrefname(), 0).edit();

		for (final String key : keyValuePairs.keySet()) {
			removePref(prefs, key);
		}
		// finally commit the values
		prefs.commit();
	}

	private boolean isItMyPref(final String keyname) {
		Log.d(APrefWidgetModel.tag, "Examinging keyname:" + keyname);
		if (keyname.indexOf("_" + iid) > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void setValueForPref(final String key, final String value) {
		return;
	}
}
