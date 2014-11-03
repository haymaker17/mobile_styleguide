//
//  ApproveExpenseDetailViewController.m
//  ConcurMobile
//
//  Created by Yuri on 1/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveExpenseDetailViewController.h"
#import "ApproveReportExpenseCell.h"
#import "ApproveReportExpenseDetailCell.h"
#import "RootViewController.h"
#import "FormatUtils.h"

static int traceLevel = 2;
#define LOG_IF(level, x) { if(level<=traceLevel) x; }

#define		kTitle				@"title"
#define		kIcon				@"icon"
#define		kRowCount			@"rowcount"
#define		kRowList			@"rowlist"
#define		kRowHeight			@"rowheight"
#define		kRowId				@"rowid"

@implementation ApproveExpenseDetailViewController
@synthesize tableView;

@synthesize txtEmployee;
@synthesize txtTotal;
@synthesize txtName;
@synthesize topBar;

@synthesize currentEntry, listSection;


#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return APPROVE_EXPENSE_DETAILS;
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{

	if ([msg.parameterBag objectForKey:@"TO_VIEW"] != nil)
	{
		//below is the pattern of getting the object you want and using it.
		NSString *toView = [msg.parameterBag objectForKey:@"TO_VIEW"];
		if (![toView isEqualToString:[self getViewIDKey]])
		{
			return;
		}
		
		// Grab parameter bag
		parameterBag = msg.parameterBag;
		LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:PARAMETER BAG: %@\n", 
						parameterBag)); 
		
		// Get current entry key to know what entry we are displaying
		currentEntry = [[parameterBag objectForKey:@"Entries"] objectForKey:[parameterBag objectForKey:@"currentEntryKey"]];
		
		LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:CURRENT ENTRY: %@\n", 
						currentEntry)); 
		
		txtEmployee.text = [parameterBag objectForKey:@"EmployeeName"];
		txtName.text = [parameterBag objectForKey:@"ReportName"];
		txtTotal.text = [FormatUtils formatMoney:[parameterBag objectForKey:@"TotalClaimedAmount"] 
										 crnCode:[parameterBag objectForKey:@"CrnCode"]];
		
		// Build section list
		// Build summary row array
		if(listSection != nil)
			[listSection removeAllObjects];
		else
			listSection = [[NSMutableArray alloc] init];
		
		// Top section - expense description
		NSString* rowCount = [NSString stringWithFormat:@"%d", 1];
		NSString* rowHeight = [NSString stringWithFormat:@"%d", 70];
		
		[listSection addObject:[NSDictionary dictionaryWithObjectsAndKeys:
								rowCount, kRowCount, 
								rowHeight, kRowHeight, 
								@"Expense", kRowId, 
								nil]]; // no icon
		
		// "Exceptions" section
		if([[currentEntry objectForKey:@"HasExceptions"] isEqualToString:@"Y"])
		{
			rowCount = [NSString stringWithFormat:@"%d", [self getExceptionCount] +1];
			rowHeight = [NSString stringWithFormat:@"%d", 50];
			
			[listSection addObject:[NSDictionary dictionaryWithObjectsAndKeys:
									[rootViewController getLocalizedText:
									 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
																		stringByAppendingString:@"_EXCEPTIONS"]], kTitle, 
									rowCount, kRowCount, 
									rowHeight, kRowHeight, 
									@"Exceptions", kRowId, 
									nil]]; // no icon
		}
		
		// Comments section
		if([[currentEntry objectForKey:@"HasComments"] isEqualToString:@"Y"])
		{
			rowCount = [NSString stringWithFormat:@"%d", [self getCommentCount] +1];
			rowHeight = [NSString stringWithFormat:@"%d", 50];
			
			// "Comments" section
			[listSection addObject:[NSDictionary dictionaryWithObjectsAndKeys:
									[rootViewController getLocalizedText:
									 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
																		stringByAppendingString:@"_COMMENT"]], kTitle, 
									@"", kIcon, 
									rowCount, kRowCount, 
									rowHeight, kRowHeight, 
									@"Comments", kRowId, 
									nil]]; // no icon
		}
		
		// "Action" section
		NSMutableArray* listRows = [[NSMutableArray alloc] init];
		if([[currentEntry objectForKey:@"IsItemized"] isEqualToString:@"Y"])
		{
			
			NSMutableDictionary* dictRows = [[NSMutableDictionary alloc] init];
			[dictRows setObject:[rootViewController getLocalizedText:
								 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
																	stringByAppendingString:@"_VIEW_ITEMIZATIONS"]] forKey:kTitle];
			[dictRows setObject:@"itemization_24X24_PNG.png" forKey:kIcon];
			[dictRows setObject:@"Itemizations" forKey:kRowId];
			[listRows addObject:dictRows];
			[dictRows release];
		}
		
		if([[currentEntry objectForKey:@"HasAttendees"] isEqualToString:@"Y"])
		{
			NSMutableDictionary* dictRows = [[NSMutableDictionary alloc] init];
			[dictRows setObject:[rootViewController getLocalizedText:
								 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
																	stringByAppendingString:@"_VIEW_ATTENDEES"]] forKey:kTitle];
			[dictRows setObject:@"attendees_24X24_PNG.png" forKey:kIcon];
			[dictRows setObject:@"Attendees" forKey:kRowId];
			[listRows addObject:dictRows];
			[dictRows release];
		}
		
		if([listRows count])
		{
			rowCount = [NSString stringWithFormat:@"%d", [listRows count]];
			rowHeight = [NSString stringWithFormat:@"%d", 50];
			
			[listSection addObject:[NSDictionary dictionaryWithObjectsAndKeys:
									@"", kTitle, 
									@"", kIcon, 
									rowCount, kRowCount, 
									rowHeight, kRowHeight, 
									listRows, kRowList,
									50, kRowHeight, 
									@"Actions", kRowId, 
									nil]]; // no icon
		}
		
		[listRows release];
		
		// "Details" section
		if([self getFieldCount])
		{
			rowCount = [NSString stringWithFormat:@"%d", [self getFieldCount] +1];
			rowHeight = [NSString stringWithFormat:@"%d", 50];
			
			[listSection addObject:[NSDictionary dictionaryWithObjectsAndKeys:
									[rootViewController getLocalizedText:
									 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
																		stringByAppendingString:@"_DETAILS"]], kTitle, 
									rowCount, kRowCount, 
									rowHeight, kRowHeight, 
									@"Fields", kRowId, 
									nil]]; // no icon
		}
		
		LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:LIST SECTION: %@\n", 
						listSection)); 
		
		[self.tableView reloadData];
	}
}


