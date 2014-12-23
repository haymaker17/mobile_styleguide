//
//  HotelPhoneCellData.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 07/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelPhoneCellData.h"

@interface HotelPhoneCellData ()
@property (nonatomic, strong) CTEHotelCellData *hotelCellData;
@end

@implementation HotelPhoneCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"hotelDetailPhoneCell";
        self.cellHeight = 50.0;
        _hotelCellData = cteHotelCellData;
    }
    return self;
}

-(CTEHotel*)getCTEHotelData
{
    return [self.hotelCellData getCTEHotel];
}

-(CTEHotelCellData *)getCTEHotelCellData
{
    return self.hotelCellData;
}

@end