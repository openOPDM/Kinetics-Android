package org.kineticsfoundation.fragment;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.google.common.primitives.Longs;
import org.kineticsfoundation.R;
import org.kineticsfoundation.activity.TestDetailActivity;
import org.kineticsfoundation.activity.TestRunActivity;
import org.kineticsfoundation.activity.VideoActivity;
import org.kineticsfoundation.adapter.TestListCursorAdapter;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.task.TestDeleteTask;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.dialog.SimpleButtonDialog;
import org.kineticsfoundation.test.TestConstants;

import java.util.List;

import static org.kineticsfoundation.dao.CacheContract.Columns;
import static org.kineticsfoundation.dao.CacheContract.Tables.TEST_SESSION;
import static org.kineticsfoundation.dao.provider.ContentProviderHelper.createUri;
import static org.kineticsfoundation.fragment.FragmentUtils.showDialog;

/**
 * Our main fragment
 * Created by akaverin on 6/5/13.
 */
public class TestListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, DialogListener {

    private static final String KEY_IDS = "KEY_IDS";
    private final View.OnClickListener TUG_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case android.R.id.button1:
                    startActivity(new Intent(getActivity(), TestRunActivity.class).putExtra(Columns.TYPE,
                            TestConstants.TestType.TUG));
                    break;

                case android.R.id.button2:
                    startActivity(new Intent(getActivity(), VideoActivity.class).putExtra(Columns.TYPE,
                            TestConstants.TestType.TUG));
                    break;
            }
        }
    };
    private final View.OnClickListener PS_LISTENER = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case android.R.id.button1:
                    startActivity(new Intent(getActivity(), TestRunActivity.class).putExtra(Columns.TYPE,
                            TestConstants.TestType.PST));
                    break;

                case android.R.id.button2:
                    startActivity(new Intent(getActivity(), VideoActivity.class).putExtra(Columns.TYPE,
                            TestConstants.TestType.PST));
                    break;
            }
        }
    };
    private final MultiChoiceHandler CHOICE_HANDLER = new MultiChoiceHandler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_list_fragment, container, false);

        setupButton(view.findViewById(R.id.tug_test), R.string.test_tug_label, TUG_LISTENER);
        setupButton(view.findViewById(R.id.ps_test), R.string.test_ps_label, PS_LISTENER);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView listView = getListView();
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(CHOICE_HANDLER);
        setListAdapter(new TestListCursorAdapter(getActivity(), null));

        setEmptyText(getString(R.string.no_tests_label));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDialogResult(Bundle bundle) {
        if (bundle.getBoolean(DialogConts.KEY_BUTTON_POSITIVE)) {
            List<Long> ids = Longs.asList(bundle.getBundle(DialogConts.KEY_EXTRA).getLongArray(KEY_IDS));
            new TestDeleteTask(getActivity().getContentResolver(), ids).execute();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), createUri(TEST_SESSION), new String[]{BaseColumns._ID, Columns.VALID,
                Columns.CREATION, Columns.TYPE, Columns.SCORE, Columns.SYNC}, Columns.SYNC + "!=?",
                new String[]{Integer.toString(CacheContract.Sync.DELETED.ordinal())}, Columns.CREATION + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((TestListCursorAdapter) getListAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((TestListCursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(getActivity(), TestDetailActivity.class).putExtra(Columns._ID, id));
    }

    private void setupButton(View testContainer, int resId, View.OnClickListener onClickListener) {
        Button button = (Button) testContainer.findViewById(android.R.id.button1);
        button.setText(getString(resId));
        button.setOnClickListener(onClickListener);

        testContainer.findViewById(android.R.id.button2).setOnClickListener(onClickListener);
    }

    private final class MultiChoiceHandler implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = getListView().getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mode.setSubtitle(R.string.action_one_select_label);
                    break;
                default:
                    mode.setSubtitle(getString(R.string.action_multiple_select_label, checkedCount));
                    break;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.test_details, menu);
            mode.setTitle(R.string.action_label);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_mi:
                    Bundle extra = new Bundle();
                    extra.putLongArray(KEY_IDS, getListView().getCheckedItemIds());

                    showDialog(TestListFragment.this, new SimpleButtonDialog.Builder(0,
                            TestListFragment.this).setTitle(R.string.dialog_delete_title)
                            .setMessage(R.string.dialog_delete_test_message).setPositiveButton(android.R.string.ok)
                            .setNegativeButton(android.R.string.cancel).setExtra(extra).build());

                    mode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }
}
