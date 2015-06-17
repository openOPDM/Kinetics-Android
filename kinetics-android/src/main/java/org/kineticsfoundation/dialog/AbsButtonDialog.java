package org.kineticsfoundation.dialog;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkState;

/**
 * Base class for Button based dialogs
 * Created by akaverin on 6/3/13.
 */
class AbsButtonDialog extends DialogFragment {

    static final String KEY_DIALOG_ID = "KEY_DIALOG_ID";
    static final String KEY_CALLBACK = "KEY_CALLBACK";
    private static final String KEY_RES_TITLE_ID = "KEY_RES_TITLE_ID";
    private static final String KEY_RES_BTN_POS_ID = "KEY_RES_BTN_POS_ID";
    private static final String KEY_RES_BTN_NEG_ID = "KEY_RES_BTN_NEG_ID";
    private static final String KEY_MESSAGE = "KEY_MESSAGE";
    private static final String KEY_RES_MESSAGE_ID = "KEY_RES_MESSAGE_ID";
    private static final String KEY_RES_ICON_ID = "KEY_RES_ICON_ID";

    static void setupDefaultArgs(AlertDialog.Builder builder, Bundle args, DialogInterface.OnClickListener
            dialogListener) {
        builder.setIcon(args.getInt(KEY_RES_ICON_ID)).setTitle(args.getInt(KEY_RES_TITLE_ID)).setPositiveButton(args
                .getInt(KEY_RES_BTN_POS_ID), dialogListener);

        if (args.containsKey(KEY_RES_MESSAGE_ID)) {
            builder.setMessage(args.getInt(KEY_RES_MESSAGE_ID));
        } else if (args.containsKey(KEY_MESSAGE)) {
            builder.setMessage(args.getString(KEY_MESSAGE));
        }
        if (args.containsKey(KEY_RES_BTN_NEG_ID)) {
            builder.setNegativeButton(args.getInt(KEY_RES_BTN_NEG_ID), dialogListener);
        }
    }

    @SuppressWarnings("unchecked")
    protected static class AbsBuilder<T extends AbsBuilder<T>> {

        final Bundle args;

        /**
         * Dialog with callbacks support
         *
         * @param id       dialog identifier
         * @param listener to be used as callback
         */
        public AbsBuilder(int id, DialogListener listener) {
            Preconditions.checkNotNull(listener);
            Preconditions.checkArgument(listener instanceof Fragment, "Only Fragments callbacks supported");

            args = new Bundle();
            args.putInt(KEY_DIALOG_ID, id);
            @SuppressWarnings("ConstantConditions") Fragment fragment = (Fragment) listener;
            fragment.getFragmentManager().putFragment(args, KEY_CALLBACK, fragment);
        }

        public AbsBuilder() {
            this.args = new Bundle();
        }

        public T setTitle(int resId) {
            args.putInt(KEY_RES_TITLE_ID, resId);
            return (T) this;
        }

        public T setIcon(int resId) {
            args.putInt(KEY_RES_ICON_ID, resId);
            return (T) this;
        }

        public T setMessage(int resId) {
            args.putInt(KEY_RES_MESSAGE_ID, resId);
            return (T) this;
        }

        public T setMessage(String message) {
            args.putString(KEY_MESSAGE, message);
            return (T) this;
        }

        public T setPositiveButton(int resId) {
            args.putInt(KEY_RES_BTN_POS_ID, resId);
            return (T) this;
        }

        public T setNegativeButton(int resId) {
            args.putInt(KEY_RES_BTN_NEG_ID, resId);
            return (T) this;
        }

        public T setExtra(Bundle extra) {
            args.putBundle(DialogConts.KEY_EXTRA, extra);
            return (T) this;
        }

        public void validate() {
            checkState(args.containsKey(KEY_RES_TITLE_ID));
            checkState(!(args.containsKey(KEY_RES_MESSAGE_ID) && args.containsKey(KEY_MESSAGE)));
            checkState(args.containsKey(KEY_RES_BTN_POS_ID));
        }
    }
}
