//
//  OfferMultiLinkVC.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/3/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "OfferMultiLinkVC.h"
#import "OfferCell.h"
#import "MultiWebLinkData.h"
#import "OfferWebVC.h"

@implementation OfferMultiLinkVC
@synthesize tableList;
@synthesize links;
@synthesize icon;


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    if([UIDevice isPad])
    {
        self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
        self.navigationItem.leftBarButtonItem =  [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(doneAction)];
        self.contentSizeForViewInPopover = CGSizeMake(320.0, 400.0);
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if ([UIDevice isPad]) {
        return YES;
    }
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [links count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"OfferCell";
    MultiWebLinkData *linkData = (MultiWebLinkData*)links[[indexPath row]];
    
    OfferCell *cell = nil;
    cell = (OfferCell *)[tableList dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"OfferCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[OfferCell class]])
                cell = (OfferCell *)oneObject;
    }
    
    // Configure the cell...
    [cell configureLabelFontForLabel:cell.lblTitle WithText:linkData.title];
	cell.ivIcon.image = icon;
    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark actions
-(void)doneAction
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    MultiWebLinkData *linkData = (MultiWebLinkData*)links[[indexPath row]];
    
    OfferWebVC *webVC = [[OfferWebVC alloc] initWithNibName:@"OfferWebVC" bundle:nil];
    webVC.url = linkData.actionURL;
    [self.navigationController pushViewController:webVC animated:YES];
    webVC.title = self.title;
}

@end
