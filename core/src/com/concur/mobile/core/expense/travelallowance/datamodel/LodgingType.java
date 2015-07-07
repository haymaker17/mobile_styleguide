package com.concur.mobile.core.expense.travelallowance.datamodel;

import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.io.Serializable;

/**
 * Representation of a lodging type used e.g. within a code list.
 *
 * @author Michael Becherer
 */
public class LodgingType implements Comparable<LodgingType>, Serializable, ICode {

    private static final long serialVersionUID = 4213447001177852573L;

    /**
     * The coded representation of a ldoging type e.g. "HOTEL"
     */
    private String code;

    /**
     * The human readable value associated with the code e.g. "Hotel"
     */
    private String codeDescription;

    /**
     * Creates a new LodgingType instance
     */
    public LodgingType() {
    }

    /**
     * Creates a new LodgingType instance
     * @param code The coded representation of a lodging type e.g. "HOTEL"
     * @param codeDescription The human readable value associated with the code e.g. "Hotel"
     */
    public LodgingType(String code, String codeDescription) {
        this.code = code;
        this.codeDescription = codeDescription;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public String getDescription() {
        return codeDescription;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void setDescription(String description) {
        this.codeDescription = description;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LodgingType)) return false;

        LodgingType that = (LodgingType) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return !(codeDescription != null ? !codeDescription.equals(that.codeDescription) : that.codeDescription != null);

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
    public int compareTo(LodgingType another) {
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
