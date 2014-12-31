/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models affinity program (frequent flyer, frequent guest, etc.) programs.
 */
public class AffinityProgram {

    private static final String CLS_TAG = AffinityProgram.class.getSimpleName();

    public static enum ProgramType {
        AIR("A"), CAR("C"), HOTEL("H");

        String value;

        ProgramType(String value) {
            this.value = value;
        }

        public static ProgramType fromString(String text) {
            ProgramType retVal = null;
            if (text != null) {
                for (ProgramType progType : ProgramType.values()) {
                    if (text.equalsIgnoreCase(progType.value)) {
                        retVal = progType;
                        break;
                    }
                }
            }
            return retVal;
        }
    }

    private static final String ACCOUNT_NUMBER = "AccountNumber";
    private static final String DESCRIPTION = "Description";
    private static final String PROGRAM_ID = "ProgramId";
    private static final String PROGRAM_NAME = "ProgramName";
    private static final String PROGRAM_TYPE = "ProgramType";
    private static final String VENDOR = "Vendor";
    private static final String VENDOR_ABBREV = "VendorAbbrev";
    private static final String EXPECTED_SELECTION = "ExpectedSelection";

    public String accountNumber;

    public String description;

    public String programId;

    public String programName;

    public ProgramType programType;

    public String vendor;

    public String vendorAbbrev;

    public boolean defaultProgram;

    /**
     * Will examine a list of passed in affinity programs and return a sub-list based on program type.
     * 
     * @param programs
     *            the list of affinity programs.
     * @param progType
     *            the type of affinity program.
     * @return a list of affinity programs contained in <code>programs</code> based on <code>progType</code>.
     */
    public static List<AffinityProgram> getProgramsByType(List<AffinityProgram> programs, ProgramType progType) {
        List<AffinityProgram> progs = null;
        if (programs != null) {
            for (AffinityProgram affProg : programs) {
                if (affProg.programType != null && affProg.programType == progType) {
                    if (progs == null) {
                        progs = new ArrayList<AffinityProgram>();
                    }
                    progs.add(affProg);
                }
            }
        }
        return progs;
    }

    /**
     * Will assign a value based on the attribute name.
     * 
     * @param localName
     *            the attribute name.
     * @param value
     *            the attribute value.
     */
    public void handleElement(String localName, String value) {
        if (localName.equalsIgnoreCase(ACCOUNT_NUMBER)) {
            accountNumber = value;
        } else if (localName.equalsIgnoreCase(DESCRIPTION)) {
            description = value;
        } else if (localName.equalsIgnoreCase(PROGRAM_ID)) {
            programId = value;
        } else if (localName.equalsIgnoreCase(PROGRAM_NAME)) {
            programName = value;
        } else if (localName.equalsIgnoreCase(PROGRAM_TYPE)) {
            programType = ProgramType.fromString(value);
            if (programType == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled affinity program type '" + value + "'.");
            }
        } else if (localName.equalsIgnoreCase(VENDOR)) {
            vendor = value;
        } else if (localName.equalsIgnoreCase(VENDOR_ABBREV)) {
            vendorAbbrev = value;
        } else if (localName.equalsIgnoreCase(EXPECTED_SELECTION)) {
            defaultProgram = Parse.safeParseBoolean(value);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled attribute '" + localName + "'.");
        }
    }
}
