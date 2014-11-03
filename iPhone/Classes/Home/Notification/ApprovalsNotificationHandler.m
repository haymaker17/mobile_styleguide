//
//  ReportApprovalNotificationHandler.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//
//  Renamed to ApprovalsNotificationHandler by Deepanshu Jain on 7/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ApprovalsNotificationHandler.h"
#import "ConcurMobileAppDelegate.h"

@implementation ApprovalsNotificationHandler

-(void) processNotificationEvent:(NotificationEvent *)event
{
    [ConcurMobileAppDelegate unwindToRootView];
    
    if ([UIDevice isPad])
    {
        iPadHome9VC *homeViewController = (iPadHome9VC*)[ConcurMobileAppDelegate findHomeVC];
        [homeViewController SwitchToApprovalsView];
    }
    else    // is iPhone
    {
        Home9VC *homeViewController = (Home9VC*)[ConcurMobileAppDelegate findHomeVC];
        [homeViewController SwitchToApprovalsView];
    }

}

@end
