//
//  QuickExpensesReceiptStoreVC.m
//  ConcurMobile
//
//  Created by charlottef on 3/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
// Change log
// MOB-13656 - 6/11/13 - Pavan Adavi
//
// Fix Summary for - MOB-13678
// EntityMobileEntry.key will hold mekey like wise pctkey for personal card and cctkey for corporate card
// In Editormode the self.selecteditems will consists of keys and keytype, keytype is later used to determine parameter bag
// for Delete : after successful delete remove entries from core data based on pctkey/mekey/cctkey, deleted/hidden cctkeys and mekeys are returned in MWS response while deleted pctkeys are not returned so they are handled differently.
// if anytime all keys are nil then its an offline entry.
// Add to report also follow the similar model to delete imported entries from coredata.
// MOB-13688 - Added support for personal card transactions
//
//
#import "QuickExpensesReceiptStoreVC.h"
#import "ViewStateHelper.h"
#import "FeedbackManager.h"
#import "FormatUtils.h"

#import "MobileEntryManager.h"
#import "MobileExpenseDelete.h"
#import "EntityReceiptInfoExtension.h"
#import "QEEntryDefaultLayoutCell.h"
#import "QEEntryCell.h"
#import "QEFormVC.h"
#import "SelectReportViewController.h"
#import "SmartExpenseManager.h"
#import "ReceiptStoreListCell.h"
#import "ReceiptCache.h"
#import "ReceiptDownloader.h"
#import "DeleteReceipt.h"
#import "UploadQueue.h"
#import "ReceiptEditorVC.h"
#import "UploadBannerView.h"
#import "UploadQueueViewController.h"
#import "ReceiptStoreUploadHelper.h"
#import "ReportDetailViewController.h"
#import "Config.h"
#import "SmartExpenseManager2.h"
#import "AnalyticsTracker.h"
#import "ExpenseTypesManager.h"

#import "HelpOverlayFactory.h"

#define SELECT_ALL_BUTTON_TAG 51

#define CORPORATE_FILTER @"CORPORATE_FILTER"
#define EXPENSEIT_FILTER @"EXPENSEIT _FILTER"
#define AMOUNT_SORTER @"AMOUNT_SORTER"
#define DATE_SORTER @"DATE_SORTER"

typedef enum
{
    EditorModeNormal,
    EditorModeMultiSelect,          // multiple-selection not have to do with deletion, e.g. add-to-report
    EditorModeMultiSelectDelete     // multiple-selection for deletion
}
EditorMode;

@interface QuickExpensesReceiptStoreVC ()
{
    NSFetchedResultsController *_fetchedResultsController;
    ReceiptStoreUploadHelper *rsuHelper;
}
@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;
@property (nonatomic, strong) UISegmentedControl            *segmentControl;
@property (nonatomic, strong) NSMutableDictionary           *selectedItems;
@property (nonatomic, strong) NSMutableDictionary           *deletedExpenseItems;

@property (nonatomic, strong) NSMutableArray                *selectedSmartExpenses;

@property (nonatomic, strong) NSMutableArray                *actionSheetItems;
@property (nonatomic, strong) NSString                      *expenseFilterName;
@property (nonatomic, strong) NSString                      *expenseSorterName;
@property (nonatomic, strong) ReceiptUploader               *uploader;
@property (nonatomic, assign) EditorMode                    editorMode;
@property (nonatomic, assign) BOOL                          deleteMEReturned;
@property (nonatomic, assign) BOOL                          deletePCTReturned;
@property (nonatomic, assign) BOOL                          deleteCCTReturned;
@property (nonatomic, assign) BOOL                          deleteRCReturned;
@property (nonatomic, assign) NSUInteger                           currentSegmentIndex;
@property (nonatomic, assign) BOOL                          canSwitchSegments;
@property (nonatomic, assign) BOOL                          canEditList;

@property (nonatomic, strong) SmartExpenseManager2          *smartExpenseManager2;

@end

@implementation QuickExpensesReceiptStoreVC

@synthesize managedObjectContext = _managedObjectContext;
@synthesize segmentControl = _segmentControl;
@synthesize editorMode = _editorMode;
@synthesize expenseFilterName = _expenseFilterName;
@synthesize deleteMEReturned = _deleteMEReturned;
@synthesize deletePCTReturned = _deletePCTReturned;
@synthesize deleteCCTReturned = _deleteCCTReturned;
@synthesize deleteRCReturned = _deleteRCReturned;
@synthesize requireRefresh;
@dynamic fetchedResultsController;

@synthesize currentAuthRefNo;

-(NSString *)getViewIDKey
{
    return QUICK_EXPENSES_AND_RECEIPTS_COMBO_VIEW;
}

-(void) setSeedDataAndShowReceiptsInitially:(BOOL)showReceiptsInitially allowSegmentSwitch:(BOOL)allowSegmentSwitch allowListEdit:(BOOL)allowListEdit
{
    self.currentSegmentIndex = (showReceiptsInitially ? 1 : 0);
    
    self.canSwitchSegments = allowSegmentSwitch;
    
    self.canEditList = allowListEdit;
    
    if (!self.canSwitchSegments)
    {
        if ([self isExpensesSegmentSelected])
            self.title = [Localizer getLocalizedText:@"Expenses"];
        else
            self.title = [Localizer getLocalizedText:@"Receipts"];
    }
    
    if (!showReceiptsInitially && !allowSegmentSwitch)
    {
        self.editorMode = EditorModeMultiSelect;
    } else {
        self.editorMode = EditorModeNormal;
    }
    self.selectedItems = [NSMutableDictionary dictionary];
    self.selectedSmartExpenses = [NSMutableArray array];
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate *) [UIApplication sharedApplication].delegate;
    self.managedObjectContext = ad.managedObjectContext;

    // MOB-16991 prevent crash, but this hides the wait view... :/
    // This is better than crashing, need to come back later.
    if (showReceiptsInitially && ([self.tableView numberOfSections] == 0 || [self.tableView numberOfRowsInSection:0] == 0)) {
    	[self loadReceipts];
    }
}

-(void) viewDidLoad
{
    [super viewDidLoad];
    
    self.tableView.allowsSelectionDuringEditing = YES;
    
    BOOL isEditMode = (self.editorMode == EditorModeMultiSelect || self.editorMode == EditorModeMultiSelectDelete);
    self.tableView.editing = isEditMode;
    
    if (self.canSwitchSegments)
    {
        [self makeSegmentControl];
        self.segmentControl.selectedSegmentIndex = self.currentSegmentIndex;
    }
    
    /*
     if ([ExSystem connectedToNetwork])
     {
     if ([self isExpensesSegmentSelected])
     [self loadExpenses];
     else
     [self reloadReceiptsWithLoadingView:YES];
     }
     */

    self.smartExpenseManager2 = [[SmartExpenseManager2 alloc] initWithContext:self.managedObjectContext];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(refreshExpenseView) name:@"SmartExpenseSplit" object:nil];

    // This should work here, but the navigation controller is nil when you come from the more menu.
    // just going to move it ot viewWillAppear
    //[self displayTestDriveOverlay];
    
    if ([[FeedbackManager sharedInstance] showRatingOnNextView] && ![Config isGov]) {
        [[FeedbackManager sharedInstance] requestRatingFromViewController:self withBlock:nil];
    }
}

-(void) dealloc
{
    // If you don't remove yourself as an observer, the Notification Center will continue to try and send notification objects to the deallocated object.
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

// reload the view when an expense has been split
-(void) refreshExpenseView
{
    [self.tableView reloadData];
    [self reloadExpensesWithLoadingView:NO];
    if(![Config isEreceiptsEnabled]){
        [self.smartExpenseManager2 mergeSmartExpenses];
     }
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    // The table view header needs to be updated (recreated) everytime the table view appears to ensure that it is up to date, e.g. shows the right number of queued items.
    [self makeTableHeaderView];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];

    // MOB-12986: Need not get data from server here, home already placed the request.
    // fetchedresultsController gets notified if there are any updates to EntityMobileEntry
    // Only refresh when new entry gets created from other views
    // reload tableview if flag is set.
    
    [self configureBars];

    // for the first view.  The navigation controller is nil during view did load if you come from the more menu.
    [self displayTestDriveOverlay];
    
    
    if ([ExSystem connectedToNetwork] && self.requireRefresh)
    {
        // refresh expense list to get latest smartExpenseID
        if ([self isExpensesSegmentSelected]) {
            [self reloadExpensesWithLoadingView:YES];
        }
        self.requireRefresh = NO;
    }

    // MOB-17433 - Add accessibility for the screen
    NSArray *items = self.navigationController.toolbar.items;
    for (UIBarButtonItem *barButton in items) {
        barButton.isAccessibilityElement = YES;
    }
    NSArray *navitems = self.navigationController.navigationBar.items;
    for (UIBarButtonItem *barButton in navitems) {
        barButton.isAccessibilityElement = YES;
    }
    self.segmentControl.isAccessibilityElement = YES;
}

/**
 Displays appropriate test drive help overlay
 */
- (void)displayTestDriveOverlay
{
    if ([self isExpensesSegmentSelected]) {
        if ([UIDevice isPad]) {
            [HelpOverlayFactory addiPadExpenseListOverlayToView:self.navigationController.view];
        } else {
            [HelpOverlayFactory addiPhoneExpenseListOverlayToView:self.navigationController.view];
        }
    } else {
        if ([UIDevice isPad]) {
            [HelpOverlayFactory addiPadReceiptListOverlayToView:self.navigationController.view];
        } else {
            [HelpOverlayFactory addiPhoneReceiptListOverlayToView:self.navigationController.view];
        }
    }
}

-(void) viewWillDisappear:(BOOL)animated
{
    if ([self isExpensesSegmentSelected])
        self.title = [Localizer getLocalizedText:@"Expenses"];
    else
        self.title = [Localizer getLocalizedText:@"Receipts"];
}


-(void) viewDidUnload
{
    self.segmentControl = nil;
}

#pragma mark - No Data View Methods

- (NSString*) titleForNoDataView
{
    if ([self isExpensesSegmentSelected])
    {
        if (self.expenseFilterName == nil)
            return [Localizer getLocalizedText:@"No Expenses"];
        else
            return [Localizer getLocalizedText:@"No Card Charges"];
    }
    else
    {
        return [Localizer getLocalizedText:@"NO_RECEIPTS_NEG"];
    }
}

- (NSString*) imageForNoDataView
{
    if ([self isExpensesSegmentSelected])
        return @"neg_quickexpense_icon";
    else
        return @"neg_receipt_icon";
}

#pragma mark - Mode Methods

-(void) transitionToMode:(EditorMode) newMode
{
    self.editorMode = newMode;
    
    // Clear out the selection
    [self clearSelection];
    
    // Set table editing mode
    BOOL isEditMode = (newMode == EditorModeMultiSelect || newMode == EditorModeMultiSelectDelete);
    
    if (self.isViewLoaded)
        self.tableView.editing = isEditMode;
    
    // Configure the nav and tool bars
    if (self.isViewLoaded)
        [self configureBars];
}

