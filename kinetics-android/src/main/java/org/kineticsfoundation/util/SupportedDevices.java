package org.kineticsfoundation.util;

import android.os.Build;
import de.akquinet.android.androlog.Log;

/**
 * Verify device for certification
 * <p/>
 * Created by Taras Sheremeta on 5/7/14.
 */
public class SupportedDevices {

    private static float[][] FixCoefficients;

    static {
        FixCoefficients = new float[DeviceType.values().length][TestValueType.values().length];

        float[] s3Fixes = new float[3];
        s3Fixes[TestValueType.JERK.ordinal()] = 1F;
        s3Fixes[TestValueType.AREA.ordinal()] = 1F;
        s3Fixes[TestValueType.RMS.ordinal()] = 1F;
        FixCoefficients[DeviceType.S3.ordinal()] = s3Fixes;


        float[] s4Fixes = new float[3];
        s4Fixes[TestValueType.JERK.ordinal()] = 1F;
        s4Fixes[TestValueType.AREA.ordinal()] = 1F;
        s4Fixes[TestValueType.RMS.ordinal()] = 1F;
        FixCoefficients[DeviceType.S4.ordinal()] = s4Fixes;

        float[] s5Fixes = new float[3];
        s5Fixes[TestValueType.JERK.ordinal()] = 1F;
        s5Fixes[TestValueType.AREA.ordinal()] = 1F;
        s5Fixes[TestValueType.RMS.ordinal()] = 1F;
        FixCoefficients[DeviceType.S5.ordinal()] = s5Fixes;

        float[] m8Fixes = new float[3];
        m8Fixes[TestValueType.JERK.ordinal()] = 1F;
        m8Fixes[TestValueType.AREA.ordinal()] = 1F;
        m8Fixes[TestValueType.RMS.ordinal()] = 1F;
        FixCoefficients[DeviceType.M8.ordinal()] = m8Fixes;

    }

    public static enum TestValueType{
        JERK, AREA, RMS
    }

    public static boolean isSupportedDevice() {
        return getDeviceType() != DeviceType.UNSUPPORTED;
    }

    public static String getSupportedPhones() {
        StringBuilder stringBuilder = new StringBuilder();
        for(int index = 0; index < supportedDevicesFriendlyNames.length; index++){
            stringBuilder.append(supportedDevicesFriendlyNames[index]);
            if(index < supportedDevicesFriendlyNames.length - 1) {
                if(index == supportedDevicesFriendlyNames.length - 2) {
                    stringBuilder.append(" and ");
                } else {
                    stringBuilder.append(", ");
                }
            }
        }
        return stringBuilder.toString();
    }

    public static float getFixCoefficient(TestValueType valueType) {
        DeviceType deviceType = getDeviceType();

        if(deviceType != DeviceType.UNSUPPORTED){
            return FixCoefficients[deviceType.ordinal()][valueType.ordinal()];
        }

        return DEFAULT_UNIQUE_COEFFICIENT;
    }

    /* *********************************************************************************
                                    Private function block
     ********************************************************************************* */

    private static final int DEFAULT_UNIQUE_COEFFICIENT = 1;

    private static DeviceType getDeviceType(){
        String deviceInfo = Build.MODEL;

        android.util.Log.d("SupportedDevices", "deviceInfo = " + deviceInfo);
        for (String device : supportedDeviceCodesS3) {
            android.util.Log.d("SupportedDevices", "Check device : " + device + "  >>> " + deviceInfo);
            if (device.contains(deviceInfo)) return DeviceType.S3;
        }

        for (String device : supportedDeviceCodesS4) {
            android.util.Log.d("SupportedDevices", "Check device : " + device + "  >>> " + deviceInfo);
            if (device.contains(deviceInfo)) return DeviceType.S4;
        }

        for (String device : supportedDeviceCodesS5) {
            android.util.Log.d("SupportedDevices", "Check device : " + device + "  >>> " + deviceInfo);
            if (device.contains(deviceInfo)) return DeviceType.S5;
        }

        for (String device : supportedDeviceCodesM8) {
            android.util.Log.d("SupportedDevices", "Check device : " + device + "  >>> " + deviceInfo);
            if (device.contains(deviceInfo)) return DeviceType.M8;
        }

        return DeviceType.UNSUPPORTED;
    }

