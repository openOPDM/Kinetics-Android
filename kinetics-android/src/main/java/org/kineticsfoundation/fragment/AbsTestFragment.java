package org.kineticsfoundation.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import org.kineticsfoundation.R;
import org.kineticsfoundation.adapter.WidgetsAdapter;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.layout.LockInputLayout;
import org.kineticsfoundation.test.TestModel;
import org.kineticsfoundation.widget.AbsWidget;
import org.kineticsfoundation.widget.WidgetProperty;

import static com.google.common.collect.Lists.newArrayList;
import static org.kineticsfoundation.dao.CacheContract.VirtualTables;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;
import static org.kineticsfoundation.widget.WidgetProperty.VALID;
import static org.kineticsfoundation.widget.WidgetProperty.VALUE;

/**
 * Base fragment for Test Detailed view
 * Created by akaverin on 6/7/13.
 */
public abstract class AbsTestFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        DialogListener, LockInputLayout.UnlockListener {

    private static final String KEY_RUN_STATE = "KEY_RUN_STATE";
    private static final String KEY_MODEL = "KEY_MODEL";
    WidgetsAdapter widgetsAdapter;
    TestModel testModel;
    RunState runState;
    MediaPlayer mediaPlayer;
    final Handler handler = new Handler();
    org.kineticsfoundation.test.sensor.Vibrator vibrator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getActivity().getActionBar().setSubtitle(R.string.run_test_label);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        vibrator = new org.kineticsfoundation.test.sensor.Vibrator(getActivity().getApplicationContext());
        if (savedInstanceState != null) {
            testModel = savedInstanceState.getParcelable(KEY_MODEL);
            runState = (RunState) savedInstanceState.getSerializable(KEY_RUN_STATE);
            buildListAdapter();
        } else {
            getLoaderManager().initLoader(0, null, this);
            runState = RunState.OFF;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (runState != RunState.FINISH) {
            getActivity().finish();
            runState = RunState.OFF;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((AbsWidget) v).onClick();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.test_run, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sumbit_mi:
                if (areRequiredFieldsOk()) {
                    Toast.makeText(getActivity(), R.string.toast_test_save_label, Toast.LENGTH_SHORT).show();
                    onPersistTest();
                    getActivity().finish();
                    return true;
                } else {
                    widgetsAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), R.string.toast_required_fields_label, Toast.LENGTH_SHORT).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Template method pattern
     */
    protected abstract void onPersistTest();

    /**
     * Template method pattern
     */
    protected abstract TestModel buildModel(Cursor data);

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), createUri(VirtualTables.EXT_METADATA_COMBINED), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        testModel = buildModel(data);
        buildListAdapter();

        //we need to avoid Test updates if sync happened in background
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setListAdapter(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_RUN_STATE, runState);
        outState.putParcelable(KEY_MODEL, testModel);
    }

    @Override
    public void onDialogResult(Bundle bundle) {
        String widgetKey = bundle.getBundle(DialogConts.KEY_EXTRA).getString(DialogConts.KEY_ID);
        if (testModel.getWidgetAttrs().containsKey(widgetKey)) {
            updateWidgetValue(bundle, widgetKey);
        } else {
            //looks like NOTE widget
            updateWidgetValue(bundle, TestModel.Attr.NOTE.name());
        }
        widgetsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUnlock() {
        getActivity().getActionBar().show();
    }

    private void updateWidgetValue(Bundle bundle, String widgetKey) {
        Bundle widgetBundle = testModel.getWidgetAttrs().get(widgetKey);
        widgetBundle.putString(VALUE.name(), bundle.getString(DialogConts.KEY_VALUE));
        widgetBundle.putBoolean(VALID.name(), true);
    }

    void activateLockAndHoldScreen() {
        getActivity().getActionBar().hide();

        LockInputLayout layout = (LockInputLayout) getActivity().getLayoutInflater().inflate(R.layout
                .lock_view, null);
        layout.setListener(this);

        getActivity().addContentView(layout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void releaseScreen() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private boolean areRequiredFieldsOk() {
        boolean isOk = true;
        for (Bundle bundle : testModel.getWidgetAttrs().values()) {
            if (bundle.getBoolean(WidgetProperty.REQUIRED.name(), false) && TextUtils.isEmpty(bundle.getString
                    (WidgetProperty.VALUE.name()))) {
                bundle.putBoolean(WidgetProperty.VALID.name(), false);
                isOk = false;
            }
        }
        return isOk;
    }

    private void buildListAdapter() {
        widgetsAdapter = new WidgetsAdapter(this, newArrayList(testModel.getWidgetAttrs().values()));
        setListAdapter(widgetsAdapter);
    }

    void updateTestStatusLabel(int resId) {
        testModel.getWidgetAttrs().get(TestModel.Attr.STAGE.name()).putString(VALUE.name(),
                getString(resId));
        widgetsAdapter.notifyDataSetChanged();
    }

    protected enum RunState {
        OFF, START, RUN, FINISH
    }

}
