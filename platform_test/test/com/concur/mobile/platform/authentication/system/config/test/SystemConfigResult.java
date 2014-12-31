package com.concur.mobile.platform.authentication.system.config.test;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Provides a model of System config response information. <br>
 * <br>
 * <b>NOTE:</b>&nbsp;&nbsp; Some of the fields below have the "required = false" flag set as this same class is used by SimpleXML
 * 
 * @author sunill
 */
@Root(name = "SystemConfig")
public class SystemConfigResult {

    /**
     * Contains the access token.
     */
    @Element(name = "Hash", required = false)
    public String hash;

    /**
     * Contains the response Id.
     */
    @Element(name = "ResponseId", required = false)
    public String responseId;

    /**
     * Contains the refundableCheckboxDefault.
     */
    @Path("RefundableInfo")
    @Element(name = "CheckboxDefault")
    public Boolean refundableCheckboxDefault;

    /**
     * Contains the refundableMessage.
     */
    @Path("RefundableInfo")
    @Element(name = "Message")
    public String refundableMessage;

    /**
     * Contains refundableShowCheckbox.
     */
    @Path("RefundableInfo")
    @Element(name = "ShowCheckbox")
    public Boolean refundableShowCheckbox;
    /**
     * Contains the ruleViolationExplanationRequired.
     */
    @Element(name = "RuleViolationExplanationRequired")
    public Boolean ruleViolationExplanationRequired;
    /**
     * Contains air Reasons.
     */
    @ElementList(name = "AirReasons")
    public List<ReasonCode> airReasonCode;

    /**
     * Contains car reasons..
     */
    @ElementList(name = "CarReasons")
    public List<ReasonCode> carReasonCode;

    /**
     * Contains hotel reasons.
     */
    @ElementList(name = "HotelReasons")
    public List<ReasonCode> hotelReasonCode;

    /**
     * Contains expense types.
     */
    @ElementList(name = "ExpenseTypes")
    public List<ExpenseType> expenseType;

    /**
     * Contains offices.
     */
    @ElementList(name = "Offices")
    public List<Office> offices;
    /**
     * Contains the user id.
     */
    @Element(name = "UserId", required = false)
    public String userId;

}
