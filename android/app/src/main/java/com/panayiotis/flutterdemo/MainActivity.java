package com.panayiotis.flutterdemo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.device.utils.WiFiUtil;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.tuya.smart.sdk.api.IResultCallback;

import java.lang.reflect.Method;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {

  private static final String CHANNEL = "flutter.native/login";
  private static final String REGISTER_CHANNEL = "flutter.register/register";
  private static final String WIFI_CHANNEL = "flutter.wifi/getssid";
  public static final int CODE_FOR_LOCATION_PERMISSION = 222;
  private Context mContext;
  MethodChannel.Result wifiResult;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
    mContext = this;

    L.setSendLogOn(true);

    TuyaSdk.init(getApplication());
    TuyaSdk.setOnNeedLoginListener(new INeedLoginListener() {
      @Override
      public void onNeedLogin(Context context) {
//        Intent intent = new Intent(context, LoginActivity.class);
//        if (!(context instanceof Activity)) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        startActivity(intent);
      }
    });

    TuyaHomeSdk.setDebugMode(true);

    new MethodChannel(getFlutterView(), CHANNEL).setMethodCallHandler(
      new MethodChannel.MethodCallHandler() {
        @Override
        public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
          if (methodCall.method.equals("getTestString")) {
            String strName = methodCall.argument("name");
            String strPass = methodCall.argument("pass");
            String strPhoneCode = methodCall.argument("phonecode");
            Log.e("Str_PHONE_CODE_Login", strPhoneCode);
            TuyaHomeSdk.getUserInstance().loginWithEmail(strPhoneCode, strName, strPass, new ILoginCallback() {
              @Override
              public void onSuccess(User user) {
                result.success("Success");
              }

              @Override
              public void onError(String s, String s1) {
                result.error("UNAVAILABLE", "Email and Password maybe wrong.", null);
              }
            });
          } else {
            result.notImplemented();
          }
        }
      }
    );

    new MethodChannel(getFlutterView(), REGISTER_CHANNEL).setMethodCallHandler(
      new MethodChannel.MethodCallHandler() {
        @Override
        public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
          if (methodCall.method.equals("getVerification")){
            String strEmail = methodCall.argument("email");
            String strPhoneCode = methodCall.argument("phonecode");

            TuyaHomeSdk.getUserInstance().getRegisterEmailValidateCode(strPhoneCode, strEmail, new IResultCallback() {
              @Override
              public void onError(String s, String s1) {
                result.error("UNAVAILABLE", "Please try to get verification code", null);
              }

              @Override
              public void onSuccess() {
                result.success("success");
              }
            });
          } else if (methodCall.method.equals("goToLogin")){
            String strEmail = methodCall.argument("email");
            String strCode = methodCall.argument("code");
            String strPass = methodCall.argument("pass");
            String strPhoneCode = methodCall.argument("phonecode");

            TuyaHomeSdk.getUserInstance().registerAccountWithEmail(strPhoneCode, strEmail, strPass, strCode, new IRegisterCallback() {
              @Override
              public void onSuccess(User user) {
                result.success("success");
              }

              @Override
              public void onError(String s, String s1) {
                result.error("UNAVAILABLE", "Register Failed", null);
              }
            });
          }
        }
      }
    );


    new MethodChannel(getFlutterView(), WIFI_CHANNEL).setMethodCallHandler(new MethodChannel.MethodCallHandler() {
      @Override
      public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (methodCall.method.equals("getSSID")){
          String currentSSID = "Not Discovery";
          if (checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            currentSSID = WiFiUtil.getCurrentSSID(mContext);
            result.success("Wifi at Present: " + currentSSID);
          } else {
            wifiResult = result;
          }
        }
      }
    });
  }

  public boolean checkSinglePermission(String permission) {
    boolean hasPermission;
    if (Build.VERSION.SDK_INT < 23) {
      return true;
    } else {
      hasPermission = hasPermission(permission);
    }

    if (!hasPermission) {
      ActivityCompat.requestPermissions(this, new String[]{permission}, CODE_FOR_LOCATION_PERMISSION);
      return false;
    }

    return true;
  }

  public boolean hasPermission(String permission) {
    int targetSdkVersion = 0;
    try {
      final PackageInfo info = mContext.getPackageManager().getPackageInfo(
              mContext.getPackageName(), 0);
      targetSdkVersion = info.applicationInfo.targetSdkVersion;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    boolean result = true;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      if (targetSdkVersion >= Build.VERSION_CODES.M) {
        // targetSdkVersion >= Android M, we can
        // use Context#checkSelfPermission
        result = ContextCompat.checkSelfPermission(mContext, permission)
                == PackageManager.PERMISSION_GRANTED;
      } else {
        // targetSdkVersion < Android M, we have to use PermissionChecker
        result = PermissionChecker.checkSelfPermission(mContext, permission)
                == PermissionChecker.PERMISSION_GRANTED;
      }
    }

    return result;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == CODE_FOR_LOCATION_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        String strWifi = WiFiUtil.getCurrentSSID(this);
        wifiResult.success("Wifi at Present: " + strWifi);
      } else {
        wifiResult.error("UNAVAILABLE", "Please connect with Wifi", null);
      }
    }
  }
}
