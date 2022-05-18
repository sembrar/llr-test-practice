package com.ghsembrar.llrtestpreparation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {
    
    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "Test";

    private static final String FILENAME_TEST_DATA = "test_data";
    private static final String SHARED_PREF_KEY_TEST_IN_PROGRESS = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "test_in_progress";

    private static class TestQuestionAndUserAnswer {
        int subject_index = -1;
        int question_index = -1;
        int user_answer = -1;
    }

    private static final int NUM_QUESTIONS = 20;
    private static final int PASS_SCORE = 12;
    private static final int NUM_MAX_SECONDS_PER_TEST = 600;  // 10 minutes

    private Resources resources;
    private CountDownTimer countDownTimer;

    // the following are saved in a file
    private final ArrayList<TestQuestionAndUserAnswer> test_questions_and_user_answers = new ArrayList<>(NUM_QUESTIONS);
    private int currentQuestionIndex = 0;
    private int numSecondsRemaining = NUM_MAX_SECONDS_PER_TEST;
    private int score = 0;
    private boolean testInProgress = false;  // this is also saved in SharedPref for quicker access

    // the following are for easier access
    private static final int[] radioButtonIDs = {
            R.id.test_radioButton_choice1, R.id.test_radioButton_choice2,
            R.id.test_radioButton_choice3, R.id.test_radioButton_choice4
    };
    private static final String[] radioButtonOptionSuffixes = {"a", "b", "c", "d"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.test_button_previous).setOnClickListener(this::clickedPrev);
        findViewById(R.id.test_button_next).setOnClickListener(this::clickedNext);
        findViewById(R.id.test_button_finish).setOnClickListener(this::clickedFinish);

        findViewById(R.id.test_radioButton_choice1).setOnClickListener(v -> clickedRadioButton(0));
        findViewById(R.id.test_radioButton_choice2).setOnClickListener(v -> clickedRadioButton(1));
        findViewById(R.id.test_radioButton_choice3).setOnClickListener(v -> clickedRadioButton(2));
        findViewById(R.id.test_radioButton_choice4).setOnClickListener(v -> clickedRadioButton(3));

        Intent intent = getIntent();
        int testType = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_TEST_OLD_OR_NEW, MainActivity.TEST_TYPE_VIEW_OR_CONTINUE_OLD_TEST);

        // initialize array
        for (int i = 0; i < NUM_QUESTIONS; i++) {
            test_questions_and_user_answers.add(new TestQuestionAndUserAnswer());
        }

        switch (testType) {
            case MainActivity.TEST_TYPE_NEW_TEST:
                // todo if a previous test in progress, ask if user wants to continue instead,
                //  this behavior should be allowed to be disabled in Settings
                setUpNewTest();
                break;
            case MainActivity.TEST_TYPE_VIEW_OR_CONTINUE_OLD_TEST:
            default:
                if (!loadTestData()) {
                    Toast.makeText(this, "No old test", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

        if (testInProgress) {
            countDownTimer = new CountDownTimer(numSecondsRemaining * 1000L, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    numSecondsRemaining = (int) (millisUntilFinished / 1000);
                    int minutes_to_show = numSecondsRemaining / 60;
                    int seconds_to_show = numSecondsRemaining - minutes_to_show * 60;
                    ((TextView) findViewById(R.id.test_textView_time)).setText(String.format("%d:%02d", minutes_to_show, seconds_to_show));
                }

                @Override
                public void onFinish() {
                    clickedFinish(null);
                }
            };
            countDownTimer.start();
        }
    }

    private void setUpNewTest() {

        // make an integer-range array of total number of questions
        // shuffle it to take the first NUM_QUESTIONS as unique testQuestions
        int num_total_questions = getResources().getInteger(R.integer.num_total_questions);
        ArrayList<Integer> allIndices = new ArrayList<>(num_total_questions);
        for (int i = 0; i < num_total_questions; i++) {
            allIndices.add(i);
        }
        Collections.shuffle(allIndices);

        // convert the first NUM_QUESTIONS integers in the above list to TestQuestion

        final int[] numQuestionsInSubjects = getResources().getIntArray(R.array.num_questions);

        for (int i = 0; i < NUM_QUESTIONS; i++) {
            int questionIndexAsInt = allIndices.get(i);

            for (int subject_index = 0; subject_index < numQuestionsInSubjects.length; subject_index++) {

                if (questionIndexAsInt < numQuestionsInSubjects[subject_index]) {
                    TestQuestionAndUserAnswer testQuestionAndUserAnswer = test_questions_and_user_answers.get(i);
                    testQuestionAndUserAnswer.subject_index = subject_index;
                    testQuestionAndUserAnswer.question_index = questionIndexAsInt;
                    testQuestionAndUserAnswer.user_answer = -1;
                    break;
                } else {
                    questionIndexAsInt -= numQuestionsInSubjects[subject_index];
                }
            }
        }

        // set all the variables
        testInProgress = true;
        currentQuestionIndex = 0;
        numSecondsRemaining = NUM_MAX_SECONDS_PER_TEST;
        score = 0;

        if (CONSTANTS.ALLOW_DEBUG) {
            for (int i = 0; i < NUM_QUESTIONS; i++) {
                TestQuestionAndUserAnswer testQuestionAndUserAnswer = test_questions_and_user_answers.get(i);
                Log.i(TAG, String.format("setUpNewTest: [%d] %d.%d", i+1, testQuestionAndUserAnswer.subject_index, testQuestionAndUserAnswer.question_index));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateResourcesVariable();  // this helps load resources of different language than system's

        setActivityAccordingToTestStatus();  // shows/hides finish button etc.  // also sets current question
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (countDownTimer != null) countDownTimer.cancel();  // can be null when a finished old test is being displayed

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHARED_PREF_KEY_TEST_IN_PROGRESS, testInProgress);
        editor.apply();

        saveTestData();
    }

    private void updateResourcesVariable() {
        SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFS_FILE_SETTINGS, Context.MODE_PRIVATE);

        boolean use_system_language = sharedPreferences.getBoolean(SettingsActivity.SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE, true);
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("updateResourcesVariable: Use system language: %b", use_system_language));

        if (use_system_language) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "updateResourcesVariable: Using default resources");
            resources = getResources();
            return;
        }

        // not use system language => use chosen language
        String chosen_language = sharedPreferences.getString(SettingsActivity.SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG, null);
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("updateResourcesVariable: Chosen language: %s", chosen_language));

        if (chosen_language == null) {  // this shouldn't happen
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "updateResourcesVariable: Using default resources");

            resources = getResources();
            return;
        }

        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "updateResourcesVariable: Using chosen language resources");

        Locale chosenLocale = new Locale(chosen_language);
        resources = getLocalizedResources(this, chosenLocale);
    }

    @NonNull
    private Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration = new Configuration(configuration);
        configuration.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(configuration);
        return localizedContext.getResources();
    }

    private void setActivityAccordingToTestStatus() {
        if (testInProgress) {
            // show finish button
            findViewById(R.id.test_button_finish).setVisibility(View.VISIBLE);

        } else {  // test finished
            // don't show finish button
            findViewById(R.id.test_button_finish).setVisibility(View.GONE);
        }

        setCurrentQuestion();
    }

    private void setCurrentQuestion() {
        // displays current question in activity
        TestQuestionAndUserAnswer testQuestionAndUserAnswer = test_questions_and_user_answers.get(currentQuestionIndex);
        int subject_index = testQuestionAndUserAnswer.subject_index;
        int question_index = testQuestionAndUserAnswer.question_index;

        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("setCurrentQuestion: SubjectIndex,QuestionIndex: %d,%d", subject_index, question_index));

        final String packageName = getPackageName();

        // set question
        int quesResID = resources.getIdentifier("s" + subject_index + "_" + question_index + "q", "string", packageName);
        ((TextView) findViewById(R.id.test_textView_question)).setText(
                quesResID == 0 ?
                        resources.getString(R.string.no_string) :
                        resources.getString(quesResID, currentQuestionIndex + 1)
        );

        // set options
        for (int i = 0; i < radioButtonIDs.length; i++) {
            int resID = resources.getIdentifier("s" + subject_index + "_" + question_index + radioButtonOptionSuffixes[i], "string", packageName);
            if (resID == 0) resID = R.string.no_string;
            ((RadioButton) findViewById(radioButtonIDs[i])).setText(resID);
        }

        // set image if exists else hide image view
        String imageName = "i" + subject_index + "_" + question_index;  // note: don't add extension (.jpg) in name, may not be found
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("setCurrentQuestion: ImageName = %s", imageName));

        int imageResID = resources.getIdentifier(imageName, "drawable", packageName);
        if (imageResID == 0) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "setCurrentQuestion: Image doesn't exist");

            findViewById(R.id.test_imageView_accompanyingImage).setVisibility(View.GONE);
        }
        else {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "setCurrentQuestion: Image exists");

            ImageView imageView = findViewById(R.id.test_imageView_accompanyingImage);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(imageResID);
        }

        // clear existing response, also clear any background colors
        // clear selection
        ((RadioGroup) findViewById(R.id.test_RadioGroup_choices)).clearCheck();
        // clear background colors
        final int color_alpha_only = resources.getColor(R.color.color_alpha_only);
        for (int radioButtonID : radioButtonIDs) {
            findViewById(radioButtonID).setBackgroundColor(color_alpha_only);
        }

        // mark the existing user answer if any
        if (testQuestionAndUserAnswer.user_answer != -1) {
            ((RadioButton) findViewById(radioButtonIDs[testQuestionAndUserAnswer.user_answer])).setChecked(true);
            // todo may be in future, if the color of the radiobutton selection (green dot) needs also be set according to
            //  right/wrong, then this code block may need to be moved into the following testInProgress or Not code block
        }

        if (testInProgress) {
            setOptionsRadioButtonsInteractivity(true);

        } else {
            // test finished
            setOptionsRadioButtonsInteractivity(false);

            // get correct answer
            int ansResId = resources.getIdentifier("a" + subject_index + "_" + question_index, "integer", packageName);
            int currentCorrectAnswer;
            if (ansResId != 0) {
                currentCorrectAnswer = resources.getInteger(ansResId);
            } else {
                // todo should(also will) never happen, so handle it properly
                currentCorrectAnswer = -1;
            }

            final int colorCorrectChoice = resources.getColor(R.color.color_correct_choice);
            final int colorWrongChoice = resources.getColor(R.color.color_wrong_choice);

            for (int i = 0; i < radioButtonIDs.length; i++) {
                int color;
                if (i == currentCorrectAnswer) {
                    color = colorCorrectChoice;
                } else if (i == testQuestionAndUserAnswer.user_answer) {
                    color = colorWrongChoice;
                } else {
                    continue;
                }
                findViewById(radioButtonIDs[i]).setBackgroundColor(color);
            }
        }
    }

    public void clickedPrev(View view) {
        if (currentQuestionIndex == 0) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clickedPrev: Already in first question");

            return;
        }

        currentQuestionIndex--;
        if (currentQuestionIndex < 0) {  // this should never happen
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clickedPrev: ERR: currentQuestionIndex < 0");

            currentQuestionIndex = 0;
        }

        setCurrentQuestion();
    }

    public void clickedNext(View view) {
        if (currentQuestionIndex == (NUM_QUESTIONS - 1)) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clickedNext: Already in last question");

            return;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex >= NUM_QUESTIONS) {  // this should never happpen
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clickedNext: ERR: currentQuestionIndex > numQuestions");

            currentQuestionIndex = NUM_QUESTIONS - 1;
        }

        setCurrentQuestion();
    }

    public void clickedFinish(View view) {
        if (countDownTimer != null) countDownTimer.cancel();  // can be null when a finished old test is being displayed
        numSecondsRemaining = 0;
        testInProgress = false;
        setCurrentQuestion();
    }

    private void setOptionsRadioButtonsInteractivity(boolean clickable) {
        for (int radioButtonID : radioButtonIDs) {
            findViewById(radioButtonID).setClickable(clickable);
        }
    }

    private void clickedRadioButton(int choiceIndex) {
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("clickedRadioButton: Clicked RadioButton %d", choiceIndex));

        test_questions_and_user_answers.get(currentQuestionIndex).user_answer = choiceIndex;
    }

    private void saveTestData() {
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try {
            fos = getApplicationContext().openFileOutput(FILENAME_TEST_DATA, Context.MODE_PRIVATE);
            dos = new DataOutputStream(fos);

            dos.writeInt(test_questions_and_user_answers.size());  // this is used while loading if the number of data saved matches with required length
            for (TestQuestionAndUserAnswer testQuestionAndUserAnswer : test_questions_and_user_answers) {
                dos.writeInt(testQuestionAndUserAnswer.subject_index);
                dos.writeInt(testQuestionAndUserAnswer.question_index);
                dos.writeInt(testQuestionAndUserAnswer.user_answer);
            }
            dos.writeInt(currentQuestionIndex);
            dos.writeInt(numSecondsRemaining);
            dos.writeInt(score);
            dos.writeBoolean(testInProgress);

        } catch (FileNotFoundException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "saveTestData: Datafile not found to write");
        } catch (IOException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "saveTestData: Write failed due to IOException");
        } finally {

            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "saveTestData: DOS close failed");
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "saveTestData: FOS close failed");
                }
            }
        }
    }

    private boolean loadTestData() {
        boolean load_is_successful = false;

        FileInputStream fis = null;
        DataInputStream dis = null;

        try {
            fis = getApplicationContext().openFileInput(FILENAME_TEST_DATA);
            dis = new DataInputStream(fis);

            int numData = dis.readInt();
            if (numData == NUM_QUESTIONS) {  // otherwise don't load, may be it is a new file, or not saved properly

                for (int i = 0; i < numData; i++) {
                    TestQuestionAndUserAnswer testQuestionAndUserAnswer = test_questions_and_user_answers.get(i);
                    testQuestionAndUserAnswer.subject_index = dis.readInt();
                    testQuestionAndUserAnswer.question_index = dis.readInt();
                    testQuestionAndUserAnswer.user_answer = dis.readInt();
                }

                currentQuestionIndex = dis.readInt();
                numSecondsRemaining = dis.readInt();
                score = dis.readInt();
                testInProgress = dis.readBoolean();

                load_is_successful = true;
            }

            dis.close();
            fis.close();

        } catch (FileNotFoundException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadTestData: Datafile not found to read");

        } catch (IOException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadTestData: Read failed due to IOException");

        } finally {

            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadTestData: DIS close failed");
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadTestData: FIS close failed");
                }
            }
        }

        return load_is_successful;
    }
}