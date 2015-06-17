package org.kineticsfoundation.widget;

import android.app.Fragment;
import android.os.Bundle;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.dialog.SingleCheckListButtonDialog;
import org.kineticsfoundation.fragment.FragmentUtils;

/**
 * Widget for single choice elements after press
 * Created by akaverin on 6/20/13.
 */
public class ListWidget extends TextWidget {

    public static final String KEY_VALUES = "KEY_VALUES";

    public ListWidget(Fragment fragment) {
        super(fragment);
    }

    @Override
    public void onClick() {
        if (!isEditMode()) {
            return;
        }
        Bundle extra = new Bundle();
        extra.putString(DialogConts.KEY_ID, data.getString(WidgetProperty.NAME.name()));

        SingleCheckListButtonDialog.Builder builder = new SingleCheckListButtonDialog.Builder(0,
                (DialogListener) fragment).setTitle(R.string.dialog_choose_value)
                .setPositiveButton(android.R.string.ok).setExtra(extra)
                .setItems(data.getBundle(WidgetProperty.EXTRA.name()).getStringArray(KEY_VALUES));

        if (data.containsKey(WidgetProperty.VALUE.name())) {
            builder.setCurrentValue(data.getString(WidgetProperty.VALUE.name()));
        }
        FragmentUtils.showDialog(fragment, builder.build());
    }
}
