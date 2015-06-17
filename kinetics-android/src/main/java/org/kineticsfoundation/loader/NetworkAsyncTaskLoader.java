package org.kineticsfoundation.loader;

import android.app.Application;
import android.content.AsyncTaskLoader;
import com.lohika.protocol.core.response.error.ServerError;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.net.NetworkConstants;
import org.kineticsfoundation.net.api.HttpRequestException;
import org.kineticsfoundation.net.api.ProtocolRequestException;
import org.kineticsfoundation.net.server.RemoteApi;

/**
 * Base class for all {@link AsyncTaskLoader} implementations handling network calls
 * Created by akaverin on 5/29/13.
 */
public abstract class NetworkAsyncTaskLoader extends AsyncTaskLoader<AsyncLoaderResult> {

    protected final RemoteApi remoteApi;
    private AsyncLoaderResult result;

    public NetworkAsyncTaskLoader(Application application) {
        super(application);
        remoteApi = ((KineticsApplication) application).getRemoteApi();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (result != null) {
            deliverResult(result);
        } else {
            forceLoad();
        }
    }

    @Override
    public AsyncLoaderResult loadInBackground() {
        try {
            result = loadInBackgroundSafe();

        } catch (ProtocolRequestException e) {
            result = new AsyncLoaderResult(e.getError());

        } catch (HttpRequestException e) {
            result = new AsyncLoaderResult(new ServerError(NetworkConstants.NETWORK_ERROR,
                    e.getHttpStatus().getReasonPhrase()));
        }
        return result;
    }

    /**
     * Template method pattern
     *
     * @return result of operation
     */
    protected abstract AsyncLoaderResult loadInBackgroundSafe();
}
