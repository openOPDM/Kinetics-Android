package org.kineticsfoundation.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import com.google.common.base.Preconditions;
import org.kineticsfoundation.R;
import org.kineticsfoundation.dao.CacheContract;
import org.kineticsfoundation.test.TestConstants;

/**
 * Video player activity
 * Created by akaverin on 10/18/13.
 */
public class VideoActivity extends Activity {

    private VideoView videoView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);

        TestConstants.TestType testType = (TestConstants.TestType) getIntent().getSerializableExtra(CacheContract
                .Columns.TYPE);

        int videoId = 0;
        switch (testType) {
            case TUG:
                videoId = R.raw.tug_video;
                break;
            case PST:
                videoId = R.raw.pst_video;
                break;

            default:
                Preconditions.checkArgument(false, "Wrong input!");
        }
        getActionBar().setTitle(getString(R.string.instruction_title, testType.name()));

        videoView = (VideoView) findViewById(R.id.video);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + videoId));
        videoView.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
    }
}