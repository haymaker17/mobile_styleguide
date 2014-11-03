//
//  HotelRoomsListDataSource.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractDataSource.h"
#import "RoomsListHeaderImageCellData.h"
#import "CTEError.h"

@interface HotelRoomsListDataSource : AbstractDataSource

@property (copy, nonatomic) void (^onRequestHotelRatesError)(NSString *error);
@property (copy,nonatomic) void(^hideWaitView)();

-(instancetype)initWithHotelCellData:(CTEHotelCellData *)hotelCellData;
-(void)showHotelDetails;
-(void)showRoomList;
-(void)showPhotos;
-(void)updateSegmentsControlCellData:(int)selectedIndex;

@end
