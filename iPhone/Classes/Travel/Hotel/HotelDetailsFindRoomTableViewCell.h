//
//  HotelDetailsFindRoomTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 9/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HotelDetailsFindRoomTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UIButton *btnFindRoom;
@property (nonatomic, copy) void (^ btnFindRoomPressed)();

@end