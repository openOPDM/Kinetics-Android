package org.kineticsfoundation.activity;

import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.kineticsfoundation.KineticsApplication;
import org.kineticsfoundation.R;
import org.kineticsfoundation.account.Authenticator;
import org.kineticsfoundation.sync.synchronizers.SyncGroup;
import org.kineticsfoundation.util.NetworkUtils;

/**
 * Our main {@link Activity} in the Application<br/>
 * Responsible for:
 * <li>Receiving Account removed message, thus redirecting to login again</li>
 * Created by akaverin on 5/29/13.
 */
public class TestListActivity extends Activity implements OnAccountsUpdateListener {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_list_activity);

        KineticsApplication application = (KineticsApplication) getApplication();
        if (!application.getAccountManager().isAccountPresent()) {
            startLogin();
        }
        getActionBar().setTitle(R.string.test_list_activity_label);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(this, R.string.credentials_invalidated, Toast.LENGTH_LONG).show();
            startLogin();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_mi:
                ((KineticsApplication) getApplication()).getSyncUtils().requestSyncForData(SyncGroup.FULL.name(), true);
                return true;
            case R.id.settings_mi:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        for (Account account : accounts) {
            if (Authenticator.ACCOUNT_TYPE.equals(account.type)) {
                return;
            }
        }
        //if we reached this point - our account was dropped
        startActivity(new Intent(this, TestListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent
                .FLAG_ACTIVITY_SINGLE_TOP));
    }

    private void startLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}