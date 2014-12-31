package com.concur.mobile.platform.config.system.dao;

/**
 * Reason code data access object which holds data for Reason Code.
 * 
 * @author sunill
 * 
 */
public class ReasonCodeDAO {

    /**
     * Contains the reason code type, see <code>Config.ReasonCodeColumns.TYPE_AIR, Config.ReasonCodeColumns.TYPE_HOTEL and
     * Config.ReasonCodeColumns.TYPE_AIR</code>.
     */
    private String type;

    /**
     * Contains the description.
     */
    private String description;

    /**
     * Contains the id.
     */
    private Integer id;

    /**
     * Contains the violation type.
     */
    private String violationType;

    /**
     * Default Construction of the Office location.
     * */
    public ReasonCodeDAO() {
    }

    /**
     * Construct the Reason Code reference using the data passed.
     * 
     * @param type
     *            contains reason code type.
     * @param desc
     *            contains description of reason code.
     * @param id
     *            contains reason code id.
     * @param violationType
     *            contains reason code violation type.
     */
    public ReasonCodeDAO(String type, String desc, String id, String violationType) {
        this.type = type;
        this.description = desc;
        this.id = Integer.parseInt(id);
        this.violationType = violationType;
    }

    /**
     * Gets reason code type.
     * 
     * @return returns reason code type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets reason code description.
     * 
     * @return returns reason code description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets reason code id.
     * 
     * @return returns reason code id.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets reason code violation type.
     * 
     * @return reason code violation type.
     */
    public String getViolationType() {
        return violationType;
    }

}
