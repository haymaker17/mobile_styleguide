//
//  HotelDetailsCellData.h
//  ConcurMobile
//
//  Created by Sally Yan on 9/24/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEHotelCellData.h"
#import "AbstractTableViewCellData.h"

@interface HotelDetailsCellData : AbstractTableViewCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData;
-(CTEHotel*)getCTEHotelData;
-(CTEHotelCellData *)getCTEHotelCellData;

@end
