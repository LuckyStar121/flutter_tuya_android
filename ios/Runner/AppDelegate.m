#include "AppDelegate.h"
#include "GeneratedPluginRegistrant.h"

	
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
            /**
             if ([CLLocationManager locationServicesEnabled]){
             
             NSLog(@"Location Services Enabled");
             
             if ([CLLocationManager authorizationStatus]==kCLAuthorizationStatusDenied){
             alert = [[UIAlertView alloc] initWithTitle:@"App Permission Denied"
             message:@"To re-enable, please go to Settings and turn on Location Service for this app."
             delegate:nil
             cancelButtonTitle:@"OK"
             otherButtonTitles:nil];
             [alert show];
             }
             }
             */
            if ([CLLocationManager locationServicesEnabled]){
                CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
                switch (status) {
                    case kCLAuthorizationStatusNotDetermined:
                        
                        //The user hasn't yet chosen whether your app can use location services or not.
                        break;
                        
                    case kCLAuthorizationStatusAuthorizedAlways:
                        
                        //The user has let your app use location services all the time, even if the app is in the background.
                        break;
                        
                    case kCLAuthorizationStatusAuthorizedWhenInUse:
                        
                        //The user has let your app use location services only when the app is in the foreground.
                        break;
                        
                    case kCLAuthorizationStatusRestricted:
                        
                        //The user can't choose whether or not your app can use location services or not, this could be due to parental controls for example.
                        break;
                        
                    case kCLAuthorizationStatusDenied:
                        
                        //The user has chosen to not let your app use location services.
                        break;
                        
                    default:
                        break;
                }
            } else {
                CLLocationManager *locationManager = [[CLLocationManager alloc] init];
                [locationManager requestAlwaysAuthorization];
//                result(@"ok");
//                self.wifiResult = result;
            }
        }
    }];
    FlutterMethodChannel* deviceChannel = [FlutterMethodChannel
                                         methodChannelWithName:@"flutter.wifi/searchdevice"
                                         binaryMessenger: controller];
    [deviceChannel setMethodCallHandler:^(FlutterMethodCall* call, FlutterResult result) {
        if ([@"getDevice" isEqualToString:call.method]) {
            self.wifiPass = call.arguments[@"wifipass"];
            
            self.deviceResult = result;
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
@end
