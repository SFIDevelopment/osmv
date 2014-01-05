package at.the.gogo.gpxviewer.util.panoramio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import at.the.gogo.gpxviewer.util.BitmapUtils;
import at.the.gogo.gpxviewer.util.GeoMath;

import com.google.android.gms.maps.model.LatLng;


public class ImageForLocationHandler {

	public static HashMap<String, Bitmap> imageCache = new HashMap<String, Bitmap>();

	public class DownloadPanoImageTask extends
			AsyncTask<LatLng, Integer, Bitmap> {

		// private static final String THUMBNAIL_URL =
		// "//www.panoramio.com/map/get_panoramas.php?order=popularity&set=public&from=0&to=1&miny=%.6f&minx=%.6f&maxy=%.6f&maxx=%.6f&size=medium&mapfilter=true";

		private Context mContext; // reference to the calling Activity
		int progress = -1;
		Bitmap downloadedImage = null;
		ImageLoadingCompleted completionNotifier;

		DownloadPanoImageTask(Context context) {
			mContext = context;

		}

		public void setCompletionNotifyer(ImageLoadingCompleted completionNotifier)
		{
			this.completionNotifier=completionNotifier;
		}
		
		// Called from main thread to re-attach
		protected void setContext(Context context) {
			mContext = context;
			if (progress >= 0) {
				publishProgress(this.progress);
			}
		}

//		protected void onPreExecute() {
//			progress = 0;
//			// We could do some other setup work here before doInBackground()
//			// runs
//		}

		protected Bitmap doInBackground(LatLng... position) {

			double[] bbox = GeoMath.boundingBoxCoords(position[0].latitude,
					position[0].longitude, 300);

			String cacheKey = position[0].latitude + " "
					+ position[0].longitude;

			if (imageCache.containsKey(cacheKey)) {
				downloadedImage = imageCache.get(cacheKey);
			} else {
				String urlStr = "http://www.panoramio.com/map/get_panoramas.php?"
						+ "set=public&from=0&to=2&"
						+ // full; first 2 images
						"miny="
						+ bbox[0]
						+ "&minx="
						+ bbox[1]
						+ "&"
						+ "maxy="
						+ bbox[2] + "&maxx=" + bbox[3] + "&" + "size=medium";

				Log.d("weather", "Pano request URL: " + urlStr);
				Log.d("doInBackground", "doing download of image...");
				try {
					final URI uri = new URI("http", urlStr, null);
					final HttpGet get = new HttpGet(uri);

					final HttpClient client = new DefaultHttpClient();
					final HttpResponse response = client.execute(get);
					final HttpEntity entity = response.getEntity();
					final String str = convertStreamToString(entity
							.getContent());
					final JSONObject json = new JSONObject(str);
					parse(json);
					
					if (downloadedImage != null)
					{
						imageCache.put(cacheKey, downloadedImage);
					}
					
				} catch (final Exception e) {
					Log.e("WeatherInfo", e.toString());
				}

				Log.d("doInBackground",
						"done downloading " + downloadedImage != null ? "OK"
								: "NOK");
			}
			return downloadedImage;
		}

		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				downloadedImage = result;
				
				
				completionNotifier.doneLoadingImage(this);
				
				//				setImageInView();
			} else {
				// NOOP
			}
		}

		private void parse(final JSONObject json) {
			try {
				final JSONArray array = json.getJSONArray("photos");
				final int count = array.length();
				for (int i = 0; i < count; i++) {

					// we only need one (working) so we simply quit here

					if (downloadedImage != null) {
						break;
					}
					final JSONObject obj = array.getJSONObject(i);

					final long id = obj.getLong("photo_id");
					String title = obj.getString("photo_title");
					final String owner = obj.getString("owner_name");
					final String thumb = obj.getString("photo_file_url");
					final String ownerUrl = obj.getString("owner_url");
					final String photoUrl = obj.getString("photo_url");

					final double latitude = obj.getDouble("latitude");
					final double longitude = obj.getDouble("longitude");
					final Bitmap b = BitmapUtils.loadBitmap(thumb);

					downloadedImage = b;

					// we ignore the pano info ......

				}
			} catch (final JSONException e) {
				Log.e("pano download", e.toString());
			}
		}

		private String convertStreamToString(final InputStream is) {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(is), 8 * 1024);
			final StringBuilder sb = new StringBuilder();

			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}

			return sb.toString();
		}
	}

}
