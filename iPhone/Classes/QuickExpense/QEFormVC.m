//
//  QEFormVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "FeedbackManager.h"
#import "QEFormVC.h"

#import "PDFVC.h"

#import "MobileEntryManager.h"
#import "PostMsgInfo.h"
#import "UploadQueueItemManager.h"
#import "UploadableReceipt.h"
#import "MRUManager.h"
#import "ReceiptCache.h"
#import "MsgControl.h"
#import "ReceiptEditorVC.h"
#import "UploadQueue.h"
#import "QuickExpensesReceiptStoreVC.h"
#import "MobileExpenseSave.h"
#import "SmartExpenseManager2.h"
#import "SelectReportViewController.h"
#import "ReportData.h"

#import "HelpOverlayFactory.h"
#import "WaitViewController.h"

@interface QEFormVC ()
-(void) showQueueAlert;
@property bool hasCloseButton;
@property (nonatomic, strong) SmartExpenseManager2 *smartExpenseManager2;
@property (nonatomic, strong) ReportData *selectedReport;
@property (nonatomic, strong) NSString *smartExpenseId;
@end

@implementation QEFormVC
@synthesize entry, isSaving, requiredDict, updatedReceiptImageId,promptSaveAlert;
@dynamic managedObjectContext;
@synthesize receipt;

//#define		kAlertViewRateApp	101781
#define     kSectionCardAuth    @"CARD_AUTH"

#pragma mark - Managed object context
-(NSManagedObjectContext*)managedObjectContext
{
    if (_managedObjectContext == nil)
    {
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate *) [UIApplication sharedApplication].delegate;
        _managedObjectContext = ad.managedObjectContext;
    }
    return _managedObjectContext;
}

- (id)initWithEntryOrNil:(EntityMobileEntry*) mobileEntry withCloseButton:(BOOL)withCloseButton
{
    self = [super initWithNibName:@"QEFormVC" bundle:nil];
    if (self)
    {
        self.hasCloseButton = withCloseButton;
        
        if (mobileEntry != nil)
        {
            self.entry = mobileEntry;
            self.smartExpenseId = self.entry.smartExpenseId;
            // MOB-13680 - Check if there is any key. All keys will be nil for offline/uploadqueue items
            if ([MobileEntryManager getKey:self.entry] == nil )
            {
                self.isInUploadQueue = true;
            }
            else
            {
                self.isInUploadQueue = false;
                self.smartExpenseManager2 = [[SmartExpenseManager2 alloc] initWithContext:self.managedObjectContext];
            }
        }
        else
        {
            self.entry = [[MobileEntryManager sharedInstance] makeNew];
            self.isInUploadQueue = false;
        }
        [self makeFieldsArray:nil];
        [self loadReceipt];
    }
    
    return self;
}

- (id)initWithEntryOrNil:(EntityMobileEntry*) mobileEntry
{
    return [self initWithEntryOrNil:mobileEntry withCloseButton:NO];
}

- (void)loadReceipt {
    
    if (receipt == nil)
    {
        receipt = [[Receipt alloc] init];
    }

    if (entry.localReceiptImageId != nil && entry.localReceiptImageId.length > 0) {
        // We have a local receipt, i.e. one that has not yet been uploaded to the server
        // Need to add a flag of PdfReceipt because of MOB-21462
        // currently we cannot attach a pdf to an expense offline, so it is not a pdf receipt
        NSString *filePath = [UploadableReceipt filePathForLocalReceiptImageId:entry.localReceiptImageId isPdfReceipt:NO];
        if (filePath != nil && filePath.length > 0)
        {
            // We found the file in which the local receipt image is stored
            // AJC - unused code 1 line below. please delete if unused by 2013-11-29
            //formVC.entry.receiptImage = [UIImage imageWithContentsOfFile:filePath];
            entry.hasReceipt = @"Y";
            updatedReceiptImageId = NO;
            
            receipt.receiptId = entry.receiptImageId;
            receipt.receiptImg = [UIImage imageWithContentsOfFile:filePath];
            //MOB-13161
            receipt.localReceiptId = entry.localReceiptImageId;
        }
    }
    // Hmmm. The mobile entry has not yet been saved to the server, yet we are referencing a receipt that is on the server.  This could have happened if the receipt was uploaded, but the mobile expense wasn't. (Upload cancelled or lost connectivity during upload)
    // TODO: figure out how to load this (and maybe check if it's already loaded)
    else if([entry.mobileReceiptImageId length])
    {
        receipt.receiptId = entry.mobileReceiptImageId;
    }
    else if ([entry.eReceiptImageId length])
    {
        receipt.receiptId = entry.eReceiptImageId;
    }
    else if ([entry.cctReceiptImageId length])
    {
        receipt.receiptId = entry.cctReceiptImageId;
    }
    else if ([entry.receiptImageId length])
    {
        receipt.receiptId = entry.receiptImageId;
    }
}

#pragma mark -
#pragma mark MobileViewController Methods

