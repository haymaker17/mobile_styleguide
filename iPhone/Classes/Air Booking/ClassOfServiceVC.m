//
//  ClassOfServiceVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "ClassOfServiceVC.h"

@implementation ClassOfServiceVC
@synthesize tableList, aClass, dictClass, selectedRowIndex, bcd; 

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

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

    self.title = [Localizer getLocalizedText:@"Class of Service"];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.tableList = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return [aClass count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
	
	NSInteger row = [indexPath row];
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
	
//	const float defaultFontSize = 14.0f;
//	CGFloat fontSize = (preferredFontSize > 0 ? preferredFontSize : defaultFontSize);
 	cell.textLabel.font = [cell.textLabel.font fontWithSize:14];
    
	cell.textLabel.numberOfLines = 2;
	NSString *key = aClass[row];
    cell.textLabel.text = (NSString*)dictClass[key];
	
	if (row == selectedRowIndex)
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
	else
		cell.accessoryType = UITableViewCellAccessoryNone;	// This is necessary because the cell may have been dequeued with a checkmark
    
    return cell;
}


#pragma mark -
#pragma mark Table view delegate
-(CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 44;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{	
	NSUInteger	row = indexPath.row;
	//NSUInteger	section = indexPath.section;
    bcd.val2 = aClass[row];
    bcd.val = dictClass[bcd.val2];
	[self.navigationController popViewControllerAnimated:YES];
	
}

-(void)scrollToSelectedRow
{
	if (selectedRowIndex < 0)
		return;
	
	NSUInteger path[2] = {0, selectedRowIndex};
	NSIndexPath *indexPath = [[NSIndexPath alloc] initWithIndexes:path length:2];
	[tableList scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
}


-(void)findSelected:(NSString *)key
{
    for(int i = 0; i < [aClass count]; i++)
    {
        if([aClass[i] isEqualToString:key])
        {
            self.selectedRowIndex = i;
            return;
        }
    }
}
@end
