package com.concur.mobile.core.config;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.config.gtm.ContainerHolderSingleton;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;

public class RuntimeConfig {

	private static final String CONTAINER_ID = "GTM-TM53XP";
	private static final String KEY_USE_APP_CENTER = "app_center_enable";

	private static RuntimeConfig instance;
	private TagManager tagManager;

	public static RuntimeConfig with(Context context) {
		if (instance == null) {
			instance = new RuntimeConfig(context);
		}

		return instance;
	}

	public boolean canUseAppCenter() {
		boolean answer = false;

		ContainerHolder holder = ContainerHolderSingleton.getContainerHolder();

		if (holder == null) {
			Log.e("CNQR", "RuntimeConfig: Holder is null.");
			return answer;
		}

		Container container = holder.getContainer();

		if (container == null) {
			Log.e("CNQR", "RuntimeConfig: Container is null.");
			return answer;
		}

		return container.getBoolean(KEY_USE_APP_CENTER);
	}

	public void load() {
		PendingResult<ContainerHolder> pending = tagManager
				.loadContainerPreferNonDefault(CONTAINER_ID,
						R.raw.gtm_tm53xp_v5);

		// The onResult method will be called as soon as one of the following
		// happens:
		// 1. a saved container is loaded
		// 2. if there is no saved container, a network container is loaded
		// 3. the request times out. The example below uses a constant to manage
		// the timeout period.
		pending.setResultCallback(new ResultCallback<ContainerHolder>() {
			@Override
			public void onResult(ContainerHolder containerHolder) {
				ContainerHolderSingleton.setContainerHolder(containerHolder);

				if (!containerHolder.getStatus().isSuccess()) {
					Log.e("CNQR", "Failure loading container: "
							+ containerHolder.getStatus().getStatusMessage());
					return;
				}

				Log.d("CNQR", "Loaded container: "
						+ containerHolder.toString());
				
				containerHolder.refresh();
			}
		}, 5, TimeUnit.SECONDS);
	}

	private RuntimeConfig(Context context) {
		tagManager = TagManager.getInstance(context);

		tagManager.setVerboseLoggingEnabled(true);
	}
}
