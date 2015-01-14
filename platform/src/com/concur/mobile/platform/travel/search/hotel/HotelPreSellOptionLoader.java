package com.concur.mobile.platform.travel.search.hotel;

import android.content.Context;

import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.travel.booking.PreSellOptionLoader;
import com.google.gson.reflect.TypeToken;

/**
 * Async task loader class for retrieving the hotel pre-sell options
 * 
 * @author RatanK
 * 
 */
public class HotelPreSellOptionLoader extends PreSellOptionLoader<HotelPreSellOption> {

    public HotelPreSellOptionLoader(Context context, String hotelPreSellOptionURL) {
        super(context, new TypeToken<MWSResponse<HotelPreSellOption>>() {}, hotelPreSellOptionURL);
    }
}
