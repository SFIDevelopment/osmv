package org.anize.ur.life.wimp.util.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.anize.ur.life.wimp.models.GeoCodeResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class GoogleReverseGeocoding {

	public static GeoCodeResult getFromLocation(final double lat,
			final double lon) {
		
		final String urlStr = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + ","
				+ lon + "&sensor=true";
		String response = "";
		
		final HttpClient client = new DefaultHttpClient();

		GeoCodeResult gcr = new GeoCodeResult();
		
		Log.d("ReverseGeocode", urlStr);
		try {
			final HttpResponse hr = client.execute(new HttpGet(urlStr));
			final HttpEntity entity = hr.getEntity();

			final BufferedReader br = new BufferedReader(new InputStreamReader(
					entity.getContent()));

			String buff = null;
			while ((buff = br.readLine()) != null) {
				response += buff;
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		JSONArray responseArray = null;
		try {
			final JSONObject jsonObject = new JSONObject(response);
			responseArray = jsonObject.getJSONArray("results");
			
			String singleLineAddres = responseArray.getJSONObject(0).getString("formatted_address");
			
			StringTokenizer tokenizer = new StringTokenizer(singleLineAddres,",");
			
			if (tokenizer.hasMoreTokens())
				gcr.setLine1(tokenizer.nextToken());
			if (tokenizer.hasMoreTokens())
				gcr.setLine2(tokenizer.nextToken());
			if (tokenizer.hasMoreTokens())
				gcr.setLine3(tokenizer.nextToken());
			if (tokenizer.hasMoreTokens())
				gcr.setLine4(tokenizer.nextToken());
			
			
			
			
		} catch (final JSONException e) {
			Log.d("ReverseGeocode", " result not parsable: " + e.getMessage());			
		}

		Log.d("ReverseGeocode", "" + responseArray.length() + " result(s)");

//		for (int i = 0; (i < responseArray.length()) && (i <= (maxResults - 1)); i++) {
//			
//			final Address addy = new Address(Locale.getDefault());
//
//			try {
//				JSONObject jsl = responseArray.getJSONObject(i);
//
//				String addressLine = jsl.getString("address");
//
//				if (addressLine.contains(",")) {
//					addressLine = addressLine.split(",")[0];
//				}
//
//				addy.setAddressLine(0, addressLine);
//
//				jsl = jsl.getJSONObject("AddressDetails").getJSONObject(
//						"Country");
//
//				try {
//					addy.setCountryName(jsl.getString("CountryName"));
//				} catch (final JSONException e) {
//					e.printStackTrace();
//				}
//				try {
//					addy.setCountryCode(jsl.getString("CountryNameCode"));
//				} catch (final JSONException e) {
//					e.printStackTrace();
//				}
//
//				try {
//					jsl = jsl.getJSONObject("AdministrativeArea");
//					addy.setAdminArea(jsl.getString("AdministrativeAreaName"));
//				} catch (final JSONException e) {
//					e.printStackTrace();
//				}
//
//				try {
//					jsl = jsl.getJSONObject("SubAdministrativeArea");
//					addy.setSubAdminArea(jsl
//							.getString("SubAdministrativeAreaName"));
//				} catch (final JSONException e) {
//					e.printStackTrace();
//				}
//
//				try {
//					jsl = jsl.getJSONObject("Locality");
//					addy.setLocality(jsl.getString("LocalityName"));
//				} catch (final JSONException e) {
//					e.printStackTrace();
//				}
//
//				try {
//					addy.setPostalCode(jsl.getJSONObject("PostalCode")
//							.getString("PostalCodeNumber"));
//				} catch (final JSONException e) {
//					e.printStackTrace();
//				}
//
//				try {
//					addy.setThoroughfare(jsl.getJSONObject("Thoroughfare")
//							.getString("ThoroughfareName"));
//				} catch (final JSONException e) {
//					e.printStackTrace();
//				}
//
//			} catch (final JSONException e) {
//				e.printStackTrace();
//				continue;
//			}
//
//			results.add(addy);
//		}

		return gcr;
	}
}