package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;

/**
 * Representation of a provision used e.g. within a provision code list.
 *
 * @author Michael Becherer
 */
public class MealProvision implements Comparable<MealProvision>, Serializable {

    private static final long serialVersionUID = -3355549333702766315L;

    /**
     * The coded representation of a provision e.g. "PRO"
     */
    private String code;

    /**
     * The human readable value associated with the code e.g. "Provided"
     */
    private String description;

    /**
     * Creates a new MealProvision instance
     * @param code The coded representation of a provision e.g. "PRO"
     * @param description The human readable value associated with the code e.g. "Provided"
     */
    public MealProvision(String code, String description) {
        this.code = code;
        this.description = description;
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
    public String getDescription() {
        return description;
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
        if (description != null ? !description.equals(that.description) : that.description != null) {
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
        result = 31 * result + (description != null ? description.hashCode() : 0);
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
}
