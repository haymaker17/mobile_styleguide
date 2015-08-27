/**
 * 
 */
package com.concur.mobile.platform.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * An implementation of <code>JsonDeserializaer&lt;Enum&gt;</code> for the purpose of providing a deserialization of Enum
 * values.
 * @author OlivierB
 * @param <T> enum type's class
 */
public class EnumDeserializer<T extends Enum<T>> implements JsonDeserializer<T> {
    // --- used to store enum's class s that we can call safeParseEnum
    private Class<T> enumClass;
    private EnumParsingType parsingType;
    
    public enum EnumParsingType {
        NAME,
        STRING_VALUE
    }
    
    public EnumDeserializer(Class<T> enumClass, EnumParsingType parsingType){
        this.enumClass = enumClass;
        this.parsingType = parsingType;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        return Parse.safeParseEnum(enumClass, json.getAsJsonPrimitive().getAsString(), parsingType);
    }

}