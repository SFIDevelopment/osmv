package at.the.gogo.gpxviewer.util.mytrack;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.google.android.apps.mytracks.content.Track;
import com.google.android.apps.mytracks.content.Waypoint;
import com.google.android.apps.mytracks.content.Waypoint.WaypointType;

public class MyTrackAdapter {

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
		long waypointId = myTracksProviderUtils.getFirstWaypointId(track
				.getId());

		while (waypointId > 0) {
			Waypoint waypoint = myTracksProviderUtils.getWaypoint(waypointId);
			if (waypoint != null) {
				track.getLocations().add(waypoint.getLocation());
			}
			long newwaypointId = myTracksProviderUtils.getNextWaypointNumber(
					track.getId(), WaypointType.WAYPOINT);

			waypointId = (newwaypointId == waypointId) ? 0 : newwaypointId;
		}

	}

}
