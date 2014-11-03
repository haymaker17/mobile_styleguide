//
//  GovDocExpensesVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocExpensesVC.h"
#import "GovDocExpenseCatInfo.h"
#import "SummaryCellMLines.h"
#import "GovDocExpense.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"

#import "MobileActionSheet.h"
#import "LabelConstants.h"
#import "GovUnappliedExpensesVC.h"
#import "GovDeleteExpFromDocData.h"
#import "GovExpenseEditViewController.h"
#import "GovDocExpenseDetailVC.h"

@implementation GovDocExpensesVC
@synthesize lblName, lblAmount, lblDocName, lblDates, img1, img2;
@synthesize tableList;
@synthesize doc, addExpenseAction;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.title = [Localizer getLocalizedText:@"Expenses"];
    [GovDocExpensesVC drawHeader:self withDoc:self.doc];
    [self.tableList reloadData];
    
    [self setUpBarItems];
}

-(void)viewWillDisappear:(BOOL)animated
{
    if (addExpenseAction != nil)
        [addExpenseAction dismissWithClickedButtonIndex:addExpenseAction.cancelButtonIndex animated:YES];
    
    [super viewWillDisappear:animated];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

+(void)drawHeader:(UIViewController< GovDocHeaderShortProtocol>*)pvc withDoc:(GovDocumentDetail *) document
{
    GovDocumentDetail * thisDoc = document;
    
    pvc.lblName.text = thisDoc.travelerName;
    //MOB-6353 - Show claimed amount
    pvc.lblAmount.text = [FormatUtils formatMoneyWithNumber:thisDoc.totalEstCost crnCode:@"USD"];
    pvc.lblDocName.text = thisDoc.docName;
    NSString *startFormatted = [DateTimeFormatter formatDate:thisDoc.tripBeginDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    NSString *endFormatted = [DateTimeFormatter formatDate:thisDoc.tripEndDate Format:@"MMM dd, yyyy"  TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    if (!(startFormatted == nil || endFormatted == nil))
        pvc.lblDates.text = [NSString stringWithFormat:@"%@ - %@", startFormatted, endFormatted];
    else
        pvc.lblDates.text = @"";
    
    [UtilityMethods drawNameAmountLabelsOrientationAdjustedWithResize:pvc.lblName AmountLabel:pvc.lblAmount LeftOffset:10 RightOffset:10 Width:[[UIScreen mainScreen] bounds].size.width];
    
    pvc.lblStatus.text = thisDoc.currentStatus;
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.doc.expenses count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];

    SummaryCellMLines *cell = (SummaryCellMLines *)[tableView dequeueReusableCellWithIdentifier: @"SummaryCell4Lines"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SummaryCell4Lines" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[SummaryCellMLines class]])
                cell = (SummaryCellMLines *)oneObject;
    }
    
    GovDocExpense *expense = [self.doc.expenses objectAtIndex:row];
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

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 64;
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

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];

    GovDocExpense *expense = [self.doc.expenses objectAtIndex:indexPath.row];

    GovDocExpenseDetailVC *detailVC = [[GovDocExpenseDetailVC alloc] initWithNibName:@"GovDocExpenseDetailVC" bundle:nil];
    [detailVC setSeedDataWithExpense:expense docType:self.doc.docType docName:self.doc.docName];
    [self.navigationController pushViewController:detailVC animated:YES];
}


#pragma show view
+(void)showDocExpenses:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail
{
 	GovDocExpensesVC *govDocExpensesVC = [[GovDocExpensesVC alloc] initWithNibName:@"GovDocHeaderShort" bundle:nil];
    govDocExpensesVC.doc = docDetail;
    
    if ([UIDevice isPad])
    {
        UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:govDocExpensesVC];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];
        
        localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
        localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
        [pvc presentViewController:localNavigationController animated:YES completion:nil];
    }
    else
        [pvc.navigationController pushViewController:govDocExpensesVC animated:YES];
}

#pragma Init data
-(void)setUpBarItems
{
    if (![ExSystem connectedToNetwork])
        [self makeOfflineBar];
    else
    {
        if([UIDevice isPad])
        {
            self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeView:)];
        }
        
        UIBarButtonItem *btnAdd = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(buttonAddPressed:)];
        [self.navigationItem setRightBarButtonItem:btnAdd];
    }
}

#pragma close button
-(void)closeView:(id)sender
{
	if([UIDevice isPad])
	{
		[self dismissViewControllerAnimated:YES completion:nil];
	}
}

#pragma add Entry Method
-(void)buttonAddExpensePressed
{
    
}
-(void)btnSelectFromExistingPressed
{
    GovUnappliedExpensesVC *vc = [[GovUnappliedExpensesVC alloc] initWithNibName:@"MobileTableViewController" bundle:nil];
    vc.isAddingMode = YES;
    vc.docName = self.doc.docName;
    vc.docType = self.doc.docType;
    vc.travID = self.doc.travelerId;
    
    NSString* msgId = GOV_UNAPPLIED_EXPENSES;
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    [[ExSystem sharedInstance].msgControl createMsg:msgId CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:vc];
    
    [self.navigationController pushViewController:vc animated:YES];
}

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
    
    self.addExpenseAction = nil;
}

#pragma mark - add expenseToVoucher
-(IBAction)buttonAddPressed:(id) sender
{
    
    if(self.addExpenseAction != nil)
        return;
    
//    self.addExpenseAction = [[MobileActionSheet alloc] initWithTitle:nil
//                                                                       delegate:self
//                                                              cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
//                                                         destructiveButtonTitle:nil
//                                                              otherButtonTitles:[Localizer getLocalizedText:@"Create New"],
//                                        [Localizer getLocalizedText:@"Select from Existing"], nil];
 
    self.addExpenseAction = [[MobileActionSheet alloc] initWithTitle:nil
                                                            delegate:self
                                                   cancelButtonTitle:[Localizer getLocalizedText:LABEL_CANCEL_BTN]
                                              destructiveButtonTitle:nil
                                                   otherButtonTitles:[Localizer getLocalizedText:@"Select from Existing"], nil];
    
    if ([UIDevice isPad])
    {
        [addExpenseAction showFromBarButtonItem:self.navigationItem.rightBarButtonItem animated:YES];
    }
    else
        [addExpenseAction showInView:self.tableList];
}

-(void) navigateBack:(id)sender
{
    if (addExpenseAction != nil)
        [addExpenseAction dismissWithClickedButtonIndex:addExpenseAction.cancelButtonIndex animated:YES];
    
    [self.navigationController popViewControllerAnimated:NO];
}

#pragma mark - Message methods
-(void) respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:GOV_DELETE_EXP_FROM_DOC])
    {
        [self hideWaitView];
        
        GovDeleteExpFromDocData* data = (GovDeleteExpFromDocData*) msg.responder;
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
        else
        {
            // Find the expense that was just deleted and remove it from the array, then reload the table data.
            int expenseCount = self.doc.expenses.count;
            for (int expenseIndex = 0; expenseIndex < expenseCount; expenseIndex++)
            {
                GovDocExpense *expense = [self.doc.expenses objectAtIndex:expenseIndex];
                if ([expense.expId isEqualToString:data.expenseId])
                {
                    [self.doc.expenses removeObjectAtIndex:expenseIndex];
                    [self.tableList reloadData];
                    break;
                }
            }
        }
    }
}

@end
