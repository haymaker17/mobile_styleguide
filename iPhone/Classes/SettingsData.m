//
//  SettingsData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SettingsData.h"
#import "DataConstants.h"
#import "FileManager.h"
#import "DataExtender.h"
#import "RootViewcontroller.h"

@implementation SettingsData


@synthesize topViewName;

@synthesize rememberUser;
@synthesize autoLogin;
@synthesize serverAddress;
@synthesize debug;
@synthesize uri;
@synthesize uriNonSSL;
@synthesize saveUserName;
@synthesize crnCode;
@synthesize requestAppRating;
@synthesize roleDict, imageCount, authentLog, currencies;
@synthesize lastViewName, lastViewKey, lastViewDataName, lastSession;
@synthesize productLine, lastRail, lastHotel, lastCar, formerSmartExpenseCctKeys, formerSmartExpensePctKeys;
@synthesize timeOut;
@synthesize currentVersion;
@synthesize previousVersion;
@synthesize showTwitter, twitterUrl, showIAd;
@synthesize lastName, firstName, mi, companyName, email, expenseCtryCode, showPanelHome, showWhatsNew;
@synthesize disableAutoLogin,doReceiptMigrate;
@synthesize backUpReceiptFilePath,receiptsPath;

#define kLAST_SESSION @"LAST_SESSION"

NSString * defaultTwitterUrl = @"http://api.twitter.com/1/statuses/user_timeline.xml?screen_name=Concur2Go";

static SettingsData* sharedInstance = nil;

+(SettingsData*)getInstance
{
	if (!sharedInstance)
	{
		@synchronized(self)
		{
			if (!sharedInstance)
			{
				sharedInstance = [[SettingsData alloc] init];
				[sharedInstance init]; 
				//sharedInstance.sessionID = nil;// The stored session is never available on start.
			}
		}
	}
	
	return sharedInstance;
}








-(id)init
{
	[self readPlist];
	return self;
}

-(void) initPlistFiles
{
}

//-(void)setSessionId:(NSString *)thisSessionID
//{
//	self.sessionID = thisSessionID;
//	[self writeToPlist];
//}


