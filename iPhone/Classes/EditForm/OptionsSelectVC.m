//
//  OptionsSelectVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "OptionsSelectVC.h"

@implementation OptionsSelectVC

@synthesize delegate = _delegate;
@synthesize tableList;
@synthesize optionTitle;
@synthesize labels;
@synthesize items;  // Full objects represented by strings in labels
@synthesize selectedRowIndex;
@synthesize preferredFontSize;
@synthesize identifier;

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
	self.title = optionTitle;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
//	[self updateTitle];
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload
{
    [super viewDidUnload];
	self.tableList = nil;
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return labels.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    static NSString *CellIdentifier = @"Cell";
	
	NSInteger row = [indexPath row];
    
    __autoreleasing UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
	
	const float defaultFontSize = 14.0f;
	CGFloat fontSize = (preferredFontSize > 0 ? preferredFontSize : defaultFontSize);
 	cell.textLabel.font = [cell.textLabel.font fontWithSize:fontSize];
	
	cell.textLabel.numberOfLines = 2;
	
    cell.textLabel.text = (NSString*)labels[row];
	
	if (row == selectedRowIndex)
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
	else
		cell.accessoryType = UITableViewCellAccessoryNone;	// This is necessary because the cell may have been dequeued with a checkmark
    
    return cell;
}

-(void)scrollToSelectedRow
{
	if (selectedRowIndex < 0)
		return;
	
	NSUInteger path[2] = {0, selectedRowIndex};
	NSIndexPath *indexPath = [[NSIndexPath alloc] initWithIndexes:path length:2];
	[tableList scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSInteger row = [indexPath row];
	
	// Notify the delegate
	if (self.delegate != nil)
    {
        if (items != nil)
            [self.delegate optionSelected:items[row] withIdentifier:identifier];
        else 
            [self.delegate optionSelectedAtIndex:row withIdentifier:identifier];
    }
	[self.navigationController popViewControllerAnimated:YES];	
}


@end
