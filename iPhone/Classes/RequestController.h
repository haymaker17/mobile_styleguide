//
//  RequestController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/7/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Msg.h"
#import "MsgHandler.h"
#import "MsgControl.h"
//#import "MobileViewController.h"
#import "MCLogging.h"


@interface RequestController : MsgHandler 
{
	NSString			*ipAddress;
    NSMutableData		*webData;
    NSMutableString		*soapResults;
    NSURLConnection		*conn;
	Msg					*msg;
	MsgControl			*msgControl;
	//NSString            *uuid;
    bool                 inprocessing;
	NSDate				*timeOfRequest;
}

@property (strong, nonatomic) NSString		*ipAddress;
@property (strong, nonatomic) Msg			*msg;
@property (strong, nonatomic) MsgControl	*msgControl;
//@property (retain, nonatomic) NSString		*uuid;
@property bool inprocessing;
@property (strong, nonatomic) NSDate *timeOfRequest;

-(void) createSOAPHeader:(NSString *)something;
-(void) init:(Msg *)msgToUse MessageControl:(MsgControl *)msgControlToUse;
//-(void) initPostMsg:(Msg *)msgToUse MessageControl:(MsgControl *)msgControlToUse;
-(void) initDirect:(Msg *)msgToUse MVC:(NSObject<ExMsgRespondDelegate> *)mobileVC;
-(void) send;

+(void) retrieveImageFromUrl: (NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc;
+(void) retrieveImageFromUrl: (NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc ParameterBag:(NSMutableDictionary*) pBag;
+(void) retrieveReportPDFImageFromUrlNoCaching:(NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc ParameterBag:(NSMutableDictionary*) pBag;
+(void) retrieveImageFromUrlWithoutCaching:(NSString*)imageUrl MsgId:(NSString*) msgId SessionID:(NSString*) sessionID MVC:(NSObject<ExMsgRespondDelegate>*) mvc ParameterBag:(NSMutableDictionary*) pBag;
-(void) makeRequest:(NSString *)strURL requestType:(NSString *)reqType soapAction:(NSString *) soapAction;
-(void) makeRequest:(NSMutableURLRequest *)request;

-(void) getCurrentSettings;

@end
