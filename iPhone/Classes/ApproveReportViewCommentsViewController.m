//
//  ApproveReportViewCommentsViewController.m
//  ConcurMobile
//
//  Created by Yuri Kiryanov on 3/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportViewCommentsViewController.h"
#import "ApproveReportExpenseDetailCell.h"
#import "RootViewController.h"
#import "FormatUtils.h"

static int traceLevel = 2;
#define LOG_IF(level, x) { if(level<=traceLevel) x; }

@implementation ApproveReportViewCommentsViewController

@synthesize txtEmployee;
@synthesize txtTotal;
@synthesize txtName;

@synthesize tableView;

-(NSString *)getViewIDKey
{
	return APPROVE_VIEW_COMMENTS;
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
		
		txtEmployee.text = [parameterBag objectForKey:@"EmployeeName"];
		txtName.text = [parameterBag objectForKey:@"ReportName"];
		txtTotal.text = [FormatUtils formatMoney:[parameterBag objectForKey:@"TotalClaimedAmount"] 
										 crnCode:[parameterBag objectForKey:@"CrnCode"]];
		
		[self.tableView reloadData];
	}
}

- (NSInteger) getCommentCount
{
	NSArray *rowComments = [parameterBag objectForKey:@"Comments"];
	if(rowComments != nil)
		return [rowComments count];
	
	return 0;   
}

- (NSDictionary *) getRowCommentData:(NSUInteger) row
{
	NSDictionary *rowData = [[parameterBag objectForKey:@"Comments"] objectAtIndex:row];
	//NSLog(@"ApproveReportViewCommentsViewController:getRowCommentData: %@", rowData);
	
	return rowData;
}

- (void)viewDidLoad 
{
    [super viewDidLoad];
	
	self.title =  [rootViewController getLocalizedText:
				   [self getViewIDKey] LocalConstant:[[self getViewIDKey] stringByAppendingString:@"_VIEW_TITLE"]];
	//NSLog(@"ApproveReportViewCommentsViewController::title set to: %@", self.title );	
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

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
     return [self getCommentCount];
}


// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
	return @"";
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
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
		
//		// Style2 cell
//		static NSString *MyCellIdentifier = @"CellStyleValue2";
//		
//		//just the header
//		UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:MyCellIdentifier];
//		if (cell == nil)  
//			cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue2 reuseIdentifier:MyCellIdentifier] autorelease]; 
//		
//		cell.textLabel.numberOfLines = 4; // 4 is line count
//		cell.textLabel.font = [UIFont systemFontOfSize:12.0];
//		cell.textLabel.textAlignment = UITextAlignmentLeft;
//		
//		cell.detailTextLabel.numberOfLines = 0; // unlimited line count
//		cell.detailTextLabel.font = [UIFont systemFontOfSize:14.0];
//		cell.detailTextLabel.textAlignment = UITextAlignmentLeft;
//		
//		cell.textLabel.text = @"";
//		cell.detailTextLabel.text = [[self getRowCommentData:row] objectForKey:@"Comment"];
		
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
		cell.labelDetail.text = [[self getRowCommentData:row] objectForKey:@"Comment"];
		
		return cell;
	}
	
	//NSLog(@"ApproveReportViewCommentsViewController::cellForRowAtIndexPath error, returns NIL cell at section: %d, row: %d", indexPath.section, indexPath.row);
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
	
	[txtEmployee release];
	[txtTotal release];
	[txtName release];
	
	[tableView release];
    [super dealloc];
}


@end

