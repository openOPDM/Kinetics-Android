package org.kineticsfoundation.dao.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import com.google.common.collect.Lists;
import org.kineticsfoundation.dao.CacheContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static org.kineticsfoundation.dao.CacheContract.Tables;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.buildInClause;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;

/**
 * Delete Async task
 * Created by akaverin on 7/8/13.
 */
public class TestDeleteTask extends AsyncTask<Void, Void, Void> {

    private final ArrayList<Long> ids;
    private final ContentResolver contentResolver;

    public TestDeleteTask(ContentResolver contentResolver, Long id) {
        this.contentResolver = contentResolver;
        this.ids = Lists.newArrayList(id);
    }

    public TestDeleteTask(ContentResolver contentResolver, Collection<Long> ids) {
        this.contentResolver = contentResolver;
        this.ids = Lists.newArrayList(ids);
    }

    @Override
    protected Void doInBackground(Void... params) {
        String inClause = buildInClause(CacheContract.Columns._ID, ids.size(), false);
        List<String> args = Lists.newArrayList(Integer.toString(CacheContract.Sync.CREATED.ordinal()));
        args.addAll(transform(ids, com.google.common.base.Functions.toStringFunction()));
        String[] selectionArgs = args.toArray(new String[args.size()]);

        int deleteCnt = contentResolver.delete(createUri(Tables.TEST_SESSION), CacheContract.Columns.SYNC + "=? AND "
                + inClause, selectionArgs);
        //nothing to update
        if (deleteCnt == ids.size()) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(CacheContract.Columns.SYNC, CacheContract.Sync.DELETED.ordinal());
        contentResolver.update(createUri(Tables.TEST_SESSION), values, CacheContract.Columns.SYNC + "!=? AND " +
                inClause, selectionArgs);

        return null;
    }
}
