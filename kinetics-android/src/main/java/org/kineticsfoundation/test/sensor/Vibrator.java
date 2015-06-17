package org.kineticsfoundation.test.sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.kineticsfoundation.R;

/**
 * Helper wrapper class
 * Created by akaverin on 7/11/13.
 */
public class Vibrator {

    private static final long[] LONG_PATTERN = new long[]{0, 400, 200, 400};
    private static final int SHORT_PATTERN = 400;
    private final android.os.Vibrator vibrator;
    private final boolean isEnabled;

    public Vibrator(Context context) {
        this.vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isEnabled = sharedPreferences.getBoolean(context.getString(R.string.key_vibrate), true);
    }

    public void single() {
        if (isEnabled) {
            vibrator.vibrate(SHORT_PATTERN);
        }
    }

    public void double2() {
        if (isEnabled) {
            vibrator.vibrate(LONG_PATTERN, -1);
        }
    }

}
