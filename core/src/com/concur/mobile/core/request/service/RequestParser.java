package com.concur.mobile.core.request.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

import com.concur.mobile.core.request.util.RequestParsingHelper;
import com.concur.mobile.platform.common.formfield.ConnectForm;
import com.concur.mobile.platform.common.formfield.IFormField;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.dto.RequestSegmentDTO;
import com.concur.mobile.platform.request.dto.RequestCommentDTO;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfigurationsContainer;
import com.concur.mobile.platform.util.BooleanDeserializer;
import com.concur.mobile.platform.util.EnumDeserializer;
import com.concur.mobile.platform.util.IntegerDeserializer;
import com.concur.mobile.platform.util.Parse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author olivierb
 * 
 */
public class RequestParser {

    /*
     * Reminder - GSON parser: Each time gson sees a {}, it creates a Map Each time gson sees a '', it creates a String Each time
     * gson sees a number, it creates a Double Each time gson sees a [], it creates an ArrayList
     */

    // REQUEST LIST
    public final static String SERVER_DATE_FORMAT = "yyyy-dd-MM";
    private static final String LIST_ARRAY_KEY = "Items";// "RequestsList";
    private static final String LIST_NAME_KEY = "Name";
    private static final String LIST_ID_KEY = "RequestID";
    private static final String LIST_PURPOSE_KEY = "Purpose";
    private static final String LIST_CURRENCY_KEY = "CurrencyCode";
    private static final String LIST_EPLOYEE_NAME_KEY = "EmployeeName";
    private static final String LIST_HEADER_FORM_ID = "HeaderFormID";
    private static final String LIST_APPROVAL_STATUS_KEY = "ApprovalStatusName";
    private static final String LIST_APPROVAL_STATUS_CODE = "ApprovalStatusCode";
    private static final String LIST_TOTAL_KEY = "TotalApprovedAmount";
    private static final String LIST_START_DATE_KEY = "StartDate";
    private static final String LIST_END_DATE_KEY = "EndDate";
    private static final String LIST_REQUEST_DATE_KEY = "CreationDate";
    private static final String LIST_LAST_COMMENT_KEY = "LastComment";
    private static final String LIST_USER_LOGIN_ID_KEY = "UserLoginID";
    private static final String LIST_APPROVER_LOGIN_ID_KEY = "ApproverLoginID";
    private static final String LIST_DETAILS_URL_KEY = "RequestDetailsUrl";
    private static final String LIST_SEGMENT_LIST = "SegmentTypes";

    // REQUEST DETAIL
    private static final String RES_DETAILS_CURRENCY_KEY = "CurrencyCode";
    private static final String RES_DETAILS_ENTRIES_LIST = "Entries";
    private static final String RES_DETAILS_LIST_KEY = "Segments";
    private static final String RES_DETAILS_EXCEPTION_LIST = "Exceptions";// TODO @See exception story
    private static final String RES_DETAILS_APPROVAL_STATUS_CODE = "ApprovalStatusCode";
    private static final String RES_DETAILS_USER_PERMISSIONS = "UserPermissions";
    private static final String RES_DETAILS_LINKS = "Links";
    private static final String RES_DETAILS_ACTION = "Action";
    private static final String RES_DETAILS_URL = "Url";

    private static final String RES_DETAILS_COMMENTS = "Comments";
    private static final String RES_DETAILS_COMMENT_VALUE = "Value";
    private static final String RES_DETAILS_COMMENT_FIRSTNAME = "AuthorFirstName";
    private static final String RES_DETAILS_COMMENT_LASTNAME = "AuthorLastName";
    private static final String RES_DETAILS_COMMENT_DATE = "CommentDateTime"; //ex : "2015-01-12T09:57:49"
    private static final String RES_DETAILS_COMMENT_ISLATEST = "IsLatest";

    // ENTRY
    private static final String RES_ENTRY_CURRENCY_CODE_KEY = "ForeignCurrencyCode";
    private static final String RES_ENTRY_FOREIGN_AMOUNT_KEY = "ForeignAmount";


