package at.the.gogo.gpxviewer.util;

import java.util.logging.Logger;

public class GeoMath {

	private static final double EARTH_RADIUS = 6367450; // in meters (geometric
														// mean)
	private static final double MIN_LAT = Math.toRadians(-90); // -PI/2
	private static final double MAX_LAT = Math.toRadians(90); // PI/2
	private static final double MIN_LON = Math.toRadians(-180); // -PI
	private static final double MAX_LON = Math.toRadians(180); // PI

	public GeoMath() {
	}

	public static double distanceApart(double lat1, double long1, double lat2,
			double long2) /*
						 * return the shortest distance over the Earth's surface
						 * between degree (lat1,long1) and (lat2,long2) using
						 * the Haversine formula (details at
						 * http://en.wikipedia.org/wiki/Haversine_formula and
						 * http://mathforum.org/library/drmath/view/51879.html )
						 */{
		// convert latitude and longitudes to radians
		double diffLat = Math.toRadians(lat2 - lat1);
		double diffLong = Math.toRadians(long2 - long1);
		double h = haversin(diffLat)
				+ (Math.cos(Math.toRadians(lat1))
						* Math.cos(Math.toRadians(lat2)) * haversin(diffLong));
		double dist = 2.0 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
		return dist;
	} // end of distanceApart()

	private static double haversin(double angle) // angle is in radians
	{
		return Math.sin(angle / 2) * Math.sin(angle / 2);
	}

	public static double[] boundingBoxCoords(double latDeg, double lonDeg,
			double distFrom) /*
							 * Return an array of minimum and maximum
							 * coordinates (a coordinate == latitude and
							 * longitude in degrees), such that latDeg and
							 * lonDeg are at their center, with distFrom
							 * distance to the edges of a bounding box.
							 * 
							 * Based on code by Philip Matuschek, September 2010
							 * at http://janmatuschek.de/
							 * LatitudeLongitudeBoundingCoordinates
							 */{
		double lat = Math.toRadians(latDeg);
		double lon = Math.toRadians(lonDeg);

		if (distFrom < 0) {
			System.out.println("Distance from cannot be negative");
			distFrom = -1 * distFrom;
		}

		// angular value for distFrom in radians on a great circle
		double radDist = distFrom / EARTH_RADIUS;

		// minimum and maximum latitudes
		double minLat = lat - radDist;
		double maxLat = lat + radDist;

		double minLon, maxLon; // minimum and maximum longitudes
		if ((minLat > MIN_LAT) && (maxLat < MAX_LAT)) {
			double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(lat));

			minLon = lon - deltaLon;
			if (minLon < MIN_LON) {
				minLon += 2 * Math.PI;
			}

			maxLon = lon + deltaLon;
			if (maxLon > MAX_LON) {
				maxLon -= 2 * Math.PI;
			}
		} else { // a pole is within the distFrom distance
			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLon = MIN_LON;
			maxLon = MAX_LON;
		}

		// convert coordinates back to degrees, and return as an array
		return new double[] { Math.toDegrees(minLat), Math.toDegrees(minLon),
				Math.toDegrees(maxLat), Math.toDegrees(maxLon) };
	} // end of boundingBoxCoords()

	/*
	 * conversion code from "Coordinate conversions made easy", Sami Salkosuo
	 * http://www.ibm.com/developerworks/java/library/j-coordconvert/ and
	 * http://www.neptuneandco.com/~jtauxe/bits/LatLonConvert.java
	 */
	public static double DMSToDD(int degrees, int minutes, int seconds, char dir) /*
																				 * convert
																				 * degrees
																				 * /
																				 * minutes
																				 * /
																				 * seconds
																				 * to
																				 * decimal
																				 * degrees
																				 * e
																				 * .
																				 * g
																				 * .
																				 * 36
																				 * degs
																				 * 57
																				 * '
																				 * 9
																				 * "
																				 * N
																				 * to
																				 * 36.9525000
																				 * 110
																				 * degs
																				 * 4
																				 * '
																				 * 21
																				 * "
																				 * W
																				 * to
																				 * -
																				 * 110.0725000
																				 */{
		double dd = degrees + minutes / 60.0 + seconds / 3600.0;
		if ((dir == 'S') || (dir == 's') || (dir == 'W') || (dir == 'w')) // don't
																			// do
																			// anything
																			// for
																			// N
																			// or
																			// E
		{
			dd *= -1;
		}
		return dd;
	} // end of DMSToDD()

	public static int[] DDtoDMS(double decDegrees) /*
													 * convert decimal degrees
													 * to
													 * degrees/minutes/seconds
													 * e.g. 36.9525 to 36 degs
													 * 57' 9" -110.0725 to -110
													 * degs 4' 21"
													 */{
		// get degrees by chopping off at the decimal
		int degrees = (int) Math.floor(decDegrees);
		if (degrees < 0) {
			degrees += 1;
		}

		// get fraction after the decimal
		double frac = Math.abs(decDegrees - degrees);

		// convert this fraction to seconds (without minutes)
		double dfSec = frac * 3600;

		// get number of whole minutes in the fraction
		int minutes = (int) Math.floor(dfSec / 60);

		// put the remainder in seconds
		int seconds = (int) Math.floor(dfSec - minutes * 60);

		// fix round-off errors
		if (seconds == 60) {
			minutes++;
			seconds = 0;
		}

		if (minutes == 60) {
			if (degrees < 0) {
				degrees--;
			} else // degrees => 0
			{
				degrees++;
			}
			minutes = 0;
		}

		return new int[] { degrees, minutes, seconds };
	} // end of DDtoDMS()

	// ------------------------------ main() for testing
	// -------------------------
	public static void main(String args[]) {
		System.out.printf("1. Distance apart (in meters): %.2f\n", GeoMath
				.distanceApart(7.001135, 100.491960, 7.007367, 100.501798));

		System.out.printf("2. Distance apart (in meters): %.2f\n", GeoMath
				.distanceApart(7.007245, 100.501938, 7.007250, 100.501935));

		double[] bbox = boundingBoxCoords(7.001135, 100.491960, 5 * 1000); // 10
																			// km
		System.out.printf(
				"5km Bounding Box. Min: (%.6f, %.6f); Max: (%.6f, %.6f)\n",
				bbox[0], bbox[1], bbox[2], bbox[3]);

		double decDegrees = GeoMath.DMSToDD(36, 57, 9, 'N');
		System.out.println("Conversion of 36 deg 57' 9'' N is: " + decDegrees);

		int[] dms = GeoMath.DDtoDMS(decDegrees);
		System.out.println("Conversion back is: " + dms[0] + " deg " + dms[1]
				+ "' " + dms[2] + "''");

	} // end of main()

	private static final Logger LOG = Logger.getLogger(GeoMath.class.getName());
} // end of GeoMath class
