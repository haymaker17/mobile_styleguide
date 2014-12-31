/**
 * 
 */
package com.concur.mobile.core.util.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

/**
 * An implementation of <code>Runnable</code> which will retrieve URL-based content and place the content in to an applications
 * private "file" area.
 * 
 * @author AndrewK
 */
public class ContentFetcher implements Runnable {

    private final String CLS_TAG = ContentFetcher.class.getSimpleName();

    /**
     * Contains the URL whose content will be fetched.
     */
    private URL url;

    // Use POST to make the image retrieve call. Needed for PDF from image server.
    private boolean usePost;

    /**
     * Contains the file name in which the content was written.
     */
    private String fileName;

    /**
     * Contains a file into which the downloaded content should be placed.
     */
    private File filePath;

    /**
     * Contains the session ID passed to retrieve the content.
     */
    private String sessionID;

    /**
     * Contains a reference to the listener to be notified of the result.
     */
    private ContentFetcher.ContentFetcherListener listener;

    /**
     * Contains a reference to an application context.
     */
    private Context context;

    /**
     * Contains whether the fetcher should cancel the download operation.
     */
    private boolean cancelled;

    /**
     * Constructs an instance of <code>ContentFetcher</code>
     * 
     * @param context
     *            the application context.
     * @param url
     *            the URL whose content is to be fetched.
     * @param sessionID
     *            the request session ID.
     * @param listener
     *            the listener to be notified when fetch is complete.
     */
    public ContentFetcher(Context context, URL url, String sessionID, ContentFetcher.ContentFetcherListener listener) {
        this(context, url, sessionID, listener, (String) null);
    }

    /**
     * Constructs an instance of <code>ContentFetcher</code>
     * 
     * @param context
     *            the application context.
     * @param url
     *            the URL whose content is to be fetched.
     * @param sessionID
     *            the request session ID.
     * @param listener
     *            the listener to be notified when fetch is complete.
     * @param fileName
     *            the file name into which the content should be placed. If the file name has no extension, one will be supplied.
     *            based on the mime-type.
     */
    public ContentFetcher(Context context, URL url, String sessionID, ContentFetcher.ContentFetcherListener listener,
            String fileName) {
        this.url = url;
        this.sessionID = sessionID;
        this.listener = listener;
        this.context = context;
        this.fileName = fileName;
    }

    /**
     * Constructs an instance of <code>ContentFetcher</code>
     * 
     * @param context
     *            the application context.
     * @param url
     *            the URL whose content is to be fetched.
     * @param usePost
     *            TODO
     * @param sessionID
     *            the request session ID.
     * @param listener
     *            the listener to be notified when fetch is complete.
     * @param filePath
     *            the file path into which the content should be placed.
     */
    public ContentFetcher(Context context, URL url, boolean usePost, String sessionID,
            ContentFetcher.ContentFetcherListener listener, File filePath) {
        this.url = url;
        this.usePost = usePost;
        this.sessionID = sessionID;
        this.listener = listener;
        this.context = context;
        this.filePath = filePath;
    }

    /**
     * Cancels any outstanding fetch and will call the 'fetchCancelled' method of the an instance of
     * <code>ContentFetcher.ContentFetcherListener</code>.
     */
    public void cancel() {
        setCancelled(true);
    }

    private synchronized void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private synchronized boolean isCancelled() {
        return cancelled;
    }

