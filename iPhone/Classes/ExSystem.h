//
//  ExSystem.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Msg.h"

#import <sys/socket.h>
#import <netinet/in.h>
#import <arpa/inet.h>
#import <netdb.h>
#import <SystemConfiguration/SCNetworkReachability.h>
#import "Authenticate.h"
#import "MsgControl.h"
#import "MobileAlertView.h"
#import "ReceiptData.h"
#import "CacheData.h"
#import "ImageControl.h"
#import "MCLogging.h"
#import "RequestController.h"
#import "ValidateSessionData.h"
#import "EntitySettings.h"
#import "KeychainItemWrapper.h"
#import "EntitySystem.h"
#import "ConcurMobileAppDelegate.h"
#import "FileManager.h"


@interface ExSystem : NSObject {

	MsgControl					*msgControl;
	NSMutableDictionary			*urlMapsDict, *currencies, *tripOffersDisplayPreference;
	ReceiptData					*receiptData;
	CacheData					*cacheData;
	ImageControl				*imageControl;
    NSArray                     *_settingsArray;
    NSManagedObjectContext      *_context; 
    EntitySettings              *entitySettings;
    NSString                    *userName;
    NSString                    *userInputOnLogin;
    NSString                    *pin;
    NSString                    *concurAccessToken;
    NSString                    *concurAccessTokenSecret;
    NSArray                     *siteSettings, *roles;
    EntitySystem                *sys;
    NSString                    *backUpReceiptsFilePath;
    BOOL                        shouldAskAgain;
    BOOL                        useFacebook;
    BOOL isTripItLinked;
    BOOL isTripItEmailAddressConfirmed;
    NSString *offersValidityChecking;
    NSString                    *sessionID;
    
    BOOL                        isCorpSSOUser;
    
    NSDate                      *timeLastGoodRequest;
    
    // MOB-8328 Keep track last new attendee key in memory only
    // MOB-9406 Move PrevAtnKey to ExSystem to retain between atn saves.
    NSString                    *prevAtnKey;
    NSData                      *pushDeviceToken;
    
    //For offline automation testing
    //Allow the background logical to be used on real devices, but UI shows no change.
    //Offline toggle is only avaiable on a simulator
    BOOL                        networkConnectivity;
}

@property (strong, nonatomic) NSDate *timeLastGoodRequest;
@property (strong, nonatomic) NSString *offersValidityChecking;
@property BOOL                          useFacebook;
@property BOOL isTripItLinked;
@property BOOL isTripItEmailAddressConfirmed;

@property BOOL shouldAskAgain;
@property (strong, nonatomic) NSMutableDictionary *tripOffersDisplayPreference;
@property (strong, nonatomic) EntitySystem      *sys;
@property (strong, nonatomic) NSString          *sessionID;

@property (strong, nonatomic) MsgControl		*msgControl;
@property (strong, nonatomic) NSMutableDictionary			*urlMapsDict;
@property (strong, nonatomic) NSMutableDictionary			*currencies;
@property (strong, nonatomic) ReceiptData		*receiptData;
@property (strong, nonatomic) CacheData			*cacheData;
@property (strong, nonatomic) ImageControl		*imageControl;
@property (strong, nonatomic) EntitySettings    *entitySettings;
@property (strong, nonatomic) NSArray           *siteSettings;
@property (strong, nonatomic) NSArray           *roles;

@property (nonatomic, strong) NSArray *settingsArray;
@property (nonatomic, strong) NSManagedObjectContext *context;

@property (strong, nonatomic) NSString *userName;
@property (strong, nonatomic) NSString *userInputOnLogin;
@property (strong, nonatomic) NSString *pin;
@property (strong, nonatomic) NSString *concurAccessToken;
@property (strong, nonatomic) NSString *concurAccessTokenSecret;
@property (strong, nonatomic) NSString *backUpReceiptsFilePath;
@property BOOL                        isCorpSSOUser;

@property (strong, nonatomic) NSString *prevAtnKey;
@property (strong, nonatomic) NSData   *pushDeviceToken;

@property (assign, nonatomic) BOOL networkConnectivity;

