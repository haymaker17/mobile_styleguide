package com.concur.mobile.platform.travel.trip;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing trip enhancement offer objects.
 */
public class Offer extends BaseParser {

    private static final String CLS_TAG = "Offer";

    // Tags.
    private static final String TAG_ID = "Id";
    private static final String TAG_OFFER_DESCRIPTION = "OfferDescription";
    private static final String TAG_OFFER_TYPE = "OfferType";

    // Tag codes.
    private static final int TAG_ID_CODE = 0;
    private static final int TAG_OFFER_DESCRIPTION_CODE = 1;
    private static final int TAG_OFFER_TYPE_CODE = 2;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_ID, TAG_ID_CODE);
        tagMap.put(TAG_OFFER_DESCRIPTION, TAG_OFFER_DESCRIPTION_CODE);
        tagMap.put(TAG_OFFER_TYPE, TAG_OFFER_TYPE_CODE);
    }

    /**
     * Contains the id.
     */
    public String id;

    /**
     * Contains the description.
     */
    public String offerDescription;

    /**
     * Contains the type.
     */
    public String offerType;

    /**
     * Contains a link.
     */
    public OfferLink link;

    /**
     * Contains the offer content.
     */
    public OfferContent offerContent;

    /**
     * Contains the validity.
     */
    public Validity validity;

    /**
     * Contains the link parser.
     */
    private ItemParser<OfferLink> linkParser;

    /**
     * Contains the offer content parser.
     */
    private ItemParser<OfferContent> offerContentParser;

    /**
     * Contains the validity parser.
     */
    private ItemParser<Validity> validityParser;

    /**
     * Contains the start tag.
     */
    private String startTag;

    public Offer(CommonParser parser, String startTag) {
        this.startTag = startTag;

        // Create and register the link parser.
        String itemTag = "Link";
        linkParser = new ItemParser<OfferLink>(parser, itemTag, OfferLink.class);
        parser.registerParser(linkParser, itemTag);

        // Create and register the offer content parser.
        itemTag = "OfferContent";
        offerContentParser = new ItemParser<OfferContent>(parser, itemTag, OfferContent.class);
        parser.registerParser(offerContentParser, itemTag);

        // Create and register the offer validity parser.
        itemTag = "Validity";
        validityParser = new ItemParser<Validity>(parser, itemTag, Validity.class);
        parser.registerParser(validityParser, itemTag);
    }

    @Override
    public void handleText(String tag, String text) {
        if (!TextUtils.isEmpty(tag)) {
            Integer tagCode = tagMap.get(tag);
            if (tagCode != null) {
                switch (tagCode) {
                case TAG_ID_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        id = text.trim();
                    }
                    break;
                }
                case TAG_OFFER_DESCRIPTION_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        offerDescription = text.trim();
                    }
                    break;
                }
                case TAG_OFFER_TYPE_CODE: {
                    if (!TextUtils.isEmpty(text)) {
                        offerType = text.trim();
                    }
                    break;
                }

                }
            } else {
                if (Const.DEBUG_PARSING) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
                }
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {
                // Set the link.
                link = linkParser.getItem();
                // Set the offer content.
                offerContent = offerContentParser.getItem();
                // Set the validity.
                validity = validityParser.getItem();
            }
        }
    }

}
