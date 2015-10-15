/**
 * 
 */
package com.concur.mobile.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.receiptstore.service.ReceiptShareService;
import com.concur.mobile.core.util.Notifications;

/**
 * An extension of <code>BroadcastReceiver</code> for handling actions at device boot time.
 * 
 * @author andy
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    /*
     * (non-Javadoc)
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        // Launch the receipt store share service.
        Intent serviceIntent = new Intent(context, ReceiptShareService.class);
        // Set the flag indicating the device boot receiver is starting the service.
        serviceIntent.putExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_DEVICE_BOOT, true);
        context.startService(serviceIntent);

        // Launch the AWS Push Notification service.
        Notifications notifications = new Notifications(ConcurCore.getContext());
        notifications.initAWSPushService();
    }

}
