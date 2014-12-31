package com.concur.mobile.platform.travel.search.hotel;

import java.util.ArrayList;

import android.util.Log;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.platform.travel.search.hotel.HotelRecommendation.RecommendationSourceEnum;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * 
 * @author RatanK
 * 
 */
public class HotelChoicesParser implements Parser {

    private static final String CLS_TAG = HotelChoicesParser.class.getSimpleName();

    public ArrayList<HotelChoice> choices;

    private HotelChoice choice;

    private HotelRoom cheapestRoom;
    private boolean inCheapestRoom;

    private HotelRoomWithViolation cheapestRoomWithViolation;
    private boolean inCheapestRoomWithViolation;

    private Violation violation;
    private boolean inViolation;
    private boolean inViolations;

    private boolean inRecommendation;
    private boolean thisChoiceHasRecommendation;

    private ImagePair imagePair;
    private boolean inImagePair;
    private boolean inPropertyImages;

    // Contains the start index of this result set into the entire list of cached results on the server.
    public Integer startIndex;

    // Contains the total count of cached results on the server.
    public Integer totalCount;

    // Contains the number of results returned in this reply.
    public Integer length;

    // hotel list has a recommendation - to be used to identify the default sorting of hotel search list
    public boolean hasRecommendation;

    public String pollingId;

    public boolean isFinal;

