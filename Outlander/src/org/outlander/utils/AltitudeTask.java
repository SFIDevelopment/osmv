package org.outlander.utils;

import org.geonames.WebService;
import org.outlander.model.PoiPoint;

import android.content.Context;
import android.os.AsyncTask;

public class AltitudeTask extends AsyncTask<PoiPoint, Void, Integer> {

    Context    context = CoreInfoHandler.getInstance().getMainActivity();
    WebService webservice;

    public void setContext(final Context context) {
        this.context = context;
    }

    WebService getWebService() {
        if (webservice == null) {
            webservice = new WebService();
        }
        return webservice;
    }

    @Override
    protected Integer doInBackground(final PoiPoint... params) {

        Integer result = 0;
        try {
            result = getWebService().getElevationFor(params[0].getGeoPoint().getLatitude(), params[0].getGeoPoint().getLongitude());

        }
        catch (final Exception x) {
            Ut.d("Webservice for altitude request failed: " + x.toString());
        }
        return result;
    }
}
