package org.kineticsfoundation.dao.task;

import android.content.ContentResolver;
import android.os.AsyncTask;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

/**
 * Performs cleanup of DB
 * Created by akaverin on 7/11/13.
 */
public class CleanDbTask extends AsyncTask<Void, Void, Void> {

    private final ContentResolver contentResolver;

    public CleanDbTask(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    protected Void doInBackground(Void... params) {
        contentResolver.delete(ContentProviderHelper.createUri(CacheContract.Tables.TEST_SESSION), null, null);
        contentResolver.delete(ContentProviderHelper.createUri(CacheContract.Tables.EXT_METADATA), null, null);
        return null;
    }
}
