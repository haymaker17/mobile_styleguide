package com.concur.mobile.base.service.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.util.Const;

/**
 * A parameterized implementation of <code>Parser<code> that supports the parsing
 * of a single item.
 * 
 * @param <T>
 *            contains a class that extends <code>Parser</code>.
 */
public class ItemParser<T extends Parser> extends BaseParser {

    private static final String CLS_TAG = "ItemParser<T>";

    /**
     * Contains the tag which starts the item.
     */
    private String itemTag;

    /**
     * Contains the class object used to construct the single item.
     */
    private Class<T> clz;

    /**
     * Contains a reference to the item.
     */
    private T item;

    /**
     * Contains a reference to a <code>CommonParser</code> object
     */
    protected CommonParser parser;

    /**
     * Constructs an instance of <code>ItemParser</code> used to parse a single object of type <code>T</code>. <br>
     * <br>
     * If <code>parser</code> is non-null, and <code>clz</code> contains a constructor accepting two arguments with the first one
     * being of type <code>CommonParser</code> and the second of type <code>String</code>, then upon constructing a new item of
     * type <code>T</code>, that constructor will be called passing <code>parser</code> and <code>itemTag</code>; respectfully.
     * Otherwise, the no-args constructor for <code>T</code> will be invoked.
     * 
     * @param parser
     *            contains a reference to a <code>CommonParser</code>.
     * @param itemTag
     *            contains the tag that identifies the item of type <code>T</code> that is being parsed.
     * @param clz
     *            contains a reference to the class object for type <code>T</code>.
     */

    public ItemParser(CommonParser parser, String itemTag, Class<T> clz) {
        this.parser = parser;
        this.itemTag = itemTag;
        this.clz = clz;
    }

    /**
     * Constructs an instance of <code>ItemParser</code> with a specific tag and class object.
     * 
     * @param itemTag
     *            contains the tag that identifies the item of type <code>T</code> that is being parsed.
     * @param clz
     *            contains a reference to the class object for type <code>T</code>.
     */
    public ItemParser(String itemTag, Class<T> clz) {
        this(null, itemTag, clz);
    }

    /**
     * Gets the parsed item of type <code>T</code>.
     * 
     * @return returns the parsed item of type <code>T</code>.
     */
    public T getItem() {
        return item;
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

    @Override
    public void startTag(String tag) {
        if (!TextUtils.isEmpty(tag) && tag.equalsIgnoreCase(itemTag)) {
            try {
                // Construct the new instance.
                item = constructItem();
                if (item != null) {
                    item.startTag(tag);
                }
            } catch (InstantiationException exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: instantiation exception.", exc);
                exc.printStackTrace();
            } catch (IllegalAccessException exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: illegal access exception.", exc);
                exc.printStackTrace();
            }
        } else {
            if (item != null) {
                item.startTag(tag);
            }
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (item != null) {
            item.handleText(tag, text);
        }
    }

    @Override
    public void endTag(String tag) {
        if (item != null) {
            item.endTag(tag);
        }
    }

}
