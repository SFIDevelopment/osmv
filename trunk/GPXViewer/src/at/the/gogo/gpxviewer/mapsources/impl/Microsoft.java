package at.the.gogo.gpxviewer.mapsources.impl;

import at.the.gogo.gpxviewer.mapsources.adapter.BaseTileProvider;
import at.the.gogo.gpxviewer.mapsources.adapter.GoogleTileCache;

public class Microsoft {

	public Microsoft() {
	}

	/**
	 * Uses QuadTree coordinate system for addressing a tile. See <a
	 * href="http://msdn.microsoft.com/en-us/library/bb259689.aspx">Virtual
	 * Earth Tile System</a> for details.
	 */
	public static abstract class AbstractMicrosoft extends BaseTileProvider {

		protected String urlBase = ".ortho.tiles.virtualearth.net/tiles/";
		protected String urlAppend = "?g=45";
		protected int serverNum = 0;
		protected final int serverNumMax = 4;
		protected final char mapTypeChar;
		protected final String tileType;

		public AbstractMicrosoft(String tileType, char mapTypeChar,
				GoogleTileCache tileCache) {
			super(1, 19, tileCache);

			this.mapTypeChar = mapTypeChar;
			this.tileType = tileType;
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String tileNum = MapSourceTools.encodeQuadTree(zoom, tilex, tiley);
			serverNum = (serverNum + 1) % serverNumMax;
			return "http://"+mapTypeChar + serverNum + urlBase + mapTypeChar + tileNum
					+ "." + tileType + urlAppend;
		}
	}

	public static class MicrosoftMaps extends AbstractMicrosoft {

		public MicrosoftMaps(GoogleTileCache tileCache) {
			super("png", 'r', tileCache);
		}

		@Override
		public String getMapName() {
			return "Bing Maps";
		}

	}

	public static class MicrosoftMapsChina extends AbstractMicrosoft {

		public MicrosoftMapsChina(GoogleTileCache tileCache) {
			super("png", 'r', tileCache);

			urlBase = ".tiles.ditu.live.com/tiles/";
			urlAppend = "?g=1";
		}

		@Override
		public String getMapName() {
			return "Bing Maps China";
		}

	}

	public static class MicrosoftVirtualEarth extends AbstractMicrosoft {

		public MicrosoftVirtualEarth(GoogleTileCache tileCache) {
			super("jpg", 'a', tileCache);
		}

		@Override
		public String getMapName() {
			return "Bing Virtual Earth";
		}

	}

	public static class MicrosoftHybrid extends AbstractMicrosoft {

		public MicrosoftHybrid(GoogleTileCache tileCache) {
			super("jpg", 'h', tileCache);
		}

		@Override
		public String getMapName() {
			return "Bing hybrid maps";
		}

	}

}
