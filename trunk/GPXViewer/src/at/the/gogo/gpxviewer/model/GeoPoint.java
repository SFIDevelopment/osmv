// Created by plusminus on 21:28:12 - 25.09.2008
package at.the.gogo.gpxviewer.model;

import at.the.gogo.gpxviewer.util.geo.GeoMathUtil;
import at.the.gogo.gpxviewer.util.geo.GeopointParser;

/**
 * @author Nicolas Gramlich
 */
public class GeoPoint {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private int mLongitudeE6;
    private int mLatitudeE6;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeoPoint(final int aLatitudeE6, final int aLongitudeE6) {
        mLatitudeE6 = aLatitudeE6;
        mLongitudeE6 = aLongitudeE6;
    }

    public GeoPoint(final double aLatitude, final double aLongitude) {
        this((int) (aLatitude * 1E6), (int) (aLongitude * 1E6));
    }

    protected static GeoPoint fromDoubleString(final String s, final char spacer) {
        final int spacerPos = s.indexOf(spacer);
        return new GeoPoint((int) (Double.parseDouble(s.substring(0, spacerPos - 1)) * 1E6),
                (int) (Double.parseDouble(s.substring(spacerPos + 1, s.length())) * 1E6));
    }

    public static GeoPoint fromDoubleString(final String s) {
        // final int commaPos = s.indexOf(',');
        final String[] f = s.split(",");
        return new GeoPoint((int) (Double.parseDouble(f[0]) * 1E6), (int) (Double.parseDouble(f[1]) * 1E6));
        // return new
        // GeoPoint((int)(Double.parseDouble(s.substring(0,commaPos-1))* 1E6),
        // (int)(Double.parseDouble(s.substring(commaPos+1,s.length()))* 1E6));
    }

    public static GeoPoint from2DoubleString(final String lat, final String lon) {
        try {
            return new GeoPoint((int) (Double.parseDouble(lat) * 1E6), (int) (Double.parseDouble(lon) * 1E6));
        }
        catch (final NumberFormatException e) {
            return new GeoPoint(0, 0);
        }
    }

    public static GeoPoint fromIntString(final String s) {
        final int commaPos = s.indexOf(',');
        return new GeoPoint(Integer.parseInt(s.substring(0, commaPos - 1)), Integer.parseInt(s.substring(commaPos + 1, s.length())));
    }

    public GeoPoint(final String latText, final String lonText) {
        this(GeopointParser.parseLatitude(latText), GeopointParser.parseLongitude(lonText));
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public int getLongitudeE6() {
        return mLongitudeE6;
    }

    public int getLatitudeE6() {
        return mLatitudeE6;
    }

    public double getLongitude() {
        return mLongitudeE6 / 1E6;
    }

    public double getLatitude() {
        return mLatitudeE6 / 1E6;
    }

    public void setLongitudeE6(final int aLongitudeE6) {
        mLongitudeE6 = aLongitudeE6;
    }

    public void setLongitude(final double aLongitude) {
        mLongitudeE6 = (int) (aLongitude * 1E6);
    }

    public void setLatitudeE6(final int aLatitudeE6) {
        mLatitudeE6 = aLatitudeE6;
    }

    public void setLatitude(final double aLatitude) {
        mLatitudeE6 = (int) (aLatitude * 1E6);
    }

    public void setCoordsE6(final int aLatitudeE6, final int aLongitudeE6) {
        mLatitudeE6 = aLatitudeE6;
        mLongitudeE6 = aLongitudeE6;
    }

    public void setCoords(final int aLatitude, final int aLongitude) {
        setLatitude(aLatitude);
        setLongitude(aLongitude);
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public String toString() {
        return new StringBuilder().append(mLatitudeE6).append(",").append(mLongitudeE6).toString();
    }

    public String toDoubleString() {
        return new StringBuilder().append(mLatitudeE6 / 1E6).append(",").append(mLongitudeE6 / 1E6).toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof GeoPoint)) {
            return false;
        }
        final GeoPoint g = (GeoPoint) obj;
        return (g.mLatitudeE6 == mLatitudeE6) && (g.mLongitudeE6 == mLongitudeE6);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * @see Source@
     *      http://web.archive.org/web/20091024181958/http://geocities.com
     *      /drchengalva/GPSDistance.html
     * @param gpA
     * @param gpB
     * @return distance in meters
     */
    public int distanceTo(final GeoPoint other) {

        return GeoMathUtil.distanceTo((mLatitudeE6 / 1E6), (mLongitudeE6 / 1E6), (other.mLatitudeE6 / 1E6), (other.mLongitudeE6 / 1E6));

    }

    // ----------------------------------

}
