package org.kineticsfoundation.sync;

import com.lohika.sync.account.AccountManager;
import com.lohika.sync.api.SynchronizerRegistry;
import com.lohika.sync.core.AbsSyncService;
import com.lohika.sync.core.SimpleSynchronizerRegistry;
import com.lohika.sync.core.SyncAdapter;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.sync.synchronizers.MetadataSynchronizer;
import org.kineticsfoundation.sync.synchronizers.SyncGroup;
import org.kineticsfoundation.sync.synchronizers.TestSessionDetailSynchronizer;
import org.kineticsfoundation.sync.synchronizers.TestSessionSynchronizer;

/**
 * Our service for {@link SyncAdapter}
 * Created by akaverin on 6/3/13.
 */
public class CacheSyncService extends AbsSyncService {

    private static final long DEFAULT_UPLOAD_DELAY = 5;

    @Override
    protected SyncAdapter setupSyncAdapter() {
        SyncAdapter syncAdapter = new SyncAdapter(setupSyncRegistry(), getApplicationContext(), true);
        syncAdapter.setSyncDelay(DEFAULT_UPLOAD_DELAY);
        return syncAdapter;
    }

    private SynchronizerRegistry setupSyncRegistry() {
        final KineticsApplication application = (KineticsApplication) getApplication();
        AccountManager accountManager = application.getAccountManager();

        SimpleSynchronizerRegistry registry = new SimpleSynchronizerRegistry();

        registry.add(new MetadataSynchronizer(accountManager, application.getMasterDAO(),
                application.getRemoteApi().getExtensionManager()), SyncGroup.FULL.name());
        registry.add(new TestSessionSynchronizer(accountManager, application.getMasterDAO(),
                application.getRemoteApi().getTestSessionManager()), SyncGroup.FULL.name(), SyncGroup.TEST.name());
        registry.add(new TestSessionDetailSynchronizer(accountManager, application.getMasterDAO(),
                application.getRemoteApi().getTestSessionManager()), SyncGroup.TEST_DETAILS.name());

        return registry;
    }

}
