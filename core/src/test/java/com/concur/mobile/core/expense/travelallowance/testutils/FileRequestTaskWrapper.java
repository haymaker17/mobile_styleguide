package com.concur.mobile.core.expense.travelallowance.testutils;

import android.os.Bundle;

import com.concur.mobile.core.service.CoreAsyncRequestTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by D028778 on 01-Oct-15.
 */
public class FileRequestTaskWrapper {


    private CoreAsyncRequestTask requestTask;

    public FileRequestTaskWrapper(CoreAsyncRequestTask requestTask) {
        this.requestTask = requestTask;
    }

    public Bundle parseFile(String directory, String fileName) {

        Method parseStreamMethod;
        Method setResultDataMethod;
        InputStream inputStream;
        Bundle result = null;

        if (this.requestTask == null) {
            return null;
        }

        try {
            parseStreamMethod = requestTask.getClass().getDeclaredMethod("parseStream", InputStream.class);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            try {
                parseStreamMethod = requestTask.getClass().getSuperclass().getDeclaredMethod("parseStream", InputStream.class);
            } catch (NoSuchMethodException f) {
                f.printStackTrace();
                return null;
            }
        }

        try {
            parseStreamMethod.setAccessible(true);
            inputStream = new FileInputStream(directory + '/' + fileName);
            parseStreamMethod.invoke(requestTask, inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        try {
            setResultDataMethod = requestTask.getClass().getDeclaredMethod("onPostParse");
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            try {
                setResultDataMethod = requestTask.getClass().getSuperclass().getSuperclass().getDeclaredMethod("onPostParse", InputStream.class);
            } catch (NoSuchMethodException f) {
                f.printStackTrace();
                return null;
            }
        }

        try {
            setResultDataMethod.setAccessible(true);
            setResultDataMethod.invoke(requestTask);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Field resultDataField = requestTask.getClass().getSuperclass().getSuperclass().getDeclaredField("resultData");
            resultDataField.setAccessible(true);
            result = (Bundle) resultDataField.get(requestTask);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}
