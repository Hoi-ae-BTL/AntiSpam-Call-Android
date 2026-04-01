package com.hoiaebtl.antispam_call_android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.Arrays;
import java.util.List;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "SafeCall_Core";

    // Danh sách đen giả lập
    private static final List<String> BLACKLIST = Arrays.asList("123456", "0999999999", "0123456789");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                
                if (incomingNumber != null) {
                    Log.d(TAG, "PHÁT HIỆN CUỘC GỌI ĐẾN: " + incomingNumber);
                    
                    if (isSpam(incomingNumber)) {
                        Log.w(TAG, "CẢNH BÁO: Số lừa đảo!");
                        
                        // KÍCH HOẠT OVERLAY CHO TUẤN
                        Intent overlayIntent = new Intent(context, OverlayService.class);
                        overlayIntent.putExtra("spam_number", incomingNumber);
                        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startService(overlayIntent);
                    }
                }
            }
        }
    }

    private boolean isSpam(String number) {
        for (String spamNum : BLACKLIST) {
            if (number.contains(spamNum)) {
                return true;
            }
        }
        return false;
    }
}