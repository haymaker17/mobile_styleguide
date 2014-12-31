package com.concur.mobile.platform.util;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Calendar;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * An implementation of <code>JsonDeserializaer&lt;Calendar&gt;</code> for the purpose of providing a deserialization of date/time
 * values.
 * 
 * @author andrewk
 */
public class CalendarDeserializer implements JsonDeserializer<Calendar> {

    private DateFormat df = Parse.XML_DF;

    /**
     * Constructs an instance of <code>CalendarDeserializer</code> using the date formatter from <code>Parse.XML_DF</code>.
     */
    public CalendarDeserializer() {
        this(Parse.XML_DF);
    }

    /**
     * Constructs an instance of <code>CalendarDeserializer</code> with date formatter <code>df</code>.
     * 
     * @param df
     *            contains the date formatter used to deserialize the date.
     */
    public CalendarDeserializer(DateFormat df) {
        this.df = df;
    }

    @Override
    public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Calendar retVal = null;
        retVal = Parse.parseTimestamp(json.getAsJsonPrimitive().getAsString(), df);
        return retVal;
    }

}
