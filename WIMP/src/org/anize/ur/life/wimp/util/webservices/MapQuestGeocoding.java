package org.anize.ur.life.wimp.util.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import org.anize.ur.life.wimp.models.GeoCodeResult;
import org.anize.ur.life.wimp.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapQuestGeocoding {
	public static int readTimeOut = 120000;
	public static int connectTimeOut = 10000;
	private static final String USER_AGENT = "geonames-webservice-client-1.0.6";

	private static final String MQ_API_BASE_URL = "http://www.mapquestapi.com/geocoding/v1/reverse?key=%s&location=%.4f,%.4f";
	private static final String MQ_API_KEY = "Fmjtd%7Cluu221082l%2C7g%3Do5-5fbn9";

	private static InputStream connect(final String url) throws IOException {
		InputStream in = null;
		try {
			final URLConnection conn = new URL(url).openConnection();
			conn.setConnectTimeout(MapQuestGeocoding.connectTimeOut);
			conn.setReadTimeout(MapQuestGeocoding.readTimeOut);
			conn.setRequestProperty("User-Agent", MapQuestGeocoding.USER_AGENT);
			in = conn.getInputStream();
			return in;
		} catch (final IOException e) {
			// we cannot reach the server
			Util.d("problems connecting to geonames url " + url + "Exception:"
					+ e);
		}
		return in;
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
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(connect(url)));
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return sb.toString();
		} catch (final Exception e) {
			Util.d(e.toString());
		}

		return null;
	} // end of webGetString()

	/**
	 * MapQuest reversegeocoding
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static GeoCodeResult reverseGeoCode(final double latitude,
			final double longitude) {

		final String urlStr = String.format(Locale.US, MapQuestGeocoding.MQ_API_BASE_URL,MQ_API_KEY,
				latitude, longitude);

		final String jsonStr = webGetString(urlStr);

		return extractGeoCodeResult(jsonStr);

	}
/*
 
 {
   "results":[
      {
         "locations":[
            {
               "latLng":{
                  "lng":-76.329999,
                  "lat":40.0755
               },
               "adminArea4":"Lancaster",
               "adminArea5Type":"City",
               "adminArea4Type":"County",
               "adminArea5":"Lancaster",
               "street":"300 Granite Run Dr",
               "adminArea1":"US",
               "adminArea3":"PA",
               "type":"s",
               "displayLatLng":{
                  "lng":-76.329999,
                  "lat":40.0755
               },
               "linkId":90819339,
               "postalCode":"17601",
               "sideOfStreet":"L",
               "dragPoint":false,
               "adminArea1Type":"Country",
               "geocodeQuality":"ADDRESS",
               "geocodeQualityCode":"L1AAA",
               "mapUrl":"http://www.mapquestapi.com/staticmap/v4/getmap?key=Fmjtd|luu221082l,7g=o5-5fbn9&type=map&size=225,160&pois=purple-1,40.0755,-76.329999,0,0|&center=40.0755,-76.329999&zoom=15&rand=-1095356024",
               "adminArea3Type":"State"
            }
         ],
         "providedLocation":{
            "latLng":{
               "lng":-76.329999,
               "lat":40.0755
            }
         }
      }
   ],
   "options":{
      "ignoreLatLngInput":false,
      "maxResults":-1,
      "thumbMaps":true
   },
   "info":{
      "copyright":{
         "text":"© 2013 MapQuest, Inc.",
         "imageUrl":"http://api.mqcdn.com/res/mqlogo.gif",
         "imageAltText":"© 2013 MapQuest, Inc."
      },
      "statuscode":0,
      "messages":[

      ]
   }
} 
  
  
  
 */
	
	
	private static GeoCodeResult extractGeoCodeResult(final String responseJSON) {
		GeoCodeResult result = null;
		try {
			if ((responseJSON != null) && (responseJSON.length() > 0)) {
				JSONObject json = new JSONObject(responseJSON);				
				JSONArray jsonArray = json.getJSONArray("results");
				
				jsonArray = jsonArray.getJSONObject(0).getJSONArray("locations");

				// for now we only take out the first one.

				final int nrOfEntries = jsonArray.length();

				// for (int i = 0; i < nrOfEntries; i++) {

				if (nrOfEntries > 0) {
					result = new GeoCodeResult();
					// result.line1 = jsonArray.
					final JSONObject addressObject = jsonArray.getJSONObject(0);

					result.setLine1(addressObject.getString("street"));
					result.setLine2(addressObject.getString("adminArea3"));
					result.setLine3(addressObject.getString("adminArea4"));
					result.setLine4(addressObject.getString("postalCode"));
					result.setCountry(addressObject.getString("adminArea1"));
					result.setCity(addressObject.getString("adminArea5"));

				}

				// }

			}
		} catch (final Exception x) {
			System.out.println("MapQuest Response not parsable..."+x.toString());
		}
		return result;
	}

	

}
