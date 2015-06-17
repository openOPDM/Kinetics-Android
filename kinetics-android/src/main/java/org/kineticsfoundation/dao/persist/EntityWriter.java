package org.kineticsfoundation.dao.persist;

import java.util.Collection;

/**
 * Abstraction for Entity persisting from POJO to {@link android.content.ContentProvider}
 * Created by akaverin on 5/28/13.
 */
public interface EntityWriter<T> {

    int save(T entity);

    void save(Collection<T> entity);

}
