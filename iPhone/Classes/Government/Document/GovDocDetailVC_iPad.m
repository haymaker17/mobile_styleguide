//
//  GovDocDetailVC_iPad.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocDetailVC_iPad.h"
#import "GovDocumentCell.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "GovDocumentDetailData.h"
#import "GovDocExpenseDetailVC.h"
#import "KeyboardNavigationController.h"
#import "GovDocStampVC.h"
#import "GovDocCommentVC.h"
#import "GovDocPerDiemVC.h"
#import "GovDocAcctVC.h"
#import "GovDocExceptionsVC.h"
#import "GovDocTotalsVC.h"
#import "GovUnappliedExpensesVC.h"
#import "ReceiptEditorVC.h"
#import "GovAttachReceiptData.h"
#import "ReceiptCache.h"

#define GAP_BETWEEN_TABLE_ROWS 8.0
#define MAX_REPORT_BUTTONS 8

#define BUTTON_ID_DOC_RECEIPTS @"BUTTON_ID_DOC_RECEIPTS"
#define BUTTON_ID_PDLOCATIONS @"BUTTON_ID_PDLOCATIONS"
#define BUTTON_ID_ADD_EXPENSE @"BUTTON_ID_ADD_EXPENSE"
#define BUTTON_ID_ACCT_ALLLOCATION @"BUTTON_ID_ACCT_ALLOCATION"
#define BUTTON_ID_TOTALS @"BUTTON_ID_TOTALS"
#define BUTTON_ID_AUDITS @"BUTTON_ID_AUDITS"
#define BUTTON_ID_COMMENTS @"BUTTON_ID_COMMENTS"

#define BUTTON_ID_STAMP_DOC @"BUTTON_ID_STAMP_DOC"

@interface GovDocDetailVC_iPad (Private)
-(void)drawLeftPanel;
-(void) buttonEntryDetailClicked:(GovDocExpense*)expense;
-(void) buttonAddEntryClicked:(id)sender withFrame:(CGRect)rect;
-(void) buttonAuditsClicked:(id)sender;
-(void) buttonAllocationClicked:(id)sender;
-(void) buttonTotalsClicked:(id)sender;
-(void) buttonCommentsClicked:(id)sender;
-(void) buttonPerdiemLocationClicked:(id)sender;
-(void) buttonReceiptsClicked:(id)sender;
@end

@implementation GovDocDetailVC_iPad
@synthesize receipt, doc;
@synthesize rightTableView, lblExpensesTitle, lblDocTraveler, lblDocName, lblDocType, lblDocDate;
@synthesize lblStatus, lblTANumber, lblTrip, lblEmissions, lblTotalAmount, lblLabelStatus, lblLabelTANumber, lblLabelTrip, lblLabelEmissions;
@synthesize btnStamp;
@synthesize iPadHome;
@synthesize stampSuccess;

