//
//  GovDocExpenseDetailVC.m
//  ConcurMobile
//
//  Created by charlottef on 2/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocExpenseDetailVC.h"
#import "SummaryCellMLines.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "DrillCell.h"
#import "ReceiptEditorVC.h"
#import "FormViewControllerBase.h"
#import "GovAttachReceiptData.h"

#define kAlertViewConfirmExitWithoutSave 3821

#define kSectionSummary @"SectionSummary"
#define kSectionReceipt @"SectionReceipt"

#define kRowSummary @"RowSummary"
#define kRowReceipt @"RowReceipt"

@interface GovDocExpenseDetailVC ()

@property BOOL isDirty;
// UIControl *backCover is the best way at the time to change the default behavior for BACK button
@property UIControl *backCover;

@property (nonatomic, strong) NSMutableArray *sections;
@property (nonatomic, strong) NSMutableDictionary *rows;

-(void) initSectionsAndRows;

-(UITableViewCell *) configureSummaryCell;
-(UITableViewCell *) configureReceiptCellForTableView:(UITableView *)tableView;

-(int) getReceiptSectionIndex;

-(BOOL) isReceiptUpdated;

-(void) actionBack:(id)sender;

-(void) updateSaveBtn;
-(void) actionSave:(id)sender;

-(void)saveCompleted;
-(void)saveFailed;

-(void) closeMe;

@end

@implementation GovDocExpenseDetailVC

@synthesize tableList, expense, docType, docName, receipt;
@synthesize isDirty = _isDirty;
@synthesize sections = _sections;
@synthesize rows = _rows;

#pragma mark - Initialization
-(void)setSeedDataWithExpense:(GovDocExpense*) documentExpense docType:(NSString*)documentType docName:(NSString*)documentName
{
    self.expense = documentExpense;
    self.docType = documentType;
    self.docName = documentName;
    
    // If the expense has a receipt id, then create a receipt object.
    if (self.expense.imageId != nil && self.expense.imageId.length > 0)
    {
        self.receipt = [[Receipt alloc] init];
        self.receipt.receiptId = self.expense.imageId;
    }
    
    [self initSectionsAndRows];
}

-(void) initSectionsAndRows
{
    self.sections = [[NSMutableArray alloc] initWithObjects:kSectionSummary, kSectionReceipt, nil];
    self.rows = [[NSMutableDictionary alloc] init];

    NSMutableArray *summarySection = [[NSMutableArray alloc] initWithObjects:kRowSummary, nil];
    [self.rows setObject:summarySection forKey:kSectionSummary];
    
    NSMutableArray *receiptSection = [[NSMutableArray alloc] initWithObjects:kRowReceipt, nil];
    [self.rows setObject:receiptSection forKey:kSectionReceipt];
}

-(void) actionClose:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    self.title = [Localizer getLocalizedText:@"Expense"];
}

-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    [self updateSaveBtn];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if ( self.backCover == nil )
    {
        const int kButtonA2RW_Max = 100;
        const int kButtonA2RW_Min = 80; //40;
        
        NSUInteger count = [self.navigationController.viewControllers count];
        // TODO : count-2 could be negative
        UIViewController *parentView = self.navigationController.viewControllers[MAX(0,count-2)];
        
        CGSize s = [parentView.title sizeWithFont:[UIFont boldSystemFontOfSize:22]];
        int size = (s.width > kButtonA2RW_Max) ? kButtonA2RW_Max : ((s.width < kButtonA2RW_Min)?kButtonA2RW_Min:s.width);
        size += 10;
        self.backCover = [[UIControl alloc] initWithFrame:CGRectMake( 0, 0, size, 44)];
        // Uncomment these lines to see the coverage of back button
#if TARGET_IPHONE_SIMULATOR
        //        // show the cover for testing
        //        backCover.backgroundColor = [UIColor colorWithRed:1.0 green:0.0 blue:0.0 alpha:0.15];
#endif
        [self.backCover addTarget:self action:@selector(actionBack:) forControlEvents:UIControlEventTouchDown];
        UINavigationBar *navBar = self.navigationController.navigationBar;
        [navBar addSubview:self.backCover];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    [self.backCover removeFromSuperview];
    self.backCover = nil;
}


