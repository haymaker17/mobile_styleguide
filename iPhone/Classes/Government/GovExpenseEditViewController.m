//
//  GovExpenseEditViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 9/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovExpenseEditViewController.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "GovExpenseTypesViewController.h"
#import "GovExpenseFormData.h"
#import "DrillCell.h"
#import "ReceiptEditorVC.h"
#import "GovExpenseSaveData.h"
#import "EntityGovExpenseExtension.h"
#import "GovListFieldEditVC.h"
#import "SummaryCellMLines.h"
#import "GovAttachReceiptData.h"

#define kSummarySection @"SummarySection"
#define kReceiptSection @"ReceiptSection"
#define kFieldsSection  @"FieldsSection"

@interface GovExpenseEditViewController (private)
-(void) configureFieldsForNewExpense;
-(void) configureFieldsForExistingExpense;
-(BOOL) hasSummarySection;
-(BOOL) hasReceiptSection;
-(int) getReceiptSectionIndex;

@end

@implementation GovExpenseEditViewController

@synthesize expenseId, receipt, formAttributes;
@synthesize delegate = _delegate;

#pragma mark - Seed
-(void) setSeedDelegate:(id<GovExpenseEditDelegate>)del
{
    self.delegate = del;
}

#pragma mark - View lifecycle
-(void)viewWillAppear:(BOOL)animated
{
	if(doReload)
	{
		doReload = NO;
		[tableList reloadData];
        [[MCLogging getInstance] log:@"viewWillAppear:reload table" Level:MC_LOG_DEBU];
	}
	
	[super viewWillAppear:animated];
}

- (void)viewDidLoad
{
    [self setupNaviBar];
    
    if (self.allFields == nil)
    {
        if ([self.expenseId length])
            [self configureFieldsForExistingExpense];
        else
            [self configureFieldsForNewExpense];
    }
    
    [super viewDidLoad];
    
    // Show default back button
    [self.navigationItem setHidesBackButton:NO animated:NO];
    
    self.title = [Localizer getLocalizedText:@"Quick Expense"];
}

-(void)setupNaviBar
{
    NSInteger count = [self.navigationController.viewControllers count];
    int viewIndex = 0;
	for (int ix = 0; ix <count; ix++)
	{
		MobileViewController* vc = (MobileViewController*) (self.navigationController.viewControllers)[ix];
		if (vc == self)
		{
			viewIndex = ix;
			break;
		}
	}
    
    if (viewIndex < 1)
	{
		if([UIDevice isPad])
		{
			UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionBack:)];
			self.navigationItem.leftBarButtonItem = btnClose;
		}
    }
}

// show the gov specific expense types
-(void)showExpenseTypeEditor:(FormFieldData*) field
{
	GovExpenseTypesViewController *lvc = [[GovExpenseTypesViewController alloc] initWithNibName:@"ListFieldEditVC" bundle:nil];
    [lvc setSeedData:field delegate:self];
	
	if([UIDevice isPad])
		lvc.modalPresentationStyle = UIModalPresentationFormSheet;
	[self.navigationController pushViewController:lvc animated:YES];
//	[self prefetchForListEditor:lvc];
}

#pragma mark - Field configuration
-(void) configureFieldsForNewExpense
{
    self.sections = [[NSMutableArray alloc] initWithObjects:kFieldsSection, nil];

    self.allFields = [[NSMutableArray alloc] initWithObjects: nil];
    
    FormFieldData *expenseDescriptionField = [[FormFieldData alloc] initField:@"expense.tran_description" label:@"Expense Description" value:@"" ctrlType:@"edit" dataType:@"VARCHAR"];
    expenseDescriptionField.required = @"Y";
    [allFields addObject:expenseDescriptionField];

    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    [sectionFieldsMap setObject:allFields forKey:kFieldsSection];

    self.sectionDataMap = [[NSMutableDictionary alloc] init];
    
    [tableList reloadData];
}