- (void)readPlist
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = [paths objectAtIndex:0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"MobileSettings.plist"];
	NSMutableDictionary* plistDict = [[NSMutableDictionary alloc] initWithContentsOfFile:path];

	if (plistDict != nil)
	{
		if([plistDict objectForKey:@"SHOW_PANEL_HOME"] != nil)
		{
			NSString *showIt = [plistDict objectForKey:@"SHOW_PANEL_HOME"];
			if([showIt isEqualToString:@"Y"])
				self.showPanelHome = YES;
			else 
				self.showPanelHome = NO;
		}
		else 
			self.showPanelHome = NO;
		self.topViewName = [plistDict objectForKey:@"VIEW"];

		self.autoLogin = [plistDict objectForKey:@"AUTO_LOGIN"];
		self.disableAutoLogin = [plistDict objectForKey:@"DISABLE_AUTO_LOGIN"];

		self.rememberUser = [plistDict objectForKey:@"REMEMBER_USER"];
		self.serverAddress = [plistDict objectForKey:@"SERVER_ADDRESS"];
		self.debug = [plistDict objectForKey:@"DEBUG"];

		self.uri = [plistDict objectForKey:@"URI"];
		self.uriNonSSL = [plistDict objectForKey:@"URI_NONSSL"];
		self.saveUserName = [plistDict objectForKey:@"SAVE_USER_NAME"];



		self.crnCode = [plistDict objectForKey:@"CRN_CODE"];
		if (self.crnCode == nil)
			self.crnCode = @"USD";
		self.requestAppRating = [plistDict objectForKey:@"REQUEST_APP_RATING"];
		NSNumber *ic = [plistDict objectForKey:@"IMAGE_COUNT"];
		self.imageCount = [ic intValue];
		self.authentLog = [plistDict objectForKey:@"AUTHENT_LOG"];
		if ([plistDict objectForKey:@"AUTHENT_LOG"] == nil) 
			self.authentLog = [[NSMutableArray alloc] initWithObjects:nil];
		
		if (topViewName == nil)
			[self setTopViewName:@"LOGIN"];
		
		if (serverAddress == nil)
			[self setServerAddress:@"172.16.230.131"];
		
		if (uri == nil)
			[self setUri:@"https://concursolutions.com"];
		
		if (uriNonSSL == nil)
			[self setUriNonSSL:@"https://concursolutions.com"];
		
		self.currencies = [plistDict objectForKey:@"CURRENCIES"];
		
		self.lastViewName = [plistDict objectForKey:@"LAST_VIEW_NAME"];
		self.lastViewDataName = [plistDict objectForKey:@"LAST_VIEW_DATA_NAME"];
		self.lastViewKey = [plistDict objectForKey:@"LAST_VIEW_KEY"];
		self.lastSession = [plistDict objectForKey:kLAST_SESSION];
		
		if (lastSession == nil) 
			self.lastSession = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		
		self.productLine = [plistDict objectForKey:@"PRODUCT_LINE"];
		NSString* strTO = [plistDict objectForKey:@"TIME_OUT"];
		self.timeOut = strTO == nil? 120: [strTO intValue];

		self.lastRail = [plistDict objectForKey:@"LAST_RAIL"];
		if (lastRail == nil) {
			self.lastRail = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		}
		
		self.lastHotel = [plistDict objectForKey:@"LAST_HOTEL"];
		if (lastHotel == nil)
			self.lastHotel = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

		self.lastCar = [plistDict objectForKey:@"LAST_CAR"];
		if (lastCar == nil)
			self.lastCar = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		
		self.formerSmartExpenseCctKeys =  [plistDict objectForKey:@"FORMER_SMART_EXPENSE_CCT_KEYS"];
		if (formerSmartExpenseCctKeys == nil)
			self.formerSmartExpenseCctKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

		self.formerSmartExpensePctKeys =  [plistDict objectForKey:@"FORMER_SMART_EXPENSE_PCT_KEYS"];
		if (formerSmartExpensePctKeys == nil)
			self.formerSmartExpensePctKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		
		self.currentVersion = [plistDict objectForKey:@"VERSION"];
		self.previousVersion = currentVersion;
		
		NSString *ver = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
		if (currentVersion == nil || ![ver isEqualToString:currentVersion]) 
		{
			self.currentVersion = ver;
			[self checkReceiptMigration];            
			self.showWhatsNew = YES;
			[FileManager cleanCache];
		}
		
		self.showTwitter = [plistDict objectForKey:@"SHOW_TWITTER"];
		if (showTwitter == nil)
			self.showTwitter = @"YES";
		
		self.twitterUrl = [plistDict objectForKey:@"TWITTER_URL"];
		if (twitterUrl == nil)
			self.twitterUrl = defaultTwitterUrl;
	}
	else 
	{
		[self setTopViewName:@"LOGIN"];
		plistDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"LOGIN", @"VIEW", @"", @"SESSION_ID", 
					 @"YES", @"AUTO_LOGIN", 
					 @"NO", @"REMEMBER_USER", @"172.16.230.131", @"SERVER_ADDRESS", @"YES", @"DEBUG", @"https://concursolutions.com", @"URI",
					 @"https://concursolutions.com", @"URI_NONSSL", @"YES", @"REQUEST_APP_RATING", 
					 @"YES", @"SAVE_USER_NAME", @"USD", @"CRN_CODE", @"0.0.0.0", @"VERSION", @"N", @"SHOW_PANEL_HOME", nil];
		
		if([plistDict objectForKey:@"SHOW_PANEL_HOME"] != nil)
		{
			NSString *showIt = [plistDict objectForKey:@"SHOW_PANEL_HOME"];
			if([showIt isEqualToString:@"Y"])
				self.showPanelHome = YES;
			else 
				self.showPanelHome = NO;
		}
		else 
			self.showPanelHome = NO;
		self.topViewName = [plistDict objectForKey:@"VIEW"];

		self.autoLogin = [plistDict objectForKey:@"AUTO_LOGIN"];
		self.disableAutoLogin = [plistDict objectForKey:@"DISABLE_AUTO_LOGIN"];



		self.rememberUser = [plistDict objectForKey:@"REMEMBER_USER"];
		self.serverAddress = [plistDict objectForKey:@"SERVER_ADDRESS"];
		self.debug = [plistDict objectForKey:@"DEBUG"];

		self.uriNonSSL = [plistDict objectForKey:@"URI_NONSSL"];
		self.saveUserName = [plistDict objectForKey:@"SAVE_USER_NAME"];
		self.crnCode = [plistDict objectForKey:@"CRN_CODE"];

		if(self.roleDict == nil)
			self.roleDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];

		
		self.requestAppRating = [plistDict objectForKey:@"REQUEST_APP_RATING"];
		self.authentLog = [[NSMutableArray alloc] initWithObjects:nil];
		
		if (topViewName == nil)
			[self setTopViewName:@"LOGIN"];
		
		if (serverAddress == nil)
			[self setServerAddress:@"172.16.230.131"];
		
		if (uri == nil)
			[self setUri:@"https://concursolutions.com"];
		
		self.imageCount = 0;
		
		self.lastSession = [plistDict objectForKey:kLAST_SESSION];
		if (lastSession == nil) 
			self.lastSession = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		
		self.lastRail = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		self.lastHotel = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		self.lastCar = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		self.formerSmartExpenseCctKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		self.formerSmartExpensePctKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		
		self.currentVersion = [plistDict objectForKey:@"VERSION"];
		if (currentVersion == nil)
			self.currentVersion = @"0.0.0.0";
		
		self.showTwitter = @"YES";
		self.twitterUrl = defaultTwitterUrl;
	}
	
	self.debug = @"NO";
	[plistDict release];
	
}




