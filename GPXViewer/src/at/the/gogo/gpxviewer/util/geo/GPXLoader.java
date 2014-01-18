package at.the.gogo.gpxviewer.util.geo;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.util.Log;

public class GPXLoader {

	private static final String TAG = "GPXLoader";

	public static GPXContentHolder importFile(InputStream input) {

		GPXContentHolder gpxContent = null;

//		if (filename != null) {
//
//			File file = new File(filename);

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
				
//				if (FileUtils.getExtension(file.getName()).equalsIgnoreCase(
//						".gpx")) {

					GpxParser gpxParser = new GpxParser(false);

//					parser.parse(filename, gpxParser);
					parser.parse(input, gpxParser);
					gpxContent = gpxParser.getGPXContent();
//				}

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
//		}
		return gpxContent;
	}

}
