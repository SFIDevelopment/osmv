package org.outlander.model;

import java.util.Date;

import org.andnav.osm.util.GeoPoint;

public class TrackPoint extends GeoPoint {

    public double alt;
    public double speed;
    public Date   date;

    public TrackPoint() {
        super(0, 0);
        alt = 0;
        speed = 0;
        date = new Date();
    }

    public TrackPoint(final double lat, final double lon) {
        super((int) (lat * 1E6), (int) (lon * 1E6));
        alt = 0;
        speed = 0;
        date = new Date();
    }

};
