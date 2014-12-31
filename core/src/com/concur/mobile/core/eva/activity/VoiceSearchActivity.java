/**
 * Copyright (c) 2011 Concur Technologies, Inc.
 */
package com.concur.mobile.core.eva.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.activity.AbstractTravelSearchProgress;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.eva.data.EvaTime;
import com.concur.mobile.eva.service.EvaAirReply;
import com.concur.mobile.eva.service.EvaApiReply;
import com.concur.mobile.eva.service.EvaApiRequest;
import com.concur.mobile.eva.service.EvaApiRequest.BookingSelection;
import com.concur.mobile.eva.service.EvaApiRequestListener;
import com.concur.mobile.eva.service.EvaCarReply;
import com.concur.mobile.eva.service.EvaHotelReply;

/**
 * Main activity for searching Air, Hotel, Rail, etc. via Voice and Evature API.
 * 
 * @author Chris N. Diaz
 * 
 */
public abstract class VoiceSearchActivity extends AbstractTravelSearchProgress implements TextToSpeech.OnInitListener,
        EvaApiRequestListener {

    public final static String CLS_TAG = VoiceSearchActivity.class.getSimpleName();

    private static final int RESULT_SPEECH = 1;

    // List of languages the Eva API currently supports.
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(new String[] { "en", "es" });

    protected Intent resultsIntent;
    protected Intent noResultsIntent;

    protected ImageButton speakButton;
    protected TextSwitcher textSwitcher;
    protected TextSwitcher responseTextSwitcher;
    protected View responseTextViewLayout;
    protected View searchingLayout;
    protected MediaPlayer mPlayer;
    protected volatile boolean isDestroyed = false;
    protected EvaApiRequest currentEvaRequest;
    protected Spanned greetingText;
    protected View criteriaLayout;

    protected TextToSpeech tts;
    private Intent speechIntent;
    protected SimpleDateFormat voiceDateFormat;

    protected String genericErrorMessage;

    protected Animation leftInAnim;
    protected Animation rightInAnim;

    private Locale voiceLocale;
    private String recognizerLanguage;
    private boolean performedSearch;
    protected String sessionId = "1"; // Passed to Eva for the new "Flow"

    private boolean useFlowEngine = true; // Set this to false to disable the "Flow Engine".

    /**
     * Gets the default greeting "chat" message text.
     * 
     * @return the default greeting "chat" message text.
     */
    protected abstract Spanned getGreetingText();

    /**
     * Cancels any pending MWS requests.
     */
    protected abstract void cancelMwsRequests();

    /**
     * This book type, e.g. Hotel, Air, Car, or Rail.
     * 
     * @return this book type, e.g. Hotel, Air, Car, or Rail.
     */
    protected abstract BookingSelection getBookType();

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_book);

        // Set the header.
        getSupportActionBar().setTitle(R.string.home_action_voice_book);

        genericErrorMessage = getString(R.string.voice_book_unable_to_search);
        greetingText = getGreetingText();

        // Initialize the voice search language.
        String[] savedLanguage = getVoiceLocale().split("_");
        String country = "US";
        String language = "en";
        if (savedLanguage.length == 2) {
            country = savedLanguage[1];
            language = savedLanguage[0];
        }

        // This may look weird, but we need two references of the language/locale
        // because the TTS accepts a Locale object while the RecognizerIntent
        // and Eva API accepts a hyphenated language-code String.
        voiceLocale = new Locale(language, country);
        recognizerLanguage = language + "-" + country;

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Use "web search" type of voice recognition, i.e. not so strict or accurate as free-form.
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, recognizerLanguage);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, recognizerLanguage);
        // speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        // Set the conversation text switcher.
        textSwitcher = (TextSwitcher) findViewById(R.id.voiceBookTextSwitcher);
        textSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            /*
             * (non-Javadoc)
             * 
             * @see android.widget.ViewSwitcher.ViewFactory#makeView()
             */
            @Override
            public View makeView() {

                TextView t = new TextView(VoiceSearchActivity.this);

                // Convert 10dp to pixels
                Resources r = getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
                t.setPadding(px, px, px, px);

                t.setBackgroundResource(R.drawable.transparent_voice_text_view);
                t.setTextAppearance(getApplicationContext(), R.style.VoiceTextboxPrimary);

                return t;
            }

        });

        // Create animations for the TextView
        leftInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        rightInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        // Set up the response text switcher.
        responseTextViewLayout = findViewById(R.id.conversationResponseLayout);
        responseTextSwitcher = (TextSwitcher) responseTextViewLayout.findViewById(R.id.responseTextSwitcher);
        responseTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            /*
             * (non-Javadoc)
             * 
             * @see android.widget.ViewSwitcher.ViewFactory#makeView()
             */
            @Override
            public View makeView() {

                TextView t = new TextView(VoiceSearchActivity.this);

                // Convert 10dp to pixels
                Resources r = getResources();
                int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
                t.setPadding(px, px, px, px);

                t.setBackgroundResource(R.drawable.transparent_voice_text_view);
                t.setTextAppearance(getApplicationContext(), R.style.VoiceTextboxPrimary);

                return t;
            }

        });
        responseTextSwitcher.setInAnimation(leftInAnim);

        // Initialize the button to launch the STT
        speakButton = (ImageButton) findViewById(R.id.voiceBookSpeakButton);
        speakButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // sessionId = "1"; // Always reset the Session ID when doing a manual search.
                startVoiceCapture();
            }
        });

        mPlayer = MediaPlayer.create(VoiceSearchActivity.this, R.raw.speak_now);
        mPlayer.setVolume(1.0f, 1.0f);

        // TTS
        tts = new TextToSpeech(this, this);
        tts.setSpeechRate(1.2f); // Speed up the voice just a tad bit.

        // DateFormat used by the TTS so it's not so verbose.
        voiceDateFormat = new SimpleDateFormat("MMMM d", Locale.getDefault());

        // Reference to the progress layout so we can easily show/hide it.
        searchingLayout = findViewById(R.id.voiceBookProgressLayout);

        // Set the initial UI & greeting text.
        resetUI(true);

    } // onCreate()

    /*
     * (non-Javadoc)
     * 
     * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
     */
    @Override
    public void onInit(int status) {

        // After initializing the TTS, try to set the language.
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(voiceLocale);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");

            Toast t = Toast.makeText(getApplicationContext(), R.string.voice_book_speech_to_text_unsupported,
                    Toast.LENGTH_LONG);
            t.show();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.activity.BaseActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {

        isDestroyed = true;

        // Cancel any running searches.
        cancelSearch(false);

        // Don't forget to shutdown TTS!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        // Don't want to hog resources!
        if (mPlayer != null) {
            mPlayer.release();
        }

        // Log Flurry event that the user launched Voice Search
        // but didn't actually search for anything.
        if (!performedSearch) {
            Map<String, String> params = new HashMap<String, String>();
            BookingSelection bookType = getBookType();
            params.put(Flurry.PARAM_NAME_TYPE, bookType.getName());
            EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, Flurry.EVENT_NAME_USAGE_CANCELLED, params);
        }

        super.onDestroy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onAttachedToWindow()
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // If a search is in progress, then pressing the BACK key
        // cancels the search.
        if (keyCode == KeyEvent.KEYCODE_BACK && isSearching()) {
            cancelSearch(true);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case RESULT_SPEECH: {

            // If we get results back from the Android STT,
            // get the first result and invoke the Eva API.
            if (resultCode == RESULT_OK && data != null) {

                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!text.isEmpty()) {

                    // Change the text view arrow to the right,
                    // indicating what the user just said.
                    switchTextViewArrow(true);

                    String firstUtterance = text.get(0);
                    setText(firstUtterance, true);

                    // Check for connectivity, if none, then display dialog and return.
                    if (!ConcurCore.isConnected()) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                        return;
                    }

                    // Hide the SpeakButton and show the animated progress button.
                    switchSpeakButton(true);

                    // Also hide the button hint text.
                    View buttonHint = findViewById(R.id.voiceBookHintText);
                    if (buttonHint != null && buttonHint.getVisibility() == View.VISIBLE) {
                        buttonHint.setVisibility(View.INVISIBLE);
                    }

                    // Send text to Eva WebService.
                    Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: Calling Eva!");
                    BookingSelection bookType = getBookType();
                    ConcurCore core = getConcurCore();
                    currentEvaRequest = new EvaApiRequest(core, core.getCurrentLocation(), this, bookType, text);
                    currentEvaRequest.execute(recognizerLanguage);

                } else {
                    logFailedUsageFlurryEvent();
                    logErrorFlurryEvent(Flurry.PARAM_VALUE_SPEECH_RECOGNIZER);

                    showErrorMessage();
                }

            } else if (resultCode != RESULT_CANCELED) {
                showErrorMessage();
            }

            break;
        }

        } // switch-case

    } // onActivityResult()

    /**
     * Sets a new Eva session Id.
     * 
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 
     * @return the current Eva session Id.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns <code>true</code> if we want to use the new Eva "Flow" engine.
     * 
     * @return <code>true</code> if we want to use the new Eva "Flow" engine.
     */
    public boolean useFlow() {
        return useFlowEngine;
    }

    @Override
    public String getInstallId() {
        return Preferences.getInstallID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#getResultsIntent()
     */
    @Override
    public Intent getResultsIntent() {
        return resultsIntent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.travel.air.activity.AbstractTravelSearchProgress#getNoResultsIntent()
     */
    @Override
    public Intent getNoResultsIntent() {
        return noResultsIntent;
    }

    /**
     * Sets the current "chat" message text.
     * 
     * @param text
     *            the text message to set.
     * @param isUserText
     *            if <code>true</code>, shows the message text arrow on the right, as if the user were saying the message.
     *            Otherwise, the message text arrow will be on the left, indicating the app is saying the message.
     */
    public void setText(CharSequence text, boolean isUserText) {

        // Hide the error text (if showing).
        if (responseTextViewLayout.getVisibility() == View.VISIBLE) {
            responseTextViewLayout.setVisibility(View.GONE);
        }

        // Switch which side the text should come from.
        if (isUserText) {
            textSwitcher.setInAnimation(rightInAnim);
            switchTextViewArrow(true);
        } else {
            textSwitcher.setInAnimation(leftInAnim);
            switchTextViewArrow(false);
        }

        textSwitcher.setText(text);
    }

    /**
     * Show a generic error toast if we are unable to perform a voice search.
     */
    public void showErrorMessage() {

        showErrorMessage(genericErrorMessage);
    }

    /**
     * Show's (and speaks) the given errorMessage.
     * 
     * @param errorMessage
     *            the error message to show/speak.
     */
    public void showErrorMessage(String errorMessage) {
        // Reset the speak button, but not the chat text.
        resetUI(false);

        showResponseText(errorMessage);
    }

    /**
     * Shows (and speaks) the given <code>msg</code>.
     * 
     * @param msg
     *            the message to show and speak
     */
    public void showResponseText(final String msg) {

        // MOB-14271 - For OS 2.3 and older, there seems to be a problem with threading.
        // Handle UI changes in on the main UI thread or else you will get the following:
        // "CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views."
        runOnUiThread(new Runnable() {

            public void run() {

                if (responseTextViewLayout.getVisibility() != View.VISIBLE) {
                    responseTextViewLayout.setVisibility(View.VISIBLE);
                }

                responseTextSwitcher.setText(msg);

                if (tts != null) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Double.toString(Math.random()));
                    tts.speak(msg, TextToSpeech.QUEUE_FLUSH, params);
                }
            }
        });
    }

    /**
     * Returns <code>true</code> if the TTS is currently speaking.
     * 
     * @return <code>true</code> if the TTS is currently speaking.
     */
    public boolean isSpeaking() {
        if (tts != null) {
            return tts.isSpeaking();
        }

        return false;
    }

    /**
     * Subclasses should override this to handle searching for hotels.
     * 
     * @param hotelSearch
     * @throws NoSuchMethodError
     *             - thrown if the sub-classes has not overridden this method and it is invoked.
     */
    protected void doHotelSearch(EvaHotelReply hotelSearch) throws NoSuchMethodError {
        throw new NoSuchMethodError("Voice Hotel Search Not Implemented.");
    } // doHotelSearch()

    /**
     * Subclasses should override this to handle searching for flights.
     * 
     * @param airReply
     * @throws NoSuchMethodError
     *             - thrown if the sub-classes has not overridden this method and it is invoked.
     */
    protected void doAirSearch(EvaAirReply airReply) throws NoSuchMethodError {
        throw new NoSuchMethodError("Voice Air Search Not Implemented.");
    } // doAirSearch()

    /**
     * Subclasses should override this to handle searching for rail.
     * 
     * @param apiReply
     * @throws NoSuchMethodError
     *             - thrown if the sub-classes has not overridden this method and it is invoked.
     */
    protected void doRailSearch(EvaApiReply apiReply) throws NoSuchMethodError {
        throw new NoSuchMethodError("Voice Rail Search Not Implemented.");
    } // doRailSearch()

    /**
     * Subclasses should override this to handle searching for car.
     * 
     * @param carReply
     * @throws NoSuchMethodError
     *             - thrown if the sub-classes has not overridden this method and it is invoked.
     */
    protected void doCarSearch(EvaCarReply apiReply) throws NoSuchMethodError {
        throw new NoSuchMethodError("Voice Car Search Not Implemented.");
    } // doCarSearch()

    public void startVoiceCaptureOnUtteranceCompleted() {

        tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {

            @Override
            public void onUtteranceCompleted(String utteranceId) {
                if (!isDestroyed) {
                    startVoiceCapture();
                }

                // After starting the Voice Capture, be sure to remove it!
                if (tts != null) {
                    tts.setOnUtteranceCompletedListener(null);
                }
            }
        });
    }

    /**
     * Launches the speech recognizer.
     */
    public void startVoiceCapture() {

        if (isDestroyed) {
            return;
        }

        // Flurry flag for checking if user actually performed a search,
        // or at least attempted to.
        performedSearch = true;

        try {

            // Stop the TTS if it's saying something.
            if (tts != null && tts.isSpeaking()) {
                tts.stop();
            }

            startActivityForResult(speechIntent, RESULT_SPEECH);

            // Play a beep sound so user knows to start talking...
            // Be sure it's on the UI thread!!!
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mPlayer != null && !isDestroyed) {
                                mPlayer.start();
                            }
                        }
                    }, 500);
                }
            });

        } catch (ActivityNotFoundException a) {

            logFailedUsageFlurryEvent();

            // If Android device/OS doesn't support Speech-to-Text,
            // then show error toast.
            Toast t = Toast.makeText(getApplicationContext(), R.string.voice_book_speech_to_text_unsupported,
                    Toast.LENGTH_LONG);
            t.show();
        }

    } // speakNow();

    /**
     * Gets the voice search language saved in the Preference store. If none is set, then this defaults to the device locale. If
     * the device locale is not supported by <code>SUPPORTED_LANGUAGES</code> then <code>en_US</code> is returned.
     * 
     * @return the locale code, e.g. <code>en_US</code>
     */
    private String getVoiceLocale() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String savedLocale = prefs.getString(Const.PREF_VOICE_SEARCH_LANGUAGE, null);

        // If a language wasn't specified, then default to the device locale.
        if (savedLocale == null) {
            savedLocale = Locale.getDefault().toString();
        }

        // If the language is not supported by Eva,
        // then the last resort is to use en_US.
        String lang = savedLocale.substring(0, 2);
        if (SUPPORTED_LANGUAGES.contains(lang)) {
            return savedLocale;
        }

        return Locale.US.toString();
    }

    /**
     * Convenience method to show the "chat" text message arrow on the left or right.
     * 
     * @param showRightArrow
     *            - if <code>true</code> will show the arrow on the right side and hide the left arrow. If <code>false</code>, the
     *            left arrow will be displayed while the right arrow is hidden.
     */
    private void switchTextViewArrow(boolean showRightArrow) {

        if (showRightArrow) {
            findViewById(R.id.voiceBookArrowLeft).setVisibility(View.GONE);
            findViewById(R.id.voiceBookArrowRight).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.voiceBookArrowLeft).setVisibility(View.VISIBLE);
            findViewById(R.id.voiceBookArrowRight).setVisibility(View.GONE);
        }
    }

    /**
     * Convenience method to show the either the speak button or the animated searching button.
     * 
     * @param showSearchingButton
     *            - if <code>true</code>, will show the animated searching button. Otherwise, will show the default speak button.
     */
    private void switchSpeakButton(boolean showSearchingButton) {

        if (showSearchingButton) {
            speakButton.setVisibility(View.GONE);
            searchingLayout.setVisibility(View.VISIBLE);
        } else {
            speakButton.setVisibility(View.VISIBLE);
            searchingLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Resets the UI to its default state. That is, will reset the current message to the default greeting message, switch back to
     * the default speak button, and clear out any search criteria text.
     * 
     * @param resetText
     *            if <code>true</code> will set the "chat" message text to the <code>greetingText</code>.
     */
    public void resetUI(final boolean resetText) {

        // MOB-14271 - For OS 2.3 and older, there seems to be a problem with threading.
        // Handle UI changes in on the main UI thread or else you will get the following:
        // "CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views."
        runOnUiThread(new Runnable() {

            public void run() {

                switchSpeakButton(false);

                if (resetText) {
                    switchTextViewArrow(false);
                    setText(greetingText, false);
                    // If the user backs out of a search completely, we don't want to retain the session.
                    sessionId = "1";
                }

                if (criteriaLayout != null) {
                    criteriaLayout.setVisibility(View.GONE);
                }
                View buttonHint = findViewById(R.id.voiceBookHintText);
                if (buttonHint != null) {
                    buttonHint.setVisibility(View.VISIBLE);
                }

                // Also clear out the extra Intent data.
                if (resultsIntent != null) {
                    resultsIntent.replaceExtras((Bundle) null);
                }

                if (noResultsIntent != null) {
                    noResultsIntent.replaceExtras((Bundle) null);
                }
            }
        });
    }

    /**
     * Checks the <code>apiReply</code> and delegates to the appropriate search method (e.g. hotel or air search).
     * 
     * @param apiReply
     *            the <code>EvaApiReply</code> response from the Eva web service.
     */
    public void doSearch(final EvaApiReply apiReply) {

        if (apiReply == null) {
            return;
        }

        // Check for connectivity, if none, then display dialog and return.
        if (!ConcurCore.isConnected()) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
            return;
        }

        String searchType = "Unkown";
        try {
            if (apiReply.isHotelSearch()) {
                // Perform Concur MWS Hotel search.
                searchType = "Hotel";
                Log.d(Const.LOG_TAG, CLS_TAG + ".onPostExecute: Running Hotel Search...");
                ConcurCore concurCore = getConcurCore();
                doHotelSearch(new EvaHotelReply(ConcurCore.getContext(), concurCore.getCurrentLocation(),
                        concurCore.getCurrentAddress(), apiReply));
                logSuccessUsageFlurryEvent();

            } else if (apiReply.isFlightSearch()) {
                // Perform Concur MWS Air search.
                searchType = "Air";
                Log.d(Const.LOG_TAG, CLS_TAG + ".onPostExecute: Running Flight Search...");
                doAirSearch(new EvaAirReply(ConcurCore.getContext(), Const.AIR_SEAT_CLASS_ECONOMY,
                        Const.AIR_SEAT_CLASS_PREMIUM_ECONOMY, Const.AIR_SEAT_CLASS_BUSINESS,
                        Const.AIR_SEAT_CLASS_FIRST, LocationChoice.KEY_NAME, LocationChoice.KEY_LATITUDE,
                        LocationChoice.KEY_LONGITUDE, apiReply));
                logSuccessUsageFlurryEvent();

            } else if (apiReply.isCarSearch()) {
                // Perform Concur MWS Car search.
                searchType = "Car";
                Log.d(Const.LOG_TAG, CLS_TAG + ".onPostExecute: Running Car Search...");
                ConcurCore concurCore = getConcurCore();
                doCarSearch(new EvaCarReply(ConcurCore.getContext(), concurCore.getCurrentLocation(),
                        concurCore.getCurrentAddress(), apiReply));
                logSuccessUsageFlurryEvent();

            } else {
                Log.w(Const.LOG_TAG, CLS_TAG + ".onPostExecute: Invalid search type.");
                showErrorMessage();
            }

        } catch (IllegalArgumentException e) {

            Log.e(Const.LOG_TAG, CLS_TAG + ".doSearch() - error performing search.", e);

            String msg = e.getMessage();
            if (msg != null && msg.length() > 0) {
                // Reset the speak button, but not the chat text.
                resetUI(false);
                showResponseText(e.getMessage());
            } else {
                showErrorMessage();
            }

        } catch (NoSuchMethodError e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doSearch() - trying to search for " + searchType + " while in " + CLS_TAG);
            showErrorMessage();

        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doSearch() - error performing search.", e);
            showErrorMessage();

            logErrorFlurryEvent(Flurry.PARAM_VALUE_OTHER);
        }

    } // doSearch()

    public void handleDifferentSearchContext() {
        // reset your session id
        sessionId = "1";
        resetUI(false);
    }

    /**
     * Cancels the current search.
     */
    protected void cancelSearch(boolean showErrorToast) {
        // reset your session id
        sessionId = "1";
        // Stop the TTS.
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }

        // Cancel the Eva request.
        if (currentEvaRequest != null && currentEvaRequest.inProgress()) {
            currentEvaRequest.cancelReqeust();
        }

        // Cancel any MWS requests.
        cancelMwsRequests();

        // Need to rest the UI
        resetUI(true);

        if (showErrorToast) {
            // Show toast that the search was canceled.
            Toast t = Toast.makeText(getApplicationContext(), R.string.voice_search_canceled, Toast.LENGTH_SHORT);
            t.show();
        }

        logFailedUsageFlurryEvent();

    } // cancelSearch()

    /**
     * 
     * @return <code>true</code> if we're in the middle of an EVA request or MWS request.
     * 
     */
    protected boolean isSearching() {

        if (currentEvaRequest != null && currentEvaRequest.inProgress()) {

            return true;
        }

        if (getSearchRequest() != null && getSearchRequest().isProcessing) {

            return true;
        }

        return false;
    }

    /**
     * Parses the eva date into the shortened format, MMMM d, that should be used in the TTS.
     * 
     * @param evaDate
     *            the eva date in MM/DD/YYYY
     * 
     * @return the shortened date format, MMMM d, that should be used in the TTS.
     */
    protected String toVoiceDateFormat(String evaDate) {
        if (evaDate != null) {
            return voiceDateFormat.format(EvaTime.convertToConcurServerCalendar(evaDate, null).getTime());
        }
        return evaDate;
    }

    /**
     * Parses the location to get the short name for passing to TTS.
     * 
     * @param location
     *            a location name, e.g. Portland, Oregon, US
     * @return
     */
    protected String getShortLocationName(String location) {
        if (location != null) {
            int index = location.indexOf(",");
            if (index > 0) {
                return location.substring(0, index);
            }
        }
        return location;
    }

    /**
     * Logs a Flurry Event in case an error occurs.
     * 
     * @param errorType
     *            either <code>Eva</code>, <code>SpeechRecognizer</code>, or <code>Other</code>.
     */
    public void logErrorFlurryEvent(String errorType) {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Flurry.PARAM_NAME_ERROR_TYPE, errorType);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, "Error " + getBookType(), params);
    }

    /**
     * Convenience method to log a "usage failed" Flurry Event.
     */
    protected void logFailedUsageFlurryEvent() {
        Map<String, String> params = new HashMap<String, String>();
        BookingSelection bookType = getBookType();
        params.put(Flurry.PARAM_NAME_TYPE, bookType.getName());
        params.put(Flurry.PARAM_NAME_WORKED, Flurry.PARAM_VALUE_NO);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, Flurry.EVENT_NAME_USAGE_SUCCESS,params);
    }

    /**
     * Convenience method to log a "usage failed" Flurry Event. That is, when a user successfully spoke something and the Eva API
     * returned valid results.
     */
    protected void logSuccessUsageFlurryEvent() {
        Map<String, String> params = new HashMap<String, String>();
        BookingSelection bookType = getBookType();
        params.put(Flurry.PARAM_NAME_TYPE, bookType.getName());
        params.put(Flurry.PARAM_NAME_WORKED, Flurry.PARAM_VALUE_YES);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_VOICE_BOOK, Flurry.EVENT_NAME_USAGE_SUCCESS,params);
    }

} // VoiceSearchActivity

