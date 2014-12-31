package com.concur.mobile.core.travel.hotel.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.concur.mobile.platform.util.Parse;

public class HotelRecommendation {

    // to identify the recommendation type
    public static enum RecommendationSourceEnum {
        Unknown, UserStay, CompanyStay, UserFavorite, CompanyFavorite, CompanyPreferred, ItemRecommendation, MeetingRecommendation
    };

    public static class RecommendationSAXHandler extends DefaultHandler {

        private static final String TOTAL_SCORE = "TotalScore";
        private static final String SOURCE = "Source";
        private static final String DISPLAY_VALUE = "DisplayValue";

        protected StringBuilder chars;
        protected boolean elementHandled;
        private static RecommendationSourceEnum recomSourceEnum;
        private double recommendationScore;
        private long recommendationSourceNumber;
        private Long recomSrcNumber; // temporary fix as the MWS end point is sending double value

        RecommendationSAXHandler() {
            chars = new StringBuilder();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            elementHandled = false;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            elementHandled = false;
            if (localName.equalsIgnoreCase(TOTAL_SCORE)) {
                recommendationScore = Parse.safeParseDouble(chars.toString().trim());
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(SOURCE)) {
                try {
                    // temporary fix, need to be removed after the MWS end point changes
                    recomSourceEnum = RecommendationSourceEnum.valueOf(chars.toString().trim());
                } catch (IllegalArgumentException enumExc) {
                    recomSourceEnum = RecommendationSourceEnum.Unknown;
                }
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(DISPLAY_VALUE)) {
                // temporary fix as the MWS end point is sending value like 5.53696521434058
                recomSrcNumber = Parse.safeParseLong(chars.toString().trim());
                recommendationSourceNumber = (recomSrcNumber == null ? 0 : recomSrcNumber);
                elementHandled = true;
            }

            // Clear out the parsed characters if the parser instance is of this class.
            if (this.getClass().equals(RecommendationSAXHandler.class)) {
                chars.setLength(0);
            }
        }

        public RecommendationSourceEnum getSourceEnum() {
            return recomSourceEnum;
        }

        public double getScore() {
            return recommendationScore;
        }

        public long getSourceNumber() {
            return recommendationSourceNumber;
        }

    }

}
