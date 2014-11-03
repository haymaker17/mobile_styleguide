//
//  HotelDetailsCellData.m
//  ConcurMobile
//
//  Created by Sally Yan on 9/24/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelDetailsCellData.h"

@interface HotelDetailsCellData ()
@property (nonatomic, strong) CTEHotelCellData *hotelCellData;
@end

@implementation HotelDetailsCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"hotelDetailAddressCell";
        self.cellHeight = 123.0;
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
