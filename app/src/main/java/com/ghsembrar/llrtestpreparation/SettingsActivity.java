package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    
    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "Settings";

    public static final String SHARED_PREFS_FILE_SETTINGS = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "SettingsPrefs";
    public static final String SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_system_language";
    public static final String SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "chosen_lang_if_not_use_system_lang";
    public static final String SHARED_PREFS_KEY_THEME = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "theme";
    public static final String SHARED_PREFS_KEY_USE_BUTTONS_FOR_TRAVERSAL = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_buttons_for_traversal";
    public static final String SHARED_PREFS_KEY_USE_SWIPE_FOR_TRAVERSAL = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_swipe_for_traversal";
    public static final String SHARED_PREFS_KEY_USE_CHECK_BUTTON_IN_PRACTICE_MODE = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_check_button_in_practice";

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
        switch_use_system_language.setOnClickListener(v -> clicked_use_system_language_switch());

        // fill in the choices for theme
        Spinner spinnerTheme = findViewById(R.id.settings_spinner_theme);
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(this,
                R.array.available_themes, android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);
        spinnerTheme.setOnItemSelectedListener(this);

        // ----
        load_settings_from_shared_prefs();
    }

    private static SharedPreferences get_application_context_shared_prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(SHARED_PREFS_FILE_SETTINGS, Context.MODE_PRIVATE);
    }

    public static boolean get_setting_use_system_language(Context context) {
        return get_application_context_shared_prefs(context)
                .getBoolean(SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE, true);
    }

    public static String get_setting_code_of_language_choice(Context context) {
        return get_application_context_shared_prefs(context)
                .getString(SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG, "en");
    }

    public static int get_setting_theme(Context context) {
        return get_application_context_shared_prefs(context)
                .getInt(SHARED_PREFS_KEY_THEME, 0);
    }

    public static boolean get_setting_use_buttons_for_traversal(Context context) {
        return get_application_context_shared_prefs(context)
                .getBoolean(SHARED_PREFS_KEY_USE_BUTTONS_FOR_TRAVERSAL, true);
    }

    public static boolean get_setting_use_swipe_for_traversal(Context context) {
        return get_application_context_shared_prefs(context)
                .getBoolean(SHARED_PREFS_KEY_USE_SWIPE_FOR_TRAVERSAL, true);
    }

    public static boolean get_setting_use_check_button_in_practice_mode(Context context) {
        return get_application_context_shared_prefs(context)
                .getBoolean(SHARED_PREFS_KEY_USE_CHECK_BUTTON_IN_PRACTICE_MODE, false);
    }

    private void save_settings_to_shared_prefs() {
        SharedPreferences sharedPreferences = get_application_context_shared_prefs(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(
                SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE,
                ((Switch) findViewById(R.id.settings_switch_use_system_language)).isChecked());

        int language_choice_position = ((Spinner) findViewById(R.id.settings_spinner_language)).getSelectedItemPosition();
        editor.putString(
                SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG,
                getResources().getStringArray(R.array.available_languages_as_codes)[language_choice_position]);

        editor.putInt(
                SHARED_PREFS_KEY_THEME,
                ((Spinner) findViewById(R.id.settings_spinner_theme)).getSelectedItemPosition());

        editor.putBoolean(
                SHARED_PREFS_KEY_USE_BUTTONS_FOR_TRAVERSAL,
                ((CheckBox) findViewById(R.id.settings_checkBox_question_traversal_buttons)).isChecked());

        editor.putBoolean(
                SHARED_PREFS_KEY_USE_SWIPE_FOR_TRAVERSAL,
                ((CheckBox) findViewById(R.id.settings_checkBox_question_traversal_swipe)).isChecked());

        editor.putBoolean(
                SHARED_PREFS_KEY_USE_CHECK_BUTTON_IN_PRACTICE_MODE,
                ((RadioButton) findViewById(R.id.settings_radioButton_use_Check_button_in_practice)).isChecked());

        editor.apply();
    }

    private void load_settings_from_shared_prefs() {
        // set use system language switch
        Switch switch_use_system_language = findViewById(R.id.settings_switch_use_system_language);
        switch_use_system_language.setChecked(get_setting_use_system_language(this));
        clicked_use_system_language_switch();  // to hide/show language choice spinner

        // set spinner choice
        Spinner spinnerLanguage = findViewById(R.id.settings_spinner_language);
        String[] language_codes_array = getResources().getStringArray(R.array.available_languages_as_codes);
        for (int i = 0; i < language_codes_array.length; i++) {
            if (language_codes_array[i].equals(get_setting_code_of_language_choice(this))) {
                spinnerLanguage.setSelection(i);
                break;
            }
        }

        // set spinner theme
        ((Spinner) findViewById(R.id.settings_spinner_theme)).setSelection(get_setting_theme(this));

        ((CheckBox) findViewById(R.id.settings_checkBox_question_traversal_buttons)).setChecked(get_setting_use_buttons_for_traversal(this));
        ((CheckBox) findViewById(R.id.settings_checkBox_question_traversal_swipe)).setChecked(get_setting_use_swipe_for_traversal(this));

        if (get_setting_use_check_button_in_practice_mode(this)) {
            ((RadioButton) findViewById(R.id.settings_radioButton_use_Check_button_in_practice)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.settings_radioButton_auto_validation_in_practice)).setChecked(true);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String new_choice = getResources().getStringArray(R.array.available_languages_as_codes)[position];
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, String.format("onItemSelected: selected:%s", new_choice));

        String code_of_language_choice_if_not_use_system_lang = get_setting_code_of_language_choice(this);

        if (code_of_language_choice_if_not_use_system_lang.equals(new_choice)) return;

        code_of_language_choice_if_not_use_system_lang = new_choice;
        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onItemSelected: Changing to new language");

        // todo update settings activity too
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void clicked_use_system_language_switch() {
        boolean use_system_language = ((Switch) findViewById(R.id.settings_switch_use_system_language)).isChecked();
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
        save_settings_to_shared_prefs();
    }
}