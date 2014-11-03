//
//  GovDocumentListVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/19/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocumentListVC.h"
#import "FormatUtils.h"
#import "MobileActionSheet.h"

#import "GovDocumentCell.h"
#import "EntityGovDocument.h"
#import "DateTimeFormatter.h"
#import "GovDocumentManager.h"
#import "GovDocDetailVC.h"
#import "GovDocDetailVC_iPad.h"
#import "GovSelectAuthForVoucherVC.h"

#define SORT_BY_DATE @"SORT_BY_DATE"
#define SORT_BY_DOC_NAME @"SORT_BY_DOC_NAME"
#define ADD_ACTIONSHEET_TAG 111
#define SORT_ACTIONSHEET_TAG 222

@interface GovDocumentListVC (Private)
-(void)closeView:(id)sender;
-(void)displaySelectAuthForVouchVC;

@end

@interface GovDocumentListVC ()
@property (nonatomic, strong) NSString      *sortOrder;
@end

@implementation GovDocumentListVC
@synthesize fetchedResultsController=__fetchedResultsController;
@synthesize managedObjectContext=__managedObjectContext;
@synthesize filter;

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

#pragma mark -
#pragma mark ActionStatus
-(void)closeView:(id)sender
{
	if([UIDevice isPad])
	{
		[self dismissViewControllerAnimated:YES completion:nil];
        [MobileActionSheet dismissAllMobileActionSheets];
	}
}

-(BOOL) canAdd
{
    // MOB-12301 disable Add button for Auth
    return ! ([GOV_DOC_TYPE_STAMP isEqualToString:self.filter] || [GOV_DOC_TYPE_AUTH isEqualToString:self.filter]);
}


-(BOOL) isDocumentsToStamp
{
    return [self.filter isEqualToString:GOV_DOC_TYPE_STAMP];
}


-(void)setupToolbar
{
    // Navi bar items
    if ([self canAdd])
    {
        UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
        
        self.navigationItem.rightBarButtonItem = nil;
        [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
    }
    
    // Toolbar items
    UIBarButtonItem *btnSort = [[UIBarButtonItem alloc] initWithTitle:@"Sort" style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSortPressed:)];
    UIBarButtonItem *btnFlexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSArray *toolBarItems = @[btnFlexibleSpace, btnSort, btnFlexibleSpace];
    
    [self setToolbarItems:toolBarItems];
    [self.navigationController setToolbarHidden:NO];
}

