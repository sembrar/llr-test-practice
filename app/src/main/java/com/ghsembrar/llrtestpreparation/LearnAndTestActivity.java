package com.ghsembrar.llrtestpreparation;

import static java.lang.Math.abs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ghsembrar.llrtestpreparation.model.ModelBase;
import com.ghsembrar.llrtestpreparation.model.ModelLearn;
import com.ghsembrar.llrtestpreparation.model.ModelTest;

public class LearnAndTestActivity extends AppCompatActivity {

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "L&T";

    private static final String KEY_SHARED_PREF_LEARN_MODE_IS_READ = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "read_mode";
    // true/false is saved in SharedPrefs with above key; true meaning read mode, and false meaning practice mode

    private GestureDetectorCompat gestureDetectorCompat;

    enum MODE {
        READ,
        PRACTICE,
        TEST_IN_PROGRESS,
        TEST_FINISHED
    }

    MODE mode;
    ModelBase ltModel = null;

    CountDownTimer countDownTimer;

    // for easier access
    private static final int[] radioButtonIDs = {
            R.id.lt_radioButton_option0, R.id.lt_radioButton_option1,
            R.id.lt_radioButton_option2, R.id.lt_radioButton_option3
    };

    // even though settings can be read anywhere, the following data members save from multiple calls
    // they are set in onResume
    private boolean use_check_button_in_practice;
    private boolean use_swipe_for_traversal;

    // the following are used to set checked status of those option menu items
    // the 'l' in the name stands for 'learn' which means that they appear when the mode is either read/practice
    MenuItem menu_item_l_read_mode;
    MenuItem menu_item_l_practice_mode;  // this and the above are updated in onCreateOptionsMenu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_and_test);

        Log.i(TAG, "onCreate");

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
        // navigation buttons
        findViewById(R.id.lt_button_previous).setOnClickListener(v -> clicked_button_previous());
        findViewById(R.id.lt_button_next).setOnClickListener(v -> clicked_button_next());
        findViewById(R.id.lt_button_check_or_finish).setOnClickListener(v -> clicked_button_check_or_finish());
        // radio buttons
        findViewById(R.id.lt_radioButton_option0).setOnClickListener(v -> clicked_radio_button(0));
        findViewById(R.id.lt_radioButton_option1).setOnClickListener(v -> clicked_radio_button(1));
        findViewById(R.id.lt_radioButton_option2).setOnClickListener(v -> clicked_radio_button(2));
        findViewById(R.id.lt_radioButton_option3).setOnClickListener(v -> clicked_radio_button(3));

        // set views (one time settings / texts)
        show_or_hide_views_based_on_mode_and_settings();
        set_title_and_detail_according_to_mode_and_model();

        // create gesture detector compat
        gestureDetectorCompat = new GestureDetectorCompat(this, new MyGestureListener());

