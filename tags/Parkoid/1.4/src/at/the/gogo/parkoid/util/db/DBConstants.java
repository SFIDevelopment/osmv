package at.the.gogo.parkoid.util.db;

public class DBConstants {

    public static final int    EMPTY_ID               = -777;
    public static final int    ZERO                   = 0;
    public static final int    ONE                    = 1;
    public static final String EMPTY                  = "";
    public static final String ONE_SPACE              = " ";

    public static final String DATA                   = "data";
    public static final String DATA_FILENAME          = "/parkdata.db";

    public static final String TABLE_CARS             = "cars";
    public static final String TABLE_LOCATIONS        = "locations";
    public static final String TABLE_SMS              = "sms";
    public static final String TABLE_SMSR             = "smsr";

    // CARS
    public static final String SQL_CREATE_Cars        = "CREATE TABLE IF NOT EXISTS 'cars' (carid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,licence VARCHAR);";
    public static final String SQL_GET_Car            = "SELECT carid,name,licence FROM cars WHERE carid = @1";
    public static final String SQL_GET_CarList        = "SELECT carid _id,name,licence FROM cars ORDER BY name";
    public static final String SQL_DELETE_Car         = "DELETE FROM cars WHERE carid = @1";
    public static final String SQL_DELETE_Cars        = "DELETE FROM cars";
    public static final String SQL_UPDATE_Car         = "carid = @1";

    // LOCATIONS
    public static final String SQL_CREATE_locations   = "CREATE TABLE IF NOT EXISTS 'locations' (locationid INTEGER NOT NULL PRIMARY KEY UNIQUE,carid INTEGER NOT NULL, lat FLOAT DEFAULT '0',lon FLOAT DEFAULT '0',date DATETIME);";
    public static final String SQL_GET_CarLocation    = "SELECT locationid,carid,lat,lon,date FROM locations WHERE carid = @1";
    public static final String SQL_GET_CarLocations   = "SELECT locationid,carid,lat,lon,date FROM locations";
    public static final String SQL_GET_Location       = "SELECT locationid,carid,lat,lon,date FROM locations WHERE locationid = @1";
    public static final String SQL_DELETE_CarLocation = "DELETE FROM locations WHERE carid = @1";
    public static final String SQL_DELETE_Location    = "DELETE FROM locations WHERE locationid = @1";
    public static final String SQL_DELETE_Locations   = "DELETE FROM locations";
    public static final String SQL_UPDATE_Location    = "locationid = @1";

    // SMS
    public static final String SQL_CREATE_Sms         = "CREATE TABLE IF NOT EXISTS 'sms' (smsid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,text VARCHAR,date DATETIME );";
    public static final String SQL_GET_Sms            = "SELECT smsid,name,text,date FROM sms WHERE smsid = @1";
    public static final String SQL_GET_SmsList        = "SELECT smsid _id,name,text,date FROM sms ORDER BY date DESC";
    public static final String SQL_DELETE_Sms         = "DELETE FROM sms WHERE smsid = @1";
    public static final String SQL_DELETE_Smss        = "DELETE FROM sms";
    public static final String SQL_UPDATE_Sms         = "smsid = @1";

    public static final String SQL_CREATE_Smsr        = "CREATE TABLE IF NOT EXISTS 'smsr' (smsid INTEGER NOT NULL PRIMARY KEY UNIQUE,name VARCHAR,text VARCHAR,date DATETIME );";
    public static final String SQL_GET_Smsr           = "SELECT smsid,name,text,date FROM smsr WHERE smsid = @1";
    public static final String SQL_GET_SmsrList       = "SELECT smsid _id,name,text,date FROM smsr ORDER BY name";
    public static final String SQL_DELETE_Smsr        = "DELETE FROM smsr WHERE smsid = @1";
    public static final String SQL_DELETE_Smssr       = "DELETE FROM smsr";
    public static final String SQL_UPDATE_Smsr        = "smsid = @1";

    // UPDATE commandos

    // public static final String SQL_UPDATE_3_1 =
    // "ALTER TABLE 'routecategory' add descr VARCHAR;";
    // public static final String SQL_UPDATE_3_2 =
    // "ALTER TABLE 'tracks' add categoryid INTEGER DEFAULT 0;";
    // public static final String SQL_UPDATE_3_3_1 =
    // "ALTER TABLE 'tracks' add avgspeed FLOAT DEFAULT 0;";
    // public static final String SQL_UPDATE_3_3_2 =
    // "ALTER TABLE 'tracks' add distance FLOAT DEFAULT 0;";
    // public static final String SQL_UPDATE_3_3_3 =
    // "ALTER TABLE 'tracks' add time LONG DEFAULT 0;";
    //
    // public static final String SQL_UPDATE_4_1 =
    // "ALTER TABLE 'category' add descr VARCHAR;";

}
