package com.concur.mobile.core.travel.car.data;

import java.net.URI;
import java.util.List;

import android.util.Log;

import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class CarChoice {

    private static final String CLS_TAG = CarChoice.class.getSimpleName();

    public String carId;
    public String chainCode;
    public String carType;
    public String isCompanyPreferred;
    public String discountCode;
    public String currency;
    public double rate;
    public double dailyRate;
    public double baseRate;
    public double totalRate;
    public String ratePeriod;
    public double mileRate;
    public String freeMiles; // It may be odd to have a string here but the value 'UNL' is returned for unlimited.
    public String dropoffRestrictions;
    public String rateKey;
    public String rateCode;
    public String rateCategory;
    public String rateDescription;
    public double dropChargeAmount;
    public Boolean sendCreditCard; // Boolean will help me to handle if sendCreditCard is empty. default value is NULL.
    public URI imageUri;
    public List<Violation> violations;

    public String dropOffCategory;
    public String dropOffNumber;
    public String pickUpCategory;
    public String pickUpNumber;
    public String choiceId;
    public Integer maxEnforcementLevel;
    public String gdsName;

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("MaxEnforcementLevel")) {
            maxEnforcementLevel = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("BaseRate")) {
            baseRate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("CarId")) {
            carId = cleanChars;
        } else if (localName.equalsIgnoreCase("CarType")) {
            carType = cleanChars;
        } else if (localName.equalsIgnoreCase("ChainCode")) {
            chainCode = cleanChars;
        } else if (localName.equalsIgnoreCase("ChoiceId")) {
            choiceId = cleanChars;
        } else if (localName.equalsIgnoreCase("Currency")) {
            currency = cleanChars;
        } else if (localName.equalsIgnoreCase("DailyRate")) {
            dailyRate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("DiscountCode")) {
            discountCode = cleanChars;
        } else if (localName.equalsIgnoreCase("DropoffRestrictions")) {
            dropoffRestrictions = cleanChars;
        } else if (localName.equalsIgnoreCase("FreeMiles")) {
            freeMiles = cleanChars;
        } else if (localName.equalsIgnoreCase("ImageUri")) {
            imageUri = FormatUtil.buildURI(cleanChars, false);
        } else if (localName.equalsIgnoreCase("IsCompanyPreferred")) {
            isCompanyPreferred = cleanChars;
        } else if (localName.equalsIgnoreCase("Rate")) {
            rate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("MileRate")) {
            mileRate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("RateCategory")) {
            rateCategory = cleanChars;
        } else if (localName.equalsIgnoreCase("RateCode")) {
            rateCode = cleanChars;
        } else if (localName.equalsIgnoreCase("RateDescription")) {
            rateDescription = cleanChars;
        } else if (localName.equalsIgnoreCase("RateKey")) {
            rateKey = cleanChars;
        } else if (localName.equalsIgnoreCase("RatePeriod")) {
            ratePeriod = cleanChars;
        } else if (localName.equalsIgnoreCase("TotalRate")) {
            totalRate = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("DropoffCategory")) {
            dropOffCategory = cleanChars;
        } else if (localName.equalsIgnoreCase("DropoffNumber")) {
            dropOffNumber = cleanChars;
        } else if (localName.equalsIgnoreCase("PickupCategory")) {
            pickUpCategory = cleanChars;
        } else if (localName.equalsIgnoreCase("PickupNumber")) {
            pickUpNumber = cleanChars;
        } else if (localName.equalsIgnoreCase("SendCreditCard")) {
            sendCreditCard = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("GdsName")) {
            gdsName = cleanChars;
        } else if (localName.equalsIgnoreCase("Violations")) {
            // No-op.
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled XML node '" + localName + "' with value '"
                    + cleanChars + "'.");
        }

    }
}
