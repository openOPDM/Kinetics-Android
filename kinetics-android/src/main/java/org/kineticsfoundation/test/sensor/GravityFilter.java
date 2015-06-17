package org.kineticsfoundation.test.sensor;

/**
 * Helper class to remove gravity force from Accelerometer data
 * Created by akaverin on 7/15/13.
 */
class GravityFilter {
    // alpha is calculated as t / (t + dT)
    // with t, the low-pass filter's time-constant
    // and dT, the event delivery rate
    private static final float ALPHA = 0.8F;
    //accumulate weighted gravity value - use for usual ACCELEROMETER
    private final float[] gravity = new float[3];

    float[] filter(float[] values) {
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * values[0];
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * values[1];
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * values[2];

        final float x = values[0] - gravity[0];
        final float y = values[1] - gravity[1];
        final float z = values[2] - gravity[2];

        return new float[]{x, y, z};
    }

}
