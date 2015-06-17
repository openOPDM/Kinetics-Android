package org.kineticsfoundation.fragment;

import android.accounts.Account;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.R;
import org.kineticsfoundation.account.Authenticator;
import org.kineticsfoundation.activity.CreateAccountActivity;
import org.kineticsfoundation.activity.LoginActivity;
import org.kineticsfoundation.activity.TestListActivity;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.dao.model.Project;
import org.kineticsfoundation.dao.task.CleanDbTask;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.dialog.SingleCheckListButtonDialog;
import org.kineticsfoundation.loader.AsyncLoaderResult;
import org.kineticsfoundation.loader.NetworkAsyncTaskLoader;
import org.kineticsfoundation.net.api.AccountManager;
import org.kineticsfoundation.util.NetworkUtils;

import java.io.Serializable;
import java.util.List;

import static android.accounts.AccountManager.*;
import static android.widget.Toast.LENGTH_LONG;
import static java.util.Collections.sort;
import static org.kineticsfoundation.dialog.SimpleProgressDialog.newDialog;
import static org.kineticsfoundation.util.ValidationUtils.ValidationType.EMAIL;
import static org.kineticsfoundation.util.ValidationUtils.ValidationType.PASSWORD;
import static org.kineticsfoundation.util.ValidationUtils.isValid;

/**
 * Login logic fragment
 * Created by akaverin on 5/29/13.
 */
