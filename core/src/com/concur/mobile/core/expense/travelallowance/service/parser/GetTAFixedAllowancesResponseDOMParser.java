package com.concur.mobile.core.expense.travelallowance.service.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceControlData;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.LodgingType;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvisionEnum;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by Michael Becherer on 26-Jun-15.
 */
public class GetTAFixedAllowancesResponseDOMParser {

    private static final String FIXED_ALLOWANCES = "FixedAllowances";
    private static final String PROVIDED_MEAL = "ProvidedMeal";
    private static final String PROVIDED_MEAL_VALUES = "ProvidedMealValues";
    private static final String LODGING_TYPE_VALUES = "LodgingTypeValues";
    private static final String LODGING_TYPE = "LodgingType";


    private static final String CONTROL = "Control";


    private List<FixedTravelAllowance> fixedTravelAllowances;
    private Context context;
    private SimpleDateFormat dateFormat;

    private FixedTravelAllowanceControlData controlData;



    public GetTAFixedAllowancesResponseDOMParser(Context context) {
        this.context = context;
        this.fixedTravelAllowances = new ArrayList<FixedTravelAllowance>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    /**
     * @return  Returns the parsed {@link FixedTravelAllowance} list.
     */
    public List<FixedTravelAllowance> getFixedTravelAllowances() {
        return this.fixedTravelAllowances;
    }

    public void parse(Document dom) {
        NodeList nodeList = dom.getElementsByTagName(CONTROL);
        if (nodeList.getLength() > 0) {
            parseControlData(nodeList.item(0));
        }

        nodeList = dom.getElementsByTagName(PROVIDED_MEAL_VALUES);
        if (nodeList.getLength() > 0) {
            Map<String, ICode> providedMealValues = new HashMap<String, ICode>();
            parseCodeValueList(nodeList.item(0), PROVIDED_MEAL, providedMealValues, MealProvision.class);
            controlData.setProvidedMealValues(providedMealValues);
        }

        nodeList = dom.getElementsByTagName(LODGING_TYPE_VALUES);
        if (nodeList.getLength() > 0) {
            Map<String, ICode> lodgingTypeValues = new HashMap<String, ICode>();
            parseCodeValueList(nodeList.item(0), LODGING_TYPE, lodgingTypeValues, LodgingType.class);
            controlData.setLodgingTypeValues(lodgingTypeValues);
        }

        nodeList = dom.getElementsByTagName(FIXED_ALLOWANCES);
        if (nodeList.getLength() > 0) {
            parseFixedAllowances(nodeList.item(0));
        }

    }

    private void parseFixedAllowances(Node rootFixedAllowances) {
        NodeList fixedAllowancesList = rootFixedAllowances.getChildNodes();
        for (int i = 0; i < fixedAllowancesList.getLength(); i++ ) {
            NodeList list = fixedAllowancesList.item(i).getChildNodes();
            FixedTravelAllowance currentAllowance = new FixedTravelAllowance();
            for (int f = 0; f < list.getLength(); f++) {
                Node attribute = list.item(f);
                String tag = attribute.getNodeName();
                String text = attribute.getFirstChild().getNodeValue();
                if (tag.equals("TaDayKey")) {
                    currentAllowance.setFixedTravelAllowanceId(text);
                }
                if (tag.equals("AllowanceDate")) {
                    try {
                        currentAllowance.setDate(dateFormat.parse(text));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (tag.equals("AllowanceAmount")) {
                    currentAllowance.setAmount(Double.parseDouble(text));
                }
                if (tag.equals("MealsRateCrnCode")) {
                    currentAllowance.setCurrencyCode(text);
                }
                if (tag.equals("MarkedExcluded")) {
                    currentAllowance.setExcludedIndicator(StringUtilities.toBoolean(text));
                }
                if (tag.equals("Location")) {
                    currentAllowance.setLocationName(text);
                }
                if (tag.equals("BreakfastProvided")) {
                    if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_CHECKBOX)
                            || controlData
                                    .getControlValue(FixedTravelAllowanceControlData.SHOW_BREAKFAST_PROVIDED_PICKLIST)) {
                        ICode breakfast = controlData.getProvidedMealValues().get(text);
                        if (breakfast != null) {
                            currentAllowance.setBreakfastProvision((MealProvision) breakfast);
                        } else {
                            currentAllowance.setBreakfastProvision(MealProvisionEnum.fromCode(text, context));
                        }
                    }
                }
                if (tag.equals("LunchProvided")) {
                    if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_CHECKBOX)
                            || controlData
                            .getControlValue(FixedTravelAllowanceControlData.SHOW_LUNCH_PROVIDED_PICKLIST)) {
                        ICode lunch = controlData.getProvidedMealValues().get(text);
                        if (lunch != null) {
                            currentAllowance.setLunchProvision((MealProvision) lunch);
                        } else {
                            currentAllowance.setLunchProvision(MealProvisionEnum.fromCode(text, context));
                        }
                    }

                }
                if (tag.equals("DinnerProvided")) {
                    if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_CHECKBOX)
                            || controlData
                            .getControlValue(FixedTravelAllowanceControlData.SHOW_DINNER_PROVIDED_PICKLIST)) {
                        ICode dinner = controlData.getProvidedMealValues().get(text);
                        if (dinner != null) {
                            currentAllowance.setDinnerProvision((MealProvision) dinner);
                        } else {
                            currentAllowance.setDinnerProvision(MealProvisionEnum.fromCode(text, context));
                        }
                    }
                }
                if (tag.equals("Overnight")) {
                    currentAllowance.setOvernightIndicator(StringUtilities.toBoolean(text));
                }
                if (tag.equals("LodgingType")) {
                    if (controlData.getControlValue(FixedTravelAllowanceControlData.SHOW_LODGING_TYPE_PICKLIST)) {
                        ICode lodgingType = controlData.getLodgingTypeValues().get(text);
                        if (lodgingType != null) {
                            currentAllowance.setLodgingType((LodgingType) lodgingType);
                        } else {
                            currentAllowance.setLodgingType(new LodgingType(text, ""));
                        }
                    }
                }
            }
            fixedTravelAllowances.add(currentAllowance);
        }
    }

    private void parseControlData(Node controlNode) {
        NodeList nodes = controlNode.getChildNodes();
        for (int i = 0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (controlData.getAttributeKeys().contains(node.getNodeName())) {
                controlData.putControlData(node.getNodeName(), node.getFirstChild().getNodeValue());
            }
        }
    }

    private void parseCodeValueList(Node rootValueNode, String nodeName, Map<String, ICode> resultMap, Class codeClass) {
        NodeList valueNodeList = rootValueNode.getChildNodes();
        for (int i = 0; i < valueNodeList.getLength(); i++) {
            String code = null;
            String value = null;
            Element valueElement = (Element) valueNodeList.item(i);
            if (nodeName.equals(valueElement.getNodeName())) {
                NodeList nodeList = valueElement.getElementsByTagName("Code");
                if (nodeList.getLength() > 0) {
                    code = nodeList.item(0).getFirstChild().getNodeValue();
                }
                nodeList = valueElement.getElementsByTagName("Value");
                if (nodeList.getLength() > 0) {
                    value = nodeList.item(0).getFirstChild().getNodeValue();
                }
            }
            if (code != null && value != null) {
                try {
                    ICode codeObject = (ICode) codeClass.newInstance();
                    codeObject.setCode(code);
                    codeObject.setDescription(value);
                    resultMap.put(code, codeObject);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public void setControlData(FixedTravelAllowanceControlData controlData) {
        this.controlData = controlData;
    }
}