package org.outlander.fragments;

import org.outlander.views.LocationInfoView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LocationInfoFragment extends Fragment implements
        PageChangeNotifyer {

    private LocationInfoView infoView;

    static LocationInfoFragment newInstance() {
        final LocationInfoFragment f = new LocationInfoFragment();

        return f;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        final View v = infoView.createView(getActivity(), inflater, container,
                savedInstanceState);
        return v;
    }

    @Override
    public void onResume() {
        resume();
        super.onResume();
    }

    private void resume() {
        infoView.fillData();
    }

    @Override
    public void pageGetsActivated() {
        resume();
    }

    @Override
    public void pageGetsDeactivated() {

    }

    @Override
    public void refresh() {
        resume();
    }

}
