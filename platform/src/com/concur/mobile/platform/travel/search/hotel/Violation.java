/**
 * 
 */
package com.concur.mobile.platform.travel.search.hotel;

import android.util.Log;

import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models a violation.
 */
public class Violation {

    private static final String CLS_TAG = Violation.class.getSimpleName();

    public static enum StandardViolationType {
        AIR("A"), HOTEL("H"), CAR("C"), ITINERARY("I"), AIR_EXCHANGE("X"), GENERAL("G");

        private String name;

        /**
         * Constructor an instance of <code>StandardViolation</code>.
         * 
         * @param name
         *            the violation name.
         */
        StandardViolationType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        String getName() {
            return name;
        }

        /**
         * Gets an enum value of <code>StandardViolationType</code> for <code>name</code>.
         * 
         * @param curValue
         *            the enumeration value name.
         * @return an instance of <code>Violation.StandardViolationType</code>.
         * @throws IllegalArgumentException
         *             if <code>name</code> does not match an enumeration name.
         * @throws NullPointerException
         *             if <code>name</code> is <code>null</code>.
         */
        public static StandardViolationType fromString(String name) throws IllegalArgumentException {
            if (name != null) {
                for (StandardViolationType sv : StandardViolationType.values()) {
                    if (sv.name.equalsIgnoreCase(name)) {
                        return sv;
                    }
                }
                throw new IllegalArgumentException("can't locate enum value for name '" + name + "'.");
            } else {
                throw new NullPointerException("name is null!");
            }
        }
    };

    public String message;
    public String code;
    public Integer enforcementLevel;
    public String violationType;

    private static final String MESSAGE = "Message";
    private static final String CODE = "Code";
    private static final String ENFORCEMENT_LEVEL = "EnforcementLevel";
    private static final String VIOLATION_TYPE = "ViolationType";

    public boolean handleElement(String localName, String cleanChars) {
        boolean elementHandled = false;
        if (localName.equalsIgnoreCase(MESSAGE)) {
            message = cleanChars;
            elementHandled = true;
        } else if (localName.equalsIgnoreCase(CODE)) {
            code = cleanChars;
            elementHandled = true;
        } else if (localName.equalsIgnoreCase(VIOLATION_TYPE)) {
            violationType = cleanChars;
            elementHandled = true;
        } else if (localName.equalsIgnoreCase(ENFORCEMENT_LEVEL)) {
            enforcementLevel = Parse.safeParseInteger(cleanChars);
            elementHandled = true;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled XML tag '" + localName + "' with value '"
                    + cleanChars + "'.");
        }
        return elementHandled;
    }

}
