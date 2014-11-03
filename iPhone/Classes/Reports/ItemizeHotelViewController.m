//
//  ItemizeHotelViewController.m
//  ConcurMobile
//
//  Created by yiwen on 1/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ItemizeHotelViewController.h"
#import "MobileAlertView.h"
#import "LabelConstants.h"
#import "ItemizeHotelData.h"
#import "ReportEntryViewController.h"
#import "ReportItemListViewController.h"
#import "ExSystem.h"
#import "DateTimeFormatter.h"
#import "ExpenseTypesViewController.h"
#import "FormatUtils.h"

@implementation ItemizeHotelViewController
@synthesize entry, itemTbHelper, expTypeField;

#define kSectionDatesName @"Dates"
#define kSectionRoomRatesName @"RoomRates"
#define kSectionExtraChargesName @"ExtraCharges"

#pragma mark -
#pragma mark Initialization
- (void)setSeedData:(NSDictionary*)pBag
{
    [self setSeedData:pBag[@"REPORT"] entry:pBag[@"ENTRY"] role:pBag[@"ROLE"]];
    
}

- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry role:(NSString*) curRole
{
    self.role = curRole;
    [self loadEntry:thisEntry withReport:report];
}

// Replace both loadEntry and updateEntry
- (void) loadEntry:(EntryData*) thisEntry withReport:(ReportData*) report
{
    self.rpt = report;
    self.entry = thisEntry;
    self.itemTbHelper = [[ItemizedToolbarHelper alloc] init];
    [itemTbHelper setSeedData:self.entry];
    [self initFields];
    [self recalculateSections];
    [self refreshView];
}

- (void) refreshView
{
    if (entry != nil && [self isViewLoaded])
	{
		[tableList reloadData];
		[self setupToolbar];
        [self hideWaitView];        
	}
    else
        self.doReload = YES;
    
}


-(void)setupToolbar
{
    [super setupToolbar];
    if ([self canEdit] && 
        [ExSystem connectedToNetwork])
    {
        [itemTbHelper setupToolbar:self];
    }
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:ITEMIZE_HOTEL_DATA])
	{
		ItemizeHotelData* srData = (ItemizeHotelData*) msg.responder;
		
		BOOL success = (msg.errBody == nil && [srData.actionStatus.status isEqualToString:@"SUCCESS"]);
		
		if (!success)
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
			
			return;
		}
        else
        {
            UINavigationController* navCtl = self.navigationController;
		
            // Update entry/rpt object
            int vcCount = [navCtl.viewControllers count];
            if (vcCount >=2)
            {
                MobileViewController *pvc = (MobileViewController *)(navCtl.viewControllers)[vcCount - 2];
                
                [self.navigationController popViewControllerAnimated:NO];
                
                ReportEntryViewController* parentMVC = nil;
                if (([[pvc getViewIDKey] isEqualToString:APPROVE_EXPENSE_DETAILS])
                    && [pvc isKindOfClass:ReportEntryViewController.class])
                {
                    parentMVC = (ReportEntryViewController*)pvc;
                    [parentMVC respondToEntrySave:msg];
                    
                    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"YES", @"SHORT_CIRCUIT", nil];
                    if (self.role != nil)
                        pBag[@"ROLE"] = self.role;
                    
                    pBag[@"REPORT"] = parentMVC.rpt;
                    pBag[@"ENTRY"] = parentMVC.entry;
                    
                    ReportItemListViewController* vc = [[ReportItemListViewController alloc] initWithNibName:@"EditFormView" bundle:nil];
                    [vc setSeedData:pBag];
                    [navCtl pushViewController:vc animated:YES];
                }		
            }
		}

        if ([self isViewLoaded]) {
            [self hideWaitView];
        }
		
		
	}
	else
		[super respondToFoundData:msg];
}

-(NSString*) getCurrencyCodeForField:(FormFieldData*) fld
{
	return entry.transactionCrnCode;
}

- (void)recalculateSections
{
    if (self.sections == nil)
    {
        self.sections = [[NSMutableArray alloc] init];
        [self.sections addObject:kSectionDatesName];
        [self.sections addObject:kSectionRoomRatesName];
        [self.sections addObject:kSectionExtraChargesName]; 
    }
}

