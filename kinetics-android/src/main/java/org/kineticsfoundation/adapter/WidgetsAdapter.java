package org.kineticsfoundation.adapter;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import org.kineticsfoundation.widget.*;

import java.util.List;

/**
 * Adapter to show widget arrays
 * Created by akaverin on 6/7/13.
 */
//TODO: Busy Coder page 772 -> consider better approach to handle Model <-> Widget changes
public class WidgetsAdapter extends ArrayAdapter<Bundle> {

    private final Fragment fragment;

    public WidgetsAdapter(Fragment fragment, List<Bundle> attrs) {
        super(fragment.getActivity().getApplicationContext(), 0, attrs);
        this.fragment = fragment;
    }

    @Override
    public int getViewTypeCount() {
        return WidgetType.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getCount()) {
            return 0;
        }
        return getType(getItem(position)).ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AbsWidget widget = (AbsWidget) convertView;

        Bundle attr = getItem(position);
        if (widget == null) {
            widget = getWidget(getType(attr));
            widget.buildView(attr.getBoolean(WidgetProperty.EDIT_MODE.name()));
        }
        widget.updateData(getItem(position));

        return widget;
    }

    private WidgetType getType(Bundle bundle) {
        return (WidgetType) bundle.getSerializable(WidgetProperty.TYPE.name());
    }

    private AbsWidget getWidget(WidgetType type) {
        switch (type) {
            case TEXT:
            case NUMBER:
                return new TextWidget(fragment);

            case NOTE:
                return new NoteWidget(fragment);

            case CHECKBOX:
                return new CheckWidget(fragment);

            case LIST:
                return new ListWidget(fragment);

            case SEPARATOR:
                return new SeparatorWidget(fragment);

            default:
                return null;
        }
    }
}