-(NSString *)getViewIDKey
{
	return OUT_OF_POCKET_FORM;
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)navigateView:(BOOL)refresh
{
    if ([UIDevice isPad] && [self.navigationController.viewControllers count] ==1)
    {
        // AJC - unused code 1 line below. please delete if unused by 2013-11-29
        //[self dismissViewControllerAnimated:YES completion:nil];
        // MOB-13130 :if Quickexpense is added from home then show the expense list 
        QuickExpensesReceiptStoreVC *nextController = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        [nextController setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
        // notify QuickExpensesReceiptStoreVC to refresh expense list
        nextController.requireRefresh = refresh;
        [self.navigationController pushViewController:nextController animated:YES];

    }
    else if (![UIDevice isPad] && [self.navigationController.viewControllers count] ==2)
    {
        // Quick Expense from Home, go to expenses list
        [self.navigationController popToRootViewControllerAnimated:NO];
        
        QuickExpensesReceiptStoreVC *view = [[QuickExpensesReceiptStoreVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
        [view setSeedDataAndShowReceiptsInitially:NO allowSegmentSwitch:YES allowListEdit:YES];
        // notify QuickExpensesReceiptStoreVC to refresh expense list
        view.requireRefresh = refresh;
        [[ConcurMobileAppDelegate getBaseNavigationController] pushViewController:view animated:YES];
    }
    else
    {
        // notify QuickExpensesReceiptStoreVC to refresh expense list
        QuickExpensesReceiptStoreVC *view = (QuickExpensesReceiptStoreVC *)[ConcurMobileAppDelegate getMobileViewControllerByViewIdKey:QUICK_EXPENSES_AND_RECEIPTS_COMBO_VIEW];
        view.requireRefresh = refresh;
        [self.navigationController popViewControllerAnimated:YES];
    }
}

-(void)executeActionAfterSave
{
	self.isDirty = NO;
    [self navigateView:self.isNewQuickExpense];
}


-(void) didProcessMessage:(Msg *)msg
{
    if ([msg.idKey isEqualToString:ME_SAVE_DATA])
    {
        
        MobileExpenseSave *oopSaved = (MobileExpenseSave *)msg.responder;
		if (![oopSaved.returnStatus isEqualToString:@"SUCCESS"])
		{
			if ([self isViewLoaded])
			{
				UIAlertView *alert = [[MobileAlertView alloc]
									  initWithTitle:[Localizer getLocalizedText:@"Error"]
									  message:[Localizer getLocalizedText:@"The Out of Pocket Entry could not be saved.  Please try again later."]
									  delegate:nil
									  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
									  otherButtonTitles:nil];
				[alert show];
			}
            
            [self clearActionAfterSave];

            if ([self isViewLoaded])
            {
                [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            }
            self.isSaving = NO;
		}
        else
        {
            //MOB-12986: TODO - get the MWS response and set the mekeys to entity.key so that the expenselist gets updated
           

            if (self.selectedReport)
            {
                 self.entry.key = oopSaved.meKey;
                // Eventually replace this call with a call to the new code! -EC
                NSMutableArray *meKeys = [[NSMutableArray alloc] initWithObjects:oopSaved.meKey, nil];
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             SELECT_REPORT, @"TO_VIEW",
                                             meKeys, @"ME_KEYS",
                                             nil];
                pBag[@"RPT_KEY"] = self.selectedReport.rptKey;

                [[ExSystem sharedInstance].msgControl createMsg:ADD_TO_REPORT_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            }
            else
            {
            // update the expense in local coredata if its not a new expense
                if (!self.isNewQuickExpense) {
                    [[MobileEntryManager sharedInstance] saveIt:self.entry];
                }
                
#define AT_LOGGING_LEVEL_DEBUG
                
                [[FeedbackManager sharedInstance] setShowRatingOnNextView:YES];

                if ([self isViewLoaded])
                {
                    [WaitViewController hideAnimated:YES withCompletionBlock:nil];
                }
                self.isSaving = NO;
                
                [self navigateView:YES];
            }
        }
    }

    if ([msg.idKey isEqualToString:ADD_TO_REPORT_DATA])
    {
        if ([self isViewLoaded])
        {
            [self hideWaitView];
        }
        self.isSaving = NO;

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
                                      delegate:self
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                      otherButtonTitles:nil];
			[alert show];
		}
		else
		{
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
                
            // MOB-7484
            [ReportViewControllerBase refreshSummaryData];
                
            // Dismiss this dialog
            [self goToReportDetailScreen:auth];
		}
    }
    
    // TODO: Need to queue if upload_image_data or save_OOP fails.
}

-(void) goToReportDetailScreen:(AddToReportData*)ard  // Add to Report from OOP List
{
	if([UIDevice isPad])
	{

		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];

        UINavigationController *homeNavigationController = (UINavigationController*)delegate.navController;

		[self dismissViewControllerAnimated:YES completion:nil];

		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: ard.rpt, @"REPORT", ard.rpt.rptKey, @"ID_KEY",
									 ard.rpt, @"REPORT_DETAIL",
                                     ard.rpt.rptKey, @"RECORD_KEY",
                                     ROLE_EXPENSE_TRAVELER, @"ROLE",
                                     @"YES", @"SHORT_CIRCUIT"
									 ,nil];

        pBag[@"COMING_FROM"] = @"REPORT"; // Force it to fe

        ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		//newDetailViewController.iPadHome = homeVC;
		newDetailViewController.isReport = YES;

        [homeNavigationController pushViewController:newDetailViewController animated:YES];

		//[iPadHome switchToDetail:@"Report" ParameterBag:pBag];
        [newDetailViewController loadReport:pBag]; // Note that the original implementation called
	}
	else
	{
        ReportDetailViewController *detailVC = [[ReportDetailViewController alloc] initWithNibName:@"ReportHeaderView" bundle:nil];
        //        NSArray *newNavStack = [NSArray arrayWithObjects:[self.navigationController.viewControllers objectAtIndex:0], detailVC, nil];
        //        [self.navigationController setViewControllers:newNavStack animated:YES];

        // MOB-12689 setViewControllers cause the custom back button to stack on top of default back button
        // causes back button in later screen to be hidden.
        UINavigationController* nav = self.navigationController;
        [nav popToRootViewControllerAnimated:NO];
        [nav pushViewController:detailVC animated:YES];

		NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									  ard.rpt.rptKey, @"ID_KEY",
									  ard.rpt, @"REPORT",
									  ard.rpt.rptKey, @"RECORD_KEY",
                                      ROLE_EXPENSE_TRAVELER, @"ROLE",
                                      @"YES", @"SHORT_CIRCUIT", nil];
        Msg *msg = [[Msg alloc] init];
        msg.parameterBag = pBag;
        msg.idKey = @"SHORT_CIRCUIT";
        [detailVC respondToFoundData:msg];
	}
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

-(void) viewWillAppear:(BOOL)animated
{
    self.title = [Localizer getLocalizedText:@"Expense"];
    if (self.entry.isMergedSmartExpense.boolValue) {
        self.navigationController.toolbarHidden = NO;
    } else {
        self.navigationController.toolbarHidden = YES;
    }

    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //[super viewWillAppear:animated];
    [self makeSaveButton:nil];
    [tableList reloadData];
    
	[super viewWillAppear:animated]; 
}

-(void)viewDidAppear:(BOOL)animated
{
    
    [super viewDidAppear:animated];
    // MOB-17433 - Add accessibility for the screen
    NSArray *items = self.navigationController.toolbar.items;
    for (UIBarButtonItem *barButton in items) {
        barButton.isAccessibilityElement = YES;
    }
    NSArray *navitems = self.navigationController.navigationBar.items;
    for (UIBarButtonItem *barButton in navitems) {
        barButton.isAccessibilityElement = YES;
    }
}
- (void)viewDidLoad
{
    // Do any additional setup after loading the view from its nib.
   /* if([UIDevice isPad])
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeMe:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }*/
    
    if(self.hasCloseButton)
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeMe:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }

    
    if (self.allFields == nil)
        [self makeFieldsArray:nil];

    if (self.title == nil)
    {
        self.title = [Localizer getLocalizedText:@"Add Expense"];
    }
    
    [super viewDidLoad];
    
    if (self.entry.rcKey == nil)
        
    [self makeSaveButton:nil];

    [self configureToolbar];
}

