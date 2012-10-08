package org.outlander.model;

import org.andnav.osm.util.GeoPoint;
import org.outlander.constants.DBConstants;

public class ProximityTarget {

    private final int Id;
    public String     Title;
    public String     Descr;
    public GeoPoint   GeoPoint;
    public int        RouteId;
    public int        RouteOrder;
    public int        PointSourceId;
    public int        Distance;
    public boolean    enterOrLeave = true;

    public ProximityTarget(final int id, final String mTitle, final String mDescr, final GeoPoint mGeoPoint, final int routeid, final int routeOrder,
            final int distance, final int enter) {
        Id = id;
        Title = mTitle;
        Descr = mDescr;
        GeoPoint = mGeoPoint;
        RouteId = routeid;
        RouteOrder = routeOrder;
        Distance = distance;
        enterOrLeave = enter == 1 ? true : false;
    }

    public ProximityTarget() {
        this(DBConstants.EMPTY_ID, "", "", null, -1, -1, 100, 1);
    }

    public int getId() {
        return Id;
    }

    public static int EMPTY_ID() {
        return DBConstants.EMPTY_ID;
    }

}
