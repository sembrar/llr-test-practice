package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String PACKAGE_NAME_FOR_KEY_PREFIX = "com.ghsembrar.llrtestpreparation.";
    public static final String INTENT_EXTRA_KEY_SUBJECT_INDEX = PACKAGE_NAME_FOR_KEY_PREFIX + "subject_index";

    public static final int SUBJECT_INDEX_FIRST = 0;
    public static final int SUBJECT_INDEX_SECOND = 1;
    public static final int SUBJECT_INDEX_THIRD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AndroidStudio says onClick in xml is broken in older versions
        findViewById(R.id.main_button_learn_subject1).setOnClickListener(v -> startLearnActivity(SUBJECT_INDEX_FIRST));
        findViewById(R.id.main_button_learn_subject2).setOnClickListener(v -> startLearnActivity(SUBJECT_INDEX_SECOND));
        findViewById(R.id.main_button_learn_subject3).setOnClickListener(v -> startLearnActivity(SUBJECT_INDEX_THIRD));
    }

    private void startLearnActivity(int subjectIndex) {
        Intent intent = new Intent(this, LearnActivity.class);
        intent.putExtra(INTENT_EXTRA_KEY_SUBJECT_INDEX, subjectIndex);
        startActivity(intent);
    }
}