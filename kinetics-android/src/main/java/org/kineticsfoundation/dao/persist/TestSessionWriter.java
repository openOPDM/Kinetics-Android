package org.kineticsfoundation.dao.persist;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import com.google.common.base.Functions;
import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.dao.model.extension.ExtensionData;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static android.content.ContentProviderOperation.newDelete;
import static android.content.ContentProviderOperation.newUpdate;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.kineticsfoundation.dao.CacheContract.Columns;
import static org.kineticsfoundation.dao.CacheContract.Tables.EXT_DATA;
import static org.kineticsfoundation.dao.CacheContract.Tables.TEST_SESSION;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.buildInClause;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;

/**
 * Our mapper class from {@link org.kineticsfoundation.dao.model.TestSession} to {@link android.content.ContentValues}
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/24/13
 * Time: 11:42 AM
 */
public class TestSessionWriter extends AbsEntityWriter<TestSession> {

    private static final BulkUpdatePlugin<TestSession> UPDATE_PLUGIN = new BulkUpdatePlugin<TestSession>() {
        @Override
        public Collection<ContentProviderOperation> getPrepareOperations(Map<Integer, Long> uniqueToCacheId,
                                                                         Collection<TestSession> testSessions) {
            return newArrayList(newDelete(createUri(EXT_DATA))
                    .withSelection(buildInClause(Columns.TEST_SESSION_ID, uniqueToCacheId.size(), false),
                            transform(uniqueToCacheId.values(), Functions.toStringFunction()).toArray(new
                                    String[uniqueToCacheId.size()])).build());
        }

        @Override
        public ContentValues entityToContentValues(TestSession entity) {
            return mapTestSessionToContentValues(entity);
        }

        @Override
        public Collection<ContentProviderOperation> getUpdateDependentOperations(TestSession testSession,
                                                                                 Long entityId) {
            return buildExtDataInsertOperations(entityId.intValue(), testSession);
        }
    };

    public TestSessionWriter(ContentProviderHelper providerHelper) {
        super(providerHelper, TEST_SESSION, UPDATE_PLUGIN);
    }

    @Override
    protected int insert(TestSession testSession) {
        final int newId = providerHelper.insert(TEST_SESSION, mapTestSessionToContentValues(testSession), false);
        //insert dependent
        apply(buildExtDataInsertOperations(newId, testSession));

        return newId;
    }

    @Override
    protected void update(Integer testSessionId, TestSession testSession) {
        ArrayList<ContentProviderOperation> pendingOperations = newArrayList();

        String[] selectionArgs = {testSessionId.toString()};
        pendingOperations.add(newUpdate(createUri(TEST_SESSION))
                .withValues(mapTestSessionToContentValues(testSession))
                .withSelection(Columns._ID + "=?", selectionArgs)
                .withExpectedCount(1).build());

        //TODO: handle case when Extensions will only come with Details...
        //cleanup old data
        pendingOperations.addAll(buildExtDataCleanOperations(testSessionId));

        pendingOperations.addAll(buildExtDataInsertOperations(testSessionId, testSession));

        apply(pendingOperations);
    }

    @Override
    protected void insert(Collection<TestSession> testSessions) {
        List<ContentValues> sessionValues = newArrayListWithCapacity(testSessions.size());
        for (TestSession testSession : testSessions) {
            sessionValues.add(mapTestSessionToContentValues(testSession));
        }
        providerHelper.insert(TEST_SESSION, sessionValues.toArray(new ContentValues[sessionValues.size()]), false);

        Map<Integer, Long> testSessionIdsMap = buildIdsMapping(testSessions);

        ArrayList<ContentProviderOperation> pendingOperations = newArrayList();
        for (TestSession testSession : testSessions) {
            pendingOperations.addAll(buildExtDataInsertOperations(testSessionIdsMap.get(testSession
                    .getId()).intValue(), testSession));
        }
        apply(pendingOperations);
    }

    private static ContentValues mapTestSessionToContentValues(TestSession testSession) {
        ContentValues values = new ContentValues();

        values.put(Columns.ID, testSession.getId());
        values.put(Columns.TYPE, testSession.getType());
        values.put(Columns.SCORE, testSession.getScore());
        //avoid cleaning up, if we have non Details request
        if (testSession.getRawData() != null) {
            values.put(Columns.RAW_DATA, testSession.getRawData());
        }
        values.put(Columns.NOTES, testSession.getNotes());
        values.put(Columns.VALID, testSession.getIsValid());
        values.put(Columns.CREATION, testSession.getCreationDate().getTime());

        return values;
    }

    private static ContentValues mapExtensionDataToContentValues(int testSessionId, ExtensionData data) {
        ContentValues values = new ContentValues();

        values.put(Columns.NAME, data.getName());
        values.put(Columns.VALUE, data.getValue());
        values.put(Columns.META_ID, data.getMetaId());
        values.put(Columns.TEST_SESSION_ID, testSessionId);

        return values;
    }

    private static ArrayList<ContentProviderOperation> buildExtDataInsertOperations(int testSessionId,
                                                                                    TestSession testSession) {
        ArrayList<ContentProviderOperation> pendingOperations = newArrayList();
        if (testSession.getExtension() == null) {
            return pendingOperations;
        }
        for (ExtensionData data : testSession.getExtension()) {
            pendingOperations.add(ContentProviderOperation.newInsert(createUri(EXT_DATA))
                    .withValues(mapExtensionDataToContentValues(testSessionId, data)).build());
        }
        return pendingOperations;
    }

    private static Collection<ContentProviderOperation> buildExtDataCleanOperations(int testSessionId) {
        ArrayList<ContentProviderOperation> operations = newArrayList();
        operations.add(newDelete(createUri(EXT_DATA))
                .withSelection(Columns.TEST_SESSION_ID + "=?", new String[]{Integer.toString(testSessionId)})
                .build());
        return operations;
    }
}
