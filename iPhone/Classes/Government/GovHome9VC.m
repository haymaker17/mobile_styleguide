//
//  GovHome9VC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovHome9VC.h"
#import "ApplicationLock.h"
#import "UploadQueue.h"
#import "TripsViewController.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "ReportApprovalListViewController.h"
#import "GovSelectTANumVC.h"
#import "ExpenseTypesManager.h"
#import "UploadQueueViewController.h"
#import "SettingsViewController.h"
#import "GovDocumentListVC.h"
#import "GovDocumentManager.h"
#import "GovUnappliedExpensesVC.h"
#import "GovExpenseEditViewController.h"
#import "GovLoginNoticeVC.h"
#import "SettingsButton.h"
#import "DataConstants.h"

#import "Flurry.h"
#import "Localizer.h"
#import "ViewConstants.h"
#import "NSString+Additions.h"
#import "NSStringAdditions.h"

#define BOOKINGS_ACTIONSHEET_TAG 101
#define PRIVACY_ACT_ALERT_ACTION 113

@implementation GovHome9VC

@synthesize fetchedResultsController=__fetchedResultsController;
@synthesize managedObjectContext=__managedObjectContext;
@synthesize currentTrip, rootVC;
@synthesize allMessages;
@synthesize postLoginAttribute;

#pragma mark -
#pragma mark Init Methods
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // respond to total of 3 servercalls in the class
        self.serverCallCounts = [[NSMutableDictionary alloc] initWithCapacity:3];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [ExSystem sharedInstance].sys.topViewName = self.getViewIDKey;
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.managedObjectContext = [ad managedObjectContext];
    [self refetchData];
    //Gov does NOT display what's new. Only use the check to detect the first time install.
    if([[ExSystem sharedInstance].sys.showWhatsNew boolValue])
    {
        [self clearHomeData];
    }

    self.postLoginAttribute = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
    if (![BaseManager hasEntriesForEntityName:@"EntityWarningMessages" withContext:[ad managedObjectContext]])
    {
        [[ExSystem sharedInstance].msgControl createMsg:GOV_WARNING_MSG CacheOnly:@"NO" ParameterBag:nil SkipCache:NO RespondTo:self];
        [self.serverCallCounts setObject:GOV_WARNING_MSG forKey:GOV_WARNING_MSG];
    }
    
    if ([ExSystem sharedInstance].sys.productLine == PROD_GOVERNMENT){
        NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Gov", @"Type", nil];
        [Flurry logEvent:@"User: Type" withParameters:dictionary];
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    // Handle background to foreground etc states
    
    if ([[ApplicationLock sharedInstance] isLoggedIn])
	{
        //TODO: do refresh auto pull donw refresh
//		if (requireHomeScreenRefresh)
//		{
//            //refresh home screen with fresh datas
            [self fetchHomePageDataAndSkipCache:YES];
//            requireHomeScreenRefresh = NO;
//        }
        // Refresh the table - incase there is upload queue update.
        [self.tableView reloadData];
	}
    else
    {
        [[ApplicationLock sharedInstance] onHomeScreenAppeared];
    }
    
}

- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return @"GOV_HOME_PAGE90";
}

#pragma mark - ApplicationLock Notifications
-(void) doPostLoginInitialization
{
    // When user logs in always get data from server
    [self fetchHomePageDataAndSkipCache:YES];
    
    MobileAlertView *alert = [self getPrivacyActView:self];
    [alert show];
}

#pragma mark -Gov Warning messages
-(MobileAlertView*) getPrivacyActView:(UIViewController* )del
{
    NSManagedObjectContext *context = [ExSystem sharedInstance].context;
    NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:context];
    if ([allMessage count] > 0)
    {
        self.allMessages = (EntityWarningMessages*) [allMessage objectAtIndex:0];
    }
    
    __autoreleasing MobileAlertView *alert = [[MobileAlertView alloc]
                                              initWithTitle:allMessages.privacyTitle
                                              message:allMessages.privacyText
                                              delegate:del
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                              otherButtonTitles:nil];
    alert.tag = PRIVACY_ACT_ALERT_ACTION;
    return alert;
}

