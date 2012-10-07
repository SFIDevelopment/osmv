package at.the.gogo.windig.widget;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

public class WindigWidgetModel extends APrefWidgetModel {
    private static String tag                  = "WindigWidgetModel";

    private static String WIDGET_PROVIDER_NAME = "at.the.gogo.WindigWidgetProvider";

    private String        siteix               = "0";
    private static String SITE_IX              = "site";

    private String        speedix              = "0";
    private static String SPEED_IX             = "speed";

    public WindigWidgetModel(final int instanceId) {
        super(instanceId);
    }

    public WindigWidgetModel(final int instanceId, final String inSite,
            final String inSpeed) {
        super(instanceId);
        siteix = inSite;
        speedix = inSpeed;
    }

    @Override
    public void init() {

    }

    public void setSite(final String inSite) {
        siteix = inSite;
    }

    public void setSpeed(final String inSpeed) {
        speedix = inSpeed;
    }

    public String getSite() {
        return siteix;
    }

    public String getSpeed() {
        return speedix;
    }

    // ****************************
    // Implement save contract
    // ****************************
    @Override
    public void setValueForPref(final String key, final String value) {
        if (key.equals(getStoredKeyForFieldName(WindigWidgetModel.SITE_IX))) {
            Log.d(WindigWidgetModel.tag, "Setting site to:" + value);
            siteix = value;
            return;
        }
        if (key.equals(getStoredKeyForFieldName(WindigWidgetModel.SPEED_IX))) {
            Log.d(WindigWidgetModel.tag, "Setting speed to:" + value);
            speedix = value;
            return;
        }
        Log.d(WindigWidgetModel.tag, "Sorry the key does not match:" + key);
    }

    // you need to do this
    @Override
    public String getPrefname() {
        return WindigWidgetModel.WIDGET_PROVIDER_NAME;
    }

    // default exists, returns null
    // return key value pairs you want to be saved
    @Override
    public Map<String, String> getPrefsToSave() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(WindigWidgetModel.SITE_IX, siteix);
        map.put(WindigWidgetModel.SPEED_IX, speedix);
        return map;
    }

    @Override
    public String toString() {
        final StringBuffer sbuf = new StringBuffer();
        sbuf.append("iid:" + iid);
        sbuf.append("site:" + siteix);
        sbuf.append("speed:" + speedix);
        return sbuf.toString();
    }

    public static void clearAllPreferences(final Context ctx) {
        APrefWidgetModel.clearAllPreferences(ctx,
                WindigWidgetModel.WIDGET_PROVIDER_NAME);
    }

    public static WindigWidgetModel retrieveModel(final Context ctx,
            final int widgetId) {
        final WindigWidgetModel m = new WindigWidgetModel(widgetId);
        final boolean found = m.retrievePrefs(ctx);
        return found ? m : null;
    }

}
