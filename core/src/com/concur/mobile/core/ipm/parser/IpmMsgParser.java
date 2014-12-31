package com.concur.mobile.core.ipm.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ipm.data.IpmMsg;
import com.concur.mobile.core.ipm.data.IpmParams;
import com.google.gson.Gson;

/**
 * Parser for IPM Msg response
 * 
 * @author tejoa
 * 
 */
public class IpmMsgParser extends BaseParser {

    public static final String TAG_ARRAY_OF_IPM_MSGS = "ArrayOfIpmMsg";
    private static final String TAG_IPM_MSG = "IpmMsg";
    private static final String TAG_EXTERNAL_SRC = "ExternalSrc";
    private static final String TAG_EXTERNAL_SRC_TYPE = "ExternalSrcType";
    private static final String TAG_EXTERNAL_SRC_PARAMS = "ExternalSrcParams";

    private static final int TAG_ARRAY_OF_IPM_MSGS_CODE = 0;
    private static final int TAG_IPM_MSG_CODE = 1;
    private static final int TAG_EXTERNAL_SRC_CODE = 2;
    private static final int TAG_EXTERNAL_SRC_TYPE_CODE = 3;
    private static final int TAG_EXTERNAL_SRC_PARAMS_CODE = 4;

    /**
     * Contains the ipmMsgs Array.
     */
    public ArrayList<IpmMsg> ipmMsgs;

    /**
     * Contains the ipmMsg params.
     */
    public IpmMsg ipmMsg;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;
    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ARRAY_OF_IPM_MSGS, TAG_ARRAY_OF_IPM_MSGS_CODE);
        tagMap.put(TAG_IPM_MSG, TAG_IPM_MSG_CODE);
        tagMap.put(TAG_EXTERNAL_SRC, TAG_EXTERNAL_SRC_CODE);
        tagMap.put(TAG_EXTERNAL_SRC_TYPE, TAG_EXTERNAL_SRC_TYPE_CODE);
        tagMap.put(TAG_EXTERNAL_SRC_PARAMS, TAG_EXTERNAL_SRC_PARAMS_CODE);
    }

    /**
     * Contains the start tag used to register this parser.
     */
    private String startTag;

    /**
     * Constructs an instance of <code>CancelSegmentResponseParser</code> for parsing a UserConfig object.
     * 
     * @param parser
     *            contains a reference to a <code>CommonParser</code> object.
     * @param startTag
     *            contains the start tag used to register this parser.
     */
    public IpmMsgParser(CommonParser parser, String startTag) {

        // Set the start tag and register this parser.
        this.startTag = startTag;
        parser.registerParser(this, startTag);

    }

    @Override
    public void startTag(String tag) {
        if (tag.equalsIgnoreCase(TAG_ARRAY_OF_IPM_MSGS)) {
            ipmMsgs = new ArrayList<IpmMsg>();
        } else if (tag.equalsIgnoreCase(TAG_IPM_MSG)) {
            ipmMsg = new IpmMsg();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equalsIgnoreCase(TAG_EXTERNAL_SRC)) {
            ipmMsg.adUnitId = text;
        } else if (tag.equalsIgnoreCase(TAG_EXTERNAL_SRC_TYPE)) {
            ipmMsg.adType = text;
        } else if (tag.equalsIgnoreCase(TAG_EXTERNAL_SRC_PARAMS)) {
            // gson parser for extras
            Gson gson = new Gson();
            ipmMsg.params = gson.fromJson(text, IpmParams.class);
        }

    }

    @Override
    public void endTag(String tag) {
        if (tag.equalsIgnoreCase(TAG_IPM_MSG)) {
            ipmMsgs.add(ipmMsg);
        }
    }

}
