package com.concur.mobile.platform.config.user.test;

import java.util.HashMap;
import java.util.List;

import org.junit.Assert;

import android.content.Context;

import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.config.user.dao.AttendeeColumnDefinitionDAO;
import com.concur.mobile.platform.config.user.dao.AttendeeTypeDAO;
import com.concur.mobile.platform.config.user.dao.CarTypeDAO;
import com.concur.mobile.platform.config.user.dao.CurrencyDAO;
import com.concur.mobile.platform.config.user.dao.ExpenseConfirmationDAO;
import com.concur.mobile.platform.config.user.dao.PolicyDAO;
import com.concur.mobile.platform.config.user.dao.TravelPointsConfigDAO;
import com.concur.mobile.platform.config.user.dao.UserConfigDAO;
import com.concur.mobile.platform.config.user.dao.YodleePaymentTypeDAO;
import com.concur.mobile.platform.util.Parse;

public class VerifyUserConfigResult {

    private static final String CLS_TAG = "VerifyUserConfigResult";

    /**
     * Will verify login response information stored in the config content provider against information stored in
     * <code>loginResult</code>.
     * 
     * @param context
     *            contains a reference to the application context.
     * @param loginResult
     *            contains a reference to a login response.
     * @throws Exception
     *             throws an exception if the stored login result data does not match <code>loginResult</code>.
     */
    public void verify(Context context, UserConfigResult userConfigResult) throws Exception {

        final String MTAG = CLS_TAG + ".verify";

        SessionInfo sessionInfo = ConfigUtil.getSessionInfo(context);
        String userId = sessionInfo.getUserId();
        UserConfigDAO dao = new UserConfigDAO(context, userId);
        if (dao != null) {
            // check flags.
            if (userConfigResult.flags != null) {
                Assert.assertEquals(MTAG + ": flags", userConfigResult.flags, dao.getFlags());
            }

            // check showGDSNameInSearchResults.
            if (userConfigResult.showGDSNameInSearchResults != null) {
                Assert.assertEquals(MTAG + ": showGDSNameInSearchResults", userConfigResult.showGDSNameInSearchResults,
                        dao.getshowGDSNameInSearchResults());
            }

            // verify AttendeeColDefs.
            List<AttendeeColumnDefinitionDAO> attendeeColumnDefinitionProvider = dao.getAttendeeColumnDefinitions();
            List<AttendeeColumnDefinition> attendeeColumnDefinitionResult = userConfigResult.attendeeColumnDefinitions;
            verifyAttendeeColumnDefinitionList(MTAG, attendeeColumnDefinitionProvider, attendeeColumnDefinitionResult);

            // verify Attendee Types.
            List<AttendeeTypeDAO> attendeeTypeProvider = dao.getAttendeeTypes();
            List<AttendeeType> attendeeTypeResult = userConfigResult.attendeeTypes;
            verifyAttendeeTypeList(MTAG, attendeeTypeProvider, attendeeTypeResult);

            // verify Car Types.
            List<CarTypeDAO> carTypeProvider = dao.getAllowedCarTypes();
            List<CarType> carTypeResult = userConfigResult.allowedCarTypes;
            verifyCarTypeList(MTAG, carTypeProvider, carTypeResult);

            // verify Currency.
            List<CurrencyDAO> currencyProvider = dao.getCurrencies();
            List<Currency> currencyResult = userConfigResult.currencies;
            verifyCurrencyList(MTAG, currencyProvider, currencyResult);

            // verify Reimbursement Currency.
            List<CurrencyDAO> reimbCurrencyProvider = dao.getCurrencies();
            List<Currency> reimbCurrencyResult = userConfigResult.currencies;
            verifyCurrencyList(MTAG, reimbCurrencyProvider, reimbCurrencyResult);

            // verify Expense Confirmation.
            List<ExpenseConfirmationDAO> expConfProvider = dao.getExpenseConfirmations();
            List<ExpenseConfirmation> expConfResult = userConfigResult.expenseConfirmations;
            verifyExpenseConfirmationList(MTAG, expConfProvider, expConfResult);

            // verify Policy.
            List<PolicyDAO> policyProvider = dao.getExpensePolicies();
            List<ExpensePolicy> policyResult = userConfigResult.expensePolicies;
            verifyPolicyList(MTAG, policyProvider, policyResult);

            // verify YodleePaymentType.
            List<YodleePaymentTypeDAO> yodleePaymentTypeProvider = dao.getYodleePaymentTypes();
            List<YodleePaymentType> yodleePaymentTypeResult = userConfigResult.yodleePaymentTypes;
            verifyYodleePaymentTypeList(MTAG, yodleePaymentTypeProvider, yodleePaymentTypeResult);

            // verify TravelPointsConfig.
            TravelPointsConfigDAO travelPointsConfigProvider = dao.getTravelPointsConfig();
            TravelPointsConfig travelPointsConfigResult = userConfigResult.travelPointsConfig;
            verifyTravelPointsConfig(MTAG, travelPointsConfigProvider, travelPointsConfigResult);

            // verify Allowed Air Classes Of Service.
            List<String> airClassesOfServiceProvider = dao.getAllowedAirClassesOfService();
            List<String> airClassesOfServiceResult = userConfigResult.allowedAirClassServiceList;
            verifyAllowedAirClassesOfServiceResultList(MTAG, airClassesOfServiceProvider, airClassesOfServiceResult);

        }
    }

