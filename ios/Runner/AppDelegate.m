#include "AppDelegate.h"
#include "GeneratedPluginRegistrant.h"
#include <SystemConfiguration/SystemConfiguration.h>
@import SystemConfiguration.CaptiveNetwork;
	
@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    [[TuyaSmartSDK sharedInstance] startWithAppKey:@"d4stc4wdmmg5dsaev44x" secretKey:@"qyexddfcdsw88dxfkqjddtvty7afve8h"];
    #ifdef DEBUG
        [[TuyaSmartSDK sharedInstance] setDebugMode:YES];
    #else
    #endif
    self.homeManager = [[TuyaSmartHomeManager alloc] init];
    currentHomeID = 0;
    FlutterViewController* controller = (FlutterViewController*)self.window.rootViewController;
    
    FlutterMethodChannel* loginChannel = [FlutterMethodChannel
                                            methodChannelWithName:@"flutter.native/login"
                                            binaryMessenger:controller];
    
    [loginChannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
        // TODO
//        NSString* strName = call.argument("name");
//        NSString* strPass = call.argument("pass");
        if ([@"getTestString" isEqualToString:call.method]) {
            NSString* strName = call.arguments[@"name"];
            NSString* strPass = call.arguments[@"pass"];
            NSString* strPhoneCode = call.arguments[@"phonecode"];
            [[TuyaSmartUser sharedInstance] loginByEmail:strPhoneCode email:strName password:strPass success:^{
                NSLog(@"login success");
//                result(@"success,testFamily");
                self.loginResult = result;
                if (currentHomeID != 0){
                    currentHomeID = 0;
                }
                [self checkFamilyCount];
            } failure:^(NSError *error) {
                NSLog(@"login failure: %@", error);
                
                result([FlutterError errorWithCode:@"UNAVAILABLE"
                                           message:@"Email and Password maybe wrong."
                                           details:nil]);
            }];
            
        }
    }];
    
    FlutterMethodChannel* registerChannel = [FlutterMethodChannel
                                             methodChannelWithName:@"flutter.register/register"
                                             binaryMessenger: controller];
    [registerChannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
        if ([@"getVerification" isEqualToString:call.method]) {
            NSString* strEmail = call.arguments[@"email"];
            NSString* strPhoneCode = call.arguments[@"phonecode"];
            [[TuyaSmartUser sharedInstance] sendVerifyCodeByEmail:strPhoneCode email:strEmail success:^{
                result(@"success");
            } failure:^(NSError *error) {
                result([FlutterError errorWithCode:@"UNAVAILABLE"
                                           message:@"Please try to get Verification Code"
                                           details:nil]);
            }];
        } else if ([@"goToLogin" isEqualToString:call.method]) {
            NSString* strEmail = call.arguments[@"email"];
            NSString* strPhoneCode = call.arguments[@"phonecode"];
            NSString* strPass = call.arguments[@"pass"];
            NSString* strCode = call.arguments[@"code"];
            [[TuyaSmartUser sharedInstance] registerByEmail:strPhoneCode email:strEmail password:strPass code:strCode success:^{
                result(@"success");
            } failure:^(NSError *error) {
                result([FlutterError errorWithCode:@"UNAVAILABLE"
                                           message:@"Register Failed"
                                           details:nil]);
            }];
        }
    }];
    
    FlutterMethodChannel* wifiChannel = [FlutterMethodChannel
                                             methodChannelWithName:@"flutter.wifi/getssid"
                                             binaryMessenger: controller];
    [wifiChannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
        if ([@"getSSID" isEqualToString:call.method]) {
            self.wifiSSID = [TuyaSmartActivator currentWifiSSID];
//            _wifiSSID = [self getSSID];
            if (self.wifiSSID != nil){
                result([NSMutableString stringWithFormat:@"Wifi At Present:%@", self.wifiSSID]);
            } else{
                result([FlutterError errorWithCode:@"UNAVAILABLE"
                                           message:@"Wifi Connection Failed"
                                           details:nil]);
                self.wifiSSID = nil;
            }
        }
    }];
    
    FlutterMethodChannel* deviceChannel = [FlutterMethodChannel
                                         methodChannelWithName:@"flutter.wifi/searchdevice"
                                         binaryMessenger: controller];
    [deviceChannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
        if ([@"getDevice" isEqualToString:call.method]) {
            if (self.wifiSSID != nil){
                self.wifiPass = call.arguments[@"wifipass"];
                [self getTokenForConfigDevice];
                self.deviceResult = result;
            }
        }
    }];
  [GeneratedPluginRegistrant registerWithRegistry:self];
  // Override point for customization after application launch.
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

