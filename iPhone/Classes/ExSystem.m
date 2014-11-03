//
//  ExSystem.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/18/11.
//  Copyright 2011 Concur. All rights reserved.
//
#import <CommonCrypto/CommonHMAC.h>
#import <CoreTelephony/CTCallCenter.h>
#import <CoreTelephony/CTCall.h> 
#import "ExSystem.h"
#import "ConcurMobileAppDelegate.h"
#import "EntitySettings.h"
#import "EntitySiteSetting.h"
#import "EntityCar.h"
#import "EntityHotel.h"
#import "EntityRail.h"
#import "EntitySmartExpenseCctKeys.h"
#import "EntitySmartExpensePctKeys.h"
#import "EntityRoles.h"
#import "EntitySystem.h"
#import "DateTimeFormatter.h"
#import "MessageRegistrationCenter.h"
#import "NotificationController.h"

#import "EntitySalesForceUser.h"
#import "Config.h"

#import "KeychainManager.h"
#import "SignInWithTouchID.h"
#import "CTENetworkSettings.h"

#define SHOULD_SEND_REQUESTS_OVER_NETWORK_DEFAULT_VALUE             YES
#define SHOULD_ERROR_RESPONSES_BE_HANDLED_SILENTLY_DEFAULT_VALUE    NO

static ExSystem *sharedInstance;

@interface ExSystem ()

// moved keychain handling to a keychainManager class
// old code still goes through ExSystem, so we just redirect all the keychain calls here to KeychainManager
@property (nonatomic, readwrite, strong) KeychainManager *keychainManager;
@end

@implementation ExSystem

@synthesize msgControl, urlMapsDict, receiptData, cacheData, imageControl, entitySettings, userInputOnLogin, userName, pin, concurAccessToken, concurAccessTokenSecret, siteSettings, roles, sys, currencies,backUpReceiptsFilePath, shouldAskAgain, useFacebook, isTripItLinked, isTripItEmailAddressConfirmed, tripOffersDisplayPreference, offersValidityChecking, sessionID, isCorpSSOUser, timeLastGoodRequest;

@synthesize prevAtnKey;
@synthesize pushDeviceToken;

@synthesize settingsArray = _settingsArray;
@synthesize context = _context;

@synthesize networkConnectivity;

@synthesize deviceId = _deviceId;

+(ExSystem*)sharedInstance
{
	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[ExSystem alloc] init];
                [sharedInstance loadLastGoodRequest];
                [sharedInstance doBaseInit];
			}
		}
		return sharedInstance;
	}
}
// har role is still public so ignore deprecated declaration for the moment
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"

-(ExSystem*)init
{
    self = [super init];
	if (self)
	{
        self.keychainManager = [[KeychainManager alloc] init];
        self.shouldAskAgain = YES;

        if ([self loadCompanySSOLoginPageUrl])
            self.isCorpSSOUser = YES;
    }
	return self;
}


#pragma mark -
#pragma mark Network Methods
+ (BOOL) reachableVialocalWiFiForFlags: (SCNetworkReachabilityFlags) flags
{
	BOOL retVal = NO; // NotReachable;
	if((flags & kSCNetworkReachabilityFlagsReachable) && (flags & kSCNetworkReachabilityFlagsIsDirect))
	{
		retVal = YES; //ReachableViaWiFi;	
	}
	return retVal;
}


+ (BOOL) reachableViaNetworkForFlags: (SCNetworkReachabilityFlags) flags
{
	if ((flags & kSCNetworkReachabilityFlagsReachable) == 0)
	{
		// if target host is not reachable
		return NO; // NotReachable;
	}
	
	BOOL retVal = NO; // NotReachable;
	
	if ((flags & kSCNetworkReachabilityFlagsConnectionRequired) == 0)
	{
		// if target host is reachable and no connection is required
		//  then we'll assume (for now) that your on Wi-Fi
		retVal = YES; // ReachableViaWiFi;
	}
	
	if ((((flags & kSCNetworkReachabilityFlagsConnectionOnDemand ) != 0) ||
		 (flags & kSCNetworkReachabilityFlagsConnectionOnTraffic) != 0))
	{
		// ... and the connection is on-demand (or on-traffic) if the
		//     calling application is using the CFSocketStream or higher APIs
		
		if ((flags & kSCNetworkReachabilityFlagsInterventionRequired) == 0)
		{
			// ... and no [user] intervention is needed
			retVal = YES; // ReachableViaWiFi;
		}
	}
	
	if ((flags & kSCNetworkReachabilityFlagsIsWWAN) == kSCNetworkReachabilityFlagsIsWWAN)
	{
		// ... but WWAN connections are OK if the calling application
		//     is using the CFNetwork (CFSocketStream?) APIs.
		retVal = YES; //ReachableViaWWAN;
	}
	return retVal;
}


+ (BOOL) connectedToNetwork
{
    if ([Config isDevBuild] && [[ExSystem sharedInstance] networkConnectivity] == NO)
    {
        return NO;
    }
    
	// Create zero addy
	struct sockaddr_in zeroAddress;
	bzero(&zeroAddress, sizeof(zeroAddress));
	zeroAddress.sin_len = sizeof(zeroAddress);
	zeroAddress.sin_family = AF_INET;
	
	// Recover reachability flags
	SCNetworkReachabilityRef defaultRouteReachability = SCNetworkReachabilityCreateWithAddress(NULL, (struct sockaddr *)&zeroAddress);
	SCNetworkReachabilityFlags flags;
	
	BOOL didRetrieveFlags = SCNetworkReachabilityGetFlags(defaultRouteReachability, &flags);
	CFRelease(defaultRouteReachability);
	
	if (!didRetrieveFlags)
	{
        [Flurry logEvent:@"Offline: Age" timed:YES];
		return NO;
	}
	
	BOOL isReachable = flags & kSCNetworkFlagsReachable;
    
    if (!isReachable)
    {
        [Flurry logEvent:@"Offline: Age" timed:YES];
    }else
    {
        [Flurry endTimedEvent:@"Offline: Age" withParameters:nil];
    }
    
	return (isReachable) ? YES : NO;
}

+(BOOL) isPhoneInUse
{
    CTCallCenter *callcentre = [[CTCallCenter alloc] init];
    for (CTCall *call in callcentre.currentCalls) {
        return YES;
    }
    return NO;
}

-(void) updateUserInfo:(Msg*)msg
{
    ValidateSessionData *validateSessionData = (ValidateSessionData *)msg.responder;
    
    entitySettings.firstName = (validateSessionData.dict)[@"FirstName"];
    entitySettings.lastName = (validateSessionData.dict)[@"LastName"];
    entitySettings.email = (validateSessionData.dict)[@"Email"];
    entitySettings.companyName = (validateSessionData.dict)[@"CompanyName"];
    entitySettings.mi =  (validateSessionData.dict)[@"Mi"];
}

