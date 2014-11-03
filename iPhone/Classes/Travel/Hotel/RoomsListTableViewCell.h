//
//  RoomsListTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/6/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RoomsListCellData.h"
#import "CTETriangleBadge.h"

@interface RoomsListTableViewCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *labelRoomDescription;
@property (nonatomic, weak) IBOutlet UILabel *labelRoomRate;
@property (nonatomic, weak) IBOutlet UILabel *labelDepositRequired;
@property (nonatomic, weak) IBOutlet UILabel *labelOutOfPolicy;

@property (nonatomic, weak) IBOutlet UILabel *labelPerNight;
@property (nonatomic, weak) IBOutlet CTETriangleBadge *triangleBadge;

-(void)setCellData:(RoomsListCellData *)cellData;

@end
