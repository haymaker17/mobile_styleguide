//
//  ApproveReportViewItemizations.m
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportViewItemizations.h"
#import "ApproveReportExpenseCell.h"
#import "ApproveReportExpenseDetailCell.h"
#import "RootViewController.h"
#import "FormatUtils.h"

static int traceLevel = 2;
#define LOG_IF(level, x) { if(level<=traceLevel) x; }

#define kItemizationSummarySection	0

@implementation ApproveReportViewItemizations
@synthesize tableView;

@synthesize txtExpense;
@synthesize txtTotal;
@synthesize topBar;

@synthesize currentEntry;
@synthesize listSection;

-(NSString *)getViewIDKey
{
	return APPROVE_VIEW_ITEMIZATIONS;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)respondToFoundData:(Msg *)msg
{
	// respond to data that might be coming from the cache
	//	//NSLog(@"ReportsViewController::respondToFoundData for %@", msg.idKey);
	if ([msg.parameterBag objectForKey:@"TO_VIEW"] != nil)
	{
		//below is the pattern of getting the object you want and using it.
		NSString *toView = [msg.parameterBag objectForKey:@"TO_VIEW"];
		if (![toView isEqualToString:[self getViewIDKey]])
		{
			return;
		}
		
		parameterBag = msg.parameterBag;
		
		LOG_IF(2, NSLog(@"ApproveReportViewItemizations:PARAMETER BAG: %@\n", 
						parameterBag)); 
		
		// Get current entry key to know what entry we are displaying
		currentEntry = [[parameterBag objectForKey:@"Entries"] objectForKey:[parameterBag objectForKey:@"currentEntryKey"]];
		
		LOG_IF(2, NSLog(@"ApproveReportViewItemizations:CURRENT ENTRY: %@\n", 
						currentEntry)); 
		
		txtExpense.text = [currentEntry objectForKey:@"ExpName"];
		txtTotal.text = [FormatUtils formatMoney:[currentEntry objectForKey:@"TransactionAmount"] 
										 crnCode:[currentEntry objectForKey:@"TransactionCrnCode"]];
		
		[self.tableView reloadData];
	}
}

- (NSInteger) getItemizationCount
{
	NSArray *rowItemizations = [currentEntry objectForKey:@"Itemizations"];
	if(rowItemizations != nil) 
		return [rowItemizations count];
	
	return 0;   
}

