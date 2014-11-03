//
//  ApproveReportViewExceptionsViewController.m
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportViewExceptionsViewController.h"
#import "ApproveReportExpenseDetailCell.h"
#import "RootViewController.h"
#import "FormatUtils.h"

static int traceLevel = 2;
#define LOG_IF(level, x) { if(level<=traceLevel) x; }

#define kReportLevelExceptionsSection		0
#define kExpenseHeaderSection				1
#define kExpenseLevelExceptionsSection		2

@implementation ApproveReportViewExceptionsViewController

@synthesize txtTotal, txtEmployee, txtName, tableView;
@synthesize listExpenseExceptions;

-(NSString *)getViewIDKey
{
	return APPROVE_VIEW_EXCEPTIONS;
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
		
		LOG_IF(2, NSLog(@"ApproveReportViewExceptionsViewController:parameterBag: %@", 
						parameterBag));
		
		LOG_IF(2, NSLog(@"ApproveReportViewExceptionsViewController:Report Level Exceptions: %@", 
						[parameterBag objectForKey:@"Exceptions"]
			   ));
		
		// Collect expense exceptions
		[listExpenseExceptions removeAllObjects];
		
		NSEnumerator *enumEntries = [[parameterBag objectForKey:@"Entries"] objectEnumerator];
		NSDictionary* anEntry;
		
		while (anEntry = [enumEntries nextObject]) 
		{
			// Build array of expense exceptions
			NSEnumerator *enumExceptions = [[anEntry objectForKey:@"Exceptions"] objectEnumerator];
			NSDictionary* anException = nil;

			NSMutableDictionary* dictEntry = [[NSMutableDictionary alloc] init]; 
			while(anException = [enumExceptions nextObject])
			{
				[dictEntry setObject:[anException objectForKey:@"ExceptionsStr"] forKey:@"Value"];
			}
				 
			// If an exception not nil, add other name and amount
			if([dictEntry objectForKey:@"Value"] != nil)
			{
				[dictEntry setObject:[anEntry objectForKey:@"ExpName"] forKey:@"ExpName"];
				[dictEntry setObject:[anEntry objectForKey:@"TransactionAmount"] forKey:@"TransactionAmount"];
				
				// Add to list
				[listExpenseExceptions addObject:dictEntry];
				[dictEntry release]; 
			}
		}
		
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:listExpenseExceptions: %@", 
					listExpenseExceptions));
				 
		txtEmployee.text = [parameterBag objectForKey:@"EmployeeName"];
		txtName.text = [parameterBag objectForKey:@"ReportName"];
		txtTotal.text = [FormatUtils formatMoney:[parameterBag objectForKey:@"TotalClaimedAmount"] 
										 crnCode:[parameterBag objectForKey:@"CrnCode"]];
		
		[self.tableView reloadData];
	}
}

- (void)viewDidLoad 
{
    [super viewDidLoad];
	
	self.title =  [rootViewController getLocalizedText:
				   [self getViewIDKey] LocalConstant:[[self getViewIDKey] stringByAppendingString:@"_VIEW_TITLE"]];
	NSLog(@"ApproveReportViewExceptionsViewController::title set to: %@", self.title );

	listExpenseExceptions = [[NSMutableArray alloc] init];
}

