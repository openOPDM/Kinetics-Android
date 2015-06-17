package org.kineticsfoundation.dao.persist;

import java.util.Collection;

/**
 * Abstraction for Entity building POJO from {@link android.content.ContentProvider} storage
 * Created by akaverin on 5/28/13.
 */
public interface EntityReader<T> {

    /**
     * Load entities by DB primary keys
     *
     * @param ids keys collection
     * @return collection of POJO objects
     */
    Collection<T> getAll(Collection<Integer> ids);

    /**
     * Get entities by specific criteria
     *
     * @param field to be filtered by
     * @param value to be used as filter
     * @return collection of POJO objects
     */
    Collection<T> getAllByField(String field, String value);

}