#pragma mark - Messaging

-(void)respondToFoundData:(Msg *)msg
{
    if (![self isViewLoaded])
        return;
    
    if ([msg.idKey isEqualToString:GOV_ATTACH_RECEIPT])
    {
        [self hideWaitView];

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
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return self.sections.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionName = [self.sections objectAtIndex:section];
    NSArray* rows = [self.rows objectForKey:sectionName];
    return rows.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = nil;

    NSString *sectionName = [self.sections objectAtIndex:indexPath.section];
    NSArray* rows = [self.rows objectForKey:sectionName];
    NSString *rowName = [rows objectAtIndex:indexPath.row];
    
    if ([kRowSummary isEqualToString:rowName])
    {
        cell = [self configureSummaryCell];
    }
    else if ([kRowReceipt isEqualToString:rowName])
    {
        cell = [self configureReceiptCellForTableView:tableView];
    }
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = [self.sections objectAtIndex:indexPath.section];
    
    if ([kSectionSummary isEqualToString:sectionName])
        return 64;
	if ([kSectionReceipt isEqualToString:sectionName])
        return 40;
    
    return 0;
}

-(UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *sectionName = [self.sections objectAtIndex:indexPath.section];
    NSArray* rows = [self.rows objectForKey:sectionName];
    NSString *rowName = [rows objectAtIndex:indexPath.row];

    if ([kRowReceipt isEqualToString:rowName])
    {
        [self showReceiptViewer];
    }
}

#pragma mark - Table cell configuration

-(UITableViewCell *) configureSummaryCell
{
    SummaryCellMLines *cell = (SummaryCellMLines *)[self.tableList dequeueReusableCellWithIdentifier: @"SummaryCell4Lines"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell4Lines" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[SummaryCellMLines class]])
                cell = (SummaryCellMLines *)oneObject;
    }
    
    NSString* amountStr = [FormatUtils formatMoneyWithNumber:expense.amount crnCode:@"USD"];
    NSString* dateStr = [DateTimeFormatter formatDate:expense.expDate Format:@"MMM dd, yyyy" TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    [cell resetCellContent:expense.expenseDesc withAmount:amountStr withLine1: expense.expenseCategory withLine2: dateStr withImage1:nil withImage2:nil withImage3:nil];
    
    cell.lblRLine1.text = expense.paymentMethod;
    CGRect frame = cell.lblRLine1.frame;
    cell.lblRLine1.frame = CGRectMake(frame.origin.x, cell.lblLine2.frame.origin.y, frame.size.width, frame.size.height);
    return cell;
    
	[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
    return cell;
}

-(UITableViewCell *) configureReceiptCellForTableView:(UITableView *)tableView
{
    NSString* text = @"";

    if (self.receipt == nil && ![self.receipt hasReceipt] && [ExSystem connectedToNetwork])
    {
        text = [Localizer getLocalizedText:@"Add Receipt"];
    }
    else
    {
        text = [Localizer getLocalizedText:@"View Receipt"];
    }
    
    UITableViewCell* cell = [DrillCell makeDrillCell:tableView withText:text withImage:@"icon_receipt_button" enabled:YES];
    return cell;
}

#pragma mark - Receipt Viewer
- (void)showReceiptViewer
{
	ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = NO;
    receiptView.canUpdate = YES;
    [receiptView setSeedData:self.receipt];
    [self.navigationController pushViewController:receiptView animated:YES];
}

#pragma mark - ReceiptEditorDelegate Methods

-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint
{
    self.receipt = rcpt;
    self.isDirty = YES;
    
    NSMutableArray* ixPaths = [[NSMutableArray alloc] init];
    NSUInteger _path[2] = {[self getReceiptSectionIndex], 0};
    NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
    [ixPaths addObject:_indexPath];
    [self.tableList reloadRowsAtIndexPaths:ixPaths withRowAnimation:UITableViewRowAnimationRight];
}

-(void) receiptDeleted:(Receipt*) rcpt
{
    self.receipt = nil;
}

//-(void) receiptQueued:(Receipt*) receipt;

#pragma mark - Receipt Utils
-(int) getReceiptSectionIndex
{
    for (int sectionIndex = 0; sectionIndex < self.sections.count; sectionIndex++)
    {
        NSString *sectionId = [self.sections objectAtIndex:sectionIndex];
        if (sectionId != nil && [sectionId isEqualToString:kSectionReceipt])
            return sectionIndex;
    }
    return NSNotFound;
}

#pragma mark - Buttons
-(void) actionBack:(id)sender
{
    if (self.isDirty)
        [self askToSaveChanges];
    else
        [self closeMe];
}

-(void) updateSaveBtn
{
	UIBarButtonItem* saveBtn  = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave target:self action:@selector(actionSave:)];
    [saveBtn setTitle:[Localizer getLocalizedText:@"LABEL_SAVE_BTN"]];
	[saveBtn setEnabled:self.isDirty];
	[self.navigationItem setRightBarButtonItem:saveBtn animated:NO];
}

-(void) actionSave:(id)sender
{
	if (![self isDirty])
		return;
    
	if(![ExSystem connectedToNetwork])
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle: [Localizer getLocalizedText:@"Cannot Save Changes"]
							  message: [Localizer getLocalizedText:@"OFFLINE_MSG"]
							  delegate:nil
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
	}
    else
    {
        if (self.expense.expId != nil && [self isReceiptUpdated])
        {
            if ([self.receipt.receiptId length])
            {
                [self showWaitViewWithText:[Localizer getLocalizedText:@"Saving Expense"]];
                
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             self.receipt.receiptId, @"RECEIPT_ID",
                                             self.expense.expId, @"EXP_ID",
                                             self.docType, @"DOC_TYPE",
                                             self.docName, @"DOC_NAME",
                                             nil];
                [[ExSystem sharedInstance].msgControl createMsg:GOV_ATTACH_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            }
            else
            {
                // Handle receipt deletion
            }
        }
    }
}

-(void)saveCompleted
{
    // The expense variable is a pointer to the very same expense object that is used by GovDocDetailVC and GovDocExpensesVC so updating it here will cause it to be updated there was well.
    self.expense.imageId = self.receipt.receiptId;
    
    [self closeMe];
}

-(void)saveFailed
{
    UIAlertView *alert = [[MobileAlertView alloc]
                          initWithTitle: nil
                          message: [Localizer getLocalizedText:@"Save entry receipt failed"]
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                          otherButtonTitles:nil];
    [alert show];
}

#pragma mark - Util Methods

-(BOOL) isReceiptUpdated
{
    NSString *oldReceiptId = self.expense.imageId;
    
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

#pragma mark - Exit Methods
-(void) askToSaveChanges
{
	// Alert to set required fields before save
	UIAlertView *alert = [[MobileAlertView alloc]
						  initWithTitle:nil
						  message:[Localizer getLocalizedText:@"RPT_SAVE_CONFIRM_MSG"]
						  delegate:self
						  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CANCEL_BTN"]
						  otherButtonTitles:[Localizer getLocalizedText:@"Yes"],[Localizer getLocalizedText:@"No"], nil];
	
	alert.tag = kAlertViewConfirmExitWithoutSave;
	[alert show];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if(alertView.tag == kAlertViewConfirmExitWithoutSave)
	{
		if (buttonIndex == 1) // Yes - save changes
		{
            [self actionSave:self];
		}
		else if (buttonIndex == 2) // No - exit without changes
		{
            [self closeMe];
		}
	}
}

-(void) closeMe
{
    if ([UIDevice isPad])
        [self dismissViewControllerAnimated:YES completion:nil];
    else
        [self.navigationController popViewControllerAnimated:YES];
}

@end
