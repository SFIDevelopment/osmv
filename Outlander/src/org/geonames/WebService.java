/*
 * Copyright 2008-2010 Marc Wick, geonames.org Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.geonames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.outlander.model.PanoramioItem;
import org.outlander.utils.Ut;
import org.outlander.utils.geo.GeoMathUtil;

/**
 * provides static methods to access the <a
 * href="http://www.geonames.org/export/ws-overview.html">GeoNames web
 * services</a>.
 * <p>
 * Note : values for some fields are only returned with sufficient {@link Style}
 * . Accessing these fields (admin codes and admin names, elevation,population)
 * will throw an {@link InsufficientStyleException} if the {@link Style} was not
 * sufficient.
 * 
 * @author marc@geonames
 */
public class WebService {

    private static final String YAHOO_API_BASE_URL     = "http://where.yahooapis.com/geocode?q=%1$s,+%2$s&flags=J&gflags=R&appid=";
    private static final String YAHOO_API_KEY          = "VQc11HnV34E4TgyaQmzuMwDnkU2pO889TPoa8polQiZTJVWwgb3Xf9gYnbWYAMg1WpPhA1Mexhi7XXZ.QZyYDgMJND.jUyg-";

    private static final String USER_AGENT             = "geonames-webservice-client-1.0.6";
    private static String       geoNamesServer         = "http://api.geonames.org";
    private static String       geoNamesServerFailover = "http://api.geonames.org";
    private static final String PANORAMIO_URL          = "http://www.panoramio.com/map/get_panoramas.php";
    private static long         timeOfLastFailureMainServer;
    private static Style        defaultStyle           = Style.MEDIUM;
    public static int           readTimeOut            = 120000;
    public static int           connectTimeOut         = 10000;
    public final static String  minuteDateFmt          = "yyyy-MM-dd HH:mm";
    private static final String DATEFMT                = "yyyy-MM-dd HH:mm:ss";
    public final static String  USERNAME               = "osmv";                                                                                            // default
                                                                                                                                                             // user
                                                                                                                                                             // name
    /**
     * user name to pass to commercial web services for authentication and
     * authorization
     */
    private static String       userName               = WebService.USERNAME;
    /**
     * token to pass to as optional authentication parameter to the commercial
     * web services.
     */
    private static String       token;

    /**
     * adds the username stored in a static variable to the url. It also adds a
     * token if one has been set with the static setter previously.
     * 
     * @param url
     * @return url with the username appended
     */
    private static String addUserName(final String url) {
        String personalizedUrl = url;
        if (WebService.userName != null) {
            personalizedUrl += "&username=" + WebService.userName;
        }
        if (WebService.token != null) {
            personalizedUrl += "&token=" + WebService.token;
        }
        return personalizedUrl;
    }

    /**
     * adds the default style to the url. The default style can be set with the
     * static setter. It is 'MEDIUM' if not set.
     * 
     * @param url
     * @return url with the style parameter appended
     */
    private static String addDefaultStyle(final String url) {
        String styledUrl = url;
        if (WebService.defaultStyle != Style.MEDIUM) {
            styledUrl = styledUrl + "&style=" + WebService.defaultStyle.name();
        }
        return styledUrl;
    }

    /**
     * returns the currently active server. Normally this is the main server, if
     * the main server recently failed then the failover server is returned. If
     * the main server is not available we don't want to try with every request
     * whether it is available again. We switch to the failover server and try
     * from time to time whether the main server is again accessible.
     * 
     * @return
     */
    private static String getCurrentlyActiveServer() {
        if (WebService.timeOfLastFailureMainServer == 0) {
            // no problems with main server
            return WebService.geoNamesServer;
        }
        // we had problems with main server
        if ((System.currentTimeMillis() - WebService.timeOfLastFailureMainServer) > (1000l * 60l * 10l)) {
            // but is was some time ago and we switch back to the main server to
            // retry. The problem may have been solved in the mean time.
            WebService.timeOfLastFailureMainServer = 0;
            return WebService.geoNamesServer;
        }
        if (System.currentTimeMillis() < WebService.timeOfLastFailureMainServer) {
            throw new Error("time of last failure cannot be in future.");
        }
        // the problems have been very recent and we continue with failover
        // server
        if (WebService.geoNamesServerFailover != null) {
            return WebService.geoNamesServerFailover;
        }
        return WebService.geoNamesServer;
    }

    /**
     * opens the connection to the url and sets the user agent. In case of an
     * IOException it checks whether a failover server is set and connects to
     * the failover server if it has been defined and if it is different from
     * the normal server.
     * 
     * @param url
     *            the url to connect to
     * @return returns the inputstream for the connection
     * @throws IOException
     */
    private static InputStream connect(final String url) throws IOException {
        final String currentlyActiveServer = getCurrentlyActiveServer();
        try {
            final URLConnection conn = new URL(currentlyActiveServer + url).openConnection();
            conn.setConnectTimeout(WebService.connectTimeOut);
            conn.setReadTimeout(WebService.readTimeOut);
            conn.setRequestProperty("User-Agent", WebService.USER_AGENT);
            final InputStream in = conn.getInputStream();
            return in;
        }
        catch (final IOException e) {
            // we cannot reach the server
            Ut.d("problems connecting to geonames server " + WebService.geoNamesServer + "Exception:" + e);
            // is a failover server defined?
            if ((WebService.geoNamesServerFailover == null)
            // is it different from the one we are using?
                    || currentlyActiveServer.equals(WebService.geoNamesServerFailover)) {
                throw e;
            }
            WebService.timeOfLastFailureMainServer = System.currentTimeMillis();
            Ut.d("trying to connect to failover server " + WebService.geoNamesServerFailover);
            // try failover server
            final URLConnection conn = new URL(WebService.geoNamesServerFailover + url).openConnection();
            conn.setRequestProperty("User-Agent", WebService.USER_AGENT + " failover from " + WebService.geoNamesServer);
            final InputStream in = conn.getInputStream();
            return in;
        }
    }

    public static String webGetString(final URL url) {
        final String result = "";

        try {
            final URLConnection conn = url.openConnection();
            conn.setConnectTimeout(WebService.connectTimeOut);
            conn.setReadTimeout(WebService.readTimeOut);
            conn.setRequestProperty("User-Agent", WebService.USER_AGENT);

            String line;
            final StringBuilder sb = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        }
        catch (final Exception x) {
            Ut.d(x.toString());
        }

        return result;
    }

    public static String webGetString(final String url)// contact the specified
                                                       // URL, and
    // return the response as a
    // string
    {
        // System.out.println("Contacting \"" + urlStr + "\"");
        try {

            String line;
            final StringBuilder sb = new StringBuilder();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(connect(url)));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        }
        catch (final Exception e) {
            Ut.d(e.toString());
        }

