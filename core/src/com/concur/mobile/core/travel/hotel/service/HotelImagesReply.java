/**
 * 
 */
package com.concur.mobile.core.travel.hotel.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.os.Bundle;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.ImagePair;
import com.concur.mobile.core.travel.data.ImagePair.ImagePairSAXHandler;

public class HotelImagesReply extends ServiceReply {

    protected final static String KEY_COUNT = "hotel.images.count";
    protected final static String KEY_PAIR_PREFIX = "hotel.images.pair_";

    public ArrayList<ImagePair> imagePairs;

    public Bundle getImagePairBundle() {
        Bundle b = new Bundle();

        int imageCount = 0;
        if (imagePairs != null) {
            imageCount = imagePairs.size();
        }

        b.putInt(KEY_COUNT, imageCount);

        if (imageCount > 0) {
            StringBuilder sb;
            int imageNum = 0;

            for (ImagePair pair : imagePairs) {
                sb = new StringBuilder(KEY_PAIR_PREFIX);
                sb.append(imageNum);
                pair.writeToBundle(b, sb.toString());
                imageNum++;
            }
        }

        return b;
    }

    public static ArrayList<ImagePair> getImagesFromBundle(Bundle b) {
        ArrayList<ImagePair> pairs = null;

        int imageCount = b.getInt(KEY_COUNT, 0);
        if (imageCount > 0) {
            StringBuilder sb;
            pairs = new ArrayList<ImagePair>(imageCount);
            for (int imageNum = 0; imageNum < imageCount; imageNum++) {
                sb = new StringBuilder(KEY_PAIR_PREFIX);
                sb.append(imageNum);
                pairs.add(ImagePair.createFromBundle(b, sb.toString()));
            }
        }

        return pairs;
    }

    public static HotelImagesReply parseXMLReply(String responseXml) {

        HotelImagesReply reply = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ImagePairSAXHandler handler = new ImagePairSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            reply = new HotelImagesReply();
            reply.imagePairs = handler.getImagePairs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return reply;
    }

}
