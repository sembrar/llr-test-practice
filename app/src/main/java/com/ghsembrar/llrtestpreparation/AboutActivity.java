package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.webkit.WebView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ((TextView) findViewById(R.id.about_textView_source_code_text)).setMovementMethod(LinkMovementMethod.getInstance());
    }
}
