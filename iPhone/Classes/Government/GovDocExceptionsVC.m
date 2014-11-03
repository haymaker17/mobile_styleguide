//
//  GovDocExceptionsVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 12/28/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovDocExceptionsVC.h"
#import "GovDocException.h"
#import "GovDocExpensesVC.h"
#import "GovDocExceptionAlertCell.h"
#import "TextEditVC.h"

@interface GovDocExceptionsVC ()

@end

@implementation GovDocExceptionsVC
@synthesize doc;

-(void) actionSave:(id)sender
{
    // Send save request
    DLog(@"Send save request");
}

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
    else
    {
        UIBarButtonItem *btnSave = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave target:self action:@selector(actionSave:)];
        [btnSave setEnabled:NO];
        self.navigationItem.rightBarButtonItem = btnSave;
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.title = [Localizer getLocalizedText:@"Doc Audits"];
    [GovDocExpensesVC drawHeader:self withDoc:self.doc];
    [self.tableList reloadData];
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
    return [self.doc.exceptions count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    GovDocExceptionAlertCell *cell = (GovDocExceptionAlertCell *)[tableView dequeueReusableCellWithIdentifier:@"GovDocExceptionAlertCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"GovDocExceptionAlertCell" owner:self options:nil];
        for ( id oneObject in nib)
        {
            if ([oneObject isKindOfClass:[GovDocExceptionAlertCell class]])
                cell = oneObject;
        }
    }

    GovDocException *exception = [self.doc.exceptions objectAtIndex:row];
    if (exception != nil)
    {
        NSString *exceptionName = exception.name;
        NSString *errorStatus = exception.errorStatus;
        NSString *comment = exception.comments;
        
        // set cell content
        cell.exceptionName.text = exceptionName;
        cell.alertName.text = comment;
        cell.passOrFail.text = errorStatus;
        
        if ([errorStatus isEqualToString:@"PASS"])
        {
            CGRect frame = cell.exceptionName.frame;
            cell.exceptionName.frame = CGRectMake(frame.origin.x, cell.passOrFail.frame.origin.y, frame.size.width, frame.size.height);
            [cell.alertName setHidden:YES];
            [cell.alertImg setHidden:YES];
        }
        else
        {
            [cell.alertImg setHidden:NO];
            [cell.alertImg setHidden:NO];
        }
    }
    return cell;
}

#pragma mark - Table view delegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    GovDocException *exception = [self.doc.exceptions objectAtIndex:row];

    TextEditVC *vc = [[TextEditVC alloc] initWithNibName:@"TextEditVC" bundle:nil];
    [vc setSeedData:@"" context:@""
           delegate:self
                tip:[Localizer getLocalizedText: @"Enter Justification" ]
              title:exception.name
             prompt:nil isNumeric:NO isPassword:NO err:nil];
    [self.navigationController pushViewController:vc animated:YES];

    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}

#pragma mark - show view
+(void)showDocExceptions:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail
{
    GovDocExceptionsVC *c = [[GovDocExceptionsVC alloc]initWithNibName:@"GovDocHeaderShort" bundle:nil];
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

#pragma mark - TextEditDelegate
- (void)textUpdated:(NSObject *)context withValue:(NSString *)value
{
    if (![NSString isEmpty:value]) {
        [self.navigationItem.rightBarButtonItem setEnabled:YES];
    }
}

@end
