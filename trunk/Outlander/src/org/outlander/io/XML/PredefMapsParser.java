package org.outlander.io.XML;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.views.util.OpenStreetMapRendererInfo;
import org.outlander.constants.PrefConstants;
import org.outlander.model.MapMenuItemInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceGroup;

public class PredefMapsParser extends DefaultHandler {

    private final OpenStreetMapRendererInfo mRendererInfo;
    private final String                    mMapId;

    private static final String             MAP                  = "map";
    private static final String             LAYER                = "layer";
    private static final String             CACHE                = "cache";
    private static final String             TRUE                 = "true";
    private static final String             ID                   = "id";
    private static final String             NAME                 = "name";
    private static final String             DESCR                = "descr";
    private static final String             BASEURL              = "baseurl";
    private static final String             IMAGE_FILENAMEENDING = "IMAGE_FILENAMEENDING";
    private static final String             ZOOM_MINLEVEL        = "ZOOM_MINLEVEL";
    private static final String             ZOOM_MAXLEVEL        = "ZOOM_MAXLEVEL";
    private static final String             MAPTILE_SIZEPX       = "MAPTILE_SIZEPX";
    private static final String             URL_BUILDER_TYPE     = "URL_BUILDER_TYPE";
    private static final String             TILE_SOURCE_TYPE     = "TILE_SOURCE_TYPE";
    private static final String             PROJECTION           = "PROJECTION";
    // private static final String YANDEX_TRAFFIC_ON = "YANDEX_TRAFFIC_ON";

    private final PreferenceGroup           mPrefMapsgroup;
    private Context                         mPrefActivity;
    private SharedPreferences               mSharedPreferences;

    private final List<MapMenuItemInfo>     menuItemInfos;

    public PredefMapsParser(final PreferenceGroup aPrefMapsgroup, final Context aPrefActivity) {
        super();
        menuItemInfos = null;
        mRendererInfo = null;
        mMapId = null;
        mPrefMapsgroup = aPrefMapsgroup;
        mPrefActivity = aPrefActivity;
    }

    public PredefMapsParser(final SharedPreferences pref) {
        super();
        menuItemInfos = new ArrayList<MapMenuItemInfo>();
        mSharedPreferences = pref;
        mRendererInfo = null;
        mMapId = null;
        mPrefMapsgroup = null;
    }

    public PredefMapsParser(final OpenStreetMapRendererInfo aRendererInfo, final String aMapId) {
        super();
        menuItemInfos = null;
        mRendererInfo = aRendererInfo;
        mMapId = aMapId;
        mPrefMapsgroup = null;
    }

    public List<MapMenuItemInfo> getMapMenuItemInfoList() {
        return menuItemInfos;
    }

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
        if (localName.equalsIgnoreCase(PredefMapsParser.MAP)) {
            if (mRendererInfo != null) {
                if (attributes.getValue(PredefMapsParser.ID).equalsIgnoreCase(mMapId)) {
                    mRendererInfo.ID = attributes.getValue(PredefMapsParser.ID);
                    mRendererInfo.NAME = attributes.getValue(PredefMapsParser.NAME);
                    mRendererInfo.BASEURL = attributes.getValue(PredefMapsParser.BASEURL);
                    mRendererInfo.ZOOM_MINLEVEL = Integer.parseInt(attributes.getValue(PredefMapsParser.ZOOM_MINLEVEL));
                    mRendererInfo.ZOOM_MAXLEVEL = Integer.parseInt(attributes.getValue(PredefMapsParser.ZOOM_MAXLEVEL));
                    mRendererInfo.IMAGE_FILENAMEENDING = attributes.getValue(PredefMapsParser.IMAGE_FILENAMEENDING);
                    mRendererInfo.MAPTILE_SIZEPX = Integer.parseInt(attributes.getValue(PredefMapsParser.MAPTILE_SIZEPX));
                    mRendererInfo.URL_BUILDER_TYPE = Integer.parseInt(attributes.getValue(PredefMapsParser.URL_BUILDER_TYPE));
                    mRendererInfo.TILE_SOURCE_TYPE = Integer.parseInt(attributes.getValue(PredefMapsParser.TILE_SOURCE_TYPE));
                    mRendererInfo.PROJECTION = Integer.parseInt(attributes.getValue(PredefMapsParser.PROJECTION));
                    // mRendererInfo.YANDEX_TRAFFIC_ON = Integer
                    // .parseInt(attributes.getValue(YANDEX_TRAFFIC_ON));

                    mRendererInfo.LAYER = false;
                    if (attributes.getIndex(PredefMapsParser.LAYER) > -1) {
                        mRendererInfo.LAYER = Boolean.parseBoolean(attributes.getValue(PredefMapsParser.LAYER));
                    }

                    mRendererInfo.CACHE = "";
                    if (attributes.getIndex(PredefMapsParser.CACHE) > -1) {
                        mRendererInfo.CACHE = attributes.getValue(PredefMapsParser.CACHE);
                    }
                }
            }
            else if (menuItemInfos != null) {
                final int i = attributes.getIndex(PredefMapsParser.LAYER);
                if (mSharedPreferences.getBoolean(PrefConstants.PREF_PREDEFMAPS_ + attributes.getValue(PredefMapsParser.ID), true)
                        && ((i == -1) || !attributes.getValue(PredefMapsParser.LAYER).equalsIgnoreCase(PredefMapsParser.TRUE))) {

                    // final MenuItem item = mSubmenu.add(attributes
                    // .getValue(NAME));
                    // item.setTitleCondensed(attributes.getValue(ID));

                    final MapMenuItemInfo mii = new MapMenuItemInfo();
                    mii.setName(attributes.getValue(PredefMapsParser.NAME));
                    mii.setId(attributes.getValue(PredefMapsParser.ID));
                    menuItemInfos.add(mii);

                }
            }
            else if (mPrefMapsgroup != null) {
                final int i = attributes.getIndex(PredefMapsParser.LAYER);
                if ((i == -1) || !attributes.getValue(PredefMapsParser.LAYER).equalsIgnoreCase(PredefMapsParser.TRUE)) {
                    final CheckBoxPreference pref = new CheckBoxPreference(mPrefActivity);
                    pref.setKey(PrefConstants.PREF_PREDEFMAPS_ + attributes.getValue(PredefMapsParser.ID));
                    pref.setTitle(attributes.getValue(PredefMapsParser.NAME));
                    pref.setSummary(attributes.getValue(PredefMapsParser.DESCR));
                    pref.setDefaultValue(true);
                    mPrefMapsgroup.addPreference(pref);
                }
            }
        }
        super.startElement(uri, localName, name, attributes);
    }

}
