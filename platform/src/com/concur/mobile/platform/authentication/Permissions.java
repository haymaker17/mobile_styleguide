package com.concur.mobile.platform.authentication;

import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.platform.util.Const;

/**
 * @author OlivierB
 */
public class Permissions extends BaseParser {
    private static final String CLS_TAG = "Permissions";
    
	public enum PermissionName{
		HAS_TRAVEL_REQUEST,
		TR_USER,
		TR_APPROVER;
	}

    // Tags
    private static final String TAG_AREAS = "Areas";
    private static final String TAG_TR = "TravelRequest";
    
    private ItemParser<AreasPermissions> areasItemParser;
    private ItemParser<TravelRequestPermissions> travelRequestItemParser;
    private String processedTag = null;
    private String debugTag = null;
    
    /**
     * Permissions objects
     */
    private AreasPermissions areasPermissions = null;
    private TravelRequestPermissions travelRequestPermissions = null;
    /**
     * Constructor in use on DB loading
     */
    public Permissions(){ }
    
    /**
     * Constructor in use on ws response parsing
     * @param cp
     * @param startTag
     */
    public Permissions(CommonParser cp, String startTag){
    	processedTag = startTag;
    	
        // Create and register the areas parser.
    	areasItemParser = new ItemParser<AreasPermissions>(TAG_AREAS, AreasPermissions.class);
        cp.registerParser(areasItemParser, TAG_AREAS);
    	
        // Create and register TR parser.
        travelRequestItemParser = new ItemParser<TravelRequestPermissions>(TAG_TR, TravelRequestPermissions.class);
        cp.registerParser(travelRequestItemParser, TAG_TR);
    }
    
    @Override
    public void startTag(String tag){
    	if (Const.DEBUG_PARSING && debugTag == null){
			if (!tag.equals(TAG_AREAS)
				&& !tag.equals(TAG_TR)){
		            Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
			}
			debugTag = tag;
    	}
    }

    @Override
    public void endTag(String tag) {
    	if (Const.DEBUG_PARSING && debugTag != null && debugTag.equals(tag)){
    		debugTag = null;
    	}
    	if (tag.equals(processedTag)){
			areasPermissions = areasItemParser.getItem();
			travelRequestPermissions = travelRequestItemParser.getItem();
    	}
    }

	public AreasPermissions getAreasPermissions() {
		if (areasPermissions == null)
			areasPermissions = new AreasPermissions();
		return areasPermissions;
	}

	public TravelRequestPermissions getTravelRequestPermissions() {
		if (travelRequestPermissions == null)
			travelRequestPermissions = new TravelRequestPermissions();
		return travelRequestPermissions;
	}
}
