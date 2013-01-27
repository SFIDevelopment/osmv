package org.outlander.utils.geo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.util.Util;
import org.outlander.R;
import org.outlander.model.TrackPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.util.SparseArray;

public class GeoMathUtil {

    // for general info see
    // http://www.maptiler.org/google-maps-coordinates-tile-bounds-projection/

    // ===========================================================
    // Constants
    // ===========================================================
    public static final int     RADIUS_EARTH_METERS         = 6378140;
    public static final double  EARTH_RADIUS                = RADIUS_EARTH_METERS / 1000;
    public static final double  kmInMiles                   = 1 / 1.609344;

//    private static int EARTH_RADIUS_KM = 6371;

    public static int MILLION = 1000000;

    
    public static final double  KM_TO_MI                    = 0.621371192;
    public static final double  M_TO_FT                     = 3.2808399;
    public static final double  MI_TO_M                     = 1609.344;
    public static final double  MI_TO_FEET                  = 5280.0;
    public static final double  KMH_TO_MPH                  = (1000 * M_TO_FT) / MI_TO_FEET;
    public static final double  TO_RADIANS                  = Math.PI / 180.0;
    public static final float   erad                        = 6371.0f;
    public static final float   DEG2RAD                     = (float) (Math.PI / 180.0);
    public static final float   RAD2DEG                     = (float) (180.0 / Math.PI);

    public static final float   PI                          = (float) Math.PI;
    public static final float   PI_2                        = PI / 2.0f;
    public static final float   PI_4                        = PI / 4.0f;

    // private static final double EARTH_RADIUS = 6367450; // in meters
    // (geometric mean) ??
    private static final double MIN_LAT                     = Math.toRadians(-90);                                  // -PI/2
    private static final double MAX_LAT                     = Math.toRadians(90);                                   // PI/2
    private static final double MIN_LON                     = Math.toRadians(-180);                                 // -PI
    private static final double MAX_LON                     = Math.toRadians(180);                                  // PI

    // format identifiers
    public static final int     FORMAT_DEZIMAL              = 0;
    public static final int     FORMAT_DM                   = 1;
    public static final int     FORMAT_DMS                  = 2;
    public static final int     FORMAT_UTM                  = 3;
    public static final int     FORMAT_MGRS                 = 4;
    public static final int     FORMAT_UTMNAD27             = 5;
    public static final int     FORMAT_MGRSNAD27            = 6;
    public static final int     FORMAT_GEOHASH              = 7;
    public static final int     FORMAT_MAIDENHEAD           = 8;
    public static final int     MAX_FORMAT                  = 8;

    private static float        superelevation              = 1.0f;
    private static final double PRECISION                   = 1e-6;

    private static GeoMathUtil  instance                    = null;

    // http://www.engineeringtoolbox.com/velocity-units-converter-d_1035.html
    // meters/sec to ...
    public final static double  speedConversionFactors[]    = { 1, 3.6, 2.24, 1.94, 196.9, 3.28, 65.6 };
    // http://www.engineeringtoolbox.com/length-units-converter-d_1033.html
    // meter to ...
    public final static double  distanceConversionFactors[] = { 1, 0.001, 3.28, 1.09, 0.00621 };

    
    public static float[] distanceAndBearing(final double latitude1, final double longitude1, final double latitude2, final double longitude2) {
        
        final float results[] = { (float) 1.0,(float) 1.0 };
        
         Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, results);
        