-(void) configureFieldsForExistingExpense
{
    self.sections = [[NSMutableArray alloc] initWithObjects:kSummarySection, kReceiptSection, kFieldsSection, nil];
    
    self.allFields = [[NSMutableArray alloc] initWithObjects: nil];

    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
    [sectionFieldsMap setObject:[NSArray arrayWithObject:kSummarySection] forKey:kSummarySection];
    [sectionFieldsMap setObject:[NSArray arrayWithObject:kReceiptSection] forKey:kReceiptSection];
    [sectionFieldsMap setObject:allFields forKey:kFieldsSection];
    
    self.sectionDataMap = [[NSMutableDictionary alloc] init];

    // Look up the expense in core data
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    EntityGovExpense *exp = [EntityGovExpense fetchById:self.expenseId inContext:[ad managedObjectContext]];

    // If the expense has a receipt id, then create a receipt object.
    if (exp != nil && exp.imageId != nil && exp.imageId.length > 0)
    {
        self.receipt = [[Receipt alloc] init];
        self.receipt.receiptId = exp.imageId;
    }
    
    
    // TODO: Add the expense's fields to self.allFields
}

-(void) updateExpenseTypeImpl:(GovExpenseType *)newExpenseType
{
	[self showLoadingViewWithText:[Localizer getLocalizedText:@"Loading Data"]];
	
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 newExpenseType.docType, @"DOC_TYPE",
                                 newExpenseType.expenseDescription, @"EXP_DESCRIP",
								 //newExpenseType.expKey, @"EXP_KEY",
								 //rpt.rptKey, @"RPT_KEY",
								 //entry.rpeKey, @"PARENT_RPE_KEY",
								 nil];
	[[ExSystem sharedInstance].msgControl createMsg:GOV_EXPENSE_FORM_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    
    //[self dismissViewControllerAnimated:YES completion:nil];
}

-(void) updateExpenseType:(GovExpenseType*) newExpenseType;
{
    [allFields removeAllObjects];
    [allFields addObjectsFromArray:newExpenseType.expenseForm.formData];
    
    // Define receipt section
    self.sections = [[NSMutableArray alloc] initWithObjects:kReceiptSection, kFieldsSection, nil];
    [sectionDataMap setObject:[NSArray arrayWithObject:kReceiptSection] forKey:kReceiptSection];

    [self updateExpenseTypeImpl:newExpenseType];
    
    [tableList reloadData];
}

