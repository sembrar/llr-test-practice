package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class TestActivity extends AppCompatActivity {

    private class TestQuestionAndUserAnswer {
        int subject_index = -1;
        int question_index = -1;
        int user_answer = -1;
    }

    private static final int NUM_QUESTIONS = 20;
    private ArrayList<TestQuestionAndUserAnswer> test_questions_and_user_answers = new ArrayList<>(NUM_QUESTIONS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Intent intent = getIntent();
        int testType = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_TEST_OLD_OR_NEW, MainActivity.TEST_TYPE_VIEW_OR_CONTINUE_OLD_TEST);

        // initialize array
        for (int i = 0; i < NUM_QUESTIONS; i++) {
            test_questions_and_user_answers.add(new TestQuestionAndUserAnswer());
        }

        switch (testType) {
            case MainActivity.TEST_TYPE_NEW_TEST:
                setUpNewTest();
                break;
            case MainActivity.TEST_TYPE_VIEW_OR_CONTINUE_OLD_TEST:
            default:
                // todo try to load the previous test if it exists
                Toast.makeText(this, "No old test", Toast.LENGTH_SHORT).show();
                finish();
                break;
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

        if (CONSTANTS.ALLOW_DEBUG) {
            for (int i = 0; i < NUM_QUESTIONS; i++) {
                TestQuestionAndUserAnswer testQuestionAndUserAnswer = test_questions_and_user_answers.get(i);
                Log.i(CONSTANTS.LOG_TAG, String.format("setUpNewTest: [%d] %d.%d", i+1, testQuestionAndUserAnswer.subject_index, testQuestionAndUserAnswer.question_index));
            }
        }
    }
}