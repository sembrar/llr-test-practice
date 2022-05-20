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

public class ModelLearn extends ModelBase {

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "ModelLearn";

    // file name to save practiceAnswers
    private static final String FILENAME_SAVE_PRACTICE_ANSWERS_PREFIX = "practice_answers_";  // add subjectIndex to it

    int subject_index;

    // the constructor
    public ModelLearn(Context context, int subject_index) {
        super(context);

        // check that subject index is in bounds
        int[] num_questions_in_subjects = context.getResources().getIntArray(R.array.num_questions);
        if (subject_index < 0 || subject_index >= num_questions_in_subjects.length)
            throw new IndexOutOfBoundsException("ModelLearn: subject_index out of bounds");

        this.subject_index = subject_index;

        // set other data members
        num_questions = num_questions_in_subjects[subject_index];
        user_answers = new int[num_questions];

        clear_all_user_answers();  // fills all user answers with -1 (the default for no answer chosen)
        load_practice_answers();  // load any previously saved answers
    }

    @Override
    String get_question_res_identifier_name() {
        return "s" + subject_index + "_" + current_question_index + "q";
    }

    @Override
    String get_accompanying_image_res_identifier_name() {
        return "i" + subject_index + "_" + current_question_index;  // note: don't add extension (.jpg) in name, may not be found
    }

    @Override
    String get_option_res_identifier_name(int option_index) {
        final String[] option_suffixes = {"a", "b", "c", "d"};
        return "s" + subject_index + "_" + current_question_index + option_suffixes[option_index];
    }

    @Override
    String get_correct_answer_res_identifier_name() {
        return "a" + subject_index + "_" + current_question_index;
    }

    public void save_practice_answers() {
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try {
            fos = context.getApplicationContext().openFileOutput(FILENAME_SAVE_PRACTICE_ANSWERS_PREFIX + subject_index, Context.MODE_PRIVATE);
            dos = new DataOutputStream(fos);

            dos.writeInt(user_answers.length);  // this is used while loading if the number of data saved matches with required length
            for (int practiceAns : user_answers) {
                dos.writeInt(practiceAns);
            }


        } catch (FileNotFoundException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "savePracticeAnswers: Datafile not found to write");

        } catch (IOException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "savePracticeAnswers: IOException");

        } finally {

            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "savePracticeAnswers: DOS close failed");
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "savePracticeAnswers: FOS close failed");
                }
            }
        }
    }

    private void load_practice_answers() {
        FileInputStream fis = null;
        DataInputStream dis = null;

        try {
            fis = context.getApplicationContext().openFileInput(FILENAME_SAVE_PRACTICE_ANSWERS_PREFIX + subject_index);
            dis = new DataInputStream(fis);

            int numData = dis.readInt();
            if (numData == user_answers.length) {  // otherwise don't load, may be it is a new file, or not saved properly
                for (int i = 0; i < numData; i++) {
                    user_answers[i] = dis.readInt();
                }
            }

            dis.close();
            fis.close();

        } catch (FileNotFoundException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadPracticeAnswers: Datafile not found to read");

        } catch (IOException ignored) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadPracticeAnswers: Read failed due to IOException");

        } finally {

            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadPracticeAnswers: DIS close failed");
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {
                    if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "loadPracticeAnswers: FIS close failed");
                }
            }
        }
    }
}