-(void) configureToolbar
{
    if (self.entry.isMergedSmartExpense.boolValue) {
        UIBarButtonItem *btnSplitSmartExpense = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Unmatch"] style:UIBarButtonItemStyleBordered target:self action:@selector(splitSmartExpense)];
        self.toolbarItems = @[btnSplitSmartExpense];
    }
}

-(void) splitSmartExpense
{
    if (self.entry.isMergedSmartExpense.boolValue) {
        [self.smartExpenseManager2 splitSmartExpense:self.entry];
        [self.navigationController popViewControllerAnimated:YES];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"SmartExpenseSplit" object:self];
    }
}

-(void) viewWillDisappear:(BOOL)animated
{
    // user decided not to save while making a new quick expense
    if( false == self.isSaving &&
         true == self.isNewQuickExpense )
    {
        // track the cancel
        [Flurry logEvent:@"Mobile Entry: Cancelled"];
    }
    
    [super viewWillDisappear:animated];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    self.tableList = nil;
}


#pragma mark - Make Fields Array
- (void)makeFieldsArray:(id)sender
{
    if (self.entry != nil && [MobileEntryManager isCardAuthorizationTransaction:self.entry])
        self.sections = [[NSMutableArray alloc] initWithObjects:@"0", kSectionCardAuth, @"1", nil];
    else
        self.sections = [[NSMutableArray alloc] initWithObjects:@"0", @"1", nil];
    
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    self.allFields = [[NSMutableArray alloc] initWithObjects: nil];
    
    // MIB : MOB-13462  - Re-order fields
    [allFields addObject:[self getExpenseTypeField]];
    [allFields addObject:[self getAmountField]];
    [allFields addObject:[self getCurrencyField]];
    [allFields addObject:[self getDateField]];
    [allFields addObject:[self getLocationField]];
    [allFields addObject:[self getVendorField]];
    [allFields addObject:[self getCommentField]];
    
    sectionFieldsMap[@"1"] = allFields;
}

- (FormFieldData *)getCommentField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"CommentEx" label:[Localizer getLocalizedText:@"Comment"] value:entry.comment ctrlType:@"textarea" dataType:@"VARCHAR"];
    if (entry.rcKey != nil || entry.ereceiptId != nil)
        field.access = @"RO";
    return field;
}

- (FormFieldData *)getLocationField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"LocName" label:[Localizer getLocalizedText:@"Location"] value:entry.locationName ctrlType:@"edit" dataType:@"LOCATION"];
    field.liKey = entry.locationName; // Prevent None gets checked

    //MOB-14662 - if we have no location, and this is a new entry then default to the MRU value
    if (self.entry.locationName == nil &&  ![MobileEntryManager isCardTransaction:entry] && self.entry.key == nil && self.entry.expKey == nil) {
        ListItem *lastUsedLocation = [[MRUManager sharedInstance] getLastUsedLocation];

        if (lastUsedLocation != nil) {
            field.fieldValue = lastUsedLocation.liName;
            field.liKey = [NSString stringWithFormat:@"%@", lastUsedLocation.liKey];
            field.liCode = lastUsedLocation.liCode;
        }
    }
    if (entry.rcKey != nil || entry.ereceiptId != nil)
    {
        field.access = @"RO";
    }
    return field;
}

- (FormFieldData *)getVendorField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"VENDOR" label:[Localizer getLocalizedText:@"Vendor"] value:entry.vendorName ctrlType:@"edit" dataType:@"VARCHAR"];
    if (entry.cctKey != nil || entry.rcKey != nil || entry.ereceiptId != nil) {
        field.access = @"RO"; // MOB-6874
    }
    return field;
}

// assumes that currency has already been setup by calling getCurrencyField
- (FormFieldData *)getAmountField
{
    NSString *amountFieldValue = [NSNumberFormatter localizedStringFromNumber:entry.transactionAmount numberStyle:NSNumberFormatterDecimalStyle];
    
    FormFieldData *field = [[FormFieldData alloc] initField:@"TransactionAmount" label:[Localizer getLocalizedText:@"Amount"] value: amountFieldValue ctrlType:@"edit" dataType:@"MONEY"];

    if (entry.crnCode) {
        field.extraDisplayInfo = entry.crnCode;
    } else {
        field.extraDisplayInfo = [self getDefaultCrnCode];
    }

    field.fieldValue = [FormatUtils formatMoneyWithoutCrn:field.fieldValue crnCode:[field getCrnCodeForMoneyFldType]];
    if (entry.pctKey != nil || entry.cctKey != nil || entry.rcKey != nil || entry.ereceiptId != nil) {
        field.access = @"RO";
    }
    field.required = @"Y";
    
    // MOB-12779: Force the transaction field to be blank when the quick expense is newly created
    double dblVal = 0.0;
    NSString *amount = [NSString stringWithFormat:@"%@", entry.transactionAmount];
    if ([self isZeroAmount:amount doubleValue:&dblVal field:field] && [self isNewQuickExpense] && ![self isInUploadQueue] && !([field.access isEqualToString:@"RO"] || [field.access isEqualToString:@"HD"]) )
        field.fieldValue = @"";
    
    return field;
}

- (NSString *)getDefaultCrnCode
{
    NSLocale* locale = [NSLocale currentLocale];
    return [locale objectForKey:NSLocaleCurrencyCode];
}

