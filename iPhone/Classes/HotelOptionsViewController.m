//
//  HotelOptionsViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelOptionsViewController.h"
#import "ExSystem.h" 


@implementation HotelOptionsViewController

@synthesize fromView;
@synthesize optionTypeId;
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
	return HOTEL_OPTIONS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"FROM_VIEW"] != nil &&
			(msg.parameterBag)[@"OPTION_TYPE_ID"] != nil &&
			(msg.parameterBag)[@"LABELS"] != nil)
		{
			self.fromView = (NSString*)(msg.parameterBag)[@"FROM_VIEW"];
			self.optionTypeId = (NSString*)(msg.parameterBag)[@"OPTION_TYPE_ID"];
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
            
            NSString* titleParam = (msg.parameterBag)[@"TITLE"];
            if ([titleParam length])
                self.optionTitle = titleParam;

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

	if (labels == nil)
		self.labels = @[];
	
	self.selectedRowIndex = -1;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self updateTitle];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
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
    return [labels count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    static NSString *CellIdentifier = @"Cell";
	
	NSInteger row = [indexPath row];
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
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
	NSNumber *rowAsNumber = @(row);
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.optionTypeId, @"OPTION_TYPE_ID", rowAsNumber, @"SELECTED_ROW_INDEX", @"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", nil];

    //NSLog(@"fromView=%@", fromView);

    if([fromView isEqualToString:@"HOTEL_BOOKING"] || [fromView isEqualToString:@"CAR" ] || [fromView isEqualToString:@"CAR_DETAILS" ] || [fromView isEqualToString:@"AIR_BOOKING"] || [fromView isEqualToString:@"TRAIN_DETAILS"])
    {
        int vcCount = [self.navigationController.viewControllers count];
        
        UIViewController *vc = (self.navigationController.viewControllers)[vcCount - 2];
        MobileViewController *mvc = (MobileViewController *)vc;
        Msg *msg = [[Msg alloc] init];
        msg.idKey = @"SHORT_CIRCUIT";
        msg.parameterBag = pBag;
        [mvc respondToFoundData:msg];
        [self.navigationController popViewControllerAnimated:YES];
    }
    else 
    {
        UIViewController *vc = (self.navigationController.viewControllers)[0];
        MobileViewController *mvc = (MobileViewController *)vc;
        Msg *msg = [[Msg alloc] init];
        msg.idKey = self.optionTypeId;
        msg.parameterBag = pBag;
        [mvc respondToFoundData:msg];
        [self.navigationController popToRootViewControllerAnimated:YES];
    }
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}



@end

