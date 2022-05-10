package com.ghsembrar.llrtestpreparation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    public static final String PACKAGE_NAME_FOR_KEY_PREFIX = "com.ghsembrar.llrtestpreparation.";
    public static final String INTENT_EXTRA_KEY_SUBJECT_INDEX = PACKAGE_NAME_FOR_KEY_PREFIX + "subject_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // AndroidStudio says onClick in xml is broken in older versions
        findViewById(R.id.main_button_learn_subject0).setOnClickListener(v -> startLearnActivity(0));
        findViewById(R.id.main_button_learn_subject1).setOnClickListener(v -> startLearnActivity(1));
        findViewById(R.id.main_button_learn_subject2).setOnClickListener(v -> startLearnActivity(2));
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

    private void startLearnActivity(int subjectIndex) {
        Intent intent = new Intent(this, LearnActivity.class);
        intent.putExtra(INTENT_EXTRA_KEY_SUBJECT_INDEX, subjectIndex);
        startActivity(intent);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}