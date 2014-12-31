/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.eva.service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.concur.mobile.eva.R;
import com.concur.mobile.eva.data.EvaFlow;
import com.concur.mobile.eva.data.EvaFlow.EvaFlowType;
import com.concur.mobile.eva.data.EvaFlow.QuestionSubCategory;
import com.concur.mobile.eva.data.EvaFlow.StatementType;
import com.concur.mobile.eva.data.EvaTime;
import com.concur.mobile.eva.util.Const;
import com.concur.mobile.eva.util.Flurry;

/**
 * 
 * This task takes a URL string and uses it to create an HttpUrlConnection. Once the connection has been established, the
 * AsyncTask downloads the contents of the webpage as an InputStream. Finally, the InputStream is converted into a string, which
 * is displayed in the UI by the AsyncTask's onPostExecute method.
 * 
 * @author Chris N. Diaz
 */
public class EvaApiRequest extends AsyncTask<String, Integer, String> {

    public final static String CLS_TAG = EvaApiRequest.class.getSimpleName();

    /**
     * Flag to switch between the Eva PROD and DEV servers.
     */
    private final static boolean PROD_SERVER = true;

    private volatile boolean inProgress = false;
    private BookingSelection selection = null;

    private final Context context;

    private EvaApiRequestListener listener;

    private Location currentLocation;

    protected List<String> inputTextList;

