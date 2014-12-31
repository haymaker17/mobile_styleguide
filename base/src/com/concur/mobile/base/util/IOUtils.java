/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.base.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Utility class containing various methods for handling IO.
 * 
 * @author Chris N. Diaz
 * 
 */
public class IOUtils {

    /**
     * Copies the given <code>InptuStream</code> into a <code>ByteArrayOutputStream</code> so that it can be used (i.e. read)
     * multiple times. <br>
     * <br>
     * Usage:<br>
     * <code>
     * ByteArrayOutputStream baos = reusableOutputStream(is);<br>
     * InputStream is1 = new ByteArrayInputStream(baos.toByteArray());<br>
     * InputStream is2 = new ByteArrayInputStream(baos.toByteArray());<br> 
     * </code>
     * 
     * 
     * @param input
     *            the <code>InputStream</code> to copy.
     * @return a <code>ByteArrayOutputStream</code> containing a copy of the <code>InputStream</code> which can be read/used
     *         multiple times.
     * 
     * @throws IOException
     */
    public static ByteArrayOutputStream reusableOutputStream(InputStream input) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();

        return baos;
    }

    /**
     * A simple helper to convert an InputStream into a String.
     * 
     * @param is
     *            The input stream
     * @return A String containing the full contents of the InputStream
     * @throws IOException
     */
    public static String readStream(InputStream is, String encoding) throws IOException {
        if (is == null) {
            return null;
        }

        final char[] buffer = new char[8192];
        StringBuilder out = new StringBuilder();

        Reader in;
        try {
            in = new InputStreamReader(is, encoding);
        } catch (UnsupportedEncodingException e) {
            in = new InputStreamReader(is);
        }

        int readCount;
        do {
            readCount = in.read(buffer, 0, buffer.length);
            if (readCount > 0) {
                out.append(buffer, 0, readCount);
            }
        } while (readCount >= 0);

        return out.toString();
    }

}
