package org.kineticsfoundation.fragment;

import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Handler;
import android.os.Message;
import com.lohika.protocol.core.response.error.ServerError;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dialog.SimpleButtonDialog;
import org.kineticsfoundation.dialog.SimpleProgressDialog;
import org.kineticsfoundation.loader.AsyncLoaderResult;

import static android.R.string.ok;

/**
 * Fragment supporting network operations
 * Created by akaverin on 6/3/13.
 */
abstract class AbsNetworkFragment extends AbsFragment implements LoaderManager
        .LoaderCallbacks<AsyncLoaderResult> {

    /**
     * Workaround to handle issues with closing dialogs from
     * {@link android.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.content.Loader, Object)}
     */
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            onLoadError(msg.what, (ServerError) msg.obj);
            return true;
        }
    });

    void showDialog(DialogFragment dialog) {
        FragmentUtils.showDialog(this, dialog);
    }

    void showDialogSkip(DialogFragment dialog) {
        FragmentUtils.showDialog(this, dialog, true);
    }

    void dismissProgressDialog() {
        DialogFragment fragment = (DialogFragment) getFragmentManager().findFragmentByTag(SimpleProgressDialog
                .class.getSimpleName());
        if (fragment != null) {
            fragment.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onLoadFinished(Loader<AsyncLoaderResult> loader, AsyncLoaderResult data) {
        dismissProgressDialog();
        if (data.getError() != null) {
            handleNetworkError(loader.getId(), data.getError());
            return;
        }
        onLoadFinishedNoError(loader, data);
    }

    @Override
    public void onLoaderReset(Loader<AsyncLoaderResult> loader) {
    }

    /**
     * Template method pattern. All error handling done before this call.
     *
     * @param loader which finished the job
     * @param data   obtained by loader
     */
    protected abstract void onLoadFinishedNoError(Loader<AsyncLoaderResult> loader, AsyncLoaderResult data);

    /**
     * Default {@link ServerError} handler. Override for specific processing and call this version for default behavior.
     *
     * @param id    loader id
     * @param error ServerError returned
     */
    void onLoadError(int id, ServerError error) {
        showDialog(new SimpleButtonDialog.Builder().setTitle(R.string.error_title).setMessage(error.getDescription())
                .setPositiveButton(ok).build());
    }

    void handleNetworkError(int id, ServerError error) {
        Message.obtain(handler, id, error).sendToTarget();
    }
}
