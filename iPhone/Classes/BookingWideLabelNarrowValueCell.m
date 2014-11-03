//
//  BookingWideLabelNarrowValueCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "BookingWideLabelNarrowValueCell.h"


@implementation BookingWideLabelNarrowValueCell

@synthesize labelLabel;
@synthesize labelValue;

NSString * const BOOKING_WIDE_LABEL_NARROW_VALUE_CELL_REUSABLE_IDENTIFIER = @"BOOKING_WIDE_LABEL_NARROW_VALUE_CELL_REUSABLE_IDENTIFIER";


// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return BOOKING_WIDE_LABEL_NARROW_VALUE_CELL_REUSABLE_IDENTIFIER;
}

+(BookingWideLabelNarrowValueCell*)makeCell:(UITableView*)tableView owner:(id)owner label:(NSString*)labelText value:(NSString*)valueText;
{
	BookingWideLabelNarrowValueCell *cell = [self makeEmptyCell:tableView owner:owner];
	
	cell.labelLabel.text = labelText;
	cell.labelValue.text = valueText;
	
	return cell;
}

+(BookingWideLabelNarrowValueCell*)makeEmptyCell:(UITableView*)tableView owner:(id)owner
{
	BookingWideLabelNarrowValueCell *cell = (BookingWideLabelNarrowValueCell *)[tableView dequeueReusableCellWithIdentifier: BOOKING_WIDE_LABEL_NARROW_VALUE_CELL_REUSABLE_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"BookingWideLabelNarrowValueCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[BookingWideLabelNarrowValueCell class]])
			{
				cell = (BookingWideLabelNarrowValueCell *)oneObject;
				break;
			}
		}
	}
	
	return cell;
}



@end
