package at.the.gogo.parkoid.util.webservices;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import at.the.gogo.parkoid.models.C2G_Gas;
import at.the.gogo.parkoid.models.C2G_Location;
import at.the.gogo.parkoid.models.C2G_ParkingSpot;
import at.the.gogo.parkoid.models.C2G_Vehicle;
import at.the.gogo.parkoid.models.Position;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.webservices.scribe.builder.Car2GoApi;

// consumerkey: Parkoid
// shared secret: 9cBESeTa2nR30ap3
// application name: Parkoid
public class C2G_Services {

    public final static String  C2G_BASE          = "http://www.car2go.com/api/v2.1/";
    public final static String  C2G_PARKING_SPOTS = "parkingspots";
    public final static String  C2G_LOCATIONS     = "locations";
    public final static String  C2G_GAS_STATIONS  = "gasstations";
    public final static String  C2G_VEHICLES      = "vehicles";
    public final static String  C2G_ACCOUNTS      = "accounts";
    public final static String  C2G_BOOKINGS      = "bookings";
    public final static String  C2G_BOOKING       = "booking";

    private final static String C2G_CONSUMERKEY   = "Parkoid";
    private final static String C2G_SHAREDSECRET  = "9cBESeTa2nR30ap3";

    private static OAuthService service;
    private static Token        requestToken;
    private static Token        accessToken;

    private static void initSession() {
        service = new ServiceBuilder().provider(Car2GoApi.class)
                .apiKey(C2G_CONSUMERKEY).apiSecret(C2G_SHAREDSECRET)
                .scope(C2G_BASE).build();
        requestToken = service.getRequestToken();
        accessToken = service.getAccessToken(requestToken, new Verifier(
                "Parkoid"));

    }

    private static String callC2GService(final String requestUrl) {

        String result = null;

        if (accessToken == null) {
            initSession();
        }
        if (accessToken != null) {

            final OAuthRequest request = new OAuthRequest(Verb.GET, requestUrl);
            service.signRequest(accessToken, request);
            final Response response = request.send();
            Util.d("C2G rc=" + response.getCode());
            result = response.getBody();
        }
        return result;
    }

    // {"placemarks":[{"coordinates":[-97.750983,30.269577,0],"name":"West Ave","totalCapacity":4,"usedCapacity":0,"chargingPole":false},{"coordinates":[-97.74225,30.265976,0],"name":"100 East 4th Street","totalCapacity":4,"usedCapacity":0,"chargingPole":true}]}

