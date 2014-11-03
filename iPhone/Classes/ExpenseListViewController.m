//
//  ExpenseListViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/12/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "ExpenseListViewController.h"
#import "MyCell.h";
#import "BigCell.h";
#import "RootViewController.h"

@implementation ExpenseListViewController

@synthesize listData;
@synthesize listOGData;
@synthesize tableView;
@synthesize listDict;
@synthesize navBar;
//@synthesize rootViewController;

-(NSArray *)getPreviewStack
{
	//This method needs to add and do the following:
//	if([from isEqualToString:SPLASH])
//	{
//		[self.view insertSubview:self.homePageViewController.view atIndex:0];
//		from = @"";
//	}
	return nil;
}

-(IBAction)expandCell:(id)sender detailType:(NSString *)detail rowNumber:(NSUInteger *)rowNum
{	
	[self.listData replaceObjectAtIndex:rowNum withObject:detail];
	[self.tableView reloadData];
}

-(IBAction)switchViews:(id)sender
{
	[rootViewController switchViews:sender ParameterBag:nil];
}

- (void)viewDidLoad 
{
	self.title = @"Active Reports";
	UINavigationItem *navItem;
	navItem = [UINavigationItem alloc];
	
	UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
	label.backgroundColor = [UIColor clearColor];
	label.font = [UIFont boldSystemFontOfSize:20.0];
	label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
	label.textAlignment = UITextAlignmentCenter;
	label.textColor =[UIColor whiteColor];
	label.text=self.title;		
	navItem.titleView = label;
	[label release];
	
	[navBar pushNavigationItem:navItem animated:YES];
	[navBar setDelegate:self]; 
	[navItem release];
	
	//Sample data
    NSMutableArray *array = [[NSMutableArray alloc] initWithObjects:@"Trip from Seattle to Atlanta", @"Company Annual Meeting Prep",
							 @"Usability Test at Boeing", @"Seattle to Boston", @"Dallas Board Meeting", nil];
    NSMutableArray *arrayOG = [[NSMutableArray alloc] initWithObjects:@"Trip from Seattle to Atlanta", @"Company Annual Meeting Prep",
							   @"Usability Test at Boeing", @"Seattle to Boston", @"Dallas Board Meeting", nil];
    self.listData = array;
	self.listOGData = arrayOG;
    [array release];
	[arrayOG release];
	
	
	NSDictionary *row1 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Dallas Conference", @"Name", @"$4,929.45", @"Total", @"Pending Approval", @"Status", @"10/15/2009", @"ReportDate", @"Travel to Dallas for the TexiComiCon.  Also includes a wide range of CosPlay outfits so that I could fit in with the convention crowd.", @"Purpose", @"Not Paid", @"PayStatus", @"$4,929.45", @"Claimed", @"$4,400.45", @"Approved", nil];
    NSDictionary *row2 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Boston Trip", @"Name", @"$759.00", @"Total", @"Not Filed", @"Status", @"10/18/2009", @"ReportDate", @"Checked out the old Lotus offices that are now occupied by folks from IBM.", @"Purpose", @"Not Paid", @"PayStatus", @"$759.00", @"Claimed", @"$0.00", @"Approved", nil];
    NSDictionary *row3 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Software Upgrades", @"Name", @"$49.34", @"Total", @"Final Approval", @"Status", @"10/21/2009", @"ReportDate", @"Bought VMWare Fusion 3.0, which is needed in order to run Windows applications from my MacBook.", @"Purpose", @"Not Paid", @"PayStatus", @"$49.34", @"Claimed", @"$49.34", @"Approved", nil];
    NSDictionary *row4 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Client Appreciation", @"Name", @"$163.56", @"Total", @"Rejected", @"Status", @"10/27/2009", @"ReportDate", @"We went to Daniels in Bellevue and had some awesome drinks.  I think the clients like us even more than they did before.", @"Purpose", @"Not Paid", @"PayStatus", @"$163.56", @"Claimed", @"$0.00", @"Approved", nil];
    NSDictionary *row5 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Conversion Team Party", @"Name", @"$227.00", @"Total", @"Not Filed", @"Status", @"10/31/2009", @"ReportDate", @"After a successful deployment of ETL the folks involved in the upgrades decided thast we needed a party.  We also were given brand new iPhones.", @"Purpose", @"Not Paid", @"PayStatus", @"$227.00", @"Claimed", @"$0.00", @"Approved", nil];
    
    NSArray *arrayDict = [[NSArray alloc] initWithObjects:row1, row2, 
						  row3, row4, row5, nil];
	self.listDict = arrayDict;
	[row1 release];
    [row2 release];
    [row3 release];
    [row4 release];
    [row5 release];
    [arrayDict release];
	
    [super viewDidLoad];
}

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        // Custom initialization
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}
*/

