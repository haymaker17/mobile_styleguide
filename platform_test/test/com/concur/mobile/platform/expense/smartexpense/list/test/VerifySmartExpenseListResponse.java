package com.concur.mobile.platform.expense.smartexpense.list.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import android.content.Context;
import android.text.TextUtils;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.list.dao.PersonalCardDAO;
import com.concur.mobile.platform.expense.smartexpense.SmartExpenseList;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseDAO;
import com.concur.mobile.platform.expense.smartexpense.dao.SmartExpenseListDAO;

/**
 * Provides a class to verify a <code>SmartExpenseListResponse</code> object against data stored in the content provider.
 * 
 * @author andrewk
 */
public class VerifySmartExpenseListResponse {

    private static final String CLS_TAG = "VerifySmartExpenseListResponse";

    /**
     * Will verify smart expense information in <code>expListResp</code> against smart expense information stored in the expense
     * content provider optionally ignoring session id.
     * 
     * @param context
     *            contains an application context.
     * @param expListResp
     *            contains the expense list response.
     */
    public void verify(Context context, SmartExpenseList expListResp) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(context);
        Assert.assertNotNull(MTAG + ": session info is null", sessInfo);

        SmartExpenseListDAO smExpListDAO = new SmartExpenseListDAO(context, sessInfo.getUserId());

        // Verify count of expenses.
        List<SmartExpenseDAO> smExpDAOS = smExpListDAO.getSmartExpenses(context, sessInfo.getUserId());
        Long smExpDaosCount = (smExpDAOS != null) ? smExpDAOS.size() : 0L;
        List<SmartExpenseDAO> parsedSmartExpenses = (expListResp != null) ? expListResp.getSmartExpenses() : null;
        Long smExpCount = (parsedSmartExpenses != null) ? parsedSmartExpenses.size() : 0L;
        Assert.assertEquals(MTAG + ": smart expense count", smExpCount, smExpDaosCount);

