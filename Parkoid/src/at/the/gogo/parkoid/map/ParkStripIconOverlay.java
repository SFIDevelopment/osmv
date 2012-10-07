package at.the.gogo.parkoid.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;
import at.the.gogo.parkoid.fragments.LocationListenerFragment;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.models.VKPZVisual;
import at.the.gogo.parkoid.models.ViennaKurzParkZone;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.webservices.YahooGeocoding;

public class ParkStripIconOverlay extends ItemizedIconOverlay<VKPZVisual> {

    public static ParkStripIconOverlay getNewOverlay(final Context context) {
        return new ParkStripIconOverlay(context, new ArrayList<VKPZVisual>());
    }

    public void refresh() {

        final Map<String, ViennaKurzParkZone> locations = CoreInfoHolder
                .getInstance().getVKPZCacheList();

        removeAllItems();

        if ((locations != null) && (locations.size() > 0)) {
            for (final String key : locations.keySet()) {
                final ViennaKurzParkZone vkpz = locations.get(key);
                addItem(new VKPZVisual(vkpz));
            }
        }
    }

    @Override
    protected void onDrawItem(final Canvas canvas, final VKPZVisual item,
            final Point curScreenCoords) {

        final Drawable marker = item.getMarker(0);

        final HotspotPlace hotspot = item.getMarkerHotspot();

        boundToHotspot(marker, hotspot);
        final Matrix directionRotater = new Matrix();
        final float[] mMatrixValues = new float[9];

        canvas.getMatrix(directionRotater);
        directionRotater.getValues(mMatrixValues);

        directionRotater.setTranslate(-20, -20); // this is all wrong but.....
        directionRotater.postScale(1 / mMatrixValues[Matrix.MSCALE_X],
                1 / mMatrixValues[Matrix.MSCALE_Y]);
        directionRotater.postTranslate(curScreenCoords.x, curScreenCoords.y);
        final Paint paint = new Paint();

        canvas.drawBitmap(((BitmapDrawable) marker).getBitmap(),
                directionRotater, paint);
    }

    protected ParkStripIconOverlay(final Context pContext,
            final List<VKPZVisual> pList) {
        super(
                pContext,
                pList,
                new org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<VKPZVisual>() {
                    @Override
                    public boolean onItemLongPress(final int index,
                            final VKPZVisual item) {
                        // Toast.makeText(
                        // CoreInfoHolder.getInstance().getContext(),
                        // "Item '" + item.mTitle + "' (index=" + index
                        // + ") got single tapped up",
                        // Toast.LENGTH_LONG).show();

                        showAddress(item);

                        return true;
                    }

                    @Override
                    public boolean onItemSingleTapUp(final int index,
                            final VKPZVisual item) {
                        Toast.makeText(
                                CoreInfoHolder.getInstance().getContext(),
                                item.getTitle(), Toast.LENGTH_LONG).show();
                        return false;
                    }
                });

        CoreInfoHolder.getInstance().setParkStripOverlay(this);

        setEnabled(false);

        // setFocusItemsOnTap(true);
        // setFocusedItem(0);
    }

    private static void showAddress(final VKPZVisual item) {
        final ShowAddressTask asyncTask = new ShowAddressTask();
        asyncTask.execute(item);
    }

    public static class ShowAddressTask extends
            AsyncTask<VKPZVisual, Void, GeoCodeResult> {

        @Override
        protected void onPostExecute(final GeoCodeResult address) {

            final String info = (address != null) ? "Parkzone:\n"
                    + LocationListenerFragment.formatAddress(address)
                    : "Info nicht verfügbar"; // nasty

            Toast.makeText(CoreInfoHolder.getInstance().getContext(), info,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected GeoCodeResult doInBackground(final VKPZVisual... params) {

            GeoCodeResult address = null;

            if (Util.isInternetConnectionAvailable(CoreInfoHolder.getInstance()
                    .getContext())) {
                address = YahooGeocoding.reverseGeoCode(params[0].getPoint()
                        .getLatitudeE6() / 1E6, params[0].getPoint()
                        .getLongitudeE6() / 1E6);
            }
            return address;
        }
    }

}
