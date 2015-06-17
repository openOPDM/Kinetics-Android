package org.kineticsfoundation.test.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Our Adapter for getting Sensor notifications
 * Created by akaverin on 6/26/13.
 */
public class MotionDetector implements SensorEventListener {

    private static final int INACTIVE_THRESHOLD = 500;
    private final SensorManager sensorManager;
    private final WeakReference<MotionDetectorListener> weakListener;
    private Sensor accelerometer;
    private long lastTimeStamp = 0;
    private MotionStrategy strategy;

    public MotionDetector(Context context, MotionDetectorListener listener) {
        this.weakListener = new WeakReference<MotionDetectorListener>(listener);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        chooseSensorAndStrategy();
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final double movement = strategy.calculateMovement(event.values);

        // XXX fix for Nexus 4, 5
        event.timestamp = System.nanoTime();

        Log.d("Sensor", "Event Timestamp = " + event.timestamp);
        long millisecondTimeStamp = TimeUnit.NANOSECONDS.toMillis(event.timestamp);
        if (lastTimeStamp == 0 || movement >= strategy.getThreshold()) {
            lastTimeStamp = millisecondTimeStamp;
        } else {
            if (millisecondTimeStamp - lastTimeStamp >= INACTIVE_THRESHOLD) {
                MotionDetectorListener listener = weakListener.get();
                if (listener != null) {//1400250670734415099
                    listener.onMoveStop(lastTimeStamp);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void chooseSensorAndStrategy() {
        //TODO: for some reason it doesn't behave well when Phone is held vertically
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        if (accelerometer != null) {
//            strategy = MotionStrategy.LINEAR;
//            return;
//        }
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            throw new IllegalStateException("Failed to initialize Accelerometer!");
        }
        strategy = MotionStrategy.GRAVITATION;
    }

    public interface MotionDetectorListener {
        void onMoveStop(long timestamp);
    }

}
