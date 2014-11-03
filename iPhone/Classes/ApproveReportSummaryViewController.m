//
//  ApproveReportSummaryViewController.m
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportSummaryViewController.h"
#import "ApproveReportExpenseDetailCell.h"
#import "RootViewController.h"
#import "FormatUtils.h"

static int traceLevel = 2;
#define LOG_IF(level, x) { if(level<=traceLevel) x; }

// #define kDisbursements

#define		kTitle				@"title"
#define		kIcon				@"icon"
#define		kRowCount			@"rowcount"
#define		kRowList			@"rowlist"
#define		kRowHeight			@"rowheight"
#define		kRowId				@"rowid"

@implementation ApproveReportSummaryViewController

@synthesize txtEmployee;
@synthesize txtTotal;
@synthesize txtName;

@synthesize tableView;

@synthesize reportHeaderFields;
@synthesize companyDisbursementFields;
@synthesize employeeDisbursementsFields;
@synthesize listSection;

-(NSString *)getViewIDKey
{
	return APPROVE_REPORT_SUMMARY;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

- (NSInteger) getHeaderFieldCount
{
	if(reportHeaderFields != nil) 
		return [reportHeaderFields count];
	
	return 0;   
}

- (NSDictionary *) getHeaderRowFieldData:(NSUInteger) row
{
	if(row < [reportHeaderFields count])
	{
		NSDictionary *rowData = [reportHeaderFields objectAtIndex:row]; 
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:getRowFieldData: %@", rowData));
		
		return rowData;
	}
	
	LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getRowFieldData failed"));
	return nil;
}

- (NSInteger) getCommentCount
{
	NSArray *comments = [parameterBag objectForKey:@"Comments"];
	if(comments != nil)
		return [comments count];
	
	return 0;   
}

- (NSDictionary *) getCommentData:(NSUInteger) row
{
	NSArray *comments = [parameterBag objectForKey:@"Comments"];
	if(comments != nil)
	{
		if(row < [comments count])
		{
			NSDictionary *comment = [comments objectAtIndex:row]; 
			LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getCommentData: %@", comment));
			
			return comment;
		}
	}
	
	LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getCommentData failed"));
	return nil;
}

- (NSInteger) getCompanyFieldCount
{
	if(companyDisbursementFields != nil) 
		return [companyDisbursementFields count];
	
	return 0;   
}

- (NSDictionary *) getCompanyRowFieldData:(NSUInteger) row
{
	if(row < [companyDisbursementFields count])
	{
		NSDictionary *rowData = [companyDisbursementFields objectAtIndex:row]; 
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:getCompanyRowFieldData: %@", rowData));
		
		return rowData;
	}
	
	LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getCompanyRowFieldData failed"));
	return nil;
}

- (NSInteger) getEmployeeFieldCount
{
	if(employeeDisbursementsFields != nil) 
		return [employeeDisbursementsFields count];
	
	return 0;   
}

- (NSDictionary *) getEmployeeRowFieldData:(NSUInteger) row
{
	if(row < [employeeDisbursementsFields count])
	{
		NSDictionary *rowData = [employeeDisbursementsFields objectAtIndex:row]; 
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:getEmployeeRowFieldData: %@", rowData));
		
		return rowData;
	}
	
	LOG_IF(2, NSLog(@"ApproveExpenseDetailViewController:getEmployeeRowFieldData failed"));
	return nil;
}

-(NSDictionary *) getParameterFor:(NSString*)keyInParamBag:(NSString*)titleSuffix
{
	return [NSDictionary dictionaryWithObjectsAndKeys:
	 [rootViewController getLocalizedText:
	  [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
		stringByAppendingString:titleSuffix]], @"Label",
	 [parameterBag objectForKey:keyInParamBag], @"Value",
	 nil];
}

-(NSDictionary *) getParameterForReportField:(NSString*)keyInParamBag:(NSString*)titleSuffix
{
	return [NSDictionary dictionaryWithObjectsAndKeys:
			[rootViewController getLocalizedText:
			 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
												stringByAppendingString:titleSuffix]], @"Label",
			[[[parameterBag objectForKey:@"Fields"] objectForKey:keyInParamBag] objectForKey:@"Value"], @"Value",
			nil];
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
		
		// Get parameter bag
		parameterBag = msg.parameterBag;
		
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:reportDetailData parameterBag: %@", 
						parameterBag));
		
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
		
		[reportHeaderFields removeAllObjects];
		[companyDisbursementFields removeAllObjects];
		[employeeDisbursementsFields removeAllObjects];
		
		// Building report header section
		NSEnumerator *enumFields = [[parameterBag objectForKey:@"Fields"] objectEnumerator];
		NSDictionary* aField;
		
		while (aField = [enumFields nextObject]) 
		{
			if([[aField objectForKey:@"Id"] isEqualToString:@"ReportId"])
			{
				// Report Id
				[reportHeaderFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:[aField objectForKey:@"Value"], @"Value", 
											   [self.rootViewController getLocalizedText:
												[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
												stringByAppendingString:@"_REPORT_ID"]], @"Label",
												nil]];
			}
			else
			if([[aField objectForKey:@"Id"] isEqualToString:@"PolKey"])
			{
				 [reportHeaderFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:[aField objectForKey:@"Value"], @"Value", 
												[self.rootViewController getLocalizedText:
												 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
												 stringByAppendingString:@"_POLICY"]], @"Label",
												 nil]];
			}
		}
		
		// Other fields
		NSString* nsField;
		nsField = [parameterBag objectForKey:@"TotalPersonalAmount"];
		if(nsField != nil)
		{
			nsField = [FormatUtils formatMoney:nsField crnCode:[parameterBag objectForKey:@"CrnCode"]];
										 
			[reportHeaderFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:nsField, @"Value", 
										   [self.rootViewController getLocalizedText:
											[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
											stringByAppendingString:@"_LESS_PERSONAL_AMOUNT"]], @"Label",
											nil]];
		}
		
		nsField = [parameterBag objectForKey:@"TotalClaimedAmount"];
		if(nsField != nil)
		{
			nsField = [FormatUtils formatMoney:nsField crnCode:[parameterBag objectForKey:@"CrnCode"]];
			
			[reportHeaderFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:nsField, @"Value", 
										   [self.rootViewController getLocalizedText:
											[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
											stringByAppendingString:@"_AMOUNT_CLAIMED"]], @"Label",
											nil]];
		}
			 
		nsField = [parameterBag objectForKey:@"TotalRejectedAmount"];
		if(nsField != nil)
		{
			nsField = [FormatUtils formatMoney:nsField crnCode:[parameterBag objectForKey:@"CrnCode"]];
			
			[reportHeaderFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:nsField,  @"Value", 
										   [self.rootViewController getLocalizedText:
											[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
											stringByAppendingString:@"_AMOUNT_REJECTED"]], @"Label",
											nil]];
		}
			 
		nsField = [parameterBag objectForKey:@"TotalApprovedAmount"];
		if(nsField != nil)
		{
			nsField = [FormatUtils formatMoney:nsField crnCode:[parameterBag objectForKey:@"CrnCode"]];
			
			[reportHeaderFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:nsField, @"Value", 
										   [self.rootViewController getLocalizedText:
											[self getViewIDKey] LocalConstant:[[self getViewIDKey] 
											stringByAppendingString:@"_LESS_AMOUNT_APPROVED"]], @"Label",
											nil]];
		}
			 
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:reportDetailData reportHeaderFields: %@, field: %@", 
						reportHeaderFields));

		// Top section - header 
		NSString* rowCount = [NSString stringWithFormat:@"%d", [reportHeaderFields count]];
		NSString* rowHeight = [NSString stringWithFormat:@"%d", 50];
		
		[listSection addObject:[NSDictionary dictionaryWithObjectsAndKeys:
			rowCount, kRowCount, 
			rowHeight, kRowHeight, 
			@"Header", kRowId,
			 [rootViewController getLocalizedText:
			  [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
				stringByAppendingString:@"_HEADER"]], kTitle,
			nil]]; // no icon

		// Comment section
		if([self getCommentCount])
		{
			rowCount = [NSString stringWithFormat:@"%d", [self getCommentCount]];
			rowHeight = [NSString stringWithFormat:@"%d", 50];
			
			[listSection addObject:[NSDictionary dictionaryWithObjectsAndKeys:
				rowCount, kRowCount, 
				rowHeight, kRowHeight, 
				@"Comments", kRowId, 
				 [rootViewController getLocalizedText:
				  [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
					stringByAppendingString:@"_COMMENTS"]], kTitle,
				nil]]; // no icon
		}	
		
