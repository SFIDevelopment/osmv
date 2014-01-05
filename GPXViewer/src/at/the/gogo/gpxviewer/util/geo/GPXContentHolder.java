package at.the.gogo.gpxviewer.util.geo;

import java.util.ArrayList;
import java.util.List;

import at.the.gogo.gpxviewer.model.PoiPoint;
import at.the.gogo.gpxviewer.model.Route;
import at.the.gogo.gpxviewer.model.Track;

public class GPXContentHolder {

	List<Route> routes;
	List<Track> tracks;
	List<PoiPoint> points;

	public List<Route> getRoutes() {

		if (routes == null) {
			routes = new ArrayList<Route>();
		}

		return routes;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}

	public List<Track> getTracks() {
		if (tracks == null) {
			tracks = new ArrayList<Track>();
		}
		return tracks;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	public List<PoiPoint> getPoints() {
		if (points == null) {
			points = new ArrayList<PoiPoint>();
		}
		return points;
	}

	public void setPoints(List<PoiPoint> points) {
		this.points = points;
	}

}
