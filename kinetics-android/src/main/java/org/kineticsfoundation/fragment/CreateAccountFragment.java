package org.kineticsfoundation.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.lohika.protocol.core.response.error.ServerError;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dao.model.Project;
import org.kineticsfoundation.dao.model.User;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.dialog.SimpleButtonDialog;
import org.kineticsfoundation.dialog.TermsDialog;
import org.kineticsfoundation.loader.AsyncLoaderResult;
import org.kineticsfoundation.loader.NetworkAsyncTaskLoader;
import org.kineticsfoundation.util.NetworkUtils;

import java.util.List;

import static android.widget.Toast.makeText;
import static java.util.Collections.sort;
import static org.kineticsfoundation.dialog.SimpleEditButtonDialog.Builder;
import static org.kineticsfoundation.dialog.SimpleProgressDialog.newDialog;
import static org.kineticsfoundation.util.ValidationUtils.ValidationType.*;
import static org.kineticsfoundation.util.ValidationUtils.isValid;

/**
 * Fragment with Account creation logic
 * Created by akaverin on 5/31/13.
 */
public class CreateAccountFragment extends AbsNetworkFragment implements View.OnClickListener,
        TextView.OnEditorActionListener, LoaderManager.LoaderCallbacks<AsyncLoaderResult>, DialogListener {

    static final String KEY_EMAIL = "KEY_EMAIL";
    static final String KEY_PASS = "KEY_PASS";
    private static final int LOADER_CREATE_ACCOUNT = 0;
    private static final int LOADER_CONFIRM = 1;
    private static final int LOADER_PROJECTS = 2;
    private static final String KEY_CONFIRM_CODE = "KEY_CONFIRM_CODE";
    private static final int DIALOG_CONFIRM = 0;
    private static final int DIALOG_PROJECT_ERROR = 1;
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            showDialog(new Builder(DIALOG_CONFIRM, CreateAccountFragment.this)
                    .setTitle(R.string
                            .dialog_confirm_title)
                    .setMessage(R.string.confirmation_message)
                    .setPositiveButton(R.string.submit_label)
                    .setNegativeButton(R.string.cancel_label)
                    .setEditHint(R.string.confirmation_code_label)
                    .setRequired().setEditProps(EditorInfo.TYPE_CLASS_NUMBER)
                    .build());
            return true;
        }
    });
    private EditText firstNameText;
    private EditText secondNameText;
    private EditText emailText;
    private EditText passText;
    private EditText passRetypeText;
    private Spinner projectSpinner;
    private TextView termsTitle;
    private Dialog termsDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_account_fragment, container, false);

        firstNameText = (EditText) view.findViewById(R.id.first_name_ef);
        secondNameText = (EditText) view.findViewById(R.id.second_name_ef);
        emailText = (EditText) view.findViewById(R.id.email_ef);
        passText = (EditText) view.findViewById(R.id.password_ef);
        passText.setTypeface(Typeface.DEFAULT);

        passRetypeText = (EditText) view.findViewById(R.id.password_retype_ef);
        passRetypeText.setTypeface(Typeface.DEFAULT);
        passRetypeText.setOnEditorActionListener(this);

        projectSpinner = (Spinner) view.findViewById(R.id.customer_sp);

        termsTitle = (TextView) view.findViewById(R.id.terms_tv);
        setTextViewHTML(termsTitle, getString(R.string.terms_checkbox_title), getActivity());

        view.findViewById(R.id.sign_up_btn).setOnClickListener(this);
        view.findViewById(R.id.enter_code_btn).setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //start loading Projects...
        getLoaderManager().initLoader(LOADER_PROJECTS, null, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up_btn:
                startCreate();
                break;

            case R.id.enter_code_btn:
                if (isValid(getActivity(), EMAIL, emailText)) {
                    Message.obtain(handler).sendToTarget();
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            startCreate();
            return true;
        }
        return false;
    }

    @Override
    public Loader<AsyncLoaderResult> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_CREATE_ACCOUNT:
                showDialog(newDialog(R.string.dialog_submit_message));

                final User user = new User(emailText.getText().toString(), firstNameText.getText().toString(),
                        secondNameText.getText().toString());
                final String pass = passText.getText().toString();
                final Integer projectId = ((Project) projectSpinner.getSelectedItem()).getId();

                return new NetworkAsyncTaskLoader(application) {
                    @Override
                    protected AsyncLoaderResult loadInBackgroundSafe() {
                        remoteApi.getAccountManager().createUser(user, pass, projectId);
                        return AsyncLoaderResult.EMPTY;
                    }
                };

            case LOADER_CONFIRM:
                showDialog(newDialog(R.string.dialog_submit_message));

                final String code = args.getString(KEY_CONFIRM_CODE);
                final String email = emailText.getText().toString();
                return new NetworkAsyncTaskLoader(application) {
                    @Override
                    protected AsyncLoaderResult loadInBackgroundSafe() {
                        remoteApi.getAccountManager().confirmCreate(email, code);
                        return AsyncLoaderResult.EMPTY;
                    }
                };

            case LOADER_PROJECTS:
                showDialog(newDialog(R.string.dialog_loading_message));

                return new NetworkAsyncTaskLoader(application) {
                    @Override
                    public AsyncLoaderResult loadInBackgroundSafe() {
                        List<Project> projects = remoteApi.getProjectManager().getProjectInfoList();
                        return new AsyncLoaderResult(projects);
                    }
                };
        }
        return null;
    }

    @Override
    protected void onLoadFinishedNoError(Loader<AsyncLoaderResult> loader, AsyncLoaderResult data) {
        switch (loader.getId()) {
            case LOADER_CREATE_ACCOUNT:
                Message.obtain(handler).sendToTarget();
                break;

            case LOADER_CONFIRM:
                finishCreate();
                break;

            case LOADER_PROJECTS:
                dismissProgressDialog();
                //noinspection unchecked
                List<Project> projects = (List<Project>) data.getResult();
                sort(projects);
                projectSpinner.setAdapter(FragmentUtils.getCustomersAdapter(getActivity(), projects));

                //automatic handling of one single project
                if (projects.size() == 1) {
                    projectSpinner.setVisibility(View.GONE);
                }
                break;
        }
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    void onLoadError(int id, ServerError error) {
        switch (id) {
            case LOADER_PROJECTS:
                showDialog(new SimpleButtonDialog.Builder(DIALOG_PROJECT_ERROR, CreateAccountFragment.this).setTitle
                        (R.string.error_title).setMessage(getString(R.string.error_customer_load)).setPositiveButton
                        (R.string.retry).setNegativeButton(android.R.string.no).build());
                break;

            default:
                super.onLoadError(id, error);
        }
    }

    @Override
    public void onDialogResult(Bundle data) {
        int id = data.getInt(DialogConts.KEY_ID);
        switch (id) {
            case DIALOG_PROJECT_ERROR:
                if (data.getBoolean(DialogConts.KEY_BUTTON_POSITIVE)) {
                    getLoaderManager().restartLoader(LOADER_PROJECTS, null, this);
                } else {
                    getActivity().finish();
                }
                break;

            case DIALOG_CONFIRM:
                if (data.getBoolean(DialogConts.KEY_BUTTON_POSITIVE)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_CONFIRM_CODE, data.getString(DialogConts.KEY_VALUE));
                    getLoaderManager().restartLoader(LOADER_CONFIRM, bundle, this);
                }
                break;
        }
    }

    private void startCreate() {
        if (IsInvalidInput()) {
            return;
        }
        if (isInvalidPassword()) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            makeText(getActivity(), R.string.no_network_message, Toast.LENGTH_LONG).show();
            return;
        }
        getLoaderManager().restartLoader(LOADER_CREATE_ACCOUNT, null, this);
    }

    private void finishCreate() {
        makeText(getActivity(), R.string.account_success_create_message_text, Toast.LENGTH_LONG).show();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_EMAIL, emailText.getText().toString().toLowerCase());
        resultIntent.putExtra(KEY_PASS, passText.getText().toString());
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    private boolean isInvalidPassword() {
        if (!passText.getText().toString().equals(passRetypeText.getText().toString())) {
            passRetypeText.setError(getString(R.string.error_pass_match));
            return true;
        }
        return false;
    }

    private boolean IsInvalidInput() {
        return !isValid(getActivity(), TEXT, firstNameText) || !isValid(getActivity(), TEXT, secondNameText) ||
                !isValid(getActivity(), EMAIL, emailText) || !isValid(getActivity(), PASSWORD, passText) ||
                !isValid(getActivity(), PASSWORD, passRetypeText);
    }

    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, final Context context){
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                Log.d("CreateAccountFragment", "Terms clicked!");
                if("show_terms".equals(span.getURL())){
                    Log.d("CreateAccountFragment", "Show terms dialog!");
                    termsDialog = TermsDialog.createTerms(context, TermsDialog.Type.TERMS);
                    termsDialog.show();
                } else if("show_policy".equals(span.getURL())){
                    Log.d("CreateAccountFragment", "Show terms dialog!");
                    termsDialog = TermsDialog.createTerms(context, TermsDialog.Type.POLICY);
                    termsDialog.show();
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void setTextViewHTML(TextView text, String html, Context context){
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, context);
        }
        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.setText(strBuilder);
    }

    @Override
    public void onPause() {
        if(termsDialog != null)
            termsDialog.dismiss();

        super.onPause();
    }
}
