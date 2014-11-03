//
//  Authenticate.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "XMLBase.h"
#import "MsgResponder.h"
#import "MsgControl.h"
#import "Msg.h"
#import "UserMessage.h"
#import "ExSiteSetting.h"

@interface Authenticate : MsgResponder
{
	NSURLConnection			*conn;

	NSString				* currentElement, *roles;

	NSString				*path, *userName, *pin, *password, *pinOrPassword, *sessionID, *accessToken, *accessTokenSecret, *authenticated, *entityType, *crnCode, *expenseCtryCode;
	
    NSString                *productOffering; // BRONX, etc.
    NSString                *hasRequiredCustomFields; // true/false
	NSString				*timedOut;
	NSString				*remoteWipe;
	NSString                *needAgreement;
    NSString                *commonResponseCode;
    NSString                *commonResponseSystemMessage;
    NSString                *commonResponseUserMessage;
    
	NSMutableString			*buildString;

	// User Messages
	NSMutableArray			*userMessages;
	UserMessage				*userMsg;
	
	NSString				*disableAutoLogin;
	BOOL					inSiteSetting, isTripItEmailAddressConfirmed, isTripItLinked;
    NSString                *hasTravelRequest;
    NSString                *isRequestApprover;
    NSString                *isRequestUser;
	ExSiteSetting			*curSiteSetting;
    
    NSDate                  *dateCreated;
    NSDate                  *dateResponseReceived;
    NSDate                  *dateResponseParsed;
}

@property BOOL isTripItLinked;
@property BOOL isTripItEmailAddressConfirmed;
@property (nonatomic, strong) NSString *needAgreement;
@property (nonatomic, strong) NSString *commonResponseCode;
@property (nonatomic, strong) NSString *commonResponseSystemMessage;
@property (nonatomic, strong) NSString *commonResponseUserMessage;
@property (nonatomic, strong) NSString *path;
@property (nonatomic, strong) NSString *userName;
@property (nonatomic, strong) NSString *pin;
@property (nonatomic, strong) NSString *password;
@property (nonatomic, strong) NSString *pinOrPassword;
@property (nonatomic, strong) NSString *sessionID;
@property (nonatomic, strong) NSString *accessToken;
@property (nonatomic, strong) NSString *accessTokenSecret;
@property (nonatomic, strong) NSString *authenticated;
@property (nonatomic, strong) NSString *entityType;
@property (nonatomic, strong) NSString *productOffering;

@property (nonatomic, strong) NSString *crnCode;
@property (nonatomic, strong) NSString *expenseCtryCode;

@property (strong, nonatomic) NSString *timedOut;
@property (strong, nonatomic) NSString *roles;
@property (strong, nonatomic) NSString *currentElement;
@property (strong, nonatomic) NSString *remoteWipe;

@property (strong, nonatomic) NSMutableString *buildString;
@property (strong, nonatomic) NSMutableArray *userMessages;
@property (strong, nonatomic) UserMessage *userMsg;
@property (strong, nonatomic) NSString *disableAutoLogin;
@property (strong, nonatomic) NSString *userId;
@property BOOL inSiteSetting;
@property (strong, nonatomic) ExSiteSetting *curSiteSetting;

@property (strong, nonatomic) NSDate    *dateCreated;
@property (strong, nonatomic) NSDate    *dateResponseReceived;
@property (strong, nonatomic) NSDate    *dateResponseParsed;
@property (strong, nonatomic) NSDate    *accountExpirationDate;
@property BOOL    *isAccountExpired;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;

-(NSString *)makeXMLBody;
-(NSString *)makeXMLBodyCorpSSO:(NSString *)sessionKey;
@end
