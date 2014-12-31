package com.concur.mobile.platform.authentication;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * @author OlivierB
 *
 */
public class TravelRequestPermissions extends BaseParser {
    private static final String CLS_TAG = "TravelRequest";

    // Tags
    private static final String TAG_IS_REQUEST_APPROVER = "IsRequestApprover";
    private static final String TAG_IS_REQUEST_USER = "IsRequestUser";

    private static final int TAG_IS_REQUEST_APPROVER_CODE = 1;
    private static final int TAG_IS_REQUEST_USER_CODE = 2;

    // Tag codes.
    private static final Map<String, Integer> trTagMap;
	
    static {
    	trTagMap = new HashMap<String, Integer>();
        trTagMap.put(TAG_IS_REQUEST_APPROVER, TAG_IS_REQUEST_APPROVER_CODE);
        trTagMap.put(TAG_IS_REQUEST_USER, TAG_IS_REQUEST_USER_CODE);
    }

    /**
     * True if the TR user is an approver.
     */
    public Boolean isRequestApprover = false;

    /**
     * True if this is a TR user.
     */
    public Boolean isRequestUser = false;

    @Override
    public void handleText(String tag, String text) {
		final Integer tagCode = trTagMap.get(tag);
		
		if (!TextUtils.isEmpty(text)){
			if (tagCode.equals(TAG_IS_REQUEST_APPROVER_CODE))
				isRequestApprover = Parse.safeParseBoolean(text.trim());
			else if (tagCode.equals(TAG_IS_REQUEST_USER_CODE))
				isRequestUser = Parse.safeParseBoolean(text.trim());
			else if (Const.DEBUG_PARSING) 
	            Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
	    } else if (Const.DEBUG_PARSING) 
            Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: empty tag '" + tag + "'.");
    }
}