- (NSDictionary *) getRowItemizationData:(NSUInteger) row
{
	NSArray *rowItemizations = [currentEntry objectForKey:@"Itemizations"];
	if(rowItemizations != nil)
	{
		if(row < [rowItemizations count])
		{
			NSDictionary *rowData = [rowItemizations objectAtIndex:row]; 
			LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowItemizationData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowItemizationData failed"));
	return nil;
}

- (NSInteger) getExceptionCount
{
	NSArray *rowExceptions = [currentEntry objectForKey:@"Exceptions"];
	if(rowExceptions != nil) 
		return [rowExceptions count];
	
	return 0;   
}

- (NSDictionary *) getRowExceptionData:(NSUInteger) row
{
	NSArray *rowExceptions = [currentEntry objectForKey:@"Exceptions"];
	if(rowExceptions != nil)
	{
		if(row < [rowExceptions count])
		{
			NSDictionary *rowData = [rowExceptions objectAtIndex:row]; 
			LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowExceptionData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowExceptionData failed"));
	return nil;
}

- (NSInteger) getFieldCount
{
	NSArray *rowFields = [currentEntry objectForKey:@"Fields"];
	if(rowFields != nil) 
		return [rowFields count];
	
	return 0;   
}

- (NSDictionary *) getRowFieldData:(NSUInteger) row
{
	NSArray *rowFields = [currentEntry objectForKey:@"Fields"];
	if(rowFields != nil)
	{
		if(row < [rowFields count])
		{
			NSDictionary *rowData = [rowFields objectAtIndex:row]; 
			LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowFieldData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowFieldData failed"));
	return nil;
}

- (NSInteger) getCommentCount
{
	NSArray *rowComments = [currentEntry objectForKey:@"Comments"];
	if(rowComments != nil)
		return [rowComments count];
	
	return 0;   
}

- (NSDictionary *) getRowCommentData:(NSUInteger) row
{
	NSArray *rowComments = [currentEntry objectForKey:@"Comments"];
	if(rowComments != nil)
	{
		if(row < [rowComments count])
		{
			NSDictionary *rowData = [rowComments objectAtIndex:row]; 
			LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowCommentData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveReportViewItemizations:getRowFieldData failed"));
	return nil;
}

- (void)viewDidLoad 
{
	self.title = [rootViewController getLocalizedText:
				  [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
													 stringByAppendingString:@"_VIEW_TITLE"]];
	NSLog(@"ApproveReportViewItemizations::title set to: %@", self.title );
	
	[super viewDidLoad];
}


// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
		// Custom initialization
	}
	return self;
}


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
	[topBar release];
	[txtExpense release];
	[txtTotal release];
	
	[tableView release];
	
	[listSection release];
	
    [super dealloc];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1 + [self getItemizationCount];
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	NSInteger count = [self getItemizationCount];
	if(section == kItemizationSummarySection)
	{
		if(count)
			return count; // 1 row for header plus number of itemizations
	}
	
	// Else number of itemizations
	return count;
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if(indexPath.section == kItemizationSummarySection)
	{
		return 50.0; 
	}

	return 50.0; 
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
	if(section == kItemizationSummarySection)
	{
		return [rootViewController getLocalizedText:
							   [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
											stringByAppendingString:@"_SUMMARY_SECTION_TITLE"]];
	}
	else {
		return [[self getRowItemizationData:section-1] objectForKey:@"ExpName"];
	}

	return @"";
}

- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if(indexPath.section == kItemizationSummarySection)
	{
		static NSString *MyCustomCellIdentifier = @"ApproveExpenseDetailCell";
		
		ApproveReportExpenseDetailCell *cell = (ApproveReportExpenseDetailCell *)[self.tableView 
			dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];

		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveExpenseDetailCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ApproveReportExpenseDetailCell class]])
					cell = (ApproveReportExpenseDetailCell *)oneObject;
		}
		
		NSString* amount = [FormatUtils formatMoney:[[self getRowItemizationData:indexPath.row] objectForKey:@"TransactionAmount"] 
						crnCode:[[self getRowItemizationData:indexPath.row] objectForKey:@"TransactionCrnCode"]];
		
		cell.labelText.text = [[self getRowItemizationData:indexPath.row] objectForKey:@"ExpName"];
		cell.labelDetail.text = amount;
		
		return cell;
	}
	else { // Itemizations
		
		static NSString *MyCustomCellIdentifier = @"ApproveExpenseDetailCell";
		
		ApproveReportExpenseDetailCell *cell = (ApproveReportExpenseDetailCell *)[self.tableView 
					dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];
		
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveExpenseDetailCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ApproveReportExpenseDetailCell class]])
					cell = (ApproveReportExpenseDetailCell *)oneObject;
		}
		
		NSInteger itemization = indexPath.section - 1;
		NSString* amount = [FormatUtils formatMoney:[[self getRowItemizationData:itemization] objectForKey:@"TransactionAmount"] 
											crnCode:[[self getRowItemizationData:itemization] objectForKey:@"TransactionCrnCode"]];
		
		cell.labelText.text = @""; // [[self getRowItemizationData:row] objectForKey:@"ExpName"];
		cell.labelDetail.text = amount;
		
		return cell;
	}

	//NSLog(@"ApproveReportViewItemizations::cellForRowAtIndexPath error, returns NIL cell at section: %d, row: %d", indexPath.section, indexPath.row);
 	return nil;
}
#pragma mark -
#pragma mark Table Delegate Methods 

-(NSIndexPath *)tableView:(UITableView *)tableView 
 willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	return indexPath; 
}

- (void)tableView:(UITableView *)tableView 
didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	[self.tableView deselectRowAtIndexPath:[self.tableView indexPathForSelectedRow] animated:YES];
}

-(IBAction)expandCell:(id)sender detailType:(NSString *)detail rowNumber:(NSUInteger)rowNum
{
	[self.tableView reloadData];
}

@end
