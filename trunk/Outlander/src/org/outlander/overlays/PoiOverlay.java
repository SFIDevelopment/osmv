package org.outlander.overlays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.model.PoiCategory;
import org.outlander.model.PoiPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class PoiOverlay extends BasePointOverlay implements RefreshableOverlay {

    private final PoiListUpdater           updater;
    Thread                                 mThread;

    private boolean                        mCanUpdateList = true;
    private double                         oldDeltaX;
    private double                         oldDeltaY;

    protected SharedPreferences            sharedPreferences;

    public final static String             POI_CMD        = "POI_CMD";
    public final static String             POI_REFRESH    = "POI_REFRESH";
    public final static String             POI_DEL        = "POI_DEL";
    public final static String             POI_FOCUS      = "POI_FOCUS";
    public final static String             POI_ID         = "ID";

    // protected Drawable markerShadow;

    protected HashMap<Integer, MarkerInfo> mBtnMap;                       // iconcache

    public PoiOverlay(final Context ctx, final OnItemTapListener<PoiPoint> onItemTapListener, final SharedPreferences sharedPreferences) {

        super(ctx, onItemTapListener);
        this.sharedPreferences = sharedPreferences;

        mCanUpdateList = true;
        updater = new PoiListUpdater();
        mThread = new Thread(updater);

        registerMessageReceiver(ctx, POI_CMD);
    }

    public void setGpsStatusGeoPoint(final GeoPoint geopoint, final String title, final String descr) {
        final PoiPoint poi = new PoiPoint(title, descr, geopoint, R.drawable.poi);
        if (mItemList == null) {
            mItemList = new ArrayList<PointInfo>();
        }
        else {
            mItemList.clear();
        }

        final PointInfo pointInfo = new PointInfo();
        pointInfo.poiPoint = poi;
        mItemList.add(pointInfo);
        mCanUpdateList = false;
    }

    @Override
    public void requestPointsForArea(final double deltaX, final double deltaY) {
        if (mCanUpdateList) {
            updater.setParams(1.5 * deltaX, 1.5 * deltaY);
            mThread.run();
        }
    }

    private class PoiListUpdater implements Runnable {

        private double mdeltaX;
        private double mdeltaY;

        public void setParams(final double deltaX, final double deltaY) {
            mdeltaX = deltaX;
            mdeltaY = deltaY;

            oldDeltaX = deltaX;
            oldDeltaY = deltaY;
        }

        @Override
        public void run() {

            try {
                final List<PoiPoint> itemList = CoreInfoHandler.getInstance().getDBManager(null)
                        .getPoiListNotHidden(mLastZoom, mLastMapCenter, mdeltaX, mdeltaY);

                final List<PointInfo> itemInfoList = new ArrayList<PointInfo>();
                for (final PoiPoint point : itemList) {
                    final PointInfo info = new PointInfo();
                    info.poiPoint = point;
                    info.shadowIconId = R.drawable.poi_shadow;

                    if (point.getIconId() <= 0) {
                        // get category & icon
                        final PoiCategory cat = CoreInfoHandler.getInstance().getDBManager(null).getPoiCategory(point.getCategoryId());

                        info.iconId = cat.IconId;
                    }
                    itemInfoList.add(info);
                }

                setItemList(itemInfoList);
            }
            catch (final Exception x) {
                Ut.d("Error retrieving poilist: " + x.toString());
            }
        }
    }

    @Override
    protected void zoomLevelChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void centerChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh() {
        requestPointsForArea(oldDeltaX, oldDeltaY);
        CoreInfoHandler.getInstance().getPoiOverlay().refresh();
    }

    private void removePOI(int id) {
        removePoiById(id);
    }

    protected void messageReceived(Context context, Intent intent) {

        // String action = intent.getAction();
        String cmd = intent.getStringExtra(POI_CMD);

        if (cmd.equals(POI_REFRESH)) {
            refresh();
        }
        else if (cmd.equals(POI_DEL)) {
            int poiId = intent.getIntExtra(POI_ID, -1);
            if (poiId > -1)
                removePOI(poiId);
        }
        else if (cmd.equals(POI_FOCUS)) {
            int poiId = intent.getIntExtra(POI_ID, -1);
            // if (poiId > -1)
            // setSelectRouteById(routeId);
        }

    }
}
