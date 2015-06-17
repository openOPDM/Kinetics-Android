package org.kineticsfoundation.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.lohika.sync.core.exception.SynAuthErrorException;
import de.akquinet.android.androlog.Log;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.R;
import org.kineticsfoundation.activity.PstCalibrationActivity;
import org.kineticsfoundation.activity.TestListActivity;
import org.kineticsfoundation.dao.provider.CacheContentProvider;
import org.kineticsfoundation.dialog.DialogConts;
import org.kineticsfoundation.dialog.DialogListener;
import org.kineticsfoundation.dialog.SimpleButtonDialog;
import org.kineticsfoundation.dialog.TermsDialog;
import org.kineticsfoundation.util.NetworkUtils;

import static org.kineticsfoundation.fragment.FragmentUtils.showDialog;

/**
 * Settings fragment with main logic
 * Created by akaverin on 7/8/13.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference
        .OnPreferenceChangeListener, DialogListener {

    private static final int DIALOG_GLOBAL_SYNC = 0;
    private static final int DIALOG_AUTO_SYNC = 1;
    private KineticsApplication application;
    private Dialog termsDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        application = (KineticsApplication) activity.getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Preference accountPref = findPreference(getString(R.string.key_account_name));
        accountPref.setSummary(application.getAccountManager().getAccount().name);

        findPreference(getString(R.string.key_logout)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_sync_start)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_interval)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.key_terms)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.key_policy)).setOnPreferenceClickListener(this);

        updateAppVersion();
    }

    @Override
    public void onResume() {
        super.onResume();
        //setupCalibrationButton();
    }

    @Override
    public void onPause() {
        if(termsDialog != null){
            termsDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.key_logout))) {
            new LogoutAsyncTask(application).execute();
        } else if (key.equals(getString(R.string.key_sync_start))) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
            if (checkBoxPreference.isChecked()) {
                verifySyncSettings(R.string.sync_on_start_label);
            }
        } else if (key.equals(getString(R.string.key_terms))) {
            termsDialog = TermsDialog.createTerms(getActivity(), TermsDialog.Type.TERMS);
            termsDialog.show();
        } else if (key.equals(getString(R.string.key_policy))) {
            termsDialog = TermsDialog.createTerms(getActivity(), TermsDialog.Type.POLICY);
            termsDialog.show();
        } /*else if (key.equals(getString(R.string.key_calibrate))) {
            startActivity(new Intent(getActivity(), PstCalibrationActivity.class));
        } */

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ListPreference listPreference = (ListPreference) preference;
        int idx = listPreference.findIndexOfValue((String) newValue);
        listPreference.setSummary(getString(R.string.pref_sync_time_summary, listPreference.getEntries()[idx]));

        Long syncInterval = Long.parseLong((String) newValue);
        if (syncInterval == 0) {
            application.getSyncUtils().stopPeriodicSync();
        } else {
            application.getSyncUtils().startPeriodicSync(syncInterval);
            verifySyncSettings(R.string.sync_periodic_label);
        }
        return true;
    }

    @Override
    public void onDialogResult(Bundle bundle) {
        int id = bundle.getInt(DialogConts.KEY_ID);
        switch (id) {
            case DIALOG_GLOBAL_SYNC:
                if (bundle.getBoolean(DialogConts.KEY_BUTTON_POSITIVE)) {
                    ContentResolver.setMasterSyncAutomatically(true);
                    enableAutoSync();
                }
                break;
            case DIALOG_AUTO_SYNC:
                if (bundle.getBoolean(DialogConts.KEY_BUTTON_POSITIVE)) {
                    enableAutoSync();
                }
        }
    }

    private void setupCalibrationButton() {
        String key = getString(R.string.key_calibrate);
        Preference preference = findPreference(key);
        preference.setOnPreferenceClickListener(this);

        if (getPreferenceManager().getSharedPreferences().getBoolean(key, false)) {
            preference.setSummary(R.string.setting_calibrated_device);
        } else {
            preference.setSummary(R.string.setting_uncalibrated_device);
        }
    }

    private void updateAppVersion() {
        Preference preference = findPreference(getString(R.string.key_version));
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName()
                    , 0);
            preference.setSummary(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(this, "Failed to obtain current app version", e);
        }
    }

    private void verifySyncSettings(int settingId) {
        if (!ContentResolver.getMasterSyncAutomatically()) {
            showDialog(this, buildSyncDialog(DIALOG_GLOBAL_SYNC, settingId, R.string.global_sync));

        } else if (!ContentResolver.getSyncAutomatically(application.getAccountManager().getAccount(),
                CacheContentProvider.AUTHORITY)) {
            showDialog(this, buildSyncDialog(DIALOG_AUTO_SYNC, settingId, R.string.auto_sync));
        }
    }

    private SimpleButtonDialog buildSyncDialog(int dialogId, int settingId, int syncType) {
        return new SimpleButtonDialog.Builder(dialogId, this).setTitle(R
                .string.dialog_sync_title).setPositiveButton(android.R.string.ok).setNegativeButton(android.R.string
                .cancel).setIcon(R.drawable.navigation_refresh).setMessage(getString(R.string.dialog_sync_message,
                getString(settingId), getString(syncType))).build();
    }

    private void enableAutoSync() {
        ContentResolver.setSyncAutomatically(application.getAccountManager().getAccount(),
                CacheContentProvider.AUTHORITY, true);
    }

    private final class LogoutAsyncTask extends AsyncTask<Void, Void, Void> {

        private final KineticsApplication application;

        private LogoutAsyncTask(KineticsApplication application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final String token = application.getAccountManager().getBlockingAuthToken(false);
            application.getAccountManager().removeAccounts();

            if (NetworkUtils.isNetworkAvailable(application)) {
                try {
                    application.getRemoteApi().getAccountManager().logout(token);
                } catch (SynAuthErrorException e) {
                    //ignore as it can be additional request due to logout call
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            startActivity(new Intent(getActivity(), TestListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            getActivity().finish();
        }
    }
}
