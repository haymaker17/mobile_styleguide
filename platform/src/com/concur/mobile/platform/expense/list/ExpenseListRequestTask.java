package com.concur.mobile.platform.expense.list;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;

/**
 * An extension of <code>PlatformAsyncRequestTask</code> for the purpose of requesting an expense list.
 * 
 * @author andrewk
 */
public class ExpenseListRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "ExpenseListRequestTask";

    // Contains the service end-point for the <code>GetAllExpenses</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/Expense/GetAllExpenses";

    ExpenseList expenseList;

    /**
     * Constructs an instance of <code>ExpenseListRequestTask</code>.
     * 
     * @param context
     *            contains an application context.
     * @param requestId
     *            contains the request id.
     * @param receiver
     *            contains a result receiver.
     */
    public ExpenseListRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    @Override
    public int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {
                // Construct the expense list parser and register the parser.
                expenseList = new ExpenseList(parser, ExpenseList.TAG_ALL_EXPENSES);
                parser.registerParser(expenseList, ExpenseList.TAG_ALL_EXPENSES);
                // Parse.
                parser.parse();
            } else {
                result = BaseAsyncRequestTask.RESULT_ERROR;
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to construct common parser!");
            }
        } catch (XmlPullParserException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        } catch (IOException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
                }
            }
        }
        return result;
    }

    @Override
    public int onPostParse() {

        int result = super.onPostParse();

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        String userId = sessInfo.getUserId();

        if (expenseList != null) {

            // Reconcile corporate card transactions.
            if (expenseList.corporateCardTransactions != null) {
                CorporateCardTransaction.reconcile(getContext(), userId, expenseList.corporateCardTransactions);
                for (CorporateCardTransaction corpCardTrans : expenseList.corporateCardTransactions) {
                    corpCardTrans.update(getContext(), userId);
                }
            }

            // Reconcile receipt captures.
            if (expenseList.receiptCaptures != null) {
                ReceiptCapture.reconcile(getContext(), userId, expenseList.receiptCaptures);
                for (ReceiptCapture recCap : expenseList.receiptCaptures) {
                    recCap.update(getContext(), userId);
                }
            }

            // Reconcile personal card transactions
            if (expenseList.personalCards != null) {
                for (PersonalCard persCard : expenseList.personalCards) {
                    PersonalCardTransaction.reconcile(getContext(), userId, persCard.transactions);
                    for (PersonalCardTransaction persCardTrans : persCard.transactions) {
                        persCardTrans.update(getContext(), userId);
                    }
                }
            }

            // Reconcile personal cards.
            if (expenseList.personalCards != null) {
                PersonalCard.reconcile(getContext(), userId, expenseList.personalCards);
                for (PersonalCard persCard : expenseList.personalCards) {
                    persCard.update(getContext(), userId);
                }
            }

            // Reconcile mobile entries
            // NOTE: The list of mobile entries to be reconciled has to come from the entries list and
            // any mobile entries referenced from CCT/PCT's.
            if (expenseList.entries != null) {
                // Add in any mobile entries referenced by 'expenseList.entries'.
                List<MobileEntry> allMobEntries = new ArrayList<MobileEntry>(expenseList.entries.size());
                allMobEntries.addAll(expenseList.entries);
                // Add in any mobile entries referenced by corporate card transactions.
                if (expenseList.corporateCardTransactions != null) {
                    for (CorporateCardTransaction ccTrans : expenseList.corporateCardTransactions) {
                        if (ccTrans.mobileEntry != null) {
                            allMobEntries.add(ccTrans.mobileEntry);
                        }
                    }
                }
                // Add in any mobile entries referenced by personal card transactions.
                if (expenseList.personalCards != null) {
                    for (PersonalCard pc : expenseList.personalCards) {
                        if (pc.transactions != null) {
                            for (PersonalCardTransaction pcTrans : pc.transactions) {
                                if (pcTrans.mobileEntry != null) {
                                    allMobEntries.add(pcTrans.mobileEntry);
                                }
                            }
                        }
                    }

                }
                MobileEntry.reconcile(getContext(), userId, allMobEntries);
                for (MobileEntry mobEnt : expenseList.entries) {
                    mobEnt.update(getContext(), userId);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: expense list was not parsed.");
            result = BaseAsyncRequestTask.RESULT_ERROR;
        }
        return result;
    }

}
