package org.kineticsfoundation.test;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dao.model.extension.ExtensionType;
import org.kineticsfoundation.widget.ListWidget;
import org.kineticsfoundation.widget.WidgetBuilder;
import org.kineticsfoundation.widget.WidgetType;

import java.util.*;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static org.kineticsfoundation.dao.CacheContract.Columns;
import static org.kineticsfoundation.dao.model.extension.ExtensionProperty.isRequired;
import static org.kineticsfoundation.test.TestModel.Attr.*;
import static org.kineticsfoundation.util.Format.formatPstMeasurement;
import static org.kineticsfoundation.util.Format.formatPstRmsMeasurement;
import static org.kineticsfoundation.util.Format.formatScoreOutput;
import static org.kineticsfoundation.widget.WidgetBuilder.builder;

/**
 * TestModel to be used for build widgets UI
 * Created by akaverin on 6/19/13.
 */
public class TestModel implements Parcelable {

    public static final Parcelable.Creator<TestModel> CREATOR = new Parcelable.Creator<TestModel>() {
        public TestModel createFromParcel(Parcel in) {
            return new TestModel(in);
        }

        public TestModel[] newArray(int size) {
            return new TestModel[size];
        }
    };
    private final LinkedHashMap<String, Bundle> widgetAttrs = Maps.newLinkedHashMap();

    private TestModel() {
    }