#pragma mark Utility Methods
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
			LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getRowExceptionData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getRowExceptionData failed"));
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
			LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getRowFieldData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getRowFieldData failed"));
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
			LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getRowCommentData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getRowCommentData failed"));
	return nil;
}



- (void)viewDidLoad 
{
	self.title = [rootViewController getLocalizedText:
				 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
				  stringByAppendingString:@"_VIEW_TITLE"]];
	//NSLog(@"ApproveExpenseDetailViewController::title set to: %@", self.title );
	
	currentEntry = nil;
	listSection = nil;
	
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
	[txtEmployee release];
	[txtTotal release];
	[txtName release];
	
	[tableView release];
	
	[listSection release];
	 
    [super dealloc];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return [listSection count];
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	return [[[listSection objectAtIndex:section] objectForKey:kRowCount] intValue];
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return [[[listSection objectAtIndex:indexPath.section] objectForKey:kRowHeight] floatValue];
}

- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Expense"])
	{
		// Airfare, Oct 21 etc
		if(indexPath.row == 0) // Description Row
		{
			static NSString *MyCellIdentifier = @"ApproveReportExpenseCell";
			
			ApproveReportExpenseCell *cell = (ApproveReportExpenseCell *)[self.tableView dequeueReusableCellWithIdentifier: MyCellIdentifier];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveReportExpenseCell" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[ApproveReportExpenseCell class]])
						cell = (ApproveReportExpenseCell *)oneObject;
			}
			
			[cell initWithData:currentEntry];
			cell.accessoryType = UITableViewCellAccessoryNone;
			
			return cell;
		}
	}
	else
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Exceptions"])
	{
		// 1 row for header plus number of exceptions
		if(indexPath.row == 0) // header
		{
			static NSString *MyCellIdentifier = @"DefaultCellStyle";
			
			//just the header
			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
			if (cell == nil)  
				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
			
			cell.textLabel.text = [self.rootViewController getLocalizedText:
								   [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
																	  stringByAppendingString:@"_EXCEPTIONS"]];
			cell.textLabel.font = [UIFont boldSystemFontOfSize:16.0];
			cell.textLabel.textAlignment = UITextAlignmentLeft;

			return cell;
		}
		else {
			NSInteger row = indexPath.row - 1; // Skipping header
			
			static NSString *MyCellIdentifier = @"DefaultCellStyle";
			
			//just the header
			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
			if (cell == nil)  
				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
			
			cell.textLabel.numberOfLines = 0; // unlimited line count
			cell.textLabel.font = [UIFont systemFontOfSize:14.0];
			cell.textLabel.textAlignment = UITextAlignmentLeft;
			
			cell.textLabel.text = [[self getRowExceptionData:row] objectForKey:@"ExceptionsStr"];
			return cell;
		}
	}
	else
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Actions"])
	{
		// Use default style with size 18 font
		static NSString *MyDefaultCellIdentifier = @"MyDefaultCellIdentifier";
		
		UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyDefaultCellIdentifier];
		if (cell == nil)  
		{
			cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyDefaultCellIdentifier]; 
		}
		
		cell.textLabel.font = [UIFont boldSystemFontOfSize:18];
		cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
		
		// 2 actions: View Itemizations, View Attendees
		NSDictionary* dictRows = [listSection objectAtIndex:indexPath.section];
		
		NSArray* listRows = [dictRows objectForKey:kRowList];
		NSString* nsAction = [[listRows objectAtIndex:indexPath.row] objectForKey:@"rowid"];
		if([nsAction isEqualToString:@"Itemizations"])
		{
			cell.textLabel.text = [rootViewController getLocalizedText:
								   [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
									stringByAppendingString:@"_VIEW_ITEMIZATIONS"]];
			
			cell.imageView.image = [UIImage imageNamed:@"itemization_24X24_PNG.png"];				   
		}
		else
		if([nsAction isEqualToString:@"Attendees"])
		{
			cell.textLabel.text = [rootViewController getLocalizedText:
								   [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
										stringByAppendingString:@"_VIEW_ATTENDEES"]];
			//
			cell.imageView.image = [UIImage imageNamed:@"attendees_24X24_PNG.png"];		
		}
		
		return cell;
	}
	else
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Fields"])
	{
		// 1 row for header plus field count
		if(indexPath.row == 0) // header
		{
			static NSString *MyCellIdentifier = @"DefaultCellStyle";
			
			//just the header
			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
			if (cell == nil)  
				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
			
			cell.textLabel.text = [rootViewController getLocalizedText:
						 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
															stringByAppendingString:@"_DETAILS"]];
			cell.textLabel.font = [UIFont boldSystemFontOfSize:16.0];
			cell.textLabel.textAlignment = UITextAlignmentLeft;
			
			return cell;
		}
		else { 
			NSInteger row = indexPath.row - 1; // Skipping header
			
			static NSString *MyCustomCellIdentifier = @"ApproveExpenseDetailCell";
			
			ApproveReportExpenseDetailCell *cell = (ApproveReportExpenseDetailCell *)[self.tableView dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];
			if (cell == nil)  
			{
				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveExpenseDetailCell" owner:self options:nil];
				for (id oneObject in nib)
					if ([oneObject isKindOfClass:[ApproveReportExpenseDetailCell class]])
						cell = (ApproveReportExpenseDetailCell *)oneObject;
			}
			
			cell.labelText.text = [[self getRowFieldData:row] objectForKey:@"Label"];

			if([cell.labelText.text rangeOfString:@"Amount"].location != NSNotFound)
				cell.labelDetail.text = [FormatUtils formatMoney:[[self getRowFieldData:row] objectForKey:@"Value"] 
										 crnCode:[parameterBag objectForKey:@"CrnCode"]];			
			else
				cell.labelDetail.text = [[self getRowFieldData:row] objectForKey:@"Value"];
			
			return cell;
		}
	}
	else
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Comments"])
	{
		// 1 row for header plus field count
		if(indexPath.row == 0) // header
		{
			static NSString *MyCellIdentifier = @"DefaultCellStyle";
			
			//just the header
			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
			if (cell == nil)  
				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
			
			cell.textLabel.text = [rootViewController getLocalizedText:
						 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
															stringByAppendingString:@"_COMMENT"]];
			cell.textLabel.font = [UIFont boldSystemFontOfSize:16.0];
			cell.textLabel.textAlignment = UITextAlignmentLeft;
			
			return cell;
		}
		else { 
			NSInteger row = indexPath.row - 1; // Skipping header
			
			static NSString *MyCellIdentifier = @"DefaultCellStyleComment";
			
			//just the header
			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
			if (cell == nil)  
				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
						
			cell.textLabel.text = [[self getRowCommentData:row] objectForKey:@"Comment"];
			cell.textLabel.font = [UIFont systemFontOfSize:14.0];
			cell.textLabel.textAlignment = UITextAlignmentLeft;
			
			return cell;
		}
	}
	
	NSLog(@"ApproveExpenseDetailViewController::cellForRowAtIndexPath error, returns NIL cell at section: %d, row: %d", indexPath.section, indexPath.row);
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
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Actions"])
	{
		NSDictionary* dictRows = [listSection objectAtIndex:indexPath.section];
	
		if(dictRows != nil && [[dictRows objectForKey:kRowId] isEqualToString:@"Actions"])
		{
			NSArray* listRows = [dictRows objectForKey:kRowList];
			NSString* nsAction = [[listRows objectAtIndex:indexPath.row] objectForKey:@"rowid"];
			if([nsAction isEqualToString:@"Itemizations"])
			{
				LOG_IF(3, NSLog(@"ApproveEntriesViewController:didSelectRowAtIndexPath sending %@", parameterBag));
				[rootViewController switchToView:APPROVE_VIEW_ITEMIZATIONS viewFrom:APPROVE_EXPENSE_DETAILS ParameterBag:parameterBag];
			}
			else 
			if([nsAction isEqualToString:@"Attendees"])
			{
				LOG_IF(3, NSLog(@"ApproveEntriesViewController:didSelectRowAtIndexPath sending %@", parameterBag));
				[rootViewController switchToView:APPROVE_VIEW_ATTENDEES viewFrom:APPROVE_EXPENSE_DETAILS ParameterBag:parameterBag];
			}
		}
	}
}

@end