-(void)updateSettings:(Msg *)msg
{
	Authenticate *auth = (Authenticate *)msg.responder;
	
    [self useOnlineSession:auth.sessionID];
    [self saveSession:self.sessionID];
	
	self.concurAccessToken = auth.accessToken;
    [self saveConcurAccessToken:self.concurAccessToken];
    // Debug log for enpty Concur Access token
    if (![self.concurAccessToken lengthIgnoreWhitespace])
    {
        ALog(@"*************** saved empty token! ****************");
        DLog(@"*************** saved empty token! ****************")
    }
    // Debug log for enpty Concur Access token
	
	self.concurAccessTokenSecret = auth.accessTokenSecret;
    [self saveConcurAccessTokenSecret:self.concurAccessTokenSecret];
	
    [self makeRoles:auth.roles];
    
    // Government users are only identified by ROLE_GOVERNMENT_USER
    if ([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_USER]) {
        sys.productLine = PROD_GOVERNMENT;
        sys.productOffering = PROD_GOVERNMENT;
    } else {
        sys.productLine = auth.entityType;
        sys.productOffering = auth.productOffering;
    }
    
    if (self.isCorpSSOUser)
    {
        NSDictionary *dict = @{@"Type": @"SSO"};
        [Flurry logEvent:@"Sign In: Authentication Type" withParameters:dict];
    }
    
	sys.timeOut = @(auth.timedOut == nil? 120 : [auth.timedOut intValue]);
	sys.crnCode = auth.crnCode;
	entitySettings.disableAutoLogin = auth.disableAutoLogin;
	if ([entitySettings.disableAutoLogin isEqualToString:@"Y"])
		[entitySettings setAutoLogin:@"NO"];
	
    // Auto login is not supported for an SSO user, in this version of the app...
    if (isCorpSSOUser) 
        entitySettings.disableAutoLogin = @"YES";
    else if([entitySettings.disableAutoLogin isEqualToString:@"NO"])
    {
        NSDictionary *dict = @{@"Type": @"auto-login"};
        [Flurry logEvent:@"Sign In: Authentication Type" withParameters:dict];
    }
    
	int i=0;
	if (auth.userMessages != nil && [auth.userMessages count]>0) 
	{
		for (i=0; i <[auth.userMessages count]; i++) 
		{
			NSString *otherBtnTitle = nil;
			UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[(auth.userMessages)[i] msgTitle] 
																message:[(auth.userMessages)[i] msgBody] 
															   delegate:nil 
													  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
													  otherButtonTitles:otherBtnTitle,nil];
			[alert show];
		}
	}
}

#pragma mark - web extension
-(NSURL*) urlForWebExtension:(NSString*)page
{
    NSString *baseUrl = entitySettings.uri;
    NSString *sessionId = sessionID;
    NSString *locale = [[NSLocale currentLocale] localeIdentifier];
    
    NSString *urlString = [NSString stringWithFormat:@"%@/mobile/web/signin#mobile?pageId=%@&sessionId=%@&locale=%@",
                           baseUrl,
                           page,
                           sessionId,
                           locale];
    
    NSURL *url = [NSURL URLWithString:urlString];
    return url;
}


#pragma mark -
#pragma mark URLs for Travel
-(void) initURLMaps
{//the en file is included in the bundle, we will download other langs as needed
	NSString *path = [[NSBundle mainBundle] bundlePath];
	NSString *finalPath = [path stringByAppendingPathComponent:@"URLMaps.plist"];
	self.urlMapsDict = [NSDictionary dictionaryWithContentsOfFile:finalPath];
}


-(NSString *)getURLMap:(NSString *)dictName LocalConstant:(NSString *)localConstant
{
	NSDictionary *dict = urlMapsDict[dictName];
	NSString *foundVal = dict[localConstant];
	return foundVal;
}


#pragma mark -
#pragma mark Session Handling
-(BOOL) isValidSessionID:(NSString*) sessID
{
	// This filters out sessionID like 'You logged out'
	return sessID != nil && sessID.length > 24;
}


#pragma mark -
#pragma mark Initialization
-(EntitySystem *) loadSystem
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySystem" inManagedObjectContext:_context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *a = [_context executeFetchRequest:fetchRequest error:&error];
    
    if(a == nil || [a count] == 0)
    {
        NSManagedObjectContext *context = self.context;
        EntitySystem *s = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySystem" inManagedObjectContext:context];
        s.sessionId = @"";
        s.lastViewKey = @"";
        s.lastSessionID = @"";
        s.lastViewDataName = @"";
        s.lastViewName = @"LOGIN";
        s.crnCode = @"USD";
        s.currentVersion = @"1.0.0.0";
        s.debug = @NO;
        s.doReceiptMigrate = @NO;
        s.expenseCtryCode = @"USD";
        s.previousVersion = @"";
        s.productLine = @"";
        s.receiptsPath = @"";
        s.topViewName = @"";
        s.isOffline = @NO;
        
        NSError *error;
        if (![context save:&error]) {
            NSLog(@"Whoops, couldn't save initial system: %@", [error localizedDescription]);
            return nil;
        }
        return s;
    }
    else
    {
        return a[0];
    }
}

-(void) saveSystem
{
    if(self.sys != nil)
    {
        NSError *error;
        if (![_context save:&error]) {
            NSLog(@"Whoops, couldn't save system: %@", [error localizedDescription]);
        }
    }
}

-(EntitySettings*) loadSettings
{//loads up the settings entity from core data
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySettings" inManagedObjectContext:_context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(RowKey = %@)", @"ONLY"];
    
    [fetchRequest setPredicate:pred];
    
    NSError *error = nil;
    self.settingsArray = [_context executeFetchRequest:fetchRequest error:&error];
    
    if(self.settingsArray == nil || [self.settingsArray count] == 0)
    {
        //need to create
        NSManagedObjectContext *context = self.context;
        EntitySettings *setts = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySettings" inManagedObjectContext:context];
        setts.RowKey = @"ONLY";
        setts.autoLogin = @"YES"; //MOB-5761-Switched default from OFF to ON.
        setts.rememberUser = @YES;
        setts.enableTouchID = @"YES"; //MOB-21148 Switch default from OFF to ON.
        setts.serverAddress = @"172.16.230.131";
        setts.uri = [Config isGov] ? @"https://cge.concursolutions.com" : @"https://www.concursolutions.com";
        setts.requestAppRating = @"YES";
        setts.showIAd = NO;
        setts.roles = @"";
        setts.disableAutoLogin = @"NO";
        //MOB-10828
		setts.uriNonSSL = [Config isGov] ? @"https://cge.concursolutions.com" : @"https://www.concursolutions.com";
		setts.saveUserName = @"YES";
        setts.isTripItLinked = @NO;
        setts.isTripItEmailAddressConfirmed = @NO;
        NSError *error;
        if (![context save:&error]) {
            NSLog(@"Whoops, couldn't save: %@", [error localizedDescription]);
            return nil;
        }
        return setts;
    }
    else
    {
        EntitySettings *sett = (self.settingsArray)[0];
        self.isTripItLinked = [sett.isTripItLinked boolValue];
        self.isTripItEmailAddressConfirmed = [sett.isTripItEmailAddressConfirmed boolValue];
        return sett;
    }
    
}

-(void) saveSettings
{
    if (self.entitySettings != nil)
    {
        self.entitySettings.isTripItLinked = @(self.isTripItLinked);
        self.entitySettings.isTripItEmailAddressConfirmed = @(self.isTripItEmailAddressConfirmed);
        
        NSError *error = nil;
        if (![_context save:&error]) {
            NSLog(@"Whoops, couldn't save settings: %@", [error localizedDescription]);
        }
    }
    
	[self savePin:self.pin];
	[self saveConcurAccessToken:self.concurAccessToken];
    // Debug log for enpty Concur Access token
    if (![self.concurAccessToken lengthIgnoreWhitespace])
    {
        ALog(@"*************** saved empty token! ****************");
        DLog(@"*************** saved empty token! ****************")
    }
    // Debug log for enpty Concur Access token
	[self saveConcurAccessTokenSecret:self.concurAccessTokenSecret];
    [self saveSession:self.sessionID];
    
    if(self.userName != nil)
        [self saveUserId:self.userName];
    
    if ([self.userInputOnLogin lengthIgnoreWhitespace])
    {
        [self saveUserInputOnLogin:self.userInputOnLogin];
        
        if ([SignInWithTouchID canEvaluatePolicy])
        {
            [_keychainManager saveACLuserID:self.userInputOnLogin];
            if ([self.pin lengthIgnoreWhitespace])
            {
                [_keychainManager saveACLpassword:self.pin];
            }
        }
    }
    
    // set the server path to the path specified in the settings.
    [[CTENetworkSettings sharedInstance] saveServerURL:[ExSystem sharedInstance].entitySettings.uri];


}

-(void) loadSiteSettings
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySiteSetting" inManagedObjectContext:_context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    self.siteSettings = [_context executeFetchRequest:fetchRequest error:&error];
}


