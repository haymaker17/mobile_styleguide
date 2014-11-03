//
//  MsgHandler.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 11/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "MsgHandler.h"
#import "WeakReference.h"
#import "MCLogging.h"

@implementation MsgHandler

@dynamic respondToMvc;
@synthesize cancellationReceived;

static NSString *MVC_CANCELLATION_MESSAGE = @"MVC_CANCELLATION_MESSAGE";

#pragma mark -
#pragma mark Dynamic Methods
-(NSObject<ExMsgRespondDelegate>*) respondToMvc
{
	return respondToMvc;
}

-(void)setRespondToMvc:(NSObject<ExMsgRespondDelegate>*)newValue
{
	NSObject<ExMsgRespondDelegate> *oldValue = respondToMvc;
	
	if (newValue == oldValue)
		return;
	
	if (oldValue != nil)
		[self stopListeningToCancellationNotifications];
	
	respondToMvc = newValue;
	
	if (newValue != nil)
		[self startListeningToCancellationNotifications];
}

-(NSString *)getMsgIdKey
{
	return @"MsgHandler";
}

#pragma mark -
#pragma mark Registration Methods
-(void) startListeningToCancellationNotifications
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter addObserver:self selector:@selector(receivedCancellationNotification:) name:MVC_CANCELLATION_MESSAGE object:nil];
}

-(void) stopListeningToCancellationNotifications
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter removeObserver:self name:MVC_CANCELLATION_MESSAGE object:nil];
}

- (void)receivedCancellationNotification:(NSNotification*)notification
{
	NSDictionary* userInfoDict = notification.userInfo;
	if (userInfoDict != nil)
	{
		WeakReference *weakRefToCancelledMvc = userInfoDict[@"CANCELLED_MVC"];
		if (weakRefToCancelledMvc != nil)
		{
			if (weakRefToCancelledMvc.ref == respondToMvc)
			{
				[[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgHandler::receivedCancellationNotification for message %@", [self getMsgIdKey]] Level:MC_LOG_DEBU];
				self.cancellationReceived = YES;
				self.respondToMvc = nil;  // Stops listening to cancellation notifications as a side effect of setting this property
			}
		}
	}
}

-(void)dealloc
{
	self.respondToMvc = nil;	// Stops listening to cancellation notifications as a side effect of setting this property
}

+(void) cancelAllRequestsForDelegate:(NSObject<ExMsgRespondDelegate>*)mvc
{
	WeakReference *weakRefToCancelledMvc = [WeakReference toObject:mvc];
	NSDictionary *userInfoDict = @{@"CANCELLED_MVC": weakRefToCancelledMvc};
	NSNotification *notif = [NSNotification notificationWithName:MVC_CANCELLATION_MESSAGE object:nil userInfo:userInfoDict];
	[[NSNotificationCenter defaultCenter] postNotification:notif];
}

@end
