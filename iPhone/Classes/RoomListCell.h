//
//  RoomListCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "VerticallyAlignedLabel.h"
//#import "RoomListViewController.h"
@class RoomListViewController;

@interface RoomListCell : UITableViewCell
{
	RoomListViewController	*__weak parentMVC;
	VerticallyAlignedLabel	*rate;
	UILabel					*summary;
	UIButton				*reserveButton;
	int						roomIndex;
    UILabel                 *lblHeading, *lblSub1, *lblSub2;
    UIImageView             *ivException;
}

@property (nonatomic, strong) IBOutlet UIImageView             *ivException;
@property (nonatomic, weak) RoomListViewController	*parentMVC;
@property (nonatomic, strong) IBOutlet VerticallyAlignedLabel			*rate;
@property (nonatomic, strong) IBOutlet UILabel			*summary;
@property (nonatomic, strong) IBOutlet UIButton			*reserveButton;
@property (nonatomic) int								roomIndex;
@property (nonatomic, strong) IBOutlet UILabel          *lblHeading;
@property (nonatomic, strong) IBOutlet UILabel          *lblSub1;
@property (nonatomic, strong) IBOutlet UILabel          *lblSub2;
@property (strong, nonatomic) IBOutlet UILabel          *lblSub3;
@property (strong, nonatomic) IBOutlet UILabel *lblTravelPoints;

extern NSString * const ROOM_LIST_CELL_REUSABLE_IDENTIFIER;

-(NSString*)reuseIdentifier;

-(IBAction)btnReserve:(id)sender;



@end
