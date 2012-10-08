package org.outlander.fragments;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.outlander.R;
import org.outlander.utils.Ut;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ErrorDialogFragment extends SherlockDialogFragment {

    public static ErrorDialogFragment newInstance() {
        final ErrorDialogFragment frag = new ErrorDialogFragment();
        final Bundle args = new Bundle();
        // args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                // .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.error_title).setMessage(getText(R.string.error_text))
                .setPositiveButton(R.string.error_send, new DialogInterface.OnClickListener() {

                    @Override
                    @SuppressWarnings("static-access")
                    public void onClick(final DialogInterface dialog, final int whichButton) {

                        final SharedPreferences settings = getActivity().getPreferences(Activity.MODE_PRIVATE);
                        String text = settings.getString("error", "");
                        String subj = getText(R.string.app_name) + " error: ";
                        try {
                            final String[] lines = text.split("\n", 2);
                            final Pattern p = Pattern.compile("[.][\\w]+[:| |\\t|\\n]");
                            final Matcher m = p.matcher(lines[0] + "\n");
                            if (m.find()) {
                                subj += m.group().replace(".", "").replace(":", "").replace("\n", "") + " at ";
                            }
                            final Pattern p2 = Pattern.compile("[.][\\w]+[(][\\w| |\\t]*[)]");
                            final Matcher m2 = p2.matcher(lines[1]);
                            if (m2.find()) {
                                subj += m2.group().substring(2);
                            }
                        }
                        catch (final Exception e) {
                        }

                        final Build b = new Build();
                        final Build.VERSION v = new Build.VERSION();
                        text = "Your message:" + "\n\n" + getText(R.string.app_name) + ": " + Ut.getAppVersion(getActivity()) + "\nAndroid: " + v.RELEASE
                                + "\nDevice: " + b.BOARD + " " + b.BRAND + " " + b.DEVICE + " " + b.MANUFACTURER + " " + b.MODEL + " " + b.PRODUCT + "\n\n"
                                + text;

                        startActivity(Ut.sendErrorReportMail(subj, text));
                        Ut.e(text);
                        final SharedPreferences uiState = getActivity().getPreferences(Activity.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = uiState.edit();
                        editor.putString("error", "");
                        editor.commit();

                    }
                }).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {

                        final SharedPreferences uiState = getActivity().getPreferences(Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = uiState.edit();
                        editor.putString("error", "");
                        editor.commit();
                    }
                }).create();

    }

}
