//
//  IgniteChatterConversationCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterConversationCell.h"


@implementation IgniteChatterConversationCell

@synthesize lblName, lblCompanyName, lblAge, lblLikes, lblText, imgView, imgThumb, imgLink, lblLink, imgFile, lblFile;
@synthesize delegate = _delegate;

NSString * const IGNITE_CHATTER_CONVERSATION_CELL_IDENTIFIER = @"IGNITE_CHATTER_CONVERSATION_CELL_IDENTIFIER";

+(IgniteChatterConversationCell*)makeCell:(UITableView*)tableView owner:(id)owner withDelegate:(id<IgniteChatterConversationCellDelegate>) del
{
	IgniteChatterConversationCell *cell = (IgniteChatterConversationCell *)[tableView dequeueReusableCellWithIdentifier: IGNITE_CHATTER_CONVERSATION_CELL_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteChatterConversationCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[IgniteChatterConversationCell class]])
			{
				cell = (IgniteChatterConversationCell *)oneObject;
				break;
			}
		}
	}
	
    cell.delegate = del;
	return cell;
}

// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return IGNITE_CHATTER_CONVERSATION_CELL_IDENTIFIER;
}

#pragma mark - Lifecycle


@end