    private TestModel(Parcel in) {
        ArrayList<String> keys = Lists.newArrayList();
        in.readStringList(keys);
        Bundle[] bundles = (Bundle[]) in.readParcelableArray(getClass().getClassLoader());

        if (keys.size() != bundles.length) {
            throw new IllegalArgumentException("Invalid data");
        }

        for (int i = 0; i < keys.size(); ++i) {
            widgetAttrs.put(keys.get(i), bundles[i]);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeStringList(Lists.newArrayList(widgetAttrs.keySet()));
        out.writeParcelableArray(widgetAttrs.values().toArray(new Bundle[widgetAttrs.size()]), 0);
    }

    public LinkedHashMap<String, Bundle> getWidgetAttrs() {
        return widgetAttrs;
    }

    public static List<Bundle> buildPstDetails(Context context, Cursor data) {
        data.moveToFirst();

        List<Bundle> attrs = Lists.newArrayList();

        attrs.add(buildTSField(context, data));
        attrs.add(buildPstField(JERK.name(), data.getDouble(data.getColumnIndex(Columns.SCORE))));

        String rawData = data.getString(data.getColumnIndex(Columns.RAW_DATA));
        //we might be missing of cache was cleaned
        if (rawData == null) {
            //do nothing so far
            attrs.add(buildPstField(AREA.name(), 0));
            attrs.add(buildPstField(RMS.name(), 0));
        } else {
            Iterator<String> rawIter = Splitter.on(';').split(rawData).iterator();

            attrs.add(buildPstField(AREA.name(), Double.parseDouble(rawIter.next())));
            attrs.add(buildPstField(RMS.name(), Double.parseDouble(rawIter.next())));
        }
        attrs.add(buildValidField(context, data));
        attrs.add(buildNotesField(context, data));

        attrs.add(builder().setType(WidgetType.SEPARATOR).build());
        attrs.addAll(buildExtensionsFields(data));

        return attrs;
    }

    public static List<Bundle> buildTugDetails(Context context, Cursor data) {
        data.moveToFirst();

        List<Bundle> attrs = Lists.newArrayList();

        //parse test session data
        attrs.add(buildTSField(context, data));
        attrs.add(buildScoreField(context, data, R.string.duration_label));
        attrs.add(buildValidField(context, data));
        attrs.add(buildNotesField(context, data));

        attrs.add(builder().setType(WidgetType.SEPARATOR).build());
        attrs.addAll(buildExtensionsFields(data));

        return attrs;
    }

    public static List<Bundle> buildKBDetails(Context context, Cursor data) {
        data.moveToFirst();

        List<Bundle> attrs = Lists.newArrayList();

        attrs.add(buildTSField(context, data));
        attrs.add(buildScoreField(context, data, R.string.score_label));
        attrs.add(buildValidField(context, data));
        attrs.add(buildNotesField(context, data));

        attrs.add(builder().setType(WidgetType.SEPARATOR).build());
        attrs.addAll(buildExtensionsFields(data));

        return attrs;
    }

    public static TestModel buildTugModel(Context context, Cursor cursor) {
        TestModel testModel = new TestModel();

        testModel.widgetAttrs.put(STAGE.name(), buildStageField(context));
        testModel.widgetAttrs.put(TIME.name(), buildTimeField(context, "0"));
        testModel.widgetAttrs.put(IS_CHECKED.name(), buildValidEditField(context));
        testModel.widgetAttrs.put(NOTE.name(), buildNotesEditField(context));

        testModel.widgetAttrs.put(null, builder().setType(WidgetType.SEPARATOR).build());

        testModel.widgetAttrs.putAll(buildExtensions(cursor, true));

        return testModel;
    }

    public static TestModel buildPstModel(Context context, Cursor cursor) {
        TestModel testModel = new TestModel();

        testModel.widgetAttrs.put(STAGE.name(), buildStageField(context));
        testModel.widgetAttrs.put(TIME.name(), buildTimeLeftField(context, "30"));
        testModel.widgetAttrs.put(IS_CHECKED.name(), buildValidEditField(context));

        testModel.widgetAttrs.put(JERK.name(), builder().setType(WidgetType.TEXT).setName(JERK.name()).build());
        testModel.widgetAttrs.put(RMS.name(), builder().setType(WidgetType.TEXT).setName(RMS.name()).build());
        testModel.widgetAttrs.put(AREA.name(), builder().setType(WidgetType.TEXT).setName(AREA.name()).build());

        testModel.widgetAttrs.put(NOTE.name(), buildNotesEditField(context));
        testModel.widgetAttrs.put(null, builder().setType(WidgetType.SEPARATOR).build());

        testModel.widgetAttrs.putAll(buildExtensions(cursor, true));

        return testModel;
    }

    private static Bundle buildStageField(Context context) {
        return builder().setType(WidgetType.TEXT).setName(context.getString(R.string.test_stage_label)).setValue
                (context.getString(R.string.test_start)).build();
    }

    private static Bundle buildTimeField(Context context, String initialValue) {
        return builder().setType(WidgetType.TEXT).setName(context.getString(R.string.test_time_label)).setValue
                (initialValue).build();
    }

    private static Bundle buildTimeLeftField(Context context, String initialValue) {
        return builder().setType(WidgetType.TEXT).setName(context.getString(R.string.test_time_left_label)).setValue
                (initialValue).build();
    }

    private static Bundle buildValidEditField(Context context) {
        return builder().setType(WidgetType.CHECKBOX).setName(context.getString(R.string.is_valid_label)).setValue
                (true).setMode(true).build();
    }

    private static Bundle buildNotesEditField(Context context) {
        return builder().setType(WidgetType.NOTE).setName(context.getString(R.string.note_label)).setMode(true).build();
    }

    private static List<Bundle> buildExtensionsFields(Cursor data) {
        List<Bundle> extensions = Lists.newArrayList();
        while (data.moveToNext()) {
            extensions.add(builder().setType(WidgetType.TEXT).setName(data.getString(data.getColumnIndex(Columns.NAME)))
                    .setValue(data.getString(data.getColumnIndex(Columns.VALUE))).build());
        }
        return extensions;
    }

    private static Bundle buildNotesField(Context context, Cursor data) {
        return builder().setType(WidgetType.NOTE).setName(context.getString(R.string.note_label)).setValue(data
                .getString(data.getColumnIndex(Columns.NOTES))).build();
    }

    private static Bundle buildValidField(Context context, Cursor data) {
        return builder().setType(WidgetType.CHECKBOX).setName(context.getString(R.string.is_valid_label)).setValue
                (data.getInt(data.getColumnIndex(Columns.VALID)) != 0).build();
    }

    private static Bundle buildScoreField(Context context, Cursor data, int resId) {
        return builder().setType(WidgetType.TEXT).setName(context.getString(resId)).setValue(formatScoreOutput(data))
                .build();
    }

    private static Bundle buildPstField(String name, double value) {
        WidgetBuilder builder = builder().setType(WidgetType.TEXT).setName(name);
        if (value > 0) {
            builder.setValue("RMS".equals(name)?formatPstRmsMeasurement(value):formatPstMeasurement(value));
        }
        return builder.build();
    }

    private static Bundle buildTSField(Context context, Cursor data) {
        Date date = new Date(data.getLong(data.getColumnIndex(Columns.CREATION)));
        return builder().setType(WidgetType.TEXT).setName(context.getString(R.string.timestamp_label)).setValue
                (getDateFormat(context).format(date) + " " + getTimeFormat(context).format(date)).build();
    }

    private static LinkedHashMap<String, Bundle> buildExtensions(Cursor cursor, boolean isEdit) {
        LinkedHashMap<String, Bundle> attrs = Maps.newLinkedHashMap();
        while (cursor.moveToNext()) {
            WidgetBuilder builder = builder().setExtension().setRequired(isRequired(cursor.getInt(cursor
                    .getColumnIndex(Columns.PROPS))));
            ExtensionType type = ExtensionType.valueOf(cursor.getString(cursor.getColumnIndex(Columns.TYPE)));
            Bundle extra = new Bundle();
            extra.putInt(Columns.ID, cursor.getInt(cursor.getColumnIndex(Columns.ID)));
            switch (type) {
                case NUMERIC:
                    builder.setType(WidgetType.NUMBER);
                    break;

                case TEXT:
                    builder.setType(WidgetType.TEXT);
                    break;

                case LIST:
                    int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    ArrayList<String> listValues = Lists.newArrayList();
                    do {
                        listValues.add(cursor.getString(cursor.getColumnIndex(Columns.VALUE)));

                    } while (cursor.moveToNext() && id == cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));

                    //we reached end of current list
                    cursor.moveToPrevious();

                    extra.putStringArray(ListWidget.KEY_VALUES, listValues.toArray(new String[listValues.size()]));

                    builder.setType(WidgetType.LIST);
                    break;
            }
            String name = cursor.getString(cursor.getColumnIndex(Columns.NAME));
            attrs.put(name, builder.setName(name).setMode(isEdit).setExtra(extra).build());
        }
        return attrs;
    }

    public enum Attr {
        STAGE, TIME, IS_CHECKED, NOTE, JERK, RMS, AREA
    }
}
