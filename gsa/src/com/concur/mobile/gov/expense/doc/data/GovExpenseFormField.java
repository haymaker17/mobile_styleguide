package com.concur.mobile.gov.expense.doc.data;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldView.IFormFieldViewListener;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.platform.util.Parse;

public class GovExpenseFormField extends ExpenseReportFormField {

    protected class DropDownOption {

        protected String description;
        protected String name;
        protected String tabValue;

        protected DropDownOption(String desc, String name, String tabv) {
            this.description = desc;
            this.name = name;
            this.tabValue = tabv;
        }
    }

    private static final long serialVersionUID = -9029994370341529146L;

    protected Boolean searchable;
    protected ArrayList<DropDownOption> dropDownOptions;

    public GovExpenseFormField() {
    }

    public GovExpenseFormField(String id, String label, String value, AccessType accessType,
        ControlType controlType, DataType dataType, boolean required) {
        super(id, label, value, accessType, controlType, dataType, required);
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    public Boolean isSearchable() {
        return searchable;
    }

    public void addDropDownOption(String desc, String name, String tabv) {
        if (dropDownOptions == null) {
            dropDownOptions = new ArrayList<DropDownOption>();
        }

        dropDownOptions.add(new DropDownOption(desc, name, tabv));
    }

    public void populateStaticList() {
        staticList = null;
        if (dropDownOptions != null && dropDownOptions.size() > 0) {
            staticList = new SpinnerItem[dropDownOptions.size()];
            int i = 0;
            for (DropDownOption ddo : dropDownOptions) {
                staticList[i] = new SpinnerItem(ddo.name, ddo.description);
                i++;
            }
        }
    }

    @Override
    public ArrayList<ListItem> getSearchableStaticList() {
        if (searchableStaticList == null && (dropDownOptions != null && dropDownOptions.size() > 0)) {
            searchableStaticList = new ArrayList<ListItem>(dropDownOptions.size());
            for (DropDownOption ddo : dropDownOptions) {
                ListItem li = new ListItem();
                li.key = li.code = li.text = ddo.description;
                searchableStaticList.add(li);
            }
        }

        return searchableStaticList;
    }

    private static final String FORM_FIELD = "TMFormField";
    private static final String CTRL_TYPE = "CtrlType";
    private static final String DATA_TYPE = "DataType";
    private static final String ID = "Id";
    private static final String LABEL = "Label";
    private static final String REQUIRED = "Required";
    private static final String SEARCHABLE = "Searchable";
    private static final String VALUE = "Value";
    private static final String DD_OPTION = "TMFormFieldDropDownOption";
    private static final String DDO_DESC = "Description";
    private static final String DDO_NAME = "Name";
    private static final String DDO_TABVALUE = "TabValue";

    public String asXML(String elementName) {
        StringBuilder sb = new StringBuilder();

        sb.append('<').append(elementName).append('>');
        FormatUtil.addXMLElementEscaped(sb, CTRL_TYPE, controlType.getName());
        FormatUtil.addXMLElementEscaped(sb, DATA_TYPE, dataType.getName());
        FormatUtil.addXMLElementEscaped(sb, ID, id);
        FormatUtil.addXMLElementEscaped(sb, LABEL, label);
        FormatUtil.addXMLElementEscaped(sb, REQUIRED, (required != null && required ? "Y" : "N"));
        FormatUtil.addXMLElementEscaped(sb, VALUE, value);
        sb.append("</").append(elementName).append('>');

        return sb.toString();
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
                    if (CTRL_TYPE.equalsIgnoreCase(tag)) {
                        controlType = ControlType.fromString(xpp.getText());
                    } else if (DATA_TYPE.equalsIgnoreCase(tag)) {
                        dataType = DataType.fromString(xpp.getText());
                    } else if (ID.equalsIgnoreCase(tag)) {
                        id = xpp.getText();
                    } else if (LABEL.equalsIgnoreCase(tag)) {
                        label = xpp.getText();
                    } else if (REQUIRED.equalsIgnoreCase(tag)) {
                        required = Parse.safeParseBoolean(xpp.getText());
                    } else if (SEARCHABLE.equalsIgnoreCase(tag)) {
                        searchable = Parse.safeParseBoolean(xpp.getText());
                    } else if (VALUE.equalsIgnoreCase(tag)) {
                        value = xpp.getText();
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

    @Override
    public FormFieldView getExpensePickListFormFieldView(IFormFieldViewListener listener) {
        return new GovExpenseTypeFormFieldView(this, listener);
    }

    @Override
    public FormFieldView getSearchListFormFieldView(IFormFieldViewListener listener) {
        return new GovSearchListFormFieldView(this, listener);
    }

}
