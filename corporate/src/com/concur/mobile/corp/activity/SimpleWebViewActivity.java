package com.concur.mobile.corp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.concur.breeze.R;

public class SimpleWebViewActivity extends ActionBarActivity {

	private WebView simpleWebview;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.simple_webview);

		getSupportActionBar().setTitle("Partner Apps");
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		// getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_icon_back);

		simpleWebview = (WebView) findViewById(R.id.simple_webview);

		if (simpleWebview == null) {
			Toast.makeText(this, "Unable to load web", Toast.LENGTH_SHORT)
					.show();
		} else {
			simpleWebview.setWebViewClient(new WebViewClient());

			WebSettings settings = simpleWebview.getSettings();

			// This is needed for App Center.
			//
			settings.setJavaScriptEnabled(true);

			String url = getIntent().getStringExtra("url");

			simpleWebview.loadUrl(url);
		}
	}

	@Override
	public void onBackPressed() {
		if (simpleWebview.canGoBack()) {
			simpleWebview.goBack();
		} else {
			super.onBackPressed();
		}
	}

}