        // Verify parsed expense information against content provider information.
        if (smExpDAOS != null) {
            // First build look-up map.
            Map<String, SmartExpenseDAO> smExpDAOMap = new HashMap<String, SmartExpenseDAO>(smExpDAOS.size());
            for (SmartExpenseDAO smExpDAO : smExpDAOS) {
                if (!TextUtils.isEmpty(smExpDAO.getSmartExpenseId())) {
                    smExpDAOMap.put(smExpDAO.getSmartExpenseId(), smExpDAO);
                }
            }
            for (SmartExpenseDAO parsedExp : parsedSmartExpenses) {

                // Verify the parsed smart expense Id can be found in the map built from the content provider.
                SmartExpenseDAO smExpDAO = smExpDAOMap.get(parsedExp.getSmartExpenseId());
                Assert.assertNotNull(MTAG + ": unable to locate parsed smart expense in DAO map", smExpDAO);

                // Verify field information.

                // Id
                Assert.assertEquals(MTAG + ": SmartExpenseId", parsedExp.getSmartExpenseId(),
                        smExpDAO.getSmartExpenseId());

                // RepKey
                Assert.assertEquals(MTAG + ": RpeKey", parsedExp.getRpeKey(), smExpDAO.getRpeKey());

                // EReceiptId
                Assert.assertEquals(MTAG + ": EReceiptId", parsedExp.getEReceiptId(), smExpDAO.getEReceiptId());

                // EReceiptType
                Assert.assertEquals(MTAG + ": EReceiptType", parsedExp.getEReceiptType(), smExpDAO.getEReceiptType());

                // EReceiptSource
                Assert.assertEquals(MTAG + ": EReceiptSource", parsedExp.getEReceiptSource(),
                        smExpDAO.getEReceiptSource());

                // TravelCompanyCode
                Assert.assertEquals(MTAG + ": TravelCompanyCode", parsedExp.getTravelCompanyCode(),
                        smExpDAO.getTravelCompanyCode());

                // VendorCode
                Assert.assertEquals(MTAG + ": VendorCode", parsedExp.getVendorCode(), smExpDAO.getVendorCode());

                // AirlineCode
                Assert.assertEquals(MTAG + ": AirlineCode", parsedExp.getAirlineCode(), smExpDAO.getAirlineCode());

                // TripId
                Assert.assertEquals(MTAG + ": TripId", parsedExp.getTripId(), smExpDAO.getTripId());

                // TripName
                Assert.assertEquals(MTAG + ": TripName", parsedExp.getTripName(), smExpDAO.getTripName());

                // TransactionDate.
                Assert.assertEquals(MTAG + ": TransactionDate", parsedExp.getTransactionDate(),
                        smExpDAO.getTransactionDate());

                // ExpenseName
                Assert.assertEquals(MTAG + ": ExpenseName", parsedExp.getExpenseName(), smExpDAO.getExpenseName());

                // CrnCode
                Assert.assertEquals(MTAG + ": CrnCode", parsedExp.getCrnCode(), smExpDAO.getCrnCode());

                // TransactionAmount
                Assert.assertEquals(MTAG + ": TransactionAmount", parsedExp.getTransactionAmount(),
                        smExpDAO.getTransactionAmount());

                // PostedAmount
                Assert.assertEquals(MTAG + ": PostedAmount", parsedExp.getPostedAmount(), smExpDAO.getPostedAmount());

                // ExchangeRate
                Assert.assertEquals(MTAG + ": ExchangeRate", parsedExp.getExchangeRate(), smExpDAO.getExchangeRate());

                // TransactionCurrencyCode
                Assert.assertEquals(MTAG + ": TransactionCurrencyCode", parsedExp.getTransactionCurrencyCode(),
                        smExpDAO.getTransactionCurrencyCode());

                // PostedCurrencyCode
                Assert.assertEquals(MTAG + ": PostedCurrencyCode", parsedExp.getPostedCurrencyCode(),
                        smExpDAO.getPostedCurrencyCode());

                // SegmentId
                Assert.assertEquals(MTAG + ": SegmentId", parsedExp.getSegmentId(), smExpDAO.getSegmentId());

                // CctKey
                Assert.assertEquals(MTAG + ": CctKey", parsedExp.getCctKey(), smExpDAO.getCctKey());

                // CcaKey
                Assert.assertEquals(MTAG + ": getCcaKey", parsedExp.getCcaKey(), smExpDAO.getCcaKey());

                // ExtractCctKey
                Assert.assertEquals(MTAG + ": ExtractCctKey", parsedExp.getExtractCctKey(), smExpDAO.getExtractCctKey());

                // SegmentTypeKey
                Assert.assertEquals(MTAG + ": SegmentTypeKey", parsedExp.getSegmentTypeKey(),
                        smExpDAO.getSegmentTypeKey());

                // VenLiName
                Assert.assertEquals(MTAG + ": VenLiName", parsedExp.getVenLiName(), smExpDAO.getVenLiName());
                // MerchantName
                Assert.assertEquals(MTAG + ": MerchantName", parsedExp.getMerchantName(), smExpDAO.getMerchantName());

                // DoingBusinessAs
                Assert.assertEquals(MTAG + ": DoingBusinessAs", parsedExp.getDoingBusinessAs(),
                        smExpDAO.getDoingBusinessAs());

                // ExpKey
                Assert.assertEquals(MTAG + ": ExpKey", parsedExp.getExpKey(), smExpDAO.getExpKey());

                // LocName
                Assert.assertEquals(MTAG + ": LocName", parsedExp.getLocName(), smExpDAO.getLocName());

                // MerchantCity
                Assert.assertEquals(MTAG + ": MerchantCity", parsedExp.getMerchantCity(), smExpDAO.getMerchantCity());

                // MerchantState
                Assert.assertEquals(MTAG + ": MerchantState", parsedExp.getMerchantState(), smExpDAO.getMerchantState());

                // MerchantCountryCode
                Assert.assertEquals(MTAG + ": MerchantCountryCode", parsedExp.getMerchantCountryCode(),
                        smExpDAO.getMerchantCountryCode());

                // VendorDescription
                Assert.assertEquals(MTAG + ": SmartExpenseId", parsedExp.getVendorDescription(),
                        smExpDAO.getVendorDescription());

                // City
                Assert.assertEquals(MTAG + ": City", parsedExp.getCity(), smExpDAO.getCity());

                // State
                Assert.assertEquals(MTAG + ": State", parsedExp.getState(), smExpDAO.getState());

                // Country
                Assert.assertEquals(MTAG + ": Country", parsedExp.getCountry(), smExpDAO.getCountry());

                // CardTypeCode
                Assert.assertEquals(MTAG + ": CardTypeCode", parsedExp.getCardTypeCode(), smExpDAO.getCardTypeCode());

                // hasRichData
                Assert.assertEquals(MTAG + ": hasRichData", parsedExp.hasRichData(), smExpDAO.hasRichData());

                // EstimatedAmount
                Assert.assertEquals(MTAG + ": EstimatedAmount", parsedExp.getEstimatedAmount(),
                        smExpDAO.getEstimatedAmount());

                // MeKey
                Assert.assertEquals(MTAG + ": MeKey", parsedExp.getMeKey(), smExpDAO.getMeKey());

                // PctKey
                Assert.assertEquals(MTAG + ": PctKey", parsedExp.getPctKey(), smExpDAO.getPctKey());

                // PcaKey
                Assert.assertEquals(MTAG + ": PcaKey", parsedExp.getPcaKey(), smExpDAO.getPcaKey());

                // ChargeDescription
                Assert.assertEquals(MTAG + ": ChargeDescription", parsedExp.getChargeDescription(),
                        smExpDAO.getChargeDescription());

                // CardCategoryName
                Assert.assertEquals(MTAG + ": CardCategoryName", parsedExp.getCardCategoryName(),
                        smExpDAO.getCardCategoryName());

                // TransactionGroup
                Assert.assertEquals(MTAG + ": TransactionGroup", parsedExp.getTransactionGroup(),
                        smExpDAO.getTransactionGroup());

                // MobileReceiptImageId
                Assert.assertEquals(MTAG + ": MobileReceiptImageId", parsedExp.getMobileReceiptImageId(),
                        smExpDAO.getMobileReceiptImageId());

                // CardIconFileName
                Assert.assertEquals(MTAG + ": CardIconFileName", parsedExp.getCardIconFileName(),
                        smExpDAO.getCardIconFileName());

                // CardProgramTypeName
                Assert.assertEquals(MTAG + ": CardProgramTypeName", parsedExp.getCardProgramTypeName(),
                        smExpDAO.getCardProgramTypeName());

                // RcKey
                Assert.assertEquals(MTAG + ": RcKey", parsedExp.getRcKey(), smExpDAO.getRcKey());

                // StatKey
                Assert.assertEquals(MTAG + ": StatKey", parsedExp.getStatKey(), smExpDAO.getStatKey());

                // RejectCode
                Assert.assertEquals(MTAG + ": RejectCode", parsedExp.getRejectCode(), smExpDAO.getRejectCode());

                // ReceiptImageId
                Assert.assertEquals(MTAG + ": ReceiptImageId", parsedExp.getReceiptImageId(),
                        smExpDAO.getReceiptImageId());

                // FuelServiceCharge
                Assert.assertEquals(MTAG + ": FuelServiceCharge", parsedExp.getFuelServiceCharge(),
                        smExpDAO.getFuelServiceCharge());

                // GpsCharge
                Assert.assertEquals(MTAG + ": GpsCharge", parsedExp.getGpsCharge(), smExpDAO.getGpsCharge());

                // InsuranceCharge
                Assert.assertEquals(MTAG + ": InsuranceCharge", parsedExp.getInsuranceCharge(),
                        smExpDAO.getInsuranceCharge());

                // CardLastSegment
                Assert.assertEquals(MTAG + ": CardLastSegment", parsedExp.getCardLastSegment(),
                        smExpDAO.getCardLastSegment());

                // TicketNumber
                Assert.assertEquals(MTAG + ": TicketNumber", parsedExp.getTicketNumber(), smExpDAO.getTicketNumber());

                // EReceiptImageId
                Assert.assertEquals(MTAG + ": EReceiptImageId", parsedExp.getEReceiptImageId(),
                        smExpDAO.getEReceiptImageId());

                // CctReceiptImageId
                Assert.assertEquals(MTAG + ": CctReceiptImageId", parsedExp.getCctReceiptImageId(),
                        smExpDAO.getCctReceiptImageId());

                // Comment
                Assert.assertEquals(MTAG + ": Comment", parsedExp.getComment(), smExpDAO.getComment());

                // Total Days.
                Assert.assertEquals(MTAG + ": TotalDays", parsedExp.getTotalDays(), smExpDAO.getTotalDays());

                // PickUpDate.
                Assert.assertEquals(MTAG + ": PickUpDate", parsedExp.getPickUpDate(), smExpDAO.getPickUpDate());

                // ReturnDate.
                Assert.assertEquals(MTAG + ": ReturnDate", parsedExp.getReturnDate(), smExpDAO.getReturnDate());

                // ConfirmationNumber.
                Assert.assertEquals(MTAG + ": ConfirmationNumber", parsedExp.getConfirmationNumber(),
                        smExpDAO.getConfirmationNumber());

                // AverageDailyRate.
                Assert.assertEquals(MTAG + ": AverageDailyRate", parsedExp.getAverageDailyRate(),
                        smExpDAO.getAverageDailyRate());

            }
        }

