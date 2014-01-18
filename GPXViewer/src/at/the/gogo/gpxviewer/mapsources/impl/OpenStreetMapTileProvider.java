package at.the.gogo.gpxviewer.mapsources.impl;

import java.net.MalformedURLException;
import java.net.URL;

import at.the.gogo.gpxviewer.mapsources.adapter.BaseTileProvider;
import at.the.gogo.gpxviewer.mapsources.adapter.GoogleTileCache;

public class OpenStreetMapTileProvider extends BaseTileProvider {

	// ------------------------------------------------------------------------
	// Private Constants
	// ------------------------------------------------------------------------

	// private static final String FORMAT =
	// "http://tile.openstreetmap.org/%d/%d/%d.png";
	protected static final String MAP_MAPNIK = "http://%s.tile.openstreetmap.org/%d/%d/%d.png";
	// ------------------------------------------------------------------------
	// Members
	// ------------------------------------------------------------------------
	private static final String[] SERVER = { "a", "b", "c" };
	private int SERVER_NUM = 0;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public OpenStreetMapTileProvider(GoogleTileCache tileCache) {
		super(2, 18, tileCache);
	}

	// ------------------------------------------------------------------------
	// Public Methods
	// ------------------------------------------------------------------------

	@Override
	public String getTileUrlString(int x, int y, int z) {

		SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
		return String.format(MAP_MAPNIK, SERVER[SERVER_NUM], z, x, y);

	}

	@Override
	public String getMapName() {

		return "Mapnik";
	}

}
