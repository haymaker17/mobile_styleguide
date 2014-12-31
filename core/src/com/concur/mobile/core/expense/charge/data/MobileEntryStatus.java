/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

/**
 * An enumeration that describes the different mobile entry status states.
 * 
 * @author AndrewK
 */
public enum MobileEntryStatus {
    NEW,                        // Mobile entry is new on the client and not yet saved to server.
    NORMAL,                     // Mobile entry has been saved on the server.
    PENDING_DELETION,           // Mobile entry is pending deletion.
    PENDING_ADD_TO_REPORT       // Mobile entry is pending "add to report".
};
