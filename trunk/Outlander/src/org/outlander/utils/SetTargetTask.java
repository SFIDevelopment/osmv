package org.outlander.utils;

import org.andnav.osm.util.GeoPoint;
import org.geonames.WebService;
import org.outlander.R;
import org.outlander.constants.DBConstants;
import org.outlander.fragments.FragmentFactory;
import org.outlander.fragments.MapFragment;
import org.outlander.model.LocationPoint;
import org.outlander.model.PoiPoint;

import android.location.Address;

public class SetTargetTask extends AddressTask {

    LocationPoint lp;

    public SetTargetTask(final LocationPoint locationPoint) {
        lp = locationPoint;
    }

    @Override
    protected Address doInBackground(final LocationPoint... params) {

        final Address address = super.doInBackground(params);

        if (Ut.isInternetConnectionAvailable(context)) {
            if (params[0].getAltitude() > 1) {
                // try to fix height
                try {

                    params[0].setAltitude(WebService.astergdem(params[0].getLatitude(), params[0].getLongitude()));

                }
                catch (final Exception x) {
                    Ut.e("Webservicerequest for Altitude failed: " + x.toString());
                }
            }
        }

        return address;
    }

    @Override
    protected void onPostExecute(final Address address) {

        CoreInfoHandler.getInstance().getDBManager(context).deletePoisOfCategoryTarget();

        final PoiPoint point = new PoiPoint();
        point.setGeoPoint(new GeoPoint(lp.getLatitude(), lp.getLongitude()));
        point.setAlt(lp.getAltitude());
        point.setTitle("Target");
        if (address != null) {
            point.setDescr(MapFragment.formatAddress(address));
        }

        point.setIconId(R.drawable.poiyellow);
        point.setCategoryId(DBConstants.POI_CATEGORY_TARGET);

        CoreInfoHandler.getInstance().getDBManager(context).updatePoi(point);

        // refresh Overlay !!!
        // TODO: -->

        // switch to map
        CoreInfoHandler.getInstance().setMapCmd(MapFragment.MAP_CMD_SHOW_SEARCH);
        if (Ut.isMultiPane(context)) {
            CoreInfoHandler.getInstance().gotoPage(FragmentFactory.getFragmentTabPageIndexById(FragmentFactory.FRAG_ID_MAP)); //
        }
    }

}
