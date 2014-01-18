package at.the.gogo.gpxviewer.mapsources.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public abstract class BaseTileProvider implements TileProvider

{
	private final int dP;
	private final int dQ;
	private int minZoom;
	private int maxZoom;

	public static final int MAP_TILE_SIZE=256;
	
	GoogleTileCache tileCache;

	public BaseTileProvider(int minZoom, int maxZoom,
			GoogleTileCache tileCache) {
		this.dP = MAP_TILE_SIZE;
		this.dQ = MAP_TILE_SIZE;
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.tileCache = tileCache;
	}

	abstract public String getMapName();

	public int getMaxZoom() {
		return maxZoom;
	}

	public int getMinZoom() {
		return minZoom;
	}

	public boolean allowFileStore() {
		return true;
	}

	public URL getTileUrl(int x, int y, int z) {
		URL url = null;
		try {
			url = new URL(getTileUrlString(x, y, z));
		} catch (Exception ex) {

			// TODO log
		}
		return url;
	}

	protected abstract String getTileUrlString(int x, int y, int z);

	public final Tile getTile(int x, int y, int zoom) {

		Tile tile = NO_TILE;

		URL localURL = getTileUrl(x, y, zoom);

		if (localURL == null)
			return NO_TILE;

		String key = getMapName() + zoom + " " + x + " " + y;

		if (tileCache != null) {
			tile = tileCache.getMapTile(key);
		}
		if (tile == null) {
			Tile localTile;
			try {
				localTile = new Tile(this.dP, this.dQ, a(localURL.openStream()));

				if (localTile != null) {
					tile = localTile;
					if (tileCache != null) {
						tileCache.putTile(key, tile);
					}
				}

			} catch (IOException localIOException) {
				localTile = null;
			}
		}

		return tile;
	}

	private static byte[] a(InputStream paramInputStream) throws IOException {
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		a(paramInputStream, localByteArrayOutputStream);
		return localByteArrayOutputStream.toByteArray();
	}

	private static long a(InputStream paramInputStream,
			OutputStream paramOutputStream) throws IOException {
		byte[] arrayOfByte = new byte[4096];
		long l = 0L;
		while (true) {
			int i = paramInputStream.read(arrayOfByte);
			if (i == -1)
				break;
			paramOutputStream.write(arrayOfByte, 0, i);
			l += i;
		}
		return l;
	}

}
