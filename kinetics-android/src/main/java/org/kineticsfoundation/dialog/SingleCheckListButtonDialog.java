package org.kineticsfoundation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import static com.google.common.base.Preconditions.checkState;

/**
 * Dialog with single check list items
 * Created by akaverin on 6/20/13.
 */
public class SingleCheckListButtonDialog extends AbsButtonDialog {

    private static final String KEY_ITEMS = "KEY_ITEMS";
    private static final String CURRENT_VALUE = "CURRENT_ITEMS";

    private SingleCheckListButtonDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();

        Bundle data = new Bundle();
        data.putInt(DialogConts.KEY_ID, args.getInt(KEY_DIALOG_ID));
        data.putBundle(DialogConts.KEY_EXTRA, args.getBundle(DialogConts.KEY_EXTRA));
        DialogListener listener = (DialogListener) getFragmentManager().getFragment(args, KEY_CALLBACK);

        String[] items = args.getStringArray(KEY_ITEMS);
        final DialogInterface.OnClickListener alertDialogListener = new AlertDialogListener(data, items, listener);

        int checkedIdx = 0;
        if (args.containsKey(CURRENT_VALUE)) {
            String currentValue = args.getString(CURRENT_VALUE);
            for (int i = 0; i < items.length; ++i) {
                if (items[i].equals(currentValue)) {
                    checkedIdx = i;
                }
            }
        }
        builder.setSingleChoiceItems(items, checkedIdx, alertDialogListener);
        //setting initial value
        data.putString(DialogConts.KEY_VALUE, items[checkedIdx]);

        setupDefaultArgs(builder, args, alertDialogListener);
        return builder.create();
    }

    private static final class AlertDialogListener implements DialogInterface.OnClickListener {

        private final Bundle data;
        private final String[] values;
        private final org.kineticsfoundation.dialog.DialogListener listener;

        private AlertDialogListener(Bundle data, String[] values, DialogListener listener) {
            this.data = data;
            this.values = values;
            this.listener = listener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //just updating the data - as no button was pressed yet
            if (which >= 0) {
                data.putString(DialogConts.KEY_VALUE, values[which]);
            } else {
                data.putBoolean(DialogConts.KEY_BUTTON_POSITIVE, which == DialogInterface.BUTTON_POSITIVE);
                listener.onDialogResult(data);
            }
        }
    }

    public static class Builder extends AbsBuilder<Builder> {

        public Builder(int id, DialogListener listener) {
            super(id, listener);
        }

        public Builder setItems(String[] items) {
            args.putStringArray(KEY_ITEMS, items);
            return this;
        }

        public Builder setCurrentValue(String value) {
            args.putString(CURRENT_VALUE, value);
            return this;
        }

        public SingleCheckListButtonDialog build() {
            validate();
            //additional validation
            checkState(args.containsKey(KEY_ITEMS));

            SingleCheckListButtonDialog dialog = new SingleCheckListButtonDialog();
            dialog.setArguments(args);
            dialog.setCancelable(false);

            return dialog;
        }
    }
}
