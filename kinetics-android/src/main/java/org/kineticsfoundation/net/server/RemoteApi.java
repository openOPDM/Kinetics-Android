package org.kineticsfoundation.net.server;

import com.lohika.restclient.RestProcessor;
import org.kineticsfoundation.net.api.AccountManager;
import org.kineticsfoundation.net.api.ExtensionManager;
import org.kineticsfoundation.net.api.ProjectManager;
import org.kineticsfoundation.net.api.TestSessionManager;

/**
 * Our entry point to Server exposed API. Immutable object.
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:11 PM
 */
public class RemoteApi {

    private final AccountManager accountManager;
    private final ProjectManager projectManager;
    private final TestSessionManager testSessionManager;
    private final ExtensionManager extensionManager;

    public RemoteApi(com.lohika.sync.account.AccountManager appAccountManager) {
        RestProcessor processor = new RestProcessor();

        accountManager = new AccountManagerImpl(processor, appAccountManager);
        projectManager = new ProjectManagerImpl(processor, appAccountManager);
        testSessionManager = new TestSessionManagerImpl(processor, appAccountManager);
        extensionManager = new ExtensionManagerImpl(processor, appAccountManager);
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public ProjectManager getProjectManager() {
        return projectManager;
    }

    public TestSessionManager getTestSessionManager() {
        return testSessionManager;
    }

    public ExtensionManager getExtensionManager() {
        return extensionManager;
    }
}
