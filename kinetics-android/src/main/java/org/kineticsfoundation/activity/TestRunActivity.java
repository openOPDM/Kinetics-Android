package org.kineticsfoundation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.fragment.PstTestFragment;
import org.kineticsfoundation.fragment.TugTestFragment;
import org.kineticsfoundation.test.TestConstants;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Activity for Test Details view
 * Created by akaverin on 6/7/13.
 */
public class TestRunActivity extends AbsActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_run_activity);

        TestConstants.TestType testType = (TestConstants.TestType) getIntent().getSerializableExtra(CacheContract
                .Columns.TYPE);

        switch (testType) {
            case TUG:
                TugTestFragment tugFragment = new TugTestFragment();
                getFragmentManager().beginTransaction().add(android.R.id.content, tugFragment).commit();
                break;
            case PST:
                /*if (!getDefaultSharedPreferences(getApplicationContext()).getBoolean
                        (getString(R.string.key_calibrate), false)) {
                    startActivityForResult(new Intent(this, PstCalibrationActivity.class), PstCalibrationActivity
                            .CALIBRATE_CODE);
                } else {
                    startPstFragment();
                }*/
                startPstFragment();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PstCalibrationActivity.CALIBRATE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    startPstFragment();
                    break;

                case RESULT_CANCELED:
                    finish();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //block back button while running tests...
        if (isUnlocked()) {
            finish();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return !isUnlocked() || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return !isUnlocked() || super.onKeyLongPress(keyCode, event);
    }

    private boolean isUnlocked() {
        return getActionBar().isShowing();
    }

    private void startPstFragment() {
        PstTestFragment pstFragment = new PstTestFragment();
        getFragmentManager().beginTransaction().add(android.R.id.content, pstFragment).commit();
    }
}