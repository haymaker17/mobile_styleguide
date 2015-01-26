package com.concur.mobile.platform.travel.booking;

import java.util.List;

/**
 * Generic PreSellOption for travel
 * 
 * @author RatanK
 * 
 */
public class PreSellOption {

    // credit cards
    public List<CreditCard> creditCards;
    public URLInfo bookingURL;

    /**
     * returns the default credit card from the list of credit cards
     * 
     * @return
     */
    public CreditCard getDefualtCreditCard() {
        CreditCard defaultCC = null;
        if (creditCards != null) {
            for (CreditCard cc : creditCards) {
                if (cc.defaultCard) {
                    defaultCC = cc;
                    break;
                }
            }
        }
        return defaultCC;
    }
}
