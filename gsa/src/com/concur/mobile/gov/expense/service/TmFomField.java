package com.concur.mobile.gov.expense.service;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.concur.mobile.core.expense.data.ListItem;

public class TmFomField {

    public TmFomField() {
        // TODO Auto-generated constructor stub
    }

    public class DropDownOption {

        protected String description;
        protected String name;
        protected String tabValue;

        protected DropDownOption(String desc, String name, String tabv) {
            this.description = desc;
            this.name = name;
            this.tabValue = tabv;
        }
    }

    public String id;

    private ArrayList<DropDownOption> dropDownList;

    public ArrayList<ListItem> resultList;

    private static final String FORM_FIELD = "TMFormField";
    private static final String DD_OPTION = "TMFormFieldDropDownOptions";
    private static final String DDO_DESC = "Description";
    private static final String DDO_NAME = "Name";
    private static final String DDO_TABVALUE = "TabValue";
    private static final String ID = "Id";

    public void addDropDownOption(String desc, String name, String tabv) {
        if (dropDownList == null) {
            dropDownList = new ArrayList<DropDownOption>();
        }

        dropDownList.add(new DropDownOption(desc, name, tabv));
    }

    public ArrayList<ListItem> getSearchableStaticList() {
        if (resultList == null && (dropDownList != null && dropDownList.size() > 0)) {
            resultList = new ArrayList<ListItem>(dropDownList.size());
            for (DropDownOption ddo : dropDownList) {
                ListItem li = new ListItem();
                li.key = li.code = li.text = ddo.description;
                resultList.add(li);
            }
        }

        return resultList;
    }

    public void parse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        String tag = "";
        boolean elementDone = false;

        boolean inDDOption = false;
        String ddoDesc = "";
        String ddoName = "";
        String ddoTabValue = "";

        int eventType = xpp.getEventType();
        while (!elementDone) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                tag = xpp.getName();
                if (DD_OPTION.equalsIgnoreCase(tag)) {
                    ddoDesc = "";
                    ddoName = "";
                    ddoTabValue = "";
                    inDDOption = true;
                }
                break;
            case XmlPullParser.TEXT:
                if (inDDOption) {
                    if (DDO_DESC.equalsIgnoreCase(tag)) {
                        ddoDesc = xpp.getText();
                    } else if (DDO_NAME.equalsIgnoreCase(tag)) {
                        ddoName = xpp.getText();
                    } else if (DDO_TABVALUE.equalsIgnoreCase(tag)) {
                        ddoTabValue = xpp.getText();
                    }
                } else {
                    if (ID.equalsIgnoreCase(tag)) {
                        id = xpp.getText();
                    }
                }
                break;
            case XmlPullParser.END_TAG:
                tag = xpp.getName();
                if (inDDOption) {
                    if (DD_OPTION.equalsIgnoreCase(tag)) {
                        addDropDownOption(ddoDesc, ddoName, ddoTabValue);
                        inDDOption = false;
                    }
                } else if (FORM_FIELD.equalsIgnoreCase(tag)) {
                    elementDone = true;
                }
                tag = ""; // Make sure to clear this because TEXT can exist outside elements and we don't want it
                break;
            }

            if (!elementDone) {
                // Keep reading here
                eventType = xpp.next();
            }
        }
    }
}
