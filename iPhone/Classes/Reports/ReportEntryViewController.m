//
//  ReportEntryViewController.m
//  ConcurMobile
//
//  Created by yiwen on 4/28/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportEntryViewController.h"
// Helpers
#import "ExSystem.h" 

#import "ConcurMobileAppDelegate.h"
#import "LabelConstants.h"
#import "FormatUtils.h"
#import "NSStringAdditions.h"
#import "DateTimeFormatter.h"
#import "MobileAlertView.h"
#import "MobileActionSheet.h"
// Data Handlers
#import "CarRatesData.h"
#import "ReportData.h"
#import "FormFieldData.h"
#import "EntryData.h"
#import "ExCarDistanceToDateData.h"
#import "RequestController.h"
#import "SaveReportEntryData.h"
#import "DeleteReportEntryData.h"
#import "ExchangeRateData.h"
#import "ReportEntryFormData.h"
#import "SaveReportEntryReceipt.h"
// Managers
#import "ExpenseTypesManager.h"
#import "ExReceiptManager.h"
// View Controllers
#import "ReportDetailViewController_iPad.h"
#import "ItemizeHotelViewController.h"
#import "ReportItemListViewController.h"
#import "ReceiptEditorVC.h"
// Custom cells
#import "SummaryCellMLines.h"
#import "ReportAttendeesViewController.h"
#import "ReportDetailViewController.h"
#import "ReceiptCacheManager.h"
//    MOB-8451:MRU expenseTypes
#import "MRUManager.h"
// MOB-12832 change currency with location
#import "ListFieldSearchData.h"
#import "CarDetailData.h"

#import "CCDateUtilities.h"

#import "UserConfig.h"
#import "Localizer.h"
#import "ItineraryAllowanceAdjustmentViewController.h"

#import "ConditionalFieldsList.h"

#define kSectionEntryName @"Entry"
#define kSectionDrillsName @"Drills"
//#define kSectionCommentsName @"COMMENTS"
#define kSectionDetailsName @"Details"
#define kSectionTaxForms @"kSectionTaxForms"

#define kSectionExceptionsName @"EXCEPTIONS"
#define kSectionReceiptRequiredName @"ReceiptRequired"
// keys to localized strings
#define kViewItemizations @"APPROVE_EXPENSE_DETAILS_VIEW_ITEMIZATIONS"
#define kViewAttendees @"APPROVE_EXPENSE_DETAILS_VIEW_ATTENDEES"
#define kTravelAllowance @"Adjust Allowance"
#define kAlertViewConfirmSaveUponItem		101801
#define kAlertViewConfirmSaveUponReceipt	101802
#define kAlertViewConfirmExpTypeSwitch      101811

@interface ReportEntryViewController ()
- (void)showReceiptViewer;
-(BOOL) fetchCarDistanceToDate;
-(NSString*) getReportCrnCode;
-(void) refreshOdometerStart:(NSString*)carKey;
// MOB-11022 Allow use of entry's car rates if rpt crn not equal to emp crn.
-(CarRatesData*) getCarRates;
- (void)sendViewEntryReceiptAuditMsg;
- (void)resetReceipt:(EntryData*) theEntry;

@property BOOL hasCloseButton;
@property BOOL requirePaperReceipt;

// MOB-21970: if it is add a new expense, need to get the start odometer from server
@property BOOL isAddNewExpense;
@property BOOL isCarKeySwitched;

@end

@implementation ReportEntryViewController

@synthesize attendees;

-(id) initWithCloseButton:(BOOL)withCloseButton
{
    self = [super initWithNibName:@"EditFormView" bundle:nil];
    if (self)
    {
        self.hasCloseButton = withCloseButton;
    }
    return self;
}

- (void)viewDidLoad
{
    if(self.hasCloseButton)
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionBack:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }
    
    [super viewDidLoad];
}

#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return APPROVE_EXPENSE_DETAILS;
}


#pragma mark - Init data
- (void)setSeedData:(NSDictionary*)pBag
{
    if ([pBag[@"SOURCE_SECTION"] isEqualToString:@"REPORT_APPROVAL_SECTION"]) {
        self.isReportApproval = YES;
    }
    [self setSeedData:pBag[@"REPORT"] entry:pBag[@"ENTRY"] role:pBag[@"ROLE"]];

}
- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry role:(NSString*) curRole
{
    self.role = curRole;
    [self loadEntry:thisEntry withReport:report];
    if (![thisEntry isDetail])
    {
        isLoading = YES;
        [self fetchEntryDetail];
    }
    else // get cached data if we already loaded the report
    {
        isLoading = YES;
        [self fetchEntryDetail:NO];

    }
    [self setupToolbar];
}

-(NSString*) getReportCrnCode
{
    NSString* crnCode = rpt.crnCode == nil? [ExSystem sharedInstance].sys.crnCode :rpt.crnCode;
    if (crnCode == nil)
        crnCode = @"USD";
    
    return crnCode;
}

-(CarRatesData*) getCarRates
{
    // MOB-11022 Allow use of entry's car rates if rpt crn not equal to emp crn.  By forming a CarRatesData object, we can reuse existing rate fetching functions
    if (carRates != nil)
        return carRates;

    if (self.entry.carConfig != nil)
    {
        carRates = [[CarRatesData alloc] init];
        (carRates.items)[self.entry.carConfig.carcfgKey] = self.entry.carConfig;
        [carRates.keys addObject:self.entry.carConfig.carcfgKey];
        return carRates;
    }

    return [ConcurMobileAppDelegate findRootViewController].carRatesData;
}

- (void)resetReceipt:(EntryData*) theEntry
{
    // If receipt image id changed, re-create the Receipt object
    if (theEntry.receiptImageId != nil && ![theEntry.receiptImageId isEqualToString:self.receipt.receiptId])
    {
        self.receipt = [[Receipt alloc] init];
        self.receipt.receiptId = theEntry.receiptImageId;
    }
}

// Replace both loadEntry and updateEntry
- (void) loadEntry:(EntryData*) thisEntry withReport:(ReportData*) report
{
    if (report != nil)
        self.rpt = report;

    self.entry = thisEntry;
    self.attendees = [[NSMutableArray alloc] init];
    
	if (self.entry.attendeeKeys != nil && self.entry.attendees != nil)
	{
		for (NSString* attendeeKey in self.entry.attendeeKeys)
		{
			AttendeeData *attendee = (self.entry.attendees)[attendeeKey];
			AttendeeData *copyOfAttendee = [attendee copy];
			[self.attendees addObject:copyOfAttendee];
		}
	}
    
	[self initFields:nil];
    
    [self recalculateSections];
    [self refreshView];
    
    // MOB-12257 Save receipt to receipt store before entry save, and no longer need to block navigation here
/*    if (entryUpdatedImageId)
    {
        [self saveNewEntryReceipt];
    }*/
}

// For subclass to customize details section
-(NSArray*) getFieldsInDetailsSection
{
    if (self.sectionFieldsMap == nil)
        return nil;
    // Handle fields in taxforms also 
    return (self.sectionFieldsMap)[kSectionDetailsName];
}

-(void) recalculateSections
{
    NSMutableArray          *newSections = [[NSMutableArray alloc] init];
    NSMutableDictionary     *newSectionDataMap = [[NSMutableDictionary alloc] init]; // Non-field 
    
//	NSMutableArray *exceptionData = [[NSMutableArray alloc] initWithObjects:nil];
//	NSMutableArray *commentsData = [[NSMutableArray alloc] initWithObjects:nil];
//	
//	if (entry.comments != nil) 
//	{
//		for(NSString *key in entry.comments)
//		{
//			CommentData *c = [entry.comments objectForKey:key];
//			[commentsData addObject:c];
//		}
//	}
//	
	if([self.entry.exceptions count] > 0)
	{
		newSectionDataMap[kSectionExceptionsName] = self.entry.exceptions;
		[newSections addObject:kSectionExceptionsName];
	}
    
//	if([commentsData count] > 0)
//	{
//		[newSectionDataMap setObject:commentsData forKey:kSectionCommentsName];
//		[newSections addObject:kSectionCommentsName];
//	}
//	
    // MOB-21147 - skip image required flag if ereceiptimagid is not nil
	if(![@"Y" isEqualToString: self.entry.hasMobileReceipt] && (self.entry.eReceiptImageId == nil) &&
       ((self.entry.receiptRequired != nil && [self.entry.receiptRequired isEqualToString:@"Y"]) ||
        (self.entry.imageRequired != nil && [self.entry.imageRequired isEqualToString:@"Y"])))
    {
        NSArray* rr = @[@"ReceiptRequired"];
        newSectionDataMap[kSectionReceiptRequiredName] = rr;
        [newSections addObject:kSectionReceiptRequiredName];
    }
    
	NSMutableArray* detailsData = (self.sectionFieldsMap)[kSectionDetailsName];
	newSectionDataMap[kSectionDetailsName] = detailsData;
	[newSections addObject:kSectionDetailsName];
	
    // 10862 - Add taxforms as a seperate section
    //if kSectionTaxForms exists in sectionfieldsmap
    if ((self.sectionFieldsMap)[kSectionTaxForms]) {
        NSMutableArray *taxformsData = (self.sectionFieldsMap)[kSectionTaxForms];
        newSectionDataMap[kSectionTaxForms] = taxformsData;
        [newSections addObject:kSectionTaxForms];
    }
    else
    {
        // Remove the taxsection if sectionFieldsMap is not present
        [self removeTaxFormsSections];
        [newSections removeObject:kSectionTaxForms];
        [newSectionDataMap removeObjectForKey:kSectionTaxForms];
    }
	
	[self configureDrillData:newSectionDataMap sections:newSections];
	
    self.sections = newSections;
    self.sectionDataMap = newSectionDataMap;

}

-(NSDictionary*) getComments
{
    return self.entry.comments;
}

-(void) configureDrillData:(NSMutableDictionary*)newSectionDataMap sections:(NSMutableArray*)newSections
{
	NSMutableArray *drillData = [[NSMutableArray alloc] initWithObjects:nil];
    
	NSString* expKey = [self getCurrentExpType];
    if (expKey == nil)
        expKey = self.entry.expKey;
	ExpenseTypeData* expType = expKey == nil? nil :[[ExpenseTypesManager sharedInstance] 
                                                    expenseTypeForVersion:@"V3" policyKey:self.rpt.polKey 
                                                    expenseKey:expKey forChild:NO];
	BOOL itemizeNotAllowed = expType == nil || [expType itemizeNotAllowed];
    
    // MOB-9467 revert MOB-7283, enable itemization for SU app
    if (([self.entry.isItemized isEqualToString:@"Y"] && [expKey isEqualToString:self.entry.expKey])|| 
        (self.entry.parentRpeKey== nil && !itemizeNotAllowed && [self canEdit] &&
         !(self.isCarMileage || [expType isPersonalCarMileage]) && self.entry.rpeKey != nil 
         && ![@"Y" isEqualToString:self.entry.isPersonal])) 
    {
        [drillData addObject:kViewItemizations];
    }
    
// MOB-5085	
//	if (!isCarMileage && ![self isPersonalCarMileageExpType:expKey])
//	{
		FormFieldData *attendeesField = [self lookupAttendeesField];
		if (attendeesField != nil && ![attendeesField.access isEqualToString:@"HD"])
		{
			[drillData addObject:kViewAttendees];
		}
//	}
	
    // Mob-5795 Allow Receipts menu to be displayed if there's no rpeKey. Let it save receipt after saving the new report entry
	if (!self.isCarMileage && self.entry.parentRpeKey == nil)
        // MOB-5964 do not display receipts for itemizations
        // MOB-10872 do not display "Add receipts" for approvals
	{
		// MOB-11958, MOB-12160
        if( [self canUpdateReceipt] || [self.entry.hasMobileReceipt isEqualToString:@"Y"] )
        {
            [drillData addObject:@"Receipt"];
        }
    }

    if ([[ExSystem sharedInstance] siteSettingHasFixedTA])
    {
        // Can I see the HasTravelAllowance setting here?
        if(self.entry.hasTravelAllowance != nil && [self.entry.hasTravelAllowance isEqualToString:@"Y"])
        {
            [drillData addObject:kTravelAllowance];
        }
    }

    [newSectionDataMap removeObjectForKey:kSectionDrillsName];
	for (int i = 0; i < [newSections count]; i++)
	{
		NSString* sectionName = newSections[i];
		if ([sectionName isEqualToString:kSectionDrillsName])
		{
			[newSections removeObjectAtIndex:i];
			break;
		}
	}
    
	if([drillData count] > 0)
	{
		newSectionDataMap[kSectionDrillsName] = drillData;
		[newSections insertObject:kSectionDrillsName atIndex:0];
	}
    
}
-(void) respondToConnectionFailure:(Msg* )msg
{
    isLoading = NO; // Turning off isLoading to prevent base class from showing loading view
    [super respondToConnectionFailure:msg];
}

-(void)showOfflineView:(MobileViewController*)callingViewController
{
    [super showOfflineView:callingViewController];
    self.negDataView.frame = self.view.frame; // Make the offline view should cover everything, not just the table.
}

- (void) refreshView
{
    // In the case of iPhone, the request for data is sent to the server before the view
    // is loaded.  Therefore, a connection failure will not show the offline view (because
    // the view is not loaded), so we have to show the offline view here.
    if (![UIDevice isPad] && ![ExSystem connectedToNetwork] && [self isViewLoaded] && (self.allFields == nil || self.allFields.count == 0))
    {
        [self showOfflineView:self];
        return;
    }
    
    if (self.entry != nil && [self isViewLoaded])
	{
		[self drawHeaderEntry:self.entry HeadLabel:lblName AmountLabel:lblAmount LabelLine1:lblLine1 LabelLine2:lblLine2 Image1:img1 Image2:img2 Image3:img3];
		
//        [self recalculateSections];
		[tableList reloadData];
		//[self setupRefreshingToolbar];
		[self setupToolbar];
        
        if ([self isWaitViewShowing])
            [self hideWaitView];
	}
    else
        self.doReload = YES;
}

- (void) fetchEntryDetail:(BOOL)skipCache
{
    if ([self isViewLoaded])
        [self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
    
    // If entry does not have form fields, then fetch entry detail
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 self.rpt.rptKey, @"RPT_KEY",
								 self.entry.rpeKey, @"RPE_KEY",
								 self.role, @"ROLE_CODE",
								 [self getViewIDKey], @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_DETAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:skipCache RespondTo:self];
}


- (void) fetchEntryDetail
{
    isLoading = YES;
    [self showLoadingView];
    [self fetchEntryDetail:YES];
}

-(UITableViewCell*) renderReceiptCell:(UITableView *)tblView 
{
	NSString* command = @"";
    // MOB-8256
   if (self.entry.receiptImageId == nil && self.receipt.receiptId == nil && [ExSystem connectedToNetwork])
   {
       command = [Localizer getLocalizedText:@"Add Receipt"];
   }
    else
    {
        command = [Localizer getLocalizedText:@"View Receipt"];
    }
    UITableViewCell* cell = [self makeDrillCell:tblView withText:command withImage:@"icon_receipt_button" enabled:YES];
    return cell;
}

