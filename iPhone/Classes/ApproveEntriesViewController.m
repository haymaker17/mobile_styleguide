//
//  ApproveEntriesViewController.m
//  ConcurMobile
//
//  Created by yiwen on 1/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveEntriesViewController.h"
#import "RootViewController.h"
#import "ApproveReportDetailData.h"

#import "ApproveReportExpenseCell.h"
#import "ApproveReportSummaryViewController.h"
#import "ApproveReportViewExceptionsViewController.h"
#import "ApproveReportViewReceiptsViewController.h"
#import "ApproveReportViewCommentsViewController.h"

#import "BlueButton.h"
#import "FormatUtils.h"

static int traceLevel = 2;
#define LOG_IF(level, x) { if(level<=traceLevel) x; }

@implementation ApproveEntriesViewController

@synthesize	tableView;

@synthesize listSummaryRows;

@synthesize txtEmployee;
@synthesize txtName;
@synthesize txtTotal;

#define kSectionSummaryAndViews	0

#define		kSectionSummaryAndViewsCellCount	4

#define		kSectionExpenseEntries	1

#define		kTitle				@"title"
#define		kIcon				@"icon"
#define		kRowId				@"rowid"

#define		kRoleCode			@"MOBILE_EXPENSE_MANAGER"

#define		kSkipParse			1

-(NSString *)getViewIDKey
{
	return APPROVE_ENTRIES;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void)respondToFoundData:(Msg *)msg
{
	//respond to data that might be coming from the cache
	if ([msg.parameterBag objectForKey:@"TO_VIEW"] != nil)
	{
		//below is the pattern of getting the object you want and using it.
		NSString *toView = [msg.parameterBag objectForKey:@"TO_VIEW"];
		if (![toView isEqualToString:[self getViewIDKey]])
		{
			return;
		}
		
		ApproveReportDetailData * reportDetailData = (ApproveReportDetailData *)msg.responder;
		
		// Grab parameter bag from reportDetailData to show general properties of the Report
		parameterBag = reportDetailData.parameterBag;
	
		// Add all entries to parameter bag
		[parameterBag setObject:[reportDetailData entries] forKey:@"Entries"];
		[parameterBag setObject:[reportDetailData entryKeys] forKey:@"EntryKeys"];
		[parameterBag setObject:[reportDetailData fields] forKey:@"Fields"];
		[parameterBag setObject:[reportDetailData exceptions] forKey:@"Exceptions"];
		[parameterBag setObject:[reportDetailData comments] forKey:@"Comments"];
		[parameterBag setObject:[reportDetailData comments] forKey:@"Itemizations"];

		LOG_IF(2, NSLog(@"ApproveEntriesViewController::respondToFoundData:parameterBag %@", parameterBag));
		
		txtEmployee.text = [parameterBag objectForKey:@"EmployeeName"];
		txtName.text = [parameterBag objectForKey:@"ReportName"];
		txtTotal.text = [FormatUtils formatMoney:[parameterBag objectForKey:@"TotalClaimedAmount"] 
							crnCode:[parameterBag objectForKey:@"CrnCode"]];
		
		// Build summary row array
		if(listSummaryRows != nil)
			[listSummaryRows removeAllObjects];
		else
			listSummaryRows = [[NSMutableArray alloc] init];
			
		[listSummaryRows addObject:[NSDictionary dictionaryWithObjectsAndKeys:
				[rootViewController getLocalizedText:[self getViewIDKey] 
					LocalConstant:[[self getViewIDKey] stringByAppendingString:@"_ACTION_SECTION_REPORT_SUMMARY_ROW"]], kTitle, 
									@"report_summary_24X24_PNG.png", kIcon,  
									@"ReportSummary", kRowId,  
									nil]];

#if kViewCommentsActionItem
		if([reportDetailData.comments count])
		{
			[listSummaryRows addObject:[NSDictionary dictionaryWithObjectsAndKeys:
					[rootViewController getLocalizedText:[self getViewIDKey] 
						LocalConstant:[[self getViewIDKey] stringByAppendingString:@"_ACTION_SECTION_VIEW_COMMENTS_ROW"]], kTitle, 
							@"comments_24X24_PNG.png", kIcon, 
							@"Comments", kRowId,  
									nil]];
		}
#endif

		if([[reportDetailData.parameterBag objectForKey:@"ReceiptImageAvailable"] isEqualToString:@"Y"])
		{
			[listSummaryRows addObject:[NSDictionary dictionaryWithObjectsAndKeys:
				[rootViewController getLocalizedText:[self getViewIDKey] 
							LocalConstant:[[self getViewIDKey] stringByAppendingString:@"_ACTION_SECTION_VIEW_RECEIPTS_ROW"]], kTitle, 
								@"view_receipts_24X24_PNG.png", kIcon,
								@"Receipts", kRowId,  
									nil]];
		}
		
		LOG_IF(2, NSLog(@"respondToFoundData:listSummaryRows %@", self.listSummaryRows));
		
		[self.tableView reloadData];
	}
}

