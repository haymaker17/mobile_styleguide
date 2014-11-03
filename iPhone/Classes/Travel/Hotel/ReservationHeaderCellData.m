//
//  RoomReservationHeaderCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ReservationHeaderCellData.h"

@interface ReservationHeaderCellData ()
@property (nonatomic, strong) RoomsListCellData *roomsListCellData;
@property (nonatomic, strong) CTEHotelCellData *cteHotelCellData;
@end

@implementation ReservationHeaderCellData

-(instancetype)initWithRoomsListCellData:(RoomsListCellData *)cellData hotelCellData:(CTEHotelCellData *)hotelCellData
{
    self = [super init];
    if (!self) {
        return nil;
    }
    _roomsListCellData = cellData;
    _cteHotelCellData = hotelCellData;
    self.cellIdentifier = @"roomReservationHeaderCell";
    return self;
}

-(instancetype)init
{
    self = [self initWithRoomsListCellData:nil hotelCellData:nil];
    return self;
}

-(RoomsListCellData *)getRoomsListCellData
{
    return self.roomsListCellData;
}

-(CTEHotelCellData *)getCTEHotelCellData
{
    return self.cteHotelCellData;
}

//-(CTEHotelRate *)getHotelRate
//{
//    if (self.roomsListCellData) {
//        CTEHotelRate *hotelRate = [self.roomsListCellData getHotelRate];
//        return hotelRate;
//    }
//    return nil;
//}

@end
