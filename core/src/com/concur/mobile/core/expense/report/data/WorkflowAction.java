/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

/**
 * Provides a model of a workflow action.
 */
public class WorkflowAction {

    /**
     * Contains the localized action text.
     */
    public String actionText;

    /**
     * Contains the action status key.
     */
    public String statKey;

    public String toString() {
        return actionText;
    }

}
