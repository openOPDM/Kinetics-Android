package org.kineticsfoundation.activity;

import android.accounts.AccountAuthenticatorActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import org.kineticsfoundation.R;

import static org.kineticsfoundation.util.SupportedDevices.getSupportedPhones;
import static org.kineticsfoundation.util.SupportedDevices.isSupportedDevice;

/**
 * Account Login activity
 * Created by akaverin on 5/29/13.
 */
public class LoginActivity extends AccountAuthenticatorActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.login_activity);

        checkFirstStart();
    }

    private void checkFirstStart() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstStart = sharedPreferences.getBoolean(getString(R.string.key_first_start), true);

        if (!isFirstStart) return;

        if (!isSupportedDevice()) {
            String message = getString(R.string.unsupported_warning, getSupportedPhones());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_title_first_start));
            builder.setMessage(message);
            builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    dialog.dismiss();
                }
            });

            // Change first start status
            sharedPreferences.edit().putBoolean(getString(R.string.key_first_start), false)
                    .apply();


            builder.show();
        }
    }

}