package org.kineticsfoundation.fragment;

import android.app.Application;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dao.task.AbsTestPersistTask;
import org.kineticsfoundation.test.TestConstants;
import org.kineticsfoundation.test.TestModel;
import org.kineticsfoundation.test.sensor.CalibratedPstDataCollector;
import org.kineticsfoundation.test.sensor.PstDataCollector;

import java.util.concurrent.TimeUnit;

import static org.kineticsfoundation.test.TestModel.Attr.*;
import static org.kineticsfoundation.util.Format.formatPstMeasurement;
import static org.kineticsfoundation.util.Format.formatPstRmsMeasurement;
import static org.kineticsfoundation.widget.WidgetProperty.VALUE;

/**
 * PST Test specialized fragment
 * Created by akaverin on 7/15/13.
 */
public class PstTestFragment extends AbsTestFragment {

    private static final long ELAPSED_TIME_UPDATE_INTERVAL = 1000;
    private static final long DEFAULT_MEASURE_TIME = 30;
    private static final long CALIBRATE_TIME_INTERVAL = 2000;
    private long startTime;
    private CalibratedPstDataCollector dataCollector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setSubtitle("PST Test");
    }

    @Override
    public void onPause() {
        //make sure to call prior to super
        if (runState == RunState.RUN) {
            dataCollector.stop();
        }
        super.onPause();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(runState == RunState.FINISH);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected TestModel buildModel(Cursor data) {
        startTest();
        return TestModel.buildPstModel(getActivity().getApplicationContext(), data);
    }

    @Override
    protected void onPersistTest() {
        new PstTestPersistTask(testModel, getActivity().getApplication(), dataCollector).execute();
    }

    private void startTest() {
        runState = RunState.START;

        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.sway_instruction);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();

                mediaPlayer = MediaPlayer.create(getActivity(), R.raw.start);
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        calibrate();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                });
            }
        });
        activateLockAndHoldScreen();
    }

    //we start test a bit earlier to adjust gravity filter
    private void calibrate() {
        //vibrate here to avoid vibrator affecting sensor...
        vibrator.single();

        dataCollector = new CalibratedPstDataCollector(getActivity().getApplicationContext());
        dataCollector.start();

        updateTestStatusLabel(R.string.test_calibrate);

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                runTest();
            }
        }, CALIBRATE_TIME_INTERVAL);
    }

    private void runTest() {
        runState = RunState.RUN;

        dataCollector.stopCalibrating();

        updateTestStatusLabel(R.string.test_run);
        startTime = System.nanoTime();

        handler.postDelayed(new TimeUpdateTask(), ELAPSED_TIME_UPDATE_INTERVAL);
    }

    private void finishTest() {
        runState = RunState.FINISH;
        dataCollector.stop();
        getActivity().invalidateOptionsMenu();

        vibrator.double2();

        updateRemainingTime(0);
        updateTestStatusLabel(R.string.test_finish);

        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.stop);
        mediaPlayer.start();

        releaseScreen();

        dataCollector.calculate();

        updateTestMetrics();
    }

    private void updateTestMetrics() {
        testModel.getWidgetAttrs().get(JERK.name()).putString(VALUE.name(), formatPstMeasurement(dataCollector
                .getScore()));
        testModel.getWidgetAttrs().get(RMS.name()).putString(VALUE.name(), formatPstRmsMeasurement(dataCollector.getRMS
                ()));
        testModel.getWidgetAttrs().get(AREA.name()).putString(VALUE.name(), formatPstMeasurement(dataCollector
                .getArea()));
    }

    private void updateRemainingTime(long elapsedTime) {
        testModel.getWidgetAttrs().get(TIME.name()).putString(VALUE.name(), Long.toString(elapsedTime));
        widgetsAdapter.notifyDataSetChanged();
    }

    private static final class PstTestPersistTask extends AbsTestPersistTask {

        private final PstDataCollector dataCollector;

        public PstTestPersistTask(TestModel testModel, Application application, PstDataCollector dataCollector) {
            super(testModel, application);
            this.dataCollector = dataCollector;
        }

        @Override
        protected String getTestType() {
            return TestConstants.TestType.PST.name();
        }

        @Override
        protected String buildRawData() {
            return dataCollector.buildRawData() + getDeviceInfo();
        }

        @Override
        protected Double buildScore() {
            return dataCollector.getScore();
        }
    }

    private class TimeUpdateTask implements Runnable {
        @Override
        public void run() {
            long elapsedTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            long leftTime = DEFAULT_MEASURE_TIME - elapsedTime;
            if (leftTime > 0) {
                if (runState == RunState.RUN) {
                    updateRemainingTime(leftTime);
                    handler.postDelayed(this, ELAPSED_TIME_UPDATE_INTERVAL);
                }
            } else {
                finishTest();
            }
        }
    }

}