-(void) savePrarmetersAfterLogin:(NSDictionary *) pBag
{
    if (pBag != nil)
    {
        [self.postLoginAttribute addEntriesFromDictionary:pBag];
    }
}

#pragma mark - reset and then fetch the managed results
-(void) refetchData
{
    self.fetchedResultsController = nil;
    NSError *error;
	if (![[self fetchedResultsController] performFetch:&error])
    {
        if ([Config isDevBuild]) {
            exit(-1);  // Fail
        } else {
            // be more graceful when dying abort();
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"RootViewController::viewDidLoad: fetchedResultsController %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        }
	}
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [[self.fetchedResultsController sections] objectAtIndex:0];
    return [sectionInfo numberOfObjects];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityHome *obj = [self.fetchedResultsController objectAtIndexPath:indexPath];

    HomeCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"HomeCell" forIndexPath:indexPath];
    
    [self configureCell:cell atIndexPath:indexPath];
    
    //Show the cells as disabled if sitesettings are disable them.
    // display user data even though the cell is disabled.
    if([obj.key isEqualToString:kSECTION_TRAVEL])
    {
        // if the user has no travel role and the cell is travel cell then show only the image
        // Use a blank trip call instead.
        if(! ([[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER]) )
        {
            cell.lblSubTitle.hidden = YES;
            cell.lblTitle.hidden = YES;
            cell.ivIcon.hidden = YES;
            cell.whiteback.hidden = YES;
            cell.userInteractionEnabled = NO;
        }
        else // dequeueReusableCellWithIdentifier might return cell with hidden elements
        {
            cell.lblSubTitle.hidden = NO;
            cell.lblTitle.hidden = NO;
            cell.ivIcon.hidden = NO;
            cell.whiteback.hidden = NO;
            cell.userInteractionEnabled = YES;
        }
        
    }
    else if([obj.key isEqualToString:kSECTION_EXPENSE_REPORTS] )
    {
        // MOB-15438 Vouchers and Stamp Document Buttons Disabled
        // Verified with Ernest and Sunil that Gov voucher and stamp doc are not role or module setting based.
        cell.lblSubTitle.hidden = NO;
        cell.lblTitle.hidden = NO;
        cell.ivIcon.hidden = NO;
        cell.whiteback.hidden = NO;
        cell.userInteractionEnabled = YES;
    }
    else if([obj.key isEqualToString:kSECTION_EXPENSE_CARDS] )
    {
        // check sitesettings and disable Expenses cell - ??
        // No sitesettings yet for Expenses
        cell.lblSubTitle.hidden = NO;
        cell.lblTitle.hidden = NO;
        cell.ivIcon.hidden = NO;
        cell.whiteback.hidden = NO;
        cell.userInteractionEnabled = YES;
    }
    else if([obj.key isEqualToString:kSECTION_EXPENSE_APPROVALS] )
    {
        // MOB-15438 Vouchers and Stamp Document Buttons Disabled
        // Verified with Ernest and Sunil that Gov voucher and stamp doc are not role or module setting based.
        cell.lblSubTitle.hidden = NO;
        cell.lblTitle.hidden = NO;
        cell.ivIcon.hidden = NO;
        cell.whiteback.hidden = NO;
        cell.userInteractionEnabled = YES;
    }
    
    return cell;
}


-(void) configureCell:(HomeCell*)cell atIndexPath:(NSIndexPath*)indexPath
{
    EntityHome *entity = (EntityHome *)[self.fetchedResultsController objectAtIndexPath:indexPath];
    
    cell.backgroundView = [[UIView alloc] initWithFrame:cell.bounds];
    cell.lblTitle.text = entity.name;
    cell.lblSubTitle.text = entity.subLine;
    if(entity.imageName != nil)
        cell.ivIcon.image = [UIImage imageNamed:entity.imageName];
    
    // Dequereuseable cell might return disabled values
    cell.lblSubTitle.enabled = YES;
    cell.lblTitle.enabled =YES;
    cell.ivIcon.alpha = 1.0;
    cell.whiteback.alpha = 1.0;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    cell.userInteractionEnabled = YES;
}

-(void) disableCell:(HomeCell*)cell
{
    // Dequereuseable cell might return enabled values
    cell.lblSubTitle.enabled = NO;
    cell.lblTitle.enabled = NO;
    cell.ivIcon.alpha = 0.5;
    cell.whiteback.alpha = 0.5;
    cell.accessoryType = UITableViewCellAccessoryNone;
    
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
        return 40.0;
    else
        return 0.0;
}

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    int itemNum = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (itemNum > 0)
    {
        [self makeUploadView];
        self.uploadView.delegate = self;
        return self.uploadView;
    }
    else
        return nil;
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:newIndexPath];
    EntityHome *entity = (EntityHome *)managedObject;

    if([entity.key isEqualToString:kSECTION_TRIPS_TRIPS_BUTTON])
    {        
        NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"View Trips", @"Action", nil];
        [Flurry logEvent:@"Home: Action" withParameters:dictionary];
        
        TripsViewController *nextController = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
        [self.navigationController pushViewController:nextController animated:YES];
        
    }
    else if([entity.key isEqualToString:kSECTION_EXPENSE_CARDS]) // Expenses
    {
       
        [GovUnappliedExpensesVC showUnappliedExpenses:self];
    }
    else if ([entity.key isEqualToString:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON]) // auth
    {
        [self showGovDocumentListView:GOV_DOC_TYPE_AUTH];
    }
    else if ([entity.key isEqualToString:kSECTION_EXPENSE_REPORTS]) // voucher
    {
        [self showGovDocumentListView:GOV_DOC_TYPE_VOUCHER];
    }
    else if ([entity.key isEqualToString:kSECTION_EXPENSE_APPROVALS])   //stamped doc
    {
        [self showGovDocumentListView:GOV_DOC_TYPE_STAMP];
    }
}


