package com.concur.mobile.platform.request.util;

import android.util.Log;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.request.dto.RequestCommentDTO;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.location.Location;
import com.concur.mobile.platform.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author olivierb
 */
public class RequestParser {

    private static final String CLS_TAG = "RequestParser";

    /*
     * Reminder - GSON parser: Each time gson sees a {}, it creates a Map Each time gson sees a '', it creates a String Each time
     * gson sees a number, it creates a Double Each time gson sees a [], it creates an ArrayList
     */

    /**
     * Enum which describes permitted actions usable on a given TR object (received from ws)
     */
    public enum PermittedAction {
        SAVE("save"),
        SUBMIT("submit");

        private String action;

        PermittedAction(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }

    /**
     * Custom objects only used in parse processes
     * *************************************************************
     */
    private class ActionResponse {

        @SerializedName("ID")
        private String id = null;
        @SerializedName("URI")
        private String uri = null;

        public String getId() {
            return id;
        }

        public String getUri() {
            return uri;
        }
    }

    private class UserPermission {

        @SerializedName("Action")
        private String action;
        @SerializedName("Method")
        private String method;

        public String getAction() {
            return action;
        }

        public String getMethod() {
            return method;
        }
    }

    private class Link {

        @SerializedName("Links")
        private List<UserPermission> permissions;

        public List<UserPermission> getPermissions() {
            return permissions;
        }
    }

    private class TRDetailResponse {

        // --- TR object properties
        @SerializedName("ApprovalStatusName")
        private String approvalStatus;
        @SerializedName("ApprovalStatusCode")
        private String approvalStatusCode;
        @SerializedName("TotalApprovedAmount")
        private Double total;
        // --- TR childs + permissions properties
        @SerializedName("UserPermissions")
        private Link link;
        @SerializedName("SegmentsEntries")
        private List<RequestEntryDTO> entryList;
        @SerializedName("Comments")
        private List<RequestCommentDTO> commentList;
        // --- required to post/put
        @SerializedName(("PolicyID"))
        private String policyId;

        public Link getLink() {
            return link;
        }

        public List<RequestEntryDTO> getEntryList() {
            return entryList;
        }

        public List<RequestCommentDTO> getCommentList() {
            return commentList;
        }

        public String getApprovalStatus() {
            return approvalStatus;
        }

        public String getApprovalStatusCode() {
            return approvalStatusCode;
        }

        public Double getTotal() {
            return total;
        }

        public String getPolicyId() {
            return policyId;
        }
    }

    /**
     * ************************************************************
     */

    private Date parseDate(String baseStr, SimpleDateFormat sdf) {
        if (baseStr != null) {
            try {
                return sdf.parse(baseStr);
            } catch (ParseException e) {
                // do nothing - either data is corrupted or format isn't
                // recognized, which is quite the same.
            }
        }
        return null;
    }

    public List<RequestDTO> parseTRListResponse(String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Double.class, new DoubleDeserializer());
        builder.registerTypeAdapter(Date.class, new DateDeserializer(Parse.LONG_YEAR_MONTH_DAY));
        Log.d(CLS_TAG, "parseTRListResponse :: starting parse");
        final Gson gson = builder.create();
        final GsonListContainer<RequestDTO> requestList = gson
                .fromJson(jsonRes, new TypeToken<GsonListContainer<RequestDTO>>() {}.getType());
        return requestList.getList();
    }

    @SuppressWarnings("rawtypes")
    public void parseTRDetailResponse(RequestDTO tr, String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Boolean.class, new BooleanDeserializer());
        builder.registerTypeAdapter(Integer.class, new IntegerDeserializer());
        builder.registerTypeAdapter(Double.class, new DoubleDeserializer());
        builder.registerTypeAdapter(Date.class, new DateDeserializer(Parse.LONG_YEAR_MONTH_DAY));

        final Gson gson = builder.create();
        Log.d(CLS_TAG, "parseTRDetailResponse :: starting parse");
        final TRDetailResponse connectTR = gson.fromJson(jsonRes, TRDetailResponse.class);

        // --- Apply properties on TR object
        tr.setTotal(connectTR.getTotal());
        tr.setApprovalStatusCode(connectTR.getApprovalStatusCode());
        tr.setApprovalStatus(connectTR.getApprovalStatus());
        tr.setPolicyId(connectTR.getPolicyId());

        // --- custom processing : permissions
        final List<String> actionsList = new ArrayList<String>();
        for (UserPermission up : connectTR.getLink().getPermissions()) {
            actionsList.add(up.getAction());
        }
        tr.setListPermittedActions(actionsList);

        // --- custom processing : last comment
        for (RequestCommentDTO com : connectTR.getCommentList()) {
            if (com.getIsLatest()) {
                tr.setLastComment(com.getValue());
                break;
            }
        }

