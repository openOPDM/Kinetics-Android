package org.kineticsfoundation;

import android.test.AndroidTestCase;
import android.util.Log;
import org.kineticsfoundation.test.sensor.PstDataCollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Test for pst data collector
 * Created by tsheremeta on 5/23/14.
 */
public class PstDataConnectorTest extends AndroidTestCase {
    private static final String TAG = "PstDataConnectorTest";

    private static final double EXCEPTED_JERK_DIFF = 0.025;
    private static final double EXCEPTED_AREA_DIFF = 0.025;
    private static final double EXCEPTED_RMS_DIFF = 0.025;

    public void testCalculate() {
        double EXCEPTED_JERK_VALUE = 55.379200;
        double EXCEPTED_AREA_VALUE = 0.192635;
        double EXCEPTED_RMS_VALUE = 0.996005;

        PstDataCollector dataCollector = new PstDataCollector();
        dataCollector.stopCalibrating();
        // Read Data from File
        try {
            InputStream inputStream = getContext().getAssets().open("RAWData.txt");
            Log.d(TAG, "File Opened!");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        inputStream);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);

                String receiveString;
                while ((receiveString = bufferedReader.readLine()) != null) {
                    String[] cells = receiveString.split(" ");
                    if (cells.length != 4) return;

                    float[] parsedValues = new float[3];
                    parsedValues[0] = Float.parseFloat(cells[0]);// / 9.81F;
                    parsedValues[1] = Float.parseFloat(cells[1]);// / 9.81F;
                    parsedValues[2] = Float.parseFloat(cells[2]);// / 9.81F;

                    double timestamp = Double.parseDouble(cells[3]);
                    dataCollector.onSensorEventHandle(parsedValues, timestamp);
                }
                inputStream.close();
            }

        } catch (IOException e) {
            Log.d(TAG, "Cannot open file!");
            e.printStackTrace();
        }

        dataCollector.calculate();

        Log.d(TAG, "Expected Result:  JERK: " + EXCEPTED_JERK_VALUE + "; AREA: " + EXCEPTED_AREA_VALUE + "; RMS: " + EXCEPTED_RMS_VALUE);
        Log.d(TAG, "JERK = " + dataCollector.getScore());
        Log.d(TAG, "AREA = " + dataCollector.getArea());
        Log.d(TAG, "RMS = " + dataCollector.getRMS());

        assertTrue("JERK diff is more than " + EXCEPTED_JERK_DIFF, (EXCEPTED_JERK_VALUE - dataCollector.getScore()) / dataCollector.getScore() < EXCEPTED_JERK_DIFF);
        assertTrue("AREA diff is more than " + EXCEPTED_AREA_DIFF, (EXCEPTED_AREA_VALUE - dataCollector.getArea()) / dataCollector.getArea() < EXCEPTED_AREA_DIFF);
        assertTrue("RMS diff is more than " + EXCEPTED_RMS_DIFF, (EXCEPTED_RMS_VALUE - dataCollector.getRMS()) / dataCollector.getRMS() < EXCEPTED_RMS_DIFF);
    }

    public void testCalculate981() {
        double EXCEPTED_JERK_VALUE = 164.165017;
        double EXCEPTED_AREA_VALUE = 0.750143;
        double EXCEPTED_RMS_VALUE = 4.318340;


        PstDataCollector dataCollector = new PstDataCollector();
        dataCollector.stopCalibrating();
        // Read Data from File
        try {
            InputStream inputStream = getContext().getAssets().open("RAWData981.txt");
            Log.d(TAG, "File Opened!");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        inputStream);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);

                String receiveString;
                while ((receiveString = bufferedReader.readLine()) != null) {
                    String[] cells = receiveString.split(" ");
                    if (cells.length != 4) return;

                    float[] parsedValues = new float[3];
                    parsedValues[0] = Float.parseFloat(cells[0]);// / 9.81F;
                    parsedValues[1] = Float.parseFloat(cells[1]);// / 9.81F;
                    parsedValues[2] = Float.parseFloat(cells[2]);// / 9.81F;

                    double timestamp = Double.parseDouble(cells[3]);
                    dataCollector.onSensorEventHandle(parsedValues, timestamp);
                }
                inputStream.close();
            }

        } catch (IOException e) {
            Log.d(TAG, "Cannot open file!");
            e.printStackTrace();
        }

        dataCollector.calculate();

        Log.d(TAG, "Expected Result:  JERK: " + EXCEPTED_JERK_VALUE + "; AREA: " + EXCEPTED_AREA_VALUE + "; RMS: " + EXCEPTED_RMS_VALUE);
        Log.d(TAG, "JERK = " + dataCollector.getScore());
        Log.d(TAG, "AREA = " + dataCollector.getArea());
        Log.d(TAG, "RMS = " + dataCollector.getRMS());

        assertTrue("JERK diff is more than " + EXCEPTED_JERK_DIFF, (EXCEPTED_JERK_VALUE - dataCollector.getScore()) / dataCollector.getScore() < EXCEPTED_JERK_DIFF);
        assertTrue("AREA diff is more than " + EXCEPTED_AREA_DIFF, (EXCEPTED_AREA_VALUE - dataCollector.getArea()) / dataCollector.getArea() < EXCEPTED_AREA_DIFF);
        assertTrue("RMS diff is more than " + EXCEPTED_RMS_DIFF, (EXCEPTED_RMS_VALUE - dataCollector.getRMS()) / dataCollector.getRMS() < EXCEPTED_RMS_DIFF);
    }
}
