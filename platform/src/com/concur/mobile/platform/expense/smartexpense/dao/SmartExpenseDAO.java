package com.concur.mobile.platform.expense.smartexpense.dao;

import java.util.Calendar;

import android.content.Context;
import android.net.Uri;

import com.concur.mobile.platform.expense.provider.Expense;

/**
 * Provides an interface describing a smart expense DAO object.
 * 
 * @author andrewk
 */
public interface SmartExpenseDAO {

    /**
     * Gets the smart expense Id.
     * 
     * Smart Expense ID format: <code>CctKey|TripId?SegType?SegId|EReceiptId|PctKey|MeKey|RcKey</code>
     * 
     * @return the smart expense Id.
     */
    public String getSmartExpenseId();

    /**
     * Gets the report entry key.
     * 
     * @return the report entry key.
     */
    public String getRpeKey();

    /**
     * Gets the E-Receipt id.
     * 
     * @return the E-Receipt id.
     */
    public String getEReceiptId();

    /**
     * Gets the E-Receipt type.
     * 
     * @return the E-Receipt type.
     */
    public String getEReceiptType();

    /**
     * Gets the E-Receipt source.
     * 
     * @return the E-Receipt source.
     */
    public String getEReceiptSource();

    /**
     * Gets the travel company code.
     * 
     * @return the travel company code.
     */
    public String getTravelCompanyCode();

    /**
     * Gets the vendor code.
     * 
     * @return the vendor code.
     */
    public String getVendorCode();

    /**
     * Gets the airline code.
     * 
     * @return the airline code.
     */
    public String getAirlineCode();

    /**
     * Gets the trip id.
     * 
     * @return the trip id.
     */
    public String getTripId();

    /**
     * Gets the transaction date.
     * 
     * @return the transaction date.
     */
    public Calendar getTransactionDate();

    /**
     * Gets the trip name.
     * 
     * @return the trip name.
     */
    public String getTripName();

    /**
     * Gets the expense name.
     * 
     * @return the expense name.
     */
    public String getExpenseName();

    /**
     * Gets the currency code.
     * 
     * @return the currency code.
     */
    public String getCrnCode();

    /**
     * Gets the transaction amount.
     * 
     * @return the transaction amount.
     */
    public Double getTransactionAmount();

    /**
     * Gets the posted amount.
     * 
     * @return the posted amount.
     */
    public Double getPostedAmount();

    /**
     * Gets the exchange rate.
     * 
     * @return the exchange rate.
     */
    public Double getExchangeRate();

    /**
     * Gets the transaction currency code.
     * 
     * @return the transaction currency code.
     */
    public String getTransactionCurrencyCode();

    /**
     * Gets the posted currency code.
     * 
     * @return the posted currency code.
     */
    public String getPostedCurrencyCode();

    /**
     * Gets the segment id.
     * 
     * @return the segment id.
     */
    public String getSegmentId();

    /**
     * Gets the credit card transaction key.
     * 
     * @return the credit card transaction key.
     */
    public String getCctKey();

    /**
     * Gets the credit card account key.
     * 
     * @return the credit card account key.
     */
    public String getCcaKey();

    /**
     * Gets the extract credit card transaction key.
     * 
     * @return the extract credit card transaction key.
     */
    public String getExtractCctKey();

    /**
     * Gets the segment type key.
     * 
     * @return the segment type key.
     */
    public String getSegmentTypeKey();

    /**
     * Gets the vendor li name.
     * 
     * @return the vendor li name.
     */
    public String getVenLiName();

    /**
     * Gets the merchant name.
     * 
     * @return the merchant name.
     */
    public String getMerchantName();

    /**
     * Get the "doing business as" name.
     * 
     * @return the "doing business as" name.
     */
    public String getDoingBusinessAs();

    /**
     * Gets the expense key.
     * 
     * @return the expense key.
     */
    public String getExpKey();

    /**
     * Gets the location name.
     * 
     * @return the location name.
     */
    public String getLocName();

    /**
     * Gets the merchant city.
     * 
     * @return the merchant city.
     */
    public String getMerchantCity();

    /**
     * Gets the merchant state.
     * 
     * @return the merchant state.
     */
    public String getMerchantState();

    /**
     * Gets the merchant country code.
     * 
     * @return the merchant country code.
     */
    public String getMerchantCountryCode();

    /**
     * Gets the vendor description.
     * 
     * @return the vendor description.
     */
    public String getVendorDescription();

    /**
     * Gets the city.
     * 
     * @return the city.
     */
    public String getCity();

