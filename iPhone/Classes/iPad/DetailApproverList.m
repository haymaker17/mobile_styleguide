    //
//  DetailApproverList.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DetailApproverList.h"
#import "KeyValue.h"
#import "DetailApprovalCell.h"
#import "AttendeeData.h"
#import "ExceptionData.h"
#import "FormatUtils.h"

#define kEntryDetails 0
#define kAttendeeDetails 1
#define kSectionComments 2
#define kExceptionDetails 3
#define kItemizeDetail 4
#define kSummary 5

@implementation DetailApproverList
@synthesize			tableList;
@synthesize			listType;
@synthesize			delegate = _delegate;
@synthesize			aRows, aSections;
@synthesize			entry;
@synthesize			report, adjustedHeight, tBar, isSummary, aHeaders, labelFontSize, valueFontSize;
/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

-(void)viewWillAppear:(BOOL)animated
{
	//[self resizePopover];
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	
	//[self resizePopover];
	
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200	
	if(adjustedHeight < 1)
		self.contentSizeForViewInPopover = CGSizeMake(500.0, 400.0);
	else 
		self.contentSizeForViewInPopover = CGSizeMake(500.0, adjustedHeight);
#endif
	if(labelFontSize == 0.0)
		labelFontSize = 13.0f;
	
	if(valueFontSize == 0.0)
		valueFontSize = 13.0f;
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
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


- (void)dealloc {
	self.delegate = nil;
	
	//[report release];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	if(aSections != nil)
		return [aSections count];
	else 
		return 1;
    
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
    if ([aSections count] == 0 && [aRows count] == 0)
        return 0;
    
	if([aRows count] > 0)
		return [aRows count];
	
	NSMutableArray *a = aSections[section];
	if(a == nil)
		return 0;
    return [a count];
	
}




- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
//    int section = [indexPath section];
    int row = [indexPath row];
    KeyValue *kv = nil;
	
	if(aSections == nil)
		kv = aRows[row];
	
	if(aSections != nil)
	{
		NSMutableArray *a = aSections[indexPath.section];
		kv = a[row];
	}
//	else 
//		NSMutableArray *a = [aSections objectAtIndex:section];
	
	if(listType == kEntryDetails || listType == kAttendeeDetails|| listType == kSectionComments || listType == kExceptionDetails
	   || kItemizeDetail == listType || kSummary == listType)
	{
		DetailApprovalCell *cell = (DetailApprovalCell *)[tableView dequeueReusableCellWithIdentifier: @"DetailApprovalCell"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"DetailApprovalCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[DetailApprovalCell class]])
					cell = (DetailApprovalCell *)oneObject;
		}
		
		cell.lblLabel.text = kv.label;
		cell.lblValue.text = kv.val;
		cell.lblSep.hidden = YES;
		
		if(listType == kEntryDetails)
		{
			cell.lblLabel.frame = CGRectMake(11, -2, 163, 42);
			[cell.lblLabel setNumberOfLines:2];
			[cell.lblLabel setLineBreakMode:NSLineBreakByWordWrapping];
		}
		
		if(listType == kSectionComments || listType == kExceptionDetails || listType == kAttendeeDetails || listType == kSummary)
		{
			CGFloat w = 311.0;
			//KeyValue *kv = [aRows objectAtIndex:row];
			NSString *val = kv.val;
			CGFloat height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:valueFontSize];
			float x = 0;
            if(cell.lblValue != nil)
                x = cell.lblValue.frame.origin.x;
			cell.lblValue.frame = CGRectMake(x, 0, w, height);
			[cell.lblValue setNumberOfLines:4];
			
			w = 152.0;
			val = kv.label;
			height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:labelFontSize];
			x = 0;
            if(cell.lblLabel != nil)
                x = cell.lblLabel.frame.origin.x;
			cell.lblLabel.frame = CGRectMake(x, 0, w, height);
			
//			if (listType == kExceptionDetails && [kv.label isEqualToString:@"WARNING"]) {
//				cell.lblLabel.textColor = [UIColor colorWithRed:144.0f green:117.0f blue:20.0f alpha:1.0f];
//			}
		}
	
		
		return cell;
	}

	return nil;
}



#pragma mark -
#pragma mark Table View Delegate Methods
//- (NSString *)tableView:(UITableView *)tableView 
//titleForHeaderInSection:(NSInteger)section
//{
//   // NSDate *key = [aSectionHeaders objectAtIndex:section];
//    //return @"E";
//}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)newIndexPath
{
//	NSUInteger section = [newIndexPath section];
//    NSUInteger row = [newIndexPath row];
}


- (NSIndexPath *)tableView:(UITableView *)tableView 
  willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView reloadData];
    return indexPath;
}