-(UITableViewCell*) renderTravelAllowanceCell:(UITableView *)tblView
{
    NSString *command = [Localizer getLocalizedText:@"Adjust Allowance"];
    if([self isApproving])
    {
        command = [Localizer getLocalizedText:@"View Allowance"];
    }

    UITableViewCell* cell = [self makeDrillCell:tblView withText:command withImage:@"icon_travel_allowance" enabled:YES];  //TODO change Image
    return cell;
}

#pragma mark Editing Methods

-(void) initFields:(EntryData *)newentry
{
    BOOL isNewExpenseEntry = NO;
    if(newentry == nil)
    {
        newentry = self.entry;
    }
    
    if (newentry.transactionAmount == nil)
    {
        isNewExpenseEntry = YES;
    }
    self.formKey = newentry.formKey;
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
	self.allFields = [[NSMutableArray alloc] init];

	[self initFieldsWithEntry:newentry forSection:kSectionDetailsName];
    // this makes sure the liKey is set for MRU currency / Japan Rail currency
    // also updates the exchange rate in case the MRU currency is not USD
    // MOB-18965 only refreash currency rate for newly created expense.  Addresses two bugs 1). not getting exchange rate for MRU(above comment) 2). allow user to save customized exchange rate.
    if (isNewExpenseEntry) {
        [self requestCurrencyData];
    }
    
    // MOB-10862 - Add a tax forms section here.
    [self initFieldsWithEntryForTaxFroms:newentry forSection:kSectionTaxForms];
	[super initFields];
}


// MOB-10862 -
// There might be VAT Tax forms associated with the Expense type so append these to the allfields so displaying is taken care by itself.
-(void) initFieldsWithEntryForTaxFroms:(EntryData*) theEntry forSection:(NSString*) sectionName
{
   	// Mob-10862 - The EntryData now has taxforms also.
    // if this is a taxforms section then get the fields from the tax forms
    NSMutableArray *fields = [[NSMutableArray alloc] init];
	NSArray *fKeys = nil;
    NSDictionary *formFields = nil;
    
    // Check if there are some taxforms in the taxforms data.
    //
    if ([theEntry.taxforms.taxFormsData count] == 0 )
    {
        //TODO: Remove the tax section from the allfields
        
        [self removeTaxFormsSections];
        return;
    }
    fKeys = [theEntry.taxforms getFieldsKeys] ;
    formFields = [theEntry.taxforms getFormFields];
  
    // Add these fields to all fields. 
	for (NSString *fKey in fKeys)
	{
		FormFieldData* fld = formFields[fKey];
        //FormFieldData* fldForEdit = [fld copy];
        [self.allFields addObject:fld];
        [fields addObject:fld];
    }
  
    if([fields count] > 0)
    {
        (self.sectionFieldsMap)[sectionName] = fields;
    }

}
// Common API shared between entry and itemization
// Refactored this for displaying taxform fields.
// Moved common code to the initfields
-(void) initFieldsWithEntry:(EntryData*) theEntry forSection:(NSString*) sectionName
{
    [self resetReceipt:theEntry];

    // MOB-11608 iOS - Currency for Japan Public Transport expense entry is initially empty
    // The TransactionCurrencyName field for this has copy-down members set, but I can find
    // nowhere it's automatically handled upon initial load. This is more than a bit of a
    // hack, and I'd rather a more general solution, but need to do this expediently.
    if ([theEntry.expKey isEqualToString:@"JTRAN"])
    {
        theEntry.transactionCrnCode = @"JPY";
        FormFieldData *crnField = (theEntry.fields)[@"TransactionCurrencyName"];
        if (crnField != nil)
        {
            crnField.liCode = @"JPY";
            crnField.liKey = @"73";
            NSLocale *locale = [NSLocale currentLocale];
            crnField.fieldValue = [locale displayNameForKey:NSLocaleCurrencyCode value:@"JPY"];
        }
    }
    
	BOOL needExchangeRate = theEntry.transactionCrnCode != nil &&
    ![theEntry.transactionCrnCode isEqualToString:[self getReportCrnCode]];
	int vendorDescFldIndex = -1;
	int venLiKeyFldIndex = -1;
	
	BOOL canEditCardDate = [@"Y" isEqualToString:[[ExSystem sharedInstance] getSiteSetting:@"IS_DATE_EDITABLE" withType:@"CARD"]];
	
	NSMutableArray *fields = [[NSMutableArray alloc] init];
	
	NSArray *fKeys = theEntry.fieldKeys;
	BOOL isCCT = [@"Y" isEqualToString:theEntry.isCreditCardCharge];
    self.originalIsCCEntry = isCCT;
	BOOL isPCT = [@"Y" isEqualToString:theEntry.isPersonalCardCharge];
	BOOL isItemization = [theEntry.parentRpeKey length];
	BOOL isItemized = [theEntry.isItemized isEqualToString:@"Y"];
    BOOL isPerCarExpType = [self isPersonalCarMileageExpType:theEntry.expKey];
    BOOL isBusCarExpType = [self isCompanyCarMileageExpType:theEntry.expKey];
    // MOB-13426
    BOOL isPersonal = (theEntry.isPersonal != nil && [theEntry.isPersonal isEqualToString:@"Y"]);
    BOOL isCash = NO;

    
	if(isPerCarExpType || isBusCarExpType)
		[self addCarMileageFields:theEntry isPersonal:isPerCarExpType];
	
	for (NSString *fKey in fKeys)
	{
		FormFieldData* fld = (theEntry.fields)[fKey];
        
        
		FormFieldData* fldForEdit = [fld copy];
		BOOL isRW = fldForEdit.access == nil || [@"RW" isEqualToString:fldForEdit.access];
		if (isRW && [fldForEdit.iD isEqualToString:@"VenLiKey"] && fldForEdit.listKey == nil)
		{
			isRW = NO;
			fldForEdit.access = @"HD";
		}
		
		// MOB-4344 - For Itemization, block editing on Vendor, City, Payment type, Currency fields 
        // MOB-6788 Block editing on ExchangeRate for Item
		if (isRW && isItemization && 
			([fldForEdit.iD isEqualToString:@"VenLiKey"] 
			 || [fldForEdit.iD isEqualToString:@"VendorDescription"]
			 ||[fldForEdit.iD isEqualToString:@"LocName"]
			 ||[fldForEdit.iD isEqualToString:@"PatKey"]
			 ||[fldForEdit.iD isEqualToString:@"ExchangeRate"]
			 ||[fldForEdit.iD isEqualToString:@"TransactionCurrencyName"]))
		{
			isRW = NO;
			fldForEdit.access = @"RO";			
		}
        
        // MOB-11282 Personal car mileage is required
        if ([fldForEdit.iD isEqualToString:@"PersonalDistance"])
		{
            fldForEdit.required = @"Y";
            //MOB-14437 - Since this mandatory, set the value to zero so its not annoying user when saving.
            //We want user to change business distance and total anyway so dont set defaults there.
            if(![fldForEdit.fieldValue lengthIgnoreWhitespace])
            {
                fldForEdit.fieldValue = @"0";
            }
            
        }
		
		if (isRW && isItemized && ([fldForEdit.iD isEqualToString:@"IsPersonal"]))
		{
			isRW = NO;
			fldForEdit.access = @"HD";
		}
		
		BOOL isVisible = ![fldForEdit.access isEqualToString:@"HD"];
		// Hide VenLiKey if no list is available.
		// Set both flds if both are visible.
		
		if ([fldForEdit.iD isEqualToString:@"PostedAmount"])
		{
            // MOB-8117 Default crn to user currency, b/c SU entry form are displayed before report is selected.
            NSString* rptCrnCode = [self getReportCrnCode];

            fldForEdit.extraDisplayInfo = rptCrnCode;
            if (isVisible)
            {
                // Posted amount is a calculated field
                fldForEdit.access = needExchangeRate ? @"RO":@"HD";
                fldForEdit.label = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Amount in"], rptCrnCode];
            }
		}
		//MOB-13426 - Check if the policy allows changing amount for this expense type
		// this check is done here for existing entries. New entries go through a different path
        if ([fldForEdit.iD isEqualToString:@"PatKey"])
        {
            isCash = (fld.liKey != nil && [fld.liKey isEqualToString:@"CASH"]);
        }

        // change the status to not required and read only
        if (!isPersonal && !isPCT && !isCCT && !theEntry.isChild && isCash)
        {
            // does the server handle amount calculations?
            ExpenseTypeData *expenseTypeData = [[ExpenseTypesManager sharedInstance] expenseTypeForVersion:@"V3" policyKey:rpt.polKey expenseKey:[self getCurrentExpType] forChild:NO];
            if (([fldForEdit.iD isEqualToString:@"TransactionAmount"] || [fldForEdit.iD isEqualToString:@"PostedAmount"]) && [expenseTypeData serverDoesPostAmountCalculation])
            {
                fldForEdit.required = @"N";
                fldForEdit.access = @"RO";
            }
        }
        
		if ([fldForEdit.iD isEqualToString:@"TransactionAmount"])
        {
            fldForEdit.extraDisplayInfo = self.entry.transactionCrnCode;
            
            // MOB-12779: allows to save the report expese with zero amount and forces the transaction amount field to be blank when it is a newly created expense
            if (self.entry.rpeKey == nil && !self.isDirty && (![fldForEdit.access isEqualToString:@"RO"] || ![fldForEdit.access isEqualToString:@"HD"]))
                fldForEdit.fieldValue = @"";
            
            if (isVisible)
                fldForEdit.label = [Localizer getLocalizedText:@"Amount"];
        }
        
		if ([fldForEdit.iD isEqualToString:@"ExchangeRate"] && !needExchangeRate)
		{
			fldForEdit.access = @"HD";
		}
        else if ([fldForEdit.ctrlType isEqualToString:@"hidden"])
        {
            fldForEdit.access = @"HD";
        }
		else if (isCCT & isRW)
		{
			if ([fldForEdit.iD isEqualToString:@"TransactionAmount"] 
				|| ([fldForEdit.iD isEqualToString:@"TransactionDate"] && !canEditCardDate)
				|| [fldForEdit.iD isEqualToString:@"TransactionCurrencyName"]
				|| [fldForEdit.iD isEqualToString:@"PatKey"]
				|| [fldForEdit.iD isEqualToString:@"VendorDescription"])
			{
				fldForEdit.access = @"RO";				
			}
		}
        else if (isPCT && isRW && 
                 ([fldForEdit.iD isEqualToString:@"TransactionAmount"] || 
                  [fldForEdit.iD isEqualToString:@"TransactionCurrencyName"]))
        {
            fldForEdit.access = @"RO";
        }
		else if(self.isCarMileage || isPerCarExpType)
		{
			
			if ([fldForEdit.iD isEqualToString:@"TransactionAmount"] 
				|| [fldForEdit.iD isEqualToString:@"TransactionCurrencyName"]
				|| (isRW && [fldForEdit.iD isEqualToString:@"ExpKey"] && self.isCarMileage))
			{
				fldForEdit.access = @"RO";
				if ([fldForEdit.iD isEqualToString:@"TransactionCurrencyName"]) 
				{
					CarConfigData *carConfig = [[self getCarRates] fetchPersonalCarConfig];
					fldForEdit.liCode = carConfig.crnCode;
					fldForEdit.liKey = carConfig.crnKey;
					
					fldForEdit.fieldValue = [[NSLocale currentLocale] displayNameForKey:NSLocaleCurrencyCode value:carConfig.crnCode];
				}
                
			}
			else if([fldForEdit.iD isEqualToString:@"PatKey"] && isRW)
			{
				fldForEdit.access = @"RO";
				if (fldForEdit.fieldValue == nil|| fldForEdit.liKey == nil)
				{
					fldForEdit.fieldValue = [Localizer getLocalizedText:@"Cash"];
					fldForEdit.liKey = @"CASH";
				}
			}
		}
        
        if (isBusCarExpType && [fldForEdit.iD isEqualToString:@"TransactionAmount"])
            fldForEdit.access = @"RO";

        
		
        //		if ([fldForEdit.iD isEqualToString:@"TransactionAmount"])
        //			fldForEdit.label = [Localizer getLocalizedText:@"Amount"];
		
		if (![fldForEdit.access isEqualToString:@"HD"] && ![fldForEdit.iD isEqualToString:@"Attendees"])
		{
			[fields addObject:fldForEdit];
			if ([fldForEdit.iD isEqualToString:@"VendorDescription"])
				vendorDescFldIndex = [fields count]-1;
			else if ([fldForEdit.iD isEqualToString:@"VenLiKey"])
				venLiKeyFldIndex = [fields count] -1;
		}
		[self.allFields addObject:fldForEdit];
	}
	
	if(self.isCarMileage || isPerCarExpType || isBusCarExpType)
	{
		// Copy over distanceToDate to BusinessDistance' tip
		FormFieldData* distanceToDateFld = [self findEditingField:@"DistanceToDate"];
		FormFieldData* businessDistanceFld = [self findEditingField:@"BusinessDistance"];
//		if (businessDistanceFld != nil && distanceToDateFld != nil)
//			businessDistanceFld.tip = [NSString stringWithFormat:[Localizer getLocalizedText:@"Distance to Date = %@"], 
//                                       distanceToDateFld.fieldValue == nil ? @"":distanceToDateFld.fieldValue];
        
		FormFieldData* tranAmtFld = [self findEditingField:@"TransactionAmount"];
		FormFieldData* carKeyFld = [self findEditingField:@"CarKey"];
        NSString* carKey = carKeyFld == nil? nil:carKeyFld.liKey;
        
        NSString* rateTip = nil;
        CarRatesData *crd = [self getCarRates];
        // MOB-10882 : 
        // if the vehicle is not preselected Set Vehicle id to first company car by default  (in edit mode the fieldvalue is not nil so need not change).
        // Potential bug/request : if user has more than one car then user might complain that he wants his last selected value.
        //                          we default to first value always.
        if(!self.isReportApproval && isBusCarExpType && carKeyFld.fieldValue == nil)
        {
            NSArray *allCars = [[crd fetchCompanyCarDetails:@""] allValues];
            CarDetailData *firstcar = nil;
            // Only if there is a company car
            // MOB-21355: if it is a report approval, should not manipulate the Vehicle ID and display what server provides
            if ([allCars count] > 0)
            {
                firstcar  = [allCars objectAtIndex:0];
                carKeyFld.fieldValue = firstcar.vehicleId;
                carKeyFld.liKey = firstcar.carKey;
                [self refreshCarRates];
            }
         }
		if (!self.isReportApproval && tranAmtFld != nil) //BusinessDistance
		{
			tranAmtFld.tip = [crd fetchCarReimbursementRates:[NSDate date]
                       isPersonal:YES distance:@"1" carKey:carKey==nil? @"":carKey ctryCode:@""];
            rateTip = tranAmtFld.tip;
		}

        if (businessDistanceFld != nil && distanceToDateFld != nil)
		{
            businessDistanceFld.tip = [NSString stringWithFormat:[Localizer getLocalizedText:@"Distance to Date = %@"], 
                                       distanceToDateFld.fieldValue == nil ? @"":distanceToDateFld.fieldValue];
            businessDistanceFld.tip = [NSString stringWithFormat:@"%@\n%@", businessDistanceFld.tip, rateTip];
        }
        
        if (!self.isReportApproval) {
            [self fetchCarDistanceToDate];
        }
	}
	
	// Let's consolidate both vendor fields
	if (vendorDescFldIndex > -1 && venLiKeyFldIndex > -1)
	{
		FormFieldData* vendorDescFld = fields[vendorDescFldIndex];
		FormFieldData* venLiKeyFld = fields[venLiKeyFldIndex];
		if ([vendorDescFld.access isEqualToString:@"RO"] && [venLiKeyFld.access isEqualToString:@"RO"])
		{
			// Hide one of them
			if (vendorDescFld.fieldValue == nil)
			{
				vendorDescFld.access = @"HD";
				[fields removeObjectAtIndex:vendorDescFldIndex];
			}
			else {
				venLiKeyFld.access = @"HD";
				[fields removeObjectAtIndex:venLiKeyFldIndex];
			}
		}
		else if ([vendorDescFld.access isEqualToString:@"RW"] && [venLiKeyFld.access isEqualToString:@"RW"])
		{
			// Hide description, but copy over value and change list ctrl to combo
			[fields removeObjectAtIndex:vendorDescFldIndex];
			if (venLiKeyFld.fieldValue == nil && vendorDescFld.fieldValue != nil)
			{
				venLiKeyFld.fieldValue = vendorDescFld.fieldValue;
			}
			venLiKeyFld.ctrlType = @"combo";
		}
		else {
			// Display the RW one and hide the RO one.
			if ([vendorDescFld.access isEqualToString:@"RO"])
			{
				vendorDescFld.access = @"HD";
				[fields removeObjectAtIndex:vendorDescFldIndex];
			}
			else {
				venLiKeyFld.access = @"HD";
				[fields removeObjectAtIndex:venLiKeyFldIndex];
			}
            
		}
	}
	
    // Load from MRU
    // MOB-15833 - Do not set MRU currency if currency field is RO field. 
    if (self.entry.rpeKey == nil && self.entry.parentRpeKey == nil)
    {
        // set currency from MRU
        FormFieldData* crnField = [self findEditingField:@"TransactionCurrencyName"];
        ListItem* currency = [[MRUManager sharedInstance] getLastUsedCurrency];
        if (currency != nil && [crnField isEditable]) {
            
            
            crnField.fieldValue = currency.liName;
            crnField.liKey = currency.liKey == nil ? nil : [NSString stringWithFormat:@"%d", [currency.liKey intValue]];
            crnField.liCode = currency.liCode;
        }

        // set location from MRU.  This might overright the currency field
        ListItem* location = [[MRUManager sharedInstance] getLastUsedLocation];
        if (location != nil) {
            FormFieldData* locFld = [self findEditingField:@"LocName"];

            locFld.fieldValue = location.liName;
            locFld.liKey = location.liKey == nil ? nil : [NSString stringWithFormat:@"%d", [location.liKey intValue]];
            locFld.liCode = location.liCode;

            // MOB-14611 - check if the fields are not empty otherwise currency will be set to nil
            // MOB-15833 - Do not set MRU currency if currency is RO field. 
            if (crnField != nil && [location.fields count] > 0 && [crnField isEditable] )
            {
                crnField.liCode = [location.fields objectForKey:@"CrnCode"];
                crnField.liKey = [location.fields objectForKey:@"CrnKey"];
                NSLocale *locale = [NSLocale currentLocale];
                crnField.fieldValue = [locale displayNameForKey:NSLocaleCurrencyCode value:[location.fields objectForKey:@"CrnCode"]];
            }
        }
    }
    
	
	(self.sectionFieldsMap)[sectionName] = fields;


}