-(void) setSeedData:(NSMutableDictionary*)pBag
{
    [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    self.stampSuccess = NO;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.rightTableView.dataSource = self;
    self.rightTableView.delegate = self;
    
    if (self.doc == nil)
        [self showLoadingView];
    else
    {
        [self updateViews];
    }
}

- (void) viewWillLayoutSubviews
{
    [super viewWillLayoutSubviews];
    
    //Handle table layout
    float viewWidth = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 1024.0 : 768.0;
    float viewHeight = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 660.0 : 916.0;
    
    // Layout the left pane
    float newPaneWidth = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 320.0 : 266.0;
    CGRect newPaneFrame = CGRectMake(0, 0, newPaneWidth, viewHeight);
    self.leftPaneView.frame = newPaneFrame;
    
    // Layout the right table
    const float widthOfBorderAroundTable = 12.0;
    float newTableLeft = newPaneWidth + widthOfBorderAroundTable; // Account for left border
    float newTableTop = widthOfBorderAroundTable + lblExpensesTitle.frame.origin.y + lblExpensesTitle.frame.size.height; // Account for top border
    float newTableWidth = viewWidth - newPaneWidth - widthOfBorderAroundTable - widthOfBorderAroundTable; // Account for left and right borders
    float newTableHeight = viewHeight - newTableTop;
    CGRect newTableFrame = CGRectMake(newTableLeft, newTableTop, newTableWidth, newTableHeight);
    self.rightTableView.frame = newTableFrame;
    
    self.lblExpensesTitle.frame = CGRectMake(rightTableView.frame.origin.x, lblExpensesTitle.frame.origin.y, lblExpensesTitle.frame.size.width, lblExpensesTitle.frame.size.height);
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidUnload {
    [self setLblExpensesTitle:nil];
    [self setLblDocName:nil];
    [self setLblDocTraveler:nil];
    [self setLblDocType:nil];
    [self setLblDocDate:nil];
    [self setLblStatus:nil];
    [self setLblTANumber:nil];
    [self setLblTrip:nil];
    [self setLblEmissions:nil];
    [self setLblTotalAmount:nil];
    [self setLabelOnButton1:nil];
    [self setLabelOnButton2:nil];
    [self setLabelOnButton6:nil];
    [self setLabelOnButton7:nil];
    [super viewDidUnload];
}

#pragma mark - Base Detail Overriden Methods
- (void) didPressButtonAtIndex:(NSInteger)buttonIndex withId:(NSString*)buttonId inRect:(CGRect)rect
{
    if ([buttonId isEqualToString:BUTTON_ID_ACCT_ALLLOCATION])
        [self buttonAllocationClicked:self];
    if ([buttonId isEqualToString:BUTTON_ID_ADD_EXPENSE])
        [self buttonAddEntryClicked:self withFrame:rect];
    if ([buttonId isEqualToString:BUTTON_ID_AUDITS])
        [self buttonAuditsClicked:self];
    if ([buttonId isEqualToString:BUTTON_ID_COMMENTS])
        [self buttonCommentsClicked:self];
    if ([buttonId isEqualToString:BUTTON_ID_DOC_RECEIPTS])
        [self buttonReceiptsClicked:self];
    if ([buttonId isEqualToString:BUTTON_ID_PDLOCATIONS])
        [self buttonPerdiemLocationClicked:self];
    if ([buttonId isEqualToString:BUTTON_ID_TOTALS])
        [self buttonTotalsClicked:self];
}

- (IBAction) buttonPressed:(id)sender
{
    UIButton *button = (UIButton*)sender;
    NSInteger buttonIndex = button.tag;
    
    if (buttonIndex < _buttonDescriptors.count)
    {
        ButtonDescriptor *descriptor = [_buttonDescriptors objectAtIndex:buttonIndex];
        CGRect buttonRect = [self.view convertRect:button.frame fromView:self.leftPaneView];
        [self didPressButtonAtIndex:buttonIndex withId:descriptor.buttonId inRect:buttonRect];
    }
}

- (UIButton*) buttonAtIndex:(NSInteger)index
{
    if (index == 6)
        return self.button6;
    else if (index == 7)
        return self.button7;
    
    return [super buttonAtIndex:index];
}

- (UILabel*) labelOnButtonAtIndex:(int)index
{
    if (index == 6)
        return self.labelOnButton6;
    else if (index == 7)
        return self.labelOnButton7;
    
    return [super labelOnButtonAtIndex:index];
}

- (void) configureButtons
{
    if (!self.isViewLoaded || _buttonDescriptors == nil)
        return;
    
    for (int buttonIndex = 0; buttonIndex < _buttonDescriptors.count; buttonIndex++)
    {
        ButtonDescriptor *descriptor = [_buttonDescriptors objectAtIndex:buttonIndex];
        
        UIButton *button = [self buttonAtIndex:buttonIndex];
        button.hidden = NO;
        
        UILabel *labelForButton = [self labelOnButtonAtIndex:buttonIndex];
        labelForButton.text = descriptor.title;
    }
    
    for (NSInteger unusedButtonIndex = _buttonDescriptors.count; unusedButtonIndex < MAX_REPORT_BUTTONS; unusedButtonIndex++)
    {
        UIButton *unusedButton = [self buttonAtIndex:unusedButtonIndex];
        unusedButton.hidden = YES;
    }
}

- (void) setButtonDescriptors:(NSArray*)descriptors
{
    // This method can be called any time and the UI will be updated to show the new buttons
    
    // This method makes its own immutable copy of the array, so if you need to change a button, then (1) arrange the buttons in your own array, and (2) pass your array to this method which will copy it.
    
    _buttonDescriptors = [NSArray arrayWithArray:descriptors]; // Make an immutable copy of the array.
    [self configureButtons];
}

#pragma mark - Button Methods
- (void)makeButtonLabels
{
    NSMutableArray *descriptors = [NSMutableArray array];
//    MOB-20307 Removed receipt per request.
//    [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_DOC_RECEIPTS title:[Localizer getLocalizedText:@"Receipts"]]];
    if (self.doc.perdiemTDY != nil && [self.doc.perdiemTDY count]>0)
    {
        [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_PDLOCATIONS title:[NSString stringWithFormat:@"%@ (%lu)", [Localizer getLocalizedText:@"Per Diem Locations"], (unsigned long)[self.doc.perdiemTDY count]]]];
    }
    [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_ADD_EXPENSE title:[Localizer getLocalizedText:@"Add Expense"]]];
    [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_ACCT_ALLLOCATION title:[Localizer getLocalizedText:@"Accounting Allocation"]]];
    [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_TOTALS title:[Localizer getLocalizedText:@"Totals and Travel Advances"]]];
    [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_AUDITS title:[NSString stringWithFormat:@"%@ %@ (%d) %@ (%d)", [Localizer getLocalizedText:@"Audits"], [@"Pass" localize], [self.doc.auditPassed intValue], [@"Fail" localize], [self.doc.auditFailed intValue]]]];
    [descriptors addObject:[ButtonDescriptor buttonDescriptorWithId:BUTTON_ID_COMMENTS title:[Localizer getLocalizedText:@"Comments"]]];
    
    [self setButtonDescriptors:descriptors];
}

