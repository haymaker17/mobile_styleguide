package com.concur.mobile.platform.config.user.dao;

/**
 * Car Type data access object.
 * 
 * @author yiwenw
 * 
 */
public class CarTypeDAO {

    /**
     * Contains the car-type description.
     */
    private String description;

    /**
     * Contains the car-type code.
     */
    private String code;

    /**
     * Contains whether this car-type is the default selection.
     */
    private Boolean isDefault;

    /**
     * Default Construction of the Car Type DAO.
     * */
    public CarTypeDAO() {
    }

    /**
     * Construct the Car Type reference using the data passed.
     * 
     * @param desc
     *            contains description of reason code.
     * @param id
     *            contains reason code id.
     * @param violationType
     *            contains reason code violation type.
     */
    public CarTypeDAO(String desc, String code, Boolean isDefault) {
        this.description = desc;
        this.code = code;
        this.isDefault = isDefault;
    }

    /**
     * Gets car type code.
     * 
     * @return returns car type code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets car type description.
     * 
     * @return returns car type description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets car type is default flag.
     * 
     * @return returns car type is default flag.
     */
    public Boolean getIsDefault() {
        return isDefault;
    }

}