-(void)addCarMileageFields:(EntryData*) theEntry isPersonal:(BOOL)isPerCarExpType
{
    if (isPerCarExpType)
    {
        FormFieldData *newField = (theEntry.fields)[@"DistanceToDate"];
        if(newField != nil)
            newField.access = @"HD";
	}
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];	
	NSString *sectionName = sections[section];
	
    // MOB-10862 - build the cell for taxforms also same way as detailsection
	if ([sectionName isEqualToString:kSectionDetailsName] || [sectionName isEqualToString:kSectionTaxForms] )
	{
		UITableViewCell *cell = [super tableView:tblView cellForRowAtIndexPath:indexPath];
		if (cell != nil)
			return cell;
	}
	
	if([sectionName isEqualToString:kSectionExceptionsName])
	{
		NSString *key = sections[section];
		NSMutableArray *sectionData = sectionDataMap[key];
		ExceptionData *e = sectionData[row];
		
		NSString* imgName = nil;
        if([e.severityLevel isEqualToString:@"ERROR"])
            imgName = @"icon_redex";
        else
            imgName = @"icon_yellowex";
        
        UITableViewCell* cell = [self makeExceptionCell:tblView withText:e.exceptionsStr withImage:imgName];
		return cell;
	}
	else if ([sectionName isEqualToString: kSectionEntryName]) 
	{
		SummaryCellMLines *cell = (SummaryCellMLines *)[tblView dequeueReusableCellWithIdentifier: @"SummaryCell3Lines"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell3Lines" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SummaryCellMLines class]])
					cell = (SummaryCellMLines *)oneObject;
		}
		[self makeEntryCell:cell Entry:self.entry];
		return cell;
	}
	else if([sectionName isEqualToString: kSectionDrillsName])
	{
		UITableViewCell *cell = nil;
		NSString *key = sections[section];
		NSMutableArray *sectionData = sectionDataMap[key];
		NSString *val = sectionData[row];
		
		if([val isEqualToString:kViewItemizations])
		{
			NSString* command = [Localizer getLocalizedText:kViewItemizations]; 
			if (![self.entry.isItemized isEqualToString:@"Y"] && [self canEdit])
				command = [Localizer getLocalizedText:@"Itemize"];
			cell = [self makeDrillCell:tblView withText:command withImage:@"icon_itemize_button" enabled:YES];
		}
		else if ([val isEqualToString:kViewAttendees])
		{
			cell = [self makeDrillCell:tblView withText:[Localizer getLocalizedText:([self allowEditAttendees] ? @"APPROVE_EXPENSE_DETAILS_ATTENDEES" : @"APPROVE_EXPENSE_DETAILS_VIEW_ATTENDEES")] withImage:@"icon_attendee_button" enabled:YES];
		}
		else if ([val isEqualToString:@"Receipt"]){
			cell = [self renderReceiptCell:tblView];
		}
        else if ([val isEqualToString:kTravelAllowance]){
			cell = [self renderTravelAllowanceCell:tblView];
		}

		return cell;
	}
    else if ([sectionName isEqualToString: kSectionReceiptRequiredName])
    {
        NSString* imgName = @"icon_receiptrequired_details";
        UITableViewCell *cell = nil;
        if (self.requirePaperReceipt)
        {
            cell = [self makeExceptionCell:tblView withText:[Localizer getLocalizedText:@"Original receipt required"] withImage:imgName];
        }
        else
        {
            cell = [self makeExceptionCell:tblView withText:[@"Receipt image required" localize] withImage:imgName];
        }
        
		return cell;
    }
	return nil;
}


#pragma mark -
#pragma mark Table Delegate Methods 
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger section = [newIndexPath section];
    NSUInteger row = [newIndexPath row];		
	NSString *sectionName = sections[section];
	
	if([sectionName isEqualToString:kSectionDrillsName])
	{
		NSString *key = sections[section];
		NSMutableArray *sectionData = sectionDataMap[key];
		NSString *val = sectionData[row];
        
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHORT_CIRCUIT", nil];
		if (self.role != nil)
			pBag[@"ROLE"] = self.role;
		
		pBag[@"REPORT"] = rpt;
		pBag[@"ENTRY"] = self.entry;
		pBag[@"RECORD_KEY"] = rpt.rptKey;
		pBag[@"ID_KEY"] = rpt.rptKey;
		
		if([val isEqualToString:kViewItemizations])
		{
			if ([self isDirty])
			{
				[self confirmToSave:kAlertViewConfirmSaveUponItem];
			}
			else
			{
				[self goToItemization];
			}
		}
		else if([val isEqualToString:kViewAttendees])
		{
			[self showAttendeeEditor];
		}
		else if([val isEqualToString:kTravelAllowance])
		{
            UIStoryboard* storyboard = nil;
            if([self isApproving])
            {
                storyboard = [UIStoryboard storyboardWithName:[@"ItineraryApproverStoryboard" storyboardName] bundle:nil];

            }
            else
            {
                storyboard = [UIStoryboard storyboardWithName:[@"ItineraryStoryboard" storyboardName] bundle:nil];

            }

            ItineraryAllowanceAdjustmentViewController *o = [storyboard instantiateViewControllerWithIdentifier:@"ItineraryAllowanceAdjustment"];
            o.rptKey = self.entry.rptKey;
            o.taDayKey = self.entry.taDayKey;
            o.role = self.role;

            BOOL canEdit = [self canEdit];
            o.hideGenerateExpenseButton = !canEdit;
            if(![self isApproving])
            {
                [o setOnSuccessfulSave:^(NSDictionary *dictionary)
                {
//                    [self recalculateSections];
//                    [self refreshView];

                    //TODO Strong reference
                    [o performSegueWithIdentifier:@"unwindFromAllowanceAdjustment" sender:o];
//                    [self.navigationController popViewControllerAnimated:YES];

                }];
            }

            [self.navigationController pushViewController:o animated:YES];

            return;
        }
		else if ([val isEqualToString:@"Receipt"])
		{
			if ([self isDirty] && [self.entry.rpeKey length])
			{   // MOB-7170 Save before update receipt for existing entries
				[self confirmToSave:kAlertViewConfirmSaveUponReceipt];
			}
			else
			{
                [self showReceiptViewer];
            }
		}
	}
	[super tableView:tableView didSelectRowAtIndexPath:newIndexPath];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	NSString *sectionName = sections[section];
    NSMutableArray *sectionData = sectionDataMap[sectionName];
	if ([sectionName isEqualToString: kSectionEntryName])
		return 55;
	else if ([sectionName isEqualToString:kSectionDetailsName] || [sectionName isEqualToString:kSectionTaxForms])
	{
		return [super tableView:tableView heightForRowAtIndexPath:indexPath];
	}
//	else if(sectionName == kSectionCommentsName)
//	{	
//		CommentData *c = [sectionData objectAtIndex:row];
//		NSString *text = c.comment;
//		
//		int commentWidth = 265;
//		if([ExSystem isLandscape])
//			commentWidth = 425;
//		CGFloat height =  [FormatUtils getTextFieldHeight:commentWidth Text:text FontSize:13.0f];
//        
//		return height + 20;
//	}
	else if([sectionName isEqualToString: kSectionExceptionsName])
	{
		ExceptionData *e = sectionData[row];

		int exceptionW = tableView.frame.size.width - 20 - 40;
		CGFloat height =  [self getExceptionTextHeight:e.exceptionsStr withWidth:exceptionW];

		return height;
	}
	else
		return 40;
}


#pragma mark -
#pragma mark Attendee methods
-(BOOL)hasAttendeesField
{
	for (FormFieldData *ff in allFields)
	{
		if (ff.iD != nil && [ff.iD isEqualToString:@"Attendees"])
		{
			return YES;
		}
	}
	return NO;
}

-(FormFieldData*)lookupAttendeesField
{
	for (FormFieldData *ff in allFields)
	{
		if (ff.iD != nil && [ff.iD isEqualToString:@"Attendees"])
		{
			return ff;
		}
	}
	return nil;
}

-(BOOL) allowEditAttendees
{
	FormFieldData *attendeesField = [self lookupAttendeesField];
	BOOL canEditAttendees = (attendeesField != nil && ![attendeesField.access isEqualToString:@"RO"]);
	
	return ([self canEdit] && canEditAttendees);
}


#pragma mark ReportAttendeeDelegate Methods
-(void)attendeesEdited:(NSMutableArray*)editedAttendees
{
	self.attendees = editedAttendees;
	self.isDirty = TRUE;
}

-(BOOL)isParentEntry
{
    return [@"Y" isEqualToString:self.entry.isItemized];
}

#pragma mark -
#pragma mark  Merging Methods
-(void) mergeChanges:(ReportDetailDataBase*) fData // Form data
{
	BOOL hadAttendeesFieldBeforeChanges = [self hasAttendeesField];
	
	EntryData* newEntry = fData.rpt.entry; 
	
    // MOB-10862 : TODO: Find and fix what mergefields does.
    // For now just do the same for tax forms assume there is only one tax form
	NSMutableArray *detailsData = [self mergeFields:newEntry.fields withKeys:newEntry.fieldKeys];
    
    NSArray *fKeys = [newEntry.taxforms getFieldsKeys];
    NSDictionary *formFields = [newEntry.taxforms getFormFields];
    NSMutableArray *newtaxformsData = [[NSMutableArray alloc] init];
    if([fKeys count] >0 )
    {
       newtaxformsData = [self mergeFields:formFields withKeys:fKeys];
    }
    
    [self initFields:newEntry];
	(self.sectionDataMap)[kSectionDetailsName] = detailsData;
    
     if([fKeys count] >0 )
     {
         (self.sectionDataMap)[kSectionTaxForms] = newtaxformsData;
         (self.sectionFieldsMap)[kSectionTaxForms] = newtaxformsData;
         [self insertTaxFormsSections];
     }
    else
    {
        [self removeTaxFormsSections];
    }
	
	BOOL hasAttendeesFieldAfterChanges = [self hasAttendeesField];
	
	if (hasAttendeesFieldAfterChanges && !hadAttendeesFieldBeforeChanges &&
		(attendees == nil || [attendees count] == 0))
	{
		AttendeeData *attendeeRepresentingThisEmployee = [[ExpenseTypesManager sharedInstance] attendeeRepresentingThisEmployee];
		if (attendeeRepresentingThisEmployee != nil)
		{
			if (attendees == nil)
			{
				self.attendees = [[NSMutableArray alloc] init];
			}
            
            AttendeeData *attendeeCopy = [attendeeRepresentingThisEmployee copy];
			[attendees addObject:attendeeCopy];
			[self recalculateAttendeeAmounts];
		}
	}
    
	[self configureDrillData:self.sectionDataMap sections:self.sections];
    
	[tableList reloadData];
}

#pragma mark -
#pragma mark Form Edit Methods 

