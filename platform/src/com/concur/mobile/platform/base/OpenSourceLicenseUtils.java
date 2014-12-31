package com.concur.mobile.platform.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;

import com.concur.mobile.base.util.IOUtils;
import com.concur.mobile.platform.R;

/**
 * A general utility class surrounding the use of Open Source Library items.
 * 
 * @author westonw
 * 
 */
public class OpenSourceLicenseUtils {

    // Don't instantiate a utility class!
    private OpenSourceLicenseUtils() {
    }

    /**
     * Builds a <code>List</code> of {@link OpenSourceLibraryItem} items which contains all open source licenses, and libraries
     * using them, that are used in Concur Platform. <br>
     * <br>
     * This list can be appended, adding any additional licenses used in other projects. Individual list items can be edited to
     * append additional libraries using a license used in Platform to avoid showing the same license multiple times.
     * 
     * @return The <code>List</code> of items containing licenses used in Platform.
     */
    public static List<OpenSourceLicenseListItem> getPlatformOpenSrcLicListItems() {
        List<OpenSourceLicenseListItem> openSrcLicListItems = new ArrayList<OpenSourceLicenseListItem>();

        openSrcLicListItems.add(new OpenSourceLicenseListItem(R.raw.apache_2_0_license, "Apache License", new String[] {
                "Apache Commons", "Guava", "SQLCipher" }));
        openSrcLicListItems.add(new OpenSourceLicenseListItem(R.raw.sqlcipher_community_license,
                "SQLCipher Community License", new String[] { "Concur" }));
        openSrcLicListItems.add(new OpenSourceLicenseListItem(R.raw.icu_license, "ICU License",
                new String[] { "SQLCipher" }));
        openSrcLicListItems.add(new OpenSourceLicenseListItem(R.raw.gson_license, "Google Gson License",
                new String[] { "Concur" }));

        return openSrcLicListItems;
    }

    /**
     * Reads the license information from a file and returns it as a String.
     * 
     * @param resources
     *            Resources instance for the package calling this method.
     * @param fileResId
     *            Resource ID for the File that is to be read.
     * @return A <code>String</code> containing the license information contained in the text file.
     */
    public static String readLicenseFile(Resources resources, int fileResId) {

        if (resources != null) {
            InputStream is = resources.openRawResource(fileResId);
            if (is != null) {
                try {
                    return IOUtils.readStream(is, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
