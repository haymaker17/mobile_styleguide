//
//  HotelDetailsMapViewCellData.h
//  ConcurMobile
//
//  Created by Sally Yan on 9/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractTableViewCellData.h"
#import "CTEHotelCellData.h"

@interface HotelDetailsMapViewCellData : AbstractTableViewCellData
-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData;
-(CTEHotelCellData *)getCTEHotelCellData;
@end