@property (strong, nonatomic, readonly) NSString *deviceId;

+(ExSystem*)sharedInstance;

#pragma mark -
#pragma mark Network Methods
-(void)updateSettings:(Msg *)msg;
+ (BOOL) connectedToNetwork;
+ (BOOL) reachableViaNetworkForFlags: (SCNetworkReachabilityFlags) flags;
+ (BOOL) reachableVialocalWiFiForFlags: (SCNetworkReachabilityFlags) flags;
+ (BOOL) isPhoneInUse;

#pragma mark - temporary code to store home and work addresses
//Temporary, until we update the core data model
-(void) setAddress:(NSString*) value forKey:(NSString*) key;
-(NSString*)getHomeAddress;
-(NSString*)getWorkAddress;
-(void) resetAddresses;

#pragma mark - web extension
-(NSURL*) urlForWebExtension:(NSString*)page;

#pragma mark URLs for Travel
-(void) initURLMaps;
-(NSString *)getURLMap:(NSString *)dictName LocalConstant:(NSString *)localConstant;

#pragma mark -
#pragma mark Session Handling
-(BOOL) isValidSessionID:(NSString*) sessID;

#pragma mark -
#pragma mark Initialization
-(void)doBaseInit;
-(EntitySettings*) loadSettings;
-(void) loadSiteSettings;
-(void) saveSiteSetting:(NSString *)val type:(NSString *)type name:(NSString *)name;
-(void) clearSiteSettings;
-(NSString*)getSiteSetting:(NSString*)name withType:(NSString*) type;
-(BOOL) siteSettingAllowsExpenseApprovals;
-(BOOL) siteSettingAllowsExpenseReports;
-(BOOL) siteSettingAllowsTravelBooking;
-(BOOL) siteSettingHotelStreamingEnabled;
-(BOOL) siteSettingHasFixedTA;
-(BOOL) siteSettingCanImportTrips;
-(BOOL) siteSettingVoiceBookingEnabled;
-(BOOL) siteSettingAllowsTouchID;
-(BOOL) siteSettingGemsEnabled;
-(void) loadRoles;
-(void) saveRole:(NSString *)role;
-(void) clearRoles;
-(void)makeRoles:(NSString *)roleCommas;
-(BOOL) hasRole:(NSString*) role __attribute__ ((deprecated("this will become a private method soon")));
-(void) saveSystem;
-(EntitySystem *) loadSystem;
-(void) saveSettings;

#pragma mark -
#pragma user config checks

- (BOOL)isTravelRelated;
- (BOOL)isExpenseRelated;
- (BOOL)isTravelOnly;
- (BOOL)isExpenseOnlyUser;
- (BOOL)isApprovalOnlyUser;
- (BOOL) isTravelAndApprovalOnlyUser;
- (BOOL) isTravelAndExpenseOnlyUser;
- (BOOL) isExpenseAndApprovalOnlyUser;
- (BOOL) isAllFeatureUser;
- (BOOL)isCTEUser;
- (BOOL)hasTravelRequest;
- (BOOL)isRequestApprover;
- (BOOL)isRequestUser;
- (BOOL)hasTravelBooking;
- (BOOL)hasReceiptStore;
- (BOOL)hasCarMileageOnHome;
- (BOOL)hasLocateAndAlert;
- (BOOL)hasGateGuru;
- (BOOL)hasTaxiMagic;
- (BOOL)hasMetro;
- (BOOL)hasExpenseIt;
- (BOOL)hasJpt;
- (BOOL)hasFeedBacks;
- (BOOL)shouldShowPriceToBeatGenerator;
- (BOOL)canBookRail;
- (BOOL)canUseConditionalFields;
- (NSString*)getUserType;

#pragma mark -
#pragma salesforce credentials
- (void)saveSalesForceToken:(NSString *)token andUrl:(NSString *)url;

#pragma mark -
#pragma mark User settings as child nodes of LoginResult
-(NSString*) getUserSetting:(NSString*) name withDefault:(NSString*) defValue;
-(void) saveUserSetting:(NSString*) val name:(NSString*) name;

#pragma mark -
#pragma mark findMe doInitialization
-(void)doFindMe;


