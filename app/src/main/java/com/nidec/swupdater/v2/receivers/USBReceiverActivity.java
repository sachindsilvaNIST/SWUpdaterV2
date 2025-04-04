package com.nidec.swupdater.v2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.hardware.usb.UsbDevice;

import android.util.Log;

import com.nidec.swupdater.v2.ui.RebootCheckActivity;


public class USBReceiverActivity extends BroadcastReceiver {

    private static final String TAG_USB_RECEIVER_ACTIVITY = "USBReceiverActivity";

    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {

            Log.d(TAG_USB_RECEIVER_ACTIVITY, "USB_DEVICE_ATTACHED!!!");

            Intent launchIntent = new Intent(context, RebootCheckActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        }
    }
}