- (void) checkFamilyCount {
    [self getHomeList];
}
- (void) getHomeList {
    [self.homeManager getHomeListWithSuccess:^(NSArray<TuyaSmartHomeModel *> *homes) {
        if ( !(homes == NULL || [homes count] == 0) && currentHomeID == 0){
            [self setCurrentHome:[homes firstObject]];
        }
        if (homes == NULL || [homes count] == 0){
            NSArray* checkRoomList = [NSArray arrayWithObjects: @"main", @"bath", nil];
            [self addFamily:@"testFamily" RoomList:checkRoomList];
        }
    } failure:^(NSError *error) {
        NSLog(@"get home list failure: %@", error);
    }];
}

- (void) setCurrentHome:(TuyaSmartHomeModel *)home {
    if (home == NULL){
        return;
    }
    
    Boolean isChange = false;
    if (currentHomeID == 0){
        isChange = true;
    } else {
        
        long targethomeID = [home homeId];
        if (currentHomeID != targethomeID){
            isChange = true;
        }
    }
    
    currentHomeID = [home homeId];
    
    NSMutableString* result = [NSMutableString stringWithFormat:@"success,%@", [home name]];
    self.loginResult(result);
}

- (void) addFamily:(NSString *)homeName RoomList:(NSArray *)roomlist {
    if (roomlist == NULL || [roomlist count] == 0)
        return;
    
    [self.homeManager addHomeWithName:homeName geoName:@"" rooms:roomlist latitude:0 longitude:0 success:^(long long result) {
        [self checkFamilyCount];
    } failure:^(NSError *error) {
        self.loginResult([FlutterError errorWithCode:@"UNAVAILABLE"
                                   message:@"Add Family Failed"
                                   details:nil]);
    }];
}

- (NSString *) getSSID {
    NSString* wifiName = nil;
    NSArray *ifs = (__bridge_transfer id)CNCopySupportedInterfaces();
    for (NSString *ifnam in ifs){
        NSDictionary *info = (__bridge_transfer id)CNCopyCurrentNetworkInfo((__bridge CFStringRef)ifnam);
        if (info[@"SSID"]){
            wifiName = info[@"SSID"];
        }
    }
    return wifiName;
}

- (void) getTokenForConfigDevice {
    if (currentHomeID == 0)
        return;
    
    [[TuyaSmartActivator sharedInstance] getTokenWithHomeId:currentHomeID success:^(NSString *token) {
        self.timer = [NSTimer scheduledTimerWithTimeInterval:1/60.0 target:self selector:@selector(timerCallback) userInfo:nil repeats:YES];
        self.fireDate = [NSDate date];
        [self startConfigWifi:self.wifiSSID password:self.wifiPass token:token];
    } failure:^(NSError *error) {
        self.deviceResult([FlutterError errorWithCode:@"UNAVAILABLE"
                                              message:@"try to search Device"
                                              details:nil]);
    }];
}

- (void) initConfigDevice {
//    [self startConfigWifi:<#(NSString *)#> password:<#(NSString *)#> token:<#(NSString *)#>]
}

- (void) startConfigWifi:(NSString *)ssid password:(NSString *)password token: (NSString *)token{
    // Set TuyaSmartActivator delegate, impl delegate method
    [TuyaSmartActivator sharedInstance].delegate = self;
    
    // start activator
    [[TuyaSmartActivator sharedInstance] startConfigWiFi:TYActivatorModeEZ ssid:ssid password:password token:token timeout:100];
}

- (void) stopConfigWifi {
    [TuyaSmartActivator sharedInstance].delegate = self;
    [[TuyaSmartActivator sharedInstance] stopConfigWiFi];
}

- (void) timerCallBack {
//    CGFloat progress = MIN(fabs([self.fireDate timeInterValSinceNow] / Timeout), 1.0);
    CGFloat progress = MIN([self.fireDate timeIntervalSinceNow] / 100, 1.0);
    if (progress >= 1)
        [_timer invalidate];
}

#pragma mark - TuyaSmartActivator Delegate

- (void)activator:(TuyaSmartActivator *)activator didReceiveDevice:(TuyaSmartDeviceModel *)deviceModel error:(NSError *)error {
    if (!error && deviceModel){
        [_timer invalidate];
        [TuyaSmartActivator sharedInstance].delegate = nil;
        [self stopConfigWifi];
        self.deviceResult([deviceModel name]);
    }
    
    if (error){
        [_timer invalidate];
        self.deviceResult([FlutterError errorWithCode:@"UNAVAILABLE"
                                             message:@"Searching Device Failed"
                                             details:nil]);
    }
}

@end
