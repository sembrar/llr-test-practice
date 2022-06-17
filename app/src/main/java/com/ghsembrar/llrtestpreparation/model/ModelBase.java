package com.ghsembrar.llrtestpreparation.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.ghsembrar.llrtestpreparation.CONSTANTS;
import com.ghsembrar.llrtestpreparation.R;

import java.util.Arrays;


abstract public class ModelBase {

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "ModelBase";

    Context context;
    Resources resources;
    String questions_and_choices_language_code = "en";  // this will be set by the app using the function at the bottom
    int num_questions = 0;
    int current_question_index = 0;
    int[] user_answers;

    public static final int NO_ANSWER_CHOSEN_YET = -1;
    // the following is used when user clicks check in practice mode without selecting a choice
    public static final int EMPTY_ANSWER_CHOSEN = -2;
    private static final int USER_ANSWER_MIN = EMPTY_ANSWER_CHOSEN;
    private static final int USER_ANSWER_MAX = 3;  // as there are 4 choices [0, 3]

    // sets the data member: context
    ModelBase(Context context) {
        this.context = context;
        resources = this.context.getResources();
    }

    public int get_num_questions() {
        return num_questions;
    }

    public int get_current_question_index() {
        return current_question_index;
    }

    public boolean set_to_previous_question() {
        if (current_question_index == 0) return false;

        current_question_index--;
        if (current_question_index < 0) current_question_index = 0;  // this should never happen
        return true;
    }

    public boolean set_to_next_question() {
        if (current_question_index == num_questions - 1) return false;

        current_question_index++;
        if (current_question_index >= num_questions) current_question_index = num_questions - 1;  // this shouldn't happen
        return true;
    }

    // the following 4 functions should be overridden in derived classes
    // they should generate and return the name string of the required resource

    abstract String get_question_res_identifier_name();
    abstract String get_accompanying_image_res_identifier_name();
    abstract String get_option_res_identifier_name(int option_index);
    abstract String get_correct_answer_res_identifier_name();

    // calls get_question_res_identifier_name
    // if the name exists, returns the corresponding string with question number formatted into it
    // else returns no_string
    public String get_question_string() {
        int res_id = resources.getIdentifier(get_question_res_identifier_name(),
                "string", context.getPackageName());
        if (res_id == 0) return resources.getString(R.string.no_string);
        return resources.getString(res_id, get_current_question_index() + 1);
    }

    // calls get_accompanying_image_res_identifier_name
    // if the name exists, returns the corresponding resource id
    // else returns 0
    public int get_accompanying_image_res_id() {
        return context.getResources().getIdentifier(get_accompanying_image_res_identifier_name(),
                "drawable", context.getPackageName());
    }

    // calls get_option_res_identifier_name
    // if the name exists, returns the corresponding string resource
    // else returns resource no_string
    public String get_option_string(int option_index) {
        int res_id = resources.getIdentifier(get_option_res_identifier_name(option_index),
                "string", context.getPackageName());
        if (res_id == 0) res_id = R.string.no_string;
        return resources.getString(res_id);
    }

    // calls get_correct_answer_red_identifier_name
    // if the name exists, returns the corresponding integer
    // else returns -1  // as correct answer will always be in [0, 4)
    public int get_correct_answer_option_index() {
        Resources resources = context.getResources();
        int res_id = resources.getIdentifier(get_correct_answer_res_identifier_name(),
                "integer", context.getPackageName());
        if (res_id == 0) return -1;
        return resources.getInteger(res_id);
    }

    public int get_user_answer() {
        return user_answers[current_question_index];
    }

    public void set_user_answer(int new_user_answer) {
        if (new_user_answer < USER_ANSWER_MIN || new_user_answer > USER_ANSWER_MAX) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "set_user_answer: answer out of bounds: " + new_user_answer);
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "set_user_answer: defaults to no answer chosen yet");
            new_user_answer = NO_ANSWER_CHOSEN_YET;
        }
        user_answers[current_question_index] = new_user_answer;
    }

    public void clear_all_user_answers() {
        Arrays.fill(user_answers, NO_ANSWER_CHOSEN_YET);
    }

    public void set_language(String language_code) {
        this.questions_and_choices_language_code = language_code;
    }
}
