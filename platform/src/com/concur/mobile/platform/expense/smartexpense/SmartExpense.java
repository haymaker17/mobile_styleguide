package com.concur.mobile.platform.expense.smartexpense;

import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;
import com.google.gson.annotations.SerializedName;

/**
 * An Implementation of smart expense data access object for the purpose of get the smart expense.
 * 
 * @author sunill
 */
public class SmartExpense implements SmartExpenseDAO {

    private static final String CLS_TAG = "SmartExpense";

    // full column list of smart expense.
    public static String[] fullColumnList = { Expense.SmartExpenseColumns._ID

    , Expense.SmartExpenseColumns.FUEL_SERVICE_CHARGE

    , Expense.SmartExpenseColumns.ESTIMATED_AMOUNT

    , Expense.SmartExpenseColumns.CCA_KEY

    , Expense.SmartExpenseColumns.CCT_KEY

    , Expense.SmartExpenseColumns.EXTRACT_CCT_KEY

    , Expense.SmartExpenseColumns.SMART_EXPENSE_ID

    , Expense.SmartExpenseColumns.E_RECEIPT_SOURCE

    , Expense.SmartExpenseColumns.TRANSACTION_DATE

    , Expense.SmartExpenseColumns.HAS_RICH_DATA

    , Expense.SmartExpenseColumns.E_RECEIPT_ID

    , Expense.SmartExpenseColumns.SEGMENT_ID

    , Expense.SmartExpenseColumns.EXP_NAME

    , Expense.SmartExpenseColumns.TRANSACTION_GROUP

    , Expense.SmartExpenseColumns.VENDOR_CODE

    , Expense.SmartExpenseColumns.COUNTRY

    , Expense.SmartExpenseColumns.TRIP_ID

    , Expense.SmartExpenseColumns.VENDOR_DESCRIPTION

    , Expense.SmartExpenseColumns.EXCHANGE_RATE

    , Expense.SmartExpenseColumns.POSTED_AMOUNT

    , Expense.SmartExpenseColumns.TRANSACTION_AMOUNT

    , Expense.SmartExpenseColumns.INSURANCE_CHARGE

    , Expense.SmartExpenseColumns.GPS_CHARGE

    , Expense.SmartExpenseColumns.CARD_LAST_SEGMENT

    , Expense.SmartExpenseColumns.CRN_CODE

    , Expense.SmartExpenseColumns.LOC_NAME

    , Expense.SmartExpenseColumns.EXP_KEY

    , Expense.SmartExpenseColumns.TRAVEL_COMPANY_CODE

    , Expense.SmartExpenseColumns.MERCHANT_STATE

    , Expense.SmartExpenseColumns.MERCHANT_CITY

    , Expense.SmartExpenseColumns.MERCHANT_CUNTRY_CODE

    , Expense.SmartExpenseColumns.MERCHANT_NAME

    , Expense.SmartExpenseColumns.POSTED_CRN_CODE

    , Expense.SmartExpenseColumns.TRIP_NAME

    , Expense.SmartExpenseColumns.CITY

    , Expense.SmartExpenseColumns.E_RECEIPT_TYPE

    , Expense.SmartExpenseColumns.STATE

    , Expense.SmartExpenseColumns.TRANSACTION_CRN_CODE

    , Expense.SmartExpenseColumns.TICKET_NUMBER

    , Expense.SmartExpenseColumns.E_RECEIPT_IMAGE_ID

    , Expense.SmartExpenseColumns.VEN_LI_NAME

    , Expense.SmartExpenseColumns.RPE_KEY

    , Expense.SmartExpenseColumns.AIRLINE_CODE

    , Expense.SmartExpenseColumns.SEGMENT_TYPE_KEY

    , Expense.SmartExpenseColumns.DOING_BUSINESS_AS

    , Expense.SmartExpenseColumns.CARD_TYPE_CODE

    , Expense.SmartExpenseColumns.ME_KEY

    , Expense.SmartExpenseColumns.PCT_KEY

    , Expense.SmartExpenseColumns.PCA_KEY

    , Expense.SmartExpenseColumns.CHARGE_DESC

    , Expense.SmartExpenseColumns.CARD_CATEGORY_NAME

    , Expense.SmartExpenseColumns.MOB_RECEIPT_ID

    , Expense.SmartExpenseColumns.CARD_ICON_FILE_NAME

    , Expense.SmartExpenseColumns.CARD_PROGRAM_TYPE_NAME

    , Expense.SmartExpenseColumns.RC_KEY

    , Expense.SmartExpenseColumns.STATUS_KEY

    , Expense.SmartExpenseColumns.REJECT_CODE

    , Expense.SmartExpenseColumns.RECEIPT_IMAGE_ID

    , Expense.SmartExpenseColumns.CCT_RECEIPT_IMG_ID

    , Expense.SmartExpenseColumns.COMMENT

    , Expense.SmartExpenseColumns.TOTAL_DAYS

    , Expense.SmartExpenseColumns.PICK_UP_DATE

    , Expense.SmartExpenseColumns.RETURN_DATE

    , Expense.SmartExpenseColumns.CONFIRMATION_NUMBER

    , Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE

    , Expense.SmartExpenseColumns.USER_ID };

