//
//  BookingLabelValueCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/26/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "BookingLabelValueCell.h"


@implementation BookingLabelValueCell

@synthesize labelHead;
@synthesize labelLabel;
@synthesize labelValue;

NSString * const BOOKING_LABEL_CELL_REUSABLE_IDENTIFIER = @"BOOKING_LABEL_CELL_REUSABLE_IDENTIFIER";


// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return BOOKING_LABEL_CELL_REUSABLE_IDENTIFIER;
}

+(BookingLabelValueCell*)makeHeaderCell:(UITableView*)tableView owner:(id)owner header:(NSString*)headerText
{
	BookingLabelValueCell *cell = [self makeEmptyCell:tableView owner:owner];
	
	[cell.labelHead setHidden:NO];

	[cell setAccessoryType:UITableViewCellAccessoryNone];
	
	cell.labelHead.text = headerText; 
	//[cell setBackgroundColor:[UIColor colorWithRed:0.0f green:(109.0f / 255.0f) blue:(217.0f /255.0f) alpha:1.0f]];
	
	//[cell.labelHead setShadowColor:[UIColor grayColor]];
	[cell.labelHead setTextColor:[UIColor blackColor]];
	//[cell.labelHead setShadowOffset:CGSizeMake(-1.0, -1.0)];
	cell.labelHead.frame = CGRectMake(10, 5, cell.labelHead.frame.size.width, cell.labelHead.frame.size.height);
	return cell;
}

+(BookingLabelValueCell*)makeCell:(UITableView*)tableView owner:(id)owner label:(NSString*)labelText value:(NSString*)valueText;
{
	BookingLabelValueCell *cell = [self makeEmptyCell:tableView owner:owner];
	
	[cell.labelLabel setHidden:NO];
	[cell.labelValue setHidden:NO];

	cell.labelLabel.text = labelText;
	cell.labelValue.text = valueText;
	
	return cell;
}

+(BookingLabelValueCell*)makeEmptyCell:(UITableView*)tableView owner:(id)owner
{
	BookingLabelValueCell *cell = (BookingLabelValueCell *)[tableView dequeueReusableCellWithIdentifier: BOOKING_LABEL_CELL_REUSABLE_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BookingLabelValueCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[BookingLabelValueCell class]])
			{
				cell = (BookingLabelValueCell *)oneObject;
				break;
			}
		}
	}
	
	[cell.labelHead setHidden:YES];
	[cell.labelLabel setHidden:YES];
	[cell.labelValue setHidden:YES];

	[cell setBackgroundColor:[UIColor whiteColor]];

	return cell;
}



@end
