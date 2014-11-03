//
//  GovDocTotalsVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocTotalsVC.h"
#import "GovDocExpensesVC.h"
#import "GovDocAcctCell.h"
#import "FormatUtils.h"

@interface GovDocTotalsVC ()

@end

@implementation GovDocTotalsVC
@synthesize lblName, lblAmount, lblDocName, lblDates, img1, img2;
@synthesize tableList;
@synthesize doc;

-(void) actionClose:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

-(void) setupToolbar
{
    if([UIDevice isPad])
    {
        //self.contentSizeForViewInPopover = CGSizeMake(320.0, 360.0);
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(actionClose:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }
}


- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.title = [Localizer getLocalizedText:@"Totals and Travel Advances"];
    [GovDocExpensesVC drawHeader:self withDoc:self.doc];
    [tableList reloadData];
    [self setupToolbar];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if ([doc.gtmDocType isEqualToString:@"VCH"])
    {
        return 5;
    }
    else if ([doc.gtmDocType isEqualToString:@"AUTH"])
    {
        return 3;
    }
    
    return 0;
}

- (UITableViewCell *) tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    GovDocAcctCell *cell = (GovDocAcctCell *)[tableView dequeueReusableCellWithIdentifier: @"GovDocAcctCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"GovDocAcctCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[GovDocAcctCell class]])
                cell = oneObject;
    }

    if ([doc.gtmDocType isEqualToString:@"VCH"])
    {
        if (row == 0)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.totalEstCost crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Total Expenses"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
        else if (row == 1)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.nonReimbursableAmount crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Non - Reimbursable Expenses"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
        else if (row == 2)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.advApplied crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Advance Applied"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
        else if (row == 3)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.payToChargeCard crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Pay to Charge Card"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
        else if (row == 4)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.payToTraveler crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Pay to Traveler"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
    }
    else if ([doc.gtmDocType isEqualToString:@"AUTH"])
    {
        if (row == 0)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.totalEstCost crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Estimated Cost"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
        else if (row == 1)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.nonReimbursableAmount crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Non - Reimbursable Expenses"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
        else if (row == 2)
        {
            NSString* amountStr = [FormatUtils formatMoneyWithNumber:doc.advAmtRequested crnCode:@"USD"];
            cell.acctName.text = [Localizer getLocalizedText:@"Advance Requested"];
            cell.acctAmount.text = amountStr;
            
            [cell.acctAmount setFont:[UIFont boldSystemFontOfSize:17]];
        }
    }

    return cell;
}

#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}

#pragma show view
+(void)showDocTotals:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail
{
    GovDocTotalsVC *c = [[GovDocTotalsVC alloc] initWithNibName:@"GovDocHeaderShort" bundle:nil];
    c.doc = docDetail;
    
    if ([UIDevice isPad])
    {
        UINavigationController *localNavigationController = [[UINavigationController alloc] initWithRootViewController:c];
        localNavigationController.modalPresentationStyle = UIModalPresentationFormSheet;
        [localNavigationController setToolbarHidden:NO];
        localNavigationController.toolbar.tintColor = [UIColor navBarTintColor_iPad];
        localNavigationController.navigationBar.tintColor = [UIColor navBarTintColor_iPad];
        
        [pvc presentViewController:localNavigationController animated:YES completion:nil];
    }
    else
        [pvc.navigationController pushViewController:c animated:YES];
}
@end
