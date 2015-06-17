package org.kineticsfoundation.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;
import org.kineticsfoundation.util.Format;

import java.util.Date;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;

/**
 * Our {@link android.widget.ListAdapter} implementation specific to Tests List
 * Created by akaverin on 6/6/13.
 */
public class TestListCursorAdapter extends CursorAdapter {

    private static final CompoundButton.OnCheckedChangeListener CHECKBOX_LISTENER = new CompoundButton
            .OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            CheckTag tag = (CheckTag) buttonView.getTag();

            new ChangeStatusTask(buttonView.getContext(), isChecked).execute(tag);
        }
    };
    private final LayoutInflater inflater;
    private final java.text.DateFormat dateFormat;
    private final java.text.DateFormat timeFormat;

    public TestListCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        inflater = LayoutInflater.from(context);
        dateFormat = getDateFormat(context);
        timeFormat = getTimeFormat(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.test_list_row_item, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
        holder.checkBox.setTag(new CheckTag());

        holder.date = (TextView) view.findViewById(android.R.id.text1);
        holder.type = (TextView) view.findViewById(android.R.id.text2);
        holder.score = (TextView) view.findViewById(R.id.score);

        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        //map values
        Date date = new Date(cursor.getLong(cursor.getColumnIndex(CacheContract.Columns.CREATION)));
        holder.date.setText(dateFormat.format(date) + " " + timeFormat.format(date));

        holder.type.setText(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.TYPE)));

        holder.score.setText(Format.formatScoreOutput(cursor));

        //we need to disable notifications while changing checkBox state
        holder.checkBox.setOnClickListener(null);
        holder.checkBox.setChecked(cursor.getInt(cursor.getColumnIndex(CacheContract.Columns.VALID)) == 1);
        holder.checkBox.setOnCheckedChangeListener(CHECKBOX_LISTENER);

        //set id
        CheckTag checkTag = (CheckTag) holder.checkBox.getTag();
        checkTag._id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        checkTag.sync = cursor.getInt(cursor.getColumnIndex(CacheContract.Columns.SYNC));
    }

    private static final class ChangeStatusTask extends AsyncTask<CheckTag, Void, Void> {

        private final ContentProviderHelper providerHelper;
        private final boolean isChecked;

        private ChangeStatusTask(Context context, boolean checked) {
            isChecked = checked;
            providerHelper = new ContentProviderHelper(context.getContentResolver());
        }

        @Override
        protected Void doInBackground(CheckTag... params) {
            CheckTag tag = params[0];

            ContentValues values = new ContentValues();
            values.put(CacheContract.Columns.VALID, isChecked);
            //we cannot MODIFY entities which are not CREATED yet, so just update value
            if (tag.sync != CacheContract.Sync.CREATED.ordinal()) {
                values.put(CacheContract.Columns.SYNC, CacheContract.Sync.MODIFIED.ordinal());
            }
            providerHelper.update(CacheContract.Tables.TEST_SESSION, tag._id, values, true);
            return null;
        }
    }

    private static final class ViewHolder {
        CheckBox checkBox;
        TextView date;
        TextView type;
        TextView score;
    }

    private static final class CheckTag {
        int _id;
        int sync;
    }

}
