//
//  GovHome9VC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileTableViewController.h"
#import "EntityTrip.h"
#import "SummaryData.h"
#import "EntityWarningMessages.h"

@interface GovHome9VC : MobileTableViewController<NSFetchedResultsControllerDelegate, UIActionSheetDelegate, UIAlertViewDelegate, UploadBannerDelegate>
{
    
}

@property (nonatomic, strong) RootViewController            *rootVC;

@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
@property (nonatomic, strong) EntityTrip                    *currentTrip;
@property (nonatomic, strong) SummaryData                   *summaryData;

@property (nonatomic, strong) NSMutableDictionary           *postLoginAttribute;
@property (strong, nonatomic) EntityWarningMessages         *allMessages;
@property (nonatomic, strong) NSMutableDictionary *serverCallCounts;

-(void) doPostLoginInitialization;
-(void) showManualLoginView;
-(BOOL) refreshView:(UIRefreshControl*) refresh;
-(void) forceReload;
-(void) refreshSummaryData;
-(void) refreshTripsData;

-(void) bookingsActionPressed:(id)sender;
-(void) buttonQuickExpensePressed:(id)sender;

@end