    public static List<C2G_ParkingSpot> getParkingSpots(final String location) {

        List<C2G_ParkingSpot> parkingSpots = null;

        final String requestUrl = C2G_BASE + C2G_PARKING_SPOTS + "?loc="
                + location + "&oauth_consumer_key=" + C2G_CONSUMERKEY
                + "&format=json";

        final String resultJson = callC2GService(requestUrl);

        if (resultJson != null) {
            try {
                final JSONObject json = new JSONObject(resultJson);
                // json = json.getJSONObject("ResultSet");
                final JSONArray jsonArray = json.getJSONArray("placemarks");

                final int nrOfEntries = jsonArray.length();

                for (int i = 0; i < nrOfEntries; i++) {

                    if (parkingSpots == null) {
                        parkingSpots = new ArrayList<C2G_ParkingSpot>();
                    }
                    final JSONObject parkingSpotObject = jsonArray
                            .getJSONObject(i);
                    final C2G_ParkingSpot parkingSpot = new C2G_ParkingSpot(
                            parkingSpotObject.getString("name"));
                    parkingSpot.setTotalCapacity(parkingSpotObject
                            .getInt("totalCapacity"));
                    parkingSpot.setUsedCapacity(parkingSpotObject
                            .getInt("usedCapacity"));
                    parkingSpot.setChargingPole(parkingSpotObject
                            .getBoolean("chargingPole"));

                    final JSONArray joCoords = parkingSpotObject
                            .getJSONArray("coordinates");

                    final Position position = new Position(
                            joCoords.getDouble(0), joCoords.getDouble(1));

                    parkingSpot.setPosition(position);

                    parkingSpots.add(parkingSpot);
                }
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return parkingSpots;
    }

    // {"location":[
    // {"countryCode":"DE",
    // "defaultLanguage":"de",
    // "locationId":1,
    // "locationName":"Ulm",
    // "mapSection":{
    // "center":{
    // "latitude":48.398917,
    // "longitude":9.99139
    // },"lowerRight":{
    // "latitude":48.3446,
    // "longitude":10.0459
    // },"upperLeft":{
    // "latitude":48.4383,
    // "longitude":9.9146
    // }
    // },"timezone":"Europe/Berlin"
    // }],
    // "returnValue":{
    // "code":0,
    // "description":"Operation successful."
    // }}
    public static List<C2G_Location> getAllLocations() {

        List<C2G_Location> locations = null;

        final String requestUrl = C2G_BASE + C2G_LOCATIONS
                + "?oauth_consumer_key=" + C2G_CONSUMERKEY + "&format=json";

        final String resultJson = callC2GService(requestUrl);

        if (resultJson != null) {
            try {
                final JSONObject json = new JSONObject(resultJson);
                // json = json.getJSONObject("ResultSet");
                final JSONArray jsonArray = json.getJSONArray("location");

                final int nrOfEntries = jsonArray.length();

                for (int i = 0; i < nrOfEntries; i++) {

                    if (locations == null) {
                        locations = new ArrayList<C2G_Location>();
                    }
                    final JSONObject locationObject = jsonArray
                            .getJSONObject(i);
                    final C2G_Location location = new C2G_Location();

                    location.setCountryCode(locationObject
                            .getString("countryCode"));

                    location.setDefaultLanguage(locationObject
                            .getString("defaultLanguage"));
                    location.setLoationId(locationObject
                            .getString("locationId"));
                    location.setLocationName(locationObject
                            .getString("locationName"));
                    location.setLocationName(locationObject
                            .getString("timeZone"));

                    final JSONObject joMapSection = locationObject
                            .getJSONObject("mapSection");

                    Position position = new Position(joMapSection
                            .getJSONObject("center").getDouble("latitude"),
                            joMapSection.getJSONObject("center").getDouble(
                                    "longitude"));
                    location.setMapCenter(position);

                    position = new Position(joMapSection.getJSONObject(
                            "lowerRight").getDouble("latitude"), joMapSection
                            .getJSONObject("lowerRight").getDouble("longitude"));
                    location.setMapCenter(position);

                    position = new Position(joMapSection.getJSONObject(
                            "upperLeft").getDouble("latitude"), joMapSection
                            .getJSONObject("upperLeft").getDouble("longitude"));
                    location.setMapCenter(position);

                    locations.add(location);
                }
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return locations;
    }

    // {"placemarks":[{"coordinates":[9.987988,48.358829,0],"name":"Shell, Hauptstrasse 12"},{"coordinates":[9.990183,48.404832,0],"name":"Shell, Karlstrasse 38"}]}

    public static List<C2G_Gas> getGasStations(final String location) {

        List<C2G_Gas> gasStations = null;

        final String requestUrl = C2G_BASE + C2G_GAS_STATIONS + "?loc="
                + location + "&oauth_consumer_key=" + C2G_CONSUMERKEY
                + "&format=json";

        final String resultJson = callC2GService(requestUrl);

        if (resultJson != null) {
            try {
                final JSONObject json = new JSONObject(resultJson);
                // json = json.getJSONObject("ResultSet");
                final JSONArray jsonArray = json.getJSONArray("placemarks");

                final int nrOfEntries = jsonArray.length();

                for (int i = 0; i < nrOfEntries; i++) {

                    if (gasStations == null) {
                        gasStations = new ArrayList<C2G_Gas>();
                    }
                    final JSONObject parkingSpotObject = jsonArray
                            .getJSONObject(i);
                    final C2G_Gas gasStation = new C2G_Gas(
                            parkingSpotObject.getString("name"));

                    final JSONArray joCoords = parkingSpotObject
                            .getJSONArray("coordinates");

                    final Position position = new Position(
                            joCoords.getDouble(0), joCoords.getDouble(1));

                    gasStation.setPosition(position);

                    gasStations.add(gasStation);
                }
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return gasStations;
    }

    // {"placemarks":[{"address":"Reuttier Strasse 133, 89231 Neu-Ulm","coordinates":[10.023925,48.383125,0],"exterior":"GOOD","fuel":98,"interior":"GOOD","name":"UL-C5704","vin":"WME4513001K155530","engineType":"CE"}]}

    public static List<C2G_Vehicle> getFreeVehicles(final String location) {

        List<C2G_Vehicle> vehicles = null;

        final String requestUrl = C2G_BASE + C2G_VEHICLES + "?loc=" + location
                + "&oauth_consumer_key=" + C2G_CONSUMERKEY + "&format=json";

        final String resultJson = callC2GService(requestUrl);

        if (resultJson != null) {
            try {
                final JSONObject json = new JSONObject(resultJson);
                // json = json.getJSONObject("ResultSet");
                final JSONArray jsonArray = json.getJSONArray("placemarks");

                final int nrOfEntries = jsonArray.length();

                for (int i = 0; i < nrOfEntries; i++) {

                    if (vehicles == null) {
                        vehicles = new ArrayList<C2G_Vehicle>();
                    }
                    final JSONObject vehicleObject = jsonArray.getJSONObject(i);
                    final C2G_Vehicle vehicle = new C2G_Vehicle(-1,
                            vehicleObject.getString("name"),
                            vehicleObject.getString("name"));

                    vehicle.setEngineType(vehicleObject.getString("engineType"));
                    vehicle.setAddress(vehicleObject.getString("address"));
                    vehicle.setExterior(vehicleObject.getString("exterior"));
                    vehicle.setInterior(vehicleObject.getString("interior"));
                    vehicle.setFuel(vehicleObject.getString("fuel"));

                    final JSONArray joCoords = vehicleObject
                            .getJSONArray("coordinates");

                    final Position position = new Position(
                            joCoords.getDouble(0), joCoords.getDouble(1));

                    vehicle.setPosition(position);

                    vehicles.add(vehicle);
                }
            } catch (final JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return vehicles;
    }

}
