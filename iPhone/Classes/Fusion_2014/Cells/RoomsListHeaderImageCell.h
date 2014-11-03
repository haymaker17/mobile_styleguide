//
//  Fusion14HotelRoomDetailsTableViewHeaderCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 4/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RoomsListHeaderImageCellData.h"

@interface RoomsListHeaderImageCell : UITableViewCell <ImageDownloaderOperationDelegate>

//
@property (weak, nonatomic) IBOutlet UIImageView *largeHotelImage;

-(void)setCellData:(RoomsListHeaderImageCellData *)cellData;

@end
