package com.concur.mobile.core.travel.rail.data;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class RailChoiceSegment {

    public int totalTime;
    public ArrayList<RailChoiceLeg> legs;

    public RailChoiceSegment() {
        legs = new ArrayList<RailChoiceLeg>();
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("TotalElapsedTime")) {
            totalTime = Parse.safeParseInteger(cleanChars);
        }

    }

    public String getDepStation() {
        return legs.get(0).depStationCode;
    }

    public Calendar getDepDateTime() {
        return legs.get(0).depDateTime;
    }

    public String getDepTrainNumber() {
        return legs.get(0).trainNum;
    }

    public String getArrStation() {
        return legs.get(legs.size() - 1).arrStationCode;
    }

    public Calendar getArrDateTime() {
        return legs.get(legs.size() - 1).arrDateTime;
    }

    public String getElapsedTime(Context context) {
        return FormatUtil.formatElapsedTime(context, totalTime);
    }

    public boolean hasAcela() {
        for (RailChoiceLeg leg : legs) {
            if (leg.isAcela) {
                return true;
            }
        }
        return false;
    }
}