//- (NSInteger)tableView:(UITableView *)tableView 
//sectionForSectionIndexTitle:(NSString *)title 
//               atIndex:(NSInteger)index
//{
//    NSString *key = [keys objectAtIndex:index];
//    if (key == UITableViewIndexSearch)
//    {
//        [tableView setContentOffset:CGPointZero animated:NO];
//        return NSNotFound;
//    }
//    else return index;
//    
//}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
//	NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	
	KeyValue *kv = nil;
	if(aRows != nil && aSections == nil)
		kv = aRows[row];
	else if(aSections != nil)
	{
		NSMutableArray *a = aSections[indexPath.section];
		kv = a[row];
	}
	
	
	if(listType == kSectionComments || listType == kExceptionDetails || listType == kAttendeeDetails || listType == kSummary)
	{
		CGFloat w = 311.0;
		
		NSString *val = kv.val;
		CGFloat height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:valueFontSize];
		
		w = 152.0;
		val = kv.label;
		CGFloat heightLabel =  [FormatUtils getTextFieldHeight:w Text:val FontSize:labelFontSize];

		if(heightLabel > height)
			height = heightLabel;
		
		return height;
	}


	return 44;
}


#pragma mark -
#pragma mark Toolbar Method 
-(void) setToolBarTitle:(NSString *)viewTitle
{
	tBar.frame = CGRectMake(0, 0, tBar.frame.size.width, 30);
	UIBarButtonItem *btnTitle = [[UIBarButtonItem alloc] initWithTitle:viewTitle style:UIBarButtonItemStylePlain target:self action:nil];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = @[flexibleSpace, btnTitle, flexibleSpace];
	[tBar setItems:toolbarItems];
}


#pragma mark -
#pragma mark Data Init Methods
-(void) makeEntryDetails:(EntryData *) e
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"APPROVE_EXPENSE_DETAILS"]];
	entry = e;
	
	aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	if (e.fieldKeys != nil && e.fields != nil) 
	{
		for(int i = 0; i < e.fieldKeys.count; i++)
		{
			NSString* key = (e.fieldKeys)[i];
			FormFieldData *ffd = (e.fields)[key];
			if (ffd != nil)
			{
				KeyValue *kv = [[KeyValue alloc] init];
				kv.label = ffd.label;
				kv.val = ffd.fieldValue;
				[aRows addObject:kv];
			}
		}
	}
	
	[tableList reloadData];
}


-(NSArray *) makeAttendeeDetails:(EntryData *) e
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"Attendees"]];
	
	entry = e;
	
	self.aRows = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableDictionary *sectionData = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	NSMutableArray *aKeys = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray *sections = [[NSMutableArray alloc] initWithObjects:nil];
	
	NSMutableArray *sorter = [[NSMutableArray alloc] init];
	
	for(int i = 0; i < [entry.attendeeKeys count]; i++)
	{
		NSString *key = (entry.attendeeKeys)[i];
		AttendeeData *a = (entry.attendees)[key];
		NSString *name = [a getFullName];	// Do not localize
		NSString *company = [a getNonNullableValueForFieldId:@"Company"];			// Do not localize
		NSString *title = [a getNonNullableValueForFieldId:@"Title"];				// Do not localize
		NSMutableArray *aData = [[NSMutableArray alloc] initWithObjects:name, a.amount, company, title, nil];
		sectionData[key] = aData;
		
		NSMutableDictionary *sortVals = [[NSMutableDictionary alloc] initWithObjectsAndKeys:name, @"ANAME", a.attnKey, @"KEY", nil];
		[sorter addObject:sortVals];
	}
	
	//need to sort the attendees
	
	NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"ANAME" ascending:YES];
	[sorter sortUsingDescriptors:@[aSortDescriptor]];
	for(int i = 0; i < [sorter count]; i++)
	{
		NSMutableDictionary *sortVals = sorter[i];
		[aKeys addObject: sortVals[@"KEY"]];
		[sections addObject: sortVals[@"ANAME"]];
	}
	
	if ([aKeys count] > 0) 
	{
		for(int i = 0; i < [aKeys count]; i++)
		{
			NSString *key = aKeys[i];
			AttendeeData *a = (entry.attendees)[key];
			if (a != nil)
			{
				KeyValue *kv = [[KeyValue alloc] init];
				kv.label = [a getNonNullableValueForFieldId:@"AtnTypeName"];
				NSMutableString *val = [[NSMutableString alloc] init];
				[val appendString: [a getFullName]];
				
				NSString *title = [a getNullableValueForFieldId:@"Title"];
				NSString *company = [a getNullableValueForFieldId:@"Company"];
				
				if(title != nil)
					[val appendString:[NSString stringWithFormat:@"\n%@", title]];
				
				if(company != nil)
					[val appendString:[NSString stringWithFormat:@"\n%@",company]];
				
				if(a.amount != nil)
					[val appendString:[NSString stringWithFormat:@"\n%@", [FormatUtils formatMoney:a.amount crnCode:e.transactionCrnCode]]];

				kv.val = val;
				[aRows addObject:kv];
			}
		}
	}
	
	
	[tableList reloadData];
	
	return aRows;
}