#pragma mark - Bar Methods
-(void) configureBars
{
    // hide if we are showing negative state
    if(self.baseViewState != VIEW_STATE_NEGATIVE)
    {
        // MOB-16898 - to stop bottom bar show and hide momentarily in receipts list screen.
        if ([self isExpensesSegmentSelected] && [ExSystem is6Plus]) {
            self.navigationController.toolbarHidden = NO;
        }
        else {
            self.navigationController.toolbarHidden = YES;
        }
        if (self.editorMode == EditorModeMultiSelect)
            [self configureBarsForEditorModeMultiSelect];
        else if (self.editorMode == EditorModeMultiSelectDelete)
            [self configureBarsForEditorModeMultiSelectDelete];
        else
            [self configureBarsForEditorModeNormal];
        if (self.editorMode == EditorModeMultiSelect)
            [self configureBarsForEditorModeMultiSelect];
        else if (self.editorMode == EditorModeMultiSelectDelete)
            [self configureBarsForEditorModeMultiSelectDelete];
        else
            [self configureBarsForEditorModeNormal];
    }
    else

    {
        self.navigationController.toolbarHidden = YES;
        // MOB-17002 : if there are no expenses then navbar might not have "+" action. so add the "+" button explicitly to the nav bar.
        SEL addAction = nil;
        if ([self isExpensesSegmentSelected])
            addAction = @selector(buttonAddExpensePressed:);
         else
             addAction = @selector(buttonAddReceiptPressed:);

        // Nav bar
        if (self.canEditList)
        {
            UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:addAction];
            self.navigationItem.rightBarButtonItem = btnAdd;
        }
    }
    
	if([UIDevice isPad])
	{
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonClosePressed:)];
		self.navigationItem.leftBarButtonItem = btnClose;
	}   
}

-(void) configureBarsForEditorModeNormal
{
    if ([self isExpensesSegmentSelected])
    {
        // Nav bar
        if (self.canEditList)
        {
            UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddExpensePressed:)];
            self.navigationItem.rightBarButtonItem = btnAdd;
        }
        
        // Tool bar
        NSArray *toolbarItems = nil;
        
        if ([ExSystem connectedToNetwork])
        {
            UIBarButtonItem *btnEdit = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Edit"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonEditPressed:)];
            UIBarButtonItem *btnAddToReport = [self makeAddToReportButton];
            UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(buttonActionPressed:)];
            UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
            toolbarItems = @[btnEdit, flexibleSpace, btnAction, flexibleSpace, btnAddToReport];
        }
        else
        {
            UIBarButtonItem *btnOffline = [self makeOfflineButton];
            UIBarButtonItem *btnAction = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAction target:self action:@selector(buttonActionPressed:)];
            UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
            toolbarItems = @[flexibleSpace, btnOffline,flexibleSpace, btnAction];
        }
        
        [self.navigationController.toolbar setItems:toolbarItems];
    }
    else // receipts selected
    {
        // Nav bar
        if (self.canEditList)
        {
            UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddReceiptPressed:)];
            self.navigationItem.rightBarButtonItem = btnAdd;
        }
        // clear bar, show empty bar
        self.navigationController.toolbar.items = nil;
        self.navigationController.toolbarHidden = YES;
    }
}

-(void) configureBarsForEditorModeMultiSelect
{
    // Nav bar
    if (self.canEditList)
    {
        UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonCancelPressed:)];
        self.navigationItem.rightBarButtonItem = btnCancel;
    }
    
    // Tool bar
    UIBarButtonItem *btnSelectAll = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Select All"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonSelectAllPressed:)];
    btnSelectAll.tag = SELECT_ALL_BUTTON_TAG;
    
    UIBarButtonItem *btnAddToReport = [self makeAddToReportButton];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSArray *toolbarItems = @[btnSelectAll, flexibleSpace, btnAddToReport];
    
    [self.navigationController.toolbar setItems:toolbarItems];
}

-(void) configureBarsForEditorModeMultiSelectDelete
{                      
    // Nav bar
    UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonCancelPressed:)];
    self.navigationItem.rightBarButtonItem = btnCancel;
    
    // Tool bar

    UIBarButtonItem *btnDelete = [self makeDeleteButton];
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSArray *toolbarItems = @[btnDelete, flexibleSpace, flexibleSpace];
    
    [self.navigationController.toolbar setItems:toolbarItems];
}

#pragma mark - Offline Button
-(UIBarButtonItem*)makeOfflineButton
{
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 30, 100, 30)];
	[lbl setTextColor:[UIColor orangeColor]];
	[lbl setText:[Localizer getLocalizedText:@"Offline"]];
	[lbl setFont:[UIFont boldSystemFontOfSize:17.0f]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setTextAlignment:NSTextAlignmentCenter];
	[lbl setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
	[lbl setShadowOffset:CGSizeMake(0.0f, -1.0f)];
	UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:lbl];
	return customBarItem;
}

#pragma mark - Receipt Last Updated Methods
-(void) showReceiptLastUpdated:(NSString*)lastUpdated
{
    int refreshDateWidth = 280;
	int refreshDateHeight = 20;
	int numberOfLines = 1;
    
    UIBarButtonItem *btnRefreshDate = nil;
    if (lastUpdated != nil)
    {
        UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
        UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, refreshDateWidth, refreshDateHeight)];
        lblText.numberOfLines = numberOfLines;
        lblText.lineBreakMode = NSLineBreakByWordWrapping;
        lblText.textAlignment = NSTextAlignmentLeft;
        lblText.text = lastUpdated;

        if( false == [ExSystem is7Plus] )
        {
            // iOS6 shows a black shadow behind white refresh text
            // iOS7 shows this as white text on white background so leave colors default
            [lblText setBackgroundColor:[UIColor clearColor]];
            [lblText setTextColor:[UIColor whiteColor]];
            [lblText setShadowColor:[UIColor blackColor]];
            [lblText setShadowOffset:CGSizeMake(0, -1)];
        }
        [lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
        [cv addSubview:lblText];
        
        btnRefreshDate = [[UIBarButtonItem alloc] initWithCustomView:cv];
    }
    
    UIBarButtonItem *refreshBtn = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(buttonRefreshReceiptListPressed:)];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
	NSArray *toolbarItems = nil;
    if ([ExSystem connectedToNetwork])
    {
        if (btnRefreshDate != nil)
        {
            toolbarItems =  @[btnRefreshDate,flexibleSpace,refreshBtn];
        }
        else
        {
            toolbarItems =  @[flexibleSpace,refreshBtn];
        }
    }
    else
    {
        UIBarButtonItem *btnOffline = [self makeOfflineButton];
        toolbarItems =  @[flexibleSpace, btnOffline, flexibleSpace];
    }
    
    [self.navigationController.toolbar setItems:toolbarItems];
}

-(void) buttonRefreshReceiptListPressed:(id)sender
{
    [self reloadReceiptsWithLoadingView:YES];
}

-(BOOL) refreshView:(UIRefreshControl*) refresh
{
    if(self.editorMode != EditorModeNormal) // Dont refresh if we are in edit mode.
        return YES;
    
    // Dont show loading view when pulldown refresh is done. 
    if ([self isExpensesSegmentSelected])
    {
        [self reloadExpensesWithLoadingView:NO];
        return NO;
    }
    else
    {
        [self reloadReceiptsWithLoadingView:NO];
        return NO;
    }
    
    return YES;
}

#pragma mark - Update Button Methods
-(void) updateButtons
{
    if (self.editorMode == EditorModeMultiSelect)
    {
        [self updateSelectAllButton];
        [self updateAddToReportButton];
    }
    else if (self.editorMode == EditorModeMultiSelectDelete)
    {
        [self updateDeleteButton];
    }
}

-(void) updateSelectAllButton
{
    NSArray *items = self.navigationController.toolbar.items;
    
    if (items == nil)
        return;
    
    for (UIBarButtonItem* item in items)
    {
        if (item.tag == SELECT_ALL_BUTTON_TAG)
        {
            BOOL areAllItemsSelected = (self.fetchedResultsController.fetchedObjects.count == self.selectedItems.count);
            item.title = [Localizer getLocalizedText:(areAllItemsSelected ? @"Unselect All" : @"Select All")];
            return;
        }
    }
}

-(void) updateAddToReportButton
{
    NSArray *oldToolBarItems = self.navigationController.toolbar.items;
    NSMutableArray *newToolBarItems = [NSMutableArray arrayWithArray:oldToolBarItems];
    
    if (newToolBarItems.count > 1)
        [newToolBarItems removeLastObject];
    
    [newToolBarItems addObject:[self makeAddToReportButton]];
    [self.navigationController.toolbar setItems:newToolBarItems];
}

-(void) updateDeleteButton
{
    NSArray *oldToolBarItems = self.navigationController.toolbar.items;
    NSMutableArray *newToolBarItems = [NSMutableArray arrayWithArray:oldToolBarItems];
    
    if (newToolBarItems.count > 1)
        [newToolBarItems removeObjectAtIndex:0];
    
    [newToolBarItems insertObject:[self makeDeleteButton] atIndex:0];
    [self.navigationController.toolbar setItems:newToolBarItems];
}

#pragma mark - Add to Report Button
-(void) buttonAddToReportPressed:(id)sender
{
	//MOB-16758
    [MobileActionSheet dismissAllMobileActionSheets];
    
    if (self.editorMode != EditorModeMultiSelect)
        [self transitionToMode:EditorModeMultiSelect];
    else
    {
        if (self.selectedItems.count == 0)
            return;
        
        int undefinedCount = 0;
        int noReceiptCount = 0;
        BOOL hasCards = NO;
        
        // Gather stats for flurry
        for (NSString *key in self.selectedItems.allKeys)
        {
            EntityMobileEntry *entry = [[MobileEntryManager sharedInstance] fetchByKey:key];
            BOOL hasReceipt = (entry.receiptImageId == nil && entry.localReceiptImageId == nil ? @"N" : @"Y");
            if (!hasReceipt)
                noReceiptCount ++;
            
            if ([entry.expKey isEqualToString:@"UNDEF"])
                undefinedCount ++;
            
            // MOB-12986 : check for cctkey or pcaKey != nil for checkin if it is a card transaction
            if([MobileEntryManager isCardTransaction:entry])
                hasCards = YES;
            //
            // Google Analytics
            if([MobileEntryManager isEreceipt:entry])
                [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"Add To Report" eventLabel:@"E-Receipt" eventValue:nil];
        }
        // If there were card transactions then refresh the home
        if ( hasCards )
        {
            [self refreshHome];
        }
        
        [self goToSelectReport];
        
        // Write stats for flurry
        // MOB-11978
        NSString *camefromVC = self.cameFrom;
        if([camefromVC isEqualToString:@"Report"])
        {
            camefromVC = @"Report Header";
        }
        else
        {
            camefromVC = @"Expense List";
        }
        
        NSDictionary *dictionary = @{@"How many added": [NSString stringWithFormat:@"%lu", (unsigned long)[self.selectedItems count]], @"Came From": camefromVC, @"Has Credit Cards": (hasCards?@"Yes":@"No"), @"Has Receipts": (noReceiptCount >0? @"Yes":@"No")};
        [Flurry logEvent:@"All Mobile Expenses: Add to Report" withParameters:dictionary];
    }
}

