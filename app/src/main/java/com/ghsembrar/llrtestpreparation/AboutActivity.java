package com.ghsembrar.llrtestpreparation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        WebView webView = findViewById(R.id.about_webView);
        String html = getString(R.string.about_app);
        String base_url = getString(R.string.base_url);
        webView.loadDataWithBaseURL(base_url, html, "text/html", null, base_url);
    }
}
