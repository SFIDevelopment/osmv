package org.outlander.fragments;

import org.outlander.R;
import org.outlander.utils.Ut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class AboutDialogFragment extends SherlockDialogFragment {

    public static AboutDialogFragment newInstance() {
        final AboutDialogFragment frag = new AboutDialogFragment();
        final Bundle args = new Bundle();
        // args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.about)
                .setTitle(R.string.menu_about)
                .setMessage(
                        getText(R.string.app_name) + " v."
                                + Ut.getAppVersion(getActivity()) + "\n\n"
                                + getText(R.string.about_dialog_text))
                .setPositiveButton(R.string.about_dialog_whats_new,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int whichButton) {

                                final FragmentTransaction ft = getFragmentManager()
                                        .beginTransaction();

                                ft.remove(AboutDialogFragment.this);
                                ft.addToBackStack(null);

                                final WhatsNewDialogFragment newFragment = WhatsNewDialogFragment
                                        .newInstance();
                                newFragment.show(ft, "dialog");
                                return;
                            }
                        })
                .setNegativeButton(R.string.about_dialog_close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int whichButton) {

                                /* User clicked Cancel so do some stuff */
                            }
                        }).create();
    }

}
