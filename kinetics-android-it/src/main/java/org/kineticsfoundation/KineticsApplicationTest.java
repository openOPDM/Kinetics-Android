package org.kineticsfoundation;

import android.test.ApplicationTestCase;
import android.test.RenamingDelegatingContext;
import org.kineticsfoundation.test.TestConsts;

/**
 * Basic App tests Created by akaverin on 5/27/13.
 */
public class KineticsApplicationTest extends
        ApplicationTestCase<KineticsApplication> {

    public KineticsApplicationTest() {
        super(KineticsApplication.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        setContext(new RenamingDelegatingContext(getContext(),
                TestConsts.PREFIX));
        createApplication();
    }

    public void testGetRemoteApi() {
        assertNotNull(getApplication().getRemoteApi());
    }

    public void testGetMasterDAO() {
        assertNotNull(getApplication().getMasterDAO());
    }

    public void testGetAccountManager() {
        assertNotNull(getApplication().getAccountManager());
    }

    public void testGetSyncUtils() {
        assertNotNull(getApplication().getSyncUtils());
    }

}
