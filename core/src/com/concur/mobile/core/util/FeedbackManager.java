package com.concur.mobile.core.util;

import android.app.Activity;
import android.content.Context;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.storage.PersonManager;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;

public class FeedbackManager {

	private static FeedbackManager instance;
	private Activity activity;
	
	public static FeedbackManager with(Activity activity) {
		if (instance == null) {
			instance = new FeedbackManager(activity);
		}
		
		return instance;
	}
	
	public void showRatingsPrompt() {
		Apptentive.showRatingFlowIfConditionsAreMet(activity);
	}
	
	private FeedbackManager(Activity activity) {
		this.activity = activity;

		SessionInfo sessionInfo = ConfigUtil.getSessionInfo(activity);
		String userId = sessionInfo.getLoginId();

		setUserEmail(activity, userId);
	}
	
	public void setUserEmail(Context context, String email) {
        PersonManager.storePersonEmail(context, email);
        Person person = PersonManager.storePersonAndReturnDiff(context);
        
        if (person != null) {
            com.apptentive.android.sdk.Log.d("Person was updated.");
            com.apptentive.android.sdk.Log.v(person.toString());
            ApptentiveDatabase.getInstance(context).addPayload(person);
        } else {
            com.apptentive.android.sdk.Log.d("Person was not updated.");
        }
    }

}
