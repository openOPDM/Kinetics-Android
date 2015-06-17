package org.kineticsfoundation.widget;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.RelativeLayout;

/**
 * Basic widget class
 * Created by akaverin on 6/17/13.
 */
public abstract class AbsWidget extends RelativeLayout {

    //TODO: do we really need to cache it???
    Bundle data;
    final Fragment fragment;

    AbsWidget(Fragment fragment) {
        super(fragment.getActivity());
        this.fragment = fragment;
    }

    public void buildView(boolean isEdit) {
        removeAllViews();
        inflate(this.getContext(), getWidgetLayout(isEdit), this);
        setupHolder(isEdit);
    }

    protected abstract void setupHolder(boolean isEdit);

    protected abstract int getWidgetLayout(boolean isEdit);

    public void updateData(Bundle data) {
        this.data = data;
        performUpdate();
    }

    protected abstract void performUpdate();

    public void onClick() {
    }

    boolean isEditMode() {
        return data.getBoolean(WidgetProperty.EDIT_MODE.name(), false);
    }

}
