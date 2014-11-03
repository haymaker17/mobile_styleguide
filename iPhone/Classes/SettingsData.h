//
//  SettingsData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "ExSiteSetting.h"

@interface SettingsData : NSObject 
{
	NSString		*topViewName, *rememberUser, *autoLogin, *serverAddress, *debug, *uri, *uriNonSSL, *saveUserName, *roles, *crnCode, *requestAppRating;
	NSString		*showTwitter, *twitterUrl, *showIAd;
	NSString		*lastViewName, *lastViewKey, *lastViewDataName, *currentVersion, *previousVersion;
	NSMutableDictionary	*roleDict, *currencies, *lastSession, *lastRail, *lastHotel, *lastCar, *formerSmartExpenseCctKeys, *formerSmartExpensePctKeys;

	int				imageCount;
	NSMutableArray	*authentLog;
	BOOL			showPanelHome;
	NSString		*productLine;
	int				timeOut;
	NSString		*disableAutoLogin;
	// Current user info - do not persist
	NSString		*lastName, *firstName, *mi, *companyName, *email, *expenseCtryCode;

	BOOL			showWhatsNew;
    
    // Receipt migration code for version 7.3
	BOOL			doReceiptMigrate;
    NSString        *backUpReceiptFilePath;
    NSString        *receiptsPath;
}
@property BOOL showPanelHome;
@property BOOL showWhatsNew;


@property (retain, nonatomic) NSString *topViewName;

@property (retain, nonatomic) NSString *rememberUser;
@property (retain, nonatomic) NSString *autoLogin;
@property (retain, nonatomic) NSString *saveUserName;
@property (retain, nonatomic) NSString *serverAddress;
@property (retain, nonatomic) NSString *debug;
@property (retain, nonatomic) NSString *uri;
@property (retain, nonatomic) NSString *uriNonSSL;

@property (retain, nonatomic) NSString *crnCode;
@property (retain, nonatomic) NSString *requestAppRating;
@property (retain, nonatomic) NSMutableDictionary	*roleDict;

@property (retain, nonatomic) NSMutableDictionary	*currencies;
@property (retain, nonatomic) NSMutableDictionary	*lastSession;
@property (retain, nonatomic) NSMutableDictionary	*lastRail;
@property (retain, nonatomic) NSMutableDictionary	*lastHotel;
@property (retain, nonatomic) NSMutableDictionary	*lastCar;
@property (retain, nonatomic) NSMutableDictionary	*formerSmartExpenseCctKeys;
@property (retain, nonatomic) NSMutableDictionary	*formerSmartExpensePctKeys;
@property (retain, nonatomic) NSMutableArray		*authentLog;
@property (retain, nonatomic) NSString				*currentVersion;
@property (retain, nonatomic) NSString				*previousVersion;

@property (retain, nonatomic) NSString		*lastViewName;
@property (retain, nonatomic) NSString		*lastViewKey;
@property (retain, nonatomic) NSString		*lastViewDataName;
@property (retain, nonatomic) NSString		*productLine;

@property (retain, nonatomic) NSString *showTwitter;
@property (retain, nonatomic) NSString *twitterUrl;
@property (retain, nonatomic) NSString *showIAd;

@property (retain, nonatomic) NSString *lastName;
@property (retain, nonatomic) NSString *firstName;
@property (retain, nonatomic) NSString *mi;
@property (retain, nonatomic) NSString *companyName;
@property (retain, nonatomic) NSString *email;
@property (retain, nonatomic) NSString *expenseCtryCode;

@property (retain, nonatomic) NSString *disableAutoLogin;

@property BOOL doReceiptMigrate;
@property (nonatomic,retain) NSString        *backUpReceiptFilePath;
@property (nonatomic,retain) NSString        *receiptsPath;

@property int imageCount;
@property int timeOut;

+(SettingsData*)getInstance;

-(void)readPlist;
-(void)writeToPlist;
-(void)initPlistFiles;
-(void)setSessionId:(NSString *)thisSessionID;
-(id)init;
-(void)setMyTopViewName:(NSString *)mary;


-(BOOL) isBreeze;

-(void) initKeychain;

-(BOOL) isQADevOrProdConcurCompany;
-(BOOL) enableLimitedNewFeature;
-(BOOL) enableAttendeeEditing;
-(BOOL) enableAttendeeEditingInMobileExpense;




-(BOOL) needsReceiptMigration;
-(void) checkReceiptMigration;

@end
