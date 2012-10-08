package org.outlander.utils;

import org.outlander.model.LocationPoint;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;

public class AddressTask extends AsyncTask<LocationPoint, Void, Address> {

    Context context = CoreInfoHandler.getInstance().getMainActivity();

    public void setContext(final Context context) {
        this.context = context;
    }

    @Override
    protected Address doInBackground(final LocationPoint... params) {
        Address result = null;

        if (params[0] != null) {
            if (Ut.isInternetConnectionAvailable(context)) {

                result = Ut.getRawAddressFromYahoo(context, params[0].getLatitude(), params[0].getLongitude());

            }
        }

        return result;
    }
}
