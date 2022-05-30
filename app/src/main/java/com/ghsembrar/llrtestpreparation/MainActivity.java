package com.ghsembrar.llrtestpreparation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ghsembrar.llrtestpreparation.model.ModelTest;

public class MainActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_KEY_SUBJECT_INDEX = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "subject_index";
    public static final String INTENT_EXTRA_KEY_TEST_OLD_OR_NEW = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "test_type";
    public static final int TEST_TYPE_VIEW_OR_CONTINUE_OLD_TEST = 0;
    public static final int TEST_TYPE_NEW_TEST = 1;

    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AndroidStudio says onClick in xml is broken in older versions
        findViewById(R.id.main_button_learn_subject0).setOnClickListener(v -> startLearnAndTestActivityForLearn(0));
        findViewById(R.id.main_button_learn_subject1).setOnClickListener(v -> startLearnAndTestActivityForLearn(1));
        findViewById(R.id.main_button_learn_subject2).setOnClickListener(v -> startLearnAndTestActivityForLearn(2));

        findViewById(R.id.main_button_test_old).setOnClickListener(v -> startLearnAndTestActivityForTest(TEST_TYPE_VIEW_OR_CONTINUE_OLD_TEST));
        findViewById(R.id.main_button_test_new).setOnClickListener(v -> startLearnAndTestActivityForTest(TEST_TYPE_NEW_TEST));

        SettingsActivity.set_theme(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // handle options menu item selection
        switch (item.getItemId()) {
            case R.id.mainAct_menu_item_settings:
                startSettingsActivity();
                return true;
            case R.id.mainAct_menu_item_about:
                // startAboutActivity();  // todo
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startLearnAndTestActivityForLearn(int subjectIndex) {
        Intent intent = new Intent(this, LearnAndTestActivity.class);
        intent.putExtra(INTENT_EXTRA_KEY_SUBJECT_INDEX, subjectIndex);
        startActivity(intent);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startLearnAndTestActivityForTest(int test_type) {
        Intent intent = new Intent(this, LearnAndTestActivity.class);
        intent.putExtra(INTENT_EXTRA_KEY_TEST_OLD_OR_NEW, test_type);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // set the textview test-in-progress based on Model
        TextView textView = findViewById(R.id.main_textView_test_old_unfinished);

        ModelTest modelTest = new ModelTest(this);
        boolean load_successful = modelTest.load_test_data();
        if (!load_successful) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onResume: Load ModelTest not successful");
            textView.setVisibility(View.GONE);
            return;
        }

        if (!modelTest.is_test_in_progress()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);

            int num_questions_attempted = modelTest.get_num_attempted_questions();

            int num_total_seconds_remaining = modelTest.get_num_seconds_remaining();
            int num_minutes = num_total_seconds_remaining / 60;
            int num_seconds = num_total_seconds_remaining - num_minutes * 60;

            textView.setText(getString(
                    R.string.test_old_unfinished_details,
                    num_questions_attempted, num_minutes, num_seconds
                    ));
        }
    }
}