    /**
     * Contains the Smart Expense Id
     */
    protected String smartExpenseId;

    /**
     * Contains the Report Entry Key
     */
    protected String rpeKey;

    /**
     * Contains the E-receipt Id
     */
    @SerializedName("ereceiptId")
    protected String eReceiptId;

    /**
     * Contains the E-receipt type
     */
    @SerializedName("ereceiptType")
    protected String eReceiptType;

    /**
     * Contains the E-receipt source
     */
    @SerializedName("ereceiptSource")
    protected String eReceiptSource;

    /**
     * Contains the Travel company code
     */
    protected String travelCompanyCode;

    /**
     * Contains the Vendor code
     */
    protected String vendorCode;

    /**
     * Contains the Airline code
     */
    protected String airlineCode;

    /**
     * Contains the trip Id
     */
    protected String tripId;

    /**
     * Contains the Transaction date
     */
    protected Calendar transactionDate;

    /**
     * Contains the Trip name
     */
    protected String tripName;

    /**
     * Contains the Expense name
     */
    @SerializedName("expName")
    protected String expenseName;

    /**
     * Contains the Currency code
     */
    protected String crnCode;

    /**
     * Contains the Transaction amount
     */
    protected Double transactionAmount;

    /**
     * Contains the Posted amount
     */
    protected Double postedAmount;

    /**
     * Contains the Exchange rate
     */
    protected Double exchangeRate;

    /**
     * Contains the Transaction currency code
     */
    protected String transactionCrnCode;

    /**
     * Contains the Posted currency code
     */
    protected String postedCrnCode;

    /**
     * Contains the Segment id
     */
    protected String segmentId;

    /**
     * Contains the CCT key
     */
    protected String cctKey;

    /**
     * Contains the CCA Key
     */
    protected String ccaKey;

    /**
     * Contains the Extracted CCT key
     */
    protected String extractCctKey;

    /**
     * Contains the Segment type key
     */
    protected String segmentTypeKey;

    /**
     * Contains the Vendor Li-name
     */
    protected String venLiName;

    /**
     * Contains the Merchant name
     */
    protected String merchantName;

    /**
     * Contains the Doing business as
     */
    protected String doingBusinessAs;

    /**
     * Contains the Expense key
     */
    protected String expKey;

    /**
     * Contains the Location name
     */
    protected String locName;

    /**
     * Contains the Merchant city
     */
    protected String merchantCity;

    /**
     * Contains the Merchant State
     */
    protected String merchantState;

    /**
     * Contains the Merchant Country Code
     */
    protected String merchantCountryCode;

    /**
     * Contains the Vendor Description
     */
    protected String vendorDescription;

