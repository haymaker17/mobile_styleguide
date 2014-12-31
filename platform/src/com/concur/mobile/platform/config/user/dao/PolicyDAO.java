package com.concur.mobile.platform.config.user.dao;

public class PolicyDAO {

    /**
     * Contains the policy key.
     */
    private String key;

    /**
     * Contains whether or not the policy supports imaging.
     */
    private Boolean supportsImaging;

    /**
     * Contains the Expense confirmation key for approval.
     */
    private String approvalConfirmationKey;

    /**
     * Contains the Expense confirmation key for submit.
     */
    private String submitConfirmationKey;

    /**
     * Default Construction of the Policy DAO.
     * */
    public PolicyDAO() {
    }

    /**
     * Construct the Policy reference using the data passed.
     * 
     * @param key
     *            contains Policy key.
     * @param supportsImaging
     *            contains whether imaging is supported under the policy.
     * @param approvalKey
     *            contains Expense confirmation key for approval.
     * @param submitKey
     *            contains Expense confirmation key for submit.
     */
    public PolicyDAO(String key, Boolean supportsImaging, String approvalKey, String submitKey) {
        this.key = key;
        this.supportsImaging = supportsImaging;
        this.approvalConfirmationKey = approvalKey;
        this.submitConfirmationKey = submitKey;
    }

    /**
     * Gets Policy key.
     * 
     * @return returns Policy key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets whether imaging is supported under the policy.
     * 
     * @return returns whether imaging is supported under the policy.
     */
    public Boolean getSupportsImaging() {
        return supportsImaging;
    }

    /**
     * Gets Expense confirmation key for approval.
     * 
     * @return returns Expense confirmation key for approval.
     */
    public String getApprovalConfirmationKey() {
        return approvalConfirmationKey;
    }

    /**
     * Gets Expense confirmation key for submit.
     * 
     * @return returns Expense confirmation key for submit.
     */
    public String getSubmitConfirmationKey() {
        return submitConfirmationKey;
    }

}
