package org.outlander.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.geonames.WebService;
import org.openintents.filemanager.util.FileUtils;
import org.outlander.io.XML.GpxParser;
import org.outlander.io.XML.KmlPoiParser;
import org.outlander.io.XML.ParserResults;
import org.outlander.io.db.DBManager;
import org.outlander.model.PoiPoint;
import org.outlander.model.Route;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.Ut;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class ImportTask extends AsyncTask<Void, Void, ParserResults> {

    DBManager mPoiManager;
    String    filename;
    int       poiCategoryId;
    int       routeCategoryId;
    Context   context;

    public ImportTask(final Context context, final String filename, final int poiCategoryId, final int routeCategoryId, final DBManager poiManager) {
        super();

        this.context = context;

        this.filename = filename;
        this.poiCategoryId = poiCategoryId;
        this.routeCategoryId = routeCategoryId;
        mPoiManager = poiManager;

    }

    @Override
    protected ParserResults doInBackground(final Void... params) {

        final File file = new File(filename);

        final SAXParserFactory fac = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = fac.newSAXParser();
        }
        catch (final ParserConfigurationException e) {

            Ut.d(e.toString());

        }
        catch (final SAXException e) {
            Ut.d(e.toString());

        }

        final ParserResults parserResult = new ParserResults();

        if (parser != null) {
            mPoiManager.beginTransaction();
            Ut.dd("Start parsing file " + file.getName());

            try {
                if (FileUtils.getExtension(file.getName()).equalsIgnoreCase(".kml")) {
                    parser.parse(file, new KmlPoiParser(mPoiManager, poiCategoryId));
                }
                else if (FileUtils.getExtension(file.getName()).equalsIgnoreCase(".gpx")) {

                    parser.parse(file, new GpxParser(mPoiManager, poiCategoryId, routeCategoryId, parserResult, false));
                }

                mPoiManager.commitTransaction();
            }
            catch (final SAXException e) {

                Ut.d(e.toString());
                // e.printStackTrace();
                mPoiManager.rollbackTransaction();
            }
            catch (final IOException e) {

                Ut.d(e.toString());
                e.printStackTrace();
                mPoiManager.rollbackTransaction();
            }
            catch (final IllegalStateException e) {
            }
            catch (final OutOfMemoryError e) {
                Ut.w("OutOfMemoryError");
                mPoiManager.rollbackTransaction();
            }
            Ut.dd("Pois commited");
        }
        return parserResult;
    }

    @Override
    protected void onPostExecute(final ParserResults result) {

        final AltitudeRepairTask arTask = new AltitudeRepairTask();
        Ut.d("Task for altrepair started: ");
        arTask.execute(context);

        Toast.makeText(context, "Import Ready:\n POIs: " + result.pointCounter + "\n Routes: " + result.routeCounter + "\n Tracks: " + result.trackCounter,
                Toast.LENGTH_LONG).show();
    }

    class AltitudeRepairTask extends AsyncTask<Context, Void, Void> {

        WebService webservice = null;

        private void requestAltitude(final PoiPoint point) {
            try {
                final int altitude = getWebService().getElevationFor(point.getGeoPoint().getLatitude(), point.getGeoPoint().getLongitude());

                if (altitude > 0) {
                    point.setAlt(altitude);
                }
            }
            catch (final Exception x) {
                Ut.d("Webservice for altitude request failed: " + x.toString());
            }
        }

        WebService getWebService() {
            if (webservice == null) {
                webservice = new WebService();
            }
            return webservice;
        }

        @Override
        protected Void doInBackground(final Context... context) {

            if (Ut.isInternetConnectionAvailable(context[0])) {
                // check all POIs

                List<PoiPoint> allPoints = CoreInfoHandler.getInstance().getDBManager(context[0]).getPoiList();

                for (final PoiPoint point : allPoints) {
                    if (point.getAlt() < 0.0) {
                        // request from webservice

                        requestAltitude(point);

                        CoreInfoHandler.getInstance().getDBManager(context[0]).updatePoi(point);
                    }
                }
                allPoints = null;

                // check all Routes
                final List<Route> allRoutes = CoreInfoHandler.getInstance().getDBManager(context[0]).getAllRoutes();

                for (final Route route : allRoutes) {
                    for (final PoiPoint point : route.getPoints()) {
                        if (point.getAlt() < 0.0) {
                            point.setAlt(0);

                            requestAltitude(point);

                        }
                    }
                    CoreInfoHandler.getInstance().getDBManager(context[0]).updateRoute(route, true);
                }
            }
            return null;
        }

    }
}
