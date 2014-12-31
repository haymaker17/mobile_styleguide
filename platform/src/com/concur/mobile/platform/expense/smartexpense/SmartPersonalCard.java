package com.concur.mobile.platform.expense.smartexpense;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.concur.mobile.platform.expense.list.PersonalCard;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;

public class SmartPersonalCard extends PersonalCard {

    private static final String CLS_TAG = "SmartPersonalCard";

    /**
     * Constructs a new instance of <code>SmartPersonalCard</code>.
     */
    public SmartPersonalCard() {
        super();
    }

    /**
     * Constructs an instance of <code>SmartPersonalCard</code> from a cursor.
     * 
     * @param context
     *            contains the application context.
     * @param cursor
     *            contains the content cursor.
     */
    public SmartPersonalCard(Context context, Cursor cursor) {
        super(context, cursor);
    }

    /**
     * Constructs an instance of <code>SmartPersonalCard</code> given a context and an Uri.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content uri.
     */
    public SmartPersonalCard(Context context, Uri contentUri) {
        super(context, contentUri);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.expense.list.PersonalCard#getPersonalCardTransactionDAOS()
     */
    @Override
    public List<PersonalCardTransactionDAO> getPersonalCardTransactionDAOS() {
        throw new UnsupportedOperationException(
                CLS_TAG
                        + ".getPersonalCardTransactionDAOS: unsupported for a personal card included in the smart expense list.");
    }

}
