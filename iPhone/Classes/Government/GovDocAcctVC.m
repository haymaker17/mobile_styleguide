//
//  GovDocAcctVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocAcctVC.h"
#import "GovDocExpensesVC.h"
#import "GovDocAcctCell.h"
#import "GovDocAccountCode.h"

#import "FormatUtils.h"

@interface GovDocAcctVC ()

@end

@implementation GovDocAcctVC
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
    self.title = [Localizer getLocalizedText:@"Accounting Allocation"];
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
    return [self.doc.accountCodes count];
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
    
    GovDocAccountCode *account = [self.doc.accountCodes objectAtIndex:row];
    if (account != nil)
    {
        NSString* amountStr = [FormatUtils formatMoneyWithNumber:account.amount crnCode:@"USD"];
        
        cell.acctName.text = account.account;
        cell.acctAmount.text = amountStr;
    }
    return cell;
}

#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}

#pragma show view
+(void)showDocAccts:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail
{
 	GovDocAcctVC *c = [[GovDocAcctVC alloc] initWithNibName:@"GovDocHeaderShort" bundle:nil];
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
