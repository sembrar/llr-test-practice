package com.ghsembrar.llrtestpreparation.model;

import android.content.Context;

import com.ghsembrar.llrtestpreparation.R;

public class ModelLearn extends ModelBase {

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
}
