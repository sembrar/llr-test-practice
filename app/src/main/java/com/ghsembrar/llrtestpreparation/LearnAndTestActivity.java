package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.ghsembrar.llrtestpreparation.model.ModelBase;
import com.ghsembrar.llrtestpreparation.model.ModelLearn;
import com.ghsembrar.llrtestpreparation.model.ModelTest;

public class LearnAndTestActivity extends AppCompatActivity {

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "L&T";

    private GestureDetectorCompat gestureDetectorCompat;

    ModelBase ltModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_and_test);

        // read intent data
        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.INTENT_EXTRA_KEY_SUBJECT_INDEX)) {
            int subject_index = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_SUBJECT_INDEX, 0);
            ltModel = new ModelLearn(this, subject_index);
        } else  if (intent.hasExtra(MainActivity.INTENT_EXTRA_KEY_TEST_OLD_OR_NEW)) {
            int test_type = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_TEST_OLD_OR_NEW, MainActivity.TEST_TYPE_NEW_TEST);
            ltModel = new ModelTest(this);
        } else {
            // no intent data
            // this can happen if the activity is restarted
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onCreate: No intent data");
            if (ltModel == null) {
                if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onCreate: model is null");
                Toast.makeText(this, "Err: Please reopen", Toast.LENGTH_LONG).show();
                finish();
            }
        }

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