-(void)initFields
{
	self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
	self.allFields = [[NSMutableArray alloc] init];
	
	NSMutableArray *fields = [[NSMutableArray alloc] init];
	FormFieldData* checkInDateFld = [[FormFieldData alloc] 
			initField:@"checkInDate" label:[Localizer getLocalizedText:@"Check-In Date"] value:nil ctrlType:@"edit" dataType:@"TIMESTAMP"];
	checkInDateFld.required = @"Y";
	FormFieldData* checkOutDateFld = [[FormFieldData alloc] 
									 initField:@"checkOutDate" label:[Localizer getLocalizedText:@"Check-Out Date"] 
									  value:entry.transactionDate ctrlType:@"edit" dataType:@"TIMESTAMP"];
	checkOutDateFld.required = @"Y";
	FormFieldData* numberOfNightsFld = [[FormFieldData alloc] 
									  initField:@"numberOfNights" label:[Localizer getLocalizedText:@"Number Of Nights"] value:@"0" ctrlType:@"edit" dataType:@"INTEGER"];
	numberOfNightsFld.access = @"RW";
	
	[fields addObject:checkInDateFld];
	[fields addObject:checkOutDateFld];
	[fields addObject:numberOfNightsFld];
	
	
	(self.sectionFieldsMap)[kSectionDatesName] = fields;
	[self.allFields addObject:checkInDateFld];
	[self.allFields addObject:checkOutDateFld];
	[self.allFields addObject:numberOfNightsFld];

	fields = [[NSMutableArray alloc] init];
	FormFieldData* roomRateFld = [[FormFieldData alloc] 
										initField:@"roomRate" label:[Localizer getLocalizedText:@"Room Rate"] value:@"0.00" ctrlType:@"edit" dataType:@"MONEY"];
	roomRateFld.required = @"Y";

	FormFieldData* roomTaxFld = [[FormFieldData alloc] 
								  initField:@"roomTax" label:[Localizer getLocalizedText:@"Room Tax"] value:@"0.00" ctrlType:@"edit" dataType:@"MONEY"];
	
    FormFieldData* otherRoomTax1 = [[FormFieldData alloc]
                                 initField:@"otherRoomTax1" label:[Localizer getLocalizedText:@"Other Room Tax 1"] value:@"0.00" ctrlType:@"edit" dataType:@"MONEY"];
    
    FormFieldData* otherRoomTax2 = [[FormFieldData alloc]
                                 initField:@"otherRoomTax2" label:[Localizer getLocalizedText:@"Other Room Tax 2"] value:@"0.00" ctrlType:@"edit" dataType:@"MONEY"];
    
	[fields addObject:roomRateFld];
	[fields addObject:roomTaxFld];
    [fields addObject:otherRoomTax1];
    [fields addObject:otherRoomTax2];
	(self.sectionFieldsMap)[kSectionRoomRatesName] = fields;
	[self.allFields addObject:roomRateFld];
	[self.allFields addObject:roomTaxFld];
    [self.allFields addObject:otherRoomTax1];
    [self.allFields addObject:otherRoomTax2];

	fields = [[NSMutableArray alloc] init];
	FormFieldData* expType1Fld = [[FormFieldData alloc] 
								  initField:@"expType1" label:[Localizer getLocalizedText:@"Expense Type"] value:nil ctrlType:@"edit" dataType:@"EXPTYPE"];
	FormFieldData* amount1Fld = [[FormFieldData alloc] 
								 initField:@"amount1" label:[Localizer getLocalizedText:@"Amount"] value:@"0.00" ctrlType:@"edit" dataType:@"MONEY"];
	FormFieldData* expType2Fld = [[FormFieldData alloc] 
								  initField:@"expType2" label:[Localizer getLocalizedText:@"Expense Type"] value:nil ctrlType:@"edit" dataType:@"EXPTYPE"];
	FormFieldData* amount2Fld = [[FormFieldData alloc] 
								 initField:@"amount2" label:[Localizer getLocalizedText:@"Amount"] value:@"0.00" ctrlType:@"edit" dataType:@"MONEY"];
	
	[fields addObject:expType1Fld];
	[fields addObject:amount1Fld];
	[fields addObject:expType2Fld];
	[fields addObject:amount2Fld];
	(self.sectionFieldsMap)[kSectionExtraChargesName] = fields;
	[self.allFields addObject:expType1Fld];
	[self.allFields addObject:amount1Fld];
	[self.allFields addObject:expType2Fld];
	[self.allFields addObject:amount2Fld];
	
    
	[super initFields];
}

