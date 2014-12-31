package com.concur.mobile.platform.base;

import java.util.HashSet;

/**
 * A list item for use in a <code>ListView</code> which will show the user information about an Open Source License used by this
 * application. Can be used with {@link OpenSourceLicenseAdapter} to display a list of licenses.
 * 
 * @author westonw
 * 
 */
public class OpenSourceLicenseListItem {

    private int fileResId;
    private String licenseName;
    private String[] usedBy; // use arraylist? If we need to append items in corp.

    /**
     * Constructs a list item used to display information about an open source license used by the application.
     * 
     * @param fileResId
     *            The Resource ID of the raw file containing the license information.
     * @param licenseName
     *            The license name
     * @param usedBy
     *            The libraries that use this license
     */
    public OpenSourceLicenseListItem(int fileResId, String licenseName, String[] usedBy) {

        this.fileResId = fileResId;
        this.licenseName = licenseName;
        this.usedBy = usedBy;
    }

    /**
     * @return the Resource ID of the raw file containing the license information.
     */
    public int getFileResId() {
        return fileResId;
    }

    /**
     * Set the Resource ID of the raw file containing the license information.
     * 
     * @param fileResId
     */
    public void setFileResId(int fileResId) {
        this.fileResId = fileResId;
    }

    /**
     * @return the license name.
     */
    public String getLicenseName() {
        return licenseName;
    }

    /**
     * Set the license name.
     * 
     * @param licenseName
     */
    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    /**
     * @return a list of libraries the license is used by.
     */
    public String[] getUsedBy() {
        return usedBy;
    }

    /**
     * Set the list of libraries the license is used by.
     * 
     * @param usedBy
     *            The list of libraries the license is used by.
     */
    public void setUsedBy(String[] usedBy) {
        this.usedBy = usedBy;
    }

    /**
     * In the case that platform libraries use a license, this method will append additional libraries the license is used by.
     * 
     * @param additionalStrings
     *            Any additional libraries using the license.
     */
    public void appendUsedBy(String[] additionalStrings) {
        HashSet<String> newList = new HashSet<String>();
        for (String s : usedBy) {
            newList.add(s);
        }

        for (String s : additionalStrings) {
            newList.add(s);
        }

        usedBy = newList.toArray(new String[newList.size()]);
    }
}
