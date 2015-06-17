package org.kineticsfoundation.widget;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import org.kineticsfoundation.R;

/**
 * Check box widget
 * Created by akaverin on 6/17/13.
 */
public class CheckWidget extends AbsWidget {

    private static final CompoundButton.OnCheckedChangeListener CHECKED_CHANGE_LISTENER = new CompoundButton
            .OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Bundle bundle = (Bundle) buttonView.getTag();
            bundle.putBoolean(WidgetProperty.VALUE.name(), isChecked);
        }
    };

    public CheckWidget(Fragment fragment) {
        super(fragment);
    }

    @Override
    protected void setupHolder(boolean isEdit) {
        ViewHolder holder = new ViewHolder();
        holder.textView = (TextView) findViewById(android.R.id.text1);
        holder.checkBox = (CheckBox) findViewById(android.R.id.checkbox);
        setTag(holder);
    }

    @Override
    protected int getWidgetLayout(boolean isEdit) {
        return R.layout.check_widget;
    }

    @Override
    protected void performUpdate() {
        ViewHolder holder = (ViewHolder) getTag();

        holder.checkBox.setOnCheckedChangeListener(null);

        holder.textView.setText(data.getString(WidgetProperty.NAME.name()));
        holder.checkBox.setChecked(data.getBoolean(WidgetProperty.VALUE.name()));

        if (isEditMode()) {
            holder.checkBox.setTag(data);
            holder.checkBox.setOnCheckedChangeListener(CHECKED_CHANGE_LISTENER);
        } else {
            holder.checkBox.setEnabled(false);
        }
    }

    @Override
    public void onClick() {
        ViewHolder holder = (ViewHolder) getTag();
        holder.checkBox.performClick();
    }

    private static final class ViewHolder {
        TextView textView;
        CheckBox checkBox;
    }
}
