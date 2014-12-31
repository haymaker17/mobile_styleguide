package com.concur.mobile.core.expense.ta.service;

import com.concur.mobile.core.util.FormatUtil;

public class TaXmlUtil {
    public static void appendXml(StringBuilder sb, String name, String value) {
    	FormatUtil.addXMLElementEscaped(sb, name, value);
    }

    public static void appendXmlIfNotEmpty(StringBuilder sb, String name, String value) {
    	if (value != null && value.length() > 0) {
    		FormatUtil.addXMLElementEscaped(sb, name, value);
    	}
    }
   
    public static void appendXml(StringBuilder sb, String name, boolean value) {
    	FormatUtil.addXMLElementEscaped(sb, name, value ? "Y" : "N");
    }

    public static void appendXmlIfTrue(StringBuilder sb, String name, boolean value) {
    	if (value) {
    		FormatUtil.addXMLElementEscaped(sb, name, value ? "Y" : "N");
    	}
    }
}