    private void verifyAttendeeColumnDefinitionList(String MTAG,
            List<AttendeeColumnDefinitionDAO> attendeeColumnDefinitionProvider,
            List<AttendeeColumnDefinition> attendeeColumnDefinitionResult) {
        if (attendeeColumnDefinitionProvider != null && attendeeColumnDefinitionResult != null) {
            HashMap<String, AttendeeColumnDefinitionDAO> currencyTypes = new HashMap<String, AttendeeColumnDefinitionDAO>();

            Assert.assertEquals(MTAG + ": attendeeColumnDefinition list size", attendeeColumnDefinitionResult.size(),
                    attendeeColumnDefinitionProvider.size());
            for (AttendeeColumnDefinitionDAO dao : attendeeColumnDefinitionProvider) {
                String id = dao.getId();
                currencyTypes.put(id, dao);
            }
            for (AttendeeColumnDefinition currencyResultItem : attendeeColumnDefinitionResult) {
                String id = currencyResultItem.id;
                boolean haveKey = currencyTypes.containsKey(id);

                Assert.assertTrue(haveKey);

                AttendeeColumnDefinitionDAO currencyProviderItem = currencyTypes.get(id);

                Assert.assertEquals(MTAG + ": attendeeColumnDefinition item", currencyResultItem.label,
                        currencyProviderItem.getLabel());

                Assert.assertEquals(MTAG + ": attendeeColumnDefinition item", currencyResultItem.accessType,
                        currencyProviderItem.getAccessType());

                Assert.assertEquals(MTAG + ": attendeeColumnDefinition item", currencyResultItem.ctrlType,
                        currencyProviderItem.getControlType());

                Assert.assertEquals(MTAG + ": attendeeColumnDefinition item", currencyResultItem.dataType,
                        currencyProviderItem.getDataType());

            }
        } else if (attendeeColumnDefinitionProvider == null && attendeeColumnDefinitionResult != null) {
            Assert.assertNull(MTAG + ": attendeeColumnDefinition list from provider is null",
                    attendeeColumnDefinitionProvider);
            Assert.assertNotNull(MTAG + ": attendeeColumnDefinition list from response is not null",
                    attendeeColumnDefinitionResult);
        } else if (attendeeColumnDefinitionProvider != null && attendeeColumnDefinitionResult == null) {
            Assert.assertNull(MTAG + ": attendeeColumnDefinition list from response is null",
                    attendeeColumnDefinitionResult);
            Assert.assertNotNull(MTAG + ": attendeeColumnDefinition list from provider is not null",
                    attendeeColumnDefinitionProvider);
        }

    }

