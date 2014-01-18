// Created by plusminus on 17:58:57 - 25.09.2008
package at.the.gogo.gpxviewer.mapsources.adapter;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;

import at.the.gogo.gpxviewer.util.OpenStreetMapConstants;
import at.the.gogo.gpxviewer.util.Util;

import com.google.android.gms.maps.model.Tile;

/**
 * @author Nicolas Gramlich
 */
public class GoogleTileCache implements OpenStreetMapConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected HashMap<String, SoftReference<Tile>> mCachedTiles;
	protected LinkedHashMap<String, Tile> mHardCachedTiles;
	protected LinkedHashMap<String, Tile> mHardCachedTiles2;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GoogleTileCache() {
		this(OpenStreetMapConstants.CACHE_MAPTILECOUNT_DEFAULT);
	}

	/**
	 * @param aMaximumCacheSize
	 *            Maximum amount of MapTiles to be hold within.
	 */
	public GoogleTileCache(final int aMaximumCacheSize) {
		mCachedTiles = new LRUMapTileCache(aMaximumCacheSize);
		mHardCachedTiles = new LinkedHashMap<String, Tile>(aMaximumCacheSize);
		mHardCachedTiles2 = new LinkedHashMap<String, Tile>(aMaximumCacheSize);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public synchronized Tile getMapTile(final String aTileURLString) {
		final Tile bmpHard = mHardCachedTiles.get(aTileURLString);

		if (bmpHard != null) {
			mHardCachedTiles2.put(aTileURLString, bmpHard);
			Util.d("found in Cache: "+aTileURLString);
			return bmpHard;
		}

		final SoftReference<Tile> ref = mCachedTiles.get(aTileURLString);
		if (ref == null) {
			return null;
		}
		final Tile bmp = ref.get();
		if (bmp == null) {
			Util.w("EMPTY SoftReference");
			mCachedTiles.remove(ref);
		} else if (bmp != null) {
			mCachedTiles.remove(ref);
		}
		mHardCachedTiles2.put(aTileURLString, bmp);
		return bmp;
	}

	public synchronized void putTile(final String aTileURLString,
			final Tile aTile) {
		mCachedTiles.put(aTileURLString, new SoftReference<Tile>(aTile));
		mHardCachedTiles2.put(aTileURLString, aTile);
		Util.w("OpenStreetMapTileCache size = " + mCachedTiles.size());
	}

	public synchronized void Commit() {
		final LinkedHashMap<String, Tile> tmp = mHardCachedTiles;
		mHardCachedTiles = mHardCachedTiles2;
		mHardCachedTiles2 = tmp;
		mHardCachedTiles2.clear();
		Util.w("mHardCachedTiles size = " + mHardCachedTiles.size());
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
