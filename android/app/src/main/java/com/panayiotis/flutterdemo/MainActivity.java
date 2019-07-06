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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.SafeHandler;
import com.tuya.smart.android.device.utils.WiFiUtil;
import com.tuya.smart.android.mvp.bean.Result;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.interior.device.bean.GwDevResp;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.bean.DeviceBean;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity{

  private static final String CHANNEL = "flutter.native/login";
  private static final String REGISTER_CHANNEL = "flutter.register/register";
  private static final String WIFI_CHANNEL = "flutter.wifi/getssid";
  private static final String SEARCH_DEVICE_CHANNEL = "flutter.wifi/searchdevice";

  public static final int CODE_FOR_LOCATION_PERMISSION = 222;

  private static final int MESSAGE_SHOW_SUCCESS_PAGE = 1001;
  private Context mContext;
  private HomeBean currentHomeBean = null;
  MethodChannel.Result wifiResult;
  MethodChannel.Result loginResult;

  private SafeHandler mHandler;
  private Handler.Callback mCallback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
//        case MESSAGE_SHOW_SUCCESS_PAGE:
//          mView.showSuccessPage();
//          break;
//        case MESSAGE_CONFIG_WIFI_OUT_OF_TIME:
//          checkLoop();
//          break;
//        //网络错误异常情况
//        case DeviceBindModel.WHAT_EC_GET_TOKEN_ERROR:            //获取token失败
//          stopSearch();
//          mView.showNetWorkFailurePage();
//          break;
//        //ec激活失败
//        case DeviceBindModel.WHAT_EC_ACTIVE_ERROR:
//          L.d(TAG, "ec_active_error");
//          stopSearch();
//          if (mBindDeviceSuccess) {
//            mView.showBindDeviceSuccessFinalTip();
//            break;
//          }
//          mView.showFailurePage();
//          break;
//
//        //AP激活失败
//        case DeviceBindModel.WHAT_AP_ACTIVE_ERROR:
//          L.d(TAG, "ap_active_error");
//          stopSearch();
//          if (mBindDeviceSuccess) {
//            mView.showBindDeviceSuccessFinalTip();
//            break;
//          }
//          mView.showFailurePage();
//          String currentSSID = WiFiUtil.getCurrentSSID(mContext);
//          if (BindDeviceUtils.isAPMode())
//            WiFiUtil.removeNetwork(mContext, currentSSID);
//          break;
//
//        case DeviceBindModel.WHAT_EC_ACTIVE_SUCCESS:  //EC激活成功
//        case DeviceBindModel.WHAT_AP_ACTIVE_SUCCESS:  //AP激活成功
//          L.d(TAG, "active_success");
//          DeviceBean configDev = (DeviceBean) ((Result)msg.obj).getObj();
//          stopSearch();
//          configSuccess(configDev);
//          break;
//
//        case DeviceBindModel.WHAT_DEVICE_FIND:
//          L.d(TAG, "device_find");
//          deviceFind((String) ((Result) (msg.obj)).getObj());
//          break;
//        case DeviceBindModel.WHAT_BIND_DEVICE_SUCCESS:
//          L.d(TAG, "bind_device_success");
//          bindDeviceSuccess(((GwDevResp) ((Result) (msg.obj)).getObj()).getName());
//          break;
      }
      return false;
    }
  };
  String currentSSID = "Not Discovery";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
    mContext = this;
    mHandler = new SafeHandler(mCallback);
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
                loginResult = result;
                if (currentHomeBean != null)
                  currentHomeBean = null;
                checkFamilyCount();
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

          if (checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            currentSSID = WiFiUtil.getCurrentSSID(mContext);
            result.success("Wifi at Present: " + currentSSID);
          } else {
            wifiResult = result;
          }
        }
      }
    });

    new MethodChannel(getFlutterView(), SEARCH_DEVICE_CHANNEL).setMethodCallHandler(new MethodChannel.MethodCallHandler() {
      @Override
      public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        if (methodCall.method.equals("getDevice")){
          String strPass = methodCall.argument("wifipass");
//          result.success("Door Sensor,lkjlj;lkasdfasdf");
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

  private void getTokenForConfigDevice() {
//    long homeId = FamilyManager.getInstance().getCurrentHomeId();
//    TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
//      @Override
//      public void onSuccess(String token) {
//        ProgressUtil.hideLoading();
//        initConfigDevice(token);
//      }
//
//      @Override
//      public void onFailure(String s, String s1) {
//        ProgressUtil.hideLoading();
//        if (mConfigMode == ECActivity.EC_MODE) {
//          mView.showNetWorkFailurePage();
//        }
//      }
//    });
  }

  private void initConfigDevice(String token) {
//    if (mConfigMode == ECActivity.EC_MODE) {
//      mModel.setEC(mSSId, mPassWord, token);
//      startSearch();
//    } else if(mConfigMode == ECActivity.AP_MODE){
//      mModel.setAP(mSSId, mPassWord, token);
//    }
  }

  public void checkFamilyCount() {
    getHomeList(new ITuyaGetHomeListCallback() {
      @Override
      public void onSuccess(List<HomeBean> list) {
//        if (null == mHomeView) {
//          return;
//        }
        if (list == null || list.isEmpty()) {
          Log.e(CHANNEL, "List is Empty or Null");
          List<String> checkRoomList = new ArrayList<>();
          checkRoomList.add("main");
          checkRoomList.add("bath");
          addFamily("testFamily", checkRoomList);
        }
      }

      @Override
      public void onError(String s, String s1) {

      }
    });
  }

  public void getHomeList(@NonNull final ITuyaGetHomeListCallback callback) {
    TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
      @Override
      public void onSuccess(List<HomeBean> list) {
        if (!(list == null || list.isEmpty()) && null == currentHomeBean) {
          Log.e(CHANNEL, "Current HomeBean is Null");
          setCurrentHome(list.get(0));
        }
        callback.onSuccess(list);
      }

      @Override
      public void onError(String s, String s1) {
        callback.onError(s, s1);
      }
    });
  }

  public void setCurrentHome(HomeBean homeBean) {
    if (null == homeBean) {
      return;
    }
    boolean isChange = false;

    if (null == currentHomeBean) {
      isChange = true;
    } else {
      long currentHomeId = currentHomeBean.getHomeId();
      long targetHomeId = homeBean.getHomeId();
      if (currentHomeId != targetHomeId) {
        isChange = true;
      }
    }

    currentHomeBean = homeBean;
    loginResult.success("success," + currentHomeBean.getName());
    if (isChange) {
      EventBus.getDefault().post(new EventCurrentHomeChange(currentHomeBean));
    }
  }

  public void addFamily(String homeName, List<String> roomList) {
    if (roomList == null && roomList.isEmpty()) {
      return;
    }
    createHome(homeName, roomList, new ITuyaHomeResultCallback() {
      @Override
      public void onSuccess(HomeBean homeBean) {
        currentHomeBean = homeBean;
      }

      @Override
      public void onError(String s, String s1) {
        currentHomeBean = null;
      }
    });
  }

  public void createHome(String homeName, List<String> roomList, ITuyaHomeResultCallback callback) {
    TuyaHomeSdk.getHomeManagerInstance().createHome(homeName, 0, 0, "", roomList, new ITuyaHomeResultCallback() {
      @Override
      public void onSuccess(HomeBean homeBean) {
        setCurrentHome(homeBean);
        callback.onSuccess(homeBean);
      }

      @Override
      public void onError(String s, String s1) {
        callback.onError(s, s1);
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == CODE_FOR_LOCATION_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        currentSSID = WiFiUtil.getCurrentSSID(this);
        wifiResult.success("Wifi at Present: " + currentSSID);
      } else {
        wifiResult.error("UNAVAILABLE", "Please connect with Wifi", null);
      }
    }
  }
}

class EventCurrentHomeChange {

  private HomeBean homeBean;

  public EventCurrentHomeChange(HomeBean homeBean) {
    this.homeBean = homeBean;
  }

  public HomeBean getHomeBean() {
    return homeBean;
  }

  public void setHomeBean(HomeBean homeBean) {
    this.homeBean = homeBean;
  }
}
