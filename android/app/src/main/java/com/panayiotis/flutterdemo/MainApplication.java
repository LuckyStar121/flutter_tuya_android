package com.panayiotis.flutterdemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;

public class MainApplication extends Application {
    private static Context mContext;

    private static final String TAG = "LonghuanApp";
    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        L.d(TAG, "onCreate " + getProcessName(this));
        L.setSendLogOn(true);

        TuyaSdk.init(this);
        TuyaSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
//                Intent intent = new Intent(context, LoginActivity.class);
//                if (!(context instanceof Activity)) {
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                }
//                startActivity(intent);
            }
        });
        TuyaHomeSdk.setDebugMode(true);
    }

    public static String getProcessName(Context context){
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    public static Context getAppContext() {
        return mContext;
    }
}
