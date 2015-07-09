/**
 * 
 */
package com.concur.mobile.platform.expense.list.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;
import com.concur.mobile.platform.expense.list.dao.CorporateCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.ExpenseDAO;
import com.concur.mobile.platform.expense.list.dao.ExpenseListDAO;
import com.concur.mobile.platform.expense.list.dao.MobileEntryDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.ReceiptCaptureDAO;
import com.concur.mobile.platform.test.Const;

/**
 * Provides a class to verify an <code>ExpenseListResponse</code> object against data stored in the content provider.
 * 
 * @author andrewk
 */
public class VerifyExpenseListResponse {

    private static final String CLS_TAG = "VerifyExpenseListResponse";

    /**
     * Will verify session information in <code>loginResult</code> against session information stored in the config content
     * provider optionally ignoring session id.
     * 
     * @param context
     *            contains an application context.
     * @param expListResp
     *            contains the expense list response.
     */
    public void verify(Context context, ExpenseListResponse expListResp) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        Assert.assertNotNull(MTAG + ": session info is null", sessInfo);

        ExpenseListDAO expListDAO = new ExpenseListDAO(context, sessInfo.getUserId());

        // Verify count of corporate card charges.
        List<CorporateCardTransactionDAO> corpCardTransDAOS = expListDAO.getCorporateCardTransactions();
        Long cctDaosCount = (corpCardTransDAOS != null) ? corpCardTransDAOS.size() : 0L;
        Long ccTransCount = (expListResp.corporateCardTransactions != null) ? expListResp.corporateCardTransactions
                .size() : 0L;
        Assert.assertEquals(MTAG + ": corporate transaction count", ccTransCount, cctDaosCount);

        // Verify parsed corporate card charge information against content provider information.
        if (corpCardTransDAOS != null) {
            // First build look-up map.
            Map<String, CorporateCardTransactionDAO> corpCardTransDAOMap = new HashMap<String, CorporateCardTransactionDAO>(
                    corpCardTransDAOS.size());
            for (CorporateCardTransactionDAO corpCardTransDAO : corpCardTransDAOS) {
                corpCardTransDAOMap.put(corpCardTransDAO.getCctKey(), corpCardTransDAO);
            }
            for (CorporateCardTransaction cctTrans : expListResp.corporateCardTransactions) {
                // Locate cctTrans in DAO map.
                CorporateCardTransactionDAO cctDAO = corpCardTransDAOMap.get(cctTrans.cctKey);
                Assert.assertNotNull(MTAG + ": unable to locate parsed corp card trans in DAO map", cctDAO);
                verifyCorporateCardTransaction(cctDAO, cctTrans, expListResp);
            }
        }

        // Verify count of personal cards.
        List<PersonalCardDAO> persCardDAOS = expListDAO.getPersonalCards();
        Long pcDaosCount = (persCardDAOS != null) ? persCardDAOS.size() : 0L;
        Long pcCount = (expListResp.personalCards != null) ? expListResp.personalCards.size() : 0L;
        Assert.assertEquals(MTAG + ": personal card count", pcCount, pcDaosCount);

        // Verify parsed personal card information against content provider information.
        if (persCardDAOS != null) {
            // First build look-up map.
            Map<String, PersonalCardDAO> persCardDAOMap = new HashMap<String, PersonalCardDAO>(persCardDAOS.size());
            for (PersonalCardDAO persCardDAO : persCardDAOS) {
                persCardDAOMap.put(persCardDAO.getPCAKey(), persCardDAO);
            }
            for (PersonalCard persCard : expListResp.personalCards) {
                // Locate persCard in DAO map.
                PersonalCardDAO persCardDAO = persCardDAOMap.get(persCard.pcaKey);
                Assert.assertNotNull(MTAG + ": unable to locate parsed personal card in DAO map", persCardDAO);
                verifyPersonalCard(persCardDAO, persCard);

                // Verify personal card transaction counts...content of each personal card transaction
                // will be verified below.
                List<PersonalCardTransactionDAO> persCardTransDAOList = persCardDAO.getPersonalCardTransactionDAOS();
                Long persCardTransDAOSListCount = (persCardTransDAOList != null) ? persCardTransDAOList.size() : 0L;
                Long persCardTransListCount = (persCard.transactions != null) ? persCard.transactions.size() : 0L;
                Assert.assertEquals(MTAG + ": personal card translist list size count", persCardTransListCount,
                        persCardTransDAOSListCount);
            }
        }

