package org.kineticsfoundation.test.sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.kineticsfoundation.R;
import org.kineticsfoundation.test.TestConstants;
import org.kineticsfoundation.util.SupportedDevices;

import static com.google.common.base.Preconditions.checkState;

/**
 * Specialized PstDataCollector which takes into account fixes for sensors
 * Created by akaverin on 10/17/13.
 */
public class CalibratedPstDataCollector extends PstDataCollector {

    //private final float jerkFix;
    //private final float rmsFix;
    //private final float areaFix;

    public CalibratedPstDataCollector(Context context) {
        super(context);
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //checkState(sharedPreferences.getBoolean(context.getString(R.string.key_calibrate), false));

        //jerkFix = sharedPreferences.getFloat(TestConstants.KEY_JERK_FIX, 0);
        //areaFix = sharedPreferences.getFloat(TestConstants.KEY_AREA_FIX, 0);
        //rmsFix = sharedPreferences.getFloat(TestConstants.KEY_RMS_FIX, 0);
    }

    @Override
    public double getScore() {
        return super.getScore();// * jerkFix * SupportedDevices.getFixCoefficient(SupportedDevices.TestValueType.JERK);
    }

    @Override
    public double getArea() {
        return super.getArea();// * areaFix * SupportedDevices.getFixCoefficient(SupportedDevices.TestValueType.AREA);
    }

    @Override
    public double getRMS() {
        return super.getRMS();// * rmsFix * SupportedDevices.getFixCoefficient(SupportedDevices.TestValueType.RMS);
    }
}