    private static enum DeviceType{
        UNSUPPORTED, S3, S4, S5, M8
    }

    private static String[] supportedDeviceCodesS3 = {
            // Galaxy S3
            "SAMSUNG-GT-I9300", // International
            "SAMSUNG-GT-I9300T", // International
            "SAMSUNG-GT-I9305", // International
            "SAMSUNG-GT-I9305N", // International
            "SAMSUNG-GT-I9305T", // International
            "SAMSUNG-SHV-E210", // South Korea
            "SAMSUNG-SGH-T999", // Canada, United States
            "SAMSUNG-SGH-I747", // Canada, United States
            "SAMSUNG-SGH-N06", // Japan
            "SAMSUNG-SC-06D", // Japan
            "SAMSUNG-SGH-N035", // Japan
            "SAMSUNG-SC-03E", // Japan
            "SAMSUNG-SCH-J021", // Japan
            "SAMSUNG-SCL21", // Japan
            "SAMSUNG-SCH-R530", // United States
            "SAMSUNG-SCH-I535", // United States
            "SAMSUNG-SCH-S960L", // United States
            "SAMSUNG-SCH-S968C", // United States
            "SAMSUNG-GT-I9308", // China
            "SAMSUNG-SCH-I939", // China, Taiwan
    };

    private static String[] supportedDeviceCodesS4 = {
            // Galaxy S4
            "SAMSUNG-GT-I9500", // International
            "SAMSUNG-SHV-E300", // South Korea
            "SAMSUNG-SHV-E300K", // South Korea
            "SAMSUNG-SHV-E300L", // South Korea
            "SAMSUNG-SHV-E300S", // South Korea
            "SAMSUNG-SHV-E300", // South Korea
            "SAMSUNG-SHV-E300K", // South Korea
            "SAMSUNG-SHV-E330L", // South Korea
            "SAMSUNG-SHV-E330S", // South Korea
            "SAMSUNG-GT-I9505", // International
            "SAMSUNG-GT-I9506", // International
            "SAMSUNG-GT-I9505G",// United States
            "SAMSUNG-SGH-I337", // United States
            "SAMSUNG-SGH-M919", // United States
            "SAMSUNG-SCH-I545", // United States
            "SAMSUNG-SPH-L720", // United States
            "SAMSUNG-SCH-R970", // United States
            "SAMSUNG-SCH-I959", // China
            "SAMSUNG-GT-I9502", // China
            "SAMSUNG-GT-I9508", // China
            "SAMSUNG-SGH-N045", // Japan
            "SAMSUNG-SC-04E",   // Japan
            "SAMSUNG-SGH-I337M",// Canada/Mexico
            "SAMSUNG-SGH-M919V",// Canada
            "SAMSUNG-SCH-R970X",//United States
            "SAMSUNG-SCH-R970C",//United States
    };


    private static String[] supportedDeviceCodesS5 = {
            // Galaxy S5
            "SAMSUNG-SM-G900F",
            "SAMSUNG-SM-G900H",
            "SAMSUNG-SM-G900R4",
            "SAMSUNG-SM-G900V",
            "SAMSUNG-SM-G900RZWAUSC",
            "SAMSUNG-SM-G900W8",
    };

    private static String[] supportedDeviceCodesM8 = {
            // HTC One M8
            "HTC One_M8"

            // TODO: HTC Desire S - to another array

            // TODO: НТС Desire SV326 - to another array
    };

    private static String[] supportedDevicesFriendlyNames = {
            "Samsung Galaxy S3", "Samsung Galaxy S4", "Samsung Galaxy S5", /*"HTC Desire S", "НТС Desire SV326",*/ "HTC One M8"
    };


}
