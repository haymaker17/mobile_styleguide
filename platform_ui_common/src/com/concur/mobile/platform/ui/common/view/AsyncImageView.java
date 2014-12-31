package com.concur.mobile.platform.ui.common.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class AsyncImageView extends ImageView {

    // Private directory used to hold images
    private static final String CACHE_DIR = "ImageCache";

    // Our own log tag
    private static final String LOG_TAG = "AsyncImageView";

    // A remote URI for the image resource. Not be to be confused with ImageView.mUri
    private URI mAsyncUri;

    // Contains the destination file path into which the downloaded content is stored.
    private File destFilePath;

    // Contains a map from request header field names to values.
    private Map<String, String> headerMap;

    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Retrieve the value for the asynchronous image URI
     * 
     * @return A {@link URI} object
     */
    public URI getAsyncUri() {
        return mAsyncUri;
    }

    /**
     * Set the asynchronous image URI. If the image already exists in the cache then retrieve it and immediately set the view's
     * bitmap. If the image is not cached then immediately kick off a thread to go retrieve the image, cache it, then set the
     * view's bitmap.
     * 
     * @param uri
     *            The {@link URI} of the remote image.
     */
    public void setAsyncUri(URI uri) {
        mAsyncUri = uri;

        if (mAsyncUri != null) {
            // Check the cache for the image...
            Bitmap b = retrieveCachedImage();

            if (b != null) {
                // If it exists then set the bitmap/drawable for the view and be done
                this.setImageBitmap(b);
            } else {
                // Else, go fetch the image (asynchronously)
                new RemoteImageRetriever(this).start();
            }
        }

    }

    /**
     * Will set a destination file path into which the retrieved image should be stored. <br>
     * <b>NOTE:</b><br>
     * This method should be invoked prior to <code>setAsyncUri</code> to ensure <code>destFilePath</code> is first used.
     * 
     * @param destFilePath
     *            the destination file path for the image.
     */
    public void setDestinationFilePath(File destFilePath) {
        this.destFilePath = destFilePath;
    }

    /**
     * Adds a request header field/value pair to be sent with the request to retrieve the image.
     * 
     * @param name
     *            the request header field name.
     * @param value
     *            the request header field value.
     */
    public void addRequestHeader(String name, String value) {
        if (headerMap == null) {
            headerMap = new HashMap<String, String>();
        }
        headerMap.put(name, value);
    }

    /**
     * Retrieve a cached image as a {@link Bitmap}
     * 
     * @return
     */
    protected Bitmap retrieveCachedImage() {
        Bitmap bm = null;

        File cacheFile = getCacheFile();
        try {
            FileInputStream fis = new FileInputStream(cacheFile);
            bm = BitmapFactory.decodeFileDescriptor(fis.getFD());
        } catch (FileNotFoundException e) {
            // Do nothing.
        } catch (IOException e) {
            Log.w(LOG_TAG, "Error reading image cache file: " + cacheFile.getName(), e);
        }

        return bm;
    }

    protected void remoteImageRetrieved() {
        // Load the image from disk
        final Bitmap b = retrieveCachedImage();

        // Set it for our view
        this.post(new Runnable() {

            public void run() {
                AsyncImageView.this.setImageBitmap(b);
            }
        });
    }

    /**
     * Convert the URI into a usable cache filename
     */
    protected String getCacheFileName() {
        // The path should only ever have forward-slashes but replace both just in case.
        String fileName = mAsyncUri.getPath().replace('/', '_');
        fileName = fileName.replace('\\', '_');
        return fileName.toLowerCase();
    }

    protected File getCacheFile() {
        File cacheFile = null;
        if (destFilePath == null) {
            File cacheDir = getContext().getDir(CACHE_DIR, Context.MODE_PRIVATE);
            String fileName = getCacheFileName();
            cacheFile = new File(cacheDir, fileName);
        } else {
            cacheFile = destFilePath;
        }
        return cacheFile;
    }

    protected class RemoteImageRetriever extends Thread {

        private AsyncImageView mHandler;

        RemoteImageRetriever(AsyncImageView handler) {
            mHandler = handler;
        }

        public void run() {

            HttpURLConnection connection = null;
            // Set any specific request headers.

            File cacheFile = null;

            try {
                // Create and open the connection
                URL imageUrl = mHandler.mAsyncUri.toURL();
                connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(30000);

                if (headerMap != null && !headerMap.isEmpty()) {
                    Iterator<String> keyIter = headerMap.keySet().iterator();
                    while (keyIter.hasNext()) {
                        String key = keyIter.next();
                        connection.setRequestProperty(key, headerMap.get(key));
                    }
                }

                if (connection.getResponseCode() == HttpStatus.SC_OK) {
                    InputStream is = new BufferedInputStream(connection.getInputStream());

                    cacheFile = mHandler.getCacheFile();
                    FileOutputStream fos = new FileOutputStream(cacheFile);

                    final byte[] buffer = new byte[8192];

                    int readCount;
                    do {
                        readCount = is.read(buffer, 0, buffer.length);
                        if (readCount > 0) {
                            fos.write(buffer, 0, readCount);
                        }
                    } while (readCount >= 0);

                    fos.close();
                }

                mHandler.remoteImageRetrieved();

            } catch (FileNotFoundException e) {
                Log.w(LOG_TAG, "Failed to write cache file: " + cacheFile.getName(), e);
            } catch (ClientProtocolException e) {
                Log.w(LOG_TAG, "Failed to retrieve image using: " + mHandler.mAsyncUri.toASCIIString(), e);
            } catch (IOException e) {
                Log.w(LOG_TAG, "Failed to retrieve image using: " + mHandler.mAsyncUri.toASCIIString(), e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

            }

        }
    }

}
