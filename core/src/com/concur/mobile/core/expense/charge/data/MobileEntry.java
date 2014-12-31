/**
 * 
 */
package com.concur.mobile.core.expense.charge.data;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.core.expense.charge.data.Expense.ExpenseEntryType;
import com.concur.mobile.core.util.Base64;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * An implementation of <code>MobileEntry</code>.
 * 
 * @author AndrewK
 */
public class MobileEntry implements Cloneable {

    private static String CLS_TAG = MobileEntry.class.getSimpleName();

    /**
     * Contains the transaction currency code.
     */
    private String crnCode;

    /**
     * Contains the expense entry type key.
     */
    private String expKey;

    /**
     * Contains the expense name.
     */
    private String expName;

    /**
     * Contains the expense location name.
     */
    private String locationName;

    /**
     * Contains the expense vendor name.
     */
    private String vendorName;

    /**
     * Contains the expense entry type.
     */
    private Expense.ExpenseEntryType entryType = Expense.ExpenseEntryType.CASH;

    /**
     * Contains the mobile expense entry key.
     */
    private String meKey;

    /**
     * Contains the personal card account key containing a transaction associated with this mobile entry.
     */
    private String pcaKey;

    /**
     * Contains the personal card transaction key for a transaction associated with this mobile entry.
     */
    private String pctKey;

    /**
     * Contains the corporate card transaction key for a corporate card transaction associated with this mobile entry.
     */
    private String cctKey;

    /**
     * Contains the receipt capture key for a receipt capture transaction associated with this mobile entry.
     */
    private String rcKey;

    /**
     * Contains the expense transaction amount.
     */
    private Double transactionAmount;

    /**
     * Contains the expense transaction date.
     */
    private String transactionDate;

    /**
     * Contains the expense entry transaction date as a <code>Calendar</code> object.
     */
    private Calendar transactionDateCalendar;

    /**
     * Contains whether the expense entry has a receipt image.
     */
    private boolean hasReceiptImage;

    /**
     * Contains the receipt image id.
     */
    private String receiptImageId;

    /**
     * Contains whether the expense entry has local receipt image data.
     */
    private boolean receiptImageDataLocal;

    /**
     * Contains the receipt image local file path.
     */
    private String receiptImageDataLocalFilePath;

    /**
     * Contains the expense comment.
     */
    private String comment;

    /**
     * Contains the expense local key.
     */
    private String localKey;

    /**
     * Contains the mobile entry update date.
     */
    private Calendar updateDate;

    /**
     * Contains the mobile entry creation date.
     */
    private Calendar createDate;

    /**
     * Contains a reference to the mobile entry status.
     */
    private MobileEntryStatus status = MobileEntryStatus.NEW;

    /**
     * Contains the smart expense id
     */
    public String smartExpenseId;

    public MobileEntry() {

        transactionAmount = 0.0;

        // Initialize the create/update time to the same value (now).
        updateDate = createDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    }

