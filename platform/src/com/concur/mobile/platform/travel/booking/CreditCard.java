package com.concur.mobile.platform.travel.booking;

import com.google.gson.annotations.SerializedName;

/**
 * Credit card
 * 
 * @author RatanK
 * 
 */
public class CreditCard {

    public String id;
    public String type;
    public String lastFour;
    public String maskedNumber;
    public String name;

    @SerializedName("default")
    public boolean defaultCard;

}