        return results;
    }
    
    /**
     * @see Source@ http://www.geocities.com/DrChengalva/GPSDistance.html
     * @param gpA
     * @param gpB
     * @return distance in meters //, initial and final bearing
     */
    public static int distanceTo(final double latitude1, final double longitude1, final double latitude2, final double longitude2) {

        final float results[] = { (float) 1.0 };
        Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, results);

        // final double a1 = GeoMathUtil.DEG2RAD * latitude1;
        // final double a2 = GeoMathUtil.DEG2RAD * longitude1;
        // final double b1 = GeoMathUtil.DEG2RAD * latitude2;
        // final double b2 = GeoMathUtil.DEG2RAD * longitude2;
        //
        // final double cosa1 = Math.cos(a1);
        // final double cosb1 = Math.cos(b1);
        //
        // final double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);
        //
        // final double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);
        //
        // final double t3 = Math.sin(a1) * Math.sin(b1);
        //
        // final double tt = Math.acos(t1 + t2 + t3);
        //
        // return (int) (GeoMathUtil.RADIUS_EARTH_METERS * tt);

        return (int) results[0];
    }

    /*
     * a) heading: your heading from the hardware compass. This is in degrees
     * east of magnetic north b) bearing: the bearing from your location to the
     * destination location. This is in degrees east of true north.
     * myLocation.bearingTo(destLocation); c) declination: the difference
     * between true north and magnetic north The heading that is returned from
     * the magnetometer + accelermometer is in degrees east of true (magnetic)
     * north (-180 to +180) so you need to get the difference between north and
     * magnetic north for your location. This difference is variable depending
     * where you are on earth. You can obtain by using GeomagneticField class.
     * GeomagneticField geoField; private final LocationListener
     * locationListener = new LocationListener() { public void
     * onLocationChanged(Location location) { geoField = new GeomagneticField(
     * Double.valueOf(location.getLatitude()).floatValue(),
     * Double.valueOf(location.getLongitude()).floatValue(),
     * Double.valueOf(location.getAltitude()).floatValue(),
     * System.currentTimeMillis() ); ... } } Armed with these you calculate the
     * angle of the arrow to draw on your map to show where you are facing in
     * relation to your destination object rather than true north. First adjust
     * your heading with the declination: heading += geoField.getDeclination();
     * Second, you need to offset the direction in which the phone is facing
     * (heading) from the target destination rather than true north. This is the
     * part that I got stuck on. The heading value returned from the compass
     * gives you a value that describes where magnetic north is (in degrees east
     * of true north) in relation to where the phone is pointing. So e.g. if the
     * value is -10 you know that magnetic north is 10 degrees to your left. The
     * bearing gives you the angle of your destination in degrees east of true
     * north. So after you've compensated for the declination you can use the
     * formula below to get the desired result: heading = myBearing - (myBearing
     * + heading); You'll then want to convert from degrees east of true north
     * (-180 to +180) into normal degrees (0 to 360): Math.round(-heading / 360
     * + 180)
     */

    public int headinginDegrees(final Location source, final Location destination) { // TODO:
        // Wrong!!
        int heading = 0; // should be magnetic
        final float myBearing = source.bearingTo(destination);

        final GeomagneticField geoField = new GeomagneticField(Double.valueOf(source.getLatitude()).floatValue(), Double.valueOf(source.getLongitude())
                .floatValue(), Double.valueOf(source.getAltitude()).floatValue(), System.currentTimeMillis());
        heading += (int) geoField.getDeclination();

        heading = Math.round(((myBearing - (myBearing + heading)) / 360) + 180);

        return heading;
    }

    /**
     * Calculates geopoint from given bearing and distance.
     * 
     * @param bearing
     *            bearing in degree
     * @param distance
     *            distance in km
     * @return the projected geopoint
     */
    public GeoPoint project(final GeoPoint source, final double bearing, final double distance) {
        final double rlat1 = source.getLatitude() * DEG2RAD;
        final double rlon1 = source.getLongitude() * DEG2RAD;
        final double rbearing = bearing * DEG2RAD;
        final double rdistance = distance / erad;

        final double rlat = Math.asin((Math.sin(rlat1) * Math.cos(rdistance)) + (Math.cos(rlat1) * Math.sin(rdistance) * Math.cos(rbearing)));
        final double rlon = rlon1
                + Math.atan2(Math.sin(rbearing) * Math.sin(rdistance) * Math.cos(rlat1), Math.cos(rdistance) - (Math.sin(rlat1) * Math.sin(rlat)));

        return new GeoPoint(rlat * RAD2DEG, rlon * RAD2DEG);
    }

    public static String formatGeoPoint(final GeoPoint point, final int formatId) {

        return formatCoordinate(point.getLatitude(), point.getLongitude(), formatId);
    }

    public static String formatLocation(final Location point, final int formatId) {

        return formatCoordinate(point.getLatitude(), point.getLongitude(), formatId);
    }

    public static CharSequence formatGeoCoord(final double double1) {
        return new StringBuilder().append(double1).toString();
    }

    public double[] utm2LatLon(final String UTM) {
        final UTM2LatLon c = new UTM2LatLon();
        return c.convertUTMToLatLong(UTM);
    }

    public String latLon2UTM(final double latitude, final double longitude) {
        final LatLon2UTM c = new LatLon2UTM();
        return c.convertLatLonToUTM(latitude, longitude);

    }

    private static void validate(final double latitude, final double longitude) {
        if ((latitude < -90.0) || (latitude > 90.0) || (longitude < -180.0) || (longitude >= 180.0)) {
            throw new IllegalArgumentException("Legal ranges: latitude [-90,90], longitude [-180,180).");
        }
    }

    public String latLon2MGRUTM(final double latitude, final double longitude) {

        // final GeoPoint gp = new GeoPoint((int) (latitude * 1E6),
        // (int) (longitude * 1E6));

        final LatLon2MGRUTM c = new LatLon2MGRUTM();
        return c.convertLatLonToMGRUTM(latitude, longitude);

    }

    public double[] mgrutm2LatLon(final String MGRUTM) {
        final MGRUTM2LatLon c = new MGRUTM2LatLon();
        return c.convertMGRUTMToLatLong(MGRUTM);
    }

    public double degreeToRadian(final double degree) {
        return (degree * Math.PI) / 180;
    }

    public double radianToDegree(final double radian) {
        return (radian * 180) / Math.PI;
    }

    private double POW(final double a, final double b) {
        return Math.pow(a, b);
    }

    private double SIN(final double value) {
        return Math.sin(value);
    }

    private double COS(final double value) {
        return Math.cos(value);
    }

    private double TAN(final double value) {
        return Math.tan(value);
    }

    private class LatLon2UTM {

        private LatLon2UTM() {
        }

        public String convertLatLonToUTM(final double latitude, final double longitude) {
            validate(latitude, longitude);
            String UTM = "";

            setVariables(latitude, longitude);

            final String longZone = getLongZone(longitude);
            final LatZones latZones = new LatZones();
            final String latZone = latZones.getLatZone(latitude);

            final double _easting = getEasting();
            final double _northing = getNorthing(latitude);

            UTM = longZone + " " + latZone + " " + ((int) _easting) + " " + ((int) _northing);
            // UTM = longZone + " " + latZone + " " +
            // decimalFormat.format(_easting) +
            // " "+ decimalFormat.format(_northing);

            return UTM;

        }

        protected void setVariables(double latitude, final double longitude) {
            latitude = degreeToRadian(latitude);
            rho = (equatorialRadius * (1 - (e * e))) / POW(1 - POW(e * SIN(latitude), 2), 3 / 2.0);

            nu = equatorialRadius / POW(1 - POW(e * SIN(latitude), 2), (1 / 2.0));

            double var1;
            if (longitude < 0.0) {
                var1 = ((int) ((180 + longitude) / 6.0)) + 1;
            }
            else {
                var1 = ((int) (longitude / 6)) + 31;
            }
            final double var2 = (6 * var1) - 183;
            final double var3 = longitude - var2;
            p = (var3 * 3600) / 10000;

            S = ((((A0 * latitude) - (B0 * SIN(2 * latitude))) + (C0 * SIN(4 * latitude))) - (D0 * SIN(6 * latitude))) + (E0 * SIN(8 * latitude));

            K1 = S * k0;
            K2 = (nu * SIN(latitude) * COS(latitude) * POW(sin1, 2) * k0 * (100000000)) / 2;
            K3 = ((POW(sin1, 4) * nu * SIN(latitude) * Math.pow(COS(latitude), 3)) / 24)
                    * ((5 - POW(TAN(latitude), 2)) + (9 * e1sq * POW(COS(latitude), 2)) + (4 * POW(e1sq, 2) * POW(COS(latitude), 4))) * k0
                    * (10000000000000000L);

            K4 = nu * COS(latitude) * sin1 * k0 * 10000;

            K5 = POW(sin1 * COS(latitude), 3) * (nu / 6) * ((1 - POW(TAN(latitude), 2)) + (e1sq * POW(COS(latitude), 2))) * k0 * 1000000000000L;

            A6 = ((POW(p * sin1, 6) * nu * SIN(latitude) * POW(COS(latitude), 5)) / 720)
                    * (((61 - (58 * POW(TAN(latitude), 2))) + POW(TAN(latitude), 4) + (270 * e1sq * POW(COS(latitude), 2))) - (330 * e1sq * POW(SIN(latitude),
                            2))) * k0 * (1E+24);

        }

        protected String getLongZone(final double longitude) {
            double longZone = 0;
            if (longitude < 0.0) {
                longZone = ((180.0 + longitude) / 6) + 1;
            }
            else {
                longZone = (longitude / 6) + 31;
            }
            String val = String.valueOf((int) longZone);
            if (val.length() == 1) {
                val = "0" + val;
            }
            return val;
        }

        protected double getNorthing(final double latitude) {
            double northing = K1 + (K2 * p * p) + (K3 * POW(p, 4));
            if (latitude < 0.0) {
                northing = 10000000 + northing;
            }
            return northing;
        }

        protected double getEasting() {
            return 500000 + ((K4 * p) + (K5 * POW(p, 3)));
        }

        // Lat Lon to UTM variables
        // equatorial radius
        final double equatorialRadius  = 6378137;
        // polar radius
        final double polarRadius       = 6356752.314;
        // flattening
        @SuppressWarnings("unused")
        double       flattening        = 0.00335281066474748;                                                // (equatorialRadius-polarRadius)/equatorialRadius;
        // inverse flattening 1/flattening
        @SuppressWarnings("unused")
        double       inverseFlattening = 298.257223563;                                                      // 1/flattening;
        // Mean radius
        @SuppressWarnings("unused")
        double       rm                = POW(equatorialRadius * polarRadius, 1 / 2.0);
        // scale factor
        final double k0                = 0.9996;
        // eccentricity
        final double e                 = Math.sqrt(1 - POW(polarRadius / equatorialRadius, 2));
        final double e1sq              = (e * e) / (1 - (e * e));
        @SuppressWarnings("unused")
        double       n                 = (equatorialRadius - polarRadius) / (equatorialRadius + polarRadius);
        // r curv 1
        @SuppressWarnings("unused")
        double       rho               = 6368573.744;
        // r curv 2
        double       nu                = 6389236.914;
        // Calculate Meridional Arc Length
        // Meridional Arc
        double       S                 = 5103266.421;
        final double A0                = 6367449.146;
        final double B0                = 16038.42955;
        final double C0                = 16.83261333;
        final double D0                = 0.021984404;
        final double E0                = 0.000312705;
        // Calculation Constants
        // Delta Long
        double       p                 = -0.483084;
        final double sin1              = 4.84814E-06;
        // Coefficients for UTM Coordinate
        double       K1                = 5101225.115;
        double       K2                = 3750.291596;
        double       K3                = 1.397608151;
        double       K4                = 214839.3105;
        double       K5                = -2.995382942;
        @SuppressWarnings("unused")
        double       A6                = -1.00541E-07;
    }

    private final class LatLon2MGRUTM extends LatLon2UTM {

        private LatLon2MGRUTM() {
        }

        public String convertLatLonToMGRUTM(final double latitude, final double longitude) {
            validate(latitude, longitude);
            String mgrUTM = "";

            setVariables(latitude, longitude);

            final String longZone = getLongZone(longitude);
            final LatZones latZones = new LatZones();
            final String latZone = latZones.getLatZone(latitude);

            final double _easting = getEasting();
            final double _northing = getNorthing(latitude);
            final Digraphs digraphs = new Digraphs();
            final String digraph1 = digraphs.getDigraph1(Integer.parseInt(longZone), _easting);
            final String digraph2 = digraphs.getDigraph2(Integer.parseInt(longZone), _northing);

            String easting = String.valueOf((int) _easting);
            if (easting.length() < 5) {
                easting = "00000" + easting;
            }
            easting = easting.substring(easting.length() - 5);

            String northing;
            northing = String.valueOf((int) _northing);
            if (northing.length() < 5) {
                northing = "0000" + northing;
            }
            northing = northing.substring(northing.length() - 5);

            mgrUTM = longZone + latZone + digraph1 + digraph2 + easting + northing;
            return mgrUTM;
        }
    }

    private class MGRUTM2LatLon extends UTM2LatLon {

        private MGRUTM2LatLon() {
        }

        public double[] convertMGRUTMToLatLong(final String mgrutm) {
            final double[] latlon = { 0.0, 0.0 };
            // 02CNR0634657742
            final int zone = Integer.parseInt(mgrutm.substring(0, 2));
            final String latZone = mgrutm.substring(2, 3);

            final String digraph1 = mgrutm.substring(3, 4);
            final String digraph2 = mgrutm.substring(4, 5);
            easting = Double.parseDouble(mgrutm.substring(5, 10));
            northing = Double.parseDouble(mgrutm.substring(10, 15));

            final LatZones lz = new LatZones();
            final double latZoneDegree = lz.getLatZoneDegree(latZone);

            final double a1 = (latZoneDegree * 40000000) / 360.0;
            final double a2 = 2000000 * Math.floor(a1 / 2000000.0);

            final Digraphs digraphs = new Digraphs();

            final double digraph2Index = digraphs.getDigraph2Index(digraph2);

            double startindexEquator = 1;
            if ((1 + (zone % 2)) == 1) {
                startindexEquator = 6;
            }

            double a3 = a2 + ((digraph2Index - startindexEquator) * 100000);
            if (a3 <= 0) {
                a3 = 10000000 + a3;
            }
            northing = a3 + northing;

            zoneCM = -183 + (6 * zone);
            final double digraph1Index = digraphs.getDigraph1Index(digraph1);
            final int a5 = 1 + (zone % 3);
            final double[] a6 = { 16, 0, 8 };
            final double a7 = 100000 * (digraph1Index - a6[a5 - 1]);
            easting += a7;

            setVariables();

            double latitude = 0;
            latitude = (180 * (phi1 - (fact1 * (fact2 + fact3 + fact4)))) / Math.PI;

            if (latZoneDegree < 0) {
                latitude = 90 - latitude;
            }

            final double d = (_a2 * 180) / Math.PI;
            final double longitude = zoneCM - d;

            if (getHemisphere(latZone).equals("S")) {
                latitude = -latitude;
            }

            latlon[0] = latitude;
            latlon[1] = longitude;
            return latlon;
        }
    }

    private class UTM2LatLon {

        double       easting;
        double       northing;
        int          zone;
        final String southernHemisphere = "ACDEFGHJKLM";

        private UTM2LatLon() {
        }

        protected String getHemisphere(final String latZone) {
            String hemisphere = "N";
            if (southernHemisphere.indexOf(latZone) > -1) {
                hemisphere = "S";
            }
            return hemisphere;
        }

        public double[] convertUTMToLatLong(final String UTM) {
            final double[] latlon = { 0.0, 0.0 };
            final String[] utm = UTM.split(" ");
            zone = Integer.parseInt(utm[0]);
            final String latZone = utm[1];
            easting = Double.parseDouble(utm[2]);
            northing = Double.parseDouble(utm[3]);
            final String hemisphere = getHemisphere(latZone);
            double latitude = 0.0;
            double longitude = 0.0;

            if (hemisphere.equals("S")) {
                northing = 10000000 - northing;
            }
            setVariables();
            latitude = (180 * (phi1 - (fact1 * (fact2 + fact3 + fact4)))) / Math.PI;

            if (zone > 0) {
                zoneCM = (6 * zone) - 183.0;
            }
            else {
                zoneCM = 3.0;

            }

            longitude = zoneCM - _a3;
            if (hemisphere.equals("S")) {
                latitude = -latitude;
            }

            latlon[0] = latitude;
            latlon[1] = longitude;
            return latlon;

        }

        protected void setVariables() {
            arc = northing / k0;
            mu = arc / (a * (1 - (POW(e, 2) / 4.0) - ((3 * POW(e, 4)) / 64.0) - ((5 * POW(e, 6)) / 256.0)));

            ei = (1 - POW((1 - (e * e)), (1 / 2.0))) / (1 + POW((1 - (e * e)), (1 / 2.0)));

            ca = ((3 * ei) / 2) - ((27 * POW(ei, 3)) / 32.0);

            cb = ((21 * POW(ei, 2)) / 16) - ((55 * POW(ei, 4)) / 32);
            cc = (151 * POW(ei, 3)) / 96;
            cd = (1097 * POW(ei, 4)) / 512;
            phi1 = mu + (ca * SIN(2 * mu)) + (cb * SIN(4 * mu)) + (cc * SIN(6 * mu)) + (cd * SIN(8 * mu));

            n0 = a / POW((1 - POW((e * SIN(phi1)), 2)), (1 / 2.0));

            r0 = (a * (1 - (e * e))) / POW((1 - POW((e * SIN(phi1)), 2)), (3 / 2.0));
            fact1 = (n0 * TAN(phi1)) / r0;

            _a1 = 500000 - easting;
            dd0 = _a1 / (n0 * k0);
            fact2 = (dd0 * dd0) / 2;

            t0 = POW(TAN(phi1), 2);
            Q0 = e1sq * POW(COS(phi1), 2);
            fact3 = (((5 + (3 * t0) + (10 * Q0)) - (4 * Q0 * Q0) - (9 * e1sq)) * POW(dd0, 4)) / 24;

            fact4 = (((61 + (90 * t0) + (298 * Q0) + (45 * t0 * t0)) - (252 * e1sq) - (3 * Q0 * Q0)) * POW(dd0, 6)) / 720;

            //
            lof1 = _a1 / (n0 * k0);
            lof2 = ((1 + (2 * t0) + Q0) * POW(dd0, 3)) / 6.0;
            lof3 = (((((5 - (2 * Q0)) + (28 * t0)) - (3 * POW(Q0, 2))) + (8 * e1sq) + (24 * POW(t0, 2))) * POW(dd0, 5)) / 120;
            _a2 = ((lof1 - lof2) + lof3) / COS(phi1);
            _a3 = (_a2 * 180) / Math.PI;

        }

        double       arc;
        double       mu;
        double       ei;
        double       ca;
        double       cb;
        double       cc;
        double       cd;
        double       n0;
        double       r0;
        double       _a1;
        double       dd0;
        double       t0;
        double       Q0;
        double       lof1;
        double       lof2;
        double       lof3;
        double       _a2;
        double       phi1;
        double       fact1;
        double       fact2;
        double       fact3;
        double       fact4;
        double       zoneCM;
        double       _a3;
        @SuppressWarnings("unused")
        double       b    = 6356752.314;
        final double a    = 6378137;
        final double e    = 0.081819191;
        final double e1sq = 0.006739497;
        final double k0   = 0.9996;
    }

    private class Digraphs {

        private final SparseArray<String> digraph1      = new SparseArray<String>();
        private final SparseArray<String> digraph2      = new SparseArray<String>();
        private final String[]            digraph1Array = { "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V",
                                                                "W", "X", "Y", "Z" };
        private final String[]            digraph2Array = { "V", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U",
                                                                "V" };

        public Digraphs() {
            digraph1.put(1, "A");
            digraph1.put(2, "B");
            digraph1.put(3, "C");
            digraph1.put(4, "D");
            digraph1.put(5, "E");
            digraph1.put(6, "F");
            digraph1.put(7, "G");
            digraph1.put(8, "H");
            digraph1.put(9, "J");
            digraph1.put(10, "K");
            digraph1.put(11, "L");
            digraph1.put(12, "M");
            digraph1.put(13, "N");
            digraph1.put(14, "P");
            digraph1.put(15, "Q");
            digraph1.put(16, "R");
            digraph1.put(17, "S");
            digraph1.put(18, "T");
            digraph1.put(19, "U");
            digraph1.put(20, "V");
            digraph1.put(21, "W");
            digraph1.put(22, "X");
            digraph1.put(23, "Y");
            digraph1.put(24, "Z");

            digraph2.put(0, "V");
            digraph2.put(1, "A");
            digraph2.put(2, "B");
            digraph2.put(3, "C");
            digraph2.put(4, "D");
            digraph2.put(5, "E");
            digraph2.put(6, "F");
            digraph2.put(7, "G");
            digraph2.put(8, "H");
            digraph2.put(9, "J");
            digraph2.put(10, "K");
            digraph2.put(11, "L");
            digraph2.put(12, "M");
            digraph2.put(13, "N");
            digraph2.put(14, "P");
            digraph2.put(15, "Q");
            digraph2.put(16, "R");
            digraph2.put(17, "S");
            digraph2.put(18, "T");
            digraph2.put(19, "U");
            digraph2.put(20, "V");

        }

        public int getDigraph1Index(final String letter) {
            for (int i = 0; i < digraph1Array.length; i++) {
                if (digraph1Array[i].equals(letter)) {
                    return i + 1;
                }
            }

            return -1;
        }

        public int getDigraph2Index(final String letter) {
            for (int i = 0; i < digraph2Array.length; i++) {
                if (digraph2Array[i].equals(letter)) {
                    return i;
                }
            }

            return -1;
        }

        public String getDigraph1(final int longZone, final double easting) {
            final int a1 = longZone;
            final double a2 = (8 * ((a1 - 1) % 3)) + 1;

            final double a3 = easting;
            final double a4 = (a2 + ((int) (a3 / 100000))) - 1;
            return digraph1.get((int) Math.floor(a4));
        }

        public String getDigraph2(final int longZone, final double northing) {
            final int a1 = longZone;
            final double a2 = 1 + (5 * ((a1 - 1) % 2));
            final double a3 = northing;
            double a4 = (a2 + ((int) (a3 / 100000)));
            a4 = (a2 + ((int) (a3 / 100000.0))) % 20;
            a4 = Math.floor(a4);
            if (a4 < 0) {
                a4 += 19;
            }
            return digraph2.get((int) Math.floor(a4));

        }
    }

    private class LatZones {

        private final char[] letters     = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };
        private final int[]  degrees     = { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16, -8, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };
        private final char[] negLetters  = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M' };
        private final int[]  negDegrees  = { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16, -8 };
        private final char[] posLetters  = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };
        private final int[]  posDegrees  = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };
        private final int    arrayLength = 22;

        public LatZones() {
        }

        public int getLatZoneDegree(final String letter) {
            final char ltr = letter.charAt(0);
            for (int i = 0; i < arrayLength; i++) {
                if (letters[i] == ltr) {
                    return degrees[i];
                }
            }
            return -100;
        }

        public String getLatZone(final double latitude) {
            int latIndex = -2;
            final int lat = (int) latitude;

            if (lat >= 0) {
                final int len = posLetters.length;
                for (int i = 0; i < len; i++) {
                    if (lat == posDegrees[i]) {
                        latIndex = i;
                        break;
                    }

                    if (lat > posDegrees[i]) {
                        continue;
                    }
                    else {
                        latIndex = i - 1;
                        break;
                    }
                }
            }
            else {
                final int len = negLetters.length;
                for (int i = 0; i < len; i++) {
                    if (lat == negDegrees[i]) {
                        latIndex = i;
                        break;
                    }

                    if (lat < negDegrees[i]) {
                        latIndex = i - 1;
                        break;
                    }
                    else {
                        continue;
                    }

                }

            }

            if (latIndex == -1) {
                latIndex = 0;
            }
            if (lat >= 0) {
                if (latIndex == -2) {
                    latIndex = posLetters.length - 1;
                }
                return String.valueOf(posLetters[latIndex]);
            }
            else {
                if (latIndex == -2) {
                    latIndex = negLetters.length - 1;
                }
                return String.valueOf(negLetters[latIndex]);

            }
        }
    }

    /**
     * Converts a double representation of a coordinate with decimal degrees
     * into a string representation. There are string syntaxes supported are the
     * same as for the convert(String) method. The implementation shall provide
     * as many significant digits for the decimal fractions as are allowed by
     * the string syntax definition.
     * 
     * @param coordinate
     *            a double reprentation of a coordinate
     * @param outputType
     *            identifier of the type of the string representation wanted for
     *            output The constant DD_MM_SS identifies the syntax 1 and the
     *            constant DD_MM identifies the syntax 2.
     * @return a string representation of the coordinate in a representation
     *         indicated by the parameter
     * @throws java.lang.IllegalArgumentException
     *             if the outputType is not one of the two costant values
     *             defined in this class or if the coordinate value is not
     *             within the range [-180.0, 180.0) or is Double.NaN
     * @see #convert(string)
     */
    /**
     * Identifier for string coordinate representation Degrees, Minutes, decimal
     * fractions of a minute.
     */
    private static final DecimalFormat threePlaces = new DecimalFormat("00.000"); // DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    private static final DecimalFormat fourPlaces  = new DecimalFormat("00.0000"); // DecimalFormatSymbols.getInstance(Locale.ENGLISH))
    public static final int            DD_MM       = 1;
    public static final int            DD_MM_SS    = 2;

    public static String convert(final double coordinate, final int outputType, final boolean islatitude) {

        String rc = "";

        int deg = (int) Math.floor(Math.abs(coordinate));

        String dg = Integer.toString(Math.abs(deg));

        if (dg.length() < 2) {
            dg = "0" + dg;
        }
        if ((!islatitude) && (dg.length() < 3)) {
            dg = "0" + dg;
        }

        if (islatitude) {
            rc = (coordinate < 0) ? "S " : "N ";
        }
        else {
            rc = (coordinate > 0) ? "E " : "W ";
        }

        deg = Math.abs(deg);

        final float min = (float) ((Math.abs(coordinate) - deg) * 60);

        if (outputType == GeoMathUtil.DD_MM) {

            String mn = GeoMathUtil.threePlaces.format(min).replace(',', '.');

            while (mn.length() < 6) {
                mn += "0";
            }

            rc += dg + "° " + mn;

        }
        else if (outputType == GeoMathUtil.DD_MM_SS) {

            final int rmin = (int) min;

            float sec = (min * 100) - (rmin * 100);
            sec = min - rmin;
            sec *= 60;

            String secstr = GeoMathUtil.fourPlaces.format(sec).replace(',', '.');

            while (secstr.length() < 7) {
                secstr += "0";
            }

            String minstr = "" + rmin;

            if (minstr.length() < 2) {
                minstr = "0" + minstr;
            }

            rc += deg + "° " + minstr + "' " + secstr + "\"";
        }
        else {
            throw new IllegalArgumentException();
        }
        return rc;
    }

    /**
     * Calculates the azimuth between the two points according to the ellipsoid
     * model of WGS84. The azimuth is relative to true north. The Coordinate
     * object on which this method is called is considered the origin for the
     * calculation and the Coordinate object passed as a parameter is the
     * destination which the azimuth is calculated to. When the origin is the
     * North pole and the destination is not the North pole, this method returns
     * 180.0. When the origin is the South pole and the destination is not the
     * South pole, this method returns 0.0. If the origin is equal to the
     * destination, this method returns 0.0. The implementation shall calculate
     * the result as exactly as it can. However, it is required that the result
     * is within 1 degree of the correct result.
     * 
     * @param from
     * @param to
     *            the Coordinate of the destination
     * @return the azimuth to the destination in degrees. Result is within the
     *         range [0.0 ,360.0).
     * @throws java.lang.NullPointerException
     *             if the parameter is null
     */
    public static double azimuthTo(final GeoPoint from, final GeoPoint to) {

        final double lat1 = from.getLatitude();
        final double lon1 = from.getLongitude();
        final double lat2 = to.getLatitude();
        final double lon2 = to.getLongitude();

        return azimuthTo(lat1, lon1, lat2, lon2);
    }

    public static double azimuthTo(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double dtor = Math.PI / 180.0;
        final double rtod = 180.0 / Math.PI;
        final double distance = rtod
                * Math.acos((Math.sin(lat1 * dtor) * Math.sin(lat2 * dtor)) + (Math.cos(lat1 * dtor) * Math.cos(lat2 * dtor) * Math.cos((lon2 - lon1) * dtor)));
        final double cosAzimuth = ((Math.cos(lat1 * dtor) * Math.sin(lat2 * dtor)) - (Math.sin(lat1 * dtor) * Math.cos(lat2 * dtor) * Math.cos((lat2 - lon1)
                * dtor)))
                / Math.sin(distance * dtor);
        final double sinAzimuth = (Math.cos(lat2 * dtor) * Math.sin((lat2 - lon1) * dtor)) / Math.sin(distance * dtor);

        return (rtod * Math.atan2(sinAzimuth, cosAzimuth));
    }

    /**
     * Calculates the geodetic distance between the two points according to the
     * ellipsoid model of WGS84. Altitude is neglected from calculations.
     * <p/>
     * The implementation shall calculate this as exactly as it can. However, it
     * is required that the result is within 0.35% of the correct result.
     * 
     * @param from
     * @param to
     *            the Coordinate of the destination
     * @return the distance to the destination in meters
     * @throws java.lang.NullPointerException
     *             if the parameter is null
     */
    public static float distance(final GeoPoint from, final GeoPoint to) {

        return from.distanceTo(to);

        // /*
        // * Haversine Formula (from R.W. Sinnott, "Virtues of the Haversine",
        // Sky
        // * and Telescope, vol. 68, no. 2, 1984, p. 159):
        // *
        // * See the following URL for more info on calculating distances:
        // * http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1
        // */
        //
        // double earthRadius = 6371; // km
        // double lat1 = to.getLatitudeInRadians();
        // double lon1 = to.getLongitudeInRadians();
        // double lat2 = from.getLatitudeInRadians();
        // double lon2 = from.getLongitudeInRadians();
        // double dlon = (lon2 - lon1);
        // double dlat = (lat2 - lat1);
        // double a = (Math.sin(dlat / 2)) * (Math.sin(dlat / 2)) +
        // (Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon / 2))) *
        // (Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon / 2)));
        // double c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)));
        // double km = earthRadius * c;
        //
        // return (float) (km * 1000);

    }

    public static String getHumanDistanceString(final double distance, int conversionIx) {
        double distanceNew = convertDistance(distance, conversionIx);

        if ((conversionIx == 0) && (distanceNew > 1000)) {
            distanceNew /= 1000;
            conversionIx++;
        }

        return String.format(Locale.getDefault(), "%.2f", distanceNew)
                + " "
                + CoreInfoHandler.getInstance().getMainActivity().getResources().getStringArray(R.array.distance_unit_title)[conversionIx];
    }

    public static final DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");

    public static String distanceAsString(final GeoPoint from, final GeoPoint to, final boolean meters) {
        float distance = distance(from, to);

        if (!meters) {
            distance /= 1000;
        }

        return GeoMathUtil.twoDecimalFormat.format(distance) + ((meters) ? " m" : " km");
    }

    // calc Bearing using CacheWolf
    // Stringify using cachHound
    private final static String Bearings[] = { "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW" };

    /**
     * Erstellt ein Bearing-Objekt aus einer Winkelangabe.
     * 
     * @param d
     *            Der Winkel
     * @return
     */
    public static String getBearingFromDeg(final double d) {
        if ((d > 360.5) || (d < -0.5)) {
            return GeoMathUtil.Bearings[0];
        }

        int i = 1;
        int ix = 0;
        while (ix < GeoMathUtil.Bearings.length) {
            // for (Bearing b : Bearing.values()) {
            if (d < ((360.0d * i) / (GeoMathUtil.Bearings.length * 2))) {
                return GeoMathUtil.Bearings[ix];

            }
            i += 2;
            ix++;
        }
        return GeoMathUtil.Bearings[0];

    }

    /**
     * geodetic Coordinate are to be defined in WGS84 and will get converted to
     * NAD27
     * 
     * @param lat_wgs
     * @param lon_wgs
     * @return
     */
    public static double[] toNAD27(final double lat_wgs, final double lon_wgs) {
        final double latlon[] = new double[2];

        // --- parameter
        final double a = 6378137.000;
        final double fq = 3.35281066e-3;
        final double f = fq + 3.726464e-5;
        final int dx = 8;
        final int dy = -160;
        final int dz = -176;
        final double e2q = ((2 * fq) - (fq * fq));
        final double e2 = ((2 * f) - (f * f));
        final double pi = Math.PI;
        // ---
        final double lat1 = lat_wgs * (pi / 180);
        final double lon1 = lon_wgs * (pi / 180);
        final double nd = a / Math.sqrt(1 - (e2q * Math.sin(lat1) * Math.sin(lat1)));
        final double xw = nd * Math.cos(lat1) * Math.cos(lon1);
        final double yw = nd * Math.cos(lat1) * Math.sin(lon1);
        final double zw = (1 - e2q) * nd * Math.sin(lat1);
        final double x = xw + dx;
        final double y = yw + dy;
        final double z = zw + dz;
        final double rb = Math.sqrt((x * x) + (y * y));
        final double lat2 = (180 / pi) * Math.atan((z / rb) / (1 - e2));
        double lon2 = lon1;
        if (x > 0) {
            lon2 = (180 / pi) * Math.atan(y / x);
        }
        if ((x < 0) && (y > 0)) {
            lon2 = ((180 / pi) * Math.atan(y / x)) + 180;
        }
        if ((x < 0) && (y < 0)) {
            lon2 = ((180 / pi) * Math.atan(y / x)) - 180;
        }

        latlon[0] = lat2;
        latlon[1] = lon2;

        return latlon;

    }

    public static double[] toWGS84(final double lat_nad, final double lon_nad) {
        final double latlon[] = new double[2];

        final double a = 6378137.000 + 69.4;
        final double fq = 3.35281066e-3 + 3.726464e-05;
        final double f = 3.35281066e-3;
        final int dx = -8;
        final int dy = 160;
        final int dz = 176;
        final double e2q = ((2 * fq) - (fq * fq));
        final double e2 = ((2 * f) - (f * f));
        final double pi = Math.PI;
        final double b1 = lat_nad * (pi / 180);
        final double l1 = lon_nad * (pi / 180);
        final double nd = a / Math.sqrt(1 - (e2q * Math.sin(b1) * Math.sin(b1)));
        final double xp = nd * Math.cos(b1) * Math.cos(l1);
        final double yp = nd * Math.cos(b1) * Math.sin(l1);
        final double zp = (1 - e2q) * nd * Math.sin(b1);
        final double x = xp + dx;
        final double y = yp + dy;
        final double z = zp + dz;
        final double rb = Math.sqrt((x * x) + (y * y));
        final double lat2 = (180 / pi) * Math.atan((z / rb) / (1 - e2));
        double lon2 = lon_nad;
        if (x > 0) {
            lon2 = (180 / pi) * Math.atan(y / x);
        }
        if ((x < 0) && (y > 0)) {
            lon2 = ((180 / pi) * Math.atan(y / x)) + 180;
        }
        if ((x < 0) && (y < 0)) {
            lon2 = ((180 / pi) * Math.atan(y / x)) - 180;
        }

        latlon[0] = lat2;
        latlon[1] = lon2;

        return latlon;

    }

    public static String toUTM(final double lat, final double lon) {
        String utm = "";
        try {
            // --- local parameter
            final double a = 6378137.000;
            final double f = 3.35281068e-3;
            final double pi = Math.PI;
            final String b_sel = "CDEFGHJKLMNPQRSTUVWXX";
            final double c = a / (1 - f);
            // ---
            final double ex2 = ((2 * f) - (f * f)) / ((1 - f) * (1 - f));
            final double ex4 = ex2 * ex2;
            final double ex6 = ex4 * ex2;
            final double ex8 = ex4 * ex4;
            final double e0 = c * (pi / 180) * ((((1 - ((3 * ex2) / 4)) + ((45 * ex4) / 64)) - ((175 * ex6) / 256)) + ((11025 * ex8) / 16384));
            final double e2 = c * (((((-3 * ex2) / 8) + ((15 * ex4) / 32)) - ((525 * ex6) / 1024)) + ((2205 * ex8) / 4096));
            final double e4 = c * ((((15 * ex4) / 256) - ((105 * ex6) / 1024)) + ((2205 * ex8) / 16384));
            final double e6 = c * (((-35 * ex6) / 3072) + ((315 * ex8) / 12288));
            final int lzn = (int) (((lon + 180) / 6) + 1.10);
            String lz = "" + lzn;
            if (lzn < 10) {
                lz = "0" + lzn;
            }
            final int bd = (int) (1 + ((lat + 80) / 8.10));
            final String bz = b_sel.substring(bd - 1, bd);
            final double br = (lat * pi) / 180;
            final double tan1 = Math.tan(br);
            final double tan2 = tan1 * tan1;
            final double tan4 = tan2 * tan2;
            final double cos1 = Math.cos(br);
            final double cos2 = cos1 * cos1;
            final double cos4 = cos2 * cos2;
            final double cos3 = cos2 * cos1;
            final double cos5 = cos4 * cos1;
            final double etasq = ex2 * cos2;
            final double nd = c / Math.sqrt(1 + etasq);
            final double g = (e0 * lat) + (e2 * Math.sin(2 * br)) + (e4 * Math.sin(4 * br)) + (e6 * Math.sin(6 * br));
            final double lh = ((lzn - 30) * 6) - 3;
            final double dl = ((lon - lh) * pi) / 180;
            final double dl2 = dl * dl;
            final double dl4 = dl2 * dl2;
            final double dl3 = dl2 * dl;
            final double dl5 = dl4 * dl;
            double nres = 0;

            if (lat < 0) {
                nres = 10e6 + (0.9996 * (g + ((nd * cos2 * tan1 * dl2) / 2) + ((nd * cos4 * tan1 * ((5 - tan2) + (9 * etasq)) * dl4) / 24)));
            }
            else {
                nres = 0.9996 * (g + ((nd * cos2 * tan1 * dl2) / 2) + ((nd * cos4 * tan1 * ((5 - tan2) + (9 * etasq)) * dl4) / 24));
            }
            final double eres = (0.9996 * ((nd * cos1 * dl) + ((nd * cos3 * ((1 - tan2) + etasq) * dl3) / 6) + ((nd * cos5 * ((5 - (18 * tan2)) + tan4) * dl5) / 120))) + 500000;
            final String zone = lz + bz;

            utm = zone + " " + GeoMathUtil.threePlaces.format(eres).replace(',', '.') + " " + GeoMathUtil.threePlaces.format(nres).replace(',', '.');
        }
        catch (final Exception x) {

            Ut.e("UTM conversion for " + lat + ":" + lon + " failed");

        }
        return utm;

    }

    public static double[] UTMtoLatLon(final String utm) {
        final double latlon[] = new double[2];

        final String b_sel = "CDEFGHJKLMNPQRSTUVWXX";

        final String[] utm1 = utm.split(" ");
        final double zone = Integer.parseInt(utm1[0].substring(0, utm1[0].length() - 1));
        @SuppressWarnings("unused")
        final String latZone = utm1[0];
        final double easting = Double.parseDouble(utm1[1]);
        final double northing = Double.parseDouble(utm1[2]);

        final String band = utm.substring(utm1[0].length() - 1, utm1[0].length());
        // zone = parseFloat(zone);
        // ew = parseFloat(ew);
        final double a = 6378137.000;
        final double f = 3.35281068e-3;
        final double pi = Math.PI;
        final double c = a / (1 - f);
        final double ex2 = ((2 * f) - (f * f)) / ((1 - f) * (1 - f));
        final double ex4 = ex2 * ex2;
        final double ex6 = ex4 * ex2;
        final double ex8 = ex4 * ex4;
        final double e0 = c * (pi / 180) * ((((1 - ((3 * ex2) / 4)) + ((45 * ex4) / 64)) - ((175 * ex6) / 256)) + ((11025 * ex8) / 16384));
        final double f2 = (180 / pi) * (((((3 * ex2) / 8) - ((3 * ex4) / 16)) + ((213 * ex6) / 2048)) - ((255 * ex8) / 4096));
        final double f4 = (180 / pi) * ((((21 * ex4) / 256) - ((21 * ex6) / 256)) + ((533 * ex8) / 8192));
        final double f6 = (180 / pi) * (((151 * ex6) / 6144) - ((453 * ex8) / 12288));

        double m_nw = northing;

        if ((band.length() < 1) || (b_sel.indexOf(band) >= b_sel.indexOf("N"))) {
            m_nw = northing;
        }
        else {
            m_nw = northing - 10e6;
        }
        final double sigma = (m_nw / 0.9996) / e0;
        final double sigmr = (sigma * pi) / 180;
        final double bf = sigma + (f2 * Math.sin(2 * sigmr)) + (f4 * Math.sin(4 * sigmr)) + (f6 * Math.sin(6 * sigmr));
        final double br = (bf * pi) / 180;
        final double tan1 = Math.tan(br);
        final double tan2 = tan1 * tan1;
        final double tan4 = tan2 * tan2;
        final double cos1 = Math.cos(br);
        final double cos2 = cos1 * cos1;
        final double etasq = ex2 * cos2;
        final double nd = c / Math.sqrt(1 + etasq);
        final double nd2 = nd * nd;
        final double nd4 = nd2 * nd2;
        final double nd6 = nd4 * nd2;
        final double nd3 = nd2 * nd;
        final double nd5 = nd4 * nd;
        final double lh = ((zone - 30) * 6) - 3;
        final double dy = (easting - 500000) / 0.9996;
        final double dy2 = dy * dy;
        final double dy4 = dy2 * dy2;
        final double dy3 = dy2 * dy;
        final double dy5 = dy3 * dy2;
        final double dy6 = dy3 * dy3;
        final double b2 = (-tan1 * (1 + etasq)) / (2 * nd2);
        final double b4 = (tan1 * (5 + (3 * tan2) + (6 * etasq * (1 - tan2)))) / (24 * nd4);
        final double b6 = (-tan1 * (61 + (90 * tan2) + (45 * tan4))) / (720 * nd6);
        final double l1 = 1 / (nd * cos1);
        final double l3 = -(1 + (2 * tan2) + etasq) / (6 * nd3 * cos1);
        final double l5 = (5 + (28 * tan2) + (24 * tan4)) / (120 * nd5 * cos1);
        latlon[0] = bf + ((180 / pi) * ((b2 * dy2) + (b4 * dy4) + (b6 * dy6)));
        latlon[1] = lh + ((180 / pi) * ((l1 * dy) + (l3 * dy3) + (l5 * dy5)));

        return latlon;
    }

    /**
     * input expected to be in correct
     * 
     * @param latitude
     * @param longitude
     * @param formatId
     * @param datumId
     * @return
     */
    public static String formatCoordinate(final double latitude, final double longitude, final int formatId) {
        String formattedCoords = "";
        switch (formatId) {
            case FORMAT_DEZIMAL:
                formattedCoords = GeoMathUtil.threePlaces.format(latitude).replace(',', '.') + "  "
                        + GeoMathUtil.threePlaces.format(longitude).replace(',', '.');
                break;
            case FORMAT_DM:
                formattedCoords = convert(latitude, GeoMathUtil.DD_MM, true) + "  " + convert(longitude, GeoMathUtil.DD_MM, false);
                break;
            case FORMAT_DMS:
                formattedCoords = convert(latitude, GeoMathUtil.DD_MM_SS, true) + "  " + convert(longitude, GeoMathUtil.DD_MM_SS, false);
                break;
            case FORMAT_UTM:
                formattedCoords = toUTM(latitude, longitude);
                break;
            case FORMAT_MGRS:
                formattedCoords = getInstance().latLon2MGRUTM(latitude, longitude);
                break;
            case FORMAT_UTMNAD27:
                double[] latlon = toNAD27(latitude, longitude);
                formattedCoords = toUTM(latlon[0], latlon[1]);
                break;
            case FORMAT_MGRSNAD27:
                latlon = toNAD27(latitude, longitude);
                formattedCoords = getInstance().latLon2MGRUTM(latlon[0], latlon[1]);
                break;
            case FORMAT_GEOHASH:
                formattedCoords = GeoHash.encode(latitude, longitude);
            default:
                break;
        }
        return formattedCoords;
    }

    public static double gudermannInverse(final double aLatitude) {
        return Math.log(Math.tan(GeoMathUtil.PI_4 + ((GeoMathUtil.DEG2RAD * aLatitude) / 2)));
    }

    public static double gudermann(final double y) {
        return GeoMathUtil.RAD2DEG * Math.atan(Math.sinh(y));
    }

    public static int mod(int number, final int modulus) {
        if (number > 0) {
            return number % modulus;
        }

        while (number < 0) {
            number += modulus;
        }

        return number;
    }

    private static GeoMathUtil getInstance() {
        if (GeoMathUtil.instance == null) {
            GeoMathUtil.instance = new GeoMathUtil();
        }
        return GeoMathUtil.instance;
    }

    /**
     * @param a
     * @param b
     * @return m/s
     */
    public static double speed(final TrackPoint a, final TrackPoint b) {
        double speed = 0.0;
        final double distance = distance(a, b); // * 1000; // ???

        long date1 = 0, date2 = 0;

        if (a.date != null) {
            date1 = a.date.getTime();
        }
        if (b.date != null) {
            date2 = b.date.getTime();
        }
        final double difftime = (date2 - date1); // / 1000;  // seconds

        if (difftime != 0) {
            speed = Math.abs(distance / difftime);
        }
        return speed;
    }

    public static double avgSpeed(final Collection<TrackPoint> waypoints) {
        TrackPoint lastPoint = null;
        double speed = 0;
        for (final TrackPoint currentPoint : waypoints) {
            if (lastPoint != null) {
                speed += speed(lastPoint, currentPoint);
            }
            lastPoint = currentPoint;
        }
        return (waypoints.size() > 0) ? speed / waypoints.size() : 0.0;
    }

    public static double distance(final Collection<? extends GeoPoint> waypoints) {
        GeoPoint lastPoint = null;
        double distance = 0;
        for (final GeoPoint currentPoint : waypoints) {
            if (lastPoint != null) {
                distance += distance(lastPoint, currentPoint);
            }
            lastPoint = currentPoint;
        }
        return distance;
    }

    // track optimization for visualization !
    /**
     * Reduces the number of trackpoints in GPS tracks preserving the shape of
     * the three-dimensional trajectory as much as possible. More precisely, the
     * goal is to keep the area between the original and the reduced tracks as
     * small as possible. The algorithm that I use here will not generally
     * achieve this global goal but breaks it down to a local goal in order to
     * decide which points to remove and which to keep.
     * <p>
     * Endpoints of a track segment are never removed. For each inner point p,
     * the area of the triangle spanned by this point and its neighbours is
     * calculated. This is exactly the cost in terms of area between the
     * original and the reduced (linearly interpolated) tracks if you remove
     * just that single point. We call this area the weight w(p) of the point p.
     * The algorithm proceeds by calculating the threshold weight wmin for which
     * #{p | w(p) >= wmin} >= n and #{p | w(p) > wmin} < n , where n is the
     * target number of track points. Now, the algorithm runs once over all
     * inner points looking for points p[i] with w(p[i]) <= wmin whose
     * neighbours p[i+1] and p[i - 1] have larger weights. These points are
     * removed. The additional condition that w(p[i]) must have a local minimum
     * at i will in general prevent some points with w(p) <= wmin from being
     * removed, and the target number of track points will in general not be
     * achieved in the first pass. Therefore, the weights of the points whose
     * neighbours have been removed are updated and the procedure is repeated
     * until the target number of track points has been reached.
     * <p>
     * Convergence is assured by the fact that w is infinite at the end points
     * of each track segment and finite at the inner points, therefore w has a
     * global minimum at one of the inner points (as long as there are inner
     * points), and therefore at least one point is removed in each pass.
     * 
     * @author Moritz Ringler
     * @param track
     * @param numpoints
     * @see #setPerTrack
     */
    public static void shrinkTrack(final List<TrackPoint> trackpoints, final int numpoints) {
        if (numpoints < 1) {
            throw new IllegalArgumentException();
        }

        if (numpoints < 1) {
            throw new IllegalArgumentException();
        }

        // calculate weight of all points
        final Map<TrackPoint, Double> weights = new HashMap<TrackPoint, Double>();
        {
            int i = 0;
            for (final TrackPoint trackpoint : trackpoints) {
                weights.put(trackpoint, weight(trackpoints, i));
                i++;
            }
        }

        final List<Double> weightValues = new ArrayList<Double>(weights.values());

        int ntotal = weightValues.size();
        // message(ntotal + " points");

        while (ntotal > numpoints) {
            // calculate minimum weight of points to keep
            Collections.sort(weightValues);
            final int mindx = ntotal - 1 - numpoints;
            int ntrkpt = ntotal;
            final double minWeight = weightValues.get(mindx);

            final List<TrackPoint> toDelete = new ArrayList<TrackPoint>();

            // remove points whose weight is less than minWeight

            final List<TrackPoint> waypoints = trackpoints;

            // must decrement !!!
            for (int i = waypoints.size() - 2; i > 0; i--) {
                double w = 0;
                TrackPoint wpt = null;
                try {
                    wpt = waypoints.get(i);
                    w = weights.get(wpt);
                    // System.out.println("get i = " + i);
                }
                catch (final Exception x) {
                    System.out.println(x.toString() + " i = " + i);
                }
                // is weight below threshold?
                if (w <= minWeight) {

                    // Look for neighbour with lower weight
                    for (double w2 = weights.get(waypoints.get(i - 1)); w2 < w;) {
                        i--;
                        w = w2;
                        w2 = weights.get(waypoints.get(i - 1));
                    }

                    // remove trkpt and check success
                    // trackSegment.getTrkpt().remove(waypoints.get(i));
                    toDelete.add(waypoints.get(i));
                    // throw new Error("Error removing Trkpt.");

                    ntrkpt--;
                    if (ntrkpt == numpoints) {
                        break;
                    }

                    // update weights
                    final double dd = weights.remove(waypoints.get(i));
                    assert (w == dd);
                    if (i < (waypoints.size() - 2)) {
                        weights.put(waypoints.get(i + 1), area(waypoints.get(i + 1), waypoints.get(i + 2), waypoints.get(i - 1)));
                    }
                    if (i > 1) {
                        weights.put(waypoints.get(i - 1), area(waypoints.get(i - 1), waypoints.get(i + 1), waypoints.get(i - 2)));
                    }

                    // skip next point lest we assign a weight to the
                    // point we have just removed when updating weights
                    i--;
                }
            }

            for (final TrackPoint wp : toDelete) {
                trackpoints.remove(wp);
            }

            weightValues.clear();
            weightValues.addAll(weights.values());
            assert (ntrkpt == weightValues.size());
            if (ntrkpt >= ntotal) {
                String message = "ntrkpt: " + ntrkpt + "\n";
                message += "ntotal: " + ntotal + "\n";
                message += "minWeight: " + minWeight + "\n";
                throw new Error("Not converging. This is a bug.\n" + message);
            }
            ntotal = ntrkpt;
            // message(ntotal + " points after pass " + numiter++);
        }
    }

    private static double weight(final List<TrackPoint> waypoints, final int i) {
        if ((i == 0) || (i == (waypoints.size() - 1))) {
            return Double.POSITIVE_INFINITY;
        }
        else {
            return area(waypoints.get(i), waypoints.get(i - 1), waypoints.get(i + 1));
        }
    }

    private static double area(final TrackPoint p1, final TrackPoint p2, final TrackPoint p3) {
        final boolean threed = (p1.alt > 0) && (p2.alt > 0) && (p3.alt > 0);

        final double d12 = distance(p1, p2, threed);
        final double d23 = distance(p2, p3, threed);
        final double d13 = distance(p1, p3, threed);
        return triangleArea(d12, d23, d13);
    }

    /**
     * Calculates triangle area using the formula given in <a
     * href="http://http.cs.berkeley.edu/~wkahan/Triangle.pdf"
     * >http://http.cs.berkeley.edu/~wkahan/Triangle.pdf</a> page 4.
     * 
     * @param s1
     * @param s2
     * @param s3
     */
    private static double triangleArea(final double s1, final double s2, final double s3) {
        final double[] cba = new double[] { Math.abs(s1), Math.abs(s2), Math.abs(s3) };
        Arrays.sort(cba);
        final double a = cba[2];
        final double b = cba[1];
        final double c = cba[0];
        final double diff = c - (a - b);
        if (diff < 0) {
            if ((-diff / a) < GeoMathUtil.PRECISION) {
                // the minus sign is probably not significant
                // and we return 0
                return 0.0;
            }
            // System.err.println(-diff / a);
            throw new IllegalArgumentException("triangleArea(" + s1 + "," + s2 + "," + s3 + ")\n" + "The sum of the length of the two smaller "
                    + "sides of the triangle is not greater than the length of the " + "third side. This is not a triangle!");
        }
        final double sqrtarg = (a + (b + c)) * (c - (a - b)) * (c + (a - b)) * (a + (b - c));
        return 0.25 * Math.sqrt(sqrtarg);
    }

    /**
     * Sets the relative weight of the third dimension (elevation) in
     * determining which points to keep and which to remove. The algorithm used
     * in shrink tries to keep the distance between the original and the reduced
     * track as small as possible. Elevation (z) usually varies much less than
     * the horizontal location (x and y). Therefore, if z is measured in the
     * same units as x and y its influence on this distance will usually be
     * negligible. With this method you can give z a larger weight than x and y
     * so that the reduced track data will more faithfully reproduce the
     * original altitude profile (and less faithfully reproduce the original
     * horizontal trajectory).
     * 
     * @param f
     */
    public void setSuperelevation(final float f) {
        if (f < 0) {
            throw new IllegalArgumentException();
        }
        GeoMathUtil.superelevation = f;
    }

    public static double straightKmDistance(final TrackPoint w1, final TrackPoint w2) {

        final double dGC = distanceTo(w1.getLatitude(), w1.getLongitude(), w2.getLatitude(), w2.getLongitude()) / 1000;
        return 2 * GeoMathUtil.EARTH_RADIUS * Math.sin(dGC / 2 / GeoMathUtil.EARTH_RADIUS);
    }

    private static double distance(final TrackPoint w1, final TrackPoint w2, final boolean threed) {
        // probably we should use straight line distance here, not geodetic
        // but then again the difference will usually not be large
        double dd = straightKmDistance(w1, w2);
        if (threed) {
            final double dz = ((w1.alt - w2.alt) * GeoMathUtil.superelevation) / 1000;
            dd = Math.sqrt((dd * dd) + (dz * dz));
        }
        return dd;
    }

    /*
     * Return an array of minimum and maximum coordinates (a coordinate ==
     * latitude and longitude in degrees), such that latDeg and lonDeg are at
     * their center, with distFrom distance to the edges of a bounding box.
     * Based on code by Philip Matuschek, September 2010 at
     * http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates
     */
    public static double[] boundingBoxCoords(final double latDeg, final double lonDeg, double distFrom) {
        final double lat = Math.toRadians(latDeg);
        final double lon = Math.toRadians(lonDeg);

        if (distFrom < 0) {
            System.out.println("Distance from cannot be negative");
            distFrom = -1 * distFrom;
        }

        // angular value for distFrom in radians on a great circle
        final double radDist = distFrom / GeoMathUtil.EARTH_RADIUS;

        // minimum and maximum latitudes
        double minLat = lat - radDist;
        double maxLat = lat + radDist;

        double minLon, maxLon; // minimum and maximum longitudes
        if ((minLat > GeoMathUtil.MIN_LAT) && (maxLat < GeoMathUtil.MAX_LAT)) {
            final double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(lat));

            minLon = lon - deltaLon;
            if (minLon < GeoMathUtil.MIN_LON) {
                minLon += 2 * Math.PI;
            }

            maxLon = lon + deltaLon;
            if (maxLon > GeoMathUtil.MAX_LON) {
                maxLon -= 2 * Math.PI;
            }
        }
        else { // a pole is within the distFrom distance
            minLat = Math.max(minLat, GeoMathUtil.MIN_LAT);
            maxLat = Math.min(maxLat, GeoMathUtil.MAX_LAT);
            minLon = GeoMathUtil.MIN_LON;
            maxLon = GeoMathUtil.MAX_LON;
        }

        // convert coordinates back to degrees, and return as an array
        return new double[] { Math.toDegrees(minLat), Math.toDegrees(minLon), Math.toDegrees(maxLat), Math.toDegrees(maxLon) };
    } // end of boundingBoxCoords()

    /*
     * return the shortest distance over the Earth's surface between degree
     * (lat1,long1) and (lat2,long2) using the Haversine formula (details at
     * http://en.wikipedia.org/wiki/Haversine_formula and
     * http://mathforum.org/library/drmath/view/51879.html )
     */
    public static double distanceApart(final double lat1, final double long1, final double lat2, final double long2) {
        // convert latitude and longitudes to radians
        final double diffLat = Math.toRadians(lat2 - lat1);
        final double diffLong = Math.toRadians(long2 - long1);
        final double h = haversin(diffLat) + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * haversin(diffLong));
        final double dist = 2.0 * GeoMathUtil.EARTH_RADIUS * Math.asin(Math.sqrt(h));
        return dist;
    } // end of distanceApart()

    private static double haversin(final double angle) // angle is in radians
    {
        return Math.sin(angle / 2) * Math.sin(angle / 2);
    }

    public static double convertSpeed(final double speedInMS, final int conversionIx) {
        final double newSpeed = (int) (speedInMS * GeoMathUtil.speedConversionFactors[conversionIx]);
        return newSpeed;
    }

    public static double convertDistance(final double meters, final int conversionIx) {
        final double newDistance = (int) (meters * GeoMathUtil.distanceConversionFactors[conversionIx]);
        return newDistance;
    }

    public static String formatElapsedTime(long millis) {

        long elapsedTime = millis / 1000;
        String time = "unknown";
        try {
            time = String.format("%02d:%02d:%02d", (int) (elapsedTime / 3600),

            (int) ((elapsedTime % 3600) / 60), (int) (elapsedTime % 60));
        }
        catch (Exception x) {
            Ut.e("problem formatting time");
        }
        return time;
    }

}
