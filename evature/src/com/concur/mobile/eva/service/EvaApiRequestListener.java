package com.concur.mobile.eva.service;


/**
 * Provides a listener interface for use with an <code>EvaApiRequest</code> object.
 * 
 * @author andrewk
 */
public interface EvaApiRequestListener {

    /**
     * Gets whether or not the api request should use the Eva flow engine.
     * 
     * @return returns whether or not the api request should use the Eva flow engine.
     */
    public boolean useFlow();

    /**
     * Gets the session id to be used with api request.
     * 
     * @return returns the session id to be used with the api request.
     */
    public String getSessionId();

    /**
     * Sets the session id associated with the api request.
     * 
     * @param sessionId
     *            contains the Eva session id.
     */
    public void setSessionId(String sessionId);

    /**
     * Gets the Eva installation id to be used with an api request.
     * 
     * @return returns the Eva installation id to be used with an api request.
     */
    public String getInstallId();

    /**
     * Will show a generic error message in the event voice search can not be performed.
     */
    public void showErrorMessage();

    /**
     * Logs a Flurry Event in case an error occurs.
     * 
     * @param errorType
     *            either <code>Eva</code>, <code>SpeechRecognizer</code>, or <code>Other</code>.
     */
    public void logErrorFlurryEvent(String errorType);

    /**
     * Will handle a change in search context.
     */
    public void handleDifferentSearchContext();

    /**
     * Resets the UI to its default state. That is, will reset the current message to the default greeting message, switch back to
     * the default speak button, and clear out any search criteria text.
     * 
     * @param resetText
     *            if <code>true</code> will set the "chat" message text to the <code>greetingText</code>.
     */
    public void resetUI(boolean resetText);

    /**
     * Shows (and speaks) the given <code>text</code>.
     * 
     * @param text
     *            the text to show and speak
     */
    public void showResponseText(String text);

    /**
     * Will start the voice capture once any utterance has been completed.
     */
    public void startVoiceCaptureOnUtteranceCompleted();

    /**
     * Checks the <code>apiReply</code> and delegates to the appropriate search method (e.g. hotel or air search).
     * 
     * @param apiReply
     *            the <code>EvaApiReply</code> response from the Eva web service.
     */
    public void doSearch(EvaApiReply apiReply);

}
