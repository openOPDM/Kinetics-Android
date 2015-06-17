package org.kineticsfoundation.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Simple dialog to show some progress with message
 * Created by akaverin on 5/29/13.
 */
public class SimpleProgressDialog extends DialogFragment {

    private static final String KEY_MESSAGE = "KEY_MESSAGE";
    private static final String KEY_CANCEL = "KEY_CANCEL";

    public static SimpleProgressDialog newDialog(int resId) {
        return buildFragment(resId, false);
    }

    public static SimpleProgressDialog newCancelableInstance(int resId) {
        return buildFragment(resId, true);
    }

    private static SimpleProgressDialog buildFragment(int resId, boolean isCancelable) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_MESSAGE, resId);
        bundle.putBoolean(KEY_CANCEL, isCancelable);

        SimpleProgressDialog fragment = new SimpleProgressDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        ProgressDialog dialog = new ProgressDialog(getActivity(),
                ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getActivity().getString(args.getInt(KEY_MESSAGE)));
        setCancelable(args.getBoolean(KEY_CANCEL));

        return dialog;
    }
}
