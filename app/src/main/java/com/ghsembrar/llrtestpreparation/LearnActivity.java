package com.ghsembrar.llrtestpreparation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

public class LearnActivity extends AppCompatActivity {

    private Resources resources;  // this is updated in onResume with required language resources

    // the following are saved to shared prefs
    private boolean is_read_mode = true;  // if false, it's practice mode
    private int subject_index = -1;  // 0 based subject index (this is used to access questions, images and answers)
    private int currentQuestionIndex = 0;

    // the following are not saved to shared prefs
    private int currentCorrectAnswer = -1;  // this is updated whenever a question is loaded
    private int numQuestions = 0;  // this is updated in onResume

    // the following are saved to files as there are many values
    private int[] practiceAnswers;

    // keys for the variables to be saved in shared prefs
    private static final String SHARED_PREF_KEY_IS_READ_MODE = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "is_read_mode";
    private static final String SHARED_PREF_KEY_SUBJECT_INDEX = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "subject_index";
    private static final String SHARED_PREF_KEY_CURRENT_QUESTION_INDEX_PREFIX = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "current_question_index_";  // add subject index to this

    // some utility variables for easier access
    private static final int[] radioButtonIDs = {
            R.id.learn_radioButton_choice1, R.id.learn_radioButton_choice2,
            R.id.learn_radioButton_choice3, R.id.learn_radioButton_choice4
    };
    private static final String[] radioButtonOptionSuffixes = {"a", "b", "c", "d"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // read data from intent
        Intent intent = getIntent();
        subject_index = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_SUBJECT_INDEX, -1);