    /**
     * Default constructor takes in a <code>VoiceSearchActivity</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param currentLocation
     *            contains a reference to a current location.
     * @param listener
     *            contains a reference to an <code>EvaApiRequestListener</code>.
     * 
     * @param inputTextList
     *            <code>List</code> of speech input text.
     */
    public EvaApiRequest(Context context, Location currentLocation, EvaApiRequestListener listener,
            List<String> inputTextList) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.listener = listener;
        this.inputTextList = inputTextList;
    }

    /**
     * Constructor takes in a <code>VoiceSearchActivity</code>.
     * 
     * @param context
     *            : Activity context
     * @param currentLocation
     *            contains a reference to a current location.
     * @param listener
     *            contains a reference to an <code>EvaApiRequestListener</code>.
     * @param selection
     *            : Selected booking type
     * @param inputTextList
     *            <code>List</code> of speech input text.
     */
    public EvaApiRequest(Context context, Location currentLocation, EvaApiRequestListener listener,
            BookingSelection selection, List<String> inputTextList) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.listener = listener;
        this.selection = selection;
        this.inputTextList = inputTextList;
    }

    public static enum BookingSelection {
        AIR("Air", "f", "fhc"), HOTEL("Hotel", "h", "fhc"), CAR("Car", "c", "fhc"), RAIL("Rail", "", "");

        private String name;
        private String searchContext;
        private String scope;

        /**
         * Constructor an instance of <code>BookingSelection</code>.
         * 
         * @param name
         *            the booking name.
         * @param searchContext
         *            search context(Air,car,hotel)
         */
        BookingSelection(String name, String searchContext, String scope) {
            this.name = name;
            this.searchContext = searchContext;
            this.scope = scope;
        }

        /**
         * Gets the name of this enum value.
         * 
         * @return the name of the enum value.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the voice search search context;
         * 
         * @return context{f,h,c} for eva voice search
         */
        public String getSearchContext() {
            return searchContext;
        }

        /**
         * Gets the scope of this enum value.
         * 
         * @return the scope of the enum value.
         */
        public String getScope() {
            return scope;
        }

    };

    /**
     * @param single
     *            argument that *must* be the supported language we are seaching in. e.g. "en_US"
     */
    @Override
    protected String doInBackground(String... args) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".doInBackground");

        inProgress = true;

        // params comes from the execute() call: params[0] is the url.
        String API_KEY = context.getString((PROD_SERVER) ? R.string.EVA_API_KEY : R.string.EVA_API_DEV_KEY);
        String SITE_CODE = context.getResources().getString(
                (PROD_SERVER) ? R.string.EVA_SITE_CODE : R.string.EVA_SITE_DEV_CODE);
        StringBuilder strBldrEvaURL = new StringBuilder("");
        String url = context.getResources().getString((PROD_SERVER) ? R.string.EVA_WS_URL : R.string.EVA_WS_DEV_URL);
        strBldrEvaURL.append(url);
        strBldrEvaURL.append("?site_code=").append(SITE_CODE);
        strBldrEvaURL.append("&api_key=").append(API_KEY);

        // MOB-12969 - Set the user's time-zone & unique ID to track statistics
        strBldrEvaURL.append("&time_zone=").append(EvaTime.getCurrentTimezoneOffset());
        strBldrEvaURL.append("&uid=").append(listener.getInstallId());

        // Add this "from future" parameter so we can get the new
        // hotel chain attributes: http://blog.evature.com/2013/05/changes-to-hotel-chains-format_1.html
        strBldrEvaURL.append("&ffi_chains");

        // MOB-17163 This allows Eva to correctly overcome speech recognition blunders, as well as disable
        // text-specific processing such as a spell checker, resulting in a faster, more accurate reply.
        strBldrEvaURL.append("&from_speech");

        // NOTE: When setting the language, we only need the country code if it's Chinese.
        // For all other languages, we only the language code. For more info see:
        // https://developers.google.com/translate/v2/using_rest#language-params
        String language = args[0];
        if (language != null && !language.startsWith("zh-")) {
            language = language.substring(0, 2);
        }

        strBldrEvaURL.append("&language=").append(language);
        try {
            for (String inputText : inputTextList) {
                strBldrEvaURL.append("&input_text=").append(URLEncoder.encode(inputText, "UTF-8"));
            }

        } catch (UnsupportedEncodingException e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doInBackground - couldn't encode URL.", e);
        }

        // Set the user's current lat/long of where "home" is.
        Location currLoc = currentLocation;
        if (currLoc != null) {
            double latitude = currLoc.getLatitude();
            double longitude = currLoc.getLongitude();
            if (latitude != 0 && longitude != 0) {
                strBldrEvaURL.append("&longitude=").append(longitude).append("&latitude=").append(latitude);
            }
        }

        // Set random request ID.
        Random rand = new Random();
        String rid = Long.toString(rand.nextLong()).substring(2, 6); // We only want a positive 4 digits long.
        strBldrEvaURL.append("&rid=").append(rid);

        // Set the session ID to enable "Flow".
        if (listener.useFlow()) {
            strBldrEvaURL.append("&session_id=").append(listener.getSessionId());
        }

        String context = null;
        String scope = null;
        if (selection != null) {
            context = selection.getSearchContext();
            scope = selection.getScope();
            strBldrEvaURL.append("&scope=").append(scope);
            strBldrEvaURL.append("&context=").append(context);
            Log.e(Const.LOG_TAG, CLS_TAG + "name = : " + selection.getName() + " context = : " + context
                    + " scope = : " + scope);
        }

        // add ffi_statement
        strBldrEvaURL.append("&ffi_statement");
        Log.e(Const.LOG_TAG, CLS_TAG + "url = : " + strBldrEvaURL.toString());
        Log.e(Const.LOG_TAG, CLS_TAG + "sessionid = : " + listener.getSessionId());
        // Perform the HTTP GET request.
        HttpURLConnection connection = null;

        try {
            URL evaUrl = new URL(strBldrEvaURL.toString());
            connection = (HttpURLConnection) evaUrl.openConnection();

            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                InputStream is = new BufferedInputStream(connection.getInputStream());
                Reader r = new InputStreamReader(is, "UTF-8");
                StringBuilder buf = new StringBuilder();
                while (true) {
                    int ch = r.read();
                    if (ch < 0)
                        break;
                    buf.append((char) ch);
                }
                String str = buf.toString();
                return str;
            }

            return null;

        } catch (Exception e) {
            inProgress = false;

            Log.e(Const.LOG_TAG, CLS_TAG + ".doInBackground() - error invoking Eva web service.", e);
            return "Unable to retrieve web page. URL may be invalid.";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 
     * @param apiReply
     */
    protected boolean handleSayIt(EvaApiReply apiReply) {

        if (apiReply.sayIt != null) {
            String say_it = apiReply.sayIt;
            if (say_it != null && say_it.trim().length() > 0) {

                // Update the UI.
                // context.setText(say_it, true);

                return true;

            } else {
                Log.w(Const.LOG_TAG, CLS_TAG + ".handleSayIt() - EvaApiReply has null response.");
                listener.showErrorMessage();

                listener.logErrorFlurryEvent(Flurry.PARAM_VALUE_OTHER);

                return false;
            }
        }

        return true;
    }

    protected boolean handleFlowSayIt(EvaFlowApiReply flowReply) {

        if (flowReply.flows != null) {

            EvaFlow actionFlow = null;
            // Get the first non Hotel, Car, and Flight Flow type.
            for (EvaFlow flow : flowReply.flows) {
                if (flow.type != EvaFlowType.CAR && flow.type != EvaFlowType.HOTEL && flow.type != EvaFlowType.FLIGHT) {

                    actionFlow = flow;
                    break;
                }
            }

            if (actionFlow != null && actionFlow.sayIt != null && actionFlow.sayIt.trim().length() > 0) {

                boolean doSearch = false;
                String sayIt;
                switch (actionFlow.type) {

                case QUESTION:
                case STATEMENT:
                    if (actionFlow.questionSubCategory == QuestionSubCategory.Unsupported
                            || (actionFlow.stmtType == StatementType.Unsupported)) {
                        listener.handleDifferentSearchContext();
                        sayIt = getCustomSayItMsg();
                    } else {
                        // Reset the speak button, but not the chat text.
                        listener.resetUI(false);
                        sayIt = actionFlow.sayIt;
                    }
                    // Show and say the Eva Question.
                    listener.showResponseText(sayIt);
                    // Prompt for an answer after TTS has completed.
                    listener.startVoiceCaptureOnUtteranceCompleted();
                    break;

                case ANSWER:

                    break;

                default:
                    // Undefined!!!
                    Log.d(Const.LOG_TAG, CLS_TAG + ".handleFlowSayIt: Undefined Flow Type!");

                    listener.showErrorMessage();

                    listener.logErrorFlurryEvent(Flurry.PARAM_VALUE_OTHER);

                    return false;

                } // end switch-case

                return doSearch;

            }

        }

        return true;
    }

    /**
     * Get custom message for different search context.
     * 
     * @return : Returns a custom message for user, so user can continue with their search within the search
     *         context(Air,Car,Hotel).
     */
    protected String getCustomSayItMsg() {
        StringBuilder strBldr = new StringBuilder(context.getString(R.string.voice_search_not_supported_category));
        strBldr.append(" ");
        switch (selection) {
        case AIR:
            strBldr.append(context.getString(R.string.voice_air_search_different_scope));
            break;
        case CAR:
            strBldr.append(context.getString(R.string.voice_car_search_different_scope));
            break;
        case HOTEL:
            strBldr.append(context.getString(R.string.voice_hotel_search_different_scope));
            break;
        default:
            break;
        }
        return strBldr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".onPostExecute: Got EVA Response!");

        inProgress = false;

        EvaApiReply apiReply = null;
        boolean performSearch = false;

        // Handle Flow engine differently.
        if (listener.useFlow()) {

            apiReply = new EvaFlowApiReply(result);
            listener.setSessionId(((EvaFlowApiReply) apiReply).sessionId);

            if (!apiReply.isSuccessfulParse()) {
                listener.resetUI(false);
                listener.showErrorMessage();
                listener.logErrorFlurryEvent(Flurry.PARAM_VALUE_EVA);

                return;
            }

            performSearch = handleFlowSayIt((EvaFlowApiReply) apiReply); // Say (using TTS) the eva reply

        } else {

            apiReply = new EvaApiReply(result);

            if (!apiReply.isSuccessfulParse()) {
                listener.resetUI(false);
                listener.showErrorMessage();
                listener.logErrorFlurryEvent(Flurry.PARAM_VALUE_EVA);

                return;
            }

            performSearch = handleSayIt(apiReply); // Say (using TTS) the eva reply
        }

        if (performSearch) {
            // Invoke Concur MWS to perform appropriate search.
            listener.doSearch(apiReply);
        }
    }

    /**
     * Returns whether or not an Eva request is in progress.
     * 
     * @return <code>true</code> if the task is currently in progress, i.e. making the Eva HTTP GET request.
     */
    public boolean inProgress() {
        return inProgress;
    }

    /**
     * Cancels the current request.
     */
    public void cancelReqeust() {
        inProgress = false;
        cancel(true);
    }

} // end CallEvaTask
