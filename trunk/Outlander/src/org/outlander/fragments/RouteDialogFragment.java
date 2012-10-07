package org.outlander.fragments;

import org.outlander.R;
import org.outlander.chart.ChartViewFactory;
import org.outlander.model.PoiPoint;
import org.outlander.model.Route;
import org.outlander.utils.CoreInfoHandler;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class RouteDialogFragment extends SherlockDialogFragment {

    EditText      mName, mDescr;
    private Route route;

    static public RouteDialogFragment newInstance(final int routeid,
            final String name, final String descr, final int dialogTitle) {
        final RouteDialogFragment fragment = new RouteDialogFragment();

        final Bundle args = new Bundle();
        args.putInt("routeid", routeid);
        args.putString("name", name);
        args.putString("descr", descr);
        args.putInt("dialogTitle", dialogTitle);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);

        final Bundle extras = getArguments();

        final int title = extras.getInt("dialogTitle");
        if (title != 0) {
            dialog.setTitle(title);
        }

        return dialog;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.route, container, false);

        mName = (EditText) v.findViewById(R.id.Name);
        mDescr = (EditText) v.findViewById(R.id.Descr);

        Bundle extras = getArguments();
        if (extras == null) {
            extras = new Bundle();
        }
        final int id = extras.getInt("routeid", PoiPoint.EMPTY_ID());

        if (id < 0) {
            route = new Route();
            mName.setText(extras.getString("name"));
            mDescr.setText(extras.getString("descr"));
        } else {
            route = CoreInfoHandler.getInstance().getDBManager(getActivity())
                    .getRoute(id);

            if (route == null) {
                dismiss();
            }

            final View graphView = ChartViewFactory.getRouteChartView(
                    getActivity(), route);

            final LinearLayout layout = (LinearLayout) v
                    .findViewById(R.id.chart);
            layout.addView(graphView);

            mName.setText(route.getName());
            mDescr.setText(route.getDescr());
        }

        ((Button) v.findViewById(R.id.saveButton))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        doSaveAction();
                        dismiss();
                    }
                });
        ((Button) v.findViewById(R.id.discardButton))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        dismiss();
                    }
                });

        return v;
    }

    private void doSaveAction() {
        route.setName(mName.getText().toString());
        route.setDescr(mDescr.getText().toString());

        CoreInfoHandler.getInstance().getDBManager(getActivity())
                .updateRoute(route, false);
        dismiss();

        Toast.makeText(getActivity(), R.string.message_saved,
                Toast.LENGTH_SHORT).show();
    }
}
