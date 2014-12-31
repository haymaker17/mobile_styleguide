package com.concur.mobile.platform.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * An implementation of <code>JsonDeserializaer&lt;Double&gt;</code> for the purpose of providing a deserialization of double
 * values.
 * 
 * @author andrewk
 */
public class DoubleDeserializer implements JsonDeserializer<Double> {

    @Override
    public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Double retVal = null;
        retVal = Parse.safeParseDouble(json.getAsJsonPrimitive().getAsString());
        return retVal;
    }

}
