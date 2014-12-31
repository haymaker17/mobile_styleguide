package com.concur.mobile.eva.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.data.EvaFlow;
import com.concur.mobile.eva.util.Const;

/**
 * Object representing an Eva "Flow" response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaFlowApiReply extends EvaApiReply {

    private final static String CLS_TAG = EvaFlowApiReply.class.getSimpleName();

    /**
     * The Eve Session ID.
     */
    protected String sessionId;

    /**
     * The first Flow that isn't a Hotel, Flight, or Car action type. That is, it's action type is either a 'Answer', 'Question',
     * 'Greeting', or 'Statement'.
     */
    protected List<EvaFlow> flows;

    /**
     * Constructor that parses the XML reply.
     * 
     * @param fullReply
     *            the XML reply from the Eva web service.
     */
    public EvaFlowApiReply(String fullReply) {
        super(fullReply);

        Log.d(Const.LOG_TAG, CLS_TAG + " - Constructor");

        try {
            // Get the new Session ID.
            sessionId = jFullReply.getString("session_id");

            JSONObject jApiReply = jFullReply.getJSONObject("api_reply");
            JSONArray jFlows = jApiReply.getJSONArray("Flow");

            if (jFlows != null) {

                flows = new ArrayList<EvaFlow>();

                // Get the first Flow that isn't a Hotel, Flight, or Car action type.
                for (int i = 0; i < jFlows.length(); i++) {
                    JSONObject flow = jFlows.getJSONObject(i);
                    flows.add(new EvaFlow(flow));
                }

            }

        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".EvaApiReply() - Bad EVA reply!", e);

            // Override the status to be unsuccessful.
            status = false;
        }
    }

} // end EvaFlowApiReply