/*
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}
*/
/*
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}
*/
/*
- (void)viewWillDisappear:(BOOL)animated {
	[super viewWillDisappear:animated];
}
*/
/*
- (void)viewDidDisappear:(BOOL)animated {
	[super viewDidDisappear:animated];
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

- (NSInteger) getReportExceptionCount
{
	NSArray *rowExceptions = [parameterBag objectForKey:@"Exceptions"];
	if(rowExceptions != nil) 
		return [rowExceptions count];
	
	return 0;   
}

- (NSDictionary *) getRowReportExceptionData:(NSUInteger) row
{
	NSArray *rowExceptions = [parameterBag objectForKey:@"Exceptions"];
	if(rowExceptions != nil)
	{
		if(row < [rowExceptions count])
		{
			NSDictionary *rowData = [[parameterBag objectForKey:@"Exceptions"] objectAtIndex:row];
			LOG_IF(2, NSLog(@"ApproveReportViewExceptionsViewController:getRowReportExceptionData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveReportViewExceptionsViewController:getRowExceptionData failed"));
	return nil;
}

// Expense level exceptions
- (NSInteger) getExpenseExceptionCount
{
	if(listExpenseExceptions != nil) 
		return [listExpenseExceptions count];
	
	return 0;   
}

- (NSDictionary *) getRowExpenseExceptionData:(NSUInteger) row
{
	if(listExpenseExceptions != nil)
	{
		if(row < [listExpenseExceptions count])
		{
			NSDictionary *rowData = [listExpenseExceptions objectAtIndex:row]; 
			LOG_IF(2, NSLog(@"ApproveReportViewExceptionsViewController:getRowExpenseExceptionData: %@", rowData));
			
			return rowData;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveReportViewExceptionsViewController:getRowExpenseExceptionData failed"));
	return nil;
}

#pragma mark Table view methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    
	NSInteger count = 1; // 1 for Report Level Section
	
	count = count + 1; // 1 for "Expenses" header
	if([self listExpenseExceptions] > 0)
		count = count + [self getExpenseExceptionCount]; // [listExpenseExceptions count] for expense exceptions

	return count; 	
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
	if(section == kReportLevelExceptionsSection)
	{
		NSInteger count = [self getReportExceptionCount];
		if(count)
			return count; // exception count
	}
	else
	if(section == kExpenseHeaderSection)
	{
		return 0; // Just header
	}
	else
	if(section == kExpenseLevelExceptionsSection)
	{
		NSInteger count = [self getExpenseExceptionCount];
		if(count)
			return 1 + count; // 1 row for header plus field count
	}
	
	return 0;
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if(indexPath.section == kReportLevelExceptionsSection)
	{
		return 50.0; 
	}
	else
	if(indexPath.section == kExpenseHeaderSection)
	{
		return 50.0; 
	}
	else
	if(indexPath.section == kExpenseLevelExceptionsSection)
	{
		return 50.0; 
	}
	
	// Else all for 50.0
	return 50.0;
}

- (NSString *) getTitleForHeaderInSection:(NSInteger)section
{
	if(section == kReportLevelExceptionsSection)
	{
		return [self.rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
		stringByAppendingString:@"_REPORT_LEVEL_EXCEPTION_SECTION_HEADER"]];
	}
	else
	if(section == kExpenseHeaderSection)
	{
		return [self.rootViewController getLocalizedText:
				[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
		stringByAppendingString:@"_EXPENSE_LEVEL_EXCEPTION_SECTION_HEADER"]];
	}
	else // labels for individual exceptions
	{
		// Two less for section number
		section = section - 2;
		return [NSString stringWithFormat:@"%@\t%@", 
		[[listExpenseExceptions objectAtIndex:section] objectForKey:@"ExpName"]
		,[[listExpenseExceptions objectAtIndex:section] objectForKey:@"TransactionAmount"] ];
	}
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
	// create the parent view that will hold header Label
	UIView* customView = [[UIView alloc] initWithFrame:CGRectMake(10.0, 0.0, 300.0, 44.0)];
	
	// create the button object
	UILabel * headerLabel = [[UILabel alloc] initWithFrame:CGRectZero];
	headerLabel.backgroundColor = [UIColor clearColor];
	headerLabel.opaque = NO;
	
	headerLabel.font = [UIFont boldSystemFontOfSize:18.0];
	headerLabel.frame = CGRectMake(10.0, 0.0, 300.0, 44.0);
	
	headerLabel.text = [self getTitleForHeaderInSection:section]; // i.e. array element
	[customView addSubview:headerLabel];
	
	return customView;
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
	if(indexPath.section == kReportLevelExceptionsSection)
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
				stringByAppendingString:@"_REPORT_LEVEL_EXCEPTION_SECTION_HEADER"]];
			
			cell.textLabel.font = [UIFont boldSystemFontOfSize:16.0];
			cell.textLabel.textAlignment = UITextAlignmentLeft;
			
			return cell;
		}
		else { 
			NSInteger row = indexPath.row - 1; // Skipping header
			
//			// Style2 cell
//			static NSString *MyCellIdentifier = @"CellStyleValue2";
//			
//			//just the header
//			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
//			if (cell == nil)  
//				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:MyCellIdentifier] autorelease]; 
//			
//			cell.textLabel.numberOfLines = 4; // 4 is line count
//			cell.textLabel.font = [UIFont systemFontOfSize:12.0];
//			cell.textLabel.textColor = [UIColor blackColor];
//			cell.textLabel.textAlignment = UITextAlignmentLeft;
//			
//			cell.detailTextLabel.numberOfLines = 0; // No maximum line count
//			cell.detailTextLabel.font = [UIFont systemFontOfSize:14.0];
//			cell.detailTextLabel.textColor = [UIColor blackColor];
//			cell.detailTextLabel.textAlignment = UITextAlignmentLeft;
//			
//			cell.textLabel.text = @"";
//			cell.detailTextLabel.text = [[self getRowReportExceptionData:row] objectForKey:@"Value"];
			
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
			
			cell.labelText.text = @"";
			cell.labelDetail.text = [[self getRowReportExceptionData:row] objectForKey:@"Value"];
			
			return cell;
		}
	}
	else 
		if(indexPath.section >= kExpenseLevelExceptionsSection)
		{
			NSInteger row = indexPath.row - 1; // Skipping header
			
//			// Style2 cell
//			static NSString *MyCellIdentifier = @"CellStyleValue2";
//			
//			//just the header
//			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
//			if (cell == nil)  
//				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:MyCellIdentifier] autorelease]; 
//			
//			cell.textLabel.numberOfLines = 4; // 4 is line count
//			cell.textLabel.font = [UIFont systemFontOfSize:12.0];
//			cell.textLabel.textColor = [UIColor blackColor];
//			cell.textLabel.textAlignment = UITextAlignmentLeft;
//			
//			cell.detailTextLabel.numberOfLines = 0; // No maximum line count
//			cell.detailTextLabel.font = [UIFont systemFontOfSize:14.0];
//			cell.detailTextLabel.textColor = [UIColor blackColor];
//			cell.detailTextLabel.textAlignment = UITextAlignmentLeft;
//			
//			cell.textLabel.text = @"";
//			cell.detailTextLabel.text = [[self getRowExpenseExceptionData:row] objectForKey:@"Value"];
			
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
			cell.labelText.text = @"";
			cell.labelDetail.text = [[self getRowExpenseExceptionData:row] objectForKey:@"Value"];
			
			return cell;
		}
	
	//NSLog(@"ApproveReportViewExceptionsViewController::cellForRowAtIndexPath error, returns NIL cell at section: %d, row: %d", indexPath.section, indexPath.row);
 	return nil;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Navigation logic may go here. Create and push another view controller.
	// AnotherViewController *anotherViewController = [[AnotherViewController alloc] initWithNibName:@"AnotherView" bundle:nil];
	// [self.navigationController pushViewController:anotherViewController];
	// [anotherViewController release];
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/


/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:YES];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/


/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/


/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/


- (void)dealloc {
	
	[listExpenseExceptions release];
	
	[txtEmployee release];
	[txtTotal release];
	[txtName release];
	
	[tableView release];
    [super dealloc];
}


@end

