package org.kineticsfoundation.dao.provider;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import com.google.common.base.Strings;
import org.kineticsfoundation.dao.CacheContract;

import java.util.ArrayList;

import static org.kineticsfoundation.dao.provider.CacheContentProvider.CONTENT_URI;

/**
 * Generic ContentProvider operations
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 4/16/13
 * Time: 9:05 AM
 */
public class ContentProviderHelper {

    private static final String QUESTION = "=?";
    //    private static final int SQLITE_MAX_VARIABLE_NUMBER = 999;
    private final ContentResolver contentResolver;

    public ContentProviderHelper(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public int insert(String table, ContentValues values, boolean sync) {
        Uri inserted = contentResolver.insert(createUriSync(table, sync), values);
        return Integer.parseInt(inserted.getLastPathSegment());
    }

    public void insert(String table, ContentValues[] values, boolean sync) {
        contentResolver.bulkInsert(createUriSync(table, sync), values);
    }

    public boolean update(String table, Integer id, ContentValues values, boolean sync) {
        int count = contentResolver.update(createUriSync(table, sync), values, BaseColumns._ID + "=?",
                new String[]{id.toString()});
        return count != 0;
    }

    public void applyOperations(ArrayList<ContentProviderOperation> operations) throws RemoteException,
            OperationApplicationException {
        contentResolver.applyBatch(CacheContentProvider.AUTHORITY, operations);
    }

    public Cursor queryByField(String table, String[] projection, String field, String value) {
        return contentResolver.query(createUri(table), projection, field + "=?", new String[]{value}, null);
    }

    //TODO: support large IN params range, now sqlite throws exception on 1000 items
    public Cursor queryFieldIn(String table, String[] projection, String field, String[] inValues) {
        String inClause = buildInClause(field, inValues.length, false);
        return contentResolver.query(createUri(table), projection, inClause, inValues, null);
    }

    /**
     * @param table to be queried
     * @return the URI to use with {@link android.content.ContentResolver}
     */
    public static Uri createUri(String table) {
        return Uri.withAppendedPath(CONTENT_URI, table);
    }

    /**
     * @param table to be queried
     * @return the URI to use with {@link android.content.ContentResolver}
     */
    private static Uri createUriSync(String table, Boolean sync) {
        return Uri.withAppendedPath(CONTENT_URI, table).buildUpon().appendQueryParameter(CacheContract.Columns.SYNC,
                sync.toString()).build();
    }

    public static Uri createUriById(String table, Long id) {
        return createUri(table).buildUpon().appendPath(id.toString()).build();
    }

    public static String buildInClause(String field, int argNum, boolean isNotClause) {
        StringBuilder inClause = new StringBuilder(2 * argNum + 6 + field.length());
        inClause.append(field);
        if (isNotClause) {
            inClause.append(" not");
        }
        inClause.append(" in (").append(Strings.repeat("?,", argNum - 1)).append("?)");
        return inClause.toString();
    }
}
