package com.concur.mobile.platform.expense.list;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.ActionResponseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;
import com.concur.mobile.platform.util.XmlUtil;

/**
 * Replaces core class {@link com.concur.mobile.core.expense.charge.service.SaveMobileEntryRequest}.
 * 
 * @author yiwenw
 * 
 */
public class SaveMobileEntryRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "SaveMobileEntryRequestTask";

    /**
     * Contains the key that should be used to look up the URI of a mobile entry that has been saved to the server.
     */
    public static final String MOBILE_ENTRY_URI_KEY = "mobile.entry.uri";
    /**
     * Contains the key that should be used to look up the ME_KEY of a mobile entry that has been saved to the server.
     */
    public static final String MOBILE_ENTRY_ME_KEY = "mobile.entry.meKey";
    /**
     * Contains the key that should be used to look up the ME_KEY of a mobile entry that has been saved to the server.
     */
    public static final String MOBILE_ENTRY_CLEAR_IMAGE_KEY = "mobile.entry.clear.image";

    // Contains the service end-point for the <code>/mobile/Expense/SaveMobileEntry</code> MWS call.
    private final String SERVICE_END_POINT = "/mobile/Expense/SaveMobileEntry";

    private static final String TAG_ME_KEY = "MeKey";

    /**
     * Contains the mobile entry uri.
     */
    protected Uri mobileEntryUri;

    /**
     * Contains the mobile entry.
     */
    protected MobileEntry mobileEntry;

    /**
     * Contains whether or not an existing receipt image should be cleared.
     */
    public boolean clearImage;

    protected ActionResponseParser actionResponseParser;

    /**
     * Constructs an instance of <code>SaveMobileEntryRequestTask</code>.
     * 
     * @param context
     * @param requestId
     * @param receiver
     * @param mobileEntryUri
     *            contains the Uri of the mobile entry being saved.
     */
    public SaveMobileEntryRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
            Uri mobileEntryUri, boolean clearImage) {
        super(context, requestId, receiver);
        if (mobileEntryUri == null) {
            throw new IllegalArgumentException(CLS_TAG + ".<init>: mobile entry uri is null!");
        }
        this.mobileEntryUri = mobileEntryUri;
        this.clearImage = clearImage;
        this.mobileEntry = new MobileEntry(context, mobileEntryUri);
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#getPostBody()
     */
    @Override
    protected String getPostBody() {
        String postBody = null;
        if (mobileEntry != null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append("<MobileEntry>");
            // Mobile entries associated with a smart expense retain their CT keys based on
            // the paired card transaction. The CT key is not retained in the DB, but
            // rather reset upon smart expense edit based on matched card transaction.
            if (mobileEntry.getCctKey() != null && mobileEntry.getCctKey().length() > 0
                    && mobileEntry.getEntryType() != ExpenseTypeEnum.SMART_CORPORATE) {
                XmlUtil.addXmlElement(strBldr, "CctKey", mobileEntry.getCctKey());
            }
            XmlUtil.addXmlElement(strBldr, "Comment", mobileEntry.getComment() != null ? mobileEntry.getComment() : "");
            XmlUtil.addXmlElement(strBldr, "CrnCode", mobileEntry.getCrnCode());
            XmlUtil.addXmlElement(strBldr, "ExpKey", mobileEntry.getExpKey());
            XmlUtil.addXmlElement(strBldr, "LocationName", mobileEntry.getLocationName());
            if (mobileEntry.getMeKey() != null && mobileEntry.getMeKey().length() > 0) {
                XmlUtil.addXmlElement(strBldr, "MeKey", mobileEntry.getMeKey());
            }
            if (mobileEntry.getPctKey() != null && mobileEntry.getPctKey().length() > 0
                    && mobileEntry.getEntryType() != ExpenseTypeEnum.SMART_PERSONAL) {
                XmlUtil.addXmlElement(strBldr, "PctKey", mobileEntry.getPctKey());
            }
            if (mobileEntry.getReceiptImageId() != null && mobileEntry.getReceiptImageId().length() > 0 && !clearImage) {
                XmlUtil.addXmlElement(strBldr, "ReceiptImageId", mobileEntry.getReceiptImageId());
            }
            XmlUtil.addXmlElement(strBldr, "TransactionAmount", mobileEntry.getTransactionAmount());

            XmlUtil.addXmlElement(strBldr, "TransactionDate",
                    Format.safeFormatCalendar(Parse.LONG_YEAR_MONTH_DAY, mobileEntry.getTransactionDate()));

            XmlUtil.addXmlElement(strBldr, "VendorName", mobileEntry.getVendorName());
            strBldr.append("</MobileEntry>");
            postBody = strBldr.toString();
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildSaveMobileEntryPostBodyXML: mobile entry is null!");
        }

        return postBody;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.base.service.BaseAsyncRequestTask#parseStream(java.io.InputStream)
     */
    @Override
    protected int parseStream(InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {

                actionResponseParser = new ActionResponseParser();

                // register the parsers of interest
                parser.registerParser(mobileEntry, TAG_ME_KEY);
                parser.registerParser(actionResponseParser, ActionResponseParser.TAG_ACTION_STATUS);

                // Parse.
                parser.parse();

            } else {
                result = BaseAsyncRequestTask.RESULT_ERROR;
                Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: unable to construct common parser!");
            }
        } catch (XmlPullParserException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        } catch (IOException e) {
            result = BaseAsyncRequestTask.RESULT_ERROR;
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
                }
            }
        }
        return result;
    }

    @Override
    protected int onPostParse() {
        int result = super.onPostParse();

        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getContext());
        String userId = sessInfo.getUserId();

        // Set the values from 'actionResponseParser' into the result data.
        setActionResultIntoResultBundle(actionResponseParser);

        if (this.actionResponseParser.isSuccess()) {
            if (!this.mobileEntry.update(getContext(), userId)) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: unable to save mobile entry DAO object!");
                result = RESULT_ERROR;
            }

            // Set into the result bundle the Uri of the saved mobile entry object.
            resultData.putString(MOBILE_ENTRY_URI_KEY, mobileEntry.getContentURI(getContext()).toString());
            resultData.putString(MOBILE_ENTRY_ME_KEY, mobileEntry.getMeKey());
            resultData.putBoolean(MOBILE_ENTRY_CLEAR_IMAGE_KEY, clearImage);

        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onPostParse: no receipt DAO object was created!");
            result = RESULT_ERROR;
        }

        return result;
    }

}