        // if data set by intent is wrong, read it from shared prefs
        // this can happen if android force re-started this activity

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);  // declaring outside of the following if-block for use later on

        if (is_activity_start_data_not_valid()) {
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "onCreate: Activity start data from intent is bad. Reading from prefs."); }

            subject_index = sharedPreferences.getInt(SHARED_PREF_KEY_SUBJECT_INDEX, -1);
        }

        if (is_activity_start_data_not_valid()) {
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "onCreate: Activity start data from prefs is bad. Stopping activity."); }

            finish();  // todo may be show a Toast for the user that an error occurred
            return;
        }

        // show activity start data
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, String.format("onCreate: subject_index: %d", subject_index)); }

        // load data of this particular activity that is saved in its SharedPrefs
        is_read_mode = sharedPreferences.getBoolean(SHARED_PREF_KEY_IS_READ_MODE, true);
        // note subject index is already read above either from intent or from sharedPrefs
        currentQuestionIndex = sharedPreferences.getInt(SHARED_PREF_KEY_CURRENT_QUESTION_INDEX_PREFIX + subject_index, 0);

        if (CONSTANTS.ALLOW_DEBUG) {
            Log.i(CONSTANTS.LOG_TAG, "onCreate: is_read_mode: " + is_read_mode);
            Log.i(CONSTANTS.LOG_TAG, "onCreate: currentQuestionIndex: " + currentQuestionIndex);
        }

        // set onClickListeners for buttons
        findViewById(R.id.lean_button_previous).setOnClickListener(this::clickedPrev);
        findViewById(R.id.learn_button_next).setOnClickListener(this::clickedNext);
        findViewById(R.id.learn_button_check).setOnClickListener(this::clickedCheck);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateResourcesVariable();  // this helps load resources of different language than system's

        numQuestions = resources.getIntArray(R.array.num_questions)[subject_index];
        practiceAnswers = new int[numQuestions];
        Arrays.fill(practiceAnswers, -1);

        setActivityAccordingToMode();  // readMode => choices not clickable etc.  // also sets current question
    }

    @Override
    protected void onPause() {
        super.onPause();

        // save data to shared prefs
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(SHARED_PREF_KEY_IS_READ_MODE, is_read_mode);
        editor.putInt(SHARED_PREF_KEY_SUBJECT_INDEX, subject_index);
        editor.putInt(SHARED_PREF_KEY_CURRENT_QUESTION_INDEX_PREFIX + subject_index, currentQuestionIndex);

        editor.apply();
    }

    private boolean is_activity_start_data_not_valid() {
        if (subject_index < 0 || subject_index >= getResources().getStringArray(R.array.subject_names).length) return true;
        return false;
    }

    private void updateResourcesVariable() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFS_FILE_SETTINGS, Context.MODE_PRIVATE);

        boolean use_system_language = sharedPreferences.getBoolean(SettingsActivity.SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE, true);
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, String.format("updateResourcesVariable: Use system language: %b", use_system_language)); }

        if (use_system_language) {
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "updateResourcesVariable: Using default resources"); }
            resources = getResources();
            return;
        }

        // not use system language => use chosen language
        String chosen_language = sharedPreferences.getString(SettingsActivity.SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG, null);
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, String.format("updateResourcesVariable: Chosen language: %s", chosen_language)); }

        if (chosen_language == null) {  // this shouldn't happen
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "updateResourcesVariable: Using default resources"); }
            resources = getResources();
            return;
        }

        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "updateResourcesVariable: Using chosen language resources"); }
        Locale chosenLocale = new Locale(chosen_language);
        resources = getLocalizedResources(this, chosenLocale);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu_learn_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.learnAct_menu_item_settings:
                startSettingsActivity();
                return true;
            case R.id.learnAct_menu_item_about:
                // startAboutActivity();  // todo
                return true;
            case R.id.learnAct_menu_item_read_mode:
                if (!is_read_mode) {
                    is_read_mode = true;
                    setActivityAccordingToMode();
                } else {
                    if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "onOptionsItemSelected: Already in readMode"); }
                }
                return true;
            case R.id.learnAct_menu_item_practice_mode:
                if (is_read_mode) {
                    is_read_mode = false;
                    setActivityAccordingToMode();
                } else {
                    if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "onOptionsItemSelected: Already in practiceMode"); }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // the following function is from stack overflow
    // todo place link to the page
    @NonNull
    private Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration = new Configuration(configuration);
        configuration.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(configuration);
        return localizedContext.getResources();
    }

    private void setCurrentQuestion() {
        // displays current question in activity
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, String.format("setCurrentQuestion: SubjectIndex,QuestionIndex: %d,%d", subject_index, currentQuestionIndex)); }

        final String packageName = getPackageName();

        // set question
        int quesResID = resources.getIdentifier(String.format("s%d_%dq", subject_index, currentQuestionIndex), "string", packageName);
        ((TextView) findViewById(R.id.learn_textView_question)).setText(
                quesResID == 0 ?
                        resources.getString(R.string.no_string) :
                        resources.getString(quesResID, currentQuestionIndex + 1)
        );

        // set options
        for (int i = 0; i < radioButtonIDs.length; i++) {
            int resID = resources.getIdentifier(String.format("s%d_%d%s", subject_index, currentQuestionIndex, radioButtonOptionSuffixes[i]), "string", packageName);
            if (resID == 0) resID = R.string.no_string;
            ((RadioButton) findViewById(radioButtonIDs[i])).setText(resID);
        }

        // set image if exists else hide image view
        String imageName = String.format("i%d_%d", subject_index, currentQuestionIndex);  // note: don't add extension (.jpg) in name, may not be found
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, String.format("setCurrentQuestion: ImageName = %s", imageName)); }
        int imageResID = resources.getIdentifier(imageName, "drawable", packageName);
        if (imageResID == 0) {
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "setCurrentQuestion: Image doesn't exist"); }
            findViewById(R.id.learn_imageView_accompanyingImage).setVisibility(View.GONE);
        }
        else {
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "setCurrentQuestion: Image exists"); }
            ImageView imageView = findViewById(R.id.learn_imageView_accompanyingImage);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(imageResID);
        }

        // clear existing response, also clear any background colors
        clearResponse();

        // get answer, mark it in read mode
        int ansResId = resources.getIdentifier(String.format("a%d_%d", subject_index, currentQuestionIndex), "integer", packageName);
        if (ansResId != 0) {
            currentCorrectAnswer = resources.getInteger(ansResId);
            if (is_read_mode) {
                ((RadioButton) findViewById(radioButtonIDs[currentCorrectAnswer])).setChecked(true);
            } else {

            }
        } else {
            // todo should(also will) never happen, so handle it properly
            currentCorrectAnswer = -1;
        }

        if (is_read_mode || practiceAnswers[currentQuestionIndex] != -1) {
            // note: in the condition, if first is false => it is practiceMode, so second can be checked
            checkResponse();
        }
    }

    public void clickedPrev(View view) {
        if (currentQuestionIndex == 0) {
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "clickedPrev: Already in first question"); }
            return;
        }

        currentQuestionIndex--;
        if (currentQuestionIndex < 0) {  // this should never happen
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "clickedPrev: ERR: currentQuestionIndex < 0"); }
            currentQuestionIndex = 0;
        }

        setCurrentQuestion();
    }

    public void clickedNext(View view) {
        if (currentQuestionIndex == (numQuestions - 1)) {
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "clickedNext: Already in last question"); }
            return;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex >= numQuestions) {  // this should never happpen
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "clickedNext: ERR: currentQuestionIndex > numQuestions"); }
            currentQuestionIndex = numQuestions - 1;
        }

        setCurrentQuestion();
    }

    private void setActivityAccordingToMode() {
        if (is_read_mode) {
            // don't show check button
            findViewById(R.id.learn_button_check).setVisibility(View.GONE);

        } else {  // practice mode
            // show check button
            findViewById(R.id.learn_button_check).setVisibility(View.VISIBLE);
        }

        setCurrentQuestion();  // so that if read, it will mark the answer, else, clears response
    }

    private void clearResponse() {
        // clear selection
        ((RadioGroup) findViewById(R.id.learn_RadioGroup_choices)).clearCheck();
        // clear background colors
        final int color_alpha_only = resources.getColor(R.color.color_alpha_only);
        for (int radioButtonID : radioButtonIDs) {
            findViewById(radioButtonID).setBackgroundColor(color_alpha_only);
        }
        
        if (!(is_read_mode || practiceAnswers[currentQuestionIndex] != -1)) {
            // practice mode and not-answered
            // only then make the options clickable
            // because, for readMode and answeredPracticeMode, checkResponse is called which makes them non-clickable anyway
            setOptionsRadioButtonsInteractivity(true);
        }
    }

    private void checkResponse() {
        // assumes userResponse is stored in practiceAnswers array in practiceMode
        // uses currentCorrectAnswer in readMode

        int userResponse;

        if (is_read_mode) {
            userResponse = currentCorrectAnswer;

        } else {
            try {
                userResponse = practiceAnswers[currentQuestionIndex];
                // todo if userResponse is notCheckedYet(i.e. -1 in future), should this function return with a display toast
                // note: currently the function won't do anything in the following if it userResponse is -1
            } catch (Exception exception) {
                // either practiceAnswers is null or the length doesn't fit index -> both shouldn't happen // fixed in commit d0ea
                userResponse = -1;
            }
        }

        if (userResponse != -1) {  // will never be -1 in readMode, so will always be non-clickable in readMode
            setOptionsRadioButtonsInteractivity(false);  // an answer exists, so make the options non-clickable
        }

        if (is_read_mode) {
            // todo settings-> show backgroundColor in readMode -> if not, return from this function here
        }

        final int colorCorrectChoice = resources.getColor(R.color.color_correct_choice);
        final int colorWrongChoice = resources.getColor(R.color.color_wrong_choice);

        for (int i = 0; i < radioButtonIDs.length; i++) {
            int color;
            if (i == currentCorrectAnswer) {
                color = colorCorrectChoice;
            } else if (i == userResponse) {
                color = colorWrongChoice;
            } else {
                continue;
            }
            findViewById(radioButtonIDs[i]).setBackgroundColor(color);
        }
    }

    public void clickedCheck(View view) {  // note that this is only called in practiceMode
        // save selection to practiceAnswers and call checkResponse
        practiceAnswers[currentQuestionIndex] = -1;  // -1 means no option selected
        for (int i = 0; i < radioButtonIDs.length; i++) {
            if (((RadioButton) findViewById(radioButtonIDs[i])).isChecked()) {
                practiceAnswers[currentQuestionIndex] = i;
                break;
            }
        }
        checkResponse();
    }

    private void setOptionsRadioButtonsInteractivity(boolean clickable) {
        for (int radioButtonID : radioButtonIDs) {
            findViewById(radioButtonID).setClickable(clickable);
        }
    }
}