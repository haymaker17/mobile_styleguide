package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import com.concur.core.R;

/**
 * A simple display activity called by {@link OpenSourceLicenseInfo} for displaying a license.
 * 
 * @author westonw
 * 
 */
public class OpenSourceLicenseDisplay extends Activity {

    private String LICENSE_TITLE_TAG = "license.title.tag";
    private String LICENSE_TEXT_TAG = "license.text.tag";

    public OpenSourceLicenseDisplay() {
        // no-op
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.open_source_license_display);

        String licenseName;
        String licenseText;

        Intent intent = getIntent();

        if (intent != null) {
            licenseName = intent.getStringExtra(LICENSE_TITLE_TAG);
            licenseText = intent.getStringExtra(LICENSE_TEXT_TAG);

            TextView view = (TextView) findViewById(R.id.osLicDisplayTitle);
            view.setText(licenseName);

            view = (TextView) findViewById(R.id.osLicDisplayText);
            view.setText(licenseText);
            Linkify.addLinks(view, Linkify.WEB_URLS);
        }

        super.onCreate(savedInstanceState);
    }

}
