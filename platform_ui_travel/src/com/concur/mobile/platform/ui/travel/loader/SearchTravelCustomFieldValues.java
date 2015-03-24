package com.concur.mobile.platform.ui.travel.loader;

import android.content.Context;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.XmlUtil;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author RatanK
 */
// moved from Platform as there is dependency on the Platform UI classes
// TODO - change to loader
public class SearchTravelCustomFieldValues extends PlatformAsyncRequestTask {

    public static final String TRAVEL_CUSTOM_FIELD = "travelCustomField";
    private static final String CLS_TAG = "SearchTravelCustomFieldValues";
    private String attributeId;
    private String searchPattern;
    private TravelCustomFieldsParser custFieldsParser;

    public SearchTravelCustomFieldValues(Context context, int id, BaseAsyncResultReceiver receiver, String attributeId,
            String searchPattern) {
        super(context, id, receiver);
        this.attributeId = attributeId;
        this.searchPattern = searchPattern;
    }

    @Override
    protected String getServiceEndPoint() {
        return "/Mobile/Config/SearchTravelCustomFieldValues";
    }

    @Override
    protected String getPostBody() {
        // form the the request XML
        StringBuilder sb = new StringBuilder();

        sb.append("<SearchTravelCustomField>");
        XmlUtil.addXmlElement(sb, "AttributeId", attributeId);
        XmlUtil.addXmlElement(sb, "SearchPattern", searchPattern);
        sb.append("</SearchTravelCustomField>");

        return sb.toString();
    }

    @Override
    protected int parseStream(InputStream is) {

        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            CommonParser parser = initCommonParser(is);
            if (parser != null) {

                custFieldsParser = new TravelCustomFieldsParser();

                // register the parsers of interest
                parser.registerParser(custFieldsParser, "TravelCustomFieldSearch");
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
        int resultcode = RESULT_OK;

        if (custFieldsParser != null && custFieldsParser.custField != null) {
            // add in the app object
            // ConcurCore core = (ConcurCore) ConcurCore.getContext();
            // core.setTravelCustomField(custFieldsParser.custField);
            // core.setTravelCustomFieldLastRetrieved(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            resultData.putSerializable(TRAVEL_CUSTOM_FIELD, custFieldsParser.custField);

        } else {
            // log the error message
            Log.e(Const.LOG_TAG, "Error occured while doing search");
            resultcode = RESULT_ERROR;
        }

        return resultcode;
    }
}
