package org.kineticsfoundation.dao.persist;

import android.database.Cursor;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.dao.model.extension.ExtensionData;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static org.kineticsfoundation.dao.CacheContract.Tables.EXT_DATA;

/**
 * TestSession entity reader
 * Created by akaverin on 5/28/13.
 */
public class TestSessionReader extends AbsEntityReader<TestSession> {

    public TestSessionReader(ContentProviderHelper providerHelper) {
        super(providerHelper, CacheContract.Tables.TEST_SESSION);
    }

    @Override
    protected Collection<TestSession> contentValuesToEntities(Cursor cursor) {
        List<TestSession> testSessions = newArrayListWithCapacity(cursor.getCount());

        Map<Integer, String> idsToCacheMap = newHashMap();
        while (cursor.moveToNext()) {
            TestSession testSession = mapTestSession(cursor);
            testSessions.add(testSession);

            idsToCacheMap.put(testSession.getId(), cursor.getString(cursor.getColumnIndex(CacheContract.Columns._ID)));
        }
        //do query for ext_data -> better do 1 WHERE IN...
        Cursor extCursor = providerHelper.queryFieldIn(EXT_DATA, null, CacheContract.Columns.TEST_SESSION_ID,
                idsToCacheMap.values().toArray(new String[idsToCacheMap.size()]));

        Map<String, List<ExtensionData>> extDataMap = newHashMap();
        while (extCursor.moveToNext()) {
            String testSessionId = extCursor.getString(extCursor.getColumnIndex(CacheContract.Columns.TEST_SESSION_ID));

            ExtensionData data = new ExtensionData(extCursor.getString(extCursor.getColumnIndex(CacheContract.Columns
                    .NAME)), extCursor.getString(extCursor.getColumnIndex(CacheContract.Columns.VALUE)),
                    extCursor.getInt(extCursor.getColumnIndex(CacheContract.Columns.META_ID)));

            if (extDataMap.containsKey(testSessionId)) {
                extDataMap.get(testSessionId).add(data);
            } else {
                extDataMap.put(testSessionId, newArrayList(data));
            }
        }

        for (TestSession testSession : testSessions) {
            //search by ID -> server;
            String id = idsToCacheMap.get(testSession.getId());
            testSession.setExtension(extDataMap.get(id));
        }

        extCursor.close();

        return testSessions;
    }

    private TestSession mapTestSession(Cursor cursor) {
        TestSession testSession = new TestSession();
        testSession.setCacheId(cursor.getInt(cursor.getColumnIndex(CacheContract.Columns._ID)));

        int idIndex = cursor.getColumnIndex(CacheContract.Columns.ID);
        if (!cursor.isNull(idIndex)) {
            testSession.setId(cursor.getInt(idIndex));
        }
        testSession.setRawData(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.RAW_DATA)));
        testSession.setCreationDate(new Date(cursor.getLong(cursor.getColumnIndex(CacheContract.Columns.CREATION)
        )));
        testSession.setType(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.TYPE)));
        testSession.setScore(cursor.getDouble(cursor.getColumnIndex(CacheContract.Columns.SCORE)));
        testSession.setIsValid(cursor.getInt(cursor.getColumnIndex(CacheContract.Columns.VALID)) == 1 ?
                Boolean.TRUE : Boolean.FALSE);
        testSession.setNotes(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.NOTES)));
        return testSession;
    }
}