-(void) updateNumberOfNights:(FormFieldData*) field
{
	if ([field.iD isEqual:@"checkInDate"] || [field.iD isEqual:@"checkOutDate"])
	{
		FormFieldData* checkInFld = [field.iD isEqual:@"checkInDate"]? field : [self findEditingField:@"checkInDate"];
		FormFieldData* checkOutFld = [field.iD isEqual:@"checkOutDate"]?field: [self findEditingField:@"checkOutDate"];
		NSDate * checkInDate = [DateTimeFormatter getLocalDate:checkInFld.fieldValue];
		NSDate * checkOutDate = [DateTimeFormatter getLocalDate:checkOutFld.fieldValue];
		if (checkInDate == nil || checkOutDate == nil)
			return;
		
		NSTimeInterval diffTime = [checkOutDate timeIntervalSinceDate:checkInDate];
		int days = diffTime/(60*60*24);
		
		FormFieldData* numberOfNightsFld = [self findEditingField:@"numberOfNights"];
		if (days < 0)
		{
			numberOfNightsFld.fieldValue = @"0";
			if (checkInFld == field)
			{
				checkOutFld.fieldValue = checkInFld.fieldValue;
				[self refreshField:checkOutFld];
			}
			else {
				checkInFld.fieldValue = checkOutFld.fieldValue;
				[self refreshField:checkInFld];
			}
		}
		else {
			numberOfNightsFld.fieldValue = [NSString stringWithFormat:@"%d", days];
		}
		[self refreshField:numberOfNightsFld];
	} else if ([field.iD isEqual:@"numberOfNights"])
    {
        NSString* usInt = [FormatUtils convertIntegerToStringUS:field.fieldValue];
        if (usInt == nil)
            return;
        
        // Update check-in date
		FormFieldData* checkInFld = [field.iD isEqual:@"checkInDate"]? field : [self findEditingField:@"checkInDate"];
		FormFieldData* checkOutFld = [field.iD isEqual:@"checkOutDate"]?field: [self findEditingField:@"checkOutDate"];
		NSDate * checkOutDate = [DateTimeFormatter getLocalDate:checkOutFld.fieldValue]; // GMT
		if (checkOutDate == nil)
			return;
		
		NSTimeInterval diffTime = [usInt intValue] * 60*60*24;
        
        NSDate * checkInDate = [NSDate dateWithTimeInterval:-diffTime sinceDate:checkOutDate]; //GMT
		checkInFld.fieldValue = [DateTimeFormatter getLocalDateAsString:checkInDate];
        [self refreshField:checkInFld];
    }
}

-(void)showExpenseTypeEditor:(FormFieldData*) field
{
    self.expTypeField = field;
	
    [ExpenseTypesViewController showExpenseTypeEditor:self 
            policy:self.rpt.polKey
            parentVC:self selectedExpKey:field.liKey parentExpKey:self.entry.expKey withReport:self.rpt];
    
}


-(void) fieldUpdated:(FormFieldData*) field
{
	// Update number of nights
	[self updateNumberOfNights:field];
	[super fieldUpdated:field];
}


