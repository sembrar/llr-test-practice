package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class LearnActivity extends AppCompatActivity {

    private static final String TAG = "LearnActivityLogTag";

    private int subject_index = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        // read data from intent
        Intent intent = getIntent();
        subject_index = intent.getIntExtra(MainActivity.INTENT_EXTRA_KEY_SUBJECT_INDEX, -1);

        // if data set by intent is wrong, read it from shared prefs
        // this can happen if android force re-started this activity
        if (is_activity_start_data_not_valid()) {
            if (CONSTANTS.ALLOW_DEBUG) {
                Log.i(TAG, "onCreate: Activity start data from intent is bad. Reading from prefs.");
            }

            SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
            subject_index = sharedPreferences.getInt(MainActivity.INTENT_EXTRA_KEY_SUBJECT_INDEX, -1);
        }

        if (is_activity_start_data_not_valid()) {
            if (CONSTANTS.ALLOW_DEBUG) {
                Log.i(TAG, "onCreate: Activity start data from prefs is bad. Stopping activity.");
            }

            finish();  // todo may be show a Toast for the user that an error occurred
            return;
        }

        // show activity start data
        if (CONSTANTS.ALLOW_DEBUG) {
            Log.i(TAG, String.format("onCreate: subject_index: %d", subject_index));
        }
    }

    private boolean is_activity_start_data_not_valid() {
        if (subject_index < 0 || subject_index >= getResources().getStringArray(R.array.subject_names).length) return true;
        return false;
    }
}