- (void) goToSelectReport
{
	// collect all selected meKeys
	NSMutableArray *meKeys = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray *pctKeys = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray *cctKeys = [[NSMutableArray alloc] initWithObjects:nil];
    NSMutableArray *rcKeys = [[NSMutableArray alloc] initWithObjects:nil];
    NSMutableArray *eReceiptKeys = [[NSMutableArray alloc] initWithObjects:nil];
    NSMutableArray *smartExpenseIds = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableDictionary *meAtnMap = [[NSMutableDictionary alloc] init];
	
    for (NSString *key in self.selectedItems.allKeys)
	{
        // MOB-13656 - Dictionary of keys contain the key type also
        NSString *keytype = (NSString *)(self.selectedItems)[key];
        if([keytype isEqualToString:CCT_TYPE])
        {
            [cctKeys addObject:key];
        }
        else if( [keytype isEqualToString:PCT_TYPE])
        {
            [pctKeys addObject:key];
        }
        else if ([keytype isEqualToString:RC_TYPE])
        {
            [rcKeys addObject:key];
        }
        else if ([keytype isEqualToString:E_RECEIPT_TYPE])
        {
            [eReceiptKeys addObject:key];
        }
        else if ([keytype isEqualToString:SMART_EXPENSE_ID])
        {
            [smartExpenseIds addObject:key];
        }

        else
        {
            [meKeys addObject:key];
        }
	}
	
	if (self.reportToWhichToAddExpenses != nil)
	{
        if ([Config isEreceiptsEnabled]) {
            [self addToReport:smartExpenseIds];
            return;
        }
        
		// MOB-14945 : ExpenseIt entry doesn't add from report
		// send rcKeys also when adding to report
		[self addToReport:meKeys pctKeys:pctKeys cctKeys:cctKeys rcKeys:rcKeys atnMap:meAtnMap smartExpenseList:self.selectedSmartExpenses];
		return;
	}
    
	//takes you to the select report view
	SelectReportViewController * pVC = [[SelectReportViewController alloc] initWithNibName:@"SelectReportViewController" bundle:nil];
	pVC.meKeys = meKeys;
	pVC.pctKeys = pctKeys;
	pVC.cctKeys = cctKeys;
    pVC.rcKeys = rcKeys;
	pVC.meAtnMap = meAtnMap;
    pVC.smartExpenseIds = smartExpenseIds;
    pVC.smartExpenseList = self.selectedSmartExpenses;
	//pVC.parentMVC = self;

    [self.navigationController pushViewController:pVC animated:YES];
}

// MOB-14945 : ExpenseIt entry doesn't add from report
// send rcKeys also when adding to report
-(void) addToReport:(NSMutableArray*) meKeys pctKeys:(NSArray*) pctKeys cctKeys:(NSArray*) cctKeys rcKeys:(NSArray *)rcKeys atnMap:(NSDictionary*) meAtnMap smartExpenseList:(NSArray *)smartExpenseList
{
	[self showWaitView];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 SELECT_REPORT, @"TO_VIEW",
								 meKeys, @"ME_KEYS",
								 nil];
	if (pctKeys != nil)
		pBag[@"PCT_KEYS"] = pctKeys;
	
	if (cctKeys != nil)
		pBag[@"CCT_KEYS"] = cctKeys;
	
    if([rcKeys count] > 0)
        pBag[@"RC_KEYS"] = rcKeys;
    
	if (self.reportToWhichToAddExpenses.rptKey != nil)
		pBag[@"RPT_KEY"] = self.reportToWhichToAddExpenses.rptKey;
	
	if (meAtnMap != nil)
		pBag[@"ME_ATN_MAP"] = meAtnMap;

    if (smartExpenseList != nil) {
        pBag[@"SmartExpenseList"] = smartExpenseList;
    }
	
	[[ExSystem sharedInstance].msgControl createMsg:ADD_TO_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    //[self refreshHome];
}

/*
 * Add To Report using new End point of AddToRepoortV5, only SmartExpenseIDs inclue
 */
-(void) addToReport:(NSArray*)smartExpenseIds
{
    [self showWaitView];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 SELECT_REPORT, @"TO_VIEW",
                                 smartExpenseIds, @"SmartExpenseIds",
                                 nil];
    if (self.reportToWhichToAddExpenses.rptKey != nil)
        pBag[@"RPT_KEY"] = self.reportToWhichToAddExpenses.rptKey;
    
    [[ExSystem sharedInstance].msgControl createMsg:ADD_TO_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(UIBarButtonItem*) makeAddToReportButton
{
    if (![[ExSystem sharedInstance] siteSettingAllowsExpenseReports])
    {
        UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
        return flexibleSpace;
    }
    
    NSString *text = [Localizer getLocalizedText:@"Add to Report"];
    
    NSUInteger count = self.selectedItems.count;
    if(count > 0)
        text = [NSString stringWithFormat:@"%@ (%lu)", text, (unsigned long)count];
    
    CGSize textSize = [text sizeWithFont:[UIFont boldSystemFontOfSize:12]];
    
    CGFloat w = 150.0;
    CGFloat h = 30.0;
    
    if((textSize.width + 20) < w)
        w = textSize.width + 20;
    
    NSString* buttonColor = (count > 0 ? @"BLUE" : @"BLUE_INACTIVE");
    
    if ([ExSystem is7Plus])
    {
        return [[UIBarButtonItem alloc] initWithTitle:text style:UIBarButtonItemStylePlain target:self action:@selector(buttonAddToReportPressed:)];
    }
    else
        return [ExSystem makeColoredButton:buttonColor W:w H:h Text:text SelectorString:@"buttonAddToReportPressed:" MobileVC:(MobileViewController*)self]; // Ok, it's not really a MobileViewController, but it doesn't need to be.  Cast eliminates compiler warning.
}

#pragma mark - Select All Button
-(void) buttonSelectAllPressed:(id)sender
{
    NSArray *allItems = self.fetchedResultsController.fetchedObjects;
    
    BOOL wereAllItemsSelected = (allItems.count == self.selectedItems.count);
    if (wereAllItemsSelected)
    {
        [self.selectedItems removeAllObjects];
    }
    else
    {
        for (EntityMobileEntry * item in allItems)
        {
            [self addToSelectionMobileEntryKey:item];
        }
    }
    
    [self updateButtons];
    [self.tableView reloadData];
}

#pragma mark - Cancel Button
-(void) buttonCancelPressed:(id)sender
{
    [self transitionToMode:EditorModeNormal];
}

#pragma mark - Edit Button
-(void) buttonEditPressed:(id)sender
{
	// MOB-16758
    [MobileActionSheet dismissAllMobileActionSheets];

    [self transitionToMode:EditorModeMultiSelectDelete];
}

#pragma mark - Delete Button
-(void) buttonDeletePressed:(id)sender
{
    if (self.selectedItems.count == 0)
        return;
    
	SmartExpenseManager *smartExpenseManager = [SmartExpenseManager getInstance];
	
	NSMutableDictionary *killMEKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	NSMutableDictionary *killPCTKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	NSMutableDictionary *killCCTKeys = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    
	for (NSString *key in self.selectedItems)
	{

        NSString *keytype = (NSString *)(self.selectedItems)[key];
        if ([Config isEreceiptsEnabled]) {
            EntityMobileEntry *entry = [[MobileEntryManager sharedInstance] fetchBySmartExpenseId:key];
            //Assumption: we will not get credit card or expenseit or ereceipt keys since UI wont let them delete in first place
            NSString *meKey = [MobileEntryManager getKey:entry];
            killMEKeys[meKey] = meKey;
        }
        else
        {
            //MOB-13678 - 
            if([keytype isEqualToString:CCT_TYPE])
            {
                killCCTKeys[key] = key;
                if ([smartExpenseManager isSmartExpenseCctKey:key])
                {
                    NSString *meKey = (smartExpenseManager.smartExpenseCctKeys)[key];
                    killMEKeys[meKey] = meKey;
                }
            }
            else if( [keytype isEqualToString:PCT_TYPE])
            {
                killPCTKeys[key] = key;
                if ([smartExpenseManager isSmartExpensePctKey:key])
                {
                    NSString *meKey = (smartExpenseManager.smartExpensePctKeys)[key];
                    killMEKeys[meKey] = meKey;
                }
            }
            else
            {
                killMEKeys[key] = key;
            }
        }
	}
	
	[self showWaitView];
    
   
	if ([killMEKeys count] > 0)
	{
		self.deleteMEReturned = FALSE;
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW", killMEKeys, @"KILL_KEYS", nil];
		[[ExSystem sharedInstance].msgControl createMsg:ME_DELETE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	if ([killPCTKeys count] > 0)
	{
		self.deletePCTReturned = FALSE;
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW",
									 killPCTKeys, @"KILL_KEYS", PCT_TYPE, @"TYPE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:ME_DELETE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
	if ([killCCTKeys count] > 0)
	{
		self.deleteCCTReturned = FALSE;
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_LIST, @"TO_VIEW",
									 killCCTKeys, @"KILL_KEYS", CCT_TYPE, @"TYPE", nil];
		[[ExSystem sharedInstance].msgControl createMsg:ME_DELETE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	}
    // Donot refresh home yet. refresh after we have delete confirmation
}

#pragma mark - Close Button
-(IBAction) buttonClosePressed:(id)sender
{
    [self closeMe];
}

-(void) refreshHome
{
    // Refresh homevc
    UIViewController *homeVC = [ConcurMobileAppDelegate findHomeVC];
    if ([homeVC respondsToSelector:@selector(refreshSummaryData)])
    {
        [homeVC performSelector:@selector(refreshSummaryData) withObject:nil];
    }
}
           
#pragma mark - Action Button
-(void) buttonActionPressed:(id)sender
{
    [MobileActionSheet dismissAllMobileActionSheets];
    
    self.actionSheetItems = [NSMutableArray array];
    
	[self.actionSheetItems addObject:[Localizer getLocalizedText:@"All Expenses"]];

    NSArray* personalCardEntries = [[MobileEntryManager sharedInstance] fetchAllPersonalCardNames];
    
    if (personalCardEntries != nil && [personalCardEntries count] > 0)
        [self.actionSheetItems addObjectsFromArray:personalCardEntries];
    
    NSArray *allCorpCardEntries = [[MobileEntryManager sharedInstance] fetchAllCorporateCardMobileEntries];
    
	if (allCorpCardEntries != nil && [allCorpCardEntries count] > 0)
		[self.actionSheetItems addObject:[Localizer getLocalizedText:@"Corporate Card"]];
    
   
    NSArray *allExpenseItEntries = [[MobileEntryManager sharedInstance] fetchAllExpenseItEntries];
    if (allExpenseItEntries != nil && [allExpenseItEntries count] > 0)
        [self.actionSheetItems addObject:[Localizer getLocalizedText:@"ExpenseIt Entries"]];
    
    if (self.expenseSorterName == nil || [self.expenseSorterName isEqualToString:DATE_SORTER])
        [self.actionSheetItems addObject:[Localizer getLocalizedText:@"Sort by Amount"]];
    else if ([self.expenseSorterName isEqualToString:AMOUNT_SORTER])
        [self.actionSheetItems addObject:[Localizer getLocalizedText:@"Sort by Date"]];
    
	UIActionSheet * actionSheet = [[MobileActionSheet alloc] initWithTitle:nil
                                                                  delegate:self
                                                         cancelButtonTitle:nil
                                                    destructiveButtonTitle:nil
                                                         otherButtonTitles:nil, nil];
    
    for (NSString *item in self.actionSheetItems)
    {
        [actionSheet addButtonWithTitle:item];
    }
    
    [actionSheet addButtonWithTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]];
    actionSheet.cancelButtonIndex = self.actionSheetItems.count;
    
	
	if([UIDevice isPad])
		[actionSheet showFromBarButtonItem:sender animated:YES];
	else
	{
		actionSheet.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
		[actionSheet showFromToolbar:self.navigationController.toolbar];
	}
}

-(UIBarButtonItem*) makeDeleteButton
{
    NSUInteger numItemsToDelete = self.selectedItems.count;
    
    const int buttonWidth = 100;
    NSString *buttonColor = (numItemsToDelete == 0 ? @"DELETE_INACTIVE" : @"DELETE");
    UIButton *button = [ExSystem makeColoredButtonRegular:buttonColor W:buttonWidth H:30 Text:@"" SelectorString:@"buttonDeletePressed:" MobileVC:(MobileViewController*)self];
	button.frame = CGRectMake(0, 0, buttonWidth, 30);
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(21, 0, buttonWidth - 21, 30)];
    
	lbl.font = [UIFont boldSystemFontOfSize:12];
	lbl.textColor = [UIColor colorWithRed:243/255.0 green:228/255.0 blue:229/255.0 alpha:1.0f];
	lbl.shadowColor = [UIColor lightGrayColor];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
    
	NSString *delText = [Localizer getLocalizedText:@"Delete"];
	if (numItemsToDelete > 0)
	{
		lbl.textColor =  [UIColor whiteColor];
		lbl.shadowColor = [UIColor darkGrayColor];
		delText = [NSString stringWithFormat:@"%@ (%lu)", delText, (unsigned long)numItemsToDelete];
	}
	lbl.text = delText;
	
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, 30)];
	[v addSubview:button];
	[v addSubview:lbl];

    if ([ExSystem is7Plus])
    {
        return [[UIBarButtonItem alloc] initWithTitle:delText style:UIBarButtonItemStylePlain target:self action:@selector(buttonDeletePressed:)];
    }
    else
        return [[UIBarButtonItem alloc] initWithCustomView:v];
}