-(void) saveSiteSetting:(NSString *)val type:(NSString *)type name:(NSString *)name
{
    EntitySiteSetting *ss = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySiteSetting" inManagedObjectContext:_context];
    ss.value = val;
    ss.type = type;
    ss.name = name;
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't save sitesetting: %@", [error localizedDescription]);
    }
}

-(void) clearSiteSettings
{
    for(EntitySiteSetting *ss in self.siteSettings)
    {
        [_context deleteObject:ss];
    }
    self.siteSettings = nil;
}


-(NSString*)getSiteSetting:(NSString*)name withType:(NSString*) type
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySiteSetting" inManagedObjectContext:_context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(name = %@) and (type = %@)", name, type];
    [fetchRequest setPredicate:pred];
//    pred = [NSPredicate predicateWithFormat:@"(type = %@)", type];
//    [fetchRequest setPredicate:pred];

    NSError *error;
    NSArray *a = [_context executeFetchRequest:fetchRequest error:&error];
    if(a != nil && [a count] > 0)
    {
        EntitySiteSetting *ss = a[0];
        return ss.value;
    }
    else
        return nil;
}

-(BOOL) siteSettingAllowsExpenseApprovals
{
    NSString *allow = [self getSiteSetting:@"AllowApprovals" withType:@"OTMODULE"];
    return ![allow isEqualToString:@"N"]; // MOB-12147 default to YES
}
-(BOOL) siteSettingAllowsExpenseReports
{
    NSString *allow = [self getSiteSetting:@"AllowReports" withType:@"OTMODULE"];
    return ![allow isEqualToString:@"N"]; // MOB-12147 default to YES
}

-(BOOL) siteSettingAllowsTravelBooking
{
    NSString *allow = [self getSiteSetting:@"AllowTravelBooking" withType:@"OTMODULE"];
    return ![allow isEqualToString:@"N"]; // MOB-12147 default to YES
}

-(BOOL) siteSettingVoiceBookingEnabled
{
    NSString *allow = [self getSiteSetting:@"VoiceBookingEnabled" withType:@"mobile"];
    return [allow isEqualToString:@"Y"];
}

-(BOOL) siteSettingAllowsTouchID
{
    NSString *allow = [self getSiteSetting:@"AllowFingerPrintLogin" withType:@"OTMODULE"];
    return [allow isEqualToString:@"Y"];
}

-(BOOL) siteSettingGemsEnabled
{
    NSString *allow = [self getSiteSetting:@"ENABLE_GEMS" withType:@"MILEAGE"];
    return [allow isEqualToString:@"Y"];
}

-(BOOL) siteSettingHotelStreamingEnabled
{
    NSString *allow = [self getSiteSetting:@"StreamHotelSearchResults" withType:@"OTMODULE"];
    return [allow isEqualToString:@"Y"];
}

-(BOOL) siteSettingHasFixedTA
{
//    return NO;
//    return YES;

    NSString *allow = [self getSiteSetting:@"HasTravelAllowanceFixed" withType:@"Mobile"];
    return [allow isEqualToString:@"Y"];
}

-(BOOL) siteSettingCanImportTrips
{
    NSString *allow = [self getSiteSetting:@"CanImportTrips" withType:@"Mobile"];
    return [allow isEqualToString:@"Y"];
}

-(NSString*) getUserSetting:(NSString*) name withDefault:(NSString*) defValue
{
    NSString* result = [self getSiteSetting:name withType:@"USER"];
    if (result == nil)
        result = defValue;
    
    return result;
}

-(void)saveUserSetting:(NSString*)val name:(NSString*)name
{
    [self saveSiteSetting:val type:@"USER" name:name];
}

-(void) loadRoles
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityRoles" inManagedObjectContext:_context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    self.roles = [_context executeFetchRequest:fetchRequest error:&error];
}


-(void) saveRole:(NSString *)role
{
    EntityRoles *entityRole = [NSEntityDescription insertNewObjectForEntityForName:@"EntityRoles" inManagedObjectContext:_context];
    //NSString *trimmedString = [dirtyString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    entityRole.role = [role stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't save role: %@", [error localizedDescription]);
    }
}


-(void) clearRoles
{
    if (self.roles == nil)
        [self loadRoles];
    for(EntityRoles *role in self.roles)
    {
        [_context deleteObject:role];
    }
    self.roles = nil;
}


-(void)makeRoles:(NSString *)roleCommas
{
	if (roleCommas == nil) 
		return;
	
	NSArray *tokens = [roleCommas componentsSeparatedByString:@","];
	
    [self clearRoles];
	
	for (NSString *role in tokens)
	{
        [self saveRole:role];
	}

    [self loadRoles];
	
}


-(BOOL) hasRole:(NSString*) role
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityRoles" inManagedObjectContext:_context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(role = %@)", role];
    if ([role isEqualToString:ROLE_GOVERNMENT_USER])
        pred = [NSPredicate predicateWithFormat:@"(role = %@ or role = %@)", role, ROLE_GOVERNMENT_TRAVELER];
    
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *a = [_context executeFetchRequest:fetchRequest error:&error];
    if(a != nil && [a count] > 0)
    {
        return YES;
    }
    else
        return FALSE;
}

#pragma mark - user config checks. Used by the 9.0/9.8 UI.

