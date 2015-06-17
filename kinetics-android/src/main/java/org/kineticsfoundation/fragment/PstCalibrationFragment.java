package org.kineticsfoundation.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import org.kineticsfoundation.R;
import org.kineticsfoundation.test.TestConstants;
import org.kineticsfoundation.test.sensor.PstDataCollector;

import java.util.Arrays;

import static android.text.Html.fromHtml;

/**
 * Fragment responsible for PST compensation calibration
 * Created by akaverin on 10/17/13.
 */
public class PstCalibrationFragment extends AbsFragment implements View.OnClickListener {

    private static final int PREPARE_CALIBRATE_INTERVAL = 2000;
    private static final int CALIBRATE_INTERVAL = 10000;
    private View content;
    private View progress;
    private PstDataCollector dataCollector;
    private boolean isCalibrating = false;


    private static final int calibrationStepsNumber = 5;
    private static int calibrationStep = 0;

    private static double[] calibrResultsJERK = new double[calibrationStepsNumber];
    private static double[] calibrResultsAREA = new double[calibrationStepsNumber];
    private static double[] calibrResultsRMS = new double[calibrationStepsNumber];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_calibrate_fragment, container, false);

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(fromHtml(getString(R.string.instruction_calibrate)));

        view.findViewById(android.R.id.button1).setOnClickListener(this);

        content = view.findViewById(android.R.id.content);
        progress = view.findViewById(android.R.id.progress);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isCalibrating) {
            isCalibrating = false;
            dataCollector.stop();
            Toast.makeText(getActivity(), "Calibration was interrupted.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        content.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        calibrationStep = 0;
        startCalibration();
    }

    private void startCalibration() {
        dataCollector = new PstDataCollector(getActivity().getApplicationContext());
        dataCollector.start();
        isCalibrating = true;

        progress.postDelayed(new Runnable() {
            @Override
            public void run() {
                dataCollector.stopCalibrating();

                progress.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finishCalibration();
                    }
                }, CALIBRATE_INTERVAL);
            }
        }, PREPARE_CALIBRATE_INTERVAL);
    }

    private void finishCalibration() {
        if (!isCalibrating) {
            return;
        }
        dataCollector.stop();
        dataCollector.calculate();
        isCalibrating = false;

        calibrResultsJERK[calibrationStep] = dataCollector.getScore();
        calibrResultsAREA[calibrationStep] = dataCollector.getArea();
        calibrResultsRMS[calibrationStep] = dataCollector.getRMS();

        if(calibrationStep < calibrationStepsNumber - 1) {
            calibrationStep++;
            Log.d("PstCalibrationFragment", "Go to next step " + calibrationStep);
            startCalibration();
        } else {
            double scoreAverage = median(calibrResultsJERK);
            double areaAverage = median(calibrResultsAREA);
            double rmsAverage = median(calibrResultsRMS);

            float jerkFix = (float) (TestConstants.CALIBRATED_JERK / scoreAverage);
            float rmsFix = (float) (TestConstants.CALIBRATED_RMS / areaAverage);
            float areaFix = (float) (TestConstants.CALIBRATED_AREA / rmsAverage);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity()
                    .getApplicationContext());

            preferences.edit().putFloat(TestConstants.KEY_JERK_FIX, jerkFix).putFloat(TestConstants.KEY_RMS_FIX,
                    rmsFix).putFloat(TestConstants.KEY_AREA_FIX, areaFix)
                    .putBoolean(getString(R.string.key_calibrate), true)
                    .apply();

            //for PST runner to activate..
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
            Toast.makeText(getActivity(), "Calibration finished successfully.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private static double median(double[] a) {
        double[] b = new double[a.length];
        System.arraycopy(a, 0, b, 0, b.length);
        Arrays.sort(b);

        if (a.length % 2 == 0) {
            return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
        } else {
            return b[b.length / 2];
        }
    }
}
