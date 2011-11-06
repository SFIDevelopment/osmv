package at.the.gogo.parkoid.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.Car;
import at.the.gogo.parkoid.util.CoreInfoHolder;

public class CarFragment extends DialogFragment {

    EditText        carName;
    EditText        carLicence;
    CarListFragment carList;

    static public CarFragment newInstance(final int carid, final String name,
            final String licence, final int title, final CarListFragment carList) {
        final CarFragment fragment = new CarFragment();

        fragment.carList = carList;

        final Bundle args = new Bundle();
        args.putInt("carid", carid);
        args.putString("name", name);
        args.putString("licence", licence);
        args.putInt("dialogTitle", title);

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

        final View view = inflater.inflate(R.layout.auto, null);

        carName = (EditText) view.findViewById(R.id.car_description);
        carLicence = (EditText) view.findViewById(R.id.car_licence);

        final Bundle args = getArguments();
        if (args != null) {
            if (args.getString("name") != null) {
                carName.setText(args.getString("name"));
            }
            if (args.getString("licence") != null) {
                carLicence.setText(args.getString("licence"));
            }
        }

        ((Button) view.findViewById(R.id.buttonYES))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        saveCar();
                        carList.refreshData(true);
                        dismiss();
                    }
                });
        ((Button) view.findViewById(R.id.buttonNO))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        dismiss();
                    }
                });
        return view;
    }

    private void saveCar() {
        final Bundle args = getArguments();
        int id = -1;
        if (args != null) {
            id = args.getInt("carid");
        }

        final Car car = new Car(carName.getText().toString(), carLicence
                .getText().toString());
        if (id > -1) {
            car.setId(id);
        }

        CoreInfoHolder.getInstance().getDbManager().updateCar(car);

    }
}
