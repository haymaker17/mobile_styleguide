    //
//  ModalOptionsViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ModalOptionsViewController.h"
#import "ExSystem.h" 


@implementation ModalOptionsViewController

@synthesize delegate = _delegate;
@synthesize tblView;
@synthesize tBar;
@synthesize cancelBtn;
@synthesize optionTitle;
@synthesize labels;
@synthesize selectedRowIndex;
@synthesize preferredFontSize;

-(void)updateTitle
{
	if (optionTitle != nil)
		self.title = optionTitle;
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return MODAL_OPTIONS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"FROM_VIEW"] != nil &&
			(msg.parameterBag)[@"OPTION_TYPE_ID"] != nil &&
			(msg.parameterBag)[@"LABELS"] != nil)
		{
			self.labels = (NSArray*)(msg.parameterBag)[@"LABELS"];
			
			if ((msg.parameterBag)[@"SELECTED_ROW_INDEX"] != nil)
			{
				self.selectedRowIndex = [(NSNumber*)(msg.parameterBag)[@"SELECTED_ROW_INDEX"] intValue];
			}
			
			if ((msg.parameterBag)[@"PREFERRED_FONT_SIZE"] != nil)
			{
				self.preferredFontSize = [(NSNumber*)(msg.parameterBag)[@"PREFERRED_FONT_SIZE"] intValue];
			}
			else
			{
				self.preferredFontSize = 0;
			}
			
			
			if (tblView != nil)
				[tblView reloadData];
			
			[self scrollToSelectedRow];
		}
	}
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

	tBar.topItem.title = optionTitle;
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self updateTitle];
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
	[tblView scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionMiddle animated:NO];
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSInteger row = [indexPath row];
	
	// Notify the delegate
	if (self.delegate != nil)
		[self.delegate optionSelectedAtIndex:row];

	[self dismissViewControllerAnimated:YES completion:nil];	
}


#pragma mark -
#pragma mark Cancel and Close Methods
-(IBAction) btnCancel:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];	
	/*
	 // call delegate
	 if (self.delegate != nil)
	 [self.delegate fieldCanceled:self.field];
	 */
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
	self.tblView = nil;
	self.tBar = nil;
	self.cancelBtn = nil;
}


@end
