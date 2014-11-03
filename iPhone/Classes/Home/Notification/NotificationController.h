//
//  NotificationController.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NotificationHandler.h"
#import "NotificationEvent.h"

@interface NotificationController : NSObject
{
    NSMutableDictionary             *eventHandlerMap; // Map between event-type to handler class
    NotificationEvent               *pendingNotification;    // Current event to be processed.  One at a time.
}

@property (nonatomic, strong) NSMutableDictionary               *eventHandlerMap;
@property (nonatomic, strong) NotificationEvent                 *pendingNotification;

+(NotificationController*) sharedInstance;

-(void) registerHandler:(NotificationHandler*)handler withEventType:(NSString*)eventType;

// Return whether this event has been processed
-(BOOL) processNotificationEvent:(NotificationEvent *)event;

@end