-(void)fetchData:(NSMutableDictionary *)pBag
{
	NSString *msgName = [pBag objectForKey:@"MSG_NAME"];
	
	BOOL skipCache = NO;
	if(pBag != nil && ([pBag objectForKey:@"SKIP_CACHE"] != nil))
		skipCache = YES;
    
	NSString *cacheOnly = [pBag objectForKey:@"CACHE_ONLY"] ;
	[[ExSystem sharedInstance].msgControl createMsg:msgName CacheOnly:cacheOnly ParameterBag:pBag SkipCache:skipCache];
}


#pragma mark - Fetched results controller
- (NSFetchedResultsController *)fetchedResultsController
{
    if (__fetchedResultsController != nil) {
        return __fetchedResultsController;
    }
    
    // Killing the app and opening an application might make set the managedobject to nil.
    if(self.managedObjectContext == nil)
    {
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.managedObjectContext = [ad managedObjectContext];
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHome" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"sectionPosition" ascending:YES];
    NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"rowPosition" ascending:YES];
    [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sort, sort2, nil]];
    
    NSFetchedResultsController *theFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil cacheName:@"Root"];
    self.fetchedResultsController = theFetchedResultsController;
    __fetchedResultsController.delegate = self;
    
    return __fetchedResultsController;
}

