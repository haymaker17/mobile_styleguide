//
//  GovUnappliedExpensesVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovUnappliedExpensesVC.h"
#import "EntityGovExpense.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "ImageUtil.h"
#import "GovAttachExpToDocData.h"
#import "GovDocumentListVC.h"
#import "GovDocDetailVC.h"
#import "GovDocDetailVC_iPad.h"
#import "GovExpenseEditViewController.h"
#import "GovDeleteUnappliedExpenseData.h"

@interface GovUnappliedExpensesVC (Private)
-(void)closeView:(id)sender;
-(void)resetData;
@end

@implementation GovUnappliedExpensesVC
@synthesize fetchedResultsController=__fetchedResultsController;
@synthesize managedObjectContext=__managedObjectContext;
@synthesize isAddingMode, selectedRows, docName, docType, travID, expKeys;
@synthesize delegate = _delegate;

#pragma mark - Seed
-(void) setSeedDelegate:(id<GovUnappliedExpensesDelegate>)del
{
    self.delegate = del;
}

#pragma mark -
#pragma mark ActionStatus
-(void)closeView:(id)sender
{
	if([UIDevice isPad])
	{
		[self dismissViewControllerAnimated:YES completion:nil];
	}
}

- (void)viewDidLoad
{
    if([UIDevice isPad]/* && !isAddingMode*/)
	{
		self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
	}
    
    if (isAddingMode)
    {
        self.title = [Localizer getLocalizedText:@"Select Expenses"];
    }
    else
    {
        self.title = [Localizer getLocalizedText:@"Expenses"];
        [self setupNavBar];
    }
    
    [super viewDidLoad];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.managedObjectContext = [ad managedObjectContext];
    
    self.selectedRows = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
    if (self.fetchedResultsController == nil)
    {
        [self showLoadingView]; // Init load, still waiting for data to come back
    }
    else
    {
        [self resetData];       // Refresh view with NoData view, etc, since msg is back
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    NSMutableDictionary *param = [NSMutableDictionary dictionary];
    param[@"Count"] = [NSNumber numberWithInteger:[[self.fetchedResultsController fetchedObjects] count]];

    if (self.isAddingMode)          // add expense to document
    {
        param[@"Type"] = self.docType;
        [Flurry logEvent:@"Unapplied Expense: Add to doc" withParameters:param];
    }
    else        // View unapplied expenses.
    {
        int expWithReceiptCnt = 0;
        NSIndexPath *indexPath = nil;
        for (int i = 0; i < [[self.fetchedResultsController fetchedObjects] count]; i++)
        {
            indexPath = [NSIndexPath indexPathForRow:i inSection:0];
            EntityGovExpense *expense = (EntityGovExpense*) [self.fetchedResultsController objectAtIndexPath:indexPath];
            if ([expense.imageId length])
            {
                expWithReceiptCnt ++;
            }
        }
        param[@"Count with receipts"] = [NSNumber numberWithInt:expWithReceiptCnt];
        [Flurry logEvent:@"Unapplied Expense: View" withParameters:param];
    }
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
        }
    }
    
    if (isAddingMode)
    {
        [self buttonAddToVoucherOnePressed:nil];
    }
}

