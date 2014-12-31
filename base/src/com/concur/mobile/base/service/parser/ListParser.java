package com.concur.mobile.base.service.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.util.Const;

/**
 * Provides an implementation of <code>Parser</code> for parsing lists of items of type <code>T</code>.
 * 
 * @param <T>
 *            contains the type of object being parsed into a list.
 */
public class ListParser<T extends Parser> implements Parser {

    private static final String CLS_TAG = "ListParser<T>";

    /**
     * Contains the list of items of type <code>T</code> being parsed.
     */
    protected List<T> items;

    /**
     * Contains the current item of type <code>T</code> being parsed.
     */
    protected T curItem;

    /**
     * Contains the tag which indicates a list of items of type <code>T</code> is being parsed.
     */
    protected String listTag;

    /**
     * Contains the tag indicating an item of type <code>T</code> is being parsed.
     */
    protected String itemTag;

    /**
     * Contains a reference to the class object for type <code>T</code>.
     */
    protected Class<T> clz;

    /**
     * Contains a reference to a <code>CommonParser</code> object
     */
    protected CommonParser parser;

    /**
     * Constructs an instance of <code>ListParser</code> used to parse a list of objects of type <code>T</code>. <br>
     * <br>
     * If <code>parser</code> is non-null, and <code>clz</code> contains a constructor accepting two arguments with the first one
     * being of type <code>CommonParser</code> and the second of type <code>String</code>, then upon constructing a new item of
     * type <code>T</code>, that constructor will be called passing <code>parser</code> and <code>itemTag</code>; respectfully.
     * Otherwise, the no-args constructor for <code>T</code> will be invoked.
     * 
     * @param parser
     *            contains a reference to a <code>CommonParser</code>.
     * @param listTag
     *            contains the tag that identifies the list of items of type <code>T</code> being parsed. If <code>listTag</code>
     *            is <code>null</code>, then the list will be implicitly constructed the first time <code>itemTag</code> is passed
     *            to this parsers <code>startTag</code> method.
     * @param itemTag
     *            contains the tag that identifies the item of type <code>T</code> that is being parsed.
     * @param clz
     *            contains a reference to the class object for type <code>T</code>.
     */
    public ListParser(CommonParser parser, String listTag, String itemTag, Class<T> clz) {
        this.parser = parser;
        this.listTag = listTag;
        this.itemTag = itemTag;
        this.clz = clz;
    }

    /**
     * Constructs an instance of <code>ListParser</code> used to parse a list of objects of type <code>T</code>.
     * 
     * @param listTag
     *            contains the tag that identifies the list of items of type <code>T</code> being parsed. If <code>listTag</code>
     *            is <code>null</code>, then the list will be implicitly constructed the first time <code>itemTag</code> is passed
     *            to this parsers <code>startTag</code> method.
     * @param itemTag
     *            contains the tag that identifies the item of type <code>T</code> that is being parsed.
     * @param clz
     *            contains a reference to the class object for type <code>T</code>.
     */
    public ListParser(String listTag, String itemTag, Class<T> clz) {
        this(null, listTag, itemTag, clz);
    }

    /**
     * Retrieves the parsed list of items of type <code>T</code>.
     * 
     * @return returns the parsed list of items of type <code>T</code>.
     */
    public List<T> getList() {
        return items;
    }

    /**
     * Constructs a new item of type <code>T</code>.
     * 
     * @return returns a new constructed item of type <code>T</code>.
     */
    private T constructItem() throws InstantiationException, IllegalAccessException {
        T newItem = null;
        if (parser != null) {
            // Locate constructor that accepts a CommonParser and a String object.
            try {
                Constructor<T> ct = null;
                @SuppressWarnings("unchecked")
                Constructor<T>[] allConstructors = (Constructor<T>[]) clz.getDeclaredConstructors();
                for (Constructor<T> ctor : allConstructors) {
                    Class<?>[] pType = ctor.getParameterTypes();
                    if (pType.length == 2 && pType[0].equals(CommonParser.class) && pType[1] == String.class) {
                        ct = ctor;
                        break;
                    }
                }
                if (ct != null) {
                    Object[] params = new Object[2];
                    params[0] = parser;
                    params[1] = itemTag;
                    newItem = ct.newInstance(params);
                }
            } catch (SecurityException exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".constructItem: security exception.", exc);
                exc.printStackTrace();
            } catch (IllegalArgumentException exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".constructItem: illegal argument exception.", exc);
                exc.printStackTrace();
            } catch (InvocationTargetException exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".constructItem: invocation target exception.", exc);
                exc.printStackTrace();
            }
            if (newItem == null) {
                Log.w(Const.LOG_TAG,
                        CLS_TAG + ".constructItem: common parser set, but unable to locate constructor in class '"
                                + clz.getSimpleName() + "' accepting parser!");
                newItem = clz.newInstance();
            }
        } else {
            // Parser not set, use default no-arg constructor.
            newItem = clz.newInstance();
        }
        return newItem;
    }

    public void startTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(listTag) && listTag.equalsIgnoreCase(tag)) {
                if (items == null) {
                    items = new ArrayList<T>();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: items is *not* null.");
                }
            } else if (!TextUtils.isEmpty(itemTag) && itemTag.equalsIgnoreCase(tag)) {
                if (curItem == null) {
                    try {
                        if (clz != null) {
                            // Construct the new instance.
                            curItem = constructItem();
                            if (curItem != null) {
                                curItem.startTag(tag);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: clz is null.");
                        }
                    } catch (InstantiationException exc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: instantiation exception.", exc);
                        exc.printStackTrace();
                    } catch (IllegalAccessException exc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: illegal access exception.", exc);
                        exc.printStackTrace();
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: current item is *not* null.");
                }
            } else {
                if (curItem != null) {
                    curItem.startTag(tag);
                }
            }
        }
    }

    public void handleText(String tag, String text) {
        if (curItem != null) {
            curItem.handleText(tag, text);
        }
    }

    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (!TextUtils.isEmpty(itemTag)) {
                if (itemTag.equalsIgnoreCase(tag)) {
                    if (curItem != null) {
                        curItem.endTag(tag);
                        // Add 'curItem' to the list.
                        // If 'items' has not been constructed, then construct it now.
                        if (items == null) {
                            items = new ArrayList<T>();
                        }
                        items.add(curItem);
                        // Reset 'curItem'.
                        curItem = null;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endTag: current item is null.");
                    }
                } else {
                    if (curItem != null) {
                        curItem.endTag(tag);
                    }
                }
            }
        }
    }
}
