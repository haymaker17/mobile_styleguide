/**
 * 
 */
package com.concur.mobile.platform.travel.recommendation;

import com.concur.mobile.base.service.parser.BaseParser;

/**
 * An extension of <code>BaseParser</code> for parsing hotel recommendation information.
 */
public class Hotel extends BaseParser {

    private static final String CLS_TAG = "Hotel";

    /**
     * Contains the recommendation information.
     */
    public Recommendation recommendation;

}