- (FormFieldData *)getCurrencyField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"TransactionCurrencyName" label:[Localizer getLocalizedText:@"Currency"] value:entry.crnCode ctrlType:@"edit" dataType:@"CURRENCY"];
    // MIB : Minor fixes
    NSString *crnCode = entry.crnCode;
    
    if (![[entry crnCode] lengthIgnoreWhitespace]) {
        // default to USD
        crnCode = [ExSystem sharedInstance].sys.crnCode == nil ? @"USD" : [ExSystem sharedInstance].sys.crnCode;

        // check MRU
        ListItem *lastUsedCurrency = [[MRUManager sharedInstance] getLastUsedCurrency];
        if (lastUsedCurrency != nil) // if there is mru, use it
        {
            field.fieldValue = lastUsedCurrency.liName;
            field.liKey = lastUsedCurrency.liKey == nil ? nil : [NSString stringWithFormat:@"%d", [lastUsedCurrency.liKey intValue]];
            crnCode = lastUsedCurrency.liCode;
        }
    }
    field.liCode = crnCode;
    NSLocale *locale = [NSLocale currentLocale];
    NSString *currencyName = [locale displayNameForKey:NSLocaleCurrencyCode value:field.liCode];
    field.fieldValue = currencyName;
    if (entry.pctKey != nil || entry.cctKey != nil || entry.rcKey != nil || entry.ereceiptId != nil) {
        field.access = @"RO";
    }
    field.required = @"Y";
    return field;
}

- (FormFieldData *)getExpenseTypeField
{
    FormFieldData *field = [[FormFieldData alloc] initField:@"ExpKey" label:[Localizer getLocalizedText:@"Type"] value:entry.expName ctrlType:@"EXPTYPE" dataType:@"EXPTYPE"];
    if (entry.expKey == nil) {
        field.fieldValue = [Localizer getLocalizedText:@"Undefined"];
        field.liKey = @"UNDEF";
    } else {
        field.liKey = entry.expKey;
    }
    if (entry.rcKey != nil || entry.ereceiptId != nil) {
        field.access = @"RO";
    }
    field.required = @"Y";
    return field;
}

// All OOP date is current date in local sense but GMT based, so to get today's date using [NSDate date],
// we have to convert it to string using local timezone first.
- (FormFieldData *)getDateField
{
    FormFieldData *field;

    NSDate *transactionDate = [entry transactionDate];
    if (transactionDate == nil || ![[transactionDate description] lengthIgnoreWhitespace]) {
        NSString *lastDate = [CCDateUtilities formatDateToYearMonthDateTimeZoneMidNight:[NSDate date]];
        field = [[FormFieldData alloc] initField:@"TransactionDate" label:[Localizer getLocalizedText:@"Date"] value:lastDate ctrlType:@"TIMESTAMP" dataType:@"TIMESTAMP"];
    } else {
        field = [[FormFieldData alloc] initField:@"TransactionDate" label:[Localizer getLocalizedText:@"Date"] value:[CCDateUtilities formatDateToISO8601DateTimeInString:entry.transactionDate] ctrlType:@"TIMESTAMP" dataType:@"TIMESTAMP"];
    }
    
    field.required = @"Y";
    if (entry.pctKey != nil || entry.cctKey != nil || entry.rcKey != nil || entry.ereceiptId != nil) {
        field.access = @"RO";
    }
    return field;
}

-(BOOL) isZeroAmount:(NSString*) fieldValue doubleValue:(double*) dblVal field:(FormFieldData*) fld
{
	NSScanner* scanner = [NSScanner scannerWithString:fieldValue];
// AJC - unused code 1 line below. please delete if unused by 2013-11-29
//	[scanner setLocale:[NSLocale currentLocale]];
	if ([scanner isAtEnd] == NO)
	{
        if (![scanner scanDouble:dblVal])
        {
            fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
            return FALSE;
        }
        else{
			// Make sure no garbage character at the end or 0.0 value
			if (0.0 == *dblVal)
				return TRUE;
			
			if (![scanner isAtEnd] || *dblVal == HUGE_VAL)
			{
				if (*dblVal == HUGE_VAL)
					fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_TOO_BIG_ERR_MSG"];
				else
					fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
				
				return FALSE;
			}
		}
	}
	return TRUE;
}

#pragma mark -
#pragma mark Table View Data Source Methods
-(UIView*)makeTableHeaderView:(NSString*)title
{
    UIView *expenseItHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 40)];
    int xoffset = 15;
    if ([UIDevice isPad])
        xoffset = 30;
    UILabel *headerLbl = [[UILabel alloc] initWithFrame:CGRectMake(xoffset, 5, 300, 32)];
    [headerLbl setBackgroundColor:[UIColor clearColor]];
    [headerLbl setFont:[UIFont fontWithName:@"Helvetica neueu" size:14.0]];
    [headerLbl setLineBreakMode:NSLineBreakByWordWrapping];
    [headerLbl setShadowColor:[UIColor whiteColor]];
    [headerLbl setTextColor:[UIColor colorWithRed:(69.0/255.0) green:(69.0/255.0) blue:(69.0/255.0) alpha:1.0f]];
    [headerLbl setTextAlignment:NSTextAlignmentLeft];
    [headerLbl setNumberOfLines:1];
    
//    [headerLbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
    [headerLbl setText:title];
    [expenseItHeaderView addSubview:headerLbl];
    
    return expenseItHeaderView;
}

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if (section == 0)
    {
        if (self.entry.rcKey != nil)
        {
            return [self makeTableHeaderView:[Localizer getLocalizedText:@"ExpenseIt Entry"]];
        }
        else if (self.entry.ereceiptId != nil || self.entry.eReceiptImageId != nil)
        {
            return [self makeTableHeaderView:[Localizer getLocalizedText:@"E-Receipt"]];
        }
    }
    
    return nil;
}

-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    if (section == 0)
    {
        if (self.entry.rcKey != nil)
        {
        return [[self makeTableHeaderView:[Localizer getLocalizedText:@"ExpenseIt Entry"]] frame].size.height;
        }
        else if ([MobileEntryManager isSmartMatched:self.entry]) //MOB-21064
        {
            return [[self makeTableHeaderView:[Localizer getLocalizedText:@"Matched Expense"]] frame].size.height;
        }
        else if (self.entry.ereceiptId != nil || self.entry.eReceiptImageId != nil)
        {
        return [[self makeTableHeaderView:[Localizer getLocalizedText:@"E-Receipt"]] frame].size.height;
        }
    }
    return 0;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [self.sections count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionName = [self.sections objectAtIndex:section];
    if ([sectionName isEqualToString:kSectionCardAuth]) {
        return 1;
    }
    
    if(section == 0) {
    	// 1 is the receipt select, 2 is the report select
        return 1;
        // TODO: uncomment this after the 9.7 branch.  This enables add quick expense to report!!
        //return 2;
    } else {
        return [super tableView:tableView numberOfRowsInSection:section];
    }
}

