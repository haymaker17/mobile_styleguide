/**
 * 
 */
package com.concur.mobile.platform.authentication.system.config.test;

import java.util.Hashtable;
import java.util.List;

import org.junit.Assert;

import android.content.Context;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.config.system.dao.ExpenseTypeDAO;
import com.concur.mobile.platform.config.system.dao.OfficeLocationDAO;
import com.concur.mobile.platform.config.system.dao.ReasonCodeDAO;
import com.concur.mobile.platform.config.system.dao.SystemConfigDAO;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a class to verify a <code>VerifySystemConfigResult</code> object against data stored in the content provider.
 * 
 * @author sunill
 */
public class VerifySystemConfigResult {

    private static final String CLS_TAG = "VerifySystemConfigResult";
    private Hashtable<Integer, ReasonCodeDAO> reasonCode = new Hashtable<Integer, ReasonCodeDAO>();
    private Hashtable<String, ExpenseTypeDAO> expenseTypes = new Hashtable<String, ExpenseTypeDAO>();

    /**
     * Will verify System Config response information stored in the config content provider against information stored in
     * <code>SystemConfigResult</code>.
     * 
     * @param context
     *            contains a reference to the application context.
     * @param systemConfigResult
     *            contains a reference to a System Config Response.
     * @throws Exception
     *             throws an exception if the stored System Config response data does not match <code>systemConfigResult</code>.
     */
    public void verify(Context context, SystemConfigResult systemConfigResult) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        // Verify User Information.
        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
        String userId = sessionInfo.getUserId();
        SystemConfigDAO dao = new SystemConfigDAO(context, userId);
        if (dao != null) {
            // check refundableCheckboxDefault.
            Assert.assertEquals(MTAG + ": refundableCheckboxDefault", systemConfigResult.refundableCheckboxDefault,
                    dao.getRefundableCheckboxDefault());
            // check refundableMessage.
            Assert.assertEquals(MTAG + ": refundableMessage", systemConfigResult.refundableMessage,
                    dao.getRefundableMessage());
            // check refundableShowCheckbox.
            Assert.assertEquals(MTAG + ": refundableShowCheckbox", systemConfigResult.refundableShowCheckbox,
                    dao.getRefundableShowCheckbox());
            // check ruleViolationExplanationRequired.
            Assert.assertEquals(MTAG + ": ruleViolationExplanationRequired",
                    systemConfigResult.ruleViolationExplanationRequired, dao.getRuleViolationExplanationRequired());

            // Verify air reasons.
            List<ReasonCodeDAO> reasonProvider = dao.getAirReasons();
            List<ReasonCode> reasonResult = systemConfigResult.airReasonCode;
            verifyReason(MTAG, reasonProvider, reasonResult);

            // Verify car reasons.
            reasonProvider = dao.getCarReasons();
            reasonResult = systemConfigResult.carReasonCode;
            verifyReason(MTAG, reasonProvider, reasonResult);

            // Verify hotel reasons.
            reasonProvider = dao.getHotelReasons();
            reasonResult = systemConfigResult.hotelReasonCode;
            verifyReason(MTAG, reasonProvider, reasonResult);

            // verify office locations.
            List<OfficeLocationDAO> officeLocationProvider = dao.getCompanyLocations();
            List<Office> officeLocationResult = systemConfigResult.offices;
            verifyOffice(MTAG, officeLocationProvider, officeLocationResult);

            // verify Expense Types.
            List<ExpenseTypeDAO> expenseTypeProvider = dao.getExpenseTypes();
            List<ExpenseType> expenseTypeResult = systemConfigResult.expenseType;
            verifyExpenseTypeList(MTAG, expenseTypeProvider, expenseTypeResult);
        }
    }

    /**
     * Verify Reason code list of car/hotel/air.
     * 
     * @param MTAG
     *            verify TAG.
     * @param reasonProvider
     *            reason code list from content provider.
     * @param reasonResult
     *            reason code list from xml or MWS
     */
    private void verifyReason(String MTAG, List<ReasonCodeDAO> reasonProvider, List<ReasonCode> reasonResult) {
        if (reasonProvider != null && reasonResult != null) {
            Assert.assertEquals(MTAG + ": reason list size", reasonResult.size(), reasonProvider.size());
            reasonCode.clear();
            for (ReasonCodeDAO reasonCodeDAO : reasonProvider) {
                Integer id = reasonCodeDAO.getId();
                reasonCode.put(id, reasonCodeDAO);
            }
            for (ReasonCode reasonCodeResultItem : reasonResult) {
                Integer id = reasonCodeResultItem.id;
                boolean haveKey = reasonCode.containsKey(id);
                Assert.assertTrue(haveKey);
                ReasonCodeDAO reasonCodeProviderItem = reasonCode.get(id);
                Assert.assertEquals(MTAG + ": air reason list item", reasonCodeProviderItem.getDescription().trim(),
                        reasonCodeResultItem.description.trim());
                Assert.assertEquals(MTAG + ": air reason list item", reasonCodeProviderItem.getViolationType(),
                        reasonCodeResultItem.violationType.trim());
            }
        } else if (reasonProvider == null && reasonResult != null) {
            Assert.assertNull(MTAG + ": reason list from provider is null", reasonProvider);
            Assert.assertNotNull(MTAG + ": reason list from response is not null", reasonResult);
        } else if (reasonProvider != null && reasonResult == null) {
            Assert.assertNull(MTAG + ": reason list from response is null", reasonResult);
            Assert.assertNotNull(MTAG + ": reason list from provider is not null", reasonProvider);
        }
    }

    /**
     * Verify Office location list
     * 
     * @param MTAG
     *            verify TAG.
     * @param officeProvider
     *            office list from content provider.
     * @param officeResult
     *            office list from xml or MWS
     */
    private void verifyOffice(String MTAG, List<OfficeLocationDAO> officeProvider, List<Office> officeResult) {
        if (officeProvider != null && officeResult != null) {
            Assert.assertEquals(MTAG + ": office location list size", officeResult.size(), officeProvider.size());
            // N-squared search!
            for (Office office : officeResult) {
                boolean foundOffice = false;
                for (OfficeLocationDAO officeDao : officeProvider) {
                    if (Double.compare(office.latitude, officeDao.getLat()) == 0
                            && Double.compare(office.longitude, officeDao.getLon()) == 0) {
                        foundOffice = true;
                        Assert.assertEquals(MTAG + ": office address ", office.address, officeDao.getAddress());
                        Assert.assertEquals(MTAG + ": office city ", office.city, officeDao.getCity());
                        Assert.assertEquals(MTAG + ": office country", office.country, officeDao.getCountry());
                        Assert.assertEquals(MTAG + ": office lat", office.latitude, officeDao.getLat());
                        Assert.assertEquals(MTAG + ": office lon", office.longitude, officeDao.getLon());
                        Assert.assertEquals(MTAG + ": office state ", office.state, officeDao.getState());
                    }
                }
                Assert.assertTrue(MTAG + ": found office", foundOffice);
            }
        } else if (officeProvider == null && officeResult != null) {
            Assert.assertNull(MTAG + ": office list from provider is null", officeProvider);
            Assert.assertNotNull(MTAG + ": office list from response is not null", officeResult);
        } else if (officeProvider != null && officeResult == null) {
            Assert.assertNull(MTAG + ": office list from response is null", officeResult);
            Assert.assertNotNull(MTAG + ": office list from provider is not null", officeProvider);
        }
    }

    /**
     * Verify Expense Type list.
     * 
     * @param MTAG
     *            verify TAG.
     * @param expenseTypeProvider
     *            expense type list from content provider.
     * @param expenseTypeResult
     *            expense type list from xml or MWS
     */
    private void verifyExpenseTypeList(String MTAG, List<ExpenseTypeDAO> expenseTypeProvider,
            List<ExpenseType> expenseTypeResult) {
        if (expenseTypeProvider != null && expenseTypeResult != null) {
            Assert.assertEquals(MTAG + ": expense type list size", expenseTypeResult.size(), expenseTypeProvider.size());
            expenseTypes.clear();
            for (ExpenseTypeDAO expenseTypeDAO : expenseTypeProvider) {
                String id = expenseTypeDAO.getExpKey();
                expenseTypes.put(id, expenseTypeDAO);
            }
            for (ExpenseType expTypeResultItem : expenseTypeResult) {
                String id = expTypeResultItem.expKey;
                boolean haveKey = expenseTypes.containsKey(id);

                Assert.assertTrue(haveKey);

                ExpenseTypeDAO expenseTypeProviderItem = expenseTypes.get(id);

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.expCode,
                        expenseTypeProviderItem.getExpCode());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.expName,
                        expenseTypeProviderItem.getExpName());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.formKey,
                        expenseTypeProviderItem.getFormKey());

                Boolean hasPostAmtCalc = null;
                if (expTypeResultItem.hasPostAmtCalc != null) {
                    hasPostAmtCalc = Parse.safeParseBoolean(expTypeResultItem.hasPostAmtCalc);
                }
                Assert.assertEquals(MTAG + ": expense type item", hasPostAmtCalc,
                        expenseTypeProviderItem.getHasPostAmtCalc());

                Boolean hasTaxForm = null;
                if (expTypeResultItem.hasTaxForm != null) {
                    hasTaxForm = Parse.safeParseBoolean(expTypeResultItem.hasTaxForm);
                }
                Assert.assertEquals(MTAG + ": expense type item", hasTaxForm, expenseTypeProviderItem.getHasTaxForm());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.itemizationUnallowExpKeys,
                        expenseTypeProviderItem.getItemizationUnallowExpKeys());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.itemizeFormKey,
                        expenseTypeProviderItem.getItemizeFormKey());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.itemizeStyle,
                        expenseTypeProviderItem.getItemizeStyle());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.itemizeType,
                        expenseTypeProviderItem.getItemizeType());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.parentExpKey,
                        expenseTypeProviderItem.getParentExpKey());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.parentExpName,
                        expenseTypeProviderItem.getParentExpName());

                Boolean supportsAttendees = null;
                if (expTypeResultItem.supportsAttendees != null) {
                    supportsAttendees = Parse.safeParseBoolean(expTypeResultItem.supportsAttendees);
                }
                Assert.assertEquals(MTAG + ": expense type item", supportsAttendees,
                        expenseTypeProviderItem.getSupportsAttendees());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.vendorListKey,
                        expenseTypeProviderItem.getVendorListKey());

                Boolean allowEditAtnAmt = null;
                if (expTypeResultItem.allowEditAtnAmt != null) {
                    allowEditAtnAmt = Parse.safeParseBoolean(expTypeResultItem.allowEditAtnAmt);
                }
                Assert.assertEquals(MTAG + ": expense type item", allowEditAtnAmt,
                        expenseTypeProviderItem.getAllowEditAtnAmt());

                Boolean allowEditAtnCount = null;
                if (expTypeResultItem.allowEditAtnCount != null) {
                    allowEditAtnCount = Parse.safeParseBoolean(expTypeResultItem.allowEditAtnCount);
                }
                Assert.assertEquals(MTAG + ": expense type item", allowEditAtnCount,
                        expenseTypeProviderItem.getAllowEditAtnCount());

                Boolean allowNoShows = null;
                if (expTypeResultItem.allowNoShows != null) {
                    allowNoShows = Parse.safeParseBoolean(expTypeResultItem.allowNoShows);
                }
                Assert.assertEquals(MTAG + ": expense type item", allowNoShows,
                        expenseTypeProviderItem.getAllowNoShows());

                Boolean displayAddAtnOnForm = null;
                if (expTypeResultItem.displayAddAtnOnForm != null) {
                    displayAddAtnOnForm = Parse.safeParseBoolean(expTypeResultItem.displayAddAtnOnForm);
                }
                Assert.assertEquals(MTAG + ": expense type item", displayAddAtnOnForm,
                        expenseTypeProviderItem.getDisplayAddAtnOnForm());

                Boolean displayAtnAmounts = null;
                if (expTypeResultItem.displayAtnAmounts != null) {
                    displayAtnAmounts = Parse.safeParseBoolean(expTypeResultItem.displayAtnAmounts);
                }
                Assert.assertEquals(MTAG + ": expense type item", displayAtnAmounts,
                        expenseTypeProviderItem.getDisplayAtnAmounts());

                Boolean userAsAtnDefault = null;
                if (expTypeResultItem.userAsAtnDefault != null) {
                    userAsAtnDefault = Parse.safeParseBoolean(expTypeResultItem.userAsAtnDefault);
                }
                Assert.assertEquals(MTAG + ": expense type item", userAsAtnDefault,
                        expenseTypeProviderItem.getUserAsAtnDefault());

                Assert.assertEquals(MTAG + ": expense type item", expTypeResultItem.unallowAtnTypeKeys,
                        expenseTypeProviderItem.getUnallowAtnTypeKeys());
            }
        } else if (expenseTypeProvider == null && expenseTypeResult != null) {
            Assert.assertNull(MTAG + ": expense type list from provider is null", expenseTypeProvider);
            Assert.assertNotNull(MTAG + ": expense type list from response is not null", expenseTypeResult);
        } else if (expenseTypeProvider != null && expenseTypeResult == null) {
            Assert.assertNull(MTAG + ": expense type list from response is null", expenseTypeResult);
            Assert.assertNotNull(MTAG + ": expense type list from provider is not null", expenseTypeProvider);
        }
    }
}
