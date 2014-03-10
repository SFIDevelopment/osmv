package at.the.gogo.gpxviewer.util.mytrack;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.google.android.apps.mytracks.content.Track;
import com.google.android.apps.mytracks.content.Waypoint;

public class MyTrackAdapter {

	/**
	 * Maximum number of track points displayed by the map overlay. Set to 2X of
	 * 
	 */
	public static final int MAX_DISPLAYED_TRACK_POINTS = 10000;

	/**
	 * Maximum number of waypoints displayed by the map overlay.
	 */
	public static final int MAX_DISPLAYED_WAYPOINTS_POINTS = 128;

	/**
	 * Maximum number of track points that will be loaded at one time. With
	 * recording frequency of 2 seconds, 20000 corresponds to 11.1 hours.
	 */
	public static final int MAX_LOADED_TRACK_POINTS = 20000;

	/**
	 * Maximum number of waypoints that will be loaded at one time.
	 */
	public static final int MAX_LOADED_WAYPOINTS_POINTS = 10000;

	private MyTracksProviderUtils myTracksProviderUtils;

	public MyTrackAdapter(Context context) {
		try {
			myTracksProviderUtils = MyTracksProviderUtils.Factory.get(context);
		} catch (Exception x) {
			Log.e("MyTrackAdapter", "binding failed");
		}
	}

	public boolean isAvailable() {
		return (myTracksProviderUtils != null);
	}

	public List<Track> getTrackList() {
		List<Track> tracks = myTracksProviderUtils.getAllTracks();
		return tracks;
	}

	public void getTrackdetails(Track track) {
		Cursor cursor = null;

		try {
			cursor = myTracksProviderUtils.getWaypointCursor(track.getId(), 0,
					MAX_LOADED_WAYPOINTS_POINTS);
			if (cursor != null && cursor.moveToFirst()) {
				// This will skip the first waypoint (it carries the stats for
				// the
				// track).
				while (cursor.moveToNext()) {
					Waypoint waypoint = myTracksProviderUtils
							.createWaypoint(cursor);
					try {
						track.getLocations().add(waypoint.getLocation());

					} catch (Exception e) {
						Log.e("MyTrackAdapter", e.toString());
					}
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		// long waypointId = myTracksProviderUtils.getFirstWaypointId(track
		// .getId());
		//
		// while (waypointId > 0) {
		// Waypoint waypoint = myTracksProviderUtils.getWaypoint(waypointId);
		// if (waypoint != null) {
		// track.getLocations().add(waypoint.getLocation());
		// }
		// long newwaypointId = myTracksProviderUtils.getNextWaypointNumber(
		// track.getId(), WaypointType.WAYPOINT);
		//
		// waypointId = (newwaypointId == waypointId) ? 0 : newwaypointId;
		// }

	}

}