#pragma mark - Add Buttons
-(void) buttonAddExpensePressed:(id)sender
{
    QEFormVC *formVC = [[QEFormVC alloc] initWithEntryOrNil:nil];
    [self.navigationController pushViewController:formVC animated:YES];
    NSDictionary *dict = @{@"Came From": @"ExpenseList"};
    [Flurry logEvent:@"Mobile Entry: Create2" withParameters:dict];
}

-(void) buttonAddReceiptPressed:(id)sender
{
    if (rsuHelper == nil)
    {
        rsuHelper = [[ReceiptStoreUploadHelper alloc] init];
        rsuHelper.vc = self;
    }
    rsuHelper.isFromReceiptStore = TRUE;
    if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera] && [UIDevice isPad])
    {
        [self dismissViewControllerAnimated:NO completion:nil];
        UIViewController *homevc = [ConcurMobileAppDelegate findHomeVC];
        if ([homevc respondsToSelector:@selector(showPhotoAlbum)]){
            [homevc performSelector:@selector(showPhotoAlbum) withObject:nil];
        }
    }
    else
        [rsuHelper startCamera:self.navigationItem.rightBarButtonItem];
//    [self showActionSheet];
}

#pragma mark - Segment Methods
-(void) makeSegmentControl
{
    NSArray *segmentItems = @[[Localizer getLocalizedText:@"Expenses"], [Localizer getLocalizedText:@"Receipts"]];
    
    self.segmentControl = [[UISegmentedControl alloc] initWithItems:segmentItems];
    [self.segmentControl addTarget:self action:@selector(segmentSelected:) forControlEvents:UIControlEventValueChanged];
    self.segmentControl.selectedSegmentIndex = 0;
    self.segmentControl.segmentedControlStyle = UISegmentedControlStyleBar;
    
    self.navigationItem.titleView = self.segmentControl;
}

-(void) segmentSelected:(id)sender
{
    self.currentSegmentIndex = self.segmentControl.selectedSegmentIndex;
    
    // If a loading view is showing, then hide it
    [self hideLoadingView];
    
    // Clear fetchedResultsController so that a new one will be created when transitionToMode calls clearSelection which calls reloadData which calls fetchedResultsController
    self.fetchedResultsController = nil;
    [self transitionToMode:EditorModeNormal];
    
    [self makeTableHeaderView];
    [self.tableView scrollRectToVisible:CGRectMake(0, 0, 1, 1) animated:YES];
    
    if ([self.tableView numberOfSections] == 0 || [self.tableView numberOfRowsInSection:0] == 0)
    {
        if (![self isExpensesSegmentSelected])
            [self loadReceipts];
    }

    [self configureBars];

    // for the first time switching to the other segment
    [self displayTestDriveOverlay];
}

-(BOOL) isExpensesSegmentSelected
{
    NSUInteger segmentIndex = self.currentSegmentIndex;
    return (segmentIndex == 0);
}

#pragma mark - NSFetchedResultsController
- (NSFetchedResultsController *)fetchedResultsController
{
    if (_fetchedResultsController == nil)
    {
        NSEntityDescription *entityDescription = nil;
        NSPredicate *predicate = nil;
        if ([self isExpensesSegmentSelected])
        {
            entityDescription = [NSEntityDescription entityForName:@"EntityMobileEntry" inManagedObjectContext:self.managedObjectContext];

            // if there is no filter get all the entries
            // key == nil when expense is created offline.
            // Offline expenses donot show up in this list.
            //MOB-13680 - entry.key always stores mekey only
            if (self.expenseFilterName == nil)
            {
            	// Get every entry
                predicate = [NSPredicate predicateWithFormat:@"((key != nil or cctKey != nil or pctKey != nil or rcKey !=nil or ereceiptId != nil) && isHidden == NO)"];
            }
            else if ([self.expenseFilterName isEqualToString:CORPORATE_FILTER])
            {
                predicate = [NSPredicate predicateWithFormat:@"((cctKey != nil) && isHidden == NO)"];
            }
            else if ([self.expenseFilterName isEqualToString:EXPENSEIT_FILTER])
            {
                predicate = [NSPredicate predicateWithFormat:@"(rcKey != nil)"];
            }
            else // Personal card
            {
               predicate = [NSPredicate predicateWithFormat:@"((pctKey != nil) && isHidden == NO)"];
            }
        }
        else
        {
            entityDescription = [NSEntityDescription entityForName:@"EntityReceiptInfo" inManagedObjectContext:self.managedObjectContext];
        }
        
        NSFetchRequest *request = [[NSFetchRequest alloc] init];
        [request setEntity:entityDescription];
        
        if (predicate != nil) {
            [request setPredicate:predicate];
        }
        
        // Descending order by transaction date, expense title, and amount
        NSString *sortKey = ([self isExpensesSegmentSelected] ? @"transactionDate" : @"dateCreated");
        NSSortDescriptor *sortDate = [[NSSortDescriptor alloc] initWithKey:sortKey ascending:NO];
        
        if ([self isExpensesSegmentSelected])
        {
            NSSortDescriptor *sortTitle= [[NSSortDescriptor alloc] initWithKey:@"expName" ascending:YES];
            NSSortDescriptor *sortAmount = [[NSSortDescriptor alloc] initWithKey:@"transactionAmount" ascending:NO];
            if (self.expenseSorterName == nil || [self.expenseSorterName isEqualToString:DATE_SORTER])
            {
                [request setSortDescriptors:[NSArray arrayWithObjects:sortDate, sortTitle, sortAmount, nil]];
            }
            else if ([self.expenseSorterName isEqualToString:AMOUNT_SORTER])
            {
                [request setSortDescriptors:[NSArray arrayWithObjects:sortAmount,sortDate,sortTitle, nil]];
            }
        }
        else  
        {
            NSSortDescriptor *imgID= [[NSSortDescriptor alloc] initWithKey:@"receiptId" ascending:YES];
            [request setSortDescriptors:[NSArray arrayWithObjects:sortDate, imgID, nil]];
        }
        
        NSString *sectionNameKeyPath = ([self isExpensesSegmentSelected] ? nil : @"dateCreated.dateGroupByMonth");
        _fetchedResultsController = [[NSFetchedResultsController alloc]
                                     initWithFetchRequest:request
                                     managedObjectContext:self.managedObjectContext
                                     sectionNameKeyPath:sectionNameKeyPath
                                     cacheName:nil];
        _fetchedResultsController.delegate = self;
        
        NSError *error;
        if (![_fetchedResultsController performFetch:&error])
        {
            // Update to handle the error appropriately.
            [[MCLogging getInstance] log:[NSString stringWithFormat:@"QuickExpensesReceiptStoreVC: fetchedResults %@, %@", error, [error userInfo]] Level:MC_LOG_DEBU];
        }
        // Do not need smart matching with GetSmartExpneses endpoint
        else {
            // on success try to merge Smart Expenses
            if ([self isExpensesSegmentSelected] && ![Config isEreceiptsEnabled]) {
                [self.smartExpenseManager2 mergeSmartExpenses];
            }
        }
    }
    
    return _fetchedResultsController;
}

-(void)setFetchedResultsController:(NSFetchedResultsController*)newValue
{
	_fetchedResultsController = newValue;
}

#pragma mark - NSFetchedResultsControllerDelegate
- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableView reloadData];
}

#pragma mark - Table view data source
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    NSArray *sections = self.fetchedResultsController.sections;
    if (sections == nil || section >= sections.count)
        return nil;
    
    NSString *sectionName = [sections[section] name];
    return sectionName;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    NSUInteger numSections = [[self.fetchedResultsController sections] count];
    
    id <NSFetchedResultsSectionInfo> sectionInfo = nil;
    if (numSections > 0)
        sectionInfo = [self.fetchedResultsController sections][0];
    
    if (sectionInfo == nil || [[self.fetchedResultsController sections][0] numberOfObjects] == 0)
        [self showNewNoDataView:self];
    else
        [self hideNoDataView];
    
    return numSections;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    return [sectionInfo numberOfObjects];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return [self tableView:tableView configureCellForRowAtIndexPath:indexPath];
}

- (UITableViewCell *)tableView:(UITableView *)tableView configureCellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    id object = [self.fetchedResultsController objectAtIndexPath:indexPath];
    if ([object isKindOfClass:[EntityMobileEntry class]])
        return [self tableView:tableView configureCellForMobileEntry:(EntityMobileEntry*)object];
    else if ([object isKindOfClass:[EntityReceiptInfo class]])
        return [self tableView:tableView configureCellForReceipt:(EntityReceiptInfo*)object];
    
    // condition should not be reached
    return nil;
}