#if kDisbursements		

		// Building company disbursement section
		float totalDueEmployee = 0.0;
		nsField = [parameterBag objectForKey:@"TotalDueEmployee"];
		if(nsField != nil)
		{
			totalDueEmployee = totalDueEmployee + [nsField floatValue];
		}
		
		[companyDisbursementFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:
												[NSString stringWithFormat:@"%.2f", totalDueEmployee],  @"Value", 
											  [self.rootViewController getLocalizedText:
											   [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
												stringByAppendingString:@"_TOTAL_PAID_BY_COMPANY"]], @"Label", 
												nil]];
		
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:reportDetailData companyDisbursementFields: %@, field: %@", 
						companyDisbursementFields));
		
		// Building employee disbursements section
		float totalPaidByEmployee = 0.0;
		nsField = [parameterBag objectForKey:@"TotalDueCompany"];
		if(nsField != nil)
		{
			[employeeDisbursementsFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:nsField,  @"Value", 
													[self.rootViewController getLocalizedText:
													 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
													stringByAppendingString:@"_AMOUNT_DUE_COMPANY"]], @"Label", 
													 nil]];
			
			 totalPaidByEmployee = totalPaidByEmployee + [nsField floatValue];
		}
		
		nsField = [parameterBag objectForKey:@"TotalDueCompanyCard"];
		if(nsField != nil)
		{
			[employeeDisbursementsFields addObject:nsField];
			totalPaidByEmployee = totalPaidByEmployee + [nsField floatValue];
		}
		
		[employeeDisbursementsFields addObject:[NSDictionary dictionaryWithObjectsAndKeys:
												[NSString stringWithFormat:@"%.2f", totalPaidByEmployee],  @"Value", 
												[self.rootViewController getLocalizedText:
												 [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
												stringByAppendingString:@"_TOTAL_PAID_BY_EMPLOYEE"]], @"Label", 
												 nil]];
		
		LOG_IF(2, NSLog(@"ApproveReportSummaryViewController:reportDetailData employeeDisbursementsFields: %@, field: %@", 
						employeeDisbursementsFields));