-(void) sendItemizeHotelMsg
{
	
	FormFieldData* checkInDateFld = [self findEditingField:@"checkInDate"];
	FormFieldData* checkOutDateFld = [self findEditingField:@"checkOutDate"];
	FormFieldData* numberOfNightsFld = [self findEditingField:@"numberOfNights"];
	NSNumber* numberOfNights = @([[numberOfNightsFld getServerValue] intValue]);
	FormFieldData* roomRateFld = [self findEditingField:@"roomRate"];
	NSNumber* roomRate = @([[roomRateFld getServerValue] doubleValue]);
	FormFieldData* roomTaxFld = [self findEditingField:@"roomTax"];
	NSNumber* roomTax = @([[roomTaxFld getServerValue] doubleValue]);
    
    FormFieldData* otherRoomTax1Fld = [self findEditingField:@"otherRoomTax1"];
	NSNumber* otherRoomTax1 = @([[otherRoomTax1Fld getServerValue] doubleValue]);
    FormFieldData* otherRoomTax2Fld = [self findEditingField:@"otherRoomTax2"];
	NSNumber* otherRoomTax2 = @([[otherRoomTax2Fld getServerValue] doubleValue]);
    
	FormFieldData* expType1Fld = [self findEditingField:@"expType1"];
	FormFieldData* amount1Fld = [self findEditingField:@"amount1"];
	
	NSArray* extraCharges = nil;
	// TODO - check amount format
	if (expType1Fld.liKey != nil && ![expType1Fld.liKey isEqual:@""])
	{
		NSArray* exp1 = @[expType1Fld.liKey, [amount1Fld getServerValue]];
		NSArray* exp2 = nil;
		FormFieldData* expType2Fld = [self findEditingField:@"expType2"];
		FormFieldData* amount2Fld = [self findEditingField:@"amount2"];
		if (expType2Fld.liKey != nil && ![expType2Fld.liKey isEqual:@""])
		{
			exp2 = @[expType2Fld.liKey, [amount2Fld getServerValue]];
		}
		extraCharges = [NSArray arrayWithObjects:exp1, exp2, nil];
	}
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 self.entry, @"ENTRY",
								 self.role, @"ROLE_CODE",
								 checkInDateFld.fieldValue, @"CHECK_IN_DATE",
								 checkOutDateFld.fieldValue, @"CHECK_OUT_DATE",
								 numberOfNights, @"NUMBER_OF_NIGHTS",
								 roomRate, @"ROOM_RATE",
								 roomTax, @"ROOM_TAX",
                                 otherRoomTax1, @"OTHER_ROOM_TAX1",
                                 otherRoomTax2, @"OTHER_ROOM_TAX2",
								 extraCharges, @"ADDITIONAL_CHARGES",
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:ITEMIZE_HOTEL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
	
	
}

-(BOOL) validateFields:(BOOL*) missingReqFields
{
	BOOL result = [super validateFields:missingReqFields];
	if (*missingReqFields || ! result)
		return FALSE;
	
	FormFieldData* checkInFld = [self findEditingField:@"checkInDate"];
	FormFieldData* checkOutFld = [self findEditingField:@"checkOutDate"];
	NSDate * checkInDate = [DateTimeFormatter getLocalDate:checkInFld.fieldValue];
	NSDate * checkOutDate = [DateTimeFormatter getLocalDate:checkOutFld.fieldValue];
	if (checkInDate == nil || checkOutDate == nil)
		return FALSE;
	
	NSTimeInterval diffTime = [checkOutDate timeIntervalSinceDate:checkInDate];
	int days = diffTime/(60*60*24);
	if (days < 1)
	{
		checkOutFld.validationErrMsg = [Localizer getLocalizedText:@"CHECK_OUT_DATE_ERR_MSG"];
		return FALSE;
	}
    else if (days > 90)
    {
        checkOutFld.validationErrMsg = [Localizer getLocalizedText:@"NIGHTS_TOO_LARGE_ERR_MSG"];
        return FALSE;
    }
	return TRUE;
}
-(void) saveForm:(BOOL) copyDownToChildForms
{
	if ([self isDirty])
	{
		[self showWaitView];
		[self sendItemizeHotelMsg];
	}
}

#pragma mark -
#pragma mark ExpenseType delegate- (FormFieldData*) getCurrentExpenseTypeField
- (FormFieldData*) getCurrentExpenseTypeField
{
    return expTypeField;
}

#pragma mark -
#pragma mark View lifecycle


- (void)viewDidLoad {
    [super viewDidLoad];
    
    NSDictionary *dictionary = @{@"Hotel": @"Type"};
    [Flurry logEvent:@"Report Entry: Itemize Entry" withParameters:dictionary];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.title = [Localizer getLocalizedText:@"Itemizations"];
}


#pragma mark -
#pragma mark Memory management


#pragma mark -
#pragma mark Table delegate 
- (NSString *)tableView:(UITableView *)tblView titleForHeaderInSection:(NSInteger)section
{
    if (section == 2)
    {
        return [Localizer getLocalizedText:@"Additional Charges (each night)"];
    }
	return nil;
}

@end