// network request to get the crn liKey. this should be cached
-(void) requestCurrencyData
{
    FormFieldData *currencyField = [[FormFieldData alloc] init];
    currencyField.iD = @"TransactionCurrencyName";
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: currencyField, @"FIELD", @"Y", @"MRU", nil];
    [[ExSystem sharedInstance].msgControl createMsg:LIST_FIELD_SEARCH_DATA CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

-(void) fieldUpdated:(FormFieldData*) field
{
	// MOB-12832 modified from Quick Expense implementation.
    if ([field.iD isEqualToString:@"LocName"])
    {
        NSDictionary *extra = (NSDictionary*)field.extraDisplayInfo;
        NSString *crn = nil;
        NSString *key = nil;
        if (extra != nil) {
            crn = [extra objectForKey:@"CrnCode"];
            key = [extra objectForKey:@"CrnKey"];
        } else {
            crn = @"USD";
            key = @"1";
        }
        
        FormFieldData *crnField = [self findEditingField:@"TransactionCurrencyName"];
        // MOB-16065 - Dont update the currency field if its not editable.
        // For CC transaction server sets the currency field as RO. 
        if (crnField != nil && crn != nil && [crnField isEditable])
        {
            crnField.liCode = crn;
            crnField.liKey = key;
            NSLocale *locale = [NSLocale currentLocale];
            crnField.fieldValue = [locale displayNameForKey:NSLocaleCurrencyCode value:crn];
            
            // Unlike Quick Expense, formfields require the liKey.  Kick off a network request for the liKey.
            [self requestCurrencyData];

            [super fieldUpdated:crnField];
        }
        
    }
    // if the transaction date is changed, need to check the tax form
    // MOB-15084 - Get Tax forms if any of locations are available city/country/state
    if ([field.iD isEqualToString:@"TransactionDate"] ||  [field.iD isEqualToString:@"LocName"] || [field.iD isEqualToString:@"CtryCode"] || [field.iD isEqualToString:@"CtrySubCode"])
    {
        [self makeTaxFormsSeverCall];
    }
    
    // Call the server to get DynamicActions for Dynamic Field
    if ([field.isDynamicField isEqualToString:@"Y"])
    {
        [self makeDynamicActionServerCall: field];
    }
    
    // Vendor description needs to be updated
	if ([field.ctrlType isEqualToString:@"combo"])
	{
		FormFieldData* vendorDescFld = [self findEditingField:@"VendorDescription"];
		if (field.liKey == nil)
			vendorDescFld.fieldValue = field.fieldValue;
		else 
			vendorDescFld.fieldValue = nil;
	}
    
    // MOB-21991: if switch to a different car, need to recalute everything
    if ([field.iD isEqualToString:@"CarKey"]) {
        self.isCarKeySwitched = YES;
    }

	// Update exchange rate for crn change
	[self updateAmountFields:field];
    
    // Check ItemCopyDownAction flag for parent entries
    if (field.itemCopyDownAction!= nil && [@"P" isEqualToString:field.itemCopyDownAction] && 
        ![self.entry.parentRpeKey length] &&
        [self.entry.rpeKey length] &&
        [@"Y" isEqualToString:self.entry.isItemized])
	{
		(self.ccopyDownSrcChanged)[field.label] = field.label;
	}

	[super fieldUpdated:field];
}


- (void) makeTaxFormsSeverCall
{
    FormFieldData *trasactionDate = [self findEditingField:@"TransactionDate"];
    FormFieldData *locName = [self findEditingField:@"LocName"];
    FormFieldData *ctryCode = [self findEditingField:@"CtryCode"];
    FormFieldData *ctrySubCode = [self findEditingField:@"CtrySubCode"];
    
    BOOL hasTransactionDate = [trasactionDate.fieldValue lengthIgnoreWhitespace] ;
    BOOL hasLocName = [locName.liKey lengthIgnoreWhitespace] && [locName.fieldValue lengthIgnoreWhitespace] ;
    BOOL hasCtryCode = [ctryCode.liKey lengthIgnoreWhitespace] && [ctryCode.fieldValue lengthIgnoreWhitespace] ;
    BOOL hasCtrySubCode = [ctrySubCode.liKey lengthIgnoreWhitespace] && [ctrySubCode.fieldValue lengthIgnoreWhitespace] ;
    
    // check if the tax forms is applied
    NSString* curExpKey = [self getCurrentExpType];
    ExpenseTypeData* expTypeData = [self getExpType:curExpKey];

    // MOB-15084 - either location/country/subcountry or all of them
    if ([expTypeData hasVATForm] && hasTransactionDate  && ( hasLocName || hasCtryCode || hasCtrySubCode) )
    {
         NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                      expTypeData.expKey, @"EXP_KEY",
                                      trasactionDate.fieldValue, @"TRANS_DATE",
                                      nil];
         // Any of the below could be nil so add to pBag using setvalue
         [pBag setValue:self.entry.rpeKey  forKey: @"RPE_KEY"];
         [pBag setValue:locName.liKey forKey:@"LN_KEY"];
         [pBag setValue:ctryCode.liKey forKey:@"CTRY_CODE"];
         [pBag setValue:ctrySubCode.liKey forKey:@"CTRY_SUB_CODE"];
         
         
         [self showLoadingViewWithText:[Localizer getLocalizedText:@"Waiting"]];
         [[ExSystem sharedInstance].msgControl createMsg:GET_TAX_FORMS_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

     }
}

-(BOOL) fetchCarDistanceToDate
{
    FormFieldData* tranDateFld = [self findEditingField:@"TransactionDate"];
    NSString* tranDateStr = tranDateFld.fieldValue;
    NSString *carKey = @"";
    FormFieldData* carKeyField = [self findEditingField:@"CarKey"];
    if(carKeyField != nil)
        carKey = carKeyField.liKey;
    
    // MOB-5153 - check if a car is selected
    if (carKey != nil && ![carKey isEqualToString:@""])
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     carKey, @"CAR_KEY", 
                                     [tranDateStr substringToIndex:10], @"TRAN_DATE",
                                     self.entry.rpeKey, @"EXCLUDE_RPE_KEY",
                                     nil];
        [[ExSystem sharedInstance].msgControl createMsg:CAR_DISTANCE_TO_DATE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
        return TRUE; 
    }
    return FALSE;  // no server fetch
}

-(BOOL) validateFields:(BOOL*)missingReqFlds;
{
    // MOB-6994 Block if no rates for car mileage 
    BOOL isPerCarExpType = [self isPersonalCarMileageExpType:[self getCurrentExpType]];
    if (isPerCarExpType && [self findEditingField:@"CarKey"] != nil)
    {
        FormFieldData* dateFld = [self findEditingField:@"TransactionDate"];
        if ([dateFld.validationErrMsg length])
            return FALSE;
    }
    
	BOOL result = [super validateFields:missingReqFlds];
    return result;
}

-(void) fetchNewExchangeRate:(FormFieldData*)crnFld withDateField:(FormFieldData*) dateFld
{
	if (crnFld == nil)
		crnFld = [self findEditingField:@"TransactionCurrencyName"];
	
	if (![[self getReportCrnCode] isEqualToString:crnFld.liCode])
	{
		if (dateFld == nil)
			dateFld = [self findEditingField:@"TransactionDate"];
		NSString* tranDateStr = dateFld.fieldValue;
		NSString* forDate = [CCDateUtilities formatDateForExchangeRateEndpoint:tranDateStr];
        if (![self isLoadingViewShowing]) {
            [self showLoadingView];
        }
		
		
		// If new currency is not the same as the report currency		
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									 crnFld.liCode, @"FROM_CRN_CODE", 
									 [self getReportCrnCode], @"TO_CRN_CODE",
									 forDate, @"FOR_DATE",
									 nil];
		[[ExSystem sharedInstance].msgControl createMsg:EXCHANGE_RATE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		
	}		
}

// Common API between entry and itemization
-(void) updateAmountFieldsCommon:(FormFieldData*) field
{
	// Update exchange rate for date change
	if ([field.iD isEqualToString:@"TransactionDate"])
	{
		[self fetchNewExchangeRate:nil withDateField:field];
	}
	// Update exchange rate for crn change
	else if ([field.iD isEqualToString:@"TransactionCurrencyName"])
	{
        // Decimal digits may change after crn change
		FormFieldData* tranAmountFld = [self findEditingField:@"TransactionAmount"];
        tranAmountFld.extraDisplayInfo = field.liCode;  // Refresh currency code
    
        NSString* crnCode = [tranAmountFld getCrnCodeForMoneyFldType];
        tranAmountFld.fieldValue = [FormatUtils formatMoneyWithoutCrnInternational:tranAmountFld.fieldValue crnCode:crnCode];
                
		[self refreshField:tranAmountFld];

		if (![[self getReportCrnCode] isEqualToString:field.liCode])
		{
            [self fetchNewExchangeRate:field withDateField:nil];
			
			FormFieldData* exchangeRateFld = [self findEditingField:@"ExchangeRate"];
			FormFieldData* transAmtFld = [self findEditingField:@"TransactionAmount"];
			FormFieldData* postedAmtFld = [self findEditingField:@"PostedAmount"];

            [self showField:exchangeRateFld afterField:transAmtFld];
			exchangeRateFld.access = @"RW";
            
			[self showField:postedAmtFld afterField:transAmtFld];
        }
		else {
			FormFieldData* exchangeRateFld = [self findEditingField:@"ExchangeRate"];
			exchangeRateFld.fieldValue = [FormatUtils formatDouble:@"1.00000000"];
            
			[self hideField:exchangeRateFld];
			[self hideFieldWithId:@"PostedAmount"];
            
			//[self refreshField:exchangeRateFld];
			[self recalculatePostedAmount];
		}

		[tableList reloadData];
	}
    else if ([field.iD isEqualToString:@"ExchangeRate"])
    {
        [self recalculatePostedAmount];
    }
	else if ([field.iD isEqualToString:@"TransactionAmount"])
	{
		[self recalculatePostedAmount];	
	}
}

-(void) updateAmountFields:(FormFieldData *)field
{
    BOOL isPerCarExpType = [self isPersonalCarMileageExpType:self.entry.expKey];
    BOOL isBusCarExpType = [self isCompanyCarMileageExpType:self.entry.expKey];
    
	if ([field.iD isEqualToString:@"PassengerCount"])
    {
        [self recalculateCarMileageAmount:NO];
    }
    else if (([field.iD isEqualToString:@"CarKey"] || [field.iD isEqualToString:@"TransactionDate"]) && (isPerCarExpType || isBusCarExpType))
    {
        [self showWaitView];
        BOOL terminateWaitView = ![self fetchCarDistanceToDate];
        if (terminateWaitView)
        {
            // MOB-7960 Already have rates. Need to recalculate b/c TransactionDate change can result in rate change
            [self recalculateCarMileageAmount:YES];
            [self hideWaitView];
        }
        else
        {
            [self refreshCarRates]; // Amt will be recalculated after rates/distance comes back
		}
	}
    else if ([field.iD isEqualToString:@"OdometerStart"] || [field.iD isEqualToString:@"OdometerEnd"])
    {
        // MOB-21991: refactor the code by creating another method for it so it can be reused at the other place
        [self refreshBusinessDistance];
        [self recalculateCarMileageAmount:NO];
    }
	else if ([field.iD isEqualToString:@"PersonalDistance"])
    {
        FormFieldData *fdTotal = [self findEditingField:@"TotalDistance"];
        FormFieldData *fdBiz = [self findEditingField:@"BusinessDistance"];
        FormFieldData *fdPer = [self findEditingField:@"PersonalDistance"];
        FormFieldData* fdEnd = [self findEditingField:@"OdometerEnd"];
        FormFieldData* fdStart = [self findEditingField:@"OdometerStart"];
        
        int total = [fdTotal.fieldValue intValue];
        int per = [fdPer.fieldValue intValue];
        
        // MOB-14619 -
        // if the odo end is not set then update odo end and subsequent fields.
        if(fdEnd.fieldValue == nil || [fdEnd.fieldValue intValue] <= 0 )
        {
            fdEnd.fieldValue = [NSString stringWithFormat:@"%d", [fdStart.fieldValue intValue] + [fdBiz.fieldValue intValue] + [fdPer.fieldValue intValue]];
            [self refreshField:fdEnd];
            [self updateAmountFields:fdEnd]; // this will take care of updating other fields.
        }
         else // just update business distance value 
        {
            fdBiz.fieldValue = [NSString stringWithFormat:@"%d", total - per];
            [self refreshField:fdBiz];
        }
        [self recalculateCarMileageAmount:NO];
        
    }
    else if ([field.iD isEqualToString:@"BusinessDistance"])
    {
        if ([self isCompanyCarMileageExpType:[self getCurrentExpType]])
        {
            FormFieldData *fdTotal = [self findEditingField:@"TotalDistance"];
            FormFieldData *fdBiz = [self findEditingField:@"BusinessDistance"];
            FormFieldData *fdPer = [self findEditingField:@"PersonalDistance"];
            FormFieldData* fdStart = [self findEditingField:@"OdometerStart"];
            FormFieldData* fdEnd = [self findEditingField:@"OdometerEnd"];

            int total = [fdTotal.fieldValue intValue];
            int biz = [fdBiz.fieldValue intValue];
             
            // MOB-14619 if the odo end is not set then update th odo end
            if(fdEnd.fieldValue == nil || [fdEnd.fieldValue intValue] <= 0 )
            {
                fdEnd.fieldValue = [NSString stringWithFormat:@"%d", [fdStart.fieldValue intValue] + [fdBiz.fieldValue intValue] + [fdPer.fieldValue intValue]];
                [self refreshField:fdEnd];
                [self updateAmountFields:fdEnd]; // this will take care of updating other fields.
            }
            else // just update personal distance value
            {
                fdPer.fieldValue = [NSString stringWithFormat:@"%d",total - biz];
                [self refreshField:fdPer];
            }
		}
        
        [self recalculateCarMileageAmount:NO];
        
    }
    else if ([field.iD isEqualToString:@"TotalDistance"])
    {
        FormFieldData* fdStart = [self findEditingField:@"OdometerStart"];
        FormFieldData* fdEnd = [self findEditingField:@"OdometerEnd"];
        FormFieldData *fdTotal = [self findEditingField:@"TotalDistance"];
        FormFieldData *fdBiz = [self findEditingField:@"BusinessDistance"];
        FormFieldData *fdPer = [self findEditingField:@"PersonalDistance"];
        
        int start = [fdStart.fieldValue intValue];
        int total = [fdTotal.fieldValue intValue];
        
        fdEnd.fieldValue = [NSString stringWithFormat:@"%d", start+total];
        [self refreshField:fdEnd];
        
        int per = [fdPer.fieldValue intValue];
        
        fdBiz.fieldValue = [NSString stringWithFormat:@"%d", total-per];
        [self refreshField:fdBiz];
        
        [self recalculateCarMileageAmount:NO];
        
    }
    else {
        [self updateAmountFieldsCommon:field];
	}
}

