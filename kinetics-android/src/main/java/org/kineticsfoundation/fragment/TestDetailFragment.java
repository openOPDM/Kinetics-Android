package org.kineticsfoundation.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.R;
import org.kineticsfoundation.adapter.WidgetsAdapter;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.task.TestDeleteTask;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.dialog.SimpleButtonDialog;
import org.kineticsfoundation.sync.synchronizers.SyncGroup;
import org.kineticsfoundation.test.TestConstants;
import org.kineticsfoundation.test.TestModel;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.kineticsfoundation.dao.CacheContract.VirtualTables;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUriById;
import static org.kineticsfoundation.fragment.FragmentUtils.showDialog;

/**
 * Fragment for any test details
 * Created by akaverin on 7/4/13.
 */
public class TestDetailFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, DialogListener {

    private KineticsApplication application;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        application = (KineticsApplication) getActivity().getApplication();
        getLoaderManager().initLoader(0, getActivity().getIntent().getExtras(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.test_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_mi:
                showDialog(this, new SimpleButtonDialog.Builder(0,
                        this).setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_test_message).setPositiveButton(android.R.string.ok)
                        .setNegativeButton(android.R.string.cancel).build());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogResult(Bundle bundle) {
        if (bundle.getBoolean(DialogConts.KEY_BUTTON_POSITIVE)) {
            Bundle args = getActivity().getIntent().getExtras();
            new TestDeleteTask(getActivity().getContentResolver(), args.getLong(CacheContract.Columns._ID))
                    .execute();
            //return, as nothing to show...
            getActivity().finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), createUriById(VirtualTables.TEST_SESSION_DETAILS,
                args.getLong(BaseColumns._ID)), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            Toast.makeText(getActivity(), R.string.test_deleted_on_server, Toast.LENGTH_LONG).show();
            getActivity().finish();
            return;
        }
        getMissingTestDetails(data);

        final String type = data.getString(data.getColumnIndex(CacheContract.Columns.TYPE));
        TestConstants.TestType testType;
        if (StringUtils.startsWithIgnoreCase(type, TestConstants.TestType.KB.name())) {
            testType = TestConstants.TestType.KB;
        } else {
            testType = TestConstants.TestType.valueOf(type);
        }
        switch (testType) {
            case TUG:
                setupAdapter(TestModel.buildTugDetails(getActivity(), data));
                break;
            case PST:
                setupAdapter(TestModel.buildPstDetails(getActivity(), data));
                break;
            case KB:
                setupAdapter(TestModel.buildKBDetails(getActivity(), data));
                break;
        }
        getActivity().setTitle(getActivity().getString(R.string.report_label, testType.name()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setListAdapter(null);
    }

    private void setupAdapter(List<Bundle> attrs) {
        WidgetsAdapter widgetsAdapter = new WidgetsAdapter(this, newArrayList(attrs));
        setListAdapter(widgetsAdapter);
    }

    private void getMissingTestDetails(Cursor data) {
        if (data.isNull(data.getColumnIndex(CacheContract.Columns.RAW_DATA))) {
            Bundle bundle = new Bundle();
            bundle.putInt(CacheContract.Columns.ID, data.getInt(data.getColumnIndex(CacheContract.Columns.ID)));
            bundle.putLong(CacheContract.Columns._ID, data.getLong(data.getColumnIndex(CacheContract.Columns._ID)));

            application.getSyncUtils().requestSyncForData(SyncGroup.TEST_DETAILS.name(), bundle, true);
        }
    }
}