#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self hideWaitView];
    [self.tableView beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [self.tableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableView;
    
    switch(type)
    {
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [self configureCell:(HomeCell*)[self.tableView cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:[NSArray arrayWithObject:newIndexPath]withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableView endUpdates];
}

-(void) clearHomeData
{
    if(currentTrip != nil)
    {
        self.currentTrip = nil;
    }
    [[HomeManager sharedInstance] clearAll];
}

# pragma mark -
# pragma mark Pulldown Refresh

// Return whether the task is completed
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    // Implement pulldown refresh
    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Refresh Data", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    [self fetchHomePageDataAndSkipCache:YES];
    // Update toolbar also
    return NO;
}

# pragma mark -
# pragma mark implementation
-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg]; // TODO: handle case where msg.didConnectionFail is YES
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:TRIPS_DATA])
	{
		TripsData* tripsData = (TripsData *)msg.responder;
		[self refreshUIWithTripsData:tripsData];
        [self.serverCallCounts removeObjectForKey:TRIPS_DATA];
	}
    else if ([msg.idKey isEqualToString:GOV_WARNING_MSG])
    {
        GovWarningMessagesData *messages = (GovWarningMessagesData*) msg.responder;
        if (messages != nil)
        {
            NSManagedObjectContext *context = [ExSystem sharedInstance].context;
            NSArray *allMessage = [BaseManager fetchAll:@"EntityWarningMessages" withContext:context];
            if ([allMessage count] > 0)
            {
                self.allMessages = (EntityWarningMessages*) [allMessage objectAtIndex:0];
            }
        }
        [self.serverCallCounts removeObjectForKey:GOV_WARNING_MSG];
    }
    
    // close loading view if there are no more servercalls
    if([self.serverCallCounts count]  == 0)
    {
        [self hideWaitView];
        [self hideLoadingView];
        [self.tableView reloadData];
        if(self.isRefreshing)
            [self doneRefreshing];
    }
}

- (void)fetchHomePageData
{
    [self fetchHomePageDataAndSkipCache:NO];
}

- (void)fetchHomePageDataAndSkipCache:(BOOL)shouldSkipCache
{    
    // Show wait view if the refresh is not yet complete
    if([[self.fetchedResultsController sections] count] <= 0)
        [self showWaitView];
        
    [self refreshSummaryData];
	
	if([[ExSystem sharedInstance] isValidSessionID:[ExSystem sharedInstance].sessionID] || [@"OFFLINE" isEqualToString:[ExSystem sharedInstance].sessionID])
	{
        [self getTripsData:shouldSkipCache];

        // Prepopulate Expense types
//        [[ExSystem sharedInstance].msgControl createMsg:GET_GOV_EXPENSE_TYPES CacheOnly:@"NO" ParameterBag:nil SkipCache:YES RespondTo:self];
    }
}

//Place a server call to get Trips data
-(void)getTripsData:(BOOL)shouldSkipCache
{
    if (([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER] || [[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER]))
    {
        NSMutableDictionary* pBag2 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", nil];
        [[ExSystem sharedInstance].msgControl createMsg:TRIPS_DATA CacheOnly:@"NO" ParameterBag:pBag2 SkipCache:shouldSkipCache RespondTo:self];
        [self.serverCallCounts setObject:TRIPS_DATA forKey:TRIPS_DATA];
    }
    else
    {
        // Remove any old Travel section and create an empty travel section so the table displays an empty cell
        EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:kSECTION_TRIPS_FIND_TRAVEL];
        if(entity != nil)
            [[HomeManager sharedInstance] deleteObj:entity];
        
        EntityHome *emptyentity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
        emptyentity.sectionValue = kSECTION_TRIPS;
        emptyentity.sectionPosition = [NSNumber numberWithInt:kSECTION_TRIPS_POS];
        emptyentity.rowPosition = [NSNumber numberWithInt:0];
        emptyentity.key = kSECTION_TRAVEL;
        [[HomeManager sharedInstance] saveIt:emptyentity];
    }
    
}

