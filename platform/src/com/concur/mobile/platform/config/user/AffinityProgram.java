package com.concur.mobile.platform.config.user;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of affinity program information.
 */
public class AffinityProgram extends BaseParser {

    private static final String CLS_TAG = "AffinityProgram";

    private static final String TAG_DESCRIPTION = "Description";
    private static final String TAG_VENDOR = "Vendor";
    private static final String TAG_VENDOR_ABBREV = "VendorAbbrev";
    private static final String TAG_PROGRAM_NAME = "ProgramName";
    private static final String TAG_PROGRAM_TYPE = "ProgramType";
    private static final String TAG_PROGRAM_ID = "ProgramId";
    private static final String TAG_EXPECTED_SELECTION = "ExpectedSelection";

    private static final int TAG_DESCRIPTION_CODE = 0;
    private static final int TAG_VENDOR_CODE = 1;
    private static final int TAG_VENDOR_ABBREV_CODE = 2;
    private static final int TAG_PROGRAM_NAME_CODE = 3;
    private static final int TAG_PROGRAM_TYPE_CODE = 4;
    private static final int TAG_PROGRAM_ID_CODE = 5;
    private static final int TAG_EXPECTED_SELECTION_CODE = 6;

    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_DESCRIPTION, TAG_DESCRIPTION_CODE);
        tagMap.put(TAG_VENDOR, TAG_VENDOR_CODE);
        tagMap.put(TAG_VENDOR_ABBREV, TAG_VENDOR_ABBREV_CODE);
        tagMap.put(TAG_PROGRAM_NAME, TAG_PROGRAM_NAME_CODE);
        tagMap.put(TAG_PROGRAM_TYPE, TAG_PROGRAM_TYPE_CODE);
        tagMap.put(TAG_PROGRAM_ID, TAG_PROGRAM_ID_CODE);
        tagMap.put(TAG_EXPECTED_SELECTION, TAG_EXPECTED_SELECTION_CODE);
    }

    /**
     * Contains the description.
     */
    public String description;

    /**
     * Contains the vendor name.
     */
    public String vendor;

    /**
     * Contains the vendor abbreviated name.
     */
    public String vendorAbbrev;

    /**
     * Contains the program name.
     */
    public String programName;

    /**
     * Contains the program type.
     */
    public String programType;

    /**
     * Contains the program id.
     */
    public String programId;

    /**
     * Contains whether this is the default affinity program.
     */
    public Boolean isDefault;

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_DESCRIPTION_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    description = text.trim();
                }
                break;
            }
            case TAG_VENDOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    vendor = text.trim();
                }
                break;
            }
            case TAG_VENDOR_ABBREV_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    vendorAbbrev = text.trim();
                }
                break;
            }
            case TAG_PROGRAM_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    programName = text.trim();
                }
                break;
            }
            case TAG_PROGRAM_TYPE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    programType = text.trim();
                }
                break;
            }
            case TAG_PROGRAM_ID_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    programId = text.trim();
                }
                break;
            }
            case TAG_EXPECTED_SELECTION_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    isDefault = Parse.safeParseBoolean(text.trim());
                }
                break;
            }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
