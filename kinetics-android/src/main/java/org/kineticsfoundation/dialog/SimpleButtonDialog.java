package org.kineticsfoundation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * Simple AlertDialog with 1 or 2 buttons
 */
public class SimpleButtonDialog extends AbsButtonDialog {

    private SimpleButtonDialog() {
    }

    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();

        OnClickListener alertDialogListener = null;
        if (args.containsKey(KEY_CALLBACK)) {
            DialogListener listener = (DialogListener) getFragmentManager().getFragment(args, KEY_CALLBACK);
            alertDialogListener = new AlertDialogListener(args.getInt(KEY_DIALOG_ID), listener,
                    args.getBundle(DialogConts.KEY_EXTRA));
        }
        setupDefaultArgs(builder, args, alertDialogListener);
        return builder.create();
    }

    public static class Builder extends AbsBuilder<Builder> {

        /**
         * Dialog with callbacks support
         *
         * @param id       dialog identifier
         * @param listener to be used as callback
         */
        public Builder(int id, DialogListener listener) {
            super(id, listener);
        }

        public Builder() {
        }

        public SimpleButtonDialog build() {
            validate();

            SimpleButtonDialog dialog = new SimpleButtonDialog();
            dialog.setArguments(args);
            dialog.setCancelable(false);
            return dialog;
        }
    }

    private static class AlertDialogListener implements OnClickListener {

        private final DialogListener listener;
        private final Bundle data;

        private AlertDialogListener(int id, DialogListener listener, Bundle extra) {
            data = new Bundle();
            data.putBundle(DialogConts.KEY_EXTRA, extra);
            data.putInt(DialogConts.KEY_ID, id);
            this.listener = listener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            data.putBoolean(DialogConts.KEY_BUTTON_POSITIVE, which == DialogInterface.BUTTON_POSITIVE);
            listener.onDialogResult(data);
        }
    }
}
