//
//  NotificationController.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NotificationController.h"
#import "ApplicationLock.h"

#if defined(CORP) && !defined(IGNITE)
#import "ApprovalsNotificationHandler.h"
#import "CreditCardAuthNotificationHandler.h"
#import "CreditCardTransNotificationHandler.h"
#import "GoGoNotificationHandler.h"
#import "LocateAndAlertNotificationHandler.h"
#endif

@interface NotificationController (Private)
-(void)registerAllHandlers;
@end

@implementation NotificationController
@synthesize eventHandlerMap;
@synthesize pendingNotification;

static NotificationController* sharedInstance;

+(NotificationController*) sharedInstance
{
    if (sharedInstance == nil)
	{
		@synchronized (self)
		{
			if (sharedInstance == nil)
			{
				sharedInstance = [[NotificationController alloc] init];
			}
		}
	}
	return sharedInstance;
}

-(id) init
{
    self = [super init];
	if (self)
    {
        self.eventHandlerMap = [[NSMutableDictionary alloc] init];
        [self registerAllHandlers];
	}
	return self;

}

-(void) registerHandler:(NotificationHandler*)handler withEventType:(NSString*)eventType
{
    [eventHandlerMap setObject:handler forKey:eventType];
}

-(void) registerAllHandlers
{
#if defined(CORP) && !defined(IGNITE)
    [self registerHandler:[[ApprovalsNotificationHandler alloc] init]
            withEventType:PUSH_NOTIFICATION_TYPE_EXP_RPT_APPR];
    
    [self registerHandler:[[CreditCardAuthNotificationHandler alloc] init]
            withEventType:PUSH_NOTIFICATION_TYPE_EXP_CCT_AUTH];
    
    [self registerHandler:[[CreditCardTransNotificationHandler alloc] init]
            withEventType:PUSH_NOTIFICATION_TYPE_EXP_CCT_TRXN];
    
    [self registerHandler:[[LocateAndAlertNotificationHandler alloc] init]
            withEventType:PUSH_NOTIFICATION_TYPE_LNA];
    
    [self registerHandler:[[ApprovalsNotificationHandler alloc] init]
            withEventType:PUSH_NOTIFICATION_TYPE_TRV_TRP_APPR];
    
    [self registerHandler:[[GoGoNotificationHandler alloc] init]
            withEventType:PUSH_NOTIFICATION_TYPE_IPM_GOGO];
#endif
}

-(BOOL) processNotificationEvent:(NotificationEvent *)event
{
    // Only one event can be processed at a time
    if (event != nil)
        self.pendingNotification = event;
    else
        event = self.pendingNotification;
    
    if (event == nil)
    {
        // No event to handle, return TRUE as done.
        return TRUE;
    }
    
    NotificationHandler * handler = (NotificationHandler*) [self.eventHandlerMap objectForKey:event.type];
    if (handler != nil)
    {
        if (![[ApplicationLock sharedInstance] isLoggedIn] && [handler requiresValidSession])
        {
            // Event not handled, but queue for processing after login.
            return FALSE;
        }
        else
        {
            [handler processNotificationEvent:event];
            self.pendingNotification = nil;
            return TRUE;
        }
    }
    else
    {
        // No handler found, finished processing the event.
        self.pendingNotification = nil;
        return TRUE;
    }
}

@end
