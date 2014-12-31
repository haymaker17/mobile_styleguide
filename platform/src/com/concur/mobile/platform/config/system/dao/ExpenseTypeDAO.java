package com.concur.mobile.platform.config.system.dao;

/**
 * Expense Type data access object which holds data for Expense Type.
 * 
 * @author sunill
 * 
 */
public class ExpenseTypeDAO {

    /**
     * Contains the expense code.
     */
    private String expCode;

    /**
     * Contains the expense key.
     */
    private String expKey;

    /**
     * Contains the expense name.
     */
    private String expName;

    /**
     * Contains the form key.
     */
    private Integer formKey;

    /**
     * Contains whether or not there is a post amount calculation.
     */
    private Boolean hasPostAmtCalc;

    /**
     * Contains whether or not there is a tax form.
     */
    private Boolean hasTaxForm;

    /**
     * Contains the list of expense keys that are not permitted itemizations of this expense type.
     */
    private String itemizationUnallowExpKeys;

    /**
     * Contains the itemize form key.
     */
    public Integer itemizeFormKey;

    /**
     * Contains the itemize style.
     */
    private String itemizeStyle;

    /**
     * Contains the itemize type.
     */
    private String itemizeType;

    /**
     * Contains the parent expense key.
     */
    private String parentExpKey;

    /**
     * Contains the parent expense name.
     */
    private String parentExpName;

    /**
     * Contains whether this expense type supports attendees.
     */
    private Boolean supportsAttendees;

    /**
     * Contains the vendor list key.
     */
    private Integer vendorListKey;

    /**
     * Contains the "allow edit attendee amount" value.
     */
    private Boolean allowEditAtnAmt;

    /**
     * Contains the "allow edit attendee count" value.
     */
    private Boolean allowEditAtnCount;

    /**
     * Contains the "allow no shows" value.
     */
    private Boolean allowNoShows;

    /**
     * Contains the "display add attendee on form" value.
     */
    private Boolean displayAddAtnOnForm;

    /**
     * Contains the "display attendee amounts" value.
     */
    private Boolean displayAtnAmounts;

    /**
     * Contains the "user as attendee default" value.
     */
    private Boolean userAsAtnDefault;

    /**
     * Contains the "unallowed attendee type keys" value.
     */
    private String unallowAtnTypeKeys;

    public ExpenseTypeDAO() {
    }

    /**
     * Construct the Expense Type reference using the data passed.
     * 
     * @param expCode
     *            expense type code.
     * @param expKey
     *            expense type key.
     * @param expName
     *            expense type name.
     * @param formKey
     *            expense type form key.
     * @param hasPostAmtCalc
     *            whether expense type has posted amount.
     * @param hasTaxForm
     *            whether expense type has tax form.
     * @param itemizationUnallowExpKeys
     *            whther expense type contains unallowed itemization expense type keys.
     * @param itemizeFormKey
     *            expense type's itemized form key.
     * @param itemizeStyle
     *            expense type's itemization style.
     * @param itemizeType
     *            expense type's itemization type.
     * @param parentExpKey
     *            expense type's parent expense key.
     * @param parentExpName
     *            expense type's parent expense name.
     * @param supportsAttendees
     *            whether or not expense type supports attendees.
     * @param vendorListKey
     *            expense type's vendor list key.
     * @param allowEditAtnAmt
     *            whether expense type allow to edit attendee amount.
     * @param allowEditAtnCount
     *            whether expense type allow to edit attendee count.
     * @param allowNoShows
     *            whether expense type allow to show number of noShow attendee.
     * @param displayAddAtnOnForm
     *            whether expense type allow to display add attendee on form.
     * @param displayAtnAmounts
     *            whether expense type allow to display attendee amount.
     * @param userAsAtnDefault
     *            whether expense type current user is default user.
     * @param unallowAtnTypeKeys
     *            expense type's unallow allow attendee type keys.
     */
    public ExpenseTypeDAO(String expCode, String expKey, String expName, Integer formKey, Boolean hasPostAmtCalc,
            Boolean hasTaxForm, String itemizationUnallowExpKeys, Integer itemizeFormKey, String itemizeStyle,
            String itemizeType, String parentExpKey, String parentExpName, Boolean supportsAttendees,
            Integer vendorListKey, Boolean allowEditAtnAmt, Boolean allowEditAtnCount, Boolean allowNoShows,
            Boolean displayAddAtnOnForm, Boolean displayAtnAmounts, Boolean userAsAtnDefault, String unallowAtnTypeKeys) {
        this.expCode = expCode;
        this.expKey = expKey;
        this.expName = expName;
        this.formKey = formKey;
        this.hasPostAmtCalc = hasPostAmtCalc;
        this.hasTaxForm = hasTaxForm;
        this.itemizationUnallowExpKeys = itemizationUnallowExpKeys;
        this.itemizeFormKey = itemizeFormKey;
        this.itemizeStyle = itemizeStyle;
        this.itemizeType = itemizeType;
        this.parentExpKey = parentExpKey;
        this.parentExpName = parentExpName;
        this.supportsAttendees = supportsAttendees;
        this.vendorListKey = vendorListKey;
        this.allowEditAtnAmt = allowEditAtnAmt;
        this.allowEditAtnCount = allowEditAtnCount;
        this.allowNoShows = allowNoShows;
        this.displayAddAtnOnForm = displayAddAtnOnForm;
        this.displayAtnAmounts = displayAtnAmounts;
        this.userAsAtnDefault = userAsAtnDefault;
        this.unallowAtnTypeKeys = unallowAtnTypeKeys;
    }

