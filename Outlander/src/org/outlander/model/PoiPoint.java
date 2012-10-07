package org.outlander.model;

import org.andnav.osm.util.GeoPoint;
import org.outlander.R;
import org.outlander.constants.DBConstants;

public class PoiPoint {

    private int      id;
    private String   title;
    private String   descr;
    private GeoPoint geoPoint;
    private int      iconId;
    private double   alt;
    private int      categoryId;
    private int      pointSourceId;
    private boolean  hidden;

    public PoiPoint(final int newId, final String newtitle, final String descr,
            final GeoPoint geoPoint, final int iconid, final int categoryid,
            final double alt, final int sourceid, final int hidden) {

        setId(newId);
        setTitle(newtitle);
        setDescr(descr);
        setGeoPoint(geoPoint);
        setIconId(iconid);
        setAlt(alt);
        setCategoryId(categoryid);
        setPointSourceId(sourceid);
        setHidden(hidden == 1 ? true : false);
    }

    public PoiPoint() {
        this(DBConstants.EMPTY_ID, "", "", null, R.drawable.poi, 0, -1, 0, 0);
    }

    public PoiPoint(final int id, final String mTitle, final String mDescr,
            final GeoPoint mGeoPoint, final int categoryid, final int iconid) {
        this(id, mTitle, mDescr, mGeoPoint, iconid, categoryid, -1, 0, 0);
    }

    public PoiPoint(final String mTitle, final String mDescr,
            final GeoPoint mGeoPoint, final int iconid) {
        this(DBConstants.EMPTY_ID, mTitle, mDescr, mGeoPoint, iconid, 0, -1, 0,
                0);
    }

    public int getId() {
        return id;
    }

    public static int EMPTY_ID() {
        return DBConstants.EMPTY_ID;
    }

    public void setId(final int mId) {
        this.id = mId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(final String descr) {
        this.descr = descr;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(final GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(final int iconId) {
        this.iconId = iconId;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(final double alt) {
        this.alt = alt;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final int categoryId) {
        this.categoryId = categoryId;
    }

    public int getPointSourceId() {
        return pointSourceId;
    }

    public void setPointSourceId(final int pointSourceId) {
        this.pointSourceId = pointSourceId;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

}
