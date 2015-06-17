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
import org.kineticsfoundation.test.sensor.MotionDetector;
import org.kineticsfoundation.util.Format;

import java.util.concurrent.TimeUnit;

import static org.kineticsfoundation.test.TestModel.Attr.TIME;
import static org.kineticsfoundation.widget.WidgetProperty.EXTRA;
import static org.kineticsfoundation.widget.WidgetProperty.VALUE;

/**
 * TUG test specialized fragment
 * Created by akaverin on 7/12/13.
 */
public class TugTestFragment extends AbsTestFragment implements MotionDetector.MotionDetectorListener {

    private static final int ELAPSED_TIME_UPDATE_INTERVAL = 100;
    private static final int TEST_RUN_DELAY = 2;
    private static final double RUN_THRESHOLD = 3;
    private MotionDetector motionDetector;
    private long startTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getActionBar().setTitle("TUG Test");
    }

    @Override
    public void onPause() {
        //make sure to call prior to super
        if (runState == RunState.RUN) {
            motionDetector.stop();
        }
        super.onPause();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(startTime != 0 && runState == RunState.FINISH);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onMoveStop(long timestamp) {
        finishTest(timestamp);
    }

    @Override
    protected void onPersistTest() {
        new TugTestPersistTask(testModel, getActivity().getApplication()).execute();
    }

    @Override
    protected TestModel buildModel(Cursor data) {
        startTest();
        return TestModel.buildTugModel(getActivity(), data);
    }

    private void startTest() {
        runState = RunState.START;

        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.tug_instruction);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                runTest();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
        activateLockAndHoldScreen();
    }

    private void runTest() {
        runState = RunState.RUN;
        vibrator.single();

        updateTestStatusLabel(R.string.test_run);
        motionDetector = new MotionDetector(getActivity(), this);
        startTime = System.nanoTime();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (runState == RunState.RUN) {
                    motionDetector.start();
                }
            }
        }, TimeUnit.SECONDS.toMillis(TEST_RUN_DELAY));

        handler.postDelayed(new TimeUpdateTask(), ELAPSED_TIME_UPDATE_INTERVAL);
    }

    private void finishTest(long timestamp) {
        runState = RunState.FINISH;
        motionDetector.stop();
        vibrator.double2();

        double elapsedTime = (timestamp - TimeUnit.NANOSECONDS.toMillis(startTime)) / 1000F;
        updateElapsedTime(elapsedTime, true);
        updateTestStatusLabel(R.string.test_finish);

        mediaPlayer = MediaPlayer.create(getActivity(), R.raw.stop);
        mediaPlayer.start();

        releaseScreen();
    }

    private void updateElapsedTime(double elapsedTime, boolean checkRange) {
        if (checkRange) {
            if (elapsedTime < RUN_THRESHOLD) {
                elapsedTime = startTime = 0;
            }
            getActivity().invalidateOptionsMenu();
        }
        Bundle widgetBundle = testModel.getWidgetAttrs().get(TIME.name());
        widgetBundle.putString(VALUE.name(), Format.formatScoreOutput(elapsedTime));

        //save original Double value
        Bundle extra = new Bundle();
        extra.putDouble(VALUE.name(), elapsedTime);
        widgetBundle.putBundle(EXTRA.name(), extra);
        widgetsAdapter.notifyDataSetChanged();
    }

    private static final class TugTestPersistTask extends AbsTestPersistTask {

        public TugTestPersistTask(TestModel testModel, Application application) {
            super(testModel, application);
        }

        @Override
        protected String getTestType() {
            return TestConstants.TestType.TUG.name();
        }

        @Override
        protected Double buildScore() {
            return testModel.getWidgetAttrs().get(TIME.name()).getBundle(EXTRA.name()).getDouble(VALUE.name());
        }

        @Override
        protected String buildRawData() {
            return getDeviceInfo();
        }
    }

    /**
     * Times task
     */
    private final class TimeUpdateTask implements Runnable {

        @Override
        public void run() {
            if (runState == RunState.RUN) {
                double elapsedTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) / 1000F;
                updateElapsedTime(elapsedTime, false);

                handler.postDelayed(this, ELAPSED_TIME_UPDATE_INTERVAL);
            }
        }
    }
}
