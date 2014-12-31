package com.concur.mobile.platform.config.user.test;

import org.simpleframework.xml.Element;

public class ExpensePolicy {

    /**
     * Contains the expense report policy key.
     */
    @Element(name = "PolKey")
    public String polKey;

    /**
     * Contains the approval confirmation key.
     */
    @Element(name = "ApprovalConfirmationKey", required = false)
    public String approvalConfirmationKey;

    /**
     * Contains the submit confirmation key.
     */
    @Element(name = "SubmitConfirmationKey", required = false)
    public String submitConfirmationKey;

    /**
     * Contains whether this expense policy configuration supports imaging.
     */
    @Element(name = "SupportsImaging")
    public Boolean supportsImaging;

}