    /**
     * Verify Attendee Type list.
     * 
     * @param MTAG
     *            verify TAG.
     * @param attendeeTypeProvider
     *            attendee type list from content provider.
     * @param attendeeTypeResult
     *            attendee type list from xml or MWS
     */
    private void verifyAttendeeTypeList(String MTAG, List<AttendeeTypeDAO> attendeeTypeProvider,
            List<AttendeeType> attendeeTypeResult) {
        if (attendeeTypeProvider != null && attendeeTypeResult != null) {
            HashMap<String, AttendeeTypeDAO> attendeeTypes = new HashMap<String, AttendeeTypeDAO>();
            Assert.assertEquals(MTAG + ": attendee type list size", attendeeTypeResult.size(),
                    attendeeTypeProvider.size());
            attendeeTypes.clear();
            for (AttendeeTypeDAO attendeeTypeDAO : attendeeTypeProvider) {
                String id = attendeeTypeDAO.getAtnTypeKey();
                attendeeTypes.put(id, attendeeTypeDAO);
            }
            for (AttendeeType atnTypeResultItem : attendeeTypeResult) {
                String id = atnTypeResultItem.atnTypeKey;
                boolean haveKey = attendeeTypes.containsKey(id);

                Assert.assertTrue(haveKey);

                AttendeeTypeDAO atnTypeProviderItem = attendeeTypes.get(id);

                Assert.assertEquals(MTAG + ": attendee type item", atnTypeResultItem.atnTypeCode,
                        atnTypeProviderItem.getAtnTypeCode());

                Assert.assertEquals(MTAG + ": attendee type item", atnTypeResultItem.atnTypeName,
                        atnTypeProviderItem.getAtnTypeName());

                Assert.assertEquals(MTAG + ": attendee type item", atnTypeResultItem.formKey,
                        atnTypeProviderItem.getFormKey());

                Boolean isExternal = null;
                if (atnTypeResultItem.isExternal != null) {
                    isExternal = Parse.safeParseBoolean(atnTypeResultItem.isExternal);
                }
                Assert.assertEquals(MTAG + ": attendee type item", isExternal, atnTypeProviderItem.getIsExternal());

                Boolean allowEditAtnCount = null;
                if (atnTypeResultItem.allowEditAtnCount != null) {
                    allowEditAtnCount = Parse.safeParseBoolean(atnTypeResultItem.allowEditAtnCount);
                }
                Assert.assertEquals(MTAG + ": attendee type item", allowEditAtnCount,
                        atnTypeProviderItem.getAllowEditAtnCount());

            }
        } else if (attendeeTypeProvider == null && attendeeTypeResult != null) {
            Assert.assertNull(MTAG + ": attendee type list from provider is null", attendeeTypeProvider);
            Assert.assertNotNull(MTAG + ": attendee type list from response is not null", attendeeTypeResult);
        } else if (attendeeTypeProvider != null && attendeeTypeResult == null) {
            Assert.assertNull(MTAG + ": attendee type list from response is null", attendeeTypeResult);
            Assert.assertNotNull(MTAG + ": attendee type list from provider is not null", attendeeTypeProvider);
        }
    }

