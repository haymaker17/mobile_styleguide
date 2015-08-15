package com.concur.mobile.core.expense.travelallowance;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import android.util.Log;

import com.concur.mobile.base.util.Const;
import com.concur.mobile.platform.service.parser.ActionResponseParser;

public class ReflectionActionResponseParser extends ActionResponseParser {

    public enum PropType {
        STRING, BOOLEAN, DATE, DOUBLE
    };

    public static void setStringProperty(Object target, String name, String value) throws Exception {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method m = target.getClass().getMethod("set" + name, new Class[] { String.class });
        if (m == null) {
            Log.w(Const.LOG_TAG, "method not found: set" + name);
        } else {
            m.invoke(target, value);
        }
    }

    public static void setBooleanProperty(Object target, String name, String value) throws Exception {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method m = target.getClass().getMethod("set" + name, new Class[] { boolean.class });
        if (m == null) {
            Log.w(Const.LOG_TAG, "method not found: set" + name);
        } else {
            if ("Y".equals(value)) {
                m.invoke(target, true);
            } else {
                m.invoke(target, false);
            }
        }
    }

    public static void setDoubleProperty(Object target, String name, String value) throws Exception {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method m = target.getClass().getMethod("set" + name, new Class[] { Double.class });
        if (m == null) {
            Log.w(Const.LOG_TAG, "method not found: set" + name);
        } else {
            m.invoke(target, Double.parseDouble(value));
        }
    }

    public static void setDateProperty(Object target, String name, String value) throws Exception {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method m = target.getClass().getMethod("set" + name, new Class[] { Date.class });
        if (m == null) {
            Log.w(Const.LOG_TAG, "method not found: set" + name);
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date d = format.parse(value);
            m.invoke(target, d);
        }
    }
    
    public void handleText(String tag, String text, Map<String, PropType> props, Object target) throws Exception {
        PropType propType = props.get(tag);
        if (propType != null) {
            switch (propType) {
            case STRING:
                setStringProperty(target, tag, text);
                break;
            case DATE:
                setDateProperty(target, tag, text);
                break;
            case BOOLEAN:
                setBooleanProperty(target, tag, text);
                break;
            case DOUBLE:
                setDoubleProperty(target, tag, text);
                break;
            default:
                super.handleText(tag, text);
            }
        }
    }
}