-(void) makeCommentDetails:(EntryData *) e
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"Comments"]];
	
	entry = e;
	
	aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(NSString *key in entry.comments)
	{
		CommentData *c = (entry.comments)[key];
		NSString *dt = [CCDateUtilities formatDateToMMMddYYYFromString:c.creationDate];
		NSString *commentHeader = [NSString stringWithFormat:@"%@ - %@", dt, c.commentBy];
		
		KeyValue *kv = [[KeyValue alloc] init];
		kv.label = commentHeader;
		kv.val = c.comment;
		[aRows addObject:kv];

	}

	[tableList reloadData];
}

-(void) makeCommentDetailsReport:(ReportData *) r
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"Report Comments"]];
	
	report = r;
	
	aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(NSString *key in r.comments)
	{
		CommentData *c = (r.comments)[key];
		NSString *dt = [CCDateUtilities formatDateToMMMddYYYFromString:c.creationDate];
		NSString *commentHeader = [NSString stringWithFormat:@"%@ - %@", dt, c.commentBy];
		
		KeyValue *kv = [[KeyValue alloc] init];
		kv.label = commentHeader;
		kv.val = c.comment;
		[aRows addObject:kv];
		
	}
	
	[tableList reloadData];
}


-(void) makeExceptionDetailsReport:(ReportData *) r
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"Exception Details"]];
	
	report = r;
	
	aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(int i = 0; i < [report.exceptions count]; i++)
	{
		ExceptionData *ex = (report.exceptions)[i];
		//NSString *val = [NSString stringWithFormat:@"%@ - %@", e.severityLevel, e.exceptionsStr];
		
		KeyValue *kv = [[KeyValue alloc] init];
		kv.label = ex.severityLevel;
		kv.val = ex.exceptionsStr;
		[aRows addObject:kv];
		
	}
	
	[tableList reloadData];
}

-(void) makeExceptionDetails:(EntryData *) e
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"Exception Details"]];
	
	entry = e;
	
	aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(int i = 0; i < [entry.exceptions count]; i++)
	{
		ExceptionData *ex = (entry.exceptions)[i];
		//NSString *val = [NSString stringWithFormat:@"%@ - %@", e.severityLevel, e.exceptionsStr];
		
		KeyValue *kv = [[KeyValue alloc] init];
		kv.label = ex.severityLevel;
		kv.val = ex.exceptionsStr;
		[aRows addObject:kv];
		
	}
	
	[tableList reloadData];
}

-(void) makeItemizationDetails:(EntryData *) e
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"EXPENSE_DETAILS_ITEMIZATIONS"]];
	
	entry = e;
	
	aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(int i = 0; i < [entry.itemKeys count]; i++)
	{
		NSString *key = (entry.itemKeys)[i];
		EntryData *item = (entry.items)[key];
		NSString *amt = [FormatUtils formatMoney:item.transactionAmount crnCode:item.transactionCrnCode];
		
		KeyValue *kv = [[KeyValue alloc] init];
		kv.label = item.expName;
		kv.val = amt;
		[aRows addObject:kv];
		
	}
	
	[tableList reloadData];
}

