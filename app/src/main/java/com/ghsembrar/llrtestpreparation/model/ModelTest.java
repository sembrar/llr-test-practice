package com.ghsembrar.llrtestpreparation.model;

import android.content.Context;
import android.util.Log;

import com.ghsembrar.llrtestpreparation.CONSTANTS;
import com.ghsembrar.llrtestpreparation.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ModelTest extends ModelBase {

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "ModelTest";

    private static final int NUM_QUESTIONS = 20;
    private static final int NUM_MAX_SECONDS = 600;  // 10 minutes

    private static final String FILENAME_TEST_DATA = "test_data";

    class TestQuestion {
        int subject_index = 0;
        int question_index = 0;
    }

    private final ArrayList<TestQuestion> test_questions;
    private int num_seconds_remaining = NUM_MAX_SECONDS;
    private int score = 0;
    private boolean test_in_progress = false;


    public ModelTest(Context context) {
        super(context);

        num_questions = NUM_QUESTIONS;
        test_questions = new ArrayList<>(num_questions);
        user_answers = new int[num_questions];

        clear_all_user_answers();  // fills all answers with no_answer_chosen_yet
        // populate test_questions
        for (int i = 0; i < num_questions; i++) {
            test_questions.add(new TestQuestion());
        }
    }

    public int get_score() {
        return score;
    }

    public boolean is_test_in_progress() {
        return test_in_progress;
    }

    public int get_num_seconds_remaining() {
        return num_seconds_remaining;
    }

    @Override
    String get_question_res_identifier_name() {
        TestQuestion testQuestion = test_questions.get(current_question_index);
        return "s" + testQuestion.subject_index + "_" + testQuestion.question_index + "q";
    }

    @Override
    String get_accompanying_image_res_identifier_name() {
        TestQuestion testQuestion = test_questions.get(current_question_index);
        return "i" + testQuestion.subject_index + "_" + testQuestion.question_index;
        // note: don't add extension (.jpg) in name, may not be found
    }

    @Override
    String get_option_res_identifier_name(int option_index) {
        final String[] option_suffixes = {"a", "b", "c", "d"};
        TestQuestion testQuestion = test_questions.get(current_question_index);
        return "s" + testQuestion.subject_index + "_" + testQuestion.question_index + option_suffixes[option_index];
    }

    @Override
    String get_correct_answer_res_identifier_name() {
        TestQuestion testQuestion = test_questions.get(current_question_index);
        return "a" + testQuestion.subject_index + "_" + testQuestion.question_index;
    }

    public void save_test_data() {
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try {
            fos = context.getApplicationContext().openFileOutput(FILENAME_TEST_DATA, Context.MODE_PRIVATE);
            dos = new DataOutputStream(fos);

            if (test_questions.size() == num_questions) {  // save data only if both are same (they won't be different)
                dos.writeInt(num_questions);  // this is used while loading if the number of data saved matches with required length
                for (int i = 0; i < num_questions; i++) {
                    TestQuestion testQuestion = test_questions.get(i);
                    dos.writeInt(testQuestion.subject_index);
                    dos.writeInt(testQuestion.question_index);
                    dos.writeInt(user_answers[i]);
                }
                dos.writeInt(current_question_index);
                dos.writeInt(num_seconds_remaining);
                dos.writeInt(score);
                dos.writeBoolean(test_in_progress);
            }

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

    public boolean load_test_data() {
        boolean load_is_successful = false;

        FileInputStream fis = null;
        DataInputStream dis = null;

        try {
            fis = context.getApplicationContext().openFileInput(FILENAME_TEST_DATA);
            dis = new DataInputStream(fis);

            int numData = dis.readInt();
            if (numData == NUM_QUESTIONS) {  // otherwise don't load, may be it is a new file, or not saved properly

                for (int i = 0; i < numData; i++) {
                    TestQuestion testQuestion = test_questions.get(i);
                    testQuestion.subject_index = dis.readInt();
                    testQuestion.question_index = dis.readInt();
                    user_answers[i] = dis.readInt();
                }

                current_question_index = dis.readInt();
                num_seconds_remaining = dis.readInt();
                score = dis.readInt();
                test_in_progress = dis.readBoolean();

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

    public void set_up_new_test() {
        // make an integer-range array of total number of questions
        // shuffle it to take the first NUM_QUESTIONS as unique testQuestions
        int num_total_questions = context.getResources().getInteger(R.integer.num_total_questions);
        ArrayList<Integer> allIndices = new ArrayList<>(num_total_questions);
        for (int i = 0; i < num_total_questions; i++) {
            allIndices.add(i);
        }
        Collections.shuffle(allIndices);

        // convert the first NUM_QUESTIONS integers in the above list to TestQuestion

        final int[] numQuestionsInSubjects = context.getResources().getIntArray(R.array.num_questions);

        for (int i = 0; i < NUM_QUESTIONS; i++) {
            int questionIndexAsInt = allIndices.get(i);

            for (int subject_index = 0; subject_index < numQuestionsInSubjects.length; subject_index++) {

                if (questionIndexAsInt < numQuestionsInSubjects[subject_index]) {
                    TestQuestion testQuestion = test_questions.get(i);
                    testQuestion.subject_index = subject_index;
                    testQuestion.question_index = questionIndexAsInt;
                    break;
                } else {
                    questionIndexAsInt -= numQuestionsInSubjects[subject_index];
                }
            }
        }

        // set all the variables
        clear_all_user_answers();
        test_in_progress = true;
        current_question_index = 0;
        num_seconds_remaining = NUM_MAX_SECONDS;
        score = 0;

        if (CONSTANTS.ALLOW_DEBUG) {
            for (int i = 0; i < NUM_QUESTIONS; i++) {
                TestQuestion testQuestion = test_questions.get(i);
                Log.i(TAG, String.format("setUpNewTest: [%d] %d.%d", i+1, testQuestion.subject_index, testQuestion.question_index));
            }
        }
    }

    public void finish_test() {
        test_in_progress = false;
        calculate_score();
    }

    private void calculate_score() {

        // store current value to set it back at the end (as it is modified in the function)
        int temp_store_current_question_index = current_question_index;

        score = 0;
        for (current_question_index = 0; current_question_index < num_questions; current_question_index++) {
            if (get_user_answer() == get_correct_answer_option_index()) score++;
        }

        // reset to initial value
        current_question_index = temp_store_current_question_index;
    }

    public void decrement_num_seconds_remaining() {
        num_seconds_remaining--;
        if (num_seconds_remaining < 0) num_seconds_remaining = 0;
    }
}
