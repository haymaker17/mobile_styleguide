package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.service.parser.GetTAFixedAllowancesResponseDOMParser;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.service.CoreAsyncRequestTask;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Michael Becherer on 26-Jun-15.
 */
public class GetTAFixedAllowancesRequest2 extends CoreAsyncRequestTask {

    public static final String CLASS_TAG = GetTAFixedAllowancesRequest2.class
            .getSimpleName();

    private String rptKey;

    private GetTAFixedAllowancesResponseDOMParser parser;

    private List<FixedTravelAllowance> fixedTravelAllowances;

    private long startMillis;

    private long parserStartMillis;

    private Context context;

    private FixedTravelAllowanceControlData controlData;

    public GetTAFixedAllowancesRequest2(Context context,
                                        BaseAsyncResultReceiver receiver, String rptKey) {
        super(context, 0, receiver);
        this.context = context;
        this.rptKey = rptKey;
    }


    @Override
    protected void onPreExecute() {
        startMillis = System.currentTimeMillis();
        super.onPreExecute();
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/GetTaFixedAllowances/" + rptKey;
    }


    @Override
    protected int onPostParse() {
        long currentMillisParser = System.currentTimeMillis();
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onPostParse",
                "Parsing time = " + (currentMillisParser - parserStartMillis) + "ms"));

        resultData.putBoolean(IS_SUCCESS, true);
        this.fixedTravelAllowances = parser.getFixedTravelAllowances();
        this.controlData = parser.getControlData();
        ArrayList<FixedTravelAllowance> allowances = new ArrayList<FixedTravelAllowance>(this.fixedTravelAllowances);
        resultData.putSerializable(BundleId.ALLOWANCE_LIST, allowances);
        resultData.putSerializable(BundleId.FIXED_TRAVEL_ALLOWANCE_CONTROL_DATA, controlData);
        long currentMillis = System.currentTimeMillis();
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "onPostParse",
                "Request total = " + (currentMillis - startMillis) + "ms"));
        return RESULT_OK;
    }

    public List<FixedTravelAllowance> getFixedTravelAllowances() {
        return fixedTravelAllowances;
    }

    @Override
    protected int parseStream(InputStream is) {
        parser = new GetTAFixedAllowancesResponseDOMParser(context);
        try {
            parserStartMillis = System.currentTimeMillis();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dom = builder.parse(is);
            parser.parse(dom);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected int parse(CommonParser parser) {
        // parse method not needed. The parsing happens directly in the parseStream method.
        return 0;
    }
}