// MULTI
- (UITableViewCell*)tableView:(UITableView *)tableView configureCellForMobileEntry:(EntityMobileEntry *)mobileEntry
{
    static NSString *cellIdentity = @"QEEntryCell";
    QEEntryCell *cell = (QEEntryCell *)[tableView dequeueReusableCellWithIdentifier: cellIdentity];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:cellIdentity owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[QEEntryCell class]])
                cell = (QEEntryCell *)oneObject;
    }
    
    cell.relatedTableView = tableView;
    
    cell.ivIcon2.hidden = YES;
    cell.ivIcon1.hidden = YES;
    
    // Correct possible gray to black color
    cell.lblHeading.textColor = [UIColor blackColor];
    cell.lblAmount.textColor = [UIColor blackColor];

    // If it's a credit card
    BOOL isCardTransaction = [MobileEntryManager isCardTransaction:mobileEntry];
    if( isCardTransaction)
    {
        cell.lblSub2.text = mobileEntry.cardName;
        cell.ivIcon2.hidden = NO;
        if ([MobileEntryManager isCardAuthorizationTransaction:mobileEntry])
        {
            cell.ivIcon2.image = [UIImage imageNamed: @"icon_card_19"];
            cell.lblHeading.textColor = [UIColor grayColor];
            cell.lblAmount.textColor = [UIColor grayColor];
        }
        else
            cell.ivIcon2.image = [UIImage imageNamed: @"icon_card_19"];
	}
    
    cell.lblHeading.text = mobileEntry.expName;

    cell.lblSub1.text = [CCDateUtilities formatDateMediumByDate:mobileEntry.transactionDate];
	
	cell.lblAmount.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", [mobileEntry.transactionAmount doubleValue]] crnCode:mobileEntry.crnCode];
	if (mobileEntry.vendorName != nil)
	{
		if(mobileEntry.locationName != nil)
			cell.lblSub2.text = [NSString stringWithFormat:@"%@ - %@", mobileEntry.vendorName, mobileEntry.locationName];
		else
			cell.lblSub2.text = mobileEntry.vendorName;
	}
	else
	{
		cell.lblSub2.text = mobileEntry.locationName;
	}
	
    // MOB-12986 : check for entry.hasRecieptImage or receiptimageId != nil
    if ([mobileEntry.hasReceipt boolValue])
    {
        if([mobileEntry.mobileReceiptImageId length] || [mobileEntry.ereceiptId length] || [mobileEntry.cctReceiptImageId length] || [mobileEntry.receiptImageId length])
        {
            /* MOB-5783
             OK, I'm using the icon_receipt_button icon, not the icon_receipt_19 image.*/
            
            if(isCardTransaction)
            {
                cell.ivIcon1.hidden = NO;
                cell.ivIcon1.image = [UIImage imageNamed: @"icon_receipt_19"];
            }
            else
            {
                cell.ivIcon2.hidden = NO;
                cell.ivIcon2.image = [UIImage imageNamed: @"icon_receipt_19"];
            }
        }
    }
    
    [self tableView:tableView updateCell:cell mobileEntry:mobileEntry];
    
    //
    // Google Analytics
    [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"Expense List" eventLabel:@"All Expenses" eventValue:nil];
    
    if([MobileEntryManager isEreceipt:mobileEntry]){
        [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"Expense List" eventLabel:@"E-Receipt" eventValue:nil];
    }
    if([MobileEntryManager isSmartMatched:mobileEntry]){
        [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"Expense List" eventLabel:@"SmartMatched Expense" eventValue:nil];
    }
    if([MobileEntryManager isSmartMatchedEReceipt:mobileEntry]){
        [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"Expense List" eventLabel:@"SmartMatched Expense E-Receipt" eventValue:nil];
    }
    if([MobileEntryManager isCorporateCardTransaction:mobileEntry]){
        [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"Expense List" eventLabel:@"Corportate Credit Card Expense" eventValue:nil];
    }
	return cell;
}

-(void) tableView:(UITableView *)tableView updateCell:(UITableViewCell*)cellToUpdate mobileEntry:(EntityMobileEntry *)mobileEntry
{
    // Only expenses can be multiple selected
    if (![cellToUpdate isKindOfClass:[QEEntryCell class]])
        return;
    
    QEEntryCell *cell = (QEEntryCell*)cellToUpdate;
    
    BOOL isRowSelected = [self isSelectedMobileEntryKey:mobileEntry];
    BOOL deleting = (self.editorMode == EditorModeMultiSelectDelete);
    
    [cell updateAppearanceWithSelection:isRowSelected editing:tableView.editing deleting:deleting];
}

-(UITableViewCell*)tableView:(UITableView *)tableView configureCellForReceipt:(EntityReceiptInfo*)receipt
{
    static NSString *CellIdentifier = @"ReceiptStoreListCell";
	
	ReceiptStoreListCell *cell = (ReceiptStoreListCell *)[tableView dequeueReusableCellWithIdentifier:CellIdentifier];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ReceiptStoreListCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[ReceiptStoreListCell class]])
				cell = (ReceiptStoreListCell *)oneObject;
	}
    // MOB-21462: can get thumb mail image from server, so do not need the pdf webview for pdf receipt
    [cell.pdfWebView setHidden:YES];
    cell.receiptId = receipt.receiptId;
	
	[cell.imageBackgroundView setIsRoundingDisabled:YES];
    
    [cell.activityView stopAnimating];
    [cell.activityView setHidden:YES];
    
    NSString *dataType = nil;
    NSData *cachedImageData = [[ReceiptCache sharedInstance] getThumbNailReceiptForId:receipt.receiptId dataType:&dataType];
    if (cachedImageData != nil)
    {
        // Show the image obtained from the cache
        UIImage *cachedThumbNailImage = [UIImage imageWithData:cachedImageData];
        [cell.activityView stopAnimating];
        [cell.activityView setHidden:YES];
        [cell.thumbImageView setImage:cachedThumbNailImage];
    }
    else if (receipt.thumbUrl != nil)
    {
        // Show that we are downloading the image
        [cell.thumbImageView setImage:[UIImage imageNamed:@"receipt_store_loading"]];
        [cell.activityView startAnimating];
        [cell.activityView setHidden:NO];
        
        BOOL attemptingDownload = [ReceiptDownloader downloadReceiptForId:receipt.receiptId dataType:receipt.imageType thumbNail:YES url:receipt.thumbUrl delegate:self];
        if (!attemptingDownload)
            [self showPlaceHolderImageInCell:cell];
    }
    else
    {
        [self showPlaceHolderImageInCell:cell];
    }

    // MOB-17329: Missing date if region set to UK and 12 hr format
    NSString* imgDayString = [CCDateUtilities formatDateToEEEMonthDayYear:receipt.dateCreated];
    NSString *imgHourString = [CCDateUtilities formatDateToTime:receipt.dateCreated];

	[cell.tagLbl setText:imgDayString];
	[cell.imageDateLbl setText:imgHourString];
    
    [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    return cell;
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ([self isExpensesSegmentSelected])
        return UITableViewCellEditingStyleNone; // Use custom multiselect-deletion code for expenses
    else
        return UITableViewCellEditingStyleDelete; // Swipe to delete receipts
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle != UITableViewCellEditingStyleDelete)
        return;
    
    id object = [self.fetchedResultsController objectAtIndexPath:indexPath];
    if ([object isKindOfClass:[EntityReceiptInfo class]])
    {
		[self showWaitViewWithText:[Localizer getLocalizedText:@"Deleting Receipt"]];
        
        EntityReceiptInfo *receiptInfo = (EntityReceiptInfo*)object;
		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: receiptInfo.receiptId, @"ReceiptImageId", nil];
		[[ExSystem sharedInstance].msgControl createMsg:DELETE_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

#pragma mark - Table view delegate
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 74;      //Height for one row in OutOfPocketListVC
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    id item = [self.fetchedResultsController objectAtIndexPath:indexPath];
    if ([item isKindOfClass:[EntityMobileEntry class]])
    {
        EntityMobileEntry* entry = (EntityMobileEntry*)item;
        
        // MOB-21279: somehow we have a new property of mobileReceiptImageId which is a duplicate of receiptImageId.
        // sometimes receiptImageId is nil and mobileReceiptImageId has something
        [self checkForReceiptImageId:entry];
        if (self.editorMode == EditorModeMultiSelect || self.editorMode == EditorModeMultiSelectDelete)
        {
            // check if they are card entries only for delete mode
            if (self.editorMode == EditorModeMultiSelectDelete && [self disallowDeleteMobile:entry])
            {
                //MOB-13674
                // Ideally we should check this flag. however as per Prashanth's guidance we will not allow user to hide credit car transactions. 
//                BOOL canDeleteCardTran = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"ALLOW_TRANS_DELETE" withType:@"CARD"]];
//                if (!canDeleteCardTran)
//                {
                // Show alert message
                NSString *msgBody = nil;
                if ([MobileEntryManager isCardTransaction:entry])
                    msgBody = [Localizer getLocalizedText:@"DISALLOW_TRANS_DELETE"];
                else if ([MobileEntryManager isReceiptCapture:entry])
                    msgBody = [Localizer getLocalizedText:@"DISALLOW_EXPENSEIT_DELETE"];
                else if ([MobileEntryManager isEreceipt:entry])
                    msgBody = [Localizer getLocalizedText:@"Removal of E-Receipt items is not supported on mobile"];
                UIAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:[Localizer getLocalizedText:@"ERROR"]
                                      message:msgBody
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
                [alert show];


                return;
//                }
            }

            if ([self isSelectedMobileEntryKey:entry])
                [self removeFromSelectionMobileEntryKey:entry];
            else
                [self addToSelectionMobileEntryKey:entry];
            
            UITableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
            [self tableView:tableView updateCell:cell mobileEntry:item];
        }
        else
        {
            NSString *flurryStr = nil;
            if (entry.rcKey != nil)
                flurryStr = @"ExpenseIt";
            else if (entry.cctKey != nil)
                flurryStr = @"Corporate Card";
            else if (entry.pctKey != nil)
                flurryStr = @"Personal Card";
            else if(entry.ereceiptId != nil)
                 flurryStr = @"E-Receipt";    
            else
                flurryStr = @"Cash";
                
            NSDictionary *dict = @{@"Type": flurryStr};
            [Flurry logEvent:@"Quick Expense: View Expense" withParameters:dict];
            
            [AnalyticsTracker logEventWithCategory:@"All Mobile Expenses" eventAction:@"View Receipt details" eventLabel:flurryStr eventValue:nil];
            
            [self showQuickExpenseEditor:entry];
        }
    }
    else if ([item isKindOfClass:[EntityReceiptInfo class]])
    {
        EntityReceiptInfo* receipt = (EntityReceiptInfo*)item;
        ReceiptStoreReceipt *receiptInfo = [receipt makeReceiptStoreReceipt];
        if (receiptInfo.imageUrl != nil)
        {
            // Navigation logic may go here. Create and push another view controller.
            ReceiptStoreDetailViewController *rsDetailViewController = [[ReceiptStoreDetailViewController alloc] initWithNibName:@"ReceiptStoreDetailViewController" bundle:nil];
            
            rsDetailViewController.title  = [Localizer getLocalizedText:@"Receipt"];
            rsDetailViewController.receiptData = receiptInfo;
            [self.navigationController pushViewController:rsDetailViewController animated:YES];
            self.navigationController.toolbarHidden = YES;
        }
    }
}

