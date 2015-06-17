package org.kineticsfoundation.sync.synchronizers;

import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import com.lohika.sync.account.AccountManager;
import de.akquinet.android.androlog.Log;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.dao.persist.EntityWriter;
import org.kineticsfoundation.dao.persist.MasterDAO;
import org.kineticsfoundation.net.api.ProtocolRequestException;
import org.kineticsfoundation.net.api.TestSessionManager;

import static org.kineticsfoundation.dao.CacheContract.Tables.TEST_SESSION;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUriById;
import static org.kineticsfoundation.net.NetworkConstants.ErrorCodes.TEST_NOT_FOUND;

/**
 * Performs synchronization of specific Test Session details only -> gets RAW data and extension data
 * Created by akaverin on 7/5/13.
 */
public class TestSessionDetailSynchronizer extends AbsSynchronizer {

    public static final int DEFAULT_VALUE = -1;
    private final MasterDAO masterDAO;
    private final TestSessionManager testSessionManager;

    public TestSessionDetailSynchronizer(AccountManager accountManager, MasterDAO masterDAO, TestSessionManager
            testSessionManager) {
        super(accountManager);
        this.masterDAO = masterDAO;
        this.testSessionManager = testSessionManager;
    }

    @Override
    protected void postLocalChangesSafe(SyncResult result, Bundle extraData) {
        //no-op
    }

    @Override
    protected void getRemoteChangesSafe(SyncResult result, Bundle extraData) {
        Integer id = extraData.getInt(CacheContract.Columns.ID, DEFAULT_VALUE);
        //we need guard here, as our logic can be executed after Account creation
        if (id.equals(DEFAULT_VALUE)) {
            return;
        }
        try {
            TestSession testSession = testSessionManager.getDetails(getSessionToken(), id);
            EntityWriter<TestSession> entityWriter = masterDAO.getWriter(TestSession.class);
            entityWriter.save(testSession);

        } catch (ProtocolRequestException e) {
            if (e.getError().getCode().equals(TEST_NOT_FOUND)) {
                Log.w(this, "Test with ID:" + id + " was not found on server");
                masterDAO.getContentResolver().delete(createUri(TEST_SESSION), CacheContract.Columns.ID + "=?",
                        new String[]{id.toString()});
            } else {
                throw e;
            }
        }
        //manual request to force Complex Place Adapter to be reloaded
        Uri notifyUri = createUriById(CacheContract.VirtualTables.TEST_SESSION_DETAILS,
                extraData.getLong(CacheContract.Columns._ID));

        masterDAO.getContentResolver().notifyChange(notifyUri, null, false);
    }
}
