package com.concur.mobile.core.expense.travelallowance.datamodel;

import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.io.Serializable;

/**
 * Representation of a provision used e.g. within a provision code list.
 *
 * @author Michael Becherer
 */
public class MealProvision implements Comparable<MealProvision>, Serializable {

    private static final long serialVersionUID = -3355549333702766315L;

    public static final String NOT_PROVIDED_CODE = "NPR";
    /**
     * The coded representation of a provision e.g. "PRO"
     */
    private String code;

    /**
     * The human readable value associated with the code e.g. "Provided"
     */
    private String codeDescription;

    /**
     * Creates a new MealProvision instance
     * @param code The coded representation of a provision e.g. "PRO"
     * @param codeDescription The human readable value associated with the code e.g. "Provided"
     */
    public MealProvision(String code, String codeDescription) {
        this.code = code;
        this.codeDescription = codeDescription;
    }

    /**
     * Getter method
     *
     * @return The coded representation of a provision e.g. "PRO"
     */
    public String getCode() {
        return code;
    }

    /**
     * Getter method
     *
     * @return The human readable value associated with the code e.g. "Provided"
     */
    public String getCodeDescription() {
        return codeDescription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MealProvision)) {
            return false;
        }

        MealProvision that = (MealProvision) o;

        if (code != null ? !code.equals(that.code) : that.code != null) {
            return false;
        }
        if (codeDescription != null ? !codeDescription.equals(that.codeDescription) : that.codeDescription != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (codeDescription != null ? codeDescription.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final MealProvision another) {

        if (another == null) {
            return 1;
        }
        if (code != null && another.code != null) {
            return code.compareTo(another.code);
        }

        if (code != null && another.code == null) {
            return 1;
        }

        if (code == null && another.code != null) {
            return -1;
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (StringUtilities.isNullOrEmpty(codeDescription)) {
            return code;
        } else {
            return codeDescription;
        }
    }
}
