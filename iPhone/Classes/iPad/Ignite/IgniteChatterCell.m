//
//  IgniteChatterCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterCell.h"


@implementation IgniteChatterCell

@synthesize lblName, lblCompanyName, lblAge, lblLikes, lblText, imgView, imgThumb, imgReplyCount, lblReplyCount, btnReplyCount, imgLink, lblLink, imgFile, lblFile, lblTrip, lblTripDate, btnTrip, imgReply;
@synthesize delegate = _delegate;

NSString * const IGNITE_CHATTER_CELL_IDENTIFIER = @"IGNITE_CHATTER_CELL_IDENTIFIER";

+(IgniteChatterCell*)makeCell:(UITableView*)tableView owner:(id)owner withDelegate:(id<IgniteChatterCellDelegate>) del
{
    // MOB-10474 Cell reuse is being disabled because we're finding that when you scroll through
    // the chatter feed really fast, you sometimes see the wrong photos next to people's names. The
    // reason is cell reuse.  By the time the image is fetched from the Salesforce server, the cell
    // for which it was needed is already being used to show someone else's name.  The fetched image
    // is pushed into the UIImageView of the reused cell causing the wrong person's picture to appear.
    // There is not enough time for a more comprehensive fix, so cell reuse is being disabled.
    //
	IgniteChatterCell *cell = nil;
    //
    // TODO: If Ignite becomes productized, then turn cell reuse back on and implement a comprehensive
    // fix to the problem described above.
    //
	//IgniteChatterCell *cell = (IgniteChatterCell *)[tableView dequeueReusableCellWithIdentifier: IGNITE_CHATTER_CELL_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteChatterCell" owner:owner options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[IgniteChatterCell class]])
			{
				cell = (IgniteChatterCell *)oneObject;
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
	return IGNITE_CHATTER_CELL_IDENTIFIER;
}

#pragma mark - Button handlers
- (IBAction)buttonReplyPressed:(id)sender
{
    [self.delegate replyButtonPressedForCell:self];
}

- (IBAction)buttonConversationPressed:(id)sender
{
    [self.delegate conversationButtonPressedForCell:self];
}

- (IBAction)buttonTripPressed:(id)sender
{
    [self.delegate tripButtonPressedForCell:self];
}

#pragma mark - Lifecycle


@end