    /**
     * Verify Car Type list.
     * 
     * @param MTAG
     *            verify TAG.
     * @param carTypeProvider
     *            attendee type list from content provider.
     * @param carTypeResult
     *            attendee type list from xml or MWS
     */
    private void verifyCarTypeList(String MTAG, List<CarTypeDAO> carTypeProvider, List<CarType> carTypeResult) {
        if (carTypeProvider != null && carTypeResult != null) {
            HashMap<String, CarTypeDAO> carTypes = new HashMap<String, CarTypeDAO>();

            Assert.assertEquals(MTAG + ": car type list size", carTypeResult.size(), carTypeProvider.size());
            carTypes.clear();
            for (CarTypeDAO carTypeDAO : carTypeProvider) {
                String id = carTypeDAO.getCode();
                carTypes.put(id, carTypeDAO);
            }
            for (CarType carTypeResultItem : carTypeResult) {
                String id = carTypeResultItem.code;
                boolean haveKey = carTypes.containsKey(id);

                Assert.assertTrue(haveKey);

                CarTypeDAO carTypeProviderItem = carTypes.get(id);

                Assert.assertEquals(MTAG + ": car type item", carTypeResultItem.description,
                        carTypeProviderItem.getDescription());

                Boolean isDefault = null;
                if (carTypeResultItem.isDefault != null) {
                    isDefault = Parse.safeParseBoolean(carTypeResultItem.isDefault);
                }
                Assert.assertEquals(MTAG + ": car type item", isDefault, carTypeProviderItem.getIsDefault());

            }
        } else if (carTypeProvider == null && carTypeResult != null) {
            Assert.assertNull(MTAG + ": car type list from provider is null", carTypeProvider);
            Assert.assertNotNull(MTAG + ": car type list from response is not null", carTypeResult);
        } else if (carTypeProvider != null && carTypeResult == null) {
            Assert.assertNull(MTAG + ": car type list from response is null", carTypeResult);
            Assert.assertNotNull(MTAG + ": car type list from provider is not null", carTypeProvider);
        }
    }

    private void verifyCurrencyList(String MTAG, List<CurrencyDAO> currencyProvider, List<Currency> currencyResult) {
        if (currencyProvider != null && currencyResult != null) {
            HashMap<String, CurrencyDAO> currencyTypes = new HashMap<String, CurrencyDAO>();

            Assert.assertEquals(MTAG + ": currency list size", currencyResult.size(), currencyProvider.size());
            currencyTypes.clear();
            for (CurrencyDAO currencyDAO : currencyProvider) {
                String id = currencyDAO.getCrnCode();
                currencyTypes.put(id, currencyDAO);
            }
            for (Currency currencyResultItem : currencyResult) {
                String id = currencyResultItem.crnCode;
                boolean haveKey = currencyTypes.containsKey(id);

                Assert.assertTrue(haveKey);

                CurrencyDAO currencyProviderItem = currencyTypes.get(id);

                Assert.assertEquals(MTAG + ": currency item", currencyResultItem.crnName,
                        currencyProviderItem.getCrnName());

                Assert.assertEquals(MTAG + ": currency item", currencyResultItem.decimalDigits, currencyProviderItem
                        .getDecimalDigits().intValue());

            }
        } else if (currencyProvider == null && currencyResult != null) {
            Assert.assertNull(MTAG + ": currency list from provider is null", currencyProvider);
            Assert.assertNotNull(MTAG + ": currency list from response is not null", currencyResult);
        } else if (currencyProvider != null && currencyResult == null) {
            Assert.assertNull(MTAG + ": currency list from response is null", currencyResult);
            Assert.assertNotNull(MTAG + ": currency list from provider is not null", currencyProvider);
        }

    }

