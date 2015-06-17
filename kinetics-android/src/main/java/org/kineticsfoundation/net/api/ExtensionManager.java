package org.kineticsfoundation.net.api;

import org.kineticsfoundation.dao.model.extension.ExtendedEntity;
import org.kineticsfoundation.dao.model.extension.ExtensionMetaData;

import java.util.List;

/**
 * Extension Manager APIs
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:25 PM
 */
public interface ExtensionManager {

    List<ExtensionMetaData> getExtensionsByEntity(String sessionToken, ExtendedEntity entity);

}
