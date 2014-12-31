package com.concur.mobile.platform.config.user.dao;

public class YodleePaymentTypeDAO {

    /**
     * Contains the payment type key.
     */
    private String key;

    /**
     * Contains the payment type text.
     */
    private String text;

    /**
     * Default Construction of the Yodlee Payment Type DAO.
     * */
    public YodleePaymentTypeDAO() {
    }

    /**
     * Construct the Yodlee Payment Type reference using the data passed.
     * 
     * @param key
     *            contains Yodlee payment type key.
     * @param text
     *            contains Yodlee payment type text.
     */
    public YodleePaymentTypeDAO(String key, String text) {
        this.key = key;
        this.text = text;
    }

    /**
     * Gets Yodlee Payment type key.
     * 
     * @return returns Yodlee Payment type key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets Yodlee Payment type text.
     * 
     * @return returns Yodlee Payment type text.
     */
    public String getText() {
        return text;
    }
}
