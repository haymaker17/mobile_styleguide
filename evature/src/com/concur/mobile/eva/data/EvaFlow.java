/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.eva.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.concur.mobile.eva.util.Const;

/**
 * @author Chris N. Diaz
 * 
 */
public class EvaFlow {

    /**
     * 'Answer', 'Question', 'Greeting', 'Statement', 'Hotel', 'Car', 'Flight'.
     * 
     * @author Chris N. Diaz
     * 
     */
    public enum EvaFlowType {
        CAR("Car"), HOTEL("Hotel"), FLIGHT("Flight"), ANSWER("Answer"), QUESTION("Question"), GREETING("Greeting"), STATEMENT(
                "Statement"), UNDEFINED("Undefined");

        public String actionType;

        private EvaFlowType(String actionType) {
            this.actionType = actionType;
        }

        public static EvaFlowType getEvaFlowType(String actionType) {
            for (EvaFlowType flow : EvaFlowType.values()) {
                if (flow.actionType.equalsIgnoreCase(actionType)) {
                    return flow;
                }
            }

            return UNDEFINED;
        }
    }; // end EvaFlowType enum

    public static enum QuestionSubCategory {
        Unsupported("Unsupported"), Undefined("Undefined");

        private String name;

        /**
         * Constructor an instance of <code>QuestionSubCategory</code>.
         * 
         * @param name
         *            the QuestionSubCategory name;
         */
        QuestionSubCategory(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        public static QuestionSubCategory getQueSubCat(String actionType) {
            for (QuestionSubCategory subCat : QuestionSubCategory.values()) {
                if (subCat.name.equalsIgnoreCase(actionType)) {
                    return subCat;
                }
            }

            return Undefined;
        }

    };

    public static enum StatementType {
        Unsupported("Unsupported"), Undefined("Undefined");

        private String name;

        /**
         * Constructor an instance of <code>QuestionSubCategory</code>.
         * 
         * @param name
         *            the QuestionSubCategory name;
         */
        StatementType(String name) {
            this.name = name;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        public static StatementType getStatementType(String type) {
            for (StatementType stmtType : StatementType.values()) {
                if (stmtType.name.equalsIgnoreCase(type)) {
                    return stmtType;
                }
            }

            return Undefined;
        }

    };

    private final static String CLS_TAG = EvaFlow.class.getSimpleName();

    public EvaFlowType type;

    public String sayIt;

    public QuestionSubCategory questionSubCategory;

    public StatementType stmtType;

    public JSONObject json;

    public EvaFlow(EvaFlowType type, String sayIt, QuestionSubCategory questionSubCategory, StatementType stmtType) {
        this.type = type;
        this.sayIt = sayIt;
        this.questionSubCategory = questionSubCategory;
        this.stmtType = stmtType;
    };

    public EvaFlow(JSONObject flowJson) throws JSONException {

        this.json = flowJson;

        try {
            this.sayIt = flowJson.getString("SayIt");

            String flowType = flowJson.getString("Type");
            this.type = EvaFlowType.getEvaFlowType(flowType);

            if (flowJson.has("QuestionSubCategory")) {
                String questionSubCategory = flowJson.getString("QuestionSubCategory");
                this.questionSubCategory = QuestionSubCategory.getQueSubCat(questionSubCategory);
            } else {
                this.questionSubCategory = QuestionSubCategory.Undefined;
            }

            if (flowJson.has("StatementType")) {
                String stmtType = flowJson.getString("StatementType");
                this.stmtType = StatementType.getStatementType(stmtType);
            } else {
                this.stmtType = StatementType.Undefined;
            }

        } catch (JSONException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".EvaFlow() - Problem parsing Eva Flow!", e);
            throw e;
        }

    }

} // end EvaFlow
