package org.kineticsfoundation.sync.synchronizers;

import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.Bundle;
import com.google.common.collect.Lists;
import com.lohika.sync.account.AccountManager;
import com.lohika.sync.api.Synchronizer;
import com.lohika.sync.core.exception.SyncException;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.UniqueEntity;
import org.kineticsfoundation.net.api.RequestException;
import org.kineticsfoundation.util.NetworkUtils;

import java.util.ArrayList;
import java.util.Collection;

import static org.kineticsfoundation.dao.provider.ContentProviderHelper.buildInClause;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;

/**
 * Genetic sync logic
 * Created by akaverin on 6/3/13.
 */
abstract class AbsSynchronizer implements Synchronizer {

    private final AccountManager accountManager;

    AbsSynchronizer(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public void postLocalChanges(SyncResult result, Bundle extraData) {
        try {
            postLocalChangesSafe(result, extraData);
        } catch (RequestException e) {
            throw new SyncException(e);
        }
    }

    @Override
    public void getRemoteChanges(SyncResult result, Bundle extraData) {
        try {
            getRemoteChangesSafe(result, extraData);
        } catch (RequestException e) {
            throw new SyncException(e);
        }
    }

    /**
     * Template method pattern
     *
     * @param result    operations counter
     * @param extraData extra from {@link com.lohika.sync.core.SyncAdapter}
     * @throws RequestException                                     for request exceptions
     * @throws com.lohika.sync.core.exception.SynAuthErrorException for issues with token
     */
    protected abstract void postLocalChangesSafe(SyncResult result, Bundle extraData);

    protected abstract void getRemoteChangesSafe(SyncResult result, Bundle extraData);

    String getSessionToken() {
        return NetworkUtils.getSessionToken(accountManager);
    }

    static <E extends UniqueEntity> void cleanup(ContentResolver contentResolver, String table,
                                                 Collection<E> entities) {
        ArrayList<String> ids = Lists.newArrayListWithCapacity(entities.size());
        for (UniqueEntity entity : entities) {
            ids.add(entity.getId().toString());
        }
        String whereClause = buildInClause(CacheContract.Columns.ID, ids.size(), true);
        contentResolver.delete(createUri(table), whereClause, ids.toArray(new String[ids.size()]));
    }

    static void deleteAll(ContentResolver contentResolver, String table) {
        contentResolver.delete(createUri(table), null, null);
    }
}