    public MobileEntry(SmartExpenseDAO smartExpense) {

        this.status = MobileEntryStatus.NORMAL;
        this.createDate = this.updateDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")); // Initialize the create/update
                                                                                               // times to the save value (now).

        crnCode = smartExpense.getCrnCode();
        expKey = smartExpense.getExpKey();
        expName = smartExpense.getExpenseName();
        locationName = smartExpense.getLocName();
        vendorName = smartExpense.getVendorDescription(); // vendorDescription for mobile entry
        if (TextUtils.isEmpty(vendorName))
            vendorName = smartExpense.getMerchantName(); // Fall back to MerchantName
        
        meKey = smartExpense.getMeKey();
        pcaKey = smartExpense.getPcaKey();
        pctKey = smartExpense.getPctKey();
        cctKey = smartExpense.getCctKey();
        rcKey = smartExpense.getRcKey();

        transactionAmount = smartExpense.getTransactionAmount();
        transactionDateCalendar = smartExpense.getTransactionDate();
        transactionDate = Format.safeFormatCalendar(FormatUtil.MONTH_DAY_FULL_YEAR_DISPLAY, transactionDateCalendar);
        receiptImageId = smartExpense.getReceiptImageId();
        comment = smartExpense.getComment();
        smartExpenseId = smartExpense.getSmartExpenseId();
        
        hasReceiptImage = true;
        if (!TextUtils.isEmpty(smartExpense.getMobileReceiptImageId())) {
            receiptImageId = smartExpense.getMobileReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getEReceiptImageId())) {
            receiptImageId = smartExpense.getEReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getCctReceiptImageId())) {
            receiptImageId = smartExpense.getCctReceiptImageId();
        } else if (!TextUtils.isEmpty(smartExpense.getReceiptImageId())) {
            receiptImageId = smartExpense.getReceiptImageId();
        } else {
            hasReceiptImage = false;
        }
        
        // E-DAO: localKey, receiptImageDataLocal, receiptImageDataLocalFilePath are all offline items

        if (!TextUtils.isEmpty(meKey)) {

            if (!TextUtils.isEmpty(cctKey)) {
                this.entryType = ExpenseEntryType.SMART_CORPORATE;
            } else if (!TextUtils.isEmpty(pctKey)) {
                this.entryType = ExpenseEntryType.SMART_PERSONAL;
            } else {
                this.entryType = ExpenseEntryType.CASH;
            }

        } else if (!TextUtils.isEmpty(cctKey)) {

            this.entryType = ExpenseEntryType.CORPORATE_CARD;

            StringBuilder strBldr = new StringBuilder();
            if (smartExpense.getMerchantCity() != null) {
                strBldr.append(smartExpense.getMerchantCity());
            }
            if (!TextUtils.isEmpty(smartExpense.getMerchantState())) {
                if (strBldr.length() > 0) {
                    strBldr.append(',');
                }
                strBldr.append(smartExpense.getMerchantState());
            }
            if (!TextUtils.isEmpty(smartExpense.getMerchantCountryCode())) {
                if (strBldr.length() > 0) {
                    strBldr.append(',');
                }
                strBldr.append(smartExpense.getMerchantCountryCode());
            }
            this.locationName = strBldr.toString();

        } else if (!TextUtils.isEmpty(pctKey)) {

            this.entryType = ExpenseEntryType.PERSONAL_CARD;

        } else if (!TextUtils.isEmpty(smartExpense.getEReceiptId())) {

            this.entryType = ExpenseEntryType.E_RECEIPT;

        } else if (!TextUtils.isEmpty(rcKey)) {

            this.entryType = ExpenseEntryType.RECEIPT_CAPTURE;

        } else {
            // Last resort, just set it to UNKOWN
            this.entryType = ExpenseEntryType.UNKNOWN_EXPENSE;
            Log.e(Const.LOG_TAG, CLS_TAG + ".Expense() - could not determine the expense type!!!");
        }

        // Need to default certain emepty/null fields for certain types.
        if (this.entryType == ExpenseEntryType.RECEIPT_CAPTURE || this.entryType == ExpenseEntryType.E_RECEIPT) {

            if (crnCode == null || crnCode.length() == 0) {
                // MOB-15545 : if currency code is null treat it as a "USD"; to be consistent with other platforms
                this.crnCode = "USD";
            }
            if (transactionAmount == null) {
                // MOB-15545 : if transaction amount is null treat it as a 0.00 to be consistent with other platforms
                transactionAmount = 0.0;
            }
        }

    }

    /**
     * Will construct an instance of <code>MobileEntry</code> based on information in a personal card transaction.
     * 
     * @param persCardTrans
     *            the personal card transaction.
     */
    public MobileEntry(PersonalCard persCard, PersonalCardTransaction persCardTrans) {
        this.crnCode = persCard.crnCode;
        this.expKey = persCardTrans.expKey;
        this.expName = persCardTrans.expName;
        // TODO: How to map location name?
        this.locationName = "";
        this.vendorName = persCardTrans.description;
        this.transactionAmount = persCardTrans.amount;
        this.transactionDate = Format.safeFormatCalendar(FormatUtil.MONTH_DAY_FULL_YEAR_DISPLAY,
                persCardTrans.datePosted);
        this.transactionDateCalendar = persCardTrans.datePosted;
        this.pcaKey = persCard.pcaKey;
        this.pctKey = persCardTrans.pctKey;
        this.entryType = Expense.ExpenseEntryType.PERSONAL_CARD;
        // Initialize the create/update times to the save value (now).
        this.createDate = this.updateDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // MOB-13441 - Init 'status' to normal since this MobileEntry is being created to refer to
        // information about the card transaction.
        this.status = MobileEntryStatus.NORMAL;
    }

    /**
     * Constructs an instance of <code>MobileEntry</code> based on information in a corporate card transaction.
     * 
     * @param corpCardTrans
     */
    public MobileEntry(CorporateCardTransaction corpCardTrans) {
        this.crnCode = corpCardTrans.getTransactionCrnCode();
        this.expKey = corpCardTrans.getExpenseKey();
        this.expName = corpCardTrans.getExpenseName();
        StringBuilder strBldr = new StringBuilder();
        if (corpCardTrans.getMerchantCity() != null) {
            strBldr.append(corpCardTrans.getMerchantCity());
        }
        if (corpCardTrans.getMerchantState() != null && corpCardTrans.getMerchantState().length() > 0) {
            if (strBldr.length() > 0) {
                strBldr.append(',');
            }
            strBldr.append(corpCardTrans.getMerchantState());
        }
        if (corpCardTrans.getMerchantCountryCode() != null && corpCardTrans.getMerchantCountryCode().length() > 0) {
            if (strBldr.length() > 0) {
                strBldr.append(',');
            }
            strBldr.append(corpCardTrans.getMerchantCountryCode());
        }
        this.locationName = strBldr.toString();
        this.vendorName = corpCardTrans.getMerchantName();
        this.transactionAmount = corpCardTrans.getTransactionAmount();
        this.transactionDate = Format.safeFormatCalendar(FormatUtil.MONTH_DAY_FULL_YEAR_DISPLAY,
                corpCardTrans.getTransactionDate());
        this.transactionDateCalendar = corpCardTrans.getTransactionDate();
        this.cctKey = corpCardTrans.getCctKey();
        this.entryType = Expense.ExpenseEntryType.CORPORATE_CARD;
        // Initialize the create/update times to the save value (now).
        this.createDate = this.updateDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // MOB-13441 - Init 'status' to normal since this MobileEntry is being created to refer to
        // information about the card transaction.
        this.status = MobileEntryStatus.NORMAL;
    }

    /**
     * Constructs an instance of <code>MobileEntry</code> based on information in a corporate card transaction.
     * 
     * @param corpCardTrans
     */
    public MobileEntry(ReceiptCapture receiptCaptures) {
        this.crnCode = receiptCaptures.crnCode;
        if (crnCode == null || crnCode.length() == 0) {
            // MOB-15545 : if currency code is null treat it as a "USD"; to be consistent with other platforms
            this.crnCode = "USD";
        }
        this.expKey = receiptCaptures.expKey;
        this.expName = receiptCaptures.expName;
        this.locationName = "";
        this.vendorName = receiptCaptures.vendorName;
        this.transactionAmount = receiptCaptures.transactionAmount;
        if (transactionAmount == null) {
            // MOB-15545 : if transaction amount is null treat it as a 0.00 to be consistent with other platforms
            transactionAmount = 0.0;
        }
        this.transactionDate = Format.safeFormatCalendar(FormatUtil.MONTH_DAY_FULL_YEAR_DISPLAY,
                receiptCaptures.transactionDate);
        this.transactionDateCalendar = receiptCaptures.transactionDate;
        this.rcKey = receiptCaptures.rcKey;
        this.entryType = Expense.ExpenseEntryType.RECEIPT_CAPTURE;
        // Initialize the create/update times to the save value (now).
        this.createDate = this.updateDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.receiptImageId = receiptCaptures.receiptImageId;
        // MOB-13441 - Init 'status' to normal since this MobileEntry is being created to refer to
        // information about the card transaction.
        this.status = MobileEntryStatus.NORMAL;
    }

    /**
     * Create a new mobile entry with data from a eReceipt
     * 
     * @param eReceipt
     * @param isCashTransaction
     *            a flag indicating whether the generated mobile entry is a separate entity with expenseEntryType as CASH.
     */
    public MobileEntry(EReceipt eReceipt, boolean isCashTransaction) {
        this.crnCode = eReceipt.getCrnCode();
        this.comment = eReceipt.getComment();
        if (crnCode == null || crnCode.length() == 0) {
            // MOB-15545 : if currency code is null treat it as a "USD"; to be consistent with other platforms
            this.crnCode = "USD";
        }
        this.expKey = eReceipt.getExpKey();
        this.expName = eReceipt.getExpName();
        this.locationName = eReceipt.getLocationName();
        this.vendorName = eReceipt.getVendorDescription();
        this.transactionAmount = eReceipt.getTransactionAmount();
        if (transactionAmount == null) {
            // MOB-15545 : if transaction amount is null treat it as a 0.00 to be consistent with other platforms
            transactionAmount = 0.0;
        }
        this.transactionDate = Format.safeFormatCalendar(FormatUtil.MONTH_DAY_FULL_YEAR_DISPLAY,
                eReceipt.getTransactionDate());
        this.transactionDateCalendar = eReceipt.getTransactionDate();
        if (isCashTransaction)
            this.entryType = Expense.ExpenseEntryType.CASH;
        else
            this.entryType = Expense.ExpenseEntryType.E_RECEIPT;
        // Initialize the create/update times to the save value (now).
        this.createDate = this.updateDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.receiptImageId = eReceipt.getEReceiptImageId();
        // MOB-13441 - Init 'status' to normal since this MobileEntry is being created to refer to
        // information about the card transaction.
        this.status = MobileEntryStatus.NORMAL;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public MobileEntry clone() {
        MobileEntry clone = null;
        try {
            return (MobileEntry) super.clone();
        } catch (CloneNotSupportedException cloneNotSupExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".clone: ", cloneNotSupExc);
        }
        return clone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getCrnCode()
     */
    public String getCrnCode() {
        return crnCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getExpKey()
     */
    public String getExpKey() {
        return expKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getExpName()
     */
    public String getExpName() {
        return expName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getLocationName()
     */
    public String getLocationName() {
        return locationName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getMeKey()
     */
    public String getMeKey() {
        return meKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getPcaKey()
     */
    public String getPcaKey() {
        return pcaKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getPctKey()
     */
    public String getPctKey() {
        return pctKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setPcaKey(java.lang.String)
     */
    public void setPcaKey(String pcaKey) {
        this.pcaKey = pcaKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setPctKey(java.lang.String)
     */
    public void setPctKey(String pctKey) {
        this.pctKey = pctKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getCctKey()
     */
    public String getCctKey() {
        return cctKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setCctKey(java.lang.String)
     */
    public void setCctKey(String cctKey) {
        this.cctKey = cctKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getTransactionAmount()
     */
    public Double getTransactionAmount() {
        return transactionAmount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getTransactionDate()
     */
    public String getTransactionDate() {
        return transactionDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getTransactionDateCalendar()
     */
    public Calendar getTransactionDateCalendar() {
        return transactionDateCalendar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setTransactionDate(java.lang.String)
     */
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setTransactionDateCalendar(java.util.Calendar)
     */
    public void setTransactionDateCalendar(Calendar transactionDateCalendar) {
        this.transactionDateCalendar = transactionDateCalendar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getFormattedTransactionDate()
     */
    public String getFormattedTransactionDate() {
        return FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(transactionDateCalendar.getTime());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#hasReceiptImage()
     */
    public boolean hasReceiptImage() {
        return hasReceiptImage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getReceiptImageId()
     */
    public String getReceiptImageId() {
        return receiptImageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setReceiptImageId(java.lang.String)
     */
    public void setReceiptImageId(String receiptImageId) {
        this.receiptImageId = receiptImageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getReceiptImageDataLocalFilePath()
     */
    public String getReceiptImageDataLocalFilePath() {
        return receiptImageDataLocalFilePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setReceiptImageDataLocalFilePath(java.lang.String)
     */
    public void setReceiptImageDataLocalFilePath(String receiptImageLocalFilePath) {
        this.receiptImageDataLocalFilePath = receiptImageLocalFilePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setReceiptImageDataLocal(boolean)
     */
    public void setReceiptImageDataLocal(boolean receiptImageDataLocal) {
        this.receiptImageDataLocal = receiptImageDataLocal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#hasReceiptImageDataLocal()
     */
    public boolean hasReceiptImageDataLocal() {
        return receiptImageDataLocal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setCrnCode(java.lang.String)
     */
    public void setCrnCode(String crnCode) {
        this.crnCode = crnCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setExpKey(java.lang.String)
     */
    public void setExpKey(String expKey) {
        this.expKey = expKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setExpName(java.lang.String)
     */
    public void setExpName(String expName) {
        this.expName = expName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setHasReceiptImage(boolean)
     */
    public void setHasReceiptImage(boolean hasReceiptImage) {
        this.hasReceiptImage = hasReceiptImage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setLocationName(java.lang.String)
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getVendorName()
     */
    public String getVendorName() {
        return vendorName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setVendorName(java.lang.String)
     */
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getEntryType()
     */
    public ExpenseEntryType getEntryType() {
        return entryType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setEntryType(com.concur.mobile.data.expense.Expense.ExpenseEntryType)
     */
    public void setEntryType(ExpenseEntryType entryType) {
        this.entryType = entryType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setMeKey(java.lang.String)
     */
    public void setMeKey(String meKey) {
        this.meKey = meKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setTransactionAmount(java.lang.String)
     */
    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getComment()
     */
    public String getComment() {
        return comment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#hasComment()
     */
    public boolean hasComment() {
        return (comment != null && comment.length() > 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setComment(java.lang.String)
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getLocalKey()
     */
    public String getLocalKey() {
        return localKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setLocalKey(java.lang.String)
     */
    public void setLocalKey(String localKey) {
        this.localKey = localKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getLastSaveDate()
     */
    public Calendar getUpdateDate() {
        return updateDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setLastSaveDate(java.util.Calendar)
     */
    public void setUpdateDate(Calendar lastSaveDate) {
        this.updateDate = lastSaveDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getCreateDate()
     */
    public Calendar getCreateDate() {
        return createDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setCreateDate(java.util.Calendar)
     */
    public void setCreateDate(Calendar createDate) {
        this.createDate = createDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#getStatus()
     */
    public MobileEntryStatus getStatus() {
        return status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#setStatus(com.concur.mobile.data.expense.MobileEntryStatus)
     */
    public void setStatus(MobileEntryStatus status) {
        this.status = status;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.expense.MobileEntry#update(com.concur.mobile.data.expense.MobileEntry)
     */
    public void update(MobileEntry entry) {
        this.comment = entry.getComment();
        this.createDate = entry.getCreateDate();
        this.crnCode = entry.getCrnCode();
        // Updating with a mobile entry of type "SMART" is actually the paired cash entry.
        this.entryType = (entry.getEntryType() != Expense.ExpenseEntryType.SMART_CORPORATE && entry.getEntryType() != Expense.ExpenseEntryType.SMART_PERSONAL) ? entry
                .getEntryType() : Expense.ExpenseEntryType.CASH;
        this.expKey = entry.getExpKey();
        this.expName = entry.getExpName();
        this.hasReceiptImage = entry.hasReceiptImage();
        this.receiptImageId = entry.getReceiptImageId();
        this.localKey = entry.getLocalKey();
        this.locationName = entry.getLocationName();
        this.meKey = entry.getMeKey();
        this.pcaKey = entry.getPcaKey();
        this.pctKey = (entry.getEntryType() != Expense.ExpenseEntryType.SMART_PERSONAL) ? entry.getPctKey() : null;
        // CT keys are used with "SMART" expense types and represent the matched card expense.
        // The CT keys value gets set on an instance of MobileEntry, but is not persisted. It will be
        // set again upon editing a "SMART" expense.
        this.cctKey = (entry.getEntryType() != Expense.ExpenseEntryType.SMART_CORPORATE) ? entry.getCctKey() : null;
        this.receiptImageDataLocal = entry.hasReceiptImageDataLocal();
        this.receiptImageDataLocalFilePath = entry.getReceiptImageDataLocalFilePath();
        this.status = entry.getStatus();
        this.transactionAmount = entry.getTransactionAmount();
        this.transactionDate = entry.getTransactionDate();
        this.transactionDateCalendar = entry.getTransactionDateCalendar();
        this.updateDate = entry.getUpdateDate();
        this.vendorName = entry.getVendorName();
    }

    /**
     * Will parse the XML representation of a list of mobile entries.
     * 
     * @param xmlStr
     *            the XML representation.
     * 
     * @return a list of mobile entries.
     */
    public static ArrayList<MobileEntry> parseMobileEntryXml(String mobileEntryXml, Context context) {

        ArrayList<MobileEntry> mobileEntries = null;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            MobileEntrySAXHandler handler = new MobileEntrySAXHandler();
            handler.context = context;
            parser.parse(new ByteArrayInputStream(mobileEntryXml.getBytes()), handler);
            mobileEntries = handler.getMobileEntries();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mobileEntries;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing mobile expense entry information.
     * 
     * @author AndrewK
     */
    static class MobileEntrySAXHandler extends DefaultHandler {

        private static final String CLS_TAG = MobileEntry.CLS_TAG + "." + MobileEntrySAXHandler.class.getSimpleName();

        private static final String MOBILE_ENTRY_LIST = "ArrayOfMobileEntry";

        public static final String MOBILE_ENTRY = "MobileEntry";

        private static final String CRN_CODE = "CrnCode";

        private static final String EXP_KEY = "ExpKey";

        private static final String EXP_NAME = "ExpName";

        private static final String HAS_RECEIPT_IMAGE = "HasReceiptImage";

        private static final String RECEIPT_IMAGE_ID = "ReceiptImageId";

        private static final String RECEIPT_IMAGE = "ReceiptImage";

        private static final String LOCATION_NAME = "LocationName";

        private static final String ME_KEY = "MeKey";

        private static final String TRANSACTION_AMOUNT = "TransactionAmount";

        private static final String TRANSACTION_DATE = "TransactionDate";

        private static final String COMMENT = "Comment";

        private static final String VENDOR_NAME = "VendorName";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>MobileEntry</code> objects that have been parsed.
         */
        private ArrayList<MobileEntry> mobileEntries = new ArrayList<MobileEntry>();

        /**
         * Contains a reference to the report comment currently being built.
         */
        MobileEntry mobileEntry;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * A reference to a context object used to construct a local file.
         */
        Context context;

        /**
         * Gets the list of <code>MobileEntry</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>MobileEntry</code> objects.
         */
        ArrayList<MobileEntry> getMobileEntries() {
            return mobileEntries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(MOBILE_ENTRY_LIST)) {
                mobileEntries = new ArrayList<MobileEntry>();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(MOBILE_ENTRY)) {
                mobileEntry = new MobileEntry();
                chars.setLength(0);
                elementHandled = true;
            }
        }

        void handleElement(String localName, String cleanChars) {

            if (mobileEntry != null) {
                if (localName.equalsIgnoreCase(CRN_CODE)) {
                    mobileEntry.setCrnCode(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXP_KEY)) {
                    mobileEntry.setExpKey(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXP_NAME)) {
                    mobileEntry.setExpName(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RECEIPT_IMAGE_ID)) {
                    mobileEntry.setReceiptImageId(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_RECEIPT_IMAGE)) {
                    Boolean result = Parse.safeParseBoolean(cleanChars);
                    if (result != null) {
                        mobileEntry.setHasReceiptImage(result.booleanValue());
                        elementHandled = true;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unable to parse '" + HAS_RECEIPT_IMAGE
                                + "' field.");
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(RECEIPT_IMAGE)) {
                    try {
                        // Write out the base64 encoded image to a file based on the current date.
                        mobileEntry.setReceiptImageDataLocalFilePath(writeDecodeBase64(cleanChars));
                        mobileEntry.setReceiptImageDataLocal(true);
                    } catch (IOException ioExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: error decoding base64 receipt image -- ", ioExc);
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(LOCATION_NAME)) {
                    mobileEntry.setLocationName(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ME_KEY)) {
                    mobileEntry.setMeKey(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TRANSACTION_AMOUNT)) {
                    mobileEntry.setTransactionAmount(Parse.safeParseDouble(cleanChars));
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(TRANSACTION_DATE)) {
                    mobileEntry.setTransactionDate(cleanChars);
                    mobileEntry.setTransactionDateCalendar(Parse.parseXMLTimestamp(mobileEntry.getTransactionDate()));
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(COMMENT)) {
                    mobileEntry.setComment(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VENDOR_NAME)) {
                    mobileEntry.setVendorName(cleanChars);
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(MOBILE_ENTRY)) {
                    // Anything parsed in at this point is a NORMAL entry. It exists on the server.
                    mobileEntry.setStatus(MobileEntryStatus.NORMAL);
                    mobileEntries.add(mobileEntry);
                    mobileEntry = null;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(MOBILE_ENTRY_LIST)) {
                    // Finished parsing.
                    elementHandled = true;
                } else {
                    // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled element '" +
                    // localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current mobile entry - localName: " + localName
                        + ", chars: " + chars.toString() + ".");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            elementHandled = false;

            super.endElement(uri, localName, qName);

            String cleanChars = chars.toString().trim();
            handleElement(localName, cleanChars);

            // Clear out the stored element values.
            chars.setLength(0);
        }

        /**
         * Will decode and write out to disk base64 decoded receipt image data.
         * 
         * @param base64Text
         *            the base64 text to decode and write out.
         * 
         * @return the file path of the written content.
         * 
         * @throws IOException
         *             if an error occurs while writing out the file.
         */
        private String writeDecodeBase64(String base64Text) throws IOException {
            String receiptImageFilePath = null;
            File receiptImageDir = context.getDir(Const.RECEIPT_DIRECTORY, Context.MODE_PRIVATE);
            Calendar cal = Calendar.getInstance();
            String fileName = Format.safeFormatCalendar(FormatUtil.LONG_YEAR_MONTH_DAY_24HOUR_TIME_MINUTE_SECOND, cal)
                    + ".png";
            File receiptFile = new File(receiptImageDir, fileName);
            receiptImageFilePath = receiptFile.getAbsolutePath();
            Base64.decodeToFile(base64Text, receiptImageFilePath);
            // TODO: look at first 4 bytes to determine the type of image, then change the file name extension.
            return receiptImageFilePath;
        }

    }

}
