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
public class AreasPermissions extends BaseParser {
    private static final String CLS_TAG = "Areas";
    
    // Tags
    private static final String TAG_HAS_TR = "HasTravelRequest";

    private static final int TAG_HAS_TR_CODE = 1;

    // Tag codes.
    private static final Map<String, Integer> areasTagMap;
	
    static {
    	areasTagMap = new HashMap<String, Integer>();
        areasTagMap.put(TAG_HAS_TR, TAG_HAS_TR_CODE);
    }

    /**
     * True if TR is enabled for this user.
     */
    public Boolean hasTravelRequest = false;

    @Override
    public void handleText(String tag, String text) {
    	final Integer tagCode = areasTagMap.get(tag);
		if (!TextUtils.isEmpty(text)){
			if (tagCode.equals(TAG_HAS_TR_CODE))
				hasTravelRequest = Parse.safeParseBoolean(text.trim());
			else if (Const.DEBUG_PARSING) 
		        Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
		}
        else if (Const.DEBUG_PARSING) 
            Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: empty tag '" + tag + "'.");
    }

}
