//
//  HotelDetailsVariableHeightCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelDetailsVariableHeightCell.h"
#import "ExSystem.h" 


@implementation HotelDetailsVariableHeightCell


@synthesize nameLabel;
@synthesize descriptionLabel;

#define FONT_SIZE 13.0f
#define DESCRIPTION_LABEL_X 85.0f
#define DESCRIPTION_LABEL_BUFFER 14.0f
#define DESCRIPTION_LABEL_MINIMUM_HEIGHT 38.0f
#define DESCRIPTION_LABEL_VERTICAL_MARGIN 4.0f
#define DISCLOSURE_WIDTH_ALLOWANCE 20.0f

// Set GROUPED_TABLE_BUFFER to 0.0f if you are not using a grouped table.
#define GROUPED_TABLE_BUFFER 20.0f

NSString * const FONT_NAME = @"Helvetica";

NSString * const HOTEL_DETAILS_VARIABLE_HEIGHT_CELL_IDENTIFIER = @"HOTEL_DETAILS_VARIABLE_HEIGHT_CELL_IDENTIFIER";


+(CGFloat)calculateCellHeight:(UITableView*)tableView hideCellLabel:(BOOL)hideCellLabel cellValue:(NSString*)val allowDisclosure:(BOOL)allowDisclosure
{
	int descriptionLabelWidth;
	
	if (hideCellLabel)
	{
		descriptionLabelWidth = (tableView.frame.size.width - GROUPED_TABLE_BUFFER) - (2 * DESCRIPTION_LABEL_BUFFER);
	}
	else
	{
		descriptionLabelWidth = (tableView.frame.size.width - GROUPED_TABLE_BUFFER) - DESCRIPTION_LABEL_X - DESCRIPTION_LABEL_BUFFER;
	}
	
	if (allowDisclosure)
		descriptionLabelWidth -= DISCLOSURE_WIDTH_ALLOWANCE;

	CGSize constraint = CGSizeMake(descriptionLabelWidth, 20000.0f);
	CGSize size = [val sizeWithFont:[UIFont fontWithName:FONT_NAME size:FONT_SIZE] constrainedToSize:constraint lineBreakMode:NSLineBreakByWordWrapping];
	CGFloat height = MAX(DESCRIPTION_LABEL_MINIMUM_HEIGHT, size.height);
	return height + (DESCRIPTION_LABEL_VERTICAL_MARGIN * 2);
}


+(HotelDetailsVariableHeightCell*)makeCell:(UITableView*)tableView owner:owner cellLabel:(NSString*)label cellValue:(NSString*)val allowDisclosure:(BOOL)allowDisclosure
{
	BOOL hideCellLabel = (label == nil);
	
	HotelDetailsVariableHeightCell *cell = (HotelDetailsVariableHeightCell *)[tableView dequeueReusableCellWithIdentifier: HOTEL_DETAILS_VARIABLE_HEIGHT_CELL_IDENTIFIER];

	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelDetailsVariableHeightCell" owner:self options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[HotelDetailsVariableHeightCell class]])
			{
				cell = (HotelDetailsVariableHeightCell *)oneObject;
				break;
			}
		}
	}
	
	// Reset cell content properties
	cell.nameLabel.hidden = NO;
	cell.descriptionLabel.textColor = [UIColor blackColor];
	cell.accessoryType = UITableViewCellAccessoryNone;
	
	// Adjust cell size
	int x;
	int descriptionLabelWidth;
	
	if (hideCellLabel)
	{
		x = DESCRIPTION_LABEL_BUFFER;
		descriptionLabelWidth = (tableView.frame.size.width - GROUPED_TABLE_BUFFER) - (2 * DESCRIPTION_LABEL_BUFFER);
	}
	else
	{
		x = DESCRIPTION_LABEL_X;
		descriptionLabelWidth = (tableView.frame.size.width - GROUPED_TABLE_BUFFER) - DESCRIPTION_LABEL_X - DESCRIPTION_LABEL_BUFFER;
	}
	
	if (allowDisclosure)
		descriptionLabelWidth -= DISCLOSURE_WIDTH_ALLOWANCE;
	
	if([UIDevice isPad])
		descriptionLabelWidth = 200;// descriptionLabelWidth - 25;

	CGFloat descriptionLabelHeight = [HotelDetailsVariableHeightCell calculateCellHeight:tableView hideCellLabel:hideCellLabel cellValue:val allowDisclosure:allowDisclosure] - (DESCRIPTION_LABEL_VERTICAL_MARGIN * 2);
	cell.descriptionLabel.frame = CGRectMake(x, DESCRIPTION_LABEL_VERTICAL_MARGIN, descriptionLabelWidth, descriptionLabelHeight);
	
	// Set cell content text
	cell.descriptionLabel.text = val;
	
	if (hideCellLabel)
		cell.nameLabel.hidden = YES;
	else
		cell.nameLabel.text = label;
	
	return cell;
}

// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return HOTEL_DETAILS_VARIABLE_HEIGHT_CELL_IDENTIFIER;
}




@end
