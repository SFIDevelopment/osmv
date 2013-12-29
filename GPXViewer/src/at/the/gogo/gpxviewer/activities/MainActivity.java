package at.the.gogo.gpxviewer.activities;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import at.the.gogo.gpxviewer.R;
import at.the.gogo.gpxviewer.model.PoiPoint;
import at.the.gogo.gpxviewer.model.Route;
import at.the.gogo.gpxviewer.model.Track;
import at.the.gogo.gpxviewer.util.geo.GPXContentHandler;
import at.the.gogo.gpxviewer.util.geo.GpxParser;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class MainActivity extends Activity {
	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	static final LatLng KIEL = new LatLng(53.551, 9.993);
	static final LatLng WIEN = new LatLng(48.551, 16.993);
	private GoogleMap map;
	private ClusterManager<PoiPoint> mClusterManager;

	private static final int FILE_REQUEST_CODE = 6384;
	private static final String TAG = "FileChooserActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		if (map != null) {
			Marker hamburg = map.addMarker(new MarkerOptions()
					.position(HAMBURG).title("Hamburg"));
			Marker kiel = map.addMarker(new MarkerOptions()
					.position(KIEL)
					.title("Kiel")
					.snippet("Kiel is cool")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ic_launcher)));

			Marker wien = map.addMarker(new MarkerOptions().position(WIEN)
					.title("Wien").snippet("Wien eoe!"));

			// initialize ClusterManager
			mClusterManager = new ClusterManager<PoiPoint>(this, map);
			map.setOnCameraChangeListener(mClusterManager);

			// Move the camera instantly to hamburg with a zoom of 15.
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(WIEN, 15));

			// Zoom in, animating the camera.
			map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		}

	}

	private void showChooser() {
		// Use the GET_CONTENT intent from the utility class
		Intent target = FileUtils.createGetContentIntent();
		// Create the chooser Intent
		Intent intent = Intent.createChooser(target,
				getString(R.string.chooser_title));
		try {
			startActivityForResult(intent, FILE_REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_REQUEST_CODE:
			// If the file selection was successful
			if (resultCode == RESULT_OK) {
				if (data != null) {
					// Get the URI of the selected file
					final Uri uri = data.getData();
					Log.i(TAG, "Uri = " + uri.toString());
					try {
						// Get the file path from the URI
						final String path = FileUtils.getPath(this, uri);

						Toast.makeText(this, "File Selected: " + path,
								Toast.LENGTH_LONG).show();

						// start import
						importFile(path);

					} catch (Exception e) {
						Log.e(TAG, "File select error", e);
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_file:
			showChooser();
			return true;
		case R.id.menu_about:

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void importFile(String filename) {

		File file = new File(filename);

		final SAXParserFactory fac = SAXParserFactory.newInstance();
		SAXParser parser = null;
		try {
			parser = fac.newSAXParser();
		} catch (final ParserConfigurationException e) {
			Log.e(TAG, e.toString());
			// e.printStackTrace();
		} catch (final SAXException e) {
			Log.e(TAG, e.toString());
			// e.printStackTrace();
		}
		try {
			// if
			// (FileUtils.getExtension(file.getName()).equalsIgnoreCase(".kml"))
			// {
			// parser.parse(filename, new KmlPoiParser(mPoiManager,
			// pOICategoryId));
			// }
			// else
			if (FileUtils.getExtension(file.getName()).equalsIgnoreCase(".gpx")) {

				GpxParser gpxParser = new GpxParser(false);

				parser.parse(filename, gpxParser);

				if (gpxParser.getGPXContent() != null) {
					addInfoToMap(gpxParser.getGPXContent());
				}

			}

		} catch (final SAXException e) {

			Log.e(TAG, e.toString());
			// e.printStackTrace();

		} catch (final IOException e) {

			Log.e(TAG, e.toString());
			// e.printStackTrace();

		} catch (final IllegalStateException e) {
		} catch (final OutOfMemoryError e) {
			Log.e(TAG, "OutOfMemoryError");

		}

	}

	private void addInfoToMap(GPXContentHandler gpxContent) {

		map.clear();

		for (PoiPoint point : gpxContent.getPoints()) {
			mClusterManager.addItem(point);
		}
		for (Route route : gpxContent.getRoutes()) {
			for (PoiPoint point : route.getRoutePoints()) {
				mClusterManager.addItem(point);
			}
		}
		
		for (Track track : gpxContent.getTracks()) {
			
//			mClusterManager.addItem(track.getFirstTrackPoint());
//			mClusterManager.addItem(track.getLastTrackPoint());
		}

	}

}