    private void verifyExpenseConfirmationList(String MTAG, List<ExpenseConfirmationDAO> expenseConfirmationProvider,
            List<ExpenseConfirmation> expenseConfirmationResult) {
        if (expenseConfirmationProvider != null && expenseConfirmationResult != null) {
            HashMap<String, ExpenseConfirmationDAO> expConfs = new HashMap<String, ExpenseConfirmationDAO>();

            Assert.assertEquals(MTAG + ": expense confirmation list size", expenseConfirmationResult.size(),
                    expenseConfirmationProvider.size());
            expConfs.clear();
            for (ExpenseConfirmationDAO expConfDAO : expenseConfirmationProvider) {
                String id = expConfDAO.getKey();
                expConfs.put(id, expConfDAO);
            }
            for (ExpenseConfirmation expConfResultItem : expenseConfirmationResult) {
                String id = expConfResultItem.confirmationKey;
                boolean haveKey = expConfs.containsKey(id);

                Assert.assertTrue(haveKey);

                ExpenseConfirmationDAO expConfProviderItem = expConfs.get(id);

                Assert.assertEquals(MTAG + ": expense confirmation item", expConfResultItem.text,
                        expConfProviderItem.getText());

                Assert.assertEquals(MTAG + ": expense confirmation item", expConfResultItem.title,
                        expConfProviderItem.getTitle());

            }
        } else if (expenseConfirmationProvider == null && expenseConfirmationResult != null) {
            Assert.assertNull(MTAG + ": expense confirmation list from provider is null", expenseConfirmationProvider);
            Assert.assertNotNull(MTAG + ": expense confirmation list from response is not null",
                    expenseConfirmationResult);
        } else if (expenseConfirmationProvider != null && expenseConfirmationResult == null) {
            Assert.assertNull(MTAG + ": expense confirmation list from response is null", expenseConfirmationResult);
            Assert.assertNotNull(MTAG + ": expense confirmation list from provider is not null",
                    expenseConfirmationProvider);
        }

    }

    private void verifyPolicyList(String MTAG, List<PolicyDAO> policyProvider, List<ExpensePolicy> policyResult) {
        if (policyProvider != null && policyResult != null) {
            HashMap<String, PolicyDAO> currencyTypes = new HashMap<String, PolicyDAO>();

            Assert.assertEquals(MTAG + ": policy list size", policyResult.size(), policyProvider.size());
            currencyTypes.clear();
            for (PolicyDAO policyDAO : policyProvider) {
                String id = policyDAO.getKey();
                currencyTypes.put(id, policyDAO);
            }
            for (ExpensePolicy policyResultItem : policyResult) {
                String id = policyResultItem.polKey;
                boolean haveKey = currencyTypes.containsKey(id);

                Assert.assertTrue(haveKey);

                PolicyDAO currencyProviderItem = currencyTypes.get(id);

                Assert.assertEquals(MTAG + ": policy item", policyResultItem.approvalConfirmationKey,
                        currencyProviderItem.getApprovalConfirmationKey());

                Assert.assertEquals(MTAG + ": policy item", policyResultItem.submitConfirmationKey,
                        currencyProviderItem.getSubmitConfirmationKey());

            }
        } else if (policyProvider == null && policyResult != null) {
            Assert.assertNull(MTAG + ": policy list from provider is null", policyProvider);
            Assert.assertNotNull(MTAG + ": policy list from response is not null", policyResult);
        } else if (policyProvider != null && policyResult == null) {
            Assert.assertNull(MTAG + ": policy list from response is null", policyResult);
            Assert.assertNotNull(MTAG + ": policy list from provider is not null", policyProvider);
        }

    }

