package com.concur.mobile.platform.config.user.dao;

/**
 * Attendee Type data access object.
 * 
 * @author yiwenw
 * 
 */
public class AttendeeTypeDAO {

    /**
     * Contains whether the attendee count is editable.
     */
    private Boolean allowEditAtnCount;

    /**
     * Contains the attendee type code.
     */
    private String atnTypeCode;

    /**
     * Contains the attendee type key.
     */
    private String atnTypeKey;

    /**
     * Contains the attendee type name.
     */
    private String atnTypeName;

    /**
     * Contains the attendee type form key.
     */
    private String formKey;

    /**
     * Contains whether the attendee type is external.
     */
    private Boolean isExternal;

    /**
     * Default Construction of the Attendee Type.
     * */
    public AttendeeTypeDAO() {
    }

    /**
     * Construct the Attendee Type reference using the data passed.
     * 
     * @param atnTypeKey
     *            contains attendee type key.
     * @param atnTypeCode
     *            contains attendee type code.
     * @param atnTypeName
     *            contains attendee type name.
     * @param formKey
     *            contains form key.
     * @param allowEditAtnCount
     *            contains flag on whether to allow editing on attendee count
     * @param isExternal
     *            contains flag on whether this is an external attendee.
     */
    public AttendeeTypeDAO(String atnTypeKey, String atnTypeCode, String atnTypeName, String formKey,
            Boolean allowEditAtnCount, Boolean isExternal) {
        this.atnTypeKey = atnTypeKey;
        this.atnTypeCode = atnTypeCode;
        this.atnTypeName = atnTypeName;
        this.formKey = formKey;
        this.allowEditAtnCount = allowEditAtnCount;
        this.isExternal = isExternal;
    }

    /**
     * Gets attendee type key.
     * 
     * @return returns attendee type key.
     */
    public String getAtnTypeKey() {
        return atnTypeKey;
    }

    /**
     * Gets attendee type code.
     * 
     * @return returns attendee type code.
     */
    public String getAtnTypeCode() {
        return atnTypeCode;
    }

    /**
     * Gets attendee type name.
     * 
     * @return returns attendee type name.
     */
    public String getAtnTypeName() {
        return atnTypeName;
    }

    /**
     * Gets form key.
     * 
     * @return returns form key.
     */
    public String getFormKey() {
        return formKey;
    }

    /**
     * Gets whether the attendee count is editable.
     * 
     * @return returns whether the attendee count is editable.
     */
    public Boolean getAllowEditAtnCount() {
        return allowEditAtnCount;
    }

    /**
     * Gets whether the attendee is external.
     * 
     * @return returns whether the attendee is external.
     */
    public Boolean getIsExternal() {
        return isExternal;
    }

}