    /**
     * Gets the state.
     * 
     * @return the state.
     */
    public String getState();

    /**
     * Gets the country.
     * 
     * @return the country.
     */
    public String getCountry();

    /**
     * Gets the card type code.
     * 
     * @return the card type code.
     */
    public String getCardTypeCode();

    /**
     * Gets whether this smart expense has rich data.
     * 
     * @return whether this smart expense has rich data.
     */
    public Boolean hasRichData();

    /**
     * Gets the estimated amount.
     * 
     * @return the estimated amount.
     */
    public Double getEstimatedAmount();

    /**
     * Gets the mobile entry key.
     * 
     * @return the mobile entry key.
     */
    public String getMeKey();

    /**
     * Gets the personal card transaction key.
     * 
     * @return the personal card transaction key.
     */
    public String getPctKey();

    /**
     * Gets the personal card account key.
     * 
     * @return the personal card account key.
     */
    public String getPcaKey();

    /**
     * Gets the charge description.
     * 
     * @return the charge description.
     */
    public String getChargeDescription();

    /**
     * Gets the card category name.
     * 
     * @return the card category name.
     */
    public String getCardCategoryName();

    /**
     * Gets the transaction group.
     * 
     * @return the transaction group.
     */
    public String getTransactionGroup();

    /**
     * Gets the mobile receipt image id.
     * 
     * @return the mobile receipt image id.
     */
    public String getMobileReceiptImageId();

    /**
     * Gets the card icon file name.
     * 
     * @return the card icon file name.
     */
    public String getCardIconFileName();

    /**
     * Gets the card program type name.
     * 
     * @return the card program type name.
     */
    public String getCardProgramTypeName();

    /**
     * Gets the receipt capture key.
     * 
     * @return the receipt capture key.
     */
    public String getRcKey();

    /**
     * Gets the stat key. <br>
     * <br>
     * For a definition of values:
     * 
     * @see Expense.ReceiptColumns#OCR_STAT_KEY
     * 
     * @return the stat key.
     */
    public String getStatKey();

    /**
     * Gets the reject code. <br>
     * <br>
     * For a definition of values:
     * 
     * @see Expense.ReceiptColumns#OCR_REJECT_CODE
     * 
     * @return the reject code.
     */
    public String getRejectCode();

    /**
     * Gets the receipt image id (this is the Smart Capture/ExpenseIt image id).
     * 
     * @return the receipt image id.
     */
    public String getReceiptImageId();

    /**
     * Gets the fuel service charge.
     * 
     * @return the fuel service charge.
     */
    public Double getFuelServiceCharge();

    /**
     * Gets the GPS charge.
     * 
     * @return the GPS charge.
     */
    public Double getGpsCharge();

    /**
     * Gets the insurance charge.
     * 
     * @return the insurance charge.
     */
    public Double getInsuranceCharge();

    /**
     * Gets the card last segment.
     * 
     * @return the card last segment.
     */
    public String getCardLastSegment();

    /**
     * Gets the ticket number.
     * 
     * @return the ticket number.
     */
    public String getTicketNumber();

    /**
     * Gets the E-Receipt image id.
     * 
     * @return the E-Receipt image id.
     */
    public String getEReceiptImageId();

    /**
     * Gets the corporate card transaction receipt image id.
     * 
     * @return the corporate card transaction receipt image id.
     */
    public String getCctReceiptImageId();

    /**
     * Gets the comment.
     * 
     * @return the comment.
     */
    public String getComment();

    /**
     * Gets the total days.
     * 
     * @return the total days.
     */
    public Integer getTotalDays();

    /**
     * Gets the pick-up date.
     * 
     * @return the pick-up date.
     */
    public Calendar getPickUpDate();

    /**
     * Gets the return date.
     * 
     * @return the return date.
     */
    public Calendar getReturnDate();

    /**
     * Gets the confirmation number.
     * 
     * @return the confirmation number.
     */
    public String getConfirmationNumber();

    /**
     * Gets the average daily rate.
     * 
     * @return the average daily rate.
     */
    public Double getAverageDailyRate();

    /**
     * Gets the last time this DAO was synced with the server.
     *
     * @return the last time this DAO was synced with the server.
     */
    public Calendar getLastSyncTime();

    /**
     * Gets the content uri associated with this DAO object.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @return the content Uri associated with this DAO object.
     */
    public Uri getContentURI(Context context, String userId);

    /**
     * Will perform an update based on current values.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * 
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean update(Context context, String userId);

    /**
     * Will delete the smart expense.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * 
     * @return <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean delete(Context context, String userId);

}