/*
// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
*/

- (void)didReceiveMemoryWarning {
	// Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
	
	// Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
	// Release any retained subviews of the main view.
	// e.g. self.myOutlet = nil;
	self.listData = nil;
}


- (void)dealloc {
	[listData release];
	[listOGData release];
	[tableView release];
	[listDict release];
	[navBar release];
	[rootViewController release];
    [super dealloc];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
    return [self.listData count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
    NSUInteger row = [indexPath row];
	static NSString *MyCellIdentifier = @"MyCellIdentifier";
	
	NSString *val = [self.listData objectAtIndex:row];
	if (val != @"Justin")
	{//just the header
		MyCell *cell = (MyCell *)[self.tableView dequeueReusableCellWithIdentifier: MyCellIdentifier];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"MyCell" 
														 owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[MyCell class]])
					cell = (MyCell *)oneObject;
		}
		
		NSDictionary *rowData = [self.listDict objectAtIndex:row];
		
		cell.label.text = [rowData objectForKey:@"Name"]; //[listOGData objectAtIndex:row];
		cell.labelTotal.text = [rowData objectForKey:@"Total"];
		NSString *status = [rowData objectForKey:@"Status"];
		if (status == @"Rejected")
		{
			cell.labelStatus.textColor = [UIColor redColor];
		}
		else if (status == @"Not Filed")
		{
			cell.labelStatus.textColor = [UIColor grayColor];
		}
		else 
		{
			cell.labelStatus.textColor = [UIColor darkGrayColor];
		}

		cell.labelStatus.text = [rowData objectForKey:@"Status"];
		cell.rootVC = self;
		cell.currentRow = row;
		
		return cell;
	}
	else 
	{//expanded cell view		
		BigCell *cell = (BigCell *)[self.tableView dequeueReusableCellWithIdentifier: @"BigCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BigCell" 
														 owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[BigCell class]])
					cell = (BigCell *)oneObject;
		}
		NSDictionary *rowData = [self.listDict objectAtIndex:row];
		
		cell.label.text = [rowData objectForKey:@"Name"]; //[listOGData objectAtIndex:row];
		cell.labelTotal.text = [rowData objectForKey:@"Total"];
		NSString *status = [rowData objectForKey:@"Status"];
		if (status == @"Rejected")
		{
			cell.labelStatus.textColor = [UIColor redColor];
		}
		else if (status == @"Not Filed")
		{
			cell.labelStatus.textColor = [UIColor grayColor];
		}
		else 
		{
			cell.labelStatus.textColor = [UIColor darkGrayColor];
		}
		cell.labelStatus.text = [rowData objectForKey:@"Status"];
		cell.labelReportDate.text = [rowData objectForKey:@"ReportDate"];
		cell.labelPayStatus.text = [rowData objectForKey:@"PayStatus"];
		cell.labelClaimed.text = [rowData objectForKey:@"Claimed"];
		cell.labelApproved.text = [rowData objectForKey:@"Approved"];
		cell.purpose.text = [rowData objectForKey:@"Purpose"];
		cell.rootVC = self;
		cell.currentRow = row;
		return cell;
	}
	
	
}
#pragma mark -
#pragma mark Table Delegate Methods 

-(NSIndexPath *)tableView:(UITableView *)tableView 
 willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    if (row == 0)
        return nil;
    
    return indexPath; 
}

- (void)tableView:(UITableView *)tableView 
didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	//    NSUInteger row = [indexPath row];
	//	NSString *val = [self.listData objectAtIndex:row];
	//	if (val == @"Justin")
	//	{
	//		val = @"Birds";
	//	}
	//	else{
	//		val = @"Justin";
	//	}
	//	
	//	[self.listData replaceObjectAtIndex:row withObject:val];
	//	[tableView reloadData];
	//    [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	NSString *val = [self.listData objectAtIndex:row];
	if (val == @"Justin")
	{
		return 200;
	}
	else 
	{
		return 50;
	}
}

@end
