package at.the.gogo.parkoid.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.activities.NavigationActivity;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class RoutingDialogFragment extends SherlockDialogFragment {

    RadioGroup radios;
    double     slat, slon, dlat, dlon;

    static public RoutingDialogFragment newInstance(final String title,
            final double slat, final double slon, final double dlat,
            final double dlon, final int dialogTitle) {
        final RoutingDialogFragment fragment = new RoutingDialogFragment();

        final Bundle args = new Bundle();
        args.putString("title", title);
        args.putDouble("slat", slat);
        args.putDouble("slon", slon);
        args.putDouble("dlat", dlat);
        args.putDouble("dlon", dlon);

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
        final View v = inflater.inflate(R.layout.choosenavmode, container,
                false);

        Bundle extras = getArguments(); // savedInstanceState; use to restore
                                        // visual state!
        if (extras == null) {
            extras = new Bundle();
        }

        radios = (RadioGroup) v.findViewById(R.id.radioGroupNavi);

        final EditText title = (EditText) v.findViewById(R.id.Title);

        title.setText(extras.getString("title"));

        slat = extras.getDouble("lat");
        slon = extras.getDouble("lon");

        dlat = extras.getDouble("lat");
        dlon = extras.getDouble("lon");

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
        switch (radios.getCheckedRadioButtonId()) {
            case R.id.radioFeet: {
                getActivity().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri
                                .parse("google.navigation:ll=" + dlat + ","
                                        + dlon + "&mode=w")));
                break;
            }
            case R.id.radioInt: {
                final Bundle bundle = new Bundle();
                bundle.putDouble("sourceLat", slat);
                bundle.putDouble("sourceLon", slon);
                bundle.putDouble("destLat", dlat);
                bundle.putDouble("destLon", dlon);

                final Intent newIntent = new Intent(getActivity(),
                        NavigationActivity.class);
                newIntent.putExtras(bundle);
                startActivity(newIntent);
                break;
            }
        }
    }

}
