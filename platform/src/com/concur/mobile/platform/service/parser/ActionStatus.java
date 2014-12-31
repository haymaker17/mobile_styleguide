/**
 * 
 */
package com.concur.mobile.platform.service.parser;

import java.io.Serializable;

/**
 * This class holds action status information.
 * 
 * @author andrewk
 */
public class ActionStatus implements Serializable {

    // Contains the serialization ID.
    private static final long serialVersionUID = -687419450316652429L;

    /**
     * Contains the constant identifying a success action status.
     */
    public static final String SUCCESS = "SUCCESS";

    /**
     * Contains the constant identifying a failure action status.
     */
    public static final String FAILURE = "FAILURE";

    /**
     * Contains the action status result. Should be equal to one of <code>ActionStatus.SUCCESS</code> or
     * <code>ActionStatus.FAILURE</code>.
     */
    public String status;

    /**
     * Contains an error message.
     */
    public String errorMessage;

}