// rule checking for related featurs
- (BOOL)isTravelRelated{
    return [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER];
}
- (BOOL)isExpenseRelated{
    return [[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER];
}
/*
 - The roles checking below is only for displaying the cells and tool bar on home screen
 - Each method is independent from others
 */
- (BOOL)isTravelOnly
{
    if ([self hasRole:ROLE_TRAVEL_USER] &&
        !([self hasApprovalRole] || [self hasRole:ROLE_EXPENSE_TRAVELER] || [self hasRole:ROLE_GOVERNMENT_USER] || [self hasRole:ROLE_TRAVEL_REQUEST_APPROVER]))
    {
        return YES;
    }
    return NO;
}

- (BOOL) isExpenseOnlyUser
{
    if ([self hasRole:ROLE_EXPENSE_ONLY_USER] && ![self hasApprovalRole])
        return YES;
    return NO;
}

- (BOOL) isApprovalOnlyUser
{
    if ([self hasApprovalRole] &&
        !([self hasRole:ROLE_EXPENSE_TRAVELER] || [self hasRole:ROLE_TRAVEL_USER] || [self hasRole:ROLE_GOVERNMENT_USER] || [self hasRole:ROLE_OPEN_BOOKING_USER]))
        {
            return YES;
        }
        return NO;
}

- (BOOL) isTravelAndApprovalOnlyUser
{
// MOB-17062 -Added check for open booking. Openbooking users have trips but not trip booking. 
    if ( ([self hasRole:ROLE_TRAVEL_USER] || [self hasRole:ROLE_OPEN_BOOKING_USER]) && [self hasApprovalRole] && ![[ExSystem sharedInstance] hasRole:ROLE_EXPENSE_TRAVELER])
    {
        return YES;
    }
    return NO;
}

- (BOOL) isTravelAndExpenseOnlyUser
{
    if ( ([self hasRole:ROLE_EXPENSE_TRAVELER] && [self hasRole:ROLE_TRAVEL_USER]) && ![self hasApprovalRole])
    {
        return YES;
    }

    // MOB-16743 triplink is open booking.
    // It is possible to have an expense user that has open booking, but not regular concur booking.  This is lame.
    if ([self isExpenseOnlyUser] && [self hasRole:ROLE_OPEN_BOOKING_USER]) {
        return YES;
    }

    return NO;
}

- (BOOL) isExpenseAndApprovalOnlyUser
{
    
    if ( ([self hasApprovalRole] && [self hasRole:ROLE_EXPENSE_TRAVELER] && [self hasRole:ROLE_EXPENSE_ONLY_USER]) && ![self hasRole:ROLE_TRAVEL_USER] )
    {
        return YES;
    }
    return NO;
}

-(BOOL) hasApprovalRole
{
    return [self hasRole:ROLE_EXPENSE_MANAGER] || [self hasRole:ROLE_INVOICE_APPROVER] || [self hasRole:MOBILE_INVOICE_PAYMENT_USER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_REQUEST_APPROVER]
    || [[ExSystem sharedInstance] hasRole:ROLE_TRIP_APPROVER] || [[ExSystem sharedInstance]hasRole:ROLE_MOBILE_INVOICE_PURCH_APRVR];

}

- (BOOL) isAllFeatureUser{
    @throw [NSException exceptionWithName:NSInternalInconsistencyException
                                   reason:[NSString stringWithFormat:@"Method not yet implemeted %@", NSStringFromSelector(_cmd)]
                                 userInfo:nil];
}

- (BOOL) isCTEUser
{
    if (!([self isBreeze] || [self isBronxUser] || [self isGovernment] || [self isTravelOnly] || [self isExpenseOnlyUser] || [self isApprovalOnlyUser]))
        {
            return YES;
        }
    return NO;
}


- (BOOL)hasTravelRequest
{
    NSString *hasTravelRequest = [self getUserSetting:@"HasTravelRequest" withDefault:@"false"];
    return [hasTravelRequest isEqualToString:@"true"] && [Config isTravelRequestEnabled];
}

- (BOOL)isRequestApprover
{
    NSString *hasTravelRequest = [self getUserSetting:@"IsRequestApprover" withDefault:@"false"];
    return [hasTravelRequest isEqualToString:@"true"];
}

- (BOOL)isRequestUser
{
    NSString *hasTravelRequest = [self getUserSetting:@"IsRequestUser" withDefault:@"false"];
    return [hasTravelRequest isEqualToString:@"true"];
}


- (BOOL)hasTravelBooking // This checks if Booking a Trip is allowed for the user (checks on Roles and siteSettings)
{
    // apparently we have a long list of roles to check now...
    // check if the user has Travel only role

    return ([self hasRole:ROLE_TRAVEL_USER] || [self hasRole:ROLE_GOVERNMENT_TRAVELER]) && ![self hasRole:ROLE_EXPENSE_ONLY_USER] && [self siteSettingAllowsTravelBooking];
}

- (BOOL)hasReceiptStore 
{
    return ![@"Y" isEqualToString:[self getSiteSetting:@"HIDE_RECEIPT_STORE" withType:@"CTE_EXPENSE_ADMIN"]] && [self hasRole:ROLE_EXPENSE_TRAVELER];
}

- (BOOL) canUseConditionalFields
{
    return [@"Y" isEqualToString:[self getSiteSetting:@"ENABLE_DYNAMIC_FIELD_EVALUATION" withType:@"UI_MODIFICATION"]];
}

- (BOOL)hasCarMileageOnHome
{
    // MOB-16698
    // Server team made a last minute change to the test drive configuration, which breaks our overlays. We're going to ignore car mileage for now
    return (![@"N" isEqualToString:[self getSiteSetting:@"PersonalCarMileageOnHome" withType:@"Mobile"]] && [self hasRole:ROLE_EXPENSE_TRAVELER] && ![self isTestDrive]);
}

- (BOOL)hasJpt
{
    return [@"Y" isEqualToString:[self getSiteSetting:@"ENABLE_JPY_PUB_TRANS" withType:@"SITE"]];
}

-(BOOL)hasFeedBacks
{
    return [Config isNewHotelBooking];
}

- (BOOL)canBookRail
{
    return [self hasRole:ROLE_GOVERNMENT_TRAVELER] || ([self hasRole:ROLE_AMTRAK_USER] && (![self hasRole:ROLE_ITINVIEWER_USER] || [self hasRole:ROLE_TRAVEL_USER]));
}

- (BOOL)hasLocateAndAlert
{
    return [@"Y" isEqualToString:[self getSiteSetting:@"LocateAndAlert" withType:@"OTMODULE"]] &&
    [self hasRole:ROLE_LNA_USER];
}

- (BOOL)hasGateGuru
{
    return [self hasRole:@"GateGuru_User"];
}

- (BOOL)hasTaxiMagic
{
    return [self hasRole:@"Taxi_User"];
}

- (BOOL)hasMetro
{
    return [self hasRole:@"Metro_User"];
}

- (BOOL)hasExpenseIt
{
    return [self hasRole:@"ExpenseItUser"];
}

- (BOOL)shouldShowPriceToBeatGenerator
{
    return [self hasRole:@"ShowP2BGenerator"];
}

- (NSString*)getUserType
{
    if ([self isExpenseOnlyUser])
    {
        return @"Expense Only";
    }
    else if ([self isTravelOnly])
    {
        return @"Travel Only";
    }
    else if ([self isApprovalOnlyUser])
    {
        return @"Approval Only";
    }
    else if ([self isBreeze])
    {
        return @"Breeze";
    }
    else if ([self isCTEUser])
    {
        return @"CTE";
    }
    return @"";
}

#pragma mark -
#pragma salesforce credentials
// just to be consistent with our other save methods
- (void)saveSalesForceToken:(NSString *)token andUrl:(NSString *)url
{
    if (token != nil && url != nil) {
        [self deleteExistingUsers];

        // Add a new user to core data.
        EntitySalesForceUser *user = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySalesForceUser" inManagedObjectContext:[self context]];
        user.accessToken = token;
        user.instanceUrl = url;

        NSError *error;
        if (![_context save:&error]) {
            NSLog(@"Whoops, couldn't save salesforce info: %@", [error localizedDescription]);
        }
    }
}

-(void) deleteExistingUsers // The caller is responsible for saving the changes to core data
{
    // Find the existing feed (if any)
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySalesForceUser" inManagedObjectContext:[self context]];
    [fetchRequest setEntity:entity];

    NSError *error;
    NSArray *aFetch = [[self context] executeFetchRequest:fetchRequest error:&error];
    // TODO: Handle error

	// Delete old users (if any)
    for(EntitySalesForceUser *existingUser in aFetch)
        [[self context] deleteObject:existingUser];
}


#pragma mark Whats New Checks
-(void) doWhatsNewChecks
{
    NSString *currentVersion = sys.currentVersion;
    
    NSString *ver = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
    if (currentVersion == nil || ![ver isEqualToString:currentVersion]) 
    {
        sys.currentVersion = ver;
//        [self checkReceiptMigration];
        //  MOB-13570 - Do not show tip overlay
        // if we need to show tip overlay or whatsnew on home then set this flag to YES here or in the home VC and save it.
        // Leave this code commented so we know where to reset this if required during upgrade
//        sys.showWhatsNew = [NSNumber numberWithBool: YES];

        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentsDirectory = paths[0];
        NSString *path = [documentsDirectory stringByAppendingPathComponent:@"MobileSettings.plist"];
        NSMutableDictionary* plistDict = [[NSMutableDictionary alloc] initWithContentsOfFile:path];
        
        if(plistDict != nil)
        {            
            //            MOB-5393
            //            If we are in an upgrade situation, then a check is made to see if the username was able to be loaded from the keychain, and if not, then we check to see if it is in the current dictionary, and if so, then we set the variable to that value.  The same goes for the pin.  In order for pin to be pulled from the dictionary the user will have need to be upgrading from 7.2.1, since pin in the keychain was added around then.  The access token implementation came after the migration to the keychain, so there is no need to check for it in the dictionary.
            if(plistDict[@"USER_NAME"] != nil && self.userName == nil)
                self.userName = plistDict[@"USER_NAME"];
            
            if(plistDict[@"PIN"] != nil && self.pin == nil)
                self.pin = plistDict[@"PIN"];
        }
        
        
        [FileManager cleanCache];
        
        [self saveSystem];
        
        //kill the old plist
        NSFileManager *manager = [NSFileManager defaultManager];
        [manager removeItemAtPath:path error:NULL];
        
        NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
        [prefs setObject:@"****" forKey:@"ConcurPIN"];
    }
}


#pragma mark -
#pragma mark Base Init Stuff
-(void)doBaseInit
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*)[[UIApplication sharedApplication] delegate];
    self.context = [ad managedObjectContext];
    
	if(self.msgControl == nil)
	{
        self.sys = [self loadSystem];
		self.msgControl = [MsgControl alloc];
		[self.msgControl init:nil];
        [MessageRegistrationCenter registerAllMessages];  // Create msg map
        
        [NotificationController sharedInstance]; // Initialize notificationcontroller and its event map
        
        if ([Config isDevBuild]) {
            [self loadNetworkConnectionSettings];
        }
        
        self.tripOffersDisplayPreference = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        self.currencies = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        
        self.useFacebook = [self.sys.useFacebook boolValue];

        [self loadSiteSettings];
        [self loadRoles];
        
        self.pin = [self loadPin];
		self.concurAccessToken = [self loadConcurAccessToken];
		self.concurAccessTokenSecret = [self loadConcurAccessTokenSecret];
        self.userName = [self loadUserId];
        self.userInputOnLogin = [self loadUserInputOnLogin];
        self.sessionID = [self loadSession];
        
        [self doWhatsNewChecks];

        self.entitySettings = [self loadSettings];
		
        self.offersValidityChecking = @"YES";
		[self initURLMaps];
		
		self.receiptData = [[ReceiptData alloc] initPlistFiles];
		[receiptData readPlist]; 
		
		self.cacheData = [[CacheData alloc] initPlistFiles];
		[cacheData readPlist]; 
		
		self.imageControl = [[ImageControl alloc] init];
		self.imageControl.exSys = self;
	}
}


