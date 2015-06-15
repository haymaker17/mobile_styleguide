package com.concur.mobile.platform.ui.common.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A singleton that manages image downloading and caching.
 * <p/>
 * This class handles the downloads in an asynchronous manner and uses broadcast messages to report status.
 */
public class ImageCache {

    private static final String CLS_TAG = "ImageCache";

    /**
     * Contains the action that broadcast receivers should listen to in order to determine result of an image download.
     */
    public static final String IMAGE_DOWNLOAD_ACTION = "com.concur.mobile.action.image.cache.download";

    /**
     * Contains the boolean extra used in <code>IMAGE_DOWNLOAD_ACTION</code> to report the download result.
     */
    public static final String EXTRA_IMAGE_DOWNLOAD_RESULT = "com.concur.mobile.extra.image.download.result";

    /**
     * Contains the java.net.URI extra (Serializable URI object) in <code>IMAGE_DOWNLOAD_ACTION</code> to report the downloaded
     * uri.
     */
    public static final String EXTRA_IMAGE_DOWNLOAD_URI = "com.concur.mobile.extra.image.download.uri";

    // Contains the name of the directory in which cached images are stored.
    private static final String CACHE_DIRECTORY = "image_cache";

    // Contains the singleton instance.
    private static ImageCache singleton;

    // Contains a reference to an application context.
    private Context context;

    // Contains a reference to the cache directory.
    private File cacheDirectory;

    // Contains a map from URI's to in-memory bitmap objects.
    private Map<URI, Bitmap> bitmapCache = new HashMap<URI, Bitmap>();

    // Contains a map from URI's to instances of RemoteImageRetriever to track in-process downloads.
    private Map<URI, RemoteImageRetriever> uriRetrieverMap = new HashMap<URI, RemoteImageRetriever>();

    /**
     * Constructs an instance of <code>ImageCache</code> with a given application context.
     *
     * @param context contains an application context.
     */
    private ImageCache(Context context) {
        this.context = context;
        initDownloadDirectory();
    }

    /**
     * Gets the singleton instance.
     *
     * @param context contains an application context.
     * @return returns an instance of <code>ImageCache</code>.
     */
    public static synchronized ImageCache getInstance(Context context) {
        if (singleton == null) {
            singleton = new ImageCache(context);

        }
        return singleton;
    }

    /**
     * Gets an instance of <code>Bitmap</code> for a URI.
     *
     * @param uri            contains the URI to be retrieved.
     * @param requestHeaders contains an optional set of request headers.
     * @return returns an instance of <code>Bitmap</code> if the content at <code>uri</code> has already been downloaded;
     * otherwise <code>null</code> is returned. If <code>null</code> is returned, then the class will perform an
     * asychronous download of the content and send a broadcast message with the action <code>IMAGE_DOWNLOAD_ACTION</code>
     * detailing the result.
     */
    public Bitmap getBitmap(URI uri, Map<String, String> requestHeaders) {
        Bitmap retVal = null;

        retVal = getBitmapFromCache(uri);

        // Third, perform an asynchronous download if the same uri is not already being downloaded.
        if (retVal == null) {
            if (!uriRetrieverMap.containsKey(uri)) {
                RemoteImageRetriever retriever = new RemoteImageRetriever(uri, requestHeaders);
                uriRetrieverMap.put(uri, retriever);
                retriever.start();
            }
        }
        return retVal;
    }

    /**
     * Gets an instance of <code>Bitmap</code> for a URI.
     *
     * @param uri contains the URI to be retrieved.
     * @return returns an instance of <code>Bitmap</code> if the content at <code>uri</code> has already been downloaded;
     */
    public Bitmap getBitmapFromCache(URI uri) {
        Bitmap retVal = null;

        // First, check the in-memory cache.
        if (bitmapCache.containsKey(uri)) {
            retVal = bitmapCache.get(uri);
        }
        // Second, check whether cache directory contains uri.
        if (retVal == null) {
            String cacheFileName = getCacheFileName(uri);
            File cacheFile = new File(cacheDirectory, cacheFileName);
            if (cacheFile.exists()) {
                retVal = loadBitmap(cacheFile);
                // Populate the in-memory cache if successfully loaded.
                if (retVal != null) {
                    bitmapCache.put(uri, retVal);
                }
            }
        }
        return retVal;
    }

    /**
     * will cache the bitmap
     *
     * @param uri
     * @param bitmap
     */

    public void setBitmapCache(URI uri, Bitmap bitmap) {
        if (bitmap != null && uri != null) {
            bitmapCache.put(uri, bitmap);
        }
    }

