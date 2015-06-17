package org.kineticsfoundation.sync.synchronizers;

import android.content.SyncResult;
import android.os.Bundle;
import com.lohika.sync.account.AccountManager;
import org.kineticsfoundation.dao.model.extension.ExtendedEntity;
import org.kineticsfoundation.dao.model.extension.ExtensionMetaData;
import org.kineticsfoundation.dao.persist.EntityWriter;
import org.kineticsfoundation.dao.persist.MasterDAO;
import org.kineticsfoundation.net.api.ExtensionManager;

import java.util.List;

import static org.kineticsfoundation.dao.CacheContract.Tables.EXT_METADATA;

/**
 * Performs sync of {@link org.kineticsfoundation.dao.model.extension.ExtensionMetaData} entities
 * Created by akaverin on 6/3/13.
 */
public class MetadataSynchronizer extends AbsSynchronizer {

    private final MasterDAO masterDAO;
    private final ExtensionManager extensionManager;

    public MetadataSynchronizer(AccountManager accountManager, MasterDAO masterDAO, ExtensionManager extensionManager) {
        super(accountManager);
        this.masterDAO = masterDAO;
        this.extensionManager = extensionManager;
    }

    @Override
    protected void postLocalChangesSafe(SyncResult result, Bundle extraData) {
        //noop - we only persist metadata
    }

    @Override
    protected void getRemoteChangesSafe(SyncResult result, Bundle extraData) {
        List<ExtensionMetaData> metaDataList = extensionManager.getExtensionsByEntity(getSessionToken(),
                ExtendedEntity.TEST_SESSION);
        if (metaDataList.isEmpty()) {
            deleteAll(masterDAO.getContentResolver(), EXT_METADATA);
            return;
        }
        EntityWriter<ExtensionMetaData> entityWriter = masterDAO.getWriter(ExtensionMetaData.class);
        //persist all
        entityWriter.save(metaDataList);

        cleanup(masterDAO.getContentResolver(), EXT_METADATA, metaDataList);
    }

}
