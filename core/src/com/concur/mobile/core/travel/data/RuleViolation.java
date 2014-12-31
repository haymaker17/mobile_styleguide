package com.concur.mobile.core.travel.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule violation value object
 * 
 * @author RatanK
 * 
 */
public class RuleViolation {

    /**
     * rule configured i.e. Hotel rate is greater than $250
     */
    private List<String> rules;

    private List<RuleViolationReason> violationReasons;

    public static enum SegmentType {
        UNKNOWN, Itinerary, Car, Hotel, Rail, Air;
    }

    private SegmentType segmentType;

    public List<String> getRules() {
        return (rules == null ? new ArrayList<String>() : rules);
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public List<RuleViolationReason> getViolationReasons() {
        return (violationReasons == null ? new ArrayList<RuleViolationReason>() : violationReasons);
    }

    public void setViolationReasons(List<RuleViolationReason> violationReasons) {
        this.violationReasons = violationReasons;
    }

    public SegmentType getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(SegmentType segmentType) {
        this.segmentType = segmentType;
    }

    public void setSegmentType(String segmentTypeStr) {
        boolean found = false;
        for (SegmentType segType : SegmentType.values()) {
            if (segType.toString().equals(segmentTypeStr)) {
                found = true;
                setSegmentType(segType);
                break;
            }
        }
        if (!found) {
            setSegmentType(SegmentType.UNKNOWN);
        }
    }
}
