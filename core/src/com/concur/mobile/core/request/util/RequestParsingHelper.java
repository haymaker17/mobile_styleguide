/**
 * 
 */
package com.concur.mobile.core.request.util;

import java.util.Map;

/**
 * @author OlivierB
 *
 */
public class RequestParsingHelper {

    public static String stringSafeParse(Map<?, ?> o, String key) {
        return String.valueOf(o.get(key));
    }
}