-(void) saveForm:(BOOL) cpDownToChildForms
{
    BOOL formHasFields = (self.allFields == nil || self.allFields.count > 0);
    if (formHasFields)
    {
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Saving Expense"]];
        [self processFieldForSaveRequest];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: GOV_QUICK_EXPENSE, @"TO_VIEW", self.allFields, @"FIELDS", self.formAttributes, @"ATTRIBUTES", nil];
        
        [[ExSystem sharedInstance].msgControl createMsg:SAVE_GOV_EXPENSE CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else
    {
        if (self.expenseId != nil && [self isReceiptUpdated])
        {
            if ([self.receipt.receiptId length])
            {
                [self showWaitViewWithText:[Localizer getLocalizedText:@"Saving Expense"]];

                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             self.receipt.receiptId, @"RECEIPT_ID",
                                             self.expenseId, @"EXP_ID",
                                             nil];
                [[ExSystem sharedInstance].msgControl createMsg:GOV_ATTACH_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            }
            else
            {
                // TODO: Handle receipt deletion
            }
        }
    }
}

-(void) processFieldForSaveRequest
{
    // change date format per GSA endpoint reques
    for (FormFieldData *field in allFields) {
        if ([field.dataType isEqualToString:@"TIMESTAMP"])
        {
            NSString *gsaDateFormat = [self formatDate:field.fieldValue];
            field.fieldValue = gsaDateFormat;
        }
    }
}

-(void)saveCompleted
{
// hide wait view, so background view can become alive after dismiss QE form.
    [self.delegate updatedExpense:self.expenseId];
        
    [self hideWaitView];

    if ([UIDevice isPad])
    {
        if (self.isWaitViewShowing)
            [self hideWaitView];
        //cause adding receipt to an exising unapplied expenses view to dismiss.
        //correct behavior pop back to unapplied expenses list view.
        if ([self.navigationController.viewControllers count] > 1)
        {
            [self.navigationController popViewControllerAnimated:YES];
        }
        else
            [self dismissViewControllerAnimated:YES completion:nil];
    }
    else
    {
        if (self.isWaitViewShowing)
            [self hideWaitView];
        [self.navigationController popViewControllerAnimated:YES];
    }
    
    NSDictionary *param = @{@"With Receipt": ![receipt.receiptId length] ? @"NO":@"YES", @"Result": @"Success"};
    [Flurry logEvent:@"Quick Expense: Create" withParameters:param];
}

-(void)saveFailed
{
    [self hideWaitView];

    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle: nil
                          message: [Localizer getLocalizedText:@"Save entry receipt failed"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                          otherButtonTitles:nil];
    [alert show];
    
    NSDictionary *param = @{@"With Receipt": ![receipt.receiptId length] ? @"NO":@"YES", @"Result": @"Fail"};
    [Flurry logEvent:@"Quick Expense: Create" withParameters:param];
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:GOV_ATTACH_RECEIPT])
    {
        if (![self isViewLoaded])
            return;
        
        GovAttachReceiptData *receiptMsg = (GovAttachReceiptData*)msg.responder;
        if (msg.responseCode == 200 && [@"Success" isEqualToString:receiptMsg.status.status])
        {
            [self saveCompleted];
        }
        else
        {
            [self saveFailed];
        }
    }
    else if ([msg.idKey isEqualToString:SAVE_GOV_EXPENSE])
	{
        // TODO: save images and handle errors
        if ([self isReceiptUpdated] /*&& TODO : save is successful*/)
        {
            GovExpenseSaveData* esData = (GovExpenseSaveData*) msg.responder;
            NSString* expId = esData.expId;
            
            if ([expId length])
            {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             receipt.receiptId, @"RECEIPT_ID",
                                             expId, @"EXP_ID",
                                             nil];
                [[ExSystem sharedInstance].msgControl createMsg:GOV_ATTACH_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

                return;
            }
        }

        [self saveCompleted];
    }
    
    if ([msg.idKey isEqualToString:GOV_EXPENSE_FORM_DATA])
    {
        GovExpenseFormData* srd = (GovExpenseFormData*) msg.responder;
        if (srd != nil) {
//            if (self.expenseType != nil)
//            {
//                [allFields removeAllObjects];
//                [allFields addObject:expenseTypeField];
//            }
//            //Remove this causes new saved unapplied expense to have NO title
//            //New Jira created: MOB-11960
//            [self removeField:srd.formData withString:@"Expense Description"];
            [allFields removeAllObjects];
            [allFields addObjectsFromArray:srd.formData];
            self.formAttributes = srd.otherFormAttributes;
        }
        [tableList reloadData];
    }
    
    if (self.isLoadingViewShowing)
        [self hideLoadingView];
}

#pragma mark - FormViewControllerBase override
-(BOOL)shouldUseCacheOnlyForListEditor:(GovListFieldEditVC *)lvc
{
    if([lvc.field.listChoices count])
        return YES;
    else
        return NO;
}

-(BOOL) canEdit
{
    return ([super canEdit] || [self hasReceiptSection]);
}

-(BOOL) hasReceiptSection
{
    NSArray *list = [self.sectionFieldsMap objectForKey:kReceiptSection];
    BOOL hasReceiptSection = (list != nil && list.count > 0 && [[list objectAtIndex:0] isEqualToString:kReceiptSection]);
    return hasReceiptSection;
}

