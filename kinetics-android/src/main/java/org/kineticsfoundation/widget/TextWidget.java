package org.kineticsfoundation.widget;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.dialog.SimpleEditButtonDialog;
import org.kineticsfoundation.fragment.FragmentUtils;

/**
 * Usual TextWidget
 * Created by akaverin on 6/7/13.
 */
public class TextWidget extends AbsWidget {

    public TextWidget(Fragment fragment) {
        super(fragment);
    }

    @Override
    protected void setupHolder(boolean isEdit) {
        TextView textView = (TextView) findViewById(android.R.id.text1);
        TextView textView2 = (TextView) findViewById(android.R.id.text2);

        ViewHolder holder = new ViewHolder();
        holder.textView1 = textView;
        holder.textView2 = textView2;
        holder.textView2.setHint(R.string.widget_text_hint);

        setTag(holder);
    }

    @Override
    protected int getWidgetLayout(boolean isEdit) {
        return R.layout.text_widget;
    }

    @Override
    protected void performUpdate() {
        ViewHolder holder = (ViewHolder) getTag();

        holder.textView1.setText(data.getString(WidgetProperty.NAME.name()));

        String value = data.getString(WidgetProperty.VALUE.name());
        holder.textView2.setText(value != null ? Html.fromHtml(value):value);

        if (!data.getBoolean(WidgetProperty.VALID.name(), true)) {
            setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            holder.textView2.setHint("!");
        } else {
            setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

    @Override
    public void onClick() {
        if (!isEditMode()) {
            return;
        }
        Bundle extra = new Bundle();
        extra.putString(DialogConts.KEY_ID, data.getString(WidgetProperty.NAME.name()));

        SimpleEditButtonDialog.Builder builder = new SimpleEditButtonDialog.Builder(0,
                (DialogListener) fragment).setTitle(R.string.dialog_enter_value)
                .setMessage(data.getString(WidgetProperty.NAME.name())).setEditHint(R.string.new_value)
                .setPositiveButton(android.R.string.ok)
                .setNegativeButton(android.R.string.cancel).setExtra(extra);

        if (data.containsKey(WidgetProperty.VALUE.name())) {
            builder.setDefaultValue(data.getString(WidgetProperty.VALUE.name()));
        }

        if (data.getSerializable(WidgetProperty.TYPE.name()).equals(WidgetType.NUMBER)) {
            builder.setEditProps(EditorInfo.TYPE_CLASS_NUMBER);
        }
        FragmentUtils.showDialog(fragment, builder.build());
    }

    static final class ViewHolder {
        TextView textView1;
        TextView textView2;
    }

}