    /**
     * Contains the City
     */
    protected String city;

    /**
     * Contains the State
     */
    protected String state;

    /**
     * Contains the Country
     */
    protected String country;

    /**
     * Contains the card type code
     */
    protected String cardTypeCode;

    /**
     * Contains the Has rich data
     */
    protected Boolean hasRichData;

    /**
     * Contains the Estimated Amount
     */
    protected Double estimatedAmount;

    /**
     * Contains the Mobile entry key
     */
    protected String meKey;

    /**
     * Contains the PCT key
     */
    protected String pctKey;

    /**
     * Contains the PCA key
     */
    protected String pcaKey;

    /**
     * Contains the Charge description
     */
    protected String chargeDescription;

    /**
     * Contains the Card category name
     */
    protected String cardCategoryName;

    /**
     * Contains the Transaction group
     */
    protected String transactionGroup;

    /**
     * Contains the Mobile receipt image id
     */
    protected String mobileReceiptImageId;

    /**
     * Contains the Card icon file name
     */
    protected String cardIconFileName;

    /**
     * Contains the Card program type name
     */
    protected String cardProgramTypeName;

    /**
     * Contains the Rckey
     */
    protected String rcKey;

    /**
     * Contains the Status key
     */
    protected String statKey;

    /**
     * Contains the Reject Code
     */
    protected String rejectCode;

    /**
     * Contains the Receipt image id
     */
    protected String receiptImageId;

    /**
     * Contains the Fuel service charge
     */
    protected Double fuelServiceCharge;

    /**
     * Contains the GPS charge
     */
    protected Double gpsCharge;

    /**
     * Contains the Insurance charge
     */
    protected Double insuranceCharge;

    /**
     * Contains the Card last segment
     */
    protected String cardLastSegment;

    /**
     * Contains the Ticket number
     */
    protected String ticketNumber;

    /**
     * Contains the E-receipt image id
     */
    protected String eReceiptImageId;

    /**
     * Contains the CCT receipt image id
     */
    protected String cctReceiptImageId;

    /**
     * Contains the comment
     */
    protected String comment;

    /**
     * Contains the total days.
     */
    protected Integer totalDays;

    /**
     * Contains the pick-up date.
     */
    @SerializedName("pickupDate")
    protected Calendar pickUpDate;

    /**
     * Contains the return date.
     */
    protected Calendar returnDate;

    /**
     * Contains the confirmation number.
     */
    protected String confirmationNumber;

    /**
     * Contains the average daily rate.
     */
    protected Double averageDailyRate;

    /**
     * Contains the content Uri.
     */
    private Uri contentUri;

    /**
     * The last time this SmartExpense was synced with the remote server.
     */
    protected Calendar lastSyncTime;

    /**
     * Contains the list of smart expenses.
     */
    protected List<SmartExpenseDAO> smartExpenses;

    /**
     * Contains the user id.
     */
    protected transient String userId;

    /**
     * Contains the application context.
     */
    protected transient Context context;

    /**
     * Constructs a new instnace of <code>SmartExpense</code>.
     */
    public SmartExpense() {
    }

    /**
     * Will construct an instance of <code>Receipt</code> with an application context.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     */
    public SmartExpense(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    /**
     * Constructs an instance of <code>SmartExpense</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     *            contains the cursor.
     */
    public SmartExpense(Cursor cursor) {
        init(cursor);
    }

    /**
     * Constructs an instance of <code>SmartExpense</code> based on reading values from a <code>Uri</code> object.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content Uri.
     */
    public SmartExpense(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Expense.SmartExpenseColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    init(cursor);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Will initialize the mobile entry fields from a cursor object.
     * 
     * @param cursor
     *            contains the cursor used to initialize fields.
     */
    private void init(Cursor cursor) {
        smartExpenseId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.SMART_EXPENSE_ID);

        rpeKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.RPE_KEY);

        eReceiptId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.E_RECEIPT_ID);

