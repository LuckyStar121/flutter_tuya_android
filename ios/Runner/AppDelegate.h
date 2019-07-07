#import <Flutter/Flutter.h>
#import <UIKit/UIKit.h>
#import <TuyaSmartHomeKit/TuyaSmartKit.h>

@interface AppDelegate : FlutterAppDelegate {
    long currentHomeID;
}

@property (nonatomic, strong) FlutterResult loginResult;
@property (nonatomic, strong) FlutterResult wifiResult;
@property (nonatomic, strong) FlutterResult deviceResult;
@property (nonatomic, strong) TuyaSmartHomeManager* homeManager;
@property (nonatomic, strong) TuyaSmartHomeModel* currentHome;
@property (nonatomic, strong) NSString* wifiPass;

- (void) checkFamilyCount;
- (void) setCurrentHome:(TuyaSmartHomeModel *)home;
- (void) addFamily:(NSString *) homeName RoomList:(NSArray *) roomlist;
@end
