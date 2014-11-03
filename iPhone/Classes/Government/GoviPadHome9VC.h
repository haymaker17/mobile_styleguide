//
//  GoviPadHome9VC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 4/9/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ReceiptStoreUploadHelper.h"
#import "TripsData.h"
#import "LoginDelegate.h"

@interface GoviPadHome9VC : MobileViewController <NSFetchedResultsControllerDelegate, UIActionSheetDelegate, UploadBannerDelegate, UIAlertViewDelegate, LoginDelegate>
{
    TripsData *tripsData;
    NSMutableDictionary         *postLoginAttribute;
    UIBarButtonItem             *btnBookTravel;
}

@property (nonatomic, strong) RootViewController        *rootVC;
@property (strong, nonatomic) IBOutlet UILabel          *lblOffline;
@property (strong, nonatomic) IBOutlet UIView			*offlineHeaderView;
@property (strong, nonatomic) IBOutlet UIImageView		*imgHeaderView;
@property (strong, nonatomic) TripsData *tripsData;
@property (nonatomic, strong) NSMutableDictionary               *postLoginAttribute;
@property (nonatomic, strong) UIBarButtonItem           *btnBookTravel;

-(void) doPostLoginInitialization;
-(void) showMoreMenu:(id)sender;

-(void) showManualLoginView;
-(BOOL) refreshView:(UIRefreshControl*) refresh;
-(void) refreshUIWithTripsData: (TripsData *) tripsData;
//for other VC's to post refresh
-(void) forceReload;
-(void) refreshSummaryData;
-(void) refreshTripsData;

-(IBAction)btnBookTravelPressed:(id)sender;
-(IBAction)btnQEpressed:(id)sender;
-(IBAction)btnBookFlightsPressed:(id)sender;
-(IBAction)btnBookHotelPressed:(id)sender;
-(IBAction)btnBookCarPressed:(id)sender;
-(IBAction)btnBookRailPressed:(id)sender;
-(IBAction)tripsButtonPressed:(id)sender;
-(IBAction)btnExpensesPressed:(id)sender;
-(IBAction)btnAuthorizationsPressed:(id)sender;
-(IBAction)btnVouchersPressed:(id)sender;
-(IBAction)btnStampDocumentsPressed:(id)sender;

@end
