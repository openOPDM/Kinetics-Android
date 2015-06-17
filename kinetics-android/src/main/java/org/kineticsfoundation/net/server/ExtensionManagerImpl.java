package org.kineticsfoundation.net.server;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.processor.ResponseDataParser;
import com.lohika.restclient.RestProcessor;
import com.lohika.sync.account.AccountManager;
import org.codehaus.jackson.type.TypeReference;
import org.kineticsfoundation.dao.model.extension.ExtendedEntity;
import org.kineticsfoundation.dao.model.extension.ExtensionMetaData;
import org.kineticsfoundation.net.api.ExtensionManager;

import java.util.List;

/**
 * API implementation
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/24/13
 * Time: 9:28 AM
 */
public class ExtensionManagerImpl extends AbsManager implements ExtensionManager {

    private static final String MANAGER = "ExtensionManager";
    private static final TypeReference<List<ExtensionMetaData>> dataType = new TypeReference<List<ExtensionMetaData>>
            () {
    };

    public ExtensionManagerImpl(RestProcessor restProcessor, AccountManager accountManager) {
        super(restProcessor, MANAGER, accountManager);
    }

    public List<ExtensionMetaData> getExtensionsByEntity(String sessionToken, ExtendedEntity entity) {
        RequestBuilder builder = newTokenBuilder("getExtensionsByEntity", sessionToken)
                .addArg("entity", entity.name());

        return ResponseDataParser.extractData(executeRequest(builder), "extension", dataType);
    }
}
