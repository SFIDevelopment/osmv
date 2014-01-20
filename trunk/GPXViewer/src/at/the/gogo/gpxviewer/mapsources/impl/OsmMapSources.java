package at.the.gogo.gpxviewer.mapsources.impl;

import at.the.gogo.gpxviewer.mapsources.adapter.BaseTileProvider;
import at.the.gogo.gpxviewer.mapsources.adapter.GoogleTileCache;

public class OsmMapSources {

	protected static final String MAP_MAPNIK = "http://%s.tile.openstreetmap.org/%d/%d/%d.png";
	protected static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";
	public static final String MAP_HIKING_TRAILS = "http://www.wanderreitkarte.de/topo/";
	public static final String MAP_HIKING_BASE = "http://abo.wanderreitkarte.de/"; // "http://www.wanderreitkarte.de/base/";
	public static final String MAP_HIKING_RELIEF = "http://topo.geofabrik.de/hills/";
	protected static final String MAP_PISTE = "http://openpistemap.org/tiles/contours/";
	protected static final String MAP_OPENSEA = "http://tiles.openseamap.org/seamark/";
	// private static String WRR_LICENCE = "735673907818";
	protected static final String MAP_THUNDERFOREST = "http://%s.tile.thunderforest.com/%s/%d/%d/%d.png";

	public OsmMapSources() {
	}

	protected static abstract class AbstractOsmTileSource extends
			BaseTileProvider {

		protected String mapName;

		@Override
		public String getMapName() {

			return mapName;
		}

		public AbstractOsmTileSource(String name, GoogleTileCache tileCache) {
			super(1, 18, tileCache);
			mapName = name;
		}

		public AbstractOsmTileSource(String name, int maxZoom,
				GoogleTileCache tileCache) {
			super(1, maxZoom, tileCache);
			mapName = name;
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		public String getTileType() {
			return "png";
		}

		@Override
		public boolean allowFileStore() {
			return true;
		}
	}

	public static class Mapnik extends AbstractOsmTileSource {

		private static final String[] SERVER = { "a", "b", "c" };
		private int SERVER_NUM = 0;

		public Mapnik(GoogleTileCache tileCache) {
			super("Mapnik", tileCache);
		}

		// @Override
		// public String getTileUrl(int zoom, int tilex, int tiley) {
		// return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
		// }
		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String url = String.format(MAP_MAPNIK, SERVER[SERVER_NUM], zoom,
					tilex, tiley);
			SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
			return url;
		}

		// @Override
		// public String toString() {
		// return "OpenStreetMap Mapnik";
		// }
	}

	public static class ThunderforestBase extends AbstractOsmTileSource {

		private static final String[] SERVER = { "a", "b", "c" };
		private int SERVER_NUM = 0;
		String mapType;

		public ThunderforestBase(String mapType, GoogleTileCache tileCache) {
			super("Thunderforest-" + mapType, tileCache);
			this.mapType = mapType;
		}

		// @Override
		// public String getTileUrl(int zoom, int tilex, int tiley) {
		// return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
		// }
		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String url = String.format(MAP_THUNDERFOREST, SERVER[SERVER_NUM],
					mapType, zoom, tilex, tiley);
			SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
			return url;
		}

		// @Override
		// public TileUpdate getTileUpdate() {
		// return TileUpdate.IfNoneMatch;
		// }

