//
//  GovDocPerDiemVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovDocPerDiemVC.h"
#import "GovDocPerDiemCell.h"
#import "GovDocPerdiemTDY.h"
#import "DateTimeFormatter.h"
#import "GovDocExpensesVC.h"

@interface GovDocPerDiemVC ()

@end

@implementation GovDocPerDiemVC
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
    self.title = [Localizer getLocalizedText:@"Per Diem Locations"];
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
    return [self.doc.perdiemTDY count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    
    GovDocPerDiemCell *cell = [tableView dequeueReusableCellWithIdentifier: @"GovDocPerDiemCell"];
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"GovDocPerDiemCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[GovDocPerDiemCell class]])
                cell = oneObject;
    }
    
    GovDocPerdiemTDY *perDiemTDY = [self.doc.perdiemTDY objectAtIndex:row];
    // parsed data has unnecessary white characters for user display
    NSString *perDiemRate = [perDiemTDY.rate stringByReplacingOccurrencesOfString:@".00" withString:@""];
    NSString *trimmedRate = [perDiemRate stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    NSString *noWhiteSpacesRate = [trimmedRate stringByReplacingOccurrencesOfString:@"    " withString:@""];
    NSString *beginDateStr = [DateTimeFormatter formatDate:perDiemTDY.beginTdy Format:@"MMM dd, yyyy" TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    NSString *endDateStr = [DateTimeFormatter formatDate:perDiemTDY.endTdy Format:@"MMM dd, yyyy" TimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    
    cell.location.text = perDiemTDY.perdiemLocation;
    cell.tripDate.text = [NSString stringWithFormat:@"%@ - %@", beginDateStr,endDateStr];
    cell.rate.text = noWhiteSpacesRate;

    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 93;
}
#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}

#pragma show view
+(void)showDocPerDiem:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail
{
    GovDocPerDiemVC *c = [[GovDocPerDiemVC alloc] initWithNibName:@"GovDocHeaderShort" bundle:nil];
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
