package org.kineticsfoundation.activity;

import android.os.Bundle;
import org.kineticsfoundation.R;

/**
 * Activity for calibration
 * Created by akaverin on 10/17/13.
 */
public class PstCalibrationActivity extends AbsActivity {

    public static final int CALIBRATE_CODE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_calibrate_activity);
    }
}