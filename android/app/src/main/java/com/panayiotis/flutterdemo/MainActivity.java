package com.panayiotis.flutterdemo;

import android.Manifest;
import android.content.Context;
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
import android.util.Log;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.SafeHandler;
import com.tuya.smart.android.device.utils.WiFiUtil;
import com.tuya.smart.android.mvp.bean.Result;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.builder.ActivatorBuilder;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.interior.device.bean.GwDevResp;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.enums.ActivatorEZStepCode;
import com.tuya.smart.sdk.enums.ActivatorModelEnum;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

import static com.tuya.smart.sdk.enums.ActivatorModelEnum.TY_AP;
import static com.tuya.smart.sdk.enums.ActivatorModelEnum.TY_EZ;

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
  MethodChannel.Result deviceResult;

  private String strWifiID;
  private String strWifiPass;

  private SafeHandler mHandler;
  private ITuyaActivator mTuyaActivator;
  private ActivatorModelEnum mModelEnum;

  private boolean mBindDeviceSuccess;
  private int mTime;
  private boolean mStop;

  public static final int WHAT_EC_ACTIVE_ERROR = 0x02;
  public static final int WHAT_EC_ACTIVE_SUCCESS = 0x03;
  public static final int WHAT_AP_ACTIVE_ERROR = 0x04;
  public static final int WHAT_AP_ACTIVE_SUCCESS = 0x05;
  public static final int WHAT_EC_GET_TOKEN_ERROR = 0x06;
  public static final int WHAT_DEVICE_FIND = 0x07;
  public static final int WHAT_BIND_DEVICE_SUCCESS = 0x08;
  private static final int MESSAGE_CONFIG_WIFI_OUT_OF_TIME = 0x16;

  private Handler.Callback mCallback = new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case MESSAGE_SHOW_SUCCESS_PAGE:
          break;
        case MESSAGE_CONFIG_WIFI_OUT_OF_TIME:
          checkLoop();
          break;
        case WHAT_EC_GET_TOKEN_ERROR:
          stopSearch();
          break;
        //ec
        case WHAT_EC_ACTIVE_ERROR:
          L.d("Handler", "ec_active_error");
          stopSearch();
          if (mBindDeviceSuccess) {
            break;
          }
          break;

        case WHAT_AP_ACTIVE_ERROR:
          L.d("Handler", "ap_active_error");
          stopSearch();
          if (mBindDeviceSuccess) {
            break;
          }
          //Show Failed View
          currentSSID = WiFiUtil.getCurrentSSID(mContext);

          break;

        case WHAT_EC_ACTIVE_SUCCESS:  //EC Active Success
        case WHAT_AP_ACTIVE_SUCCESS:  //AP Active Success
          L.d("Handler", "active_success");
          DeviceBean configDev = (DeviceBean) ((Result)msg.obj).getObj();
          stopSearch();
          configSuccess(configDev);
          break;

        case WHAT_DEVICE_FIND:
          L.d("Handler", "device_find");
          deviceFind((String) ((Result) (msg.obj)).getObj());
          break;
        case WHAT_BIND_DEVICE_SUCCESS:
          L.d("Handler", "bind_device_success");
          bindDeviceSuccess(((GwDevResp) ((Result) (msg.obj)).getObj()).getName());
          break;
      }
      return false;
    }
  };

  private void bindDeviceSuccess(String name) {
    if (!mStop) {
      mBindDeviceSuccess = true;
      //Show Bind_Device_Find_View
    }
  }

  private void deviceFind(String gwId) {
    if (!mStop) {
      //Show Device_Find_View
    }
  }

  private void checkLoop() {
    if (mStop) return;
    if (mTime >= 100) {
      stopSearch();
      configFailure();
    } else {
      mTime++;
      mHandler.sendEmptyMessageDelayed(MESSAGE_CONFIG_WIFI_OUT_OF_TIME, 1000);
    }
  }

  private void configSuccess(DeviceBean deviceBean) {
    Log.e("ActiveSuccess", deviceBean.getName());
    if (deviceBean != null){
      deviceResult.success(deviceBean.getName() + "," + deviceBean.getDevId());
    }
    stopSearch();
    mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_SUCCESS_PAGE, 1000);
  }
  public void configFailure() {
    if (mModelEnum == null) return;
    if (mModelEnum == TY_AP) {
      //ap mode
      resultError(WHAT_AP_ACTIVE_ERROR, "TIME_ERROR", "OutOfTime");
    } else {
      //ez mode
      resultError(WHAT_EC_ACTIVE_ERROR, "TIME_ERROR", "OutOfTime");
    }
  }
  //暂停配网
  private void stopSearch() {
    mStop = true;
    mHandler.removeMessages(MESSAGE_CONFIG_WIFI_OUT_OF_TIME);
    cancel();
  }

  public void cancel() {
    if (mTuyaActivator != null) {
      mTuyaActivator.stop();
    }
  }
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
            strWifiID = currentSSID;
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
          strWifiPass = methodCall.argument("wifipass");
          getTokenForConfigDevice();
          deviceResult = result;
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
    if (null == currentHomeBean) {
      return;
    }
    long homeId = currentHomeBean.getHomeId();
    TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
      @Override
      public void onSuccess(String token) {
        initConfigDevice(token);
      }

      @Override
      public void onFailure(String s, String s1) {
      }
    });
  }

  private void initConfigDevice(String token) {
    mModelEnum = TY_EZ;
    mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newMultiActivator(new ActivatorBuilder()
            .setSsid(strWifiID)
            .setContext(mContext)
            .setPassword(strWifiPass)
            .setActivatorModel(TY_EZ)
            .setTimeOut(100)
            .setToken(token).setListener(new ITuyaSmartActivatorListener() {
              @Override
              public void onError(String s, String s1) {
                switch (s) {
                  case "1004":
                    resultError(WHAT_EC_GET_TOKEN_ERROR, "wifiError", s1);
                    return;
                }
                resultError(WHAT_EC_ACTIVE_ERROR, s, s1);
              }

              @Override
              public void onActiveSuccess(DeviceBean gwDevResp) {

                resultSuccess(WHAT_EC_ACTIVE_SUCCESS, gwDevResp);
              }

              @Override
              public void onStep(String s, Object o) {
                switch (s) {
                  case ActivatorEZStepCode.DEVICE_BIND_SUCCESS:
                    resultSuccess(WHAT_BIND_DEVICE_SUCCESS, o);
                    break;
                  case ActivatorEZStepCode.DEVICE_FIND:
                    resultSuccess(WHAT_DEVICE_FIND, o);
                    break;
                }
              }
            }));
    startSearch();

  }

  protected void resultSuccess(int what, Object obj) {
    if (this.mHandler != null) {
      Message message = this.mHandler.obtainMessage(what);
      message.obj = new Result(obj);
      this.mHandler.sendMessage(message);
    }

  }

  protected void resultError(int what, String errorCode, String error) {
    if (this.mHandler != null) {
      Message message = this.mHandler.obtainMessage(what);
      message.obj = new Result(errorCode, error);
      this.mHandler.sendMessage(message);
    }

  }

  public void startSearch() {
    start();
    mBindDeviceSuccess = false;
    startLoop();
  }

  private void startLoop() {
    mTime = 0;
    mStop = false;
    mHandler.sendEmptyMessage(MESSAGE_CONFIG_WIFI_OUT_OF_TIME);

  }

  public void start() {
    if (mTuyaActivator != null) {
      mTuyaActivator.start();
    }
  }

  public void checkFamilyCount() {
    getHomeList(new ITuyaGetHomeListCallback() {
      @Override
      public void onSuccess(List<HomeBean> list) {

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
        strWifiID = currentSSID;
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