        // set current question for the first time
        set_current_question();
    }

    void set_model_and_mode_for_learn(int subject_index) {
        ltModel = new ModelLearn(this, subject_index);

        // get mode (read/practice) from sharedPrefs
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        boolean is_read_mode = sharedPreferences.getBoolean(KEY_SHARED_PREF_LEARN_MODE_IS_READ, true);
        if (is_read_mode) mode = MODE.READ;
        else mode = MODE.PRACTICE;
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
                findViewById(R.id.lt_button_check_or_finish).setVisibility(use_check_button_in_practice ? View.VISIBLE: View.GONE);
                break;
            case TEST_IN_PROGRESS:
                findViewById(R.id.lt_textView_timer).setVisibility(View.VISIBLE);
                findViewById(R.id.lt_button_check_or_finish).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.lt_button_check_or_finish)).setText(R.string.button_finish);
                break;
        }

        findViewById(R.id.lt_button_previous).setVisibility(
                SettingsActivity.get_setting_use_buttons_for_traversal(this) ? View.VISIBLE : View.GONE);
        findViewById(R.id.lt_button_next).setVisibility(
                SettingsActivity.get_setting_use_buttons_for_traversal(this) ? View.VISIBLE : View.GONE);
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

    void set_current_question() {
        // set question
        ((TextView) findViewById(R.id.lt_textView_question)).setText(ltModel.get_question_string());

        // set image if it exists, else hide image view
        int img_res_id = ltModel.get_accompanying_image_res_id();
        ImageView img_view = findViewById(R.id.lt_imageView_accompanying_image);
        if (img_res_id == 0) {
            img_view.setVisibility(View.GONE);
        } else {
            img_view.setVisibility(View.VISIBLE);
            img_view.setImageResource(img_res_id);
        }

        // set choices
        for (int i = 0; i < 4; i++) {
            ((RadioButton) findViewById(radioButtonIDs[i])).setText(ltModel.get_option_string_res_id(i));
        }

        // set click-ability of option buttons
        set_click_ability_of_option_buttons_based_on_mode_and_model();

        // mark user choice and/or correct choice based on mode and model
        mark_user_and_or_correct_answer_option_based_on_mode_and_model();
    }

    void set_click_ability_of_option_buttons_based_on_mode_and_model() {

        boolean clickable = false;

        switch (mode) {

            case READ:  // not clickable
            case TEST_FINISHED:  // not clickable
                clickable = false;
                break;
            case PRACTICE:  // not clickable if answer is checked
                clickable = ltModel.get_user_answer() == ModelBase.NO_ANSWER_CHOSEN_YET;  // answer needs yet to be set, so clickable
                break;
            case TEST_IN_PROGRESS:  // clickable
                clickable = true;
                break;
        }

        if (CONSTANTS.ALLOW_DEBUG)
            Log.i(TAG, "set_click_ability_of_option_buttons_based_on_mode_and_model: Clickable:" + clickable);

        for (int radio_button_id : radioButtonIDs) {
            findViewById(radio_button_id).setClickable(clickable);
        }
    }

    void mark_user_and_or_correct_answer_option_based_on_mode_and_model() {
        int user_answer = ltModel.get_user_answer();
        int correct_answer = ltModel.get_correct_answer_option_index();


        // radio button selection:

        switch (mode) {

            case READ:  // mark the correct answer
                ((RadioButton) findViewById(radioButtonIDs[correct_answer])).setChecked(true);
                break;

            case PRACTICE:  // mark if user has previously selected an answer, else clear response
            case TEST_IN_PROGRESS:
            case TEST_FINISHED:
                if (user_answer >= 0 && user_answer < radioButtonIDs.length) {
                    ((RadioButton) findViewById(radioButtonIDs[user_answer])).setChecked(true);
                } else {
                    // user answer is NO_ANSWER_CHOSEN_YET for finished test or
                    // one of NO_ANSWER_CHOSEN_YET or EMPTY_ANSWER_CHOSEN for practice
                    // in all of those cases, no radio button should be in checked state
                    ((RadioGroup) findViewById(R.id.lt_radioGroup_choices)).clearCheck();
                }
                break;

        }  //  todo can the color of the circle in radiobutton be changed


        // color:

        final int bg_color_correct_option = getResources().getColor(R.color.color_correct_choice);
        final int bg_color_wrong_option = getResources().getColor(R.color.color_wrong_choice);
        final int bg_color_alpha_only = getResources().getColor(R.color.color_alpha_only);

        switch (mode) {

            case READ:
                // only correct answer is colored
                user_answer = correct_answer;  // so it will be the only colored option
                break;

            case PRACTICE:
                // if answer is not chosen yet, no color
                if (user_answer == ModelBase.NO_ANSWER_CHOSEN_YET) correct_answer = -1;  // no color
                // else they will be colored as usual
                // note that if the user answer is empty answer chosen, correct answer will be colored
                break;

            case TEST_IN_PROGRESS:
                user_answer = -1;  // for just alpha background color
                correct_answer = -1;  // same reason as above
                break;

            case TEST_FINISHED:
                // same as practice, but correct answer needs to be colored regardless of user answer
                break;
        }

        // correct option gets correct option background color
        // any other user option gets wrong option background color
        // all other options get alpha background color
        for (int i = 0; i < radioButtonIDs.length; i++) {
            int color;
            if (i == correct_answer) color = bg_color_correct_option;
            else if (i == user_answer) color = bg_color_wrong_option;
            else color = bg_color_alpha_only;
            findViewById(radioButtonIDs[i]).setBackgroundColor(color);
        }
    }

    void clicked_radio_button(int option_index) {
        // this function is called when an option is chosen, i.e. one of the radio buttons is clicked
        // this can happen in practice mode (when answer is not checked yet) and test_in_progress mode

        switch (mode) {

            case PRACTICE:
                if (use_check_button_in_practice) {
                    // do nothing, as scrolling to next question, need not save this answer
                } else {
                    // check the answer and re-set the question
                    ltModel.set_user_answer(option_index);
                    set_current_question();  // this will also make the buttons not-clickable
                }
                break;
            case TEST_IN_PROGRESS:
                ltModel.set_user_answer(option_index);  // the answer is saved in a test
                break;
            case TEST_FINISHED:
            case READ:
                // should not reach here
                if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clicked_radio_button: In mode:" + mode);
                return;
        }

        if (use_check_button_in_practice) {
            // don't
        }
    }

    void clicked_button_previous() {
        boolean prev_question_exists = ltModel.set_to_previous_question();
        if (!prev_question_exists) {
            Toast.makeText(this, "Already in first question", Toast.LENGTH_SHORT).show();
            return;
        }
        set_current_question();
    }

    void clicked_button_next() {
        boolean next_question_exists = ltModel.set_to_next_question();
        if (!next_question_exists) {
            Toast.makeText(this, "Already in last question", Toast.LENGTH_SHORT).show();
            return;
        }
        set_current_question();
    }

    void clicked_button_check_or_finish() {
        switch (mode) {

            case READ:
            case TEST_FINISHED:
                if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clicked_button_check_or_finish: in mode read/test_finished");
                return;

            case PRACTICE:
                if (ltModel.get_user_answer() != ModelBase.NO_ANSWER_CHOSEN_YET) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clicked_button_check_or_finish: for already checked question");
                    return;
                }

                // save the user answer
                ltModel.set_user_answer(ModelBase.EMPTY_ANSWER_CHOSEN);  // if user selected a radio button it will be updated below
                for (int i = 0; i < radioButtonIDs.length; i++) {
                    if (((RadioButton) findViewById(radioButtonIDs[i])).isChecked()) {
                        ltModel.set_user_answer(i);
                        break;
                    }
                }

                break;

            case TEST_IN_PROGRESS:
                if (countDownTimer != null) countDownTimer.cancel();
                ((ModelTest) ltModel).finish_test();
                mode = MODE.TEST_FINISHED;
                show_or_hide_views_based_on_mode_and_settings();
                set_title_and_detail_according_to_mode_and_model();
                break;
        }

        set_current_question();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        use_check_button_in_practice = SettingsActivity.get_setting_use_check_button_in_practice_mode(this);
        use_swipe_for_traversal = SettingsActivity.get_setting_use_swipe_for_traversal(this);
        show_or_hide_views_based_on_mode_and_settings();

        if (mode == MODE.TEST_IN_PROGRESS) {
            countDownTimer = new CountDownTimer(((ModelTest) ltModel).get_num_seconds_remaining() * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    ModelTest modelTest = (ModelTest) ltModel;

                    int num_seconds_remaining = modelTest.get_num_seconds_remaining();
                    modelTest.decrement_num_seconds_remaining();
                    // decrement after reading, otherwise, it starts at one less, and at the end, 0 is shown for 1 second

                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onTick: " + num_seconds_remaining);

                    int minutes = num_seconds_remaining / 60;
                    int seconds = num_seconds_remaining - minutes * 60;
                    ((TextView) findViewById(R.id.lt_textView_timer)).setText(getResources().getString(R.string.time_remaining, minutes, seconds));
                }

                @Override
                public void onFinish() {
                    clicked_button_check_or_finish();
                }
            };
            countDownTimer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (countDownTimer != null) countDownTimer.cancel();

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (mode) {

            case READ:
            case PRACTICE:
                ((ModelLearn) ltModel).save_practice_answers();
                editor.putBoolean(KEY_SHARED_PREF_LEARN_MODE_IS_READ, mode == MODE.READ);
                break;
            case TEST_IN_PROGRESS:
            case TEST_FINISHED:
                ((ModelTest) ltModel).save_test_data();
                break;
        }

        editor.apply();
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
            // check setting before flinging
            if (!use_swipe_for_traversal) return true;

            // if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onFling: " + e1.toString() + e2.toString());
            float deltaX = e1.getX() - e2.getX();
            float deltaY = e1.getY() - e2.getY();
            float biggerDelta = (abs(deltaX) > abs(deltaY)) ? deltaX : deltaY;
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onFling: DeltaX:" + deltaX + " DeltaY:" + deltaY + " Bigger:" + biggerDelta);

            if (biggerDelta > 0) {
                clicked_button_next();
            } else {
                clicked_button_previous();
            }

            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onCreateOptionsMenu: mode:" + mode);

        MenuInflater inflater = getMenuInflater();

        switch (mode) {

            case READ:
            case PRACTICE:
                inflater.inflate(R.menu.lt_app_bar_options_menu_during_learn, menu);
                menu_item_l_read_mode = menu.findItem(R.id.lt_learn_menu_item_read_mode);
                menu_item_l_practice_mode = menu.findItem(R.id.lt_learn_menu_item_practice_mode);
                // set their checked status initially
                menu_item_l_read_mode.setChecked(mode == MODE.READ);
                menu_item_l_practice_mode.setChecked(mode == MODE.PRACTICE);
                break;
            case TEST_IN_PROGRESS:
            case TEST_FINISHED:
                inflater.inflate(R.menu.lt_app_bar_options_menu_during_test, menu);
                menu_item_l_read_mode = menu_item_l_practice_mode = null;
                break;
        }

        return true;
    }

    // note related to the function onPrepareOptionsMenu which can be overridden to update menu at run time:
    /*
    * On Android 2.3.x and lower, the system calls onPrepareOptionsMenu() each time the user opens
    *  the options menu (presses the Menu button).
    * On Android 3.0 and higher, the options menu is considered to always be open when
    *  menu items are presented in the app bar. When an event occurs and you want to perform a
    *  menu update, you must call invalidateOptionsMenu() to request that the system call onPrepareOptionsMenu()
    * */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onOptionsItemSelected: mode:" + mode);

        int item_id = item.getItemId();

        switch (mode) {

            case READ:
            case PRACTICE:
                if (item_id == R.id.lt_learn_menu_item_read_mode) {
                    if (mode != MODE.READ) {
                        mode = MODE.READ;
                        show_or_hide_views_based_on_mode_and_settings();
                        set_current_question();
                        menu_item_l_read_mode.setChecked(mode == MODE.READ);
                        menu_item_l_practice_mode.setChecked(mode == MODE.PRACTICE);
                    }
                } else if (item_id == R.id.lt_learn_menu_item_practice_mode) {
                    if (mode != MODE.PRACTICE) {
                        mode = MODE.PRACTICE;
                        show_or_hide_views_based_on_mode_and_settings();
                        set_current_question();
                        menu_item_l_read_mode.setChecked(mode == MODE.READ);
                        menu_item_l_practice_mode.setChecked(mode == MODE.PRACTICE);
                    }
                } else if (item_id == R.id.lt_learn_menu_item_settings) {
                    start_settings_activity();
                } else if (item_id == R.id.lt_learn_menu_item_about) {
                    start_about_activity();
                } else {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onOptionsItemSelected: Unknown menu item id:" + item_id);
                    return false;  // needs to return false, else, the app bar back button that leads to parentActivity is not working
                }

                break;

            case TEST_IN_PROGRESS:
            case TEST_FINISHED:

                if (item_id == R.id.lt_test_menu_item_settings) {
                    start_settings_activity();
                } else if (item_id == R.id.lt_test_menu_item_about) {
                    start_about_activity();
                } else {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onOptionsItemSelected: Unknown menu item id:" + item_id);
                    return false;
                }
                break;
        }

        return true;
    }

    private void start_settings_activity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void start_about_activity() {
        // todo
    }
}
