package org.kineticsfoundation.dao.persist;

import android.content.ContentResolver;
import android.content.Context;
import org.kineticsfoundation.dao.model.TestSession;
import org.kineticsfoundation.dao.model.User;
import org.kineticsfoundation.dao.model.extension.ExtensionMetaData;
import org.kineticsfoundation.dao.provider.ContentProviderHelper;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Access point to the {@link EntityReader} and {@link EntityWriter} implementations
 * Created by akaverin on 5/27/13.
 */
public class MasterDAO {

    private final Map<Class<?>, EntityWriter> writerMap;
    private final Map<Class<?>, EntityReader> readerMap;
    private final Context context;

    public MasterDAO(Context context) {
        this.context = context;
        ContentProviderHelper providerHelper = new ContentProviderHelper(context.getContentResolver());

        writerMap = newHashMap();
        writerMap.put(User.class, new UserWriter(providerHelper));
        writerMap.put(TestSession.class, new TestSessionWriter(providerHelper));
        writerMap.put(ExtensionMetaData.class, new ExtensionMetaDataWriter(providerHelper));

        readerMap = newHashMap();
        readerMap.put(User.class, new UserReader(providerHelper));
        readerMap.put(TestSession.class, new TestSessionReader(providerHelper));
    }

    /**
     * Find {@link EntityWriter} implementation by Entity type
     *
     * @param clazz to query writer for
     * @param <T>   entity type
     * @return
     */
    public <T> EntityWriter<T> getWriter(Class<T> clazz) {
        //noinspection unchecked
        return writerMap.get(clazz);
    }

    /**
     * Find {@link EntityReader} implementation by Entity type
     *
     * @param clazz to query reader for
     * @param <T>   entity type
     * @return
     */
    public <T> EntityReader<T> getReader(Class<T> clazz) {
        //noinspection unchecked
        return readerMap.get(clazz);
    }

    /**
     * @return current content resolver
     */
    public ContentResolver getContentResolver() {
        return context.getContentResolver();
    }

}