    /**
     * Will create a local file name based on examining the path part of <code>url</code>.
     * 
     * <b>NOTE: This implementation was lifted from <code>AsyncImageView.getCacheFileName</code>.</b>
     * 
     * @param url
     *            the URL from which to create a local file name.
     * @param fileName
     *            desired file name.
     * @param contentType
     *            content type as returned from URL.
     * 
     * @return a local file name.
     */
    private String getLocalFileName(URL url, String contentType) {

        String fileEnding = null;
        if (contentType != null) {
            int slashInd = contentType.indexOf('/');
            if (slashInd != -1 && slashInd < (contentType.length() - 1)) {
                fileEnding = "." + contentType.substring(slashInd + 1);
            }
        }
        String contentFileName = url.getPath().replace('/', '_');
        contentFileName = contentFileName.replace('\\', '_');
        if (fileEnding != null && !contentFileName.endsWith(fileEnding)) {
            contentFileName = contentFileName + fileEnding;
        }
        return contentFileName.toLowerCase();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        HttpURLConnection connection = null;
        // Create and open the connection
        try {

            boolean enableSpdy = Preferences.shouldEnableSpdy();

            if (enableSpdy && Build.VERSION.SDK_INT < 19) {
                OkHttpClient client = new OkHttpClient();
                // client.setCache(new Cache(cacheFolder.getRoot(), 10 * 1024 * 1024));
                OkUrlFactory factory = new OkUrlFactory(client);
                connection = factory.open(url);
                Log.d(Const.LOG_TAG, getClass().getSimpleName() + " // SPDY is enabled // ");

            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

            if (usePost) {
                connection.setDoOutput(true);

            } else {
                if (sessionID != null && sessionID.length() > 0) {
                    connection.setRequestProperty("X-SessionID", URLEncoder.encode(sessionID));
                }
            }

            if (!isCancelled()) {
                if (connection.getResponseCode() == HttpStatus.SC_OK) {
                    InputStream is = new BufferedInputStream(connection.getInputStream());
                    byte[] data = new byte[(8 * 1024)];
                    BufferedInputStream bufIn = null;
                    BufferedOutputStream bufOut = null;
                    try {
                        FileOutputStream fos = null;
                        bufIn = new BufferedInputStream(is, data.length);
                        if (filePath == null) {
                            if (fileName == null) {
                                fileName = getLocalFileName(url, connection.getContentType());
                            }
                            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                        } else {
                            fileName = filePath.getName();
                            fos = new FileOutputStream(filePath);
                        }
                        bufOut = new BufferedOutputStream(fos);
                        int dataRead;
                        long totalRead = 0L;
                        while ((dataRead = bufIn.read(data)) != -1 && !isCancelled()) {
                            totalRead += dataRead;
                            fos.write(data, 0, dataRead);
                        }
                        bufOut.flush();
                        bufOut.close();
                        bufOut = null;
                        if (listener != null) {
                            // Sanity check that any bytes were actually written to the file.
                            if (totalRead > 0L) {
                                if (!isCancelled()) {
                                    String localURL = null;
                                    if (filePath == null) {
                                        localURL = "file://" + context.getFileStreamPath(fileName).getPath();
                                    } else {
                                        localURL = filePath.toURL().toExternalForm();
                                    }
                                    listener.fetchSucceeded(localURL, fileName);
                                } else {
                                    Log.i(Const.LOG_TAG,
                                            CLS_TAG + ".run: fetch cancelled while retrieving content for URL '"
                                                    + url.toExternalForm() + "'.");
                                    if (listener != null) {
                                        listener.fetchCancelled(url);
                                    }
                                }
                            } else {
                                if (!isCancelled()) {
                                    if (listener != null) {
                                        listener.fetchFailed(connection.getResponseCode(), "Content-Length: is 0!");
                                    }
                                } else {
                                    Log.i(Const.LOG_TAG,
                                            CLS_TAG + ".run: fetch cancelled while retrieving content for URL '"
                                                    + url.toExternalForm() + "'.");
                                    if (listener != null) {
                                        listener.fetchCancelled(url);
                                    }
                                }
                            }
                        }
                    } finally {
                        // Ensure the input stream is closed.
                        if (bufIn != null) {
                            try {
                                bufIn.close();
                                bufIn = null;
                            } catch (IOException ioExc) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".run: I/O exception closing network input stream!",
                                        ioExc);
                            }
                        }
                        // Ensure the output stream is closed.
                        if (bufOut != null) {
                            try {
                                bufOut.close();
                                bufOut = null;
                            } catch (IOException ioExc) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".run: I/O exception closing file output stream!", ioExc);
                            }
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.fetchFailed(connection.getResponseCode(), connection.getResponseMessage());
                    }
                }
            } else {
                Log.i(Const.LOG_TAG,
                        CLS_TAG + ".run: fetch cancelled while retrieving content for URL '" + url.toExternalForm()
                                + "'.");
                if (listener != null) {
                    listener.fetchCancelled(url);
                }
            }
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG,
                    CLS_TAG + ".run: I/O exception while retrieving content for URL '" + url.toExternalForm() + "'.",
                    ioExc);
            if (listener != null) {
                listener.fetchFailed(-1, ioExc.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * An interface used to provide a notification of the results of the content fetch.
     * 
     * @author AndrewK
     */
    public interface ContentFetcherListener {

        /**
         * Provides a notification that the fetch succeeded providing the local URL to retrieve the content.
         * 
         * @param localURL
         *            the local URL containing the content.
         * @param localFile
         *            the file name containing the content.
         */
        public void fetchSucceeded(String localURL, String localFile);

        /**
         * Provides a notification that the fetch has failed providing the status code and message.
         * 
         * The status code will be one of the constants defined on the <code>HttpStatus</code>.
         * 
         * @param status
         *            the status code.
         * @param message
         *            the status message.
         */
        public void fetchFailed(int status, String message);

        /**
         * Provides a notification that the fetch was cancelled.
         * 
         * @param url
         *            the url associated with the fetch.
         */
        public void fetchCancelled(URL url);

    }

}
