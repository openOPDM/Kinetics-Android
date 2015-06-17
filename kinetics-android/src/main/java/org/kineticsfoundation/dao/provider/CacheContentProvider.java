package org.kineticsfoundation.dao.provider;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import novoda.lib.sqliteprovider.provider.SQLiteContentProviderImpl;
import novoda.lib.sqliteprovider.sqlite.ExtendedSQLiteOpenHelper;
import novoda.lib.sqliteprovider.util.Log;
import novoda.lib.sqliteprovider.util.UriUtils;
import org.kineticsfoundation.dao.CacheContract;

import java.io.IOException;
import java.util.Map;

import static org.kineticsfoundation.dao.CacheContract.Columns;
import static org.kineticsfoundation.dao.CacheContract.Columns.*;
import static org.kineticsfoundation.dao.CacheContract.Tables.*;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;
import static org.kineticsfoundation.dao.provider.DbUtils.*;

/**
 * Our main {@link android.content.ContentProvider} implementation
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/24/13
 * Time: 11:43 AM
 */
public class CacheContentProvider extends SQLiteContentProviderImpl {

    @SuppressWarnings("SpellCheckingInspection")
    public static final String AUTHORITY = "org.kineticsfoundation";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int EXT_DATA_CODE = 0;
    private static final int TEST_SESSION_DATA_CODE = 1;

    static {
        matcher.addURI(AUTHORITY, CacheContract.VirtualTables.EXT_METADATA_COMBINED, EXT_DATA_CODE);
        matcher.addURI(AUTHORITY, CacheContract.VirtualTables.TEST_SESSION_DETAILS + "/#", TEST_SESSION_DATA_CODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (matcher.match(uri)) {
            case EXT_DATA_CODE:
                return queryForExtCombined(projection);

            case TEST_SESSION_DATA_CODE:
                return queryForTestSessionDetails(uri);
        }
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * @param uri testSession URI
     * @return merged cursor, with first line -> TestSession data, and other lines - extension data
     */
    private Cursor queryForTestSessionDetails(Uri uri) {
        Long testId = ContentUris.parseId(uri);

        String[] selectionArgs = {testId.toString()};

        SQLiteQueryBuilder testQueryBuilder = new SQLiteQueryBuilder();
        testQueryBuilder.setTables(CacheContract.Tables.TEST_SESSION);

        Cursor testCursor = testQueryBuilder.query(getReadableDatabase(), null, _ID + "=?", selectionArgs, null,
                null, null);
        //no such test, just return empty cursor
        testCursor.setNotificationUri(getContext().getContentResolver(), uri);
        if (testCursor.getCount() == 0) {
            return testCursor;
        }

        SQLiteQueryBuilder extQueryBuilder = new SQLiteQueryBuilder();
        extQueryBuilder.setTables(EXT_DATA);

        Cursor extCursor = extQueryBuilder.query(getReadableDatabase(), null, Columns.TEST_SESSION_ID +
                "=?", selectionArgs, null, null, _ID);

        return new MergeCursor(new Cursor[]{testCursor, extCursor});
    }

    private Cursor queryForExtCombined(String[] projection) {

//        SELECT e.*, el.label as "Value"
//        FROM ext_metadata e LEFT JOIN ext_list_node el on (e._id = el.ext_metadata_id)
//        ORDER BY el._id

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setProjectionMap(QueryParams.EXT_DATA_PROJECTION_MAP);

        queryBuilder.setTables(joinFrom(EXT_METADATA, leftJoin(EXT_LIST_NODE, select(EXT_METADATA, _ID),
                select(EXT_LIST_NODE, EXT_METADATA_ID))));

        Cursor cursor = queryBuilder.query(getReadableDatabase(), projection, null, null, null, null,
                select(EXT_METADATA, _ID) + "," + select(EXT_LIST_NODE, _ID));

        cursor.setNotificationUri(getContext().getContentResolver(), createUri(EXT_METADATA));
        return cursor;
    }

    //We override it to skip Upsert logic of SQLiteContentProviderImpl
    @Override
    protected Uri insertInTransaction(Uri uri, ContentValues values) {
        ContentValues insertValues = (values != null) ? new ContentValues(values) : new ContentValues();

        long rowId = getWritableDatabase().insert(UriUtils.getItemDirID(uri), null, insertValues);
        if (rowId > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, rowId);
            notifyUriChange(newUri);
            return newUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public void notifyUriChange(Uri uri) {
        if (uri.getBooleanQueryParameter(SYNC, false)) {
            getContext().getContentResolver().notifyChange(uri, null, true);
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    protected SQLiteOpenHelper getDatabaseHelper(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return new ExtendedSQLiteOpenHelper(context) {
                    /**
                     * Override onConfigure to turn on Foreign Keys support in SQLite
                     */
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onConfigure(SQLiteDatabase db) {
                        super.onConfigure(db);
                        db.setForeignKeyConstraintsEnabled(true);
                    }
                };
            } else {
                return new ExtendedSQLiteOpenHelper(context) {
                    /**
                     * For lower version need to setup Foreign Keys support manually
                     */
                    @Override
                    public void onOpen(SQLiteDatabase db) {
                        super.onOpen(db);
                        db.execSQL("PRAGMA foreign_keys = ON;");
                    }
                };
            }

        } catch (IOException e) {
            Log.Provider.e(e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static final class QueryParams {
        private static final Map<String, String> EXT_DATA_PROJECTION_MAP = ProjectionMap.builder()
                .add(_ID, select(EXT_METADATA, _ID))
                .add(Columns.ID, select(EXT_METADATA, Columns.ID))
                .add(NAME, select(EXT_METADATA, NAME))
                .add(TYPE, select(EXT_METADATA, TYPE))
                .add(PROPS, select(EXT_METADATA, PROPS))
                .add(VALUE, select(EXT_LIST_NODE, LABEL))
                .build();
    }

}