#pragma mark -
#pragma mark FindMe Initialization
-(void)doFindMe
{
//	findMe = [FindMe alloc];
//	[findMe init:self];
//	isFindingMe = NO;
}


#pragma mark -
#pragma mark Utility Methods
-(BOOL) isUsingConcurProductionServer
{
    NSRange concursolutionsRange = [[ExSystem sharedInstance].entitySettings.uri rangeOfString:@"concursolutions.com" options:NSCaseInsensitiveSearch];
    NSRange rqaRange = [[ExSystem sharedInstance].entitySettings.uri rangeOfString:@"rqa" options:NSCaseInsensitiveSearch];
    return (concursolutionsRange.location != NSNotFound && rqaRange.location == NSNotFound);
}

+(BOOL) isLandscape 
{
    if (!UIDeviceOrientationIsValidInterfaceOrientation([UIDevice currentDevice].orientation))
        return (UIDeviceOrientationIsLandscape([[UIApplication sharedApplication] statusBarOrientation]));
    else
        return (UIDeviceOrientationIsLandscape([UIDevice currentDevice].orientation));
}


+(BOOL) is5
{
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
    {
        CGSize result = [[UIScreen mainScreen] bounds].size;
        if(result.height == 480)
        {
            // iPhone 4
            return NO;
        }
        else if(result.height == 568)
        {
            // iPhone 5
            return YES;
        }
        else
            // unknow screen size
            return NO;
    }
    else
        return NO;
}

+(BOOL) is6Plus // 6+
{
    CGFloat sysVers = [UIDevice currentDevice].systemVersion.floatValue;
    return sysVers >= 6.0;
}

+(BOOL) is7Plus //iOS 7+
{
    if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_6_1) {
        return NO;
    } else {
        return YES;
    }
}

+(BOOL) is8Plus // iOS 8+
{
    CGFloat sysVers = [UIDevice currentDevice].systemVersion.floatValue;
    return sysVers >= 8.0;
}


//[[UIDevice currentDevice] systemVersion]

+ (void) addMsg:(Msg *)msg
{//when a message is added it will auto execute and attempt to fetch the data
	[[ExSystem sharedInstance].msgControl add:msg];
}

#pragma mark -
#pragma mark Color Button Maker
+(UIBarButtonItem*) makeBackButton:(NSString*)text4Btn target:(id)tgt action:(SEL) sel
{
	UIFont* sysFont13B = [UIFont boldSystemFontOfSize:13]; 
	CGSize s = [text4Btn sizeWithFont:sysFont13B];
	
	const int kButtonA2RW_Max = 80;
	const int kButtonA2RW_Min = 50; //40;
	const int kButtonA2RH = 30;
    
	int size = (s.width > kButtonA2RW_Max) ? kButtonA2RW_Max : ((s.width < kButtonA2RW_Min)?kButtonA2RW_Min:s.width);
	size += 10;
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, size, kButtonA2RH)];
	
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	
    NSString *btnImageName = @"fake_back_button";
    //if ([UIDevice isPad])
        //btnImageName = @"fake_ipad_gray_back_button";
        
	[button setBackgroundImage:[[UIImage imageNamed:btnImageName]
                                stretchableImageWithLeftCapWidth:12.0f
                                topCapHeight:0.0f]
                      forState:UIControlStateNormal];
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0, size, kButtonA2RH);
	
	[button addTarget:tgt action:sel forControlEvents:UIControlEventTouchUpInside];
	
	// Text needs to be lifted up by one pixel to match default ones
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(8, 0, size-10, kButtonA2RH-3)];  
	
	lbl.font = sysFont13B;
	lbl.textColor = [UIColor whiteColor];
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
	[lbl setLineBreakMode:NSLineBreakByTruncatingTail];
	
	lbl.text = text4Btn;
	
	[v addSubview:button];
	[v addSubview:lbl];
	
	//create a UIBarButtonItem with the button as a custom view
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:v];
    
    
    // apparently we push these screens in a non-standard way and need to create a custom back button instead of getting one for free from the navigation controller.  :(
    //UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithTitle:text4Btn style:UIBarButtonItemStyleBordered target:tgt action:sel];
    
	return customBarItem;
	
}

+(UIBarButtonItem *)makeSilverBarButton:(NSString *)btnTitle width:(float)w height:(float)h selectorString:(NSString *)selectorString target:(NSObject*) obj
{
    UILabel *lbl = nil;
    lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	lbl.font = [UIFont boldSystemFontOfSize:12];
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
	
	lbl.textColor =  [UIColor colorWithRed:41.0/255 green:41.0/255 blue:41.0/255 alpha:1.0];
	
	lbl.text = btnTitle;
    
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 2, w, h)];
	
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	
	NSString *btnImage = @"button_gray";
    
    UIEdgeInsets insets = UIEdgeInsetsMake(0.0f, 12.0f, 0.0f, 12.0f);
	
    // MOB-13110
    // iOS 5 does not support this api.  causes an app crash.
	if ([UIImage instancesRespondToSelector:@selector(resizableImageWithCapInsets:resizingMode:)]) {
        [button setBackgroundImage:[[UIImage imageNamed:btnImage] resizableImageWithCapInsets:insets resizingMode:UIImageResizingModeStretch] forState:UIControlStateNormal];
    } else {
        [button setBackgroundImage:[UIImage imageNamed:btnImage] forState:UIControlStateNormal];
    }
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0, w, h);
	
	[button addTarget:obj action:NSSelectorFromString(selectorString) forControlEvents:UIControlEventTouchUpInside];
	
	[v addSubview:button];
	[v addSubview:lbl];
    
	//create a UIBarButtonItem with the button as a custom view
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:v];
	
	return customBarItem;

}

