package org.outlander.fragments;

import org.outlander.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class WhatsNewDialogFragment extends SherlockDialogFragment {

    public static WhatsNewDialogFragment newInstance() {
        final WhatsNewDialogFragment frag = new WhatsNewDialogFragment();
        final Bundle args = new Bundle();
        // args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.about).setTitle(R.string.about_dialog_whats_new)
                .setMessage(R.string.whats_new_dialog_text).setNegativeButton(R.string.about_dialog_close, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {

                        /* User clicked Cancel so do some stuff */
                    }
                }).create();
    }

}