- (void)viewDidLoad
{
    if([UIDevice isPad])
	{
		self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
	}

    if ([self.filter isEqualToString:GOV_DOC_TYPE_AUTH])
        self.title = [Localizer getLocalizedText:@"Authorizations"];
	else if ([self.filter isEqualToString:GOV_DOC_TYPE_VOUCHER])
        self.title = [Localizer getLocalizedText:@"Vouchers"];
    else
        self.title = [Localizer getLocalizedText:@"Stamp Documents"];
    
    self.sortOrder = SORT_BY_DATE;
    
    [super viewDidLoad];

    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.managedObjectContext = [ad managedObjectContext];

    if (self.fetchedResultsController == nil)
    {
        [self showLoadingView]; // Init load, still waiting for data to come back
    }
    else
    {
        [self resetData];       // Refresh view with NoData view, etc, since msg is back
    }
    
    [self setupToolbar];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    NSDictionary *param = nil;
    if ([self.filter isEqualToString:GOV_DOC_TYPE_AUTH])
    {
        param = @{@"Auth list" : [NSNumber numberWithInteger:[[self.fetchedResultsController fetchedObjects] count]] };
        [Flurry logEvent:@"Authorization: Auth list" withParameters:param];
    }
    else if ([self.filter isEqualToString:GOV_DOC_TYPE_VOUCHER])
    {
        param = @{@"Voucher List" : [NSNumber numberWithInteger:[[self.fetchedResultsController fetchedObjects] count]] };
        [Flurry logEvent:@"Voucher: Voucher List" withParameters:param];
    }
    else
    {
        param = @{@"Stamp Doc List" : [NSNumber numberWithInteger:[[self.fetchedResultsController fetchedObjects] count]] };
        [Flurry logEvent:@"Stamp Document: Stamp Doc List" withParameters:param];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark -
#pragma mark Message methods
- (void)resetData
{
    [self fetchedResults];
    [self.tableView reloadData];
    [self hideLoadingView];
    
    if ([self isViewLoaded]) {
        if ([[self.fetchedResultsController fetchedObjects] count]<1)
        {
            [self showNoDataView:self];
        }
        else
        {//refresh from the server, after an initial no show...
            [self hideNoDataView];
            [self.navigationController setToolbarHidden:NO];
        }
    }
}

-(void) respondToFoundData:(Msg *)msg
{
	if(([msg.idKey isEqualToString:GOV_DOCUMENTS]) || ([msg.idKey isEqualToString:GOV_DOCUMENTS_TO_STAMP] && [self isDocumentsToStamp]))
	{
        [self resetData];
        
        if ([@"YES" isEqualToString: [msg.parameterBag objectForKey:@"REFRESHING"]])
            [self doneRefreshing];
    }
}

#pragma mark -
#pragma mark UIActionSheetDelegate Methods
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    // MOB 12301 delete "create Voucher" function for Gov 1.0 release.
//    if (buttonIndex == 0) // Add voucher
//    {
//        // TODO -
//    }
//    else if (buttonIndex == 1) // Add voucher from Authorization expenses
//    {
//        [self displaySelectAuthForVouchVC];
//    }
//    else
//        [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
    if (actionPopOver.tag == ADD_ACTIONSHEET_TAG)
    {
        if (buttonIndex == 0)
        {
            [self displaySelectAuthForVouchVC];
        }
        else
            [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
    }
    else if (actionPopOver.tag == SORT_ACTIONSHEET_TAG)
    {
        if (buttonIndex == 0)
        {
            self.sortOrder = SORT_BY_DATE;
            [self resetData];
        }
        else if (buttonIndex == 1)
        {
            self.sortOrder = SORT_BY_DOC_NAME;
            [self resetData];
        }
        else
        {
            [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
        }
    }
}

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    self.actionPopOver = nil;
}

-(IBAction)buttonAddPressed:(id) sender
{
    if (self.actionPopOver) {
        [self.actionPopOver dismissWithClickedButtonIndex:-1 animated:YES];
        self.actionPopOver = nil;
        return;
    }
    
    if ([GOV_DOC_TYPE_VOUCHER isEqualToString:self.filter])
    {
        // MOB 12301 delete "create Voucher" for Gov 1.0 release
//        UIActionSheet * addDocAction = [[MobileActionSheet alloc] initWithTitle:nil
//                                                                       delegate:self
//                                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
//                                                         destructiveButtonTitle:nil
//                                                              otherButtonTitles:[Localizer getLocalizedText:@"Create Voucher"],
//                                        [Localizer getLocalizedText:@"Create from Authorization"], nil];
        
        self.actionPopOver = [[MobileActionSheet alloc] initWithTitle:nil
                                                                       delegate:self
                                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
                                                         destructiveButtonTitle:nil
                                                              otherButtonTitles:[Localizer getLocalizedText:@"Create from Authorization"], nil];
        self.actionPopOver.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
        [self.actionPopOver setTag:ADD_ACTIONSHEET_TAG];        //
        // MOB-12249 changed actionsheet location at the same place.
        [self.actionPopOver showFromBarButtonItem:sender animated:YES];
	}
    else
    {
        // TODO - Add authorization
    }
}

- (IBAction)buttonSortPressed:(id)sender
{
    if (self.actionPopOver) {
        [self.actionPopOver dismissWithClickedButtonIndex:-1 animated:YES];
        self.actionPopOver = nil;
        return;
    }
    
    self.actionPopOver = [[MobileActionSheet alloc] initWithTitle:nil delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:nil otherButtonTitles:@"Sort by date", @"Sort by document name", nil];
    self.actionPopOver.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
    [self.actionPopOver setTag:SORT_ACTIONSHEET_TAG];
    [self.actionPopOver showFromBarButtonItem:sender animated:YES];
}

-(void)displaySelectAuthForVouchVC
{
    GovSelectAuthForVoucherVC *vc = [[GovSelectAuthForVoucherVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENTS_AUTH_FOR_VCH CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:vc];
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - Cell Config
- (void)configureCell:(GovDocumentCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityGovDocument *doc = (EntityGovDocument *)managedObject;
    
    cell.lblName.text = doc.travelerName;
    cell.lblAmount.text = [FormatUtils formatMoneyWithNumber:doc.totalExpCost crnCode:@"USD"];
    
    NSString *startFormatted = [DateTimeFormatter formatDate:doc.tripBeginDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];

    NSString *endFormatted = [DateTimeFormatter formatDate:doc.tripEndDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    if (!(startFormatted == nil || endFormatted == nil))
        cell.lblLine2.text = [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];
    else
        cell.lblLine2.text = @"";
    
    cell.lblLine1.text= doc.docName;
    cell.lblRLine1.text = doc.docTypeLabel;

    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [[self.fetchedResultsController fetchedObjects] count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    GovDocumentCell *cell = (GovDocumentCell *)[self.tableView dequeueReusableCellWithIdentifier: @"GovDocumentCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"GovDocumentCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[GovDocumentCell class]])
                cell = (GovDocumentCell *)oneObject;
    }
    
    [self configureCell:cell atIndexPath:indexPath];
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 64;
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityGovDocument *doc = (EntityGovDocument *)managedObject;
    if ([UIDevice isPad])
    {
        [GovDocDetailVC_iPad showDocDetailWithTraveler:doc.travelerId withDocName:doc.docName withDocType:doc.docType];
        [self closeView:nil];
    }
    else
    {
        [GovDocDetailVC showDocDetail:self withTraveler:doc.travelerId withDocName:doc.docName withDocType:doc.docType withGtmDocType:doc.gtmDocType];
        [tableView deselectRowAtIndexPath:indexPath animated:NO];
    }
}

#pragma mark - Fetched results controller
- (void)fetchedResults
{
    if (self.fetchedResultsController == nil) {
        
        NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityGovDocument" inManagedObjectContext:self.managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSSortDescriptor *sortByDocName = [[NSSortDescriptor alloc] initWithKey:@"docName" ascending:YES];
        NSSortDescriptor *sortByTripBeginDate = [[NSSortDescriptor alloc] initWithKey:@"tripBeginDate" ascending:YES];
        
        if ([self.sortOrder isEqualToString:SORT_BY_DOC_NAME]) {
            [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sortByDocName, sortByTripBeginDate, nil]];
        }
        else        // Default should be SORT_BY_DATE
        {
            [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sortByTripBeginDate, sortByDocName, nil]];
        }
        
        
        NSPredicate *pred = [NSPredicate predicateWithFormat:@"(gtmDocType = %@)", self.filter];
        // Use filter
        if ([self isDocumentsToStamp ])
            pred = [NSPredicate predicateWithFormat:@"(needsStamping = %@)", [NSNumber numberWithBool:YES]];
        [fetchRequest setPredicate:pred];
       
        NSFetchedResultsController *theFetchedResultsController =
        [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                            managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil
                                                       cacheName:nil] ;
        self.fetchedResultsController = theFetchedResultsController;
        self.fetchedResultsController.delegate = self;
    }
    else
    {
        NSSortDescriptor *sortByDocName = [[NSSortDescriptor alloc] initWithKey:@"docName" ascending:YES];
        NSSortDescriptor *sortByTripBeginDate = [[NSSortDescriptor alloc] initWithKey:@"tripBeginDate" ascending:YES];
        
        if ([self.sortOrder isEqualToString:SORT_BY_DOC_NAME]) {
            [[self.fetchedResultsController fetchRequest] setSortDescriptors:[NSArray arrayWithObjects:sortByDocName, sortByTripBeginDate, nil]];
        }
        else        // Default should be SORT_BY_DATE
        {
            [[self.fetchedResultsController fetchRequest] setSortDescriptors:[NSArray arrayWithObjects:sortByTripBeginDate, sortByDocName, nil]];
        }
    }
    NSError *error;
	if (![self.fetchedResultsController performFetch:&error]) {
		// Update to handle the error appropriately.
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"GovDocumentListVC: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
	}
    
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
            [self configureCell:(GovDocumentCell*)[self.tableView cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
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

#pragma NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    [self buttonAddPressed:self];
}

-(BOOL)canShowActionOnNoData
{
    // MOB-18114 states the piller button for add action in empty list is unnecessary
    // Delete the empty piller button.
//    return ![self isDocumentsToStamp];
    return NO;
}

- (NSString*) buttonTitleForNoDataView
{
    if ([self.filter isEqualToString:GOV_DOC_TYPE_VOUCHER])
        return @"Add Voucher";
    //MOB-12204 adjust negative view for Gov.
    //For Gov first release. Don't support manual adding auth or stampdoc.
//    else if ([self.filter isEqualToString:GOV_DOC_TYPE_AUTH])
//        return @"Add Authorization";
    return @"";
}

- (NSString*) titleForNoDataView
{
    if ([self.filter isEqualToString:GOV_DOC_TYPE_VOUCHER])
        return @"No Vouchers";
    else if ([self.filter isEqualToString:GOV_DOC_TYPE_AUTH])
        return @"No Authorizations";
    return @"No Documents to Stamp";
}

- (NSString *)instructionForNoDataView
{
    return @"";
}

-(NSString*) imageForNoDataView
{
    if ([self.filter isEqualToString:GOV_DOC_TYPE_VOUCHER])
        return @"neg_vouchers_icon";
    else if ([self.filter isEqualToString:GOV_DOC_TYPE_AUTH])
        return @"neg_authorization_icon";

    return @"neg_approvals_icon";
}


#pragma Refresh
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    [self showLoadingView];
    NSString* msgId = [filter isEqualToString:GOV_DOC_TYPE_STAMP]? GOV_DOCUMENTS_TO_STAMP: GOV_DOCUMENTS;

    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"REFRESHING", nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    return NO;
}
@end