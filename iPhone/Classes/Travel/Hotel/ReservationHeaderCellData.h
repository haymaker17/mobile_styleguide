//
//  RoomReservationHeaderCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RoomsListCellData.h"
#import "CTEHotelCellData.h"
#import "CTEHotelRate.h"

@interface ReservationHeaderCellData : AbstractTableViewCellData

-(instancetype)initWithRoomsListCellData:(RoomsListCellData *)cellData hotelCellData:(CTEHotelCellData *)hotelCellData;
-(RoomsListCellData *)getRoomsListCellData;
-(CTEHotelCellData *)getCTEHotelCellData;
//-(CTEHotelRate *)getHotelRate;
@end