        eReceiptType = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.E_RECEIPT_TYPE);

        eReceiptSource = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.E_RECEIPT_SOURCE);

        travelCompanyCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.TRAVEL_COMPANY_CODE);

        vendorCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.VENDOR_CODE);

        airlineCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.AIRLINE_CODE);

        tripId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.TRIP_ID);

        Long transDateMillis = CursorUtil.getLongValue(cursor, Expense.SmartExpenseColumns.TRANSACTION_DATE);
        if (transDateMillis != null) {
            transactionDate = Calendar.getInstance(Parse.UTC);
            transactionDate.setTimeInMillis(transDateMillis);
            transactionDate.set(Calendar.MILLISECOND, 0);
        }

        tripName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.TRIP_NAME);

        expenseName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.EXP_NAME);

        crnCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CRN_CODE);

        transactionAmount = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.TRANSACTION_AMOUNT);

        postedAmount = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.POSTED_AMOUNT);

        exchangeRate = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.EXCHANGE_RATE);

        transactionCrnCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.TRANSACTION_CRN_CODE);

        postedCrnCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.POSTED_CRN_CODE);

        segmentId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.SEGMENT_ID);

        cctKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CCT_KEY);

        ccaKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CCA_KEY);

        extractCctKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.EXTRACT_CCT_KEY);

        segmentTypeKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.SEGMENT_TYPE_KEY);

        venLiName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.VEN_LI_NAME);

        merchantName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.MERCHANT_NAME);

        doingBusinessAs = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.DOING_BUSINESS_AS);

        expKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.EXP_KEY);

        locName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.LOC_NAME);

        merchantCity = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.MERCHANT_CITY);

        merchantState = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.MERCHANT_STATE);

        merchantCountryCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.MERCHANT_CUNTRY_CODE);

        vendorDescription = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.VENDOR_DESCRIPTION);

        city = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CITY);

        state = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.STATE);

        country = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.COUNTRY);

        cardTypeCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CARD_TYPE_CODE);

        hasRichData = CursorUtil.getBooleanValue(cursor, Expense.SmartExpenseColumns.HAS_RICH_DATA);

        estimatedAmount = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.ESTIMATED_AMOUNT);

        meKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.ME_KEY);

        pctKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.PCT_KEY);

        pcaKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.PCA_KEY);

        chargeDescription = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CHARGE_DESC);

        cardCategoryName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CARD_CATEGORY_NAME);

        transactionGroup = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.TRANSACTION_GROUP);

        mobileReceiptImageId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.MOB_RECEIPT_ID);

        cardIconFileName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CARD_ICON_FILE_NAME);

        cardProgramTypeName = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CARD_PROGRAM_TYPE_NAME);

        rcKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.RC_KEY);

        statKey = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.STATUS_KEY);

        rejectCode = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.REJECT_CODE);

        receiptImageId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.RECEIPT_IMAGE_ID);

        fuelServiceCharge = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.FUEL_SERVICE_CHARGE);

        gpsCharge = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.GPS_CHARGE);

        insuranceCharge = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.INSURANCE_CHARGE);

        cardLastSegment = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CARD_LAST_SEGMENT);

        ticketNumber = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.TICKET_NUMBER);

        eReceiptImageId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.E_RECEIPT_IMAGE_ID);

        cctReceiptImageId = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CCT_RECEIPT_IMG_ID);

        comment = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.COMMENT);

        totalDays = CursorUtil.getIntValue(cursor, Expense.SmartExpenseColumns.TOTAL_DAYS);

        Long pickUpDateMillis = CursorUtil.getLongValue(cursor, Expense.SmartExpenseColumns.PICK_UP_DATE);
        if (pickUpDateMillis != null) {
            pickUpDate = Calendar.getInstance(Parse.UTC);
            pickUpDate.setTimeInMillis(pickUpDateMillis);
            pickUpDate.set(Calendar.MILLISECOND, 0);
        }

        Long returnDateMillis = CursorUtil.getLongValue(cursor, Expense.SmartExpenseColumns.RETURN_DATE);
        if (returnDateMillis != null) {
            returnDate = Calendar.getInstance(Parse.UTC);
            returnDate.setTimeInMillis(returnDateMillis);
            returnDate.set(Calendar.MILLISECOND, 0);
        }

        confirmationNumber = CursorUtil.getStringValue(cursor, Expense.SmartExpenseColumns.CONFIRMATION_NUMBER);

        averageDailyRate = CursorUtil.getDoubleValue(cursor, Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE);

        Long contentId = CursorUtil.getLongValue(cursor, Expense.SmartExpenseColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.SmartExpenseColumns.CONTENT_URI, contentId);
        }
    }

    @Override
    public String getSmartExpenseId() {
        return smartExpenseId;
    };

    @Override
    public String getRpeKey() {
        return rpeKey;
    }

    @Override
    public String getEReceiptId() {
        return eReceiptId;
    }

    @Override
    public String getEReceiptType() {
        return eReceiptType;
    }

    @Override
    public String getEReceiptSource() {
        return eReceiptSource;
    }

    @Override
    public String getTravelCompanyCode() {
        return travelCompanyCode;
    }

    @Override
    public String getVendorCode() {
        return vendorCode;
    }

    @Override
    public String getAirlineCode() {
        return airlineCode;
    }

    @Override
    public String getTripId() {
        return tripId;
    }

    @Override
    public Calendar getTransactionDate() {
        return transactionDate;
    }

    @Override
    public String getTripName() {
        return tripName;
    }

    @Override
    public String getExpenseName() {
        return expenseName;
    }

    @Override
    public String getCrnCode() {
        return crnCode;
    }

    @Override
    public Double getTransactionAmount() {
        return transactionAmount;
    }

    @Override
    public Double getPostedAmount() {
        return postedAmount;
    }

    @Override
    public Double getExchangeRate() {
        return exchangeRate;
    }

    @Override
    public String getTransactionCurrencyCode() {
        return transactionCrnCode;
    }

    @Override
    public String getPostedCurrencyCode() {
        return postedCrnCode;
    }

    @Override
    public String getSegmentId() {
        return segmentId;
    }

    @Override
    public String getCctKey() {
        return cctKey;
    }

    @Override
    public String getCcaKey() {
        return ccaKey;
    }

    @Override
    public String getExtractCctKey() {
        return extractCctKey;
    }

    @Override
    public String getSegmentTypeKey() {
        return segmentTypeKey;
    }

    @Override
    public String getVenLiName() {
        return venLiName;
    }

    @Override
    public String getMerchantName() {
        return merchantName;
    }

    @Override
    public String getDoingBusinessAs() {
        return doingBusinessAs;
    }

    @Override
    public String getExpKey() {
        return expKey;
    }

    @Override
    public String getLocName() {
        return locName;
    }

    @Override
    public String getMerchantCity() {
        return merchantCity;
    }

    @Override
    public String getMerchantState() {
        return merchantState;
    }

    @Override
    public String getMerchantCountryCode() {
        return merchantCountryCode;
    }

    @Override
    public String getVendorDescription() {
        return vendorDescription;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public String getCardTypeCode() {
        return cardTypeCode;
    }

    @Override
    public Boolean hasRichData() {
        return hasRichData;
    }

    @Override
    public Double getEstimatedAmount() {
        return estimatedAmount;
    }

    @Override
    public String getMeKey() {
        return meKey;
    }

    @Override
    public String getPctKey() {
        return pctKey;
    }

    @Override
    public String getPcaKey() {
        return pcaKey;
    }

    @Override
    public String getChargeDescription() {
        return chargeDescription;
    }

    @Override
    public String getCardCategoryName() {
        return cardCategoryName;
    }

    @Override
    public String getTransactionGroup() {
        return transactionGroup;
    }

    @Override
    public String getMobileReceiptImageId() {
        return mobileReceiptImageId;
    }

    @Override
    public String getCardIconFileName() {
        return cardIconFileName;
    }

    @Override
    public String getCardProgramTypeName() {
        return cardProgramTypeName;
    }

    @Override
    public String getRcKey() {
        return rcKey;
    }

    @Override
    public String getStatKey() {
        return statKey;
    }

    @Override
    public String getRejectCode() {
        return rejectCode;
    }

    @Override
    public String getReceiptImageId() {
        return receiptImageId;
    }

    @Override
    public Double getFuelServiceCharge() {
        return fuelServiceCharge;
    }

    @Override
    public Double getGpsCharge() {
        return gpsCharge;
    }

    @Override
    public Double getInsuranceCharge() {
        return insuranceCharge;
    }

    @Override
    public String getCardLastSegment() {
        return cardLastSegment;
    }

    @Override
    public String getTicketNumber() {
        return ticketNumber;
    }

    @Override
    public String getEReceiptImageId() {
        return eReceiptImageId;
    }

    @Override
    public String getCctReceiptImageId() {
        return cctReceiptImageId;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public Integer getTotalDays() {
        return totalDays;
    }

    @Override
    public Calendar getPickUpDate() {
        return pickUpDate;
    }

    @Override
    public Calendar getReturnDate() {
        return returnDate;
    }

    @Override
    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    @Override
    public Double getAverageDailyRate() {
        return averageDailyRate;
    }

    @Override
    public Calendar getLastSyncTime() {
        return lastSyncTime;
    }

    @Override
    public Uri getContentURI(Context context, String userId) {
        if (!TextUtils.isEmpty(smartExpenseId)) {
            String[] columnNames = { Expense.SmartExpenseColumns.SMART_EXPENSE_ID, Expense.SmartExpenseColumns.USER_ID };
            String[] columnValues = { smartExpenseId, userId };
            contentUri = ContentUtils.getContentUri(context, Expense.SmartExpenseColumns.CONTENT_URI, columnNames,
                    columnValues);
        }
        return contentUri;
    }

    @Override
    public boolean update(Context context, String userId) {
        boolean retVal = true;

        ContentResolver resolver = context.getContentResolver();

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.SMART_EXPENSE_ID, smartExpenseId);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.RPE_KEY, rpeKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.E_RECEIPT_ID, eReceiptId);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.E_RECEIPT_TYPE, eReceiptType);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.E_RECEIPT_SOURCE, eReceiptSource);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TRAVEL_COMPANY_CODE, travelCompanyCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.VENDOR_CODE, vendorCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.AIRLINE_CODE, airlineCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TRIP_ID, tripId);

        Long transDateInMillis = null;
        if (transactionDate != null) {
            transDateInMillis = transactionDate.getTimeInMillis();
        }
        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TRANSACTION_DATE, transDateInMillis);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TRIP_NAME, tripName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.EXP_NAME, expenseName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CRN_CODE, crnCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TRANSACTION_AMOUNT, transactionAmount);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.POSTED_AMOUNT, postedAmount);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.EXCHANGE_RATE, exchangeRate);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TRANSACTION_CRN_CODE, transactionCrnCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.POSTED_CRN_CODE, postedCrnCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.SEGMENT_ID, segmentId);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CCT_KEY, cctKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CCA_KEY, ccaKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.EXTRACT_CCT_KEY, extractCctKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.SEGMENT_TYPE_KEY, segmentTypeKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.VEN_LI_NAME, venLiName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.MERCHANT_NAME, merchantName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.DOING_BUSINESS_AS, doingBusinessAs);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.EXP_KEY, expKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.LOC_NAME, locName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.MERCHANT_CITY, merchantCity);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.MERCHANT_STATE, merchantState);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.MERCHANT_CUNTRY_CODE, merchantCountryCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.VENDOR_DESCRIPTION, vendorDescription);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CITY, city);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.STATE, state);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.COUNTRY, country);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CARD_TYPE_CODE, cardTypeCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.HAS_RICH_DATA, hasRichData);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.ESTIMATED_AMOUNT, estimatedAmount);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.ME_KEY, meKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.PCT_KEY, pctKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.PCA_KEY, pcaKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CHARGE_DESC, chargeDescription);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CARD_CATEGORY_NAME, cardCategoryName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TRANSACTION_GROUP, transactionGroup);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.MOB_RECEIPT_ID, mobileReceiptImageId);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CARD_ICON_FILE_NAME, cardIconFileName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CARD_PROGRAM_TYPE_NAME, cardProgramTypeName);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.RC_KEY, rcKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.STATUS_KEY, statKey);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.REJECT_CODE, rejectCode);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.RECEIPT_IMAGE_ID, receiptImageId);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.FUEL_SERVICE_CHARGE, fuelServiceCharge);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.GPS_CHARGE, gpsCharge);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.INSURANCE_CHARGE, insuranceCharge);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CARD_LAST_SEGMENT, cardLastSegment);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TICKET_NUMBER, ticketNumber);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.E_RECEIPT_IMAGE_ID, eReceiptImageId);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CCT_RECEIPT_IMG_ID, cctReceiptImageId);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.COMMENT, comment);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.TOTAL_DAYS, totalDays);

        Long pickUpDateInMillis = null;
        if (pickUpDate != null) {
            pickUpDateInMillis = pickUpDate.getTimeInMillis();
        }
        ContentUtils.putValue(values, Expense.SmartExpenseColumns.PICK_UP_DATE, pickUpDateInMillis);

        Long returnDateInMillis = null;
        if (returnDate != null) {
            returnDateInMillis = returnDate.getTimeInMillis();
        }
        ContentUtils.putValue(values, Expense.SmartExpenseColumns.RETURN_DATE, returnDateInMillis);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.CONFIRMATION_NUMBER, confirmationNumber);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE, averageDailyRate);

        ContentUtils.putValue(values, Expense.SmartExpenseColumns.USER_ID, userId);

        contentUri = getContentURI(context, userId);
        if (contentUri != null) {
            // Perform an update.
            int rowsUpdated = resolver.update(contentUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + contentUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.SmartExpenseColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                retVal = true;
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".update: more than 1 row updated for Uri '" + contentUri.toString()
                            + "'.");
                }
            }
            retVal = (rowsUpdated == 1);
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Expense.SmartExpenseColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }
        return retVal;
    }

    @Override
    public boolean delete(Context context, String userId) {
        boolean retVal = true;

        contentUri = getContentURI(context, userId);
        if (contentUri != null) {
            // delete the actual entry in the database.
            ContentResolver resolver = context.getContentResolver();
            int count = resolver.delete(contentUri, null, null);
            retVal = (count == 1);
            // Clear out the URI.
            contentUri = null;
        } else {
            retVal = false;
        }
        return retVal;
    }

    // TODO All setters methods are temporary, we need to remove it once smart expense with OCR will be implemented.
    public void setSmartExpenseId(String smartExpenseId) {
        this.smartExpenseId = smartExpenseId;
    }

    public void setRpeKey(String rpeKey) {
        this.rpeKey = rpeKey;
    }

    public void seteReceiptId(String eReceiptId) {
        this.eReceiptId = eReceiptId;
    }

    public void seteReceiptType(String eReceiptType) {
        this.eReceiptType = eReceiptType;
    }

    public void seteReceiptSource(String eReceiptSource) {
        this.eReceiptSource = eReceiptSource;
    }

    public void setTravelCompanyCode(String travelCompanyCode) {
        this.travelCompanyCode = travelCompanyCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setTransactionDate(Calendar transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    public void setCrnCode(String crnCode) {
        this.crnCode = crnCode;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public void setPostedAmount(Double postedAmount) {
        this.postedAmount = postedAmount;
    }

    public void setExchangeRate(Double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setTransactionCrnCode(String transactionCrnCode) {
        this.transactionCrnCode = transactionCrnCode;
    }

    public void setPostedCrnCode(String postedCrnCode) {
        this.postedCrnCode = postedCrnCode;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public void setCctKey(String cctKey) {
        this.cctKey = cctKey;
    }

    public void setCcaKey(String ccaKey) {
        this.ccaKey = ccaKey;
    }

    public void setExtractCctKey(String extractCctKey) {
        this.extractCctKey = extractCctKey;
    }

    public void setSegmentTypeKey(String segmentTypeKey) {
        this.segmentTypeKey = segmentTypeKey;
    }

    public void setVenLiName(String venLiName) {
        this.venLiName = venLiName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setDoingBusinessAs(String doingBusinessAs) {
        this.doingBusinessAs = doingBusinessAs;
    }

    public void setExpKey(String expKey) {
        this.expKey = expKey;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public void setMerchantState(String merchantState) {
        this.merchantState = merchantState;
    }

    public void setMerchantCountryCode(String merchantCountryCode) {
        this.merchantCountryCode = merchantCountryCode;
    }

    public void setVendorDescription(String vendorDescription) {
        this.vendorDescription = vendorDescription;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCardTypeCode(String cardTypeCode) {
        this.cardTypeCode = cardTypeCode;
    }

    public void setHasRichData(Boolean hasRichData) {
        this.hasRichData = hasRichData;
    }

    public void setEstimatedAmount(Double estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public void setMeKey(String meKey) {
        this.meKey = meKey;
    }

    public void setPctKey(String pctKey) {
        this.pctKey = pctKey;
    }

    public void setPcaKey(String pcaKey) {
        this.pcaKey = pcaKey;
    }

    public void setChargeDescription(String chargeDescription) {
        this.chargeDescription = chargeDescription;
    }

    public void setCardCategoryName(String cardCategoryName) {
        this.cardCategoryName = cardCategoryName;
    }

    public void setTransactionGroup(String transactionGroup) {
        this.transactionGroup = transactionGroup;
    }

    public void setMobileReceiptImageId(String mobileReceiptImageId) {
        this.mobileReceiptImageId = mobileReceiptImageId;
    }

    public void setCardIconFileName(String cardIconFileName) {
        this.cardIconFileName = cardIconFileName;
    }

    public void setCardProgramTypeName(String cardProgramTypeName) {
        this.cardProgramTypeName = cardProgramTypeName;
    }

    public void setRcKey(String rcKey) {
        this.rcKey = rcKey;
    }

    public void setStatKey(String statKey) {
        this.statKey = statKey;
    }

    public void setRejectCode(String rejectCode) {
        this.rejectCode = rejectCode;
    }

    public void setReceiptImageId(String receiptImageId) {
        this.receiptImageId = receiptImageId;
    }

    public void setFuelServiceCharge(Double fuelServiceCharge) {
        this.fuelServiceCharge = fuelServiceCharge;
    }

    public void setGpsCharge(Double gpsCharge) {
        this.gpsCharge = gpsCharge;
    }

    public void setInsuranceCharge(Double insuranceCharge) {
        this.insuranceCharge = insuranceCharge;
    }

    public void setCardLastSegment(String cardLastSegment) {
        this.cardLastSegment = cardLastSegment;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public void seteReceiptImageId(String eReceiptImageId) {
        this.eReceiptImageId = eReceiptImageId;
    }

    public void setCctReceiptImageId(String cctReceiptImageId) {
        this.cctReceiptImageId = cctReceiptImageId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public void setPickUpDate(Calendar pickUpDate) {
        this.pickUpDate = pickUpDate;
    }

    public void setReturnDate(Calendar returnDate) {
        this.returnDate = returnDate;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public void setAverageDailyRate(Double averageDailyRate) {
        this.averageDailyRate = averageDailyRate;
    }
    public void setLastSyncTime(Calendar calendar){
        lastSyncTime = calendar;
    }
}
