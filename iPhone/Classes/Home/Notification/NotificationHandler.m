//
//  NotificationHandler.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NotificationHandler.h"
#import "ConcurMobileAppDelegate.h"

@implementation NotificationHandler

-(void) processNotificationEvent:(NotificationEvent *)event
{
}

-(BOOL) requiresValidSession
{
    return TRUE;
}

- (BOOL) isTopViewOfClass:(Class) theClass
{
    NSMutableArray* allViews = [[NSMutableArray alloc] init];
    [ConcurMobileAppDelegate addViewControllersToUnwindToArray:allViews];
    MobileViewController *lnaVc = nil;
    if ([allViews count] > 0)
    {
        lnaVc = allViews[([allViews count] -1)];
    }
    return [lnaVc isKindOfClass:theClass];
}


@end
