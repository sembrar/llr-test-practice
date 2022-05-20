package com.ghsembrar.llrtestpreparation.model;

import android.content.Context;

import java.util.ArrayList;

public class ModelTest extends ModelBase {

    private static final int NUM_QUESTIONS = 20;

    class TestQuestion {
        int subject_index = 0;
        int question_index = 0;
    }

    private final ArrayList<TestQuestion> test_questions;

    public ModelTest(Context context) {
        super(context);

        num_questions = NUM_QUESTIONS;
        test_questions = new ArrayList<>(num_questions);
        user_answers = new int[num_questions];

        clear_all_user_answers();  // fills all answers with -1 (the default for no selection)
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
}
