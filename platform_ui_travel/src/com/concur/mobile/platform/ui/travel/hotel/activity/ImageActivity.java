/**
 * 
 */
package com.concur.mobile.platform.ui.travel.hotel.activity;

import java.net.URI;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.ImageListItem;
import com.concur.mobile.platform.ui.travel.util.Const;

/**
 * An extension of <code>Activity</code> for the purpose of viewing an image.
 * 
 * @author AndrewK
 */
public class ImageActivity extends Activity {

    private static final String CLS_TAG = ImageActivity.class.getSimpleName();

    /**
     * Contains a reference to the receipt image web view.
     */
    private WebView webView;
    // mainLayout is the child of the HorizontalScrollView ...
    private LinearLayout mainLayout;
    private List<ImageListItem> hotelimages;
    private View cell;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.full_image_view);

        // Grab a reference to the web view.
        mainLayout = (LinearLayout) findViewById(R.id.image_linearLayout);
        Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        hotelimages = ((List<ImageListItem>) bundle.getSerializable(Const.EXTRA_HOTEL_IMAGES));

        for (ImageListItem image : hotelimages) {
            cell = getLayoutInflater().inflate(R.layout.image_cell, null);

            final ImageView imageView = (ImageView) cell.findViewById(R.id._image);
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // do whatever you want ...
                    Toast.makeText(ImageActivity.this, (CharSequence) imageView.getTag(), Toast.LENGTH_SHORT).show();
                }
            });

            imageView.setTag("Image#");

            URI uri = URI.create(image.getHotelImage().image);
            ImageCache imgCache = ImageCache.getInstance(this);
            Bitmap bitmap = imgCache.getBitmap(uri, null);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

            mainLayout.addView(cell);
        }

        // Set the screen title
        getActionBar().setTitle(intent.getStringExtra(Const.EXTRA_IMAGE_TITLE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (webView != null) {
            // Enable on screen image zooming capabilities.
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

            Intent intent = getIntent();
            String urlStr = intent.getStringExtra(Const.EXTRA_IMAGE_URL);
            if (urlStr != null) {
                // URL url = null;
                // try {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onStart: url -> " + urlStr);
                // url = new URL(urlStr);
                // MOB-13759
                // Setting a WebViewClient ensures that all images are opened in the WebView and not in a browser
                webView.setWebViewClient(new WebViewClient());
                webView.loadUrl(urlStr);
                // } catch( MalformedURLException mlfUrlExc ) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: invalid URL", mlfUrlExc);
                // }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: missing URL string!");
            }
        }
    }

}
