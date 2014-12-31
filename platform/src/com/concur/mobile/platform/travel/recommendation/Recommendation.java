/**
 * 
 */
package com.concur.mobile.platform.travel.recommendation;

import java.util.List;

import android.text.TextUtils;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for parsing recommendation data.
 */
public class Recommendation extends BaseParser {

    private static final String CLS_TAG = "Recommendation";

    private static final String TAG_TOTAL_SCORE = "TotalScore";

    /**
     * Contains the list of recommendation factors.
     */
    public List<Factor> factors;

    /**
     * Contains the recommendation total score.
     */
    public Double totalScore;

    @Override
    public void handleText(String tag, String text) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_TOTAL_SCORE)) {
                if (!TextUtils.isEmpty(text)) {
                    totalScore = Parse.safeParseDouble(text.trim());
                }
            }
        }
    }

}
