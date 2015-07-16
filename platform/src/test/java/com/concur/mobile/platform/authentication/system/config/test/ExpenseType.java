package com.concur.mobile.platform.authentication.system.config.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ExpenseType")
public class ExpenseType {

    /**
     * Contains the exp code.
     */
    @Element(name = "ExpCode")
    public String expCode;

    /**
     * Contains the expense key.
     */
    @Element(name = "ExpKey")
    public String expKey;

    /**
     * Contains the expense name.
     */
    @Element(name = "ExpName")
    public String expName;

    /**
     * Contains the expense form key.
     */
    @Element(name = "FormKey")
    public Integer formKey;

    /**
     * Contains whether or not the posted amount has been calculated.
     */
    @Element(name = "HasPostAmtCalc")
    public String hasPostAmtCalc;

    /**
     * Contains whether the VAT form available or not.
     */
    @Element(name = "HasTaxForm")
    public String hasTaxForm;

    /**
     * Contains the list of expense types that are not allowed for itemizations of this expense type.
     */
    @Element(name = "ItemizationUnallowExpKeys", required = false)
    public String itemizationUnallowExpKeys;

    /**
     * Contains the itemization form key.
     */
    @Element(name = "ItemizeFormKey")
    public Integer itemizeFormKey;

    /**
     * Contains the itemization style.
     */
    @Element(name = "ItemizeStyle")
    public String itemizeStyle;

    /**
     * Contains the itemization type.
     */
    @Element(name = "ItemizeType")
    public String itemizeType;

    /**
     * Contains the parent expense key.
     */
    @Element(name = "ParentExpKey", required = false)
    public String parentExpKey;

    /**
     * Contains the parent expense type.
     */
    @Element(name = "ParentExpName", required = false)
    public String parentExpName;

    /**
     * Contains whether the expense type supports attendees.
     */
    @Element(name = "SupportsAttendees")
    public String supportsAttendees;

    /**
     * Contains the vendor list key.
     */
    @Element(name = "VendorListKey", required = false)
    public Integer vendorListKey;

    /**
     * Contains whether the expense type permits editing of attendee amounts not filled in by the server.
     */
    @Element(name = "AllowEditAtnAmt", required = false)
    public String allowEditAtnAmt;

    /**
     * Contains whether the expense type permits editing of attendee counts not filled in by the server.
     */
    @Element(name = "AllowEditAtnCount", required = false)
    public String allowEditAtnCount;

    /**
     * Contains whether no shows are permitted.
     */
    @Element(name = "AllowNoShows", required = false)
    public String allowNoShows;

    /**
     * Contains whether attendee amounts should be displayed.
     */
    @Element(name = "DisplayAddAtnOnForm", required = false)
    public String displayAddAtnOnForm;

    /**
     * Contains whether or not the posted amount has been calculated.
     */
    @Element(name = "DisplayAtnAmounts", required = false)
    public String displayAtnAmounts;

    /**
     * Contains whether the user can be the default attendee.
     */
    @Element(name = "UserAsAtnDefault", required = false)
    public String userAsAtnDefault;

    /**
     * Contains the unallow attendee type key.
     */
    @Element(name = "UnallowAtnTypeKeys", required = false)
    public String unallowAtnTypeKeys;

}
