//
//  RoomsListHeaderCellData.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractTableViewCellData.h"
#import "CTEHotelCellData.h"

@interface RoomsListHeaderImageCellData : AbstractTableViewCellData

-(instancetype)initWithCTEHotelCellData:(CTEHotelCellData *)cteHotelCellData;
-(CTEHotelCellData *)getHotelData;

@end
