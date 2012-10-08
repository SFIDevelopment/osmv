package org.outlander.overlays;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.model.PoiPoint;

import android.content.Context;

public class ExternalPointOverlay extends BasePointOverlay {

    public final static int ICON_ID = R.drawable.poi_attraction;

    public ExternalPointOverlay(final Context ctx, final OnItemTapListener<PoiPoint> onItemTapListener) {
        super(ctx, onItemTapListener);
    }

    public void setLocation(final double latitude, final double longitude, final String address, final String title) {

        final List<PointInfo> points = new ArrayList<PointInfo>();

        final PointInfo point = new PointInfo();

        point.poiPoint = new PoiPoint(((title != null) ? title : "Target"), ((address != null) ? address : "unknown"), new GeoPoint(latitude, longitude),
                ExternalPointOverlay.ICON_ID);
        points.add(point);

        setItemList(points);
    }

    @Override
    public void clear() {
        super.clear();
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
