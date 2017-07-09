package com.aiyaapp.aiya.track;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.aiyaapp.aiya.R;

/**
 * Created by aiya on 2017/7/9.
 */

public class TrackActivity extends AppCompatActivity{

    private TrackView mTrackView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        mTrackView=(TrackView) findViewById(R.id.mTrack);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTrackView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTrackView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTrackView.onDestroy();
    }
}
