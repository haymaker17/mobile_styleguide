package com.concur.mobile.platform.travel.search.hotel;

import com.concur.mobile.platform.travel.booking.PreSellOption;

/**
 * Hotel specific PreSellOption
 * 
 * @author RatanK
 * 
 */
public class HotelPreSellOption extends PreSellOption {

    public String[] hotelCancellationPolicy;
    public String[] hotelRateChangesOverStay;
    public TotalPrice totalPrice;

}
