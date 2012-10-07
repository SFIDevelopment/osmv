package at.the.gogo.parkoid.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.ViennaKurzParkZone;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.json.ParseWKPZ;
import at.the.gogo.parkoid.util.webservices.VKPZQuery;

public class VKPZOverlay extends Overlay {

    public final String       VKPZ_FILE       = "wien_kpz.json";
    public final String       VKPS_FILE       = "wien_kps.json";

    final public static int   VKPZ_REFRESH    = 1234;

    // List<ViennaKurzParkZone> parkingAreas;
    // List<ViennaKurzParkZone> parkingStreetSegments;
    Context                   context;
    boolean                   mThreadRunning  = false;
    boolean                   mStopDraw       = true;
    boolean                   mInitialized    = false;
    private MapView           mapView;
    private List<Path>        mPathZones      = null;
    private List<Path>        mPathStripes    = null;
    protected ExecutorService mThreadExecutor = Executors
                                                      .newSingleThreadExecutor();
    private int               mLastZoom;

    private final Paint       mAreaBorder, mAreaFill;
    private final Semaphore   sem;

    private final Handler     handler;
    // zoomlevel 13 - 17 nasty approximation.....
    private final static int  RANGES[]        = { 3000, 1000, 700, 400, 200 };

    private class AquireDataThread implements Runnable {

        @Override
        public void run() {
            mThreadRunning = true;
            Util.d("try parsing Vienna Parkraum JSON");

            // get new definitions
            // Location currLoc = CoreInfoHolder.getInstance()
            // .getLastKnownLocation();

            double lat = 0;
            double lon = 0;
            if (mapView != null) {
                lat = mapView.getMapCenter().getLatitudeE6() / 1E6;
                lon = mapView.getMapCenter().getLongitudeE6() / 1E6;
            } else if (Util.DEBUGMODE) {
                lat = 48.208336;
                lon = 16.372223;
            }

            int range = 500;
            if (mLastZoom > 0) {
                if ((mLastZoom >= 13) && (mLastZoom <= 17)) {
                    range = VKPZOverlay.RANGES[mLastZoom - 13];
                }
            }

            // get data
            final Map<String, Map<String, String>> rawMeat = VKPZQuery
                    .getZones(lat, lon, range, 0);

            if ((rawMeat != null) && (rawMeat.size() > 0)) {

                // CoreInfoHolder.getInstance().setVKPZCacheList(
                // ParseWKPZ.parseWebserviceData(rawMeat, true));
                ParseWKPZ.parseWebserviceData(rawMeat, true); // query for
                                                              // complete data
                calcScreenPaths();

            }

            // ---------- local ----------
            // AssetManager assetManager = context.getAssets();
            // InputStream is = null;
            // try {
            // is = assetManager.open(VKPZ_FILE);
            //
            // // parkingAreas = ParseWKPZ.parseJSONData(is);
            // // Util.d("Kurzparkzonen mapped");
            // //
            // // is = assetManager.open(VKPS_FILE);
            // // parkingStreetSegments = ParseWKPZ.parseJSONData(is);
            // // Util.d("KurzparkSegments mapped");
            // } catch (Exception x) {
            // Util.e("Vienna Parkraum Info files could not be handled");
            // } finally {
            // if (is != null) {
            // try {
            // is.close();
            // } catch (IOException e) {
            // }
            // }
            // }
            // ---------- local ----------

            if (CoreInfoHolder.getInstance().getVKPZCacheList() != null) {
                mStopDraw = false;
            }

            mInitialized = true;
            mThreadRunning = false;

            Util.d("Zone infos available : "
                    + ((CoreInfoHolder.getInstance().getVKPZCacheList() != null) ? CoreInfoHolder
                            .getInstance().getVKPZCacheList().size()
                            : "NO DATA"));

        }
    }

