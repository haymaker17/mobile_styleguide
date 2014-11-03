//
//  TrainTimeVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainTimeVC.h"
#import "TrainTimeCell.h"

@implementation TrainTimeVC
@synthesize aTimes, tableList, bcdDate, bcdTime, lblDepartureDate, isReturn;
/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	//tableList.frame = CGRectMake(0, tableList.frame.origin.y, tableList.frame.size.width, tableList.frame.size.height + 44);
	self.hidesBottomBarWhenPushed = YES;
	
    [super viewDidLoad];
	[self initTimeList];
	
	if(isReturn)
		self.title = [Localizer getLocalizedText:@"Return Time"];
	else 
		self.title = [Localizer getLocalizedText:@"Departure Time"];
	
	lblDepartureDate.text = bcdDate.val;
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}



-(void) initTimeList
{
	aTimes = [[NSMutableArray alloc] initWithObjects:@"Anytime", @"Morning", @"Afternoon", @"Evening", @"Midnight", @"1 AM"
			  , @"2 AM", @"3 AM", @"4 AM", @"5 AM", @"6 AM", @"7 AM", @"8 AM", @"9 AM", @"10 AM", @"11 AM", @"Noon"
			  , @"1 PM", @"2 PM", @"3 PM", @"4 PM", @"5 PM", @"6 PM", @"7 PM", @"8 PM", @"9 PM", @"10 PM", @"11 PM", nil];
	
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
		return [aTimes count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	//NSUInteger section = [indexPath section];
	
	TrainTimeCell *cell = (TrainTimeCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainTimeCell"];
	if (cell == nil)
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainTimeCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[TrainTimeCell class]])
				cell = (TrainTimeCell *)oneObject;
	}
	
	cell.lbl.text = aTimes[row];
	
	if(row > 0 && row < 3)
		cell.iv.image = [UIImage imageNamed:@"clearday.gif"];
	else if(row > 2 && row < 10)
		cell.iv.image = [UIImage imageNamed:@"clearnight.gif"];
	else if(row > 9 && row < 23)
		cell.iv.image = [UIImage imageNamed:@"clearday.gif"];
	else if(row > 22)
		cell.iv.image = [UIImage imageNamed:@"clearnight.gif"];
	else {
		cell.iv.image = nil;
	}
	
	if([bcdTime.val isEqualToString:aTimes[row]])
		[cell setAccessoryType:UITableViewCellAccessoryCheckmark];
	else {
		[cell setAccessoryType:UITableViewCellAccessoryNone];
	}

	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	bcdTime.val = aTimes[indexPath.row];
	[self.navigationController popViewControllerAnimated:YES];
}

@end