// Build UI data here
-(void)refreshUIWithSummaryData:(SummaryData*)sd
{
//	NSString *reportsToApproveCount = [sd.dict objectForKey:@"ReportsToApproveCount"];
//    if (![reportsToApproveCount length])
//        reportsToApproveCount = @"0";
//	NSString *unsubmittedReportsCount = [sd.dict objectForKey:@"UnsubmittedReportsCount"];
//    if (![unsubmittedReportsCount length])
//        unsubmittedReportsCount = @"0";
//	NSString *corpCardTransactionCount = [sd.dict objectForKey:@"CorporateCardTransactionCount"];
//    if (![corpCardTransactionCount length])
//        corpCardTransactionCount = @"0";
//    
//    NSString *travelRequestsToApprove = [sd.dict objectForKey:@"TravelRequestApprovalCount"];
//    
//    if (![travelRequestsToApprove length])
//        travelRequestsToApprove = @"0";
//    
    int iPos = 0;
    EntityHome *entity = nil;

    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON];
    entity.name = [Localizer getLocalizedText:@"Authorizations"];
    entity.subLine  = [Localizer getLocalizedText:@"View and update authorizations"];
    entity.imageName = @"icon_authorizations";
    entity.key = kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos++;
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
    entity.name = [Localizer getLocalizedText:@"Vouchers"];
    entity.subLine  = [Localizer getLocalizedText:@"View, create and update vouchers"];
    entity.key = kSECTION_EXPENSE_REPORTS;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.imageName = @"icon_vouchers";
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos++;
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
    entity.name = [Localizer getLocalizedText:@"Stamp Documents"];
    entity.subLine  = [Localizer getLocalizedText:@"Approve authorizations and vouchers"];
    entity.key = kSECTION_EXPENSE_APPROVALS;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.imageName = @"icon_stamp_documents";
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos++;
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
    entity.name = [Localizer getLocalizedText:@"Expenses"];
    entity.subLine  = [Localizer getLocalizedText:@"View unapplied expenses"];
    entity.key = kSECTION_EXPENSE_CARDS;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.imageName = @"home_icon_expense";
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos ++;
}

// Post a server call to refresh summary data
// Temp fix only. Server call is actually not needed here. we can simply update core data when summary is udpated
-(void) refreshSummaryData
{
    int iPos = 0;
    EntityHome *entity = nil;
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON];
    entity.name = [Localizer getLocalizedText:@"Authorizations"];
    entity.subLine  = [Localizer getLocalizedText:@"View and update authorizations"];
    entity.imageName = @"icon_authorizations";
    entity.key = kSECTION_TRIPS_TRAVEL_REQUEST_BUTTON;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos++;
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_REPORTS];
    entity.name = [Localizer getLocalizedText:@"Vouchers"];
    entity.subLine  = [Localizer getLocalizedText:@"View, create and update vouchers"];
    entity.key = kSECTION_EXPENSE_REPORTS;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.imageName = @"icon_vouchers";
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos++;
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_APPROVALS];
    entity.name = [Localizer getLocalizedText:@"Stamp Documents"];
    entity.subLine  = [Localizer getLocalizedText:@"Approve authorizations and vouchers"];
    entity.key = kSECTION_EXPENSE_APPROVALS;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.imageName = @"icon_stamp_documents";
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos++;
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_EXPENSE_CARDS];
    entity.name = [Localizer getLocalizedText:@"Expenses"];
    entity.subLine  = [Localizer getLocalizedText:@"View unapplied expenses"];
    entity.key = kSECTION_EXPENSE_CARDS;
    entity.sectionValue = kSECTION_EXPENSE;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_EXPENSE_POS];
    entity.imageName = @"home_icon_expense";
    entity.rowPosition = [NSNumber numberWithInt:iPos];
    [[HomeManager sharedInstance] saveIt:entity];
    iPos ++;
}

