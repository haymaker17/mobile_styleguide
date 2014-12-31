package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purposes of parsing booking passenger information.
 */
public class Passenger extends BaseParser {

    private static final String CLS_TAG = "Passenger";

    private static final String TAG_FIRST_NAME = "NameFirst";
    private static final String TAG_LAST_NAME = "NameLast";
    private static final String TAG_NAME_IDENTIFIER = "NameIdentifier";
    private static final String TAG_PASSENGER_KEY = "PassengerKey";

    private static final int TAG_FIRST_NAME_CODE = 0;
    private static final int TAG_LAST_NAME_CODE = 1;
    private static final int TAG_NAME_IDENTIFIER_CODE = 2;
    private static final int TAG_PASSENGER_KEY_CODE = 3;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_FIRST_NAME, TAG_FIRST_NAME_CODE);
        tagMap.put(TAG_LAST_NAME, TAG_LAST_NAME_CODE);
        tagMap.put(TAG_NAME_IDENTIFIER, TAG_NAME_IDENTIFIER_CODE);
        tagMap.put(TAG_PASSENGER_KEY, TAG_PASSENGER_KEY_CODE);
    }

    /**
     * Contains the list of frequent traveler programs.
     */
    public List<FrequentTravelerProgram> frequentTravelerPrograms;

    /**
     * Contains the first name.
     */
    public String firstName;

    /**
     * Contains the last name.
     */
    public String lastName;

    /**
     * Contains the name identifier.
     */
    public String nameIdentifier;

    /**
     * Contains the passenger key.
     */
    public String passengerKey;

    /**
     * Contains the frequent traveler program list parser.
     */
    private ListParser<FrequentTravelerProgram> frequentTravelerProgramListParser;

    /**
     * Contains the start tag for this parser.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>Passenger</code> in order to parse passenger information.
     * 
     * @param parser
     *            contains a reference to a <code>CommonParser</code> instance.
     * @param startTag
     *            contains the start tag for this parser.
     */
    public Passenger(CommonParser parser, String startTag) {

        this.startTag = startTag;

        // Create and register the frequent traveler program list parser.
        String listTag = "FrequentTravelerPrograms";
        frequentTravelerProgramListParser = new ListParser<FrequentTravelerProgram>(listTag, "FrequentTravelerProgram",
                FrequentTravelerProgram.class);
        parser.registerParser(frequentTravelerProgramListParser, listTag);

    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_FIRST_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    firstName = text.trim();
                }
                break;
            }
            case TAG_LAST_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    lastName = text.trim();
                }
                break;
            }
            case TAG_NAME_IDENTIFIER_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    nameIdentifier = text.trim();
                }
                break;
            }
            case TAG_PASSENGER_KEY_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    passengerKey = text.trim();
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

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Set the frequent traveler program list.
                frequentTravelerPrograms = frequentTravelerProgramListParser.getList();
            }
        }
    }

}