#endif		
		
		[self.tableView reloadData];
	}
}

- (void)viewDidLoad 
{
    [super viewDidLoad];
	
	self.title =  [rootViewController getLocalizedText:
				   [self getViewIDKey] LocalConstant:[[self getViewIDKey] stringByAppendingString:@"_VIEW_TITLE"]];
	LOG_IF(2, NSLog(@"ApproveReportSummaryViewController::title set to: %@", self.title));

	reportHeaderFields = [[NSMutableArray alloc] init];
	companyDisbursementFields  = [[NSMutableArray alloc] init];
	employeeDisbursementsFields = [[NSMutableArray alloc] init];
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


#pragma mark Table view methods

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

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
	return [[listSection objectAtIndex:section] objectForKey:kTitle];
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Header"])
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
		
		cell.labelText.text = [[self getHeaderRowFieldData:indexPath.row] objectForKey:@"Label"];
		cell.labelDetail.text = [[self getHeaderRowFieldData:indexPath.row] objectForKey:@"Value"];
		
		return cell;
	}
	else
	if([[[listSection objectAtIndex:indexPath.section] objectForKey:kRowId] isEqualToString:@"Comments"])
	{
		static NSString *MyCellIdentifier = @"DefaultCellStyleComment";
		
		//just the header
		UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
		if (cell == nil)  
			cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
					
		cell.textLabel.text = [[self getCommentData:indexPath.row] objectForKey:@"Comment"];
		cell.textLabel.font = [UIFont systemFontOfSize:14.0];
		cell.textLabel.textAlignment = UITextAlignmentLeft;
		
		return cell;
	}
