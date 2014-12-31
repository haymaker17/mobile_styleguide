package com.concur.mobile.platform.config.user.dao;

public class AttendeeColumnDefinitionDAO {

    /**
     * Contains the id.
     */
    private String id;

    /**
     * Contains the label.
     */
    private String label;

    /**
     * Contains the data type.
     */
    private String dataType;

    /**
     * Contains the control type.
     */
    private String controlType;

    /**
     * Contains the access type.
     */
    private String accessType;

    /**
     * Default Construction of the Attendee Column Definition DAO.
     * */
    public AttendeeColumnDefinitionDAO() {
    }

    /**
     * Construct the Attendee Column Definition reference using the data passed.
     * 
     * @param id
     *            contains field id.
     * @param desc
     *            contains field label.
     * @param dataType
     *            contains data type.
     * @param controlType
     *            contains control type.
     * @param accessType
     *            contains access type.
     */
    public AttendeeColumnDefinitionDAO(String id, String label, String dataType, String controlType, String accessType) {
        this.id = id;
        this.label = label;
        this.dataType = dataType;
        this.controlType = controlType;
        this.accessType = accessType;
    }

    /**
     * Gets field id.
     * 
     * @return returns field id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets field label.
     * 
     * @return returns field label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets data type.
     * 
     * @return returns data type.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Gets control type.
     * 
     * @return returns control type.
     */
    public String getControlType() {
        return controlType;
    }

    /**
     * Gets access type.
     * 
     * @return returns access type.
     */
    public String getAccessType() {
        return accessType;
    }
}
