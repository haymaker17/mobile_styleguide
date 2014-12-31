package com.concur.mobile.core.travel.rail.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

public class RailChoice {

    public String groupId;
    public String bucket;

    public Double baseFare;
    public Double cost;
    public String currency;
    public String description;
    public URI imageUri;
    public String vendorCode;
    public ArrayList<RailChoiceSegment> segments;
    public List<Violation> violations;
    public String choiceId;
    public Integer maxEnforcementLevel;
    public String gdsName;

    public RailChoice() {
        segments = new ArrayList<RailChoiceSegment>();
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("MaxEnforcementLevel")) {
            maxEnforcementLevel = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("BaseFare")) {
            baseFare = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("ChoiceId")) {
            choiceId = cleanChars;
        } else if (localName.equalsIgnoreCase("Bucket")) {
            bucket = cleanChars;
        } else if (localName.equalsIgnoreCase("Cost")) {
            cost = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("Currency")) {
            currency = cleanChars;
        } else if (localName.equalsIgnoreCase("Description")) {
            description = cleanChars;
        } else if (localName.equalsIgnoreCase("GroupId")) {
            groupId = cleanChars;
        } else if (localName.equalsIgnoreCase("ImageUri")) {
            imageUri = Format.formatServerURI(false, Preferences.getServerAddress(), cleanChars);
        } else if (localName.equalsIgnoreCase("VendorCode")) {
            vendorCode = cleanChars;
        } else if (localName.equalsIgnoreCase("gdsName")) {
            gdsName = cleanChars;
        }

    }

    /**
     * Set the seat class names based on the description field at the rail choice level. Also set the Acela flag.
     */
    public void populateLegClass() {

        String[] legClasses = description.split("/");

        // Do the outbound legs
        ArrayList<RailChoiceLeg> legs = segments.get(0).legs;
        int outboundLegs = legs.size();
        for (int i = 0; i < outboundLegs; i++) {
            final RailChoiceLeg leg = legs.get(i);
            leg.seatClassName = legClasses[i].trim();
            if (leg.seatClassName.contains("Acela")) {
                leg.isAcela = true;
            }
        }

        if (segments.size() == 2) {
            // And the return leg
            legs = segments.get(1).legs;
            for (int i = 0; i < legs.size(); i++) {
                final RailChoiceLeg leg = legs.get(i);
                leg.seatClassName = legClasses[i + outboundLegs].trim();
                if (leg.seatClassName.contains("Acela")) {
                    leg.isAcela = true;
                }
            }
        }
    }

    /**
     * Gets the list of violations for this rail choice.
     * 
     * @return
     */
    public List<Violation> getViolations() {
        return violations;
    }

    // TODO: The terms 'outbound' and 'return' do not work in a multi-segment situation. However,
    // mobile does not support those so we'll stick with them until we do support multi-segment.

    public RailChoiceSegment getOutboundSegment() {
        return segments.get(0);
    }

    public RailChoiceSegment getReturnSegment() {
        if (segments.size() == 2) {
            return segments.get(1);
        } else {
            return null;
        }
    }

}