- (NSInteger) getEntryCount
{
	NSArray *entryKeys = [parameterBag objectForKey:@"EntryKeys"];
	if(entryKeys != nil) 
		return [entryKeys count];
	
	return 0;   
}

- (NSDictionary *) getRowEntryData:(NSUInteger) row
{
	NSArray *entryKeys = [parameterBag objectForKey:@"EntryKeys"];
	if(entryKeys != nil)
	{
		if(row < [entryKeys count])
		{
			NSDictionary *rowData = [[parameterBag objectForKey:@"Entries"] objectForKey:[entryKeys objectAtIndex:row]]; 
			LOG_IF(2, NSLog(@"ApproveEntriesViewController:getRowEntryData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveEntriesViewController:getRowEntryData failed"));
	return nil;
}

-(IBAction) viewExpenseDetails : (NSUInteger ) row
{
	// Set skip parsing flag
	[parameterBag setObject:[[parameterBag objectForKey:@"EntryKeys"] objectAtIndex:row] forKey:@"currentEntryKey"];
	[parameterBag setObject:@"YES" forKey:@"SKIP_PARSE"];
	LOG_IF(3, NSLog(@"ApproveEntriesViewController:viewExpenseDetails sending %@", parameterBag));
	
	[rootViewController switchToView:APPROVE_EXPENSE_DETAILS viewFrom:APPROVE_ENTRIES ParameterBag:parameterBag];
}

-(IBAction) viewReportSummary : (NSUInteger ) row
{
	LOG_IF(2, NSLog(@"ApproveEntriesViewController:viewReportSummary"));
	LOG_IF(1, NSLog(@"ApproveEntriesViewController::viewReportSummary:parameterBag %@", parameterBag));

	// Set skip parsing flag
	[parameterBag setObject:@"YES" forKey:@"SKIP_PARSE"];
	[rootViewController switchToView:APPROVE_REPORT_SUMMARY viewFrom:APPROVE_ENTRIES ParameterBag:parameterBag];
}

-(IBAction) viewExceptions : (NSUInteger ) row
{
	LOG_IF(2, NSLog(@"ApproveEntriesViewController:viewExceptions"));
	
	// Set skip parsing flag
	[parameterBag setObject:@"YES" forKey:@"SKIP_PARSE"];
	[rootViewController switchToView:APPROVE_VIEW_EXCEPTIONS viewFrom:APPROVE_ENTRIES ParameterBag:parameterBag];
}

-(IBAction) viewReceipts : (NSUInteger ) row
{
	LOG_IF(2, NSLog(@"ApproveEntriesViewController:viewReceipts"));
	
	// Set skip parsing flag
	[parameterBag setObject:@"YES" forKey:@"SKIP_PARSE"];
	[rootViewController switchToView:APPROVE_VIEW_RECEIPTS viewFrom:APPROVE_ENTRIES ParameterBag:parameterBag];
}

-(IBAction) viewComments : (NSUInteger ) row
{
	LOG_IF(2, NSLog(@"ApproveEntriesViewController:viewComments"));
	
	// Set skip parsing flag
	[parameterBag setObject:@"YES" forKey:@"SKIP_PARSE"];
	[rootViewController switchToView:APPROVE_VIEW_COMMENTS viewFrom:APPROVE_ENTRIES ParameterBag:parameterBag];
}

- (void)viewDidLoad 
{
	self.title =  [rootViewController getLocalizedText:
				   [self getViewIDKey] LocalConstant:[[self getViewIDKey] stringByAppendingString:@"_VIEW_TITLE"]];
	//NSLog(@"ApproveEntriesViewController::title set to: %@", self.title );
	
	// Show basic report info while message is being parsed.
//	txtEmployee.text = [parameterBag objectForKey:@"EmployeeName"];
//	txtName.text = [parameterBag objectForKey:@"ReportName"];
//	txtTotal.text = [FormatUtils formatMoney:[parameterBag objectForKey:@"TotalClaimedAmount"] 
//									 crnCode:[parameterBag objectForKey:@"CrnCode"]];
	
	listSummaryRows = [[NSMutableArray alloc] init];
	
  	parameterBag = nil;

	
	
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
	[txtTotal release];
	[txtEmployee release];
	[txtName release];
	[listSummaryRows release];

 	[super dealloc];
}


#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 2;
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	if(section == kSectionSummaryAndViews)
		return [listSummaryRows count]; 
	else	
		return [self getEntryCount];
}

- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if(indexPath.section == kSectionSummaryAndViews)
	{
		static NSString *MyDefaultCellIdentifier = @"MyDefaultCellIdentifier";
		
		UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyDefaultCellIdentifier];
		if (cell == nil)  
		{
			cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyDefaultCellIdentifier]; 
		}
		
		cell.textLabel.text = [[listSummaryRows objectAtIndex:indexPath.row] objectForKey:kTitle];
		cell.textLabel.font = [UIFont boldSystemFontOfSize:18];
		
		NSString* iconName = [[listSummaryRows objectAtIndex:indexPath.row] objectForKey:kIcon];
		cell.imageView.image = [UIImage imageNamed:iconName];				   
		cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;

		return cell;
	}
	else
	if(indexPath.section == kSectionExpenseEntries)
	{	
		static NSString *MyCustomCellIdentifier = @"ApproveReportExpenseCell";
		
		ApproveReportExpenseCell *cell = (ApproveReportExpenseCell *)[self.tableView dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveReportExpenseCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ApproveReportExpenseCell class]])
					cell = (ApproveReportExpenseCell *)oneObject;
		}
		
		NSDictionary *rowData = [self getRowEntryData:indexPath.row];
		if(rowData == nil)
			return nil;
		
		[cell initWithData:rowData];

		return cell;
	}
	
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

	if(indexPath.section == kSectionSummaryAndViews)
	{	
		NSString* rowId = [[listSummaryRows objectAtIndex:indexPath.row] objectForKey:kRowId];
		if([rowId isEqualToString:@"ReportSummary"])
		{
			[self viewReportSummary:indexPath.row];
		}
		else
		if([rowId isEqualToString:@"Exceptions"])
		{
			[self viewExceptions:indexPath.row];
		}
		if([rowId isEqualToString:@"Receipts"])
		{
			[self viewReceipts:indexPath.row];
		}
		if([rowId isEqualToString:@"Comments"])
		{
			[self viewComments:indexPath.row];
		}
	}
	else
	if(indexPath.section == kSectionExpenseEntries)
	{	
		[self viewExpenseDetails:indexPath.row];
	}
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if(indexPath.section == kSectionSummaryAndViews)
		return 50;
	else
	{
		return 70;
	}

	return 0;
}


@end
