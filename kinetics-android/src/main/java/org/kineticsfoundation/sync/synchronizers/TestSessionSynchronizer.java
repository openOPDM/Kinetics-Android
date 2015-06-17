package org.kineticsfoundation.sync.synchronizers;

import android.content.ContentValues;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.lohika.sync.account.AccountManager;
import de.akquinet.android.androlog.Log;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.dao.persist.EntityWriter;
import org.kineticsfoundation.dao.persist.MasterDAO;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;
import org.kineticsfoundation.net.api.ProtocolRequestException;
import org.kineticsfoundation.net.api.TestSessionManager;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.kineticsfoundation.dao.CacheContract.Columns;
import static org.kineticsfoundation.dao.CacheContract.Columns.*;
import static org.kineticsfoundation.dao.CacheContract.Sync.*;
import static org.kineticsfoundation.dao.CacheContract.Tables.TEST_SESSION;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.buildInClause;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;

/**
 * Performs sync of {@link org.kineticsfoundation.dao.model.TestSession} entities
 * Created by akaverin on 6/3/13.
 */
public class TestSessionSynchronizer extends AbsSynchronizer {

    private final MasterDAO masterDAO;
    private final TestSessionManager testSessionManager;
    private final ContentValues syncedMarker;
    private final ContentProviderHelper providerHelper;

    public TestSessionSynchronizer(AccountManager accountManager, MasterDAO masterDAO, TestSessionManager
            testSessionManager) {
        super(accountManager);
        this.masterDAO = masterDAO;
        this.testSessionManager = testSessionManager;

        providerHelper = new ContentProviderHelper(masterDAO.getContentResolver());
        syncedMarker = new ContentValues();
        syncedMarker.put(SYNC, getSyncString(NO_SYNC));
    }

    @Override
    protected void postLocalChangesSafe(SyncResult result, Bundle extraData) {
        postCreatedTests(result);
        postModifiedTests(result);
        postDeletedTests(result);
    }

    @Override
    protected void getRemoteChangesSafe(SyncResult result, Bundle extraData) {
        List<TestSession> testSessions = testSessionManager.getAll(getSessionToken());
        if (testSessions.isEmpty()) {
            deleteAll(masterDAO.getContentResolver(), TEST_SESSION);
            return;
        }
        EntityWriter<TestSession> entityWriter = masterDAO.getWriter(TestSession.class);
        entityWriter.save(testSessions);

        cleanup(masterDAO.getContentResolver(), TEST_SESSION, testSessions);
    }

    private void postCreatedTests(SyncResult result) {
        //post local create items
        Collection<TestSession> createdTestSessions = masterDAO.getReader(TestSession.class).getAllByField(SYNC,
                getSyncString(CREATED));
        if (createdTestSessions.isEmpty()) {
            return;
        }
        for (TestSession testSession : createdTestSessions) {
            try {
                testSession.setId(testSessionManager.add(getSessionToken(), testSession));
                ++result.stats.numInserts;

                updateCreatedTest(testSession);

            } catch (ProtocolRequestException e) {
                ++result.stats.numIoExceptions;
                Log.e(this, "Failed to add Test Session with ID " + testSession.getId(), e);
            }
        }
    }

    private void postModifiedTests(SyncResult result) {
        Cursor cursor = providerHelper.queryByField(TEST_SESSION, new String[]{ID, VALID}, SYNC,
                getSyncString(MODIFIED));
        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }
        List<Integer> validIds = newArrayList();
        List<Integer> invalidIds = newArrayList();
        while (cursor.moveToNext()) {
            if (cursor.getInt(1) == 1) {
                validIds.add(cursor.getInt(0));
            } else {
                invalidIds.add(cursor.getInt(0));
            }
        }
        cursor.close();

        postModifyStatusRequest(result, validIds, true);
        postModifyStatusRequest(result, invalidIds, false);
    }

    private void postDeletedTests(SyncResult result) {
        Cursor cursor = providerHelper.queryByField(TEST_SESSION, new String[]{_ID, ID}, SYNC, getSyncString(DELETED));
        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }
        List<Integer> serverIds = newArrayListWithCapacity(cursor.getCount());
        List<String> ids = newArrayListWithCapacity(cursor.getCount());
        while (cursor.moveToNext()) {
            serverIds.add(cursor.getInt(1));
            ids.add(cursor.getString(0));
        }
        cursor.close();

        testSessionManager.delete(getSessionToken(), serverIds);
        result.stats.numDeletes += serverIds.size();

        //consider to delete by IDs rather than by status, as new entities can be deleted...
        masterDAO.getContentResolver().delete(createUri(TEST_SESSION), buildInClause(_ID, ids.size(), false), ids
                .toArray(new String[ids.size()]));
    }

    private void postModifyStatusRequest(SyncResult result, List<Integer> ids, boolean isValid) {
        if (ids.isEmpty()) {
            return;
        }
        try {
            testSessionManager.modifyStatus(getSessionToken(), ids, isValid);
            result.stats.numUpdates += ids.size();
            markSynced(ids);

        } catch (ProtocolRequestException e) {
            Log.e(this, "Failed to post isValid change.", e);
        }
    }

    private void markSynced(List<Integer> serverIds) {
        masterDAO.getContentResolver().update(createUri(TEST_SESSION), syncedMarker, buildInClause(ID, serverIds.size(),
                false), Lists.transform(serverIds, Functions.toStringFunction()).toArray(new String[serverIds.size()]));
    }

    private void updateCreatedTest(TestSession testSession) {
        ContentValues updateValues = new ContentValues(syncedMarker);
        updateValues.put(ID, testSession.getId());
        masterDAO.getContentResolver().update(createUri(TEST_SESSION), updateValues, Columns._ID + "=?",
                new String[]{testSession.getCacheId().toString()});
    }

    private String getSyncString(CacheContract.Sync sync) {
        return Integer.toString(sync.ordinal());
    }
}
