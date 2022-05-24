package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    
    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "Settings";

    public static final String SHARED_PREFS_FILE_SETTINGS = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "SettingsPrefs";
    public static final String SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_system_language";
    public static final String SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "chosen_lang_if_not_use_system_lang";

    // all required settings
    // language
    private boolean use_system_language;
    private String code_of_language_choice_if_not_use_system_lang;
    // theme
    private int theme;
    // question traversal (at least one of the following must be true)
    private boolean use_buttons_for_traversal;
    private boolean use_swipe_for_traversal;
    // practice mode user choice validation event (at least one of the following must be true)
    private boolean use_check_button_in_practice_mode;
    private boolean auto_validation_on_choice_selection;
    // new test when an unfinished old test exists (todo feature)
    // private static final int START_A_NEW_TEST = 0;
    // private static final int CONTINUE_WITH_THE_UNFINISHED_TEST = 1;
    // private static final int ASK_USER = 2;
    // private int new_test_when_an_unfinished_old_test_exists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // fill in the choices for language spinner
        Spinner spinnerLanguage = findViewById(R.id.settings_spinner_language);
        // create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> languagesAdapter = ArrayAdapter.createFromResource(this,
                R.array.available_languages, android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        spinnerLanguage.setAdapter(languagesAdapter);
        // set the item selected listener
        spinnerLanguage.setOnItemSelectedListener(this);

        Switch switch_use_system_language = findViewById(R.id.settings_switch_use_system_language);
        switch_use_system_language.setOnClickListener(this::clicked_use_system_language_switch);

        // fill in the choices for theme
        Spinner spinnerTheme = findViewById(R.id.settings_spinner_theme);
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(this,
                R.array.available_themes, android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);
        spinnerTheme.setOnItemSelectedListener(this);

        // read existing settings from SharedPrefs
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE_SETTINGS, Context.MODE_PRIVATE);
        use_system_language = sharedPreferences.getBoolean(SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE, true);
        code_of_language_choice_if_not_use_system_lang = sharedPreferences.getString(SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG, "en");

        // fill the GUI with loaded settings

        // set use system language switch
        switch_use_system_language.setChecked(use_system_language);
        clicked_use_system_language_switch(null);  // to hide/show language choice spinner

        // set spinner choice
        String[] language_codes_array = getResources().getStringArray(R.array.available_languages_as_codes);
        for (int i = 0; i < language_codes_array.length; i++) {
            if (language_codes_array[i].equals(code_of_language_choice_if_not_use_system_lang)) {
                spinnerLanguage.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String new_choice = getResources().getStringArray(R.array.available_languages_as_codes)[position];
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("onItemSelected: selected:%s", new_choice));

        if (code_of_language_choice_if_not_use_system_lang.equals(new_choice)) return;

        code_of_language_choice_if_not_use_system_lang = new_choice;
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onItemSelected: Changing to new language");

        // todo update settings activity too
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void clicked_use_system_language_switch(View view) {
        use_system_language = ((Switch) findViewById(R.id.settings_switch_use_system_language)).isChecked();
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("clicked_use_system_language_switch: status: %b", use_system_language));

        if (use_system_language) {
            // hide language choice spinner
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clicked_use_system_language_switch: Hiding choice spinner");

            findViewById(R.id.settings_textView_choose_language).setVisibility(View.GONE);
            findViewById(R.id.settings_spinner_language).setVisibility(View.GONE);
        } else {
            // show language choice spinner
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clicked_use_system_language_switch: Showing choice spinner");

            findViewById(R.id.settings_textView_choose_language).setVisibility(View.VISIBLE);
            findViewById(R.id.settings_spinner_language).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // save settings
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE, use_system_language);
        editor.putString(SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG, code_of_language_choice_if_not_use_system_lang);
        editor.apply();
    }
}