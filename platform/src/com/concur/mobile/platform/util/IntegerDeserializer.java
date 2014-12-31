package com.concur.mobile.platform.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * An implementation of <code>JsonDeserializaer&lt;Integer&gt;</code> for the purpose of providing a deserialization of Integer
 * values.
 * 
 * @author OlivierB
 */
public class IntegerDeserializer implements JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return Parse.safeParseInteger(json.getAsJsonPrimitive().getAsString());
    }

}
