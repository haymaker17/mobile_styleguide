//
//  LocationSearchCellData.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractTableViewCellData.h"
#import "CTELocation.h"
#import "OfficeLocationResult.h"

@interface LocationSearchCellData : AbstractTableViewCellData

@property BOOL isCurrentLocation;
-(instancetype)initWithCTELocation:(CTELocation *)cteLocation;
-(instancetype)initWithCurrentLocation:(CTELocation *)cteLocation;
-(instancetype)initWithOfficeLocation:(OfficeLocationResult *)officeLocation;
-(CTELocation *)getCTELocation;

@end
