//
//  LocateAndAlertNotificationHandler.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "LocateAndAlertNotificationHandler.h"
#import "SafetyCheckInVC.h"


@implementation LocateAndAlertNotificationHandler

-(void) processNotificationEvent:(NotificationEvent *)event
{
    bool enableLocateAndAlert = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"LocateAndAlert" withType:@"OTMODULE"]];
    if (enableLocateAndAlert)
    {
        // MOB-7502 check LNA_User role
        enableLocateAndAlert = [[ExSystem sharedInstance] hasRole:ROLE_LNA_USER];
    }
    
    if (enableLocateAndAlert && ![[ExSystem sharedInstance] isBreeze] && ![UIDevice isPad])
    {
        if (![self isTopViewOfClass:[SafetyCheckInVC class]])
        {
            [ConcurMobileAppDelegate unwindToRootView];
            // Go to LNA page
            [self openSafetyCheckIn];
        }
    }

}

- (void)openSafetyCheckIn
{
    if (![ExSystem connectedToNetwork])
    {
 		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Location Check Offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;

    }

    NSDictionary *dictionary = @{@"Action": @"Safety Checkin"};
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];

    SafetyCheckInVC* vc = [[SafetyCheckInVC alloc] initWithNibName:@"EditFormView" bundle:nil];
    [vc setSeedData:nil];
    [[ConcurMobileAppDelegate findHomeVC].navigationController pushViewController:vc animated:YES];
}
@end
