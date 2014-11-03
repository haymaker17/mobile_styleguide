//
//  RoomsListCellData.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/6/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractTableViewCellData.h"
#import "CTEHotelRate.h"

@interface RoomsListCellData : AbstractTableViewCellData

-(instancetype)initWithHoteRateData:(CTEHotelRate *)ratesData;
-(CTEHotelRate *)getHotelRatesData;

@end