// Build trips data for UI
- (void) refreshUIWithTripsData: (TripsData *) tripsData
{
	//TripData* currentTrip = nil;
    int upcoming = 0;
    int active = 0;
    
    NSArray *aTrips = [[TripManager sharedInstance] fetchAll];
	
    // MOB-7192 Strip out timezone info in current local time, so that we can compare it with travel time in GMT(w/o timezone info).
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
    
    // MOB-5945 Whenever new TripsData comes, we need to reset active/upcoming trips as well as currentTrip
	if ([aTrips count] > 0) /*&& self.currentTrip == nil*/
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)[aTrips objectAtIndex:i];

            // MOB-5945 - Make comparison consistent with the logic in TripsViewController
			if (([trip.tripEndDateLocal compare:now] == NSOrderedDescending) && ([trip.tripStartDateLocal compare:now] == NSOrderedAscending))
            {
                //is the end date of the looped to trip greater than today and is the startdate before now?
                active ++;
                if (self.currentTrip == nil)
                {
                    self.currentTrip = trip;
                }
                else
                {
                    if ([trip.tripStartDateLocal compare:currentTrip.tripStartDateLocal] == NSOrderedAscending)
                    {
                        self.currentTrip = trip;
                    }
                }
            }
		}
	}
    else
        self.currentTrip = nil;  // No trips
    
    
    if ([aTrips count] > 0)
	{
		for (int i = 0; i < [aTrips count]; i++)
		{
			EntityTrip* trip = (EntityTrip*)[aTrips objectAtIndex:i];
			//NSDate* startDate = [DateTimeFormatter getLocalDate:trip.tripStartDateLocal];
			if ([trip.tripStartDateLocal compare:now] == NSOrderedAscending) //is the start date of the looped to trip greater than today?
				continue; //yup yup
			
            upcoming++;
		}
	}
	
    //Trips Row
    EntityHome *entity = (EntityHome *)[[HomeManager sharedInstance] fetchOrMake:@"EntityHome" key:kSECTION_TRAVEL];
    entity.name = [Localizer getLocalizedText:@"Trips"];
    
    NSString *sub = nil;
    if (active==0 && upcoming == 0)
    {
        sub = [Localizer getLocalizedText:@"TRAVEL_NEG_TEXT"];
    }
    else
    {
        sub = [NSString stringWithFormat:[Localizer getLocalizedText:@"int active int upcoming"], active, upcoming];
    }
    entity.subLine = sub;
    entity.key = kSECTION_TRAVEL;
    entity.sectionValue = kSECTION_TRIPS;
    entity.sectionPosition = [NSNumber numberWithInt:kSECTION_TRIPS_POS];
    entity.imageName = @"home_icon_trip";
    entity.rowPosition = [NSNumber numberWithInt:0];
    [[HomeManager sharedInstance] saveIt:entity];
    
    //kill off book travel rows if the user should not have it
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_HOTEL];
    if (entity != nil)
        [[HomeManager sharedInstance] deleteObj:entity];
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_AIR];
    if (entity != nil)
        [[HomeManager sharedInstance] deleteObj:entity];
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_CAR];
    if (entity != nil)
        [[HomeManager sharedInstance] deleteObj:entity];
    
    entity = (EntityHome *)[[HomeManager sharedInstance] fetchHome:BOOKINGS_BTN_RAIL];
    if (entity != nil)
        [[HomeManager sharedInstance] deleteObj:entity];
}

-(void) showManualLoginView
{
	if ([ConcurMobileAppDelegate isLoginViewShowing])
		return;

    // MOB-16161- Load new login UI , new login UI is in a storyboard.
    LoginViewController* lvc = [[UIStoryboard storyboardWithName:@"Login" bundle:nil] instantiateViewControllerWithIdentifier:@"GovLogin"];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:lvc];
	[self presentViewController:navi animated:YES completion:nil];
    ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	delegate.topView = LOGIN;
}

