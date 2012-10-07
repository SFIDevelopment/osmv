package org.outlander.model;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.outlander.constants.DBConstants;

public class Track {

    private int              Id;
    public String            Name;
    public String            Descr;
    public TrackPoint        LastTrackPoint;
    public boolean           Show;
    public double            AvgSpeed;
    public double            Distance;
    public long              Time;

    private List<TrackPoint> trackpoints = null;

    public List<TrackPoint> getPoints() {
        if (trackpoints == null) {
            return new ArrayList<TrackPoint>(0);
        }

        return trackpoints;
    }

    public void setPoints(final List<TrackPoint> newList) {
        trackpoints = newList;
    }

    public void AddTrackPoint() {
        LastTrackPoint = new TrackPoint();
        if (trackpoints == null) {
            trackpoints = new ArrayList<TrackPoint>(1);
        }
        trackpoints.add(LastTrackPoint);
    }

    public Track() {
        this(DBConstants.EMPTY_ID, "", "", false);
    }

    public Track(final int id, final String name, final String descr,
            final boolean show) {
        Id = id;
        Name = name;
        Descr = descr;
        Show = show;
    }

    public int getId() {
        return Id;
    }

    public GeoPoint getFirstGeoPoint() {
        if (trackpoints.size() > 0) {
            return new GeoPoint(trackpoints.get(0).getLatitudeE6(), trackpoints
                    .get(0).getLongitudeE6());
        }
        return null;
    }

    public GeoPoint getLastGeoPoint() {
        if (trackpoints.size() > 0) {
            return new GeoPoint(trackpoints.get(trackpoints.size() - 1)
                    .getLatitudeE6(), trackpoints.get(0).getLongitudeE6());
        }
        return null;
    }

    public TrackPoint getFirstTrackPoint() {
        if (trackpoints.size() > 0) {
            return trackpoints.get(0);
        }
        return null;
    }

    public TrackPoint getLastTrackPoint() {
        if (trackpoints.size() > 0) {
            return trackpoints.get(trackpoints.size() - 1);
        }
        return null;
    }

    public void setId(final int id) {
        Id = id;
    }

}