#pragma mark - Bar Methods
-(void) configureBars
{
    self.navigationController.navigationBar.hidden = NO;
    self.navigationController.toolbarHidden = NO;
    
    self.navigationItem.title = self.doc.docName;

    //TODO: use filter in document list
    
    // Nav bar
//    GoviPadHome9VC *homeVC = (GoviPadHome9VC*)[ConcurMobileAppDelegate findHomeVC];
//    UIBarButtonItem *btnReportsList = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Documents List"] style:UIBarButtonItemStyleBordered target:homeVC action:@selector(showGovDocumentListView:)];
//    self.navigationItem.rightBarButtonItem = btnReportsList;
    
    // Toolbar
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    NSMutableArray *toolbarItems = nil;
    if (self.doc != nil && !stampSuccess && (self.doc.needsStamping != nil && [self.doc.needsStamping boolValue]==TRUE))
    {
        UIBarButtonItem *btnStampDoc = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Stamp"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionStamp:)];
        self.btnStamp = (UIButton*) btnStampDoc;
        toolbarItems = [NSMutableArray arrayWithObjects: flexibleSpace, flexibleSpace, btnStampDoc, nil];
    }
    
    [self setToolbarItems:toolbarItems];
}

-(void)closeView:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void) updateViews
{
    [self drawLeftPanel];
    [self makeButtonLabels];
    [self configureBars];
    [self.rightTableView reloadData];
}

