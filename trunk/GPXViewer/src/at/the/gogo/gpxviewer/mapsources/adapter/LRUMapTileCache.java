package at.the.gogo.gpxviewer.mapsources.adapter;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

import com.google.android.gms.maps.model.Tile;

public class LRUMapTileCache extends LinkedHashMap<String, SoftReference<Tile>> {

    
	private static final long serialVersionUID = -7617801587673114625L;
	private final int         mCapacity;

    public LRUMapTileCache(final int pCapacity) {
        super(pCapacity + 2, 0.1f, true);
        mCapacity = pCapacity;
    }

    @Override
    public SoftReference<Tile> remove(final Object pKey) {
        final SoftReference<Tile> ref = super.remove(pKey);
        if (ref != null) {
            Tile bm = ref.get();
            if (bm != null) {
                bm= null;
            }
        }
        return ref;
    }

    @Override
    protected boolean removeEldestEntry(final Entry<String, SoftReference<Tile>> pEldest) {
        return size() > mCapacity;
    }

}
