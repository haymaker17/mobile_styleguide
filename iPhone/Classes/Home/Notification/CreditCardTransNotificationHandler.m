//
//  CreditCardTransNotificationHandler.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CreditCardTransNotificationHandler.h"
#import "QuickExpensesReceiptStoreVC.h"

@implementation CreditCardTransNotificationHandler
-(void) processNotificationEvent:(NotificationEvent *)event
{
    ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    UINavigationController *nav = delegate.navController;
 
    if (![UIDevice isPad])
    {
        [ConcurMobileAppDelegate unwindToRootView];
        
        QuickExpensesReceiptStoreVC* qeVc = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        [qeVc setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:NO allowListEdit:YES];
        [nav pushViewController:qeVc animated:YES];
    }
}
@end
