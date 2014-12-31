package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.base.service.parser.ListParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for parsing offer content.
 */
public class OfferContent extends BaseParser {

    public static final String CLS_TAG = "OfferContent";

    private static final String TAG_TITLE = "Title";
    private static final String TAG_OFFER_VENDOR = "OfferVendor";
    private static final String TAG_OFFER_ACTION = "OfferAction";
    private static final String TAG_OFFER_APPLICATION = "OfferApplication";
    private static final String TAG_IMAGE_NAME = "ImageName";

    private static final int TAG_TITLE_CODE = 0;
    private static final int TAG_OFFER_VENDOR_CODE = 1;
    private static final int TAG_OFFER_ACTION_CODE = 2;
    private static final int TAG_OFFER_APPLICATION_CODE = 3;
    private static final int TAG_IMAGE_NAME_CODE = 4;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_TITLE, TAG_TITLE_CODE);
        tagMap.put(TAG_OFFER_VENDOR, TAG_OFFER_VENDOR_CODE);
        tagMap.put(TAG_OFFER_ACTION, TAG_OFFER_ACTION_CODE);
        tagMap.put(TAG_OFFER_APPLICATION, TAG_OFFER_APPLICATION_CODE);
        tagMap.put(TAG_IMAGE_NAME, TAG_IMAGE_NAME_CODE);
    }

    /**
     * Contains the title.
     */
    public String title;

    /**
     * Contains the vendor.
     */
    public String vendor;

    /**
     * Contains the action.
     */
    public String action;

    /**
     * Contains the application.
     */
    public String application;

    /**
     * Contains the image name.
     */
    public String imageName;

    /**
     * Contains the list of content links.
     */
    public List<ContentLink> contentLinks;

    /**
     * Contains a reference to a MapDisplay object.
     */
    public MapDisplay mapDisplay;

    /**
     * Contains the content link list parser.
     */
    private ListParser<ContentLink> contentLinkListParser;

    /**
     * Contains a reference to a MapDisplay parser.
     */
    private ItemParser<MapDisplay> mapDisplayParser;

    /**
     * Contains the parser start tag.
     */
    private String startTag;

    public OfferContent(CommonParser parser, String startTag) {

        this.startTag = startTag;

        // Create and register the content link list parser.
        String listTag = "Links";
        contentLinkListParser = new ListParser<ContentLink>(listTag, "Link", ContentLink.class);
        parser.registerParser(contentLinkListParser, listTag);

        // Create and register the map display parser.
        String itemTag = "MapDisplay";
        mapDisplayParser = new ItemParser<MapDisplay>(parser, itemTag, MapDisplay.class);
        parser.registerParser(mapDisplayParser, itemTag);
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            switch (tagCode) {
            case TAG_TITLE_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    title = text.trim();
                }
                break;
            }
            case TAG_OFFER_VENDOR_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    vendor = text.trim();
                }
                break;
            }
            case TAG_OFFER_ACTION_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    action = text.trim();
                }
                break;
            }
            case TAG_OFFER_APPLICATION_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    application = text.trim();
                }
                break;
            }
            case TAG_IMAGE_NAME_CODE: {
                if (!TextUtils.isEmpty(text)) {
                    imageName = text.trim();
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
                // Set the content link list.
                contentLinks = contentLinkListParser.getList();
                // Set the map display object.
                mapDisplay = mapDisplayParser.getItem();
            }
        }
    }

}