-(UIBarButtonItem *)getCustomBarButton:(NSString *)imgName withTitle:(NSString *)title withSelector:(SEL)selectorName withBorder:(UIColor *)bordercolor specialLayOut:(BOOL)hasSpecialLayout
{
    UIButton *button = nil;
    UIBarButtonItem *barButton = nil;
    UIImage *toolbarimage = [UIImage imageNamed: @"action_bar_cell"];
    
    button = [UIButton buttonWithType:UIButtonTypeCustom];
    [button setImage:[UIImage imageNamed:imgName] forState:UIControlStateNormal];
    [button setTitle:title forState:UIControlStateNormal];
    [button.titleLabel setFont:[UIFont fontWithName:@"HelveticaNeue" size:17.0f]];
    [button setTitleColor:[UIColor colorWithRed:42/255.f green:46/255.f blue:56/255.f alpha:1.f] forState:UIControlStateNormal];
    button.bounds = CGRectMake(0,0,self.navigationController.toolbar.frame.size.width/2, toolbarimage.size.height + 6);
    [button addTarget:self action:selectorName forControlEvents:UIControlEventTouchUpInside];
    
    if(bordercolor!=nil)
    {
        // Set border
        [[button layer] setBorderWidth:1.0f];
        [[button layer] setBorderColor:bordercolor.CGColor];
    }
    
    if(selectorName == nil)
    {
        button.userInteractionEnabled = NO;
    }
    
    if (hasSpecialLayout)
    {
        CGSize imageSize = button.imageView.frame.size;
        CGSize titleSize = button.titleLabel.frame.size;
        
        button.titleEdgeInsets = UIEdgeInsetsMake(0.0, -imageSize.width-28.0, 0.0, 0.0);
        button.imageEdgeInsets = UIEdgeInsetsMake(0.0, titleSize.width, 0.0, 0.0);
    }
    barButton = [[UIBarButtonItem alloc] initWithCustomView:button];
    
    return barButton;
}

#pragma mark -
#pragma mark Properties

-(BOOL) checkBookAir
{
    NSString* msg = nil;
    if (!([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] || [[ExSystem sharedInstance] hasRole:ROLE_AIR_BOOKING_ENABLED]))
    {
        msg = [Localizer getLocalizedText:@"AIR_BOOKING_DISABLED_MSG"];
    }
    else
    {
        NSString* profileStatus = [[ExSystem sharedInstance] getUserSetting:@"ProfileStatus" withDefault:@"0"];
        // MOB-10390 Allow users with profileStatus 1 (missing middlename, gender) to go ahead and search air.
        if (![profileStatus isEqualToString:@"0"] && ![profileStatus isEqualToString:@"1"])
        {
            if ([profileStatus isEqualToString:@"20"])
                profileStatus = @"2";
            NSString* msgKey = [NSString stringWithFormat:@"AIR_BOOKING_PROFILE_%@_MSG", profileStatus];
            msg = [NSString stringWithFormat:@"%@\n\n%@", [Localizer getLocalizedText:msgKey], [@"AIR_BOOKING_PROFILE_PROLOG_MSG" localize]];
        }
        else
            return TRUE;
    }
    
    MobileAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:msg
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                              otherButtonTitles:nil];
    [alert show];
    
    
    // MOB-12675 toolbar disappears after this popup. display it again.
    return FALSE;
}

