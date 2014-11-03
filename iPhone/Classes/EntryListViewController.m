//
//  EntryListViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/12/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "EntryListViewController.h"
#import "EntryCell.h"
#import "EntryCellBig.h"
#import "RootViewController.h"

@implementation EntryListViewController

@synthesize listData;
@synthesize listOGData;
@synthesize tableView;
@synthesize listDict;
@synthesize navBar;
//@synthesize rootViewController;

-(NSString *)getViewIDKey
{
	return APPROVE_ENTRIES;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(NSArray *)getPreviewStack
{
	//This method needs to add and do the following:
//	if([from isEqualToString:SPLASH])
//	{
//		[self.view insertSubview:self.homePageViewController.view atIndex:0];
//		from = @"";
//		if (self.expenseListViewController == nil)
//		{
//			ExpenseListViewController *listController = [[ExpenseListViewController alloc] initWithNibName:@"ExpenseListView" bundle:nil];
//			self.expenseListViewController = listController;
//			self.expenseListViewController.rootViewController = self;
//			[listController release];
//		}
//		[delegate.navController pushViewController:self.expenseListViewController animated:NO];
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
	self.title = @"Entries";
//	UINavigationItem *navItem;
//	navItem = [UINavigationItem alloc];
//	
//	UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
//	label.backgroundColor = [UIColor clearColor];
//	label.font = [UIFont boldSystemFontOfSize:20.0];
//	label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
//	label.textAlignment = UITextAlignmentCenter;
//	label.textColor =[UIColor whiteColor];
//	label.text=self.title;		
//	navItem.titleView = label;
//	[label release];
//	
//	[navBar pushNavigationItem:navItem animated:YES];
//	[navBar setDelegate:self]; 
//	[navItem release];
	
	//Sample data
//    NSMutableArray *array = [[NSMutableArray alloc] initWithObjects:@"A",nil];
//    NSMutableArray *arrayOG = [[NSMutableArray alloc] initWithObjects:@"A",nil];
    NSMutableArray *array = [[NSMutableArray alloc] initWithObjects:@"A", @"B",
							 @"C", @"D", nil];
    NSMutableArray *arrayOG = [[NSMutableArray alloc] initWithObjects:@"A", @"B",
							   @"C", @"D", nil];
    self.listData = array;
	self.listOGData = arrayOG;
    [array release];
	[arrayOG release];
	
	
	NSDictionary *row1 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Airfare", @"Name", @"$1,929.45", @"Amount", @"Oct 21, 2009", @"Date", @"Alaska Airlines, Seattle", @"VendorLoc", @"amex_21X21.png", @"Image1", @"airfare_21X21.png", @"Image2", @"eReceipt_21X21.png", @"Image3", nil];
    NSDictionary *row2 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Car Rental", @"Name", @"$579.00", @"Amount", @"Oct 21, 2009", @"Date", @"Hertz, Atlanta", @"VendorLoc", @"eReceipt_21X21.png", @"Image1", @"car_21X21.png", @"Image2", @"", @"Image3", nil];
    NSDictionary *row3 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Breakfast", @"Name", @"$12.34", @"Amount", @"Oct 21, 2009", @"Date", @"Starbucks, Atlanta", @"VendorLoc", @"camera_21X21.png", @"Image1", @"", @"Image2", @"", @"Image3", nil];
    NSDictionary *row4 = [[NSDictionary alloc] initWithObjectsAndKeys:
                          @"Room Rate", @"Name", @"$168.56", @"Amount", @"Oct 22, 2009", @"Date", @"Radisson Hotels, Atlanta", @"VendorLoc", @"amex_21X21.png", @"Image1", @"", @"Image2", @"", @"Image3", nil];
    
	//NSArray *arrayDict = [[NSArray alloc] initWithObjects:row1, nil];
    NSArray *arrayDict = [[NSArray alloc] initWithObjects:row1, row2, row3, row4, nil];
	// NSArray *arrayDict = [[NSArray alloc] initWithObjects:row1, nil];
	self.listDict = arrayDict;
	[row1 release];
    [row2 release];
    [row3 release];
    [row4 release];
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
		EntryCell *cell = (EntryCell *)[tableView dequeueReusableCellWithIdentifier: MyCellIdentifier];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"EntryCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[EntryCell class]])
					cell = (EntryCell *)oneObject;
		}
		
		NSDictionary *rowData = [self.listDict objectAtIndex:row];
		if ([rowData objectForKey:@"Image1"] != nil)
		{
			UIImage *getimg = [UIImage imageNamed:@"flight.png"];
			[cell.image1 setImage:getimg];
		}
		
		cell.label.text = [rowData objectForKey:@"Name"]; 
		cell.labelAmount.text = [rowData objectForKey:@"Amount"];
		cell.labelStatus.text = [rowData objectForKey:@"Date"];
		cell.labelStatusTwo.text = [rowData objectForKey:@"VendorLoc"];
		cell.image1.image = [UIImage imageNamed:[rowData objectForKey:@"Image1"]];
		cell.image2.image = [UIImage imageNamed:[rowData objectForKey:@"Image2"]];
		cell.image3.image = [UIImage imageNamed:[rowData objectForKey:@"Image3"]];
		cell.rootVC = self;
		cell.currentRow = row;
		
		return cell;
	}
	else 
	{//expanded cell view		
		EntryCellBig *cell = (EntryCellBig *)[tableView dequeueReusableCellWithIdentifier: @"EntryCellBig"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"EntryCellBig" 
														 owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[EntryCellBig class]])
					cell = (EntryCellBig *)oneObject;
		}
		
		NSDictionary *rowData = [self.listDict objectAtIndex:row];
		
		cell.label.text = [rowData objectForKey:@"Name"]; 
		cell.labelAmount.text = [rowData objectForKey:@"Amount"];
		cell.labelStatus.text = [rowData objectForKey:@"Date"];
		cell.labelStatusTwo.text = [rowData objectForKey:@"VendorLoc"];
		cell.image1.image = [UIImage imageNamed:[rowData objectForKey:@"Image1"]];
		cell.image2.image = [UIImage imageNamed:[rowData objectForKey:@"Image2"]];
		cell.image3.image = [UIImage imageNamed:[rowData objectForKey:@"Image3"]];
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