-(void) makeSummaryReport:(ReportData *) r
{
	[self setToolBarTitle:[Localizer getLocalizedText:@"Report Summary"]];
	
	report = r;
	
	isSummary = YES;
	
	self.aRows = [[NSMutableArray alloc] initWithObjects:nil];
	self.aSections = [[NSMutableArray alloc] initWithObjects:nil];
	self.aHeaders = [[NSMutableArray alloc] initWithObjects:nil];
	
	
	[aHeaders addObject:[Localizer getLocalizedText:@"Report Header"]];
	if (r.fieldKeys != nil && r.fields != nil) 
	{
		for(int i = 0; i < r.fieldKeys.count; i++)
		{
			NSString* key = (r.fieldKeys)[i];
			FormFieldData *ffd = (r.fields)[key];
			if (ffd != nil)
			{
				KeyValue *kv = [[KeyValue alloc] init];
				//[headerLabelData addObject:ffd.label];
				kv.label = ffd.label;
				kv.val = ffd.fieldValue;
				[aRows addObject:kv];
			}
		}
	}
	
	[aSections addObject:aRows];
	self.aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	[aHeaders addObject:[Localizer getLocalizedText:@"Company"]];
	KeyValue *kv = [[KeyValue alloc] init];
	kv.label = [Localizer getLocalizedText:@"Total Due Employee"];
	kv.val = [FormatUtils formatMoney:r.totalDueEmployee crnCode:r.crnCode] ;
	[aRows addObject:kv];
	
	kv = [[KeyValue alloc] init];
	kv.label = [Localizer getLocalizedText:@"Total Due Company Card"];
	kv.val = [FormatUtils formatMoney:r.totalDueCompanyCard crnCode:r.crnCode];
	[aRows addObject:kv];
	
	kv = [[KeyValue alloc] init];
	kv.label = [Localizer getLocalizedText:@"Total Paid by Company"];
	kv.val = [FormatUtils formatMoney:r.totalPaidByCompany crnCode:r.crnCode];
	[aRows addObject:kv];
	
	[aSections addObject:aRows];
	self.aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	
	[aHeaders addObject:[Localizer getLocalizedText:@"EMPLOYEE"]];
	//Employee
	kv = [[KeyValue alloc] init];
	kv.label = [Localizer getLocalizedText:@"Total Owed by Employee"];
	kv.val = [FormatUtils formatMoney:r.totalOwedByEmployee crnCode:r.crnCode];
	[aRows addObject:kv];
	
	kv = [[KeyValue alloc] init];
	kv.label = [Localizer getLocalizedText:@"Total Due Company Card"];
	kv.val = [FormatUtils formatMoney:r.totalDueCompanyCard crnCode:r.crnCode];
	[aRows addObject:kv];
	
	kv = [[KeyValue alloc] init];
	kv.label = [Localizer getLocalizedText:@"Total Personal Amount"];
	kv.val = [FormatUtils formatMoney:r.totalPersonalAmount crnCode:r.crnCode];
	[aRows addObject:kv];
	
	[aSections addObject:aRows];
	self.aRows = [[NSMutableArray alloc] initWithObjects:nil];
	
	

	
//	NSMutableArray *commentData = [[NSMutableArray alloc] initWithObjects:nil];
//	NSMutableArray *sorter = [[NSMutableArray alloc] init];
//	if (r.comments != nil) 
//	{
//		for(NSString *key in r.comments)
//		{
//			CommentData *c = [r.comments objectForKey:key];
//			
//			NSMutableDictionary *sortVals = [[NSMutableDictionary alloc] initWithObjectsAndKeys:c.creationDate, @"DATE", c.commentKey, @"KEY", nil];
//			[sorter addObject:sortVals];
//			[sortVals release];
//		}
//	}
//	NSSortDescriptor *aSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"DATE" ascending:YES];
//	[sorter sortUsingDescriptors:[NSArray arrayWithObject:aSortDescriptor]];
//	for(int i = 0; i < [sorter count]; i++)
//	{
//		NSMutableDictionary *sortVals = [sorter objectAtIndex:i];
//		NSString *key = [sortVals objectForKey:@"KEY"];
//		CommentData *c = [r.comments objectForKey:key];
//		[commentData addObject:c];
//	}
//	
//	[aSortDescriptor release];
//	[sorter release];
//	
//	for(int i = 0; i < [commentData count]; i++)
//	{
//		CommentData *c = [commentData objectAtIndex:i];
//		KeyValue *kv = [[KeyValue alloc] init];
//		kv.label = c.creationDate;
//		kv.val = c.comment;
//		[aRows addObject:kv];
//		[kv release];
//	}
//	
//	[commentData release];
		
	[tableList reloadData];
}


- (NSString *)tableView:(UITableView *)tableView 
titleForHeaderInSection:(NSInteger)section
{
    
    return aHeaders[section];
}

-(void)resizePopover
{
	
	if([aRows count] > 0)
	{
		CGFloat runningHeight = 0.0;
		
		for(KeyValue *kv in aRows)
		{
			if(listType == kSectionComments || listType == kExceptionDetails || listType == kAttendeeDetails || listType == kSummary)
			{
				CGFloat w = 311.0;
				
				NSString *val = kv.val;
				CGFloat height =  [FormatUtils getTextFieldHeight:w Text:val FontSize:valueFontSize];
				
				w = 152.0;
				val = kv.label;
				CGFloat heightLabel =  [FormatUtils getTextFieldHeight:w Text:val FontSize:labelFontSize];
				
				if(heightLabel > height)
					height = heightLabel;
				
				runningHeight = runningHeight + height;
			}
		}
		
		float screenW = self.view.frame.size.width;
		float screenH = self.view.frame.size.height;

		if(runningHeight < screenH && runningHeight > 0.0)
		{
			self.view.frame= CGRectMake(0, 0, screenW, runningHeight);
			self.contentSizeForViewInPopover = CGSizeMake(screenW, runningHeight);
			self.adjustedHeight = runningHeight;
		}
	}
}
@end
