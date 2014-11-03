//
//  RoomsListHeaderCellData.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RoomsListHeaderImageCellData.h"

@interface RoomsListHeaderImageCellData()
@property (nonatomic, strong) CTEHotelCellData *hotelData;
@end

@implementation RoomsListHeaderImageCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData
{
    self = [super init];
    if (!self) {
        return nil;
    }
    self.hotelData = cteHotelCellData;
    self.cellIdentifier = @"RoomsListHeaderImageCell";
    self.cellHeight = 108.0 ;

    return self;
}

-(instancetype)init
{
    self = [self initWithCTEHotelCellData:nil];
    return self;
}

-(CTEHotelCellData *)getHotelData
{
    return self.hotelData;
}

@end