#pragma mark -
#pragma mark Utility Methods
-(BOOL) isUsingConcurProductionServer;
+(BOOL) isLandscape;
+(BOOL) is5;
+(BOOL) is6Plus; // 6+
+(BOOL) is7Plus; //iOS 7+
+(BOOL) is8Plus; //iOS 8+

+ (void) addMsg:(Msg *)msg;

#pragma mark -
#pragma mark Color Button Maker
+(UIBarButtonItem*) makeBackButton:(NSString*)text4Btn target:(id)tgt action:(SEL) sel;
+(UIBarButtonItem *)makeColoredButton:(NSString *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle SelectorString:(NSString *)selectorString MobileVC:(MobileViewController *)mvc;
+(UIButton *)makeColoredButtonRegular:(NSString *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle SelectorString:(NSString *)selectorString MobileVC:(MobileViewController *)mvc;
+(UIBarButtonItem *)makeSilverBarButton:(NSString *)btnTitle width:(float)w height:(float)h selectorString:(NSString *)selectorString target:(NSObject*) obj;

#pragma mark -
#pragma mark Credentials
-(void) updateTimeOfLastGoodRequest;
-(void) useOfflineSession;
-(void) useOnlineSession:(NSString*)session;
-(void) clearSession;
-(void) clearSessionWithOptionToSave:(BOOL)save;
-(void) clearNonFacebookCredentialsAndSession;
-(void) clearUserCredentialsAndSession;
-(void) clearAccessToken;


#pragma mark -
#pragma mark Keychain stuff
-(void)savePin:(NSString *)sPin;
-(NSString *)loadPin;
-(NSString *)loadUserId;
-(void)saveUserId:(NSString *)sUserId;
-(void)clearUserId;
-(NSString *)loadUserInputOnLogin;
-(void)saveUserInputOnLogin:(NSString *)sUserInputOnLogin;
-(void)clearUserInputOnLogin;
-(NSString *)loadConcurAccessToken;
-(void) saveConcurAccessToken:(NSString*)token;
-(NSString *)loadConcurAccessTokenSecret;
-(void) saveConcurAccessTokenSecret:(NSString*)secret;

#pragma mark -
#pragma mark Stolen methods from SettingsData

-(void)setMyTopViewName:(NSString *)mary;
-(BOOL) isQADevOrProdConcurCompany;
-(BOOL) enableAttendeeEditingInMobileExpense;
-(BOOL) enableAttendeeEditing;
-(BOOL) enableLimitedNewFeature;
-(BOOL) isBreeze;
-(BOOL) isBronxUser;
-(BOOL) isBronxUserProductLine:(NSString*)productLine productOffering:(NSString*)productOffering;
-(BOOL) isCorpUser;
-(BOOL) isCorpUserProductLine:(NSString*)productLine;
-(BOOL) isGovernment;
- (BOOL)isTestDrive;

#pragma mark - LastGoodRequest plist

-(void) saveLastGoodRequest;
-(void) loadLastGoodRequest;

#pragma mark - networkConnection plist
-(void) saveNetworkConnectionSettings;

#pragma mark -
#pragma mark Utility Methods
+(void)setNavigationBarBaseColor:(UINavigationBar*)navBar;
+(void)setToolBarBaseColor:(UIToolbar*)tBar;
+(UIColor *)getBaseBackgroundColor;
+(void)setStatusBarBlack __attribute__ ((deprecated("iOS 7 does not support black status bar, this call will reset it to default colors")));

#pragma mark - whats new
-(void) doWhatsNewChecks;

-(NSString *)loadSession;
-(void)saveSession:(NSString *)sSession;

-(NSString *)loadCompanySSOLoginPageUrl;
-(void)saveCompanySSOLoginPageUrl:(NSString *)ssoUrl;
-(void)clearCompanySSOLoginPageUrl;
-(void) deleteCorpSSOSessionCookie;
-(void) updateUserInfo:(Msg*)msg;

-(void)clearCompanyCode;
-(NSString *)loadCompanyCode;
-(void)saveCompanyCode:(NSString *)cCode;

@end