+(UIBarButtonItem *)makeColoredButton:(NSString *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle SelectorString:(NSString *)selectorString MobileVC:(MobileViewController *)mvc
{
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 2, w, h)];
	
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	
	NSString *btnImage = @"red_button";
	if([btnColor isEqualToString:@"BLUE"])
		btnImage = @"blue_button";
	else if([btnColor isEqualToString:@"BLUE_INACTIVE"])
		btnImage = @"bluelighter_button";
	else if([btnColor isEqualToString:@"GREEN"])
		btnImage = @"green_button";
	else if([btnColor isEqualToString:@"GREEN_INACTIVE"])
		btnImage = @"greenlighter_button";
	else if([btnColor isEqualToString:@"GRAY"])
		btnImage = @"gray_button";
	else if([btnColor isEqualToString:@"GRAY_INACTIVE"])
		btnImage = @"graylighter_button";
	else if([btnColor isEqualToString:@"DELETE"])
		btnImage = @"delete_button";
	else if([btnColor isEqualToString:@"DELETE_INACTIVE"])
		btnImage = @"delete_light_button";
	else if([btnColor isEqualToString:@"SMOKE"])
		btnImage = @"smoke_button";
	else if([btnColor isEqualToString:@"BLUE_LANDSCAPE"])
		btnImage = @"blue_landscape";
    else if([btnColor isEqualToString:@"BACK"])
		btnImage = @"fake_back_button";
    else if([btnColor isEqualToString:@"DARK_BLUE"])
		btnImage = @"ipad_drk_blue_button";
    else if([btnColor isEqualToString:@"DARK_BLUE_OFFERS_PAD"])
        btnImage = @"ipad_drk_blue_button";
    else if([btnColor isEqualToString:@"DARK_BLUE_OFFERS"])
        btnImage = @"blank_button";
	
    float capW = 7.0f;
    if([btnColor isEqualToString:@"BACK"])
        capW = 18.0f;
	[button setBackgroundImage:[[UIImage imageNamed:btnImage]
								stretchableImageWithLeftCapWidth:capW 
								topCapHeight:0.0f]
					  forState:UIControlStateNormal];
	
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0, w, h);
	
	[button addTarget:mvc action:NSSelectorFromString(selectorString) forControlEvents:UIControlEventTouchUpInside];
	
	UILabel *lbl = nil;
    if([btnColor isEqualToString:@"BACK"])
        lbl = [[UILabel alloc] initWithFrame:CGRectMake(10, -1, w-15, h)];
    else if ([btnColor isEqualToString:@"DARK_BLUE_OFFERS"] || [btnColor isEqualToString:@"DARK_BLUE_OFFERS_PAD"])
        lbl = [[UILabel alloc] initWithFrame:CGRectMake(28, 0, w-32, h)];
    else
        lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	lbl.font = [UIFont boldSystemFontOfSize:12];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.shadowColor = [UIColor darkGrayColor];
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
	
	lbl.textColor =  [UIColor whiteColor];
	
	lbl.text = btnTitle;
    
    if ([btnColor isEqualToString:@"DARK_BLUE_OFFERS"] || [btnColor isEqualToString:@"DARK_BLUE_OFFERS_PAD"]) {
        UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(7, 7, 15, 15)];
        if ([UIDevice isPad])
            iv.image = [UIImage imageNamed:@"icon_offers_pad"];
        else 
            iv.image = [UIImage imageNamed:@"icon_offers"];
        [iv setContentMode:UIViewContentModeScaleAspectFit];
        [button addSubview:iv];
        [button setAlpha:0.9];
    }
	
	[v addSubview:button];
	[v addSubview:lbl];
	   
	//create a UIBarButtonItem with the button as a custom view
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:v];
	
	return customBarItem;
}


+(UIButton *)makeColoredButtonRegular:(NSString *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle SelectorString:(NSString *)selectorString MobileVC:(MobileViewController *)mvc
{
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	
	NSString *btnImage = @"red_button";
	
    if ([btnColor isEqualToString:@"DARK_BLUE_HOME_IPAD"])
        btnImage = @"button_home_ipad";
	else if([btnColor isEqualToString:@"BLUE"])
		btnImage = @"blue_button";
	else if([btnColor isEqualToString:@"BLUE_INACTIVE"])
		btnImage = @"bluelighter_button";
    else if([btnColor isEqualToString:@"GREEN_BIG"])
		btnImage = @"signin_greenbutton";
	else if([btnColor isEqualToString:@"GREEN"])
		btnImage = @"green_button";
	else if([btnColor isEqualToString:@"GREEN_INACTIVE"])
		btnImage = @"greenlighter_button";
	else if([btnColor isEqualToString:@"GRAY"])
		btnImage = @"gray_button";
	else if([btnColor isEqualToString:@"GRAY_DARK"])
		btnImage = @"gray_dark_button";
	else if([btnColor isEqualToString:@"GRAY_INACTIVE"])
		btnImage = @"graylighter_button";
    else if ([btnColor isEqualToString:@"GRAY_SOCIAL"])
        btnImage = @"social_button";
	else if([btnColor isEqualToString:@"DELETE"])
		btnImage = @"delete_button";
	else if([btnColor isEqualToString:@"DELETE_INACTIVE"])
		btnImage = @"deletelight_button";
	else if([btnColor isEqualToString:@"BLACK"])
		btnImage = @"black_button";
	else if([btnColor isEqualToString:@"BLACK_INACTIVE"])
		btnImage = @"black_button_light";
	else if([btnColor isEqualToString:@"SMOKE"])
		btnImage = @"smoke_button";
	else if([btnColor isEqualToString:@"RED"])
		btnImage = @"red_button";
	else if([btnColor isEqualToString:@"RED_LIGHT"])
		btnImage = @"redlighter_button";
	else if([btnColor isEqualToString:@"NEXT"])
    {
        btnImage = @"next_button";
    }
	else if([btnColor isEqualToString:@"PREVIOUS"])
    {
        btnImage = @"previous_button";
    }
    else if([btnColor isEqualToString:@"DARK_BLUE"])
		btnImage = @"ipad_drk_blue_button";
    else if([btnColor isEqualToString:@"JOIN"])
		btnImage = @"button_join_sm";
    else if([btnColor isEqualToString:@"SIGNIN"])
		btnImage = @"button_signin_gray";
    else if([btnColor isEqualToString:@"FB"])
		btnImage = @"button_facebook";
    else if([btnColor isEqualToString:@"DARK_BLUE_OFFERS_PAD"])
        btnImage = @"ipad_drk_blue_button";
    else if([btnColor isEqualToString:@"SIGN_IN_GREEN"])
        btnImage = @"signin_button";
    
    if([btnColor isEqualToString:@"PREVIOUS"])
	{
		[button setContentHorizontalAlignment:UIControlContentHorizontalAlignmentRight];
		btnTitle = [NSString stringWithFormat:@"%@   ", btnTitle];
	}
	else if([btnColor isEqualToString:@"NEXT"] )
	{
		[button setContentHorizontalAlignment:UIControlContentHorizontalAlignmentLeft];
		btnTitle = [NSString stringWithFormat:@"   %@", btnTitle];
	}
    else if([btnColor isEqualToString:@"FB"] )
	{
//		[button setContentHorizontalAlignment:UIControlContentHorizontalAlignmentLeft];
		btnTitle = [NSString stringWithFormat:@"       %@", btnTitle];
	}
	
    
    if([btnColor isEqualToString:@"DELETE"] || [btnColor isEqualToString:@"DELETE_INACTIVE"])
    {
        [button setBackgroundImage:[[UIImage imageNamed:btnImage]
                                    stretchableImageWithLeftCapWidth:24.0f 
                                    topCapHeight:0.0f]
                          forState:UIControlStateNormal];
    }
    else if([btnColor isEqualToString:@"FB"])
    {
        [button setBackgroundImage:[[UIImage imageNamed:btnImage]
                                    stretchableImageWithLeftCapWidth:50.0f 
                                    topCapHeight:0.0f]
                          forState:UIControlStateNormal];
    }
    else
    {
        [button setBackgroundImage:[[UIImage imageNamed:btnImage]
								stretchableImageWithLeftCapWidth:12.0f 
								topCapHeight:0.0f]
					  forState:UIControlStateNormal];
    }
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0, w, h);
	
	[button addTarget:mvc action:NSSelectorFromString(selectorString) forControlEvents:UIControlEventTouchUpInside];
	
	[button setTitle:btnTitle forState:UIControlStateNormal];
    
    if([btnColor isEqualToString:@"FB"] || [btnColor isEqualToString:@"SIGNIN"])
        button.titleLabel.font = [UIFont boldSystemFontOfSize:18.0];
    else
        button.titleLabel.font = [UIFont boldSystemFontOfSize:12.0];
    
    if([btnColor isEqualToString:@"GRAY"] || [btnColor isEqualToString:@"SIGNIN"] )
    {
        [button.titleLabel setTextColor:[UIColor blackColor]];
    }
	
	return button;
}