-(void)refreshBusinessDistance
{
    FormFieldData* fdStart = [self findEditingField:@"OdometerStart"];
    FormFieldData* fdEnd = [self findEditingField:@"OdometerEnd"];
    FormFieldData *fdTotal = [self findEditingField:@"TotalDistance"];
    FormFieldData *fdBiz = [self findEditingField:@"BusinessDistance"];
    FormFieldData *fdPer = [self findEditingField:@"PersonalDistance"];
    
    int start = [fdStart.fieldValue intValue];
    int end = [fdEnd.fieldValue intValue];
    int total = 0;
    if (end > 0) {
        total = end-start;
    }

    fdTotal.fieldValue = [NSString stringWithFormat:@"%d", total];
    [self refreshField:fdTotal];
    
    if (total <= 0)
    {
        fdBiz.fieldValue = @"0";
        [self refreshField:fdBiz];
        
        fdPer.fieldValue = @"0";
        [self refreshField:fdPer];
    }
    else{
        int per = [fdPer.fieldValue intValue];
        fdBiz.fieldValue = [NSString stringWithFormat:@"%d", total-per];
        [self refreshField:fdBiz];
    }
}

-(void) refreshOdometerStart:(NSString*)carKey
{
    // MOB-10710 Update odometer start
    if ([carKey length])
    {
        CarDetailData* carDetail = [[self getCarRates] fetchCompanyCarDetail:carKey];
        FormFieldData* odometerStartFld = [self findEditingField:@"OdometerStart"];
        if (odometerStartFld != nil && carDetail != nil)
        {
            odometerStartFld.fieldValue = [NSString stringWithFormat:@"%d", carDetail.odometerStart];//[FormatUtils formatInteger:[NSString stringWithFormat:@"%d", carDetail.odometerStart]];
            [self refreshField:odometerStartFld];
            
            // MOB-21970: recalculate end odomenter only if the user is not adding a new expense. If the user is adding a new expense, should enter end odometer value manually
            // Recalculate odometerEnd
            if (!self.isAddNewExpense) {
                FormFieldData* fdTotal = [self findEditingField:@"TotalDistance"];
                int total = [fdTotal.fieldValue intValue];
                int end = carDetail.odometerStart + total;
                FormFieldData* fdEnd = [self findEditingField:@"OdometerEnd"];
                fdEnd.fieldValue = [NSString stringWithFormat:@"%d", end];
                [self refreshField:fdEnd];
            }
        }
    }
}

// MOB-16441 - CRMC - Company Car Mileage on iPad cannot update odometer / can in CTE
// Changed this method to let users change the odo start. 
-(void) recalculateCarMileageAmount:(BOOL)newCarKey
{
    // Key on the presence of an odometer field to detect corporate version
    FormFieldData *odometerStartFld = [self findEditingField:@"OdometerStart"];
    //MOB-14357 / MOB-15034 - Fix odo resetting to initial value if total start > end.
    // As per web behaviour user can set the start and end odo to any value as long as end > start.
    // however due to mob-15034 MWS doesnt send updated odo reading
    
    FormFieldData* odometerEndFld = [self findEditingField:@"OdometerEnd"];
    
    int total = 0;
    if (!newCarKey) {
        total = [odometerEndFld.fieldValue intValue] - [odometerStartFld.fieldValue intValue];
    }

    if (odometerStartFld != nil)
    {
        FormFieldData* buisDistanceField = [self findEditingField:@"BusinessDistance"];
        FormFieldData* perDistanceField = [self findEditingField:@"PersonalDistance"];
        
        FormFieldData* dToDateFld = [self findEditingField:@"DistanceToDate"];
        int dToDate = [dToDateFld.fieldValue intValue];
        
        
        FormFieldData* tranDateFld = [self findEditingField:@"TransactionDate"];
        NSString* tranDateStr = tranDateFld.fieldValue;
        NSDate *date = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:tranDateStr];
        NSString *carKey = @"";
        FormFieldData* carKeyField = [self findEditingField:@"CarKey"];
        if(carKeyField != nil)
            carKey = carKeyField.liKey;
        
        BOOL isBusCarExpType = [self isCompanyCarMileageExpType:self.entry.expKey];
        
        NSString *transactionAmount = [self calculateCarmileageAmount:isBusCarExpType distanceTodate:dToDate date:date carKey:carKey];
        FormFieldData* tranAmountFld = [self findEditingField:@"TransactionAmount"];
        tranAmountFld.fieldValue = [FormatUtils formatMoneyWithoutCrn:transactionAmount crnCode:[self getReportCrnCode]];

        // If we're doing biz AND biz/personal amounts are zero
        // then we're starting a new expense. If so, go ahead and
        // update odometer start for user, otherwise leave it.
        // MOB-14357/MOB-15034 - Do not reset the ODO if the total < 0 - since if user enters a wrong data in odo end we dont want to reset the odo start
   		// MOB-16441 - CRMC - Company Car Mileage on iPad cannot update odometer / can in CTE
   		// The below newCarKey ensure that the odo is refreshed only if the user changes the car key in the form.
   		// Without the below check the odo start will be reset to car config default
        
        float businessAmount = [self calculateBusinessAmountWithDistanceToDate:dToDate date:date carKey:carKey];
        float personalAmount = [self calculatePersonalAmountWithDistanceToDate:dToDate date:date carKey:carKey isBusinessExpenseType:isBusCarExpType];
        if (self.isCarKeySwitched || (isBusCarExpType && businessAmount == 0 && personalAmount == 0 && total >= 0 && newCarKey) ){
            [self refreshOdometerStart:carKey];
            
            // MOB-21991: if user change to a different car, the business distance could has been changed, need to recalculate the transaction amount
            if (self.isCarKeySwitched) {
                [self refreshBusinessDistance];
                NSString *transactionAmount = [self calculateCarmileageAmount:isBusCarExpType distanceTodate:dToDate date:date carKey:carKey];
                FormFieldData* tranAmountFld = [self findEditingField:@"TransactionAmount"];
                tranAmountFld.fieldValue = [FormatUtils formatMoneyWithoutCrn:transactionAmount crnCode:[self getReportCrnCode]];
            }
        }
        
        NSString* perRateTip = [[self getCarRates] fetchCarReimbursementRates:date isPersonal:YES distance:perDistanceField.fieldValue carKey:carKey ctryCode:@""];
        NSString* buisRateTip = [[self getCarRates] fetchCarReimbursementRates:date isPersonal:NO distance:buisDistanceField.fieldValue carKey:carKey ctryCode:@""];
        
      
        [self refreshField:tranAmountFld];
        // MOB-6994 show no rates err msg
        if (carKeyField != nil && ![perRateTip length] && ![buisRateTip length])
        {
            tranDateFld.validationErrMsg = [Localizer getLocalizedText:@"PER_VARIABLE_NO_RATES"];
        }
        else 
            tranDateFld.validationErrMsg = nil;
        [self refreshField:tranDateFld];   
    }
    else {
        
        FormFieldData* distanceFld = [self findEditingField:@"BusinessDistance"];
        float distance = [distanceFld.fieldValue floatValue];
        FormFieldData* dToDateFld = [self findEditingField:@"DistanceToDate"];
        int dToDate = [dToDateFld.fieldValue intValue];
        FormFieldData* tranDateFld = [self findEditingField:@"TransactionDate"];
        NSString* tranDateStr = tranDateFld.fieldValue;
        NSDate *date = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:tranDateStr];
        NSString *carKey = @"";
        FormFieldData* carKeyField = [self findEditingField:@"CarKey"];
        if(carKeyField != nil)
            carKey = carKeyField.liKey;
        
        NSString *numPassengers = @"0";
        FormFieldData* passengerCountField = [self findEditingField:@"PassengerCount"];
        if(passengerCountField != nil)
            numPassengers = passengerCountField.fieldValue;
        
        float rate = [[self getCarRates] fetchRate:date isPersonal:YES isPersonalPartOfBusiness:NO distance:distanceFld.fieldValue carKey:carKey ctryCode:@"" numPassengers:numPassengers distanceToDate:dToDate]; //have to use today's date until date stuff is fixed
        
        FormFieldData* tranAmountFld = [self findEditingField:@"TransactionAmount"];
        NSString* tranAmtStr = [NSNumberFormatter localizedStringFromNumber:@(rate * distance)  numberStyle:NSNumberFormatterDecimalStyle];
        
        if([[self getCarRates] isPersonalVariable:@""])
            tranAmtStr = [NSNumberFormatter localizedStringFromNumber:@(rate)  numberStyle:NSNumberFormatterDecimalStyle]; //becuse fetchRate for variable returns the actual rate...
        
        
        tranAmountFld.fieldValue = [FormatUtils formatMoneyWithoutCrn:tranAmtStr crnCode:[self getReportCrnCode]];
        //tranAmountFld.label = [NSString stringWithFormat:@"%@ (%@)", [Localizer getLocalizedText:@"Amount"], [self getDistanceUnit]];
        
        
        NSString* rateTip = [[self getCarRates] fetchCarReimbursementRates:date isPersonal:YES distance:distanceFld.fieldValue carKey:carKey ctryCode:@""];
        NSString* distanceTip = [NSString stringWithFormat:[Localizer getLocalizedText:@"Distance to Date = %@"], 
                                 dToDateFld.fieldValue == nil ? @"":dToDateFld.fieldValue];
        distanceFld.tip = [NSString stringWithFormat:@"%@; %@", distanceTip, rateTip];
        [self refreshField:tranAmountFld];
        // MOB-6994 show no rates err msg
        if (carKeyField != nil && ![rateTip length])
        {
            tranDateFld.validationErrMsg = [Localizer getLocalizedText:@"PER_VARIABLE_NO_RATES"];
        }
        else 
            tranDateFld.validationErrMsg = nil;
        [self refreshField:tranDateFld];
            
    }
    
	// MOB-5085
    [self recalculateAttendeeAmounts];
}


-(NSString *)calculateCarmileageAmount:(BOOL)isBusCarExpType distanceTodate:(int)distanceToDate date:(NSDate *)date carKey:(NSString *)carKey
{
    float buisAmt = 0.0;
    float perAmt = 0.0;
    
    if (isBusCarExpType){
        buisAmt = [self calculateBusinessAmountWithDistanceToDate:distanceToDate date:date carKey:carKey];
        perAmt = [self calculatePersonalAmountWithDistanceToDate:distanceToDate date:date carKey:carKey isBusinessExpenseType:isBusCarExpType];
    }
    else {
        perAmt = [self calculatePersonalAmountWithDistanceToDate:distanceToDate date:date carKey:carKey isBusinessExpenseType:isBusCarExpType];
    }
    NSString* tranAmtStr = [NSNumberFormatter localizedStringFromNumber:@(buisAmt + perAmt)  numberStyle:NSNumberFormatterDecimalStyle];
    return tranAmtStr;
}

-(float)calculateBusinessAmountWithDistanceToDate:(int)distanceToDate date:(NSDate *)date carKey:(NSString *)carKey
{
    FormFieldData* buisDistanceField = [self findEditingField:@"BusinessDistance"];
    FormFieldData* passengerCountField = [self findEditingField:@"PassengerCount"];
    
    NSString *numPassengers = @"0";
    if(passengerCountField != nil){
        numPassengers = passengerCountField.fieldValue;
    }
    
    float buisAmt = [[self getCarRates] fetchRate:date isPersonal:NO  isPersonalPartOfBusiness:NO distance:buisDistanceField.fieldValue carKey:carKey ctryCode:@"" numPassengers:numPassengers distanceToDate:distanceToDate];
    return buisAmt;
}

-(float)calculatePersonalAmountWithDistanceToDate:(int)distanceToDate date:(NSDate *)date carKey:(NSString *)carKey isBusinessExpenseType:(BOOL)isBusinessExpense
{
    float personalAmount = 0.0;
    FormFieldData* perDistanceField = [self findEditingField:@"PersonalDistance"];
    if (isBusinessExpense) {
       personalAmount = [[self getCarRates] fetchRate:date isPersonal:NO  isPersonalPartOfBusiness:YES distance:perDistanceField.fieldValue carKey:carKey ctryCode:@"" numPassengers:@"0" distanceToDate:distanceToDate];
    }
    else{
        personalAmount = [[self getCarRates] fetchRate:date isPersonal:YES  isPersonalPartOfBusiness:NO distance:perDistanceField.fieldValue carKey:carKey ctryCode:@"" numPassengers:@"0" distanceToDate:distanceToDate];
    }

    return personalAmount;
}

-(void) refreshCarRates
{
	FormFieldData* distanceFld = [self findEditingField:@"BusinessDistance"];
	FormFieldData* dToDateFld = [self findEditingField:@"DistanceToDate"];
	NSString* dToDate = (dToDateFld== nil || dToDateFld.fieldValue == nil)? @"1" : dToDateFld.fieldValue;
	FormFieldData* tranDateFld = [self findEditingField:@"TransactionDate"];
	NSString* tranDateStr = tranDateFld.fieldValue;
	NSDate *date = tranDateStr== nil ? [NSDate date] : [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:tranDateStr];
	
	FormFieldData* tranAmtFld = [self findEditingField:@"TransactionAmount"];
	FormFieldData* carKeyFld = [self findEditingField:@"CarKey"];
	if (tranAmtFld != nil) //BusinessDistance
	{
		NSString* carKey = carKeyFld == nil? nil:carKeyFld.liKey;
        
		NSString* rateTip = [[self getCarRates] fetchCarReimbursementRates:date
                                                                                                        isPersonal:YES distance:dToDate carKey:carKey==nil? @"":carKey ctryCode:@""];
        NSString* distanceTip = [NSString stringWithFormat:[Localizer getLocalizedText:@"Distance to Date = %@"], 
                                 dToDateFld.fieldValue == nil ? @"":dToDateFld.fieldValue];
        distanceFld.tip = [NSString stringWithFormat:@"%@\n%@", distanceTip, rateTip];
    }
	
	[self refreshField:tranAmtFld];	
    
}

-(void) recalculatePostedAmount
{
	double exchangeRate = [self getDoubleFromField:@"ExchangeRate"];
	double transAmt = [self getDoubleFromField:@"TransactionAmount"];
	double postedAmt = exchangeRate * transAmt;
	if (postedAmt != 0.0)
	{
		FormFieldData* fld = [self findEditingField:@"PostedAmount"];
		
        NSString* postedAmtStr = [NSNumberFormatter localizedStringFromNumber:@(postedAmt)  numberStyle:NSNumberFormatterDecimalStyle];
        
        fld.fieldValue = [FormatUtils formatMoneyWithoutCrn:postedAmtStr crnCode:[self getReportCrnCode]];
		if (![fld.access isEqualToString:@"HD"])
			[self refreshField:fld];
	}
	
	[self recalculateAttendeeAmounts];
}