-(void)respondToFoundData:(Msg *)msg
{
    [super respondToFoundData:msg];
    if ([msg.idKey isEqualToString:GOV_DOCUMENT_DETAIL])
    {
        if ([self isViewLoaded]) {
            [self hideLoadingView];
            [self hideWaitView];
        }
        
        if (msg.errBody != nil)
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Unable to add expense"]
                                  message:msg.errBody
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            
            [alert show];
        }
        else
        {
            GovDocumentDetailData * resp = (GovDocumentDetailData*)msg.responder;
            self.doc = resp.currentDoc;
            if ([self.doc.receiptId lengthIgnoreWhitespace])
            {
                self.receipt = [[Receipt alloc] init];
                self.receipt.receiptId = self.doc.receiptId;
                self.receipt.dataType = @"pdf";
            }
            
//            [self initSections];
            [self updateViews];
            
            NSDictionary *param = @{@"Type": doc.docType, @"Audit Count": [NSNumber numberWithFloat:[doc.auditFailed floatValue] + [doc.auditPassed floatValue]]};
            [Flurry logEvent:@"Document Detail: View" withParameters:param];
            
            int count = 0;
            for (GovDocExpense *expense in self.doc.expenses)
            {
                if ( [expense.imageId length] )
                    count++;
            }
            param = @{@"Count" : [NSNumber numberWithInteger:[doc.expenses count]], @"Count with receipt" : [NSNumber numberWithInt:count]};
            [Flurry logEvent:@"Document Detail: Expenses" withParameters:param];
        }
    }
    else if ([msg.idKey isEqualToString:GOV_ATTACH_RECEIPT])
    {
        GovAttachReceiptData* data = (GovAttachReceiptData*) msg.responder;
        if (msg.errBody != nil && ![data.status.status isEqualToString:@"SUCCESS"])
        {
            NSString * errMsg = msg.errBody == nil? data.status.errMsg : msg.errBody;
            
            [self hideWaitView];
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Unable to attach receipt to document"]
                                  message:errMsg
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            
            [alert show];
        }
        else
        {
            if (self.doc.receiptId == nil)
            {
                // Refresh doc details with new receipt id and audits
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                             self.doc.travelerId, @"TRAVELER_ID",
                                             self.doc.docType, @"DOC_TYPE",
                                             self.doc.docName, @"DOC_NAME",
                                             nil];
                [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
            }
            else
            {
                // Need to refetch the document level receipt
                [self hideWaitView];
                [[ReceiptCache sharedInstance] deleteReceiptsMatchingId:self.doc.receiptId];
            }
        }
    }

}

- (void) drawLeftPanel
{
    GovDocumentDetail * thisDoc = (GovDocumentDetail*) self.doc;
    
    self.lblDocTraveler.text = thisDoc.travelerName;
    
    NSString *totalAmountValue = [FormatUtils formatMoneyWithNumber:thisDoc.totalEstCost crnCode:@"USD"];
    if ([ExSystem is6Plus])
    {
        self.lblTotalAmount.text = totalAmountValue;
    }
    else
    {
        self.lblTotalAmount.text = [NSString stringWithFormat:@"%@: %@",[Localizer getLocalizedText:@"Total Amount"], totalAmountValue];
    }
    
    self.lblDocName.text = thisDoc.docName;
    self.lblDocType.text = thisDoc.docTypeLabel;
    NSString *startFormatted = [DateTimeFormatter formatDate:thisDoc.tripBeginDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    NSString *endFormatted = [DateTimeFormatter formatDate:thisDoc.tripEndDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    if(!(startFormatted == nil || endFormatted == nil))
        self.lblDocDate.text = [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];
    else
        self.lblDocDate.text = @"";
    
	if (true/*[self isAuth]*/)
	{
        self.lblStatus.text = thisDoc.currentStatus;
        self.lblTANumber.text = thisDoc.tANumber;
        self.lblTrip.text = thisDoc.purposeCode;
        self.lblEmissions.text = [NSString stringWithFormat:@"%d",[thisDoc.emissionsLbs intValue]];
	}
}

- (NSAttributedString*)attributedStringWithAmountForLabel:(UILabel*)label labelText:(NSString*)labelText valueText:(NSString*)valueText fontName:(NSString*)fontName fontSize:(float)fontSize
{
    if (label.attributedText == nil)
        return nil;
    
    // Get the attributes from the existing text inside the UILabel
    NSDictionary *labelAttributes = [label.attributedText attributesAtIndex:0 effectiveRange:nil];
    
    // Create a new label with the same attributes
    NSMutableAttributedString *labelString = [[NSMutableAttributedString alloc] initWithString:labelText attributes:labelAttributes];
    
    if (valueText != nil && valueText.length > 0)
    {
        // Create a new dictionary of attributes that is identical to the label attributes except for the font
        NSMutableDictionary *valueAttributes = [NSMutableDictionary dictionaryWithDictionary:labelAttributes];
        [valueAttributes setObject:[UIFont fontWithName:fontName size:fontSize] forKey:NSFontAttributeName];
        
        // Create an attributed string for the value using the new attributes
        NSAttributedString *value = [[NSAttributedString alloc] initWithString:valueText attributes:valueAttributes];
        
        // Put the label and value together
        [labelString appendAttributedString:value];
    }
    
    return labelString;
}

#pragma mark -
#pragma mark Table View Data Source Methods
//
// In order to get a gap to appear betwen rows, there will be one section per expense and each section will have only 1 row.  The gap is actually the section header with the same background color as the view behind the table.
//
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return (self.doc.expenses == nil ? 0 : self.doc.expenses.count);
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
//  MOB-14286 duplicated expense in expense table
    NSUInteger rowFromSection = [indexPath section];
    
    SummaryCellMLines *cell = (SummaryCellMLines *)[tableView dequeueReusableCellWithIdentifier: @"SummaryCell4Lines"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell4Lines" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[SummaryCellMLines class]])
                cell = (SummaryCellMLines *)oneObject;
    }
    
    GovDocExpense *expense = [self.doc.expenses objectAtIndex:rowFromSection];
    NSString* amountStr = [FormatUtils formatMoneyWithNumber:expense.amount crnCode:@"USD"];
    NSString* dateStr = [DateTimeFormatter formatDate:expense.expDate Format:@"MMM dd, yyyy" TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    [cell resetCellContent:expense.expenseDesc withAmount:[@"Amount" localize] withLine1: expense.expenseCategory withLine2: dateStr withImage1:nil withImage2:nil withImage3:nil];
    
    cell.lblPmtType.text = [@"Payment Type" localize];
    cell.lblRLine1.text = expense.paymentMethod;
    cell.lblAmtValue.text = amountStr;
    
//    cell.frame = CGRectMake(cell.frame.origin.x, cell.frame.origin.y + 10, cell.frame.size.width, cell.frame.size.height);
    return cell;
}

-(CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return GAP_BETWEEN_TABLE_ROWS;
}

-(UIView*)tableView:(UITableView*)tableView viewForHeaderInSection:(NSInteger)section
{
    UIView* tableHeaderView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, tableView.frame.size.width, GAP_BETWEEN_TABLE_ROWS)];
    tableHeaderView.backgroundColor = tableView.superview.backgroundColor;
    return tableHeaderView;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 80.0;
}