- (void)writeToPlist
{	
	//NSUserDefaults *prefs = [NSUserDefaults standardUserDefaults];
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = [paths objectAtIndex:0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"MobileSettings.plist"];
	NSMutableDictionary*plistDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"N", @"SHOW_PANEL_HOME", @"LOGIN", @"VIEW", @"", @"SESSION_ID"
									 , @"NO", @"AUTO_LOGIN", @"NO", @"REMEMBER_USER", @"172.16.230.131", @"SERVER_ADDRESS"
									 , @"YES", @"DEBUG", @"https://concursolutions.com", @"URI"
									 ,@"YES", @"SAVE_USER_NAME", @"USD", @"CRN_CODE", [NSNumber numberWithInt:0], @"IMAGE_COUNT", @"", @"CURRENCIES"
									 ,@"", @"LAST_VIEW_NAME", @"", @"LAST_VIEW_DATA_NAME", @"", @"LAST_VIEW_KEY"
									 ,@"YES", @"REQUEST_APP_RATING", lastRail, @"LAST_RAIL", lastHotel, @"LAST_HOTEL", lastCar, @"LAST_CAR"
									 , formerSmartExpenseCctKeys, @"FORMER_SMART_EXPENSE_CCT_KEYS", formerSmartExpensePctKeys, @"FORMER_SMART_EXPENSE_PCT_KEYS"
									 , currentVersion, @"VERSION"
									 , authentLog, @"AUTHENT_LOG"
									 , nil];

	if(showPanelHome)
		[plistDict setObject:@"Y" forKey:@"SHOW_PANEL_HOME"];
	else 
		[plistDict setObject:@"N" forKey:@"SHOW_PANEL_HOME"];
	
	[plistDict setValue:@"HOME_PAGE" forKey:@"VIEW"];
	


	[plistDict setValue:rememberUser forKey:@"REMEMBER_USER"];


	[plistDict setValue:autoLogin forKey:@"AUTO_LOGIN"];
	
	if (disableAutoLogin != nil)
		[plistDict setValue:disableAutoLogin forKey:@"DISABLE_AUTO_LOGIN"];
	
	[plistDict setValue:serverAddress forKey:@"SERVER_ADDRESS"];
	[plistDict setValue:debug forKey:@"DEBUG"];
	[plistDict setValue:uri forKey:@"URI"];
	[plistDict setValue:uriNonSSL forKey:@"URI_NONSSL"];
	[plistDict setValue:saveUserName forKey:@"SAVE_USER_NAME"];
	[plistDict setValue:crnCode forKey:@"CRN_CODE"];
	[plistDict setValue:[NSNumber numberWithInt:imageCount] forKey:@"IMAGE_COUNT"];
	[plistDict setValue:requestAppRating forKey:@"REQUEST_APP_RATING"];
	[plistDict setValue:showTwitter forKey:@"SHOW_TWITTER"];
	[plistDict setValue:twitterUrl forKey:@"TWITTER_URL"];


	
	int aCount = [authentLog count];
	if (aCount > 20) 
	{
		int numToKill = aCount - 21;
		for (int x = numToKill; x > -1; x--) 
		{
			[authentLog removeObjectAtIndex:x];
		}
	}
	
	if(lastSession != nil)
	{
		//NSString *s = [lastSession objectForKey:@"SESSION_ID"];
		//NSLog(@"SID = %@", s);
		[plistDict setObject:lastSession forKey:kLAST_SESSION];
	}
	
	if(lastRail != nil)
		[plistDict setObject:lastRail forKey:@"LAST_RAIL"];

	if(lastHotel != nil)
		[plistDict setObject:lastHotel forKey:@"LAST_HOTEL"];

	if(lastCar != nil)
		[plistDict setObject:lastCar forKey:@"LAST_CAR"];
	
	if (formerSmartExpenseCctKeys != nil)
		[plistDict setObject:formerSmartExpenseCctKeys forKey:@"FORMER_SMART_EXPENSE_CCT_KEYS"];
	
	if (formerSmartExpensePctKeys != nil)
		[plistDict setObject:formerSmartExpensePctKeys forKey:@"FORMER_SMART_EXPENSE_PCT_KEYS"];
	
	if(lastViewName != nil)
		[plistDict setValue:lastViewName forKey:@"LAST_VIEW_NAME"];
	
	if(lastViewDataName != nil)
		[plistDict setValue:lastViewDataName forKey:@"LAST_VIEW_DATA_NAME"];
	
	if(lastViewKey != nil)
		[plistDict setValue:lastViewKey forKey:@"LAST_VIEW_KEY"];
	
	[plistDict setValue:authentLog forKey:@"AUTHENT_LOG"];
	[plistDict setValue:productLine forKey:@"PRODUCT_LINE"];
	[plistDict setValue:[NSString stringWithFormat:@"%d", timeOut] forKey:@"TIME_OUT"];
	
	self.currentVersion = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
	[plistDict setObject:currentVersion forKey:@"VERSION"];
	
	[plistDict writeToFile:path atomically: YES];
	[plistDict release];
}

-(void)dealloc
{

	[currentVersion release];

	[topViewName release];

	[rememberUser release];
	[autoLogin release];
	[serverAddress release];
	[debug release];
	[uri release];
	[uriNonSSL release];
	[saveUserName release];

	[crnCode release];
	[requestAppRating release];
	[roleDict release];
	[authentLog release];
	[currencies release];
	
	[lastViewName release];
	[lastViewKey release];
	[lastViewDataName release];
	[lastSession release];
	[previousVersion release];
	[productLine release];
	
	[lastRail release];
	[lastHotel release];
	[lastCar release];
	[formerSmartExpenseCctKeys release];
	[formerSmartExpensePctKeys release];
	[showTwitter release];
	[twitterUrl release];
	[showIAd release];
	
	[lastName release];
	[firstName release];
	[mi release];
	[companyName release];
	[email release];
	[expenseCtryCode release];
	
	[disableAutoLogin release];

    [backUpReceiptFilePath release];
    [receiptsPath release];
	[super dealloc];
}


-(void) initKeychain
{

}







@end
