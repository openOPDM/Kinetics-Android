package org.kineticsfoundation.dao.persist;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import com.google.common.collect.Lists;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.User;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.kineticsfoundation.dao.CacheContract.Tables.USER;

/**
 * User entity writer
 * Created by akaverin on 5/28/13.
 */
public class UserWriter extends AbsEntityWriter<User> {

    private static final BulkUpdatePlugin<User> UPDATE_PLUGIN = new BulkUpdatePlugin<User>() {
        @Override
        public Collection<ContentProviderOperation> getPrepareOperations(Map<Integer, Long> uniqueToCacheId,
                                                                         Collection<User> entities) {
            return Collections.emptyList();
        }

        @Override
        public ContentValues entityToContentValues(User user) {
            return mapUserToContentValues(user);
        }

        @Override
        public Collection<ContentProviderOperation> getUpdateDependentOperations(User entity, Long entityId) {
            return Collections.emptyList();
        }
    };

    UserWriter(ContentProviderHelper providerHelper) {
        super(providerHelper, USER, UPDATE_PLUGIN);
    }

    @Override
    protected int insert(User user) {
        return providerHelper.insert(USER, mapUserToContentValues(user), false);
    }

    @Override
    protected void update(Integer entityId, User user) {
        providerHelper.update(USER, entityId, mapUserToContentValues(user), false);
    }

    @Override
    protected void insert(Collection<User> users) {
        List<ContentValues> valuesList = Lists.newArrayListWithCapacity(users.size());
        for (User user : users) {
            valuesList.add(mapUserToContentValues(user));
        }
        providerHelper.insert(USER, valuesList.toArray(new ContentValues[valuesList.size()]), false);
    }

    private static ContentValues mapUserToContentValues(User user) {
        ContentValues values = new ContentValues();

        values.put(CacheContract.Columns.ID, user.getId());
        values.put(CacheContract.Columns.EMAIL, user.getEmail());
        values.put(CacheContract.Columns.FIRST_NAME, user.getFirstName());
        values.put(CacheContract.Columns.SECOND_NAME, user.getSecondName());
        values.put(CacheContract.Columns.UID, user.getUID());
        values.put(CacheContract.Columns.BIRTHDAY, user.getBirthday().getTime());
        values.put(CacheContract.Columns.GENDER, user.getGender().name());

        return values;
    }

}