    // SEGMENT
    private static final String RES_SEGMENT_TYPE_KEY = "SegmentType";
    private static final String RES_SEGMENT_CURRENCY_NAME_KEY = "ForeignCurrencyName";
    private static final String RES_SEGMENT_CURRENCY_CODE_KEY = "ForeignCurrencyCode";
    private static final String RES_SEGMENT_FOREIGN_AMOUNT_KEY = "ForeignAmount";
    private static final String RES_SEGMENT_DEPARTURE_DATE_KEY = "DepartureDate";
    private static final String RES_SEGMENT_ARRIVAL_DATE_KEY = "ArrivalDate";
    private static final String RES_SEGMENT_FROM_LOCATION_NAME_KEY = "FromLocationName";
    private static final String RES_SEGMENT_TO_LOCATION_NAME_KEY = "ToLocationName";
    private static final String RES_SEGMENT_EXCEPTION_LIST = "Exceptions";
    private static final String RES_SEGMENT_FORM_ID = "SegmentFormID";

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

    /**
     * @param jsonRes the string result of the ws call
     * @return a list of RequestDTO
     */
    @SuppressWarnings("rawtypes")
    public List<RequestDTO> parseTRListResponse(String jsonRes) {
        final List<RequestDTO> values = new ArrayList<RequestDTO>();
        final SimpleDateFormat sdf = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.US);

        final List trList = (List) new Gson().fromJson(jsonRes, Map.class).get(LIST_ARRAY_KEY);
        final int listSize = trList.size();
        for (int i = 0; i < listSize; i++) {
            final Map res = (Map) trList.get(i);
            final RequestDTO tr = new RequestDTO();
            tr.setId(RequestParsingHelper.stringSafeParse(res, LIST_ID_KEY));
            tr.setName(RequestParsingHelper.stringSafeParse(res, LIST_NAME_KEY));
            tr.setApprovalStatus(RequestParsingHelper.stringSafeParse(res, LIST_APPROVAL_STATUS_KEY));
            tr.setApprovalStatusCode(RequestParsingHelper.stringSafeParse(res, LIST_APPROVAL_STATUS_CODE));
            tr.setApproverLoginId(RequestParsingHelper.stringSafeParse(res, LIST_APPROVER_LOGIN_ID_KEY));
            tr.setCurrency(RequestParsingHelper.stringSafeParse(res, LIST_CURRENCY_KEY));
            tr.setDetailsUrl(RequestParsingHelper.stringSafeParse(res, LIST_DETAILS_URL_KEY));
            tr.setEmployeeName(RequestParsingHelper.stringSafeParse(res, LIST_EPLOYEE_NAME_KEY));
            tr.setLastComment(RequestParsingHelper.stringSafeParse(res, LIST_LAST_COMMENT_KEY));
            tr.setStartDate(parseDate(RequestParsingHelper.stringSafeParse(res, LIST_START_DATE_KEY), sdf));
            System.out.println("########## " + RequestParsingHelper.stringSafeParse(res, LIST_START_DATE_KEY) + " " + RequestParsingHelper.stringSafeParse(res, LIST_END_DATE_KEY));
            tr.setEndDate(parseDate(RequestParsingHelper.stringSafeParse(res, LIST_END_DATE_KEY), sdf));
            tr.setRequestDate(parseDate(RequestParsingHelper.stringSafeParse(res, LIST_REQUEST_DATE_KEY), sdf));
            tr.setPurpose(RequestParsingHelper.stringSafeParse(res, LIST_PURPOSE_KEY));
            tr.setTotal(Parse.safeParseDouble(RequestParsingHelper.stringSafeParse(res, LIST_TOTAL_KEY)));
            tr.setHeaderFormId(RequestParsingHelper.stringSafeParse(res, LIST_HEADER_FORM_ID));
            tr.setUserLoginId(RequestParsingHelper.stringSafeParse(res, LIST_USER_LOGIN_ID_KEY));
            tr.setSegmentListString(RequestParsingHelper.stringSafeParse(res, LIST_SEGMENT_LIST));
            values.add(tr);
        }

