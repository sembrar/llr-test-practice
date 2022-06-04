package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
import android.widget.Toast;


public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    
    private static final String TAG = CONSTANTS.LOG_TAG_PREFIX + "Settings";

    public static final String SHARED_PREFS_FILE_SETTINGS = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "SettingsPrefs";
    public static final String SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "chosen_lang_if_not_use_system_lang";
    public static final String SHARED_PREFS_KEY_THEME = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "theme";
    public static final String SHARED_PREFS_KEY_USE_BUTTONS_FOR_TRAVERSAL = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_buttons_for_traversal";
    public static final String SHARED_PREFS_KEY_USE_SWIPE_FOR_TRAVERSAL = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_swipe_for_traversal";
    public static final String SHARED_PREFS_KEY_USE_CHECK_BUTTON_IN_PRACTICE_MODE = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_check_button_in_practice";

    private static final int[] themeRadioButtonIDS = {
            R.id.settings_radioButton_themeDark,
            R.id.settings_radioButton_themeLight,
            R.id.settings_radioButton_themeFollowSystem
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // fill in the choices for language spinner
        Spinner spinnerLanguage = findViewById(R.id.settings_spinner_language_for_questions);
        // create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> languagesAdapter = ArrayAdapter.createFromResource(this,
                R.array.available_languages, android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        spinnerLanguage.setAdapter(languagesAdapter);
        // set the item selected listener
        spinnerLanguage.setOnItemSelectedListener(this);

        findViewById(R.id.settings_radioButton_themeDark).setOnClickListener(v -> set_theme(0));
        findViewById(R.id.settings_radioButton_themeLight).setOnClickListener(v -> set_theme(1));
        findViewById(R.id.settings_radioButton_themeFollowSystem).setOnClickListener(v -> set_theme(2));
        findViewById(R.id.settings_button_restore_defaults).setOnClickListener(v -> clicked_reset_to_defaults());

        // ----
        load_settings_from_shared_prefs();

        final int[] tips_textViews = {
                R.id.settings_textView_info_language,
                R.id.settings_textView_info_theme,
                R.id.settings_textView_info_question_traversal,
                R.id.settings_textView_info_practice_mode_validation
        };
        final int[] tips_titles = {
                R.string.tip_setting_language_for_questions_title,
                R.string.tip_setting_theme_title,
                R.string.tip_setting_question_traversal_title,
                R.string.tip_setting_choice_validation_title
        };
        final int[] tips_messages = {
                R.string.tip_setting_language_for_questions_message,
                R.string.tip_setting_theme_message,
                R.string.tip_setting_question_traversal_message,
                R.string.tip_setting_choice_validation_message
        };
        for (int i = 0; i < tips_textViews.length; i++) {
            int tip_tView = tips_textViews[i];
            int tip_title = tips_titles[i];
            int tip_message = tips_messages[i];
            findViewById(tip_tView).setOnClickListener(v -> TipsUtility.show_tip(this, tip_title, tip_message));
        }

        findViewById(R.id.settings_checkBox_question_traversal_buttons).setOnClickListener(this::clickedSettingForTraversal);
        findViewById(R.id.settings_checkBox_question_traversal_swipe).setOnClickListener(this::clickedSettingForTraversal);
    }

    private static SharedPreferences get_application_context_shared_prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(SHARED_PREFS_FILE_SETTINGS, Context.MODE_PRIVATE);
    }

    public static String get_setting_code_of_language_choice(Context context) {
        return get_application_context_shared_prefs(context)
                .getString(SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG, "en");
    }

    private static int get_setting_theme(Context context) {
        return get_application_context_shared_prefs(context)
                .getInt(SHARED_PREFS_KEY_THEME, 0);
    }

    public static void set_theme(Context context) {
        set_theme(get_setting_theme(context));
    }

    private static void set_theme(int theme_index) {
        int theme_to_be_set;

        // the order of names in themes array in resources: dark, light, follow system
        switch (theme_index) {
            case 0:
                theme_to_be_set = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case 1:
                theme_to_be_set = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            default:
                theme_to_be_set = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(theme_to_be_set);
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

        int language_choice_position = ((Spinner) findViewById(R.id.settings_spinner_language_for_questions)).getSelectedItemPosition();
        editor.putString(
                SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG,
                getResources().getStringArray(R.array.available_languages_as_codes)[language_choice_position]);

        int theme_index = 2;  // 0: dark, 1: light, 2: follow system
        for (int i = 0; i < themeRadioButtonIDS.length; i++) {
            if (((RadioButton) findViewById(themeRadioButtonIDS[i])).isChecked()) {
                theme_index = i;
                break;
            }
        }
        editor.putInt(SHARED_PREFS_KEY_THEME, theme_index);

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
        // set spinner choice
        Spinner spinnerLanguage = findViewById(R.id.settings_spinner_language_for_questions);
        String[] language_codes_array = getResources().getStringArray(R.array.available_languages_as_codes);
        for (int i = 0; i < language_codes_array.length; i++) {
            if (language_codes_array[i].equals(get_setting_code_of_language_choice(this))) {
                spinnerLanguage.setSelection(i);
                break;
            }
        }

        // set theme radioButtons
        int theme_index = get_setting_theme(this);
        if (theme_index < 0 || theme_index >= themeRadioButtonIDS.length) theme_index = 2;  // 0: dark, 1: light, 2: follow system
        ((RadioButton) findViewById(themeRadioButtonIDS[theme_index])).setChecked(true);

        ((CheckBox) findViewById(R.id.settings_checkBox_question_traversal_buttons)).setChecked(get_setting_use_buttons_for_traversal(this));
        ((CheckBox) findViewById(R.id.settings_checkBox_question_traversal_swipe)).setChecked(get_setting_use_swipe_for_traversal(this));

        if (get_setting_use_check_button_in_practice_mode(this)) {
            ((RadioButton) findViewById(R.id.settings_radioButton_use_Check_button_in_practice)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.settings_radioButton_auto_validation_in_practice)).setChecked(true);
        }
    }

    private void reset_to_defaults() {
        SharedPreferences sharedPreferences = get_application_context_shared_prefs(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        load_settings_from_shared_prefs();
        set_theme(this);
    }

    private void clicked_reset_to_defaults() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.button_restore_defaults)
                .setMessage(R.string.alert_message_are_you_sure)
                .setPositiveButton(R.string.alert_button_yes, (dialog, which) -> reset_to_defaults())
                .setNegativeButton(R.string.alert_button_no, null)
                .create().show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.settings_spinner_language_for_questions) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onItemSelected: SpinnerLanguage:" + position);

        } else {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "onItemSelected: onItemSelected with unknown view");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onPause() {
        super.onPause();

        // save settings
        save_settings_to_shared_prefs();
    }

    private void clickedSettingForTraversal(View view) {
        CheckBox checkBox_traversal_buttons = findViewById(R.id.settings_checkBox_question_traversal_buttons);
        CheckBox checkBox_traversal_swipe = findViewById(R.id.settings_checkBox_question_traversal_swipe);

        // don't do anything if one or both are checked
        if (checkBox_traversal_buttons.isChecked() || checkBox_traversal_swipe.isChecked()) {
            if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clickedSettingForTraversal: At least one of them is checked");
            return;
        }

        if (CONSTANTS.ALLOW_DEBUG) Log.i(TAG, "clickedSettingForTraversal: Both are unChecked. Forcing the other to be checked");

        if (view.getId() == checkBox_traversal_buttons.getId()) {
            checkBox_traversal_swipe.setChecked(true);
        } else {
            checkBox_traversal_buttons.setChecked(true);
        }

        Toast.makeText(this, R.string.setting_toast_for_traversal_force_at_least_one, Toast.LENGTH_SHORT).show();
    }
}