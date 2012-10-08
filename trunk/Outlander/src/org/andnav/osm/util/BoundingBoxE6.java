// Created by plusminus on 19:06:38 - 25.09.2008
package org.andnav.osm.util;

import java.util.ArrayList;

import org.andnav.osm.util.constants.OpenStreetMapConstants;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;
import org.outlander.utils.geo.GeoMathUtil;

/**
 * @author Nicolas Gramlich
 */
public class BoundingBoxE6 implements OpenStreetMapViewConstants, OpenStreetMapConstants {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    protected final int mLatNorthE6;
    protected final int mLatSouthE6;
    protected final int mLonEastE6;
    protected final int mLonWestE6;

    // ===========================================================
    // Constructors
    // ===========================================================

    public BoundingBoxE6(final int northE6, final int eastE6, final int southE6, final int westE6) {
        mLatNorthE6 = northE6;
        mLatSouthE6 = southE6;
        mLonWestE6 = westE6;
        mLonEastE6 = eastE6;
    }

    public BoundingBoxE6(final double north, final double east, final double south, final double west) {
        mLatNorthE6 = (int) (north * 1E6);
        mLatSouthE6 = (int) (south * 1E6);
        mLonWestE6 = (int) (west * 1E6);
        mLonEastE6 = (int) (east * 1E6);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public int getDiagonalLengthInMeters() {
        return new GeoPoint(mLatNorthE6, mLonWestE6).distanceTo(new GeoPoint(mLatSouthE6, mLonEastE6));
    }

    public int getLatNorthE6() {
        return mLatNorthE6;
    }

    public int getLatSouthE6() {
        return mLatSouthE6;
    }

    public int getLonEastE6() {
        return mLonEastE6;
    }

    public int getLonWestE6() {
        return mLonWestE6;
    }

    public int getLatitudeSpanE6() {
        return Math.abs(mLatNorthE6 - mLatSouthE6);
    }

    public int getLongitudeSpanE6() {
        return Math.abs(mLonEastE6 - mLonWestE6);
    }

    /**
     * @param aLatitude
     * @param aLongitude
     * @param reuse
     * @return relative position determined from the upper left corner.<br />
     *         {0,0} would be the upper left corner. {1,1} would be the lower
     *         right corner. {1,0} would be the lower left corner. {0,1} would
     *         be the upper right corner.
     */
    public float[] getRelativePositionOfGeoPointInBoundingBoxWithLinearInterpolation(final int aLatitude, final int aLongitude, final float[] reuse) {
        final float[] out = (reuse != null) ? reuse : new float[2];
        out[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] = ((float) (mLatNorthE6 - aLatitude) / getLatitudeSpanE6());
        out[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] = 1 - ((float) (mLonEastE6 - aLongitude) / getLongitudeSpanE6());
        return out;
    }

    public float[] getRelativePositionOfGeoPointInBoundingBoxWithExactGudermannInterpolation(final int aLatitudeE6, final int aLongitudeE6, final float[] reuse) {
        final float[] out = (reuse != null) ? reuse : new float[2];
        out[OpenStreetMapViewConstants.MAPTILE_LATITUDE_INDEX] = (float) ((GeoMathUtil.gudermannInverse(mLatNorthE6 / 1E6) - GeoMathUtil
                .gudermannInverse(aLatitudeE6 / 1E6)) / (GeoMathUtil.gudermannInverse(mLatNorthE6 / 1E6) - GeoMathUtil.gudermannInverse(mLatSouthE6 / 1E6)));
        out[OpenStreetMapViewConstants.MAPTILE_LONGITUDE_INDEX] = 1 - ((float) (mLonEastE6 - aLongitudeE6) / getLongitudeSpanE6());
        return out;
    }

    public GeoPoint getGeoPointOfRelativePositionWithLinearInterpolation(final float relX, final float relY) {

        int lat = (int) (mLatNorthE6 - (getLatitudeSpanE6() * relY));

        int lon = (int) (mLonWestE6 + (getLongitudeSpanE6() * relX));

        /* Bring into bounds. */
        while (lat > 90500000) {
            lat -= 90500000;
        }
        while (lat < -90500000) {
            lat += 90500000;
        }

        /* Bring into bounds. */
        while (lon > 180000000) {
            lon -= 180000000;
        }
        while (lon < -180000000) {
            lon += 180000000;
        }

        return new GeoPoint(lat, lon);
    }

    public GeoPoint getGeoPointOfRelativePositionWithExactGudermannInterpolation(final float relX, final float relY) {

        final double gudNorth = GeoMathUtil.gudermannInverse(mLatNorthE6 / 1E6);
        final double gudSouth = GeoMathUtil.gudermannInverse(mLatSouthE6 / 1E6);
        final double latD = GeoMathUtil.gudermann((gudSouth + ((1 - relY) * (gudNorth - gudSouth))));
        int lat = (int) (latD * 1E6);

        int lon = (int) ((mLonWestE6 + (getLongitudeSpanE6() * relX)));

        /* Bring into bounds. */
        while (lat > 90500000) {
            lat -= 90500000;
        }
        while (lat < -90500000) {
            lat += 90500000;
        }

        /* Bring into bounds. */
        while (lon > 180000000) {
            lon -= 180000000;
        }
        while (lon < -180000000) {
            lon += 180000000;
        }

        return new GeoPoint(lat, lon);
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public String toString() {
        return new StringBuffer().append("N:").append(mLatNorthE6).append("; E:").append(mLonEastE6).append("; S:").append(mLatSouthE6).append("; W:")
                .append(mLonWestE6).toString();
    }

    public static BoundingBoxE6 fromGeoPoints(final ArrayList<? extends GeoPoint> partialPolyLine) {
        int minLat = Integer.MAX_VALUE;
        int minLon = Integer.MAX_VALUE;
        int maxLat = Integer.MIN_VALUE;
        int maxLon = Integer.MIN_VALUE;
        for (final GeoPoint gp : partialPolyLine) {
            final int latitudeE6 = gp.getLatitudeE6();
            final int longitudeE6 = gp.getLongitudeE6();

            minLat = Math.min(minLat, latitudeE6);
            minLon = Math.min(minLon, longitudeE6);
            maxLat = Math.max(maxLat, latitudeE6);
            maxLon = Math.max(maxLon, longitudeE6);
        }

        return new BoundingBoxE6(minLat, maxLon, maxLat, minLon);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
