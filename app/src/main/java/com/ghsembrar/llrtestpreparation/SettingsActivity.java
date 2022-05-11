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

    public static final String SHARED_PREFS_FILE_SETTINGS = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "SettingsPrefs";
    public static final String SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_system_language";
    public static final String SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "chosen_lang_if_not_use_system_lang";

    private boolean use_system_language;
    private String code_of_language_choice_if_not_use_system_lang;

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

        // read existing settings from SharedPrefs
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE_SETTINGS, Context.MODE_PRIVATE);
        use_system_language = sharedPreferences.getBoolean(SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE, true);
        code_of_language_choice_if_not_use_system_lang = sharedPreferences.getString(SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG, "en");

        Switch switch_use_system_language = findViewById(R.id.settings_switch_use_system_language);
        switch_use_system_language.setOnClickListener(this::clicked_use_system_language_switch);
        switch_use_system_language.setChecked(use_system_language);
        clicked_use_system_language_switch(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String new_choice = getResources().getStringArray(R.array.available_languages_as_codes)[position];
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, String.format("onItemSelected: selected:%s", new_choice)); }

        if (code_of_language_choice_if_not_use_system_lang.equals(new_choice)) return;

        code_of_language_choice_if_not_use_system_lang = new_choice;
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "onItemSelected: Changing to new language"); }

        // todo update settings activity too
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void clicked_use_system_language_switch(View view) {
        use_system_language = ((Switch) findViewById(R.id.settings_switch_use_system_language)).isChecked();
        if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, String.format("clicked_use_system_language_switch: status: %b", use_system_language)); }

        if (use_system_language) {
            // hide language choice spinner
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "clicked_use_system_language_switch: Hiding choice spinner"); }
            findViewById(R.id.settings_textView_choose_language).setVisibility(View.GONE);
            findViewById(R.id.settings_spinner_language).setVisibility(View.GONE);
        } else {
            // show language choice spinner
            if (CONSTANTS.ALLOW_DEBUG) { Log.i(CONSTANTS.LOG_TAG, "clicked_use_system_language_switch: Showing choice spinner"); }
            findViewById(R.id.settings_textView_choose_language).setVisibility(View.VISIBLE);
            findViewById(R.id.settings_spinner_language).setVisibility(View.VISIBLE);
        }
    }
}