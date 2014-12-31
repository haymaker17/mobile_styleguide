/**
 * @author sunill
 */
package com.concur.mobile.gov.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.util.Log;

public class SerializerUtil {

    private final static String LOG_TAG = SerializerUtil.class.getSimpleName();

    public SerializerUtil() {
        // TODO Auto-generated constructor stub
    }

    public static byte[] serializeObject(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.close();
            // Get the bytes of the serialized object
            byte[] buf = bos.toByteArray();
            return buf;
        } catch (IOException e) {
            Log.e(LOG_TAG, " .serializeObject : ioexception ", e);
            return null;
        }
    }

    public static Object deserializeObject(byte[] b) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            Object object = in.readObject();
            in.close();
            return object;
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, " .deserializeObject : classnotfound exception ", e);
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, " .deserializeObject : ioexception ", e);
            return null;
        }
    }
}
