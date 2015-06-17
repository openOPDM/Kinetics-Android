package org.kineticsfoundation;

import android.app.Application;
import com.lohika.sync.account.AccountManager;
import com.lohika.sync.core.SyncUtils;
import de.akquinet.android.androlog.Log;
import org.kineticsfoundation.account.Authenticator;
import org.kineticsfoundation.dao.persist.MasterDAO;
import org.kineticsfoundation.dao.provider.CacheContentProvider;
import org.kineticsfoundation.net.server.RemoteApi;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Our custom {@link Application} instance. Singleton by nature, should be used as a handly place for other singletons
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:09 PM
 */
public class KineticsApplication extends Application {

    private AccountManager accountManager;
    private SyncUtils syncUtils;
    private RemoteApi remoteApi;
    private MasterDAO masterDAO;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.init(this);

        accountManager = new AccountManager(this, Authenticator.ACCOUNT_TYPE);
        syncUtils = new SyncUtils(this, accountManager, CacheContentProvider.AUTHORITY);
        remoteApi = new RemoteApi(accountManager);
        masterDAO = new MasterDAO(this);

        syncOnStart();
    }

    public RemoteApi getRemoteApi() {
        return remoteApi;
    }

    public MasterDAO getMasterDAO() {
        return masterDAO;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public SyncUtils getSyncUtils() {
        return syncUtils;
    }

    private void syncOnStart() {
        if (accountManager.isAccountPresent() && getDefaultSharedPreferences(this).getBoolean(getString(R.string
                .key_sync_start), false)) {
            syncUtils.requestSync();
        }
    }
}