-(void) refreshWithExchangeRateMsg:(Msg*) msg
{
	ExchangeRateData * erData = (ExchangeRateData*) msg.responder;
	if (msg.errBody == nil && [erData.status isEqualToString:@"SUCCESS"]) 
	{
		FormFieldData* transCrnFld = [self findEditingField:@"TransactionCurrencyName"];
		if ([transCrnFld.liCode isEqualToString:erData.fromCrnCode])
		{
//			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Received exchange rate for %@:%f", erData.fromCrnCode, erData.rate] Level:MC_LOG_DEBU];
            if (erData.rate != 0.0)
            {
                FormFieldData* exchangeRateFld = [self findEditingField:@"ExchangeRate"];
                exchangeRateFld.fieldValue = [FormatUtils formatDouble:[NSString stringWithFormat:@"%f", erData.rate]];
                [self refreshField:exchangeRateFld];
                [self recalculatePostedAmount];
            }
		}
		else {
			[[MCLogging getInstance] log:[NSString stringWithFormat:@"Received OLD exchange rate for %@:%f", erData.fromCrnCode, erData.rate] Level:MC_LOG_DEBU];
		}
        
	}
	
	[self hideWaitView];
    
}

-(void)prefetchForListEditor:(ListFieldEditVC*) lvc
{
    if (!([lvc.field.iD isEqualToString:@"PatKey"] &&
           ([self.entry.isPersonalCardCharge isEqualToString:@"Y"]|| [[ExSystem sharedInstance] isBreeze])
          && [UserConfig getSingleton].yodleePaymentTypes != nil)
        )
    {
        [super prefetchForListEditor:lvc];
        return;
    }
    
    // MOB-12282 Breeze or PCT (stardard), display YodleePaymentTypes
    lvc.sectionKeys = [[NSMutableArray alloc] initWithObjects: @"MAIN_SECTION", nil];
    lvc.sections = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [UserConfig getSingleton].yodleePaymentTypes, @"MAIN_SECTION", nil];
    [lvc hideSearchBar];
}

#pragma mark Car Mileage methods
-(void) refreshWithDistanceToDateMsg:(Msg*) msg isNewCarMileage:(BOOL)isNewCarMileage
{
	ExCarDistanceToDateData* distanceToDate = (ExCarDistanceToDateData*) msg.responder;
	if (msg.errBody == nil && distanceToDate.distanceToDate!= nil)
	{
		// Copy over distanceToDate to BusinessDistance' tip
		FormFieldData* distanceToDateFld = [self findEditingField:@"DistanceToDate"];
		distanceToDateFld.fieldValue = distanceToDate.distanceToDate;
        [self refreshField:distanceToDateFld];
		FormFieldData* businessDistanceFld = [self findEditingField:@"BusinessDistance"];
		if (businessDistanceFld != nil && distanceToDateFld != nil)
			businessDistanceFld.tip = [NSString stringWithFormat:[Localizer getLocalizedText:@"Distance to Date = %@"], distanceToDateFld.fieldValue == nil ? @"":distanceToDateFld.fieldValue];
		[self refreshField:businessDistanceFld];
        
        // MOB-16441: The problem is that we always recalculate the start odometer by fetch the data from cache which is from the carConfig when user log in.
        // MOB-21384: if it is an existing car mileage item, do not need to recalculate the start odometer, simply just display whatever server provided
		[self recalculateCarMileageAmount:isNewCarMileage];
	}
    
	[self hideWaitView];
    
}

-(NSString*) getDistanceUnit
{
	NSString *distanceUnit = @"MILE";
	if([self getCarRates] != nil)
	{
		FormFieldData* tranDateFld = [self findEditingField:@"TransactionDate"];
		NSString* tranDateStr = tranDateFld.fieldValue;
        NSDate *date = [CCDateUtilities formatDateStringWithTimeZoneToNSDateWithLocalTimeZone:tranDateStr];
		if(date == nil)
			date = [NSDate date];
		
		FormFieldData* distanceFld = [self findEditingField:@"BusinessDistance"];
		FormFieldData* carField = [self findEditingField:@"CarKey"];
		NSString *carKey = @"";
		if(carField != nil)
			carKey = carField.liKey;
		
		distanceUnit = [[self getCarRates] fetchDistanceUnitAndRate:YES date:date ctryCode:@"" distance:distanceFld.fieldValue carKey:carKey];
	}
	else 
	{
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
		[[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
	}
	return distanceUnit;
}

#pragma mark Utility methods
-(NSString*)lookupCurrencyCode
{
	for (FormFieldData *ff in allFields)
	{
		if (ff.iD != nil && [ff.iD isEqualToString:@"TransactionCurrencyName"])
		{
			return ff.liCode;
		}
	}
	return nil;
}

-(NSString*) getCurrentExpType
{
	FormFieldData* fld = [self findEditingField:@"ExpKey"];
	if (fld != nil)
		return fld.liKey;
	return nil;
}

-(ExpenseTypeData*) getExpType:(NSString*)expType
{
    ExpenseTypeData* expTypeData = [[ExpenseTypesManager sharedInstance] 
                                    expenseTypeForVersion:@"V3" policyKey:self.rpt.polKey
                                    expenseKey:expType forChild:NO];
    return expTypeData;
}

-(BOOL) isPersonalCarMileageExpType:(NSString*)expType
{
    
    ExpenseTypeData* expTypeData = [[ExpenseTypesManager sharedInstance]
                                expenseTypeForVersion:@"V3" policyKey:self.rpt.polKey 
                                expenseKey:expType forChild:NO];
    // In case we haven't received the expense type meta data, we should disable amount editing for default mileage expense types, just to be safe
    if (expTypeData == nil && [expType isEqualToString:@"MILEG"])
        return TRUE;
    return [expTypeData isPersonalCarMileage];
}

-(BOOL) isCompanyCarMileageExpType:(NSString*)expType
{
    ExpenseTypeData* expTypeData = [[ExpenseTypesManager sharedInstance]
                                    expenseTypeForVersion:@"V3" policyKey:self.rpt.polKey 
                                    expenseKey:expType forChild:NO];
    // MOB-11022 TODO need to update MWS to return Car Mileage expense type even with no rates.  Need to make sure Android filters out the expense type on client first.
    // In case we haven't received the expense type meta data, we should disable amount editing for default mileage expense types, just to be safe
    if (expTypeData == nil && [expType isEqualToString:@"CARMI"])
        return TRUE;
    return [expTypeData isCompanyCarMileage];
}

#pragma mark -
#pragma mark - Overridden FormViewControllerBase Methods
-(void)showExpenseTypeEditor:(FormFieldData*) field
{
//	self.expTypeField = field;
	NSString* parentExpKey = nil;
    if ([self.entry isChild])
    {
        EntryData* parentEntry = (self.rpt.entries)[self.entry.parentRpeKey];
        parentExpKey = parentEntry.expKey;
    }
    [ExpenseTypesViewController showExpenseTypeEditor:self 
        policy:self.rpt.polKey
        parentVC:self selectedExpKey:field.liKey parentExpKey:parentExpKey withReport:self.rpt];
    
}


-(void) recalculateAttendeeAmounts
{
	if (![[ExSystem sharedInstance] enableAttendeeEditing])
		return;
    
	if (attendees != nil && [attendees count] > 0)
	{
		NSString *crnCode = [self lookupCurrencyCode];
		NSDecimalNumber *amount = [self getDecimalNumberFromField:@"TransactionAmount"];
        
        int noShowCount = ![self.entry.noShowCount length] ? 0: [self.entry.noShowCount intValue];
		[AttendeeData divideAmountAmongAttendees:attendees noShows:noShowCount amount:amount crnCode:crnCode];
	}
}

-(BOOL) isPersonal
{
    FormFieldData* fld = [self findEditingField:@"IsPersonal"];
    if (fld != nil)
    {
        return [@"Y" isEqualToString:fld.liKey];
    }
    else
    {
        return [@"Y" isEqualToString:self.entry.isPersonal];
    }
}

-(void) showAttendeeEditor
{

	{
		// MOB-4477 From the log attached to this bug, it appears that showAttendeeEditor is being called twice
		// in succession on the iPad resulting in a crash from trying to transition to a view controller while a
		// transition is already in progress.  The following check is to guard against this problem.
		if (self.presentedViewController != nil)
			return;
		
        NSString* expKey = [self getCurrentExpType];
        
        ExpenseTypeData* expType = [[ExpenseTypesManager sharedInstance] 
                                    expenseTypeForVersion:@"V3" policyKey:self.rpt.polKey 
                                    expenseKey:expKey forChild:self.entry.parentRpeKey!=nil];
        NSString * exAtnTypeKeysStr = expType.unallowAtnTypeKeys;
        NSArray *exAtnTypeKeys = exAtnTypeKeysStr == nil? nil : [exAtnTypeKeysStr componentsSeparatedByString:@","];
        
		ReportAttendeesViewController *vc = [[ReportAttendeesViewController alloc] initWithNibName:@"ReportAttendeesViewController" bundle:nil];
		vc.delegate = self;
		vc.canEdit = [self canEdit] && ![self isPersonal];
        vc.excludedAtnTypeKeys = exAtnTypeKeys;
        vc.entry = self.entry;
        vc.expKey = expKey; // self.entry.expKey;
        vc.polKey = self.rpt.polKey;
		NSString *crnCode = [self lookupCurrencyCode];
		NSDecimalNumber *amount = [self getDecimalNumberFromField:@"TransactionAmount"];
		[vc configureAttendees:attendees columns:self.entry.atnColumns crnCode:crnCode transactionAmount:amount];
		
		[self.navigationController pushViewController:vc animated:YES];
		
	}
}

-(BOOL) shouldAllowOfflineEditingwAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
	NSString *sectionName = sections[section];
	
    BOOL isDrillsSection = [sectionName isEqualToString:kSectionDrillsName];
    return isDrillsSection;
}

#pragma mark -
#pragma mark Actions from this page Methods

-(void)goToItemization
{
    ExpenseTypeData* expType = [[ExpenseTypesManager sharedInstance] 
                                                    expenseTypeForVersion:@"V3" policyKey:self.rpt.polKey 
                                                    expenseKey:self.entry.expKey forChild:NO];

	BOOL itemizeNotAllowed = (expType ==nil || [expType itemizeNotAllowed] 
                              || self.entry.parentRpeKey!= nil || [@"Y" isEqualToString:self.entry.isPersonal])
                        && ![self.entry.isItemized isEqualToString:@"Y"];
    if (itemizeNotAllowed)
    {
        UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:nil
							  message:[Localizer getLocalizedText:@"Itemization not allowed"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
							  otherButtonTitles:nil];
		[alert show];

        return;
    }
    
	if ([expType usesHotelItemizeWizard] && 
		(self.entry.items == nil || [self.entry.items count] == 0) 
		&& [self canEdit])
	{
		ItemizeHotelViewController *ihvc = [[ItemizeHotelViewController alloc] 
											initWithNibName:@"EditFormView" bundle:nil];
        [ihvc setSeedData:self.rpt entry:self.entry role:self.role];
		[self.navigationController pushViewController:ihvc animated:YES];
		return;
	}
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHORT_CIRCUIT", nil];
	if (self.role != nil)
		pBag[@"ROLE"] = self.role;
	
	pBag[@"REPORT"] = rpt;
	pBag[@"ENTRY"] = self.entry;
	pBag[@"RECORD_KEY"] = rpt.rptKey;
	pBag[@"ID_KEY"] = rpt.rptKey;
	ReportItemListViewController *vc;
	vc = [[ReportItemListViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
	
	[vc setSeedData:pBag];
	[self.navigationController pushViewController:vc animated:YES];
    
}

-(void)executeActionAfterSave
{
    // MOB-8730 Fixed car mileage redirect issue introduced by fix to MOB-7170
    bool naviNotHandled = self.actionAfterSave == 0 || self.actionAfterSave == kActionAfterSaveDefault;
    
	if (self.actionAfterSave == kAlertViewConfirmSaveUponItem)
	{
		[self goToItemization];
	}
    else if (self.actionAfterSave == kAlertViewConfirmSaveUponReceipt)
    {
        [self showReceiptViewer];
    }
    
    [super executeActionAfterSave]; // Handle Back
	
    if (naviNotHandled)
    {
        // MOB-12257 Save receipt to receipt store before entry save, and no longer need to block navigation here
        if((self.isCarMileage || self.isFromHome) && self.rpt.rptKey != nil /*&& !(entryUpdatedImageId)*/)
            [self goToReportDetailScreen];
    }
}


-(void) goToReportDetailScreen
{
	if([UIDevice isPad])
	{
        // MOB-12952 : push report details screen here.
        if(pickerPopOver != nil)
        {
            [pickerPopOver dismissPopoverAnimated:YES];
            pickerPopOver = nil;
        }

		// use use new home9VC show the report detail controller
		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        UINavigationController *homeNavigationController = (UINavigationController*)delegate.navController;
        
		[self dismissViewControllerAnimated:YES completion:nil];
        
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.rpt, @"REPORT", self.rpt.rptKey, @"ID_KEY",
									 self.rpt, @"REPORT_DETAIL",
                                     self.rpt.rptKey, @"RECORD_KEY",
                                     ROLE_EXPENSE_TRAVELER, @"ROLE",
                                     @"YES", @"SHORT_CIRCUIT"
									 ,nil];
        
        pBag[@"COMING_FROM"] = @"REPORT"; // Force it to fe
        
        ReportDetailViewController_iPad *newDetailViewController = [[ReportDetailViewController_iPad alloc] initWithNibName:@"ReportDetailViewController_iPad" bundle:nil];
		newDetailViewController.role = ROLE_EXPENSE_TRAVELER;
		newDetailViewController.isReport = YES;
        
        [homeNavigationController pushViewController:newDetailViewController animated:YES];
        [newDetailViewController loadReport:pBag]; // Note that the original implementation called switchTopDetail which pushed the VC first and then called loadReport

	}
	else 
	{
		[self dismissViewControllerAnimated:YES completion:nil];	
		
		if(!self.isFromAlert)
		{
			NSMutableDictionary * pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
										  @"YES", @"POP_TO_ROOT_VIEW", 
										  self.rpt.rptKey, @"ID_KEY", 
										  self.rpt, @"REPORT",
										  self.role, @"ROLE",
										  //self.rpt, @"REPORT_DETAIL",
										  self.rpt.rptKey, @"RECORD_KEY", @"YES", @"SHORT_CIRCUIT", nil];	
			[ConcurMobileAppDelegate switchToView:ACTIVE_ENTRIES viewFrom:SELECT_REPORT ParameterBag:pBag];
		}
	}
}


-(BOOL)isSaveConfirmDialog:(int) tag
{
	if (tag == kAlertViewConfirmSaveUponItem || tag == kAlertViewConfirmSaveUponReceipt)
		return YES;
	return [super isSaveConfirmDialog:tag];
}


-(void)respondToEntryForm:(Msg *)msg
{
	[self hideWaitView];
    
	ReportEntryFormData* fData = (ReportEntryFormData*) msg.responder;
	
	if (msg.errBody != nil || fData.rpt.entry == nil) 
	{
		NSString* errMsg = msg.errBody;
		
		if(errMsg == nil)
			errMsg = [Localizer getLocalizedText:@"Unexpected Error"];
		
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:msg.errCode
							  message:errMsg
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
							  otherButtonTitles:nil];
		[alert show];
	}
	else
    {
        // MOB-13944 Editing imported CC entry inside report makes certain fields available
        // if originally the entry is CC entry, but new response somehow lost the entry type(CC)
        // We will take the value from the old entry.
        if (self.originalIsCCEntry && fData.rpt.entry.isCreditCardCharge == nil)
                fData.rpt.entry.isCreditCardCharge = @"Y";
        
		[self mergeChanges:fData];
	}
	
}

