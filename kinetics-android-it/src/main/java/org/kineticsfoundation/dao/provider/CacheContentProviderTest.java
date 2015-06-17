package org.kineticsfoundation.dao.provider;

import android.content.ContentProvider;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.dao.model.User;
import org.kineticsfoundation.dao.model.extension.ExtensionMetaData;
import org.kineticsfoundation.dao.model.extension.ExtensionProperty;
import org.kineticsfoundation.dao.model.extension.ExtensionType;
import org.kineticsfoundation.dao.persist.EntityReader;
import org.kineticsfoundation.dao.persist.EntityWriter;
import org.kineticsfoundation.dao.persist.MasterDAO;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.kineticsfoundation.dao.CacheContract.Tables.*;
import static org.kineticsfoundation.dao.CacheContract.VirtualTables.TEST_SESSION_DETAILS;
import static org.kineticsfoundation.dao.DaoUtils.*;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUriById;

/**
 * DAO layer test
 * Created by akaverin on 5/27/13.
 */
public class CacheContentProviderTest extends AndroidTestCase {

    private ContentProvider provider;
    private MasterDAO masterDAO;

    @Override
    public void setUp() throws Exception {
        //Copied from android.test.ProviderTestCase2
        MockContentResolver contentResolver = new MockContentResolver();

        final String filenamePrefix = "test.";
        RenamingDelegatingContext targetContextWrapper = new RenamingDelegatingContext(new MockContext2(getContext())
                , getContext(), filenamePrefix);

        Context context = new IsolatedContext(contentResolver, targetContextWrapper);

        provider = CacheContentProvider.class.newInstance();
        provider.attachInfo(context, null);
        assertNotNull(provider);
        contentResolver.addProvider(CacheContentProvider.AUTHORITY, provider);

        masterDAO = new MasterDAO(context);
    }

    public void testEmptyTables() {
        String[] tables = new String[]{USER, TEST_SESSION, EXT_DATA, EXT_METADATA, EXT_LIST_NODE};
        for (String table : tables) {
            assertEquals(0, query(table).getCount());
        }
    }

    public void testUserCRUD() {
        User generatedUser = generateUser();

        EntityWriter<User> userWriter = masterDAO.getWriter(User.class);
        //insert
        userWriter.save(generatedUser);
        assertEquals(1, query(USER).getCount());

        //save logic
        generatedUser.setUID(makeUniqueId());
        userWriter.save(generatedUser);

        assertEquals(1, query(USER).getCount());

        //insert new
        generatedUser.setId((int) System.currentTimeMillis());
        final Integer newId = userWriter.save(generatedUser);
        assertEquals(2, query(USER).getCount());

        EntityReader<User> userReader = masterDAO.getReader(User.class);
        //read back
        Collection<User> users = userReader.getAll(Arrays.asList(newId));
        assertEquals(generatedUser, users.iterator().next());

        //delete
        deleteById(USER, generatedUser.getId());
        assertEquals(1, query(USER).getCount());

        provider.delete(createUri(USER), null, null);
        assertEquals(0, query(USER).getCount());
    }

    public void testTestSessionCRUD() {

        User user = generateUser();
        masterDAO.getWriter(User.class).save(user);

        //map to external user id
        List<TestSession> testSessions = generateTestSessions(5);

        EntityWriter<TestSession> testSessionWriter = masterDAO.getWriter(TestSession.class);

        //bulk insert
        testSessionWriter.save(testSessions);
        assertEquals(5, query(TEST_SESSION).getCount());
        assertEquals(6, query(EXT_DATA).getCount());

        //bulk update
        testSessionWriter.save(testSessions);
        assertEquals(5, query(TEST_SESSION).getCount());
        assertEquals(6, query(EXT_DATA).getCount());

        TestSession testSession = testSessions.get(0);

        //save single
        testSession.setRawData(makeUniqueId());
        testSessionWriter.save(testSession);
        assertEquals(5, query(TEST_SESSION).getCount());

        //insert single item logic
        testSession.setId(generateInt());
        testSessionWriter.save(testSessions);
        assertEquals(6, query(TEST_SESSION).getCount());
        assertEquals(8, query(EXT_DATA).getCount());

        //read
        TestSession loadedSession = masterDAO.getReader(TestSession.class).getAllByField(CacheContract.Columns.ID,
                testSession.getId().toString()).iterator().next();
        assertEquals(testSession, loadedSession);
        assertNotNull(loadedSession.getExtension());

        //delete
        provider.delete(createUri(TEST_SESSION), null, null);
        assertEquals(0, query(TEST_SESSION).getCount());
        assertEquals(0, query(EXT_DATA).getCount());
    }

