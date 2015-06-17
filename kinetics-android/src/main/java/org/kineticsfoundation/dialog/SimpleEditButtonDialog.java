package org.kineticsfoundation.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.kineticsfoundation.R;
import org.kineticsfoundation.util.ValidationUtils;

import static com.google.common.base.Preconditions.checkState;
import static org.kineticsfoundation.util.ValidationUtils.isValid;

/**
 * Simple AlertDialog with 1-2 buttons and Edit field for input
 * Created by akaverin on 6/3/13.
 */
public class SimpleEditButtonDialog extends AbsButtonDialog {

    private static final String KEY_RES_HINT = "KEY_RES_HINT";
    private static final String KEY_DEFAULT_VALUE = "KEY_DEFAULT_VALUE";
    private static final String KEY_EDIT_PROPS = "KEY_EDIT_PROPS";
    private static final String KEY_REQUIRED = "KEY_REQUIRED";

    private SimpleEditButtonDialog() {
    }

    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();

        //build edit
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_field, null);
        final EditText input = (EditText) view.findViewById(R.id.input_et);

        if (args.containsKey(KEY_EDIT_PROPS)) {
            input.setInputType(args.getInt(KEY_EDIT_PROPS));
        }
        input.setHint(args.getInt(KEY_RES_HINT));

        if (args.containsKey(KEY_DEFAULT_VALUE)) {
            String defValue = args.getString(KEY_DEFAULT_VALUE);
            input.setText(defValue);
            input.setSelection(defValue.length());
        }
        Bundle data = new Bundle();
        data.putInt(DialogConts.KEY_ID, args.getInt(KEY_DIALOG_ID));
        data.putBundle(DialogConts.KEY_EXTRA, args.getBundle(DialogConts.KEY_EXTRA));
        DialogListener listener = (DialogListener) getFragmentManager().getFragment(args, KEY_CALLBACK);

        final DialogInterface.OnClickListener alertDialogListener = new AlertDialogListener(data, listener, input);

        //set DONE action...
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    handleEditInput(input, alertDialogListener, null);
                    return true;
                }
                return false;
            }
        });
        builder.setView(view);

        setupDefaultArgs(builder, args, alertDialogListener);

        final AlertDialog alertDialog = builder.create();
        if (args.getBoolean(KEY_REQUIRED, false)) {
            setEditRequired(input, alertDialogListener, alertDialog);
        }
        return alertDialog;
    }

    private void setEditRequired(final EditText input, final DialogInterface.OnClickListener alertDialogListener,
                                 final AlertDialog alertDialog) {
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleEditInput(input, alertDialogListener, dialog);
                    }
                });
            }
        });
    }

    public static class Builder extends AbsBuilder<Builder> {

        public Builder(int id, DialogListener listener) {
            super(id, listener);
        }

        public Builder setEditHint(int resId) {
            args.putInt(KEY_RES_HINT, resId);
            return this;
        }

        public Builder setDefaultValue(String value) {
            args.putString(KEY_DEFAULT_VALUE, value);
            return this;
        }

        public Builder setEditProps(int props) {
            args.putInt(KEY_EDIT_PROPS, props);
            return this;
        }

        public Builder setRequired() {
            args.putBoolean(KEY_REQUIRED, true);
            return this;
        }

        public SimpleEditButtonDialog build() {
            validate();
            //additional validation
            checkState(args.containsKey(KEY_RES_HINT));

            SimpleEditButtonDialog dialog = new SimpleEditButtonDialog();
            dialog.setArguments(args);
            dialog.setCancelable(false);

            return dialog;
        }

    }

    private void handleEditInput(EditText input, DialogInterface.OnClickListener alertDialogListener,
                                 DialogInterface dialog) {
        if (!isValid(getActivity().getApplicationContext(), ValidationUtils.ValidationType.TEXT,
                input)) {
            return;
        }
        alertDialogListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
        dismiss();
    }

    private static class AlertDialogListener implements DialogInterface.OnClickListener {

        private final Bundle data;
        private final DialogListener listener;
        private final EditText editText;

        private AlertDialogListener(Bundle data, DialogListener listener, EditText editText) {
            this.data = data;
            this.listener = listener;
            this.editText = editText;
        }

        @Override
        public void onClick(DialogInterface ignored, int which) {
            data.putBoolean(DialogConts.KEY_BUTTON_POSITIVE, which == DialogInterface.BUTTON_POSITIVE);
            data.putString(DialogConts.KEY_VALUE, editText.getText().toString());
            listener.onDialogResult(data);
        }
    }

}
