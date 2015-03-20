package com.concur.mobile.corp.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.SslErrorHandler;
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

		getSupportActionBar().setTitle(R.string.home_navigation_app_center);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		// getSupportActionBar().setHomeAsUpIndicator(R.drawable.nav_icon_back);

		enableDebugMode();

		simpleWebview = (WebView) findViewById(R.id.simple_webview);

		if (simpleWebview == null) {
			Toast.makeText(this, "Unable to load web", Toast.LENGTH_SHORT)
					.show();
		} else {
			simpleWebview.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.startsWith("market://")) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(url));
						startActivity(intent);
						
						return true;
					}

					return false;
				}
				
				@Override
			    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			        handler.proceed();
			    }
			});

			WebSettings settings = simpleWebview.getSettings();

			// This is needed for App Center.
			//
			settings.setJavaScriptEnabled(true);
			settings.setDomStorageEnabled(true);

			String url = getIntent().getStringExtra("url");

			simpleWebview.setVerticalScrollBarEnabled(false);
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

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void enableDebugMode() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
	}

}
