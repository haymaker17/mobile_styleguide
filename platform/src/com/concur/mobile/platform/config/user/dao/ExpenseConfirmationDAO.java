package com.concur.mobile.platform.config.user.dao;

public class ExpenseConfirmationDAO {

    /**
     * Contains the confirmation key.
     */
    private String key;

    /**
     * Contains the confirmation text.
     */
    private String text;

    /**
     * Contains the confirmation title.
     */
    private String title;

    /**
     * Default Construction of the Expense Confirmation DAO.
     * */
    public ExpenseConfirmationDAO() {
    }

    /**
     * Construct the Expense Confirmation reference using the data passed.
     * 
     * @param key
     *            contains Expense Confirmation key.
     * @param text
     *            contains Expense Confirmation text.
     * @param title
     *            contains Expense Confirmation dialog title or subject.
     */
    public ExpenseConfirmationDAO(String key, String text, String title) {
        this.key = key;
        this.text = text;
        this.title = title;
    }

    /**
     * Gets Expense Confirmation key.
     * 
     * @return returns Expense Confirmation key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets Expense Confirmation text.
     * 
     * @return returns Expense Confirmation text.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets Expense Confirmation title.
     * 
     * @return returns Expense Confirmation title.
     */
    public String getTitle() {
        return title;
    }
}