    /**
     * Gets expense code.
     * 
     * @return returns expense code.
     */
    public String getExpCode() {
        return expCode;
    }

    /**
     * Gets expense key.
     * 
     * @return returns expense key.
     */
    public String getExpKey() {
        return expKey;
    }

    /**
     * Gets expense name.
     * 
     * @return returns expense name.
     */
    public String getExpName() {
        return expName;
    }

    /**
     * Gets expense form key.
     * 
     * @return returns expense form key.
     */
    public Integer getFormKey() {
        return formKey;
    }

    /**
     * Gets whether or not there is a post amount calculation.
     * 
     * @return returns whether or not there is a post amount calculation.
     */
    public Boolean getHasPostAmtCalc() {
        return hasPostAmtCalc;
    }

    /**
     * Gets whether or not there is a tax form.
     * 
     * @return returns whether or not there is a tax form.
     */
    public Boolean getHasTaxForm() {
        return hasTaxForm;
    }

    /**
     * Gets the list of expense keys that are not permitted itemizations of this expense type.
     * 
     * @return returns the list of expense keys that are not permitted itemizations of this expense type.
     */
    public String getItemizationUnallowExpKeys() {
        return itemizationUnallowExpKeys;
    }

    /**
     * Gets the itemize form key.
     * 
     * @return returns the itemize form key.
     */
    public Integer getItemizeFormKey() {
        return itemizeFormKey;
    }

    /**
     * Gets the itemize style.
     * 
     * @return returns the itemize style.
     */
    public String getItemizeStyle() {
        return itemizeStyle;
    }

    /**
     * Gets the itemize type.
     * 
     * @return returns the itemize type.
     */
    public String getItemizeType() {
        return itemizeType;
    }

    /**
     * Gets the parent expense key.
     * 
     * @return returns the parent expense key.
     */
    public String getParentExpKey() {
        return parentExpKey;
    }

    /**
     * Gets the parent expense name.
     * 
     * @return returns the parent expense name.
     */
    public String getParentExpName() {
        return parentExpName;
    }

    /**
     * Gets whether this expense type supports attendees.
     * 
     * @return returns whether this expense type supports attendees.
     */
    public Boolean getSupportsAttendees() {
        return supportsAttendees;
    }

    /**
     * Gets the vendor list key.
     * 
     * @return returns the vendor list key.
     */
    public Integer getVendorListKey() {
        return vendorListKey;
    }

    /**
     * Gets the "allow edit attendee amount" value.
     * 
     * @return returns the "allow edit attendee amount" value.
     */
    public Boolean getAllowEditAtnAmt() {
        return allowEditAtnAmt;
    }

    /**
     * Gets the "allow edit attendee count" value.
     * 
     * @return returns the "allow edit attendee count" value.
     */
    public Boolean getAllowEditAtnCount() {
        return allowEditAtnCount;
    }

    /**
     * Gets the "allow no shows" value.
     * 
     * @return returns the "allow no shows" value.
     */
    public Boolean getAllowNoShows() {
        return allowNoShows;
    }

    /**
     * Gets the "display add attendee on form" value.
     * 
     * @return returns the "display add attendee on form" value.
     */
    public Boolean getDisplayAddAtnOnForm() {
        return displayAddAtnOnForm;
    }

    /**
     * Gets the "display attendee amounts" value.
     * 
     * @return returns the "display attendee amounts" value.
     */
    public Boolean getDisplayAtnAmounts() {
        return displayAtnAmounts;
    }

    /**
     * Gets the "user as attendee default" value.
     * 
     * @return returns the "user as attendee default" value.
     */
    public Boolean getUserAsAtnDefault() {
        return userAsAtnDefault;
    }

    /**
     * Gets the "unallowed attendee type keys" value.
     * 
     * @return returns the "unallowed attendee type keys" value.
     */
    public String getUnallowAtnTypeKeys() {
        return unallowAtnTypeKeys;
    }

}
