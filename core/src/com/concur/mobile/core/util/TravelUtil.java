package com.concur.mobile.core.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.SystemConfig;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.travel.data.ReasonCode;

import java.util.ArrayList;

/**
 * Created by tejoa on 08/09/2015.
 * TravelUtil for code re-use.
 */
public class TravelUtil {

    /**
     * Add Voice search params to intent
     *
     * @param i
     */
    public static void addSearchIntent(Intent i) {
        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        i.putExtra("currentLocation", core.getCurrentLocation());
        i.putExtra("currentAddress", core.getCurrentAddress());
        UserConfig userConfig = core.getUserConfig();
        String distanceUnit = userConfig != null ? userConfig.distanceUnitPreference : null;
        if (distanceUnit == null) {
            distanceUnit = "M";
        } else {
            distanceUnit = (distanceUnit.equalsIgnoreCase("Miles") ? "M" : "K");
        }
        i.putExtra(com.concur.mobile.platform.ui.travel.util.Const.EXTRA_TRAVEL_HOTEL_SEARCH_DISTANCE_UNIT_ID, distanceUnit);
        addViolationsReasons(i);

    }

    /**
     * Add Violation params to intent
     *
     * @param intent
     */
    public static void addViolationsReasons(Intent intent) {
        // get the hotel violation reasons from the SystemConfig and pass it on to the activities.
        // since the platform systemconfig request is not being invoked by the application, we cannot use the getHotelReasons from
        // the platform. Hence passing in to the next activities. Same with the ruleViolationExplanationRequired

        ConcurCore core = (ConcurCore) ConcurCore.getContext();
        SystemConfig sysConfig = core.getSystemConfig();
        ArrayList<ReasonCode> reasonCodesCore = (sysConfig != null
                && sysConfig.getHotelReasons() != null) ? sysConfig.getHotelReasons() : null;
        if (reasonCodesCore != null) {
            ArrayList<String[]> violationReasons = new ArrayList<String[]>(reasonCodesCore.size());
            for (com.concur.mobile.core.travel.data.ReasonCode reasonCode : reasonCodesCore) {
                violationReasons.add(new String[]{reasonCode.id, reasonCode.description});
            }
            intent.putExtra("violationReasons", violationReasons);
        }
        // Check whether SystemConfig stipulates that 'violation justification' is required.
        if (sysConfig != null && sysConfig.getRuleViolationExplanationRequired() != null && sysConfig
                .getRuleViolationExplanationRequired()) {
            intent.putExtra("ruleViolationExplanationRequired", true);
        } else {
            intent.putExtra("ruleViolationExplanationRequired", false);
        }
    }

    /**
     * Verify location service is enabled
     *
     * @param context
     * @return
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
}
