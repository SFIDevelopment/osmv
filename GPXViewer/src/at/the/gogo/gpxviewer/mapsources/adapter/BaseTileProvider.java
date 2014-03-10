package at.the.gogo.gpxviewer.mapsources.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public abstract class BaseTileProvider implements TileProvider

{
	private final int dP;
	private final int dQ;
	private int minZoom;
	private int maxZoom;

	public static final int MAP_TILE_SIZE = 256;

	GoogleTileCache tileCache;

	public BaseTileProvider(int minZoom, int maxZoom, GoogleTileCache tileCache) {
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

	public int getTransparency()
	{
		return 0;
	}
	
	
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

				byte[] imageData = readData(localURL.openStream());

				if (getTransparency() > 0)
				{
					Bitmap bitmap = adjustOpacity(BitmapFactory.decodeByteArray(imageData, 0, imageData.length),getTransparency());
					ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount()); bitmap.copyPixelsToBuffer(byteBuffer); 
					imageData = byteBuffer.array();
				}
				
				localTile = new Tile(this.dP, this.dQ, imageData);

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

	private static byte[] readData(InputStream paramInputStream)
			throws IOException {
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		readData(paramInputStream, localByteArrayOutputStream);
		return localByteArrayOutputStream.toByteArray();
	}

	private static long readData(InputStream paramInputStream,
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

	// BitmapFactory.decodeByteArray for modification with this function:
	private Bitmap adjustOpacity(Bitmap bitmap,int alpha) {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(256, 256, conf);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setAlpha(alpha);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return bmp;
	}

}