        // Verify parsed personal card information against content provider information.
        verifyPersonalCards(context, smExpListDAO, expListResp, sessInfo);

    }

    /**
     * Will verify the list of personal card objects parsed against personal card objects stored in the content provider (DAO
     * objects).
     * 
     * @param smExpListDAO
     *            contains the smart expense list DAO object.
     * @param expListResp
     *            contains the parsed smart expense list response.
     */
    private void verifyPersonalCards(Context context, SmartExpenseListDAO smExpListDAO, SmartExpenseList expListResp,
            SessionInfo sessInfo) {
        final String MTAG = "verifyPersonalCards";

        // Verify count of personal cards.
        List<PersonalCardDAO> persCardDAOS = smExpListDAO.getPersonalCards(context, sessInfo.getUserId());
        Long persCardDaosCount = (persCardDAOS != null) ? persCardDAOS.size() : 0L;
        List<PersonalCardDAO> parsedPersonalCards = (expListResp != null) ? expListResp.getPersonalCards() : null;
        Long persCardCount = (parsedPersonalCards != null) ? parsedPersonalCards.size() : 0L;
        Assert.assertEquals(MTAG + ": personal card count", persCardCount, persCardDaosCount);

        // Verify parsed personal card information against content provider information.
        if (persCardDAOS != null) {
            // First build look-up map.
            Map<String, PersonalCardDAO> persCardDAOMap = new HashMap<String, PersonalCardDAO>(persCardDAOS.size());
            for (PersonalCardDAO persCardDAO : persCardDAOS) {
                if (!TextUtils.isEmpty(persCardDAO.getPCAKey())) {
                    persCardDAOMap.put(persCardDAO.getPCAKey(), persCardDAO);
                }
            }
            for (PersonalCardDAO parsedPersCard : parsedPersonalCards) {

                // Verify the parsed personal card account key can be found in the map built from the content provider.
                PersonalCardDAO persCardDAO = persCardDAOMap.get(parsedPersCard.getPCAKey());
                Assert.assertNotNull(MTAG + ": unable to locate parsed personal card in DAO map", persCardDAO);

                // Verify field information.

                // Pca key -- given that above map match is based on pcaKey.

                // Card name.
                Assert.assertEquals(MTAG + ": cardName", parsedPersCard.getCardName(), persCardDAO.getCardName());

                // Acct num last four.
                Assert.assertEquals(MTAG + ": acctNumLastFour", parsedPersCard.getAcctNumLastFour(),
                        persCardDAO.getAcctNumLastFour());

                // Crn code.
                Assert.assertEquals(MTAG + ": crnCode", parsedPersCard.getCrnCode(), persCardDAO.getCrnCode());

            }
        }
    }
}