		// @Override
		// public String toString() {
		// return "Thunderforest " + mapType;
		// }
	}

	public static class ThunderforestOutdoors extends ThunderforestBase {

		public ThunderforestOutdoors(GoogleTileCache tileCache) {
			super("outdoors", tileCache);
		}
	}

	public static class ThunderforestLandscape extends ThunderforestBase {

		public ThunderforestLandscape(GoogleTileCache tileCache) {
			super("landscape", tileCache);
		}
	}

	public static class ThunderforestCycle extends ThunderforestBase {

		public ThunderforestCycle(GoogleTileCache tileCache) {
			super("cycle", tileCache);
		}
	}

	public static class CycleMap extends AbstractOsmTileSource {

		private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle/%d/%d/%d.png";
		private static final String[] SERVER = { "a", "b", "c" };
		private int SERVER_NUM = 0;

		public CycleMap(GoogleTileCache tileCache) {
			super("OSM Cycle Map", tileCache);
			// this.maxZoom = 17;
			// this.tileUpdate = TileUpdate.ETag;
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String url = String.format(PATTERN, SERVER[SERVER_NUM], zoom,
					tilex, tiley);
			SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
			return url;
		}

	}

	public static class OsmPublicTransport extends AbstractOsmTileSource {

		private static final String PATTERN = "http://tile.xn--pnvkarte-m4a.de/tilegen/%d/%d/%d.png";

		public OsmPublicTransport(GoogleTileCache tileCache) {
			super("OSMPublicTransport", tileCache);
			// this.maxZoom = 16;
			// this.minZoom = 2;
			// this.tileUpdate = TileUpdate.ETag;
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String url = String.format(PATTERN, zoom, tilex, tiley);
			return url;
		}

	}

	public static class TilesAtHome extends AbstractOsmTileSource {

		public TilesAtHome(GoogleTileCache tileCache) {
			super("TilesAtHome", tileCache);
			// this.maxZoom = 17;
			// this.tileUpdate = TileUpdate.IfModifiedSince;
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			return MAP_OSMA + super.getTileUrl(tilex, tiley,zoom);
		}
	}

	// public static class OsmHikingMap extends AbstractMapSource {
	//
	// public OsmHikingMap() {
	// super("Wanderreitkarte", 4, 18, "png", TileUpdate.IfNoneMatch);
	// }
	//
	// @Override
	// public String toString() {
	// // return "OpenStreetMap Hiking (Germany only)";
	// return "Wanderreitkarte (DE,AT,P)";
	// }
	//
	// @Override
	// public String getTileUrl(int zoom, int tilex, int tiley) {
	// return MAP_HIKING_BASE + zoom + "/" + tilex + "/" + tiley +
	// ".png/ticket/" + WRR_LICENCE;
	// }
	// }

	// public static class OsmHikingRelief extends AbstractMapSource {
	//
	// public OsmHikingRelief() {
	// super("OSM Hiking Relief", 4, 15, "png", TileUpdate.IfNoneMatch);
	// }
	//
	// @Override
	// public String toString() {
	// return "OpenStreetMap Hiking Relief only (Germany only)";
	// }
	//
	// @Override
	// public String getTileUrl(int zoom, int tilex, int tiley) {
	// return MAP_HIKING_RELIEF + zoom + "/" + tilex + "/" + tiley + ".png";
	// }
	// }
	//
	// public static class OsmHikingBase extends AbstractMapSource {
	//
	// public OsmHikingBase() {
	// super("OSM Hiking Base", 4, 18, "png", TileUpdate.IfNoneMatch);
	// }
	//
	// @Override
	// public String toString() {
	// return "OpenStreetMap Hiking Base only (Germany only)";
	//
	// }
	//
	// @Override
	// public String getTileUrl(int zoom, int tilex, int tiley) {
	// return MAP_HIKING_BASE + zoom + "/" + tilex + "/" + tiley +
	// ".png/ticket="+WRR_LICENCE;
	// }
	// }
	//
	// public static class OsmHikingMapWithRelief extends OsmHikingMap
	// implements MultiLayerMapSource {
	//
	// private final MapSource background = new OsmHikingRelief();
	//
	// public OsmHikingMapWithRelief() {
	// }
	//
	// @Override
	// public String toString() {
	// return "OpenStreetMap Hiking with Relief";
	// }
	//
	// @Override
	// public String getName() {
	// return "OSM Hiking with Relief";
	// }
	//
	// @Override
	// public int getMaxZoom() {
	// return 15;
	// }
	//
	// @Override
	// public MapSource getBackgroundMapSource() {
	// return background;
	// }
	// }
	//
	// public static class OsmHikingMapWithBase extends OsmHikingMap implements
	// MultiLayerMapSource {
	//
	// private final MapSource background = new OsmHikingBase();
	//
	// public OsmHikingMapWithBase() {
	// }
	//
	// @Override
	// public String toString() {
	// return "OpenStreetMap Hiking with Base";
	// }
	//
	// @Override
	// public String getName() {
	// return "OSM Hiking with Base";
	// }
	//
	// @Override
	// public MapSource getBackgroundMapSource() {
	// return background;
	// }
	// }
	// public static class OpenPisteMap extends AbstractMapSource {
	//
	// public OpenPisteMap() {
	// super("OpenPisteMap", 0, 17, "png", TileUpdate.LastModified);
	// }
	//
	// @Override
	// public String toString() {
	// return "Open Piste Map";
	// }
	//
	// @Override
	// public String getTileUrl(int zoom, int tilex, int tiley) {
	// return MAP_PISTE + zoom + "/" + tilex + "/" + tiley + ".png";
	// }
	// }
	//
	// /**
	// * Uses 512x512 tiles - not fully supported at the moment!
	// */
	// public static class Turaterkep extends AbstractMapSource {
	//
	// private static final MapSpace space = MapSpaceFactory.getInstance(512,
	// true);
	//
	// public Turaterkep() {
	// super("Turaterkep", 7, 16, "png", TileUpdate.IfNoneMatch);
	// }
	//
	// @Override
	// public String getTileUrl(int zoom, int tilex, int tiley) {
	// return "http://turaterkep.hostcity.hu/tiles/" + zoom + "/" + tilex + "/"
	// + tiley
	// + ".png";
	// }
	//
	// @Override
	// public MapSpace getMapSpace() {
	// return space;
	// }
	//
	// @Override
	// public String toString() {
	// return "Turaterkep (Hungary)";
	// }
	//
	// @Override
	// public Color getBackgroundColor() {
	// return Color.WHITE;
	// }
	// }

	public static class MapQuest extends AbstractOsmTileSource {

		private static final String[] SERVERS = { "otile1", "otile2", "otile3",
				"otile4" };
		private static int SERVER_NUM = 0;

		// private static final Semaphore SEM = new Semaphore(2);

		public MapQuest(GoogleTileCache tileCache) {
			super("MapQuest", tileCache);
			// this.minZoom = 2;
			// this.maxZoom = 18;
			// this.tileUpdate = TileUpdate.LastModified;
		}

		// public byte[] getTileData(int zoom, int x, int y,
		// MapSource.LoadMethod loadMethod)
		// throws IOException, InterruptedException {
		// SEM.acquire();
		// try {
		// byte[] arrayOfByte = super.getTileData(zoom, x, y, loadMethod);
		// return arrayOfByte;
		// } finally {
		// SEM.release();
		// }
		// throw localObject;
		// }
		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String server = SERVERS[SERVER_NUM];
			SERVER_NUM = (SERVER_NUM + 1) % SERVERS.length;
			String baseUrl = "http://" + server + ".mqcdn.com/tiles/1.0.0/osm";
			return baseUrl + super.getTileUrl(tilex, tiley, zoom);
		}

		// @Override
		// public String toString() {
		// return "OpenStreetMap MapQuest";
		// }
	}

	public static class Maps4U extends AbstractOsmTileSource {

		public Maps4U(GoogleTileCache tileCache) {
			super("4uMaps", 15, tileCache);
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String url = String.format("http://4umaps.eu/%d/%d/%d.png", zoom,
					tilex, tiley);
			return url;
		}

		// @Override
		// public MapSource.TileUpdate getTileUpdate() {
		// return MapSource.TileUpdate.IfNoneMatch;
		// }
		//
		// @Override
		// public String toString() {
		// return "4uMaps (OSM Topo)";
		// }
	}

	public static class WeatherMap extends AbstractOsmTileSource {

		public final static String services[] = { "precipitation", "clouds",
				"wind", "pressure", "temp", "snow" };
		private static final String PATTERN = "http://%s.tile.openweathermap.org/map/%s/%d/%d/%d.png";
		private static final String[] SERVER = { "1", "2", "3", "4", "5", "6",
				"7", "8", "9" };
		private int SERVER_NUM = 0;
		private int activeService;

		public WeatherMap(int activeService, GoogleTileCache tileCache) {
			super("Open Weather Map", tileCache);
			// this.maxZoom = 17;
			// this.tileUpdate = TileUpdate.ETag;
			this.activeService = activeService;
			mapName = "Open Weather Map - " + services[activeService];
		}

		public int getActiveService() {
			return activeService;
		}

		public void setActiveService(int activeService) {
			this.activeService = activeService;
		}

		@Override
		public boolean allowFileStore() {
			return false;
		}

		@Override
		public String getTileUrlString(int tilex, int tiley, int zoom) {
			String url = String.format(PATTERN, SERVER[SERVER_NUM],
					services[activeService], zoom, tilex, tiley);

			SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
			return url;
		}

	}

	/**
	 * Not working correctly:
	 * 
	 * 1. The map is a "sparse map" (only tiles are present that have content -
	 * the other are missing) <br>
	 * 2. The map layer's background is not transparent!
	 */
	// public static class OpenSeaMapLayer extends AbstractMapSource {
	//
	// public OpenSeaMapLayer() {
	// super("OpenSeaMapLayer", 11, 17, "png", TileUpdate.LastModified);
	// }
	//
	// @Override
	// public String getTileUrl(int zoom, int tilex, int tiley) {
	// return MAP_OPENSEA + zoom + "/" + tilex + "/" + tiley + ".png";
	// }
	// @Override
	// public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod)
	// throws IOException,
	// InterruptedException, TileException {
	// byte[] data = super.getTileData(zoom, x, y, loadMethod);
	// if (data != null && data.length == 0) {
	// log.info("loaded non-existing tile");
	// return null;
	// }
	// return data;
	// }
	//
	// @Override
	// public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod
	// loadMethod) throws IOException,
	// UnrecoverableDownloadException, InterruptedException {
	// try {
	// byte[] data = getTileData(zoom, x, y, loadMethod);
	// if (data == null) {
	// return null;
	// }
	// com.sixlegs.png.PngImage png = new com.sixlegs.png.PngImage();
	// BufferedImage image = png.read(new ByteArrayInputStream(data), true);
	// return image;
	// } catch (FileNotFoundException e) {
	// TileStore ts = TileStore.getInstance();
	// ts.putTile(ts.createNewEmptyEntry(x, y, zoom), this);
	// } catch (Exception e) {
	// Logging.LOG.error("Unknown error in OpenSeaMap", e);
	// }
	// return null;
	// }
	//
	// public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);
	//
	// @Override
	// public Color getBackgroundColor() {
	// return COLOR_TRANSPARENT;
	// }
	//
	// }
	// public static Image makeColorTransparent(Image im, final Color color) {
	// ImageFilter filter = new RGBImageFilter() {
	// // the color we are looking for... Alpha bits are set to opaque
	//
	// public int markerRGB = color.getRGB() | 0xFF000000;
	//
	// public final int filterRGB(int x, int y, int rgb) {
	// if ((rgb | 0xFF000000) == markerRGB) {
	// // Mark the alpha bits as zero - transparent
	// return 0x00FFFFFF & rgb;
	// } else {
	// // nothing to do
	// return rgb;
	// }
	// }
	// };
	//
	// ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
	// return Toolkit.getDefaultToolkit().createImage(ip);

}