#pragma mark -
#pragma mark Credentials

-(void) updateTimeOfLastGoodRequest
{
    [[MCLogging getInstance] log:@"ExSystem::updateTimeOfLastGoodRequest" Level:MC_LOG_DEBU];
	self.timeLastGoodRequest = [NSDate date];
}

-(void) useOnlineSession:(NSString*)sessionIdentifier
{
    [[MCLogging getInstance] log:@"ExSystem::useOnlineSession" Level:MC_LOG_DEBU];
    self.sessionID = sessionIdentifier;
    self.timeLastGoodRequest = [NSDate date];
}

-(void) useOfflineSession
{
    [[MCLogging getInstance] log:@"ExSystem::useOfflineSession" Level:MC_LOG_DEBU];
    [ExSystem sharedInstance].sessionID = @"OFFLINE";
    self.timeLastGoodRequest = nil;
}

-(void) clearSession
{
    [self clearSessionWithOptionToSave:YES];
}

-(void) clearSessionWithOptionToSave:(BOOL)save
{
    [[MCLogging getInstance] log:@"ExSystem::clearSessionWithOptionToSave" Level:MC_LOG_DEBU];

	self.sessionID = nil;
    self.timeLastGoodRequest = nil;
    // MOB-9406 Clear saved previous attendee key, if user logs out.
    self.prevAtnKey = nil;
    
    if (save)
    {
        // Save session (in key chain)
        [self saveSession:self.sessionID];
    }
}

-(void) clearNonFacebookCredentialsAndSession
{
	self.pin = nil;
	self.concurAccessToken = nil;
	self.concurAccessTokenSecret = nil;
    [self clearSessionWithOptionToSave:NO];
	
	[self saveSettings];
}

-(void) clearUserCredentialsAndSession
{
	[self clearNonFacebookCredentialsAndSession];
    
    if (self.sys != nil)
    {
        // Clear Facebook credentials
        self.sys.fbUserId = nil;
        self.sys.fbEmail = nil;
        self.sys.fbUserName = nil;
        self.sys.fbFirstName = nil;
        self.sys.fbLastName = nil;
        self.sys.fbBirthDate = nil;
        [self saveSystem];
    }
}

-(void) clearAccessToken
{
	self.concurAccessToken = nil;
	self.concurAccessTokenSecret = nil;
	
	[self saveSettings];
}

#pragma mark -
#pragma mark Cookie stuff
-(void) deleteCorpSSOSessionCookie
{
    NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies];
    
    NSHTTPCookie *cOld = nil;
    NSHTTPCookie *cNew = nil;
    
    for (NSHTTPCookie *cookie in cookies) 
    {
        if ([[cookie name] isEqualToString:@"MABQRN"]) // Grab the latest version of session cookie
        {
            cNew = cookie;
        }
        else if ([[cookie name] isEqualToString:@"OTSESSIONAABQRN"]) 
        {
            cOld = cookie;
        }
    }
    
    if (cOld != nil) 
    {
        [[NSHTTPCookieStorage sharedHTTPCookieStorage] deleteCookie:cOld];
    }
    
    if (cNew != nil) 
    {
        [[NSHTTPCookieStorage sharedHTTPCookieStorage] deleteCookie:cNew];
    }
}

#pragma mark -
#pragma mark Keychain stuff

-(NSString *)loadConcurAccessToken
{
    return [self.keychainManager loadConcurAccessToken];
}


-(void)saveConcurAccessToken:(NSString *)sToken
{
    [self.keychainManager saveConcurAccessToken:sToken];
}

-(NSString *)loadConcurAccessTokenSecret
{
    return [self.keychainManager loadConcurAccessTokenSecret];
}

-(void)saveConcurAccessTokenSecret:(NSString *)cSecret
{
    [self.keychainManager saveConcurAccessTokenSecret:cSecret];
}


-(void)clearCompanySSOLoginPageUrl
{
    [self.keychainManager clearCompanySSOLoginPageUrl];
}

-(NSString *)loadCompanySSOLoginPageUrl
{
    return [self.keychainManager loadCompanySSOLoginPageUrl];
}

-(void)saveCompanySSOLoginPageUrl:(NSString *)ssoUrl
{
    [self.keychainManager saveCompanySSOLoginPageUrl:ssoUrl];
}

-(NSString *)loadPin
{
    return [self.keychainManager loadPin];
}


-(void)savePin:(NSString *)sPin
{
    [self.keychainManager savePin:sPin];
}


-(NSString *)loadSession
{
    return [self.keychainManager loadSession];
}


-(void)saveSession:(NSString *)sSession
{
    [self.keychainManager saveSession:sSession];
}


-(NSString *)loadUserId
{
    return [self.keychainManager loadUserId];
}


-(void)saveUserId:(NSString *)sUserId
{
    [self.keychainManager saveUserId:sUserId];
}

// MOB-10893
-(void)clearUserId
{
    [self.keychainManager clearUserId];
}

// MOB-19094 save user input and then show it next time when start the app.
// currently the app always shows "userID" no matter what user entered(email or userID)
- (NSString *)loadUserInputOnLogin
{
    return [self.keychainManager loadUserInputOnLogin];
}

- (void)saveUserInputOnLogin:(NSString *)sUserInputOnLogin
{
    [self.keychainManager saveUserInputOnLogin:sUserInputOnLogin];
}

- (void)clearUserInputOnLogin
{
    [self.keychainManager clearUserInputOnLogin];
}

// MOB-10893 : handle caching of company code.
-(void)clearCompanyCode
{
    [self.keychainManager clearCompanyCode];
}

-(NSString *)loadCompanyCode
{
    return [self.keychainManager loadCompanyCode];
}

-(void)saveCompanyCode:(NSString *)cCode
{
    [self.keychainManager saveCompanyCode:cCode];
}


#pragma mark -
#pragma mark Stolen methods from SettingsData
-(BOOL) isCorpUser
{
	return [self isCorpUserProductLine:self.sys.productLine];
}

-(BOOL) isCorpUserProductLine:(NSString*)productLine
{
	return productLine != nil && [productLine isEqualToString:PROD_CORP];
}

/**
 Checks if the product offering is Test Drive.  Test drive is HK-TRIAL
 */
- (BOOL)isTestDrive
{
    if ([self.sys.productOffering isEqualToString:@"HK-TRIAL"])
    {
        return YES;
    }

    return NO;
}

-(BOOL) isBreeze
{
	return self.sys.productLine != nil && [self.sys.productLine isEqualToString:PROD_BREEZE] && ![PROD_OFFER_BRONX isEqualToString:self.sys.productOffering] ;
}

-(BOOL) isBronxUser
{
	return [self isBronxUserProductLine:self.sys.productLine productOffering:self.sys.productOffering];
}

-(BOOL) isBronxUserProductLine:(NSString*)productLine productOffering:(NSString*)productOffering
{
	return productLine != nil && [productLine isEqualToString:PROD_BREEZE] &&
		productOffering != nil && [productOffering isEqualToString:PROD_OFFER_BRONX];
}

-(BOOL) isGovernment
{
    return self.sys.productLine != nil && [self.sys.productLine isEqualToString:PROD_GOVERNMENT];
}

-(BOOL) enableLimitedNewFeature
{
	return YES;
}

-(BOOL) enableAttendeeEditing
{
	return YES;
}

-(BOOL) enableAttendeeEditingInMobileExpense
{
	// Feb 8, 2011.  Per Prashanth's request, attendee editing is no longer available
	// in the mobile expense part of the application.  Attendee functionality is only
	// available in the context of a report.  Edit the expense there to add an attendee.
	return NO;
}	 

