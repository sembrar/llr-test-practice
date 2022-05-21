package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ghsembrar.llrtestpreparation.model.ModelBase;
import com.ghsembrar.llrtestpreparation.model.ModelLearn;
import com.ghsembrar.llrtestpreparation.model.ModelTest;

public class LearnAndTestActivity extends AppCompatActivity {

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "L&T";

    private GestureDetectorCompat gestureDetectorCompat;

    enum MODE {
        READ,
        PRACTICE,
        TEST_IN_PROGRESS,
        TEST_FINISHED
    }

    MODE mode;
    ModelBase ltModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_and_test);

        // read intent data
        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.INTENT_EXTRA_KEY_SUBJECT_INDEX)) {
            int subject_index = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_SUBJECT_INDEX, 0);
            set_model_and_mode_for_learn(subject_index);

        } else  if (intent.hasExtra(MainActivity.INTENT_EXTRA_KEY_TEST_OLD_OR_NEW)) {
            int test_type = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_TEST_OLD_OR_NEW, MainActivity.TEST_TYPE_NEW_TEST);
            set_model_and_mode_for_test(test_type);

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

        // bind buttons to functions
        findViewById(R.id.lt_button_previous).setOnClickListener(v -> clicked_button_previous());
        findViewById(R.id.lt_button_next).setOnClickListener(v -> clicked_button_next());
        findViewById(R.id.lt_button_check_or_finish).setOnClickListener(v -> clicked_button_check_or_finish());

        // set views (one time settings / texts)
        show_or_hide_views_based_on_mode_and_settings();
        set_title_and_detail_according_to_mode_and_model();

        // create gesture detector compat
        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());
    }

    void set_model_and_mode_for_learn(int subject_index) {
        ltModel = new ModelLearn(this, subject_index);
        mode = MODE.READ;  // todo get mode (read/practice) from sharedPrefs
    }

    void set_model_and_mode_for_test(int test_type) {
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "set_model_and_mode_for_test: test_type " + test_type);

        ltModel = new ModelTest(this);

        boolean is_load_old_test_successful = ((ModelTest) ltModel).load_test_data();
        // loading old test is required for both show old test and set-up new test
        // in set-up new test, if old unfinished test exists, user should be asked if they
        // want to continue that instead, todo this behavior should be allowed to be disabled in Settings

        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "set_model_and_mode_for_test: old_test_load_successful:" + is_load_old_test_successful);

        if (test_type == MainActivity.TEST_TYPE_VIEW_OR_CONTINUE_OLD_TEST) {

            if (!is_load_old_test_successful) {  // old test is clicked, but it couldn't be read from memory
                Toast.makeText(this, "No old test exists", Toast.LENGTH_LONG).show();
                finish();

            } else {  // old test is clicked, and it is successfully read from memory
                if (((ModelTest) ltModel).is_test_in_progress()) {
                    mode = MODE.TEST_IN_PROGRESS;
                } else {
                    mode = MODE.TEST_FINISHED;
                }
            }

        } else {  // new test is clicked

            boolean an_old_unfinished_test_exists_and_user_wants_to_continue = false;

            if (is_load_old_test_successful && ((ModelTest) ltModel).is_test_in_progress()) {
                // new test is clicked, but an old test is in progress
                // todo ask if the user wants to continue
            }

            if (!an_old_unfinished_test_exists_and_user_wants_to_continue) {
                ((ModelTest) ltModel).set_up_new_test();
            }
            // note that for the other case (when above if condition fails), the data is already loaded

            mode = MODE.TEST_IN_PROGRESS;
        }
    }

    void show_or_hide_views_based_on_mode_and_settings() {
        // todo changes based on settings
        switch (mode) {

            case READ:
            case TEST_FINISHED:
                findViewById(R.id.lt_textView_timer).setVisibility(View.GONE);
                findViewById(R.id.lt_button_check_or_finish).setVisibility(View.GONE);
                break;
            case PRACTICE:
                findViewById(R.id.lt_textView_timer).setVisibility(View.GONE);
                findViewById(R.id.lt_button_check_or_finish).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.lt_button_check_or_finish)).setText(R.string.button_check);
                break;
            case TEST_IN_PROGRESS:
                findViewById(R.id.lt_textView_timer).setVisibility(View.VISIBLE);
                findViewById(R.id.lt_button_check_or_finish).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.lt_button_check_or_finish)).setText(R.string.button_finish);
                break;
        }
    }

    void set_title_and_detail_according_to_mode_and_model() {
        TextView textViewTitle = findViewById(R.id.lt_textView_title);
        TextView textViewDetail = findViewById(R.id.lt_textView_detail);

        switch (mode) {

            case READ:
            case PRACTICE:
                ModelLearn modelLearn = (ModelLearn) ltModel;
                String subject_name;
                try {
                    subject_name = getResources().getStringArray(R.array.subject_names)[modelLearn.get_subject_index()];
                } catch (Exception ignored) {  // this won't happen
                    subject_name = getResources().getString(R.string.no_string);
                }
                textViewTitle.setText(subject_name);
                textViewDetail.setText(getResources().getString(R.string.heading_num_questions, modelLearn.get_num_questions()));
                break;
            case TEST_IN_PROGRESS:
                textViewTitle.setText(R.string.heading_test_in_progress);
                textViewDetail.setText("");  // nothing to show here
                break;
            case TEST_FINISHED:
                ModelTest modelTest = (ModelTest) ltModel;
                textViewTitle.setText(R.string.heading_test_finished);
                textViewDetail.setText(getResources().getString(R.string.heading_score, modelTest.get_score(), modelTest.get_num_questions()));
                // todo set color based on pass score which means remove color for other times
                break;
        }
    }

    void clicked_button_previous() {
        // todo
    }

    void clicked_button_next() {
        // todo
    }

    void clicked_button_check_or_finish() {
        // todo
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