-(CGFloat)getMessageTextHeight:(NSString*) text withWidth:(CGFloat)width
{
    CGFloat height =  [FormatUtils getTextFieldHeight:width Text:text Font:[UIFont fontWithName:@"HelveticaNeue" size:14.0f]];
    
    if((height) < 36)
        height =  36;
    
    height = height + 4;
    
    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //    if([ExSystem isLandscape])
    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //        w = 420;
    return height;
}


-(UITableViewCell *) makeMessageCell:(UITableView*)tblView withText:(NSString*)text withImage:(NSString*)imgName
{
    DrillCell *cell = (DrillCell*)[tblView dequeueReusableCellWithIdentifier:@"ExceptionCell"];
	if (cell == nil)
	{
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ExceptionCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[DrillCell class]])
                cell = (DrillCell *)oneObject;
	}
	
    [cell resetCellContent:text withImage:imgName];
    
    CGRect r = cell.lblName.frame;
    CGFloat width = 260;
    CGFloat height = [self getMessageTextHeight:text withWidth:width];
    // grouped section table takes 20 pixels away
    cell.lblName.frame = CGRectMake(r.origin.x, 0, width+10, height);
    
    return cell;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    if(indexPath.section == 0)
    {
        if (indexPath.row == 0) {
            static NSString *cellIdentity =  @"DrillCell";
            
            DrillCell *cell = (DrillCell *)[tableView dequeueReusableCellWithIdentifier: cellIdentity];
            if (cell == nil)  
            {
                NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"DrillCell" owner:self options:nil];
                for (id oneObject in nib)
                    if ([oneObject isKindOfClass:[DrillCell class]])
                        cell = (DrillCell *)oneObject;
            }
            // MOB-14618 - check if the receipt is present
            if (self.receipt.receiptId != nil || self.receipt.localReceiptId != nil)
                [cell resetCellContent:[Localizer getLocalizedText:@"View Receipt"] withImage:@"icon_receipt_button"];
            else
                [cell resetCellContent:[Localizer getLocalizedText:@"Add Receipt"] withImage:@"icon_receipt_button"];
            
            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
            return cell;
        } else {

            // Handle select report
            static NSString *cellIdentity =  @"DrillCell";
            DrillCell *cell = (DrillCell *)[tableView dequeueReusableCellWithIdentifier: cellIdentity];
            if (cell == nil)
            {
                NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"DrillCell" owner:self options:nil];
                for (id oneObject in nib)
                    if ([oneObject isKindOfClass:[DrillCell class]])
                        cell = (DrillCell *)oneObject;
            }

            if (self.selectedReport == nil) {
                [cell resetCellContent:@"Select Report" withImage:@"icon_expensereport"];
            } else {
                [cell resetCellContent:[self.selectedReport reportName] withImage:@"icon_expensereport"];
            }

            [cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
            return cell;
        }
    }
    else
    {

        NSString *sectionName = [self.sections objectAtIndex:indexPath.section];
        if ([sectionName isEqualToString:kSectionCardAuth])
        {
            return [self makeMessageCell:tableView withText:[@"CARD_AUTH_MSG" localize] withImage:@"icon_card_gray"];
        }
        else
            return [super tableView:tableView cellForRowAtIndexPath:indexPath];
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{    
    NSString *sectionName = [self.sections objectAtIndex:indexPath.section];
    if ([sectionName isEqualToString:kSectionCardAuth])
	{
		int exceptionW = 260;
		CGFloat height =  [self getMessageTextHeight:[@"CARD_AUTH_MSG" localize] withWidth:exceptionW];
        
		return height;
	}

    if(indexPath.section == 0)
        return 44;
    else
        return 59;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
    [tableView deselectRowAtIndexPath:newIndexPath animated:YES];
    
    if(newIndexPath.section == 0)
    {
        if (newIndexPath.row == 0) {
            BOOL isOnline = [ExSystem connectedToNetwork];
            BOOL isNewExpense = [self isNewQuickExpense];
            BOOL isExpenseQueued = [self isQueuedQuickExpense];
            
           // MOB-14916: ExpenseIT: User is able to Detach receipt
           // for 9.5 release the expenseIT entry receipts are read/view only.  
            BOOL allowReceiptEdits = (isOnline || isNewExpense || isExpenseQueued) && ( self.entry.rcKey == nil && self.entry.ereceiptId == nil);
            
            BOOL willQueueExpense = (isExpenseQueued || (!isOnline && isNewExpense));
            BOOL excludeReceiptStoreOption = willQueueExpense; // User not allowed to select from receipt store for queued expense
            if (isExpenseQueued)
            {
                NSDictionary *pbag = @{@"Queued Receipt": @YES};
                [Flurry logEvent:@"Offline: Viewed" withParameters:pbag];
            }
            
            [self showReceiptViewerAndAllowEdits:allowReceiptEdits excludeReceiptStoreOption:excludeReceiptStoreOption];
        } else {
            // TEST expense to report code
            SelectReportViewController *vc = [[SelectReportViewController alloc] init];

            vc.delegate = self;
            [self.navigationController pushViewController:vc animated:YES];
        }
    }
    else
    {
        [super tableView:tableView didSelectRowAtIndexPath:newIndexPath];
        return;
     }
    
}

-(void) didChooseReport:(ReportData *)reportData
{
    self.selectedReport = reportData;
}

#pragma mark -
#pragma mark Cell data initilation Methods 
-(UIColor*) getLabelColor
{
    return [UIColor colorWithRed:100.0/255 green:100.0/255 blue:100.0/255 alpha:1];
}

#pragma mark -
#pragma mark Editing delegate Methods


-(void)showExpenseTypeEditor:(FormFieldData*) field
{
    ExpenseTypesViewController *expenseTypesVC = [[ExpenseTypesViewController alloc] init];
	expenseTypesVC.parentMVC = self;
	expenseTypesVC.delegate = self;
	expenseTypesVC.expenseTypesEndPointVersion = @"";	// Empty string means first version of endpoint
	if([UIDevice isPad])
		expenseTypesVC.modalPresentationStyle = UIModalPresentationFormSheet;
	[self presentViewController:expenseTypesVC animated:YES completion:nil];
}

- (void)saveSelectedExpenseType:(ExpenseTypeData*) et
{
    
    FormFieldData *field = [self findEditingField:@"ExpKey"];
    field.liKey = et.expKey;
    field.fieldValue = et.expName;
	[self dismissViewControllerAnimated:YES completion:nil];
    
	[self fieldUpdated:field];

}

-(void) fieldUpdated:(FormFieldData*) field
{
    [super fieldUpdated:field];
    
    // MOB-21225
    // Dummy solution for avoiding crash now .... fix later -- Ray
    // TODO: I guess we do a duplicate clear cache call for message control system somewhere. That's why we lost connection of coredata.
    if([Config isEreceiptsEnabled] && entry.isHidden == nil)
    {
        entry = [[MobileEntryManager sharedInstance] fetchBySmartExpenseId:self.smartExpenseId];
    }
    
    if ([field.iD isEqualToString:@"LocName"])
    {
        NSDictionary *extra = (NSDictionary*)field.extraDisplayInfo;
        NSString *crn = nil;
        if (extra != nil) {
            crn = extra[@"CrnCode"];
        }
        
        FormFieldData *crnField = [self findEditingField:@"TransactionCurrencyName"];
        // MOB-13690 -donot allow currency change for cc entries
        if (crnField != nil && crn != nil && ![MobileEntryManager isCardTransaction:entry] )
        {
            entry.crnCode = crn;
            crnField.liCode = crn;
            NSLocale *locale = [NSLocale currentLocale];
            crnField.fieldValue = [locale displayNameForKey:NSLocaleCurrencyCode value:crn];
            // MOB-11717 Clear likey set previously 
            crnField.liKey = nil;
            
            // MOB-14740
            // Save to currency to MRU if user changed location and currency changed automatically.
            NSInteger keyVal = crnField.liKey == nil ? NSNotFound : [crnField.liKey integerValue];
            [[MRUManager sharedInstance]addMRUForType:crnField.iD value:crnField.fieldValue key:&keyVal code:crnField.liCode];
            [super fieldUpdated:crnField];
        }
    }
    
    
    [self makeSaveButton:nil];
}

#pragma mark - Queue stuff

-(void) showQueueAlert
{
    //Your expense has been queued
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle:[Localizer getLocalizedText:@"Expense Queued"]
                          message:[Localizer getLocalizedText:@"Your expense has been queued"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                          otherButtonTitles:nil];
    [alert show];
      
    return;
}

-(void) writeToQueue
{
    // Look up the quick expense in the upload queue
    NSString *originalQuickExpenseId = self.entry.localId;
    EntityUploadQueueItem *originalQuickExpenseQueueItem = (originalQuickExpenseId == nil ? nil :[[UploadQueueItemManager sharedInstance] fetchByEntityInstanceId:originalQuickExpenseId entityTypeName:@"EntityMobileEntry"]);

    // Look up the original receipt in the upload queue, i.e. the one the expense depended on before it changed.
    NSString *originalLocalReceiptImageId = self.entry.localReceiptImageId;
    EntityUploadQueueItem *originalReceiptQueueItem = (originalLocalReceiptImageId == nil ? nil : [[UploadQueueItemManager sharedInstance] fetchByEntityInstanceId:originalLocalReceiptImageId entityTypeName:@"Receipt"]);

    // The quick expense's original creation date, if any.
    NSDate *expenseCreationDate = (originalQuickExpenseQueueItem == nil ? [NSDate date] : originalQuickExpenseQueueItem.creationDate);

    // The latestReceiptQueueItem points to the whichever queued receipt is the latest. If the receipt didn't change, then it's the original receipt.  If the receipt changed, then this variable will point to it the new receipt as soon as it gets created (see below)
    EntityUploadQueueItem *latestReceiptQueueItem = originalReceiptQueueItem;

    BOOL wasReceiptDeleted = (self.receipt == nil && originalLocalReceiptImageId != nil);
    BOOL hasReceiptNowButHadNoneBefore = (self.receipt != nil && originalLocalReceiptImageId == nil);
    BOOL currentReceiptDiffersFromOldReceipt = (self.receipt != nil && self.receipt.localReceiptId != originalLocalReceiptImageId);
    BOOL didReceiptChange = (wasReceiptDeleted || hasReceiptNowButHadNoneBefore || currentReceiptDiffersFromOldReceipt);
    if (didReceiptChange)
    {
        // Remove the dependency on the old receipt. This will cause the old receipt to become a stand-alone receipt in the queue.
        if ([originalQuickExpenseQueueItem.relRequires containsObject:originalReceiptQueueItem])
            [originalQuickExpenseQueueItem removeRelRequiresObject:originalReceiptQueueItem];
        
        // Grab the receipt from the queue
        if (self.receipt.localReceiptId)
        {
            latestReceiptQueueItem = [[UploadQueueItemManager sharedInstance] fetchByEntityInstanceId:self.receipt.localReceiptId entityTypeName:@"Receipt"];
            
            // The receipt's creation date
            NSDate *adjustedReceiptCreationDate = [expenseCreationDate dateByAddingTimeInterval:1.0]; // One second after than expenseCreationDate
            
            latestReceiptQueueItem.creationDate = adjustedReceiptCreationDate;
        }

        else
            latestReceiptQueueItem = nil;
    }
    
    // Check if this quick expense is already in the queue
    if (originalQuickExpenseQueueItem != nil)
    {
        // Update existing queued expense in the following steps
        // Step 1. Update attributes of existing EntityMobileEntry
        //MOB-13161
        entry.localReceiptImageId = latestReceiptQueueItem.entityInstanceId;
 
         // Step 2. Add dependency on receipt, if any.
        if (latestReceiptQueueItem != nil)
        {
            if (![originalQuickExpenseQueueItem.relRequires containsObject:latestReceiptQueueItem])
                [originalQuickExpenseQueueItem addRelRequiresObject:latestReceiptQueueItem];
        }
    }
    else
    {
        NSLog(@" entry.receiptid: %@ , latestreceipqueueitem.entityinstanceid: %@",  entry.localReceiptImageId ,latestReceiptQueueItem.entityInstanceId);
        entry.localReceiptImageId = latestReceiptQueueItem.entityInstanceId;
        
        // Queue the expense entry
        // MOB-12986: Localid is just any id, 
        self.entry.localId = [PostMsgInfo getUUID];
        EntityUploadQueueItem *newQuickExpenseQueueItem = [UploadQueue queueItemWithId:self.entry.localId entityTypeName:@"EntityMobileEntry" creationDate:expenseCreationDate];
        
        // Make the expense dependent upon the receipt in the queue
        if (latestReceiptQueueItem != nil)
            [newQuickExpenseQueueItem addRelRequiresObject:latestReceiptQueueItem];
    }

    NSError *error = nil;
    if (![self.managedObjectContext save:&error])
        NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]);
    else
    {
        // MOB-11276 
        if(latestReceiptQueueItem !=nil)
        {
            NSDictionary *dict = @{@"Type": @"Offline Quick Expense with Receipt"};
            [Flurry logEvent:@"Offline: Create" withParameters:dict];
        }
        else
        {
            NSDictionary *dict = @{@"Type": @"Offline Quick Expense"};
            [Flurry logEvent:@"Offline: Create" withParameters:dict];
        }
    }
}