        return values;
    }

    @SuppressWarnings("rawtypes")
    public void parseTRDetailsResponse(RequestDTO tr, String jsonRes) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        final Map trList = new Gson().fromJson(jsonRes, Map.class);
        tr.setId(RequestParsingHelper.stringSafeParse(trList, LIST_ID_KEY));
        tr.setName(RequestParsingHelper.stringSafeParse(trList, LIST_NAME_KEY));
        tr.setCurrency(RequestParsingHelper.stringSafeParse(trList, RES_DETAILS_CURRENCY_KEY));
        tr.setStartDate(parseDate(RequestParsingHelper.stringSafeParse(trList, LIST_START_DATE_KEY), sdf));
        tr.setEndDate(parseDate(RequestParsingHelper.stringSafeParse(trList, LIST_END_DATE_KEY), sdf));
        tr.setTotal(Parse.safeParseDouble(RequestParsingHelper.stringSafeParse(trList, LIST_TOTAL_KEY)));
    }

    @SuppressWarnings("rawtypes")
    public void parseTRDetailResponse(RequestDTO tr, String jsonRes) {

        // GENERAL
        final Map requestDetail = new Gson().fromJson(jsonRes, Map.class);
        // --- permitted Actions
        tr.setListPermittedActions(new ArrayList<String>());

        final Map userPermissions = (Map) requestDetail.get(RES_DETAILS_USER_PERMISSIONS);
        final List links = (List) userPermissions.get(RES_DETAILS_LINKS);
        for (int i = 0; i < links.size(); i++) {
            final Map jsonEntry = (Map) links.get(i);
            tr.getListPermittedActions().add(RequestParsingHelper.stringSafeParse(jsonEntry, RES_DETAILS_ACTION));
        }

        //COMMENTS
        final List commentsList = (List) requestDetail.get(RES_DETAILS_COMMENTS);
        for (int j = 0; j < commentsList.size(); j++) {
            final Map jsonEntry = (Map) commentsList.get(j);
            final RequestCommentDTO Comment = new RequestCommentDTO();

            Comment.setValue(RequestParsingHelper.stringSafeParse(jsonEntry, RES_DETAILS_COMMENT_VALUE));
            Comment.setAuthorFirstName(RequestParsingHelper.stringSafeParse(jsonEntry, RES_DETAILS_COMMENT_FIRSTNAME));
            Comment.setAuthorLastName(RequestParsingHelper.stringSafeParse(jsonEntry, RES_DETAILS_COMMENT_LASTNAME));
            Comment.setIsLatest(RequestParsingHelper.stringSafeParse(jsonEntry, RES_DETAILS_COMMENT_ISLATEST).equals("false")? false : true);

            if(Comment.getIsLatest()){
                tr.setLastComment(Comment.getValue());
            }
        }

        // SEGMENTS
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        final List<RequestEntryDTO> res = new ArrayList<RequestEntryDTO>();
        final List trList = (List) requestDetail.get(RES_DETAILS_ENTRIES_LIST);
        for (int j = 0; j < trList.size(); j++) {
            // --- processing entries
            final Map jsonEntry = (Map) trList.get(j);
            final RequestEntryDTO entry = new RequestEntryDTO();

            entry.setForeignCurrencyCode(RequestParsingHelper.stringSafeParse(jsonEntry, RES_ENTRY_CURRENCY_CODE_KEY));
            entry.setForeignAmount(Parse.safeParseDouble(RequestParsingHelper.stringSafeParse(jsonEntry,
                    RES_ENTRY_FOREIGN_AMOUNT_KEY)));
            entry.setApprovalStatusCode((RequestParsingHelper.stringSafeParse(jsonEntry,
                    RES_DETAILS_APPROVAL_STATUS_CODE)));

            // --- processing segments
            final List segmentsList = (List) jsonEntry.get(RES_DETAILS_LIST_KEY);
            final int slLength = segmentsList.size();
            for (int i = 0; i < slLength; i++) {
                // mapping to dto
                final Map jsonSegment = (Map) segmentsList.get(i);
                final RequestSegmentDTO trs = new RequestSegmentDTO();
                trs.setSegmentType(RequestParsingHelper.stringSafeParse(jsonSegment, RES_SEGMENT_TYPE_KEY));
                // TODO : move segmenttype within webservice on entry
                if (i == 0) {
                    entry.setSegmentType(trs.getSegmentType());
                }
                trs.setForeignCurrencyName(RequestParsingHelper.stringSafeParse(jsonSegment,
                        RES_SEGMENT_CURRENCY_NAME_KEY));
                trs.setForeignCurrencyCode(RequestParsingHelper.stringSafeParse(jsonSegment,
                        RES_SEGMENT_CURRENCY_CODE_KEY));
                trs.setForeignAmount(Parse.safeParseDouble(RequestParsingHelper.stringSafeParse(jsonSegment,
                        RES_SEGMENT_FOREIGN_AMOUNT_KEY)));
                trs.setSegmentFormId(RequestParsingHelper.stringSafeParse(jsonSegment, RES_SEGMENT_FORM_ID));
                trs.setDepartureDate(parseDate(
                        RequestParsingHelper.stringSafeParse(jsonSegment, RES_SEGMENT_DEPARTURE_DATE_KEY), sdf));
                trs.setArrivalDate(parseDate(
                        RequestParsingHelper.stringSafeParse(jsonSegment, RES_SEGMENT_ARRIVAL_DATE_KEY), sdf));
                trs.setFromLocationName(RequestParsingHelper.stringSafeParse(jsonSegment,
                        RES_SEGMENT_FROM_LOCATION_NAME_KEY));
                trs.setToLocationName(RequestParsingHelper.stringSafeParse(jsonSegment,
                        RES_SEGMENT_TO_LOCATION_NAME_KEY));
                final List exeptionList = (List) jsonSegment.get(RES_SEGMENT_EXCEPTION_LIST);
                if (exeptionList != null) {
                    for (int k = 0; k < exeptionList.size(); k++) {
                        trs.getExeptionList().add(String.valueOf(exeptionList.get(k)));
                    }
                }

                entry.getListSegment().add(trs);
            }

            res.add(entry);
        }

        tr.setEntriesList(res);
    }

    /**
     * Parse jsonRes content with Gson into a Form with n Fields
     * 
     * @param jsonRes
     *            the json string
     */
    public ConnectForm parseFormFieldsResponse(String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Boolean.class, new BooleanDeserializer());
        builder.registerTypeAdapter(Integer.class, new IntegerDeserializer());
        builder.registerTypeAdapter(IFormField.AccessType.class, new EnumDeserializer<IFormField.AccessType>(
                IFormField.AccessType.class, EnumDeserializer.EnumParsingType.STRING_VALUE));
        builder.registerTypeAdapter(IFormField.ControlType.class, new EnumDeserializer<IFormField.ControlType>(
                IFormField.ControlType.class, EnumDeserializer.EnumParsingType.STRING_VALUE));
        builder.registerTypeAdapter(IFormField.DataType.class, new EnumDeserializer<IFormField.DataType>(
                IFormField.DataType.class, EnumDeserializer.EnumParsingType.STRING_VALUE));
        final Gson gson = builder.create();
        Log.d("RequestParser", " starting parse");
        return gson.fromJson(jsonRes, ConnectForm.class);
    }

    /**
     * Parse jsonRes content with Gson into a RequestGroupConfigurationsContainer with n RequestGroupConfiguration
     *
     * @param jsonRes
     *            the json string
     */
    public RequestGroupConfigurationsContainer parseRequestGroupConfigurationsResponse(String jsonRes) {
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Boolean.class, new BooleanDeserializer());
        builder.registerTypeAdapter(Integer.class, new IntegerDeserializer());

        final Gson gson = builder.create();
        Log.d("RequestGroupConfigurationsParser", " starting parse");
        return gson.fromJson(jsonRes, RequestGroupConfigurationsContainer.class);
    }
}