        // Verify count of personal card transaction.
        List<PersonalCardTransactionDAO> pctDAOS = expListDAO.getPersonalCardTransactions();
        List<PersonalCardTransaction> pcTransList = new ArrayList<PersonalCardTransaction>();
        if (expListResp.personalCards != null) {
            for (PersonalCard persCard : expListResp.personalCards) {
                if (persCard.transactions != null) {
                    pcTransList.addAll(persCard.transactions);
                }
            }
        }
        Long pctDaosCount = (pctDAOS != null) ? pctDAOS.size() : 0L;
        Long pcTransCount = (pcTransList != null) ? pcTransList.size() : 0L;
        Assert.assertEquals(MTAG + ": personal card transaction count", pcTransCount, pctDaosCount);

        // Verify parsed personal card charge information against content provider information.
        if (pctDAOS != null) {
            // First build look-up map.
            Map<String, PersonalCardTransactionDAO> persCardTransDAOMap = new HashMap<String, PersonalCardTransactionDAO>(
                    pctDAOS.size());
            for (PersonalCardTransactionDAO persCardTransDAO : pctDAOS) {
                persCardTransDAOMap.put(persCardTransDAO.getPctKey(), persCardTransDAO);
            }
            for (PersonalCardTransaction pctTrans : pcTransList) {
                // Locate pctTrans in DAO map.
                PersonalCardTransactionDAO pctDAO = persCardTransDAOMap.get(pctTrans.pctKey);
                Assert.assertNotNull(MTAG + ": unable to locate parsed pers card trans in DAO map", pctDAO);
                verifyPersonalCardTransaction(pctDAO, pctTrans, expListResp);
            }
        }

        // Verify count of mobile entries.
        List<MobileEntryDAO> mobEntDAOS = expListDAO.getCashTransactions();
        Long mobEntDaosCount = (mobEntDAOS != null) ? mobEntDAOS.size() : 0L;
        Long mobEntCount = (expListResp.mobileEntries != null) ? expListResp.mobileEntries.size() : 0L;
        Assert.assertEquals(MTAG + ": mobile entry count", mobEntCount, mobEntDaosCount);

        // Verify mobile entry information against content provider information.
        if (mobEntDAOS != null) {
            // First build look-up map.
            Map<String, MobileEntryDAO> mobEntDAOMap = new HashMap<String, MobileEntryDAO>(mobEntDAOS.size());
            for (MobileEntryDAO mobEntDAO : mobEntDAOS) {
                mobEntDAOMap.put(mobEntDAO.getMeKey(), mobEntDAO);
            }
            for (MobileEntry mobEnt : expListResp.mobileEntries) {
                // Locate mobEnt in DAO map.
                MobileEntryDAO mobEntDAO = mobEntDAOMap.get(mobEnt.meKey);
                Assert.assertNotNull(MTAG + ": unable to locate parsed mobile entry in DAO map", mobEntDAO);
                verifyMobileEntry(mobEntDAO, mobEnt);
            }
        }

        // Verify count of receipt captures.
        List<ReceiptCaptureDAO> recCapDAOS = expListDAO.getReceiptCaptures();
        Long recCapDaosCount = (recCapDAOS != null) ? recCapDAOS.size() : 0L;
        Long recCapCount = (expListResp.receiptCaptures != null) ? expListResp.receiptCaptures.size() : 0L;
        Assert.assertEquals(MTAG + ": receipt capture count", recCapCount, recCapDaosCount);

        // Verify parsed receipt capture information against content provider information.
        if (recCapDAOS != null) {
            // First build look-up map.
            Map<String, ReceiptCaptureDAO> recCapDAOMap = new HashMap<String, ReceiptCaptureDAO>(recCapDAOS.size());
            for (ReceiptCaptureDAO recCapDAO : recCapDAOS) {
                recCapDAOMap.put(recCapDAO.getRCKey(), recCapDAO);
            }
            for (ReceiptCapture recCap : expListResp.receiptCaptures) {
                // Locate recCap in DAO map.
                ReceiptCaptureDAO recCapDAO = recCapDAOMap.get(recCap.rcKey);
                Assert.assertNotNull(MTAG + ": unable to locate parsed receipt capture in DAO map", recCapDAO);
                verifyReceiptCapture(recCapDAO, recCap);
            }
        }

