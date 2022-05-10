package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    public static final String SHARED_PREFS_FILE_SETTINGS = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "SettingsPrefs";
    public static final String SHARED_PREFS_KEY_USE_SYSTEM_LANGUAGE = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "use_system_language";
    public static final String SHARED_PREFS_KEY_CHOSEN_LANGUAGE_IF_NOT_USE_SYSTEM_LANG = CONSTANTS.PACKAGE_NAME_FOR_PREFIX + "chosen_lang_if_not_use_system_lang";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}