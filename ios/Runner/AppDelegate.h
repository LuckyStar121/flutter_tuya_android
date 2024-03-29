#import <Flutter/Flutter.h>
#import <UIKit/UIKit.h>
#import <TuyaSmartHomeKit/TuyaSmartKit.h>

@interface AppDelegate : FlutterAppDelegate <TuyaSmartActivatorDelegate>{
    long currentHomeID;
}

@property (nonatomic, strong) FlutterResult loginResult;
@property (nonatomic, strong) FlutterResult wifiResult;
@property (nonatomic, strong) FlutterResult deviceResult;
@property (nonatomic, strong) TuyaSmartHomeManager* homeManager;
@property (nonatomic, strong) TuyaSmartHomeModel* currentHome;
@property (nonatomic, strong) NSString* wifiPass;
@property (nonatomic, strong) NSString* wifiSSID;
@property (nonatomic, strong) NSTimer *timer;
@property (nonatomic, strong) NSDate *fireDate;

- (void) checkFamilyCount;
- (void) setCurrentHome:(TuyaSmartHomeModel *)home;
- (void) addFamily:(NSString *) homeName RoomList:(NSArray *) roomlist;
- (NSString *) getSSID;
- (void) getTokenForConfigDevice;
- (void) initConfigDevice;

- (void) startConfigWifi:(NSString *)ssid password:(NSString *)password token: (NSString *)token;
- (void) stopConfigWifi;
- (void) timerCallback;

@end
