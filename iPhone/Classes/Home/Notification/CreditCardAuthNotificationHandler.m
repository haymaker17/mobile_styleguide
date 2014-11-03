//
//  CreditCardAuthNotificationHandler.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CreditCardAuthNotificationHandler.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "iPadHome9VC.h"

@implementation CreditCardAuthNotificationHandler

-(void) processNotificationEvent:(NotificationEvent *)event
{
    // We are logged in
    [ConcurMobileAppDelegate unwindToRootView];
    ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    UINavigationController *nav = delegate.navController;
    
    if ([UIDevice isPad])
    {
        QuickExpensesReceiptStoreVC *nextController = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        nextController.requireRefresh = YES;
        nextController.currentAuthRefNo = [event.data objectForKey:@"AuthTrxId"];
        [nextController setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
        
        UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:nextController];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];
        iPadHome9VC * homeVc = (iPadHome9VC*) [ConcurMobileAppDelegate findHomeVC];
        //        [homeVc ResetBarColors:localNavigationController];
        [homeVc presentViewController:localNavigationController animated:YES completion:nil];
    }
    else    // is iPhone
    {
        
        QuickExpensesReceiptStoreVC* qeVc = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        qeVc.requireRefresh = YES;
        qeVc.currentAuthRefNo = [event.data objectForKey:@"AuthTrxId"];
        [qeVc setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
        [nav pushViewController:qeVc animated:YES];
    }
}

@end