#pragma mark - Save stuff
-(void) saveForm:(BOOL) cpDownToChildForms
{
    // Call processFieldsToEntry to fill in the fields of self.entry
    [self processFieldsToEntry:nil];
    
    if (self.isInUploadQueue) // Modified queued quick expense
    {
        [self writeToQueue];
        [self executeActionAfterSave];
    }
    else if (![ExSystem connectedToNetwork]) // Made new quick expense while offline
    {
        [self writeToQueue];
        if (promptSaveAlert)
        {
            [MobileAlertView dismissAllMobileAlertViews];
            self.promptSaveAlert = NO;
        }
        [self performSelector:@selector(showQueueAlert) withObject:nil afterDelay:0.5f];
        [self executeActionAfterSave];
    }
    else // Modified or made new quick expense while online
    {
        self.isSaving = YES;

        [self performSelector:@selector(forceSaveThread:) withObject:nil afterDelay:0.1f];
        
        // AJC - this needs to go into a Flurry class
        bool new = [self isNewQuickExpense];
        NSString* newValue = new ? @"New" : @"Edit";
        bool containsReceipt = entry.hasReceipt;
        NSString* containsReceiptValue = containsReceipt ? @"Yes" : @"No";
        NSDictionary *dict = @{@"Edit New": newValue, @"Constains Receipt": containsReceiptValue};
        [Flurry logEvent:@"Mobile Entry: Saved" withParameters:dict];
    }
}

