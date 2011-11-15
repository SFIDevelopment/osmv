package at.the.gogo.parkoid.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.fragments.LocationListenerFragment;
import at.the.gogo.parkoid.models.GeoCodeResult;
import at.the.gogo.parkoid.models.Position;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.webservices.YahooGeocoding;

public class ParkingCarOverlay extends ItemizedIconOverlay<ParkingCarItem> {

    public static ParkingCarOverlay overlayFactory(final Context context) {
        return new ParkingCarOverlay(context, new ArrayList<ParkingCarItem>());
    }

    public void refresh() {

        final Drawable marker = CoreInfoHolder.getInstance().getContext()
                .getResources().getDrawable(R.drawable.parking_marker);

        // Drawable marker =
        // resizeImage(CoreInfoHolder.getInstance().getContext(),
        // R.drawable.parking_marker, 50);

        // int width = marker.getIntrinsicWidth();
        // int height = marker.getIntrinsicHeight();

        // Bitmap img = Bitmap
        // .createBitmap(width>>1, height>>1, Bitmap.Config.ARGB_8888);
        // Canvas canvas = new Canvas(img);
        // Drawable drawable = new ScaleDrawable(marker, 0, width >> 2,
        // height >> 1).getDrawable();
        // drawable.setBounds(0, 0, width >> 1, height >> 1);

        // drawable.draw(canvas);

        final List<Position> locations = CoreInfoHolder.getInstance()
                .getDbManager().getLastLocationsList();
        removeAllItems();
        if ((locations != null) && (locations.size() > 0)) {

            for (final Position position : locations) {
                final ParkingCarItem parking = new ParkingCarItem("Auto",
                        "Parkplatz", new GeoPoint(position.getLatitude(),
                                position.getLongitude()), marker);
                addItem(parking);
            }
        }
    }

    public static Drawable resizeImage(final Context ctx, final int resId,
            final int percent) {

        // load the origial Bitmap
        final Bitmap BitmapOrg = BitmapFactory.decodeResource(
                ctx.getResources(), resId);

        final int width = BitmapOrg.getWidth();
        final int height = BitmapOrg.getHeight();
        final int newWidth = (width * percent) / 100;
        final int newHeight = (height * percent) / 100;

        // calculate the scale
        final float scaleWidth = ((float) newWidth) / width;
        final float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        final Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        final Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0,
                newWidth, newHeight, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return new BitmapDrawable(resizedBitmap);

    }

    @Override
    protected void onDrawItem(final Canvas canvas, final ParkingCarItem item,
            final Point curScreenCoords) {

        final Drawable marker = item.getMarker(0);

        final HotspotPlace hotspot = item.getMarkerHotspot();
        //
        // float scaleWidth = (float) 0.5;
        // float scaleHeight = (float) 0.5;
        // Matrix matrix = new Matrix();
        // matrix.postScale(scaleWidth, scaleHeight);
        //
        // Bitmap resizedBitmap = Bitmap.createBitmap(marker.get, 0, 0,
        // marker.getIntrinsicWidth(), marker.getIntrinsicHeight(),
        // matrix, true);
        // BitmapDrawable bitmapDrawableResized = new
        // BitmapDrawable(resizedBitmap);
        //
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

        // // draw it
        // Rect mRect = new Rect();
        // marker.copyBounds(mRect);
        // marker.setBounds(mRect.left + curScreenCoords.x, mRect.top
        // + curScreenCoords.y, mRect.right + curScreenCoords.x,
        // mRect.bottom + curScreenCoords.y);
        // marker.draw(canvas);
        // marker.setBounds(mRect);

    }

    private ParkingCarOverlay(final Context pContext,
            final List<ParkingCarItem> pList) {
        super(
                pContext,
                pList,
                new org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<ParkingCarItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index,
                            final ParkingCarItem item) {
                        // Toast.makeText(
                        // CoreInfoHolder.getInstance().getContext(),
                        // "Item '" + item.mTitle + "' (index=" + index
                        // + ") got single tapped up",
                        // Toast.LENGTH_LONG).show();

                        showAddress(item);

                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index,
                            final ParkingCarItem item) {
                        Toast.makeText(
                                CoreInfoHolder.getInstance().getContext(),
                                "Item '" + item.mTitle + "' (index=" + index
                                        + ") got long pressed",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                });

        CoreInfoHolder.getInstance().setParkingCarOverlay(this);
        // setFocusItemsOnTap(true);
        // setFocusedItem(0);
    }

    private static void showAddress(final ParkingCarItem item) {
        final ShowAddressTask asyncTask = new ShowAddressTask();
        asyncTask.execute(item);
    }

    public static class ShowAddressTask extends
            AsyncTask<ParkingCarItem, Void, GeoCodeResult> {

        @Override
        protected void onPostExecute(final GeoCodeResult address) {

            final String info = (address != null) ? "Auto befindet sich hier:\n"
                    + LocationListenerFragment.formatAddress(address)
                    : "Info nicht verfügbar"; // nasty

            Toast.makeText(CoreInfoHolder.getInstance().getContext(), info,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected GeoCodeResult doInBackground(final ParkingCarItem... params) {

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
