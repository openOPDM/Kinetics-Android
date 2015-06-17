package org.kineticsfoundation.loader;

import com.lohika.protocol.core.response.error.ServerError;


/**
 * Generic {@link NetworkAsyncTaskLoader} implementations operations result
 * Created by akaverin on 5/29/13.
 */
public class AsyncLoaderResult {

    public static final AsyncLoaderResult EMPTY = new AsyncLoaderResult();
    private Object result;
    private ServerError error;

    public AsyncLoaderResult(Object result) {
        this.result = result;
    }

    public AsyncLoaderResult(ServerError error) {
        this.error = error;
    }

    private AsyncLoaderResult() {
    }

    public Object getResult() {
        return result;
    }

    public ServerError getError() {
        return error;
    }

    @Override
    public String toString() {
        return "AsyncLoaderResult{" + "result=" + result + ", error=" + error + '}';
    }
}