-(void)checkForReceiptImageId:(EntityMobileEntry*)entry
{
    if (![entry.receiptImageId length]) {
        if ([entry.mobileReceiptImageId length]) {
             entry.receiptImageId = entry.mobileReceiptImageId;
        }
        else if ([MobileEntryManager isCardTransaction:entry]){
            if ([entry cctReceiptImageId]) {
                entry.receiptImageId = entry.cctReceiptImageId;
            }
        }
        else if ([MobileEntryManager isEreceipt:entry]){
            if (([entry.eReceiptImageId length])) {
                entry.receiptImageId = entry.eReceiptImageId;
            }
        }
    }
}

- (BOOL) disallowDeleteMobile:(EntityMobileEntry*) entry
{
    return ([MobileEntryManager isCardTransaction:entry] || [MobileEntryManager isReceiptCapture:entry] || [MobileEntryManager isEreceipt:entry]);
}

-(void)makeTableHeaderView
{
    UploadBannerView *uploadBannerView = nil;
    UIView *receiptHeaderView = nil;
    
    int numberOfQueuedItems = [[UploadQueue sharedInstance] visibleQueuedItemCount];
    if (numberOfQueuedItems > 0)
    {
        uploadBannerView = [UploadBannerView getUploadView];
        [uploadBannerView setBannerText:numberOfQueuedItems];
        uploadBannerView.delegate = self;
    }
    
    if (![self isExpensesSegmentSelected])
    {
        receiptHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 81)];
        [receiptHeaderView setBackgroundColor:[ExSystem getBaseBackgroundColor]];
        UILabel *headerLbl = [[UILabel alloc] initWithFrame:CGRectMake(10, 5, 300, 70)];
        [headerLbl setBackgroundColor:[UIColor clearColor]];
        [headerLbl setFont:[UIFont fontWithName:@"Helvetica-neueu" size:14.0]];
        [headerLbl setLineBreakMode:NSLineBreakByWordWrapping];
        [headerLbl setShadowColor:[UIColor whiteColor]];
        [headerLbl setTextColor:[UIColor colorWithRed:(69.0/255.0) green:(69.0/255.0) blue:(69.0/255.0) alpha:1.0f]];
        [headerLbl setTextAlignment:NSTextAlignmentCenter];
        [headerLbl setNumberOfLines:4];
        
        [headerLbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
        
        [headerLbl setText:[Localizer getLocalizedText:@"RECEIPT_STORE_HELP_TEXT"]];
        [receiptHeaderView addSubview:headerLbl];
    }
    
    if (uploadBannerView != nil && receiptHeaderView == nil)
    {
        self.tableView.tableHeaderView = uploadBannerView;
    }
    else if (uploadBannerView == nil && receiptHeaderView != nil)
    {
        self.tableView.tableHeaderView = receiptHeaderView;
    }
    else
    {
        UIView *headerViewContainer = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, uploadBannerView.frame.size.height + receiptHeaderView.frame.size.height)];
        [headerViewContainer addSubview:uploadBannerView];
        [headerViewContainer addSubview:receiptHeaderView];
        receiptHeaderView.frame = CGRectMake(0, uploadBannerView.frame.size.height, receiptHeaderView.frame.size.width, receiptHeaderView.frame.size.height);
        self.tableView.tableHeaderView = headerViewContainer;
    }
}


#pragma mark - Selection Methods

// MOB-13656 - Check what type of entries are selected and set the keys accordingly.
-(void) addToSelectionMobileEntryKey:(EntityMobileEntry*)entity
{
	// if selected expense has smartExpenseID it means its from GSEL
	// MOB-21203
    if ([entity.smartExpenseId lengthIgnoreWhitespace]) {
        //(self.selectedItems)[entity.smartExpenseId] = SMART_EXPENSE_ID;

        // MOB-21203
        // prevents crash, but I don't like this solution.  It relies on ignoring user input until the data is ready.
        // the root cause of the crash is trying to save a nil as a dictionary key
        if (entity.smartExpenseId) {
            [self.selectedItems setObject:SMART_EXPENSE_ID forKey:entity.smartExpenseId];
        }
    }
    else{
        if (entity.isMergedSmartExpense.boolValue) {
            [self.selectedSmartExpenses addObject:entity];
        }
        if([MobileEntryManager isCorporateCardTransaction:entity])
            (self.selectedItems)[entity.cctKey] = CCT_TYPE;
        else if([MobileEntryManager isPersonalCardTransaction:entity])
            (self.selectedItems)[entity.pctKey] = PCT_TYPE;
        else if ([MobileEntryManager isReceiptCapture:entity])
            (self.selectedItems)[entity.rcKey] = RC_TYPE;
        else if ([MobileEntryManager isEreceipt:entity]){
            (self.selectedItems)[entity.ereceiptId] = E_RECEIPT_TYPE;
        }
        else
            (self.selectedItems)[entity.key] = OOP_TYPE;

    }
    [self updateButtons];
}

-(void) removeFromSelectionMobileEntryKey:(EntityMobileEntry*)entity
{
    NSString *key = nil;
    // get key doesnt return smartexpenseid for backwardcompatibility so if entity is from GSEL then use smartexpenseid
    if([entity.smartExpenseId lengthIgnoreWhitespace])
    {
        key = entity.smartExpenseId;
    }
    else
        key = [MobileEntryManager getKey:entity];
    
    NSString *value = (self.selectedItems)[key];
    if (value != nil)
    {
        [self.selectedItems removeObjectForKey:key];
        [self updateButtons];
    }
}

-(BOOL) isSelectedMobileEntryKey:(EntityMobileEntry*)entity
{
    NSString *key = nil;
    if([entity.smartExpenseId lengthIgnoreWhitespace])
    {
        key = entity.smartExpenseId;
    }
    else
        key = [MobileEntryManager getKey:entity];

    NSString *value = (self.selectedItems)[key];
    return (value != nil);
}

-(void) clearSelection
{
    [self.selectedItems removeAllObjects];
    
    if (self.isViewLoaded)
        [self.tableView reloadData];
}

#pragma mark - Navigation Stuff
-(void) goToReportDetailScreen // Add to Report from Rpt Detail
{
	if([UIDevice isPad])
	{
        // If the background is report detail screen of the same report, then just refresh the big detail view
        
		// use homeVC dismiss worked
		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        UINavigationController *nav = delegate.navController;
        MobileViewController *mvc = [nav.viewControllers count]>0?
        (MobileViewController *)(nav.viewControllers)[([nav.viewControllers count]-1)]:nil;
        bool createWizard = YES;
        if (mvc != nil && [mvc isKindOfClass: [ReportDetailViewController_iPad class]])
        {
            ReportDetailViewController_iPad* padVc = (ReportDetailViewController_iPad*) mvc;
            if ([padVc.rpt.rptKey isEqualToString:self.reportToWhichToAddExpenses.rptKey])
                createWizard = NO;
        }
        [self buttonClosePressed:self];
		
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.reportToWhichToAddExpenses, @"REPORT",
									 self.reportToWhichToAddExpenses.rptKey, @"ID_KEY",
									 self.reportToWhichToAddExpenses.rptKey, @"RECORD_KEY",
                                     ROLE_EXPENSE_TRAVELER, @"ROLE",
									 @"YES", @"REPORT_CREATE_WIZARD",
									 @"YES", @"SHORT_CIRCUIT",
									 nil];
		
        if (createWizard)
        {
            // Never used?
            //[delegate.padHomeVC switchToDetail:@"Report" ParameterBag:pBag];
        }
        else
        {
            pBag[@"REPORT_CREATE_WIZARD"] = @"NO";
            pBag[@"REPORT_DETAIL"] = self.reportToWhichToAddExpenses;
            Msg *msg = [[Msg alloc] init];
            msg.parameterBag = pBag;
            msg.idKey = @"SHORT_CIRCUIT";
            [mvc respondToFoundData:msg];
        }
	}
	else
	{
		Msg *msg = [[Msg alloc] init];
		
		NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									  self.reportToWhichToAddExpenses.rptKey, @"ID_KEY",
									  self.reportToWhichToAddExpenses, @"REPORT",
									  self.reportToWhichToAddExpenses, @"REPORT_DETAIL",
                                      ROLE_EXPENSE_TRAVELER, @"ROLE",
									  self.reportToWhichToAddExpenses.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil];
		msg.parameterBag = pBag;
        
		NSUInteger vcCount = [self.navigationController.viewControllers count];
		for (int ix = 0 ; ix < vcCount; ix++ )
		{
			MobileViewController *vc = (MobileViewController *)(self.navigationController.viewControllers)[ix];
			if (vc != nil && [vc isKindOfClass:[ReportDetailViewController class]])
			{
				[vc respondToFoundData:msg];
			}
		}
		
		[self.navigationController popViewControllerAnimated:YES];
		
		// TODO - need to update active list
	}
}

-(void)selectPreAuth
{
    // Scroll to the preauth card charge
    if (self.currentAuthRefNo != nil)
    {
        NSUInteger preAuthIndex = [self.fetchedResultsController.fetchedObjects indexOfObjectPassingTest:^BOOL(id obj, NSUInteger idx, BOOL *stop) {
            if ([obj isKindOfClass:EntityMobileEntry.class] && [self.currentAuthRefNo isEqualToString:[(EntityMobileEntry*)obj authorizationRefNo]])
            {
                *stop = YES;
                return YES;
            }
            return NO;
        }];
        
        if (preAuthIndex != NSNotFound)
        {
            NSUInteger _path[2] = {0, preAuthIndex};
            NSIndexPath *ixPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
            
            [self.tableView selectRowAtIndexPath:ixPath animated:YES scrollPosition:UITableViewScrollPositionMiddle];
            [self tableView:self.tableView didSelectRowAtIndexPath:ixPath];
        }
        self.currentAuthRefNo = nil;
    }
}