        // --- generating map
        final Map<String, RequestEntryDTO> entryMap = new HashMap<String, RequestEntryDTO>();
        for (RequestEntryDTO entry : connectTR.getEntryList()) {
            // MOVED ON ENTRY BY LAST WS MODIFICATION
            entryMap.put(entry.getId(), entry);
        }
        tr.setEntriesMap(entryMap);
    }

    /**
     * Parse jsonRes content with Gson into a Form with n Fields
     *
     * @param jsonRes the json string
     */
    public ConnectForm parseFormFieldsResponse(String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Boolean.class, new BooleanDeserializer());
        builder.registerTypeAdapter(Integer.class, new IntegerDeserializer());
        builder.registerTypeAdapter(IFormField.AccessType.class,
                new EnumDeserializer<IFormField.AccessType>(IFormField.AccessType.class,
                        EnumDeserializer.EnumParsingType.STRING_VALUE));
        builder.registerTypeAdapter(IFormField.ControlType.class,
                new EnumDeserializer<IFormField.ControlType>(IFormField.ControlType.class,
                        EnumDeserializer.EnumParsingType.STRING_VALUE));
        builder.registerTypeAdapter(IFormField.DataType.class,
                new EnumDeserializer<IFormField.DataType>(IFormField.DataType.class,
                        EnumDeserializer.EnumParsingType.STRING_VALUE));
        final Gson gson = builder.create();
        Log.d(CLS_TAG, "parseFormFieldsResponse :: starting parse");
        return gson.fromJson(jsonRes, ConnectForm.class);
    }

    /**
     * Parse jsonRes content with Gson into an ActionResponse and return the ID field
     *
     * @param jsonRes the json string
     * @return related ID
     */
    public static String parseActionResponse(String jsonRes) {
        final Gson gson = new GsonBuilder().create();
        Log.d(CLS_TAG, "parseActionResponse :: starting parse");
        final ActionResponse resp = gson.fromJson(jsonRes, ActionResponse.class);
        return resp.getId();
    }

    /**
     * Parse jsonRes content with Gson into a RequestGroupConfigurationsContainer with n RequestGroupConfiguration
     *
     * @param jsonRes the json string
     */
    public List<RequestGroupConfiguration> parseRequestGroupConfigurationsResponse(String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Boolean.class, new BooleanDeserializer());
        builder.registerTypeAdapter(Integer.class, new IntegerDeserializer());

        final Gson gson = builder.create();
        Log.d(CLS_TAG, "RequestGroupConfiguration :: starting parse");
        final GsonListContainer<RequestGroupConfiguration> clc = gson
                .fromJson(jsonRes, new TypeToken<GsonListContainer<RequestGroupConfiguration>>() {}.getType());
        return clc.getList();
    }

    /**
     * Parse jsonRes content with Gson into a list of Location and return it
     *
     * @param jsonRes the json string
     * @return related ID
     */
    public static List<Location> parseLocations(String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        Log.d(CLS_TAG, "parseLocations :: starting parse");

        final Gson gson = builder.create();
        final GsonListContainer<Location> clc = gson
                .fromJson(jsonRes, new TypeToken<GsonListContainer<Location>>() {}.getType());
        return clc.getList();
    }

    public static Location parseLocation(String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        Log.d(CLS_TAG, "parseLocation :: starting parse");

        final Gson gson = builder.create();
        final Location clc = gson.fromJson(jsonRes, new TypeToken<Location>() {}.getType());
        return clc;
    }

    /*********************
     * toJson methods
     ********************/

    /**
     * @param tr travel request object
     * @return the json string representation
     */
    public static String toJson(RequestDTO tr) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Double.class, new DoubleDeserializer());
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        Log.d(CLS_TAG, "toJson[TR] :: starting parse");
        final Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(tr);
    }

    /**
     * @param requestEntryDTO travel request entry object
     * @return the json string representation
     */
    public static String toJson(RequestEntryDTO requestEntryDTO) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Double.class, new DoubleDeserializer());
        builder.registerTypeAdapter(Date.class, new DateDeserializer(Parse.LONG_YEAR_MONTH_DAY));
        builder.registerTypeAdapter(RequestEntryDTO.TripType.class,
                new EnumDeserializer<RequestEntryDTO.TripType>(RequestEntryDTO.TripType.class,
                        EnumDeserializer.EnumParsingType.STRING_VALUE));
        Log.d(CLS_TAG, "toJson[Entry] :: starting parse");
        final Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(requestEntryDTO);
    }

    /**
     * @param location location object
     * @return the json string representation
     */
    public static String toJson(Location location) {
        final GsonBuilder builder = new GsonBuilder();
        Log.d(CLS_TAG, "toJson[Location] :: starting parse");

        final Gson gson = builder.create();
        return gson.toJson(location);
    }
}
