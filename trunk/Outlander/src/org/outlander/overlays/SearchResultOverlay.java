package org.outlander.overlays;

import java.util.ArrayList;
import java.util.List;

import org.outlander.R;
import org.outlander.model.PoiPoint;

import android.content.Context;

public class SearchResultOverlay extends BasePointOverlay implements RefreshableOverlay {

    public final static int ICON_ID        = R.drawable.poi_attraction;
    public final static int SHADOW_ICON_ID = R.drawable.poi_shadow;

    List<PoiPoint>          mLocations;

    public SearchResultOverlay(final Context ctx, final OnItemTapListener<PoiPoint> onItemTapListener) {
        super(ctx, onItemTapListener);
    }

    public void setLocations(final List<PoiPoint> locations) {

        final List<PointInfo> points = new ArrayList<PointInfo>();
        mLocations = locations;

        for (final PoiPoint location : locations) {
            final PointInfo point = new PointInfo();
            point.poiPoint = location;
            point.poiPoint.setIconId(SearchResultOverlay.ICON_ID);
            points.add(point);
        }
        setItemList(points);
    }

    public List<PoiPoint> getLocations() {
        return mLocations;
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public void refresh() {
        mItemList = null;
    }

    @Override
    public void requestPointsForArea(final double deltaX, final double deltaY) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void zoomLevelChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void centerChanged() {
        // TODO Auto-generated method stub

    }

}
