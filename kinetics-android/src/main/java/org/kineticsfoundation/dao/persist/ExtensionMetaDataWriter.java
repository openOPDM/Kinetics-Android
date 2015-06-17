package org.kineticsfoundation.dao.persist;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import com.google.common.base.Functions;
import org.kineticsfoundation.dao.model.extension.ExtensionMetaData;
import org.kineticsfoundation.dao.model.extension.ExtensionProperty;
import org.kineticsfoundation.dao.model.extension.ExtensionType;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static android.content.ContentProviderOperation.*;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.kineticsfoundation.dao.CacheContract.Columns;
import static org.kineticsfoundation.dao.CacheContract.Tables.EXT_LIST_NODE;
import static org.kineticsfoundation.dao.CacheContract.Tables.EXT_METADATA;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.buildInClause;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;

/**
 * POJO to ContentProvider mapper
 * Created by akaverin on 5/28/13.
 */
public class ExtensionMetaDataWriter extends AbsEntityWriter<ExtensionMetaData> {

    private static final BulkUpdatePlugin<ExtensionMetaData> UPDATE_PLUGIN = new BulkUpdatePlugin<ExtensionMetaData>() {
        @Override
        public Collection<ContentProviderOperation> getPrepareOperations(Map<Integer, Long> uniqueToCacheId,
                                                                         Collection<ExtensionMetaData> entities) {
            return newArrayList(newDelete(createUri(EXT_LIST_NODE))
                    .withSelection(buildInClause(Columns.EXT_METADATA_ID, uniqueToCacheId.size(), false),
                            transform(uniqueToCacheId.values(), Functions.toStringFunction()).toArray(new
                                    String[uniqueToCacheId.size()])).build());
        }

        @Override
        public ContentValues entityToContentValues(ExtensionMetaData entity) {
            return mapMetaDataToContentValues(entity);
        }

        @Override
        public Collection<ContentProviderOperation> getUpdateDependentOperations(ExtensionMetaData entity,
                                                                                 Long entityId) {
            return buildListNodeInsertOperations(entityId, entity);
        }
    };

    ExtensionMetaDataWriter(ContentProviderHelper providerHelper) {
        super(providerHelper, EXT_METADATA, UPDATE_PLUGIN);
    }

    @Override
    protected int insert(ExtensionMetaData metaData) {
        long rowId = providerHelper.insert(EXT_METADATA, mapMetaDataToContentValues(metaData), false);

        apply(buildListNodeInsertOperations(rowId, metaData));

        return (int) rowId;
    }

    @Override
    protected void update(Integer entityId, ExtensionMetaData metaData) {
        ArrayList<ContentProviderOperation> operations = newArrayList();

        String[] selectionArgs = {entityId.toString()};

        operations.add(newUpdate(createUri(EXT_METADATA)).withSelection(Columns._ID + "=?",
                selectionArgs).withValues(mapMetaDataToContentValues(metaData)).withExpectedCount(1).build());

        operations.add(buildListNodeCleanOperations(entityId));

        operations.addAll(buildListNodeInsertOperations(entityId, metaData));

        apply(operations);
    }

    @Override
    protected void insert(Collection<ExtensionMetaData> metaDataList) {
        List<ContentValues> metaDataValues = newArrayListWithCapacity(metaDataList.size());
        for (ExtensionMetaData metaData : metaDataList) {
            metaDataValues.add(mapMetaDataToContentValues(metaData));
        }
        providerHelper.insert(EXT_METADATA, metaDataValues.toArray(new ContentValues[metaDataValues.size()]), false);

        Map<Integer, Long> metaDataIdsMap = buildIdsMapping(metaDataList);

        ArrayList<ContentProviderOperation> pendingOperations = newArrayList();
        for (ExtensionMetaData metaData : metaDataList) {
            pendingOperations.addAll(buildListNodeInsertOperations(metaDataIdsMap.get(metaData.getId()), metaData));
        }
        apply(pendingOperations);
    }

    private static ContentValues mapMetaDataToContentValues(ExtensionMetaData metaData) {
        ContentValues values = new ContentValues();

        values.put(Columns.ID, metaData.getId());
        values.put(Columns.NAME, metaData.getName());
        values.put(Columns.TYPE, metaData.getType().name());

        //handle properties
        int props = 0;
        for (ExtensionProperty property : metaData.getProperties()) {
            props |= property.getMask();
        }
        values.put(Columns.PROPS, props);

        return values;
    }

    private static ContentValues mapExtNodeListToContentValues(long metaDataId, String label) {
        ContentValues values = new ContentValues();

        values.put(Columns.LABEL, label);
        values.put(Columns.EXT_METADATA_ID, metaDataId);

        return values;
    }

    private static ArrayList<ContentProviderOperation> buildListNodeInsertOperations(long metaDataId,
                                                                                     ExtensionMetaData metaData) {
        ArrayList<ContentProviderOperation> operations = newArrayList();
        if (metaData.getType() != ExtensionType.LIST) {
            return operations;
        }
        for (String label : metaData.getList()) {
            operations.add(newInsert(createUri(EXT_LIST_NODE)).withValues(mapExtNodeListToContentValues(metaDataId,
                    label)).build());
        }
        return operations;
    }

    private static ContentProviderOperation buildListNodeCleanOperations(long metaDataId) {
        return newDelete(createUri(EXT_LIST_NODE)).withSelection(Columns.EXT_METADATA_ID + "=?",
                new String[]{Long.toString(metaDataId)}).build();
    }

}
