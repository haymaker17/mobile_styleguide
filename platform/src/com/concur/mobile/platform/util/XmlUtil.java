package com.concur.mobile.platform.util;


/**
 * Provides a set of utility methods for adding XML elements to a <code>StringBuilder</code> object.
 */
public class XmlUtil {

    /**
     * Adds an XML element with its value to a string builder object.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * <b>NOTE:</b> The <code>elementValue</code> parameter will be passed through the <code>FormatUtil.escapeForXML</code> method
     * call prior to placement into the generated XML.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value.
     */
    public static void addXmlElement(StringBuilder strBldr, String elementName, String elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append(Format.escapeForXML(elementValue));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds an integer XML element with its value to a string builder object.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the integer element value.
     */
    public static void addXmlElement(StringBuilder strBldr, String elementName, Integer elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append(Integer.toString(elementValue));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds a boolean XML element with its value to a string builder object providing either 'true' or 'false'.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value, if <code>true</code>, then value will be written as <code>true</code>; otherwise will be
     *            written as <code>false</code>.
     */
    public static void addXmlElementTF(StringBuilder strBldr, String elementName, Boolean elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append((elementValue ? "true" : "false"));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds a boolean XML element with its value to a string builder object providing either 'Y' or 'N'
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value, if <code>true</code>, then value will be written as <code>Y</code>; otherwise will be written
     *            as <code>N</code>.
     */
    public static void addXmlElementYN(StringBuilder strBldr, String elementName, Boolean elementValue) {
        if (elementValue != null) {
            strBldr.append('<');
            strBldr.append(elementName);
            strBldr.append('>');
            strBldr.append((elementValue ? "Y" : "N"));
            strBldr.append("</");
            strBldr.append(elementName);
            strBldr.append('>');
        }
    }

    /**
     * Adds an XML element with its value to a string builder object.
     * 
     * If <code>elementValue</code> is <code>null</code>, then <code>elementName</code> will not be added.
     * 
     * <b>NOTE:</b> The <code>elementValue</code> parameter will be passed through the <code>FormatUtil.escapeForXML</code> method
     * call prior to placement into the generated XML.
     * 
     * @param strBldr
     *            the string builder.
     * @param elementName
     *            the element name.
     * @param elementValue
     *            the element value.
     */
    public static void addXmlElement(StringBuilder strBldr, String elementName, Double doubleValue) {
        if (doubleValue != null) {
            String stringValue = doubleValue.toString();
            addXmlElement(strBldr, elementName, stringValue);
        }
    }

}
