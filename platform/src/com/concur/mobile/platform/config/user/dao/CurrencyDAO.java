package com.concur.mobile.platform.config.user.dao;

public class CurrencyDAO {

    /**
     * Contains the currency code.
     */
    private String crnCode;

    /**
     * Contains the currency name.
     */
    private String crnName;

    /**
     * Contains the number of decimal digits.
     */
    private Integer decimalDigits;

    /**
     * Default Construction of the Currency DAO.
     * */
    public CurrencyDAO() {
    }

    /**
     * Construct the Currency reference using the data passed.
     * 
     * @param crnCode
     *            contains the currency code.
     * @param crnName
     *            contains the currency name.
     * @param decimalDigits
     *            contains the number of decimal digits.
     */
    public CurrencyDAO(String crnCode, String crnName, Integer digits) {
        this.crnCode = crnCode;
        this.crnName = crnName;
        this.decimalDigits = digits;
    }

    /**
     * Gets currency code.
     * 
     * @return returns currency code.
     */
    public String getCrnCode() {
        return crnCode;
    }

    /**
     * Gets currency name.
     * 
     * @return returns currency name.
     */
    public String getCrnName() {
        return crnName;
    }

    /**
     * Gets the number of decimal digits.
     * 
     * @return returns the number of decimal digits.
     */
    public Integer getDecimalDigits() {
        return decimalDigits;
    }
}