-(UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(editingStyle == UITableViewCellEditingStyleDelete)
    {
        [self showWaitViewWithText:[Localizer getLocalizedText:@"Deleting Expense"]];
        
        GovDocExpense *expense = [self.doc.expenses objectAtIndex:indexPath.row];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:expense.expId, @"EXPENSE_ID", self.doc.docName, @"DOC_NAME", self.doc.docType, @"DOC_TYPE", nil];
        [[ExSystem sharedInstance].msgControl createMsg:GOV_DELETE_EXP_FROM_DOC CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

#pragma mark -
#pragma mark Table View Delegate Methods
#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    
    GovDocExpense *expense = [self.doc.expenses objectAtIndex:indexPath.row];
    [self buttonEntryDetailClicked:expense];
}

-(void) buttonEntryDetailClicked:(GovDocExpense*)expense
{
    GovDocExpenseDetailVC *detailVC = [[GovDocExpenseDetailVC alloc] initWithNibName:@"GovDocExpenseDetailVC" bundle:nil];
    [detailVC setSeedDataWithExpense:expense docType:self.doc.docType docName:self.doc.docName];
    
    // MOB-8533 allow dismiss keyboard using modal form sheet
    UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:detailVC];
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [localNavigationController setToolbarHidden:NO];
    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    if ([UIDevice isPad])
    {
        UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStylePlain target:self action:@selector(closeView:)];
        detailVC.navigationItem.leftBarButtonItem = nil;
        detailVC.navigationItem.leftBarButtonItem = btnCancel;
    }
    [self presentViewController:localNavigationController animated:YES completion:nil];
}

