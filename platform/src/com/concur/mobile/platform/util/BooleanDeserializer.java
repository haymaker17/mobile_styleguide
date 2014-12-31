package com.concur.mobile.platform.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * An implementation of <code>JsonDeserializaer&lt;Boolean&gt;</code> for the purpose of providing a deserialization of boolean
 * values serialized as either 'Y', 'N', 'YES', 'NO', '0', '1', 'true', 'T', 'false', 'F'.
 * 
 * @author andrewk
 */
public class BooleanDeserializer implements JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Boolean retVal = null;
        retVal = Parse.safeParseBoolean(json.getAsJsonPrimitive().getAsString());
        return retVal;
    }

}
