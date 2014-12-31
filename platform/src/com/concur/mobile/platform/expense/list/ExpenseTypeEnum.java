package com.concur.mobile.platform.expense.list;


/**
 * An enumeration representing an expense entry type.
 * 
 * @author sunill
 */
public enum ExpenseTypeEnum {
    PERSONAL_CARD, // Personal card transaction.
    CORPORATE_CARD, // Corporate card transaction.
    CASH, // Cash transaction.
    SMART_CORPORATE, // A smart expense type (corporate card transaction + cash).
    SMART_PERSONAL, // A smart expense type (personal card transaction + cash).
    RECEIPT_CAPTURE, // A receipt capture/expense it expenses.
    E_RECEIPT // An e-receipt expenses.
}