-(void) forceSaveThread:(id)sender
{
    if (updatedReceiptImageId)
    {
        entry.hasReceipt = @"Y";
        updatedReceiptImageId = NO;
        self.entry.receiptImageId = receipt.receiptId;
    }
    
    [self saveOOPE:self];
}

//thread routine
-(void)saveOOPE:(id)sender
{
    [WaitViewController showWithText:[Localizer getLocalizedText:@"Saving Expense"] animated:YES fullScreen:NO];

    [self processFieldsToEntry:nil];
    [self saveToMRU];

	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:OUT_OF_POCKET_FORM, @"TO_VIEW", entry, @"ENTRY", nil];
	[[ExSystem sharedInstance].msgControl createMsg:ME_SAVE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void) makeSaveButton:(id)sender
{
    UIBarButtonItem *btnSave = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave target:self action:@selector(actionSave:)];
    
    if(self.isDirty && [self canEdit])
       [btnSave setEnabled:YES];
    else
       [btnSave setEnabled:NO];
    self.navigationItem.rightBarButtonItem = btnSave;
}

// it appears that location is automatically saved by the picker. currency and expense type has to be manually saved. :/
-(void) saveToMRU
{
    for(FormFieldData *field in allFields)
    {
        if([field.iD isEqualToString:@"TransactionCurrencyName"])
        {
            NSInteger  keyVal = field.liKey == nil ? NSNotFound : [field.liKey integerValue] ;
            [[MRUManager sharedInstance]addMRUForType:field.iD value:field.fieldValue key:&keyVal code:field.liCode];
        }
    }

    NSInteger  keyVal =  NSNotFound;
    [[MRUManager sharedInstance]addMRUForType:@"mru_expenseType" value:entry.expName key:&keyVal code:entry.expKey];
}

-(void) processFieldsToEntry:(id)sender
{
    // MOB-21195
    // Dummy solution for avoiding crash now .... fix later -- Ray
    // TODO: I guess we do a duplicate clear cache call for message control system somewhere. That's why we lost connection of coredata.
    if([Config isEreceiptsEnabled] && entry.isHidden == nil)
    {
        self.entry = [[MobileEntryManager sharedInstance] fetchBySmartExpenseId:self.smartExpenseId];
    }
    for(FormFieldData *field in allFields)
    {
        if([field.iD isEqualToString:@"TransactionDate"])
        {
            // Convert back to GMT date
            entry.transactionDate = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:field.fieldValue];
        }
        else if([field.iD isEqualToString:@"ExpKey"])
        {
            if(field.liKey != nil)
                entry.expKey = field.liKey;
            else
                entry.expKey = field.liCode;
            
            entry.expName = field.fieldValue;
        }
        else if([field.iD isEqualToString:@"TransactionAmount"])
        {
            entry.transactionAmount = [NSDecimalNumber decimalNumberWithString:[field getServerValue]];
        }
        else if([field.iD isEqualToString:@"TransactionCurrencyName"])
        {
            entry.crnCode = field.liCode;
        }
        else if([field.iD isEqualToString:@"VENDOR"])
        {
            entry.vendorName = field.fieldValue;
        }
        else if([field.iD isEqualToString:@"LocName"])
        {
            entry.locationName = field.fieldValue; 
        }
        else if([field.iD isEqualToString:@"CommentEx"])
        {
            entry.comment = field.fieldValue;
        }
    }
}

#pragma mark -
#pragma mark Receipt methods

-(BOOL) hasReceipt
{
	return (entry == nil || entry.hasReceipt == nil)? FALSE : [entry.hasReceipt isEqualToString:@"Y"];
}

//wipes the receipt... should probably alert the user and ask them if they still want to kill the receipt
-(void)btnClearReceipt
{
    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //entry.receiptImage = nil;
	entry.hasReceipt = @"N";
    self.isDirty = YES;
}

