//
//  ApproveReportsCell.m
//  ConcurMobile
//
//  Created by Yuri on 2/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportExpenseCell.h"
#import "ApproveExpenseDetailViewController.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"

static int traceLevel = 2;

#define LOG_IF(level, x) { if(level<=traceLevel) x; }

@implementation ApproveReportExpenseCell

@synthesize labelExpense;

@synthesize labelName;
@synthesize labelTotal;
@synthesize labelDate;
@synthesize listIcon;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
	
	// Icon1: 199.0 35.0 24.0 24.0
	// Icon2: 227.0 35.0 24.0 24.0
	// Icon3: 255.0 35.0 24.0 24.0
	
    return self;
}

//- (NSString *) reuseIdentifier {
//	return @"ApproveReportExpenseCell";
//}

-(NSString*) vendorForRowData:(NSDictionary *)rowData
{
	NSEnumerator *enumFields = [[rowData objectForKey:@"Fields"] objectEnumerator];
	NSDictionary* aField = nil;
	
	while(aField = [enumFields nextObject]) 
	{
		if([[aField objectForKey:@"Id"] isEqualToString:@"VenLiKey"])
			return [aField objectForKey:@"Value"];
	}
	
	return nil;
}

-(NSString*) locationForRowData:(NSDictionary *)entryData
{
	NSString* location = [entryData objectForKey:@"LocationName"];
	if(location != nil)
	{
		if([location rangeOfString:@", "].location != NSNotFound)
			location = [[location componentsSeparatedByString:@", "] objectAtIndex:0];
		
		NSString* vendor = [self vendorForRowData:entryData];
		if(vendor != nil)
			return [NSString stringWithFormat:@"%@, %@", [self vendorForRowData:entryData], location]; 
	}

	return location;
}

- (id) initWithData:(NSDictionary*) entryData
{
	LOG_IF(2, NSLog(@"ApproveReportExpenseCell:initWithData, entryData: %@ ", entryData)); 
	
	labelName.text = [entryData objectForKey:@"ExpName"]; 
	labelExpense.text = [self locationForRowData:entryData]; 
	labelTotal.text = [FormatUtils formatMoney:[entryData objectForKey:@"TransactionAmount"]
											crnCode:[entryData objectForKey:@"TransactionCrnCode"]];
	labelDate.text = [DateTimeFormatter formatDateMedium:[entryData objectForKey:@"TransactionDate"]];
	
	NSString* hasException = [entryData objectForKey:@"HasExceptions"];
	NSString* hasComments = [entryData objectForKey:@"HasComments"];
	NSString* hasCreditCard = [entryData objectForKey:@"IsCreditCardCharge"];
	NSString* hasItemization = [entryData objectForKey:@"IsItemized"];
	NSString* hasAttendees = [entryData objectForKey:@"HasAttendees"];
	
	LOG_IF(2, NSLog(@"Flags for expense %@ - %@, %@, %@, %@, %@", labelName.text, 
		  hasException, hasComments, hasCreditCard, hasItemization, hasAttendees)); 
	
	if(listIcon == nil)
		listIcon = [[NSMutableArray alloc] init];
	else 
		[listIcon removeAllObjects];
	
	CGRect iconFrame = CGRectMake(255.0, 35.0, 24.0, 24.0); // y is 255 plus 27
	UIImageView* curIcon = nil;
	if (hasException != nil && [hasException isEqualToString:@"Y"])
	{
		curIcon = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"warning_24X24_PNG.png"]];
		curIcon.frame = iconFrame;
		[listIcon addObject:curIcon];
		[curIcon release];
		
		iconFrame.origin.x = iconFrame.origin.x -27.0; 
	}
	
	if (hasComments != nil && [hasComments isEqualToString:@"Y"])
	{
		curIcon = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"comments_24X24_PNG.png"]];
		curIcon.frame = iconFrame;
		[listIcon addObject:curIcon];
		[curIcon release];

		iconFrame.origin.x = iconFrame.origin.x -27.0; 
	}
	
	if (hasCreditCard != nil && [hasCreditCard isEqualToString:@"Y"])
	{
		curIcon = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"amex_24X24.png"]];
		curIcon.frame = iconFrame;
		[listIcon addObject:curIcon];
		[curIcon release];

		iconFrame.origin.x = iconFrame.origin.x -27.0; 
	}
	
	if (hasItemization != nil && [hasItemization isEqualToString:@"Y"])
	{
		curIcon = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"itemization_24X24_PNG.png"]];
		curIcon.frame = iconFrame;
		[listIcon addObject:curIcon];
		[curIcon release];

		iconFrame.origin.x = iconFrame.origin.x -27.0; 
	}
	
	if (hasAttendees != nil && [hasAttendees isEqualToString:@"Y"])
	{
		curIcon = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"attendees_24X24_PNG.png"]];
		curIcon.frame = iconFrame;
		[listIcon addObject:curIcon];
		[curIcon release];

		iconFrame.origin.x = iconFrame.origin.x -27.0; 
	}
	
	NSEnumerator *enumIcons = [listIcon objectEnumerator];
	curIcon = nil;
	while(curIcon = [enumIcons nextObject]) 
		[self addSubview:curIcon];
	
	return self;
}

- (void)dealloc {

	[labelExpense release];
	[labelName release];
	[labelTotal release];
	[labelDate release];
	
	[listIcon release];
    [super dealloc];
}


@end
