//
//  MsgHandler.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExMsgRespondDelegate.h"
//#import "ExSystem.h"
@class ExSystem;

@interface MsgHandler : NSObject
{
	NSObject<ExMsgRespondDelegate>	*respondToMvc;
	BOOL							cancellationReceived;
}

@property (nonatomic, weak) NSObject<ExMsgRespondDelegate> *respondToMvc;
@property BOOL cancellationReceived;

-(void) startListeningToCancellationNotifications;
-(void) stopListeningToCancellationNotifications;
-(void) receivedCancellationNotification:(NSNotification*)notification;
+(void) cancelAllRequestsForDelegate:(NSObject<ExMsgRespondDelegate>*)mvc;
-(NSString *)getMsgIdKey;

@end
