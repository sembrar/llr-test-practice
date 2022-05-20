package com.ghsembrar.llrtestpreparation.model;

import android.content.Context;
import android.content.res.Resources;

import com.ghsembrar.llrtestpreparation.R;

import java.util.Arrays;

abstract public class ModelBase {

    Context context;
    int num_questions = 0;
    int current_question_index = 0;
    int[] user_answers;

    // sets the data member: context
    ModelBase(Context context) {
        this.context = context;
    }

    boolean set_to_previous_question() {
        if (current_question_index == 0) return false;

        current_question_index--;
        if (current_question_index < 0) current_question_index = 0;  // this should never happen
        return true;
    }

    boolean set_to_next_question() {
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
    // if the name exists, returns the corresponding string resource id
    // else returns the resource id of no_string
    int get_question_string_res_id() {
        Resources resources = context.getResources();
        int res_id = resources.getIdentifier(get_question_res_identifier_name(),
                "string", context.getPackageName());
        if (res_id == 0) res_id = R.string.no_string;
        return res_id;
    }

    // calls get_accompanying_image_res_identifier_name
    // if the name exists, returns the corresponding resource id
    // else returns 0
    int get_accompanying_image_res_id() {
        return context.getResources().getIdentifier(get_accompanying_image_res_identifier_name(),
                "drawable", context.getPackageName());
    }

    // calls get_option_res_identifier_name
    // if the name exists, returns the corresponding string resource id
    // else returns resource id of no_string
    int get_option_string_res_id(int option_index) {
        int res_id = context.getResources().getIdentifier(get_option_res_identifier_name(option_index),
                "string", context.getPackageName());
        if (res_id == 0) res_id = R.string.no_string;
        return res_id;
    }

    // calls get_correct_answer_red_identifier_name
    // if the name exists, returns the corresponding integer
    // else returns -1  // as correct answer will always be in [0, 4)
    int get_correct_answer_option_index() {
        Resources resources = context.getResources();
        int res_id = resources.getIdentifier(get_correct_answer_res_identifier_name(),
                "integer", context.getPackageName());
        if (res_id == 0) return -1;
        return resources.getInteger(res_id);
    }

    int get_user_answer() {
        return user_answers[current_question_index];
    }

    void set_user_answer(int new_user_answer) {
        user_answers[current_question_index] = new_user_answer;
    }

    void clear_all_user_answers() {
        Arrays.fill(user_answers, -1);
    }
}