        return null;
    } // end of webGetString()

    // {"adminCode1":"09","countryName":"Austria","fclName":"city, village,...","countryCode":"AT","lng":16.3720750808716,"fcodeName":"capital of a political entity","toponymName":"Vienna","fcl":"P","name":"Vienna","fcode":"PPLC","geonameId":2761369,"lat":48.2084877601653,"population":1691468,"adminName1":"Vienna"}
    private static Toponym getToponymFromElement(final JSONObject toponymElement) {
        final Toponym toponym = new Toponym();
        try {
            toponym.setName(toponymElement.getString("name"));
            // toponym.setAlternateNames(toponymElement
            // .getString("alternateNames"));
            toponym.setLatitude(toponymElement.getDouble("lat"));
            toponym.setLongitude(toponymElement.getDouble("lng"));

            final String geonameId = toponymElement.getString("geonameId");
            if (geonameId != null) {
                toponym.setGeoNameId(Integer.parseInt(geonameId));
            }

            toponym.setCountryCode(toponymElement.getString("countryCode"));
            toponym.setCountryName(toponymElement.getString("countryName"));

            toponym.setFeatureClass(FeatureClass.fromValue(toponymElement.getString("fcl")));
            toponym.setFeatureCode(toponymElement.getString("fcode"));

            toponym.setFeatureClassName(toponymElement.getString("fclName"));
            toponym.setFeatureCodeName(toponymElement.getString("fCodeName"));

            final String population = toponymElement.getString("population");
            if ((population != null) && !"".equals(population)) {
                toponym.setPopulation(Long.parseLong(population));
            }
            final String elevation = toponymElement.getString("elevation");
            if ((elevation != null) && !"".equals(elevation)) {
                toponym.setElevation(Integer.parseInt(elevation));
            }

            toponym.setAdminCode1(toponymElement.getString("adminCode1"));
            toponym.setAdminName1(toponymElement.getString("adminName1"));
            toponym.setAdminCode2(toponymElement.getString("adminCode2"));
            toponym.setAdminName2(toponymElement.getString("adminName2"));
            toponym.setAdminCode3(toponymElement.getString("adminCode3"));
            toponym.setAdminCode4(toponymElement.getString("adminCode4"));

            // JSONObject timezoneElement =
            // toponymElement.getJSONObject("timezone");
            // if (timezoneElement != null) {
            // Timezone timezone = new Timezone();
            // timezone.setTimezoneId(timezoneElement.get?);
            // timezone.setDstOffset(timezoneElement.getDouble("dstOffset"));
            // timezone.setGmtOffset(timezoneElement.getDouble("gmtOffset"));
            // toponym.setTimezone(timezone);
            // }
        }
        catch (final JSONException x) {

        }
        return toponym;
    }

    private static String getJSONStringValue(final JSONObject jsonObject, final String key) {
        String value = null;
        try {
            value = jsonObject.getString(key);
        }
        catch (final JSONException x) {
            Ut.dd("JSON Key [" + key + "] not found");
        }
        return value;
    }

    private static int getJSONIntValue(final JSONObject jsonObject, final String key) {
        int value = -1;
        try {
            value = Integer.parseInt(jsonObject.getString(key));

        }
        catch (final Exception x) {
            Ut.dd("JSON Key [" + key + "] not found/valid");
        }
        return value;
    }

    private static double getJSONDoubleValue(final JSONObject jsonObject, final String key) {
        double value = Double.NaN;
        try {
            value = Double.parseDouble(jsonObject.getString(key));

        }
        catch (final Exception x) {
            Ut.dd("JSON Key [" + key + "] not found/valid");
        }
        return value;
    }

    private static WikipediaArticle getWikipediaArticleFromElement(final JSONObject wikipediaArticleElement) {
        final WikipediaArticle wikipediaArticle = new WikipediaArticle();

        wikipediaArticle.setLanguage(getJSONStringValue(wikipediaArticleElement, "lang"));
        wikipediaArticle.setTitle(getJSONStringValue(wikipediaArticleElement, "title"));
        wikipediaArticle.setSummary(getJSONStringValue(wikipediaArticleElement, "summary"));
        wikipediaArticle.setFeature(getJSONStringValue(wikipediaArticleElement, "feature"));
        wikipediaArticle.setWikipediaUrl(getJSONStringValue(wikipediaArticleElement, "wikipediaUrl"));
        wikipediaArticle.setThumbnailImg(getJSONStringValue(wikipediaArticleElement, "thumbnailImg"));
        wikipediaArticle.setLatitude(getJSONDoubleValue(wikipediaArticleElement, "lat"));
        wikipediaArticle.setLongitude(getJSONDoubleValue(wikipediaArticleElement, "lng"));
        wikipediaArticle.setDistance(getJSONDoubleValue(wikipediaArticleElement, "distance"));
        wikipediaArticle.setCountryCode(getJSONStringValue(wikipediaArticleElement, "countryCode"));
        wikipediaArticle.setPopulation(getJSONIntValue(wikipediaArticleElement, "population"));
        wikipediaArticle.setElevation(getJSONIntValue(wikipediaArticleElement, "elevation"));
        return wikipediaArticle;
    }

    private static WeatherObservation getWeatherObservationFromElement(final JSONObject weatherObservationElement) {
        final WeatherObservation weatherObservation = new WeatherObservation();
        weatherObservation.setObservation(getJSONStringValue(weatherObservationElement, "observation"));
        final SimpleDateFormat df = new SimpleDateFormat(WebService.DATEFMT);
        try {
            weatherObservation.setObservationTime(df.parse(getJSONStringValue(weatherObservationElement, "observationTime")));
        }
        catch (final Exception x) {
            Ut.dd("observationTime parsing failed");
        }
        weatherObservation.setStationName(getJSONStringValue(weatherObservationElement, "stationName"));
        weatherObservation.setIcaoCode(getJSONStringValue(weatherObservationElement, "ICAO"));
        weatherObservation.setCountryCode(getJSONStringValue(weatherObservationElement, "countryCode"));
        final String elevation = getJSONStringValue(weatherObservationElement, "elevation");
        if ((elevation != null) && !"".equals(elevation)) {
            weatherObservation.setElevation(Integer.parseInt(elevation));
        }
        weatherObservation.setLatitude(Double.parseDouble(getJSONStringValue(weatherObservationElement, "lat")));
        weatherObservation.setLongitude(Double.parseDouble(getJSONStringValue(weatherObservationElement, "lng")));
        final String temperature = getJSONStringValue(weatherObservationElement, "temperature");
        if ((temperature != null) && !"".equals(temperature)) {
            weatherObservation.setTemperature(Double.parseDouble(temperature));
        }
        final String dewPoint = getJSONStringValue(weatherObservationElement, "dewPoint");
        if ((dewPoint != null) && !"".equals(dewPoint)) {
            weatherObservation.setDewPoint(Double.parseDouble(dewPoint));
        }
        final String humidity = getJSONStringValue(weatherObservationElement, "humidity");
        if ((humidity != null) && !"".equals(humidity)) {
            weatherObservation.setHumidity(Double.parseDouble(humidity));
        }
        weatherObservation.setClouds(getJSONStringValue(weatherObservationElement, "clouds"));
        weatherObservation.setWeatherCondition(getJSONStringValue(weatherObservationElement, "weatherCondition"));
        weatherObservation.setWindSpeed(getJSONStringValue(weatherObservationElement, "windSpeed"));
        return weatherObservation;

    }

    /**
     * returns a list of postal codes for the given parameters. This method is
     * for convenience.
     * 
     * @param postalCode
     * @param placeName
     * @param countryCode
     * @return
     * @throws Exception
     */
    // public static List<PostalCode> postalCodeSearch(String postalCode,
    // String placeName, String countryCode) throws Exception {
    // PostalCodeSearchCriteria postalCodeSearchCriteria = new
    // PostalCodeSearchCriteria();
    // postalCodeSearchCriteria.setPostalCode(postalCode);
    // postalCodeSearchCriteria.setPlaceName(placeName);
    // postalCodeSearchCriteria.setCountryCode(countryCode);
    // return postalCodeSearch(postalCodeSearchCriteria);
    // }

    /**
     * returns a list of postal codes for the given search criteria matching a
     * full text search on the GeoNames postal codes database.
     * 
     * @param postalCodeSearchCriteria
     * @return
     * @throws Exception
     */
    // public static List<PostalCode> postalCodeSearch(
    // PostalCodeSearchCriteria postalCodeSearchCriteria) throws Exception {
    // List<PostalCode> postalCodes = new ArrayList<PostalCode>();
    //
    // String url = "/postalCodeSearch?";
    // if (postalCodeSearchCriteria.getPostalCode() != null) {
    // url = url
    // + "postalcode="
    // + URLEncoder.encode(postalCodeSearchCriteria.getPostalCode(), "UTF8");
    // }
    // if (postalCodeSearchCriteria.getPlaceName() != null) {
    // if (!url.endsWith("&")) {
    // url += "&";
    // }
    // url = url
    // + "placename="
    // + URLEncoder.encode(
    // postalCodeSearchCriteria.getPlaceName(), "UTF8");
    // }
    // if (postalCodeSearchCriteria.getAdminCode1() != null) {
    // url = url
    // + "&adminCode1="
    // + URLEncoder.encode(postalCodeSearchCriteria.getAdminCode1(), "UTF8");
    // }
    //
    // if (postalCodeSearchCriteria.getCountryCode() != null) {
    // if (!url.endsWith("&")) {
    // url += "&";
    // }
    // url = url + "country=" + postalCodeSearchCriteria.getCountryCode();
    // }
    // if (postalCodeSearchCriteria.getCountryBias() != null) {
    // if (!url.endsWith("&")) {
    // url += "&";
    // }
    // url = url + "countryBias="
    // + postalCodeSearchCriteria.getCountryBias();
    // }
    // if (postalCodeSearchCriteria.getMaxRows() > 0) {
    // url = url + "&maxRows=" + postalCodeSearchCriteria.getMaxRows();
    // }
    // if (postalCodeSearchCriteria.getStartRow() > 0) {
    // url = url + "&startRow=" + postalCodeSearchCriteria.getStartRow();
    // }
    // if (postalCodeSearchCriteria.isOROperator()) {
    // url += "&operator=OR";
    // }
    // if (postalCodeSearchCriteria.isReduced() != null) {
    // url = url + "&isReduced="
    // + postalCodeSearchCriteria.isReduced().toString();
    // }
    // url = addUserName(url);
    //
    // SAXBuilder parser = new SAXBuilder();
    // Document doc = parser.build(connect(url));
    //
    // Element root = rootAndCheckException(doc);
    // for (Object obj : root.getChildren("code")) {
    // Element codeElement = (Element) obj;
    // PostalCode code = new PostalCode();
    // code.setPostalCode(codeElement.getString("postalcode"));
    // code.setPlaceName(codeElement.getString("name"));
    // code.setCountryCode(codeElement.getString("countryCode"));
    // code.setAdminCode1(codeElement.getString("adminCode1"));
    // code.setAdminCode2(codeElement.getString("adminCode2"));
    // code.setAdminName1(codeElement.getString("adminName1"));
    // code.setAdminName2(codeElement.getString("adminName2"));
    //
    // code.setLatitude(Double.parseDouble(codeElement.getString("lat")));
    // code.setLongitude(Double.parseDouble(codeElement.getString("lng")));
    //
    // postalCodes.add(code);
    // }
    //
    // return postalCodes;
    // }

    /**
     * returns a list of postal codes
     * 
     * @param postalCodeSearchCriteria
     * @return
     * @throws Exception
     */
    // public static List<PostalCode> findNearbyPostalCodes(
    // PostalCodeSearchCriteria postalCodeSearchCriteria) throws Exception {
    //
    // List<PostalCode> postalCodes = new ArrayList<PostalCode>();
    //
    // String url = "/findNearbyPostalCodes?";
    // if (postalCodeSearchCriteria.getPostalCode() != null) {
    // url = url
    // + "&postalcode="
    // + URLEncoder.encode(postalCodeSearchCriteria.getPostalCode(), "UTF8");
    // }
    // if (postalCodeSearchCriteria.getPlaceName() != null) {
    // url = url
    // + "&placename="
    // + URLEncoder.encode(
    // postalCodeSearchCriteria.getPlaceName(), "UTF8");
    // }
    // if (postalCodeSearchCriteria.getCountryCode() != null) {
    // url = url + "&country=" + postalCodeSearchCriteria.getCountryCode();
    // }
    //
    // if (postalCodeSearchCriteria.getLatitude() != null) {
    // url = url + "&lat=" + postalCodeSearchCriteria.getLatitude();
    // }
    // if (postalCodeSearchCriteria.getLongitude() != null) {
    // url = url + "&lng=" + postalCodeSearchCriteria.getLongitude();
    // }
    // if (postalCodeSearchCriteria.getStyle() != null) {
    // url = url + "&style=" + postalCodeSearchCriteria.getStyle();
    // }
    // if (postalCodeSearchCriteria.getMaxRows() > 0) {
    // url = url + "&maxRows=" + postalCodeSearchCriteria.getMaxRows();
    // }
    //
    // if (postalCodeSearchCriteria.getRadius() > 0) {
    // url = url + "&radius=" + postalCodeSearchCriteria.getRadius();
    // }
    // url = addUserName(url);
    //
    // SAXBuilder parser = new SAXBuilder();
    // Document doc = parser.build(connect(url));
    //
    // Element root = rootAndCheckException(doc);
    // for (Object obj : root.getChildren("code")) {
    // Element codeElement = (Element) obj;
    // PostalCode code = new PostalCode();
    // code.setPostalCode(codeElement.getString("postalcode"));
    // code.setPlaceName(codeElement.getString("name"));
    // code.setCountryCode(codeElement.getString("countryCode"));
    //
    // code.setLatitude(Double.parseDouble(codeElement.getString("lat")));
    // code.setLongitude(Double.parseDouble(codeElement.getString("lng")));
    //
    // code.setAdminName1(codeElement.getString("adminName1"));
    // code.setAdminCode1(codeElement.getString("adminCode1"));
    // code.setAdminName2(codeElement.getString("adminName2"));
    // code.setAdminCode2(codeElement.getString("adminCode2"));
    //
    // if (codeElement.getString("distance") != null) {
    // code.setDistance(Double.parseDouble(codeElement.getString("distance")));
    // }
    //
    // postalCodes.add(code);
    // }
    //
    // return postalCodes;
    // }

    /**
     * convenience method for
     * {@link #findNearbyPlaceName(double,double,double,int)}
     * 
     * @param latitude
     * @param longitude
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static List<Toponym> findNearbyPlaceName(final double latitude, final double longitude) throws Exception {
        return findNearbyPlaceName(latitude, longitude, 0, 0);
    }

    private static List<Toponym> findNearbyPlaceName(final double latitude, final double longitude, final double radius, final int maxRows) throws Exception {
        final List<Toponym> places = new ArrayList<Toponym>();

        String url = "/findNearbyPlaceNameJSON?";

        url = url + "&lat=" + latitude;
        url = url + "&lng=" + longitude;
        if (radius > 0) {
            url = url + "&radius=" + radius;
        }
        if (maxRows > 0) {
            url = url + "&maxRows=" + maxRows;
        }
        url = addUserName(url);
        url = addDefaultStyle(url);

        final String searchResultJSON = webGetString(url);

        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                System.out.println("\nNo. of matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final Toponym toponym = getToponymFromElement(ja.getJSONObject(i));
                    places.add(toponym);
                }
            }
        }
        return places;
    }

    public static List<Toponym> findNearby(final double latitude, final double longitude, final FeatureClass featureClass, final String[] featureCodes)
            throws Exception {
        return findNearby(latitude, longitude, 0, featureClass, featureCodes, null, 0);
    }

    /* Overload function to allow backward compatibility */
    /**
     * Based on the following inforamtion: Webservice Type : REST
     * ws.geonames.org/findNearbyWikipedia? Parameters : lang : language code
     * (around 240 languages) (default = en) lat,lng, radius (in km), maxRows
     * (default = 10) featureClass featureCode Example:
     * http://ws.geonames.org/findNearby?lat=47.3&lng=9
     * 
     * @param latitude
     * @param longitude
     * @param radius
     * @param featureClass
     * @param featureCodes
     * @param language
     * @param maxRows
     * @param: latitude
     * @param: longitude
     * @param: radius
     * @param: feature Class
     * @param: feature Codes
     * @param: language
     * @param: maxRows
     * @return: list of wikipedia articles
     * @throws: Exception
     */
    private static List<Toponym> findNearby(final double latitude, final double longitude, final double radius, final FeatureClass featureClass,
            final String[] featureCodes, final String language, final int maxRows) throws Exception {
        final List<Toponym> places = new ArrayList<Toponym>();

        String url = "/findNearbyJSON?";

        url += "&lat=" + latitude;
        url += "&lng=" + longitude;
        if (radius > 0) {
            url = url + "&radius=" + radius;
        }
        if (maxRows > 0) {
            url = url + "&maxRows=" + maxRows;
        }

        if (language != null) {
            url = url + "&lang=" + language;
        }

        if (featureClass != null) {
            url += "&featureClass=" + featureClass;
        }
        if ((featureCodes != null) && (featureCodes.length > 0)) {
            for (final String featureCode : featureCodes) {
                url += "&featureCode=" + featureCode;
            }
        }

        url = addUserName(url);
        url = addDefaultStyle(url);

        final String searchResultJSON = webGetString(url);

        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                System.out.println("\nNo. of matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final Toponym toponym = getToponymFromElement(ja.getJSONObject(i));
                    places.add(toponym);
                }
            }
        }
        return places;
    }

    // public static Address findNearestAddress(double latitude, double
    // longitude)
    // throws Exception {
    //
    // String url = "/findNearestAddress?";
    //
    // url = url + "&lat=" + latitude;
    // url = url + "&lng=" + longitude;
    // url = addUserName(url);
    //
    // SAXBuilder parser = new SAXBuilder();
    // Document doc = parser.build(connect(url));
    //
    // Element root = rootAndCheckException(doc);
    // for (Object obj : root.getChildren("address")) {
    // Element codeElement = (Element) obj;
    // Address address = new Address();
    // address.setStreet(codeElement.getString("street"));
    // address.setStreetNumber(codeElement.getString("streetNumber"));
    // address.setMtfcc(codeElement.getString("mtfcc"));
    //
    // address.setPostalCode(codeElement.getString("postalcode"));
    // address.setPlaceName(codeElement.getString("placename"));
    // address.setCountryCode(codeElement.getString("countryCode"));
    //
    // address.setLatitude(Double.parseDouble(codeElement.getString("lat")));
    // address.setLongitude(Double.parseDouble(codeElement.getString("lng")));
    //
    // address.setAdminName1(codeElement.getString("adminName1"));
    // address.setAdminCode1(codeElement.getString("adminCode1"));
    // address.setAdminName2(codeElement.getString("adminName2"));
    // address.setAdminCode2(codeElement.getString("adminCode2"));
    //
    // address.setDistance(Double.parseDouble(codeElement.getString("distance")));
    //
    // return address;
    // }
    //
    // return null;
    // }

    // public static Intersection findNearestIntersection(double latitude,
    // double longitude) throws Exception {
    // return findNearestIntersection(latitude, longitude, 0);
    // }
    //
    // public static Intersection findNearestIntersection(double latitude,
    // double longitude, double radius) throws Exception {
    //
    // String url = "/findNearestIntersection?";
    //
    // url = url + "&lat=" + latitude;
    // url = url + "&lng=" + longitude;
    // if (radius > 0) {
    // url = url + "&radius=" + radius;
    // }
    // url = addUserName(url);
    //
    // SAXBuilder parser = new SAXBuilder();
    // Document doc = parser.build(connect(url));
    //
    // Element root = rootAndCheckException(doc);
    // for (Object obj : root.getChildren("intersection")) {
    // Element e = (Element) obj;
    // Intersection intersection = new Intersection();
    // intersection.setStreet1(e.getString("street1"));
    // intersection.setStreet2(e.getString("street2"));
    // intersection.setLatitude(Double.parseDouble(e.getString("lat")));
    // intersection.setLongitude(Double.parseDouble(e.getString("lng")));
    // intersection.setDistance(Double.parseDouble(e.getString("distance")));
    // intersection.setPostalCode(e.getString("postalcode"));
    // intersection.setPlaceName(e.getString("placename"));
    // intersection.setCountryCode(e.getString("countryCode"));
    // intersection.setAdminName2(e.getString("adminName2"));
    // intersection.setAdminCode1(e.getString("adminCode1"));
    // intersection.setAdminName1(e.getString("adminName1"));
    // return intersection;
    // }
    // return null;
    // }

    /**
     * @see <a * href=
     *      "http://www.geonames.org/maps/reverse-geocoder.html#findNearbyStreets"
     *      > web service documentation</a>
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     * @throws Exception
     */
    // public static List<StreetSegment> findNearbyStreets(double latitude,
    // double longitude, double radius) throws Exception {
    //
    // String url = "/findNearbyStreets?";
    //
    // url = url + "&lat=" + latitude;
    // url = url + "&lng=" + longitude;
    // if (radius > 0) {
    // url = url + "&radius=" + radius;
    // }
    // url = addUserName(url);
    //
    // List<StreetSegment> segments = new ArrayList<StreetSegment>();
    //
    // SAXBuilder parser = new SAXBuilder();
    // Document doc = parser.build(connect(url));
    //
    // Element root = rootAndCheckException(doc);
    // for (Object obj : root.getChildren("streetSegment")) {
    // Element e = (Element) obj;
    // StreetSegment streetSegment = new StreetSegment();
    // String line = e.getString("line");
    // String[] points = line.split(",");
    // double[] latArray = new double[points.length];
    // double[] lngArray = new double[points.length];
    // for (int i = 0; i < points.length; i++) {
    // String[] coords = points[i].split(" ");
    // lngArray[i] = Double.parseDouble(coords[0]);
    // latArray[i] = Double.parseDouble(coords[1]);
    // }
    //
    // streetSegment.setCfcc(e.getString("cfcc"));
    // streetSegment.setName(e.getString("name"));
    // streetSegment.setFraddl(e.getString("fraddl"));
    // streetSegment.setFraddr(e.getString("fraddr"));
    // streetSegment.setToaddl(e.getString("toaddl"));
    // streetSegment.setToaddr(e.getString("toaddr"));
    // streetSegment.setPostalCode(e.getString("postalcode"));
    // streetSegment.setPlaceName(e.getString("placename"));
    // streetSegment.setCountryCode(e.getString("countryCode"));
    // streetSegment.setAdminName2(e.getString("adminName2"));
    // streetSegment.setAdminCode1(e.getString("adminCode1"));
    // streetSegment.setAdminName1(e.getString("adminName1"));
    // segments.add(streetSegment);
    // }
    // return segments;
    // }

    /**
     * convenience method for {@link #search(ToponymSearchCriteria)}
     * 
     * @see <a href="http://www.geonames.org/export/geonames-search.html">search
     *      web service documentation</a>
     * @param q
     * @param countryCode
     * @param name
     * @param featureCodes
     * @param startRow
     * @return
     * @throws Exception
     */
    public static ToponymSearchResult search(final String q, final String countryCode, final String name, final String[] featureCodes, final int startRow)
            throws Exception {
        return search(q, countryCode, name, featureCodes, startRow, null, null, null);
    }

    /**
     * convenience method for {@link #search(ToponymSearchCriteria)} The string
     * fields will be transparently utf8 encoded within the call.
     * 
     * @see <a href="http://www.geonames.org/export/geonames-search.html">search
     *      web service documentation</a>
     * @param q
     *            search over all fields
     * @param countryCode
     * @param name
     *            search over name only
     * @param featureCodes
     * @param startRow
     * @param language
     * @param style
     * @param exactName
     * @return
     * @throws Exception
     */
    private static ToponymSearchResult search(final String q, final String countryCode, final String name, final String[] featureCodes, final int startRow,
            final String language, final Style style, final String exactName) throws Exception {
        final ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setQ(q);
        searchCriteria.setCountryCode(countryCode);
        searchCriteria.setName(name);
        searchCriteria.setFeatureCodes(featureCodes);
        searchCriteria.setStartRow(startRow);
        searchCriteria.setLanguage(language);
        searchCriteria.setStyle(style);
        searchCriteria.setNameEquals(exactName);
        return search(searchCriteria);
    }

    // {"totalResultsCount":5049,"geonames":[
    // {"countryName":"United Kingdom",
    // "adminCode1":"ENG",
    // "fclName":"city, village,...",
    // "countryCode":"GB",
    // "lng":-0.12574195861816406,
    // "fcodeName":"capital of a political entity",
    // "toponymName":"London",
    // "fcl":"P",
    // "name":"London",
    // "fcode":"PPLC",
    // "geonameId":2643743,
    // "lat":51.508528775862885,
    // "adminName1":"England",
    // "population":7556900
    // },
    // {"countryName":"Canada","adminCode1":"08","fclName":"city, village,...","countryCode":"CA","lng":-81.233042387,"fcodeName":"populated place","toponymName":"London","fcl":"P","name":"London","fcode":"PPL","geonameId":6058560,"lat":42.983389283,"adminName1":"Ontario","population":346765},{"countryName":"South Africa","adminCode1":"05","fclName":"city, village,...","countryCode":"ZA","lng":27.9116249084473,"fcodeName":"populated place","toponymName":"East London","fcl":"P","name":"East London","fcode":"PPL","geonameId":1006984,"lat":-33.0152850934643,"adminName1":"Eastern Cape","population":478676},{"countryName":"United Kingdom","adminCode1":"ENG","fclName":"city, village,...","countryCode":"GB","lng":-0.0918388366699219,"fcodeName":"section of populated place","toponymName":"City of London","fcl":"P","name":"City of London","fcode":"PPLX","geonameId":2643741,"lat":51.5127888902952,"adminName1":"England","population":7556900},{"countryName":"United Kingdom","adminCode1":"ENG","fclName":"city, village,...","countryCode":"GB","lng":-0.333333,"fcodeName":"populated place","toponymName":"London Borough of Harrow","fcl":"P","name":"London Borough of Harrow","fcode":"PPL","geonameId":7535661,"lat":51.566667,"adminName1":"England","population":216200},{"countryName":"United Kingdom","adminCode1":"ENG","fclName":"city, village,...","countryCode":"GB","lng":-0.2,"fcodeName":"seat of a third-order administrative division","toponymName":"Sutton","fcl":"P","name":"Sutton","fcode":"PPLA3","geonameId":2636503,"lat":51.35,"adminName1":"England","population":187600},{"countryName":"Canada","adminCode1":"08","fclName":"stream, lake, ...","countryCode":"CA","lng":-91.000204252,"fcodeName":"lake","toponymName":"London Lake","fcl":"H","name":"London Lake","fcode":"LK","geonameId":6058570,"lat":50.066788279,"adminName1":"Ontario","population":0},{"countryName":"Canada","adminCode1":"02","fclName":"stream, lake, ...","countryCode":"CA","lng":-123.169339683,"fcodeName":"lake","toponymName":"London Slough","fcl":"H","name":"London Slough","fcode":"LK","geonameId":6058577,"lat":49.099659794,"adminName1":"British Columbia","population":0},{"countryName":"United States","adminCode1":"CT","fclName":"country, state, region,...","countryCode":"US","lng":-72.0945205,"fcodeName":"administrative division","toponymName":"Town of New London","fcl":"A","name":"Town of New London","fcode":"ADMD","geonameId":4839433,"lat":41.3298205,"adminName1":"Connecticut","population":0},{"countryName":"United States","adminCode1":"CT","fclName":"country, state, region,...","countryCode":"US","lng":-72.096376,"fcodeName":"administrative division","toponymName":"City of New London","fcl":"A","name":"City of New London","fcode":"ADMD","geonameId":7316471,"lat":41.3327,"adminName1":"Connecticut","population":0}]}
    //

    /**
     * full text search on the GeoNames database. This service gets the number
     * of toponyms defined by the 'maxRows' parameter. The parameter 'style'
     * determines which fields are returned by the service.
     * 
     * @see <a href="http://www.geonames.org/export/geonames-search.html">search
     *      web service documentation</a> <br>
     * 
     *      <pre>
     * ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
     * searchCriteria.setQ(&quot;z&amp;uumlrich&quot;);
     * ToponymSearchResult searchResult = WebService.search(searchCriteria);
     * for (Toponym toponym : searchResult.toponyms) {
     *     System.out.println(toponym.getName() + &quot; &quot; + toponym.getCountryName());
     * }
     * </pre>
     * @param searchCriteria
     * @return
     * @throws Exception
     */
    private static ToponymSearchResult search(final ToponymSearchCriteria searchCriteria) throws Exception {
        final ToponymSearchResult searchResult = new ToponymSearchResult();

        String url = "/searchJSON?";

        if (searchCriteria.getQ() != null) {
            url = url + "q=" + URLEncoder.encode(searchCriteria.getQ(), "UTF8");
        }
        if (searchCriteria.getNameEquals() != null) {
            url = url + "&name_equals=" + URLEncoder.encode(searchCriteria.getNameEquals(), "UTF8");
        }
        if (searchCriteria.getNameStartsWith() != null) {
            url = url + "&name_startsWith=" + URLEncoder.encode(searchCriteria.getNameStartsWith(), "UTF8");
        }

        if (searchCriteria.getName() != null) {
            url = url + "&name=" + URLEncoder.encode(searchCriteria.getName(), "UTF8");
        }

        if (searchCriteria.getTag() != null) {
            url = url + "&tag=" + URLEncoder.encode(searchCriteria.getTag(), "UTF8");
        }

        if (searchCriteria.getCountryCode() != null) {
            url = url + "&country=" + searchCriteria.getCountryCode();
        }

        if (searchCriteria.getContinentCode() != null) {
            url = url + "&continentCode=" + searchCriteria.getContinentCode();
        }

        if (searchCriteria.getAdminCode1() != null) {
            url = url + "&adminCode1=" + URLEncoder.encode(searchCriteria.getAdminCode1(), "UTF8");
        }
        if (searchCriteria.getAdminCode2() != null) {
            url = url + "&adminCode2=" + URLEncoder.encode(searchCriteria.getAdminCode2(), "UTF8");
        }
        if (searchCriteria.getAdminCode3() != null) {
            url = url + "&adminCode3=" + URLEncoder.encode(searchCriteria.getAdminCode3(), "UTF8");
        }
        if (searchCriteria.getAdminCode4() != null) {
            url = url + "&adminCode4=" + URLEncoder.encode(searchCriteria.getAdminCode4(), "UTF8");
        }

        if (searchCriteria.getLanguage() != null) {
            url = url + "&lang=" + searchCriteria.getLanguage();
        }

        if (searchCriteria.getFeatureClass() != null) {
            url = url + "&featureClass=" + searchCriteria.getFeatureClass();
        }

        if (searchCriteria.getFeatureCodes() != null) {
            for (final String featureCode : searchCriteria.getFeatureCodes()) {
                url = url + "&fcode=" + featureCode;
            }
        }
        if (searchCriteria.getMaxRows() > 0) {
            url = url + "&maxRows=" + searchCriteria.getMaxRows();
        }
        if (searchCriteria.getStartRow() > 0) {
            url = url + "&startRow=" + searchCriteria.getStartRow();
        }

        if (searchCriteria.getStyle() != null) {
            url = url + "&style=" + searchCriteria.getStyle();
        }
        else {
            url = addDefaultStyle(url);
        }

        url = addUserName(url);

        final String searchResultJSON = webGetString(url);

        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final Toponym toponym = getToponymFromElement(ja.getJSONObject(i));
                    searchResult.toponyms.add(toponym);
                }
                searchResult.setTotalResultsCount(numMatches);
            }
        }
        return searchResult;
    }

    /**
     * returns the children in the administrative hierarchy of a toponym.
     * 
     * @param geonameId
     * @param language
     * @param style
     * @return
     * @throws Exception
     */
    public static ToponymSearchResult children(final int geonameId, final String language, final Style style) throws Exception {
        final ToponymSearchResult searchResult = new ToponymSearchResult();

        String url = "/childrenJSON?";

        url = url + "geonameId=" + geonameId;

        if (language != null) {
            url = url + "&lang=" + language;
        }

        if (style != null) {
            url = url + "&style=" + style;
        }
        else {
            url = addDefaultStyle(url);
        }
        url = addUserName(url);

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final Toponym toponym = getToponymFromElement(ja.getJSONObject(i));
                    searchResult.toponyms.add(toponym);
                }
            }
        }
        return searchResult;
    }

    // {
    // "totalResultsCount": 5,
    // "geonames": [
    // {
    // "alternateNames": [
    // {
    // "name": "Oostenryk",
    // "lang": "af"
    // },
    // {
    // "name": "áŠ¦áˆµá‰µáˆªá‹«",
    // "lang": "am"
    // },
    // {
    // "name": "Austria",
    // "lang": "an"
    // },
    // {
    // "name": "Ø§Ù„Ù†Ù…Ø³Ø§",
    // "lang": "ar"
    // },
    // {
    // "name": "Ð�ÑžÑ�Ñ‚Ñ€Ñ‹Ñ�",
    // "lang": "be"
    // },

    /**
     * returns the neighbours of a toponym.
     * 
     * @param geonameId
     * @param language
     * @param style
     * @return
     * @throws Exception
     */
    public static ToponymSearchResult neighbours(final int geonameId, final String language, final Style style) throws Exception {
        final ToponymSearchResult searchResult = new ToponymSearchResult();

        String url = "/neighboursJSON?";

        url = url + "geonameId=" + geonameId;

        if (language != null) {
            url = url + "&lang=" + language;
        }

        if (style != null) {
            url = url + "&style=" + style;
        }
        else {
            url = addDefaultStyle(url);
        }
        url = addUserName(url);

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final Toponym toponym = getToponymFromElement(ja.getJSONObject(i));
                    searchResult.toponyms.add(toponym);
                }
            }
        }

        return searchResult;
    }

    /**
     * returns the hierarchy for a geonameId
     * 
     * @see <a
     *      href="http://www.geonames.org/export/place-hierarchy.html#hierarchy">Hierarchy
     *      service description</a>
     * @param geonameId
     * @param language
     * @param style
     * @return
     * @throws Exception
     */
    public static List<Toponym> hierarchy(final int geonameId, final String language, final Style style) throws Exception {

        String url = "/hierarchyJSON?";

        url = url + "geonameId=" + geonameId;

        if (language != null) {
            url = url + "&lang=" + language;
        }

        if (style != null) {
            url = url + "&style=" + style;
        }
        else {
            url = addDefaultStyle(url);
        }
        url = addUserName(url);
        final List<Toponym> toponyms = new ArrayList<Toponym>();

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final Toponym toponym = getToponymFromElement(ja.getJSONObject(i));
                    toponyms.add(toponym);
                }
            }
        }

        return toponyms;
    }

    public static void saveTags(final String[] tags, final Toponym toponym, final String username, final String password) throws Exception {
        if (toponym.getGeoNameId() == 0) {
            throw new Error("no geonameid specified");
        }

        // FIXME proper url
        String url = "/servlet/geonames?srv=61";

        url = url + "&geonameId=" + toponym.getGeoNameId();
        url = addUserName(url);

        final StringBuilder tagsCommaseparated = new StringBuilder();
        for (final String tag : tags) {
            tagsCommaseparated.append(tag).append(",");
        }
        url = url + "&tag=" + tagsCommaseparated;

        // SAXBuilder parser = new SAXBuilder();
        // Document doc = parser.build(connect(url));
        //
        // Element root = rootAndCheckException(doc);
    }

    // private static Element rootAndCheckException(Document doc)
    // throws GeoNamesException {
    // // Element root = doc.getRootElement();
    // checkException(root);
    // return root;
    // }
    //
    // private static void checkException(Element root) throws GeoNamesException
    // {
    // // Element message = root.getChild("status");
    // if (message != null) {
    // int code = 0;
    // try {
    // code = Integer.parseInt(message.getAttributeValue("value"));
    // } catch (NumberFormatException numberFormatException) {
    // }
    // throw new GeoNamesException(code, message.getAttributeValue("message"));
    // }
    // }

    /**
     * full text search on geolocated wikipedia articles.
     * 
     * @param q
     * @param language
     * @return
     * @throws Exception
     */
    public static List<WikipediaArticle> wikipediaSearch(final String q, final String language) throws Exception {
        final List<WikipediaArticle> articles = new ArrayList<WikipediaArticle>();

        String url = "/wikipediaSearchJSON?";

        url = url + "q=" + URLEncoder.encode(q, "UTF8");

        if (language != null) {
            url = url + "&lang=" + language;
        }
        url = addUserName(url);

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final WikipediaArticle wikipediaArticle = getWikipediaArticleFromElement(ja.getJSONObject(i));
                    articles.add(wikipediaArticle);
                }
            }
        }
        return articles;
    }

    /**
     * full text search on geolocated wikipedia articles.
     * 
     * @param title
     * @param language
     * @return
     * @throws Exception
     */
    public static List<WikipediaArticle> wikipediaSearchForTitle(final String title, final String language) throws Exception {
        final List<WikipediaArticle> articles = new ArrayList<WikipediaArticle>();

        String url = "/wikipediaSearchJSON?";

        url = url + "title=" + URLEncoder.encode(title, "UTF8");

        if (language != null) {
            url = url + "&lang=" + language;
        }
        url = addUserName(url);

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final WikipediaArticle wikipediaArticle = getWikipediaArticleFromElement(ja.getJSONObject(i));
                    articles.add(wikipediaArticle);
                }
            }
        }

        return articles;
    }

    // {"geonames": [
    // {
    // "summary":
    // "The GlÃ¤rnisch is a mountain of the Glarus Alps, Switzerland, consisting of two ridges of either side of the GlÃ¤rnischfirn glacier: the Ruchen at 2901 m to the west, rising above the KlÃ¶ntalersee, and the BÃ¤chistock at 2914 m to the southwest. The best known peak is the VrenelisgÃ¤rtli at 2904 m.  (...)",
    // "distance": "0.1869",
    // "rank": 80,
    // "title": "GlÃ¤rnisch",
    // "wikipediaUrl": "en.wikipedia.org/wiki/Gl%C3%A4rnisch",
    // "elevation": 2880,
    // "countryCode": "CH",
    // "lng": 8.998611,
    // "feature": "mountain",
    // "lang": "en",
    // "lat": 46.998611
    // },
    // {
    // "summary":
    // "The Linth (pronounced \"lint\") is a Swiss river starting above Linthal the mountains of Glarus near the Klausen Pass and flowing from there north through the Glarus valley passing Schwanden, where it is joined by its main tributary Sernft, Ennenda, the town of Glarus, Netstal, and NÃ¤fels, from where (...)",
    // "distance": "2.0229",
    // "rank": 95,
    // "title": "Linth",
    // "wikipediaUrl": "en.wikipedia.org/wiki/Linth",
    // "countryCode": "CH",
    // "lng": 9.0095,
    // "lang": "en",
    // "lat": 46.983
    // }}

    public static List<WikipediaArticle> findNearbyWikipedia(final double latitude, final double longitude, final String language) throws Exception {
        return findNearbyWikipedia(latitude, longitude, 0, language, 0);
    }

    /* Overload function to allow backward compatibility */
    /**
     * Based on the following inform: Webservice Type : REST
     * ws.geonames.org/findNearbyWikipedia? Parameters : lang : language code
     * (around 240 languages) (default = en) lat,lng, radius (in km), maxRows
     * (default = 5) Example:
     * http://ws.geonames.org/findNearbyWikipedia?lat=47&lng=9
     * 
     * @param latitude
     * @param longitude
     * @param radius
     * @param language
     * @param maxRows
     * @param: latitude
     * @param: longitude
     * @param: radius
     * @param: language
     * @param: maxRows
     * @return: list of wikipedia articles
     * @throws: Exception
     */
    public static List<WikipediaArticle> findNearbyWikipedia(final double latitude, final double longitude, final double radius, final String language,
            final int maxRows) throws Exception {

        final List<WikipediaArticle> articles = new ArrayList<WikipediaArticle>();

        String url = "/findNearbyWikipediaJSON?";

        url = url + "lat=" + latitude;
        url = url + "&lng=" + longitude;
        if (radius > 0) {
            url = url + "&radius=" + radius;
        }
        if (maxRows > 0) {
            url = url + "&maxRows=" + maxRows;
        }

        if (language != null) {
            url = url + "&lang=" + language;
        }
        url = addUserName(url);

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONArray ja = json.getJSONArray("geonames");

                final int numMatches = ja.length();
                Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                for (int i = 0; i < numMatches; i++) {
                    final WikipediaArticle wikipediaArticle = getWikipediaArticleFromElement(ja.getJSONObject(i));
                    articles.add(wikipediaArticle);
                }
            }
        }

        return articles;
    }

    /**
     * GTOPO30 is a global digital elevation model (DEM) with a horizontal grid
     * spacing of 30 arc seconds (approximately 1 kilometer). GTOPO30 was
     * derived from several raster and vector sources of topographic
     * information.
     * 
     * @param latitude
     * @param longitude
     * @return a single number giving the elevation in meters according to
     *         gtopo30, ocean areas have been masked as "no data" and have been
     *         assigned a value of -9999
     * @throws IOException
     */
    public static int gtopo30(final double latitude, final double longitude) throws IOException {
        String url = "/gtopo30?lat=" + latitude + "&lng=" + longitude;
        url = addUserName(url);
        final BufferedReader in = new BufferedReader(new InputStreamReader(connect(url)));
        final String gtopo30 = in.readLine();
        in.close();
        return Integer.parseInt(gtopo30);
    }

    /**
     * Shuttle Radar Topography Mission (SRTM) elevation data. SRTM consisted of
     * a specially modified radar system that flew onboard the Space Shuttle
     * Endeavour during an 11-day mission in February of 2000. The dataset
     * covers land areas between 60 degrees north and 56 degrees south. This web
     * service is using SRTM3 data with data points located every 3-arc-second
     * (approximately 90 meters) on a latitude/longitude grid.
     * 
     * @param latitude
     * @param longitude
     * @return elevation or -32768 if unknown
     * @throws IOException
     */
    public static int srtm3(final double latitude, final double longitude) throws IOException {
        String url = "/srtm3?lat=" + latitude + "&lng=" + longitude;
        url = addUserName(url);
        final BufferedReader in = new BufferedReader(new InputStreamReader(connect(url)));
        final String srtm3 = in.readLine();
        in.close();
        return Integer.parseInt(srtm3);
    }

    public static int[] srtm3(final double[] latitude, final double[] longitude) throws IOException {
        if (latitude.length != longitude.length) {
            throw new Error("number of lats and longs must be equal");
        }
        final int[] elevation = new int[latitude.length];
        String lats = "";
        String lngs = "";
        for (int i = 0; i < elevation.length; i++) {
            lats += latitude[i] + ",";
            lngs += longitude[i] + ",";
        }
        String url = "/srtm3?lats=" + lats + "&lngs=" + lngs;
        url = addUserName(url);
        final BufferedReader in = new BufferedReader(new InputStreamReader(connect(url)));
        for (int i = 0; i < elevation.length; i++) {
            final String srtm3 = in.readLine();
            elevation[i] = Integer.parseInt(srtm3);
        }
        in.close();
        return elevation;
    }

    public static int astergdem(final double latitude, final double longitude) throws IOException {
        String url = "/astergdem?lat=" + latitude + "&lng=" + longitude;
        url = addUserName(url);
        final BufferedReader in = new BufferedReader(new InputStreamReader(connect(url)));
        final String astergdem = in.readLine();
        in.close();
        return Integer.parseInt(astergdem);
    }

    public static int[] astergdem(final double[] latitude, final double[] longitude) throws IOException {
        if (latitude.length != longitude.length) {
            throw new Error("number of lats and longs must be equal");
        }
        final int[] elevation = new int[latitude.length];
        String lats = "";
        String lngs = "";
        for (int i = 0; i < elevation.length; i++) {
            lats += latitude[i] + ",";
            lngs += longitude[i] + ",";
        }
        String url = "/astergdem?lats=" + lats + "&lngs=" + lngs;
        url = addUserName(url);
        final BufferedReader in = new BufferedReader(new InputStreamReader(connect(url)));
        for (int i = 0; i < elevation.length; i++) {
            final String astergdem = in.readLine();
            elevation[i] = Integer.parseInt(astergdem);
        }
        in.close();
        return elevation;
    }

    public Integer getElevationFor(final double longitude, final double latitude) throws IOException {
        int elevation = astergdem(longitude, latitude);

        if (elevation == -9999) {
            elevation = srtm3(longitude, latitude);

            if (elevation == -32768) {
                elevation = gtopo30(longitude, latitude);
            }
            if (elevation == -32768) {
                elevation = 0; // sea level 3 times asured...
            }
        }
        return elevation;
    }

    /**
     * The iso country code of any given point. It is calling
     * {@link #countryCode(double, double, double)} with radius=0.0
     * 
     * @param latitude
     * @param longitude
     * @return
     * @throws IOException
     */
    public static String countryCode(final double latitude, final double longitude) throws IOException {
        return countryCode(latitude, longitude, 0);
    }

    /**
     * The iso country code of any given point with radius for coastal areas.
     * 
     * @param latitude
     * @param longitude
     * @param radius
     * @return iso country code for the given latitude/longitude
     * @throws IOException
     */
    private static String countryCode(final double latitude, final double longitude, final double radius) throws IOException {
        String url = "/countryCode?lat=" + latitude + "&lng=" + longitude;
        if (radius != 0) {
            url += "&radius=" + radius;
        }
        url = addUserName(url);
        final BufferedReader in = new BufferedReader(new InputStreamReader(connect(url)));
        final String cc = in.readLine();
        in.close();
        if ((cc != null) && (cc.length() == 2)) {
            return cc;
        }
        return null;
    }

    /**
     * get the timezone for a given location
     * 
     * @param latitude
     * @param longitude
     * @return timezone at the given location
     * @throws IOException
     * @throws Exception
     */

    /*
     * { "time":"2011-10-09 10:59", "countryName":"Austria",
     * "sunset":"2011-10-09 18:44", "rawOffset":1, "dstOffset":2,
     * "countryCode":"AT", "gmtOffset":1, "lng":10.2,
     * "sunrise":"2011-10-09 07:28", "timezoneId":"Europe/Vienna", "lat":47.01 }
     */

    private static Timezone getTimezoneFromJSON(final JSONObject json) {
        final Timezone timezone = new Timezone();
        final SimpleDateFormat df = new SimpleDateFormat(WebService.minuteDateFmt);

        timezone.setTimezoneId(getJSONStringValue(json, "timezoneId"));
        timezone.setCountryCode(getJSONStringValue(json, "countryCode"));
        timezone.setCountryName(getJSONStringValue(json, "countryName"));

        try {
            timezone.setTime(df.parse(getJSONStringValue(json, "time")));
        }
        catch (final Exception x) {
            Ut.dd("JSON date parsing failed");
        }
        try {
            timezone.setSunrise(df.parse(getJSONStringValue(json, "sunrise")));
        }
        catch (final Exception x) {
            Ut.dd("JSON date parsing failed");
        }
        try {
            timezone.setSunset(df.parse(getJSONStringValue(json, "sunset")));
        }
        catch (final Exception x) {
            Ut.dd("JSON date parsing failed");
        }

        timezone.setGmtOffset(getJSONIntValue(json, "gmtOffset"));
        timezone.setDstOffset(getJSONIntValue(json, "dstOffset"));

        return timezone;
    }

    public static Timezone timezone(final double latitude, final double longitude) throws Exception {
        Timezone timezone = null;
        String url = "/timezoneJSON?";

        url = url + "&lat=" + latitude;
        url = url + "&lng=" + longitude;
        url = addUserName(url);

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                // final JSONArray ja = json.getJSONArray("geonames");
                //
                // final int numMatches = ja.length();
                // Ut.d("\nNo. of Geonames matches: " + numMatches + "\n");
                // if (numMatches == 0) {
                // return null;
                // }
                timezone = getTimezoneFromJSON(json);
            }
        }

        return timezone;
    }

    // {
    // "weatherObservation":{
    // "clouds":"few clouds",
    // "weatherCondition":"n/a",
    // "observation":"LESO 100630Z VRB01KT 9999 FEW003 11/10 Q1028",
    // "ICAO":"LESO",
    // "elevation":8,
    // "countryCode":"ES",
    // "lng":-1.8,
    // "temperature":"11",
    // "dewPoint":"10",
    // "windSpeed":"01",
    // "humidity":93,
    // "stationName":"San Sebastian / Fuenterrabia",
    // "datetime":"2011-10-10 06:30:00",
    // "lat":43.35,
    // "hectoPascAltimeter":1028
    // }
    // }
    /**
     * @param latitude
     * @param longitude
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static WeatherObservation findNearByWeather(final double latitude, final double longitude) throws Exception {

        String url = "/findNearByWeatherJSON?";

        WeatherObservation weatherObservation = null;

        url = url + "&lat=" + latitude;
        url = url + "&lng=" + longitude;
        url = addUserName(url);

        final String searchResultJSON = webGetString(url);
        if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
            final JSONObject json = new JSONObject(searchResultJSON);
            if (json != null) {
                final JSONObject jsonWeather = json.getJSONObject("weatherObservation");
                if (jsonWeather != null) {
                    weatherObservation = getWeatherObservationFromElement(jsonWeather);
                }

                // JSONArray ja = json.getJSONArray("weatherObservation");
                // int numMatches = ja.length();
                // System.out.println("\nNo. of matches: " + numMatches + "\n");
                // if (numMatches == 0) {
                // return null;
                // }
                // for (int i = 0; i < numMatches; i++) {
                // weatherObservation = getWeatherObservationFromElement(ja
                // .getJSONObject(i));
                //
                // }
            }
        }
        return weatherObservation;
    }

    // SAXBuilder parser = new SAXBuilder();
    // Document doc = parser.build(connect(url));
    //
    // Element root = rootAndCheckException(doc);
    // for (Object obj : root.getChildren("observation")) {
    // Element weatherObservationElement = (Element) obj;
    // WeatherObservation weatherObservation =
    // getWeatherObservationFromElement(weatherObservationElement);
    // return weatherObservation;
    // }
    //
    // return null;
    // }

    // {"weatherObservation": {
    // "clouds": "few clouds",
    // "weatherCondition": "n/a",
    // "observation":
    // "LSZH 011520Z VRB02KT 9999 FEW045 SCT220 19/08 Q1023 NOSIG",
    // "ICAO": "LSZH",
    // "elevation": 432,
    // "countryCode": "CH",
    // "lng": 8.516666666666667,
    // "temperature": "19",
    // "dewPoint": "8",
    // "windSpeed": "02",
    // "humidity": 48,
    // "stationName": "Zurich-Kloten",
    // "datetime": "2011-04-01 15:20:00",
    // "lat": 47.46666666666667,
    // "hectoPascAltimeter": 1023
    // }}
    // public static WeatherObservation weatherIcao(String icaoCode)
    // throws Exception {
    //
    // String url = "/weatherIcaoJSON?";
    //
    // url = url + "&ICAO=" + icaoCode;
    // url = addUserName(url);
    //
    // String searchResultJSON = webGetString(url);
    // if ((searchResultJSON != null) && (searchResultJSON.length() > 0)) {
    // JSONObject json = new JSONObject(searchResultJSON);
    // if (json != null) {
    // JSONArray ja = json.getJSONArray("weatherObservation");
    //
    // int numMatches = ja.length();
    // System.out.println("\nNo. of matches: " + numMatches + "\n");
    // if (numMatches == 0) {
    // return null;
    // }
    // for (int i = 0; i < numMatches; i++) {
    // WeatherObservation weatherObservation
    // =getWeatherObservationFromElement(ja.getJSONObject(i));
    // return weatherObservation;
    // }
    // }
    // }
    //
    // return null;
    // }

    /**
     * @return the geoNamesServer, default is http://ws.geonames.org
     */
    public static String getGeoNamesServer() {
        return WebService.geoNamesServer;
    }

    /**
     * @return the geoNamesServerFailover
     */
    public static String getGeoNamesServerFailover() {
        return WebService.geoNamesServerFailover;
    }

    /**
     * sets the server name for the GeoNames server to be used for the requests.
     * Default is ws.geonames.org
     * 
     * @param geoNamesServer
     *            the geonamesServer to set
     * @param pGeoNamesServer
     */
    public static void setGeoNamesServer(final String pGeoNamesServer) {
        if (pGeoNamesServer == null) {
            throw new Error();
        }
        String geonamesServer = pGeoNamesServer.trim().toLowerCase();
        // add default http protocol if it is missing
        if (!geonamesServer.startsWith("http://") && !geonamesServer.startsWith("https://")) {
            geonamesServer = "http://" + geonamesServer;
        }
        WebService.geoNamesServer = geonamesServer;
    }

    /**
     * sets the default failover server for requests in case the main server is
     * not accessible. Default is ws.geonames.org<br>
     * The failover server is only called if it is different from the main
     * server.<br>
     * The failover server is used for commercial GeoNames web service users.
     * 
     * @param geoNamesServerFailover
     *            the geoNamesServerFailover to set
     */
    public static void setGeoNamesServerFailover(final String geoNamesServerFailover) {
        String failOverServer = geoNamesServerFailover;
        if (failOverServer != null) {
            failOverServer = failOverServer.trim().toLowerCase();
            if (!failOverServer.startsWith("http://")) {
                failOverServer = "http://" + failOverServer;
            }
        }
        WebService.geoNamesServerFailover = failOverServer;
    }

    /**
     * @return the userName
     */
    public static String getUserName() {
        return WebService.userName;
    }

    /**
     * Sets the user name to be used for the requests. Needed to access the
     * commercial GeoNames web services.
     * 
     * @param userName
     *            the userName to set
     */
    public static void setUserName(final String userName) {
        WebService.userName = userName;
    }

    /**
     * @return the token
     */
    public static String getToken() {
        return WebService.token;
    }

    /**
     * sets the token to be used to authenticate the requests. This is an
     * optional parameter for the commercial version of the GeoNames web
     * services.
     * 
     * @param token
     *            the token to set
     */
    public static void setToken(final String token) {
        WebService.token = token;
    }

    /**
     * @return the defaultStyle
     */
    public static Style getDefaultStyle() {
        return WebService.defaultStyle;
    }

    /**
     * @param defaultStyle
     *            the defaultStyle to set
     */
    public static void setDefaultStyle(final Style defaultStyle) {
        WebService.defaultStyle = defaultStyle;
    }

    /**
     * @return the readTimeOut
     */
    public static int getReadTimeOut() {
        return WebService.readTimeOut;
    }

    /**
     * @param readTimeOut
     *            the readTimeOut to set
     */
    public static void setReadTimeOut(final int readTimeOut) {
        WebService.readTimeOut = readTimeOut;
    }

    /**
     * @return the connectTimeOut
     */
    public static int getConnectTimeOut() {
        return WebService.connectTimeOut;
    }

    /**
     * @param connectTimeOut
     *            the connectTimeOut to set
     */
    public static void setConnectTimeOut(final int connectTimeOut) {
        WebService.connectTimeOut = connectTimeOut;
    }

    // google search

    // [{"region":"Lower Austria",
    // "streetAddress":"2486 Pottendorf",
    // "titleNoFormatting":"2486 Pottendorf",
    // "staticMapUrl":"http:\/\/maps.google.com\/maps\/api\/staticmap?maptype=roadmap&format=gif&sensor=false&size=150x100&zoom=13&markers=47.91149,16.38802",
    // "listingType":"local",
    // "addressLines":["2486 Pottendorf","Austria"],
    // "lng":"16.38802",
    // "url":"http:\/\/www.google.com\/maps?source=uds&q=Pottendorf",
    // "country":"AT",
    // "city":"Pottendorf",
    // "GsearchResultClass":"GlocalSearch",
    // "maxAge":604800,
    // "addressLookupResult":"\/maps",
    // "title":"2486 Pottendorf",
    // "postalCode":"",
    // "ddUrlToHere":"http:\/\/www.google.com\/maps?source=uds&daddr=2486+Pottendorf,+Pottendorf,+Lower+Austria+(2486+Pottendorf)+@47.91149,16.38802&iwstate1=dir:to",
    // "ddUrl":"http:\/\/www.google.com\/maps?source=uds&daddr=2486+Pottendorf,+Pottendorf,+Lower+Austria+(2486+Pottendorf)+@47.91149,16.38802&saddr=48.208174,16.373819",
    // "ddUrlFromHere":"http:\/\/www.google.com\/maps?source=uds&saddr=2486+Pottendorf,+Pottendorf,+Lower+Austria+(2486+Pottendorf)+@47.91149,16.38802&iwstate1=dir:from",
    // "accuracy":"4",
    // "lat":"47.91149"
    // }]
    //

    private static final String GOOGLE_SEARCH_URL = "http://ajax.googleapis.com/ajax/services/search/local?v=1.0&sll=";

    public static GoogleSearchResult doGoogleSearch(final String mapCenter, final String queryString) {
        final GoogleSearchResult results = new GoogleSearchResult();

        URL url;
        try {
            url = new URL(WebService.GOOGLE_SEARCH_URL + mapCenter + "&q=" + URLEncoder.encode(queryString, "UTF-8") + "");
            final String str = webGetString(url);

            final JSONObject json = new JSONObject(str);
            // Ut.dd(json.toString(4)); //
            final JSONArray result = (JSONArray) ((JSONObject) json.get("responseData")).get("results");
            // Ut.dd("results.length="+results.length());

            for (int i = 0; i < result.length(); i++) {
                results.getGoogleResults().add(GoogleResult.fromJSonElement(result.getJSONObject(0)));
            }

        }
        catch (final Exception e) {
            // e.printStackTrace();
        }

        // Ut.dd(url.toString());
        // in = new BufferedInputStream(url.openStream(),
        // StreamUtils.IO_BUFFER_SIZE);
        //
        // final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        // out = new BufferedOutputStream(dataStream,
        // StreamUtils.IO_BUFFER_SIZE);
        // StreamUtils.copy(in, out);
        // out.flush();
        //
        // String str = dataStream.toString();

        return results;
    }

    /*
     * http://www.panoramio.com/map/get_panoramas.php?set=public&from=0&to=20&minx
     * =-180&miny=-90&maxx=180&maxy=90&size=medium&mapfilter=true for "set" you
     * can use: public (popular photos) full (all photos) user ID number for
     * "size" you can use: original medium (default value) small thumbnail
     * square mini_square
     */

    /*
     * result : { "count": 773840,"photos": [ { "photo_id": 532693,
     * "photo_title": "Wheatfield in afternoon light", "photo_url":
     * "http://www.panoramio.com/photo/532693", "photo_file_url":
     * "http://static2.bareka.com/photos/medium/532693.jpg", "longitude":
     * 11.280727, "latitude": 59.643198, "width": 500, "height": 333,
     * "upload_date": "22 January 2007", "owner_id": 39160, "owner_name":
     * "Snemann", "owner_url": "http://www.panoramio.com/user/39160", }, {
     * "photo_id": 505229, "photo_title": "Etangs près de Dijon", "photo_url":
     * "http://www.panoramio.com/photo/505229", "photo_file_url":
     * "http://static2.bareka.com/photos/medium/505229.jpg", "longitude":
     * 5.168552, "latitude": 47.312642, "width": 350, "height": 500,
     * "upload_date": "20 January 2007", "owner_id": 78506, "owner_name":
     * "Philippe Stoop", "owner_url": "http://www.panoramio.com/user/78506" },
     * ... ] }
     */
    public static List<PanoramioItem> getImages(final double lat, final double lon, final int radius) {

        List<PanoramioItem> items = null;

        Ut.dd(String.format("Searching Panoramio close to (%.6f, %.6f) Radius (m): %d\n", lat, lon, radius));

        final double[] bbox = GeoMathUtil.boundingBoxCoords(lat, lon, radius);
        Ut.dd(String.format("Bounding box: Min: (%.6f, %.6f); Max:(%.6f, %.6f)\n\n", bbox[0], bbox[1], bbox[2], bbox[3]));
        try {
            final String urlStr = WebService.PANORAMIO_URL + "?set=public&from=0&to=20&" + // full;
                                                                                           // first
                                                                                           // 20
                                                                                           // images
                    "miny=" + bbox[0] + "&minx=" + bbox[1] + "&" + "maxy=" + bbox[2] + "&maxx=" + bbox[3] + "&" + "size=square";
            final URL url = new URL(urlStr);
            final String jsonStr = webGetString(url);

            if ((jsonStr != null) && (jsonStr.length() > 0)) {
                final JSONObject json = new JSONObject(jsonStr);
                items = extractMatches(json);
            }

        }
        catch (final Exception x) {
            Ut.e("Panoramio Exception: " + x);
        }
        return items;

    }

    // private static int selectClosestItem(PanoramioItem[] items, double lat,
    // double lon) // search the Panoramio results for the image closest to
    // // (lat,lon)
    // {
    // int itemIdx = 0;
    // double minDist = GeoMathUtil.distanceApart(lat, lon,
    // items[itemIdx].getLatitude(), items[itemIdx].getLongitude());
    // for (int i = 1; i < items.length; i++) {
    // double distApart = GeoMathUtil.distanceApart(lat, lon,
    // items[i].getLatitude(), items[i].getLongitude());
    // if (distApart < minDist) {
    // minDist = distApart;
    // itemIdx = i;
    // }
    // }
    // return itemIdx;
    // } // end of selectClosestItem()

    private static List<PanoramioItem> extractMatches(final JSONObject json) /*
                                                                              * list
                                                                              * the
                                                                              * results
                                                                              * ,
                                                                              * and
                                                                              * return
                                                                              * them
                                                                              * as
                                                                              * an
                                                                              * array
                                                                              * of
                                                                              * PanoramioItem
                                                                              * objects
                                                                              */{
        List<PanoramioItem> items = null;
        try {

            final int count = Integer.parseInt(json.getString("count"));
            if (count > 0) {

                // list the photo titles and their URLs
                final JSONArray ja = json.getJSONArray("photos");
                final int numMatches = ja.length();
                System.out.println("\nNo. of matches: " + numMatches + "\n");
                if (numMatches == 0) {
                    return null;
                }
                items = new ArrayList<PanoramioItem>();
                // PanoramioItem[] items = new PanoramioItem[numMatches];

                for (int i = 0; i < numMatches; i++) {
                    final PanoramioItem item = new PanoramioItem(ja.getJSONObject(i));
                    items.add(item);

                    Ut.d((i + 1) + ". ");
                    Ut.d("Photo Title: " + item.getPhotoTitle());
                    // Ut.d("   (lat,long): (%.6f,%,6f)\n"+
                    // items[i].getLatitude()+" "+ items[i].getLongitude());
                    Ut.d("   URL: " + item.getPhotoFileURL() + "\n");
                }
                return items;
            }
            else {
                System.out.println("\nNo matches found");
            }
        }
        catch (final Exception e) {
            System.out.println(e);
        }

        return null;

    }

    /**
     * Yahoo reversegeocoding
     * 
     * @param latitude
     * @param longitude
     * @return
     */
    public static GeoCodeResult reverseGeoCode(final double latitude, final double longitude) {

        GeoCodeResult result = null;
        try {

            final String urlStr = String.format(WebService.YAHOO_API_BASE_URL, String.valueOf(latitude), String.valueOf(longitude)) + WebService.YAHOO_API_KEY;
            final URL url = new URL(urlStr);
            final String jsonStr = webGetString(url);

            result = extractGeoCodeResult(jsonStr);

        }
        catch (final Exception x) {
            Ut.d("Yahoo reverse geocoding failed");
        }
        return result;

    }

    private static GeoCodeResult extractGeoCodeResult(final String yahooAnswer) {
        GeoCodeResult result = null;
        try {
            if ((yahooAnswer != null) && (yahooAnswer.length() > 0)) {
                JSONObject json = new JSONObject(yahooAnswer);
                json = json.getJSONObject("ResultSet");
                final JSONArray jsonArray = json.getJSONArray("Results");

                // for now we only take out the first one.

                final int nrOfEntries = jsonArray.length();

                // for (int i = 0; i < nrOfEntries; i++) {

                if (nrOfEntries > 0) {
                    result = new GeoCodeResult();
                    // result.line1 = jsonArray.
                    final JSONObject addressObject = jsonArray.getJSONObject(0);

                    result.setLine1(addressObject.getString("line1"));
                    result.setLine2(addressObject.getString("line2"));
                    result.setLine3(addressObject.getString("line3"));
                    result.setLine4(addressObject.getString("line4"));
                    result.setCountry(addressObject.getString("country"));
                    result.setCity(addressObject.getString("city"));

                }

                // }

            }
        }
        catch (final Exception x) {
            Ut.d("Yahoo Response not parsable...");
        }
        return result;
    }

}