    public VKPZOverlay(final Context ctx) {
        super(ctx);
        context = ctx;
        sem = new Semaphore(1);

        mAreaFill = new Paint();
        mAreaFill.setAntiAlias(true);
        mAreaFill.setStrokeWidth(3);
        mAreaFill.setStyle(Paint.Style.FILL);
        mAreaFill.setColor(ctx.getResources().getColor(R.color.areafill));
        // mAreaFill.setARGB(0, 255, 100, 100);
        mAreaFill.setAlpha(50);
        // mAreaFill.setShadowLayer(5.5f, 6.0f, 6.0f, Color.BLACK);

        mAreaBorder = new Paint(mAreaFill);
        mAreaBorder.setStyle(Paint.Style.STROKE);
        // mAreaBorder.setColor(R.color.areaborder);
        mAreaBorder.setAlpha(200);

        handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                Util.d(String.format("Handler.handleMessage(): msg=%s", msg));

                if (msg.what == VKPZOverlay.VKPZ_REFRESH) {
                    if (mapView != null) {
                        mapView.invalidate();
                    }
                } else {
                    super.handleMessage(msg);
                }
            }
        };
    }

    public Handler getHandler() {
        return handler;
    }

    public void refresh() {
        setStopDraw(true);

        // get new definitions
        if ((!mThreadRunning) && (mapView != null)) {
            mThreadExecutor.execute(new AquireDataThread());
        }
    }

    public void setStopDraw(final boolean dontdraw) {
        mStopDraw = dontdraw;
    }

    private void calcScreenPaths() {

        List<Path> pathZonesNew = null;
        List<Path> pathStripesNew = null;
        int zones = 0;
        int stripes = 0;
        if ((mapView != null)
                && (CoreInfoHolder.getInstance().getVKPZCacheList() != null)) {

            final Projection projection = mapView.getProjection();
            pathZonesNew = new ArrayList<Path>();
            pathStripesNew = new ArrayList<Path>();
            // screenpoints = new ArrayList<Point>();

            final Iterator<String> iterator = CoreInfoHolder.getInstance()
                    .getVKPZCacheList().keySet().iterator();

            while (iterator.hasNext()) {
                final String key = iterator.next();
                final ViennaKurzParkZone vkpz = CoreInfoHolder.getInstance()
                        .getVKPZCacheList().get(key);

                final Path path = new Path();
                int ix = 0;
                final int max = vkpz.getPolygon().size();

                for (final GeoPoint gp : vkpz.getPolygon()) {

                    final Point screenPoint = projection.toPixels(gp, null);
                    // if (ix == 0) {
                    // path.setLastPoint(screenPoint.x, screenPoint.y);
                    // } else {
                    // path.lineTo(screenPoint.x, screenPoint.y);
                    // }
                    if (ix < max) {
                        if (ix == 0) {
                            path.moveTo(screenPoint.x, screenPoint.y);
                        } else {
                            path.lineTo(screenPoint.x, screenPoint.y);
                        }
                    } else {
                        path.setLastPoint(screenPoint.x, screenPoint.y);
                    }
                    ix++;
                }

                if (max < 3) {
                    stripes++;
                    pathStripesNew.add(path);
                } else {
                    zones++;
                    pathZonesNew.add(path);
                }
            }
        }

        try {
            sem.acquire();
            Util.dd("prepared : Zones:" + zones + " stripes:" + stripes);

            mPathStripes = pathStripesNew;
            mPathZones = pathZonesNew;

            sem.release();

        } catch (final InterruptedException e) {
            Util.dd("Semaphore aquiring failed");
        }

        // might be called from different thread
        // final Message msg = Message.obtain();
        // msg.what = VKPZOverlay.VKPZ_REFRESH;
        // getHandler().sendMessage(msg);

        if (mapView != null) {
            mapView.postInvalidate();
        }
    }

    IGeoPoint gp_old = null;

    @Override
    protected void draw(final Canvas canvas, final MapView osmv,
            final boolean shadow) {
        if (mapView == null) {
            mapView = osmv;
        }
        final IGeoPoint gp = mapView.getMapCenter();

        if (gp_old != null) {
            if ((Math.abs(gp_old.getLatitudeE6() - gp.getLatitudeE6()) > 2000)
                    || (Math.abs(gp_old.getLongitudeE6() - gp.getLongitudeE6()) > 2000)) {
                // moved....
                mInitialized = false;
                gp_old = gp;
            }
        } else {
            gp_old = gp;
        }

        if (!mInitialized) { // first time
            if (!mThreadRunning) {
                mThreadExecutor.execute(new AquireDataThread());
            }
        }

        // if (shadow) { // we are called twice...
        // return;
        // }

        if (!mStopDraw) {
            if (mLastZoom != osmv.getZoomLevel()) {
                mPathZones = null;
                mPathStripes = null;
            }
            mLastZoom = osmv.getZoomLevel();

            if ((mPathZones == null) && (mPathStripes == null)) {
                calcScreenPaths();
            }

            try {
                sem.acquire();

                if (mPathZones != null) {
                    for (final Path path : mPathZones) {
                        canvas.drawPath(path, mAreaFill);
                        canvas.drawPath(path, mAreaBorder);
                    }
                }
                if (mPathStripes != null) {
                    for (final Path path : mPathStripes) {
                        canvas.drawPath(path, mAreaBorder);
                    }
                }

            } catch (final InterruptedException e) {
                Util.dd("Semaphore aquireing failed");
            }
            sem.release();
        }
    }
}
