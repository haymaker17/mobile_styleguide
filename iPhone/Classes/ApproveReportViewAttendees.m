//
//  ApproveReportViewAttendees.m
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportViewAttendees.h"
#import "ApproveReportExpenseCell.h"
#import "ApproveReportExpenseDetailCell.h"
#import "RootViewController.h"
#import "FormatUtils.h"

static int traceLevel = 2;
#define LOG_IF(level, x) { if(level<=traceLevel) x; }

#define kExpenseSummarySection	0

#define kAttendeeAmountRow		0
#define kAttendeeNameRow		1
#define kAttendeeTitleRow		2
#define kAttendeeCompanyRow		3
#define kAttendeeTypeRow		4

@implementation ApproveReportViewAttendees
@synthesize tableView;

@synthesize txtExpense;
@synthesize txtTotal;
@synthesize topBar;

@synthesize currentEntry;
@synthesize listSection;

-(NSString *)getViewIDKey
{
	return APPROVE_VIEW_ATTENDEES;
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
		
		LOG_IF(2, NSLog(@"ApproveReportViewAttendees:PARAMETER BAG: %@\n", 
						parameterBag)); 
		
		// Get current entry key to know what entry we are displaying
		currentEntry = [[parameterBag objectForKey:@"Entries"] objectForKey:[parameterBag objectForKey:@"currentEntryKey"]];
		
		LOG_IF(1, NSLog(@"ApproveReportViewAttendees:CURRENT ENTRY: %@\n", 
						currentEntry)); 
		
		txtExpense.text = [currentEntry objectForKey:@"ExpName"];
		txtTotal.text = [FormatUtils formatMoney:[currentEntry objectForKey:@"TransactionAmount"] 
										 crnCode:[currentEntry objectForKey:@"TransactionCrnCode"]];
		
		[self.tableView reloadData];
	}
}

- (NSInteger) getAttendeeCount
{
	NSArray *rowAttendees = [currentEntry objectForKey:@"Attendees"];
	if(rowAttendees != nil) 
		return [rowAttendees count];
	
	return 0;   
}

- (NSDictionary *) getRowAttendeeData:(NSUInteger) row
{
	NSArray *rowAttendees = [currentEntry objectForKey:@"Attendees"];
	if(rowAttendees != nil)
	{
		if(row < [rowAttendees count])
		{
			NSDictionary *rowData = [rowAttendees objectAtIndex:row]; 
			LOG_IF(2, NSLog(@"ApproveReportViewAttendees:getRowAttendeeData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveReportViewAttendees:getRowAttendeeData failed"));
	return nil;
}

- (void)viewDidLoad 
{
	self.title = [rootViewController getLocalizedText:
				  [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
													 stringByAppendingString:@"_VIEW_TITLE"]];
	//NSLog(@"ApproveReportViewAttendees::title set to: %@", self.title );
	
	currentEntry = nil;
	
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
	
	[currentEntry release];
	[listSection release];
	
    [super dealloc];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1 + [self getAttendeeCount]; // Expense and count of attendees
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	if(section == kExpenseSummarySection)
		return 1; // 1 row to show detail cell
	
	return [self getAttendeeCount]; // number of attendees
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if(indexPath.section == kExpenseSummarySection)
	{
		return 73.0; 
	}
	
	return 50.0; 
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
	if(section == kExpenseSummarySection)
	{
		return [rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
												   stringByAppendingString:@"_SUMMARY_SECTION_TITLE"]];
	}
	else {
		return [[self getRowAttendeeData:(section-1)] objectForKey:@"Name"];
	}
	
	return @"";
}

- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if(indexPath.section == kExpenseSummarySection)
	{
		static NSString *MyCustomCellIdentifier = @"ApproveReportExpenseCell";
		
		ApproveReportExpenseCell *cell = (ApproveReportExpenseCell *)[self.tableView 
																				  dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];
		
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveReportExpenseCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ApproveReportExpenseCell class]])
					cell = (ApproveReportExpenseCell *)oneObject;
		}
		
		if(cell != nil)
		{
			[cell initWithData:currentEntry];
			cell.accessoryType = UITableViewCellAccessoryNone;
		}
		
		return cell;
	}
	else { // Attendees
		
		static NSString *MyCustomCellIdentifier = @"ApproveExpenseDetailCellAttendees";
		
		ApproveReportExpenseDetailCell *cell = (ApproveReportExpenseDetailCell *)[self.tableView 
																				  dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];
		
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveExpenseDetailCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ApproveReportExpenseDetailCell class]])
					cell = (ApproveReportExpenseDetailCell *)oneObject;
		}
		
		NSInteger attendee = indexPath.section - 1;
		
		if(indexPath.row  == kAttendeeAmountRow)
		{
			cell.labelText.text = [rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
					stringByAppendingString:@"_ATTENDEE_AMOUNT_TITLE"]];
					
			cell.labelDetail.text = 
						[FormatUtils formatMoney:[[self getRowAttendeeData:attendee] objectForKey:@"Amount"]
										 crnCode:[parameterBag objectForKey:@"CrnCode"]];			
		}
		else
		if(indexPath.row  == kAttendeeNameRow)
		{
			cell.labelText.text = [rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
					stringByAppendingString:@"_ATTENDEE_NAME_TITLE"]];

			cell.labelDetail.text = [[self getRowAttendeeData:attendee] objectForKey:@"Name"];
		}
		else
		if(indexPath.row  == kAttendeeTitleRow)
		{
			cell.labelText.text = [rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
					stringByAppendingString:@"_ATTENDEE_TITLE_TITLE"]];

			cell.labelDetail.text = [[self getRowAttendeeData:attendee] objectForKey:@"Title"];
		}
		else
		if(indexPath.row  == kAttendeeCompanyRow)
		{
			cell.labelText.text = [rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
					stringByAppendingString:@"_ATTENDEE_COMPANY_TITLE"]];

			cell.labelDetail.text = [[self getRowAttendeeData:attendee] objectForKey:@"Company"];
		}
		else
		if(indexPath.row  == kAttendeeTypeRow)
		{
			cell.labelText.text = [rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
					stringByAppendingString:@"_ATTENDEE_TYPE_TITLE"]];
					
			cell.labelDetail.text = [[self getRowAttendeeData:attendee] objectForKey:@"AtnTypeName"];
		}
		
		return cell;
	}
	
	NSLog(@"ApproveReportViewAttendees::cellForRowAtIndexPath error, returns NIL cell at section: %d, row: %d", indexPath.section, indexPath.row);
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