-(void) respondToFoundData:(Msg *)msg
{
	if([msg.idKey isEqualToString:GOV_UNAPPLIED_EXPENSES])
	{
        [self resetData];
    }
    else if ([msg.idKey isEqualToString:GOV_ATTACH_EXP_TO_DOC])
    {
        GovAttachExpToDocData* srd = (GovAttachExpToDocData*) msg.responder;
        BOOL uploadStatus = NO;
        if(srd != nil)
        {
            [self hideWaitView];
            uploadStatus = srd.overAllStatus;
        }
        
        if(uploadStatus == YES)
        {
            [self goToDocumentDetailScreen];
        }
        else
        {
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:[Localizer getLocalizedText:@"Upload Failed"]
								  message:nil
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
            [alert show];
        }
    }
    else if ([msg.idKey isEqualToString:GOV_DELETE_UNAPPLIED_EXPENSE])
    {
        [self hideWaitView];
        
        GovDeleteUnappliedExpenseData* data = (GovDeleteUnappliedExpenseData*) msg.responder;
        if (msg.responseCode != 200 || data.status == nil || data.status.status == nil || ![@"SUCCESS" isEqualToString:data.status.status])
        {
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:[Localizer getLocalizedText:@"Delete Failed"]
								  message:data.status.status //[Localizer getLocalizedText:@"Could not delete expense."]
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
        }
    }
    
    if ([@"YES" isEqualToString: [msg.parameterBag objectForKey:@"REFRESHING"]])
        [self doneRefreshing];
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


#pragma mark - Cell Config
- (void)configureCell:(SummaryCellMLines *)cell atIndexPath:(NSIndexPath *)indexPath
{
    NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
    EntityGovExpense *expense = (EntityGovExpense *)managedObject;
    
    NSString* amountStr = [FormatUtils formatMoneyWithNumber:expense.amount crnCode:@"USD"];
    NSString* dateStr = [DateTimeFormatter formatDate:expense.expDate Format:@"MMM dd, yyyy" TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    NSString * image1 = ![expense.imageId length]? nil: @"icon_receipt_19";
    [cell resetCellContent:expense.expenseDesc withAmount:amountStr withLine1:dateStr  withLine2: nil withImage1:image1 withImage2:nil withImage3:nil];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    SummaryCellMLines *cell = (SummaryCellMLines *)[tableView dequeueReusableCellWithIdentifier: @"SummaryCell3Lines"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell3Lines" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[SummaryCellMLines class]])
                cell = (SummaryCellMLines *)oneObject;
    }

    [self configureCell:cell atIndexPath:indexPath];
    [cell layoutWithSelect:self.isAddingMode];
    
    NSUInteger row = [indexPath row];
    NSString *sRow = [NSString stringWithFormat:@"%lu", (unsigned long)row];
    
    if (self.isAddingMode)
        [cell selectCell:[selectedRows objectForKey:sRow] != nil];
    else
        [cell selectCell:NO];
     
    if(!self.isAddingMode)
        [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 50;
}

//- (void)setSelected:(BOOL)selValue toCell:(SummaryCellMLines *)cell
//{
//    cell.selected = selValue;
//    if(selValue == YES)
//    {
//        UIView *selectedBackground = [[UIView alloc] init];
//        selectedBackground.backgroundColor = [UIColor colorWithRed:233/255.0 green:240/255.0 blue:251/255.0 alpha:1.0];
//        cell.lblName.highlightedTextColor = [UIColor blackColor];
//        cell.lblLine1.highlightedTextColor = [UIColor blackColor];
//        cell.lblLine2.highlightedTextColor = [UIColor blackColor];
//        cell.lblLine3.highlightedTextColor = [UIColor blackColor];
//        cell.lblAmount.highlightedTextColor = [UIColor blackColor];
//        cell.selectedBackgroundView = selectedBackground;
//    }
//    else
//        cell.selectionStyle = UITableViewCellSelectionStyleNone;
//
//}
#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    
    if (self.isAddingMode)      //show toggle selection with empty check mark
    {
        NSString *sRow = [NSString stringWithFormat:@"%lu", (unsigned long)row];
        
        if ([selectedRows objectForKey:sRow] != nil)
            [selectedRows removeObjectForKey:sRow];         //de-select
        else
            [selectedRows setObject:sRow forKey:sRow];      //select
        
        SummaryCellMLines* cell = (SummaryCellMLines*) [tableView cellForRowAtIndexPath:indexPath];
		[cell selectCell: ([selectedRows objectForKey:sRow] != nil)];

        if ([selectedRows count] < 1)
            [self makeSelectAllButtons];

        if ([selectedRows count] == [[self.fetchedResultsController fetchedObjects] count])
            [self makeUnSelectAllButtons];
        
        if([selectedRows count] > 0)
            [self buttonAddToVoucherOnePressed:self];
    }
    else                        //show detail view
    {
        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
        EntityGovExpense *expense = (EntityGovExpense *)managedObject;

        GovExpenseEditViewController* vc = [[GovExpenseEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
        [vc setSeedDelegate:self];
        vc.expenseId = expense.ccExpId;
        [self.navigationController pushViewController:vc animated:YES];
    }
}

-(void)tableView:(UITableView *)tableView didDeselectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    
    NSString *sRow = [NSString stringWithFormat:@"%lu", (unsigned long)row];
    
    if ([selectedRows objectForKey:sRow] != nil)
        [selectedRows removeObjectForKey:sRow];         //de-select
    
    if ([selectedRows count] < 1)
        [self makeSelectAllButtons];
    
    if ([selectedRows count] == [[self.fetchedResultsController fetchedObjects] count])
        [self makeUnSelectAllButtons];
    
    if([selectedRows count] > 0)
        [self buttonAddToVoucherOnePressed:self];
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(editingStyle == UITableViewCellEditingStyleDelete)
    {
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Deleting Expense"]];

        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:indexPath];
        EntityGovExpense *expense = (EntityGovExpense *)managedObject;

        //NSLog(@"Main thread is %p with name %@", [NSThread currentThread], [NSThread currentThread].name);
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:expense.ccExpId, @"EXPENSE_ID", nil];
        [[ExSystem sharedInstance].msgControl createMsg:GOV_DELETE_UNAPPLIED_EXPENSE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

#pragma mark - Fetched results controller
- (void)fetchedResults
{
    if (self.fetchedResultsController == nil) {
        
        NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityGovExpense" inManagedObjectContext:self.managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSSortDescriptor *sortDate = [[NSSortDescriptor alloc] initWithKey:@"expDate" ascending:YES];
        NSSortDescriptor *sortTitle= [[NSSortDescriptor alloc] initWithKey:@"expenseDesc" ascending:YES];
        NSSortDescriptor *sortAmount = [[NSSortDescriptor alloc] initWithKey:@"amount" ascending:YES];
        [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sortDate, sortTitle, sortAmount, nil]];
        
//        NSPredicate *pred = [NSPredicate predicateWithFormat:@"(gtmDocType = %@)"];
//        // Use filter
//        if ([self isDocumentsToStamp ])
//            pred = [NSPredicate predicateWithFormat:@"(needsStamping = %@)", [NSNumber numberWithBool:YES]];
//        [fetchRequest setPredicate:pred];
        
        NSFetchedResultsController *theFetchedResultsController =
        [[NSFetchedResultsController alloc] initWithFetchRequest:fetchRequest
                                            managedObjectContext:self.managedObjectContext sectionNameKeyPath:nil
                                                       cacheName:nil] ;
        self.fetchedResultsController = theFetchedResultsController;
        self.fetchedResultsController.delegate = self;
        
        
    }
    NSError *error;
	if (![self.fetchedResultsController performFetch:&error]) {
		// Update to handle the error appropriately.
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"GovUnappliedExpensesVC: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
	}
    
}

#pragma mark - Fetched results controller delegate
- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self hideWaitView];
    [self.tableView beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
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
            [self configureCell:(SummaryCellMLines*)[self.tableView cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
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

#pragma mark - NoDataMasterViewDelegate method
-(void) actionOnNoData:(id)sender
{
    
}

-(BOOL)canShowActionOnNoData
{
    // MOB-18114 Do no show add expense while there is a plus sign on top.
    return NO;
}

- (NSString*) buttonTitleForNoDataView
{
    // MOB-12372 disabel add expense button when no expense in the list.
    // TODO
    //return @"Add Expense";
    return nil;
}

- (NSString*) titleForNoDataView
{
    return [Localizer getLocalizedText:@"No Expenses"];
}

- (NSString *)instructionForNoDataView
{
    return @"";
}

-(NSString*) imageForNoDataView
{
    return @"neg_quickexpense_icon";
}

#pragma mark - GovExpenseEditDelegate methods

-(void) updatedExpense:(NSString*) expenseId;
{
    [self refreshView:nil];
}

#pragma mark - Refresh
-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    [self showLoadingView];
    NSString* msgId = GOV_UNAPPLIED_EXPENSES;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"REFRESHING", nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    return NO;
}

-(BOOL)enablePullDownRefresh
{
    if (isAddingMode)
        return FALSE;
    else
        return TRUE;
}

#pragma mark - show vc
+(void)showUnappliedExpenses:(UIViewController*)pvc
{
 	GovUnappliedExpensesVC *vc = [[GovUnappliedExpensesVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    
    NSString* msgId = GOV_UNAPPLIED_EXPENSES;
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:vc];

    if ([UIDevice isPad])
    {
        UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:vc];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];
        
        localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
        localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
        [pvc presentViewController:localNavigationController animated:YES completion:nil];
    }
    else
        [pvc.navigationController pushViewController:vc animated:YES];
}

//MOB-18114 Enable add expense in expense list
-(void)buttonAddExpensePressed:(id)sender
{
    GovExpenseEditViewController* vc = [[GovExpenseEditViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
    vc.delegate = self;
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - attach expenses btn click
-(void) buttonCancelPressed:(id)sender
{
    
}

-(void) buttonAddToVoucherOnePressed:(id)sender
{
    self.isAddingMode = YES;
    self.tableView.allowsMultipleSelectionDuringEditing = YES;
    
    //[self.tableView setEditing:YES];
    [self setUpToolBar:sender];
    [self.tableView reloadData];
}

-(void) buttonAddToVoucherPressed:(id)sender
{
    if ([selectedRows count] > 0)
        self.expKeys = [[NSMutableArray alloc] initWithCapacity:[selectedRows count]];
    else
        return;
    
    [self showWaitView];
    
    for(NSString *key in selectedRows)
    {
        NSUInteger x = [key intValue];
        NSIndexPath *path = [NSIndexPath indexPathForRow:x inSection:0];
        NSManagedObject *managedObject = [self.fetchedResultsController objectAtIndexPath:path];
        EntityGovExpense *expense = (EntityGovExpense *)managedObject;
        [self.expKeys addObject:expense.ccExpId];
    }
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:expKeys, @"KEYS", docType, @"DOC_TYPE", docName, @"DOC_NAME", nil];

    [[ExSystem sharedInstance].msgControl createMsg:GOV_ATTACH_EXP_TO_DOC CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void)buttonSelectAll:(id)sender
{
    NSUInteger row = 0;
    for ( id oneObject in [self.fetchedResultsController fetchedObjects])
    {
        NSString *sRow = [NSString stringWithFormat:@"%lu",(unsigned long) row];
        [selectedRows setObject:sRow forKey:sRow];
        row ++;
    }
    [self.tableView reloadData];
    [self makeUnSelectAllButtons];
}

-(void)buttonUnSelectAll:(id)sender
{
    [selectedRows removeAllObjects];
	[self.tableView reloadData];
	[self makeSelectAllButtons];
}

#pragma mark - attach expense UI set up
-(void)makeSelectAllButtons
{
    int selCount = (int)[selectedRows count];
	if(selCount == 0)
		selCount = -1;
    
    NSString *selectAll = [Localizer getLocalizedText:@"Select All"];
	
	UIBarButtonItem *btnSelectAll = [[UIBarButtonItem alloc] initWithTitle:selectAll style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSelectAll:)];
	UIBarButtonItem *btnAddToVoucher = [self makeAddToVoucherButton:selCount]; //[[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportPressed:)];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSMutableArray *toolbarItems = [NSMutableArray arrayWithObjects: btnSelectAll, flexibleSpace, btnAddToVoucher, nil];
	[self setToolbarItems:toolbarItems animated:NO];
}

-(void)makeUnSelectAllButtons
{
    int selCount = (int)[selectedRows count];
	if(selCount == 0)
		selCount = -1;
	
	NSString *unselectAll = [Localizer getLocalizedText:@"Unselect All"];
	
	UIBarButtonItem *btnUnSelectAll = [[UIBarButtonItem alloc] initWithTitle:unselectAll style:UIBarButtonItemStyleBordered target:self action:@selector(buttonUnSelectAll:)];
	UIBarButtonItem *btnAddToVoucher = [self makeAddToVoucherButton:selCount]; // [[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportPressed:)];
	UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
	flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSMutableArray *toolbarItems = [NSMutableArray arrayWithObjects: btnUnSelectAll, flexibleSpace, btnAddToVoucher, nil];
	[self setToolbarItems:toolbarItems animated:NO];
}

-(void)setupNavBar
{
    UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddExpensePressed:)];

    self.navigationItem.rightBarButtonItem = nil;
    [self.navigationItem setRightBarButtonItem:btnAdd animated:NO];
}

-(void) setUpToolBar:(id)sender
{
    if (isAddingMode)
    {
        NSUInteger selCount = [selectedRows count];
        if(selCount == 0)
            selCount = -1;
        
        NSString *cancel = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
        NSString *selectAll = [Localizer getLocalizedText:@"Select All"];
        
        UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:cancel style:UIBarButtonItemStyleBordered target:self action:@selector(buttonCancelPressed:)];
        self.navigationItem.rightBarButtonItem = nil;
        if (self.fetchedResultsController == nil)
            self.navigationItem.rightBarButtonItem = btnCancel;
        
        UIBarButtonItem *btnSelectAll = [[UIBarButtonItem alloc] initWithTitle:selectAll style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSelectAll:)];
        UIBarButtonItem *btnAddToVoucher = [self makeAddToVoucherButton:selCount];// [[UIBarButtonItem alloc] initWithTitle:addToReport style:UIBarButtonItemStyleBordered target:self action:@selector(buttonAddToReportPressed:)];
        UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];

        NSMutableArray *toolbarItems = [NSMutableArray arrayWithObjects: btnSelectAll, flexibleSpace, btnAddToVoucher, nil];
        [self setToolbarItems:toolbarItems animated:NO];
    }
}