    public void testExtensionMetaDataCRUD() {
        EntityWriter<ExtensionMetaData> metaDataWriter = masterDAO.getWriter(ExtensionMetaData.class);

        List<ExtensionMetaData> metaDataList = generateMetaData(5);
        //bulk insert
        metaDataWriter.save(metaDataList);
        assertEquals(5, query(EXT_METADATA).getCount());
        assertEquals(6, query(EXT_LIST_NODE).getCount());

        //save single
        metaDataWriter.save(metaDataList.get(0));

        //save bulk
        metaDataWriter.save(metaDataList);
        assertEquals(5, query(EXT_METADATA).getCount());
        assertEquals(6, query(EXT_LIST_NODE).getCount());

        //insert & update mix
        metaDataList.get(0).setId(generateInt());
        metaDataList.get(0).setName(makeUniqueId());
        metaDataWriter.save(metaDataList);
        assertEquals(6, query(EXT_METADATA).getCount());

        //check props:
        Cursor cursor = query(EXT_METADATA);
        cursor.moveToFirst();

        int props = cursor.getInt(cursor.getColumnIndex(CacheContract.Columns.PROPS));
        assertEquals(3, props);
        assertTrue(ExtensionProperty.isRequired(props));

        //delete
        provider.delete(createUri(EXT_METADATA), null, null);
        assertEquals(0, query(EXT_METADATA).getCount());
        assertEquals(0, query(EXT_LIST_NODE).getCount());
    }

    public void testExtensionDataCombined() {
        EntityWriter<ExtensionMetaData> metaDataWriter = masterDAO.getWriter(ExtensionMetaData.class);

        List<ExtensionMetaData> metaDataList = generateMetaData(2);
        //insert
        metaDataWriter.save(metaDataList);

        Cursor cursor = query(CacheContract.VirtualTables.EXT_METADATA_COMBINED);
        cursor.moveToFirst();

        assertEquals(ExtensionType.LIST.name(), cursor.getString(cursor.getColumnIndex(CacheContract.Columns.TYPE)));
        int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        cursor.moveToNext();
        assertEquals(ExtensionType.LIST.name(), cursor.getString(cursor.getColumnIndex(CacheContract.Columns.TYPE)));
        assertEquals(id, cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)));

        assertEquals(3, cursor.getCount());
    }

    public void testTestSessionDetail() {
        List<TestSession> testSessions = generateTestSessions(5);

        EntityWriter<TestSession> testSessionWriter = masterDAO.getWriter(TestSession.class);
        testSessionWriter.save(testSessions);

        Cursor cursor = query(TEST_SESSION);
        cursor.moveToNext();
        Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

        cursor = provider.query(createUriById(TEST_SESSION_DETAILS, id), null, null, null, null);
        assertEquals(3, cursor.getCount());

        cursor.moveToNext();
        assertNotNull(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.TYPE)));
        assertNotNull(cursor.getString(cursor.getColumnIndex(CacheContract.Columns.RAW_DATA)));
        cursor.moveToNext();
        assertEquals(id.longValue(), cursor.getLong(cursor.getColumnIndex(CacheContract.Columns.TEST_SESSION_ID)));
        cursor.moveToNext();
        assertEquals(id.longValue(), cursor.getLong(cursor.getColumnIndex(CacheContract.Columns.TEST_SESSION_ID)));
    }

    private Cursor query(String table) {
        return provider.query(createUri(table), null, null, null, null);
    }

    private void deleteById(String table, Integer id) {
        provider.delete(createUri(table), CacheContract.Columns.ID + "=?", new String[]{id.toString()});
    }

    private static class MockContext2 extends MockContext {

        private final Context delegate;

        public MockContext2(Context context) {
            delegate = context;
        }

        @Override
        public String getPackageName() {
            return delegate.getPackageName();
        }

        @Override
        public AssetManager getAssets() {
            return delegate.getAssets();
        }
    }
}
