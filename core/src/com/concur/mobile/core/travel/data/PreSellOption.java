package com.concur.mobile.core.travel.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.core.data.CreditCard;
import com.concur.mobile.core.travel.rail.data.RailTicketDeliveryOption;

/**
 * 
 * @author RatanK
 * 
 */
public class PreSellOption implements Serializable {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -7176964710578844911L;

    // credit cards
    private ArrayList<CreditCard> creditCards;

    // ticket delivery options
    private ArrayList<RailTicketDeliveryOption> ticketDeliveryOptions;

    // travel programs a.k.a infinity program
    private ArrayList<AffinityProgram> affinityPrograms;

    private boolean cvvNumberRequired;

    private List<SellOptionInfo> sellOptionInfos;

    private List<SellOptionField> sellOptionFields;

    private CancellationPolicy cancellationPolicy;

    public ArrayList<CreditCard> getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(ArrayList<CreditCard> creditCards) {
        this.creditCards = creditCards;
    }

    public ArrayList<RailTicketDeliveryOption> getTicketDeliveryOptions() {
        return ticketDeliveryOptions;
    }

    public void setTicketDeliveryOptions(ArrayList<RailTicketDeliveryOption> ticketDeliveryOptions) {
        this.ticketDeliveryOptions = ticketDeliveryOptions;
    }

    public ArrayList<AffinityProgram> getAffinityPrograms() {
        return affinityPrograms;
    }

    public void setAffinityPrograms(ArrayList<AffinityProgram> affinityPrograms) {
        this.affinityPrograms = affinityPrograms;
    }

    /**
     * returns the default credit card from the list of credit cards
     * 
     * @return
     */
    public CreditCard getDefualtCreditCard() {
        CreditCard defaultCC = null;
        if (getCreditCards() != null) {
            for (CreditCard cc : getCreditCards()) {
                if (cc.defaultCard) {
                    defaultCC = cc;
                    break;
                }
            }
        }
        return defaultCC;
    }

    public boolean isCvvNumberRequired() {
        return cvvNumberRequired;
    }

    public void setCvvNumberRequired(boolean cvvNumberRequired) {
        this.cvvNumberRequired = cvvNumberRequired;
    }

    public List<SellOptionInfo> getSellOptionInfos() {
        return sellOptionInfos;
    }

    public void setSellOptionInfos(List<SellOptionInfo> sellOptionInfos) {
        this.sellOptionInfos = sellOptionInfos;
    }

    public List<SellOptionField> getSellOptionFields() {
        if (sellOptionFields != null)
            return sellOptionFields;

        // retrieve the option item from each option group under each flight option info
        sellOptionFields = new ArrayList<SellOptionField>();
        if (sellOptionInfos != null && sellOptionInfos.size() > 0) {
            for (SellOptionInfo sellOptInfo : sellOptionInfos) {
                sellOptionFields.addAll(sellOptInfo.getSellOptionFields());
            }
        }
        return sellOptionFields;
    }

    public void setSellOptionFields(List<SellOptionField> sellOptionFields) {
        this.sellOptionFields = sellOptionFields;
    }

    public CancellationPolicy getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(CancellationPolicy cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

}
