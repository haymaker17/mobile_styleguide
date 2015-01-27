package com.concur.mobile.platform.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by OlivierB on 20/01/2015.
 */
public class DateDeserializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

    private DateFormat df = Parse.XML_DF;

    /**
     * Constructs an instance of <code>DateDeserializer</code> using the date formatter from <code>Parse.XML_DF</code>.
     */
    public DateDeserializer() {
        this(Parse.XML_DF);
    }

    /**
     * Constructs an instance of <code>DateDeserializer</code> with date formatter <code>df</code>.
     *
     * @param df contains the date formatter used to deserialize the date.
     */
    public DateDeserializer(DateFormat df) {
        this.df = df;
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return Parse.safeParseDate(json.getAsJsonPrimitive().getAsString(), df);
    }

    @Override public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(df.format(date));
    }
}