-(UIBarButtonItem *)makeAddToVoucherButton:(NSInteger)count
{
    NSString *addText = [Localizer getLocalizedText:@"Add to Document"];
    
    if(count > 0)
        addText = [NSString stringWithFormat:@"%@ (%ld)", addText, (long)count];
    
    if ([ExSystem is7Plus])
    {
        UIBarButtonItem *button = [[UIBarButtonItem alloc] initWithTitle:addText style:UIBarButtonItemStylePlain target:self action:@selector(buttonAddToVoucherOnePressed:)];
        if(count == -2)
        {
            [button setEnabled:NO];
        }
        else if(count == -1 || count == 0)
        {
            [button setAction:@selector(buttonAddToVoucherPressed:)];
            [button setEnabled:NO];
        }
        else if(count > 0)
        {
            [button setAction:@selector(buttonAddToVoucherPressed:)];
            [button setEnabled:YES];
        }
        return button;
    }
    else
    {
        CGSize textSize = [addText sizeWithFont:[UIFont boldSystemFontOfSize:12]];
        
        CGFloat w = 150.0;
        CGFloat h = 30.0;
        
        if((textSize.width + 20) < w)
            w = textSize.width + 20;
        
        if(count == -2)
            return [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:w H:h Text:addText SelectorString:@"buttonAddToVoucherOnePressed:" MobileVC:self];
        else if(count == -1 || count == 0)
            return [ExSystem makeColoredButton:@"BLUE_INACTIVE" W:w H:h Text:addText SelectorString:@"buttonAddToVoucherPressed:" MobileVC:self];
        else if(count > 0)
            return [ExSystem makeColoredButton:@"BLUE" W:w H:h Text:addText SelectorString:@"buttonAddToVoucherPressed:" MobileVC:self];

        return nil;
    }
}

-(void) goToDocumentDetailScreen
{
    if ([UIDevice isPad])
    {
        [self.delegate didAttachSelectedExpense];
    }
    else
    {
        NSUInteger vcCount = [self.navigationController.viewControllers count];
        for (int ix = 0 ; ix < vcCount; ix++ )
        {
            MobileViewController *vc = (MobileViewController *)[self.navigationController.viewControllers objectAtIndex:ix];
            if (vc != nil && [vc isKindOfClass:[GovDocDetailVC class]])
            {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             travID, @"TRAVELER_ID",
                                             docType, @"DOC_TYPE",
                                             docName, @"DOC_NAME",
                                             nil];
                [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:vc];
                [self.navigationController popToViewController:vc animated:YES];
                break;
            }
        }
    }
}
@end
