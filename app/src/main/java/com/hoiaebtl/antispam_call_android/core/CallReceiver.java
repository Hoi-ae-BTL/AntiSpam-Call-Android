package com.hoiaebtl.antispam_call_android.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG ="SafeCall_Core";
    @Override
    public void onReceive(Context context, Intent intent){
        //Kiem tra xem su kien co dung la thay doi trang thai cuoc goi khong
        if(intent.getAction()!= null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
            //Lay trang thai hien tai
            String state =intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            //Neu trang thai la "Dang do chuong"
            if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
                String incomingNumber=intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if(incomingNumber!=null){
                    Log.d(TAG,"PHAT HIEN CUOC GOI DEN: "+incomingNumber);
                    Toast.makeText(context,"Dang kiem tra so"+incomingNumber,Toast.LENGTH_SHORT).show();

                }
            }

        }



    }
}
