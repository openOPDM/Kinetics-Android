package org.kineticsfoundation.activity;

import android.os.Bundle;
import org.kineticsfoundation.R;

/**
 * Settings container Activity
 * Created by akaverin on 7/8/13.
 */
public class SettingsActivity extends AbsActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }
}