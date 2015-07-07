package com.concur.mobile.core.expense.travelallowance.datamodel;

/**
 * Interface describing a code value pair entity.
 * 
 * @author Patricius Komarnicki Created on 07.07.2015.
 */
public interface ICode {

    /**
     * 
     * @param code
     *            A code value representing a specific code - value entity like meal provision, lodging type.
     */
    void setCode(String code);

    /**
     *
     * @param description
     *            A human read and localized description of the specific code.
     */
    void setDescription(String description);

    /**
     * 
     * @return A code value representing a specific code - value entity like meal provision, lodging type.
     */
    String getCode();

    /**
     *
     * @return The human readable value associated with the code e.g. "Hotel"
     */
    String getDescription();

}
