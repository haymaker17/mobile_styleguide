//
//  HotelDetailsMapViewCellData.m
//  ConcurMobile
//
//  Created by Sally Yan on 9/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelDetailsMapViewCellData.h"

@interface HotelDetailsMapViewCellData ()
@property (nonatomic, strong) CTEHotelCellData *hotelCellData;
@end

@implementation HotelDetailsMapViewCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"hotelDetailsMapViewCell";
        self.cellHeight = 75.0;
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
