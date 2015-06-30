package com.concur.mobile.core.expense.travelallowance.service.parser;

import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Facilitates parsing of code lists from XML data containers as follows:
 * Starting with the code list denoting tag an object of this class should be instantiated.
 * As name one could use e.g. the appropriate tag name. The XML is parsed in a subsequent order.
 * Usually the code is parsed first followed by its value. However, with the setter methods
 * for code and value one can buffer these parsed texts. As soon as the parsing process for code
 * and value is finished one needs to put the pair to the internal codelist. Therefore one should
 * use the getter and setter methods for code and value, construct an codelist entry with the
 * appropriate type of the codelist and put the result to the internal codelist using the put
 * method. The get method can be used to gather an entry from the internal codelist.
 *
 * Created by Michael Becherer on 26-Jun-15.
 */
public class CodeList<T> {

    private String code;
    private String value;
    private String name;
    private Map<String, T> codes;

    public CodeList(String name) {
        this.name = name;
        this.code = StringUtilities.EMPTY_STRING;
        this.value = StringUtilities.EMPTY_STRING;
        this.codes = new HashMap<String, T>();
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void put(T entry) {
        if (this.codes != null) {
            codes.put(this.code, entry);
        }
        this.code = StringUtilities.EMPTY_STRING;
        this.value = StringUtilities.EMPTY_STRING;
    }

    public T get(String code) {
        if (this.codes != null) {
            return this.codes.get(code);
        }
        return null;
    }

    public boolean containsKey(String code) {
        if (this.codes != null) {
            return this.codes.containsKey(code);
        }
        return false;
    }

    public boolean isEmpty() {
        if (this.codes != null) {
            return this.codes.isEmpty();
        }
        return false;
    }
}
