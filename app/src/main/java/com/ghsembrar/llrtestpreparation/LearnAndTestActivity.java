package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class LearnAndTestActivity extends AppCompatActivity {

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "L&T";

    private GestureDetectorCompat gestureDetectorCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_and_test);

        // create gesture detector compat
        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
    }

    // the following function needs to be overridden for the gesture detector to work
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    // simple gesture listener required to change questions on fling gesture
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        // the following function needs to be overridden and make it return true as it is the
        // starting point of all gestures, otherwise, complex gestures don't get detected
        @Override
        public boolean onDown(MotionEvent e) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onDown: " + e.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onFling: " + e1.toString() + e2.toString());
            return true;
        }
    }
}