#pragma mark -
#pragma mark Toolbar/Nav bar action delegates
-(void)bookingsActionPressed:(id)sender
{
    if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Bookings offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
		return;
	}
    
	MobileActionSheet *action = [[MobileActionSheet alloc] initWithTitle:nil
                                                                delegate:self
                                                       cancelButtonTitle:nil
                                                  destructiveButtonTitle:nil
                                                       otherButtonTitles: nil];
    NSMutableArray* btnIds = [[NSMutableArray alloc] init];
    
    [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Air"]];
    [btnIds addObject:BOOKINGS_BTN_AIR];
    [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Hotel"]];
    [btnIds addObject:BOOKINGS_BTN_HOTEL];
    [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Car"]];
    [btnIds addObject:BOOKINGS_BTN_CAR];
    
    if([[ExSystem sharedInstance] hasRole:ROLE_GOVERNMENT_TRAVELER] ||
       ([[ExSystem sharedInstance] hasRole:ROLE_AMTRAK_USER]
        && (![[ExSystem sharedInstance] hasRole:ROLE_ITINVIEWER_USER]
            || [[ExSystem sharedInstance] hasRole:ROLE_TRAVEL_USER])))
    {
        [action addButtonWithTitle:[Localizer getLocalizedText:@"Book Rail"]];
        [btnIds addObject:BOOKINGS_BTN_RAIL];
    }
    
    [action addButtonWithTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]];
    action.cancelButtonIndex = [btnIds count];
    
    action.btnIds = btnIds;
    
	action.tag = BOOKINGS_ACTIONSHEET_TAG;
	
    [action showInView:[UIApplication sharedApplication].keyWindow];
}


-(void) buttonQuickExpensePressed:(id)sender
{

    NSDictionary *dictionary = [NSDictionary dictionaryWithObjectsAndKeys:@"Quick Expense", @"Action", nil];
    [Flurry logEvent:@"Home: Action" withParameters:dictionary];
    
    GovExpenseEditViewController* vc = [[GovExpenseEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    [self.navigationController pushViewController:vc animated:YES];

}

// Action Sheet delegates
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (actionSheet.tag == BOOKINGS_ACTIONSHEET_TAG)
	{
        if (buttonIndex != actionSheet.cancelButtonIndex)
        {
            MobileActionSheet* mas = (MobileActionSheet*) actionSheet;
            NSString* btnId = [mas getButtonId:buttonIndex];
            [self.navigationController setToolbarHidden:YES animated:YES];
            
            if ([BOOKINGS_BTN_HOTEL isEqualToString:btnId])
            {
                [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_HOTEL withFields:nil withDelegate:nil asRoot:NO];
            }
            else if ([BOOKINGS_BTN_CAR isEqualToString:btnId])
            {
                [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_CAR withFields:nil withDelegate:nil asRoot:NO];
            }
            else if ([BOOKINGS_BTN_RAIL isEqualToString:btnId])
            {
                [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_RAIL withFields:nil withDelegate:nil asRoot:NO];
            }
            else if ([BOOKINGS_BTN_AIR isEqualToString:btnId])
            {
                if ([self checkBookAir])
                {
                    [GovSelectTANumVC showSelectTANum:self withCompletion:BOOKINGS_BTN_AIR withFields:nil withDelegate:nil asRoot:NO];
                }
            }
        }
	}
}

#pragma mark - Upload Queue Banner View adjustment
-(void) showUploadViewController
{
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    UIBarButtonItem *btnUpload = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Upload"] style:UIBarButtonItemStyleBordered target:vc action:@selector(startUpload)];
    vc.title = [Localizer getLocalizedText:@"Upload Queue"];
    vc.navigationItem.rightBarButtonItem = btnUpload;
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma -mark GSA Actions
-(void) showGovDocumentListView:(NSString*) filter
{
    GovDocumentListVC *vc = [[GovDocumentListVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    vc.filter = filter;
    // MOB-12319 get same document list content on iPhone and iPad.
    NSString* msgId = [filter isEqualToString:GOV_DOC_TYPE_STAMP]? GOV_DOCUMENTS_TO_STAMP: GOV_DOCUMENTS;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:vc];
    
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark -
#pragma mark UIAlertViewDelegate Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (alertView.tag == PRIVACY_ACT_ALERT_ACTION)
    {
        // Display rules of behavior for gov Safe harbor required user
        if (buttonIndex == 0 && [[postLoginAttribute objectForKey:@"NEED_SAFEHARBOR"] isEqualToString:@"true"])
        {
            GovLoginNoticeVC *noticeVC = [[GovLoginNoticeVC alloc] initWithNibName:@"LoginHelpTopicVC" bundle:nil];
            noticeVC.title = [Localizer getLocalizedText:@"Rules of Behavior"];
            UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:noticeVC];
            localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
            [self presentViewController:localNavigationController animated:YES completion:nil];
        }
    }
}

@end