#pragma mark - Message Handling Methods
-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:ME_LIST_DATA()])
    {
        // if we refreshed data, redo the merge
        if (![Config isEreceiptsEnabled]) {
            [self.smartExpenseManager2 mergeSmartExpenses];
        }
        

        if (self.editorMode == EditorModeMultiSelectDelete)
        {
            // Now that it has been received, hide the wait view and
            // transition to the normal mode of editing
            // MOB-12986 : ME_DELETE_DATA / ME_SAVE_DATA doesnt post server calls anymore. updates are done using coredata
            if ([self isViewLoaded]) {
                [self hideWaitView];
                
            if ([self isRefreshing])
                [self doneRefreshing];
            }

            [self transitionToMode:EditorModeNormal];
        }
        else
        {
            if ([self isViewLoaded])
            {
                if ([self isWaitViewShowing])
                    [self hideWaitView];
                if ([self isLoadingViewShowing])
                    [self hideLoadingView];
                
                if ([self isRefreshing])
                    [self doneRefreshing];
            }
            
            [self.tableView reloadData];
            
            // MOB-13481 don't know why this doesn't redraw correctly unless we redo the bars.  works though.
            [self configureBars];

            if (self.currentAuthRefNo != nil)
            {
                [self performSelector:@selector(selectPreAuth) withObject:nil afterDelay:0.1f];
            }
        
        }
    }
    else if ([msg.idKey isEqualToString:ME_DELETE_DATA])
	{
        // MOB-12986 :  We should ideally check for the success/failure for each key.
        // 
        MobileExpenseDelete *deletedExpResponse = (MobileExpenseDelete *)msg.responder;
        BOOL isError = NO;
		// Dont need to make server call to delete expenses
        // just delete the entries from coredata and fetchedresultscontroller takes care of rest.
        // success state for corp card/Quick expense is in dictionary of dictionaries
        // for personalCard entries response doesnt consist of pct keys so just get the kill_keys and remove those from coredata

        // check the return status
        if(([@"PCT_TYPE" isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]]
             || [@"CCT_TYPE" isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]] || [@"RC_TYPE" isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]]) && ![deletedExpResponse.returnStatus isEqualToString:@"SUCCESS"])
        {
            isError = YES;
        }
        else if([@"PCT_TYPE" isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]])   // handle pctkeys
        {
           for (id key in  deletedExpResponse.keysToKill)
           {
               //NSLog(@"Deleting personal Card entries with key : %@" , key);
               [[MobileEntryManager sharedInstance]deleteBypctKey:key];
           }
            
        }
//        else if ([@"RC_TYPE" isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]])
//        {
//            for (id key in  deletedExpResponse.keysToKill)
//            {
//                //NSLog(@"Deleting personal Card entries with key : %@" , key);
//                [[MobileEntryManager sharedInstance]deleteByrcKey:key];
//            }
//        }

        // MOB-13656 - Handle cctkeys and mekeys
        NSDictionary *deletedKeys = deletedExpResponse.returnFailures ;
        
        for (id key in deletedKeys)
        {
            NSDictionary *returnfailure = deletedKeys[key];
            if ([returnfailure[@"STATUS"] isEqualToString: @"SUCCESS"])
            {
                // Delete Coredata entry for that key
                if(returnfailure[CCT_TYPE] != nil)
                   [[MobileEntryManager sharedInstance]deleteBycctKey:key];
                else if(returnfailure[OOP_TYPE] )
                   [[MobileEntryManager sharedInstance]deleteByKey:key];
                else if (returnfailure[RC_TYPE])
                    [[MobileEntryManager sharedInstance]deleteByrcKey:key];
            }
            else
            {
                isError = YES;
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"Deleted Expense Failed: unable to delete expense with key : %@ , ERROR_MESSAGE : %@ " , key, returnfailure[@"ERROR_MESSAGE"]]  Level:MC_LOG_ERRO];
            }

        }

        if(isError || msg.responseCode != 200)
        {
            //TODO: Generic message for all doesnt handle - One or more entries failed. We need seperate error message for server error and for scenario when some entries failed to delete
              UIAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:[Localizer getLocalizedText:@"Unexpected Error"]
                                      message:[Localizer getLocalizedText:@"Delete Entry Failed"]
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
                [alert show];
 
        }
            // Refresh summarydata only if corpcard transactions are deleted
            // only corp card transactions show in home summary anyway. 
        if (msg.parameterBag != nil && [CCT_TYPE isEqualToString:(NSString*)(msg.parameterBag)[@"TYPE"]])
        {
            //let's refresh the cache for summary data
            [self refreshHome];
        }
    
        if (self.editorMode == EditorModeMultiSelectDelete)
        {
            // hideWaitView
            if ([self isWaitViewShowing])
            {
                [self hideWaitView];
            }
            [self transitionToMode:EditorModeNormal];
        }
		
	}
    else if ([msg.idKey isEqualToString:RECEIPT_STORE_RECEIPTS])
    {
        if ([self isViewLoaded])
        {
            // We send the RECEIPT_STORE_RECEIPTS message in different scenarios.  Most don't require any special handling.  However, after we upload or delete a receipt, we refresh the receipt list and need to hide the wait view.  Therefore, hide the wait view if it is showing.
            if ([self isWaitViewShowing])
                [self hideWaitView];
            
            // If the user presses the refresh receipts button, then the loading view shows while receipts are being reloaded.  Dismiss it.
            if ([self isLoadingViewShowing])
                [self hideLoadingView];
            
            if ([self isRefreshing])
                [self doneRefreshing];
            [self.tableView reloadData];
        }
    }
	else if ([msg.idKey isEqualToString:DELETE_RECEIPT])
	{
		DeleteReceipt *deleteReceiptResponse = (DeleteReceipt*)msg.responder;
		if (msg.errBody == nil && [deleteReceiptResponse.status isEqualToString:@"SUCCESS"])
		{
            [self reloadReceiptsWithLoadingView:NO];
		}
        else
        {
            if (self.isViewLoaded)
                [self hideWaitView];
        }
	}
	else if ([msg.idKey isEqualToString:ADD_TO_REPORT_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		AddToReportData *auth = (AddToReportData *)msg.responder;
		NSString* errMsg = msg.errBody;
		if (msg.errBody == nil && msg.responseCode != 200)
        {
            errMsg = [Localizer getLocalizedText:@"Failed to add to report"];
        }
        
        if (errMsg != nil)
		{
			MobileAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:[Localizer getLocalizedText:@"Connection Error"]
                                      message:msg.errBody
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
			[alert show];
		}
		else
		{
            self.reportToWhichToAddExpenses = auth.rpt;
            if ([auth hasFailedEntry])
            {
                MobileAlertView *alert = [[MobileAlertView alloc]
                                          initWithTitle:[Localizer getLocalizedText:@"Import Error"]
                                          message:[Localizer getLocalizedText:@"Failed to import entry"]
                                          delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                          otherButtonTitles:nil];
                [alert show];
            }
            // Add to report is success, so update expense list.
            // No need for server round trip. just remove those expense entries from coredata list -- cool
            // Delete rows by each keys
            
            // Handle AddToReportV5
            for(NSString *key in auth.smartExpenseIdsStatusDict) {
                
                ActionStatus *status = (auth.smartExpenseIdsStatusDict)[key];
                if([status.status isEqualToString:@"SUCCESS" ])
                {
                    [[MobileEntryManager sharedInstance] deleteBySmartExpenseId:key];
                }

            }
            for(NSString *key in auth.pctStatusDict)
            {
                ActionStatus *status = (auth.pctStatusDict)[key];
                if([status.status isEqualToString:@"SUCCESS" ])
                {
                    [[MobileEntryManager sharedInstance] deleteBypctKey:key];
                }
                
            }
            
            for(NSString *key in auth.cctStatusDict)
            {
                ActionStatus *status = (auth.cctStatusDict)[key];
                if([status.status isEqualToString:@"SUCCESS" ])
                {
                    [[MobileEntryManager sharedInstance] deleteBycctKey:key];
                }

            }
            
            for(NSString *key in auth.meStatusDict)
            {
                ActionStatus *status = (auth.meStatusDict)[key];
                if([status.status isEqualToString:@"SUCCESS" ])
                {
                    [[MobileEntryManager sharedInstance] deleteByKey:key];
                }

            }
            // TODO : check if Delete the E-receipt is complete
            
            [self goToReportDetailScreen];
            //            if ([UIDevice isPad])
            //                [self buttonClosePressed:self];
            //            else
            //                [self.navigationController popViewControllerAnimated:YES];
        }
	}

    

}

#pragma mark - Loading Methods
-(void)loadExpenses
{
    [self loadExpenses:NO];
}

-(void)loadExpenses:(BOOL)cacheOnly
{
    [[ExSystem sharedInstance].msgControl createMsg:ME_LIST_DATA() CacheOnly:cacheOnly? @"YES" : @"NO" ParameterBag:nil SkipCache:NO RespondTo:self];
}

-(void) reloadExpensesWithLoadingView:(BOOL)showloadingView
{
    if (showloadingView)
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"Refreshing Data"]];
    
    [[ExSystem sharedInstance].msgControl createMsg:ME_LIST_DATA() CacheOnly:@"NO" ParameterBag:nil SkipCache:YES RespondTo:self];
}

-(void) loadReceipts
{
    [self loadReceipts:NO];
}

