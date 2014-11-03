//
//  iPadHome9VC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "RootViewController.h"
#import "ReceiptStoreUploadHelper.h"
#import "UINavigationController+KeyboardDismissal.h"
#import "TransparentViewUnderMoreMenu.h"
#import "LoginDelegate.h"
#import "TripsData.h"
#import "UIDevice+Additions.h"
@interface iPadHome9VC : MobileViewController <NSFetchedResultsControllerDelegate, UIActionSheetDelegate, UploadBannerDelegate, UIAlertViewDelegate, LoginDelegate>
{
    ReceiptStoreUploadHelper                    *rsuHelper;
}

@property (nonatomic, strong) RootViewController        *rootVC;
@property (strong, nonatomic) IBOutlet UILabel          *lblOffline;
@property (strong, nonatomic) IBOutlet UIView			*offlineHeaderView;
@property (strong, nonatomic) IBOutlet UIImageView		*imgHeaderView;
@property (nonatomic, strong) UINavigationController     *signInViewNavigationController;

-(void) doPostLoginInitialization;
-(void) showMessageCenter:(id)sender;
-(void) showManualLoginView;
-(BOOL) refreshView:(UIRefreshControl*) refresh;
-(void) refreshUIWithTripsData: (TripsData *) tripsData;
//for other VC's to post refresh
-(void) forceReload;
-(void) refreshSummaryData;
-(void) refreshTripsData;

-(void) btnQuickExpensePressed:(id)sender;
-(void) btnExpensesPressed:(id)sender;
-(void) btnReportsPressed:(id)sender;
-(void) btnApprovalsPressed:(id)sender;

-(void) tripsButtonPressed;
-(void) btnBookFlightsPressed:(id)sender;
-(void) btnBookCarPressed:(id)sender;
-(void) btnBookHotelPressed:(id)sender;
-(void) btnBookRailPressed:(id)sender;

-(void) showPhotoAlbum;
-(void) bookingsActionPressed:(id)sender;
-(void) cameraPressed:(id) sender;
-(void) btnCarMileagePressed:(id)sender;
-(void) resetBarColors:(UINavigationController *)navcontroller;

-(void) showTestDriveStoryBoard;
-(void) removeTestDriveStoryBoard;
-(void) showPasswordRestScreen;
-(void) showSignInScreen;
// this should be made more generic, as part of a base class controller that will work for iPhone or iPad
// this system to be refactored in 9.3 to better include Amex QE launching
-(void)SwitchToApprovalsView;
-(void)checkOffline;


@end
