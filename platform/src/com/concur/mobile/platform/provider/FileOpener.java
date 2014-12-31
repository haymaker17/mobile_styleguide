/**
 * 
 */
package com.concur.mobile.platform.provider;

import java.io.FileNotFoundException;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * Provides an interface that can be used to open a file for a content provider.
 */
public interface FileOpener {

    /**
     * Will return an instance of <code>ParcelFileDescriptor</code> that can be used to read/write data from/to <code>uri</code>.
     * 
     * @param context
     *            contains an application context.
     * @param db
     *            contains a reference to the platform sqlite database.
     * @param uri
     *            contains the uri to read/write from/to.
     * @param mode
     *            contains the mode.
     * @return an instance of <code>ParcelFileDescriptor</code> that can be used to read/write data from/to <code>uri</code>.
     * @throws FileNotFoundException
     *             a <code>FileNotFoundException</code> if <code>uri</code> is not found, or data can't be read from, or written
     *             to <code>uri</code>.
     */
    public ParcelFileDescriptor openFile(Context context, PlatformSQLiteDatabase db, Uri uri, String mode)
            throws FileNotFoundException;

}