        // Log expense count.
        List<ExpenseDAO> expDAOS = expListDAO.getExpenses();
        Long expDaosCount = (expDAOS != null) ? expDAOS.size() : 0L;
        Log.d(Const.LOG_TAG, MTAG + ": expense count -> " + expDaosCount);
        if (expDaosCount > 0L) {
            // Attempt to locate a smart corporate/personal charge and perform a split, then
            // verify new expense count.
            boolean splitExpense = false;
            for (ExpenseDAO expDAO : expDAOS) {
                if (expDAO.getType() == ExpenseTypeEnum.SMART_CORPORATE
                        || expDAO.getType() == ExpenseTypeEnum.SMART_PERSONAL) {
                    expDAO.split();
                    splitExpense = true;
                    break;
                }
            }
            if (splitExpense) {
                ExpenseListDAO postSplitExpenseListDAO = new ExpenseListDAO(context, sessInfo.getUserId());
                List<ExpenseDAO> postSplitExpList = postSplitExpenseListDAO.getExpenses();
                long postSplitExpListCount = (postSplitExpList != null) ? postSplitExpList.size() : 0;
                Log.d(Const.LOG_TAG, MTAG + ": post-split expense count -> " + postSplitExpListCount);
                Assert.assertEquals(MTAG + ": post split expense count", expDaosCount + 1, postSplitExpListCount);
            }
        }
    }

    /**
     * Verifies a corporate card transaction DAO object against a parsed corporate card transaction object.
     * 
     * @param corpCardTransDAO
     *            contains the corporate card transaction DAO object.
     * @param cctTrans
     *            contains the parsed corporate card transaction object.
     * @param expListResp
     *            contains the expense list response.
     */
    private void verifyCorporateCardTransaction(CorporateCardTransactionDAO corpCardTransDAO,
            CorporateCardTransaction cctTrans, ExpenseListResponse expListResp) {

        final String MTAG = CLS_TAG + ".verifyCorporateCardTransaction";

        // Type.
        Assert.assertEquals(MTAG + ": type", corpCardTransDAO.getType(), cctTrans.type);

        // Card type code.
        Assert.assertEquals(MTAG + ": card type code", corpCardTransDAO.getCardTypeCode(), cctTrans.cardTypeCode);

        // Card type name.
        Assert.assertEquals(MTAG + ": card type name", corpCardTransDAO.getCardTypeName(), cctTrans.cardTypeName);

        // Credit card transaction key.
        Assert.assertEquals(MTAG + ": cct key", corpCardTransDAO.getCctKey(), cctTrans.cctKey);

        // Credit card transaction type.
        Assert.assertEquals(MTAG + ": cct type", corpCardTransDAO.getCctType(), cctTrans.cctType);

        // Description
        Assert.assertEquals(MTAG + ": description", corpCardTransDAO.getDescription(), cctTrans.description);

        // HasRichData
        Assert.assertEquals(MTAG + ": has rich data", corpCardTransDAO.getHasRichData(), cctTrans.hasRichData);

        // Doing business as.
        Assert.assertEquals(MTAG + ": doing business as", corpCardTransDAO.getDoingBusinessAs(),
                cctTrans.doingBusinessAs);

        // Exp key.
        Assert.assertEquals(MTAG + ": exp key", corpCardTransDAO.getExpenseKey(), cctTrans.expenseKey);

        // Exp name.
        Assert.assertEquals(MTAG + ": exp name", corpCardTransDAO.getExpenseName(), cctTrans.expenseName);

        // Merchant city.
        Assert.assertEquals(MTAG + ": merchant city", corpCardTransDAO.getMerchantCity(), cctTrans.merchantCity);

        // Merchant country code.
        Assert.assertEquals(MTAG + ": merchant country code", corpCardTransDAO.getMerchantCountryCode(),
                cctTrans.merchantCountryCode);

        // Merchant name.
        Assert.assertEquals(MTAG + ": merchant name", corpCardTransDAO.getMerchantName(), cctTrans.merchantName);

        // Merchant state.
        Assert.assertEquals(MTAG + ": merchant state", corpCardTransDAO.getMerchantState(), cctTrans.merchantState);

        // Merchant state.
        Assert.assertEquals(MTAG + ": smart expense", corpCardTransDAO.getSmartExpenseMeKey(),
                cctTrans.smartExpenseMeKey);

        // Transaction amount.
        Assert.assertEquals(MTAG + ": transaction amount", corpCardTransDAO.getTransactionAmount(),
                cctTrans.transactionAmount);

        // Transaction amount.
        Assert.assertEquals(MTAG + ": transaction crn code", corpCardTransDAO.getTransactionCrnCode(),
                cctTrans.transactionCrnCode);

        // Transaction date.
        Assert.assertEquals(MTAG + ": transaction date", corpCardTransDAO.getTransactionDate(),
                cctTrans.transactionDate);

        // Mobile Entry
        MobileEntryDAO mobEntDAO = corpCardTransDAO.getMobileEntryDAO();
        if (mobEntDAO != null && cctTrans.mobileEntry != null) {
            verifyMobileEntry(mobEntDAO, cctTrans.mobileEntry);
        } else if (mobEntDAO != null) {
            Assert.assertTrue(MTAG + ": parsed mobile entry is null", false);
        } else if (cctTrans.mobileEntry != null) {
            Assert.assertTrue(MTAG + ": mobile entry DAO is null", false);
        }

        // Smart matched.
        if (corpCardTransDAO.getType() == ExpenseTypeEnum.SMART_CORPORATE) {
            // Smart matched mobile entry comparison.
            MobileEntryDAO smartMobEntDAO = corpCardTransDAO.getSmartMatchedMobileEntryDAO();
            MobileEntry smartMobEnt = null;
            if (!TextUtils.isEmpty(cctTrans.smartExpenseMeKey)) {
                if (expListResp.mobileEntries != null) {
                    for (MobileEntry mobEnt : expListResp.mobileEntries) {
                        if (!TextUtils.isEmpty(mobEnt.meKey)
                                && mobEnt.meKey.equalsIgnoreCase(cctTrans.smartExpenseMeKey)) {
                            smartMobEnt = mobEnt;
                            break;
                        }
                    }
                }
            }
            if (smartMobEntDAO != null && smartMobEnt != null) {
                verifyMobileEntry(smartMobEntDAO, smartMobEnt);
            } else if (smartMobEntDAO != null) {
                Assert.assertTrue(MTAG + ": parsed smart corporate mobile entry is null", false);
            } else if (smartMobEnt != null) {
                Assert.assertTrue(MTAG + ": smart corporate mobile entry DAO is null", false);
            }
        }
    }

    /**
     * Verifies a personal card DAO object against a parsed personal card object.
     * 
     * @param persCardDAO
     *            contains the personal card DAO object.
     * @param pcCard
     *            contains the parsed personal card object.
     */
    private void verifyPersonalCard(PersonalCardDAO persCardDAO, PersonalCard pcCard) {
        final String MTAG = CLS_TAG + ".verifyPersonalCard";

        // Pca key.
        Assert.assertEquals(MTAG + ": pca key", persCardDAO.getPCAKey(), pcCard.pcaKey);

        // Card name.
        Assert.assertEquals(MTAG + ": card name", persCardDAO.getCardName(), pcCard.cardName);

        // Account number last four.
        Assert.assertEquals(MTAG + ": account number last four", persCardDAO.getAcctNumLastFour(),
                pcCard.acctNumLastFour);

        // Assert currency code.
        Assert.assertEquals(MTAG + ": currency code", persCardDAO.getCrnCode(), pcCard.crnCode);
    }

    /**
     * Verifies a personal card transaction DAO object against a parsed personal card transaction object.
     * 
     * @param pctTransDAO
     *            contains the personal card transaction DAO object.
     * @param pctTrans
     *            contains the parsed personal card transaction object.
     */
    private void verifyPersonalCardTransaction(PersonalCardTransactionDAO pctTransDAO,
            PersonalCardTransaction pctTrans, ExpenseListResponse expListResp) {

        final String MTAG = CLS_TAG + ".verifyPersonalCardTransaction";

        // Type.
        Assert.assertEquals(MTAG + ": type", pctTransDAO.getType(), pctTrans.type);

        // Pct key.
        Assert.assertEquals(MTAG + ": pct key", pctTransDAO.getPctKey(), pctTrans.pctKey);

        // Date posted.
        Assert.assertEquals(MTAG + ": date posted", pctTransDAO.getDatePosted(), pctTrans.datePosted);

        // Description.
        Assert.assertEquals(MTAG + ": description", pctTransDAO.getDescription(), pctTrans.description);

        // Amount.
        Assert.assertEquals(MTAG + ": amount", pctTransDAO.getAmount(), pctTrans.amount);

        // Currency code.
        Assert.assertEquals(MTAG + ": crn code", pctTransDAO.getCrnCode(), pctTrans.crnCode);

        // Status
        Assert.assertEquals(MTAG + ": status", pctTransDAO.getStatus(), pctTrans.status);

        // Category
        Assert.assertEquals(MTAG + ": category", pctTransDAO.getCategory(), pctTrans.category);

        // Exp key.
        Assert.assertEquals(MTAG + ": exp key", pctTransDAO.getExpKey(), pctTrans.expKey);

        // Exp name.
        Assert.assertEquals(MTAG + ": exp name", pctTransDAO.getExpName(), pctTrans.expName);

        // Rpt key.
        Assert.assertEquals(MTAG + ": rpt key", pctTransDAO.getRptKey(), pctTrans.rptKey);

        // Rpt name.
        Assert.assertEquals(MTAG + ": rpt name", pctTransDAO.getRptName(), pctTrans.rptName);

        // Smart expense.
        Assert.assertEquals(MTAG + ": smart expense me key", pctTransDAO.getSmartExpenseMeKey(),
                pctTrans.smartExpenseMeKey);

        // Mobile Entry
        MobileEntryDAO mobEntDAO = pctTransDAO.getMobileEntryDAO();
        if (mobEntDAO != null && pctTrans.mobileEntry != null) {
            verifyMobileEntry(mobEntDAO, pctTrans.mobileEntry);
        } else if (mobEntDAO != null) {
            Assert.assertTrue(MTAG + ": parsed mobile entry is null", false);
        } else if (pctTrans.mobileEntry != null) {
            Assert.assertTrue(MTAG + ": mobile entry DAO is null", false);
        }

        // Smart matched.
        if (pctTransDAO.getType() == ExpenseTypeEnum.SMART_PERSONAL) {
            // Smart matched mobile entry comparison.
            MobileEntryDAO smartMobEntDAO = pctTransDAO.getSmartMatchedMobileEntryDAO();
            MobileEntry smartMobEnt = null;
            if (!TextUtils.isEmpty(pctTrans.smartExpenseMeKey)) {
                if (expListResp.mobileEntries != null) {
                    for (MobileEntry mobEnt : expListResp.mobileEntries) {
                        if (!TextUtils.isEmpty(mobEnt.meKey)
                                && mobEnt.meKey.equalsIgnoreCase(pctTrans.smartExpenseMeKey)) {
                            smartMobEnt = mobEnt;
                            break;
                        }
                    }
                }
            }
            if (smartMobEntDAO != null && smartMobEnt != null) {
                verifyMobileEntry(smartMobEntDAO, smartMobEnt);
            } else if (smartMobEntDAO != null) {
                Assert.assertTrue(MTAG + ": parsed smart personal mobile entry is null", false);
            } else if (smartMobEnt != null) {
                Assert.assertTrue(MTAG + ": smart personal mobile entry DAO is null", false);
            }
        }
    }

    /**
     * Verifies a mobile entry DAO object against a parsed mobile entry object.
     * 
     * @param mobEntDAO
     *            contains the mobile entry DAO object.
     * @param mobEnt
     *            contains the parsed mobile entry object.
     */
    private void verifyMobileEntry(MobileEntryDAO mobEntDAO, MobileEntry mobEnt) {
        final String MTAG = CLS_TAG + ".verifyMobileEntry";

        // Currency code.
        Assert.assertEquals(MTAG + ": crn code", mobEntDAO.getCrnCode(), mobEnt.crnCode);

        // Exp key.
        Assert.assertEquals(MTAG + ": exp key", mobEntDAO.getExpKey(), mobEnt.expKey);

        // Exp name.
        Assert.assertEquals(MTAG + ": exp name", mobEntDAO.getExpName(), mobEnt.expName);

        // Location name.
        Assert.assertEquals(MTAG + ": location name", mobEntDAO.getLocationName(), mobEnt.locationName);

        // Vendor name.
        Assert.assertEquals(MTAG + ": vendor name", mobEntDAO.getVendorName(), mobEnt.vendorName);

        // Type.
        Assert.assertEquals(MTAG + ": type", mobEntDAO.getEntryType(), mobEnt.type);

        // Me key.
        Assert.assertEquals(MTAG + ": me key", mobEntDAO.getMeKey(), mobEnt.meKey);

        // Pca key.
        Assert.assertEquals(MTAG + ": pca key", mobEntDAO.getPcaKey(), mobEnt.pcaKey);

        // Pct key.
        Assert.assertEquals(MTAG + ": pct key", mobEntDAO.getPctKey(), mobEnt.pctKey);

        // Cct key.
        Assert.assertEquals(MTAG + ": cct key", mobEntDAO.getCctKey(), mobEnt.cctKey);

        // Rc key.
        Assert.assertEquals(MTAG + ": rc key", mobEntDAO.getRcKey(), mobEnt.rcKey);

        // Transaction amount.
        Assert.assertEquals(MTAG + ": transaction amount", mobEntDAO.getTransactionAmount(), mobEnt.transactionAmount);

        // Transaction date.
        Assert.assertEquals(MTAG + ": transaction date", mobEntDAO.getTransactionDate(), mobEnt.transactionDate);

        // Has receipt image.
        Assert.assertEquals(MTAG + ": has receipt image", mobEntDAO.hasReceiptImage(), mobEnt.hasReceiptImage);

        // Receipt image id.
        Assert.assertEquals(MTAG + ": receipt image id", mobEntDAO.getReceiptImageId(), mobEnt.receiptImageId);

        // Receipt image data.
        Assert.assertEquals(MTAG + ": receipt image data", mobEntDAO.getReceiptImageData(), mobEnt.receiptImageData);

        // Comment.
        Assert.assertEquals(MTAG + ": comment", mobEntDAO.getComment(), mobEnt.comment);
    }

    /**
     * Verifies a receipt capture DAO object against a parsed receipt capture object.
     * 
     * @param recCapDAO
     *            contains the receipt capture DAO object.
     * @param recCap
     *            contains the parsed receipt capture object.
     */
    private void verifyReceiptCapture(ReceiptCaptureDAO recCapDAO, ReceiptCapture recCap) {
        final String MTAG = CLS_TAG + ".verifyReceiptCapture";

        // Type
        Assert.assertEquals(MTAG + ": type", recCapDAO.getType(), recCap.type);

        // Currency code.
        Assert.assertEquals(MTAG + ": crn code", recCapDAO.getCurrencyCode(), recCap.crnCode);

        // Exp key.
        Assert.assertEquals(MTAG + ": exp key", recCapDAO.getExpKey(), recCap.expKey);

        // Exp name.
        Assert.assertEquals(MTAG + ": exp name", recCapDAO.getExpName(), recCap.expName);

        // Vendor name.
        Assert.assertEquals(MTAG + ": vendor name", recCapDAO.getVendorName(), recCap.vendorName);

        // Rc key.
        Assert.assertEquals(MTAG + ": rc key", recCapDAO.getRCKey(), recCap.rcKey);

        // Smart expense id.
        Assert.assertEquals(MTAG + ": smart expense id", recCapDAO.getSmartExpenseId(), recCap.smartExpId);

        // Transaction amount.
        Assert.assertEquals(MTAG + ": transaction amount", recCapDAO.getTransactionAmount(), recCap.transactionAmount);

        // Transaction date.
        Assert.assertEquals(MTAG + ": transaction date", recCapDAO.getTransactionDate(), recCap.transactionDate);

        // Receipt image id.
        Assert.assertEquals(MTAG + ": receipt image id", recCapDAO.getReceiptImageId(), recCap.receiptImageId);
    }

}
