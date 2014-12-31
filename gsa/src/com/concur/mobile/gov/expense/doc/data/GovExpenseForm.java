package com.concur.mobile.gov.expense.doc.data;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.concur.mobile.core.util.FormatUtil;

public class GovExpenseForm {

    public String accLabel;
    public String description;
    public String docType;
    public ArrayList<GovExpenseFormField> fields;
    public String mode;
    public String org;
    public String sublabel;
    public String userId;
    public String vchnum;

    private static final String FORM_FIELD = "TMFormField";
    private static final String ACC_LABEL = "accLabel";
    private static final String DESC = "description";
    private static final String DOC_TYPE = "docType";
    private static final String MODE = "mode";
    private static final String ORG = "org";
    private static final String SUBLABEL = "sublabel";
    private static final String USER_ID = "userId";
    private static final String VCH_NUM = "vchnum";

    public String asXML(String elementName) {
        StringBuilder sb = new StringBuilder();

        sb.append('<').append(elementName).append('>');
        FormatUtil.addXMLElementEscaped(sb, ACC_LABEL, accLabel);
        FormatUtil.addXMLElementEscaped(sb, DESC, description);
        FormatUtil.addXMLElementEscaped(sb, DOC_TYPE, docType);
        if (fields != null && fields.size() > 0) {
            sb.append("<fields>");
            for (GovExpenseFormField ff : fields) {
                sb.append(ff.asXML(FORM_FIELD));
            }
            sb.append("</fields>");
        }
        FormatUtil.addXMLElementEscaped(sb, MODE, mode);
        FormatUtil.addXMLElementEscaped(sb, ORG, org);
        FormatUtil.addXMLElementEscaped(sb, SUBLABEL, sublabel);
        FormatUtil.addXMLElementEscaped(sb, USER_ID, userId);
        FormatUtil.addXMLElementEscaped(sb, VCH_NUM, vchnum);
        sb.append("</").append(elementName).append('>');

        return sb.toString();
    }

    public void parse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        String tag = "";

        GovExpenseFormField field = null;

        fields = new ArrayList<GovExpenseFormField>();

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                tag = xpp.getName();
                if (FORM_FIELD.equalsIgnoreCase(tag)) {
                    // Create the new object and let it read itself
                    // The object will read through its end tag so we'll never see that here.
                    field = new GovExpenseFormField();
                    field.parse(xpp);
                    fields.add(field);
                }
                break;
            case XmlPullParser.TEXT:
                if (ACC_LABEL.equalsIgnoreCase(tag)) {
                    accLabel = xpp.getText();
                } else if (DESC.equalsIgnoreCase(tag)) {
                    description = xpp.getText();
                } else if (DOC_TYPE.equalsIgnoreCase(tag)) {
                    docType = xpp.getText();
                } else if (MODE.equalsIgnoreCase(tag)) {
                    mode = xpp.getText();
                } else if (ORG.equalsIgnoreCase(tag)) {
                    org = xpp.getText();
                } else if (SUBLABEL.equalsIgnoreCase(tag)) {
                    sublabel = xpp.getText();
                } else if (USER_ID.equalsIgnoreCase(tag)) {
                    userId = xpp.getText();
                } else if (VCH_NUM.equalsIgnoreCase(tag)) {
                    vchnum = xpp.getText();
                }
                break;
            case XmlPullParser.END_TAG:
                tag = ""; // Make sure to clear this because TEXT can exist outside elements and we don't want it
                break;
            }

            eventType = xpp.next();
        }
    }

}
