package com.concur.mobile.core.travel.air.data;

import java.util.HashMap;
import java.util.Map;

public class AirDictionaries {

    public static class Pair {

        public String key;
        public String value;
    }

    public static HashMap<String, String> airportCityCodeMap;
    public static HashMap<String, String> airportCodeMap;
    public static HashMap<String, String> equipmentCodeMap;
    public static HashMap<String, String> preferenceRankMap;
    public static HashMap<String, String> vendorCodeMap;

    static {
        airportCityCodeMap = new HashMap<String, String>();
        airportCodeMap = new HashMap<String, String>();
        equipmentCodeMap = new HashMap<String, String>();
        preferenceRankMap = new HashMap<String, String>();
        vendorCodeMap = new HashMap<String, String>();
    }

    public AirDictionaries() {
    }

    public static void addPairToMap(Map<String, String> map, Pair pair) {
        map.put(pair.key, pair.value);
    }
}
