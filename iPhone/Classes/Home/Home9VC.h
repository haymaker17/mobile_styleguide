//
//  Home9VC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 2/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileTableViewController.h"
#import "HomeCell.h"
#import "HomeManager.h"
#import "RootViewController.h"
#import "ReceiptStoreUploadHelper.h"
#import "HomeDataProviderDelegate.h"
#import "LoginDelegate.h"

@interface Home9VC : MobileTableViewController <UIActionSheetDelegate, UploadBannerDelegate, UIAlertViewDelegate, HomeDataProviderDelegate, LoginDelegate >
{
    ReceiptStoreUploadHelper                    *rsuHelper;
}

@property (nonatomic, strong) RootViewController        *rootVC;
@property (nonatomic, strong) UINavigationController     *signInViewNavigationController;
// This is a temporary placeholder for carRates data.
@property (strong, nonatomic) CarRatesData                  *carRatesData;

-(void) doPostLoginInitialization;
-(void) showMoreMenu:(id)sender;
-(void) showMessageCenter:(id)sender;
-(void) showManualLoginView;
-(BOOL) refreshView:(UIRefreshControl*) refresh;
-(void) forceReload;
-(void) refreshSummaryData;
-(void) refreshTripsData;


// this should be made more generic, as part of a base class controller that will work for iPhone or iPad
// this system to be refactored in 9.3 to better include Amex QE launching
-(void)SwitchToApprovalsView;

-(void) buttonQuickExpensePressed:(id)sender;
-(void) buttonRequestPressed:(id)sender;
-(void) cameraPressed:(id)sender;
-(void) bookingsActionPressed:(id)sender;
-(void) btnCarMileagePressed:(id)sender;

-(void) removeTestDriveStoryBoard;
-(void) showTestDriveStoryBoard;
-(void) showPasswordRestScreen;
-(void) showSignInScreen;

// methods HomeDataProvider needs to update the UI
- (void)setExpensesCount:(NSNumber *)count;
- (void)setExpenseReportsCount:(NSNumber *)count;
- (void)setApprovalCount:(NSNumber *)count;
-(void)onLogout;
@end

