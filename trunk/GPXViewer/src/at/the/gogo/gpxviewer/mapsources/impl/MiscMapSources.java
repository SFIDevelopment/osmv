package at.the.gogo.gpxviewer.mapsources.impl;

import at.the.gogo.gpxviewer.mapsources.adapter.BaseTileProvider;
import at.the.gogo.gpxviewer.mapsources.adapter.GoogleTileCache;

public class MiscMapSources {

	public MiscMapSources() {
	}

	public static class YahooMaps extends BaseTileProvider {

		public YahooMaps(GoogleTileCache tileCache) {
			super(1, 16, tileCache);
		}

		@Override
		public String getMapName() {

			return "Yahoo Maps";
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			int yahooTileY = (((1 << zoom) - 2) / 2) - tiley;
			int yahooZoom = getMaxZoom() - zoom + 2;
			return "http://maps.yimg.com/hw/tile?locale=en&imgtype=png&yimgv=1.2&v=4.1&x="
					+ tilex + "&y=" + yahooTileY + "+6163&z=" + yahooZoom;
		}
	}

	public static class OviMaps extends BaseTileProvider {

		public OviMaps(GoogleTileCache tileCache) {
			super(1, 18, tileCache);
		}

		@Override
		public String getMapName() {

			return "Ovi Maps";
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			return "http://maptile.maps.svc.ovi.com/maptiler/maptile/newest/normal.day/"
					+ zoom
					+ "/"
					+ tilex
					+ "/"
					+ tiley
					+ "/256/png8?token=...&referer=maps.ovi.com";
		}

		@Override
		public String toString() {
			return "Ovi/Nokia Maps";
		}
	}

}
