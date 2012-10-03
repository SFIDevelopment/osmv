package org.anize.ur.life.wimp.util.db;

public class DBConstants {

	public static final int EMPTY_ID = -777;
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final String EMPTY = "";
	public static final String ONE_SPACE = " ";

	public static final String DATA = "data";
	public static final String DATA_FILENAME = "/location.db";

	public static final String TABLE_LOCATIONS = "locations";

	public static final String SQL_CREATE_LOCATION_CACHE = "CREATE TABLE IF NOT EXISTS 'locationcache' (_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude REAL, longitude REAL, accuracy REAL, time INTEGER, provider TEXT);";

}