    @Override
    public void startTag(String tag) {
        if (tag.equalsIgnoreCase("Choices")) {
            choices = new ArrayList<HotelChoice>();
        } else if (tag.equalsIgnoreCase("HotelChoice")) {
            choice = new HotelChoice();
        } else if (tag.equalsIgnoreCase("CheapestRoom")) {
            inCheapestRoom = true;
            cheapestRoom = new HotelRoom();
        } else if (tag.equalsIgnoreCase("CheapestRoomWithViolation")) {
            inCheapestRoomWithViolation = true;
            cheapestRoomWithViolation = new HotelRoomWithViolation();
        } else if (inCheapestRoomWithViolation) {
            if (tag.equalsIgnoreCase("Violations")) {
                inViolations = true;
                cheapestRoomWithViolation.violations = new ArrayList<Violation>();
            } else if (inViolations && tag.equalsIgnoreCase("Violation")) {
                inViolation = true;
                violation = new Violation();
            }
        } else if (tag.equalsIgnoreCase("Recommendation")) {
            inRecommendation = true;
        } else if (tag.equalsIgnoreCase("PropertyImages")) {
            inPropertyImages = true;
            choice.propertyImages = new ArrayList<ImagePair>();
        } else if (inPropertyImages) {
            if (tag.equalsIgnoreCase("ImagePair")) {
                inImagePair = true;
                imagePair = new ImagePair();
            }
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equalsIgnoreCase("StartIndex")) {
            startIndex = Parse.safeParseInteger(text);
            if (startIndex == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: invalid start index value '" + text + "'.");
            }
        } else if (tag.equalsIgnoreCase("TotalCount")) {
            totalCount = Parse.safeParseInteger(text);
            if (totalCount == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: invalid total count value '" + text + "'.");
            }
        } else if (tag.equalsIgnoreCase("Length")) {
            length = Parse.safeParseInteger(text);
            if (length == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: invalid length value '" + text + "'.");
            }
        } else if (tag.equalsIgnoreCase("PollingId")) {
            pollingId = text;
        } else if (tag.equalsIgnoreCase("IsFinal")) {
            isFinal = Parse.safeParseBoolean(text);
        } else if (tag.equalsIgnoreCase("Addr1")) {
            choice.addr1 = text;
        } else if (tag.equalsIgnoreCase("Addr2")) {
            choice.addr2 = text;
        } else if (tag.equalsIgnoreCase("ChainCode")) {
            choice.chainCode = text;
        } else if (tag.equalsIgnoreCase("ChainName")) {
            choice.chainName = text;
        } else if (inCheapestRoom) {
            if (tag.equalsIgnoreCase("CrnCode")) {
                cheapestRoom.crnCode = text;
            } else if (tag.equalsIgnoreCase("Rate")) {
                cheapestRoom.rate = text;
                cheapestRoom.rateF = Parse.safeParseFloat(text);
            } else if (tag.equalsIgnoreCase("Summary")) {
                cheapestRoom.summary = text;
            } else if (tag.equalsIgnoreCase("BicCode")) {
                cheapestRoom.bicCode = text;
            } else if (tag.equalsIgnoreCase("SellSource")) {
                cheapestRoom.sellSource = text;
            } else if (tag.equalsIgnoreCase("ChoiceId")) {
                cheapestRoom.choiceId = text;
            } else if (tag.equalsIgnoreCase("MaxEnforcementLevel")) {
                cheapestRoom.maxEnforcementLevel = Parse.safeParseInteger(text);
            } else if (tag.equalsIgnoreCase("DepositRequired")) {
                cheapestRoom.depositRequired = Parse.safeParseBoolean(text);
            }
        } else if (inCheapestRoomWithViolation) {
            if (tag.equalsIgnoreCase("CrnCode")) {
                cheapestRoomWithViolation.crnCode = text;
            } else if (tag.equalsIgnoreCase("Rate")) {
                cheapestRoomWithViolation.rate = text;
                cheapestRoomWithViolation.rateF = Parse.safeParseFloat(text);
            } else if (tag.equalsIgnoreCase("Summary")) {
                cheapestRoomWithViolation.summary = text;
            } else if (tag.equalsIgnoreCase("DepositRequired")) {
                cheapestRoomWithViolation.depositRequired = Parse.safeParseBoolean(text);
            } else if (inViolation) {
                if (tag.equalsIgnoreCase("Code")) {
                    violation.code = text;
                } else if (tag.equalsIgnoreCase("Message")) {
                    violation.message = text;
                } else if (tag.equalsIgnoreCase("ViolationType")) {
                    violation.violationType = text;
                } else if (tag.equalsIgnoreCase("EnforcementLevel")) {
                    violation.enforcementLevel = Parse.safeParseInteger(text);
                }
            }
        } else if (tag.equalsIgnoreCase("City")) {
            choice.city = text;
        } else if (tag.equalsIgnoreCase("Country")) {
            choice.country = text;
        } else if (tag.equalsIgnoreCase("CountryCode")) {
            choice.countryCode = text;
        } else if (tag.equalsIgnoreCase("Distance")) {
            choice.distance = text;
            choice.distanceF = Parse.safeParseFloat(text);
        } else if (tag.equalsIgnoreCase("DistanceUnit")) {
            choice.distanceUnit = text;
        } else if (tag.equalsIgnoreCase("Lat")) {
            choice.lat = text;
        } else if (tag.equalsIgnoreCase("Lng")) {
            choice.lon = text;
        } else if (tag.equalsIgnoreCase("Hotel")) {
            choice.hotel = text;
        } else if (tag.equalsIgnoreCase("HotelPrefRank")) {
            choice.prefRank = text;
            choice.prefRankI = Parse.safeParseInteger(text);
        } else if (tag.equalsIgnoreCase("Phone")) {
            choice.phone = text;
        } else if (tag.equalsIgnoreCase("PropertyId")) {
            choice.propertyId = text;
        } else if (tag.equalsIgnoreCase("PropertyUri")) {
            choice.propertyURI = text;
        } else if (tag.equalsIgnoreCase("StarRating")) {
            choice.starRating = text;
            choice.starRatingI = Parse.safeParseInteger(text);
        } else if (tag.equalsIgnoreCase("State")) {
            choice.state = text;
        } else if (tag.equalsIgnoreCase("StateAbbrev")) {
            choice.stateAbbrev = text;
        } else if (tag.equalsIgnoreCase("TollFree")) {
            choice.tollFree = text;
        } else if (tag.equalsIgnoreCase("Zip")) {
            choice.zipCode = text;
        } else if (tag.equalsIgnoreCase("IsAdditional")) {
            choice.isAdditional = Parse.safeParseBoolean(text);
        } else if (tag.equalsIgnoreCase("GdsRateErrorCode")) {
            // tags to verify no rates and sold out hotels
            if (text.equalsIgnoreCase("PropertyNotAvailable")) {
                choice.isSoldOut = true;
            } else if (text != null) {
                choice.isNoRates = true;
            }
        } else if (tag.equalsIgnoreCase("IsCompanyPreferredChain")) {
            choice.isCompanyPreferredChain = text;
        } else if (tag.equalsIgnoreCase("IsContract")) {
            choice.isContract = text;
        } else if (tag.equalsIgnoreCase("PreferenceType")) {
            choice.preferenceType = text;
        } else if (tag.equalsIgnoreCase("CompanyPriority")) {
            choice.companyPriority = text;
        } else if (tag.equalsIgnoreCase("ContractRate")) {
            choice.contractRate = text;
            choice.contractRateF = Parse.safeParseFloat(text);
        } else if (inRecommendation) {
            thisChoiceHasRecommendation = true;
            if (tag.equalsIgnoreCase("DisplayValue")) {
                // temporary fix as the MWS end point is sending value like 5.53696521434058
                Long recomSrcNumber = Parse.safeParseLong(text);
                choice.recommendationSourceNumber = (recomSrcNumber == null ? 0 : recomSrcNumber);
            } else if (tag.equalsIgnoreCase("Source")) {
                try {
                    // temporary fix, need to be removed after the MWS end point changes
                    choice.recommendationSource = RecommendationSourceEnum.valueOf(text);
                } catch (IllegalArgumentException enumExc) {
                    choice.recommendationSource = RecommendationSourceEnum.Unknown;
                }
            } else if (tag.equalsIgnoreCase("TotalScore")) {
                // temporary fix, need to be removed after the MWS end point changes
                choice.recommendationScore = Parse.safeParseDouble(text);
            }
        } else if (inImagePair) {
            if (tag.equalsIgnoreCase("Image")) {
                imagePair.image = text;
            } else if (tag.equalsIgnoreCase("Thumbnail")) {
                imagePair.thumbnail = text;
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled XML node '" + tag + "' with value '" + text + "'.");
        }
    }

    @Override
    public void endTag(String tag) {
        if (inCheapestRoom && tag.equalsIgnoreCase("CheapestRoom")) {
            inCheapestRoom = false;
            choice.cheapestRoom = cheapestRoom;
        } else if (inViolations && tag.equalsIgnoreCase("Violation")) {
            inViolation = false;
            cheapestRoomWithViolation.violations.add(violation);
        } else if (inViolations && tag.equalsIgnoreCase("Violations")) {
            inViolations = false;
        } else if (tag.equalsIgnoreCase("CheapestRoomWithViolation")) {
            inCheapestRoomWithViolation = false;
            choice.cheapestRoomWithViolation = cheapestRoomWithViolation;
        } else if (inRecommendation && tag.equalsIgnoreCase("Recommendation")) {
            inRecommendation = false;
        } else if (inImagePair && tag.equalsIgnoreCase("ImagePair")) {
            inImagePair = false;
            choice.propertyImages.add(imagePair);
        } else if (inPropertyImages && tag.equalsIgnoreCase("PropertyImages")) {
            inPropertyImages = false;
        } else if (tag.equalsIgnoreCase("HotelChoice")) {
            choices.add(choice);
            // if at least one hotel had recommendation then we do not care about others.
            if (!hasRecommendation) {
                hasRecommendation = thisChoiceHasRecommendation;
            }
            thisChoiceHasRecommendation = false;
        }
    }

}
