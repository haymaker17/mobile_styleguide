/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.core.util.Const;

/**
 * Models an image pair, i.e., thumbnail + full size.
 * 
 * @author AndrewK
 */
public class ImagePair {

    private static final String CLS_TAG = ImagePair.class.getSimpleName();

    public String thumbnail;
    public String image;

    public void writeToBundle(Bundle b, String tag) {
        Log.d(Const.LOG_TAG, "-- writing image to bundle: " + tag + "_thumb : " + thumbnail);
        Log.d(Const.LOG_TAG, "-- writing image to bundle: " + tag + "_image : " + image);

        b.putString(tag + "_thumb", thumbnail);
        b.putString(tag + "_image", image);
    }

    public static ImagePair createFromBundle(Bundle b, String tag) {
        ImagePair pair = new ImagePair();
        pair.thumbnail = b.getString(tag + "_thumb");
        pair.image = b.getString(tag + "_image");
        Log.d(Const.LOG_TAG, "-- read image from bundle: " + tag + "_thumb : " + pair.thumbnail);
        Log.d(Const.LOG_TAG, "-- read image from bundle: " + tag + "_image : " + pair.image);
        return pair;
    }

    public static class ImagePairSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ImagePair.CLS_TAG + "." + ImagePairSAXHandler.class.getSimpleName();

        private static final String IMAGE = "Image";
        private static final String THUMBNAIL = "Thumbnail";
        private static final String IMAGE_PAIR = "ImagePair";

        private StringBuilder chars;

        // The list of pairs that have been parsed.
        private ArrayList<ImagePair> imagePairs;

        // The pair currently being parsed.
        private ImagePair imagePair;

        /**
         * Constructs an instance of <code>ImagePairSAXHandler</code> used to parse a list of image pair objects.
         */
        public ImagePairSAXHandler() {
            chars = new StringBuilder();
            imagePairs = new ArrayList<ImagePair>();
        }

        /**
         * Gets the parsed list of image pairs.
         * 
         * @return the parsed list of image pairs.
         */
        public ArrayList<ImagePair> getImagePairs() {
            return imagePairs;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equalsIgnoreCase(IMAGE_PAIR)) {
                imagePair = new ImagePair();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (imagePair != null) {
                if (localName.equalsIgnoreCase(IMAGE_PAIR)) {
                    imagePairs.add(imagePair);
                    imagePair = null;
                } else if (localName.equalsIgnoreCase(IMAGE)) {
                    imagePair.image = chars.toString().trim();
                } else if (localName.equalsIgnoreCase(THUMBNAIL)) {
                    imagePair.thumbnail = chars.toString().trim();
                } else {
                    // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML tag '" + localName + "' with value '" +
                    // chars.toString() + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: imagePair is null!");
            }
            chars.setLength(0);
        }

    }

}
