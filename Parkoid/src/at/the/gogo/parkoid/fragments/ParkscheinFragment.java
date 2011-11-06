package at.the.gogo.parkoid.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import at.the.gogo.parkoid.R;
import at.the.gogo.parkoid.models.Car;
import at.the.gogo.parkoid.models.Sms;
import at.the.gogo.parkoid.receiver.SmsHelper;
import at.the.gogo.parkoid.util.CoreInfoHolder;
import at.the.gogo.parkoid.util.Util;
import at.the.gogo.parkoid.util.db.DBConstants;

public class ParkscheinFragment extends DialogFragment {

    public final static int    SMS_MSG_ID = 999;
    public final static String SMS_TXT    = "sms-text";
    public final static String SMS_TEL    = "sms-tel";

    View                       view;
    Spinner                    carSpinner;
    Spinner                    durationSpinner;
    public Handler             handler;

    static public ParkscheinFragment newInstance(final int title) {
        final ParkscheinFragment fragment = new ParkscheinFragment();

        final Bundle args = new Bundle();
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

        view = inflater.inflate(R.layout.parkschein, null);

        carSpinner = (Spinner) view.findViewById(R.id.carspinner);
        durationSpinner = (Spinner) view.findViewById(R.id.durationspinner);

        ((Button) view.findViewById(R.id.buttonYES))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        doSendSMS();
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

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                Util.dd(String.format("Handler.handleMessage(): msg=%s", msg));

                if (msg.what == ParkscheinFragment.SMS_MSG_ID) {

                    final Bundle bundle = msg.getData();

                    final String smsMessage = bundle
                            .getString(ParkscheinFragment.SMS_TXT);
                    sendSMS(getActivity(), smsMessage);

                } else {
                    super.handleMessage(msg);
                }
            }

        };

    }

    @Override
    public void onResume() {

        fillData();

        super.onResume();
    }

    private void fillData() {
        // List<Car> cars =
        // CoreInfoHolder.getInstance().getDbManager(getActivity()).getCarList();

        final Cursor c = CoreInfoHolder.getInstance().getDbManager()
                .getDatabase().getCarListCursor();
        getActivity().startManagingCursor(c);
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(), android.R.layout.simple_spinner_item, c,
                new String[] { "name" }, new int[] { android.R.id.text1 });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carSpinner.setAdapter(adapter);

    }

    private void sendSMS(final Context context, final String message) {

        final String receiverTel = CoreInfoHolder.getInstance().getContext()
                .getText(R.string.SMS_TEL_WIEN).toString();

        SmsHelper.sendSMS(CoreInfoHolder.getInstance().getContext(),
                receiverTel, message);

        // persist it
        CoreInfoHolder
                .getInstance()
                .getDbManager()
                .updateSMS(new Sms(receiverTel, message), DBConstants.TABLE_SMS);

    }

    private void doSendSMS() {

        final int duration = getResources().getIntArray(R.array.parkdauer)[durationSpinner
                .getSelectedItemPosition()];

        String licence = null;
        if (carSpinner.getCount() > 0) {
            final long carId = carSpinner.getSelectedItemId();

            final Car car = CoreInfoHolder.getInstance().getDbManager()
                    .getCar((int) carId);

            licence = car.getLicence();
            if ((licence != null) && (licence.length() == 0)) {
                licence = null;
            }
        }

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        final boolean check = sharedPreferences.getBoolean(
                "pref_sms_plausibility", true);

        final boolean business = sharedPreferences.getBoolean(
                "pref_sms_business", false);

        final String message = SmsHelper.formatParkingSMS(licence,
                Integer.toString(duration), business);

        if (check) {
            new AlertDialog.Builder(getActivity())
                    // .setIcon(R.drawable.alert_dialog_icon)
                    .setTitle(R.string.title_sms)
                    .setMessage(">> " + message + "<<")
                    .setPositiveButton(R.string.SMSYES,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int whichButton) {

                                    final Bundle bundle = new Bundle();
                                    bundle.putString(
                                            ParkscheinFragment.SMS_TXT, message);

                                    final Message msg = Message.obtain();
                                    msg.what = ParkscheinFragment.SMS_MSG_ID;
                                    msg.setData(bundle);
                                    ParkscheinFragment.this.getHandler()
                                            .sendMessage(msg);

                                    // sendSMS(getActivity(), message);
                                }
                            })
                    .setNegativeButton(R.string.SMSNO,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int whichButton) {

                                    dismiss();
                                }
                            }).create().show();
        } else {
            sendSMS(getActivity(), message);
        }
    }

}
