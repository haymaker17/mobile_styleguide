//
//  HotelPhoneCellData.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 07/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEHotelCellData.h"
#import "AbstractTableViewCellData.h"

@interface HotelPhoneCellData : AbstractTableViewCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData;
-(CTEHotel*)getCTEHotelData;
-(CTEHotelCellData *)getCTEHotelCellData;

@end