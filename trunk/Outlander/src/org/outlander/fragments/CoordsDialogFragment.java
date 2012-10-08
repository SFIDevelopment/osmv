package org.outlander.fragments;

import org.andnav.osm.util.GeoPoint;
import org.apache.commons.lang3.StringUtils;
import org.outlander.R;
import org.outlander.model.LocationPoint;
import org.outlander.utils.CoreInfoHandler;
import org.outlander.utils.SetTargetTask;
import org.outlander.utils.geo.GeopointParser.ParseException;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class CoordsDialogFragment extends SherlockDialogFragment {

    public enum coordInputFormatEnum {
        Plain, Deg, Min, Sec;

        static coordInputFormatEnum fromInt(final int id) {
            final coordInputFormatEnum[] values = coordInputFormatEnum.values();
            if ((id >= 0) && (id < values.length)) {
                return values[id];
            }
            else {
                return Min;
            }
        }
    }

    private Context      context       = null;
    private GeoPoint     gp            = null;

    private EditText     eLat, eLon;
    private Button       bLat, bLon;
    private EditText     eLatDeg, eLatMin, eLatSec, eLatSub;
    private EditText     eLonDeg, eLonMin, eLonSec, eLonSub;
    private TextView     tLatSep1, tLatSep2, tLatSep3;
    private TextView     tLonSep1, tLonSep2, tLonSep3;

    private Spinner      spinner;

    CoordinateUpdate     cuListener;

    coordInputFormatEnum currentFormat = null;

    static public CoordsDialogFragment newInstance(final Context context, final GeoPoint point, final String title, final int dialogTitle) {
        final CoordsDialogFragment fragment = new CoordsDialogFragment(context, point, title);

        final Bundle args = new Bundle();
        args.putString("title", title);

        args.putDouble("lat", point.getLatitude());
        args.putDouble("lon", point.getLongitude());

        args.putInt("dialogTitle", dialogTitle);

        fragment.setArguments(args);
        return fragment;
    }

    public CoordsDialogFragment(final Context contextIn, final GeoPoint gpIn, final String dlgTitle) {
        super();
        context = contextIn;

        if (gpIn != null) {
            gp = gpIn;
        }
        else {
            gp = CoreInfoHandler.getInstance().getCurrentLocationAsGeoPoint();
        }
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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.coords, container, false);

        spinner = (Spinner) v.findViewById(R.id.spinnerCoordinateFormats);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.waypoint_coordinate_formats,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new CoordinateFormatListener());

        bLat = (Button) v.findViewById(R.id.ButtonLat);
        eLat = (EditText) v.findViewById(R.id.latitude);
        eLatDeg = (EditText) v.findViewById(R.id.EditTextLatDeg);
        eLatMin = (EditText) v.findViewById(R.id.EditTextLatMin);
        eLatSec = (EditText) v.findViewById(R.id.EditTextLatSec);
        eLatSub = (EditText) v.findViewById(R.id.EditTextLatSecFrac);
        tLatSep1 = (TextView) v.findViewById(R.id.LatSeparator1);
        tLatSep2 = (TextView) v.findViewById(R.id.LatSeparator2);
        tLatSep3 = (TextView) v.findViewById(R.id.LatSeparator3);

        bLon = (Button) v.findViewById(R.id.ButtonLon);
        eLon = (EditText) v.findViewById(R.id.longitude);
        eLonDeg = (EditText) v.findViewById(R.id.EditTextLonDeg);
        eLonMin = (EditText) v.findViewById(R.id.EditTextLonMin);
        eLonSec = (EditText) v.findViewById(R.id.EditTextLonSec);
        eLonSub = (EditText) v.findViewById(R.id.EditTextLonSecFrac);
        tLonSep1 = (TextView) v.findViewById(R.id.LonSeparator1);
        tLonSep2 = (TextView) v.findViewById(R.id.LonSeparator2);
        tLonSep3 = (TextView) v.findViewById(R.id.LonSeparator3);

        eLatDeg.addTextChangedListener(new TextChanged(eLatDeg));
        eLatMin.addTextChangedListener(new TextChanged(eLatMin));
        eLatSec.addTextChangedListener(new TextChanged(eLatSec));
        eLatSub.addTextChangedListener(new TextChanged(eLatSub));
        eLonDeg.addTextChangedListener(new TextChanged(eLonDeg));
        eLonMin.addTextChangedListener(new TextChanged(eLonMin));
        eLonSec.addTextChangedListener(new TextChanged(eLonSec));
        eLonSub.addTextChangedListener(new TextChanged(eLonSub));

        bLat.setOnClickListener(new ButtonClickListener());
        bLon.setOnClickListener(new ButtonClickListener());

        final Button buttonDone = (Button) v.findViewById(R.id.done);
        buttonDone.setOnClickListener(new InputDoneListener());
        return v;
    }

    private void updateGUI() {
        if (gp == null) {
            return;
        }
        Double lat = 0.0;
        if (gp.getLatitude() < 0) {
            bLat.setText("S");
        }
        else {
            bLat.setText("N");
        }

        lat = Math.abs(gp.getLatitude());

        Double lon = 0.0;
        if (gp.getLongitude() < 0) {
            bLon.setText("W");
        }
        else {
            bLon.setText("E");
        }

        lon = Math.abs(gp.getLongitude());

        final int latDeg = (int) Math.floor(lat);
        final int latDegFrac = (int) Math.round((lat - latDeg) * 100000);

        final int latMin = (int) Math.floor((lat - latDeg) * 60);
        final int latMinFrac = (int) Math.round((((lat - latDeg) * 60) - latMin) * 1000);

        final int latSec = (int) Math.floor((((lat - latDeg) * 60) - latMin) * 60);
        final int latSecFrac = (int) Math.round((((((lat - latDeg) * 60) - latMin) * 60) - latSec) * 1000);

        final int lonDeg = (int) Math.floor(lon);
        final int lonDegFrac = (int) Math.round((lon - lonDeg) * 100000);

        final int lonMin = (int) Math.floor((lon - lonDeg) * 60);
        final int lonMinFrac = (int) Math.round((((lon - lonDeg) * 60) - lonMin) * 1000);

        final int lonSec = (int) Math.floor((((lon - lonDeg) * 60) - lonMin) * 60);
        final int lonSecFrac = (int) Math.round((((((lon - lonDeg) * 60) - lonMin) * 60) - lonSec) * 1000);

        switch (currentFormat) {
            case Plain:
                getView().findViewById(R.id.coordTable).setVisibility(View.GONE);
                eLat.setVisibility(View.VISIBLE);
                eLon.setVisibility(View.VISIBLE);
                // eLat.setText(gp.format(GeopointFormatter.Format.LAT_DECMINUTE));
                // eLon.setText(gp.format(GeopointFormatter.Format.LON_DECMINUTE));
                break;
            case Deg: // DDD.DDDDD°
                getView().findViewById(R.id.coordTable).setVisibility(View.VISIBLE);
                eLat.setVisibility(View.GONE);
                eLon.setVisibility(View.GONE);
                eLatSec.setVisibility(View.GONE);
                eLonSec.setVisibility(View.GONE);
                tLatSep3.setVisibility(View.GONE);
                tLonSep3.setVisibility(View.GONE);
                eLatSub.setVisibility(View.GONE);
                eLonSub.setVisibility(View.GONE);

                tLatSep1.setText(".");
                tLonSep1.setText(".");
                tLatSep2.setText("°");
                tLonSep2.setText("°");

                eLatDeg.setText(addZeros(latDeg, 2));
                eLatMin.setText(addZeros(latDegFrac, 5));
                eLonDeg.setText(addZeros(lonDeg, 3));
                eLonMin.setText(addZeros(lonDegFrac, 5));
                break;
            case Min: // DDD° MM.MMM
                getView().findViewById(R.id.coordTable).setVisibility(View.VISIBLE);
                eLat.setVisibility(View.GONE);
                eLon.setVisibility(View.GONE);
                eLatSec.setVisibility(View.VISIBLE);
                eLonSec.setVisibility(View.VISIBLE);
                tLatSep3.setVisibility(View.VISIBLE);
                tLonSep3.setVisibility(View.VISIBLE);
                eLatSub.setVisibility(View.GONE);
                eLonSub.setVisibility(View.GONE);

                tLatSep1.setText("°");
                tLonSep1.setText("°");
                tLatSep2.setText(".");
                tLonSep2.setText(".");
                tLatSep3.setText("'");
                tLonSep3.setText("'");

                eLatDeg.setText(addZeros(latDeg, 2));
                eLatMin.setText(addZeros(latMin, 2));
                eLatSec.setText(addZeros(latMinFrac, 3));
                eLonDeg.setText(addZeros(lonDeg, 3));
                eLonMin.setText(addZeros(lonMin, 2));
                eLonSec.setText(addZeros(lonMinFrac, 3));
                break;
            case Sec: // DDD° MM SS.SSS
                getView().findViewById(R.id.coordTable).setVisibility(View.VISIBLE);
                eLat.setVisibility(View.GONE);
                eLon.setVisibility(View.GONE);
                eLatSec.setVisibility(View.VISIBLE);
                eLonSec.setVisibility(View.VISIBLE);
                tLatSep3.setVisibility(View.VISIBLE);
                tLonSep3.setVisibility(View.VISIBLE);
                eLatSub.setVisibility(View.VISIBLE);
                eLonSub.setVisibility(View.VISIBLE);

                tLatSep1.setText("°");
                tLonSep1.setText("°");
                tLatSep2.setText("'");
                tLonSep2.setText("'");
                tLatSep3.setText(".");
                tLonSep3.setText(".");

                eLatDeg.setText(addZeros(latDeg, 2));
                eLatMin.setText(addZeros(latMin, 2));
                eLatSec.setText(addZeros(latSec, 2));
                eLatSub.setText(addZeros(latSecFrac, 3));
                eLonDeg.setText(addZeros(lonDeg, 3));
                eLonMin.setText(addZeros(lonMin, 2));
                eLonSec.setText(addZeros(lonSec, 2));
                eLonSub.setText(addZeros(lonSecFrac, 3));
                break;
        }
    }

    private static String addZeros(final int value, final int len) {
        final String n = Integer.toString(value);
        return StringUtils.repeat('0', Math.max(0, len - n.length())) + n;
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            final Button e = (Button) v;
            final CharSequence text = e.getText();
            if (StringUtils.isBlank(text)) {
                return;
            }
            switch (text.charAt(0)) {
                case 'N':
                    e.setText("S");
                    break;
                case 'S':
                    e.setText("N");
                    break;
                case 'E':
                    e.setText("W");
                    break;
                case 'W':
                    e.setText("E");
                    break;
            }
            calc(true);
        }
    }

    private class TextChanged implements TextWatcher {

        private final EditText editText;

        public TextChanged(final EditText editText) {
            this.editText = editText;
        }

        @Override
        public void afterTextChanged(final Editable s) {
            /*
             * Max lengths, depending on currentFormat formatPlain = disabled
             * DEG MIN SEC SUB formatDeg 2/3 5 - - formatMin 2/3 2 3 - formatSec
             * 2/3 2 2 3
             */

            if (currentFormat == coordInputFormatEnum.Plain) {
                return;
            }

            int maxLength = 2;
            if ((editText == eLonDeg) || (editText == eLatSub) || (editText == eLonSub)) {
                maxLength = 3;
            }
            if (((editText == eLatMin) || (editText == eLonMin)) && (currentFormat == coordInputFormatEnum.Deg)) {
                maxLength = 5;
            }
            if (((editText == eLatSec) || (editText == eLonSec)) && (currentFormat == coordInputFormatEnum.Min)) {
                maxLength = 3;
            }

            if (s.length() == maxLength) {
                if (editText == eLatDeg) {
                    eLatMin.requestFocus();
                }
                else if (editText == eLatMin) {
                    if (eLatSec.getVisibility() == View.GONE) {
                        eLonDeg.requestFocus();
                    }
                    else {
                        eLatSec.requestFocus();
                    }
                }
                else if (editText == eLatSec) {
                    if (eLatSub.getVisibility() == View.GONE) {
                        eLonDeg.requestFocus();
                    }
                    else {
                        eLatSub.requestFocus();
                    }
                }
                else if (editText == eLatSub) {
                    eLonDeg.requestFocus();
                }
                else if (editText == eLonDeg) {
                    eLonMin.requestFocus();
                }
                else if (editText == eLonMin) {
                    if (eLonSec.getVisibility() == View.GONE) {
                        eLatDeg.requestFocus();
                    }
                    else {
                        eLonSec.requestFocus();
                    }
                }
                else if (editText == eLonSec) {
                    if (eLonSub.getVisibility() == View.GONE) {
                        eLatDeg.requestFocus();
                    }
                    else {
                        eLonSub.requestFocus();
                    }
                }
                else if (editText == eLonSub) {
                    eLatDeg.requestFocus();
                }
            }
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        }

    }

    private boolean calc(final boolean signalError) {
        if (currentFormat == coordInputFormatEnum.Plain) {
            try {
                gp = new GeoPoint(eLat.getText().toString(), eLon.getText().toString());
            }
            catch (final ParseException e) {
                if (signalError) {
                    Toast.makeText(context, context.getResources().getString(R.string.err_parse_lat_lon), Toast.LENGTH_LONG).show();
                }
                return false;
            }
            catch (final Exception e) {
                if (signalError) {
                    Toast.makeText(context, context.getResources().getString(R.string.err_invalid_lat_lon), Toast.LENGTH_LONG).show();
                }
                return false;
            }
            return true;
        }

        int latDeg = 0, latMin = 0, latSec = 0;
        int lonDeg = 0, lonMin = 0, lonSec = 0;
        Double latDegFrac = 0.0, latMinFrac = 0.0, latSecFrac = 0.0;
        Double lonDegFrac = 0.0, lonMinFrac = 0.0, lonSecFrac = 0.0;

        try {
            latDeg = Integer.parseInt(eLatDeg.getText().toString());
            lonDeg = Integer.parseInt(eLonDeg.getText().toString());
            latDegFrac = Double.parseDouble("0." + eLatMin.getText().toString());
            lonDegFrac = Double.parseDouble("0." + eLonMin.getText().toString());
            latMin = Integer.parseInt(eLatMin.getText().toString());
            lonMin = Integer.parseInt(eLonMin.getText().toString());
            latMinFrac = Double.parseDouble("0." + eLatSec.getText().toString());
            lonMinFrac = Double.parseDouble("0." + eLonSec.getText().toString());
            latSec = Integer.parseInt(eLatSec.getText().toString());
            lonSec = Integer.parseInt(eLonSec.getText().toString());
            latSecFrac = Double.parseDouble("0." + eLatSub.getText().toString());
            lonSecFrac = Double.parseDouble("0." + eLonSub.getText().toString());

        }
        catch (final NumberFormatException e) {
        }

        double latitude = 0.0;
        double longitude = 0.0;

        switch (currentFormat) {
            case Deg:
                latitude = latDeg + latDegFrac;
                longitude = lonDeg + lonDegFrac;
                break;
            case Min:
                latitude = latDeg + (latMin / 60.0) + (latMinFrac / 60.0);
                longitude = lonDeg + (lonMin / 60.0) + (lonMinFrac / 60.0);
                break;
            case Sec:
                latitude = latDeg + (latMin / 60.0) + (latSec / 60.0 / 60.0) + (latSecFrac / 60.0 / 60.0);
                longitude = lonDeg + (lonMin / 60.0) + (lonSec / 60.0 / 60.0) + (lonSecFrac / 60.0 / 60.0);
                break;
            case Plain:
                // This case has been handled above
        }
        latitude *= (bLat.getText().toString().equalsIgnoreCase("S") ? -1 : 1);
        longitude *= (bLon.getText().toString().equalsIgnoreCase("W") ? -1 : 1);

        try {
            gp = new GeoPoint(latitude, longitude);
        }
        catch (final Exception e) {
            if (signalError) {
                Toast.makeText(context, context.getResources().getString(R.string.err_invalid_lat_lon), Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    private class CoordinateFormatListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(final AdapterView<?> parent, final View view, final int pos, final long id) {
            // Ignore first call, which comes from onCreate()
            if (currentFormat != null) {

                // Start new format with an acceptable value: either the current
                // one
                // entered by the user, else our current coordinates, else
                // (0,0).
                if (!calc(false)) {

                    gp = CoreInfoHandler.getInstance().getCurrentLocationAsGeoPoint();

                }
            }

            currentFormat = coordInputFormatEnum.fromInt(pos);
            // Settings.setCoordInputFormat(currentFormat);
            updateGUI();
        }

        @Override
        public void onNothingSelected(final AdapterView<?> arg0) {
        }

    }

    private class CurrentListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {

            gp = CoreInfoHandler.getInstance().getCurrentLocationAsGeoPoint();
            updateGUI();
        }
    }

    private class InputDoneListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            if (!calc(true)) {
                return;
            }
            if (gp != null) {
                cuListener.update(gp);
            }

            addTempTarget(gp);

            dismiss();
        }
    }

    private void addTempTarget(final GeoPoint gp) {

        final LocationPoint lp = new LocationPoint(gp.getLatitude(), gp.getLongitude());

        new SetTargetTask(lp).execute(lp);

    }

    public void setOnCoordinateUpdate(final CoordinateUpdate cu) {
        cuListener = cu;
    }

    public interface CoordinateUpdate {

        public void update(final GeoPoint gp);
    }

}
