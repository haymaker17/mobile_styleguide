package com.concur.mobile.core.expense.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.concur.core.R;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.fragment.ExpenseDetailFragment;
import com.concur.mobile.core.expense.fragment.ExpenseDetailFragment.ExpenseDetailCallbacks;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.list.CorporateCardTransaction;
import com.concur.mobile.platform.expense.list.MobileEntry;
import com.concur.mobile.platform.expense.list.PersonalCardTransaction;
import com.concur.mobile.platform.expense.list.SaveMobileEntryRequestTask;
import com.concur.mobile.platform.expense.list.dao.CorporateCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.MobileEntryDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;
import com.concur.mobile.platform.util.ContentUtils;

public class ExpenseDetail extends BaseActivity implements ExpenseDetailCallbacks {

    protected static final String FRAGMENT_EXPENSE_DETAIL = "FRAGMENT_EXPENSE_DETAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isServiceAvailable()) {
            buildView();
        } else {
            buildViewDelay = true;
        }

    }

    /**
     * Initializes the view.
     */
    public void buildView() {

        // Set the content view.
        setContentView(R.layout.expense_detail);

        // Begin - convert to DAO object
        Bundle extra = this.getIntent().getExtras();
        // Obtain the expense type.
        String meKey = extra.getString(Const.EXTRA_EXPENSE_MOBILE_ENTRY_KEY);
        String pcaKey = extra.getString(Const.EXTRA_EXPENSE_MOBILE_ENTRY_PERSONAL_CARD_ACCOUNT_KEY);
        String pctKey = extra.getString(Const.EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY);
        String cctKey = extra.getString(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY);

        ConcurCore ConcurCore = (ConcurCore) getApplication();

        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();

        if (meKey != null) {
            com.concur.mobile.core.expense.charge.data.MobileEntry me = expEntCache.findCashExpenseEntry(meKey)
                    .getCashTransaction();

            ExpenseDAOAdapter.convertMobileEntry(this, me);
        } else if (pcaKey != null && pctKey != null) {
            Expense expenseEntry = expEntCache.findPersonalCardExpenseEntry(pcaKey, pctKey);
            ExpenseDAOAdapter.convertPCT(this, expenseEntry.getPersonalCard(),
                    expenseEntry.getPersonalCardTransaction());
        } else if (cctKey != null) {
            com.concur.mobile.core.expense.charge.data.CorporateCardTransaction cct = expEntCache
                    .findCorporateCardExpenseEntry(cctKey).getCorporateCardTransaction();

            ExpenseDAOAdapter.convertCCT(this, cct);
        }

        // End - convert to DAO object

        FragmentManager fm = getSupportFragmentManager();

        PlatformFragment expDetailFragment = (PlatformFragment) fm.findFragmentByTag(FRAGMENT_EXPENSE_DETAIL);
        if (expDetailFragment == null) {
            expDetailFragment = new ExpenseDetailFragment();

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, expDetailFragment, FRAGMENT_EXPENSE_DETAIL);
            ft.commit();
        }

        configureViewHeader();
    }

    /**
     * Configures the view header.
     */
    protected void configureViewHeader() {
        String title = getText(R.string.quick_expense_title).toString();
        getSupportActionBar().setTitle(title);
    }

    /**
     * Invoked when the MobileEntrySave request has succeeded.
     * 
     * @param resultData
     *            the data containing results from the successful request.
     */
    public void onMobileEntrySaveRequestSuccess(Bundle resultData) {
        // Set the flag that the expense entry cache should be refetched.
        ConcurCore ConcurCore = getConcurCore();
        // Set the refresh list flag on the expense entry.
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        expEntCache.setShouldFetchExpenseList();

        // Flurry Notification.
        Map<String, String> params = new HashMap<String, String>();
        String meKey = resultData.getString(SaveMobileEntryRequestTask.MOBILE_ENTRY_ME_KEY);
        boolean clearImage = resultData.getBoolean(SaveMobileEntryRequestTask.MOBILE_ENTRY_CLEAR_IMAGE_KEY);
        String uriStr = resultData.getString(SaveMobileEntryRequestTask.MOBILE_ENTRY_URI_KEY);
        Uri uri = Uri.parse(uriStr);
        MobileEntry mobileEntry = new MobileEntry(this, uri);
        boolean hasReceipt = (!clearImage && mobileEntry.getReceiptImageId() != null);
        params.put(Flurry.PARAM_NAME_EDIT_NEW, ((meKey != null) ? Flurry.PARAM_VALUE_NEW : Flurry.PARAM_VALUE_EDIT));

        params.put(Flurry.PARAM_NAME_CONTAINS_RECEIPT, ((hasReceipt) ? Flurry.PARAM_VALUE_YES : Flurry.PARAM_VALUE_NO));
        EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_SAVED, params);
        // TODO activity.updateMRUs(activity.saveExpenseRequest);
        setResult(Activity.RESULT_OK);
        finish();

    }

    /**
     * Invoked when the MobileEntrySave request has failed.
     * 
     * @param resultData
     *            the data containing results from the failure.
     */
    public void onMobileEntrySaveRequestFail(Bundle resultData) {

    }

    /**
     * Invoked to log/track a failure during the EmailLookup request.
     * 
     * @param failureType
     *            string indicating the reason for failing the Email Lookup. For example: <code>FAILURE_REASON_FORMAT</code> or
     *            <code>FAILURE_REASON_OFFLINE</code>.
     */
    public void trackMobileEntrySaveFailure(String failureType) {

    }

    private static class ExpenseDAOAdapter {

        private static void copyMobileEntry(MobileEntryDAO dao,
                com.concur.mobile.core.expense.charge.data.MobileEntry me) {
            dao.setTransactionDate(me.getTransactionDateCalendar());
            dao.setCrnCode(me.getCrnCode());
            dao.setComment(me.getComment());
            dao.setLocationName(me.getLocationName());
            dao.setVendorName(me.getVendorName());
            dao.setTransactionAmount(me.getTransactionAmount());
            dao.setExpKey(me.getExpKey());
            dao.setExpName(me.getExpName());
            dao.setMeKey(me.getMeKey());
            dao.setReceiptImageId(me.getReceiptImageId());
        }

        public static MobileEntryDAO convertMobileEntry(Context context,
                com.concur.mobile.core.expense.charge.data.MobileEntry me) {
            MobileEntryDAO result = null;
            Uri expenseUri = ContentUtils.getContentUri(context,
                    com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI,
                    com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.MOBILE_ENTRY_KEY,
                    me.getMeKey());
            try {
                result = new MobileEntry(context, expenseUri);
            } catch (Exception e) {
            } finally {
                if (result == null) {
                    result = new MobileEntry();
                }
                // Create a new object in content provider
                copyMobileEntry(result, me);
                // Save the mobile entry DAO
                SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
                result.update(context, sessInfo.getUserId());
            }

            return result;
        }

        public static PersonalCardTransactionDAO convertPCT(Context context,
                com.concur.mobile.core.expense.charge.data.PersonalCard pca,
                com.concur.mobile.core.expense.charge.data.PersonalCardTransaction pct) {
            PersonalCardTransactionDAO result = null;
            Uri expenseUri = ContentUtils.getContentUri(context,
                    com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                    com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.PCT_KEY,
                    pct.pctKey);
            try {
                result = new PersonalCardTransaction(context, expenseUri);
            } catch (Exception e) {
            } finally {
                if (result == null) {
                    // Create a new object in content provider
                    result = new PersonalCardTransaction(new CommonParser(null), "");
                    result.setPctKey(pct.pctKey);
                }
            }
            result.setDatePosted(pct.datePosted);
            result.setCrnCode(pca.crnCode);
            result.setCategory(pct.category);
            result.setDescription(pct.description);
            result.setAmount(pct.amount);
            result.setExpKey(pct.expKey);
            result.setSmartExpenseMeKey(pct.smartExpenseMeKey);
            result.setExpName(pct.expName);

            if (pct.mobileEntry != null) {
                MobileEntry dao = (MobileEntry) result.getMobileEntryDAO();
                if (dao == null) {
                    dao = new MobileEntry();
                }
                copyMobileEntry(dao, pct.mobileEntry);
                result.setMobileEntry(dao);
            }
            // Save the mobile entry DAO
            SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
            result.update(context, sessInfo.getUserId());
            return result;
        }

        public static CorporateCardTransactionDAO convertCCT(Context context,
                com.concur.mobile.core.expense.charge.data.CorporateCardTransaction cct) {
            CorporateCardTransactionDAO result = null;
            Uri expenseUri = ContentUtils.getContentUri(context,
                    com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CONTENT_URI,
                    com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CCT_KEY,
                    cct.getCctKey());
            try {
                result = new CorporateCardTransaction(context, expenseUri);
            } catch (Exception e) {
            } finally {
                if (result == null) {
                    result = new CorporateCardTransaction(new CommonParser(null), "");
                    result.setCctKey(cct.getCctKey());
                }
            }

            // Create a new object in content provider
            result.setTransactionDate(cct.getTransactionDate());
            result.setTransactionCrnCode(cct.getTransactionCrnCode());
            result.setMerchantCity(cct.getMerchantCity());
            result.setMerchantState(cct.getMerchantState());
            result.setMerchantCountryCode(cct.getMerchantCountryCode());
            result.setMerchantName(cct.getMerchantName());
            result.setTransactionAmount(cct.getTransactionAmount());
            result.setExpenseKey(cct.getExpenseKey());
            result.setCardTypeCode(cct.getCardTypeCode());
            result.setCardTypeName(cct.getCardTypeName());
            result.setDescription(cct.getDescription());
            result.setDoingBusinessAs(cct.getDoingBusinessAs());
            result.setSmartExpenseMeKey(cct.getSmartExpenseMeKey());
            result.setExpenseName(cct.getExpenseName());
            if (cct.getMobileEntry() != null) {
                MobileEntry dao = (MobileEntry) result.getMobileEntryDAO();
                if (dao == null) {
                    dao = new MobileEntry();
                }
                copyMobileEntry(dao, cct.getMobileEntry());

                result.setMobileEntry(dao);
            }
            // Save the mobile entry DAO
            SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
            result.update(context, sessInfo.getUserId());

            return result;
        }
    }
}