// Update the parent entry in report, before refreshing the entry via load Entry
-(void)updateEntry:(EntryData*)newEntry
{
	EntryData* existingEntry = self.entry;
        
    if (newEntry.parentRpeKey == nil && existingEntry != nil)
    {
        // ##TODO## - update Receipt object
        
        
        // MOB-12257 Save receipt to receipt store before entry save, and no longer need to block navigation here
        if (self.entry.receiptImage != nil/* && entryUpdatedImage == YES*/) {
            // A new receipt image was added to entry, preserve it and call save report entry receipt
            newEntry.receiptImage = self.entry.receiptImage;
        }
        else if (self.entryUpdatedImageId)
        {
            newEntry.receiptImageId = self.entry.receiptImageId;
        }
    
        if ((self.rpt.entries)[newEntry.rpeKey] == nil)
            [self.rpt.keys addObject:newEntry.rpeKey];
        (self.rpt.entries)[newEntry.rpeKey] = newEntry;
    }
    
	if ([existingEntry.rpeKey isEqualToString:newEntry.rpeKey])
	{
        [self loadEntry:newEntry withReport:nil];
    }
	else 
	{
		EntryData* newItem = (newEntry.items)[existingEntry.rpeKey];
		if (newItem != nil)
			[self loadEntry:newItem withReport:nil];
	}
    // Moved to load Entry
    // ##TODO## - need to check for itemization?
}


-(void)respondToEntrySave:(Msg *)msg
{
	SaveReportEntryData* srData = (SaveReportEntryData*) msg.responder;
	
	if (msg.errBody != nil || ![srData.actionStatus.status isEqualToString:@"SUCCESS"]) 
	{
		NSString* errMsg = msg.errBody != nil ? msg.errBody : srData.actionStatus.errMsg;
		
		if(errMsg == nil)
			errMsg = [Localizer getLocalizedText:@"Unexpected Error"];
		
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:msg.errCode
							  message:errMsg
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
							  otherButtonTitles:nil];
		[alert show];
        
        // MOB-13176 Refresh car rates if invalid car error
        if ([srData.actionStatus.status isEqualToString:@"car.updateCarMileageLog.car_use.fail"])
        {
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
            [[ExSystem sharedInstance].msgControl createMsg:CAR_RATES_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        }
        
		[self clearActionAfterSave];
	}
	else 
	{
		self.isDirty = NO;
		NSString *dt = [DateTimeFormatter formatDateTimeMediumByDate:msg.dateOfData];
		dt = [NSString stringWithFormat:[Localizer getLocalizedText:@"Last updated"], dt];
        
		// Update current entry key, if it is a new entry
		[self.entry setRpeKey:srData.rpeKey];
		EntryData*newEntry = srData.rpt.entry;

        BOOL isBusCarExpType = [self isCompanyCarMileageExpType:newEntry.expKey];
        if (isBusCarExpType)
        {
            // MOB-10710 Update odometer start upon company car mileage save
            FormFieldData* odEndFld = (newEntry.fields)[@"OdometerEnd"];
            if (odEndFld != nil)
            {
                NSInteger odEnd = [odEndFld.fieldValue integerValue];
                
                FormFieldData* carKeyFld = (newEntry.fields)[@"CarKey"];
                NSString* carKey = carKeyFld == nil? nil:carKeyFld.liKey;
                if (carKey != nil)
                {
                    CarDetailData* carDetail = [[self getCarRates] fetchCompanyCarDetail:carKey];
                    if (odEnd >carDetail.odometerStart)
                        carDetail.odometerStart = odEnd;
                }
            }
            
        }
		// This will update the top level entry in rpt object, and update entry/item in vc
		[self updateEntry:newEntry];
		self.rpt.totalPostedAmount = srData.rptTotalPosted;
		self.rpt.totalClaimedAmount = srData.rptTotalClaimed;
		self.rpt.totalApprovedAmount = srData.rptTotalApproved;
		
		if (![newEntry.rpeKey isEqualToString:srData.rpeKey])
		{
			// Update parent entry in report
			int vcCount = [self.navigationController.viewControllers count];
			for (int ix = 0; ix < 2; ix++)
			{
				int vcIx = vcCount - 2-ix;
				if (vcIx > 2 || (vcIx >=0 && [UIDevice isPad]))
				{
//##TODO##                    
                    //					MobileViewController *parentMVC = (MobileViewController *)[vc.navigationController.viewControllers objectAtIndex:vcIx];
                    //					if (parentMVC != nil)
                    //					{
                    //						[parentMVC setDoReload:YES];
                    //					}
				}
			}
		}
		
		[self setupToolbarWithMessage:dt withActivity:NO];
		
        // MOB-8451:Start building/updating Cache here. Save last used expense type
        // MOB-11735 keyval cannot be nil

        NSInteger  keyVal =  NSNotFound ;
        [[MRUManager sharedInstance]addMRUForType:@"mru_expenseType" value:self.entry.expName key:&keyVal code:self.entry.expKey];

        // MOB-5252
        // Notify parent views that report has been changed.

        // MOB-12257 Save receipt to receipt store before entry save, and no longer need to block navigation here

//        if (!(entryUpdatedImage||entryUpdatedImageId))  //MOB-5869 hold the view up till receipt is saved for new entries
//        {
            [self refreshWithUpdatedReport:nil];
            [self executeActionAfterSave];
           	[self hideWaitView];
//        }
	}
}


