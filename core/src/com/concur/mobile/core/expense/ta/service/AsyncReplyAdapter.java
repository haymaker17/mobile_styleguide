package com.concur.mobile.core.expense.ta.service;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.core.dialog.DialogFragmentFactory;

public class AsyncReplyAdapter implements AsyncReplyListener {
	private FragmentActivity owner;
	
	public AsyncReplyAdapter(FragmentActivity owner) {
		this.owner = owner;
	}
	
	@Override
	public void onRequestSuccess(Bundle resultData) {
	}

	@Override
	public void onRequestFail(Bundle resultData) {
		showFailureDialog();
	}

	@Override
	public void onRequestCancel(Bundle resultData) {
	}

	@Override
	public void cleanup() {
	}

	public void showFailureDialog() {
	    DialogFragmentFactory.getAlertOkayInstance(R.string.general_network_error, R.string.general_error_message)
	    .show(owner.getSupportFragmentManager(), null);
	}
}
