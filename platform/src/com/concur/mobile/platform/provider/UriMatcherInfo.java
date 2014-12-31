package com.concur.mobile.platform.provider;

import java.util.Map;

import android.net.Uri;

/**
 * This class holds useful information about various items that are used in the 'insert', 'query', 'update' and 'delete' methods
 * and match on the various constants assigned at the top of the provider implementation.
 */
public class UriMatcherInfo {

    /**
     * Contains whether or not this instance is for an ID selection.
     */
    public boolean isIdSelection;

    /**
     * Contains the projection map.
     */
    public Map<String, String> projectionMap;

    /**
     * Contains the mime-type.
     */
    public String mimeType;

    /**
     * Contains the table name.
     */
    public String tableName;

    /**
     * Contains the path position reflecting a row ID in a URI.
     */
    public int idPathPosition;

    /**
     * Contains the column name storing the row ID.
     */
    public String idColumnName;

    /**
     * Contains the null column name.
     */
    public String nullColumnName;

    /**
     * Contains the content uri base.
     */
    public Uri contentIdUriBase;

    /**
     * Contains the default sort order.
     */
    public String defaultSortOrder;

    /**
     * Contains a reference to a <code>BulkInserter</code> used to perform bulk insertion operations.
     */
    public BulkInserter bulkInserter;

    /**
     * Contains a reference to a <code>FileOpener</code> used to perform a file open operation.
     */
    public FileOpener fileOpener;

}
