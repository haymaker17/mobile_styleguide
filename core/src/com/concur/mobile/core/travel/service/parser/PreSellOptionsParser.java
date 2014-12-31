package com.concur.mobile.core.travel.service.parser;

import java.util.ArrayList;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.core.data.CreditCard;
import com.concur.mobile.core.travel.data.AffinityProgram;
import com.concur.mobile.core.travel.data.CancellationPolicy;
import com.concur.mobile.core.travel.data.PreSellOption;
import com.concur.mobile.core.travel.data.SellOptionInfo;
import com.concur.mobile.core.travel.rail.data.RailTicketDeliveryOption;
import com.concur.mobile.platform.util.Parse;

/**
 * 
 * @author RatanK
 * 
 */
public class PreSellOptionsParser implements Parser {

    public PreSellOption preSellOption = new PreSellOption();

    private ArrayList<CreditCard> creditCards;
    private CreditCard creditCard;
    private boolean inCreditCards;
    private boolean inCreditCardInfos;

    private ArrayList<RailTicketDeliveryOption> ticketDeliveryOptions;
    private RailTicketDeliveryOption ticketDeliveryOption;
    private boolean inTicketDeliveryOptions;

    private ArrayList<AffinityProgram> affinityPrograms;
    private AffinityProgram affinityProgram;
    private boolean inTravelPrograms;

    private ArrayList<SellOptionInfo> sellOptionInfos;
    private SellOptionInfo sellOptionInfo;
    private boolean inSellOptionInfos;
    private boolean inSellOptionInfo;

    private boolean inCancellationPolicy;
    private ArrayList<String> cancellationPolicyStmts;
    private CancellationPolicy cancellationPolicy;

    @Override
    public void startTag(String tag) {
        if (tag.equals("CreditCards")) {
            creditCards = new ArrayList<CreditCard>();
            inCreditCards = true;
        } else if (tag.equals("CreditCardInfo")) {
            inCreditCardInfos = true;
            creditCard = new CreditCard();
        } else if (tag.equals("DeliveryOptions")) {
            ticketDeliveryOptions = new ArrayList<RailTicketDeliveryOption>();
        } else if (tag.equals("TicketDeliveryOption")) {
            inTicketDeliveryOptions = true;
            ticketDeliveryOption = new RailTicketDeliveryOption();
        } else if (tag.equals("TravelPrograms")) {
            affinityPrograms = new ArrayList<AffinityProgram>();
        } else if (tag.equals("TravelProgramChoice")) {
            inTravelPrograms = true;
            affinityProgram = new AffinityProgram();
        } else if (tag.equals("FlightOptions")) {
            inSellOptionInfos = true;
            sellOptionInfos = new ArrayList<SellOptionInfo>();
            // tcfs = new ArrayList<TravelCustomField>();
        } else if (inSellOptionInfos) {
            if (tag.equals("FlightOptionInfo")) {
                inSellOptionInfo = true;
                sellOptionInfo = new SellOptionInfo();
            } else {
                sellOptionInfo.startTag(tag);
            }
        } else if (tag.equals("HotelRateCancellationPolicy")) {
            inCancellationPolicy = true;
            cancellationPolicy = new CancellationPolicy();
            cancellationPolicyStmts = new ArrayList<String>();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (inCreditCards && tag.equals("CVVNumberRequired")) {
            preSellOption.setCvvNumberRequired(Parse.safeParseBoolean(text));
        } else if (inCreditCardInfos) {
            creditCard.handleElement(tag, text);
        } else if (inTicketDeliveryOptions) {
            ticketDeliveryOption.handleElement(tag, text);
        } else if (inTravelPrograms) {
            affinityProgram.handleElement(tag, text);
        } else if (inSellOptionInfo) {
            sellOptionInfo.handleElement(tag, text);
        } else if (inCancellationPolicy) {
            if (tag.equalsIgnoreCase("string")) {
                cancellationPolicyStmts.add(text);
            }
        }
    }

    @Override
    public void endTag(String tag) {
        if (tag.equals("CreditCardInfo")) {
            inCreditCardInfos = false;
            creditCards.add(creditCard);
        } else if (tag.equals("TicketDeliveryOption")) {
            inTicketDeliveryOptions = false;
            ticketDeliveryOptions.add(ticketDeliveryOption);
        } else if (tag.equals("TravelProgramChoice")) {
            inTravelPrograms = false;
            affinityPrograms.add(affinityProgram);
        } else if (tag.equals("TravelPrograms")) {
            preSellOption.setAffinityPrograms(affinityPrograms);
        } else if (tag.equals("DeliveryOptions")) {
            preSellOption.setTicketDeliveryOptions(ticketDeliveryOptions);
        } else if (tag.equals("CreditCards")) {
            preSellOption.setCreditCards(creditCards);
        } else if (inSellOptionInfo) {
            if (tag.equals("FlightOptionInfo")) {
                sellOptionInfos.add(sellOptionInfo);
                inSellOptionInfo = false;
            } else {
                sellOptionInfo.endTag(tag);
            }
        } else if (tag.equals("FlightOptions")) {
            inSellOptionInfos = false;
            preSellOption.setSellOptionInfos(sellOptionInfos);
            // preSellOption.setSellOptionFields(getSellOptionFields());
        } else if (inCancellationPolicy) {
            if (tag.equals("HotelRateCancellationPolicy")) {
                inCancellationPolicy = false;
                cancellationPolicy.setStatements(cancellationPolicyStmts);
                preSellOption.setCancellationPolicy(cancellationPolicy);
            }
        }
    }
}