-(void) loadReceipts:(BOOL)cacheOnly
{
    [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Receipts"]];
    
    // Uses cache, if available
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"N", @"FILTER_MOBILE_EXPENSE", nil];
    [[ExSystem sharedInstance].msgControl createMsg:RECEIPT_STORE_RECEIPTS CacheOnly:cacheOnly? @"YES" : @"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

- (void)reloadReceipts
{
    [self reloadReceiptsWithLoadingView:YES];
}

-(void) reloadReceiptsWithLoadingView:(BOOL)showloadingView
{
    if (showloadingView)
    {
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Receipts"]];
    }
    
    // Skips cache
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"N", @"FILTER_MOBILE_EXPENSE", nil];
    [[ExSystem sharedInstance].msgControl createMsg:RECEIPT_STORE_RECEIPTS CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark - Expense Methods
-(void) showQuickExpenseEditor:(EntityMobileEntry*)entry
{
    QEFormVC *formVC = [[QEFormVC alloc] initWithEntryOrNil:entry];
    formVC.title = [Localizer getLocalizedText:@"Expense"];
    [self.navigationController pushViewController:formVC animated:YES];
}

#pragma mark - ReceiptDownloaderDelegate Methods
-(void) didDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    if (!self.isViewLoaded)
        return;
    
    if ([self isExpensesSegmentSelected])
        return;
    
    // Check whether any of the visible cells care about this receipt
    NSArray *visibleCells = self.tableView.visibleCells;
    for (UITableViewCell *cell in visibleCells)
    {
        if ([cell isKindOfClass:[ReceiptStoreListCell class]])
        {
            ReceiptStoreListCell *receiptCell = (ReceiptStoreListCell*)cell;
            if ([receiptCell.receiptId isEqualToString:receiptId] && thumbNail)
            {
                // Grab the receipt image from the cache.  We know it's there because we received this notification
                NSString *dataType = nil;
                NSData *imageData = [[ReceiptCache sharedInstance] getThumbNailReceiptForId:receiptId dataType:&dataType];
                UIImage *image = [UIImage imageWithData:imageData];
                
                // It should never be nil, but check it just to be safe
                if (image != nil)
                {
                    [receiptCell.activityView stopAnimating];
                    [receiptCell.activityView setHidden:YES];
                    [receiptCell.thumbImageView setImage:image];
                }
                
                break;
            }
        }
    }
}

-(void) didFailToDownloadReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url
{
    if (!self.isViewLoaded)
        return;
    
    if ([self isExpensesSegmentSelected])
        return;
    
    // Check whether any of the visible cells care about this receipt
    NSArray *visibleCells = self.tableView.visibleCells;
    for (UITableViewCell *cell in visibleCells)
    {
        if ([cell isKindOfClass:[ReceiptStoreListCell class]])
        {
            ReceiptStoreListCell *receiptCell = (ReceiptStoreListCell*)cell;
            if ([receiptCell.receiptId isEqualToString:receiptId])
            {
                [self showPlaceHolderImageInCell:receiptCell];
            }
        }
    }
}

-(void) showPlaceHolderImageInCell:(ReceiptStoreListCell*)cell
{
    // This is what we show when we don't have the image and are not able to download it.
    [cell.activityView stopAnimating];
    [cell.activityView setHidden:YES];
    [cell.thumbImageView setImage:[UIImage imageNamed:@"receipt_store"]];
}

#pragma mark - New Receipt
-(void) showActionSheet
{
	if (self.actionPopOver != nil && self.actionPopOver.visible && self.actionSheetItems != nil)
        [self.actionPopOver dismissWithClickedButtonIndex:self.actionSheetItems.count animated:NO];
    
    self.actionPopOver = nil;
	
    if ([UIDevice isPad]) {
        if (self.pickerPopOver != nil && pickerPopOver.popoverVisible)
            [pickerPopOver dismissPopoverAnimated:YES];
        
        self.pickerPopOver = nil;
    }
    
	if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
        self.actionSheetItems = [@[@"Camera", @"Photo Album"] mutableCopy]; // Use non-localized names in this array
	else
        self.actionSheetItems = [@[@"Photo Album"] mutableCopy];
    
    MobileActionSheet* actionSheet = [[MobileActionSheet alloc] initWithTitle:nil
                                                                     delegate:self
                                                            cancelButtonTitle:nil
                                                       destructiveButtonTitle:nil
                                                            otherButtonTitles:nil, nil];
    
    for (NSString *unlocalizedItemName in self.actionSheetItems)
    {
        [actionSheet addButtonWithTitle:[Localizer getLocalizedText:unlocalizedItemName]];
    }
    
    [actionSheet addButtonWithTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]];
    actionSheet.cancelButtonIndex = self.actionSheetItems.count;
    
    if ([UIDevice isPad] && self.navigationItem.rightBarButtonItem != nil)
	{
        self.actionPopOver = actionSheet;
		[actionSheet showFromBarButtonItem:self.navigationItem.rightBarButtonItem animated:YES];
	}
	else
	{
        actionSheet.actionSheetStyle = UIActionSheetStyleBlackTranslucent;
        
        // not sure why the toolbar is sometimes hidden.  or if it's intentional.  But the way we display the actionsheet depends upon it.
        if (self.navigationController.toolbar != nil && !self.navigationController.toolbarHidden) {
            [actionSheet showFromToolbar:self.navigationController.toolbar];
        } else {
            [actionSheet showInView:self.view];
        }
    }
}

#pragma mark - UIActionSheetDelegate method
- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if ([self isExpensesSegmentSelected])
    {
        if (buttonIndex == actionSheet.cancelButtonIndex)
            return;
        
        NSString *itemName = (self.actionSheetItems)[buttonIndex];
        
        if ([itemName isEqualToString:[Localizer getLocalizedText:@"All Expenses"]])
            self.expenseFilterName = nil;
        
        else if ([itemName isEqualToString:[Localizer getLocalizedText:@"Sort by Amount"]])
        {
            [self flurryEventForFlilerSort:@"Amount"];
            self.expenseSorterName = AMOUNT_SORTER;
        }
        else if ([itemName isEqualToString:[Localizer getLocalizedText:@"Sort by Date"]])
        {
            [self flurryEventForFlilerSort:@"Date"];
            self.expenseSorterName = DATE_SORTER;
        }
        else if ([itemName isEqualToString:[Localizer getLocalizedText:@"Corporate Card"]])
            self.expenseFilterName = CORPORATE_FILTER;
        else if ([itemName isEqualToString:[Localizer getLocalizedText:@"ExpenseIt Entries"]])
            self.expenseFilterName = EXPENSEIT_FILTER;
        else // Personal card
            self.expenseFilterName = itemName;
        
        self.fetchedResultsController = nil;
        [self.tableView reloadData];
    }
    else if (buttonIndex < self.actionSheetItems.count)
    {
        NSString *unlocalizedItemName = (self.actionSheetItems)[buttonIndex];
        if ([unlocalizedItemName isEqualToString:@"Camera"])
            [self cameraActionSelected];
        else if ([unlocalizedItemName isEqualToString:@"Photo Album"])
            [self albumActionSelected];
    }
    
    [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
}

-(void) flurryEventForFlilerSort:(NSString*)sortBy
{
    NSString *flurryStr = @"";
    
    if (self.expenseFilterName == nil)
        flurryStr = [NSString stringWithFormat:@"All by %@", sortBy];
    else if ([self.expenseFilterName isEqualToString:CORPORATE_FILTER])
        flurryStr = [NSString stringWithFormat:@"Corporate Card by %@", sortBy];
    else if ([self.expenseFilterName isEqualToString:EXPENSEIT_FILTER])
        flurryStr = [NSString stringWithFormat:@"ExpenseIt by %@", sortBy];
    
    NSDictionary *dict = @{@"By": flurryStr};
    [Flurry logEvent:@"Quick Expense: FilterSort" withParameters:dict];
}

-(void) cameraActionSelected
{
	if (![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
		return;
	
    NSDictionary *dict = @{@"Added Using": @"Camera",@"Added To": @"Receipt Store"};
    [Flurry logEvent:@"Receipts: Add" withParameters:dict];
    
    if (rsuHelper == nil)
    {
        rsuHelper = [[ReceiptStoreUploadHelper alloc] init];
        rsuHelper.vc = self;
    }
    
    [rsuHelper startCamera:self.navigationItem.rightBarButtonItem];
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
    
    if ([self.pickerPopOver isPopoverVisible] )//&& (self.pickerPopOver.contentViewController == rsuHelper.cameraOverlayView.imagePickerController))
    {
        [self.pickerPopOver dismissPopoverAnimated:YES];
        self.pickerPopOver = nil;
    }
}

-(void)albumActionSelected
{
	if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypePhotoLibrary])
	{
        NSDictionary *dict = @{@"Added Using": @"Album", @"Added To": @"Receipt Store"};
        [Flurry logEvent:@"Receipts: Add" withParameters:dict];
        
		UIImagePickerController *imgPicker = (UIImagePickerController*)[[UnifiedImagePicker sharedInstance] imagePicker];
		imgPicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
		[UnifiedImagePicker sharedInstance].delegate = self;
		
		if ([UIDevice isPad])
		{
			if(pickerPopOver != nil)
				[pickerPopOver dismissPopoverAnimated:YES];
			
            self.pickerPopOver = [[UIPopoverController alloc] initWithContentViewController:imgPicker];
            [pickerPopOver presentPopoverFromBarButtonItem:self.navigationItem.rightBarButtonItem permittedArrowDirections:UIPopoverArrowDirectionDown|UIPopoverArrowDirectionUp animated:YES];
		}
		else
		{
			[self presentViewController:imgPicker animated:YES completion:nil];
		}
	}
}

#pragma mark - UnifiedImagePickerDelegate methods
-(void)unifiedImagePickerSelectedImage:(UIImage*)image
{
	if ([UIDevice isPad])
	{
		if (self.pickerPopOver != nil)
		{
			[self.pickerPopOver dismissPopoverAnimated:YES];
			self.pickerPopOver = nil;
		}
	} else {
		[[[UnifiedImagePicker sharedInstance] imagePicker] dismissViewControllerAnimated:YES completion:nil];
    }
    
    self.uploader = [[ReceiptUploader alloc] init];
    [self.uploader setSeedData:self withImage:image];
    // Check if we are connected to network before uploading
    // Quereceipt if offline
    if([ExSystem connectedToNetwork])
    {
        [self showWaitViewWithProgress:YES withText:[Localizer getLocalizedText:@"RECEIPT_IMG_UPLOADING"]];
        [self.uploader startUpload];
    }
    else
    {
        [self queueReceipt:self.uploader.receiptImage];
    }
}

#pragma mark - ReceiptUploaderDelegate Methods
-(void) failedToPrepareImageData // memory issue
{
    if (self.isViewLoaded)
        [self hideWaitView];
    
    NSDictionary *dict = @{@"Failure": @"Failed to capture or reduce resolution for receipt image"};
    [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
    
    // MOB-9416 handle image conversion failure b/c memory shortage
    NSString *errMsg = [Localizer getLocalizedText:@"Free up memory and retry receipt upload"];
    
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"]
                                                        message:errMsg
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                              otherButtonTitles:
                          nil];
    [alert show];
}

-(void) failedToUploadImage:(NSString*) errorStatus // e.g. Imaging not configured
{
    if (self.isViewLoaded)
        [self hideWaitView];
    
    // Flurry?
    if (![self handleImageConfigError:errorStatus])
    {
        NSString *errMsg = errorStatus != nil? errorStatus :
        [Localizer getLocalizedText:@"ReceiptUploadFailMsg"];
        
        UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"]
                                                            message:errMsg
                                                           delegate:nil
                                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                                  otherButtonTitles:
                              nil];
        [alert show];
        
        // If offline is supported, then queue the receipt that failed to upload
        [[MCLogging getInstance] log:@"Receipt upload failed and queue the receipt for upload later" Level:MC_LOG_DEBU];
        [self queueReceipt:self.uploader.receiptImage];
    }
    else
    {
        // Receipt Configuration error.
        [[MCLogging getInstance] log:@"Receipt upload failed because of receipt configuration error" Level:MC_LOG_DEBU];
    }
}

-(void) receiptUploadSucceeded:(NSString*) receiptImageId
{
    [self reloadReceiptsWithLoadingView:NO]; // Refresh the list. The wait view is already showing, so do not show the loading view.
}

#pragma - receipt error handling
-(BOOL) handleImageConfigError:(NSString*) errCode
{
    if ([@"Imaging Configuration Not Available." isEqualToString: errCode])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Cannot access receipt"]
                              message:[Localizer getLocalizedText:@"ERROR_BAD_CONFIG_MSG"]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        [alert show];
        return TRUE;
    }
    return FALSE;
}
#pragma mark - Queue Receipt Methods

-(void) queueReceipt:(UIImage*)receiptImage
{
    [ReceiptEditorVC queueReceiptImage:receiptImage date:[NSDate date]];
    [self receiptQueued];
}

-(void) receiptQueued
{
    [self makeTableHeaderView]; // Header needs updating in case it is showing the number of queued items which just changed.
    
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"Receipt Queued"]
                          message:[Localizer getLocalizedText:@"Your receipt has been queued"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                          otherButtonTitles:nil];
    [alert show];
}

#pragma mark - UploadBannerDelegate Methods

-(void) showUploadViewController
{
    UploadQueueViewController *vc = [[UploadQueueViewController alloc] initWithNibName:@"UploadQueueViewController" bundle:nil];
    [self.navigationController pushViewController:vc animated:YES];
}

-(void)closeMe
{
    if (self.pickerPopOver != nil && pickerPopOver.popoverVisible)
        [pickerPopOver dismissPopoverAnimated:YES];
    
    if (self.actionPopOver != nil && self.actionPopOver.visible && self.actionSheetItems != nil)
        [self.actionPopOver dismissWithClickedButtonIndex:self.actionSheetItems.count animated:NO];
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