-(void) buttonAddEntryClicked:(id)sender withFrame:(CGRect)rect
{
    if(self.actionPopOver != nil)
        return;
    
//    self.actionPopOver = [[MobileActionSheet alloc] initWithTitle:nil
//                                                            delegate:self
//                                                   cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
//                                              destructiveButtonTitle:nil
//                                                   otherButtonTitles:[Localizer getLocalizedText:@"Create New"],
//                             [Localizer getLocalizedText:@"Select from Existing"], nil];

    self.actionPopOver = [[MobileActionSheet alloc] initWithTitle:nil
                                                         delegate:self
                                                cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                                           destructiveButtonTitle:nil
                                                otherButtonTitles:[Localizer getLocalizedText:@"Select from Existing"], nil];
    
	[actionPopOver showFromRect:rect inView:self.view animated:YES];
}


-(void) buttonAuditsClicked:(id)sender
{
    [GovDocExceptionsVC showDocExceptions:self withDoc:self.doc];
    
    NSDictionary *param = @{@"Drill-in Action" : @"Audits"};
    [Flurry logEvent:@"Document Detail: Drill-in Action" withParameters:param];
}

-(void) buttonAllocationClicked:(id)sender
{
    [GovDocAcctVC showDocAccts:self withDoc:self.doc];
    
    NSDictionary *param = @{@"Drill-in Action" : @"Accounting Allocation"};
    [Flurry logEvent:@"Document Detail: Drill-in Action" withParameters:param];
}

-(void) buttonTotalsClicked:(id)sender
{
    [GovDocTotalsVC showDocTotals:self withDoc:self.doc];
    
    NSDictionary *param = @{@"Drill-in Action" : @"Totals and Travel Advances"};
    [Flurry logEvent:@"Document Detail: Drill-in Action" withParameters:param];
}

-(void) buttonCommentsClicked:(id)sender
{
    [GovDocCommentVC showDocComment:self withComment:self.doc.comments];
    
    NSDictionary *param = @{@"Drill-in Action" : @"Comments"};
    [Flurry logEvent:@"Document Detail: Drill-in Action" withParameters:param];
}

-(void) buttonPerdiemLocationClicked:(id)sender
{
    [GovDocPerDiemVC showDocPerDiem:self withDoc:self.doc];
    
    NSDictionary *param = @{@"Drill-in Action" : @"Per Diem Locations"};
    [Flurry logEvent:@"Document Detail: Drill-in Action" withParameters:param];
}

-(void) buttonReceiptsClicked:(id)sender
{
    ReceiptEditorVC *receiptView = [[ReceiptEditorVC alloc] initWithNibName:@"ReceiptEditorVC" bundle:nil];
    receiptView.title = [Localizer getLocalizedText:@"Receipt"];
    receiptView.delegate = self;
    receiptView.canDelete = NO;
    receiptView.canUpdate = YES;
    [receiptView setSeedData:self.receipt]; // Pass in receipt object to fill the downloaded image data.

    UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:receiptView];
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [localNavigationController setToolbarHidden:NO];
    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    
    [self presentViewController:localNavigationController animated:YES completion:nil];

    NSDictionary *param = @{@"Drill-in Action" : @"Receipt"};
    [Flurry logEvent:@"Document Detail: Drill-in Action" withParameters:param];
}

-(IBAction) actionStamp:(id)sender
{
    if ([ExSystem connectedToNetwork])
    {
        GovDocStampVC *c = [[GovDocStampVC alloc] initWithNibName:@"GovDocStampVC" bundle:nil];
        
        [c setSeedData:self.doc];
        [c setSeedDelegate:self];
        UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:c];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
        localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
        
        [self presentViewController:localNavigationController animated:YES completion:nil];
    }
    else
    {
        UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle:[Localizer getLocalizedText:@"Offline"]
							  message:[Localizer getLocalizedText:@"Actions offline"]
							  delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"Close"] otherButtonTitles:nil];
		[alert show];
    }
}

