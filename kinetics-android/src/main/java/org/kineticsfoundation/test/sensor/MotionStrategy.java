package org.kineticsfoundation.test.sensor;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Interface for sensor specific strategies
 * Created by akaverin on 7/4/13.
 */
public enum MotionStrategy {

    LINEAR {
        @Override
        double calculateMovement(float[] values) {
            return sqrt(pow(values[0], 2f) + pow(values[1], 2f) + pow(values[2], 2f));
        }

        @Override
        double getThreshold() {
            return 1.5;
        }

    }, GRAVITATION {
        private final GravityFilter filter = new GravityFilter();

        @Override
        double calculateMovement(float[] values) {
            float[] cleanValues = filter.filter(values);

            //noinspection SuspiciousNameCombination
            return sqrt(pow(cleanValues[0], 2f) + pow(cleanValues[1], 2f) + pow(cleanValues[2], 2f));
        }

        @Override
        double getThreshold() {
            return 1.2;
        }
    };

    abstract double calculateMovement(float[] values);

    abstract double getThreshold();

}
