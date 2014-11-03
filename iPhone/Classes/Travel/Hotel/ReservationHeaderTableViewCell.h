//
//  RoomReservationHeaderTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReservationHeaderCellData.h"
#import "RoomsListCellData.h"

@interface ReservationHeaderTableViewCell : UITableViewCell <ImageDownloaderOperationDelegate>
@property (weak, nonatomic) IBOutlet UIImageView *imageViewRoomImage;
@property (weak, nonatomic) IBOutlet UILabel *roomDescription;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coTopSpaceToRoomDescription;

@end