-(void)respondToFoundData:(Msg *)msg
{
	[super respondToFoundData:msg];

    if([msg.idKey isEqualToString:SAVE_REPORT_ENTRY_RECEIPT])
    {
        // Will be invoked only in case of Report entry receipt
		SaveReportEntryReceipt *rptEntryReceipt = (SaveReportEntryReceipt*)msg.responder;

        // MOB-9414 Check for failure, save data only if success.
        if (msg.errBody == nil && [rptEntryReceipt.actionStatus.status isEqualToString:@"SUCCESS"])
        {
            self.isDirty = TRUE;
            self.entry.receiptImageId = rptEntryReceipt.receiptImageId;
            //MOB-13018
            self.entry.hasMobileReceipt = @"Y";
        }
        else
        {
            NSDictionary *dict = @{@"Failure": @"Failed to save receipt image id for entry"};
            [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
        }

        //MOB-13018
        [self recalculateSections];
        [tableList reloadData];
        // Refresh parent view
        [self refreshWithUpdatedReport:nil];
        
        /*
         Old code for delayed save receipt for new entry
        // This code block is called only when there is a new expense (no rpeKey) added to a report. This is a delayed receipt save.
        entryUpdatedImageId = NO;
        [self setIsDirty:NO];
        
        [self refreshWithUpdatedReport:nil];
        // MOB-7595

        if ([ExSystem sharedInstance].isSingleUser)
            [[ReceiptCacheManager sharedInstance] entryReceiptUpdated:self.entry];

        [self executeActionAfterSave];
        */
        
        if ([self isViewLoaded]) {
           	[self hideWaitView];
        }
    }
    else if([msg.idKey isEqualToString:CAR_RATES_DATA])
	{
		CarRatesData* carRatesData = (CarRatesData*) msg.responder;
		[ConcurMobileAppDelegate findRootViewController].carRatesData = carRatesData;
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
		// Update distance label
		if ([self canEdit])
		{
			FormFieldData* bdFld = [self findEditingField:@"BusinessDistance"];
			bdFld.label = [NSString stringWithFormat:@"%@ (%@)", [Localizer getLocalizedText:@"Distance"], [self getDistanceUnit]];			
			[self refreshField:bdFld];
		}
		
	}
	else if ([msg.idKey isEqualToString:EXCHANGE_RATE_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
		[self refreshWithExchangeRateMsg:msg];
	}
	else if ([msg.idKey isEqualToString:CAR_DISTANCE_TO_DATE_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
        // MOB-21384: add a boolean prameter: if it is an existing car mileage item, do not need to recalculate the start odometer,
        // simply just display whatever server provided
        [self refreshWithDistanceToDateMsg:msg isNewCarMileage:self.isAddNewExpense];
	}
	else if ([msg.idKey isEqualToString:REPORT_ENTRY_FORM_DATA])
	{
  
        if ([self isViewLoaded]) {
            [self hideLoadingView];
            [self hideWaitView];
        }
        
		[self respondToEntryForm:msg];
        
        NSString* expKey = [self getCurrentExpType];
        BOOL isPerCarExpType = [self isPersonalCarMileageExpType:expKey];
        if (!self.isReportApproval && isPerCarExpType && self.entry.rpeKey == nil)
        {
            // MOB-4829 Let's fetch distanceToDate for new entry
            [self fetchCarDistanceToDate];
        }
        ReportEntryFormData* fData = (ReportEntryFormData*) msg.responder;
        ExpenseTypeData *et = [self getExpType:expKey];
        // MOB-15084- force get tax forms whenever there is a new form
        // since MRU might fill up date /location by default, so taxforms given by GetReportEntryFormV4 might not be valid
        if (msg.errBody == nil && fData.rpt.entry != nil  && [et hasVATForm])
        {
            [self makeTaxFormsSeverCall];
        }
        if(![et hasVATForm])
        {
            [self removeTaxFormsSections];
        }

	}
 	else if ([msg.idKey isEqualToString:SAVE_REPORT_ENTRY_DATA])
	{
        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		[self respondToEntrySave:msg];
	}
	else if (msg.parameterBag != nil & (msg.parameterBag)[@"REPORT"] != nil & (msg.parameterBag)[@"ENTRY"] != nil)
	{
		ReportData* report = (msg.parameterBag)[@"REPORT"];
		EntryData* curEntry = (msg.parameterBag)[@"ENTRY"];
        
//        MOB-21970: for car mileage only: if it is a new expense, need to get the start odometer from server
        NSString *newExpense = (msg.parameterBag)[@"TITLE"];
        
        if ([newExpense isEqualToString:@"Add Expense"]) {
            self.isAddNewExpense = YES;
        }
        // MOB-21980: iPad adding a new expense. if it is a car mileage expense, need to show the right start OD
        else {
            NSString *iPadAddNewExpense =msg.parameterBag[@"ADD_NEW_EXPENSE"];
            if ([iPadAddNewExpense isEqualToString:@"YES"]) {
                self.isAddNewExpense = YES;
            }
        }
        
        //  MOB-21355 CRMC - Wrong Vehicle ID displayed when it is an approving report.
        // iPad comes to this block from ReportDetailViewController_iPad
        if ([msg.parameterBag[@"SOURCE_SECTION"] isEqualToString:@"REPORT_APPROVAL_SECTION"] ) {
            self.isReportApproval = YES;
        }
        
        [self loadEntry:curEntry withReport:report];
		if (self.entry.fields != nil && [self.entry.fields count]>0)
		{
            NSString* expKey = [self getCurrentExpType];
            ExpenseTypeData *et = [self getExpType:expKey];
            BOOL isPerCarExpType = [self isPersonalCarMileageExpType:expKey];

            if([et hasVATForm] &&  self.entry.taxforms == nil)
            {
                [self makeTaxFormsSeverCall];
            }
 
            if(![et hasVATForm])
            {
                [self removeTaxFormsSections];
            }


            if (!self.isReportApproval && isPerCarExpType && self.entry.rpeKey == nil)
            {
                // MOB-4829 Let's fetch distanceToDate for new entry
                [self fetchCarDistanceToDate];
            }
            else
            {
                [self refreshView];
            }
        }
		else
		{
            [self refreshView];
			[self fetchEntryDetail];
		}
        
	}
	else if ([msg.idKey isEqualToString:REPORT_ENTRY_DETAIL_DATA])
	{
        isLoading = NO;
		ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
		EntryData* newEntry = rad.rpt.entry;
        
        // MOB-18287 fix missing receipt message based on paper receipt or original receipt
        // Logic adapted from SubmitWizard.js
        if ([@"Y" isEqualToString:newEntry.imageRequired] || [@"Y" isEqualToString: newEntry.receiptRequired])
        {
            if (newEntry.ereceiptId == nil &&  (newEntry.hasMobileReceipt == nil || [@"N" isEqualToString:newEntry.hasMobileReceipt])
                && newEntry.receiptImageId == nil)
            {
                self.requirePaperReceipt = NO;
            }
            
            // MOB-13715 need paper receipt flag
            // Logic adapted from SubmitWizard.js " updateText: function(rpt, confirmAgreement, wizardView, numExpNeedRcpt) "
            if ([newEntry.receiptRequired isEqualToString:@"Y"])
            {
                self.requirePaperReceipt = YES;
            }
        }
        
		if (newEntry != nil)
		{
			[self loadEntry:newEntry withReport:nil];
            
            if ([self isViewLoaded]) {
                [self hideLoadingView];
            }
		}
        
        // Update report object in parent view
        // Update parent entry in report
        if (![UIDevice isPad])
        {
            int vcCount = [self.navigationController.viewControllers count];
            for (int ix = 0; ix < vcCount; ix++)
            {
                MobileViewController* mvc = (MobileViewController*) (self.navigationController.viewControllers)[vcCount-ix-1];
                if ([mvc isKindOfClass:ReportDetailViewController.class])
                {
                    // Get report detail cache
                    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rpt, @"REPORT", rpt.rptKey, @"ID_KEY", rpt.rptKey, @"RECORD_KEY", self.role, @"ROLE_CODE", nil];
                    [[ExSystem sharedInstance].msgControl createMsg:ACTIVE_REPORT_DETAIL_DATA CacheOnly:@"YES" ParameterBag:pBag SkipCache:NO RespondTo:mvc];
                }
            }
        }
	}
	else if ([msg.idKey isEqualToString:ACTIVE_REPORT_DETAIL_DATA])
	{
		ReportDetailDataBase *rad = (ReportDetailDataBase *)msg.responder;
		self.rpt = rad.rpt;
		EntryData* newEntry = (rad.rpt.entries)[self.entry.rpeKey];
        if (newEntry != nil && newEntry.fields != nil && [self.entry.fields count]>0) {
            [self loadEntry:newEntry withReport:rad.rpt];
        }
        //Get Tax
        
            
	}

	else if ([msg.idKey isEqualToString:DELETE_REPORT_ENTRY_DATA])
	{
        // Called when deleting itemizations
		DeleteReportEntryData* srd = (DeleteReportEntryData*) msg.responder;
		ReportData* report =  srd.rpt;
		EntryData* thisEntry = (report.entries)[self.entry.rpeKey];
		
		[self loadEntry:thisEntry withReport:report];
	}
    else if ([msg.idKey isEqualToString:LIST_FIELD_SEARCH_DATA]) {
        
        // MOB-12932 update currency when location is updated.
        // this ugliness sets the liKey for currency when it's been updated by the location.
        FormFieldData *crnField = [self findEditingField:@"TransactionCurrencyName"];

        ListFieldSearchData *currencyData = (ListFieldSearchData *)msg.responder;

        if (crnField != nil && currencyData != nil) {

            ListItem *item = [currencyData getListItemWithLiCode:crnField.liCode];

            if (item != nil) {

                crnField.liKey = item.liKey;

                // also need to update the currency exchange rate and such.
                [self updateAmountFields:crnField];
            }
        }
    }
    else if ([msg.idKey isEqualToString:GET_TAX_FORMS_DATA])
    {
        if ([self isViewLoaded]) {
            [self hideLoadingView];
        }
        
        TaxForms *taxFormsData = (TaxForms*) msg.responder;
        self.entry.taxforms = taxFormsData;
        if ([[taxFormsData getFormFields] count] > 0 )
        {
            [self initFieldsWithEntryForTaxFroms:self.entry forSection:kSectionTaxForms];
            //[self.tableList reloadData];
            [self insertTaxFormsSections];
        }
        else
        {
            [self removeTaxFormsSections];
            
        }
    }
    else if ([msg.idKey isEqualToString:GET_DYNAMIC_ACTIONS])
    {
        BOOL fRefresh = [ self updateDynamicFields:(ConditionalFieldsList *) msg.responder
                                            fields:self.allFields];
        if (fRefresh == YES)
        {
            [self.tableList reloadData];
        }
    }

//##TODO##
//	if ([data count] < 1 || data == nil) 
//	{//show we gots no data view
//		[emptyView setHidden:NO];
//	}
//	else if (data != nil & [data count] > 0)
//	{//refresh from the server, after an initial no show...
//		[emptyView setHidden:YES];
//	}
}
-(void) insertTaxFormsSections
{
    //[self.tableList reloadData];
    if(![self.sections containsObject:kSectionTaxForms])
    {
        [self.sections addObject:kSectionTaxForms];
        
    }
    //MOB-15052
    [self.tableList reloadData];

}

// Removes the tax form fields from the section map
-(void) removeTaxFormsSections
{

//    // MOB-14947 MOB-14950
//    // dont do this if we're not visible
//    if (!self.isViewLoaded || !self.view.window) {
//        return;
//    }

    // return if there is no taxforms section
    if([self.sections indexOfObject:kSectionTaxForms] == NSNotFound)
    {
        return;
    }
    
    [self deleteTaxformFieldsFromAllFields:nil]; // clean up self.allfields
    [self.sectionDataMap removeObjectForKey:kSectionTaxForms];
    [self.sections removeObject:kSectionTaxForms];
    [self.sectionFieldsMap removeObjectForKey:kSectionTaxForms];

    //MOB-15052
    [self.tableList reloadData];


}

-(NSString*) getCDMsg
{
	return [Localizer getLocalizedText:@"COPY_DOWN_ENTRY_MSG"];
}


-(NSString*) getFormFieldsInvalidMsg
{
	return [Localizer getLocalizedText:@"ENTRY_REQ_FIELDS"];
}


-(void) saveForm:(BOOL) cpDownToChildForms
{
	// MOB-4731(4614) Go back to report detail after adding new entry 
// AJC - BEGIN - please delete this code if present past 2013-12-13
//	if (self.entry.rpeKey == nil &&
//        self.actionAfterSave == kActionAfterSaveDefault && 
//        !self.isCarMileage && !self.isFromHome)
//		self.actionAfterSave =kAlertViewConfirmSaveUponBack;
// AJC - END - please delete this code if present past 2013-12-13
    
	if (self.actionAfterSave == kActionAfterSaveDefault && 
        !self.isCarMileage && !self.isFromHome)
		self.actionAfterSave =kAlertViewConfirmSaveUponBack;
	
	[self showWaitViewWithText:[Localizer getLocalizedText:@"Saving Expense"]];
    [self saveToMRU];
    // TODO : Write back the tax form fields to entry.taxform
    
	[self sendSaveReportMsg];
}

// it appears that location is automatically saved by the picker. currency and expense type has to be manually saved. :/
// this is copied from the QEFormVC...
-(void) saveToMRU
{
    for(FormFieldData *field in allFields)
    {
        if([field.iD isEqualToString:@"TransactionCurrencyName"])
        {
            NSInteger  keyVal = field.liKey ==nil ? NSNotFound : [field.liKey integerValue] ;
            [[MRUManager sharedInstance]addMRUForType:field.iD value:field.fieldValue key:&keyVal code:field.liCode];
        }
    }

    NSInteger  keyVal =  NSNotFound;
    [[MRUManager sharedInstance]addMRUForType:@"mru_expenseType" value:self.entry.expName key:&keyVal code:self.entry.expKey];
}

-(BOOL) hasCopyDownChildren
{
    return [self.entry.rpeKey length] &&
        [@"Y" isEqualToString:self.entry.isItemized];
}

// AJC - BEGIN - please delete this code if present past 2013-12-13
//-(void) checkCopyDownForSave
//{
//	if (![self.entry.rpeKey length])
//		[self saveForm:NO];
//	else 
//		[super checkCopyDownForSave];
//}
//
// AJC - END - please delete this code if present past 2013-12-13

// This method deletes the taxform fields from self.allfields so they are not sent under formfields xml node.
- (void) deleteTaxformFieldsFromAllFields:(NSArray *)taxformFields
{
   if([taxformFields count] == 0)
       taxformFields = [self.sectionFieldsMap objectForKey:kSectionTaxForms];
    
    // Step 1 : remove taxformfields from self.allfields
    for (FormFieldData *taxformfield in taxformFields) 
    {
        int nFields = [self.allFields count];
        int deleteindex = -1 ; 
        for (int ix = 0; ix < nFields; ix ++)
        {
            FormFieldData* ff = (self.allFields)[ix];
            if([ff.iD isEqualToString:taxformfield.iD] && [ff.label isEqualToString:taxformfield.label] && [ff.ftCode isEqualToString:taxformfield.ftCode])
            {
                deleteindex = ix;
                break;
            }
        }
        if(deleteindex > -1)
            [self.allFields removeObjectAtIndex:deleteindex];
    }
}

-(void)sendSaveReportMsg
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 rpt.rptKey, @"RPT_KEY", 
								 self.entry, @"ENTRY",
								 self.allFields, @"FIELDS",
								 self.role, @"ROLE_CODE",
								 self.copyToChildForms ? @"Y" : @"N", @"COPY_DOWN_TO_CHILD_FORMS",
								 formKey, @"FORM_KEY",
								 nil];
	NSString* curExpKey = [self getCurrentExpType];
	pBag[@"CUR_EXP_KEY"] = curExpKey;
    ExpenseTypeData* expType = [self getExpType:curExpKey];
    if (expType != nil)
        pBag[@"CUR_EXP_TYPE"] = expType;
    
    if([expType hasVATForm] && [self.sectionFieldsMap objectForKey:kSectionTaxForms])
    {
        // update the taxform fields
        [self deleteTaxformFieldsFromAllFields:nil];
    }
	if ([self hasAttendeesField])
	{
		if (self.attendees == nil)
		{
			self.attendees = [[NSMutableArray alloc] init];
		}
		pBag[@"ATTENDEES"] = self.attendees;
	}
	
	[[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_ENTRY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

#pragma mark -
#pragma mark ExpenseType delegate
- (void)saveSelectedExpenseTypeImpl:(ExpenseTypeData*) et
{
    // Remove itemization link for exp switch to NALWS 
    if ([et isPersonalCarMileage])
    {
        // set amounts to 0 for MILEG
        FormFieldData* transAmtFld = [self findEditingField:@"TransactionAmount"];
        FormFieldData* postedAmtFld = [self findEditingField:@"PostedAmount"];
        if (transAmtFld != nil)
            transAmtFld.fieldValue = @"0";
        if (postedAmtFld != nil)
            postedAmtFld.fieldValue = @"0";
    }
    
    bool needToGetNewForm = ![et.formKey isEqualToString:self.formKey];
    
    ExpenseTypeData *currExp = [self getExpType:[self getCurrentExpType]];
    // if user changed the expense type and expense type has VAT form then try refresh the form to see if tax fields show up.
    if ( [et hasVATForm] || [currExp hasVATForm] )
    {
           needToGetNewForm = YES;
    }
    
    if (!needToGetNewForm)
    {
        // Check whether ExpTypeListKey field is present
        FormFieldData* fld = [self findEditingField:@"ExpTypeLiKey"];
        // 
        // MOB-12951 : if user changes the expense type 
        // Incase the formkey is same check if the selected expense type/original is/was personal car mileage or company car mileage
        // in above two cases reload the expense form
        // 
        BOOL isPerCarExpType = [self isPersonalCarMileageExpType:self.entry.expKey] || [et isPersonalCarMileage] ;
        BOOL isBusCarExpType = [self isCompanyCarMileageExpType:self.entry.expKey] || [et isCompanyCarMileage]  ;
        
        if (fld != nil || isPerCarExpType || isBusCarExpType  )
        {
            needToGetNewForm = YES;
        }
    }
    
	if (needToGetNewForm)
	{
		[self showWaitViewWithText:[Localizer getLocalizedText:@"Waiting"]];
        
		NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
									 et.expKey, @"EXP_KEY",
									 rpt.rptKey, @"RPT_KEY", 
									 self.entry.rpeKey, @"RPE_KEY",
									 nil];
		[[ExSystem sharedInstance].msgControl createMsg:REPORT_ENTRY_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
		
		
	}
    else if (![et hasVATForm] )
    {
        [self removeTaxFormsSections]; // if it need not get the form and expense type does not have vat tax form then delete the vat tax form section
    }
    
	[super saveSelectedExpenseType:et];
}

- (void)saveSelectedExpenseType:(ExpenseTypeData*) et
{
    NSString *curExp = [self getCurrentExpType];
    if([et isCompanyCarMileage] || [et isPersonalCarMileage])
    {
        NSDictionary *dictionary = @{@"Add from": @"Report Add Expense"};
        [Flurry logEvent:@"Car Mileage: Add from" withParameters:dictionary];
    }

    if (curExp != nil && [curExp isEqualToString:et.expKey])
    {
        [super saveSelectedExpenseType:et];
        return;
    }
    
	if ([et itemizeNotAllowed])
    {
        // Ask for user confirmation, if change to Personal car mileage and has itemization
        FormFieldData* expFld = [self findEditingField:@"ExpKey"];
        if (expFld != nil && ![expFld.liKey isEqualToString:et.expKey] && [self.entry.items count]>0)
        {
            UIAlertView *alert = [[MobileAlertView alloc] 
                                  initWithTitle:nil
                                  message:[Localizer getLocalizedText:@"Changing the expense type will remove all itemized data"]
                                  delegate:self 
                                  cancelButtonTitle:[Localizer getLocalizedText:@"No"] 
                                  otherButtonTitles:[Localizer getLocalizedText:@"Yes"], nil];
            
            ((MobileAlertView*) alert).eventData = et;
            alert.tag = kAlertViewConfirmExpTypeSwitch;
            
            [alert show];
            
            return;
        }
    }
    
    [self saveSelectedExpenseTypeImpl:et];
}


#pragma mark -
#pragma mark AlertView Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if(alertView.tag == kAlertViewConfirmExpTypeSwitch)
	{
        if (buttonIndex == 1)
        {
            MobileAlertView* mav = (MobileAlertView*) alertView;
            ExpenseTypeData* et = (ExpenseTypeData*) mav.eventData;
            if (et != nil)
            {
                [self saveSelectedExpenseTypeImpl:et];
                [self configureDrillData:self.sectionDataMap sections:self.sections];
            }
        }
        else
        {
            [self dismissViewControllerAnimated:YES completion:nil];
        }
    }
    else
        [super alertView:alertView clickedButtonAtIndex:buttonIndex];
}

#pragma mark -
#pragma mark Receipt Action
- (void)showReceiptViewer
{
    // As of January 22, 2013, there is no API for deleting a receipt for an existing expense.  Therefore, a receipt can only be deleted for a brand new quick expense that has not yet been saved.
    BOOL isNewExpense = self.entry.meKey == nil;
    
	ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = isNewExpense;
    // MOB-13137 remove replace receipts option on report entry for receipt hold status
    receiptView.canUpdate = [self canUpdateReceipt] && ![self.rpt.apsKey isEqualToString:@"A_RHLD"];
    receiptView.canAppend = [self canUpdateReceipt];
    receiptView.canUseReceiptStore = [self canUpdateReceipt];
    [receiptView setSeedData:self.receipt];
    
    [self.navigationController pushViewController:receiptView animated:YES];
}


#pragma mark ReceiptEditorDelegate
-(void) receiptDeleted:(Receipt*) rcpt
{
    self.receipt = nil;
    // TODO : Nil out entry receiptId
}

- (void)receiptQueued:(Receipt*)receipt{}  // For offline?

-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint;
{
    self.isDirty = YES;
    self.receipt = rcpt;
    
    if (self.receipt.receiptId != nil && ![self.receipt.receiptId isEqualToString:self.entry.receiptImageId])
    {
        
        if ([self.entry.rpeKey length])
        {
            // Save receipt for existing entry
            NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                         self.rpt, @"REPORT",
                                         self.entry, @"ENTRY",
                                         self.role, @"ROLE_CODE",
                                         nil];
            
            pBag[@"RECEIPT_IMAGE_ID"] = self.receipt.receiptId;
            [[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_ENTRY_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        }
        else
        {
            self.entryUpdatedImageId = YES;
            self.entry.receiptImageId = self.receipt.receiptId;
            [self setIsDirty:YES];
            
            NSIndexSet *indexSet = [NSIndexSet indexSetWithIndex:0];
            [self.tableList reloadSections:indexSet withRowAnimation:UITableViewRowAnimationRight];
        }
    }
}

- (void)receiptDisplayed:(Receipt *)rcpt
{
    // MOB-6132
    // MOB-10146 check for PDF as well as image files as entry receipt
    if ([self isApproving] && [rcpt hasReceipt]) {
        // Audit approver has viewed image.
        [self sendViewEntryReceiptAuditMsg];
    }
}

- (void)sendViewEntryReceiptAuditMsg
{
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/MarkEntryReceiptAsViewed/%@", [ExSystem sharedInstance].entitySettings.uri, self.entry.rpeKey];
    
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	RequestController *rc = [RequestController alloc];
	Msg *msg = [[Msg alloc] initWithData:@"MarkEntryReceiptAsViewed" State:@"" Position:nil MessageData:nil URI:path MessageResponder:nil ParameterBag:pBag];
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	msg.skipCache = YES;
	
	[rc initDirect:msg MVC:self];
}

-(IBAction)unwindFromAllowanceAdjustment:(UIStoryboardSegue *)segue
{
    //This exists to make the unwind segue from the allowance adjustment work
    //TODO There needs to be a block to refresh the entry
//    [self loadEntry:<#(EntryData*)thisEntry#> withReport:<#(ReportData*)report#>];
//    [self recalculateSections];
//    [self refreshView];
}

@end
