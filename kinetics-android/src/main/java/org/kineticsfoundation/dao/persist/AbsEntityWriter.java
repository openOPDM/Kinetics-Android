package org.kineticsfoundation.dao.persist;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.UniqueEntity;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.*;

import static android.content.ContentProviderOperation.newUpdate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;

/**
 * Generic logic container
 * Created by akaverin on 5/28/13.
 */
public abstract class AbsEntityWriter<T extends UniqueEntity> implements EntityWriter<T> {

    final ContentProviderHelper providerHelper;
    private final String entityTable;
    private final BulkUpdatePlugin<T> plugin;

    AbsEntityWriter(ContentProviderHelper providerHelper, String entityTable, BulkUpdatePlugin<T> plugin) {
        this.providerHelper = providerHelper;
        this.entityTable = entityTable;
        this.plugin = plugin;
    }

    @Override
    public int save(T entity) {
        checkNotNull(entity);
        //step #1: check if entity exists
        Cursor entityCursor = providerHelper.queryByField(entityTable, new String[]{CacheContract.Columns._ID,
                CacheContract.Columns.ID},
                CacheContract.Columns.ID, entity.getId().toString());

        if (entityCursor.moveToFirst()) {
            Integer entityId = entityCursor.getInt(0);
            entityCursor.close();

            update(entityId, entity);
            return entityId;
        } else {
            entityCursor.close();
            return insert(entity);
        }
    }

    @Override
    public void save(Collection<T> entities) {
        checkNotNull(entities);
        if (entities.isEmpty()) {
            return;
        }
        if (entities.size() == 1) {
            save(entities.iterator().next());
            return;
        }
        //step #1: build map of existing data in cache
        Map<Integer, Long> uniqueToCacheId = buildIdsMapping(entities, providerHelper, entityTable);
        //if no data exist -> just do bulk insert
        if (uniqueToCacheId.isEmpty()) {
            insert(entities);
            return;
        }
        try {
            updateAndInsert(uniqueToCacheId, entities);
        } catch (Exception e) {
            //throw unchecked to avoid signature changes
            throw new DaoException(e);
        }
    }

    /**
     * Very effective implementation -> performs bulk update with at most 1 query + insert queries
     *
     * @param uniqueToCacheId external ID to _ID in cache
     * @param entities        to be processed
     */
    private void updateAndInsert(Map<Integer, Long> uniqueToCacheId, Collection<T> entities) {
        ArrayList<ContentProviderOperation> pendingOperations = new ArrayList<ContentProviderOperation>();
        //initialize plugin

        //gather cleanup commands
        pendingOperations.addAll(plugin.getPrepareOperations(uniqueToCacheId, entities));

        List<T> entitiesToInsert = newArrayList();
        final String idSelection = BaseColumns._ID + "=?";

        for (T entity : entities) {
            Long id = uniqueToCacheId.get(entity.getId());
            //field is not present in cache yet - so it will be inserted...
            if (id == null) {
                entitiesToInsert.add(entity);
                continue;
            }
            pendingOperations.add(newUpdate(createUri(entityTable)).withExpectedCount(1).withValues(plugin
                    .entityToContentValues(entity)).withSelection(idSelection, new String[]{id
                    .toString()}).build());

            pendingOperations.addAll(plugin.getUpdateDependentOperations(entity, id));
        }
        apply(pendingOperations);
        //insert new places, should be done later as one of operations -> to drop all Providers
        if (!entitiesToInsert.isEmpty()) {
            insert(entitiesToInsert);
        }
    }

    void apply(ArrayList<ContentProviderOperation> pendingOperations) {
        try {
            providerHelper.applyOperations(pendingOperations);
        } catch (Exception e) {
            //throw unchecked to avoid signature changes
            throw new DaoException(e);
        }
    }

    /**
     * Template method pattern
     *
     * @param entity to be inserted
     * @return rowId of newly updated entity
     */
    protected abstract int insert(T entity);

    /**
     * Template method pattern
     *
     * @param entityId cache id
     * @param entity   to be updated
     */
    protected abstract void update(Integer entityId, T entity);

    /**
     * Template method pattern
     *
     * @param entities to be inserted in bulk
     */
    protected abstract void insert(Collection<T> entities);

    Map<Integer, Long> buildIdsMapping(Collection<T> entities) {
        return buildIdsMapping(entities, providerHelper, entityTable);
    }

    private static Map<Integer, Long> buildIdsMapping(Collection<? extends UniqueEntity> entities,
                                                      ContentProviderHelper providerHelper, String entityTable) {
        //build String Ids collection
        ArrayList<String> ids = Lists.newArrayListWithCapacity(entities.size());
        for (UniqueEntity entity : entities) {
            ids.add(entity.getId().toString());
        }
        Cursor idsCursor = providerHelper.queryFieldIn(entityTable, new String[]{CacheContract.Columns.ID,
                BaseColumns._ID}, CacheContract.Columns.ID, ids.toArray(new String[ids.size()]));

        //minor optimization to save memory
        if (idsCursor.getCount() == 0) {
            idsCursor.close();
            return Collections.emptyMap();
        }

        Map<Integer, Long> idToCacheIdMap = Maps.newHashMapWithExpectedSize(idsCursor.getCount());
        while (idsCursor.moveToNext()) {
            idToCacheIdMap.put(idsCursor.getInt(0), idsCursor.getLong(1));
        }
        idsCursor.close();
        return idToCacheIdMap;
    }

    /**
     * Helper plugin to perform effective bulk update
     */
    interface BulkUpdatePlugin<T> {

        Collection<ContentProviderOperation> getPrepareOperations(Map<Integer, Long> uniqueToCacheId,
                                                                  Collection<T> entities);

        ContentValues entityToContentValues(T entity);

        Collection<ContentProviderOperation> getUpdateDependentOperations(T entity, Long entityId);

    }
}