-(void)showListEditor:(FormFieldData*) field
{
    // Check if we can edit, if this is a connected list field
	if (field.parFieldId != nil && field.parLiKey == nil)
	{
		FormFieldData* parFld = [self findEditingField:field.parFieldId];
		if (field.parFtCode != nil && [field.parFtCode isEqualToString:field.ftCode]
			&& (parFld.access == nil || [parFld.access isEqualToString:@"RW"]))
		{
			// alert user that they need to select value for parent field first
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:nil
								  message:[NSString stringWithFormat:[Localizer getLocalizedText:@"FILL_IN_PARENT_MLIST_FIELD"], parFld.label]
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		else {
			// alert user that they need to select value for parent field first
			UIAlertView *alert = [[MobileAlertView alloc]
								  initWithTitle:nil
								  message:[Localizer getLocalizedText:@"MLIST_FIELD_EDIT_NOT_SUPPORT"]
								  delegate:nil
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
								  otherButtonTitles:nil];
			[alert show];
		}
		return;
	}
	
	GovListFieldEditVC *lvc = nil;
    if ([field.ctrlType isEqualToString:@"combo"])
    {
        lvc = [[GovListFieldEditVC alloc] initWithNibName:@"ComboFieldEditVC" bundle:nil];
        
    }else
    {
    	lvc = [[GovListFieldEditVC alloc] initWithNibName:@"ListFieldEditVC" bundle:nil];
    }
    
    if([UIDevice isPad])
		lvc.modalPresentationStyle = UIModalPresentationFormSheet;
	[self.navigationController pushViewController:lvc animated:YES];
    
    lvc.formAttributes = self.formAttributes;
    [lvc setSeedData:field delegate:self keysToExclude:[self getExcludeKeysForListEditor:field]];
	[self prefetchForListEditor:lvc];
}

-(BOOL) hasSummarySection
{
    NSArray *list = [self.sectionFieldsMap objectForKey:kSummarySection];
    BOOL hasSummarySection = (list != nil && list.count > 0 && [[list objectAtIndex:0] isEqualToString:kSummarySection]);
    return hasSummarySection;
}


#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return GOV_QUICK_EXPENSE;
}


#pragma mark -
#pragma mark Table View Data Source Methods

- (UITableViewCell *)tableView:(UITableView *)tblView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
    NSString *sectionId = [sections objectAtIndex:section];
	
	if ([kFieldsSection isEqualToString:sectionId])
	{
		UITableViewCell *cell = [super tableView:tblView cellForRowAtIndexPath:indexPath];
        return cell;
	}
	else if ([kReceiptSection isEqualToString:sectionId])
	{
        NSString* command = @"";
        // MOB-8256
        if (self.receipt==nil && ![self.receipt hasReceipt] && [ExSystem connectedToNetwork])
        {
            command = [Localizer getLocalizedText:@"Add Receipt"];
        }
        else
        {
            command = [Localizer getLocalizedText:@"View Receipt"];
        }
        UITableViewCell* cell = [DrillCell makeDrillCell:tblView withText:command withImage:@"icon_receipt_button" enabled:YES];

    	return cell;
	}
    else // kSummarySection
    {
        SummaryCellMLines *cell = (SummaryCellMLines *)[tblView dequeueReusableCellWithIdentifier: @"SummaryCell3Lines"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell3Lines" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[SummaryCellMLines class]])
                    cell = (SummaryCellMLines *)oneObject;
        }
        
        [self configureSummaryCell:cell atIndexPath:indexPath];
        
        return cell;
    }
}

#pragma mark - Cell Config
- (void)configureSummaryCell:(SummaryCellMLines *)cell atIndexPath:(NSIndexPath *)indexPath
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    EntityGovExpense *expense = [EntityGovExpense fetchById:self.expenseId inContext:[ad managedObjectContext]];
    
    NSString* amountStr = [FormatUtils formatMoneyWithNumber:expense.amount crnCode:@"USD"];
    NSString* dateStr = [DateTimeFormatter formatDate:expense.expDate Format:@"MMM dd, yyyy" TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    NSString * image1 = nil; //![expense.imageId length]? nil: @"icon_receipt_19";
    [cell resetCellContent:expense.expenseDesc withAmount:amountStr withLine1:dateStr  withLine2: nil withImage1:image1 withImage2:nil withImage3:nil];
}

