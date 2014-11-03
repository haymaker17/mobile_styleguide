//
//  RoomListCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RoomListCell.h"
#import "RoomListViewController.h"
#import "VerticallyAlignedLabel.h"

@implementation RoomListCell

@synthesize parentMVC;
@synthesize rate;
@synthesize summary;
@synthesize reserveButton;
@synthesize roomIndex, lblSub1, lblSub2, lblHeading, ivException;

NSString * const ROOM_LIST_CELL_REUSABLE_IDENTIFIER = @"ROOM_LIST_CELL_REUSABLE_IDENTIFIER";

// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return ROOM_LIST_CELL_REUSABLE_IDENTIFIER;
}

-(IBAction)btnReserve:(id)sender
{
	[parentMVC reserveRoomAtIndex:roomIndex];
}

// This override added so that we can set the vertical alignment of the custom label for rate
-(void)didMoveToSuperview
{
    [super didMoveToSuperview];
    // Testing for iOS7. Although iOS6 seems to ignore the statement inside, I don't want to risk any side-effects
    if ([ExSystem is7Plus])
    {
        // We make this call here for iOS7, as iOS6 seems to ignore this
        [rate setVerticalAlignment:VerticalAlignmentBottom];
    }
}
@end
