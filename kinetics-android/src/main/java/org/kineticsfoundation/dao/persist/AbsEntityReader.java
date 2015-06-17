package org.kineticsfoundation.dao.persist;

import android.database.Cursor;
import android.provider.BaseColumns;
import org.kineticsfoundation.dao.model.UniqueEntity;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.Collection;
import java.util.Collections;

/**
 * Generic logic container
 * Created by akaverin on 5/28/13.
 */
public abstract class AbsEntityReader<T extends UniqueEntity> implements EntityReader<T> {

    final ContentProviderHelper providerHelper;
    private final String entityTable;

    AbsEntityReader(ContentProviderHelper providerHelper, String entityTable) {
        this.providerHelper = providerHelper;
        this.entityTable = entityTable;
    }

    //TODO: really need it?
    @Override
    public Collection<T> getAll(Collection<Integer> ids) {
        String[] idsArray = new String[ids.size()];
        int i = 0;
        for (Integer id : ids) {
            idsArray[i++] = id.toString();
        }
        Cursor cursor = providerHelper.queryFieldIn(entityTable, null, BaseColumns._ID, idsArray);
        if (cursor.getCount() == 0) {
            cursor.close();
            return Collections.emptyList();
        }
        Collection<T> entities = contentValuesToEntities(cursor);
        cursor.close();
        return entities;
    }

    @Override
    public Collection<T> getAllByField(String field, String value) {
        Cursor cursor = providerHelper.queryByField(entityTable, null, field, value);
        if (cursor.getCount() == 0) {
            cursor.close();
            return Collections.emptyList();
        }
        Collection<T> entities = contentValuesToEntities(cursor);
        cursor.close();
        return entities;
    }

    protected abstract Collection<T> contentValuesToEntities(Cursor cursor);

}