#pragma mark -
#pragma mark Receipt Action
- (void)showReceiptViewer
{
    // As of January 22, 2013, there is no API for deleting a receipt for an existing expense.  Therefore, a receipt can only be deleted for a brand new quick expense that has not yet been saved.
    BOOL isNewExpense = (self.expenseId == nil || self.expenseId.length == 0);

	ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = isNewExpense;
    receiptView.canUpdate = YES;
    [receiptView setSeedData:self.receipt]; // Store the receipt object here??
    [self.navigationController pushViewController:receiptView animated:YES];
}

-(BOOL) isReceiptUpdated
{
    NSString *oldReceiptId = nil;
    
    if (self.expenseId != nil)
    {
        // Look up the expense in core data
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        EntityGovExpense *exp = [EntityGovExpense fetchById:self.expenseId inContext:[ad managedObjectContext]];
        
        oldReceiptId = (exp != nil ? exp.imageId : nil);
    }

    BOOL hadReceiptBefore = [oldReceiptId length];
    BOOL hasReceiptNow = [self.receipt.receiptId length];
    
    if (hadReceiptBefore)
    {
        return (!hasReceiptNow || ![self.receipt.receiptId isEqualToString:oldReceiptId]);
    }
    else
    {
        return hasReceiptNow;
    }
}

#pragma mark ReceiptEditorDelegate
-(void) receiptDeleted:(Receipt*) rcpt
{
    self.receipt = nil;
}

-(void) receiptQueued:(Receipt*) receipt{}  // For offline?

-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint
{
    self.receipt = rcpt;
    [self setIsDirty:YES];
    
    NSMutableArray* ixPaths = [[NSMutableArray alloc] init];
    NSUInteger _path[2] = {[self getReceiptSectionIndex], 0};
    NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
    [ixPaths addObject:_indexPath];
    [self.tableList reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
}

//-(void) receiptQueued:(Receipt*) receipt;  // For offline?

-(int) getReceiptSectionIndex
{
    for (int sectionIndex = 0; sectionIndex < sections.count; sectionIndex++)
    {
        NSString *sectionId = [sections objectAtIndex:sectionIndex];
        if (sectionId != nil && [sectionId isEqualToString:kReceiptSection])
            return sectionIndex;
    }
    return NSNotFound;
}

#pragma mark -
#pragma mark Table Delegate Methods
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
	NSUInteger section = [newIndexPath section];
    NSString *sectionId = [sections objectAtIndex:section];
    
    if ([kFieldsSection isEqualToString:sectionId])
    {
        [super tableView:tableView didSelectRowAtIndexPath:newIndexPath];
    }
    else
    {
        if ([kReceiptSection isEqualToString:sectionId])
        {
            //			if ([self isDirty] && [self.entry.rpeKey length])
            //			{   // MOB-7170 Save before update receipt for existing entries
            //				[self confirmToSave:kAlertViewConfirmSaveUponReceipt];
            //			}
            //			else
            //			{
            [self showReceiptViewer];
        }
    }
    
    [tableView deselectRowAtIndexPath:newIndexPath animated:NO];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger section = [indexPath section];
    NSString *sectionId = [sections objectAtIndex:section];

    if ([kSummarySection isEqualToString:sectionId])
        return 50;
	if ([kReceiptSection isEqualToString:sectionId])
        return 40;
	else // Fields section
	{
		return [super tableView:tableView heightForRowAtIndexPath:indexPath];
	}
}


// TM uses a different date format. Consider moving this to FormatUtil.
-(NSString *)formatDate:(NSString *)dateStr
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
    [dateFormatter setLocale:usLocale];
    
    [dateFormatter setDateFormat: @"yyyy-MM-dd'T'HH:mm:ss"];
    NSDate* date = [dateFormatter dateFromString:dateStr];
    
    [dateFormatter setDateFormat: @"MM/dd/yy"];
    __autoreleasing NSString* result = [dateFormatter stringFromDate:date];
    return result;
}

@end