    /**
     * Will punt from in-memory and on-disk a bitmap referenced by <code>uri</code>.
     *
     * @param uri contains the uri of the image to be deleted.
     */
    public void deleteBitmap(URI uri) {

        // First, check the in-memory cache.
        if (bitmapCache.containsKey(uri)) {
            bitmapCache.remove(uri);
        }
        // Second, check whether cache directory contains uri.
        String cacheFileName = getCacheFileName(uri);
        File cacheFile = new File(cacheDirectory, cacheFileName);
        if (cacheFile.exists()) {
            try {
                if (!cacheFile.delete()) {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".deleteBitmap: unable to remove file '" + cacheFile.getAbsolutePath() + "'.");
                }
            } catch (SecurityException secExc) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".deleteBitmap: security exception removing file '" + cacheFile.getAbsolutePath()
                                + "'.");
            }
        }

    }

    /**
     * Will load a bitmap from a file in storage.
     *
     * @param file contains the file reference in which the bitmap data is stored.
     * @return returns an instance of <code>Bitmap</code> if <code>file</code> exists and can be successfully decoded.
     */
    protected Bitmap loadBitmap(File file) {
        Bitmap retVal = null;
        File absFile = file.getAbsoluteFile();
        if (absFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                //                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                //                bmpFactoryOptions.inJustDecodeBounds = true;
                retVal = BitmapFactory.decodeFileDescriptor(fis.getFD());
            } catch (FileNotFoundException e) {
                // Do nothing.
            } catch (IOException e) {
                Log.w(Const.LOG_TAG, "Error reading image cache file: " + file.getName(), e);
            } catch (java.lang.OutOfMemoryError e) {
                //retVal.recycle();
                Log.w(Const.LOG_TAG, "Java.lang.OutOfMemoryError: " + file.getName(), e);
            }
        }
        return retVal;
    }

    /**
     * Will initialize the downloads directory.
     */
    private void initDownloadDirectory() {

        // First, check whether the external files directory is available.
        if (ImageUtil.isExternalMediaMounted()) {
            cacheDirectory = context.getExternalFilesDir(CACHE_DIRECTORY);
        } else {
            cacheDirectory = new File(context.getFilesDir(), CACHE_DIRECTORY);
        }
        if (!cacheDirectory.exists()) {
            if (!cacheDirectory.mkdirs()) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".initDownloadDirectory: unable to initialize image cache directory!");
            }
        }
    }

    /**
     * Convert the URI into a usable cache filename
     */
    protected String getCacheFileName(URI uri) {
        // The path should only ever have forward-slashes but replace both just in case.
        String fileName = uri.getPath().replace('/', '_');
        fileName = fileName.replace('\\', '_');
        return fileName.toLowerCase();
    }

    /**
     * An extension of <code>Thread</code> for the purposes of downloading an image from a URL.
     */
    protected class RemoteImageRetriever extends Thread {

        // Contains the URI being retrieved.
        private URI uri;

        // Contains a map from request header names to their values.
        private Map<String, String> requestHeaders;

        /**
         * Constructs an instance of <code>RemoteImageRetriever</code> with a URI and a set of request headers.
         *
         * @param uri            contains the URI to fetch.
         * @param requestHeaders contains an option map of request headers.
         */
        RemoteImageRetriever(URI uri, Map<String, String> requestHeaders) {
            this.uri = uri;
            this.requestHeaders = requestHeaders;
        }

        public void run() {

            boolean result = false;

            HttpURLConnection connection = null;

            try {
                URL url = uri.toURL();
                // Start the connection
                try {
                    // from KITKAT default impl is OkHttpUrlConnection
                    if (Build.VERSION.SDK_INT < 19) {
                        OkHttpClient client = new OkHttpClient();
                        OkUrlFactory factory = new OkUrlFactory(client);
                        connection = factory.open(url);
                        Log.d(com.concur.mobile.base.util.Const.LOG_TAG,
                                getClass().getSimpleName() + " // SPDY is enabled // ");

                    } else {
                        connection = (HttpURLConnection) url.openConnection();
                    }
                } catch (IOException e) {
                    Log.e(com.concur.mobile.base.util.Const.LOG_TAG,
                            getClass().getSimpleName() + " // error opening connection // " + url, e);

                }

                connection.setConnectTimeout(5000);
                connection.setReadTimeout(30000);

                // Set any specific request headers.
                if (requestHeaders != null && !requestHeaders.isEmpty()) {
                    Iterator<String> keyIter = requestHeaders.keySet().iterator();
                    while (keyIter.hasNext()) {
                        String key = keyIter.next();
                        connection.setRequestProperty(key, requestHeaders.get(key));
                    }
                }

                BufferedOutputStream bufOut = null;
                BufferedInputStream bufIn = null;
                File cacheFile = null;
                try {

                    if (connection.getResponseCode() == HttpStatus.SC_OK) {
                        int bufSize = 8192;
                        bufIn = new BufferedInputStream(connection.getInputStream(), bufSize);
                        String cacheFileName = getCacheFileName(uri);
                        cacheFile = new File(cacheDirectory, cacheFileName);
                        bufOut = new BufferedOutputStream(new FileOutputStream(cacheFile), bufSize);
                        ViewUtil.writeAllBytes(bufIn, bufOut, bufSize);
                        bufOut.flush();
                        result = true;
                    }
                } catch (FileNotFoundException e) {
                    Log.w(Const.LOG_TAG, "Failed to write cache file: " + cacheFile.getName(), e);
                } catch (ClientProtocolException e) {
                    Log.w(Const.LOG_TAG, "Failed to retrieve image using: " + uri.toASCIIString(), e);
                } catch (IOException e) {
                    Log.w(Const.LOG_TAG, "Failed to retrieve image using: " + uri.toASCIIString(), e);
                } finally {
                    if (bufOut != null) {
                        ImageUtil.closeOutputStream(bufOut);
                        bufOut = null;
                    }
                    if (bufIn != null) {
                        ImageUtil.closeInputStream(bufIn);
                        bufIn = null;
                    }
                }
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } finally {
                // No matter how control leaves the upper-block, ensure we remove
                // this instance of RemoteImageRetriever from the map.
                uriRetrieverMap.remove(uri);

                // Send out a broadcast message about the result of the URI download.
                Intent intent = new Intent(IMAGE_DOWNLOAD_ACTION);
                intent.putExtra(EXTRA_IMAGE_DOWNLOAD_RESULT, result);
                intent.putExtra(EXTRA_IMAGE_DOWNLOAD_URI, uri);
                context.sendBroadcast(intent);
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }
    }

}