-(BOOL) isQADevOrProdConcurCompany
{
	NSString* loginId = [self.userName lowercaseString];
	
	return (![self.entitySettings.uri hasPrefix:@"https://www.concursolutions.com"] && ![self.entitySettings.uri hasPrefix:@"https://concursolutions.com"])
    || [loginId hasSuffix:@"@concur.com"] || [loginId hasSuffix:@"@rndconcur.com"]
    || [loginId hasSuffix:@"@democoncur.com"] || [loginId hasSuffix:@"@concur.demo.28"]
    || [loginId hasSuffix:@"@keto.com"] ;
    
}

-(void)setMyTopViewName:(NSString *)mary
{
	self.sys.topViewName = mary;
}

//-(void) checkReceiptMigration
//{
//	if ([self needsReceiptMigration])
//	{
//		if ([UIDevice isPad])
//		{
//			[FileManager removeReceiptCacheFiles];
//		}
//		else
//		{
//            self.sys.doReceiptMigrate = [NSNumber numberWithBool:YES];
//            
//            NSArray *pathsArray = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES); 
//            NSString *documentsDir = [pathsArray objectAtIndex:0];
//            self.backUpReceiptsFilePath = [documentsDir stringByAppendingPathComponent:@"BackUpReceiptData.plist"];
//			NSString *receiptsPath = [documentsDir stringByAppendingPathComponent:@"ReceiptData.plist"];
//            [FileManager backUpCachedReceiptsFileFrom:receiptsPath toFile:self.backUpReceiptsFilePath];
//            
//		}
//	}
//	else
//	{
//		self.sys.doReceiptMigrate = [NSNumber numberWithBool:NO];
//	}
//}


#pragma mark - LastGoodRequest plist

-(void) saveLastGoodRequest
{
    // The following code is modeled on
    //
    // https://developer.apple.com/library/ios/#documentation/Cocoa/Conceptual/PropertyLists/QuickStartPlist/QuickStartPlist.html
    //
    NSString *rootPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"LastGoodRequest.plist"];
    NSMutableDictionary *plistDict = [NSMutableDictionary dictionary];
    
    if (timeLastGoodRequest != nil)
    {
        [plistDict setValue:timeLastGoodRequest forKey:@"timeLastGoodRequestKey"];
    }
    
    [plistDict writeToFile:plistPath atomically:YES];
}

-(void) loadLastGoodRequest
{
    NSString *rootPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"LastGoodRequest.plist"];
    
	NSMutableDictionary *plistDict = [[NSMutableDictionary alloc] init];
	plistDict = [plistDict initWithContentsOfFile:plistPath];
    
	if (plistDict != nil)
    {
        NSDate* timeLastGoodRequestValue = plistDict[@"timeLastGoodRequestKey"];
        if (timeLastGoodRequestValue != nil && [timeLastGoodRequestValue isKindOfClass:[NSDate class]])
            self.timeLastGoodRequest = timeLastGoodRequestValue;
    }
    
}


#pragma mark - NetworkConnection plist
-(void) saveNetworkConnectionSettings
{
    NSString *rootPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"NetworkConnectionSettings.plist"];
    NSMutableDictionary *plistDict = [NSMutableDictionary dictionary];
    
    [plistDict setValue:@(self.networkConnectivity) forKey:@"networkConnectivity"];
    
    [plistDict writeToFile:plistPath atomically:YES];
}

-(void) loadNetworkConnectionSettings
{
    NSString *rootPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"NetworkConnectionSettings.plist"];
    
	NSMutableDictionary *plistDict = [[NSMutableDictionary alloc] init];
	plistDict = [plistDict initWithContentsOfFile:plistPath];
    
    // Start with default values
    self.networkConnectivity = YES; // default value

    // If we can find the values in the dictionary, then use them instead of the defaults.
	if (plistDict != nil)
    {
        NSNumber *readConnectivity = plistDict[@"networkConnectivity"];
        if (readConnectivity != nil && [readConnectivity isKindOfClass:[NSNumber class]])
            self.networkConnectivity = [readConnectivity boolValue];
    }
}

#pragma mark -
#pragma mark Utility Methods
+(void)setNavigationBarBaseColor:(UINavigationBar*)navBar
{
    [navBar setTintColor:[UIColor colorWithRed:0.0/255.0 green:44.0/255.0 blue:106.0/255.0 alpha:0.9f]];
}

+(void)setToolBarBaseColor:(UIToolbar*)tBar
{
    [tBar setTintColor:[UIColor colorWithRed:0.0/255.0 green:44.0/255.0 blue:106.0/255.0 alpha:0.9f]];
}

// ver8.0 : Table view's background color uses this value
+(UIColor *)getBaseBackgroundColor
{
	return [UIColor colorWithRed:239.0/255.0 green:239.0/255.0 blue:239.0/255.0 alpha:1.0f];
}

// Ver 9.0.1 : Set the status bar black
// This is deprecated on iOS 7
+(void)setStatusBarBlack
{
    if (([ExSystem is7Plus])) {
        // some xib is setting the status bar to black, which is deprecated iOS 7.
        // revert status bar back to default
        [[UIApplication sharedApplication] setStatusBarStyle: UIStatusBarStyleDefault];
    } else {
        [[UIApplication sharedApplication] setStatusBarStyle: UIStatusBarStyleBlackOpaque];
    }
}

#pragma mark - Temporary code to store home and work addresses
static NSMutableDictionary* addressMap = nil;

-(void) setAddress:(NSString*) value forKey:(NSString*) key
{
    addressMap[key] = value;
}

//The address will either be null, a country only, or a country + other fields
//If it only contains a country, return an empty string.
-(NSString*)getHomeAddress
{
    NSMutableString* addr = [NSMutableString string];
    int count = 0;
    count += [self extendAddress:addr withField:addressMap[@"homeStreet"]];
    count += [self extendAddress:addr withField:addressMap[@"homeCity"]];
    count += [self extendAddress:addr withField:addressMap[@"homeState"]];
    count += [self extendAddress:addr withField:addressMap[@"homeCountry"]];
    count += [self extendAddress:addr withField:addressMap[@"homeZipCode"]];
    if (count < 2)
        return @"";
    return addr;
}

//The address will either be null, a country only, or a country + other fields
//If it only contains a country, return an empty string.
-(NSString*)getWorkAddress
{
    NSMutableString* addr = [NSMutableString string];
    int count = 0;
    count += [self extendAddress:addr withField:addressMap[@"workStreet"]];
    count += [self extendAddress:addr withField:addressMap[@"workCity"]];
    count += [self extendAddress:addr withField:addressMap[@"workState"]];
    count += [self extendAddress:addr withField:addressMap[@"workCountry"]];
    count += [self extendAddress:addr withField:addressMap[@"workZipCode"]];
    if (count < 2)
        return @"";
    return addr;
}

-(int)extendAddress:(NSMutableString*) str withField:(NSString*)field
{
    if ([field length] > 0)
    {
        if ([str length] > 0)
            [str appendString:@", "];
        
        [str appendString:field];
        return 1;
    }
    return 0;
}

-(void) resetAddresses
{
    addressMap = [[NSMutableDictionary alloc] init];
}

#pragma mark - Identification utilities

- (NSString *)deviceId {
    if (!_deviceId) {
        // If we don't have device ID cached then try to get it from
        // user defaults.
        //
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        
        _deviceId = [defaults stringForKey:@"device_id"];

        if (!_deviceId) {
            // Nothing in user defaults. Create & persist a new device ID.
            //
            CFUUIDRef uuidRef = CFUUIDCreate(kCFAllocatorDefault);
            _deviceId = (NSString *) CFBridgingRelease(CFUUIDCreateString(NULL, uuidRef));
            CFRelease(uuidRef);
            
            [defaults setObject:_deviceId forKey:@"device_id"];
            [defaults synchronize];
        }
    }
    
    return _deviceId;
}

@end


#pragma clang diagnostic pop