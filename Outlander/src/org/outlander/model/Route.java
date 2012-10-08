package org.outlander.model;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.outlander.constants.DBConstants;

public class Route {

    private int            id;
    private String         name;
    private String         descr;
    private boolean        show;
    private int            category;

    private List<PoiPoint> routePoints = null;

    public PoiPoint        lastRoutePoint;

    public List<PoiPoint> getPoints() {
        if (routePoints == null) {
            routePoints = new ArrayList<PoiPoint>();
        }

        return routePoints;
    }

    public List<GeoPoint> getGeoPoints() {
        final List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
        for (final PoiPoint point : getPoints()) {
            geoPoints.add(point.getGeoPoint());
        }
        return geoPoints;
    }

    public void addRoutePoint(final String mTitle, final String mDescr, final GeoPoint mGeoPoint, final int iconid, final int categoryid, final double alt,
            final int hidden) {

        final PoiPoint point = new PoiPoint(DBConstants.EMPTY_ID, mTitle, mDescr, mGeoPoint, iconid, categoryid, alt, id, hidden);
        getPoints().add(point);
        lastRoutePoint = point;
    }

    public void addRoutePoint(final PoiPoint poiPoint) {
        getPoints().add(poiPoint);
        lastRoutePoint = poiPoint;

        poiPoint.setPointSourceId(getId());
    }

    public void addRoutePoint() {
        addRoutePoint(new PoiPoint()); // dont remember why....
    }

    public Route() {
        this(DBConstants.EMPTY_ID, "", "", false, DBConstants.EMPTY_ID);
    }

    public Route(final int id, final String name, final String descr, final boolean show, final int category) {
        setId(id);
        setName(name);
        setDescr(descr);
        setShow(show);
        setCategory(category);
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(final String descr) {
        this.descr = descr;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(final boolean show) {
        this.show = show;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(final int category) {
        this.category = category;
    }

    public List<PoiPoint> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(final List<PoiPoint> routePoints) {
        this.routePoints = routePoints;
    }

    public PoiPoint getLastRoutePoint() {
        return lastRoutePoint;
    }

    public void setLastRoutePoint(final PoiPoint lastRoutePoint) {
        this.lastRoutePoint = lastRoutePoint;
    }

}