public class LoginFragment extends AbsNetworkFragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<AsyncLoaderResult>, DialogListener,
        TextView.OnEditorActionListener {

    private static final int LOADER_AUTHENTICATE = 0;
    private static final int LOADER_LOGIN = 1;
    private static final int DIALOG_NO_NETWORK = 0;
    private static final int DIALOG_CHOOSE_PROJECT = 1;
    private static final String KEY_PROJECTS_LIST = "KEY_PROJECTS";
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            //noinspection unchecked
            List<Project> projects = (List<Project>) msg.obj;
            Bundle extra = new Bundle();
            extra.putSerializable(KEY_PROJECTS_LIST, (Serializable) projects);

            String[] values = new String[projects.size()];
            for (int i = 0; i < values.length; ++i) {
                values[i] = projects.get(i).getName();
            }
            SingleCheckListButtonDialog.Builder builder = new SingleCheckListButtonDialog.Builder(DIALOG_CHOOSE_PROJECT,
                    LoginFragment.this).setTitle(R.string.dialog_choose_project).setPositiveButton(android.R.string
                    .ok).setNegativeButton(android.R.string.cancel).setExtra(extra).setItems(values);

            showDialog(builder.build());
            return true;
        }
    });
    private EditText emailText;
    private EditText passText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (application.getAccountManager().isAccountPresent()) {
            Toast.makeText(getActivity(), R.string.only_one_account_message, LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        view.findViewById(R.id.create_account_btn).setOnClickListener(this);
        view.findViewById(R.id.sign_in_btn).setOnClickListener(this);

        emailText = (EditText) view.findViewById(R.id.email_ef);
        passText = (EditText) view.findViewById(R.id.password_ef);
        passText.setOnEditorActionListener(this);
        passText.setTypeface(Typeface.DEFAULT);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //reload if was rotation and we didn't finish operation
        if (getLoaderManager().getLoader(LOADER_AUTHENTICATE) != null) {
            getLoaderManager().initLoader(LOADER_AUTHENTICATE, null, this);
        }
        if (getLoaderManager().getLoader(LOADER_LOGIN) != null) {
            getLoaderManager().initLoader(LOADER_LOGIN, null, this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_account_btn:
                startActivityForResult(new Intent(getActivity(), CreateAccountActivity.class),
                        CreateAccountActivity.CREATE_CODE);
                break;

            case R.id.sign_in_btn:
                startAuthenticate();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            emailText.setText(data.getStringExtra(CreateAccountFragment.KEY_EMAIL));
            passText.setText(data.getStringExtra(CreateAccountFragment.KEY_PASS));

            startAuthenticate();
        }
    }

    @Override
    public Loader<AsyncLoaderResult> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_AUTHENTICATE:
                showDialog(newDialog(R.string.dialog_logging_in_message));

                final String email = emailText.getText().toString();
                final String pass = passText.getText().toString();
                return new NetworkAsyncTaskLoader(application) {
                    @Override
                    protected AsyncLoaderResult loadInBackgroundSafe() {
                        List<Project> projects = remoteApi.getAccountManager().authenticate(email, pass);
                        return new AsyncLoaderResult(projects);
                    }
                };

            case LOADER_LOGIN:
                showDialogSkip(newDialog(R.string.dialog_logging_in_message));

                Integer projectId = args.getInt(CacheContract.Columns.ID);
                return new LoginLoader(application, emailText.getText().toString(), passText.getText().toString(),
                        projectId);
        }
        return null;
    }

    @Override
    protected void onLoadFinishedNoError(Loader<AsyncLoaderResult> loader, AsyncLoaderResult data) {
        switch (loader.getId()) {
            case LOADER_AUTHENTICATE:
                onAuthenticateSuccess(data);
                break;

            case LOADER_LOGIN:
                onLoginSuccess(data);
                break;
        }
    }

    @Override
    public void onDialogResult(Bundle bundle) {
        int id = bundle.getInt(DialogConts.KEY_ID);
        switch (id) {
            case DIALOG_NO_NETWORK:
                getActivity().finish();
                break;

            case DIALOG_CHOOSE_PROJECT:
                if (!bundle.getBoolean(DialogConts.KEY_BUTTON_POSITIVE, false)) {
                    return;
                }
                Bundle extra = bundle.getBundle(DialogConts.KEY_EXTRA);
                //noinspection unchecked
                List<Project> projects = (List<Project>) extra.getSerializable(KEY_PROJECTS_LIST);
                final String projectName = bundle.getString(DialogConts.KEY_VALUE);
                for (Project project : projects) {
                    if (project.getName().equals(projectName)) {
                        startLogin(project.getId());
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            startAuthenticate();
            return true;
        }
        return false;
    }

    private void startAuthenticate() {
        if (!isValid(getActivity(), EMAIL, emailText) || !isValid(getActivity(),
                PASSWORD, passText)) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), R.string.no_network_message, Toast.LENGTH_LONG).show();
            return;
        }
        getLoaderManager().restartLoader(LOADER_AUTHENTICATE, null, this);
    }

    private void onAuthenticateSuccess(AsyncLoaderResult data) {
        //cleanup as we got result already
        getLoaderManager().destroyLoader(LOADER_AUTHENTICATE);
        //noinspection unchecked
        List<Project> projects = (List<Project>) data.getResult();
        sort(projects);
        if (projects.size() == 1) {
            startLogin(projects.get(0).getId());
        } else {
            handler.obtainMessage(0, projects).sendToTarget();
        }
    }

    private void startLogin(Integer id) {
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), R.string.no_network_message, Toast.LENGTH_LONG).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(CacheContract.Columns.ID, id);
        getLoaderManager().restartLoader(LOADER_LOGIN, bundle, this);
    }

    private void onLoginSuccess(AsyncLoaderResult data) {
        LoginResultData loginResult = (LoginResultData) data.getResult();
        //cleanup as we got result already
        getLoaderManager().destroyLoader(LOADER_LOGIN);
        createNewAccount(loginResult.projectId);
        if (getActivity().getIntent().getExtras() != null) {
            if (getActivity().getIntent().getBooleanExtra(Authenticator.KEY_AUTH_CHECK, false)) {
                setAuthenticationResult(loginResult.token);
                Toast.makeText(getActivity(), R.string.login_success_message, Toast.LENGTH_LONG).show();
            }
        } else {
            startActivity(new Intent(getActivity(), TestListActivity.class));
        }
        getActivity().finish();
        //clean up cache
        new CleanDbTask(getActivity().getContentResolver()).execute();
    }

    private void setAuthenticationResult(String sessionToken) {
        Intent intent = createResultIntent(sessionToken);
        LoginActivity authActivity = (LoginActivity) getActivity();
        authActivity.setAccountAuthenticatorResult(intent.getExtras());
        authActivity.setResult(Activity.RESULT_OK, intent);
    }

    private Intent createResultIntent(String token) {
        return new Intent().putExtra(KEY_ACCOUNT_NAME, emailText.getText().toString())
                .putExtra(KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE)
                .putExtra(KEY_PASSWORD, passText.getText().toString())
                .putExtra(KEY_AUTHTOKEN, token);
    }

    private void createNewAccount(Integer projectId) {
        Account account = new Account(emailText.getText().toString(), Authenticator.ACCOUNT_TYPE);

        Bundle bundle = new Bundle();
        bundle.putString(Authenticator.KEY_PROJECT_ID, projectId.toString());

        android.accounts.AccountManager.get(getActivity().getApplicationContext()).addAccountExplicitly(account,
                passText.getText().toString(), bundle);

        application.getSyncUtils().enableSync();
    }

    private static class LoginLoader extends NetworkAsyncTaskLoader {

        private final String login;
        private final String pass;
        private final Integer project;

        public LoginLoader(KineticsApplication application, String login, String pass, Integer project) {
            super(application);
            this.login = login;
            this.pass = pass;
            this.project = project;
        }

        @Override
        public AsyncLoaderResult loadInBackgroundSafe() {
            AccountManager accountManager = remoteApi.getAccountManager();

            String sessionToken = accountManager.login(login, pass, project);
            return new AsyncLoaderResult(new LoginResultData(sessionToken, project));
        }
    }

    private static final class LoginResultData {
        final String token;
        final Integer projectId;

        private LoginResultData(String token, Integer projectId) {
            this.token = token;
            this.projectId = projectId;
        }
    }


}