-(void)btnSelectFromExistingPressed
{
    GovUnappliedExpensesVC *vc = [[GovUnappliedExpensesVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    vc.isAddingMode = YES;
    vc.docName = self.doc.docName;
    vc.docType = self.doc.docType;
    vc.travID = self.doc.travelerId;
    
    [vc setSeedDelegate:self];
    
    NSString* msgId = GOV_UNAPPLIED_EXPENSES;
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:vc];
    
    UINavigationController *localNavigationController = [[KeyboardNavigationController alloc] initWithRootViewController:vc];
    localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
    [localNavigationController setToolbarHidden:NO];
    localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
    localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
    
    [self presentViewController:localNavigationController animated:YES completion:nil];
}

#pragma mark ReceiptEditorDelegate
-(void) receiptUpdated:(Receipt*) rcpt useV2Endpoint:(BOOL)useV2Endpoint
{
    if (rcpt != nil && rcpt.receiptId != nil)
    {
        // MOB-12200 creating new receipt object and only use receiptID.
        // This fix "rcpt", "self.receipt" share same memory address. Cause new rcpt.receiptId getting override with old one.
        NSString *newReceiptId = rcpt.receiptId;
        if (![rcpt.receiptId isEqualToString:self.doc.receiptId] && self.doc.receiptId != nil)
        {
            // Reset - need to refresh the document level receipt.
            self.receipt.receiptId = self.doc.receiptId;
            self.receipt.pdfData = nil;
            self.receipt.receiptImg = nil;
        }
        // send out receipt attaching message
        [self showWaitView];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     newReceiptId, @"RECEIPT_ID",
                                     self.doc.docName, @"DOC_NAME",
                                     self.doc.docType, @"DOC_TYPE",
                                     nil];
        [[ExSystem sharedInstance].msgControl createMsg:GOV_ATTACH_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        
    }
    // How to handle wait view, etc...
    //    //    self.isDirty = true;  // TODO - use a different flag for receipt dirty, or new receipt vs orig receipt?
}

-(void) receiptDeleted:(Receipt*) receipt{}
-(void) receiptPicked:(Receipt*) receipt{}  // For offline?

#pragma UIActionSheetDelegate
-(void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
//    if (buttonIndex == 0)       //create new
//    {
//        [self buttonAddExpensePressed];
//    }
    if (buttonIndex == 0)  //select from existing
    {
        [self btnSelectFromExistingPressed];
    }
    else
        [super actionSheet:actionSheet clickedButtonAtIndex:buttonIndex];
    
    self.actionPopOver = nil;
}

#pragma mark GovUnappliedExpensesDelegate
-(void) didAttachSelectedExpense
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                 doc.travelerId, @"TRAVELER_ID",
                                 doc.docType, @"DOC_TYPE",
                                 doc.docName, @"DOC_NAME",
                                 nil];
    [[ExSystem sharedInstance].msgControl createMsg:GOV_DOCUMENT_DETAIL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    [self dismissViewControllerAnimated:YES completion:nil];
}

//#pragma mark GovDocStampVCDelegate
-(void) didStampSelectedDocument
{
    [self dismissViewControllerAnimated:YES completion:nil];
    self.stampSuccess = YES;
    if(btnStamp != nil)
    {
        NSMutableArray *toolBarItems = [NSMutableArray arrayWithArray:self.toolbarItems];
        // Last item should always be "Stamp" button
        [toolBarItems removeLastObject];
        self.btnStamp = nil;
        //Search for "Stamp" button
//        for (UIBarButtonItem *item in toolBarItems)
//        {
//            if ([item.title isEqualToString:[Localizer getLocalizedText:@"Stamp"]])
//            {
//                [toolBarItems removeObject:item];
//                self.btnStamp = nil;
//            }
//        }
        [self setToolbarItems:[[NSArray alloc] initWithArray:toolBarItems] animated:YES];
    }
}

// Support empty travId.
+(void)showDocDetailWithTraveler:(NSString*)travId withDocName:(NSString*) docName withDocType:(NSString*) docType
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
								 docName, @"DOC_NAME",
								 docType, @"DOC_TYPE",
                                 travId, @"TRAVELER_ID",
								 nil];
    
    UIViewController *padHome = [ConcurMobileAppDelegate findHomeVC];
    GovDocDetailVC_iPad *newDetailViewController = [[GovDocDetailVC_iPad alloc] initWithNibName:@"GovDocDetailVC_iPad" bundle:nil];
    [newDetailViewController setSeedData:pBag];
    [padHome.navigationController pushViewController:newDetailViewController animated:YES];
}
@end