#pragma mark - Overrides
-(void)updateSaveBtn
{
    [self makeSaveButton:nil];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    self.promptSaveAlert = YES;
    if ([self isReceiptUploadAlertTag:alertView.tag]) {
        [self saveOOPE:self];
    }
	else if (alertView.tag == kAlertViewMissingReqFlds)
	{
        [super alertView:alertView clickedButtonAtIndex:buttonIndex];
	}
    else if ([self isSaveConfirmDialog:alertView.tag])
	{
		self.isFromAlert = YES;
		if (buttonIndex == 1) // Yes
		{
			self.actionAfterSave = alertView.tag;
			[self actionSaveImpl];
		}
		else if (buttonIndex == 2) // No
		{
            [self setIsDirty:NO];
            
            // user decided not to save while making a new quick expense
            if( self.isNewQuickExpense )
            {
                // track the cancel
                [Flurry logEvent:@"Mobile Entry: Cancelled"];
                [self closeMe:nil];
            }
            
            [self.navigationController popViewControllerAnimated:YES];
		}
		
	}
    else
        [self clearActionAfterSave];
}

-(void) fieldCanceled:(FormFieldData*) field
{
}

- (void)cancelExpenseType
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(BOOL) canSaveOffline
{
    // We can save offline, i.e. queue this expense, IF this is a NEW expense.
    // Existing expenses cannot be saved offline.
    // MOB-13656
    return (self.entry.key == nil && self.entry.pctKey == nil  && self.entry.cctKey == nil );
}

-(BOOL) isNewQuickExpense
{
    BOOL isNew = [MobileEntryManager getKey:self.entry] == nil;
    return isNew;
}

-(BOOL) isQueuedQuickExpense
{
    // Should check the upload queue if this is already in queue.
    // MOB-13656 - Look up the quick expense in the upload queue
    NSString *originalQuickExpenseId = self.entry.localId;
    EntityUploadQueueItem *queueItem = (originalQuickExpenseId == nil ? nil :[[UploadQueueItemManager sharedInstance] fetchByEntityInstanceId:originalQuickExpenseId entityTypeName:@"EntityMobileEntry"]);

    return [MobileEntryManager getKey:self.entry] == nil && queueItem != nil;
    // AJC - unused code 1 line below. please delete if unused by 2013-11-29
    //  return self.isInUploadQueue;
}

-(BOOL) shouldAllowOfflineEditingwAtIndexPath:(NSIndexPath *)indexPath
{
    return ([self isNewQuickExpense] || [self isQueuedQuickExpense]);
}

-(BOOL) shouldUseCacheOnlyForListEditor:(ListFieldEditVC*)lvc
{
    // MOB-11290 A very slow device could be parsing cached currency data when new currency data arrives from the server and is parsed concurrently with the cached currency data.  The parsing code in ListFieldSearchData is not thread safe and could crash.  Therefore, we will check whether cached data is available and, if so, use it exclusively.  Note that currency data will be refreshed when the user logs in.  RootViewController and iPadHomeVC request the currency data from the server and purposely skip the cache.
    BOOL isCurrencyField = (lvc.field.iD != nil && [lvc.field.iD isEqualToString:@"TransactionCurrencyName"]);
    if (isCurrencyField)
    {
        BOOL cachedDataExists = [MsgControl hasCachedData:LIST_FIELD_SEARCH_DATA UserID:(NSString *)[ExSystem sharedInstance].userName RecordKey:@"TransactionCurrencyName"];
        return cachedDataExists;
    }
    
    return NO;
}

#pragma mark ReceiptEditorDelegate
-(void) receiptDeleted:(Receipt*) rcpt
{
    self.receipt = nil;
    [self setIsDirty:YES];
}

-(void) receiptQueued:(Receipt*) rcpt
{
    self.receipt = rcpt;
    [self setIsDirty:YES];
}

-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint;
{
    self.receipt = rcpt;
    //    entry.updatedImage = YES;

    if (self.receipt.receiptId != nil && ![self.receipt.receiptId isEqualToString:self.entry.receiptImageId])
    {
        //MOB-13806 - donot save receiptid until save dialogue is confirmed.
        // AJC - unused code 1 line below. please delete if unused by 2013-11-29
        //        self.entry.receiptImageId = self.receipt.receiptId;
        // AJC - unused code 1 line below. please delete if unused by 2013-11-29
        //        self.entry.hasReceipt = @"Y";
        self.updatedReceiptImageId = YES;
        [self setIsDirty:YES];
        
        NSMutableArray* ixPaths = [[NSMutableArray alloc] init];
        NSUInteger _path[2] = {0,0};
        // AJC - unused code 1 line below. please delete if unused by 2013-11-29
        //{[self getReceiptSectionIndex], 0};
        NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
        [ixPaths addObject:_indexPath];
        [self.tableList reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
    }
}

// Dont do anything . Implemented to supress warnings
- (void)receiptDisplayed:(Receipt *)rcpt
{
	return ;
}

#pragma mark - FormViewControllerBase override
- (BOOL)canEdit
{
    return (self.entry.rcKey == nil && [super canEdit]);
}

#pragma mark Receipt Action
- (void)showReceiptViewerAndAllowEdits:(BOOL)allowEdits excludeReceiptStoreOption:(BOOL)excludeReceiptStoreOption
{
    // As of January 22, 2013, there is no API for deleting a receipt for an existing expense.  Therefore, a receipt can only be deleted for a brand new quick expense that has not yet been saved or for one that is queued.
    BOOL isNewOrQueuedExpense = self.entry.key == nil ;
    BOOL isExpenseQueued = [self isQueuedQuickExpense];
    BOOL isOffline = ![ExSystem connectedToNetwork];
    
	ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"RECEIPT_VIEWER"];
    receiptView.delegate = self;
    receiptView.canDelete = isNewOrQueuedExpense;
    receiptView.canUpdate = allowEdits;

    receiptView.canUseReceiptStore = (allowEdits && !isExpenseQueued && !isOffline);
    receiptView.supportsOffline = isNewOrQueuedExpense;
    receiptView.mustQueue = (isExpenseQueued || isOffline);
    [receiptView setSeedData:self.receipt];

    [self.navigationController pushViewController:receiptView animated:YES];

}


#pragma mark - Close
-(void) closeMe:(id)sender
{
    // checking for the views because we are not using standard back button. need to check if there one more views in the stack so not dissmissing everything and leading to confusion
    NSArray *viewControllers = self.navigationController.viewControllers;
    if ( [viewControllers count] > 1){
        [self.navigationController popViewControllerAnimated:NO];
    }
    else {
        [self dismissViewControllerAnimated:YES completion:nil];
    }
}

#pragma mark App Rating Methods
-(void)afterChoiceToRateApp
{
    if ([self isViewLoaded]) {
        [self hideWaitView];
        [self executeActionAfterSave];
    }
}

@end
