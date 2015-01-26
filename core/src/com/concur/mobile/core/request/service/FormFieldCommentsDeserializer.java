package com.concur.mobile.core.request.service;

/**
 * Created by ecollomb on 21/01/2015.
 */
import java.lang.reflect.Type;

import com.concur.mobile.platform.util.Parse;
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
public class FormFieldCommentsDeserializer implements JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Boolean retVal = null;
        retVal = Parse.safeParseBoolean(json.getAsJsonPrimitive().getAsString());
        return retVal;
    }

}