//	else 
//	if(indexPath.section == kReportSummaryCompanyDisbursementSection)
//	{
//		// 1 row for header plus field count
//		if(indexPath.row == 0) // header
//		{
//			static NSString *MyCellIdentifier = @"DefaultCellStyle";
//			
//			//just the header
//			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
//			if (cell == nil)  
//				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
//			
//			cell.textLabel.text = [rootViewController getLocalizedText:
//								   [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
//																	  stringByAppendingString:@"_COMPANYDISBURSEMENT"]];
//			cell.textLabel.font = [UIFont boldSystemFontOfSize:16.0];
//			cell.textLabel.textAlignment = UITextAlignmentLeft;
//			
//			return cell;
//		}
//		else { 
//			NSInteger row = indexPath.row - 1; // Skipping header
//			
//			static NSString *MyCustomCellIdentifier = @"ApproveExpenseDetailCell";
//			
//			ApproveReportExpenseDetailCell *cell = (ApproveReportExpenseDetailCell *)[self.tableView 
//																					  dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];
//			
//			if (cell == nil)  
//			{
//				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveExpenseDetailCell" owner:self options:nil];
//				for (id oneObject in nib)
//					if ([oneObject isKindOfClass:[ApproveReportExpenseDetailCell class]])
//						cell = (ApproveReportExpenseDetailCell *)oneObject;
//			}
//			
//			cell.labelText.text = [[self getCompanyRowFieldData:row] objectForKey:@"Label"];
//			cell.labelDetail.text = [[self getCompanyRowFieldData:row] objectForKey:@"Value"];
//						
//			return cell;
//		}
//	}
//	else 
//	if(indexPath.section == kReportSummaryEmployeeDisbursementsSection)
//	{
//		// 1 row for header plus field count
//		if(indexPath.row == 0) // header
//		{
//			static NSString *MyCellIdentifier = @"DefaultCellStyle";
//			
//			//just the header
//			UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
//			if (cell == nil)  
//				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MyCellIdentifier] autorelease]; 
//			
//			cell.textLabel.text = [rootViewController getLocalizedText:
//								   [self getViewIDKey] LocalConstant:[[self getViewIDKey] 
//																	  stringByAppendingString:@"_EMPLOYEEDISBURSEMENTS"]];
//			cell.textLabel.font = [UIFont boldSystemFontOfSize:16.0];
//			cell.textLabel.textAlignment = UITextAlignmentLeft;
//			
//			return cell;
//		}
//		else { 
//			NSInteger row = indexPath.row - 1; // Skipping header
//			
//			static NSString *MyCustomCellIdentifier = @"ApproveExpenseDetailCell";
//			
//			ApproveReportExpenseDetailCell *cell = (ApproveReportExpenseDetailCell *)[self.tableView 
//																					  dequeueReusableCellWithIdentifier:MyCustomCellIdentifier];
//			
//			if (cell == nil)  
//			{
//				NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"ApproveExpenseDetailCell" owner:self options:nil];
//				for (id oneObject in nib)
//					if ([oneObject isKindOfClass:[ApproveReportExpenseDetailCell class]])
//						cell = (ApproveReportExpenseDetailCell *)oneObject;
//			}
//			
//			cell.labelText.text = [[self getEmployeeRowFieldData:row] objectForKey:@"Label"];
//			cell.labelDetail.text = [[self getEmployeeRowFieldData:row] objectForKey:@"Value"];
//			
//			return cell;
//		}
//	}
//	
	
	//NSLog(@"ApproveExpenseDetailViewController::cellForRowAtIndexPath error, returns NIL cell at section: %d, row: %d", indexPath.section, indexPath.row);
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
	
	[listSection release];
	[reportHeaderFields release];
	[companyDisbursementFields release];
	[employeeDisbursementsFields release];
	
	[txtEmployee release];
	[txtTotal release];
	[txtName release];
	
	[tableView release];

	[super dealloc];
}


@end

