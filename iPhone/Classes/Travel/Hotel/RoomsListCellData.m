//
//  RoomsListCellData.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/6/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RoomsListCellData.h"

@interface RoomsListCellData ()
@property (nonatomic, strong) CTEHotelRate *cteHotelRateData;
@end

@implementation RoomsListCellData

-(instancetype)initWithHoteRateData:(CTEHotelRate *)ratesData
{
    self = [super init];
    
    if (!self) {
        return nil;
    }
    self.cellIdentifier = @"RoomsListTableViewCell";
    self.cellHeight = 97.0;
    _cteHotelRateData = ratesData;
    
    return self;
}

-(instancetype)init
{
   self = [self initWithHoteRateData:nil];
    return self;
}

-(CTEHotelRate *)getHotelRatesData
{
    return self.cteHotelRateData;
}

@end
