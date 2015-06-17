package org.kineticsfoundation.dao.persist;

import android.database.Cursor;
import com.google.common.collect.Lists;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.Gender;
import org.kineticsfoundation.dao.model.User;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User POJO reader
 * Created by akaverin on 5/28/13.
 */
public class UserReader extends AbsEntityReader<User> {

    public UserReader(ContentProviderHelper providerHelper) {
        super(providerHelper, CacheContract.Tables.USER);
    }

    @Override
    protected Collection<User> contentValuesToEntities(Cursor cursor) {
        List<User> users = Lists.newArrayListWithCapacity(cursor.getCount());
        while (cursor.moveToNext()) {
            User newUser = new User();
            newUser.setId(cursor.getInt(cursor.getColumnIndex(CacheContract.Columns.ID)));
            newUser.setEmail(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.EMAIL)));
            newUser.setFirstName(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.FIRST_NAME)));
            newUser.setSecondName(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.SECOND_NAME)));
            newUser.setUID(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.UID)));
            newUser.setBirthday(new Date(cursor.getLong(cursor.getColumnIndex(CacheContract.Columns.BIRTHDAY))));
            newUser.setGender(Gender.valueOf(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.GENDER))));

            users.add(newUser);
        }
        return users;
    }
}