    private void verifyYodleePaymentTypeList(String MTAG, List<YodleePaymentTypeDAO> yodleePaymentTypeProvider,
            List<YodleePaymentType> yodleePaymentTypeResult) {
        if (yodleePaymentTypeProvider != null && yodleePaymentTypeResult != null) {
            HashMap<String, YodleePaymentTypeDAO> yodleePaymentTypes = new HashMap<String, YodleePaymentTypeDAO>();

            Assert.assertEquals(MTAG + ": yodlee payment type list size", yodleePaymentTypeResult.size(),
                    yodleePaymentTypeProvider.size());
            yodleePaymentTypes.clear();
            for (YodleePaymentTypeDAO yodleePaymentTypeDAO : yodleePaymentTypeProvider) {
                String id = yodleePaymentTypeDAO.getKey();
                yodleePaymentTypes.put(id, yodleePaymentTypeDAO);
            }
            for (YodleePaymentType yodleePaymentTypeResultItem : yodleePaymentTypeResult) {
                String id = yodleePaymentTypeResultItem.key;
                boolean haveKey = yodleePaymentTypes.containsKey(id);

                Assert.assertTrue(haveKey);

                YodleePaymentTypeDAO currencyProviderItem = yodleePaymentTypes.get(id);

                Assert.assertEquals(MTAG + ": yodlee payment type item", yodleePaymentTypeResultItem.text,
                        currencyProviderItem.getText());

            }
        } else if (yodleePaymentTypeProvider == null && yodleePaymentTypeResult != null) {
            Assert.assertNull(MTAG + ": yodlee payment type list from provider is null", yodleePaymentTypeProvider);
            Assert.assertNotNull(MTAG + ": yodlee payment type list from response is not null", yodleePaymentTypeResult);
        } else if (yodleePaymentTypeProvider != null && yodleePaymentTypeResult == null) {
            Assert.assertNull(MTAG + ": yodlee payment type list from response is null", yodleePaymentTypeResult);
            Assert.assertNotNull(MTAG + ": yodlee payment type list from provider is not null",
                    yodleePaymentTypeProvider);
        }

    }

    private void verifyTravelPointsConfig(String MTAG, TravelPointsConfigDAO travelPointsConfigProvider,
            TravelPointsConfig travelPointsConfigResult) {
        if (travelPointsConfigProvider != null && travelPointsConfigResult != null) {

            Assert.assertEquals(MTAG + ": travel points config", travelPointsConfigResult.airTravelPointsEnabled,
                    travelPointsConfigProvider.getAirTravelPointsEnabled());

            Assert.assertEquals(MTAG + ": travel points config", travelPointsConfigResult.hotelTravelPointsEnabled,
                    travelPointsConfigProvider.getHotelTravelPointsEnabled());

        } else if (travelPointsConfigProvider == null && travelPointsConfigResult != null) {
            Assert.assertNull(MTAG + ": travel points config from provider is null", travelPointsConfigProvider);
            Assert.assertNotNull(MTAG + ": travel points config from response is not null", travelPointsConfigResult);
        } else if (travelPointsConfigProvider != null && travelPointsConfigResult == null) {
            Assert.assertNull(MTAG + ": travel points config from response is null", travelPointsConfigResult);
            Assert.assertNotNull(MTAG + ": travel points config from provider is not null", travelPointsConfigProvider);
        }

    }

    private void verifyAllowedAirClassesOfServiceResultList(String MTAG, List<String> airClassesOfServiceProvider,
            List<String> airClassesOfServiceResult) {
        if (airClassesOfServiceProvider != null && airClassesOfServiceResult != null) {
            HashMap<String, String> airClassesOfServices = new HashMap<String, String>();

            Assert.assertEquals(MTAG + ": AirClassesOfService list size", airClassesOfServiceResult.size(),
                    airClassesOfServiceProvider.size());
            for (String air : airClassesOfServiceProvider) {
                airClassesOfServices.put(air, air);
            }
            for (String airItem : airClassesOfServiceResult) {
                boolean haveKey = airClassesOfServices.containsKey(airItem);

                Assert.assertTrue(haveKey);
            }
        } else if (airClassesOfServiceProvider == null && airClassesOfServiceResult != null) {
            Assert.assertNull(MTAG + ": AirClassesOfService list from provider is null", airClassesOfServiceProvider);
            Assert.assertNotNull(MTAG + ": AirClassesOfService list from response is not null",
                    airClassesOfServiceResult);
        } else if (airClassesOfServiceProvider != null && airClassesOfServiceResult == null) {
            Assert.assertNull(MTAG + ": AirClassesOfService list from response is null", airClassesOfServiceResult);
            Assert.assertNotNull(MTAG + ": AirClassesOfService list from provider is not null",
                    airClassesOfServiceProvider);
        }
    }

}
