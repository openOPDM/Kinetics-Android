package org.kineticsfoundation.test.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.kircherelectronics.lowpasslinearacceleration.filter.LPFAndroidDeveloper;
import com.kircherelectronics.lowpasslinearacceleration.filter.LowPassFilter;
import org.kineticsfoundation.lib.DataItem;
import org.kineticsfoundation.lib.FilterLibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Performs collection of accelerometer data for PST test calculations Created
 * by akaverin on 7/15/13.
 */
public class PstDataCollector implements SensorEventListener {

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final LowPassFilter filter;
    private final ArrayList<DataItem> data;
    private final FilterLibrary filterLibrary;
    private boolean isCalibrating = true;


    /**
     * Warning!!!!!! Constructor for unit tests
     * Do not use for application logic
     */
    public PstDataCollector() {
        sensorManager = null;
        accelerometer = null;

        // Set alpha to 1 for disable excluding gravity from accelerometer data
        // because gravity on iPhone data is already excluded
        filter = new LPFAndroidDeveloper();
        filter.setAlphaStatic(true);
        filter.setAlpha(1);

        data = Lists.newArrayList();
        filterLibrary = new FilterLibrary();
    }

    public PstDataCollector(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            throw new IllegalStateException("Failed to initialize Accelerometer!");
        }
        filter = new LPFAndroidDeveloper();
        data = Lists.newArrayList();
        filterLibrary = new FilterLibrary();
    }

    public void start() {
        sensorManager.registerListener(this, accelerometer, (int) TimeUnit.MILLISECONDS.toMicros(18));
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void stopCalibrating() {
        isCalibrating = false;
    }

    public void calculate() {
        filterLibrary.process(data.toArray(new DataItem[data.size()]));
    }

    public double getScore() {
        return filterLibrary.getJerk();
    }

    public double getRMS() {
        return filterLibrary.getRms();
    }

    public double getArea() {
        return filterLibrary.getArea();
    }

    public String buildRawData() {
        StringBuilder builder = new StringBuilder();
        builder.append(getArea()).append(";").append(getRMS()).append(";");
        for (DataItem item : data) {
            builder.append(',');
            Joiner.on(',').appendTo(builder, item.values[0], item.values[1], item.values[2], item.ts);
        }
        builder.append(";");

        return builder.toString();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] cleanValues = filter.addSamples(event.values);
        if (isCalibrating) {
            return;
        }
        data.add(new DataItem(TimeUnit.NANOSECONDS.toMillis(event.timestamp) / 1000d, Arrays.copyOf(cleanValues,
                cleanValues.length)));
    }

    /**
     * For unit tests only!!!!!
     *
     * @param values
     * @param timestamp
     */
    public void onSensorEventHandle(float[] values, double timestamp) {
        float[] cleanValues = filter.addSamples(values);
        if (isCalibrating) {
            return;
        }
        data.add(new DataItem(timestamp, Arrays.copyOf(cleanValues,
                cleanValues.length)));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
