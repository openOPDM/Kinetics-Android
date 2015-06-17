package org.kineticsfoundation.test;

/**Shared Test constants
 * Created by akaverin on 7/2/13.
 */
public interface TestConstants {

    double CALIBRATED_JERK = 0.0230;
    double CALIBRATED_RMS = 0.0500;
    double CALIBRATED_AREA = 0.008;

    String KEY_JERK_FIX = "KEY_JERK_FIX";
    String KEY_RMS_FIX = "KEY_RMS_FIX";
    String KEY_AREA_FIX = "KEY_AREA_FIX";

    enum TestType {
        TUG, PST, KB
    }

}
