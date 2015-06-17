package org.kineticsfoundation.widget;

import android.app.Fragment;
import org.kineticsfoundation.R;

/**
 * Separator useless widget
 * Created by akaverin on 6/18/13.
 */
public class SeparatorWidget extends AbsWidget {

    public SeparatorWidget(Fragment fragment) {
        super(fragment);
    }

    @Override
    protected void setupHolder(boolean isEdit) {

    }

    @Override
    protected int getWidgetLayout(boolean isEdit) {
        return R.layout.separator_widget;
    }

    @Override
    protected void performUpdate() {
    }


}
